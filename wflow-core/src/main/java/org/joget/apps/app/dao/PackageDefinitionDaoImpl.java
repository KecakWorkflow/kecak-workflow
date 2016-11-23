package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowParticipant;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;

/**
 * DAO to load/store PackageDefinition and mapping objects
 */
public class PackageDefinitionDaoImpl extends AbstractVersionedObjectDao<PackageDefinition> implements PackageDefinitionDao {

    public static final String ENTITY_NAME = "PackageDefinition";
    private AppDefinitionDao appDefinitionDao;

    public AppDefinitionDao getAppDefinitionDao() {
        return appDefinitionDao;
    }

    public void setAppDefinitionDao(AppDefinitionDao appDefinitionDao) {
        this.appDefinitionDao = appDefinitionDao;
    }

    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public void delete(PackageDefinition obj) {
        AppDefinition appDef = obj.getAppDefinition();
        if (appDef != null) {
            // disassociate from app
            Collection<PackageDefinition> list = appDef.getPackageDefinitionList();
            for (Iterator<PackageDefinition> i = list.iterator(); i.hasNext();) {
                PackageDefinition def = i.next();
                if (def.getId() != null && def.getId().equals(obj.getId())) {
                    i.remove();
                }
            }
            appDefinitionDao.saveOrUpdate(appDef);
        }
        // delete package definition
        super.delete(getEntityName(), obj);
    }

    /**
     * Loads the package definition for a specific app version
     * @param appId
     * @param appVersion
     * @return
     */
    public PackageDefinition loadAppPackageDefinition(String appId, Long appVersion) {
        PackageDefinition packageDef = null;

        // load the package definition
        String condition = " INNER JOIN e.appDefinition app WHERE app.id=? AND app.version=?";
        Object[] params = {appId, appVersion};
        @SuppressWarnings("unchecked")
		Collection<PackageDefinition> results = find(getEntityName(), condition, params, null, null, 0, 1);
        if (results != null && !results.isEmpty()) {
            packageDef = results.iterator().next();
        }

        return packageDef;
    }

    /**
     * Loads the package definition
     * @param packageId
     * @param packageVersion
     * @return
     */
    public PackageDefinition loadPackageDefinition(String packageId, Long packageVersion) {
        PackageDefinition packageDef = null;
        if (packageVersion != null) {
            // load the package definition
            String condition = " WHERE e.id=? AND e.version=?";
            Object[] params = {packageId, packageVersion};
            @SuppressWarnings("unchecked")
			Collection<PackageDefinition> results = find(getEntityName(), condition, params, null, null, 0, 1);
            if (results != null && !results.isEmpty()) {
                packageDef = results.iterator().next();
            }
        }
        return packageDef;
    }

