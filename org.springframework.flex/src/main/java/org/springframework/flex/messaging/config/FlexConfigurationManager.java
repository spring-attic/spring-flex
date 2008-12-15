package org.springframework.flex.messaging.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.servlet.ServletConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.JdkVersion;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import flex.messaging.config.ApacheXPathServerConfigurationParser;
import flex.messaging.config.ConfigurationFileResolver;
import flex.messaging.config.ConfigurationManager;
import flex.messaging.config.ConfigurationParser;
import flex.messaging.config.MessagingConfiguration;

/**
 * Implementation of {@link ConfigurationManager} that uses Spring's {@link ResourceLoader} abstraction for resolving BlazeDS xml configuration files.
 * 
 * @author Jeremy Grelle
 */
public class FlexConfigurationManager implements ConfigurationManager {
	
	private static final Log log = LogFactory.getLog(FlexConfigurationManager.class);

	private static final String DEFAULT_CONFIG_PATH = "/WEB-INF/flex/services-config.xml";
	
	private final ResourceLoader resourceLoader;

	private final String configurationPath;
	
	private ConfigurationParser parser = null;

	public FlexConfigurationManager(ResourceLoader resourceLoader, String configurationPath) {
		this.resourceLoader = resourceLoader;
		this.configurationPath = StringUtils.hasText(configurationPath) ? configurationPath : DEFAULT_CONFIG_PATH;
	}

	/**
	 * Parses the BlazeDS config files and returns a populated MessagingConfiguration
	 * 
	 * @param servletConfig the servlet config for the web application
	 */
	public MessagingConfiguration getMessagingConfiguration(ServletConfig servletConfig) {
		Assert.isTrue(JdkVersion.isAtLeastJava14(),
				"Spring BlazeDS Integration requires a minimum of Java 1.4");
		Assert.notNull(servletConfig, "FlexConfigurationManager requires a non-null ServletConfig - "
				+ "Is it being used outside a WebApplicationContext?");

		MessagingConfiguration configuration = new MessagingConfiguration();

		configuration.getSecuritySettings().setServerInfo(servletConfig.getServletContext().getServerInfo());

		if (parser == null) {
			parser = getDefaultConfigurationParser();
		}

		Assert.notNull(parser, "Unable to create a parser to load Flex messaging configuration.");
		
		parser.parse(configurationPath, new ResourceResolverAdapter(resourceLoader), configuration);

		return configuration;
	}

	public void reportTokens() {
		parser.reportTokens();
	}

	/**
	 * Sets the parser to be used in building a MessagingConfiguration.  Defaults to the BlazeDS Apache Xalan based implementation. 
	 * @param parser the configuration parser to be used
	 */
	public void setConfigurationParser(ConfigurationParser parser) {
		this.parser = parser;
	}
	
	private ConfigurationParser getDefaultConfigurationParser() {
		return new ApacheXPathServerConfigurationParser();
	}

	private static class ResourceResolverAdapter implements ConfigurationFileResolver {

		private Stack<String> configurationPathStack = new Stack<String>();

		private final ResourceLoader resourceLoader;

		public ResourceResolverAdapter(ResourceLoader resourceLoader) {
			this.resourceLoader = resourceLoader;
		}

		public InputStream getConfigurationFile(String path) {
			try {
				Resource resource = resourceLoader.getResource(path);
				if (resource.exists()) {
					pushConfigurationFile(path);
					if (log.isInfoEnabled()) {
						log.info("Loading Flex services configuration from: "+resource.toString());
					}
					return resourceLoader.getResource(path).getInputStream();
				} else {
					throw new IllegalStateException("Flex configuration file does not exist at path: "+path);
				}
				
			} catch (IOException e) {
				throw new IllegalStateException("Flex configuration file could not be loaded from path: "+path);
			}
		}

		public InputStream getIncludedFile(String relativePath) {
			String path = configurationPathStack.peek() + "/" + relativePath;
			try {
				Resource resource = resourceLoader.getResource(path);
				if (resource.exists()) {
					pushConfigurationFile(path);
					if (log.isInfoEnabled()) {
						log.info("Including Flex services configuration from: "+resource.toString());
					}
					return resourceLoader.getResource(path).getInputStream();
				} else {
					throw new IllegalStateException("Included Flex configuration file does not exist at path: "+path);
				}
				
			} catch (IOException e) {
				throw new IllegalStateException("Included Flex configuration file could not be loaded from path: "+path);
			}
		}

		public void popIncludedFile() {
			configurationPathStack.pop();
		}

		private void pushConfigurationFile(String path) {
			String topLevelPath = path.substring(0, path.lastIndexOf("/"));
			configurationPathStack.push(topLevelPath);
		}
	}
}
