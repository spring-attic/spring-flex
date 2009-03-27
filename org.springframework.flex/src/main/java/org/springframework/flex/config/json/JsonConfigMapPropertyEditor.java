package org.springframework.flex.config.json;

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
 * {@link PropertyEditor} implementation for parsing a JSON structure and converting
 * it to a new instance of {@link ConfigMap}.  This is a nice concise alternative to
 * the arbitrary XML structure used in native BlazeDS XML configuration.  
 *   
 * @author Jeremy Grelle
 */
public class JsonConfigMapPropertyEditor extends PropertyEditorSupport {

	private JsonFactory factory = new JsonFactory();
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (!StringUtils.hasText(text)) {
			setValue(new ConfigMap());
			return;
		} 
		
		JsonParser parser = null;
		try {
			parser = factory.createJsonParser(text);
			
			Assert.isTrue(parser.nextToken() == JsonToken.START_OBJECT, 
				"Text does not appear to be a proper JSON string.");
			
			setValue(parseJsonObject(parser));
			
			parser.close();
		} catch (Exception ex) {
			throw new IllegalArgumentException("Error occurred while parsing text:\n"+text+"\nas JSON for conversion to "+ConfigMap.class.getName(), ex);
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
	
	private void parseJsonArray(JsonParser parser, ConfigMap map,
			String propName) throws JsonParseException, IOException {
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
