package org.joget.apps.app.model;

import java.util.Date;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;

/**
 * Metadata for a userview.
 */
@Root
public class UserviewDefinition extends AbstractAppVersionedObject {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2863974971446339688L;
	@NotBlank
    @Element(required = false)
    private String name;
	
    @Element(required = false)
    private String description;
    
    @Element(required = false)
    private String json;
    
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

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
