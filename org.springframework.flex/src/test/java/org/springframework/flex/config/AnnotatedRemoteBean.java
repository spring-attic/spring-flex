package org.springframework.flex.config;

import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.stereotype.Service;

@Service
@RemotingDestination(channels={"my-amf","my-secure-amf"})
public class AnnotatedRemoteBean {

}
