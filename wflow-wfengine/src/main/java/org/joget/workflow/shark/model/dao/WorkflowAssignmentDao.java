package org.joget.workflow.shark.model.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.dao.WorkflowProcessLinkDao;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.shark.model.SharkAssignment;
import org.joget.workflow.util.WorkflowUtil;

public class WorkflowAssignmentDao extends AbstractSpringDao {
    
    private WorkflowProcessLinkDao workflowProcessLinkDao;
    public final static String ENTITY_NAME="SharkAssignment";
    
    public Set<String> getAssignmentProcessIds(String packageId, String processDefId, String processId, String activityDefId, String username, String state) {
        
        //required to disable lazy loading 
        String condition = "join fetch e.process p join fetch e.activity a  join fetch a.state s";
        Collection<String> params = new ArrayList<String>();
        
        if (packageId != null || processDefId != null || processId != null || activityDefId != null || username != null || state != null) {
            condition += " where 1=1";
            
            if (packageId != null && !packageId.isEmpty()) {
                condition += " and p.processDefId like ?";
                params.add(packageId + "#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
                condition += " and p.processDefId like ?";
                processDefId = ignoreVersion(processDefId);
                params.add(processDefId);
            }
            
            if (processId != null && !processId.isEmpty()) {
                condition += " and p.processId = ?";
                params.add(processId);
            }
            
            if (activityDefId != null && !activityDefId.isEmpty()) {
                condition += " and a.activityDefId = ?";
                params.add(activityDefId);
            }
            
            if (username != null && !username.isEmpty()) {
                condition += " and e.assigneeName = ?";
                params.add(username);
            }
            
            if (state != null && !state.isEmpty()) {
                condition += " and s.name like ?";
                params.add(state + ".%");
            }
        }
        @SuppressWarnings("unchecked")
		Collection<SharkAssignment> shAss = find(ENTITY_NAME, condition, params.toArray(new String[0]), null, null, null, null);
        Set<String> ass = new HashSet<String>();
        
        @SuppressWarnings("unused")
		WorkflowManager wm = (WorkflowManager) WorkflowUtil.getApplicationContext().getBean("workflowManager");
        
        if (shAss != null && !shAss.isEmpty()) {
            for (SharkAssignment s : shAss) {
                ass.add(s.getProcess().getProcessId());
            }
        }
        
        return ass;
    }
    
    public Collection<WorkflowAssignment> getAssignmentsByProcessIds(Collection<String> processIds, String username, String state, String sort, Boolean desc, Integer start, Integer rows) {
        //sorting
        if (sort != null && !sort.isEmpty()) {
            if ("processDefId".equals(sort)) {
                sort = "p.processDefId";
            } else if ("processVersion".equals(sort)) {
                sort = "p.processVersion";
            } else if ("processId".equals(sort)) {
                sort = "p.processId";
            } else if ("processName".equals(sort)) {
                sort = "p.processName";
            } else if ("processRequesterId".equals(sort)) {
                sort = "p.processRequesterId";
            } else if ("activityName".equals(sort)) {
                sort = "a.activityName";
            } else if ("activityDefId".equals(sort)) {
                sort = "a.activityDefId";
            } else if ("dateCreated".equals(sort)) {
                sort = "a.activated";
            } else if ("assigneeName".equals(sort)) {
                sort = "e.assigneeName";
            }
        }

        //required to disable lazy loading 
        String condition = "join fetch e.process p join fetch e.activity a  join fetch a.state s";
        Collection<String> params = new ArrayList<String>();
        
        if (processIds != null || username != null || state != null) {
            condition += " where 1=1";
            
            if (processIds != null && !processIds.isEmpty()) {
                condition += " and p.processId in (?";
                for (int i = 1; i < processIds.size(); i++) {
                    condition += ",?";
                }
                condition += ")";
                params.addAll(processIds);
            }
            
            if (username != null && !username.isEmpty()) {
                condition += " and e.assigneeName = ?";
                params.add(username);
            }
            
            if (state != null && !state.isEmpty()) {
                condition += " and s.name like ?";
                params.add(state + ".%");
            }
        }
        @SuppressWarnings("unchecked")
		Collection<SharkAssignment> shAss = find(ENTITY_NAME, condition, params.toArray(new String[0]), sort, desc, start, rows);
        
        return transformToWorkflowAssignment(shAss);
    }
    
    public Collection<WorkflowAssignment> getAssignments(String packageId, String processDefId, String processId, String activityDefId, String username, String state, String sort, Boolean desc, Integer start, Integer rows) {
        //sorting
        if (sort != null && !sort.isEmpty()) {
            if ("processDefId".equals(sort)) {
                sort = "p.processDefId";
            } else if ("processVersion".equals(sort)) {
                sort = "p.processVersion";
            } else if ("processId".equals(sort)) {
                sort = "p.processId";
            } else if ("processName".equals(sort)) {
                sort = "p.processName";
            } else if ("processRequesterId".equals(sort)) {
                sort = "p.processRequesterId";
            } else if ("activityName".equals(sort)) {
                sort = "a.activityName";
            } else if ("activityDefId".equals(sort)) {
                sort = "a.activityDefId";
            } else if ("dateCreated".equals(sort)) {
                sort = "a.activated";
            } else if ("assigneeName".equals(sort)) {
                sort = "e.assigneeName";
            }
        }

        //required to disable lazy loading 
        String condition = "join fetch e.process p join fetch e.activity a  join fetch a.state s";
        Collection<String> params = new ArrayList<String>();
        
        if (packageId != null || processDefId != null || processId != null || activityDefId != null || username != null || state != null) {
            condition += " where 1=1";
            
            if (packageId != null && !packageId.isEmpty()) {
                condition += " and p.processDefId like ?";
                params.add(packageId + "#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
                condition += " and p.processDefId like ?";
                processDefId = ignoreVersion(processDefId);
                params.add(processDefId);
            }
            
            if (processId != null && !processId.isEmpty()) {
                condition += " and p.processId = ?";
                params.add(processId);
            }
            
            if (activityDefId != null && !activityDefId.isEmpty()) {
                condition += " and a.activityDefId = ?";
                params.add(activityDefId);
            }
            
            if (username != null && !username.isEmpty()) {
                condition += " and e.assigneeName = ?";
                params.add(username);
            }
            
            if (state != null && !state.isEmpty()) {
                condition += " and s.name like ?";
                params.add(state + ".%");
            }
        }
        @SuppressWarnings("unchecked")
		Collection<SharkAssignment> shAss = find(ENTITY_NAME, condition, params.toArray(new String[0]), sort, desc, start, rows);
        
        return transformToWorkflowAssignment(shAss);
    }
    
    public int getAssignmentSize(String packageId, String processDefId, String processId, String activityDefId, String username, String state) {
        //required to disable lazy loading 
        String condition = "join e.process p join e.activity a join a.state s"; 
        Collection<String> params = new ArrayList<String>();
        
        if (packageId != null || processDefId != null || processId != null || activityDefId != null || username != null || state != null) {
            condition += " where 1=1";
            
            if (packageId != null && !packageId.isEmpty()) {
                condition += " and p.processDefId like ?";
                params.add(packageId + "#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
                condition += " and p.processDefId like ?";
                processDefId = ignoreVersion(processDefId);
                params.add(processDefId);
            }
            
            if (processId != null && !processId.isEmpty()) {
                condition += " and p.processId = ?";
                params.add(processId);
            }
            
            if (activityDefId != null && !activityDefId.isEmpty()) {
                condition += " and a.activityDefId = ?";
                params.add(activityDefId);
            }
            
            if (username != null && !username.isEmpty()) {
                condition += " and e.assigneeName = ?";
                params.add(username);
            }
            
            if (state != null && !state.isEmpty()) {
                condition += " and s.name like ?";
                params.add(state + ".%");
            }
        }
        Long total = count(ENTITY_NAME, condition, params.toArray(new String[0]));
        
        if (total != null) {
            return total.intValue();
        }
        return 0;
    }
    
    protected Collection<WorkflowAssignment> transformToWorkflowAssignment(Collection<SharkAssignment> shAss) {
        Collection<WorkflowAssignment> ass = new ArrayList<WorkflowAssignment>();
        
        Map<String, Long> limits = new HashMap<String, Long>();
        WorkflowManager wm = (WorkflowManager) WorkflowUtil.getApplicationContext().getBean("workflowManager");
        
        //transform to WorkflowAssignment
        if (shAss != null && !shAss.isEmpty()) {
            for (SharkAssignment s : shAss) {
                WorkflowAssignment a = new WorkflowAssignment();
                a.setActivityId(s.getActivity().getActivityId());
                a.setActivityDefId(s.getActivity().getActivityDefId());
                a.setActivityName(s.getActivity().getActivityName());
                a.setProcessDefId(s.getProcess().getProcessDefId());
                a.setProcessId(s.getProcess().getProcessId());
                a.setProcessName(s.getProcess().getProcessName());
                a.setProcessRequesterId(s.getProcess().getProcessRequesterId());
                a.setProcessVersion(s.getProcess().getProcessVersion());
                a.setAssigneeName(s.getAssigneeName());
                
                Date dateCreated = new Date(s.getActivity().getActivated());
                a.setDateCreated(dateCreated);
                
                //get duedate & service level value
                Long limit = limits.get(a.getProcessDefId() + "|" + a.getActivityDefId());
                if (limit == null) {
                    limit = wm.getActivityLimit(a.getProcessDefId(), a.getActivityDefId());
                    limits.put(a.getProcessDefId() + "|" + a.getActivityDefId(), limit);
                }
                
                if (limit > 0) {
                    Date dueDate = wm.getDueDateProceedByPlugin(a.getProcessId(), a.getActivityDefId(), limit, dateCreated, dateCreated);
                    a.setDueDate(dueDate);
                    
                    a.setServiceLevelValue(wm.getServiceLevelValue(dateCreated, null, dueDate));
                } else {
                    a.setServiceLevelValue(-1);
                }
                
                //subflow
                if (a.getProcessRequesterId() != null && !a.getProcessRequesterId().isEmpty()) {
                    a.setSubflow(true);
                    if (workflowProcessLinkDao.getWorkflowProcessLink(a.getProcessId()) == null) {
                        workflowProcessLinkDao.addWorkflowProcessLink(a.getProcessRequesterId(), a.getProcessId());
                    }
                }
                
                ass.add(a);
            }
        }
        return ass;
    }
    
    private String ignoreVersion(String processDefId) {
        processDefId = processDefId.replaceAll("#[0-9]+#", "#%#");
        return processDefId;
    }

    public WorkflowProcessLinkDao getWorkflowProcessLinkDao() {
        return workflowProcessLinkDao;
    }

    public void setWorkflowProcessLinkDao(WorkflowProcessLinkDao workflowProcessLinkDao) {
        this.workflowProcessLinkDao = workflowProcessLinkDao;
    }
}
