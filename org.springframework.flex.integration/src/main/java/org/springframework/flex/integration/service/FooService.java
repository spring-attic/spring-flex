package org.springframework.flex.integration.service;

import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.flex.remoting.RemotingExclude;
import org.springframework.flex.remoting.RemotingInclude;
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
