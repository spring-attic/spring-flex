package org.springframework.flex.core;

import java.util.HashMap;
import java.util.Map;

public class MessageInterceptionContext {

	private final Object messageTarget;
	
	private final Map<String, Object> attributes = new HashMap<String, Object>();
	
	public MessageInterceptionContext(Object messageTarget) {
		this.messageTarget = messageTarget;
	}

	public Object getMessageTarget() {
		return messageTarget;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
	
}
