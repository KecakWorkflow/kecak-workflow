package org.joget.apps.app.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.io.comparator.NameFileComparator;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.dao.EnvironmentVariableDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.dao.JdbcDataListDao;
import org.joget.apps.app.dao.MessageDao;
import org.joget.apps.app.dao.PackageDefinitionDao;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.EnvironmentVariable;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.ImportAppException;
import org.joget.apps.app.model.Message;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.service.JsonUtil;
import org.joget.apps.ext.ConsoleWebPlugin;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.lib.DefaultFormBinder;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.generator.service.GeneratorUtil;
import org.joget.apps.property.PropertiesTemplate;
import org.joget.apps.property.dao.PropertyDao;
import org.joget.apps.property.model.Property;
import org.joget.apps.route.KecakRouteManager;
import org.joget.apps.scheduler.SchedulerManager;
import org.joget.apps.scheduler.dao.SchedulerDetailsDao;
import org.joget.apps.scheduler.dao.SchedulerLogDao;
import org.joget.apps.scheduler.model.SchedulerDetails;
import org.joget.apps.scheduler.model.SchedulerLog;
import org.joget.apps.scheduler.model.TriggerTypes;
import org.joget.apps.userview.service.UserviewService;
import org.joget.commons.spring.model.EmailApprovalContent;
import org.joget.commons.spring.model.EmailApprovalContentDao;
import org.joget.commons.spring.model.ResourceBundleMessage;
import org.joget.commons.spring.model.ResourceBundleMessageDao;
import org.joget.commons.spring.model.Setting;
import org.joget.commons.util.CsvUtil;
import org.joget.commons.util.DateUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.FileLimitException;
import org.joget.commons.util.FileStore;
import org.joget.commons.util.HashSalt;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.PagingUtils;
import org.joget.commons.util.PasswordGeneratorUtil;
import org.joget.commons.util.PasswordValidator;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.TimeZoneUtil;
import org.joget.directory.dao.DepartmentDao;
import org.joget.directory.dao.EmploymentDao;
import org.joget.directory.dao.GradeDao;
import org.joget.directory.dao.GroupDao;
import org.joget.directory.dao.OrganizationDao;
import org.joget.directory.dao.RoleDao;
import org.joget.directory.dao.UserDao;
import org.joget.directory.dao.UserSaltDao;
import org.joget.directory.model.Department;
import org.joget.directory.model.Employment;
import org.joget.directory.model.Grade;
import org.joget.directory.model.Group;
import org.joget.directory.model.Organization;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import org.joget.directory.model.UserSalt;
import org.joget.directory.model.service.DirectoryManagerPlugin;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.directory.model.service.UserSecurity;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.ParticipantPlugin;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowParticipant;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowVariable;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.util.ClassUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.util.HtmlUtils;

import au.com.bytecode.opencsv.CSVWriter;

