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

package org.springframework.flex.security3;

import java.util.Collection;
import java.util.List;

import javax.servlet.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.flex.config.BeanIds;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.util.UrlMatcher;
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
public class SessionFixationProtectionPostProcessor implements BeanFactoryPostProcessor, ApplicationEventPublisherAware,
    ApplicationListener<ContextRefreshedEvent> {

    private static final Log log = LogFactory.getLog(SessionFixationProtectionPostProcessor.class);

    private boolean processed = false;

    private ApplicationEventPublisher applicationEventPublisher = null;

    private final String filterChainProxyId = "org.springframework.security.filterChainProxy";

    /**
     * 
     * {@inheritDoc}
     */
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (beanFactory.getBeanNamesForType(SpringSecurityLoginCommand.class).length > 0
            && BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, SessionFixationProtectionStrategy.class).length > 0) {

            beanFactory.registerSingleton(BeanIds.FLEX_SESSION_AUTHENTICATION_LISTENER, new FlexSessionInvalidatingAuthenticationListener());
            beanFactory.registerSingleton(BeanIds.REQUEST_CONTEXT_FILTER, new PriorityOrderedRequestContextFilter());
        } else {
            this.processed = true;
        }
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!this.processed) {
            Collection<UsernamePasswordAuthenticationFilter> authFilterBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                event.getApplicationContext(), UsernamePasswordAuthenticationFilter.class, false, false).values();
            for (UsernamePasswordAuthenticationFilter filter : authFilterBeans) {
                filter.setApplicationEventPublisher(this.applicationEventPublisher);
            }

            if (event.getApplicationContext().containsBean(filterChainProxyId)) {
                FilterChainProxy proxy = (FilterChainProxy) event.getApplicationContext().getBean(filterChainProxyId, FilterChainProxy.class);
                UrlMatcher matcher = proxy.getMatcher();
                Object compiledPath = matcher.compile(matcher.getUniversalMatchPattern());
                if (proxy.getFilterChainMap().containsKey(compiledPath)) {
                    proxy.getFilterChainMap().get(compiledPath).add(0, (Filter) event.getApplicationContext().getBean(BeanIds.REQUEST_CONTEXT_FILTER));
                }
                for (List<Filter> filters : proxy.getFilterChainMap().values()) {
                    for (Filter filter : filters) {
                        if (filter instanceof UsernamePasswordAuthenticationFilter) {
                            ((UsernamePasswordAuthenticationFilter) filter).setApplicationEventPublisher(this.applicationEventPublisher);
                        }
                    }
                }
                this.processed = true;
            } else {
                log.warn("Spring Security filter chain could not be auto-detected.  You must install the RequestContextFilter or RequestContextListener"
                    + "manually in order for the flex session fixation protection integration to function as expected.");
                return;
            }
        }
    }

    /**
     * Filter to ensure the request context gets stored before the Spring Security filter chain is invoked so that the
     * {@link FlexSessionInvalidatingAuthenticationListener} has access to the request attributes.
     */
    public static final class PriorityOrderedRequestContextFilter extends RequestContextFilter implements Ordered {

        private static final int order = Integer.MIN_VALUE;

        public int getOrder() {
            return order;
        }
    }

}
