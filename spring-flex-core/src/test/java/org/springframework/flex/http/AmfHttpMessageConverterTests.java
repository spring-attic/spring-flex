package org.springframework.flex.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.flex.core.io.domain.Person;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.Amf3Input;
import flex.messaging.io.amf.Amf3Output;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.MessageBody;
import static org.junit.Assert.*;

import flex.messaging.validators.ClassDeserializationValidator;
import org.junit.Before;
import org.junit.Test;

public class AmfHttpMessageConverterTests {

    private final MediaType amfContentType = MediaType.parseMediaType(MessageIOConstants.AMF_CONTENT_TYPE);

    private MockHttpServletResponse response;

    private MockHttpServletRequest request;

    private ClassDeserializationValidator deserializationValidator;

    @Before
    public void setUp() throws Exception {
        this.response = new MockHttpServletResponse();
        this.request = new MockHttpServletRequest();
        this.deserializationValidator = new ClassDeserializationValidator();
        this.deserializationValidator.addAllowClassPattern("org.springframework.flex.core.io.domain.Person");
		this.deserializationValidator.addAllowClassPattern("org.springframework.flex.core.io.domain.Address");
    }

    @Test
    public void canRead() {
        AmfHttpMessageConverter converter = new AmfHttpMessageConverter();
        assertTrue(converter.canRead(Object.class, this.amfContentType));
    }

    @Test
    public void canWrite() {
        AmfHttpMessageConverter converter = new AmfHttpMessageConverter();
        assertTrue(converter.canWrite(Object.class, this.amfContentType));
    }

    @Test
    public void writeSimpleString() throws Exception {
        HttpOutputMessage outputMessage = new ServletServerHttpResponse(this.response);
        AmfHttpMessageConverter converter = new AmfHttpMessageConverter();
        converter.write("foo", this.amfContentType, outputMessage);

        Object result = deserializeResponse();

        assertEquals(this.amfContentType, outputMessage.getHeaders().getContentType());
        assertEquals("foo", result);
    }

    @Test
    public void writeObject() throws Exception {
        HttpOutputMessage outputMessage = new ServletServerHttpResponse(this.response);
        AmfHttpMessageConverter converter = new AmfHttpMessageConverter();
        converter.write(Person.stubPerson(), this.amfContentType, outputMessage);

        Object result = deserializeResponse();
        assertEquals(this.amfContentType, outputMessage.getHeaders().getContentType());
        assertTrue(result instanceof Person);
    }

    @Test
    public void writeActionMessage() throws Exception {
        HttpOutputMessage outputMessage = new ServletServerHttpResponse(this.response);
        AmfHttpMessageConverter converter = new AmfHttpMessageConverter();
        ActionMessage responseMessage = new ActionMessage();
        MessageBody responseBody = new MessageBody();
        responseMessage.addBody(responseBody);
        responseBody.setData(Person.stubPerson());
        converter.write(responseMessage, this.amfContentType, outputMessage);

        ActionMessage result = deserializeResponseToActionMessage();
        assertNotNull(result);
        assertEquals(1, result.getBodyCount());
        assertTrue(result.getBody(0).getData() instanceof Person);
    }

    @Test
    public void readSimpleString() throws Exception {
        this.request.setContentType(new MediaType("application", "x-amf").toString());
        this.request.setContent(serializeToByteArray("foo"));
        HttpInputMessage inputMessage = new ServletServerHttpRequest(this.request);
        AmfHttpMessageConverter converter = new AmfHttpMessageConverter();

        Object result = converter.read(Object.class, inputMessage);
        assertEquals("foo", result);
    }

    @Test
    public void readObject() throws Exception {
        this.request.setContentType(new MediaType("application", "x-amf").toString());
        this.request.setContent(serializeToByteArray(Person.stubPerson()));
        HttpInputMessage inputMessage = new ServletServerHttpRequest(this.request);
        AmfHttpMessageConverter converter = new AmfHttpMessageConverter();
		converter.setDeserializationValidator(this.deserializationValidator);

        Object result = converter.read(Object.class, inputMessage);
        assertTrue(result instanceof Person);
    }

    @Test
    public void readNonAmfContent() throws Exception {
        this.request.setContent(("This should not be readable.").getBytes());
        HttpInputMessage inputMessage = new ServletServerHttpRequest(this.request);
        AmfHttpMessageConverter converter = new AmfHttpMessageConverter();

        try {
            converter.read(Object.class, inputMessage);
            fail("Exception was expected.");
        } catch (HttpMessageNotReadableException ex) {
            //Expected
        }
    }

    private byte[] serializeToByteArray(Object data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
		SerializationContext serializationContext = new SerializationContext();
		serializationContext.setDeserializationValidator(this.deserializationValidator);
		SerializationContext.setSerializationContext(serializationContext); // Seems to be required
		Amf3Output serializer = new Amf3Output(serializationContext);

        serializer.setOutputStream(out);
        serializer.writeObject(data);
        try {
            return out.toByteArray();
        } finally {
            out.close();
        }
    }

    private Object deserializeResponse() throws ClassNotFoundException, IOException {
		SerializationContext serializationContext = new SerializationContext();
		serializationContext.setDeserializationValidator(this.deserializationValidator);
        Amf3Input deserializer = new Amf3Input(serializationContext);
		SerializationContext.setSerializationContext(serializationContext); // Seems to be required
        this.request.setContent(this.response.getContentAsByteArray());
        deserializer.setInputStream(this.request.getInputStream());
        return deserializer.readObject();
    }

    private ActionMessage deserializeResponseToActionMessage() throws ClassNotFoundException, IOException {
        AmfMessageDeserializer deserializer = new AmfMessageDeserializer();
        this.request.setContent(this.response.getContentAsByteArray());
		SerializationContext serializationContext = new SerializationContext();
		serializationContext.setDeserializationValidator(this.deserializationValidator);
		SerializationContext.setSerializationContext(serializationContext); // Seems to be required
        deserializer.initialize(serializationContext, this.request.getInputStream(), null);
        ActionMessage result = new ActionMessage();
        deserializer.readMessage(result, new ActionContext());
        return result;
    }
}
