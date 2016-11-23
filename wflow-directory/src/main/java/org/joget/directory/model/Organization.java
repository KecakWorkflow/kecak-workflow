package org.joget.directory.model;

import org.joget.commons.spring.model.Auditable;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;
import org.springmodules.validation.bean.conf.loader.annotation.handler.RegExp;

public class Organization implements Serializable, Auditable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3097451140331816922L;
	@NotBlank
    @RegExp(value = "^[0-9a-zA-Z_-]+$")
    private String id;
    @NotBlank
    private String name;
    private String description;
    private String parentId;
    //join
    @SuppressWarnings("rawtypes")
	private Set departments;
    @SuppressWarnings("rawtypes")
	private Set groups;
    @SuppressWarnings("rawtypes")
	private Set grades;
    @SuppressWarnings("rawtypes")
	private Set employments;
    @SuppressWarnings("rawtypes")
	private Set childrens;
    private Organization parent;
    private Boolean readonly = false;
    private Date dateCreated;
	private Date dateModified;
	private String createdBy;
	private String modifiedBy;
	private Boolean deleted;
    
    public Organization getParent() {
        return parent;
    }

    public void setParent(Organization parent) {
        this.parent = parent;
    }

    @SuppressWarnings("rawtypes")
	public Set getGroups() {
        return groups;
    }

    public void setGroups(@SuppressWarnings("rawtypes") Set groups) {
        this.groups = groups;
    }

    @SuppressWarnings("rawtypes")
	public Set getEmployments() {
        return employments;
    }

    public void setEmployments(@SuppressWarnings("rawtypes") Set employments) {
        this.employments = employments;
    }

    @SuppressWarnings("rawtypes")
	public Set getChildrens() {
        return childrens;
    }

    public void setChildrens(@SuppressWarnings("rawtypes") Set childrens) {
        this.childrens = childrens;
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @SuppressWarnings("rawtypes")
	public Set getDepartments() {
        return departments;
    }

    public void setDepartments(@SuppressWarnings("rawtypes") Set departments) {
        this.departments = departments;
    }

    @SuppressWarnings("rawtypes")
	public Set getGrades() {
        return grades;
    }

    public void setGrades(@SuppressWarnings("rawtypes") Set grades) {
        this.grades = grades;
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
