/*
 * Copyright 2002-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.security3;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.flex.core.ExceptionTranslator;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import flex.messaging.FlexSession;

/**
 * Infrastructure class for setting up the necessary integration points with Spring Security.
 * 
 * <p>Given existing Spring Security configuration for implementations of {@link SessionAuthenticationStrategy}, {@link RememberMeServices}, 
 * will configure the {@link SpringSecurityLoginCommand} and {@link FilterChainProxy} to work with those services as necessary.  An existing 
 * <code>SessionAuthentiactionStrategy</code> will be wrapped with a {@link FlexSessionAwareSessionAuthenticationStrategy} in order to ensure 
 * proper handling of the {@link FlexSession}.
 * 
 * <p>This class will be configured automatically through use of the XML namespace tags.
 *
 * @author Jeremy Grelle
 */
public class SecurityConfigurationPostProcessor implements MergedBeanDefinitionPostProcessor, InitializingBean, ApplicationContextAware {

	private SessionAuthenticationStrategy sessionAuthenticationStrategy;
	
	private RememberMeServices rememberMeServices;
	
	private FilterChainProxy filterChainProxy;
	
	private ApplicationContext context;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class beanType, String beanName) {
		if (SpringSecurityLoginCommand.class.isAssignableFrom(beanType)) {
			MutablePropertyValues pv = beanDefinition.getPropertyValues();
			boolean rememberMeServicesConfigured = (pv.getPropertyValue("rememberMeServices") != null);
			if (this.sessionAuthenticationStrategy != null && pv.getPropertyValue("sessionAuthenticationStrategy") == null) {
				pv.add("sessionAuthenticationStrategy", this.sessionAuthenticationStrategy);
			}
			if (this.rememberMeServices != null && !rememberMeServicesConfigured) {
				pv.add("rememberMeServices", this.rememberMeServices);
			}
			if (pv.getPropertyValue("logoutHandlers") == null) {
				ManagedList handlers = new ManagedList();
				SecurityContextLogoutHandler contextHandler = new SecurityContextLogoutHandler();
				boolean invalidateHttpSession = (Boolean) beanDefinition.getAttribute("invalidateHttpSession");
				contextHandler.setInvalidateHttpSession(invalidateHttpSession);
				handlers.add(contextHandler);
				if (this.rememberMeServices != null && !rememberMeServicesConfigured && ClassUtils.isAssignableValue(LogoutHandler.class, this.rememberMeServices)) {
					handlers.add(this.rememberMeServices);
				}
				pv.add("logoutHandlers", handlers);
			}
		}
	}
	
	public void afterPropertiesSet() throws Exception {
		if (this.sessionAuthenticationStrategy != null) {
			this.sessionAuthenticationStrategy = new FlexSessionAwareSessionAuthenticationStrategy(this.sessionAuthenticationStrategy);
			
			Set<Filter> allFilters = new HashSet<Filter>(BeanFactoryUtils.beansOfTypeIncludingAncestors(context, Filter.class, false, false).values());
	        if (this.filterChainProxy != null) {
	            allFilters.addAll(new FilterChainAccessor(this.filterChainProxy).getFilters());
	        }    
	        for (Filter filter : allFilters) {
	        	BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(filter);
	        	for (PropertyDescriptor pd : bw.getPropertyDescriptors()) {
	        		if (ClassUtils.isAssignableValue(pd.getPropertyType(), this.sessionAuthenticationStrategy) && bw.isWritableProperty(pd.getName())) {
	        			bw.setPropertyValue(pd.getName(), this.sessionAuthenticationStrategy);
	        		}
	        	}
	        }
		}
		
		Map<String, ExceptionTranslator> exceptionTranslators = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, ExceptionTranslator.class);
		if (!CollectionUtils.isEmpty(exceptionTranslators)) {
			Map<String, FlexAuthenticationEntryPoint> entryPoints = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, FlexAuthenticationEntryPoint.class);
			for (FlexAuthenticationEntryPoint entryPoint: entryPoints.values()) {
				if (CollectionUtils.isEmpty(entryPoint.getExceptionTranslators())) {
					entryPoint.setExceptionTranslators(new HashSet<ExceptionTranslator>(exceptionTranslators.values()));
				}
			}
		}
	}

	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;		
	}
	
	public void setFilterChainProxy(FilterChainProxy filterChainProxy) {
		this.filterChainProxy = filterChainProxy;
	}

	public void setRememberMeServices(RememberMeServices rememberMeServices) {
		this.rememberMeServices = rememberMeServices;
	}
	
	public void setSessionAuthenticationStrategy(
			SessionAuthenticationStrategy sessionAuthenticationStrategy) {
		this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
	}
	
	private static final class FilterChainAccessor {
    	
    	private final Set<Filter> filters;
    	
    	public FilterChainAccessor(FilterChainProxy proxy) {    		
    		this.filters = new LinkedHashSet<Filter>();

    		List<SecurityFilterChain> mappings = proxy.getFilterChains();
    		for(SecurityFilterChain entry : mappings) {
    			List<Filter> filters = entry.getFilters();
    			this.filters.addAll(filters);
    		}
    	}
    	
    	public Set<Filter> getFilters() {
    		return this.filters;
    	}
    }
}