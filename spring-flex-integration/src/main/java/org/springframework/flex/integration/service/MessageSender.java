package org.springframework.flex.integration.service;

import org.springframework.flex.messaging.AsyncMessageCreator;
import org.springframework.flex.messaging.MessageTemplate;

import flex.messaging.messages.AsyncMessage;

public class MessageSender {

	private MessageTemplate template;

	public void doSend() {
		final Stock stock = new Stock();
		template.send(new AsyncMessageCreator() {

			public AsyncMessage createMessage() {
				AsyncMessage msg = template.createMessageForDestination("market-data-feed");
				msg.setHeader("DSSubtopic", stock.getSymbol());
				msg.setBody(stock);
				return msg;
			}

		});
	}
	
	private class Stock {
		
		public String getSymbol() { return "JAVA";}
	}
}
