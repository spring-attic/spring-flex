/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.flex.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.flex.security.FlexSessionInvalidatingAuthenticationListener;
import org.springframework.flex.security.SpringSecurityLoginCommand;
import org.springframework.security.ui.FilterChainOrder;
import org.springframework.web.filter.RequestContextFilter;

/**
 * {@link BeanFactoryPostProcessor} implementation that inspects the {@link BeanFactory} for Spring Security
 * configuration settings and installs the {@link FlexSessionInvalidatingAuthenticationListener} if Spring Security is
 * detected and session fixation protection is enabled.
 * 
 * <p>
 * This class will be configured automatically when Spring Security integration is enabled via the xml config namespace.
 * 
 * @author Jeremy Grelle
 */
public class SessionFixationProtectionConfigurer implements BeanFactoryPostProcessor {

    private static final Log log = LogFactory.getLog(SessionFixationProtectionConfigurer.class);

    /**
     * 
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (beanFactory.getBeanNamesForType(SpringSecurityLoginCommand.class).length > 0
            && beanFactory.containsBean(org.springframework.security.config.BeanIds.SESSION_FIXATION_PROTECTION_FILTER)) {

            beanFactory.registerSingleton(BeanIds.FLEX_SESSION_AUTHENTICATION_LISTENER, new FlexSessionInvalidatingAuthenticationListener());

            RootBeanDefinition filterList = null;
            if (beanFactory.containsBean("_filterChainList")) {
                filterList = (RootBeanDefinition) beanFactory.getMergedBeanDefinition("_filterChainList");
            } else {
                log.warn("Spring Security filter chain could not be auto-detected.  You must install the RequestContextFilter or RequestContextListener"
                    + "manually in order for the flex session fixation protection integration to function as expected.");
                return;
            }

            beanFactory.registerSingleton(BeanIds.REQUEST_CONTEXT_FILTER, new PriorityOrderedRequestContextFilter());

            ManagedList filters;
            MutablePropertyValues pvs = filterList.getPropertyValues();
            if (pvs.contains("filters")) {
                filters = (ManagedList) pvs.getPropertyValue("filters").getValue();
            } else {
                filters = new ManagedList();
                pvs.addPropertyValue("filters", filters);
            }

            filters.add(new RuntimeBeanReference(BeanIds.REQUEST_CONTEXT_FILTER));
        }
    }

    /**
     * Filter to ensure the request context gets stored before the Spring Security filter chain is invoked so that the
     * {@link FlexSessionInvalidatingAuthenticationListener} has access to the request attributes.
     */
    public static final class PriorityOrderedRequestContextFilter extends RequestContextFilter implements Ordered {

        private static final int order = FilterChainOrder.getOrder("FIRST");

        public int getOrder() {
            return order;
        }
    }

}
