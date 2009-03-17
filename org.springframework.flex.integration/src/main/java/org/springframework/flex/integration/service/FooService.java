package org.springframework.flex.integration.service;

import org.springframework.flex.messaging.remoting.RemotingExclude;
import org.springframework.flex.messaging.remoting.RemotingInclude;
import org.springframework.flex.messaging.remoting.RemotingDestination;
import org.springframework.stereotype.Component;

@Component
@RemotingDestination
public class FooService {

	@RemotingInclude
	public String bar() {
		return "bar";
	}
	
	@RemotingExclude
	public String baz() {
		return "baz";
	}
}
