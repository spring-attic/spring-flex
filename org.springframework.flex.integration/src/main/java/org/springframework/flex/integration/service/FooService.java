package org.springframework.flex.integration.service;

import org.springframework.flex.messaging.remoting.FlexExclude;
import org.springframework.flex.messaging.remoting.FlexInclude;
import org.springframework.flex.messaging.remoting.FlexService;
import org.springframework.stereotype.Component;

@Component
@FlexService
public class FooService {

	@FlexInclude
	public String bar() {
		return "bar";
	}
	
	@FlexExclude
	public String baz() {
		return "baz";
	}
}
