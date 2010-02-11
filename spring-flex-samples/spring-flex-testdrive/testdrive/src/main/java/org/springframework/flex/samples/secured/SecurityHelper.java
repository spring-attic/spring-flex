package org.springframework.flex.samples.secured;

import java.util.Map;

import org.springframework.flex.security.AuthenticationResultUtils;

public class SecurityHelper {

    public Map<String, Object> getAuthentication() {
        return AuthenticationResultUtils.getAuthenticationResult();
    }
}
