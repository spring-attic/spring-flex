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

package org.springframework.flex.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.flex.remoting.RemotingDestinationExporter;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Configures a {@link RemotingDestinationExporter} from a top-level <code>remote-service</code> tag.
 * 
 * @author Jeremy Grelle
 */
public class RemotingDestinationBeanDefinitionParser extends RemotingDestinationExporterBeanDefinitionFactory implements BeanDefinitionParser {

    /**
     * 
     * {@inheritDoc}
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        return parseInternal(element, parserContext, element.getAttribute(REF_ATTR)).getBeanDefinition();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    protected void validateRemotingDestination(Element element, ParserContext parserContext) {
        if (!StringUtils.hasText(element.getAttribute(REF_ATTR))) {
            parserContext.getReaderContext().error("A bean reference is required when using remoting-destination as a top-level tag.", element);
        }
    }
}
