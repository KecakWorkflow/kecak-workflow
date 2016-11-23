package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.EnvironmentVariable;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.util.WorkflowUtil;

public class EnvironmentVariableDaoImpl extends AbstractAppVersionedObjectDao<EnvironmentVariable> implements EnvironmentVariableDao {

    public static final String ENTITY_NAME = "EnvironmentVariable";

    public String getEntityName() {
        return ENTITY_NAME;
    }

    @SuppressWarnings("unchecked")
	public Collection<EnvironmentVariable> getEnvironmentVariableList(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
        String conditions = "";
        @SuppressWarnings("rawtypes")
		List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or value like ? or remarks like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.find(conditions, params.toArray(), appDefinition, sort, desc, start, rows);
    }

    @SuppressWarnings("unchecked")
	public Long getEnvironmentVariableListCount(String filterString, AppDefinition appDefinition) {
        String conditions = "";
        @SuppressWarnings("rawtypes")
		List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or value like ? or remarks like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.count(conditions, params.toArray(), appDefinition);
    }

    @Override
    public boolean delete(String id, AppDefinition appDef) {
        boolean result = false;
        try {
            EnvironmentVariable obj = loadById(id, appDef);

            // detach from app
            if (obj != null) {
                Collection<EnvironmentVariable> list = appDef.getEnvironmentVariableList();
                for (EnvironmentVariable object : list) {
                    if (obj.getId().equals(object.getId())) {
                        list.remove(obj);
                        break;
                    }
                }
                obj.setAppDefinition(null);

                // delete obj
                super.delete(getEntityName(), obj);
                result = true;
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
        return result;
    }
    
    public Integer getIncreasedCounter(final String id, final String remark, final AppDefinition appDef) {
        Integer count = 0;
        
        try {
            EnvironmentVariable env = loadByIdForUpdate(id, appDef);
            if (env != null && env.getValue() != null && env.getValue().trim().length() > 0) {
                count = Integer.parseInt(env.getValue());
            }
            count += 1;

            if (env == null) {
                env = new EnvironmentVariable();
                env.setAppDefinition(appDef);
                env.setAppId(appDef.getId());
                env.setAppVersion(appDef.getVersion());
                env.setId(id);
                env.setRemarks(remark);
                env.setValue(Integer.toString(count));
                add(env);
            } else {
                env.setValue(Integer.toString(count));
                update(env);
            }
        } catch (Exception e) {
            LogUtil.error(EnvironmentVariableDaoImpl.class.getName(), e, id);
        }
        return count;
    }
    
    @Override
    public boolean add(EnvironmentVariable object) {
    	String currentUsername = WorkflowUtil.getCurrentUsername();
        object.setCreatedBy(currentUsername);
        object.setModifiedBy(currentUsername);
        
        Date currentDate = new Date();
        object.setDateCreated(currentDate);
        object.setDateModified(currentDate);
        return super.add(object);
    }

    @Override
    public boolean update(EnvironmentVariable object) {
        String currentUsername = WorkflowUtil.getCurrentUsername();
        object.setModifiedBy(currentUsername);
        
        object.setDateModified(new Date());
        return super.update(object);
    }
}
