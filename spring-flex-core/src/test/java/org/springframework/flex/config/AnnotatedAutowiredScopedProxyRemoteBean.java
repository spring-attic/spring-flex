package org.springframework.flex.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.flex.config.RemotingAnnotationPostProcessorTests.MyDependency;
import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@RemotingDestination
@Service
@Scope(value="request")
public class AnnotatedAutowiredScopedProxyRemoteBean {

    public AnnotatedAutowiredScopedProxyRemoteBean() {}
    
    @Autowired
    public AnnotatedAutowiredScopedProxyRemoteBean(MyDependency dependency) {
        Assert.notNull(dependency);
    }
}
