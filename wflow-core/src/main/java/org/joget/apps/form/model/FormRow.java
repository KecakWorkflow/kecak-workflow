package org.joget.apps.form.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.TimeZoneUtil;

/**
 * Represents a row of form data
 */
public class FormRow extends Properties {
    /**
	 * 
	 */
	private static final long serialVersionUID = 9003210642246560176L;
	
	Map<String, String[]> tempFilePathMap;
	
	public FormRow() {
        super();
    }

    public FormRow(Properties defaults) {
        super(defaults);
    }

    /**
     * Used for storing/loading data via hibernate
     * @return
     */
    @SuppressWarnings("rawtypes")
	public Map getCustomProperties() {
        return this;
    }

    public void setCustomProperties(@SuppressWarnings("rawtypes") Map customProperties) {
        if (customProperties != null) {
            for (@SuppressWarnings("rawtypes")
			Iterator i = customProperties.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                Object value = customProperties.get(key);
                if (value != null) {
                    put(key, value);
                }
            }
        }
    }

    //--- Standard row metadata properties below
    /**
     * Identifier/primary key for the row
     * @return
     */
    public String getId() {
        return getProperty(FormUtil.PROPERTY_ID);
    }

    public void setId(String id) {
        if (id != null) {
            setProperty(FormUtil.PROPERTY_ID, id);
        } else {
            remove(FormUtil.PROPERTY_ID);
        }
    }

    /**
     * Date the row was created
     * @return
     */
    public Date getDateCreated() {
        Date date = null;
        Object obj = get(FormUtil.PROPERTY_DATE_CREATED);
        if (obj != null && obj instanceof Date) {
            date = (Date) obj;
        }
        return date;
    }

    public void setDateCreated(Date date) {
        if (date != null) {
            put(FormUtil.PROPERTY_DATE_CREATED, date);
        } else {
            remove(FormUtil.PROPERTY_DATE_CREATED);
        }
    }

    /**
     * Date the row was created
     * @return
     */
    public Date getDateModified() {
        Date date = null;
        Object obj = get(FormUtil.PROPERTY_DATE_MODIFIED);
        if (obj != null && obj instanceof Date) {
            date = (Date) obj;
        }
        return date;
    }

    public void setDateModified(Date date) {
        if (date != null) {
            put(FormUtil.PROPERTY_DATE_MODIFIED, date);
        } else {
            remove(FormUtil.PROPERTY_DATE_MODIFIED);
        }
    }

    public String getCreatedBy() {
		String createdBy = null;
    	Object obj = get(FormUtil.PROPERTY_CREATED_BY);
    	if (obj != null) {
    		createdBy = (String) obj;
    	}
    	return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		if (createdBy != null) {
            put(FormUtil.PROPERTY_CREATED_BY, createdBy);
        } else {
            remove(FormUtil.PROPERTY_CREATED_BY);
        }
	}

	public String getModifiedBy() {
		String modifiedBy = null;
    	Object obj = get(FormUtil.PROPERTY_MODIFIED_BY);
    	if (obj != null) {
    		modifiedBy = (String) obj;
    	}
    	return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		if (modifiedBy != null) {
            put(FormUtil.PROPERTY_MODIFIED_BY, modifiedBy);
        } else {
            remove(FormUtil.PROPERTY_MODIFIED_BY);
        }
	}

	public Boolean getDeleted() {
		Boolean deleted = null;
    	Object obj = get(FormUtil.PROPERTY_DELETED);
    	if (obj != null) {
    		deleted = (Boolean) obj;
    	}
    	return deleted;
	}

	public void setDeleted(Boolean deleted) {
		if (deleted != null) {
            put(FormUtil.PROPERTY_DELETED, deleted);
        } else {
            remove(FormUtil.PROPERTY_DELETED);
        }
	}

	@Override
    public boolean equals(Object obj) {
        FormRow row = (FormRow) obj;
        if (getId().equals(row.getId())) {
            return true;
        }
        return false;
    }
    
    public Map<String, String[]> getTempFilePathMap() {
        return tempFilePathMap;
    }
    
    public void setTempFilePathMap(Map<String, String[]> tempFilePathMap) {
        this.tempFilePathMap = tempFilePathMap;
    }
    
    public void putTempFilePath(String fieldId, String path) {
        if (tempFilePathMap == null) {
            tempFilePathMap = new HashMap<String, String[]>();
        }
        tempFilePathMap.put(fieldId, new String[]{path});
    }
    
    public void putTempFilePath(String fieldId, String[] path) {
        if (tempFilePathMap == null) {
            tempFilePathMap = new HashMap<String, String[]>();
        }
        tempFilePathMap.put(fieldId, path);
    }
    
    public String[] getTempFilePaths(String fieldId) {
        if (tempFilePathMap != null) {
            return tempFilePathMap.get(fieldId);
        }
        return null;
    }
    
    public String getTempFilePath(String fieldId) {
        if (tempFilePathMap != null) {
            String[] paths = tempFilePathMap.get(fieldId);
            if (paths != null && paths.length > 0) {
                return paths[0];
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
	public void putAll(FormRow row) {
        super.putAll(row);
        @SuppressWarnings("rawtypes")
		Map files = row.getTempFilePathMap();
        if (files != null && !files.isEmpty()) {
            if (tempFilePathMap == null) {
                tempFilePathMap = new HashMap<String, String[]>();
            }
            tempFilePathMap.putAll(files);
        }
    }
    
    @Override
    public String getProperty(String key) {
        Object oval = super.get(key);
       
        if (oval != null && oval instanceof Date) {
            return TimeZoneUtil.convertToTimeZone((Date) oval, null, AppUtil.getAppDateFormat());
        } else {
            return super.getProperty(key);
        }
    }
}
