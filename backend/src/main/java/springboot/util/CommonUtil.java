/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package springboot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/**
 *
 * @author TGMaster
 */
public class CommonUtil {

    public static final Random RANDOM = new SecureRandom();
    public final static ObjectMapper mapper = new ObjectMapper();

    /**
     * Validate {@code str} not null, not empty and matches {@code regex}
     *
     * @param str
     * @param regex
     * @return {@code str} matches {@code regex}
     * @throws IllegalArgumentException if {@code regex} is null
     */
    public static boolean isValidPattern(String str, String regex) {

        if (regex == null) {
            throw new IllegalArgumentException("Regex pattern must not be null");
        }

        if (!StringUtils.isEmpty(str)) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(str);
            return matcher.matches();
        }

        return false;
    }

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static Random rnd = new Random();

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }

    /**
     *
     * @param number
     * @param min
     * @param max
     * @return
     * @throws IllegalArgumentException if (number == null || (min == null &&
     * max == null) || (max != null && min != null && max.doubleValue() <
     * min.doubleValue()))
     */
    public static boolean isBetweenRange(Number number, Number min, Number max) {

        if (number == null || (min == null && max == null) || (max != null && min != null && max.doubleValue() < min.doubleValue())) {
            throw new IllegalArgumentException("Invalid arguments");
        }

        return !((min == null && number.doubleValue() > max.doubleValue())
                || (max == null && number.doubleValue() < min.doubleValue())
                || (min != null && max != null && (number.doubleValue() < min.doubleValue() || number.doubleValue() > max.doubleValue())));
    }

    @Deprecated
    public static boolean isSystemAdmin(String userId) {
        return (userId != null && userId.equals(Constant.SYSTEM_ADMIN_ID));
    }

    /**
     * Use to compare length of {@code str} is between min & max value
     *
     * @param str
     * @param min must be greater than or equal to 0
     * @param max must be greater than or equal to the min value
     * @return true if and only if {@code str} not null and between the min &
     * max value
     * @throws IllegalArgumentException if the min value greater than 0 or the
     * max value greater than the min value
     */
    public static boolean isLengthBetween(String str, int min, int max) {

        if (min < 0 || max < min) {
            throw new IllegalArgumentException("The min value must be greater than or equal to 0 and less than the max value");
        }

        return (str != null && str.length() > 0 && str.length() < max);
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String generateSalt() {
        byte[] salt = new byte[Constant.SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static boolean isEmailFormat(String valueToValidate) {
        // Regex
        String regexExpression = "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?";
        Pattern regexPattern = Pattern.compile(regexExpression);
        boolean valid = false;
        if (valueToValidate != null) {
            Matcher matcher = regexPattern.matcher(valueToValidate);
            valid = matcher.matches();
        } else { // The case of empty Regex expression must be accepted
            Matcher matcher = regexPattern.matcher("");
            valid = matcher.matches();
        }
        return valid;
    }

    public static boolean isFileNameFormat(String fileName) {
        boolean valid = false;
        if (fileName.startsWith("!o!")) {
            if (!(fileName.contains("\\")
                    || fileName.contains("?")
                    || fileName.contains("<")
                    || fileName.contains(">")
                    || fileName.contains("/")
                    || fileName.contains(":")
                    || fileName.contains("|")
                    || fileName.contains("\""))) {
                valid = true;
            }
        }
        return valid;
    }

    public static String writeObjectToJson(Object obj) {
        try {

            return mapper.writeValueAsString(obj);

        } catch (JsonProcessingException ex) {
            // Throw our exception
            throw new ApplicationException(ex.getCause());
        }
    }
}
