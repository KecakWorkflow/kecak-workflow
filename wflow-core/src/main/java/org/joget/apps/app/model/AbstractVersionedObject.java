package org.joget.apps.app.model;

import java.io.Serializable;
import java.util.Date;
import org.simpleframework.xml.Element;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;
import org.springmodules.validation.bean.conf.loader.annotation.handler.RegExp;

/**
 * Abstract class to represent an object that is versioned.
 */
public class AbstractVersionedObject implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4743635796515056705L;
	
	public static final String ID_SEPARATOR = "_";
    public static final String VERSION_LATEST = "latest";
    @Element(required = false)
    private String appId;
    
    @NotBlank
    @RegExp(value = "^[0-9a-zA-Z_]+$")
    @Element(required = false)
    private String id;
    
    @Element(required = false)
    private Long version = Long.valueOf(1);
    
    @NotBlank
    @Element(required = false)
    private String name;
    
    @Element(required = false)
    private Date dateCreated;
    
    @Element(required = false)
    private Date dateModified;
    
    @Element(required = false)
	private String createdBy;
	
	@Element(required = false)
	private String modifiedBy;
	
	@Element(required = false)
	private Boolean deleted;

    /**
     * Unique ID (primary key) for the object, which consists of the ID and version separated by underscore.
     * @return
     */
    public String getUid() {
        String key = null;
        String objId = getId();
        Long objVersion = getVersion();
        if (objId != null) {
            key = objId + ID_SEPARATOR + objVersion;
        }
        return key;
    }

    public void setUid(String uid) {
        String pkgId = null;
        Long pkgVersion = Long.valueOf(1);
        if (uid != null) {
            int i = uid.lastIndexOf(ID_SEPARATOR);
            if (i > 0) {
                pkgId = uid.substring(0, i);
                try {
                    pkgVersion = Long.parseLong(uid.substring(i + 1));
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        setId(pkgId);
        setVersion(pkgVersion);
    }

    /**
     * ID for the overall package (app) this object belongs to.
     * @return
     */
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * Identifier for the object. Each ID may have multiple versions.
     * @return
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
}
