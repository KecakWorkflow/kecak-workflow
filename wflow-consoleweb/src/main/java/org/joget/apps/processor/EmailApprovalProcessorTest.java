/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.joget.apps.processor;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Yonathan
 */
public class EmailApprovalProcessorTest {
    
    public static void main(String[] args) {
        String emailContent 
                = "Approval Status: Rejected__Rejected Remarks:[ type_remarks_inside_this_box ]";
        emailContent = emailContent.replaceAll("\\_\\_", " ");
        
        String content = emailContent.replaceAll("\\r?\\n", " ");
        System.out.println("Content: "+content);
        String emailContentPattern 
                = "Approval Status: {form_sf_approval_sf_appr_approval}{nouse}{form_sf_approval_sf_appr_sf_remark_sf_app_history_approval_status} Remarks:[{form_sf_approval_sf_appr_sf_remark_sf_app_history_remark}] ";

        String patternRegex = createRegex(emailContentPattern);
        System.out.println(patternRegex);
        Pattern pattern = Pattern.compile("\\{([a-zA-Z0-9_]+)\\}");
        Matcher matcher = pattern.matcher(emailContentPattern);

        Pattern pattern2 = Pattern.compile("^" + patternRegex + "$");
        Matcher matcher2 = pattern2.matcher(content);
        
        System.out.println("Find2: "+matcher2.find());
        while (matcher2.find()) {
            
            int count = 1;
            while (matcher.find()) {
//                System.out.println("Find: "+matcher.find());
                String key = matcher.group(1);
                String value = matcher2.group(count);
                System.out.println("key:[" + key + "]");
                System.out.println("value:[" + value + "]");
                if (key.startsWith("var_")) {
                    key = key.replaceAll("var_", "");
                    System.out.println("variable_key:[" + key + "] value:[" + value + "]");
                } else if (key.startsWith("form_")) {
                    key = key.replaceAll("form_", "");
                    System.out.println("form_key:[" + key + "] value:[" + value + "]");
                }
                count++;
            }
        }
    }

    public static String createRegex(String raw) {
        String result = escapeString(raw, null);
        result = result.replaceAll("\\\\\\{unuse\\\\\\}", "__([\\\\s\\\\S]*)").replaceAll("\\\\\\{[a-zA-Z0-9_]+\\\\\\}", "(.*?)");
        if (result.startsWith("__")) {
            result = result.substring(2);
        }
        return result;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static String escapeString(String inStr, Map<String, String> replaceMap) {
        if (replaceMap != null) {
            Iterator it = replaceMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
                inStr = inStr.replaceAll(pairs.getKey(), escapeRegex(pairs.getValue()));
            }
        }

        return escapeRegex(inStr);
    }

    public static String escapeRegex(String inStr) {
        return (inStr != null) ? inStr.replaceAll("([\\\\*+\\[\\](){}\\$.?\\^|])", "\\\\$1") : null;
    }
}
