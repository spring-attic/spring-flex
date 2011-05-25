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

package org.springframework.flex.config.json;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import org.springframework.util.StringUtils;

import flex.messaging.config.ConfigMap;

/**
 * {@link PropertyEditor} implementation for parsing a JSON structure and converting it to a new instance of
 * {@link ConfigMap}. This is a nice concise alternative to the arbitrary XML structure used in native BlazeDS XML
 * configuration.
 * 
 * @author Jeremy Grelle
 */
public class JsonConfigMapPropertyEditor extends PropertyEditorSupport {

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (!StringUtils.hasText(text)) {
            setValue(new ConfigMap());
            return;
        }
        
        try {
            setValue(new JsonConfigMapParser().parseJsonConfigMap(text));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error occurred while parsing text:\n" + text + "\nas JSON for conversion to "
                + ConfigMap.class.getName(), ex);
        }

    }
}
