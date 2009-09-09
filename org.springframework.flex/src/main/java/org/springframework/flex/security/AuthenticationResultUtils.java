
package org.springframework.flex.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;

/**
 * Helper that ensures consistent handling of a Spring Security {@link Authentication}, providing translation to a structure that will be
 * useful to a Flex client in determining the credentials of an authenticated user.
 * 
 * <p>
 * When this helper is used to convert the {@link Authentication} into a BlazeDS message, the body of the returned
 * message will contain the following properties as obtained from the {@link Authentication} object:
 * <ul>
 * <li>name - the "name" property from the authentication</li>
 * <li>authorities - an array of String representations of the authentication's authorities (i.e. obtained through
 * {@link GrantedAuthority#getAuthority})</li>
 * </ul>
 * 
 * @author Jeremy Grelle
 */
public abstract class AuthenticationResultUtils {

    /**
     * Checks for an {@link Authentication} object in the current {@link SecurityContext} and if one is found, constructs and returns
     * a map that will result in an object of the expected format when returned to the Flex client. 
     * @return a map of the {@link Authentication} properties to be serialized over AMF, or null if no Authentication is found
     */
    public static Map<String, Object> getAuthenticationResult() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
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
