package org.springframework.flex.messaging;

import flex.messaging.messages.AsyncMessage;

public interface AsyncMessageCreator {

	AsyncMessage createMessage();
}
