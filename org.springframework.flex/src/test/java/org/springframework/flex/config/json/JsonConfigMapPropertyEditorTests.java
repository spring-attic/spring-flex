package org.springframework.flex.config.json;

import java.util.ArrayList;
import java.util.List;

import org.springframework.flex.config.json.JsonConfigMapPropertyEditor;

import flex.messaging.config.ConfigMap;
import junit.framework.TestCase;

public class JsonConfigMapPropertyEditorTests extends TestCase {

	JsonConfigMapPropertyEditor editor = new JsonConfigMapPropertyEditor();
	
	public void testSimpleProperty() {
		
		String props = "{\"foo\":\"bar\"}";
		
		editor.setValue(null);
		editor.setAsText(props);
		
		ConfigMap result = (ConfigMap) editor.getValue();
		assertNotNull(result);
		assertEquals("bar", result.getProperty("foo"));
	}
	
	public void testBooleanProperty() {
		
		String props = "{\"foo\": true}";
		
		editor.setValue(null);
		editor.setAsText(props);
		
		ConfigMap result = (ConfigMap) editor.getValue();
		assertNotNull(result);
		assertEquals(true, result.getPropertyAsBoolean("foo", false));	
	}
	
	public void testNumericProperty() {
		
		String props = "{\"foo\" : 100}";
		
		editor.setValue(null);
		editor.setAsText(props);
		
		ConfigMap result = (ConfigMap) editor.getValue();
		assertNotNull(result);
		assertEquals(100, result.getPropertyAsInt("foo", -1));	
	}
	
	public void testArrayProperty() {
		
		String props = "{ \"foo\" : [ \"bar\", \"baz\", \"boo\" ] }";
		
		editor.setValue(null);
		editor.setAsText(props);
		
		ConfigMap result = (ConfigMap) editor.getValue();
		assertNotNull(result);
		List<String> expected = new ArrayList<String>();
		expected.add("bar");
		expected.add("baz");
		expected.add("boo");
		assertEquals(expected, result.getPropertyAsList("foo", null));
	}
	
	public void testComplexElement() {
		
		String props = "{ \"foo\" : { \"bar\" : \"baz\" } }";
		
		editor.setValue(null);
		editor.setAsText(props);
		
		ConfigMap result = (ConfigMap) editor.getValue();
		assertNotNull(result);
		ConfigMap foo = result.getPropertyAsMap("foo", null);
		assertNotNull(foo);
		assertEquals("baz", foo.getProperty("bar"));
	}
	
	public void testComplexNestedElements() {
		
		String props = "{ \"network\" : { \"throttle-inbound\" : { \"policy\" : \"ERROR\", \"max-frequency\" : 50  }, " +
				"\"throttle-outbound\" : { \"policy\" : \"ERROR\", \"max-frequency\" : 500 }  } }";
		
		editor.setValue(null);
		editor.setAsText(props);
		
		ConfigMap result = (ConfigMap) editor.getValue();
		assertNotNull(result);
		ConfigMap network = result.getPropertyAsMap("network", null);
		assertNotNull(network);
		
		ConfigMap throttleInbound = network.getPropertyAsMap("throttle-inbound", null);
		assertNotNull(throttleInbound);
		assertEquals("ERROR", throttleInbound.getProperty("policy"));
		assertEquals(50, throttleInbound.getPropertyAsInt("max-frequency", -1));
		
		ConfigMap throttleOutbound = network.getPropertyAsMap("throttle-outbound", null);
		assertNotNull(throttleOutbound);
		assertEquals("ERROR", throttleOutbound.getProperty("policy"));
		assertEquals(500, throttleOutbound.getPropertyAsInt("max-frequency", -1));

	}
	
	public void testInvalidJson() {
		
		String props = "foo : bar";
		
		editor.setValue(null);
		
		try {
			editor.setAsText(props);
			fail("IllegalArgumentException expected");
		} catch(IllegalArgumentException ex) {
			//Expected
		}
		
	}
	
	public void testInvalidObjectStructure() {
		
		String props = "\"foo\" : \"bar\"";
		
		editor.setValue(null);
		
		try {
			editor.setAsText(props);
			fail("IllegalArgumentException expected");
		} catch(IllegalArgumentException ex) {
			//Expected
		}
		
	}
	
	public void testMissingPropertyName() {
		
		String props = "{{\"foo\" : \"bar\"}}";
		
		editor.setValue(null);
		
		try {
			editor.setAsText(props);
			fail("IllegalArgumentException expected");
		} catch(IllegalArgumentException ex) {
			//Expected
		}
		
	}
	

		
}
