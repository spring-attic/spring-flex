package org.springframework.flex.core;

import java.util.HashMap;
import java.util.Map;

public class MessageProcessingContext {

	private final Object messageTarget;
	
	private final Map<String, Object> attributes = new HashMap<String, Object>();
	
	public MessageProcessingContext(Object messageTarget) {
		this.messageTarget = messageTarget;
	}

	public Object getMessageTarget() {
		return messageTarget;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
	
}
