
package org.springframework.flex.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;

/**
 * Helper that ensures consistent translation from a Spring Security {@link Authentication} to a structure that will be
 * useful to a Flex client in determining the credentials of an authenticated user.
 * 
 * <p>
 * When this translator is used to convert the {@link Authentication} into a BlazeDS message, the body of the returned
 * message will contain the following properties as obtained from the {@link Authentication} object:
 * <ul>
 * <li>name - the "name" property from the authentication</li>
 * <li>authorities - an array of String representations of the authentication's authorities (i.e. obtained through
 * {@link GrantedAuthority#getAuthority})</li>
 * </ul>
 * 
 * @author Jeremy Grelle
 */
public abstract class AuthenticationResultTranslator {

    protected Map<String, Object> getAuthenticationResult() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> authenticationResult = new HashMap<String, Object>();
        authenticationResult.put("name", authentication.getName());
        String[] authorities = new String[authentication.getAuthorities().length];
        for (int i = 0; i < authorities.length; i++) {
            authorities[i] = authentication.getAuthorities()[i].getAuthority();
        }
        authenticationResult.put("authorities", authorities);
        return authenticationResult;
    }

}
