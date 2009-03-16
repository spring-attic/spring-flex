package org.springframework.flex.messaging;

import flex.management.ManageableComponent;
import flex.messaging.config.ConfigMap;
import junit.framework.TestCase;

public class ManageableComponentFactoryBeanTests extends TestCase {

	private ManageableComponentFactoryBean factoryBean;
	
	public void setUp() throws Exception {
		super.setUp();
	}
	
	public void testComponentCreationAndInitialization() {
		factoryBean = new ManageableComponentFactoryBean(CustomManageableComponent.class);
	}
	
	private class CustomManageableComponent extends ManageableComponent {

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
