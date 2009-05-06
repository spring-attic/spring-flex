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

package org.springframework.flex.config.json;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.IOException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.springframework.util.Assert;
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

    private final JsonFactory factory = new JsonFactory();

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

        JsonParser parser = null;
        try {
            parser = this.factory.createJsonParser(text);

            Assert.isTrue(parser.nextToken() == JsonToken.START_OBJECT, "Text does not appear to be a proper JSON string.");

            setValue(parseJsonObject(parser));

            parser.close();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error occurred while parsing text:\n" + text + "\nas JSON for conversion to "
                + ConfigMap.class.getName(), ex);
        }

    }

    private void parseJsonArray(JsonParser parser, ConfigMap map, String propName) throws JsonParseException, IOException {
        JsonToken token = parser.nextToken();
        while (token != JsonToken.END_ARRAY) {
            if (token.isScalarValue()) {
                map.addProperty(propName, parser.getText());
            } else if (token == JsonToken.START_OBJECT) {
                map.addProperty(propName, parseJsonObject(parser));
            } else if (token == JsonToken.START_ARRAY) {
                parseJsonArray(parser, map, propName);
            }
            token = parser.nextToken();
        }
    }

    private ConfigMap parseJsonObject(JsonParser parser) throws JsonParseException, IOException {

        ConfigMap map = new ConfigMap();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String propName = parser.getCurrentName();
            if (propName != null) {
                JsonToken token = parser.nextToken();
                if (token.isScalarValue()) {
                    map.addProperty(propName, parser.getText());
                } else if (token == JsonToken.START_OBJECT) {
                    map.addProperty(propName, parseJsonObject(parser));
                } else if (token == JsonToken.START_ARRAY) {
                    parseJsonArray(parser, map, propName);
                }
            }
        }
        return map;
    }

}
