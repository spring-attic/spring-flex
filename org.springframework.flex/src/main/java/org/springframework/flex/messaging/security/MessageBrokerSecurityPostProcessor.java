package org.springframework.flex.messaging.security;

import java.util.Iterator;
import java.util.List;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import flex.messaging.MessageBroker;
import flex.messaging.endpoints.Endpoint;

public class MessageBrokerSecurityPostProcessor implements BeanPostProcessor, InitializingBean, BeanFactoryAware, BeanClassLoaderAware {

	private EndpointSecurityAdvisor[] advisors;
	private String[] advisorNames;
	private BeanFactory beanFactory;
	private ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();
	
	public MessageBrokerSecurityPostProcessor(String[] advisorNames) {
		Assert.notEmpty(advisorNames, "A non-empty array of advisor bean names is required");
		this.advisorNames = advisorNames;
	}
	
	public MessageBrokerSecurityPostProcessor(List<EndpointSecurityAdvisor> advisors) {
		Assert.notEmpty(advisors, "A non-empty list of EndpointServiceMessagePointcutAdvisors is required");
		this.advisors = advisors.toArray(new EndpointSecurityAdvisor[advisors.size()]);
	}

	@SuppressWarnings("unchecked")
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		if (!(bean instanceof MessageBroker)) {
			return bean;
		}
		MessageBroker broker = (MessageBroker) bean;
		Iterator i = broker.getEndpoints().keySet().iterator(); 
		while (i.hasNext()) {
			String key = (String) i.next();
			Endpoint endpoint = (Endpoint) broker.getEndpoints().get(key);
			ProxyFactory factory = new ProxyFactory();
			factory.setProxyTargetClass(true);
			factory.addAllAdvisors(advisors);
			factory.setTarget(endpoint);
			factory.setFrozen(true);
			broker.getEndpoints().put(key, factory.getProxy(proxyClassLoader));
		}		
		return bean;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
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

}
