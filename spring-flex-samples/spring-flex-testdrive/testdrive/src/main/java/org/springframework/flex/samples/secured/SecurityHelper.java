package org.springframework.flex.samples.secured;

import java.util.Map;

import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.flex.security.AuthenticationResultTranslator;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@RemotingDestination
@Service
public class SecurityHelper extends AuthenticationResultTranslator{

    public Map<String, Object> getAuthentication() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return getAuthenticationResult();
        } else {
            return null;
        }
    }
}