    /**
     * Loads the package definition based on a process definition ID
     * @param packageVersion
     * @param processDefId
     * @return
     */
    public PackageDefinition loadPackageDefinitionByProcess(String packageId, Long packageVersion, String processDefId) {
        PackageDefinition packageDef = null;
        if (packageVersion != null) {
            processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);

            // load the package definition
            String condition = " INNER JOIN e.packageActivityFormMap paf WHERE e.id=? AND e.version=? AND paf.processDefId=?";
            Object[] params = {packageId, packageVersion, processDefId};
            @SuppressWarnings("unchecked")
			Collection<PackageDefinition> results = find(getEntityName(), condition, params, null, null, 0, 1);
            if (results != null && !results.isEmpty()) {
                packageDef = results.iterator().next();
            }
        }
        return packageDef;
    }

    public PackageDefinition createPackageDefinition(AppDefinition appDef, Long packageVersion) {
        PackageDefinition packageDef = new PackageDefinition();
        packageDef.setId(appDef.getId());
        packageDef.setVersion(packageVersion);
        packageDef.setName(appDef.getName());
        packageDef.setAppDefinition(appDef);
        saveOrUpdate(packageDef);
        return packageDef;
    }

    public PackageDefinition updatePackageDefinitionVersion(PackageDefinition packageDef, Long packageVersion) {
        String packageId = packageDef.getId();

        // detach previous package version
        delete(packageDef);

        // update package definition
        packageDef.setId(packageId);
        packageDef.setVersion(packageVersion);
        
        //remove not exist participants, activities and tools in mapping
        Collection<String> activityIds = new ArrayList<String>();
        Collection<String> toolIds = new ArrayList<String>();
        Collection<String> participantIds = new ArrayList<String>();
        Map<String, PackageActivityForm> packageActivityFormMap = new HashMap<String, PackageActivityForm>();
        Map<String, PackageActivityPlugin> packageActivityPluginMap = new HashMap<String, PackageActivityPlugin>();
        Map<String, PackageParticipant> packageParticipantMap = new HashMap<String, PackageParticipant>();
        try {
            WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
            Collection<WorkflowProcess> processList = workflowManager.getProcessList(packageDef.getAppDefinition().getAppId(), packageVersion.toString());
            for (WorkflowProcess wp : processList) {
                String processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(wp.getId());
                Collection<WorkflowActivity> activityList = workflowManager.getProcessActivityDefinitionList(wp.getId());
                activityIds.add(processDefId+"::"+WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS);
                participantIds.add(processDefId+"::"+"processStartWhiteList");
                for (WorkflowActivity a : activityList) {
                    if (a.getType().equalsIgnoreCase("normal")) {
                        activityIds.add(processDefId+"::"+a.getId());
                    } else if (a.getType().equalsIgnoreCase("tool")) {
                        toolIds.add(processDefId+"::"+a.getId());
                    }
                }

                Collection<WorkflowParticipant> participantList = workflowManager.getProcessParticipantDefinitionList(wp.getId());
                for (WorkflowParticipant p : participantList) {
                    participantIds.add(processDefId+"::"+p.getId());
                }
            }

            String currentUsername = WorkflowUtil.getCurrentUsername();
            Date currentDate = new Date();
            Map<String, PackageActivityForm> activityForms = packageDef.getPackageActivityFormMap();
            for (String key : activityForms.keySet()) {
                if (activityIds.contains(key)) {
                    packageActivityFormMap.put(key, activityForms.get(key));
                }
            }
            Map<String, PackageActivityPlugin> activityPluginMap = packageDef.getPackageActivityPluginMap();
            for (String key : activityPluginMap.keySet()) {
                if (toolIds.contains(key)) {
                    packageActivityPluginMap.put(key, activityPluginMap.get(key));
                }
            }
            Map<String, PackageParticipant> participantMap = packageDef.getPackageParticipantMap();
            PackageParticipant packageParticipant = null;
            for (String key : participantMap.keySet()) {
                if (participantIds.contains(key)) {
                	packageParticipant = participantMap.get(key);
                	packageParticipant.setDateCreated(currentDate);
                	packageParticipant.setDateModified(currentDate);
                	packageParticipant.setCreatedBy(currentUsername);
                	packageParticipant.setModifiedBy(currentUsername);
                	
                    packageParticipantMap.put(key, packageParticipant);
                }
            }
        } catch (Exception e) {
            LogUtil.error(PackageDefinitionDaoImpl.class.getName(), e, "");
        }
                
        packageDef.setPackageActivityFormMap(packageActivityFormMap);
        packageDef.setPackageActivityPluginMap(packageActivityPluginMap);
        packageDef.setPackageParticipantMap(packageParticipantMap);

        // save app and package definition
        saveOrUpdate(packageDef);
        return packageDef;
    }

    public void addAppActivityForm(String appId, Long appVersion, PackageActivityForm activityForm) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        if (packageDef == null) {
            AppDefinition appDef = getAppDefinitionDao().loadVersion(appId, appVersion);
            packageDef = createPackageDefinition(appDef, appVersion);
        }
        String processDefId = activityForm.getProcessDefId();
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        activityForm.setProcessDefId(processDefId);
        String activityDefId = activityForm.getActivityDefId();
        if (processDefId != null && activityDefId != null) {
            packageDef.removePackageActivityForm(processDefId, activityDefId);
            saveOrUpdate(packageDef);
        }
        
        String currentUsername = WorkflowUtil.getCurrentUsername();
        Date currentDate = new Date();
        activityForm.setCreatedBy(currentUsername);
        activityForm.setDateCreated(currentDate);
        activityForm.setModifiedBy(currentUsername);
        activityForm.setDateModified(currentDate);
        
        packageDef.addPackageActivityForm(activityForm);
        saveOrUpdate(packageDef);
    }

    public void removeAppActivityForm(String appId, Long appVersion, String processDefId, String activityDefId) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        packageDef.removePackageActivityForm(processDefId, activityDefId);
        saveOrUpdate(packageDef);
    }

    public void addAppActivityPlugin(String appId, Long appVersion, PackageActivityPlugin activityPlugin) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        if (packageDef == null) {
            AppDefinition appDef = getAppDefinitionDao().loadVersion(appId, appVersion);
            packageDef = createPackageDefinition(appDef, appVersion);
        }
        String processDefId = activityPlugin.getProcessDefId();
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        activityPlugin.setProcessDefId(processDefId);
        String activityDefId = activityPlugin.getActivityDefId();
        if (processDefId != null && activityDefId != null) {
            packageDef.removePackageActivityPlugin(processDefId, activityDefId);
            saveOrUpdate(packageDef);
        }
        
        String currentUsername = WorkflowUtil.getCurrentUsername();
        Date currentDate = new Date();
        activityPlugin.setCreatedBy(currentUsername);
        activityPlugin.setDateCreated(currentDate);
        activityPlugin.setModifiedBy(currentUsername);
        activityPlugin.setDateModified(currentDate);
        
        packageDef.addPackageActivityPlugin(activityPlugin);
        saveOrUpdate(packageDef);
    }

    public void removeAppActivityPlugin(String appId, Long appVersion, String processDefId, String activityDefId) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        packageDef.removePackageActivityPlugin(processDefId, activityDefId);
        saveOrUpdate(packageDef);
    }

    public void addAppParticipant(String appId, Long appVersion, PackageParticipant participant) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        if (packageDef == null) {
            AppDefinition appDef = getAppDefinitionDao().loadVersion(appId, appVersion);
            packageDef = createPackageDefinition(appDef, appVersion);
        }
        String processDefId = participant.getProcessDefId();
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        participant.setProcessDefId(processDefId);
        String participantId = participant.getParticipantId();
        if (processDefId != null && participantId != null) {
            packageDef.removePackageParticipant(processDefId, participantId);
            saveOrUpdate(packageDef);
        }
        String currentUsername = WorkflowUtil.getCurrentUsername();
        Date currentDate = new Date();
        participant.setCreatedBy(currentUsername);
        participant.setDateCreated(currentDate);
        participant.setModifiedBy(currentUsername);
        participant.setDateModified(currentDate);
        
        packageDef.addPackageParticipant(participant);
        saveOrUpdate(packageDef);
    }

    public void removeAppParticipant(String appId, Long appVersion, String processDefId, String participantId) {
        PackageDefinition packageDef = loadAppPackageDefinition(appId, appVersion);
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        packageDef.removePackageParticipant(processDefId, participantId);
        saveOrUpdate(packageDef);
    }
}
