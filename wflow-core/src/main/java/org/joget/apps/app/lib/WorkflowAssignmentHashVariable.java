package org.joget.apps.app.lib;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowAssignment;

public class WorkflowAssignmentHashVariable extends DefaultHashVariablePlugin {

    public String processHashVariable(String variableKey) {
        WorkflowAssignment wfAssignment = (WorkflowAssignment) this.getProperty("workflowAssignment");
        if (wfAssignment != null) {
            char firstChar = variableKey.charAt(0);
            firstChar = Character.toUpperCase(firstChar);
            variableKey = firstChar + variableKey.substring(1, variableKey.length());

            try {
                Method method = WorkflowAssignment.class.getDeclaredMethod("get" + variableKey, new Class[]{});
                String returnResult = (String) method.invoke(wfAssignment, new Object[]{});
                if (returnResult != null) {
                    return returnResult;
                }
            } catch (Exception e) {
                LogUtil.error(WorkflowAssignmentHashVariable.class.getName(), e, "Error retrieving wfAssignment attribute ");
            }
        }
        return null;
    }

    public String getName() {
        return "Workflow Assignment Hash Variable";
    }

    public String getPrefix() {
        return "assignment";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "Workflow Assignment Hash Variable";
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
        syntax.add("assignment.processId");
        syntax.add("assignment.processDefId");
        syntax.add("assignment.processName");
        syntax.add("assignment.processVersion");
        syntax.add("assignment.processRequesterId");
        syntax.add("assignment.activityId");
        syntax.add("assignment.activityName");
        syntax.add("assignment.activityDefId");
        syntax.add("assignment.assigneeId");
        
        return syntax;
    }
}
