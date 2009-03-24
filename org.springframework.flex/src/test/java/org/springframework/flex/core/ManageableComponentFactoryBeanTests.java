package org.springframework.flex.core;

import org.springframework.flex.core.ManageableComponentFactoryBean;

import flex.management.ManageableComponent;
import flex.messaging.config.ConfigMap;
import junit.framework.TestCase;

public class ManageableComponentFactoryBeanTests extends TestCase {

	private ManageableComponentFactoryBean factoryBean;
	
	public void testComponentCreationAndInitialization() throws Exception {
		factoryBean = new ManageableComponentFactoryBean(CustomManageableComponent.class);
		factoryBean.setBeanName("my-adapter");
		ManageableComponent component = (ManageableComponent) factoryBean.getObject();
		assertNotNull(component);
		assertTrue(((CustomManageableComponent)component).initialized);
		assertEquals("my-adapter", component.getId());
	}
	
	private static class CustomManageableComponent extends ManageableComponent {

		boolean initialized = false;
		
		public CustomManageableComponent() {
			super(false);
		}

		protected String getLogCategory() {
			return null;
		}

		@Override
		public void initialize(String id, ConfigMap properties) {
			initialized = true;
		}
	}

}
