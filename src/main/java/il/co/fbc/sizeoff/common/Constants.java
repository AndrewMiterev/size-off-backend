package il.co.fbc.sizeoff.common;

public interface Constants {
    String AUTHENTICATION_PATH = "/api/auth";
    String REGISTRATION_PATH = "/registration";
    String LOGIN_PATH = "/login";
    String TIMESTAMP_PATH = "/timestamp";

    String INFO_PATH = "/api/info";
    String LIST_PATH = "/list";

    String JOB_PATH = "/api/job";

    int MINIMUM_HOURS_TO_DATA_EXPIRE = 3;
    int MAXIMUM_HOURS_TO_DATA_EXPIRE = 24 * 30;                     // 30 days
    int OPTIMUM_HOURS_TO_DATA_EXPIRE = 24;                          // day
    int MAXIMUM_CLEAR_OUTDATED_DATA_AFTER_HOURS = 366 * 24;         // 1 year

    String[] SWAGER_AUTH_WHITELIST = {
            "/v2/api-docs",
            "/v3/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**"
    };
    String[] LOGIN_AUTH_WHITELIST = {
            AUTHENTICATION_PATH + REGISTRATION_PATH,
            AUTHENTICATION_PATH + LOGIN_PATH
    };
    String[] SOCIAL_LOGIN_AUTH_WHITELIST = {
            // todo delete / and //sizeoffbackend
            "/",
            "/sizeoffbackend/**",                          // delete - for test
            "/login/oauth2/code/**",
    };

    String LEVEL_ANONYMOUS = "hasRole('ROLE_ANONYMOUS') or hasRole('ROLE_OBSERVER') or hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')";
    String LEVEL_OBSERVER = "hasRole('ROLE_OBSERVER') or hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')";
    String LEVEL_USER = "hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')";
    String LEVEL_ADMIN = "hasRole('ROLE_ADMIN')";
}
