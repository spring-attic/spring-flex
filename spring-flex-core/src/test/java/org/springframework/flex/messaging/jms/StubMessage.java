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

package org.springframework.flex.messaging.jms;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author Mark Fisher
 */
public class StubMessage implements Message {

    private volatile String messageId;

    private volatile String correlationId;

    private volatile int priority = DEFAULT_PRIORITY;

    private volatile int deliveryMode = DEFAULT_DELIVERY_MODE;

    private volatile long expiration;

    private volatile long timestamp;

    private volatile boolean redelivered;

    private volatile Destination destination;

    private volatile Destination replyTo;

    private volatile String type;

    private final Map<String, Object> properties = new HashMap<String, Object>();

    /**
     * Subclasses should implement this if needed.
     */
    public void acknowledge() throws JMSException {
        throw new UnsupportedOperationException();
    }

    /**
     * Subclasses should implement this if needed.
     */
    public void clearBody() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void clearProperties() throws JMSException {
        this.properties.clear();
    }

    public boolean getBooleanProperty(String name) throws JMSException {
        Object value = this.properties.get(name);
        if (value != null && Boolean.class.isAssignableFrom(value.getClass())) {
            return ((Boolean) value).booleanValue();
        }
        return false;
    }

    public byte getByteProperty(String name) throws JMSException {
        Object value = this.properties.get(name);
        if (value != null && Byte.class.isAssignableFrom(value.getClass())) {
            return ((Byte) value).byteValue();
        }
        return 0;
    }

    public double getDoubleProperty(String name) throws JMSException {
        Object value = this.properties.get(name);
        if (value != null && Double.class.isAssignableFrom(value.getClass())) {
            return ((Double) value).doubleValue();
        }
        return 0;
    }

    public float getFloatProperty(String name) throws JMSException {
        Object value = this.properties.get(name);
        if (value != null && Float.class.isAssignableFrom(value.getClass())) {
            return ((Float) value).floatValue();
        }
        return 0;
    }

    public int getIntProperty(String name) throws JMSException {
        Object value = this.properties.get(name);
        if (value != null && Integer.class.isAssignableFrom(value.getClass())) {
            return ((Integer) value).intValue();
        }
        return 0;
    }

    public String getJMSCorrelationID() throws JMSException {
        return this.correlationId;
    }

    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        if (this.correlationId != null) {
            return this.correlationId.getBytes();
        }
        return null;
    }

    public int getJMSDeliveryMode() throws JMSException {
        return this.deliveryMode;
    }

    public Destination getJMSDestination() throws JMSException {
        return this.destination;
    }

    public long getJMSExpiration() throws JMSException {
        return this.expiration;
    }

    public String getJMSMessageID() throws JMSException {
        return this.messageId;
    }

    public int getJMSPriority() throws JMSException {
        return this.priority;
    }

    public boolean getJMSRedelivered() throws JMSException {
        return this.redelivered;
    }

    public Destination getJMSReplyTo() throws JMSException {
        return this.replyTo;
    }

    public long getJMSTimestamp() throws JMSException {
        return this.timestamp;
    }

    public String getJMSType() throws JMSException {
        return this.type;
    }

    public long getLongProperty(String name) throws JMSException {
        Object value = this.properties.get(name);
        if (value != null && Long.class.isAssignableFrom(value.getClass())) {
            return ((Long) value).longValue();
        }
        return 0;
    }

    public Object getObjectProperty(String name) throws JMSException {
        return this.properties.get(name);
    }

    public Enumeration<?> getPropertyNames() throws JMSException {
        return Collections.enumeration(this.properties.keySet());
    }

    public short getShortProperty(String name) throws JMSException {
        Object value = this.properties.get(name);
        if (value != null && Short.class.isAssignableFrom(value.getClass())) {
            return ((Short) value).shortValue();
        }
        return 0;
    }

    public String getStringProperty(String name) throws JMSException {
        Object value = this.properties.get(name);
        if (value != null && String.class.isAssignableFrom(value.getClass())) {
            return (String) value;
        }
        return null;
    }

    public boolean propertyExists(String name) throws JMSException {
        return this.properties.containsKey(name);
    }

    public void setBooleanProperty(String name, boolean value) throws JMSException {
        this.properties.put(name, value);
    }

    public void setByteProperty(String name, byte value) throws JMSException {
        this.properties.put(name, value);
    }

    public void setDoubleProperty(String name, double value) throws JMSException {
        this.properties.put(name, value);
    }

    public void setFloatProperty(String name, float value) throws JMSException {
        this.properties.put(name, value);
    }

    public void setIntProperty(String name, int value) throws JMSException {
        this.properties.put(name, value);
    }

    public void setJMSCorrelationID(String correlationID) throws JMSException {
        this.correlationId = correlationID;
    }

    public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {
        this.correlationId = new String(correlationID);
    }

    public void setJMSDeliveryMode(int deliveryMode) throws JMSException {
        this.deliveryMode = deliveryMode;
    }

    public void setJMSDestination(Destination destination) throws JMSException {
        this.destination = destination;
    }

    public void setJMSExpiration(long expiration) throws JMSException {
        this.expiration = expiration;
    }

    public void setJMSMessageID(String id) throws JMSException {
        this.messageId = id;
    }

    public void setJMSPriority(int priority) throws JMSException {
        this.priority = priority;
    }

    public void setJMSRedelivered(boolean redelivered) throws JMSException {
        this.redelivered = redelivered;
    }

    public void setJMSReplyTo(Destination replyTo) throws JMSException {
        this.replyTo = replyTo;
    }

    public void setJMSTimestamp(long timestamp) throws JMSException {
        this.timestamp = timestamp;
    }

    public void setJMSType(String type) throws JMSException {
        this.type = type;
    }

    public void setLongProperty(String name, long value) throws JMSException {
        this.properties.put(name, value);
    }

    public void setObjectProperty(String name, Object value) throws JMSException {
        this.properties.put(name, value);
    }

    public void setShortProperty(String name, short value) throws JMSException {
        this.properties.put(name, value);
    }

    public void setStringProperty(String name, String value) throws JMSException {
        this.properties.put(name, value);
    }

}
