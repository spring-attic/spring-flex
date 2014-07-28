package org.springframework.flex.hibernate4.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.flex.hibernate4.config.RemotingAnnotationPostProcessorTests.MyDependency;
import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@RemotingDestination
@Service
@Scope(value = "request")
public class AnnotatedAutowiredScopedProxyRemoteBean {

	public AnnotatedAutowiredScopedProxyRemoteBean() {
		super();
	}

	@Autowired
	public AnnotatedAutowiredScopedProxyRemoteBean(MyDependency dependency) {
		Assert.notNull(dependency);
	}
}
