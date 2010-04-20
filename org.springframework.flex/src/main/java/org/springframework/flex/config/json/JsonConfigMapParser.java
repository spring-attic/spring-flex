package org.springframework.flex.config.json;

import java.io.IOException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.springframework.util.Assert;

import flex.messaging.config.ConfigMap;


class JsonConfigMapParser {
    
    private static final JsonFactory FACTORY = new JsonFactory();
    
    ConfigMap parseJsonConfigMap(String text) throws JsonParseException, IOException {
        JsonParser parser = FACTORY.createJsonParser(text);
        Assert.isTrue(parser.nextToken() == JsonToken.START_OBJECT, "Text does not appear to be a proper JSON string.");
        ConfigMap configMap = parseJsonObject(parser);
        parser.close();
        return configMap;
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
}
