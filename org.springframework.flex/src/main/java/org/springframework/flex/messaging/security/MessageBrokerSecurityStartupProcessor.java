package org.springframework.flex.messaging.security;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.flex.messaging.MessageBrokerStartupProcessor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import flex.management.BaseControl;
import flex.messaging.MessageBroker;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.SecurityConstraint;
import flex.messaging.endpoints.AMFEndpoint;
import flex.messaging.endpoints.Endpoint;

public class MessageBrokerSecurityStartupProcessor implements MessageBrokerStartupProcessor, InitializingBean, BeanFactoryAware, BeanClassLoaderAware {

	private EndpointSecurityAdvisor[] advisors;
	private String[] advisorNames;
	private BeanFactory beanFactory;
	private ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();
	
	public MessageBrokerSecurityStartupProcessor(String[] advisorNames) {
		Assert.notEmpty(advisorNames, "A non-empty array of advisor bean names is required");
		this.advisorNames = advisorNames;
	}
	
	public MessageBrokerSecurityStartupProcessor(List<EndpointSecurityAdvisor> advisors) {
		Assert.notEmpty(advisors, "A non-empty list of EndpointServiceMessagePointcutAdvisors is required");
		this.advisors = advisors.toArray(new EndpointSecurityAdvisor[advisors.size()]);
	}
	
	public MessageBroker processAfterStartup(MessageBroker broker) {
		return broker;
	}

	@SuppressWarnings("unchecked")
	public MessageBroker processBeforeStartup(MessageBroker broker) {
		Iterator i = broker.getEndpoints().keySet().iterator(); 
		while (i.hasNext()) {
			String key = (String) i.next();
			Endpoint endpoint = (Endpoint) broker.getEndpoints().get(key);
			ProxyFactory factory = new ProxyFactory();
			factory.setProxyTargetClass(true);
			factory.addAllAdvisors(advisors);
			factory.setTarget(endpoint);
			factory.setFrozen(true);
			factory.setExposeProxy(true);
			Object proxy = factory.getProxy(proxyClassLoader);
			if (proxy instanceof AMFEndpoint) {
				proxy = new ProxiedEndpointDelegate((AMFEndpoint)proxy);
			}
			broker.getEndpoints().put(key, proxy);
		}		
		return broker;
	}

	public void afterPropertiesSet() throws Exception {
		if (advisors == null || advisors.length == 0) {
			advisors = new EndpointSecurityAdvisor[advisorNames.length];
			for(int i = 0; i<advisorNames.length; i++) {
				String advisorName = advisorNames[i];
				EndpointSecurityAdvisor advisor = (EndpointSecurityAdvisor) beanFactory.getBean(advisorName, EndpointSecurityAdvisor.class);
				advisors[i] = advisor;
			}
		}		
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;		
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		proxyClassLoader = classLoader;		
	}
	
	private class ProxiedEndpointDelegate implements Endpoint{

		private Endpoint delegate;
		
		public ProxiedEndpointDelegate(Endpoint delegate) {
			this.delegate = delegate;
		}

		public ConfigMap describeEndpoint() {
			return delegate.describeEndpoint();
		}

		public String getClientType() {
			return delegate.getClientType();
		}

		public BaseControl getControl() {
			return delegate.getControl();
		}

		public String getId() {
			return delegate.getId();
		}

		public MessageBroker getMessageBroker() {
			return delegate.getMessageBroker();
		}

		public double getMessagingVersion() {
			return delegate.getMessagingVersion();
		}

		public String getParsedUrl(String contextPath) {
			return delegate.getParsedUrl(contextPath);
		}

		public int getPort() {
			return delegate.getPort();
		}

		public SecurityConstraint getSecurityConstraint() {
			return delegate.getSecurityConstraint();
		}

		public String getUrl() {
			return delegate.getUrl();
		}

		public String getUrlForClient() {
			return delegate.getUrlForClient();
		}

		public void initialize(String id, ConfigMap properties) {
			delegate.initialize(id, properties);
		}

		public boolean isManaged() {
			return delegate.isManaged();
		}

		public boolean isSecure() {
			return delegate.isSecure();
		}

		public boolean isStarted() {
			return delegate.isStarted();
		}

		public void service(HttpServletRequest req, HttpServletResponse res) {
			delegate.service(req, res);
		}

		public void setClientType(String clientType) {
			delegate.setClientType(clientType);
		}

		public void setControl(BaseControl control) {
			delegate.setControl(control);
		}

		public void setId(String id) {
			delegate.setId(id);
		}

		public void setManaged(boolean enableManagement) {
			delegate.setManaged(enableManagement);
		}

		public void setMessageBroker(MessageBroker broker) {
			delegate.setMessageBroker(broker);
		}

		public void setSecurityConstraint(SecurityConstraint constraint) {
			delegate.setSecurityConstraint(constraint);
		}

		public void setUrl(String url) {
			delegate.setUrl(url);
		}

		public void start() {
			delegate.start();
		}

		public void stop() {
			delegate.stop();
		}

		
		
		
	}
}
