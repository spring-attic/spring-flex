package org.springframework.flex.messaging;

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
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import flex.messaging.config.ConfigurationFileResolver;
import flex.messaging.config.ConfigurationManager;
import flex.messaging.config.ConfigurationParser;
import flex.messaging.config.MessagingConfiguration;

public class FlexConfigurationManager implements ConfigurationManager {
	
	private static final Log log = LogFactory.getLog(FlexConfigurationManager.class);

	static final String DEFAULT_CONFIG_PATH = "/WEB-INF/flex/services-config.xml";
	
	private final ResourceLoader resourceLoader;

	protected final String configurationPath;
	
	protected ConfigurationFileResolver configurationResolver = null;
	protected ConfigurationParser parser = null;

	public FlexConfigurationManager(ResourceLoader resourceLoader, String configurationPath) {
		this.resourceLoader = resourceLoader;
		this.configurationPath = StringUtils.hasText(configurationPath) ? configurationPath : DEFAULT_CONFIG_PATH;
	}

	public MessagingConfiguration getMessagingConfiguration(ServletConfig servletConfig) {
		Assert.isTrue(JdkVersion.isAtLeastJava14(),
				"Spring and Flex integration requires a minimum of Java 1.4");
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

	private ConfigurationParser getDefaultConfigurationParser() {
		ConfigurationParser parser = null;
		Class parserClass = null;
		String className = null;

		// Always try Sun JRE 1.4 / Apache Xalan Based Implementation first to
		// avoid performance problems with Sun JRE 1.5 Based Implementation
		if (parser == null) {
			try {
				if (ClassUtils.isPresent("org.apache.xpath.CachedXPathAPI")) {
					className = "flex.messaging.config.ApacheXPathServerConfigurationParser";
					parserClass = ClassUtils.forName(className);
					parser = (ConfigurationParser) parserClass.newInstance();
				}
			} catch (Throwable t) {
				if (log.isWarnEnabled()) {
					log.warn("Could not load Flex configuration parser as: " + className);
				}
			}
		}

		// Try Sun JRE 1.5 Based Implementation
		if (parser == null) {
			try {
				className = "flex.messaging.config.XPathServerConfigurationParser";
				parserClass = ClassUtils.forName(className);

				// double-check, on some systems the above loads but the import classes don't
				ClassUtils.forName("javax.xml.xpath.XPathExpressionException");

				parser = (ConfigurationParser) parserClass.newInstance();
			} catch (Throwable t) {
				if (log.isWarnEnabled()) {
					log.warn("Could not load configuration parser as: " + className);
				}
			}
		}
		
		if (parser != null) {
			if (log.isInfoEnabled()) {
				log.info("Loaded Flex Services Configuration Parser: " + parser.getClass().getName());
			}
		} else {
			throw new IllegalStateException("Could not load a default Flex Services Configuration Parser");
		}

		return parser;
	}

	public void reportTokens() {
		parser.reportTokens();
	}

	public void setConfigurationParser(ConfigurationParser parser) {
		this.parser = parser;
	}

	private static class ResourceResolverAdapter implements ConfigurationFileResolver {

		private Stack configurationPathStack = new Stack();

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
