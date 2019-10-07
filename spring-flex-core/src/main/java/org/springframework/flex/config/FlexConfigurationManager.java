/*
 * Copyright 2002-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.config;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.servlet.ServletConfig;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import flex.messaging.config.ConfigurationException;
import flex.messaging.config.ConfigurationFileResolver;
import flex.messaging.config.ConfigurationManager;
import flex.messaging.config.ConfigurationParser;
import flex.messaging.config.LoginCommandSettings;
import flex.messaging.config.MessagingConfiguration;
import flex.messaging.config.ServerConfigurationParser;
import flex.messaging.security.LoginCommand;

/**
 * Implementation of {@link ConfigurationManager} that uses Spring's {@link ResourceLoader} abstraction for resolving
 * BlazeDS xml configuration files.
 * 
 * @author Jeremy Grelle
 */
public class FlexConfigurationManager implements ConfigurationManager, ResourceLoaderAware {

    private static final Log log = LogFactory.getLog(FlexConfigurationManager.class);

    public static final String DEFAULT_CONFIG_PATH = "/WEB-INF/flex/services-config.xml";

    private ResourceLoader resourceLoader;

    private String configurationPath;

    private ConfigurationParser parser = null;

    /**
     * Create a new FlexConfigurationManager using the default configuration path.
     */
    public FlexConfigurationManager() {
        this.configurationPath = DEFAULT_CONFIG_PATH;
    }

    /**
     * Create a new FlexConfigurationManager with the given {@link ResourceLoader} and configuration path.
     * 
     * @param resourceLoader the {@link ResourceLoader} to be used in loading the BlazeDS config files.
     * @param configurationPath the path to the top-level BlazeDS config file (usually services-config.xml)
     */
    public FlexConfigurationManager(ResourceLoader resourceLoader, String configurationPath) {
        this.resourceLoader = resourceLoader;
        this.configurationPath = StringUtils.hasText(configurationPath) ? configurationPath : DEFAULT_CONFIG_PATH;
    }

