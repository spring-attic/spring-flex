package org.springframework.flex.core;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;

import flex.management.ManageableComponent;
import flex.messaging.config.ConfigMap;

/**
 * {@link FactoryBean} that allows for the creation of BlazeDS {@link ManageableComponent} prototype instances
 * with the appropriate {@link ManageableComponent#initialize(String, ConfigMap)} callback after creation.  Useful
 * for configuring non-singleton helper objects such as a custom {@link JavaAdapter}.
 * 
 * @author Jeremy Grelle
 */
public class ManageableComponentFactoryBean implements FactoryBean, BeanNameAware{

	private ConfigMap properties = new ConfigMap();
	
	private String beanName;
	
	private Class<? extends ManageableComponent> componentClass;
	
	public ManageableComponentFactoryBean(Class<? extends ManageableComponent> componentClass) {
		this.componentClass = componentClass;
	}
	
	public Object getObject() throws Exception {
		ManageableComponent component = (ManageableComponent) BeanUtils.instantiateClass(componentClass);
		component.setId(beanName);
		component.initialize(beanName, properties);
		return component;
	}

	public Class<?> getObjectType() {
		return componentClass;
	}

	/**
	 * It is expected that objects created by this factory will always
	 * be prototype instances.
	 */
	public final boolean isSingleton() {
		return false;
	}

	public void setBeanName(String name) {
		this.beanName = name;		
	}
	
	public void setProperties(ConfigMap properties) {
		this.properties = properties;
	}

}
