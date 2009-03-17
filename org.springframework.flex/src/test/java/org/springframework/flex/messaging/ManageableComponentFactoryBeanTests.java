package org.springframework.flex.messaging;

import flex.management.ManageableComponent;
import flex.messaging.config.ConfigMap;
import junit.framework.TestCase;

public class ManageableComponentFactoryBeanTests extends TestCase {

	private ManageableComponentFactoryBean factoryBean;
	
	public void testComponentCreationAndInitialization() throws Exception {
		factoryBean = new ManageableComponentFactoryBean(CustomManageableComponent.class);
		ManageableComponent component = (ManageableComponent) factoryBean.getObject();
		assertNotNull(component);
		assertTrue(((CustomManageableComponent)component).initialized);
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
