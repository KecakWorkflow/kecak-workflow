/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.joget.apps.scheduler;

import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowProcessResult;
import org.joget.workflow.model.service.WorkflowManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Yonathan
 */
public class ProcessAutomationProcessor implements Job{
    
    private WorkflowManager workflowManager;
    
    public void startProcess(String processDefId, Map variables){
        processDefId = processDefId.replaceAll(":", "#");
        workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
        WorkflowProcessResult result = workflowManager.processStart(processDefId, null, variables, null, null, false);
        if (result != null) {
            LogUtil.info(this.getClass().getName(), "Trying to start process");
            WorkflowProcess processStarted = result.getProcess();
            if (processStarted != null) {
                LogUtil.info(this.getClass().getName(), "Process "+processStarted.getInstanceId()+" Started Sucessfully!!!");
            }
        }else{
            LogUtil.info(this.getClass().getName(), "Failed to start process");
        }
    }

    public void execute(JobExecutionContext jec) throws JobExecutionException {
        String processDefId = "AQMS:latest:fileProcessingProcess";
        Map variables = null;
        this.startProcess(processDefId, variables);
    }
}
