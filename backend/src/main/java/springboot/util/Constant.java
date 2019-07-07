/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package springboot.util;

/**
 *
 * @author TGMaster
 */
public interface Constant {

    public static final String SYSTEM_ADMIN_ID = "th1s1ssyst3m4dm1n0d3l3t3th1sus3r";

    /**
     * ****************************
     * define AIPs path
     */
    public static final String API_PREFIX = "/api";
    public static final String WITHIN_ID = "/{id}";
    
    // news APIs
    public static final String NEWS_API = API_PREFIX + "/news";
    public static final String NEWS_LIST = "/list";

    // Request Content Type
    public static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";
    public static final String APPLICATION_URL_ENCODED = "application/x-www-form-urlencoded";

    // Common header
    public static final String BOX_AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE = "Content-type";
    public static final String HEADER_TOKEN = "X-Access-Token";

    //Charset UTF
    public static final String CHARSET_UTF8 = "UTF-8";

    //Sort key
    public static final int SORT_BY_TITLE = 1;
    public static final int SORT_BY_INFO = 2;
    public static final int SORT_BY_AUTHOR = 3;
    public static final int SORT_BY_DATE = 4;

    public static final String TYPE_ITEMS = "/items";
    public static final int RANDOM_MIN = 100000;
    public static final int RANDOM_MAX = 999999;

    public static final long DEFAULT_SESSION_TIME_OUT = 1800000; // 30 minutes
    public static final int SALT_LENGTH = 6;

    public static final int STATUS_SUCCESS = 200;


    public enum ParamError {

        MISSING_USERNAME_AND_EMAIL("accountName", "Missing both user name and email address"),
        USER_NAME("userName", "Invalid user name"),
        PASSWORD("passwordHash", "Invalid password hash"),
        TOKEN_EXPIRE_DURATION("tokenExpireDuration", "Invalid token expiry duration"),
        REDIRECT_URL("redirectUrl", "Invalid redirect URL");

        private final String name;
        private final String desc;

        private ParamError(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }
    }
}