    /**
     * Parses the BlazeDS config files and returns a populated MessagingConfiguration
     * 
     * @param servletConfig the servlet config for the web application
     */
    @SuppressWarnings("unchecked")
    public MessagingConfiguration getMessagingConfiguration(ServletConfig servletConfig) {
    	String[] javaVersionElements = System.getProperty("java.version").split("\\.");
		int major = Integer.parseInt(javaVersionElements[1]);
		
        Assert.isTrue(major >= 5, "Spring BlazeDS Integration requires a minimum of Java 1.5");

        Assert.notNull(servletConfig, "FlexConfigurationManager requires a non-null ServletConfig - "
            + "Is it being used outside a WebApplicationContext?");

        MessagingConfiguration configuration = new MessagingConfiguration();

        configuration.getSecuritySettings().setServerInfo(servletConfig.getServletContext().getServerInfo());

        if (CollectionUtils.isEmpty(configuration.getSecuritySettings().getLoginCommands())) {
            LoginCommandSettings settings = new LoginCommandSettings();
            settings.setClassName(NoOpLoginCommand.class.getName());
            configuration.getSecuritySettings().getLoginCommands().put(LoginCommandSettings.SERVER_MATCH_OVERRIDE, settings);
        }

        if (this.parser == null) {
            this.parser = getDefaultConfigurationParser();
        }

        Assert.notNull(this.parser, "Unable to create a parser to load Flex messaging configuration.");

        this.parser.parse(this.configurationPath, new ResourceResolverAdapter(this.resourceLoader), configuration);

        return configuration;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void reportTokens() {
        this.parser.reportTokens();
    }

    /**
     * Sets the parser to be used in building a MessagingConfiguration. Defaults to a JAXP 1.3+ XPath based
     * implementation.
     * 
     * @param parser the configuration parser to be used
     */
    public void setConfigurationParser(ConfigurationParser parser) {
        this.parser = parser;
    }

    /**
     * Set the path for the BlazeDS XML configuration file.
     * 
     * @param configurationPath the path for the configuration file
     */
    public void setConfigurationPath(String configurationPath) {
        this.configurationPath = configurationPath;
    }

    /**
     * Set the {@link ResourceLoader} to be used to load BlazeDS XML configuration resources
     * 
     * @param resourceLoader the {@link ResourceLoader} for loading configuration resources
     */
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private ConfigurationParser getDefaultConfigurationParser() {
        return new CachingXPathServerConfigurationParser();
    }

    /**
     * This LoginCommand implementation serves as a temporary placeholder in the case where none is defined in
     * services-config.xml and a Spring-configured LoginCommand may be injected later. This serves to eliminate a
     * potentially confusing warning message that would otherwise be logged by BlazeDS.
     */
    public static class NoOpLoginCommand implements LoginCommand {

        public void start(ServletConfig config) {

        }

        public void stop() {

        }

        public Principal doAuthentication(String username, Object credentials) {
            return null;
        }

        @SuppressWarnings("rawtypes")
        public boolean doAuthorization(Principal principal, List roles) {
            return false;
        }

        public boolean logout(Principal principal) {
            return false;
        }
    }

    /**
     * Implementation of {@link ConfigurationFileResolver} that uses a Spring {@link ResourceLoader} to load the BlazeDS
     * configuration files.
     * 
     */
    private static class ResourceResolverAdapter implements ConfigurationFileResolver {

        private final Stack<Resource> configurationPathStack = new Stack<Resource>();

        private final ResourceLoader resourceLoader;

        public ResourceResolverAdapter(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        public InputStream getConfigurationFile(String path) {
            try {
                Resource resource;
                if (this.resourceLoader instanceof ResourcePatternResolver) {
                    ResourcePatternResolver resolver = (ResourcePatternResolver) this.resourceLoader;
                    Resource[] resources = resolver.getResources(path);
                    Assert.notEmpty(resources, "Flex configuration file could not be resolved using pattern: " + path);
                    Assert.isTrue(resources.length == 1,
                        "Invalid pattern used for flex configuration file.  More than one resource resolved using pattern: " + path);
                    resource = resources[0];
                } else {
                    resource = this.resourceLoader.getResource(path);
                }
                Assert.isTrue(resource.exists(), "Flex configuration file does not exist at path: " + path);
                pushConfigurationFile(resource);
                if (log.isInfoEnabled()) {
                    log.info("Loading Flex services configuration from: " + resource.toString());
                }
                return resource.getInputStream();
            } catch (IOException e) {
                throw new IllegalStateException("Flex configuration file could not be loaded from path: " + path);
            }
        }

        public InputStream getIncludedFile(String relativePath) {
            Resource parent = this.configurationPathStack.peek();
            try {
                Resource resource = parent.createRelative(relativePath);
                if (resource.exists()) {
                    pushConfigurationFile(resource);
                    if (log.isInfoEnabled()) {
                        log.info("Including Flex services configuration from: " + resource.toString());
                    }
                    return resource.getInputStream();
                } else {
                    throw new IllegalStateException("Included Flex configuration file does not exist at relative path: " + relativePath);
                }

            } catch (IOException e) {
                throw new IllegalStateException("Included Flex configuration file could not be loaded from path: " + relativePath);
            }
        }

        public void popIncludedFile() {
            this.configurationPathStack.pop();
        }

        private void pushConfigurationFile(Resource configFile) {
            this.configurationPathStack.push(configFile);
        }

        public List<String> getFiles(String dir) {
            Resource parent = this.configurationPathStack.peek();
            String dirPath = StringUtils.cleanPath(dir);
            dirPath += dirPath.endsWith("/") ? "" : "/";
            try {
                Resource dirResource = parent.createRelative(dirPath);
                Assert.isTrue(dirResource.exists(), "Flex configuration files could not be loaded from directory: " + dirPath);

                Assert.isInstanceOf(ResourcePatternResolver.class, this.resourceLoader,
                    "Cannot locate incuded resources using the current ResourceLoader - must be an instance of ResourcePatternResolver.");
                ResourcePatternResolver resolver = (ResourcePatternResolver) this.resourceLoader;
                Resource[] resources = resolver.getResources(dirResource.getURL().toString() + "*.xml");
                List<String> result = new ArrayList<String>();
                for (Resource resource : resources) {
                    Resource parentDir = parent.createRelative("/");
                    result.add(resource.getURL().toString().substring(parentDir.getURL().toString().length()));
                }
                return result;
            } catch (IOException e) {
                throw new IllegalStateException("Flex configuration files could not be loaded from directory: " + dirPath);
            }
        }
    }

    private static class CachingXPathServerConfigurationParser extends ServerConfigurationParser {

        private XPath xpath = null;

        private Map<String, XPathExpression> exprCache = null;

        @Override
        protected void initializeExpressionQuery() {
            if (this.xpath == null) {
                this.xpath = XPathFactory.newInstance().newXPath();
            } else {
                this.xpath.reset();
            }
            this.exprCache = new HashMap<String, XPathExpression>();
        }

        @Override
        protected Object evaluateExpression(Node source, String expression) {
            try {
                return getXPathExpression(expression).evaluate(source, XPathConstants.STRING);
            } catch (XPathExpressionException expressionException) {
                throw wrapException(expressionException);
            }
        }

        @Override
        protected NodeList selectNodeList(Node source, String expression) {
            try {
                return (NodeList) getXPathExpression(expression).evaluate(source, XPathConstants.NODESET);
            } catch (XPathExpressionException expressionException) {
                throw wrapException(expressionException);
            }
        }

        @Override
        protected Node selectSingleNode(Node source, String expression) {
            try {
                return (Node) getXPathExpression(expression).evaluate(source, XPathConstants.NODE);
            } catch (XPathExpressionException expressionException) {
                throw wrapException(expressionException);
            }
        }

        private XPathExpression getXPathExpression(String expression) {
            try {
                XPathExpression compiledExpression = exprCache.get(expression);
                if (compiledExpression == null) {
                    compiledExpression = xpath.compile(expression);
                    exprCache.put(expression, compiledExpression);
                }
                return compiledExpression;
            } catch (XPathExpressionException ex) {
                throw wrapException(ex);
            }
        }

        private ConfigurationException wrapException(XPathException exception) {
            ConfigurationException result = new ConfigurationException();
            result.setDetails(PARSER_INTERNAL_ERROR);
            result.setRootCause(exception);
            return result;
        }
    }
}
