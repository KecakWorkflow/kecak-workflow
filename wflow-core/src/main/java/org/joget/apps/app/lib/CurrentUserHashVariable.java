package org.joget.apps.app.lib;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class CurrentUserHashVariable extends DefaultHashVariablePlugin {
    private User user = null;
    
    public String processHashVariable(String variableKey) {
        ApplicationContext appContext = AppUtil.getApplicationContext();
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) appContext.getBean("workflowUserManager");

        String username = workflowUserManager.getCurrentUsername();
        
        String attribute = variableKey;
        if (WorkflowUserManager.ROLE_ANONYMOUS.equals(username)) {
            return "";
        }
        String userAttr = getUserAttribute(username, attribute);
        if(userAttr!=null && !userAttr.equals("")){
            return userAttr;
        }else{
            return "";
        }
    }

    public String getName() {
        return "Current User Hash Variable";
    }

    public String getPrefix() {
        return "currentUser";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    protected String getUserAttribute(String username, String attribute) {
        String attributeValue = null;

        try {
            if (user == null) {
                ApplicationContext appContext = AppUtil.getApplicationContext();
                DirectoryManager directoryManager = (DirectoryManager) appContext.getBean("directoryManager");
                user = directoryManager.getUserByUsername(username);
            }
            
            if (user != null) {
                //convert first character to upper case
                char firstChar = attribute.charAt(0);
                firstChar = Character.toUpperCase(firstChar);
                attribute = firstChar + attribute.substring(1, attribute.length());
                Method method = User.class.getDeclaredMethod("get" + attribute, new Class[]{});
                String returnResult = null;
                attributeValue =  "";
                if(method!=null){
                    returnResult = ((Object) method.invoke(user, new Object[]{})).toString();
                    if(returnResult!=null && !returnResult.contains("#")){
                        if(!attribute.equals("Password")){
                            attributeValue = returnResult;
                        }
                    }
                }else{
                    return "";
                }
            }
        } catch (BeansException e) {
            LogUtil.error(CurrentUserHashVariable.class.getName(), e, e.getMessage());
        } catch (NoSuchMethodException e) {
            LogUtil.error(CurrentUserHashVariable.class.getName(), e, e.getMessage());
        } catch (SecurityException e) {
            LogUtil.error(CurrentUserHashVariable.class.getName(), e, e.getMessage());
        } catch (IllegalAccessException e) {
            LogUtil.error(CurrentUserHashVariable.class.getName(), e, e.getMessage());
        } catch (IllegalArgumentException e) {
            LogUtil.error(CurrentUserHashVariable.class.getName(), e, e.getMessage());
        } catch (InvocationTargetException e) {
            LogUtil.error(CurrentUserHashVariable.class.getName(), e, e.getMessage());
        }
        return attributeValue;
    }

    public String getLabel() {
        return "Current User Hash Variable";
    }

    public String getClassName() {
        return this.getClass().getName();
    }
    
    public String getPropertyOptions() {
        return "";
    }
    
    @Override
    public Collection<String> availableSyntax() {
        Collection<String> syntax = new ArrayList<String>();
        syntax.add("currentUser.username");
        syntax.add("currentUser.firstName");
        syntax.add("currentUser.lastName");
        syntax.add("currentUser.email");
        syntax.add("currentUser.active");
        syntax.add("currentUser.timeZone");
        
        return syntax;
    }
}
