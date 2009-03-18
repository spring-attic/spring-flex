package org.springframework.flex.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
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
 * {@link BeanFactoryPostProcessor} implementation that inspects the {@link BeanFactory} for 
 * Spring Security configuration settings and installs the {@link FlexSessionInvalidatingAuthenticationListener}
 * if Spring Security is detected and session fixation protection is enabled. 
 * 
 * <p>
 * This class will be configured automatically when Spring Security integration is enabled via the xml config 
 * namespace.
 * 
 * @author Jeremy Grelle
 */
public class SessionFixationProtectionConfigurer implements
		BeanFactoryPostProcessor {

	private static final Log log = LogFactory.getLog(SessionFixationProtectionConfigurer.class);
	
	@SuppressWarnings("unchecked")
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if(beanFactory.getBeanNamesForType(SpringSecurityLoginCommand.class).length > 0 &&
		   beanFactory.containsBean(org.springframework.security.config.BeanIds.SESSION_FIXATION_PROTECTION_FILTER)) {
			
			beanFactory.registerSingleton(BeanIds.FLEX_SESSION_AUTHENTICATION_LISTENER, 
					new FlexSessionInvalidatingAuthenticationListener());
			
			RootBeanDefinition filterList = (RootBeanDefinition) beanFactory.getBeanDefinition("_filterChainList");
			
			if (filterList == null) {
				log.warn("Spring Security filter chain could not be found.  You must install the RequestContextFilter or RequestContextListener" +
						"manually in order for the flex session fixation protection integration to function as expected.");
				return;
			}
			
			beanFactory.registerSingleton(BeanIds.REQUEST_CONTEXT_FILTER,
					new DefaultOrderedRequestContextFilter());
			
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
	
	public static final class DefaultOrderedRequestContextFilter extends RequestContextFilter implements Ordered{

		private static final int order = FilterChainOrder.getOrder("FIRST");
		
		public int getOrder() {
			return order;
		}
	}

}