@SuppressWarnings("restriction")
@Controller
public class ConsoleWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleWebController.class);

    public static final String APP_ZIP_PREFIX = "APP_";
    @Autowired
    UserDao userDao;
    @Autowired
    UserSaltDao userSaltDao;
    @Autowired
    OrganizationDao organizationDao;
    @Autowired
    DepartmentDao departmentDao;
    @Autowired
    GradeDao gradeDao;
    @Autowired
    GroupDao groupDao;
    @Autowired
    RoleDao roleDao;
    @Autowired
    EmploymentDao employmentDao;
    @Autowired
    WorkflowManager workflowManager;
    @Autowired
    WorkflowUserManager workflowUserManager;
    @Autowired
    PluginManager pluginManager;
    @Autowired
    @Qualifier("main")
    ExtDirectoryManager directoryManager;
    @Autowired
    ResourceBundleMessageDao rbmDao;
    @Autowired
    EmailApprovalContentDao eaContentDao;
    @Autowired
    Validator validator;
    @Autowired
    AppService appService;
    @Autowired
    UserviewService userviewService;
    @Autowired
    FormService formService;
    @Autowired
    SetupManager setupManager;
    @Resource
    AppDefinitionDao appDefinitionDao;
    @Resource
    FormDefinitionDao formDefinitionDao;
    @Resource
    PackageDefinitionDao packageDefinitionDao;
    @Resource
    MessageDao messageDao;
    @Resource
    EnvironmentVariableDao environmentVariableDao;
    @Resource
    PluginDefaultPropertiesDao pluginDefaultPropertiesDao;
    @Resource
    UserviewDefinitionDao userviewDefinitionDao;
    @Resource
    DatalistDefinitionDao datalistDefinitionDao;
    @Resource
    FormDataDao formDataDao;
    @Autowired
    LocaleResolver localeResolver;
    @Autowired
    JdbcDataListDao jdbcDataListDao;
    @Autowired
    KecakRouteManager kecakRouteManager;
    @Autowired
    SchedulerManager schedulerManager;
    @Autowired
    SchedulerDetailsDao schedulerDetailsDao;
    @Autowired
    SchedulerLogDao schedulerLogDao;
    @Autowired
    PropertiesTemplate applicationProperties;
    @Autowired
    PropertyDao propertyDao;

    @RequestMapping({"/index", "/", "/home"})
    public String index() {
        String landingPage = WorkflowUtil.getSystemSetupValue("landingPage");

        if (landingPage == null || landingPage.trim().isEmpty()) {
            landingPage = "/home";
        }
        return "redirect:" + landingPage;
    }

    @RequestMapping({"/console", "/console/home"})
    public String consoleHome() {
        return "console/home";
    }

    @RequestMapping("/help/guide")
    public void consoleHelpGuide(Writer writer, @RequestParam("key") String key) throws IOException {
        if (key != null && !key.trim().isEmpty()) {
            String message = ResourceBundleUtil.getMessage(key);
            if (message != null && !message.trim().isEmpty()) {
                message = pluginManager.processPluginTranslation(message, getClass().getName(), "console");
                writer.write(message);
            }
        }
    }

    @RequestMapping("/console/directory/orgs")
    public String consoleOrgList(ModelMap model) {
        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());
        return "console/directory/orgList";
    }

    @RequestMapping("/console/directory/org/create")
    public String consoleOrgCreate(ModelMap model) {
        model.addAttribute("organization", new Organization());
        return "console/directory/orgCreate";
    }

    @RequestMapping("/console/directory/org/view/(*:id)")
    public String consoleOrgView(ModelMap model, @RequestParam("id") String id) {
        model.addAttribute("organization", directoryManager.getOrganization(id));
        Collection<Department> departments = directoryManager.getDepartmentsByOrganizationId(null, id, "name", false, null, null);
        Collection<Grade> grades = directoryManager.getGradesByOrganizationId(null, id, "name", false, null, null);
        model.addAttribute("departments", departments);
        model.addAttribute("grades", grades);
        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());
        return "console/directory/orgView";
    }

    @RequestMapping("/console/directory/org/edit/(*:id)")
    public String consoleOrgEdit(ModelMap model, @RequestParam("id") String id) {
        model.addAttribute("organization", organizationDao.getOrganization(id));
        return "console/directory/orgEdit";
    }

    @RequestMapping(value = "/console/directory/org/submit/(*:action)", method = RequestMethod.POST)
    public String consoleOrgSubmit(ModelMap model, @RequestParam("action") String action, @ModelAttribute("organization") Organization organization, BindingResult result) {
        // validate ID
        validator.validate(organization, result);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check id exist
                if (organizationDao.getOrganization(organization.getId()) != null) {
                    errors.add("console.directory.org.error.label.idExists");
                } else {
                    invalid = !organizationDao.addOrganization(organization);
                }
            } else {
                Organization o = organizationDao.getOrganization(organization.getId());
                o.setName(organization.getName());
                o.setDescription(organization.getDescription());
                invalid = !organizationDao.updateOrganization(o);
            }

            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            model.addAttribute("organization", organization);
            if ("create".equals(action)) {
                return "console/directory/orgCreate";
            } else {
                return "console/directory/orgEdit";
            }
        } else {
            String id = organization.getId();
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/directory/org/view/" + id;
            model.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping(value = "/console/directory/org/delete", method = RequestMethod.POST)
    public String consoleOrgDelete(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String organizationId = (String) strToken.nextElement();
            organizationDao.deleteOrganization(organizationId);
        }
        return "console/directory/orgList";
    }

    @RequestMapping("/console/directory/org/(*:id)/user/assign/view")
    public String consoleOrgUserAssign(ModelMap model, @RequestParam(value = "id") String id) {
        model.addAttribute("id", id);
        return "console/directory/orgUserAssign";
    }

    @RequestMapping(value = "/console/directory/org/(*:id)/user/assign/submit", method = RequestMethod.POST)
    public String consoleOrgUserAssignSubmit(ModelMap model, @RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String userId = (String) strToken.nextElement();
            employmentDao.assignUserToOrganization(userId, id);
        }
        return "console/directory/orgUserAssign";
    }

    @RequestMapping(value = "/console/directory/org/(*:id)/user/unassign", method = RequestMethod.POST)
    public String consoleOrgUserUnassign(@RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String userId = (String) strToken.nextElement();
            employmentDao.unassignUserFromOrganization(userId, id);
        }
        return "console/directory/orgView";
    }

    @RequestMapping("/console/directory/dept/create")
    public String consoleDeptCreate(ModelMap model, @RequestParam("orgId") String orgId, @RequestParam(value = "parentId", required = false) String parentId) {
        model.addAttribute("organization", organizationDao.getOrganization(orgId));
        model.addAttribute("department", new Department());
        if (parentId != null && parentId.trim().length() > 0) {
            model.addAttribute("parent", departmentDao.getDepartment(parentId));
        }
        return "console/directory/deptCreate";
    }

    @RequestMapping("/console/directory/dept/view/(*:id)")
    public String consoleDeptView(ModelMap model, @RequestParam("id") String id) {
        Department department = directoryManager.getDepartmentById(id);
        model.addAttribute("department", department);

        User hod = directoryManager.getDepartmentHod(id);
        model.addAttribute("hod", hod);

        if (department != null && department.getOrganization() != null) {
            Collection<Grade> grades = directoryManager.getGradesByOrganizationId(null, department.getOrganization().getId(), "name", false, null, null);
            model.addAttribute("grades", grades);
        }

        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());

        return "console/directory/deptView";
    }

    @RequestMapping("/console/directory/dept/edit/(*:id)")
    public String consoleDeptEdit(ModelMap model, @RequestParam("id") String id, @RequestParam("orgId") String orgId, @RequestParam(value = "parentId", required = false) String parentId) {
        model.addAttribute("organization", organizationDao.getOrganization(orgId));
        model.addAttribute("department", departmentDao.getDepartment(id));
        if (parentId != null && parentId.trim().length() > 0) {
            model.addAttribute("parent", departmentDao.getDepartment(parentId));
        }
        return "console/directory/deptEdit";
    }

    @RequestMapping(value = "/console/directory/dept/submit/(*:action)", method = RequestMethod.POST)
    public String consoleDeptSubmit(ModelMap model, @RequestParam("action") String action, @RequestParam("orgId") String orgId, @RequestParam(value = "parentId", required = false) String parentId, @ModelAttribute("department") Department department, BindingResult result) {
        Organization organization = organizationDao.getOrganization(orgId);
        Department parent = null;
        if (parentId != null && parentId.trim().length() > 0) {
            parent = departmentDao.getDepartment(parentId);
        }

        // validate ID
        validator.validate(department, result);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check id exist
                if (departmentDao.getDepartment(department.getId()) != null) {
                    errors.add("console.directory.department.error.label.idExists");
                } else {
                    department.setOrganization(organization);
                    if (parent != null) {
                        department.setParent(parent);
                    }
                    invalid = !departmentDao.addDepartment(department);
                }
            } else {
                Department d = departmentDao.getDepartment(department.getId());
                d.setName(department.getName());
                d.setDescription(department.getDescription());
                invalid = !departmentDao.updateDepartment(d);
            }

            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            model.addAttribute("organization", organization);
            model.addAttribute("department", department);
            if (parent != null) {
                model.addAttribute("parent", parent);
            }
            if ("create".equals(action)) {
                return "console/directory/deptCreate";
            } else {
                return "console/directory/deptEdit";
            }
        } else {
            String id = organization.getId();
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath;
            if ("create".equals(action)) {
                if (parent != null) {
                    url += "/web/console/directory/dept/view/" + parent.getId();
                } else {
                    url += "/web/console/directory/org/view/" + id;
                }
            } else {
                url += "/web/console/directory/dept/view/" + department.getId();
            }
            model.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping(value = "/console/directory/dept/delete", method = RequestMethod.POST)
    public String consoleDeptDelete(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            departmentDao.deleteDepartment(id);
        }
        return "console/directory/orgList";
    }

    @RequestMapping("/console/directory/dept/(*:id)/hod/set/view")
    public String consoleDeptHodSet(ModelMap model, @RequestParam(value = "id") String id) {
        model.addAttribute("id", id);
        Department department = departmentDao.getDepartment(id);
        if (department != null && department.getOrganization() != null) {
            Collection<Grade> grades = directoryManager.getGradesByOrganizationId(null, department.getOrganization().getId(), "name", false, null, null);
            model.addAttribute("grades", grades);
        }
        return "console/directory/deptHodSetView";
    }

    @RequestMapping(value = "/console/directory/dept/(*:deptId)/hod/set/submit", method = RequestMethod.POST)
    public String consoleDeptHodSetSubmit(ModelMap model, @RequestParam(value = "deptId") String deptId, @RequestParam(value = "userId") String userId) {
        employmentDao.assignUserAsDepartmentHOD(userId, deptId);
        return "console/directory/deptHodSetView";
    }

    @RequestMapping(value = "/console/directory/dept/(*:deptId)/hod/remove", method = RequestMethod.POST)
    public String consoleDeptHodRemove(@RequestParam(value = "deptId") String deptId, @RequestParam(value = "userId") String userId) {
        employmentDao.unassignUserAsDepartmentHOD(userId, deptId);
        return "console/directory/deptView";
    }

    @RequestMapping("/console/directory/dept/(*:id)/user/assign/view")
    public String consoleDeptUserAssign(ModelMap model, @RequestParam(value = "id") String id) {
        model.addAttribute("id", id);
        Department department = directoryManager.getDepartmentById(id);
        if (department != null && department.getOrganization() != null) {
            model.addAttribute("organizationId", department.getOrganization().getId());
        }
        return "console/directory/deptUserAssign";
    }

    @RequestMapping(value = "/console/directory/dept/(*:id)/user/assign/submit", method = RequestMethod.POST)
    public String consoleDeptUserAssignSubmit(ModelMap model, @RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String userId = (String) strToken.nextElement();
            employmentDao.assignUserToDepartment(userId, id);
        }
        return "console/directory/deptUserAssign";
    }

    @RequestMapping(value = "/console/directory/dept/(*:id)/user/unassign", method = RequestMethod.POST)
    public String consoleDeptUserUnassign(@RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String userId = (String) strToken.nextElement();
            employmentDao.unassignUserFromDepartment(userId, id);
        }
        return "console/directory/deptView";
    }

    @RequestMapping("/console/directory/grade/create")
    public String consoleGradeCreate(ModelMap model, @RequestParam("orgId") String orgId) {
        model.addAttribute("organization", organizationDao.getOrganization(orgId));
        model.addAttribute("grade", new Grade());
        return "console/directory/gradeCreate";
    }

    @RequestMapping("/console/directory/grade/view/(*:id)")
    public String consoleGradeView(ModelMap model, @RequestParam("id") String id) {
        Grade grade = directoryManager.getGradeById(id);
        model.addAttribute("grade", grade);

        if (grade != null && grade.getOrganization() != null) {
            Collection<Department> departments = directoryManager.getDepartmentsByOrganizationId(null, grade.getOrganization().getId(), "name", false, null, null);
            model.addAttribute("departments", departments);
        }

        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());
        return "console/directory/gradeView";
    }

    @RequestMapping("/console/directory/grade/edit/(*:id)")
    public String consoleGradeEdit(ModelMap model, @RequestParam("id") String id, @RequestParam("orgId") String orgId) {
        model.addAttribute("organization", organizationDao.getOrganization(orgId));
        model.addAttribute("grade", gradeDao.getGrade(id));
        return "console/directory/gradeEdit";
    }

    @RequestMapping(value = "/console/directory/grade/submit/(*:action)", method = RequestMethod.POST)
    public String consoleGradeSubmit(ModelMap model, @RequestParam("action") String action, @RequestParam("orgId") String orgId, @ModelAttribute("grade") Grade grade, BindingResult result) {
        Organization organization = organizationDao.getOrganization(orgId);

        // validate ID
        validator.validate(grade, result);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check id exist
                if (gradeDao.getGrade(grade.getId()) != null) {
                    errors.add("console.directory.grade.error.label.idExists");
                } else {
                    grade.setOrganization(organization);
                    invalid = !gradeDao.addGrade(grade);
                }
            } else {
                Grade g = gradeDao.getGrade(grade.getId());
                g.setName(grade.getName());
                g.setDescription(grade.getDescription());
                invalid = !gradeDao.updateGrade(g);
            }

            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            model.addAttribute("organization", organization);
            model.addAttribute("grade", grade);
            if ("create".equals(action)) {
                return "console/directory/gradeCreate";
            } else {
                return "console/directory/gradeEdit";
            }
        } else {
            String id = organization.getId();
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath;
            if ("create".equals(action)) {
                url += "/web/console/directory/org/view/" + id;
            } else {
                url += "/web/console/directory/grade/view/" + grade.getId();
            }
            model.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping(value = "/console/directory/grade/delete", method = RequestMethod.POST)
    public String consoleGradeDelete(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            gradeDao.deleteGrade(id);
        }
        return "console/directory/orgList";
    }

    @RequestMapping("/console/directory/grade/(*:id)/user/assign/view")
    public String consoleGradeUserAssign(ModelMap model, @RequestParam(value = "id") String id) {
        model.addAttribute("id", id);
        Grade grade = directoryManager.getGradeById(id);
        if (grade != null && grade.getOrganization() != null) {
            model.addAttribute("organizationId", grade.getOrganization().getId());
        }
        return "console/directory/gradeUserAssign";
    }

    @RequestMapping(value = "/console/directory/grade/(*:id)/user/assign/submit", method = RequestMethod.POST)
    public String consoleGradeUserAssignSubmit(ModelMap model, @RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String userId = (String) strToken.nextElement();
            employmentDao.assignUserToGrade(userId, id);
        }
        return "console/directory/gradeUserAssign";
    }

    @RequestMapping(value = "/console/directory/grade/(*:id)/user/unassign", method = RequestMethod.POST)
    public String consoleGradeUserUnassign(@RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String userId = (String) strToken.nextElement();
            employmentDao.unassignUserFromGrade(userId, id);
        }
        return "console/directory/gradeView";
    }

    @RequestMapping("/console/directory/groups")
    public String consoleGroupList(ModelMap model) {
        Collection<Organization> organizations = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);
        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());
        return "console/directory/groupList";
    }

    @RequestMapping("/console/directory/group/create")
    public String consoleGroupCreate(ModelMap model) {
        Collection<Organization> organizations = organizationDao.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);
        model.addAttribute("group", new Group());
        return "console/directory/groupCreate";
    }

    @RequestMapping("/console/directory/group/view/(*:id)")
    public String consoleGroupView(ModelMap model, @RequestParam("id") String id) {
        model.addAttribute("group", directoryManager.getGroupById(id));
        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());
        return "console/directory/groupView";
    }

    @RequestMapping("/console/directory/group/edit/(*:id)")
    public String consoleGroupEdit(ModelMap model, @RequestParam("id") String id) {
        Collection<Organization> organizations = organizationDao.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);
        Group group = groupDao.getGroup(id);
        if (group.getOrganization() != null) {
            group.setOrganizationId(group.getOrganization().getId());
        }
        model.addAttribute("group", groupDao.getGroup(id));
        return "console/directory/groupEdit";
    }

    @RequestMapping(value = "/console/directory/group/submit/(*:action)", method = RequestMethod.POST)
    public String consoleGroupSubmit(ModelMap model, @RequestParam("action") String action, @ModelAttribute("group") Group group, BindingResult result) {
        // validate ID
        validator.validate(group, result);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check id exist
                if (groupDao.getGroup(group.getId()) != null) {
                    errors.add("console.directory.group.error.label.idExists");
                } else {
                    if (group.getOrganizationId() != null && group.getOrganizationId().trim().length() > 0) {
                        group.setOrganization(organizationDao.getOrganization(group.getOrganizationId()));
                    }
                    invalid = !groupDao.addGroup(group);
                }
            } else {
                Group g = groupDao.getGroup(group.getId());
                group.setUsers(g.getUsers());
                if (group.getOrganizationId() != null && group.getOrganizationId().trim().length() > 0) {
                    group.setOrganization(organizationDao.getOrganization(group.getOrganizationId()));
                }
                invalid = !groupDao.updateGroup(group);
            }

            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            Collection<Organization> organizations = organizationDao.getOrganizationsByFilter(null, "name", false, null, null);
            model.addAttribute("organizations", organizations);
            model.addAttribute("group", group);
            if ("create".equals(action)) {
                return "console/directory/groupCreate";
            } else {
                return "console/directory/groupEdit";
            }
        } else {
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath;
            url += "/web/console/directory/group/view/" + group.getId();
            model.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping(value = "/console/directory/group/delete", method = RequestMethod.POST)
    public String consoleGroupDelete(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            groupDao.deleteGroup(id);
        }
        return "console/directory/groupList";
    }

    @RequestMapping("/console/directory/group/(*:id)/user/assign/view")
    public String consoleGroupUserAssign(ModelMap model, @RequestParam(value = "id") String id) {
        model.addAttribute("id", id);
        return "console/directory/groupUserAssign";
    }

    @RequestMapping(value = "/console/directory/group/(*:id)/user/assign/submit", method = RequestMethod.POST)
    public String consoleGroupUserAssignSubmit(ModelMap model, @RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String userId = (String) strToken.nextElement();
            userDao.assignUserToGroup(userId, id);
        }
        return "console/directory/groupUserAssign";
    }

    @RequestMapping(value = "/console/directory/group/(*:id)/user/unassign", method = RequestMethod.POST)
    public String consoleGroupUserUnassign(@RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String userId = (String) strToken.nextElement();
            userDao.unassignUserFromGroup(userId, id);
        }
        return "console/directory/groupList";
    }

    @RequestMapping("/console/directory/users")
    public String consoleUserList(ModelMap model) {
        Collection<Organization> organizations = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);
        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());

        return "console/directory/userList";
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/console/directory/user/create")
    public String consoleUserCreate(ModelMap model) {
        Collection<Organization> organizations = organizationDao.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);
        model.addAttribute("roles", roleDao.getRoles(null, "name", false, null, null));
        model.addAttribute("timezones", TimeZoneUtil.getList());

        Map<String, String> status = new HashMap<String, String>();
        status.put("1", "Active");
        status.put("0", "Inactive");
        model.addAttribute("status", status);

        UserSecurity us = DirectoryUtil.getUserSecurity();
        if (us != null) {
            model.addAttribute("userFormFooter", us.getUserCreationFormFooter());
        } else {
            model.addAttribute("userFormFooter", "");
        }

        User user = new User();
        user.setActive(1);
        @SuppressWarnings("rawtypes")
        Set roles = new HashSet();
        roles.add(roleDao.getRole("ROLE_USER"));
        user.setRoles(roles);
        //user.setTimeZone(TimeZoneUtil.getServerTimeZone());
        model.addAttribute("user", user);
        model.addAttribute("employeeDepartmentHod", "no");
        return "console/directory/userCreate";
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/console/directory/user/view/(*:id)")
    public String consoleUserView(ModelMap model, @RequestParam("id") String id) {
        User user = directoryManager.getUserById(id);

        model.addAttribute("user", user);

        if (user != null) {
            //get only 1st employment
            if (user.getEmployments() != null && user.getEmployments().size() > 0) {
                Employment employment = (Employment) user.getEmployments().iterator().next();
                model.addAttribute("employment", directoryManager.getEmployment(employment.getId()));
            }

            //get roles
            String roles = "";
            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (Role role : (Set<Role>) user.getRoles()) {
                    roles += role.getName() + ", ";
                }

                //remove trailing comma
                roles = roles.substring(0, roles.length() - 2);
            }
            model.addAttribute("roles", roles);

            UserSecurity us = DirectoryUtil.getUserSecurity();
            if (us != null) {
                model.addAttribute("addOnButtons", us.getUserDetailsButtons(user));
            }
        }

        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());

        return "console/directory/userView";
    }

    @RequestMapping("/console/directory/user/edit/(*:id)")
    public String consoleUserEdit(ModelMap model, @RequestParam("id") String id) {
        Collection<Organization> organizations = organizationDao.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);
        model.addAttribute("roles", roleDao.getRoles(null, "name", false, null, null));
        model.addAttribute("timezones", TimeZoneUtil.getList());

        Map<String, String> status = new HashMap<String, String>();
        status.put("1", "Active");
        status.put("0", "Inactive");
        model.addAttribute("status", status);

        User user = userDao.getUserById(id);
        model.addAttribute("user", user);

        Employment employment = null;
        if (user.getEmployments() != null && user.getEmployments().size() > 0) {
            employment = (Employment) user.getEmployments().iterator().next();
        } else {
            employment = new Employment();
        }

        model.addAttribute("employeeCode", employment.getEmployeeCode());
        model.addAttribute("employeeRole", employment.getRole());
        model.addAttribute("employeeOrganization", employment.getOrganizationId());
        model.addAttribute("employeeDepartment", employment.getDepartmentId());
        model.addAttribute("employeeGrade", employment.getGradeId());
        model.addAttribute("employeeStartDate", employment.getStartDate());
        model.addAttribute("employeeEndDate", employment.getEndDate());
        model.addAttribute("employeeDepartmentHod", (employment.getHods() != null && employment.getHods().size() > 0) ? "yes" : "no");

        UserSecurity us = DirectoryUtil.getUserSecurity();
        if (us != null) {
            model.addAttribute("userFormFooter", us.getUserEditingFormFooter(user));
        } else {
            model.addAttribute("userFormFooter", "");
        }

        return "console/directory/userEdit";
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @RequestMapping(value = "/console/directory/user/submit/(*:action)", method = RequestMethod.POST)
    public String consoleUserSubmit(ModelMap model, @RequestParam("action") String action, @ModelAttribute("user") User user, BindingResult result,
            @RequestParam(value = "employeeCode", required = false) String employeeCode, @RequestParam(value = "employeeRole", required = false) String employeeRole,
            @RequestParam(value = "employeeOrganization", required = false) String employeeOrganization, @RequestParam(value = "employeeDepartment", required = false) String employeeDepartment,
            @RequestParam(value = "employeeDepartmentHod", required = false) String employeeDepartmentHod, @RequestParam(value = "employeeGrade", required = false) String employeeGrade,
            @RequestParam(value = "employeeStartDate", required = false) String employeeStartDate, @RequestParam(value = "employeeEndDate", required = false) String employeeEndDate) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // validate ID
        validator.validate(user, result);

        UserSecurity us = DirectoryUtil.getUserSecurity();
        UserSalt userSalt = new UserSalt();

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check username exist
                if (directoryManager.getUserByUsername(user.getUsername()) != null || (us != null && us.isDataExist(user.getUsername()))) {
                    errors.add(ResourceBundleUtil.getMessage("console.directory.user.error.label.usernameExists"));
                }

                if (us != null) {
                    Collection<String> validationErrors = us.validateUserOnInsert(user);
                    if (validationErrors != null && !validationErrors.isEmpty()) {
                        errors.addAll(validationErrors);
                    }
                }

                //Check Password Empty
                if (PasswordValidator.isPasswordEmpty(user.getPassword())) {
                    errors.add(ResourceBundleUtil.getMessage("console.directory.user.error.label.passwordNotEmpty"));
                } else if (!PasswordValidator.validate(user.getPassword())) {
                    errors.add(ResourceBundleUtil.getMessage("console.directory.user.error.label.passwordNotValid"));
                }

                errors.addAll(validateEmploymentDate(employeeStartDate, employeeEndDate));

                if (errors.isEmpty()) {
                    user.setId(user.getUsername());
                    if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                        user.setConfirmPassword(user.getPassword());
                        if (us != null) {
                            user.setPassword(us.encryptPassword(user.getUsername(), user.getPassword()));
                        } else {
                            //md5 password
                            //user.setPassword(StringUtil.md5Base16(user.getPassword()));
                            HashSalt hashSalt = PasswordGeneratorUtil.createNewHashWithSalt(user.getPassword());
                            userSalt.setId(UUID.randomUUID().toString());
                            userSalt.setRandomSalt(hashSalt.getSalt());

                            user.setPassword(hashSalt.getHash());
                        }
                    }

                    //set roles
                    if (user.getRoles() != null && user.getRoles().size() > 0) {
                        Set roles = new HashSet();
                        for (String roleId : (Set<String>) user.getRoles()) {
                            roles.add(roleDao.getRole(roleId));
                        }
                        user.setRoles(roles);
                    }
                    userSalt.setUserId(user.getUsername());
                    userSaltDao.addUserSalt(userSalt);
                    invalid = !userDao.addUser(user);
                    if (us != null && !invalid) {
                        us.insertUserPostProcessing(user);
                    }
                }
            } else {
                user.setUsername(user.getId());
                String currPassword = user.getPassword();

                if (us != null) {
                    Collection<String> validationErrors = us.validateUserOnUpdate(user);
                    if (validationErrors != null && !validationErrors.isEmpty()) {
                        errors.addAll(validationErrors);
                    }
                }

                if (!PasswordValidator.isPasswordEmpty(currPassword)) {
                    if (!PasswordValidator.validate(currPassword)) {
                        errors.add(ResourceBundleUtil.getMessage("console.directory.user.error.label.passwordNotValid"));
                    }
                }

                errors.addAll(validateEmploymentDate(employeeStartDate, employeeEndDate));

                if (errors.isEmpty()) {
                    boolean passwordReset = false;
                    boolean passwordUpdated = false;

                    User u = userDao.getUserById(user.getId());
                    u.setFirstName(user.getFirstName());
                    u.setLastName(user.getLastName());
                    u.setEmail(user.getEmail());

                    if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                        u.setConfirmPassword(user.getPassword());
                        if (us != null) {
                            passwordReset = true;
                            u.setPassword(us.encryptPassword(user.getUsername(), user.getPassword()));
                        } else {
                            //md5 password
                            //u.setPassword(StringUtil.md5Base16(user.getPassword()));
                            passwordUpdated = true;
                            HashSalt hashSalt = PasswordGeneratorUtil.createNewHashWithSalt(user.getPassword());
                            u.setPassword(hashSalt.getHash());

                            userSalt.setUserId(u.getUsername());
                            userSalt.setRandomSalt(hashSalt.getSalt());

                        }
                    }
                    //set roles
                    if (user.getRoles() != null && user.getRoles().size() > 0) {
                        Set roles = new HashSet();
                        for (String roleId : (Set<String>) user.getRoles()) {
                            roles.add(roleDao.getRole(roleId));
                        }
                        u.setRoles(roles);
                    }
                    u.setTimeZone(user.getTimeZone());
                    u.setActive(user.getActive());

                    invalid = !userDao.updateUser(u);
                    if (passwordUpdated) {
                        UserSalt currentUserSalt = userSaltDao.getUserSaltByUserId(u.getUsername());
                        if (currentUserSalt == null) {
                            userSalt.setId(UUID.randomUUID().toString());
                            userSaltDao.addUserSalt(userSalt);
                        } else {
                            userSalt.setId(currentUserSalt.getId());
                            userSaltDao.updateUserSalt(userSalt);
                        }
                    }

                    if (us != null && !invalid) {
                        us.updateUserPostProcessing(u);
                        if (passwordReset) {
                            us.passwordResetPostProcessing(u);
                        }
                    }
                }
            }

            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            Collection<Organization> organizations = organizationDao.getOrganizationsByFilter(null, "name", false, null, null);
            model.addAttribute("organizations", organizations);
            model.addAttribute("roles", roleDao.getRoles(null, "name", false, null, null));
            model.addAttribute("timezones", TimeZoneUtil.getList());

            Map<String, String> status = new HashMap<String, String>();
            status.put("1", "Active");
            status.put("0", "Inactive");
            model.addAttribute("status", status);

            model.addAttribute("user", user);

            model.addAttribute("employeeCode", employeeCode);
            model.addAttribute("employeeRole", employeeRole);
            model.addAttribute("employeeOrganization", employeeOrganization);
            model.addAttribute("employeeDepartment", employeeDepartment);
            model.addAttribute("employeeGrade", employeeGrade);
            model.addAttribute("employeeStartDate", employeeStartDate);
            model.addAttribute("employeeEndDate", employeeEndDate);
            model.addAttribute("employeeDepartmentHod", employeeDepartmentHod);

            if (us != null) {
                if ("create".equals(action)) {
                    model.addAttribute("userFormFooter", us.getUserCreationFormFooter());
                } else {
                    model.addAttribute("userFormFooter", us.getUserEditingFormFooter(user));
                }
            } else {
                model.addAttribute("userFormFooter", "");
            }

            if ("create".equals(action)) {
                return "console/directory/userCreate";
            } else {
                return "console/directory/userEdit";
            }
        } else {
            String prevDepartmentId = null;

            //set employment detail
            Employment employment = null;
            if ("create".equals(action)) {
                employment = new Employment();
            } else {
                try {
                    employment = (Employment) userDao.getUserById(user.getId()).getEmployments().iterator().next();
                } catch (Exception e) {
                    employment = new Employment();
                }
            }

            prevDepartmentId = employment.getDepartmentId();

            employment.setUserId(user.getId());
            employment.setEmployeeCode(employeeCode);
            employment.setRole(employeeRole);
            employment.setOrganizationId((employeeOrganization != null && !employeeOrganization.isEmpty()) ? employeeOrganization : null);
            employment.setDepartmentId((employeeDepartment != null && !employeeDepartment.isEmpty()) ? employeeDepartment : null);
            employment.setGradeId((employeeGrade != null && !employeeGrade.isEmpty()) ? employeeGrade : null);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            try {
                if (employeeStartDate != null && employeeStartDate.trim().length() > 0) {
                    employment.setStartDate(df.parse(employeeStartDate));
                } else {
                    employment.setStartDate(null);
                }
                if (employeeEndDate != null && employeeEndDate.trim().length() > 0) {
                    employment.setEndDate(df.parse(employeeEndDate));
                } else {
                    employment.setEndDate(null);
                }
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "Set Employee Date error");
            }
            if (employment.getId() == null) {
                employment.setUser(user);
                employmentDao.addEmployment(employment);
            } else {
                employmentDao.updateEmployment(employment);
            }

            //Hod
            if ("yes".equals(employeeDepartmentHod) && employeeDepartment != null && employeeDepartment.trim().length() > 0) {
                if (prevDepartmentId != null) {
                    User prevHod = userDao.getHodByDepartmentId(prevDepartmentId);
                    if (prevHod != null) {
                        employmentDao.unassignUserAsDepartmentHOD(prevHod.getId(), prevDepartmentId);
                    }
                }
                employmentDao.assignUserAsDepartmentHOD(user.getId(), employeeDepartment);
            } else if (prevDepartmentId != null) {
                User prevHod = userDao.getHodByDepartmentId(prevDepartmentId);
                if (prevHod != null && prevHod.getId().equals(user.getId())) {
                    employmentDao.unassignUserAsDepartmentHOD(prevHod.getId(), prevDepartmentId);
                }
            }

            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath;
            url += "/web/console/directory/user/view/" + user.getId() + ".";
            model.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping(value = "/console/directory/user/delete", method = RequestMethod.POST)
    public String consoleUserDelete(@RequestParam(value = "ids") String ids) {
        String currentUsername = workflowUserManager.getCurrentUsername();

        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();

            if (id != null && !id.equals(currentUsername)) {
                userDao.deleteUser(id);
                UserSalt userSalt = userSaltDao.getUserSaltByUserId(id);
                if (userSalt != null) {
                    userSaltDao.deleteUserSalt(userSalt.getId());
                }

                UserSecurity us = DirectoryUtil.getUserSecurity();
                if (us != null) {
                    us.deleteUserPostProcessing(id);
                }
            }
        }
        return "console/directory/userList";
    }

    @RequestMapping("/console/directory/user/(*:id)/group/assign/view")
    public String consoleUserGroupAssign(ModelMap model, @RequestParam(value = "id") String id) {
        model.addAttribute("id", id);
        Collection<Organization> organizations = organizationDao.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);
        return "console/directory/userGroupAssign";
    }

    @RequestMapping("/console/directory/user/(*:id)/reportTo/assign/view")
    public String consoleUserReportToAssign(ModelMap model, @RequestParam(value = "id") String id) {
        User user = userDao.getUserById(id);
        model.addAttribute("id", id);
        if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0) {
            Employment e = (Employment) user.getEmployments().iterator().next();
            Collection<Department> departments = directoryManager.getDepartmentsByOrganizationId(null, e.getOrganizationId(), "name", false, null, null);
            Collection<Grade> grades = directoryManager.getGradesByOrganizationId(null, e.getOrganizationId(), "name", false, null, null);
            model.addAttribute("organizationId", e.getOrganizationId());
            model.addAttribute("departments", departments);
            model.addAttribute("grades", grades);
        }
        return "console/directory/userReportToAssign";
    }

    @RequestMapping(value = "/console/directory/user/(*:id)/reportTo/assign/submit", method = RequestMethod.POST)
    public String consoleUserReportToAssignSubmit(ModelMap model, @RequestParam(value = "id") String id, @RequestParam(value = "userId") String userId) {
        employmentDao.assignUserReportTo(id, userId);
        return "console/directory/userReportToAssign";
    }

    @RequestMapping(value = "/console/directory/user/(*:id)/reportTo/unassign", method = RequestMethod.POST)
    public String consoleUserReportToUnassign(@RequestParam(value = "id") String id) {
        employmentDao.unassignUserReportTo(id);
        return "console/directory/userView";
    }

    @RequestMapping(value = "/console/directory/user/(*:id)/group/assign/submit", method = RequestMethod.POST)
    public String consoleUserGroupAssignSubmit(ModelMap model, @RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String groupId = (String) strToken.nextElement();
            userDao.assignUserToGroup(id, groupId);
        }
        return "console/directory/userGroupAssign";
    }

    @RequestMapping(value = "/console/directory/user/(*:id)/group/unassign", method = RequestMethod.POST)
    public String consoleUserGroupUnassign(@RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String groupId = (String) strToken.nextElement();
            userDao.unassignUserFromGroup(id, groupId);
        }
        return "console/directory/userList";
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @RequestMapping("/console/profile")
    public String profile(ModelMap map, HttpServletResponse response) throws IOException {
        User user = userDao.getUser(workflowUserManager.getCurrentUsername());

        if (user == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        map.addAttribute("user", user);
        map.addAttribute("timezones", TimeZoneUtil.getList());

        String enableUserLocale = SetupManager.getSettingValue("enableUserLocale");
        Map<String, String> localeStringList = new TreeMap<String, String>();
        if (enableUserLocale != null && enableUserLocale.equalsIgnoreCase("true")) {
            String userLocale = SetupManager.getSettingValue("userLocale");
            Collection<String> locales = new HashSet();
            locales.addAll(Arrays.asList(userLocale.split(",")));

            Locale[] localeList = Locale.getAvailableLocales();
            for (int x = 0; x < localeList.length; x++) {
                String code = localeList[x].toString();
                if (locales.contains(code)) {
                    localeStringList.put(code, code + " - " + localeList[x].getDisplayName(LocaleContextHolder.getLocale()));
                }
            }
        }

        UserSecurity us = DirectoryUtil.getUserSecurity();
        if (us != null) {
            map.addAttribute("policies", us.passwordPolicies());
            map.addAttribute("userProfileFooter", us.getUserProfileFooter(user));
        } else {
            map.addAttribute("policies", "");
            map.addAttribute("userProfileFooter", "");
        }

        map.addAttribute("enableUserLocale", enableUserLocale);
        map.addAttribute("localeStringList", localeStringList);

        return "console/profile";
    }

    @SuppressWarnings({"unused", "unchecked", "rawtypes"})
    @RequestMapping(value = "/console/profile/submit", method = RequestMethod.POST)
    public String profileSubmit(ModelMap model, HttpServletRequest request, HttpServletResponse response, @ModelAttribute("user") User user, BindingResult result) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        User currentUser = userDao.getUser(workflowUserManager.getCurrentUsername());

        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        Collection<String> errors = new ArrayList<String>();
        Collection<String> passwordErrors = new ArrayList<String>();

        boolean authenticated = false;

        if (!currentUser.getUsername().equals(user.getUsername())) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        } else {
            try {
                if (directoryManager.authenticate(currentUser.getUsername(), user.getOldPassword())) {
                    authenticated = true;
                }
            } catch (Exception e) {
            }
        }

        UserSecurity us = DirectoryUtil.getUserSecurity();

        if (!authenticated) {
            if (errors == null) {
                errors = new ArrayList<String>();
            }
            errors.add(ResourceBundleUtil.getMessage("console.directory.user.error.label.authenticationFailed"));
        } else {
            if (us != null) {
                errors = us.validateUserOnProfileUpdate(user);
            }

            if (user.getPassword() != null && !user.getPassword().isEmpty() && us != null) {
                passwordErrors = us.validatePassword(user.getUsername(), user.getOldPassword(), user.getPassword(), user.getConfirmPassword());
            }
        }

        if (!authenticated || (passwordErrors != null && !passwordErrors.isEmpty()) || (errors != null && !errors.isEmpty())) {
            model.addAttribute("passwordErrors", passwordErrors);
            model.addAttribute("errors", errors);
            model.addAttribute("user", user);
            model.addAttribute("timezones", TimeZoneUtil.getList());

            String enableUserLocale = SetupManager.getSettingValue("enableUserLocale");
            Map<String, String> localeStringList = new TreeMap<String, String>();
            if (enableUserLocale != null && enableUserLocale.equalsIgnoreCase("true")) {
                String userLocale = SetupManager.getSettingValue("userLocale");
                Collection<String> locales = new HashSet();
                locales.addAll(Arrays.asList(userLocale.split(",")));

                Locale[] localeList = Locale.getAvailableLocales();
                for (int x = 0; x < localeList.length; x++) {
                    String code = localeList[x].toString();
                    if (locales.contains(code)) {
                        localeStringList.put(code, code + " - " + localeList[x].getDisplayName(LocaleContextHolder.getLocale()));
                    }
                }
            }
            model.addAttribute("enableUserLocale", enableUserLocale);
            model.addAttribute("localeStringList", localeStringList);

            if (us != null) {
                model.addAttribute("policies", us.passwordPolicies());
                model.addAttribute("userProfileFooter", us.getUserProfileFooter(currentUser));
            } else {
                model.addAttribute("policies", "");
                model.addAttribute("userProfileFooter", "");
            }

            return "console/profile";
        } else if (currentUser.getUsername().equals(user.getUsername())) {
            currentUser.setFirstName(user.getFirstName());
            currentUser.setLastName(user.getLastName());
            currentUser.setEmail(user.getEmail());
            currentUser.setTimeZone(user.getTimeZone());
            currentUser.setLocale(user.getLocale());
            UserSalt userSalt = userSaltDao.getUserSaltByUserId(currentUser.getUsername());

            if (user.getPassword() != null && user.getConfirmPassword() != null && user.getPassword().length() > 0 && user.getPassword().equals(user.getConfirmPassword())) {
                if (us != null) {
                    currentUser.setPassword(us.encryptPassword(user.getUsername(), user.getPassword()));
                } else {
                    //currentUser.setPassword(StringUtil.md5Base16(user.getPassword()));
                    HashSalt hashSalt = PasswordGeneratorUtil.createNewHashWithSalt(user.getPassword());
                    userSalt.setRandomSalt(hashSalt.getSalt());

                    currentUser.setPassword(hashSalt.getHash());
                }
                currentUser.setConfirmPassword(user.getPassword());
            }
            userDao.updateUser(currentUser);
            userSaltDao.updateUserSalt(userSalt);
            if (us != null) {
                us.updateUserProfilePostProcessing(currentUser);
            }
        }

        return "console/dialogClose";
    }

    @RequestMapping("/console/app/menu")
    public String consoleMenuAppList(ModelMap model) {
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findLatestVersions(null, null, null, "name", Boolean.FALSE, null, null);
        model.addAttribute("appDefinitionList", appDefinitionList);
        return "console/apps/appMenu";
    }

    @RequestMapping("/console/app/create")
    public String consoleAppCreate(ModelMap model) {
        model.addAttribute("appDefinition", new AppDefinition());

        Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
        model.addAttribute("appList", appDefinitionList);

        return "console/apps/appCreate";
    }

    @RequestMapping(value = "/console/app/submit", method = RequestMethod.POST)
    public String consoleAppSubmit(ModelMap model, @ModelAttribute("appDefinition") AppDefinition appDefinition, BindingResult result, @RequestParam(value = "copyAppId", required = false) String copyAppId) {
        // validate ID
        validator.validate(appDefinition, result);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // create app
            AppDefinition copy = null;
            if (copyAppId != null && !copyAppId.isEmpty()) {
                copy = appService.getAppDefinition(copyAppId, null);
            }

            Collection<String> errors = appService.createAppDefinition(appDefinition, copy);
            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
            model.addAttribute("appList", appDefinitionList);

            return "console/apps/appCreate";
        } else {
            String appId = appDefinition.getId();
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/app/" + appId + "/forms";
            model.addAttribute("url", url);
            return "console/apps/dialogClose";
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @RequestMapping("/json/console/app/list")
    public void consoleAppListJson(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findLatestVersions(null, null, name, sort, desc, start, rows);
        Long count = appDefinitionDao.countLatestVersions(null, null, name);

        JSONObject jsonObject = new JSONObject();
        for (AppDefinition appDef : appDefinitionList) {
            Map data = new HashMap();
            data.put("id", appDef.getId());
            data.put("name", appDef.getName());
            data.put("version", appDef.getVersion());
            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/console/app/(*:appId)/versioning")
    public String consoleAppVersioning(ModelMap model, @RequestParam(value = "appId") String appId) {
        model.addAttribute("appId", appId);
        return "console/apps/appVersion";
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/json/console/app/(*:appId)/version/list")
    public void consoleAppVersionListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        Collection<AppDefinition> appDefList = appDefinitionDao.findVersions(appId, sort, desc, start, rows);
        Long count = appDefinitionDao.countVersions(appId);

        JSONObject jsonObject = new JSONObject();
        if (appDefList != null && appDefList.size() > 0) {
            for (AppDefinition appDef : appDefList) {
                @SuppressWarnings("rawtypes")
                Map data = new HashMap();
                data.put("version", appDef.getVersion().toString());
                data.put("published", (appDef.isPublished()) ? "<div class=\"tick\"></div>" : "");
                data.put("dateCreated", TimeZoneUtil.convertToTimeZone(appDef.getDateCreated(), null, AppUtil.getAppDateFormat()));
                data.put("dateModified", TimeZoneUtil.convertToTimeZone(appDef.getDateModified(), null, AppUtil.getAppDateFormat()));
                data.put("description", appDef.getDescription());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/version/new", method = RequestMethod.POST)
    @Transactional
    public String consoleAppCreate(@RequestParam(value = "appId") String appId) {
        appService.createNewAppDefinitionVersion(appId);
        return "console/apps/dialogClose";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/publish", method = RequestMethod.POST)
    @Transactional
    public String consoleAppPublish(@RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) {
        appService.publishApp(appId, version);
        return "console/apps/dialogClose";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/rename/(*:name)", method = RequestMethod.POST)
    @Transactional
    public String consoleAppRename(@RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "name") String name) {
        //Rename app
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef != null) {
            appDef.setName(name);
            appDefinitionDao.saveOrUpdate(appDef);
        }

        return "console/apps/dialogClose";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/note/submit", method = RequestMethod.POST)
    @Transactional
    public String consoleAppNote(@RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "description") String description) {
        //Rename app
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef != null) {
            appDef.setDescription(description);
            appDefinitionDao.saveOrUpdate(appDef);
        }

        return "redirect:/web/console/app/" + appId + "/" + version + "/properties";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/unpublish", method = RequestMethod.POST)
    @Transactional
    public String consoleAppUnpublish(@RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) {
        appService.unpublishApp(appId);
        return "console/apps/dialogClose";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/delete", method = RequestMethod.POST)
    public String consoleAppDelete(@RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) {
        Long appVersion;
        if (AppDefinition.VERSION_LATEST.equals(version)) {
            appVersion = appDefinitionDao.getLatestVersion(appId);
        } else {
            appVersion = Long.parseLong(version);
        }
        appService.deleteAppDefinitionVersion(appId, appVersion);
        return "console/apps/dialogClose";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/export")
    public void consoleAppExport(HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) throws IOException {
        ServletOutputStream output = null;
        try {
            // verify app
            AppDefinition appDef = appService.getAppDefinition(appId, version);
            if (appDef == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // determine output filename
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String timestamp = sdf.format(new Date());
            String filename = APP_ZIP_PREFIX + appDef.getId() + "-" + appDef.getVersion() + "-" + timestamp + ".jwa";

            // set response headers
            response.setContentType("application/zip");
            response.addHeader("Content-Disposition", "inline; filename=" + filename);
            output = response.getOutputStream();

            // export app
            appService.exportApp(appDef.getId(), appDef.getVersion().toString(), output);

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            if (output != null) {
                output.flush();
            }
        }
    }

    @RequestMapping("/console/app/import")
    public String consoleAppImport() {
        return "console/apps/import";
    }

    @RequestMapping(value = "/console/app/import/submit", method = RequestMethod.POST)
    public String consoleAppImportSubmit(ModelMap map) throws IOException {
        Collection<String> errors = new ArrayList<String>();

        MultipartFile appZip = null;

        try {
            appZip = FileStore.getFile("appZip");
        } catch (FileLimitException e) {
            errors.add(ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()}));
        }

        AppDefinition appDef = null;
        try {
            if (appZip != null) {
                appDef = appService.importApp(appZip.getBytes());
            }
        } catch (ImportAppException e) {
            errors.add(e.getMessage());
        }

        if (appDef == null || !errors.isEmpty()) {
            map.addAttribute("error", true);
            map.addAttribute("errorList", errors);
            return "console/apps/import";
        } else {
            String appId = appDef.getAppId();
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/app/" + appId + "/forms";
            map.addAttribute("url", url);
            map.addAttribute("appId", appId);
            map.addAttribute("appVersion", appDef.getVersion());
            map.addAttribute("isPublished", appDef.isPublished());
            return "console/apps/packageUploadSuccess";
        }
    }

    @RequestMapping({"/console/app/(*:appId)/(~:version)/package/xpdl", "/json/console/app/(*:appId)/(~:version)/package/xpdl"})
    public void getPackageXpdl(Writer writer, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) throws IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/xml; charset=utf-8");
        PackageDefinition packageDef = appDef.getPackageDefinition();
        if (packageDef != null) {
            byte[] content = workflowManager.getPackageContent(packageDef.getId(), packageDef.getVersion().toString());
            String xpdl = new String(content, "UTF-8");
            writer.write(xpdl);
        } else {
            // read default xpdl
            InputStream input = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                // get resource input stream
                String url = "/org/joget/apps/app/model/default.xpdl";
                input = pluginManager.getPluginResource(DefaultFormBinder.class.getName(), url);
                if (input != null) {
                    // write output
                    byte[] bbuf = new byte[65536];
                    int length = 0;
                    while ((input != null) && ((length = input.read(bbuf)) != -1)) {
                        out.write(bbuf, 0, length);
                    }
                    // form xpdl
                    String xpdl = new String(out.toByteArray(), "UTF-8");

                    // replace package ID and name
                    xpdl = xpdl.replace("${packageId}", appId);
                    xpdl = xpdl.replace("${packageName}", appDef.getName());
                    writer.write(xpdl);
                }
            } finally {
                if (input != null) {
                    input.close();
                }
            }
        }
    }

    @RequestMapping(value = "/json/console/app/(*:appId)/(~:version)/package/deploy", method = RequestMethod.POST)
    public void consolePackageDeploy(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, HttpServletRequest request) throws JSONException, IOException {
        String error = null;

        appService.getAppDefinition(appId, version);
        MultipartFile packageXpdl = null;

        try {
            packageXpdl = FileStore.getFile("packageXpdl");
        } catch (FileLimitException e) {
            error = ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()});
        }
        JSONObject jsonObject = new JSONObject();

        // TODO: authenticate user
        boolean authenticated = !workflowUserManager.isCurrentUserAnonymous();

        if (authenticated) {
            if (error == null) {
                try {
                    // deploy package
                    appService.deployWorkflowPackage(appId, version, packageXpdl.getBytes(), true);

                    jsonObject.accumulate("status", "complete");
                } catch (Exception e) {
                    jsonObject.accumulate("errorMsg", e.getMessage().replace(":", ""));
                }
            } else {
                jsonObject.accumulate("errorMsg", error);
            }
        } else {
            jsonObject.accumulate("errorMsg", "unauthenticated");
        }
        AppUtil.writeJson(writer, jsonObject, null);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/package/upload")
    public String consolePackageUpload(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        return "console/apps/packageUpload";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/package/upload/submit", method = RequestMethod.POST)
    public String consolePackageUploadSubmit(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, HttpServletRequest request) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        MultipartFile packageXpdl;

        try {
            packageXpdl = FileStore.getFile("packageXpdl");
        } catch (FileLimitException e) {
            map.addAttribute("errorMessage", ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()}));
            return "console/apps/packageUpload";
        }

        try {
            if (packageXpdl == null || packageXpdl.isEmpty()) {
                throw new RuntimeException("Package XPDL is empty");
            }
            appService.deployWorkflowPackage(appId, version, packageXpdl.getBytes(), false);
        } catch (Throwable e) {
            map.addAttribute("errorMessage", e.getMessage());
            return "console/apps/packageUpload";
        }
        return "console/apps/xpdlUploadSuccess";
    }

    @RequestMapping({"/console/app/(*:appId)/(~:version)/processes", "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)"})
    public String consoleProcessView(ModelMap map, @RequestParam("appId") String appId, @RequestParam(value = "processDefId", required = false) String processDefId, @RequestParam(value = "version", required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        //for launching workflow designer
        User user = directoryManager.getUserByUsername(workflowUserManager.getCurrentUsername());
        map.addAttribute("username", user.getUsername());

        WorkflowProcess process = null;
        boolean processFound = false;
        Collection<WorkflowProcess> processList = null;
        PackageDefinition packageDefinition = appDef.getPackageDefinition();
        if (packageDefinition != null) {
            Long packageVersion = packageDefinition.getVersion();
            processList = workflowManager.getProcessList(appId, packageVersion.toString());
            if (processDefId != null && processDefId.trim().length() > 0) {
                // find matching process by definition (without version)
                for (WorkflowProcess wp : processList) {
                    String processIdWithoutVersion = WorkflowUtil.getProcessDefIdWithoutVersion(wp.getId());
                    if (processIdWithoutVersion.equals(processDefId) && wp.getVersion().equals(packageVersion.toString())) {
                        process = wp;
                        processDefId = wp.getId();
                        processFound = true;
                        break;
                    }
                }
            }
        }
        checkAppPublishedVersion(appDef);
        if (!processFound) {
            // specific process not found, get list of processes
            if (processList != null && processList.size() == 1) {
                // remove attributes to prevent passing over as url parameters
                map.clear();
                // redirect to the only process
                WorkflowProcess wp = processList.iterator().next();
                return "redirect:/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/processes/" + wp.getIdWithoutVersion();
            } else {
                // show process list
                map.addAttribute("processList", processList);
                return "console/apps/processList";
            }
        }

        //get activity list
        Collection<WorkflowActivity> activityList = workflowManager.getProcessActivityDefinitionList(processDefId);

        //add 'Run Process' activity to activityList
        WorkflowActivity runProcessActivity = new WorkflowActivity();
        runProcessActivity.setId(WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS);
        runProcessActivity.setName("Run Process");
        runProcessActivity.setType("normal");
        activityList.add(runProcessActivity);

        //remove route
        @SuppressWarnings("rawtypes")
        Iterator iterator = activityList.iterator();
        while (iterator.hasNext()) {
            WorkflowActivity activity = (WorkflowActivity) iterator.next();
            if (activity.getType().equals(WorkflowActivity.TYPE_ROUTE)) {
                iterator.remove();
            }
        }

        //get activity plugin mapping
        Map<String, Plugin> pluginMap = new HashMap<String, Plugin>();
        Map<String, PackageActivityPlugin> activityPluginMap = (packageDefinition != null) ? packageDefinition.getPackageActivityPluginMap() : new HashMap<String, PackageActivityPlugin>();
        for (String activityDefId : activityPluginMap.keySet()) {
            PackageActivityPlugin pap = activityPluginMap.get(activityDefId);
            String pluginName = pap.getPluginName();
            Plugin plugin = pluginManager.getPlugin(pluginName);
            pluginMap.put(activityDefId, plugin);
        }

        //get activity form mapping
        Map<String, PackageActivityForm> activityFormMap = (packageDefinition != null) ? packageDefinition.getPackageActivityFormMap() : new HashMap<String, PackageActivityForm>();

        // get form map
        Map<String, FormDefinition> formMap = new HashMap<String, FormDefinition>();
        for (String activityDefId : activityFormMap.keySet()) {
            PackageActivityForm paf = activityFormMap.get(activityDefId);
            String formId = paf.getFormId();
            if (PackageActivityForm.ACTIVITY_FORM_TYPE_SINGLE.equals(paf.getType()) && formId != null && !formId.isEmpty()) {
                FormDefinition formDef = formDefinitionDao.loadById(formId, appDef);
                formMap.put(activityDefId, formDef);
            }
        }

        //get variable list
        Collection<WorkflowVariable> variableList = workflowManager.getProcessVariableDefinitionList(processDefId);

        //get participant list
        Collection<WorkflowParticipant> participantList = workflowManager.getProcessParticipantDefinitionList(processDefId);

        WorkflowParticipant processStartWhiteList = new WorkflowParticipant();
        processStartWhiteList.setId("processStartWhiteList");
        processStartWhiteList.setName(ResourceBundleUtil.getMessage("console.app.process.common.label.processStartWhiteList"));
        processStartWhiteList.setPackageLevel(false);
        participantList.add(processStartWhiteList);

        // get participant map
        Map<String, PackageParticipant> participantMap = (packageDefinition != null) ? packageDefinition.getPackageParticipantMap() : new HashMap<String, PackageParticipant>();

        // get participant plugin map
        Map<String, Plugin> participantPluginMap = pluginManager.loadPluginMap(ParticipantPlugin.class);

        String processIdWithoutVersion = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        map.addAttribute("processIdWithoutVersion", processIdWithoutVersion);
        map.addAttribute("process", process);
        map.addAttribute("activityList", activityList);
        map.addAttribute("pluginMap", pluginMap);
        map.addAttribute("participantPluginMap", participantPluginMap);
        map.addAttribute("activityFormMap", activityFormMap);
        map.addAttribute("formMap", formMap);
        map.addAttribute("variableList", variableList);
        map.addAttribute("participantList", participantList);
        map.addAttribute("participantMap", participantMap);
        map.addAttribute("isExtDirectoryManager", DirectoryUtil.isExtDirectoryManager());
        map.addAttribute("usersMap", DirectoryUtil.getUsersMap());
        map.addAttribute("groupsMap", DirectoryUtil.getGroupsMap());
        map.addAttribute("departmentsMap", DirectoryUtil.getDepartmentsMap());

        return "console/apps/processView";
    }

    protected String getActivityNameForParticipantMapping(String processDefId, String activityDefId) {
        String activity = "Previous Activity";

        if (activityDefId != null && activityDefId.trim().length() > 0) {
            if (WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS.equals(activityDefId)) {
                activity = "Run Process";
            } else {
                WorkflowActivity wa = workflowManager.getProcessActivityDefinition(processDefId, activityDefId);
                if (wa != null) {
                    activity = wa.getName();
                }
            }
        }

        return activity;
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/form")
    public String consoleActivityForm(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        WorkflowProcess process = workflowManager.getProcess(processDefId);
        WorkflowActivity activity = workflowManager.getProcessActivityDefinition(processDefId, activityDefId);

        if (activityDefId.equals(WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS)) {
            activity = new WorkflowActivity();
            activity.setId(WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS);
            activity.setName("Run Process");
        }

        PackageDefinition packageDef = appDef.getPackageDefinition();
        String processDefIdWithoutVersion = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        PackageActivityForm activityForm = packageDef.getPackageActivityForm(processDefIdWithoutVersion, activityDefId);
        if (activityForm != null && PackageActivityForm.ACTIVITY_FORM_TYPE_EXTERNAL.equals(activityForm.getType())) {
            map.addAttribute("externalFormUrl", activityForm.getFormUrl());
            map.addAttribute("externalFormIFrameStyle", activityForm.getFormIFrameStyle());
        }

        map.addAttribute("process", process);
        map.addAttribute("activity", activity);

        return "console/apps/activityFormAdd";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/form/submit", method = RequestMethod.POST)
    public String consoleActivityFormSubmit(
            ModelMap map,
            @RequestParam String appId,
            @RequestParam(required = false) String version,
            @RequestParam String processDefId,
            @RequestParam String activityDefId,
            @RequestParam(value = "id", required = false) String formId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "externalFormUrl", required = false) String externalFormUrl,
            @RequestParam(value = "externalFormIFrameStyle", required = false) String externalFormIFrameStyle) throws UnsupportedEncodingException {

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        boolean autoContinue = false;
        if (packageDef != null) {
            autoContinue = appService.isActivityAutoContinue(packageDef.getId(), packageDef.getVersion().toString(), processDefId, activityDefId);
        }
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        PackageActivityForm activityForm = new PackageActivityForm();
        activityForm.setProcessDefId(processDefId);
        activityForm.setActivityDefId(activityDefId);
        if (PackageActivityForm.ACTIVITY_FORM_TYPE_EXTERNAL.equals(type) && externalFormUrl != null) {
            activityForm.setType(PackageActivityForm.ACTIVITY_FORM_TYPE_EXTERNAL);
            activityForm.setFormUrl(externalFormUrl);
            activityForm.setFormIFrameStyle(externalFormIFrameStyle);
        } else {
            activityForm.setType(PackageActivityForm.ACTIVITY_FORM_TYPE_SINGLE);
            activityForm.setFormId(formId);
        }
        activityForm.setAutoContinue(autoContinue);

        packageDefinitionDao.addAppActivityForm(appId, appDef.getVersion(), activityForm);

        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));

        return "console/apps/activityFormAddSuccess";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/form/remove", method = RequestMethod.POST)
    public String consoleActivityFormRemove(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();

        // check for existing auto continue flag
        boolean autoContinue = false;
        if (packageDef != null) {
            autoContinue = appService.isActivityAutoContinue(packageDef.getId(), packageDef.getVersion().toString(), processDefId, activityDefId);
        }

        // remove mapping
        packageDefinitionDao.removeAppActivityForm(appId, appDef.getVersion(), processDefId, activityDefId);

        if (autoContinue) {
            // save autoContinue flag
            PackageActivityForm paf = new PackageActivityForm();
            paf.setProcessDefId(processDefId);
            paf.setActivityDefId(activityDefId);
            paf.setAutoContinue(autoContinue);
            packageDefinitionDao.addAppActivityForm(appId, appDef.getVersion(), paf);
        }

        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));
        return "console/apps/activityFormRemoveSuccess";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/continue", method = RequestMethod.POST)
    public void consoleActivityContinueSubmit(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId, @RequestParam String auto) throws JSONException, IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();

        // set and save
        PackageActivityForm paf = packageDef.getPackageActivityForm(processDefId, activityDefId);
        if (paf == null) {
            paf = new PackageActivityForm();
            paf.setProcessDefId(processDefId);
            paf.setActivityDefId(activityDefId);
        }
        boolean autoContinue = Boolean.parseBoolean(auto);
        paf.setAutoContinue(autoContinue);
        packageDefinitionDao.addAppActivityForm(appId, appDef.getVersion(), paf);

        // write output
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("auto", autoContinue);
        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/draft", method = RequestMethod.POST)
    public void consoleActivitySaveAsDraftSubmit(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId, @RequestParam String disable) throws JSONException, IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();

        // set and save
        PackageActivityForm paf = packageDef.getPackageActivityForm(processDefId, activityDefId);
        if (paf == null) {
            paf = new PackageActivityForm();
            paf.setProcessDefId(processDefId);
            paf.setActivityDefId(activityDefId);
        }
        boolean disableSaveAsDraft = Boolean.parseBoolean(disable);
        paf.setDisableSaveAsDraft(disableSaveAsDraft);
        packageDefinitionDao.addAppActivityForm(appId, appDef.getVersion(), paf);

        // write output
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("disable", disableSaveAsDraft);
        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/plugin")
    public String consoleActivityPlugin(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        WorkflowProcess process = workflowManager.getProcess(processDefId);
        WorkflowActivity activity = workflowManager.getProcessActivityDefinition(processDefId, activityDefId);
        map.addAttribute("process", process);
        map.addAttribute("activity", activity);
        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));
        return "console/apps/activityPluginAdd";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/plugin/submit", method = RequestMethod.POST)
    public String consoleActivityPluginSubmit(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId, @RequestParam("id") String pluginName) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        PackageActivityPlugin activityPlugin = new PackageActivityPlugin();
        activityPlugin.setProcessDefId(processDefId);
        activityPlugin.setActivityDefId(activityDefId);
        activityPlugin.setPluginName(pluginName);

        packageDefinitionDao.addAppActivityPlugin(appId, appDef.getVersion(), activityPlugin);

        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));
        return "console/apps/activityPluginAddSuccess";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/plugin/remove", method = RequestMethod.POST)
    public String consoleActivityPluginRemove(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        packageDefinitionDao.removeAppActivityPlugin(appId, appDef.getVersion(), processDefId, activityDefId);
        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));
        return "console/apps/activityFormRemoveSuccess";
    }

    @SuppressWarnings("deprecation")
    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/plugin/configure")
    public String consoleActivityPluginConfigure(ModelMap map, HttpServletRequest request, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId) throws IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();

        if (packageDef != null) {
            processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
            PackageActivityPlugin activityPlugin = packageDef.getPackageActivityPlugin(processDefId, activityDefId);
            Plugin plugin = pluginManager.getPlugin(activityPlugin.getPluginName());

            if (activityPlugin.getPluginProperties() != null && activityPlugin.getPluginProperties().trim().length() > 0) {
                if (!(plugin instanceof PropertyEditable)) {
                    @SuppressWarnings("rawtypes")
                    Map propertyMap = new HashMap();
                    propertyMap = CsvUtil.getPluginPropertyMap(activityPlugin.getPluginProperties());
                    map.addAttribute("propertyMap", propertyMap);
                } else {
                    map.addAttribute("properties", PropertyUtil.propertiesJsonLoadProcessing(activityPlugin.getPluginProperties()));
                }
            }

            if (plugin != null) {
                PluginDefaultProperties pluginDefaultProperties = pluginDefaultPropertiesDao.loadById(activityPlugin.getPluginName(), appDef);

                if (pluginDefaultProperties != null) {
                    if (!(plugin instanceof PropertyEditable)) {
                        @SuppressWarnings({"rawtypes"})
                        Map defaultPropertyMap = new HashMap();

                        String properties = pluginDefaultProperties.getPluginProperties();
                        if (properties != null && properties.trim().length() > 0) {
                            defaultPropertyMap = CsvUtil.getPluginPropertyMap(properties);
                        }
                        map.addAttribute("defaultPropertyMap", defaultPropertyMap);
                    } else {
                        map.addAttribute("defaultProperties", PropertyUtil.propertiesJsonLoadProcessing(pluginDefaultProperties.getPluginProperties()));
                    }
                }
            }

            if (plugin instanceof PropertyEditable) {
                map.addAttribute("propertyEditable", (PropertyEditable) plugin);
            }

            map.addAttribute("plugin", plugin);

            String url = request.getContextPath() + "/web/console/app/" + appId + "/" + appDef.getVersion() + "/processes/" + URLEncoder.encode(processDefId, "UTF-8") + "/activity/" + activityDefId + "/plugin/configure/submit?param_activityPluginId=" + activityPlugin.getUid();
            map.addAttribute("actionUrl", url);
        }

        return "console/plugin/pluginConfig";
    }

    @RequestMapping(value = "/console/app/(*:param_appId)/(~:param_version)/processes/(*:param_processDefId)/activity/(*:param_activityDefId)/plugin/configure/submit", method = RequestMethod.POST)
    @Transactional
    public String consoleActivityPluginConfigureSubmit(ModelMap map, @RequestParam("param_appId") String appId, @RequestParam(value = "param_version", required = false) String version, @RequestParam("param_processDefId") String processDefId, @RequestParam("param_activityDefId") String activityDefId, @RequestParam(value = "pluginProperties", required = false) String pluginProperties, HttpServletRequest request) throws IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        PackageActivityPlugin activityPlugin = packageDef.getPackageActivityPlugin(processDefId, activityDefId);
        if (activityPlugin != null) {
            if (pluginProperties == null) {
                //request params
                @SuppressWarnings({"unchecked", "rawtypes"})
                Map<String, String> propertyMap = new HashMap();
                Enumeration<String> e = request.getParameterNames();
                while (e.hasMoreElements()) {
                    String paramName = e.nextElement();

                    if (!paramName.startsWith("param_")) {
                        String[] paramValue = (String[]) request.getParameterValues(paramName);
                        propertyMap.put(paramName, CsvUtil.getDeliminatedString(paramValue));
                    }
                }

                // form csv properties
                StringWriter sw = new StringWriter();
                try {
                    CSVWriter writer = new CSVWriter(sw);
                    @SuppressWarnings("rawtypes")
                    Iterator it = propertyMap.entrySet().iterator();
                    while (it.hasNext()) {
                        @SuppressWarnings({"unchecked", "rawtypes"})
                        Map.Entry<String, String> pairs = (Map.Entry) it.next();
                        writer.writeNext(new String[]{pairs.getKey(), pairs.getValue()});
                    }
                    writer.close();
                } catch (Exception ex) {
                    LogUtil.error(getClass().getName(), ex, "");
                }
                String pluginProps = sw.toString();
                activityPlugin.setPluginProperties(pluginProps);
            } else {
                activityPlugin.setPluginProperties(PropertyUtil.propertiesJsonStoreProcessing(activityPlugin.getPluginProperties(), pluginProperties));
            }
        }

        // update and save
        packageDefinitionDao.saveOrUpdate(packageDef);

        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));

        return "console/apps/activityPluginConfigSuccess";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/participant/(*:participantId)")
    public String consoleParticipant(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String participantId) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        String processIdWithoutVersion = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        map.addAttribute("processDefId", processIdWithoutVersion);
        map.addAttribute("participantId", participantId);

        //get activity list
        Collection<WorkflowActivity> activityList = workflowManager.getProcessActivityDefinitionList(processDefId);

        //add 'Run Process' activity to activityList
        WorkflowActivity runProcessActivity = new WorkflowActivity();
        runProcessActivity.setId(WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS);
        runProcessActivity.setName("Run Process");
        runProcessActivity.setType("normal");
        activityList.add(runProcessActivity);

        //remove route & tool
        @SuppressWarnings("rawtypes")
        Iterator iterator = activityList.iterator();
        while (iterator.hasNext()) {
            WorkflowActivity activity = (WorkflowActivity) iterator.next();
            if ((activity.getType().equals(WorkflowActivity.TYPE_ROUTE)) || (activity.getType().equals(WorkflowActivity.TYPE_TOOL))) {
                iterator.remove();
            }
        }
        map.addAttribute("activityList", activityList);

        //get variable list
        Collection<WorkflowVariable> variableList = workflowManager.getProcessVariableDefinitionList(processDefId);
        map.addAttribute("variableList", variableList);

        Collection<Organization> organizations = null;
        if (DirectoryUtil.isExtDirectoryManager()) {
            organizations = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);
        }
        map.addAttribute("organizations", organizations);
        map.addAttribute("isExtDirectoryManager", DirectoryUtil.isExtDirectoryManager());

        return "console/apps/participantAdd";
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/console/app/(*:param_appId)/(~:param_version)/processes/(*:param_processDefId)/participant/(*:param_participantId)/submit/(*:param_type)", method = RequestMethod.POST)
    public String consoleParticipantSubmit(
            ModelMap map,
            HttpServletRequest request,
            @RequestParam("param_appId") String appId,
            @RequestParam(value = "param_version", required = false) String version,
            @RequestParam("param_processDefId") String processDefId,
            @RequestParam("param_participantId") String participantId,
            @RequestParam("param_type") String type,
            @RequestParam(value = "param_value", required = false) String value,
            @RequestParam(value = "pluginProperties", required = false) String pluginProperties) throws UnsupportedEncodingException {

        PackageParticipant participant = new PackageParticipant();
        participant.setProcessDefId(processDefId);
        participant.setParticipantId(participantId);

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();

        if (PackageParticipant.TYPE_PLUGIN.equals(type)) {
            if (pluginProperties == null) {
                //request params
                @SuppressWarnings({"rawtypes"})
                Map<String, String> propertyMap = new HashMap();
                Enumeration<String> e = request.getParameterNames();
                while (e.hasMoreElements()) {
                    String paramName = e.nextElement();

                    if (!paramName.startsWith("param_")) {
                        String[] paramValue = (String[]) request.getParameterValues(paramName);
                        propertyMap.put(paramName, CsvUtil.getDeliminatedString(paramValue));
                    }
                }

                // form csv properties
                StringWriter sw = new StringWriter();
                try {
                    CSVWriter writer = new CSVWriter(sw);
                    @SuppressWarnings("rawtypes")
                    Iterator it = propertyMap.entrySet().iterator();
                    while (it.hasNext()) {
                        @SuppressWarnings({"rawtypes"})
                        Map.Entry<String, String> pairs = (Map.Entry) it.next();
                        writer.writeNext(new String[]{pairs.getKey(), pairs.getValue()});
                    }
                    writer.close();
                } catch (Exception ex) {
                    LogUtil.error(getClass().getName(), ex, "");
                }
                String pluginProps = sw.toString();
                participant.setPluginProperties(pluginProps);
            } else {
                PackageParticipant participantExisting = packageDef.getPackageParticipant(processDefId, participantId);
                String oldJson = "";
                if (participantExisting != null && PackageParticipant.TYPE_PLUGIN.equals(participantExisting.getType())) {
                    oldJson = participantExisting.getPluginProperties();
                }

                participant.setPluginProperties(PropertyUtil.propertiesJsonStoreProcessing(oldJson, pluginProperties));
            }
        } else if ((PackageParticipant.TYPE_GROUP.equals(type) || PackageParticipant.TYPE_USER.equals(type)) && packageDef != null) {
            //Using Set to prevent duplicate value
            @SuppressWarnings("rawtypes")
            Set values = new HashSet();
            StringTokenizer valueToken = new StringTokenizer(value, ",");
            while (valueToken.hasMoreTokens()) {
                values.add((String) valueToken.nextElement());
            }

            PackageParticipant participantExisting = packageDef.getPackageParticipant(processDefId, participantId);
            if (participantExisting != null && participantExisting.getValue() != null) {

                StringTokenizer existingValueToken = (type.equals(participantExisting.getType())) ? new StringTokenizer(participantExisting.getValue().replaceAll(";", ","), ",") : null;
                while (existingValueToken != null && existingValueToken.hasMoreTokens()) {
                    values.add((String) existingValueToken.nextElement());
                }
            }

            //Convert Set to String
            value = "";
            @SuppressWarnings("rawtypes")
            Iterator i = values.iterator();
            while (i.hasNext()) {
                value += i.next().toString() + ',';
            }
            if (value.length() > 0) {
                value = value.substring(0, value.length() - 1);
            }
        }
        participant.setType(type);
        participant.setValue(value);

        packageDefinitionDao.addAppParticipant(appId, appDef.getVersion(), participant);

        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        map.addAttribute("participantId", participantId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));

        if (PackageParticipant.TYPE_PLUGIN.equals(type)) {
            return "console/apps/participantPluginConfigSuccess";
        } else {
            return "console/apps/participantAddSuccess";
        }
    }

    @SuppressWarnings("deprecation")
    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/participant/(*:participantId)/plugin/configure")
    public String consoleParticipantPluginConfigure(
            ModelMap map,
            HttpServletRequest request,
            @RequestParam String appId,
            @RequestParam(required = false) String version,
            @RequestParam String processDefId,
            @RequestParam String participantId,
            @RequestParam(value = "value", required = false) String value) throws UnsupportedEncodingException, IOException {

        Plugin plugin = null;

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);

        if (value != null && value.trim().length() > 0) {
            plugin = pluginManager.getPlugin(value);
        } else if (packageDef != null) {
            PackageParticipant participant = packageDef.getPackageParticipant(processDefId, participantId);
            plugin = pluginManager.getPlugin(participant.getValue());

            if (participant.getPluginProperties() != null && participant.getPluginProperties().trim().length() > 0) {
                if (!(plugin instanceof PropertyEditable)) {
                    @SuppressWarnings("rawtypes")
                    Map propertyMap = new HashMap();
                    propertyMap = CsvUtil.getPluginPropertyMap(participant.getPluginProperties());
                    map.addAttribute("propertyMap", propertyMap);
                } else {
                    map.addAttribute("properties", PropertyUtil.propertiesJsonLoadProcessing(participant.getPluginProperties()));
                }
            }
        }

        if (plugin != null) {
            PluginDefaultProperties pluginDefaultProperties = pluginDefaultPropertiesDao.loadById(value, appDef);

            if (pluginDefaultProperties != null) {
                if (!(plugin instanceof PropertyEditable)) {
                    @SuppressWarnings("rawtypes")
                    Map defaultPropertyMap = new HashMap();

                    String properties = pluginDefaultProperties.getPluginProperties();
                    if (properties != null && properties.trim().length() > 0) {
                        defaultPropertyMap = CsvUtil.getPluginPropertyMap(properties);
                    }
                    map.addAttribute("defaultPropertyMap", defaultPropertyMap);
                } else {
                    map.addAttribute("defaultProperties", PropertyUtil.propertiesJsonLoadProcessing(pluginDefaultProperties.getPluginProperties()));
                }
            }
            if (plugin instanceof PropertyEditable) {
                map.addAttribute("propertyEditable", (PropertyEditable) plugin);
            }

            String url = request.getContextPath() + "/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/processes/" + URLEncoder.encode(processDefId, "UTF-8") + "/participant/" + participantId + "/submit/plugin?param_value=" + ClassUtils.getUserClass(plugin).getName();

            map.addAttribute("plugin", plugin);
            map.addAttribute("actionUrl", url);
        }

        return "console/plugin/pluginConfig";
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/participant/(*:participantId)/remove", method = RequestMethod.POST)
    @Transactional
    public String consoleParticipantRemove(ModelMap map,
            @RequestParam String appId,
            @RequestParam(required = false) String version,
            @RequestParam String processDefId,
            @RequestParam String participantId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "value", required = false) String value) throws UnsupportedEncodingException {

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);

        if ((PackageParticipant.TYPE_USER.equals(type) || PackageParticipant.TYPE_GROUP.equals(type)) && value != null) {
            PackageParticipant participantExisting = packageDef.getPackageParticipant(processDefId, participantId);
            if (participantExisting != null && participantExisting.getValue() != null) {
                //Using Set to prevent duplicate value
                @SuppressWarnings("rawtypes")
                Set values = new HashSet();
                StringTokenizer existingValueToken = new StringTokenizer(participantExisting.getValue().replaceAll(";", ","), ",");
                while (existingValueToken.hasMoreTokens()) {
                    String temp = (String) existingValueToken.nextElement();
                    if (!temp.equals(value)) {
                        values.add(temp);
                    }
                }

                //Convert Set to String
                String result = "";
                @SuppressWarnings("rawtypes")
                Iterator i = values.iterator();
                while (i.hasNext()) {
                    result += i.next().toString() + ',';
                }
                if (value.length() > 0) {
                    result = result.substring(0, result.length() - 1);
                }
                participantExisting.setValue(result);
                packageDefinitionDao.addAppParticipant(appId, appDef.getVersion(), participantExisting);
            }
        } else {
            packageDefinitionDao.removeAppParticipant(appId, appDef.getVersion(), processDefId, participantId);
        }

        return "console/apps/participantAdd";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/datalists")
    public String consoleDatalistList(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        return "console/apps/datalistList";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/datalist/create")
    public String consoleDatalistCreate(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        DatalistDefinition datalistDefinition = new DatalistDefinition();
        map.addAttribute("datalistDefinition", datalistDefinition);

        Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
        map.addAttribute("appList", appDefinitionList);

        return "console/apps/datalistCreate";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/datalist/submit/(*:action)", method = RequestMethod.POST)
    public String consoleDatalistSubmit(ModelMap map, @RequestParam("action") String action, @RequestParam String appId, @RequestParam(required = false) String version, @ModelAttribute("datalistDefinition") DatalistDefinition datalistDefinition, BindingResult result, @RequestParam(value = "copyAppId", required = false) String copyAppId, @RequestParam(value = "copyListId", required = false) String copyListId) {
        DatalistDefinition copy = null;
        if (copyAppId != null && !copyAppId.isEmpty() && copyListId != null && !copyListId.isEmpty()) {
            AppDefinition copyAppDef = appService.getAppDefinition(copyAppId, null);
            copy = datalistDefinitionDao.loadById(copyListId, copyAppDef);
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        // validation
        validator.validate(datalistDefinition, result);
        datalistDefinition.setAppDefinition(appDef);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            // check exist
            if (datalistDefinitionDao.loadById(datalistDefinition.getId(), appDef) != null) {
                errors.add("console.datalist.error.label.exists");
            } else {
                String json = JsonUtil.generateDefaultList(datalistDefinition.getId(), datalistDefinition, copy);
                datalistDefinition.setJson(json);
                invalid = !datalistDefinitionDao.add(datalistDefinition);
            }

            if (!errors.isEmpty()) {
                map.addAttribute("errors", errors);
                invalid = true;
            }
        }

        map.addAttribute("datalistDefinition", datalistDefinition);

        if (invalid) {
            Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
            map.addAttribute("appList", appDefinitionList);

            return "console/apps/datalistCreate";
        } else {
            return "console/apps/datalistSaved";
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/datalist/list")
    public void consoleDatalistListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "filter", required = false) String filterString, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<DatalistDefinition> datalistDefinitionList = null;
        Long count = null;

        if (appDef != null) {
            datalistDefinitionList = datalistDefinitionDao.getDatalistDefinitionList(filterString, appDef, sort, desc, start, rows);
            count = datalistDefinitionDao.getDatalistDefinitionListCount(filterString, appDef);
        }

        JSONObject jsonObject = new JSONObject();
        if (datalistDefinitionList != null && datalistDefinitionList.size() > 0) {
            for (DatalistDefinition datalistDefinition : datalistDefinitionList) {
                @SuppressWarnings("rawtypes")
                Map data = new HashMap();
                data.put("id", datalistDefinition.getId());
                data.put("name", datalistDefinition.getName());
                data.put("description", datalistDefinition.getDescription());
                data.put("dateCreated", TimeZoneUtil.convertToTimeZone(datalistDefinition.getDateCreated(), null, AppUtil.getAppDateFormat()));
                data.put("dateModified", TimeZoneUtil.convertToTimeZone(datalistDefinition.getDateModified(), null, AppUtil.getAppDateFormat()));
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/datalist/delete", method = RequestMethod.POST)
    public String consoleDatalistDelete(@RequestParam(value = "ids") String ids, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            datalistDefinitionDao.delete(id, appDef);
        }
        return "console/dialogClose";
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/datalist/options")
    public void consoleDatalistOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

        Collection<DatalistDefinition> datalistDefinitionList = null;

        if (sort == null) {
            sort = "name";
            desc = false;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        datalistDefinitionList = datalistDefinitionDao.getDatalistDefinitionList(null, appDef, sort, desc, start, rows);

        JSONArray jsonArray = new JSONArray();
        @SuppressWarnings("rawtypes")
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        for (DatalistDefinition datalistDef : datalistDefinitionList) {
            @SuppressWarnings("rawtypes")
            Map data = new HashMap();
            data.put("value", datalistDef.getId());
            data.put("label", datalistDef.getName());
            jsonArray.put(data);
        }
        AppUtil.writeJson(writer, jsonArray, callback);
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/userviews")
    public String consoleUserviewList(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        return "console/apps/userviewList";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/userview/create")
    public String consoleUserviewCreate(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        UserviewDefinition userviewDefinition = new UserviewDefinition();
        map.addAttribute("userviewDefinition", userviewDefinition);

        Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
        map.addAttribute("appList", appDefinitionList);

        return "console/apps/userviewCreate";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/userview/submit/(*:action)", method = RequestMethod.POST)
    public String consoleUserviewSubmit(ModelMap map, @RequestParam("action") String action, @RequestParam String appId, @RequestParam(required = false) String version, @ModelAttribute("userviewDefinition") UserviewDefinition userviewDefinition, BindingResult result, @RequestParam(value = "copyAppId", required = false) String copyAppId, @RequestParam(value = "copyUserviewId", required = false) String copyUserviewId) {
        UserviewDefinition copy = null;
        if (copyAppId != null && !copyAppId.isEmpty() && copyUserviewId != null && !copyUserviewId.isEmpty()) {
            AppDefinition copyAppDef = appService.getAppDefinition(copyAppId, null);
            copy = userviewDefinitionDao.loadById(copyUserviewId, copyAppDef);
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        // validation
        validator.validate(userviewDefinition, result);
        userviewDefinition.setAppDefinition(appDef);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            // check exist
            if (userviewDefinitionDao.loadById(userviewDefinition.getId(), appDef) != null) {
                errors.add("console.userview.error.label.exists");
            } else {
                String json = GeneratorUtil.createNewUserviewJson(userviewDefinition.getId(), userviewDefinition.getName(), userviewDefinition.getDescription(), copy);
                userviewDefinition.setJson(json);
                invalid = !userviewDefinitionDao.add(userviewDefinition);
            }

            if (!errors.isEmpty()) {
                map.addAttribute("errors", errors);
                invalid = true;
            }
        }

        map.addAttribute("userviewDefinition", userviewDefinition);

        if (invalid) {
            Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
            map.addAttribute("appList", appDefinitionList);

            return "console/apps/userviewCreate";
        } else {
            return "console/apps/userviewSaved";
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/userview/list")
    public void consoleUserviewListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "filter", required = false) String filterString, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<UserviewDefinition> userviewDefinitionList = null;
        Long count = null;

        if (appDef != null) {
            userviewDefinitionList = userviewDefinitionDao.getUserviewDefinitionList(filterString, appDef, sort, desc, start, rows);
            count = userviewDefinitionDao.getUserviewDefinitionListCount(filterString, appDef);
        }

        JSONObject jsonObject = new JSONObject();
        if (userviewDefinitionList != null && userviewDefinitionList.size() > 0) {
            for (UserviewDefinition userviewDefinition : userviewDefinitionList) {
                @SuppressWarnings("rawtypes")
                Map data = new HashMap();
                data.put("id", userviewDefinition.getId());
                data.put("name", userviewDefinition.getName());
                data.put("description", userviewDefinition.getDescription());
                data.put("dateCreated", TimeZoneUtil.convertToTimeZone(userviewDefinition.getDateCreated(), null, AppUtil.getAppDateFormat()));
                data.put("dateModified", TimeZoneUtil.convertToTimeZone(userviewDefinition.getDateModified(), null, AppUtil.getAppDateFormat()));
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/userview/delete", method = RequestMethod.POST)
    public String consoleUserviewDelete(@RequestParam(value = "ids") String ids, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            userviewDefinitionDao.delete(id, appDef);
        }
        return "console/dialogClose";
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/userview/options")
    public void consoleUserviewOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

        Collection<UserviewDefinition> userviewDefinitionList = null;

        if (sort == null) {
            sort = "name";
            desc = false;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        userviewDefinitionList = userviewDefinitionDao.getUserviewDefinitionList(null, appDef, sort, desc, start, rows);

        JSONArray jsonArray = new JSONArray();
        @SuppressWarnings("rawtypes")
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        for (UserviewDefinition userviewDef : userviewDefinitionList) {
            @SuppressWarnings("rawtypes")
            Map data = new HashMap();
            data.put("value", userviewDef.getId());
            data.put("label", userviewDef.getName());
            jsonArray.put(data);
        }
        AppUtil.writeJson(writer, jsonArray, callback);
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/properties")
    public String consoleProperties(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        boolean protectedReadonly = false;
        if (result != null) {
            protectedReadonly = result.contains("status=invalidLicensor");
            if (!protectedReadonly) {
                return result;
            }
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        checkAppPublishedVersion(appDef);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        map.addAttribute("protectedReadonly", protectedReadonly);

        map.addAttribute("localeList", messageDao.getLocaleList(appDef));
        return "console/apps/properties";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/message/create")
    public String consoleAppMessageCreate(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        map.addAttribute("localeList", getSortedLocalList());

        Message message = new Message();
        message.setLocale(AppUtil.getAppLocale());
        map.addAttribute("message", message);
        return "console/apps/messageCreate";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/message/edit")
    public String consoleAppMessageEdit(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam("id") String id) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        Message message = messageDao.loadById(id, appDef);
        map.addAttribute("message", message);
        return "console/apps/messageEdit";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/message/submit/(*:action)", method = RequestMethod.POST)
    public String consoleAppMessageSubmit(ModelMap map, @RequestParam("action") String action, @RequestParam String appId, @RequestParam(required = false) String version, @ModelAttribute("message") Message message, BindingResult result) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        // validation
        validator.validate(message, result);
        message.setAppDefinition(appDef);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check exist
                if (messageDao.loadById(message.getId(), appDef) != null) {
                    errors.add("console.app.message.error.label.exists");
                } else {
                    invalid = !messageDao.add(message);
                }
            } else {
                Message o = messageDao.loadById(message.getId(), appDef);
                o.setMessage(message.getMessage());
                invalid = !messageDao.update(o);
            }

            if (!errors.isEmpty()) {
                map.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            map.addAttribute("message", message);
            if ("create".equals(action)) {
                map.addAttribute("localeList", getSortedLocalList());
                return "console/apps/messageCreate";
            } else {
                return "console/apps/messageEdit";
            }
        } else {
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/properties?tab=message";
            map.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/message/list")
    public void consoleMessageListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "filter", required = false) String filterString, @RequestParam(value = "locale", required = false) String locale, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<Message> messageList = null;
        Long count = null;

        if (locale != null && locale.trim().isEmpty()) {
            locale = null;
        }

        if (appDef != null) {
            messageList = messageDao.getMessageList(filterString, locale, appDef, sort, desc, start, rows);
            count = messageDao.getMessageListCount(filterString, locale, appDef);
        }

        JSONObject jsonObject = new JSONObject();
        if (messageList != null && messageList.size() > 0) {
            for (Message message : messageList) {
                @SuppressWarnings("rawtypes")
                Map data = new HashMap();
                data.put("id", message.getId());
                data.put("messageKey", message.getMessageKey());
                data.put("locale", message.getLocale());
                data.put("message", message.getMessage());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/message/delete", method = RequestMethod.POST)
    public String consoleAppMessageDelete(@RequestParam(value = "ids") String ids, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            messageDao.delete(id, appDef);
        }
        return "console/dialogClose";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/message/generatepo")
    public String consoleAppMessageGeneratePO(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        map.addAttribute("localeList", getSortedLocalList());
        map.addAttribute("locale", AppUtil.getAppLocale());

        return "console/apps/messageGeneratePO";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/message/generatepo/download")
    public void consoleAppMessageGeneratePODownload(HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "locale", required = false) String locale) throws IOException {
        ServletOutputStream output = null;
        try {
            // verify app
            AppDefinition appDef = appService.getAppDefinition(appId, version);
            if (appDef == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            // validate locale input
            SecurityUtil.validateStringInput(locale);

            // determine output filename
            String filename = appDef.getId() + "_" + appDef.getVersion() + "_" + locale + ".po";

            // set response headers
            response.setContentType("text/plain; charset=utf-8");
            response.addHeader("Content-Disposition", "attachment; filename=" + filename);
            output = response.getOutputStream();

            appService.generatePO(appId, version, locale, output);
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            if (output != null) {
                output.flush();
            }
        }
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/message/importpo")
    public String consoleAppMessageImportPO(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        return "console/apps/messageImportPO";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/message/importpo/submit", method = RequestMethod.POST)
    public String consoleAppMessageInportPOUpload(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) throws Exception {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Setting setting = SetupManager.getSettingByProperty("systemLocale");
        String systemLocale = (setting != null) ? setting.getValue() : null;
        if (systemLocale == null || systemLocale.equalsIgnoreCase("")) {
            systemLocale = "en_US";
        }

        String errorMsg = null;

        MultipartFile multiPartfile = null;

        try {
            multiPartfile = FileStore.getFile("localeFile");
        } catch (FileLimitException e) {
            errorMsg = ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()});
        }

        if (multiPartfile != null) {
            try {
                appService.importPO(appId, version, systemLocale, multiPartfile);
            } catch (IOException e) {
                errorMsg = ResourceBundleUtil.getMessage("console.app.message.import.po.error.invalidPoFile");
            }
        }

        if (errorMsg != null) {
            map.addAttribute("appId", appDef.getId());
            map.addAttribute("appVersion", appDef.getVersion());
            map.addAttribute("appDefinition", appDef);
            map.addAttribute("errorMessage", errorMsg);

            return "console/apps/messageImportPO";
        }

        String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
        String url = contextPath + "/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/properties?tab=message";
        map.addAttribute("url", url);
        return "console/dialogClose";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/envVariable/create")
    public String consoleAppEnvVariableCreate(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        map.addAttribute("environmentVariable", new EnvironmentVariable());
        return "console/apps/envVariableCreate";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/envVariable/edit/(*:id)")
    public String consoleAppEnvVariableEdit(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam("id") String id) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        EnvironmentVariable environmentVariable = environmentVariableDao.loadById(id, appDef);
        map.addAttribute("environmentVariable", environmentVariable);
        return "console/apps/envVariableEdit";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/envVariable/submit/(*:action)", method = RequestMethod.POST)
    public String consoleAppEnvVariableSubmit(ModelMap map, @RequestParam("action") String action, @RequestParam String appId, @RequestParam(required = false) String version, @ModelAttribute("environmentVariable") EnvironmentVariable environmentVariable, BindingResult result) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        // validation
        validator.validate(environmentVariable, result);
        environmentVariable.setAppDefinition(appDef);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check exist
                if (environmentVariableDao.loadById(environmentVariable.getId(), appDef) != null) {
                    errors.add("console.app.envVariable.error.label.exists");
                } else {
                    invalid = !environmentVariableDao.add(environmentVariable);
                }
            } else {
                EnvironmentVariable o = environmentVariableDao.loadById(environmentVariable.getId(), appDef);
                o.setRemarks(environmentVariable.getRemarks());
                o.setValue(environmentVariable.getValue());
                invalid = !environmentVariableDao.update(o);
            }

            if (!errors.isEmpty()) {
                map.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            map.addAttribute("environmentVariable", environmentVariable);
            if ("create".equals(action)) {
                return "console/apps/envVariableCreate";
            } else {
                return "console/apps/envVariableEdit";
            }
        } else {
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/properties?tab=variable";
            map.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/envVariable/list")
    public void consoleEnvVariableListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "filter", required = false) String filterString, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<EnvironmentVariable> environmentVariableList = null;
        Long count = null;

        if (appDef != null) {
            environmentVariableList = environmentVariableDao.getEnvironmentVariableList(filterString, appDef, sort, desc, start, rows);
            count = environmentVariableDao.getEnvironmentVariableListCount(filterString, appDef);
        }

        JSONObject jsonObject = new JSONObject();
        if (environmentVariableList != null && environmentVariableList.size() > 0) {
            for (EnvironmentVariable environmentVariable : environmentVariableList) {
                @SuppressWarnings("rawtypes")
                Map data = new HashMap();
                data.put("id", environmentVariable.getId());
                data.put("value", environmentVariable.getValue());
                data.put("remarks", environmentVariable.getRemarks());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/envVariable/delete", method = RequestMethod.POST)
    public String consoleAppEnvVariableDelete(@RequestParam(value = "ids") String ids, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            environmentVariableDao.delete(id, appDef);
        }
        return "console/dialogClose";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/pluginDefault/create")
    public String consoleAppPluginDefaultCreate(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        map.addAttribute("pluginType", getPluginTypeForDefaultProperty());

        return "console/apps/pluginDefaultCreate";
    }

    @SuppressWarnings("deprecation")
    @RequestMapping("/console/app/(*:appId)/(~:version)/pluginDefault/config")
    public String consoleAppPluginDefaultConfig(ModelMap map, HttpServletRequest request, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam("id") String id, @RequestParam(required = false) String action) throws UnsupportedEncodingException, IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        Plugin plugin = pluginManager.getPlugin(id);

        PluginDefaultProperties pluginDefaultProperties = pluginDefaultPropertiesDao.loadById(id, appDef);

        if (pluginDefaultProperties != null && pluginDefaultProperties.getPluginProperties() != null && pluginDefaultProperties.getPluginProperties().trim().length() > 0) {
            if (!(plugin instanceof PropertyEditable)) {
                @SuppressWarnings("rawtypes")
                Map propertyMap = new HashMap();
                propertyMap = CsvUtil.getPluginPropertyMap(pluginDefaultProperties.getPluginProperties());
                map.addAttribute("propertyMap", propertyMap);
            } else {
                map.addAttribute("properties", PropertyUtil.propertiesJsonLoadProcessing(pluginDefaultProperties.getPluginProperties()));
            }
        }

        if (plugin instanceof PropertyEditable) {
            map.addAttribute("propertyEditable", (PropertyEditable) plugin);
        }

        String url = request.getContextPath() + "/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/pluginDefault/submit/";
        if (pluginDefaultProperties == null) {
            url += "create";
        } else {
            url += "edit";
        }
        url += "?param_id=" + ClassUtils.getUserClass(plugin).getName();

        map.addAttribute("plugin", plugin);
        map.addAttribute("skipValidation", false);
        map.addAttribute("actionUrl", url);

        return "console/plugin/pluginConfig";
    }

    @RequestMapping(value = "/console/app/(*:param_appId)/(~:param_version)/pluginDefault/submit/(*:param_action)", method = RequestMethod.POST)
    public String consoleAppPluginDefaultSubmit(ModelMap map, HttpServletRequest request, @RequestParam("param_action") String action, @RequestParam("param_appId") String appId, @RequestParam(value = "param_version", required = false) String version, @RequestParam("param_id") String id, @RequestParam(required = false) String pluginProperties) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        PluginDefaultProperties pluginDefaultProperties = null;

        if ("create".equals(action)) {
            pluginDefaultProperties = new PluginDefaultProperties();
            pluginDefaultProperties.setAppDefinition(appDef);
            pluginDefaultProperties.setId(id);
        } else {
            pluginDefaultProperties = pluginDefaultPropertiesDao.loadById(id, appDef);
        }

        try {
            Plugin plugin = (Plugin) pluginManager.getPlugin(id);
            pluginDefaultProperties.setPluginName(plugin.getName());
            pluginDefaultProperties.setPluginDescription(plugin.getDescription());
        } catch (Exception e) {
        }

        if (pluginProperties == null) {
            //request params
            @SuppressWarnings({"rawtypes", "unchecked"})
            Map<String, String> propertyMap = new HashMap();
            Enumeration<String> e = request.getParameterNames();
            while (e.hasMoreElements()) {
                String paramName = e.nextElement();

                if (!paramName.startsWith("param_")) {
                    String[] paramValue = (String[]) request.getParameterValues(paramName);
                    propertyMap.put(paramName, CsvUtil.getDeliminatedString(paramValue));
                }
            }

            // form csv properties
            StringWriter sw = new StringWriter();
            try {
                CSVWriter writer = new CSVWriter(sw);
                @SuppressWarnings("rawtypes")
                Iterator it = propertyMap.entrySet().iterator();
                while (it.hasNext()) {
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    Map.Entry<String, String> pairs = (Map.Entry) it.next();
                    writer.writeNext(new String[]{pairs.getKey(), pairs.getValue()});
                }
                writer.close();
            } catch (Exception ex) {
                LogUtil.error(getClass().getName(), ex, "");
            }
            String pluginProps = sw.toString();
            pluginDefaultProperties.setPluginProperties(pluginProps);
        } else {
            pluginDefaultProperties.setPluginProperties(PropertyUtil.propertiesJsonStoreProcessing(pluginDefaultProperties.getPluginProperties(), pluginProperties));
        }

        if ("create".equals(action)) {
            pluginDefaultPropertiesDao.add(pluginDefaultProperties);
        } else {
            pluginDefaultPropertiesDao.update(pluginDefaultProperties);
        }
        String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
        String url = contextPath + "/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/properties?tab=pluginDefault";
        map.addAttribute("url", url);
        return "console/dialogClose";
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/pluginDefault/list")
    public void consolePluginDefaultListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "filter", required = false) String filterString, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<PluginDefaultProperties> pluginDefaultPropertiesList = null;
        Long count = null;

        if (appDef != null) {
            pluginDefaultPropertiesList = pluginDefaultPropertiesDao.getPluginDefaultPropertiesList(filterString, appDef, sort, desc, start, rows);
            count = pluginDefaultPropertiesDao.getPluginDefaultPropertiesListCount(filterString, appDef);
        }

        JSONObject jsonObject = new JSONObject();
        if (pluginDefaultPropertiesList != null && pluginDefaultPropertiesList.size() > 0) {
            for (PluginDefaultProperties pluginDefaultProperties : pluginDefaultPropertiesList) {
                @SuppressWarnings("rawtypes")
                Map data = new HashMap();
                data.put("id", pluginDefaultProperties.getId());
                Plugin p = pluginManager.getPlugin(pluginDefaultProperties.getId());
                data.put("pluginName", p.getI18nLabel());
                data.put("pluginDescription", p.getI18nDescription());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/pluginDefault/delete", method = RequestMethod.POST)
    public String consoleAppPluginDefaultDelete(@RequestParam(value = "ids") String ids, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            pluginDefaultPropertiesDao.delete(id, appDef);
        }
        return "console/dialogClose";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/forms")
    public String consoleFormList(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        checkAppPublishedVersion(appDef);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        return "console/apps/formList";
    }

    protected void checkAppPublishedVersion(AppDefinition appDef) {
        String appId = appDef.getId();
        Long publishedVersion = appService.getPublishedVersion(appId);
        if (publishedVersion == null || publishedVersion <= 0) {
            appDef.setPublished(Boolean.FALSE);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/forms")
    public void consoleFormListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

        Collection<FormDefinition> formDefinitionList = null;
        Long count = null;

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        formDefinitionList = formDefinitionDao.getFormDefinitionList(name, appDef, sort, desc, start, rows);
        count = formDefinitionDao.getFormDefinitionListCount(null, appDef);

        JSONObject jsonObject = new JSONObject();
        for (FormDefinition formDef : formDefinitionList) {
            @SuppressWarnings("rawtypes")
            Map data = new HashMap();
            data.put("id", formDef.getId());
            data.put("name", formDef.getName());
            data.put("tableName", formDef.getTableName());
            data.put("dateCreated", TimeZoneUtil.convertToTimeZone(formDef.getDateCreated(), null, AppUtil.getAppDateFormat()));
            data.put("dateModified", TimeZoneUtil.convertToTimeZone(formDef.getDateModified(), null, AppUtil.getAppDateFormat()));
            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/forms/options")
    public void consoleFormOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

        Collection<FormDefinition> formDefinitionList = null;

        if (sort == null) {
            sort = "name";
            desc = false;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        formDefinitionList = formDefinitionDao.getFormDefinitionList(null, appDef, sort, desc, start, rows);

        JSONArray jsonArray = new JSONArray();
        @SuppressWarnings("rawtypes")
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        for (FormDefinition formDef : formDefinitionList) {
            @SuppressWarnings("rawtypes")
            Map data = new HashMap();
            data.put("value", formDef.getId());
            data.put("label", formDef.getName());
            jsonArray.put(data);
        }
        AppUtil.writeJson(writer, jsonArray, callback);
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/form/create")
    public String consoleFormCreate(ModelMap model, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "activityDefId", required = false) String activityDefId, @RequestParam(value = "processDefId", required = false) String processDefId) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        model.addAttribute("appId", appId);
        model.addAttribute("appVersion", version);
        model.addAttribute("appDefinition", appDef);
        model.addAttribute("formDefinition", new FormDefinition());
        model.addAttribute("activityDefId", activityDefId);
        model.addAttribute("processDefId", processDefId);

        Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
        model.addAttribute("appList", appDefinitionList);

        return "console/apps/formCreate";
    }

    @RequestMapping("/json/console/app/(*:appId)/(~:version)/form/tableNameList")
    public void consoleFormTableNameList(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        Collection<String> tableNameList = formDefinitionDao.getTableNameList(appDef);

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("tableName", tableNameList);
        if (callback != null && callback.trim().length() != 0) {
            writer.write(HtmlUtils.htmlEscape(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/form/submit", method = RequestMethod.POST)
    public String consoleFormSubmit(ModelMap model, @ModelAttribute("formDefinition") FormDefinition formDefinition, BindingResult result, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "activityDefId", required = false) String activityDefId, @RequestParam(value = "processDefId", required = false) String processDefId, @RequestParam(value = "copyAppId", required = false) String copyAppId, @RequestParam(value = "copyFormId", required = false) String copyFormId) throws UnsupportedEncodingException {
        FormDefinition copy = null;
        if (copyAppId != null && !copyAppId.isEmpty() && copyFormId != null && !copyFormId.isEmpty()) {
            AppDefinition copyAppDef = appService.getAppDefinition(copyAppId, null);
            copy = formDefinitionDao.loadById(copyFormId, copyAppDef);
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);

        // validate ID
        validator.validate(formDefinition, result);
        boolean invalid = result.hasErrors();
        if (!invalid) {
            // create form
            String defaultJson = FormUtil.generateDefaultForm(formDefinition.getId(), formDefinition, copy);
            formDefinition.setJson(defaultJson);
            Collection<String> errors = appService.createFormDefinition(appDef, formDefinition);
            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        String formId = formDefinition.getId();
        model.addAttribute("appId", appId);
        model.addAttribute("appDefinition", appDef);
        model.addAttribute("formId", formId);
        model.addAttribute("formDefinition", formDefinition);
        model.addAttribute("activityDefId", activityDefId);
        model.addAttribute("processDefId", processDefId);

        if (invalid) {
            Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
            model.addAttribute("appList", appDefinitionList);

            return "console/apps/formCreate";
        } else {
            if (activityDefId != null && activityDefId.trim().length() > 0 && processDefId != null && processDefId.trim().length() > 0) {
                PackageDefinition packageDef = appDef.getPackageDefinition();
                boolean autoContinue = false;
                if (packageDef != null) {
                    autoContinue = appService.isActivityAutoContinue(packageDef.getId(), packageDef.getVersion().toString(), processDefId, activityDefId);
                }
                processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
                PackageActivityForm activityForm = new PackageActivityForm();
                activityForm.setProcessDefId(processDefId);
                activityForm.setActivityDefId(activityDefId);
                activityForm.setType(PackageActivityForm.ACTIVITY_FORM_TYPE_SINGLE);
                activityForm.setFormId(formId);
                activityForm.setAutoContinue(autoContinue);

                packageDefinitionDao.addAppActivityForm(appId, appDef.getVersion(), activityForm);
                model.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));
            }

            return "console/apps/formSaved";
        }
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/form/(*:formId)/update", method = RequestMethod.POST)
    public String consoleFormUpdate(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "formId") String formId, @RequestParam(value = "json") String json) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        // load existing form definition and update fields
        FormDefinition formDef = formDefinitionDao.loadById(formId, appDef);
        Form form = (Form) formService.createElementFromJson(json);
        formDef.setName(form.getPropertyString("name"));
        formDef.setTableName(form.getPropertyString("tableName"));
        formDef.setJson(PropertyUtil.propertiesJsonStoreProcessing(formDef.getJson(), json));
        formDef.setDescription(form.getPropertyString("description"));

        // update
        formDefinitionDao.update(formDef);
        formDataDao.clearFormCache(form);
        return "console/apps/dialogClose";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/form/delete", method = RequestMethod.POST)
    public String consoleFormDelete(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "formId") String formId) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        StringTokenizer strToken = new StringTokenizer(formId, ",");
        while (strToken.hasMoreTokens()) {
            String id = strToken.nextToken();
            formDefinitionDao.delete(id, appDef);
        }
        return "console/apps/dialogClose";
    }

    @RequestMapping("/console/run/apps")
    public String consoleRunApps(ModelMap model) {
        // get list of published apps.
        Collection<AppDefinition> resultAppDefinitionList = appService.getPublishedApps(null);
        model.addAttribute("appDefinitionList", resultAppDefinitionList);
        return "console/run/runApps";
    }

    @RequestMapping("/console/run/processes")
    public String consoleRunProcesses(ModelMap model) {
        // get list of published processes
        Map<AppDefinition, Collection<WorkflowProcess>> appProcessMap = appService.getPublishedProcesses(null);
        model.addAttribute("appDefinitionList", appProcessMap.keySet());
        model.addAttribute("appProcessMap", appProcessMap);
        return "console/run/runProcesses";
    }

    @RequestMapping("/console/run/inbox")
    public String consoleRunInbox(ModelMap model) {
        User user = directoryManager.getUserByUsername(WorkflowUtil.getCurrentUsername());
        if (user != null) {
            model.addAttribute("rssLink", "/web/rss/client/inbox?j_username=" + user.getUsername() + "&hash=" + user.getLoginHash());
        }
        // additional UserSecurity settings
        UserSecurity us = DirectoryUtil.getUserSecurity();
        model.addAttribute("userSecurity", us);
        return "console/run/inbox";
    }

    @RequestMapping("/console/setting/general")
    public String consoleSettingGeneral(ModelMap map) {
        Collection<Setting> settingList = setupManager.getSettingList("", null, null, null, null);

        Map<String, String> settingMap = new HashMap<String, String>();
        for (Setting setting : settingList) {
            if (SetupManager.MASTER_LOGIN_PASSWORD.equals(setting.getProperty())) {
                settingMap.put(setting.getProperty(), SetupManager.SECURE_VALUE);
            } else {
                settingMap.put(setting.getProperty(), setting.getValue());
            }
        }

        String masterLoginUsername = SetupManager.getSettingValue("masterLoginUsername");
        String masterLoginPassword = SetupManager.getSettingValue("masterLoginPassword");

        if ((masterLoginUsername != null && masterLoginUsername.trim().length() > 0)
                && (masterLoginPassword != null && masterLoginPassword.length() > 0)) {
            //decryt masterLoginPassword
            masterLoginPassword = SecurityUtil.decrypt(masterLoginPassword);

            User master = new User();
            master.setUsername(masterLoginUsername.trim());
            master.setPassword(StringUtil.md5Base16(masterLoginPassword));

            settingMap.put(SetupManager.MASTER_LOGIN_HASH, master.getLoginHash().toUpperCase());
        }

        Locale[] localeList = Locale.getAvailableLocales();
        Map<String, String> localeStringList = new TreeMap<String, String>();
        for (int x = 0; x < localeList.length; x++) {
            localeStringList.put(localeList[x].toString(), localeList[x].toString() + " - " + localeList[x].getDisplayName(LocaleContextHolder.getLocale()));
        }

        map.addAttribute("serverTZ", TimeZoneUtil.getServerTimeZoneID());
        map.addAttribute("timezones", TimeZoneUtil.getList());
        map.addAttribute("localeList", localeStringList);
        map.addAttribute("settingMap", settingMap);

        // additional UserSecurity settings
        UserSecurity us = DirectoryUtil.getUserSecurity();
        map.addAttribute("userSecurity", us);

        return "console/setting/general";
    }

    @RequestMapping("/console/setting/general/loginHash")
    public void loginHash(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("username") String username, @RequestParam("password") String password) throws JSONException, IOException {
        if (SetupManager.SECURE_VALUE.equals(password)) {
            password = SetupManager.getSettingValue(SetupManager.MASTER_LOGIN_PASSWORD);
            password = SecurityUtil.decrypt(password);
        }

        User user = userDao.getUser(username);
        //user.setUsername(username);
        //user.setPassword(StringUtil.md5Base16(password));

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("hash", user != null ? user.getLoginHash() : "");

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/setting/general/submit", method = RequestMethod.POST)
    public String consoleSettingGeneralSubmit(HttpServletRequest request, ModelMap map) throws Exception {
        String currentUsername = WorkflowUtil.getCurrentUsername();
        List<String> settingsIsNotNull = new ArrayList<String>();

        List<String> booleanSettingsList = new ArrayList<String>();
        booleanSettingsList.add("deleteProcessOnCompletion");
        booleanSettingsList.add("enableNtlm");
        booleanSettingsList.add("rightToLeft");
        booleanSettingsList.add("enableUserLocale");
        booleanSettingsList.add("dateFormatFollowLocale");
        booleanSettingsList.add("disableAdminBar");
        booleanSettingsList.add("disableWebConsole");
        booleanSettingsList.add("disablePerformanceAnalyzer");
        booleanSettingsList.add("disableListRenderHtml");

        //request params
        @SuppressWarnings("rawtypes")
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String paramName = (String) e.nextElement();
            String paramValue = request.getParameter(paramName);

            if (booleanSettingsList.contains(paramName)) {
                settingsIsNotNull.add(paramName);
                paramValue = "true";
            }

            Setting setting = SetupManager.getSettingByProperty(paramName);
            if (setting == null) {
                setting = new Setting();
                setting.setProperty(paramName);
                setting.setValue(paramValue);
            } else {
                setting.setValue(paramValue);
            }

            if (SetupManager.MASTER_LOGIN_PASSWORD.equals(paramName)) {
                if (SetupManager.SECURE_VALUE.equals(paramValue)) {
                    String currentMasterLoginPassword = SetupManager.getSettingValueFromDb(SetupManager.MASTER_LOGIN_PASSWORD);
                    setting.setValue(currentMasterLoginPassword);
                } else {
                    setting.setValue(SecurityUtil.encrypt(paramValue));
                }
            }

            if (HostManager.isVirtualHostEnabled() && ("dataFileBasePath".equals(paramName) || "designerwebBaseUrl".equals(paramName))) {
                setting.setValue("");
            }
            setting.setDateModified(new Date());
            setting.setModifiedBy(currentUsername);
            setupManager.saveSetting(setting);
        }

        for (String s : booleanSettingsList) {
            if (!settingsIsNotNull.contains(s)) {
                Setting setting = SetupManager.getSettingByProperty(s);
                if (setting == null) {
                    setting = new Setting();
                    setting.setProperty(s);
                }
                setting.setValue("false");
                setting.setDateModified(new Date());
                setting.setModifiedBy(currentUsername);
                setupManager.saveSetting(setting);
            }
        }

        pluginManager.refresh();
        workflowManager.internalUpdateDeadlineChecker();
        FileStore.updateFileSizeLimit();

        kecakRouteManager.stopContext();
        Thread.sleep(3000);

        kecakRouteManager.startContext();

        return "redirect:/web/console/setting/general";
    }

    @RequestMapping("/console/setting/datasource")
    public String consoleSettingDatasource(ModelMap map) {
        Map<String, String> settingMap = new HashMap<String, String>();

        Properties properties = DynamicDataSourceManager.getProperties();
        for (Object key : properties.keySet()) {
            if (!DynamicDataSourceManager.SECURE_FIELD.equals(key)) {
                settingMap.put(key.toString(), properties.getProperty(key.toString()));
            } else {
                settingMap.put(key.toString(), DynamicDataSourceManager.SECURE_VALUE);
            }
        }

        map.addAttribute("settingMap", settingMap);
        map.addAttribute("profileList", DynamicDataSourceManager.getProfileList());
        map.addAttribute("currentProfile", DynamicDataSourceManager.getCurrentProfile());

        return "console/setting/datasource";
    }

    @RequestMapping(value = "/console/setting/profile/change", method = RequestMethod.POST)
    public void consoleProfileChange(Writer writer, @RequestParam("profileName") String profileName) {
        if (!HostManager.isVirtualHostEnabled()) {
            SecurityUtil.validateStringInput(profileName);
            DynamicDataSourceManager.changeProfile(profileName);
        }
    }

    @RequestMapping(value = "/console/setting/profile/create", method = RequestMethod.POST)
    public void consoleProfileCreate(Writer writer, HttpServletRequest request, @RequestParam("profileName") String profileName) {
        if (!HostManager.isVirtualHostEnabled()) {
            SecurityUtil.validateStringInput(profileName);
            //get 
            String secureValue = DynamicDataSourceManager.getProperty(DynamicDataSourceManager.SECURE_FIELD);

            DynamicDataSourceManager.createProfile(profileName);
            DynamicDataSourceManager.changeProfile(profileName);

            //request params
            @SuppressWarnings("rawtypes")
            Enumeration e = request.getParameterNames();
            while (e.hasMoreElements()) {
                String paramName = (String) e.nextElement();
                if (!paramName.equals("profileName")) {
                    String paramValue = request.getParameter(paramName);

                    if (DynamicDataSourceManager.SECURE_FIELD.equals(paramName) && DynamicDataSourceManager.SECURE_VALUE.equals(paramValue)) {
                        paramValue = secureValue;
                    }

                    DynamicDataSourceManager.writeProperty(paramName, paramValue);
                }
            }
        }
    }

    @RequestMapping(value = "/console/setting/profile/delete", method = RequestMethod.POST)
    public void consoleProfileDelete(Writer writer, @RequestParam("profileName") String profileName) {
        if (!HostManager.isVirtualHostEnabled()) {
            SecurityUtil.validateStringInput(profileName);
            DynamicDataSourceManager.deleteProfile(profileName);
        }
    }

    @RequestMapping(value = "/console/setting/datasource/submit", method = RequestMethod.POST)
    public String consoleSetupDatasourceSubmit(HttpServletRequest request, ModelMap map) {
        //request params
        @SuppressWarnings("rawtypes")
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String paramName = (String) e.nextElement();
            if (!paramName.equals("profileName")) {
                String paramValue = request.getParameter(paramName);

                if (DynamicDataSourceManager.SECURE_FIELD.equals(paramName) && DynamicDataSourceManager.SECURE_VALUE.equals(paramValue)) {
                    paramValue = DynamicDataSourceManager.getProperty(DynamicDataSourceManager.SECURE_FIELD);
                }

                DynamicDataSourceManager.writeProperty(paramName, paramValue);
            }
        }

        return "redirect:/web/console/setting/datasource";
    }

    @RequestMapping("/console/setting/directory")
    public String consoleSettingDirectory(ModelMap map) {
        Collection<Setting> settingList = setupManager.getSettingList("", null, null, null, null);

        Map<String, String> settingMap = new HashMap<String, String>();
        for (Setting setting : settingList) {
            settingMap.put(setting.getProperty(), setting.getValue());
        }

        //get directory manager plugin list
        Collection<Plugin> pluginList = pluginManager.list();
        @SuppressWarnings("rawtypes")
        Iterator i = pluginList.iterator();
        while (i.hasNext()) {
            Plugin plugin = (Plugin) i.next();
            if (!(plugin instanceof DirectoryManagerPlugin)) {
                i.remove();
            }
        }
        String className = "";
        if (DirectoryUtil.isOverridden()) {
            className = DirectoryUtil.getOverriddenDirectoryManagerClassName();
        } else if (settingMap.get("directoryManagerImpl") != null) {
            className = settingMap.get("directoryManagerImpl");
        }

        if (className != null && !className.isEmpty()) {
            Plugin plugin = pluginManager.getPlugin(className);
            if (plugin != null) {
                map.addAttribute("directoryManagerName", plugin.getI18nLabel());
            }
        }

        map.addAttribute("settingMap", settingMap);
        map.addAttribute("directoryManagerPluginList", pluginList);

        return "console/setting/directoryManager";
    }

    @RequestMapping(value = "/console/setting/directoryManagerImpl/remove", method = RequestMethod.POST)
    public void consoleSettingDirectoryManagerImplRemove(Writer writer, ModelMap map) {
        setupManager.deleteSetting("directoryManagerImpl");
        setupManager.deleteSetting("directoryManagerImplProperties");
    }

    @SuppressWarnings("deprecation")
    @RequestMapping("/console/setting/directoryManagerImpl/config")
    public String consoleSettingDirectoryManagerImplConfig(ModelMap map, @RequestParam("directoryManagerImpl") String directoryManagerImpl, HttpServletRequest request) throws IOException {
        Plugin plugin = pluginManager.getPlugin(directoryManagerImpl);

        if (plugin != null) {
            String properties = "";
            if (directoryManagerImpl != null && directoryManagerImpl.equals(DirectoryUtil.getOverriddenDirectoryManagerClassName())) {
                properties = SetupManager.getSettingValue(DirectoryUtil.CUSTOM_IMPL_PROPERTIES);
            } else {
                properties = SetupManager.getSettingValue(DirectoryUtil.IMPL_PROPERTIES);
            }

            if (!(plugin instanceof PropertyEditable)) {
                @SuppressWarnings("rawtypes")
                Map propertyMap = new HashMap();
                propertyMap = CsvUtil.getPluginPropertyMap(properties);
                map.addAttribute("propertyMap", propertyMap);
            } else {
                map.addAttribute("properties", PropertyUtil.propertiesJsonLoadProcessing(properties));
            }

            if (plugin instanceof PropertyEditable) {
                map.addAttribute("propertyEditable", (PropertyEditable) plugin);
            }

            map.addAttribute("plugin", plugin);

            String url = request.getContextPath() + "/web/console/setting/directoryManagerImpl/config/submit?id=" + directoryManagerImpl;
            map.addAttribute("actionUrl", url);

            return "console/plugin/pluginConfig";
        } else {
            return "error404";
        }
    }

    @RequestMapping(value = "/console/setting/directoryManagerImpl/config/submit", method = RequestMethod.POST)
    public String consoleSettingDirectoryManagerImplConfigSubmit(ModelMap map, @RequestParam("id") String id, @RequestParam(value = "pluginProperties", required = false) String pluginProperties, HttpServletRequest request) {
        String currentUsername = WorkflowUtil.getCurrentUsername();
        @SuppressWarnings("unused")
        Plugin plugin = (Plugin) pluginManager.getPlugin(id);

        String settingName = "";
        if (id != null && id.equals(DirectoryUtil.getOverriddenDirectoryManagerClassName())) {
            settingName = DirectoryUtil.CUSTOM_IMPL_PROPERTIES;
        } else {
            settingName = DirectoryUtil.IMPL_PROPERTIES;
        }

        //save plugin
        Setting setting = SetupManager.getSettingByProperty("directoryManagerImpl");
        if (setting == null) {
            setting = new Setting();
            setting.setProperty("directoryManagerImpl");
        }
        setting.setValue(id);
        setting.setDateModified(new Date());
        setting.setModifiedBy(currentUsername);
        setupManager.saveSetting(setting);

        Setting propertySetting = SetupManager.getSettingByProperty(settingName);
        if (propertySetting == null) {
            propertySetting = new Setting();
            propertySetting.setProperty(settingName);
        }

        if (pluginProperties == null) {
            //request params
            @SuppressWarnings({"unchecked", "rawtypes"})
            Map<String, String> propertyMap = new HashMap();
            Enumeration<String> e = request.getParameterNames();
            while (e.hasMoreElements()) {
                String paramName = e.nextElement();

                if (!paramName.startsWith("param_")) {
                    String[] paramValue = (String[]) request.getParameterValues(paramName);
                    propertyMap.put(paramName, CsvUtil.getDeliminatedString(paramValue));
                }
            }

            // form csv properties
            StringWriter sw = new StringWriter();
            try {
                CSVWriter writer = new CSVWriter(sw);
                @SuppressWarnings("rawtypes")
                Iterator it = propertyMap.entrySet().iterator();
                while (it.hasNext()) {
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    Map.Entry<String, String> pairs = (Map.Entry) it.next();
                    writer.writeNext(new String[]{pairs.getKey(), pairs.getValue()});
                }
                writer.close();
            } catch (Exception ex) {
                LogUtil.error(getClass().getName(), ex, "");
            }
            String pluginProps = sw.toString();
            propertySetting.setValue(pluginProps);
        } else {
            propertySetting.setValue(PropertyUtil.propertiesJsonStoreProcessing(propertySetting.getValue(), pluginProperties));
        }
        setting.setDateModified(new Date());
        setting.setModifiedBy(currentUsername);
        setupManager.saveSetting(propertySetting);

        String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
        String url = contextPath + "/web/console/setting/directory";
        map.addAttribute("url", url);
        return "console/dialogClose";
    }

    @RequestMapping("/console/setting/plugin")
    public String consoleSettingPlugin(ModelMap map) {
        map.addAttribute("pluginType", getPluginType());
        return "console/setting/plugin";
    }

    @RequestMapping("/console/setting/plugin/refresh")
    public void consoleSettingPluginRefresh(Writer writer) {
        setupManager.clearCache();
        pluginManager.refresh();
    }

    @RequestMapping("/console/setting/plugin/upload")
    public String consoleSettingPluginUpload() {
        return "console/setting/pluginUpload";
    }

    @RequestMapping(value = "/console/setting/plugin/upload/submit", method = RequestMethod.POST)
    public String consoleSettingPluginUploadSubmit(ModelMap map, HttpServletRequest request) throws IOException {
        MultipartFile pluginFile;

        try {
            pluginFile = FileStore.getFile("pluginFile");
        } catch (FileLimitException e) {
            map.addAttribute("errorMessage", ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()}));
            return "console/setting/pluginUpload";
        }

        try {
            pluginManager.upload(pluginFile.getOriginalFilename(), pluginFile.getInputStream());
        } catch (Exception e) {
            if (e.getCause().getMessage() != null && e.getCause().getMessage().contains("Invalid jar file")) {
                map.addAttribute("errorMessage", "Invalid jar file");
            } else {
                map.addAttribute("errorMessage", "Error uploading plugin");
            }
            return "console/setting/pluginUpload";
        }
        String url = request.getContextPath() + "/web/console/setting/plugin";
        map.addAttribute("url", url);
        return "console/dialogClose";
    }

    @RequestMapping(value = "/console/setting/plugin/uninstall", method = RequestMethod.POST)
    public String consoleSettingPluginUninstall(ModelMap map, @RequestParam("selectedPlugins") String selectedPlugins) {
        StringTokenizer strToken = new StringTokenizer(selectedPlugins, ",");
        while (strToken.hasMoreTokens()) {
            String pluginClassName = (String) strToken.nextElement();
            pluginManager.uninstall(pluginClassName);
        }
        return "redirect:/web/console/setting/plugin";
    }

    @RequestMapping("/console/setting/message")
    public String consoleSettingMessage(ModelMap map) {
        map.addAttribute("localeList", getSortedLocalList());
        return "console/setting/message";
    }

    @RequestMapping("/console/setting/message/create")
    public String consoleSettingMessageCreate(ModelMap map) {
        map.addAttribute("localeList", getSortedLocalList());

        ResourceBundleMessage message = new ResourceBundleMessage();
        map.addAttribute("message", message);
        return "console/setting/messageCreate";
    }

    @RequestMapping("/console/setting/message/edit/(*:id)")
    public String consoleSettingMessageEdit(ModelMap map, @RequestParam("id") String id) {
        ResourceBundleMessage message = rbmDao.getMessageById(id);
        map.addAttribute("message", message);
        return "console/setting/messageEdit";
    }

    @RequestMapping(value = "/console/setting/message/submit/(*:action)", method = RequestMethod.POST)
    public String consoleSettingMessageSubmit(ModelMap map, @RequestParam("action") String action, @ModelAttribute("message") ResourceBundleMessage message, BindingResult result) {
        // validation
        validator.validate(message, result);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check exist
                if (rbmDao.getMessage(message.getKey(), message.getLocale()) != null) {
                    errors.add("console.app.message.error.label.exists");
                } else {
                    rbmDao.saveOrUpdate(message);
                    invalid = false;
                }
            } else {
                ResourceBundleMessage o = rbmDao.getMessageById(message.getId());
                o.setMessage(message.getMessage());
                rbmDao.saveOrUpdate(o);
                invalid = false;
            }

            if (!errors.isEmpty()) {
                map.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            map.addAttribute("message", message);
            if ("create".equals(action)) {
                map.addAttribute("localeList", getSortedLocalList());
                return "console/setting/messageCreate";
            } else {
                return "console/setting/messageEdit";
            }
        } else {
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/setting/message";
            map.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/json/console/setting/message/list")
    public void consoleSettingMessageListJson(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "filter", required = false) String filterString, @RequestParam(value = "locale", required = false) String locale, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

        String condition = "";
        List<String> param = new ArrayList<String>();

        if (locale != null && locale.trim().length() != 0) {
            condition += "e.locale = ? ";
            param.add(locale);
        }

        if (filterString != null && filterString.trim().length() != 0) {
            if (!condition.isEmpty()) {
                condition += " and";
            }
            condition += " (e.key like ? or e.message like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
        }

        if (condition.length() > 0) {
            condition = "WHERE " + condition;
        }

        List<ResourceBundleMessage> messageList = rbmDao.getMessages(condition, param.toArray(new String[param.size()]), sort, desc, start, rows);
        Long count = rbmDao.count(condition, param.toArray(new String[param.size()]));

        JSONObject jsonObject = new JSONObject();
        if (messageList != null && messageList.size() > 0) {
            for (ResourceBundleMessage message : messageList) {
                @SuppressWarnings("rawtypes")
                Map data = new HashMap();
                data.put("id", message.getId());
                data.put("key", message.getKey());
                data.put("locale", message.getLocale());
                data.put("message", message.getMessage());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/setting/message/delete", method = RequestMethod.POST)
    public String consoleSettingMessageDelete(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            ResourceBundleMessage o = rbmDao.getMessageById(id);
            rbmDao.delete(o);
        }
        return "console/dialogClose";
    }

    @RequestMapping("/console/setting/message/import")
    public String consoleSettingMessageImport(ModelMap map) {
        map.addAttribute("localeList", getSortedLocalList());
        return "console/setting/messageImport";
    }

    @RequestMapping(value = "/console/setting/message/import/submit", method = RequestMethod.POST)
    public String consoleSettingMessagePOFileUpload(ModelMap map) throws Exception {
        Setting setting = SetupManager.getSettingByProperty("systemLocale");
        String systemLocale = (setting != null) ? setting.getValue() : null;
        if (systemLocale == null || systemLocale.equalsIgnoreCase("")) {
            systemLocale = "en_US";
        }

        String errorMsg = null;
        MultipartFile multiPartfile = null;

        try {
            multiPartfile = FileStore.getFile("localeFile");
        } catch (FileLimitException e) {
            errorMsg = ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()});
        }

        if (multiPartfile != null) {
            try {
                ResourceBundleUtil.POFileImport(multiPartfile, systemLocale);
            } catch (IOException e) {
                errorMsg = ResourceBundleUtil.getMessage("console.setting.message.import.error.invalidPoFile");
            }
        }

        if (errorMsg != null) {
            map.addAttribute("errorMessage", errorMsg);
            map.addAttribute("localeList", getSortedLocalList());
            return "console/setting/messageImport";
        }

        String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
        String url = contextPath + "/web/console/setting/message";
        map.addAttribute("url", url);
        return "console/dialogClose";
    }

    @RequestMapping("/console/setting/eaContent")
    public String consoleSettingEmailApprovalContent(ModelMap map) {
        return "console/setting/eaContent";
    }

    @RequestMapping("/console/setting/eaContent/create")
    public String consoleSettingEmailApprovalContentCreate(ModelMap map) {
        EmailApprovalContent eaContent = new EmailApprovalContent();
        map.addAttribute("eaContent", eaContent);
        return "console/setting/eaContentCreate";
    }

    @RequestMapping("/console/setting/eaContent/edit/(*:id)")
    public String consoleSettingEmailApprovalContentEdit(ModelMap map, @RequestParam("id") String id) {
        EmailApprovalContent eaContent = eaContentDao.getEmailApprovalContentById(id);
        map.addAttribute("eaContent", eaContent);
        return "console/setting/eaContentEdit";
    }

    @RequestMapping(value = "/console/setting/eaContent/delete", method = RequestMethod.POST)
    public String consoleSettingEmailApprovalContentDelete(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            EmailApprovalContent eaContent = eaContentDao.getEmailApprovalContentById(id);
            eaContentDao.delete(eaContent);
        }
        return "console/dialogClose";
    }

    @RequestMapping("/json/console/setting/eaContent/list")
    public void consoleSettingEmailApprovalContentListJson(Writer writer, @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "processId", required = false) String processId, @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

        String condition = "";
        List<String> param = new ArrayList<String>();

        if (processId != null && processId.trim().length() != 0) {
            if (!condition.isEmpty()) {
                condition += " and";
            }
            condition += " (e.processId like ? or e.activityId like ?)";
            param.add("%" + processId + "%");
            param.add("%" + processId + "%");
        }

        if (condition.length() > 0) {
            condition = "WHERE " + condition;
        }

        List<EmailApprovalContent> emailApprovalContentList = eaContentDao.getEmailApprovalContents(condition, param.toArray(new String[param.size()]),
                sort, desc, start, rows);

        Long count = eaContentDao.count(condition, param.toArray(new String[param.size()]));

        JSONObject jsonObject = new JSONObject();
        if (emailApprovalContentList != null && emailApprovalContentList.size() > 0) {
            for (EmailApprovalContent eaContent : emailApprovalContentList) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("id", eaContent.getId());
                data.put("processId", eaContent.getProcessId());
                data.put("activityId", eaContent.getActivityId());
                data.put("content", eaContent.getContent());
                data.put("createdate", eaContent.getDateCreated() == null ? "" : eaContent.getDateCreated());
                data.put("modifiedate", eaContent.getDateModified() == null ? "" : eaContent.getDateModified());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/setting/eaContent/submit/(*:action)", method = RequestMethod.POST)
    public String consoleSettingEmailApprovalContentSubmit(ModelMap map, @RequestParam("action") String action, @ModelAttribute("eaContent") EmailApprovalContent eaContent, BindingResult result) {
        // validation
        validator.validate(eaContent, result);
        Date now = new Date();
        String currUsername = workflowUserManager.getCurrentUsername();

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check exist
                if (eaContentDao.getEmailApprovalContent(eaContent.getProcessId(), eaContent.getActivityId()) != null) {
                    errors.add("console.app.message.error.label.exists");
                } else {
                    eaContent.setDateCreated(now);
                    eaContent.setCreatedBy(currUsername);
                    eaContent.setDateModified(now);
                    eaContent.setModifiedBy(currUsername);

                    eaContentDao.saveOrUpdate(eaContent);
                    invalid = false;
                }
            } else {
                EmailApprovalContent o = eaContentDao.getEmailApprovalContentById(eaContent.getId());
                o.setContent(eaContent.getContent());
                o.setDateModified(now);
                o.setModifiedBy(currUsername);

                eaContentDao.saveOrUpdate(o);
                invalid = false;
            }

            if (!errors.isEmpty()) {
                map.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            map.addAttribute("eaContent", eaContent);
            if ("create".equals(action)) {
                return "console/setting/eaContentCreate";
            } else {
                return "console/setting/eaContentEdit";
            }
        } else {
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/setting/eaContent";
            map.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping("/console/setting/scheduler")
    public String consoleSettingSchedulerContent(ModelMap map) {
        return "console/setting/scheduler";
    }

    @RequestMapping("/console/setting/scheduler/create")
    public String consoleSettingSchedulerCreate(ModelMap map) {
        SchedulerDetails schedulerDetails = new SchedulerDetails();
        map.addAttribute("schedulerDetails", schedulerDetails);
        return "console/setting/schedulerCreate";
    }

    @RequestMapping(value = "/console/setting/scheduler/submit/(*:action)", method = RequestMethod.POST)
    public String consoleSettingSchedulerSubmit(ModelMap map, @RequestParam("action") String action, @ModelAttribute("schedulerDetails") SchedulerDetails schedulerDetails, BindingResult result) {
        // validation
        validator.validate(schedulerDetails, result);
        Date now = new Date();
        String currUsername = workflowUserManager.getCurrentUsername();

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check exist
                SchedulerDetails currentByJob = schedulerDetailsDao.getSchedulerDetailsByJob(schedulerDetails.getJobName(), schedulerDetails.getGroupJobName());
                SchedulerDetails currentByTrigger = schedulerDetailsDao.getSchedulerDetailsByTrigger(schedulerDetails.getTriggerName(), schedulerDetails.getGroupTriggerName());
                if (currentByJob != null || currentByTrigger != null) {
                    errors.add("console.app.message.error.label.exists");
                } else {
                    schedulerDetails.setDateCreated(now);
                    schedulerDetails.setCreatedBy(currUsername);
                    schedulerDetails.setDateModified(now);
                    schedulerDetails.setModifiedBy(currUsername);
                    schedulerDetails.setTriggerTypes(TriggerTypes.CRON);
                    try {
                        schedulerManager.saveOrUpdateJobDetails(schedulerDetails);
                        invalid = false;
                    } catch (Exception e) {
                        invalid = true;
                        errors.add("console.app.message.error.label.exception");
                        e.printStackTrace();
                        LOGGER.error(e.getMessage());
                    }
                }
            } else {
                SchedulerDetails details = schedulerDetailsDao.getSchedulerDetailsById(schedulerDetails.getId());
                details.setCronExpression(schedulerDetails.getCronExpression());
                details.setJobClassName(schedulerDetails.getJobClassName());
                details.setDateModified(now);
                details.setModifiedBy(currUsername);

                try {
                    schedulerManager.updateJobDetails(details);
                    invalid = false;
                } catch (Exception e) {
                    invalid = true;
                    errors.add("console.app.message.error.label.exception");
                    e.printStackTrace();
                    LOGGER.error(e.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                map.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            map.addAttribute("schedulerDetails", schedulerDetails);
            if ("create".equals(action)) {
                return "console/setting/schedulerCreate";
            } else {
                return "console/setting/schedulerEdit";
            }
        } else {
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/setting/scheduler";
            map.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping("/json/console/setting/scheduler/list")
    public void consoleSettingSchedulerListJson(Writer writer, @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "jobName", required = false) String jobName, @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

        String condition = "";
        List<String> param = new ArrayList<String>();

        if (jobName != null && jobName.trim().length() != 0) {
            if (!condition.isEmpty()) {
                condition += " and";
            }
            condition += " (e.jobName like ? )";
            param.add("%" + jobName + "%");
        }

        if (condition.length() > 0) {
            condition = "WHERE " + condition;
        }

        List<SchedulerDetails> schedulerList = schedulerDetailsDao.getSchedulerDetails(condition, param.toArray(new String[param.size()]),
                sort, desc, start, rows);

        Long count = schedulerDetailsDao.count(condition, param.toArray(new String[param.size()]));

        JSONObject jsonObject = new JSONObject();
        if (schedulerList != null && schedulerList.size() > 0) {
            for (SchedulerDetails details : schedulerList) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("id", details.getId());
                data.put("jobName", details.getJobName());
                data.put("groupJobName", details.getGroupJobName());
                data.put("triggerName", details.getTriggerName());
                data.put("groupTriggerName", details.getGroupTriggerName());
                data.put("jobClassName", details.getJobClassName());
                data.put("modifiedate", details.getDateModified() == null ? "" : details.getDateModified());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/setting/scheduler/delete", method = RequestMethod.POST)
    public String consoleSettingSchedulerDelete(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            SchedulerDetails details = schedulerDetailsDao.getSchedulerDetailsById(id);
            schedulerManager.deleteJob(details);
        }
        return "console/dialogClose";
    }

    @RequestMapping(value = "/console/setting/scheduler/firenow", method = RequestMethod.POST)
    public String consoleSettingSchedulerFireNow(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            SchedulerDetails details = schedulerDetailsDao.getSchedulerDetailsById(id);
            try {
                schedulerManager.fireNow(details);
            } catch (SchedulerException e) {
                LOGGER.error("Unable to fire " + "[" + details.getJobName() + "] " + e.getMessage());
                continue;
            }
        }
        return "console/dialogClose";
    }

    @RequestMapping("/console/setting/scheduler/edit/(*:id)")
    public String consoleSettingSchedulerEdit(ModelMap map, @RequestParam("id") String id) {
        SchedulerDetails schedulerDetails = schedulerDetailsDao.getSchedulerDetailsById(id);
        map.addAttribute("schedulerDetails", schedulerDetails);
        return "console/setting/schedulerEdit";
    }

    @RequestMapping("/console/setting/property")
    public String consoleSettingProperty(ModelMap map) {
        return "console/setting/property";
    }

    @RequestMapping("/json/console/setting/property/list")
    public void consoleSettingPropertyListJson(Writer writer, @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "propertyLabel", required = false) String propertyLabel, @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

        String condition = "";
        List<String> param = new ArrayList<String>();

        if (propertyLabel != null && propertyLabel.trim().length() != 0) {
            if (!condition.isEmpty()) {
                condition += " and";
            }
            condition += " (e.propertyLabel like ? )";
            param.add("%" + propertyLabel + "%");
        }

        if (condition.length() > 0) {
            condition = "WHERE " + condition;
        }

        List<Property> properties = propertyDao.getProperties(condition, param.toArray(new String[param.size()]),
                sort, desc, start, rows);

        Long count = propertyDao.count(condition, param.toArray(new String[param.size()]));

        JSONObject jsonObject = new JSONObject();
        if (properties != null && properties.size() > 0) {
            for (Property property : properties) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("id", property.getId());
                data.put("propertyLabel", property.getPropertyLabel());
                data.put("propertyValue", property.getPropertyValue());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/console/setting/property/create")
    public String consoleSettingPropertyCreate(ModelMap map) {
        Property property = new Property();
        map.addAttribute("property", property);
        return "console/setting/propertyCreate";
    }

    @RequestMapping("/console/setting/property/edit/(*:id)")
    public String consoleSettingPropertyEdit(ModelMap map, @RequestParam("id") String id) {
        Property property = propertyDao.getPropertyById(id);
        map.addAttribute("property", property);
        return "console/setting/propertyEdit";
    }

    @RequestMapping(value = "/console/setting/property/delete", method = RequestMethod.POST)
    public String consoleSettingPropertyDelete(@RequestParam(value = "ids") String ids) throws IOException {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            Property property = propertyDao.getPropertyById(id);
            propertyDao.delete(property);
        }
        applicationProperties.refresh();
        return "console/dialogClose";
    }

    @RequestMapping(value = "/console/setting/property/submit/(*:action)", method = RequestMethod.POST)
    public String consoleSettingPropertySubmit(ModelMap map, @RequestParam("action") String action, @ModelAttribute("property") Property property, BindingResult result) {
        // validation
        validator.validate(property, result);
        Date now = new Date();
        String currUsername = workflowUserManager.getCurrentUsername();

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check exist
                Property currentProperty = propertyDao.getPropertyByLabel(property.getPropertyLabel());
                if (currentProperty != null) {
                    errors.add("console.app.message.error.label.exists");
                } else {
                    property.setDateCreated(now);
                    property.setCreatedBy(currUsername);
                    property.setDateModified(now);
                    property.setModifiedBy(currUsername);
                    try {
                        propertyDao.saveOrUpdate(property);
                        applicationProperties.refresh();
                        invalid = false;
                    } catch (Exception e) {
                        invalid = true;
                        errors.add("console.app.message.error.label.exception");
                        e.printStackTrace();
                        LOGGER.error(e.getMessage());
                    }
                }
            } else {
                Property existingProperty = propertyDao.getPropertyById(property.getId());
                existingProperty.setPropertyValue(property.getPropertyValue());
                existingProperty.setDateModified(now);
                existingProperty.setModifiedBy(currUsername);
                try {
                    propertyDao.saveOrUpdate(existingProperty);
                    applicationProperties.refresh();
                    invalid = false;
                } catch (Exception e) {
                    invalid = true;
                    errors.add("console.app.message.error.label.exception");
                    e.printStackTrace();
                    LOGGER.error(e.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                map.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            map.addAttribute("property", property);
            if ("create".equals(action)) {
                return "console/setting/propertyCreate";
            } else {
                return "console/setting/propertyEdit";
            }
        } else {
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/setting/property";
            map.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping("/console/monitor/running")
    public String consoleMonitorRunning(ModelMap map) {
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findLatestVersions(null, null, null, "name", false, null, null);
        map.addAttribute("appDefinitionList", appDefinitionList);
        return "console/monitor/running";
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/json/console/monitor/running/list")
    public void consoleMonitorRunningListJson(Writer writer, @RequestParam(value = "appId", required = false) String appId, @RequestParam(value = "processId", required = false) String processId, @RequestParam(value = "processName", required = false) String processName, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        if ("startedTime".equals(sort)) {
            sort = "Started";
        } else if ("createdTime".equals(sort)) {
            sort = "Created";
        }

        Collection<WorkflowProcess> processList = workflowManager.getRunningProcessList(appId, processId, processName, version, sort, desc, start, rows);
        int count = workflowManager.getRunningProcessSize(appId, processId, processName, version);

        JSONObject jsonObject = new JSONObject();
        for (WorkflowProcess workflowProcess : processList) {
            double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningProcess(workflowProcess.getInstanceId());

            @SuppressWarnings("rawtypes")
            Map data = new HashMap();
            data.put("id", workflowProcess.getInstanceId());
            data.put("name", workflowProcess.getName());
            data.put("state", workflowProcess.getState());
            data.put("version", workflowProcess.getVersion());
            data.put("startedTime", TimeZoneUtil.convertToTimeZone(workflowProcess.getStartedTime(), null, AppUtil.getAppDateFormat()));
            data.put("requesterId", workflowProcess.getRequesterId());
            data.put("due", workflowProcess.getDue() != null ? TimeZoneUtil.convertToTimeZone(workflowProcess.getDue(), null, AppUtil.getAppDateFormat()) : "-");

            data.put("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);
        jsonObject.write(writer);
    }

    @RequestMapping("/console/monitor/running/process/view/(*:id)")
    public String consoleMonitorRunningProcess(ModelMap map, @RequestParam("id") String processId) {
        WorkflowProcess wfProcess = workflowManager.getRunningProcessById(processId);

        double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningProcess(processId);

        map.addAttribute("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

        WorkflowProcess trackWflowProcess = workflowManager.getRunningProcessInfo(processId);
        map.addAttribute("wfProcess", wfProcess);
        map.addAttribute("trackWflowProcess", trackWflowProcess);

        AppDefinition appDef = appService.getAppDefinitionForWorkflowProcess(processId);
        map.addAttribute("appDef", appDef);

        return "console/monitor/runningProcess";
    }

    @RequestMapping(value = "/console/monitor/running/process/abort/(*:id)", method = RequestMethod.POST)
    public String consoleMonitorRunningProcessAbort(ModelMap map, @RequestParam("id") String processId) {
        workflowManager.processAbort(processId);
        return "console/dialogClose";
    }

    @RequestMapping(value = "/console/monitor/running/process/reevaluate/(*:id)", method = RequestMethod.POST)
    public String consoleMonitorRunningProcessReevaluate(ModelMap map, @RequestParam("id") String processId) {
        workflowManager.reevaluateAssignmentsForProcess(processId);
        return "console/dialogClose";
    }

    @RequestMapping("/console/monitor/completed")
    public String consoleMonitorCompleted(ModelMap map) {
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findLatestVersions(null, null, null, "name", false, null, null);
        map.addAttribute("appDefinitionList", appDefinitionList);
        return "console/monitor/completed";
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/json/console/monitor/completed/list")
    public void consoleMonitorCompletedListJson(Writer writer, @RequestParam(value = "appId", required = false) String appId, @RequestParam(value = "processId", required = false) String processId, @RequestParam(value = "processName", required = false) String processName, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        if ("startedTime".equals(sort)) {
            sort = "Started";
        } else if ("createdTime".equals(sort)) {
            sort = "Created";
        }

        Collection<WorkflowProcess> processList = workflowManager.getCompletedProcessList(appId, processId, processName, version, sort, desc, start, rows);
        int count = workflowManager.getCompletedProcessSize(appId, processId, processName, version);

        JSONObject jsonObject = new JSONObject();
        for (WorkflowProcess workflowProcess : processList) {
            double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningProcess(workflowProcess.getInstanceId());

            @SuppressWarnings("rawtypes")
            Map data = new HashMap();
            data.put("id", workflowProcess.getInstanceId());
            data.put("name", workflowProcess.getName());
            data.put("state", workflowProcess.getState());
            data.put("version", workflowProcess.getVersion());
            data.put("startedTime", TimeZoneUtil.convertToTimeZone(workflowProcess.getStartedTime(), null, AppUtil.getAppDateFormat()));
            data.put("requesterId", workflowProcess.getRequesterId());
            data.put("due", workflowProcess.getDue() != null ? TimeZoneUtil.convertToTimeZone(workflowProcess.getDue(), null, AppUtil.getAppDateFormat()) : "-");
            data.put("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);
        jsonObject.write(writer);
    }

    @RequestMapping("/console/monitor/completed/process/view/(*:id)")
    public String consoleMonitorCompletedProcess(ModelMap map, @RequestParam("id") String processId) {
        WorkflowProcess wfProcess = workflowManager.getRunningProcessById(processId);

        double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningProcess(processId);

        map.addAttribute("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

        WorkflowProcess trackWflowProcess = workflowManager.getRunningProcessInfo(processId);
        map.addAttribute("wfProcess", wfProcess);
        map.addAttribute("trackWflowProcess", trackWflowProcess);

        AppDefinition appDef = appService.getAppDefinitionForWorkflowProcess(processId);
        map.addAttribute("appDef", appDef);

        return "console/monitor/completedProcess";
    }

    @RequestMapping(value = "/console/monitor/process/delete", method = RequestMethod.POST)
    public String consoleMonitorProcessDelete(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            workflowManager.removeProcessInstance(id);
        }
        return "console/dialogClose";
    }

    @RequestMapping("/console/monitor/process/viewGraph/(*:id)")
    public String consoleMonitorProcessViewGraph(ModelMap map, @RequestParam("id") String processId) {
        return consoleMonitorProcessGraph(map, processId, true);
    }

    @RequestMapping("/console/monitor/process/graph/(*:id)")
    public String consoleMonitorProcessGraph(ModelMap map, @RequestParam("id") String processId, Boolean useOldViewer) {
        // get process info
        WorkflowProcess wfProcess = workflowManager.getRunningProcessById(processId);

        // get process xpdl
        byte[] xpdlBytes = workflowManager.getPackageContent(wfProcess.getPackageId(), wfProcess.getVersion());
        if (xpdlBytes != null) {
            String xpdl = null;

            try {
                xpdl = new String(xpdlBytes, "UTF-8");
            } catch (Exception e) {
                LogUtil.debug(ConsoleWebController.class.getName(), "XPDL cannot load");
            }
            // get running activities
            Collection<String> runningActivityIdList = new ArrayList<String>();
            List<WorkflowActivity> activityList = (List<WorkflowActivity>) workflowManager.getActivityList(processId, 0, -1, "id", false);
            for (WorkflowActivity wa : activityList) {
                if (wa.getState().indexOf("open") >= 0) {
                    runningActivityIdList.add(wa.getActivityDefId());
                }
            }
            String[] runningActivityIds = (String[]) runningActivityIdList.toArray(new String[0]);

            map.addAttribute("wfProcess", wfProcess);
            map.addAttribute("xpdl", xpdl);
            map.addAttribute("runningActivityIds", runningActivityIds);
        }

        String viewer = (useOldViewer == null || !useOldViewer) ? "pbuilder/pviewer" : "console/monitor/processGraph";
        return viewer;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/json/console/monitor/activity/list")
    public void activityList(Writer writer, @RequestParam(value = "processId", required = false) String processId, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException {

        List<WorkflowActivity> activityList = (List<WorkflowActivity>) workflowManager.getActivityList(processId, start, rows, sort, desc);

        Integer total = workflowManager.getActivitySize(processId);
        JSONObject jsonObject = new JSONObject();
        for (WorkflowActivity workflowActivity : activityList) {
            double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningActivity(workflowActivity.getId());
            @SuppressWarnings("rawtypes")
            Map data = new HashMap();
            data.put("id", workflowActivity.getId());
            data.put("name", workflowActivity.getName());
            data.put("state", workflowActivity.getState());
            data.put("dateCreated", TimeZoneUtil.convertToTimeZone(workflowActivity.getCreatedTime(), null, AppUtil.getAppDateFormat()));
            data.put("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", total);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);
        jsonObject.write(writer);
    }

    @RequestMapping(value = "/console/monitor/(*:processStatus)/process/activity/view/(*:id)")
    public String consoleMonitorActivityView(ModelMap map, @RequestParam("processStatus") String processStatus, @RequestParam("id") String activityId) {
        WorkflowActivity wflowActivity = workflowManager.getActivityById(activityId);
        Collection<WorkflowVariable> variableList = workflowManager.getActivityVariableList(activityId);
        double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningActivity(activityId);
        WorkflowActivity trackWflowActivity = workflowManager.getRunningActivityInfo(activityId);

        map.addAttribute("activity", wflowActivity);
        map.addAttribute("variableList", variableList);
        map.addAttribute("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

        if (trackWflowActivity != null) {
            map.addAttribute("trackWflowActivity", trackWflowActivity);
            String[] assignmentUsers = trackWflowActivity.getAssignmentUsers();
            if (assignmentUsers != null && assignmentUsers.length > 0) {
                map.addAttribute("assignUserSize", assignmentUsers.length);
            }
        }

        //TODO - Show Activity Form
        //get form
//        String processDefId = wflowActivity.getProcessDefId();
//        String version = processDefId.split("#")[1];
//        Collection<ActivityForm> formList = activityFormDao.getFormByActivity(wflowActivity.getProcessDefId(), Integer.parseInt(version), wflowActivity.getActivityDefId());
//
//        if (formList != null && !formList.isEmpty()) {
//            ActivityForm form = formList.iterator().next();
//
//            if (form.getType().equals(ActivityFormDao.ACTIVITY_FORM_TYPE_SINGLE)) {
//                map.addAttribute("version", version);
//                map.addAttribute("formId", form.getFormId());
//            }
//        }
        map.addAttribute("processStatus", processStatus);
        return "console/monitor/activity";
    }

    @RequestMapping("/console/monitor/running/activity/reassign")
    public String consoleMonitorActivityReassign(ModelMap map, @RequestParam("state") String state, @RequestParam("processDefId") String processDefId, @RequestParam("activityId") String activityId, @RequestParam("processId") String processId) {
        map.addAttribute("activityId", activityId);
        map.addAttribute("processId", processId);
        map.addAttribute("state", state);
        map.addAttribute("processDefId", processDefId);
        Collection<Organization> organizations = null;
        if (DirectoryUtil.isExtDirectoryManager()) {
            organizations = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);
        }
        map.addAttribute("organizations", organizations);

        WorkflowActivity trackWflowActivity = workflowManager.getRunningActivityInfo(activityId);
        map.addAttribute("trackWflowActivity", trackWflowActivity);

        return "console/monitor/activityReassign";
    }

    @RequestMapping("/console/monitor/sla")
    public String consoleMonitorSla(ModelMap map) {
        map.addAttribute("processDefinitionList", workflowManager.getProcessList("name", false, 0, -1, null, true, false));
        return "console/monitor/sla";
    }

    @RequestMapping("/console/monitor/audit")
    public String consoleMonitorAuditTrail(ModelMap map) {
        return "console/monitor/auditTrail";
    }

    @RequestMapping("/console/monitor/logs")
    public String consoleMonitorLogs(ModelMap map) {
        return "console/monitor/logs";
    }

    @RequestMapping("/console/monitor/log/(*:fileName)")
    public void consoleMonitorLogs(HttpServletResponse response, @RequestParam("fileName") String fileName) throws IOException {
        if (HostManager.isVirtualHostEnabled()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        ServletOutputStream stream = response.getOutputStream();

        String decodedFileName = fileName;
        try {
            decodedFileName = URLDecoder.decode(fileName, "UTF8");
        } catch (UnsupportedEncodingException e) {
            // ignore
        }

        File file = LogUtil.getTomcatLogFile(decodedFileName);
        if (file == null || file.isDirectory() || !file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        byte[] bbuf = new byte[65536];

        try {
            // set attachment filename
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(decodedFileName, "UTF8"));

            // send output
            int length = 0;
            while ((in != null) && ((length = in.read(bbuf)) != -1)) {
                stream.write(bbuf, 0, length);
            }
        } finally {
            in.close();
            stream.flush();
            stream.close();
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/json/console/monitor/logs/list")
    public void consoleMonitorLogsJson(HttpServletResponse response, Writer writer, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {
        if (HostManager.isVirtualHostEnabled()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        JSONObject jsonObject = new JSONObject();
        File[] files = LogUtil.tomcatLogFiles();
        Collection<File> fileList = new ArrayList<File>();

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    String lowercaseFN = file.getName().toLowerCase();
                    Date lastModified = new Date(file.lastModified());
                    Date current = new Date();

                    if ("catalina.out".equals(lowercaseFN) || (lowercaseFN.indexOf(".log") > 0 && !lowercaseFN.startsWith("admin") && !lowercaseFN.startsWith("host-manager") && !lowercaseFN.startsWith("manager"))
                            && (lastModified.getTime() > (current.getTime() - (5 * 1000 * 60 * 60 * 24))) && file.length() > 0) {
                        fileList.add(file);
                    }
                }
            }
        }
        files = fileList.toArray(new File[0]);

        Arrays.sort(files, NameFileComparator.NAME_COMPARATOR);

        for (File file : files) {
            @SuppressWarnings("rawtypes")
            Map data = new HashMap();
            data.put("filename", file.getName());
            data.put("filesize", file.length());
            data.put("date", TimeZoneUtil.convertToTimeZone(new Date(file.lastModified()), null, AppUtil.getAppDateFormat()));

            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", fileList.size());
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);
        jsonObject.write(writer);
    }

    @RequestMapping("/console/monitor/scheduler")
    public String consoleMonitorScheduler(ModelMap map) {
        return "console/monitor/scheduler";
    }

    @RequestMapping("/console/monitor/scheduler/view/(*:id)")
    public String consoleMonitorSchedulerView(ModelMap map, @RequestParam("id") String id) {
        SchedulerLog schedulerLog = schedulerLogDao.getSchedulerLogById(id);
        map.addAttribute("schedulerLog", schedulerLog);
        return "console/monitor/schedulerView";
    }

    @RequestMapping("/json/console/monitor/scheduler/list")
    public void consoleMonitorSchedulerListJson(Writer writer, @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "jobName", required = false) String jobName, @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

        String condition = "";
        List<String> param = new ArrayList<String>();

        if (jobName != null && jobName.trim().length() != 0) {
            if (!condition.isEmpty()) {
                condition += " and";
            }
            condition += " (e.jobName like ? )";
            param.add("%" + jobName + "%");
        }

        if (condition.length() > 0) {
            condition = "WHERE " + condition;
        }

        List<SchedulerLog> logs = schedulerLogDao.getSchedulerLogs(condition, param.toArray(new String[param.size()]),
                sort, desc, start, rows);

        Long count = schedulerLogDao.count(condition, param.toArray(new String[param.size()]));

        JSONObject jsonObject = new JSONObject();
        if (logs != null && logs.size() > 0) {
            for (SchedulerLog log : logs) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("id", log.getId());
                data.put("jobName", log.getJobName());
                data.put("jobClassName", log.getJobClassName());
                data.put("finishTime", log.getFinishTime());
                data.put("jobStatus", log.getJobStatus().name());
                data.put("message", log.getMessage());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/console/i18n/(*:name)")
    public String consoleI18n(ModelMap map, HttpServletResponse response, @RequestParam("name") String name) throws IOException {
        Properties keys = new Properties();

        //get message key from property file
        InputStream inputStream = null;
        try {
            inputStream = this.getClass().getClassLoader().getResourceAsStream(name + ".properties");
            if (inputStream != null) {
                keys.load(inputStream);
                map.addAttribute("name", name);
                map.addAttribute("keys", keys.keys());

                return "console/i18n/lang";
            } else {
                return "error404";
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/builder/navigator/(*:builder)/(*:id)")
    public String consoleBuilderNavigator(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "builder") String builder, @RequestParam(value = "id", required = false) String id) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<FormDefinition> formDefinitionList = null;
        Collection<DatalistDefinition> datalistDefinitionList = null;
        Collection<UserviewDefinition> userviewDefinitionList = null;

        if (appDef != null) {
            formDefinitionList = formDefinitionDao.getFormDefinitionList(null, appDef, "name", false, null, null);
            datalistDefinitionList = datalistDefinitionDao.getDatalistDefinitionList(null, appDef, "name", false, null, null);
            userviewDefinitionList = userviewDefinitionDao.getUserviewDefinitionList(null, appDef, "name", false, null, null);
        }

        map.addAttribute("builder", builder);
        map.addAttribute("id", id);
        map.addAttribute("appDef", appDef);
        map.addAttribute("formDefinitionList", formDefinitionList);
        map.addAttribute("datalistDefinitionList", datalistDefinitionList);
        map.addAttribute("userviewDefinitionList", userviewDefinitionList);

        return "console/apps/builderItems";
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> getPluginType() {
        Map<String, String> pluginTypeMap = new ListOrderedMap();
        pluginTypeMap.put("org.joget.plugin.base.AuditTrailPlugin", ResourceBundleUtil.getMessage("setting.plugin.auditTrail"));
        pluginTypeMap.put("org.joget.apps.datalist.model.DataListAction", ResourceBundleUtil.getMessage("setting.plugin.datalistAction"));
        pluginTypeMap.put("org.joget.apps.datalist.model.DataListBinder", ResourceBundleUtil.getMessage("setting.plugin.datalistBinder"));
        pluginTypeMap.put("org.joget.apps.datalist.model.DataListColumnFormat", ResourceBundleUtil.getMessage("setting.plugin.datalistColumnFormatter"));
        pluginTypeMap.put("org.joget.apps.datalist.model.DataListFilterType", ResourceBundleUtil.getMessage("setting.plugin.datalistFilterType"));
        pluginTypeMap.put("org.joget.workflow.model.DeadlinePlugin", ResourceBundleUtil.getMessage("setting.plugin.deadline"));
        pluginTypeMap.put("org.joget.directory.model.service.DirectoryManagerPlugin", ResourceBundleUtil.getMessage("setting.plugin.directoryManager"));
        pluginTypeMap.put("org.joget.apps.form.model.Element", ResourceBundleUtil.getMessage("setting.plugin.formElement"));
        pluginTypeMap.put("org.joget.apps.form.model.FormLoadElementBinder", ResourceBundleUtil.getMessage("setting.plugin.formLoadBinder"));
        pluginTypeMap.put("org.joget.apps.form.model.FormLoadOptionsBinder", ResourceBundleUtil.getMessage("setting.plugin.formOptionsBinder"));
        pluginTypeMap.put("org.joget.apps.form.model.FormStoreBinder", ResourceBundleUtil.getMessage("setting.plugin.formStoreBinder"));
        pluginTypeMap.put("org.joget.apps.form.model.FormPermission", ResourceBundleUtil.getMessage("setting.plugin.formPermission"));
        pluginTypeMap.put("org.joget.apps.form.model.Validator", ResourceBundleUtil.getMessage("setting.plugin.formValidator"));
        pluginTypeMap.put("org.joget.apps.generator.model.GeneratorPlugin", ResourceBundleUtil.getMessage("setting.plugin.generator"));
        pluginTypeMap.put("org.joget.apps.app.model.HashVariablePlugin", ResourceBundleUtil.getMessage("setting.plugin.hashVariable"));
        pluginTypeMap.put("org.joget.workflow.model.ParticipantPlugin", ResourceBundleUtil.getMessage("setting.plugin.processParticipant"));
        pluginTypeMap.put("org.joget.plugin.base.ApplicationPlugin", ResourceBundleUtil.getMessage("setting.plugin.processTool"));
        pluginTypeMap.put("org.joget.apps.userview.model.UserviewMenu", ResourceBundleUtil.getMessage("setting.plugin.userviewMenu"));
        pluginTypeMap.put("org.joget.apps.userview.model.UserviewPermission", ResourceBundleUtil.getMessage("setting.plugin.userviewPermission"));
        pluginTypeMap.put("org.joget.apps.userview.model.UserviewTheme", ResourceBundleUtil.getMessage("setting.plugin.userviewTheme"));
        pluginTypeMap.put("org.joget.plugin.base.PluginWebSupport", ResourceBundleUtil.getMessage("setting.plugin.webService"));

        return PagingUtils.sortMapByValue(pluginTypeMap, false);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> getPluginTypeForDefaultProperty() {
        Map<String, String> pluginTypeMap = new ListOrderedMap();
        pluginTypeMap.put("org.joget.plugin.base.AuditTrailPlugin", ResourceBundleUtil.getMessage("setting.plugin.auditTrail"));
        pluginTypeMap.put("org.joget.workflow.model.DeadlinePlugin", ResourceBundleUtil.getMessage("setting.plugin.deadline"));
        pluginTypeMap.put("org.joget.workflow.model.ParticipantPlugin", ResourceBundleUtil.getMessage("setting.plugin.processParticipant"));
        pluginTypeMap.put("org.joget.plugin.base.ApplicationPlugin", ResourceBundleUtil.getMessage("setting.plugin.processTool"));

        return PagingUtils.sortMapByValue(pluginTypeMap, false);
    }

    protected String[] getSortedLocalList() {
        Locale[] localeList = Locale.getAvailableLocales();
        String[] localeStringList = new String[localeList.length];
        for (int i = 0; i < localeList.length; i++) {
            localeStringList[i] = localeList[i].toString();
        }
        Arrays.sort(localeStringList);

        return localeStringList;
    }

    protected String checkVersionExist(ModelMap map, String appId, String version) {
        ConsoleWebPlugin consoleWebPlugin = (ConsoleWebPlugin) pluginManager.getPlugin(ConsoleWebPlugin.class.getName());

        // get app info
        String appInfo = consoleWebPlugin.getAppInfo(appId, version);
        map.put("appInfo", appInfo);

        // verify app license
        String page = consoleWebPlugin.verifyAppVersion(appId, version);
        //LogUtil.debug(getClass().getName(), "App info: " + consoleWebPlugin.getAppInfo(appId, version));
        return page;
    }

    protected Collection<String> validateEmploymentDate(String employeeStartDate, String employeeEndDate) {
        Collection<String> errors = new ArrayList<String>();
        String format = "yyyy-MM-dd";

        //validate start date and end date
        if (!DateUtil.validateDateFormat(employeeStartDate, format)) {
            errors.add(ResourceBundleUtil.getMessage("console.directory.employment.error.startDate.invalid"));
        }

        if (!DateUtil.validateDateFormat(employeeEndDate, format)) {
            errors.add(ResourceBundleUtil.getMessage("console.directory.employment.error.endDate.invalid"));
        }

        if (!DateUtil.compare(employeeStartDate, employeeEndDate, format)) {
            errors.add(ResourceBundleUtil.getMessage("console.directory.employment.error.startdate.endDate.compare"));
        }

        return errors;
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/userview/(*:userviewId)/screenshot/submit", method = RequestMethod.POST)
    public void consoleUserviewScreenshotSubmit(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "userviewId") String userviewId) throws IOException {

        // check to ensure that userview is published
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (!appDef.isPublished()) {
            return;
        }

        // get base64 encoded image in POST body
        String imageBase64 = request.getParameter("base64data");
        imageBase64 = imageBase64.substring("data:image/png;base64,".length());

        // convert into bytes
        byte[] decodedBytes = Base64.decodeBase64(imageBase64.getBytes());

        // save into image file
        String appVersion = (version != null && !version.isEmpty()) ? appDef.getVersion().toString() : "";
        String filename = appDef.getId() + "_" + appVersion + "_" + userviewId + ".png";

        String basePath = SetupManager.getBaseDirectory();
        String dataFileBasePath = SetupManager.getSettingValue("dataFileBasePath");
        if (dataFileBasePath != null && dataFileBasePath.length() > 0) {
            basePath = dataFileBasePath;
        }
        String path = basePath + "app_screenshots";
        new File(path).mkdirs();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedBytes));
        File f = new File(path, filename);
        ImageIO.write(image, "png", f);

        LogUtil.debug(getClass().getName(), "Created screenshot for userview " + userviewId + " in " + appId);
    }

    @RequestMapping(value = "/userview/screenshot/(*:appId)/(*:userviewId)")
    public void consoleUserviewScreenshot(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "userviewId") String userviewId) throws IOException {
        version = (version != null) ? version : "";
        String filename = appId + "_" + version + "_" + userviewId + ".png";

        String basePath = SetupManager.getBaseDirectory();
        String dataFileBasePath = SetupManager.getSettingValue("dataFileBasePath");
        if (dataFileBasePath != null && dataFileBasePath.length() > 0) {
            basePath = dataFileBasePath;
        }
        String path = basePath + "app_screenshots";

        InputStream imageInput;
        File f = new File(path, filename);
        if (!f.exists()) {
            String defaultImage = "images/sampleapp.png";
            imageInput = getClass().getClassLoader().getResourceAsStream(defaultImage);
        } else {
            imageInput = new FileInputStream(f);
        }

        response.setContentType("image/png");
        OutputStream out = response.getOutputStream();
        byte[] bbuf = new byte[65536];
        DataInputStream in = new DataInputStream(imageInput);
        try {
            int length = 0;
            while ((in != null) && ((length = in.read(bbuf)) != -1)) {
                out.write(bbuf, 0, length);
            }
        } finally {
            in.close();
            out.flush();
            out.close();
        }
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/navigator")
    public String consoleAppNavigator(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<FormDefinition> formDefinitionList = null;
        Collection<DatalistDefinition> datalistDefinitionList = null;
        Collection<UserviewDefinition> userviewDefinitionList = null;

        if (appDef != null) {
            formDefinitionList = formDefinitionDao.getFormDefinitionList(null, appDef, "name", false, null, null);
            datalistDefinitionList = datalistDefinitionDao.getDatalistDefinitionList(null, appDef, "name", false, null, null);
            userviewDefinitionList = userviewDefinitionDao.getUserviewDefinitionList(null, appDef, "name", false, null, null);
        }

        map.addAttribute("appDef", appDef);
        map.addAttribute("formDefinitionList", formDefinitionList);
        map.addAttribute("datalistDefinitionList", datalistDefinitionList);
        map.addAttribute("userviewDefinitionList", userviewDefinitionList);

        return "console/apps/navigator";
    }

    @RequestMapping({"/desktop", "/desktop/home"})
    public String desktopHome() {
        return "desktop/home";
    }

    @RequestMapping("/desktop/apps")
    public String desktopApps(ModelMap model) {
        // get published apps
        Collection<AppDefinition> publishedList = appDefinitionDao.findPublishedApps("name", Boolean.FALSE, null, null);

        // get app def ids of published apps
        Collection<String> publishedIdSet = new HashSet<String>();
        for (AppDefinition appDef : publishedList) {
            publishedIdSet.add(appDef.getAppId());
        }

        // get list of unpublished apps
        Collection<AppDefinition> unpublishedList = new ArrayList<AppDefinition>();
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findLatestVersions(null, null, null, "name", Boolean.FALSE, null, null);
        for (Iterator<AppDefinition> i = appDefinitionList.iterator(); i.hasNext();) {
            AppDefinition appDef = i.next();
            if (!publishedIdSet.contains(appDef.getAppId())) {
                unpublishedList.add(appDef);
            }
        }
        model.addAttribute("appDefinitionList", appDefinitionList);
        model.addAttribute("appPublishedList", publishedList);
        model.addAttribute("appUnpublishedList", unpublishedList);
        return "desktop/apps";
    }

    @RequestMapping("/desktop/app/import")
    public String desktopAppImport() {
        return "desktop/apps/import";
    }

    @RequestMapping(value = "/desktop/app/import/submit", method = RequestMethod.POST)
    public String desktopAppImportSubmit(ModelMap map) throws IOException {
        Collection<String> errors = new ArrayList<String>();

        MultipartFile appZip = null;

        try {
            appZip = FileStore.getFile("appZip");
        } catch (FileLimitException e) {
            errors.add(ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()}));
        }

        AppDefinition appDef = null;
        if (appZip != null) {
            appDef = appService.importApp(appZip.getBytes());
        }

        if (appDef == null || !errors.isEmpty()) {
            map.addAttribute("error", true);
            map.addAttribute("errorList", errors);
            return "desktop/apps/import";
        } else {
            String appId = appDef.getAppId();
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/app/" + appId + "/forms";
            map.addAttribute("url", url);
            map.addAttribute("appId", appId);
            map.addAttribute("appVersion", appDef.getVersion());
            map.addAttribute("isPublished", appDef.isPublished());
            return "desktop/apps/packageUploadSuccess";
        }
    }

    @RequestMapping({"/desktop/marketplace/app"})
    public String marketplaceApp() {
        return "desktop/marketplaceApp";
    }

}
