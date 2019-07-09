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
    public static final String MODEL_API = API_PREFIX + "/lda";

    // Request Content Type
    public static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";

    //Charset UTF
    public static final String CHARSET_UTF8 = "UTF-8";

    //SALT
    public static final int SALT_LENGTH = 6;

    public static final int STATUS_SUCCESS = 200;


    public enum ParamError {

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