package org.springframework.flex.messaging;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import flex.management.ManageableComponent;
import flex.messaging.config.ConfigMap;

/**
 * {@link FactoryBean} that allows for the creation of BlazeDS {@link ManageableComponent} instances
 * with the appropriate {@link ManageableComponent#initialize(String, ConfigMap)} callback after creation.
 * 
 * <p>
 * The properties {@link ConfigMap} is expected to be populated from a JSON string as a concise alternative to
 * the arbitrary XML structure used in native BlazeDS XML configuration.
 * 
 * @author Jeremy Grelle
 */
public class ManageableComponentFactoryBean implements FactoryBean, BeanNameAware, InitializingBean{

	private ConfigMap properties = new ConfigMap();
	
	private String beanName;
	
	private ManageableComponent component;
	
	private Class<? extends ManageableComponent> componentClass;
	
	public ManageableComponentFactoryBean(Class<? extends ManageableComponent> componentClass) {
		this.componentClass = componentClass;
	}
	
	public Object getObject() throws Exception {
		return component;
	}
	
	public void afterPropertiesSet() throws Exception {
		component = (ManageableComponent) BeanUtils.instantiateClass(componentClass);
		component.initialize(beanName, properties);
	}

	public Class<?> getObjectType() {
		return componentClass;
	}

	/**
	 * It is expected that objects created by this factory are thread-safe and can be
	 * configured as singletons.
	 */
	public final boolean isSingleton() {
		return true;
	}

	public void setBeanName(String name) {
		this.beanName = name;		
	}
}
