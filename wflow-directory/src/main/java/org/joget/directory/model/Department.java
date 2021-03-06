package org.joget.directory.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.joget.commons.spring.model.Auditable;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;
import org.springmodules.validation.bean.conf.loader.annotation.handler.RegExp;

public class Department implements Serializable, Auditable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4587416722616349065L;
	@NotBlank
    @RegExp(value = "^[0-9a-zA-Z_-]+$")
    private String id;
    @NotBlank
    private String name;
    private String description;
    //join
    @SuppressWarnings("rawtypes")
	private Set employments;
    private Employment hod;
    private Organization organization;
    private Set<Department> childrens;
    private Department parent;
    //others
    private String treeStructure;
    private Boolean readonly = false;
	private Date dateCreated;
	private Date dateModified;
	private String createdBy;
	private String modifiedBy;
	private Boolean deleted;

    public Set<Department> getChildrens() {
        return childrens;
    }

    public void setChildrens(Set<Department> childrens) {
        this.childrens = childrens;
    }

    public Department getParent() {
        return parent;
    }

    public void setParent(Department parent) {
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @SuppressWarnings("rawtypes")
	public Set getEmployments() {
        return employments;
    }

    public void setEmployments(@SuppressWarnings("rawtypes") Set employments) {
        this.employments = employments;
    }

    public Employment getHod() {
        return hod;
    }

    public void setHod(Employment hod) {
        this.hod = hod;
    }

    public String getTreeStructure() {
        return treeStructure;
    }

    public void setTreeStructure(String treeStructure) {
        this.treeStructure = treeStructure;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
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

	public String getAuditTrailId() {
        return id;
    }
}
