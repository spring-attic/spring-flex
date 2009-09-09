package org.springframework.flex.samples.secured;

import java.util.Map;

import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.flex.security.AuthenticationResultUtils;
import org.springframework.stereotype.Service;

@RemotingDestination
@Service
public class SecurityHelper {

    public Map<String, Object> getAuthentication() {
        return AuthenticationResultUtils.getAuthenticationResult();
    }
}
