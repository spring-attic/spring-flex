/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.flex.config.json;

import java.util.ArrayList;
import java.util.List;

import flex.messaging.config.ConfigMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;

public class JsonConfigMapPropertyEditorTests {

    JsonConfigMapPropertyEditor editor = new JsonConfigMapPropertyEditor();

    @Test
    public void arrayProperty() {

        String props = "{ \"foo\" : [ \"bar\", \"baz\", \"boo\" ] }";

        this.editor.setValue(null);
        this.editor.setAsText(props);

        ConfigMap result = (ConfigMap) this.editor.getValue();
        assertNotNull(result);
        List<String> expected = new ArrayList<String>();
        expected.add("bar");
        expected.add("baz");
        expected.add("boo");
        assertEquals(expected, result.getPropertyAsList("foo", null));
    }

    @Test
    public void booleanProperty() {

        String props = "{\"foo\": true}";

        this.editor.setValue(null);
        this.editor.setAsText(props);

        ConfigMap result = (ConfigMap) this.editor.getValue();
        assertNotNull(result);
        assertEquals(true, result.getPropertyAsBoolean("foo", false));
    }

    @Test
    public void complexElement() {

        String props = "{ \"foo\" : { \"bar\" : \"baz\" } }";

        this.editor.setValue(null);
        this.editor.setAsText(props);

        ConfigMap result = (ConfigMap) this.editor.getValue();
        assertNotNull(result);
        ConfigMap foo = result.getPropertyAsMap("foo", null);
        assertNotNull(foo);
        assertEquals("baz", foo.getProperty("bar"));
    }

    @Test
    public void complexNestedElements() {

        String props = "{ \"network\" : { \"throttle-inbound\" : { \"policy\" : \"ERROR\", \"max-frequency\" : 50  }, "
            + "\"throttle-outbound\" : { \"policy\" : \"ERROR\", \"max-frequency\" : 500 }  } }";

        this.editor.setValue(null);
        this.editor.setAsText(props);

        ConfigMap result = (ConfigMap) this.editor.getValue();
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

    @Test
    public void invalidJson() {

        String props = "foo : bar";

        this.editor.setValue(null);

        try {
            this.editor.setAsText(props);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            // Expected
        }

    }

    @Test
    public void invalidObjectStructure() {

        String props = "\"foo\" : \"bar\"";

        this.editor.setValue(null);

        try {
            this.editor.setAsText(props);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            // Expected
        }

    }

    @Test
    public void missingPropertyName() {

        String props = "{{\"foo\" : \"bar\"}}";

        this.editor.setValue(null);

        try {
            this.editor.setAsText(props);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            // Expected
        }

    }

    @Test
    public void numericProperty() {

        String props = "{\"foo\" : 100}";

        this.editor.setValue(null);
        this.editor.setAsText(props);

        ConfigMap result = (ConfigMap) this.editor.getValue();
        assertNotNull(result);
        assertEquals(100, result.getPropertyAsInt("foo", -1));
    }

    @Test
    public void simpleProperty() {

        String props = "{\"foo\":\"bar\"}";

        this.editor.setValue(null);
        this.editor.setAsText(props);

        ConfigMap result = (ConfigMap) this.editor.getValue();
        assertNotNull(result);
        assertEquals("bar", result.getProperty("foo"));
    }

}
