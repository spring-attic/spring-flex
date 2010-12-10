package org.springframework.flex.http;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.AmfTrace;


public class AmfHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    private static final Log log = LogFactory.getLog(AmfHttpMessageConverter.class);
    
    public AmfHttpMessageConverter() {
        super(new MediaType("application", "x-amf"));
    }
    
    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        AmfMessageDeserializer deserializer = new AmfMessageDeserializer();
        
        AmfTrace trace = null;
        if (log.isDebugEnabled()) {
            trace = new AmfTrace();
        }
        
        deserializer.initialize(new SerializationContext(), inputMessage.getBody(), trace);
        Object result = null;
        try {
            result = deserializer.readObject();
        } catch (ClassNotFoundException e) {
           throw new HttpMessageNotReadableException("Could not read AMF message", e);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Read AMF message:\n" + trace);
        }
        
        return result;
    }

    @Override
    protected void writeInternal(Object t, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        AmfMessageSerializer serializer = new AmfMessageSerializer();

        AmfTrace trace = null;
        if (log.isDebugEnabled()) {
            trace = new AmfTrace();
        }
        
        serializer.initialize(new SerializationContext(), outputMessage.getBody(), trace);
        serializer.writeObject(t);
        
        if (log.isDebugEnabled()) {
            log.debug("Wrote AMF message:\n" + trace);
        }
    }
}
