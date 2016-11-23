package org.joget.commons.util;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.validator.EmailValidator;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.util.ReflectionUtils;
import javax.mail.internet.MimeUtility;

/**
 * Utility methods for String processing
 * 
 */
@SuppressWarnings("rawtypes")
public class StringUtil {

    public static final String TYPE_REGEX = "regex";
    public static final String TYPE_JSON = "json";
    public static final String TYPE_JAVASCIPT = "javascript";
    public static final String TYPE_HTML = "html";
    public static final String TYPE_XML = "xml";
    public static final String TYPE_JAVA = "java";
    public static final String TYPE_SQL = "sql";
    public static final String TYPE_URL = "url";

    static final Whitelist whitelistRelaxed;
    static {
        // configure jsoup whitelist
        whitelistRelaxed = Whitelist.relaxed().addTags("span", "div").addAttributes(":all","id","style","class","title","target", "name");
        java.lang.reflect.Field field = ReflectionUtils.findField(whitelistRelaxed.getClass(), "protocols");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, whitelistRelaxed, new HashMap());
    }

    /**
     * Method used to properly encode the parameters in a URL string
     * @param url
     * @return 
     */
    public static String encodeUrlParam(String url) {
        String urlResult = url;
        try {
            String[] urlPart = urlResult.split("\\?");

            urlResult = urlPart[0];

            if (urlPart.length > 1) {
                urlResult += "?" + constructUrlQueryString(getUrlParams(urlPart[1]));
            }
        } catch (Exception e) {
            LogUtil.error(StringUtil.class.getName(), e, "");
        }

        return urlResult;
    }

    /**
     * Method used to merge 2 query string. If same parameter found, the one from 
     * second query string will override the first query string.
     * @param queryString1
     * @param queryString2
     * @return 
     */
    public static String mergeRequestQueryString(String queryString1, String queryString2) {
        if (queryString1 == null || queryString2 == null) {
            return queryString1;
        }

        Map<String, String[]> params = getUrlParams(queryString1);
        params.putAll(getUrlParams(queryString2));

        return constructUrlQueryString(params);
    }

    /**
     * Add parameter and its value to url. Override the value if the parameter 
     * is exist in the url
     * @param url
     * @param paramKey
     * @param paramValue
     * @return 
     */
    public static String addParamsToUrl(String url, String paramKey, String paramValue) {
        return addParamsToUrl(url, paramKey, new String[]{paramValue});
    }

    /**
     * Add parameter and its values to url. Override the value if the parameter
     * is exist in the url
     * @param url
     * @param paramKey
     * @param paramValues
     * @return 
     */
    public static String addParamsToUrl(String url, String paramKey, String[] paramValues) {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(paramKey, paramValues);
        return addParamsToUrl(url, params);
    }

    /**
     * Add parameters and its values to url. Override the value if the parameter
     * is exist in the url
     * @param url
     * @param params
     * @return 
     */
    public static String addParamsToUrl(String url, Map<String, String[]> params) {
        String urlResult = url;
        try {
            String[] urlPart = urlResult.split("\\?");

            urlResult = urlPart[0];
            Map<String, String[]> resultParams = new HashMap<String, String[]>();

            if (urlPart.length > 1) {
                resultParams.putAll(getUrlParams(urlPart[1]));
            }
            resultParams.putAll(params);

            if (!resultParams.isEmpty()) {
                urlResult += "?" + constructUrlQueryString(resultParams);
            }
        } catch (Exception e) {
            LogUtil.error(StringUtil.class.getName(), e, "");
        }

        return urlResult;
    }

    /**
     * Converts all request parameters in url to a map
     * @param url
     * @return
     */
    @SuppressWarnings("unchecked")
	public static Map<String, String[]> getUrlParams(String url) {
        Map<String, String[]> result = new HashMap<String, String[]>();
        try {
            String queryString = url;
            if (url.contains("?")) {
                queryString = url.substring(url.indexOf("?") + 1);
            }
            if (queryString.length() > 1) {
                String[] params = queryString.split("&");

                for (String a : params) {
                    if (!a.isEmpty()) {
                        String[] param = a.split("=");
                        String key = URLDecoder.decode(param[0], "UTF-8");
                        String value = "";
                        if (param.length > 1 && !param[1].isEmpty()) {
                            value = URLDecoder.decode(param[1], "UTF-8");
                        }
                        
                        String[] values = (String[]) result.get(key);
                        if (values != null) {
                            List temp = Arrays.asList(values);
                            temp.add(value);
                            values = (String[]) temp.toArray(new String[0]);
                        } else {
                            values = new String[]{value};
                        }
                        
                        result.put(key, values);
                    }
                }
            }
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * Builds a query string based on parameters and its values
     * @param params
     * @return 
     */
    public static String constructUrlQueryString(Map<String, String[]> params) {
        String queryString = "";
        try {
            for (String key : params.keySet()) {
                String[] values = params.get(key);
                for (String value : values) {
                    queryString += URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8") + "&";
                }
            }
            if (queryString.endsWith("&")) {
                queryString = queryString.substring(0, queryString.length()-1);
            }
        } catch (Exception e) {
        }
        return queryString;
    }
    
    /**
     * Decodes provided url
     * @param url
     * @return 
     */
    public static String decodeURL(String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (Exception e) {
        }
        return url;
    }

    /**
     * Escape regex syntax in a string
     * @param inStr
     * @return 
     */
    public static String escapeRegex(String inStr) {
        return (inStr != null) ?  inStr.replaceAll("([\\\\*+\\[\\](){}\\$.?\\^|])", "\\\\$1") : null;
    }

    /**
     * Escape a string based on format and replaced string based on the replace keyword map
     * @param inStr input String
     * @param format TYPE_HTML, TYPE_JAVA, TYPE_JAVASCIPT, TYPE_JSON, TYPE_SQL, TYPE_XML, TYPE_URL or TYPE_REGEX. Support chain escaping by separate the format in semicolon (;)
     * @param replaceMap A map of keyword and new keyword pair to be replaced before escaping
     * @return 
     */
    @SuppressWarnings("unchecked")
	public static String escapeString(String inStr, String format, Map<String, String> replaceMap) {
        if (replaceMap != null) {
            Iterator it = replaceMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
                inStr = inStr.replaceAll(pairs.getKey(), escapeRegex(pairs.getValue()));
            }
        }
        
        if (format == null || inStr == null) {
            return inStr;
        }
        
        String[] formats = format.split(";");
        for (String f : formats) {
            if (TYPE_REGEX.equals(f)) {
                inStr = escapeRegex(inStr);
            } else if (TYPE_JSON.equals(f)) {
                inStr = JSONObject.escape(inStr);
            } else if (TYPE_JAVASCIPT.equals(f)) {
                inStr = StringEscapeUtils.escapeJavaScript(inStr);
            } else if (TYPE_HTML.equals(f)) {
                inStr = StringEscapeUtils.escapeHtml(inStr);
            } else if (TYPE_XML.equals(f)) {
                inStr = StringEscapeUtils.escapeXml(inStr);
            } else if (TYPE_JAVA.equals(f)) {
                inStr = StringEscapeUtils.escapeJava(inStr);
            } else if (TYPE_SQL.equals(f)) {
                inStr = StringEscapeUtils.escapeSql(inStr);
            } else if (TYPE_URL.equals(f)) {
                try {
                    inStr = URLEncoder.encode(inStr, "UTF-8");
                } catch (Exception e) {/* ignored */}
            }
        }
        
        return inStr;
    }

    /**
     * A comparator to compare string value with letter case ignored
     */
    public class IgnoreCaseComparator implements Comparator<String> {

        /**
         * Compare 2 strings with letter case ignored
         * @param strA
         * @param strB
         * @return 
         */
        public int compare(String strA, String strB) {
            return strA.compareToIgnoreCase(strB);
        }
    }

    /**
     * Encrypt the content with MD5
     * @param content
     * @return 
     */
    public static String md5(String content) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            byte[] data = content.getBytes();
            m.update(data, 0, data.length);
            BigInteger i = new BigInteger(1, m.digest());
            return String.format("%1$032X", i);
        } catch (Exception ex) {
            LogUtil.error(StringUtil.class.getName(), ex, "");
        }
        return "";
    }

    /**
     * Encrypt the content with MD5 base16
     * @param content
     * @return 
     */
    public static String md5Base16(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(content.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                String hex = Integer.toHexString((int) 0x00FF & b);
                if (hex.length() == 1) {
                    sb.append("0");
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Remove all HTML tags from the content
     * @param content
     * @return 
     */
    public static String stripAllHtmlTag(String content) {
        if (content != null && !content.isEmpty()) {
            content = Jsoup.clean(content, Whitelist.none());
        }
        return content;
    }
    
    /**
     * Removed all HTML tags not in the allowed map from the content
     * @param content
     * @param allowedTag
     * @return 
     */
    public static String stripHtmlTag(String content, String[] allowedTag) {
        if (content != null && !content.isEmpty()) {
            Whitelist whitelist = Whitelist.none().addAttributes(":all","style","class","title","id","src","href","target");
            for (String tag : allowedTag) {
                whitelist.addTags(tag);
            }
            java.lang.reflect.Field field = ReflectionUtils.findField(whitelist.getClass(), "protocols");
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, whitelist, new HashMap());
            content = Jsoup.clean(content, whitelist);
        }
        return content;
    }
    
    /**
     * Remove script and unknown tag from the content
     * @param content
     * @return 
     */
    public static String stripHtmlRelaxed(String content) {
        if (content != null && content.indexOf("<") >= 0) {
            content = Jsoup.clean(content, whitelistRelaxed);
        }
        return content;
    }
    
    /**
     * Encrypt all keywords in the content which wrapped in SecurityUtil.ENVELOPE
     * with SecurityUtil.encrypt method
     * @param content
     * @return 
     */
    public static String encryptContent(String content) {
        //parse content
        if (content != null && content.contains(SecurityUtil.ENVELOPE)) {
            Pattern pattern = Pattern.compile(SecurityUtil.ENVELOPE + "((?!" + SecurityUtil.ENVELOPE + ").)*" + SecurityUtil.ENVELOPE);
            Matcher matcher = pattern.matcher(content);
            Set<String> sList = new HashSet<String>();
            while (matcher.find()) {
                sList.add(matcher.group(0));
            }

            try {
                if (!sList.isEmpty()) {
                    for (String s : sList) {
                        String tempS = s.replaceAll(SecurityUtil.ENVELOPE, "");
                        tempS = SecurityUtil.encrypt(tempS);

                        content = content.replaceAll(s, tempS);
                    }
                }
            } catch (Exception ex) {
                LogUtil.error(StringUtil.class.getName(), ex, "");
            }
        }

        return content;
    }

    /**
     * Decrypt all keywords in the content which wrapped in SecurityUtil.ENVELOPE
     * with SecurityUtil.decrypt method
     * @param content
     * @return 
     */
    public static String decryptContent(String content) {
        //parse content
        if (content != null && content.contains(SecurityUtil.ENVELOPE)) {
            Pattern pattern = Pattern.compile(SecurityUtil.ENVELOPE + "((?!" + SecurityUtil.ENVELOPE + ").)*" + SecurityUtil.ENVELOPE);
            Matcher matcher = pattern.matcher(content);
            Set<String> sList = new HashSet<String>();
            while (matcher.find()) {
                sList.add(matcher.group(0));
            }

            try {
                if (!sList.isEmpty()) {
                    for (String s : sList) {
                        String tempS = SecurityUtil.decrypt(s);
                        content = content.replaceAll(StringUtil.escapeRegex(s), tempS);
                    }
                }
            } catch (Exception ex) {
                LogUtil.error(StringUtil.class.getName(), ex, "");
            }
        }

        return content;
    }
    
    /**
     * Search a keyword and replace it with a new keyword in byte content
     * @param bytes
     * @param search
     * @param replacement
     * @return 
     */
    public static byte[] searchAndReplaceByteContent(byte[] bytes, String search, String replacement) {
        if (search != null && replacement != null) {
            try {
                String content = new String(bytes, "UTF-8");

                content = content.replaceAll(search, replacement);
                bytes = content.getBytes("UTF-8");
            } catch (Exception e) {
                //ignore
            }
        }
        return bytes;
    }
    
    /**
     * Search keywords and replace it with corresponding new keyword in byte content
     * @param bytes
     * @param replacements
     * @return 
     */
    public static byte[] searchAndReplaceByteContent(byte[] bytes, Map<String, String> replacements) {
        if (replacements != null && !replacements.isEmpty()) {
            try {
                String content = new String(bytes, "UTF-8");
                
                for (String search : replacements.keySet()) {
                    content = content.replaceAll(search, replacements.get(search));
                }
                bytes = content.getBytes("UTF-8");
            } catch (Exception e) {
                //ignore
            }
        }
        return bytes;
    }
    
    /**
     * Method used for validate an email. Options to validate multiple email separated
     * by semicolon (;)
     * @param email
     * @param multiple
     * @return 
     */
    public static boolean validateEmail(String email, boolean multiple) {
        String[] emails;
        if (multiple) {
            emails = email.split(";");
        } else {
            emails = new String[]{email};
        }
        
        EmailValidator validator = EmailValidator.getInstance();
        
        boolean valid = true;
        for (String e : emails) {
            if (!validator.isValid(e.trim())) {
                valid = false;
                break;
            }
        }
                
        return valid;        
    }
    
    /**
     * Method used for encode personal name in an email. 
     * by semicolon (;)
     * @param email
     * @param multiple
     * @return 
     */
    public static String encodeEmail(String email) {
        if (email.contains("<") && email.contains(">")) {
            try {
                email = MimeUtility.encodeWord(email.substring(0, email.indexOf("<")), "UTF-8", null) + email.substring(email.indexOf("<"));
            } catch (Exception e) {
                LogUtil.debug(StringUtil.class.getName(), "Not able to encode " + email);
            }
        }
        return email;
    }
}
