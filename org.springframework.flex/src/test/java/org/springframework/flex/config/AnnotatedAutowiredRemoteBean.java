package org.springframework.flex.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.flex.config.RemotingAnnotationPostProcessorTests.MyDependency;
import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@RemotingDestination
public class AnnotatedAutowiredRemoteBean {

	@Autowired
	public AnnotatedAutowiredRemoteBean(MyDependency dependency){
		Assert.notNull(dependency);
	}
}
