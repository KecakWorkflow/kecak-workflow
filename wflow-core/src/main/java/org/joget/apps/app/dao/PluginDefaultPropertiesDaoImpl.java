package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.util.WorkflowUtil;

public class PluginDefaultPropertiesDaoImpl extends AbstractAppVersionedObjectDao<PluginDefaultProperties> implements PluginDefaultPropertiesDao {

    public static final String ENTITY_NAME = "PluginDefaultProperties";

    public String getEntityName() {
        return ENTITY_NAME;
    }

    @SuppressWarnings("unchecked")
	public Collection<PluginDefaultProperties> getPluginDefaultPropertiesList(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
        String conditions = "";
        @SuppressWarnings("rawtypes")
		List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or pluginName like ? or pluginDescription like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.find(conditions, params.toArray(), appDefinition, sort, desc, start, rows);
    }

    @SuppressWarnings("unchecked")
	public Long getPluginDefaultPropertiesListCount(String filterString, AppDefinition appDefinition) {
        String conditions = "";
        @SuppressWarnings("rawtypes")
		List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or pluginName like ? or pluginDescription like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.count(conditions, params.toArray(), appDefinition);
    }

    @Override
    public boolean delete(String id, AppDefinition appDef) {
        boolean result = false;
        try {
            PluginDefaultProperties obj = loadById(id, appDef);

            // detach from app
            if (obj != null) {
                Collection<PluginDefaultProperties> list = appDef.getPluginDefaultPropertiesList();
                for (PluginDefaultProperties object : list) {
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
    
    @Override
    public boolean add(PluginDefaultProperties object) {
    	String currentUsername = WorkflowUtil.getCurrentUsername();
        object.setCreatedBy(currentUsername);
        object.setModifiedBy(currentUsername);
        
        Date currentDate = new Date();
        object.setDateCreated(currentDate);
        object.setDateModified(currentDate);
        return super.add(object);
    }

    @Override
    public boolean update(PluginDefaultProperties object) {
        String currentUsername = WorkflowUtil.getCurrentUsername();
        object.setModifiedBy(currentUsername);
        
        object.setDateModified(new Date());
        return super.update(object);
    }
}
