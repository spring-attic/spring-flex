/*
 * Copyright 2002-2009 the original author or authors.
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
import java.util.Stack;

import javax.servlet.ServletConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ResourceLoaderAware;
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
    public MessagingConfiguration getMessagingConfiguration(ServletConfig servletConfig) {
        Assert.isTrue(JdkVersion.isAtLeastJava15(), "Spring BlazeDS Integration requires a minimum of Java 1.5");
        Assert.notNull(servletConfig, "FlexConfigurationManager requires a non-null ServletConfig - "
            + "Is it being used outside a WebApplicationContext?");

        MessagingConfiguration configuration = new MessagingConfiguration();

        configuration.getSecuritySettings().setServerInfo(servletConfig.getServletContext().getServerInfo());

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
     * Sets the parser to be used in building a MessagingConfiguration. Defaults to the BlazeDS Apache Xalan based
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
        return new ApacheXPathServerConfigurationParser();
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
                Resource resource = this.resourceLoader.getResource(path);
                if (resource.exists()) {
                    pushConfigurationFile(resource);
                    if (log.isInfoEnabled()) {
                        log.info("Loading Flex services configuration from: " + resource.toString());
                    }
                    return resource.getInputStream();
                } else {
                    throw new IllegalStateException("Flex configuration file does not exist at path: " + path);
                }

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
    }
}
