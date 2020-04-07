package com.budderfly.gateway.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    public static final String INVENTORY_API = "ROLE_INVENTORY_API";

    public static final String PORTAL = "ROLE_PORTAL";

    private AuthoritiesConstants() {
    }
}
