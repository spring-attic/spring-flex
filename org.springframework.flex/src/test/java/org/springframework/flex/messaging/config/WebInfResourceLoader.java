package org.springframework.flex.messaging.config;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

class WebInfResourceLoader implements ResourceLoader {

	ApplicationContext context;
	
	WebInfResourceLoader(ApplicationContext context) {
		this.context = context;
	}
	
	public ClassLoader getClassLoader() {
		return context.getClassLoader();
	}

	public Resource getResource(String location) {
		if (location.startsWith("/WEB-INF/flex/"))
		{
			location = location.replace("/WEB-INF/flex/", "classpath:org/springframework/flex/messaging/");
		}
		return context.getResource(location);
	}
	
}
