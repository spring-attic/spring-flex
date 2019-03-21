/*
 * Copyright 2002-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.config.xml;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.flex.remoting.RemotingDestinationExporter;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Configures a {@link RemotingDestinationExporter} from a nested <code>remote-service</code> tag.
 * 
 * @author Jeremy Grelle
 */
public class RemotingDestinationBeanDefinitionDecorator extends RemotingDestinationExporterBeanDefinitionFactory implements BeanDefinitionDecorator {

    /**
     * 
     * {@inheritDoc}
     */
    public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
        Element element = (Element) node;
        parseInternal(element, parserContext, definition.getBeanName());
        return definition;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected void validateRemotingDestination(Element element, ParserContext parserContext) {
        if (StringUtils.hasText(element.getAttribute(REF_ATTR))) {
            parserContext.getReaderContext().error("ref attribute not allowed when using remoting-destination as a nested tag.", element);
        }
    }
}
