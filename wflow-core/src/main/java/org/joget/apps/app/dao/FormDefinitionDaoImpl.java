package org.joget.apps.app.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.hibernate.Query;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.form.model.FormColumnCache;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.util.WorkflowUtil;

/**
 * DAO to load/store FormDefinition objects
 */
public class FormDefinitionDaoImpl extends AbstractAppVersionedObjectDao<FormDefinition> implements FormDefinitionDao {

    public static final String ENTITY_NAME = "FormDefinition";
    
    private FormColumnCache formColumnCache;
    private Cache cache;
    
    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }
    
    private String getCacheKey(String id, String appId, Long version){
        return DynamicDataSourceManager.getCurrentProfile()+"_"+appId+"_"+version+"_FORM_"+id;
    }
    
    public FormColumnCache getFormColumnCache() {
        return formColumnCache;
    }

    public void setFormColumnCache(FormColumnCache formColumnCache) {
        this.formColumnCache = formColumnCache;
    }

    public String getEntityName() {
        return ENTITY_NAME;
    }

    /**
     * Retrieves FormDefinitions mapped to a table name.
     * @param tableName
     * @return
     */
    public Collection<FormDefinition> loadFormDefinitionByTableName(String tableName) {
        // load the form definitions
        String condition = " WHERE e.tableName=?";
        Object[] params = {tableName};
        @SuppressWarnings("unchecked")
		Collection<FormDefinition> results = find(getEntityName(), condition, params, null, null, 0, -1);
        return results;
    }

    @SuppressWarnings("unchecked")
	public Collection<FormDefinition> getFormDefinitionList(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
        String conditions = "";
        @SuppressWarnings("rawtypes")
		List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or name like ? or tableName like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.find(conditions, params.toArray(), appDefinition, sort, desc, start, rows);
    }

    @SuppressWarnings("unchecked")
	public Long getFormDefinitionListCount(String filterString, AppDefinition appDefinition) {
        String conditions = "";
        @SuppressWarnings("rawtypes")
		List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or name like ? or tableName like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.count(conditions, params.toArray(), appDefinition);
    }
    
    protected FormDefinition load(String id, AppDefinition appDefinition) {
        FormDefinition formDef = super.loadById(id, appDefinition);
        return formDef;
    }

    @SuppressWarnings("deprecation")
	@Override
    public FormDefinition loadById(String id, AppDefinition appDefinition) {
        String cacheKey = getCacheKey(id, appDefinition.getAppId(), appDefinition.getVersion());
        Element element = cache.get(cacheKey);

        if (element == null) {
            FormDefinition formDef = load(id, appDefinition);

            if (formDef != null) {
                element = new Element(cacheKey, (Serializable) formDef);
                cache.put(element);
            }
            return formDef;
        } else {
            return (FormDefinition) element.getValue();
        }
    }
    
    @Override
    public boolean add(FormDefinition object) {
        formColumnCache.remove(object.getTableName());
        
        Date currentDate = new Date();
        object.setDateCreated(currentDate);
        object.setDateModified(currentDate);
        
        String currentUsername = WorkflowUtil.getCurrentUsername();
        object.setCreatedBy(currentUsername);
        object.setModifiedBy(currentUsername);
        return super.add(object);
    }

    @Override
    public boolean update(FormDefinition object) {
        // clear from cache
        formColumnCache.remove(object.getTableName());
        cache.remove(getCacheKey(object.getId(), object.getAppId(), object.getAppVersion()));
        
        // update object
        object.setDateModified(new Date());
        
        String currentUsername = WorkflowUtil.getCurrentUsername();
        object.setModifiedBy(currentUsername);
        return super.update(object);
    }

    @Override
    public boolean delete(String id, AppDefinition appDef) {
        boolean result = false;
        try {
            FormDefinition obj = super.loadById(id, appDef);

            // detach from app
            if (obj != null) {
                Collection<FormDefinition> list = appDef.getFormDefinitionList();
                if (list != null) {
                    for (FormDefinition object : list) {
                        if (obj.getId().equals(object.getId())) {
                            list.remove(obj);
                            break;
                        }
                    }
                }
                obj.setAppDefinition(null);

                // delete obj
                super.delete(getEntityName(), obj);
                result = true;
                
                // clear from cache
                formColumnCache.remove(obj.getTableName());
                cache.remove(getCacheKey(id, appDef.getId(), appDef.getVersion()));
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
        return result;
    }

    @SuppressWarnings("unchecked")
	public Collection<String> getTableNameList(AppDefinition appDefinition) {
        final AppDefinition appDef = appDefinition;
        
        String query = "SELECT DISTINCT e.tableName FROM " + getEntityName() + " e where e.appId = ? and e.appVersion = ?";

        Query q = findSession().createQuery(query);
        q.setParameter(0, appDef.getAppId());
        q.setParameter(1, appDef.getVersion());

        return q.list();
    }
}