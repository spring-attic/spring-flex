package org.springframework.flex.security3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;

import org.springframework.flex.core.ExceptionTranslator;
import org.springframework.flex.core.io.domain.Person;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import flex.messaging.MessageException;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class FlexAuthenticationEntryPointTests {

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
    }

    @Test
    public void amfActionMessage() throws IOException, ServletException, ClassNotFoundException {
        ActionMessage requestMessage = new ActionMessage();
        MessageBody body = new MessageBody();
        body.setData(new CommandMessage(CommandMessage.CLIENT_PING_OPERATION));
        body.setResponseURI("/1");
        requestMessage.addBody(body);
        requestMessage.setVersion(MessageIOConstants.AMF3);

        ByteArrayOutputStream amfBytes = new ByteArrayOutputStream();
        AmfMessageSerializer serializer = new AmfMessageSerializer();
        serializer.setVersion(MessageIOConstants.AMF3);
        serializer.initialize(new SerializationContext(), amfBytes, null);
        serializer.writeMessage(requestMessage);
        this.request.setContentType(MessageIOConstants.AMF_CONTENT_TYPE);
        this.request.setContent(amfBytes.toByteArray());

        FlexAuthenticationEntryPoint entryPoint = new FlexAuthenticationEntryPoint();
        Set<ExceptionTranslator> translators = new HashSet<ExceptionTranslator>();
        translators.add(new TestExceptionTranslator());
        entryPoint.setExceptionTranslators(translators);
        entryPoint.commence(this.request, this.response, new TestAuthenticationException());

        assertEquals(MessageIOConstants.AMF_CONTENT_TYPE, this.response.getHeader("Content-Type"));
        byte[] resultBytes = this.response.getContentAsByteArray();
        assertTrue(resultBytes.length > 0);

        AmfMessageDeserializer deserializer = new AmfMessageDeserializer();
        deserializer.initialize(new SerializationContext(), new ByteArrayInputStream(resultBytes), null);
        ActionMessage result = new ActionMessage();
        deserializer.readMessage(result, new ActionContext());
        assertNotNull(result);
        assertEquals(1, result.getBodyCount());
        assertTrue(result.getBody(0).getData() instanceof ErrorMessage);
        assertEquals(3, result.getVersion());
        assertEquals("/1"+MessageIOConstants.STATUS_METHOD, result.getBody(0).getTargetURI());
    }

    @Test
    public void amfOther() throws IOException, ServletException {
        Person person = Person.stubPerson();
        ByteArrayOutputStream amfBytes = new ByteArrayOutputStream();
        AmfMessageSerializer serializer = new AmfMessageSerializer();
        serializer.setVersion(MessageIOConstants.AMF3);
        serializer.initialize(new SerializationContext(), amfBytes, null);
        serializer.writeObject(person);
        this.request.setContentType(MessageIOConstants.AMF_CONTENT_TYPE);
        this.request.setContent(amfBytes.toByteArray());

        FlexAuthenticationEntryPoint entryPoint = new FlexAuthenticationEntryPoint();
        entryPoint.commence(this.request, this.response, new TestAuthenticationException());

        assertEquals(403, this.response.getStatus());
    }

    @Test
    public void nonAMF() throws IOException, ServletException {
        String json = "{id : '1', name : 'Bob'}";
        this.request.setContent(json.getBytes());
        this.request.setContentType("application/json");

        FlexAuthenticationEntryPoint entryPoint = new FlexAuthenticationEntryPoint();
        entryPoint.commence(this.request, this.response, new TestAuthenticationException());

        assertEquals(403, this.response.getStatus());
    }

    public static class TestAuthenticationException extends AuthenticationCredentialsNotFoundException {
        private static final long serialVersionUID = 1L;

        public TestAuthenticationException() {
			super("Test");
		}
    }

    private static class TestExceptionTranslator implements ExceptionTranslator {

        public boolean handles(Class<?> clazz) {
            if (clazz.equals(TestAuthenticationException.class)) {
                return true;
            }
            return false;
        }

        public MessageException translate(Throwable t) {
            return new MessageException("Test");
        }
    }
}
