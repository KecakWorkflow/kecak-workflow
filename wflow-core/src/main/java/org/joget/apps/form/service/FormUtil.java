package org.joget.apps.form.service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.MobileElement;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.MobileUtil;
import org.joget.apps.datalist.lib.FormRowDataListBinder;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.form.lib.FormOptionsBinder;
import org.joget.apps.form.model.AbstractSubForm;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormAction;
import org.joget.apps.form.model.FormAjaxOptionsBinder;
import org.joget.apps.form.model.FormAjaxOptionsElement;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormLoadBinder;
import org.joget.apps.form.model.FormLoadOptionsBinder;
import org.joget.apps.form.model.FormReferenceDataRetriever;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.model.FormStoreBinder;
import org.joget.apps.form.model.Validator;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.ApplicationPlugin;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility methods for the Form module.
 */
@Service("appsFormUtil")
public class FormUtil implements ApplicationContextAware {

    public static final String PROPERTY_ELEMENT_UNIQUE_KEY = "elementUniqueKey";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_FORM_DEF_ID = "formdefid";
    public static final String PROPERTY_VALUE = "value";
    public static final String PROPERTY_LABEL = "label";
    public static final String PROPERTY_OPTIONS = "options";
    public static final String PROPERTY_SELECTED = "selected";
    public static final String PROPERTY_GROUPING = "grouping";
    public static final String PROPERTY_OPTIONS_DELIMITER = ";";
    public static final String PROPERTY_CLASS_NAME = "className";
    public static final String PROPERTY_ELEMENTS = "elements";
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_VALIDATOR = "validator";
    public static final String PROPERTY_READONLY = "readonly";
    public static final String PROPERTY_READONLY_LABEL = "readonlyLabel";
    public static final String PROPERTY_DATE_CREATED = "dateCreated";
    public static final String PROPERTY_DATE_MODIFIED = "dateModified";
    public static final String PROPERTY_CREATED_BY = "createdBy";
    public static final String PROPERTY_MODIFIED_BY = "modifiedBy";
    public static final String PROPERTY_DELETED = "deleted";
    public static final String PROPERTY_CUSTOM_PROPERTIES = "customProperties";
    public static final String PROPERTY_TABLE_NAME = "tableName";
    public static final String PROPERTY_TEMP_FILE_PATH = "_tempFilePathMap";
    public static final String PROPERTY_POST_PROCESSOR = "postProcessor";
    public static final String PROPERTY_POST_PROCESSOR_RUN_ON = "postProcessorRunOn";
    public static final String FORM_META_ORIGINAL_ID = "_FORM_META_ORIGINAL_ID";
    public static final String FORM_BUILDER_ACTIVE = "formBuilderActive";
    public static final String FORM_ERRORS_PARAM = "_FORM_ERRORS";
    public static final String FORM_RESULT_LOAD_ALL_DATA = "FORM_RESULT_LOAD_ALL_DATA";
    
    static ApplicationContext appContext;
    
    @SuppressWarnings("rawtypes")
	public static ThreadLocal processedFormJson = new ThreadLocal(); 
    
    public static Long runningNumber = 0L;

    /**
     * Method used for system to set ApplicationContext
     * @param ac
     * @throws BeansException 
     */
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        appContext = ac;
    }

    /**
     * Utility method to retrieve the ApplicationContext of the system
     * @return 
     */
    public static ApplicationContext getApplicationContext() {
        return appContext;
    }

    /**
     * Parses form field element from the element json string
     * @param json
     * @return
     * @throws Exception 
     */
    public static Element parseElementFromJson(String json) throws Exception {
        // create json object
        JSONObject obj = new JSONObject(json);

        // parse json object
        Element element = FormUtil.parseElementFromJsonObject(obj, null);
        return element;
    }

    /**
     * Finds and parses the form field element from form json by field id
     * @param json
     * @param fieldId
     * @return 
     */
    public static Element findAndParseElement(String json, String fieldId) {
        if (json != null && !json.isEmpty() && fieldId != null && !fieldId.isEmpty()) {
            try {
                JSONObject obj = new JSONObject(json);
                return FormUtil.findAndParseElementFromJsonObject(obj, fieldId);
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * Finds and parses the form field element from form json object by field id
     * @param obj
     * @param fieldId
     * @return
     * @throws Exception 
     */
    public static Element findAndParseElementFromJsonObject(JSONObject obj, String fieldId) throws Exception {
        if (obj != null && !obj.isNull(FormUtil.PROPERTY_PROPERTIES)) {
            JSONObject objProperty = obj.getJSONObject(FormUtil.PROPERTY_PROPERTIES);
            if (objProperty.has(FormUtil.PROPERTY_ID) && fieldId.equals((String) objProperty.get(FormUtil.PROPERTY_ID))) {
                return FormUtil.parseElementFromJsonObject(obj, null);
            } else if (!obj.isNull(FormUtil.PROPERTY_ELEMENTS)) {
                JSONArray elements = obj.getJSONArray(FormUtil.PROPERTY_ELEMENTS);
                if (elements != null && elements.length() > 0) {
                    for (int i = 0; i < elements.length(); i++) {
                        JSONObject childObj = (JSONObject) elements.get(i);

                        // create child element
                        Element child = FormUtil.findAndParseElementFromJsonObject(childObj, fieldId);
                        if (child != null) {
                            return child;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Parses form field element from the element json object
     * @param obj
     * @param parent
     * @return
     * @throws Exception 
     */
    public static Element parseElementFromJsonObject(JSONObject obj, Element parent) throws Exception {
        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        // instantiate element
        String className = obj.getString(FormUtil.PROPERTY_CLASS_NAME);
        Element element = (Element) pluginManager.getPlugin(className);
        if (element != null) {
            // check for mobile support
            boolean isMobileView = MobileUtil.isMobileView();
            if (isMobileView && (element instanceof MobileElement) && !((MobileElement)element).isMobileSupported()) {
                // mobile not supported, ignore this element
                return null;
            }
            
            // set element properties
            Map<String, Object> properties = FormUtil.parsePropertyFromJsonObject(obj);
            element.setProperties(properties);
            element.setProperty(FormUtil.PROPERTY_ELEMENT_UNIQUE_KEY, FormUtil.getUniqueKey());

            if (parent != null) {
                element.setParent(parent);
                // recurse into child elements
                Collection<Element> childElements = parent.getChildren();
                if (childElements == null) {
                    childElements = new ArrayList<Element>();
                    parent.setChildren(childElements);
                }
                childElements.add(element);
            }
            
            // recurse into child elements
            FormUtil.parseChildElementsFromJsonObject(obj, element);

            // set binders and properties
            FormBinder loadBinder = (FormBinder) FormUtil.parseBinderFromJsonObject(obj, element, FormBinder.FORM_LOAD_BINDER);
            element.setLoadBinder((FormLoadBinder) loadBinder);
            FormBinder optionsBinder = (FormBinder) FormUtil.parseBinderFromJsonObject(obj, element, FormBinder.FORM_OPTIONS_BINDER);
            element.setOptionsBinder((FormLoadBinder) optionsBinder);
            FormBinder storeBinder = (FormBinder) FormUtil.parseBinderFromJsonObject(obj, element, FormBinder.FORM_STORE_BINDER);
            element.setStoreBinder((FormStoreBinder) storeBinder);

            // set validator
            Validator validator = FormUtil.parseValidatorFromJsonObject(obj);
            if (validator != null) {
                validator.setElement(element);
                element.setValidator(validator);
            }
        }

        return element;
    }

    /**
     * Parses the properties attribute from a JSON object into a Map
     * @param obj
     * @return
     * @throws JSONException
     */
    @SuppressWarnings("unchecked")
	public static Map<String, Object> parsePropertyFromJsonObject(JSONObject obj) throws JSONException {
        Map<String, Object> property = new HashMap<String, Object>();

        if (!obj.isNull(FormUtil.PROPERTY_PROPERTIES)) {
            JSONObject objProperty = obj.getJSONObject(FormUtil.PROPERTY_PROPERTIES);
            property = PropertyUtil.getProperties(objProperty);

            if (property.containsKey(FormUtil.PROPERTY_OPTIONS)) {
                FormRowSet options = new FormRowSet();
                Object[] objs = (Object[]) property.get(FormUtil.PROPERTY_OPTIONS);

                for (Object o : objs) {
                    @SuppressWarnings("rawtypes")
					Map temp = (HashMap) o;
                    FormRow option = new FormRow();
                    for (String key : (Set<String>) temp.keySet()) {
                        option.setProperty(key, (String) temp.get(key));
                    }
                    options.add(option);
                }
                property.put(FormUtil.PROPERTY_OPTIONS, options);
            }
        }

        return property;
    }

    /**
     * Parse child elements from element json object
     * @param obj
     * @param parent
     * @throws Exception
     */
    public static void parseChildElementsFromJsonObject(JSONObject obj, Element parent) throws Exception {
        if (!obj.isNull(FormUtil.PROPERTY_ELEMENTS)) {
            JSONArray elements = obj.getJSONArray(FormUtil.PROPERTY_ELEMENTS);
            if (elements != null && elements.length() > 0) {
                for (int i = 0; i < elements.length(); i++) {
                    JSONObject childObj = (JSONObject) elements.get(i);

                    // create child element
                    @SuppressWarnings("unused")
					Element childElement = FormUtil.parseElementFromJsonObject(childObj, parent);
                }
            }
        }
    }

    /**
     * Parse binder object from element json object
     *
     * @param obj
     * @param element
     * @param binderType The JSON property for the binder.
     * @return
     * @throws Exception
     */
    public static FormBinder parseBinderFromJsonObject(JSONObject obj, Element element, String binderType) throws Exception {
        FormBinder binder = null;
        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        if (!obj.isNull(FormUtil.PROPERTY_PROPERTIES)) {
            JSONObject objProperty = obj.getJSONObject(FormUtil.PROPERTY_PROPERTIES);
            if (!objProperty.isNull(binderType)) {
                JSONObject binderObj = objProperty.getJSONObject(binderType);

                // create binder object
                if (!binderObj.isNull(FormUtil.PROPERTY_CLASS_NAME)) {
                    String className = binderObj.getString(FormUtil.PROPERTY_CLASS_NAME);
                    if (className != null && className.trim().length() > 0) {
                        binder = (FormBinder) pluginManager.getPlugin(className);
                        if (binder != null) {
                            // set child properties
                            Map<String, Object> properties = FormUtil.parsePropertyFromJsonObject(binderObj);
                            binder.setProperties(properties);
                            binder.setElement(element);
                        }
                    }
                }
            }
        }
        return binder;
    }

    /**
     * Parse validator object from element json object
     * @param obj
     * @return
     * @throws Exception
     */
    public static Validator parseValidatorFromJsonObject(JSONObject obj) throws Exception {
        Validator validator = null;
        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        if (!obj.isNull(FormUtil.PROPERTY_PROPERTIES)) {
            JSONObject objProperty = obj.getJSONObject(FormUtil.PROPERTY_PROPERTIES);
            if (!objProperty.isNull(FormUtil.PROPERTY_VALIDATOR)) {
                JSONObject validatorObj = objProperty.getJSONObject(FormUtil.PROPERTY_VALIDATOR);

                // create validator object
                if (!validatorObj.isNull(FormUtil.PROPERTY_CLASS_NAME)) {
                    String className = validatorObj.getString(FormUtil.PROPERTY_CLASS_NAME);
                    if (className != null && className.trim().length() > 0) {
                        validator = (Validator) pluginManager.getPlugin(className);
                        if (validator != null) {
                            // set child properties
                            Map<String, Object> properties = FormUtil.parsePropertyFromJsonObject(validatorObj);
                            validator.setProperties(properties);
                        }
                    }
                }
            }
        }
        return validator;
    }

    /**
     * Utility method to recursively find and invoke option binders starting from an element.
     * @param element
     * @param formData
     * @return
     */
    public static FormData executeOptionBinders(Element element, FormData formData) {
        if (formData == null) {
            formData = new FormData();
        }
        FormLoadBinder binder = (FormLoadBinder) element.getOptionsBinder();
        if (binder != null && !isAjaxOptionsSupported(element, formData)) {
            String primaryKeyValue = (formData != null) ? element.getPrimaryKeyValue(formData) : null;
            FormRowSet data = binder.load(element, primaryKeyValue, formData);
            if (data != null) {
                formData.setOptionsBinderData(binder, data);
            }
        }
        Collection<Element> children = element.getChildren(formData);
        if (children != null) {
            for (Element child : children) {
                FormUtil.executeOptionBinders(child, formData);
            }
        }
        return formData;
    }

    /**
     * Utility method to recursively traverse and invoke load binders starting from an element.
     * @param element
     * @param formData
     * @return
     */
    public static FormData executeLoadBinders(Element element, FormData formData) {
        if (formData == null) {
            formData = new FormData();
        }
        FormLoadBinder binder = (FormLoadBinder) element.getLoadBinder();
        if (!(element instanceof AbstractSubForm) && binder != null) {
            String primaryKeyValue = (formData != null) ? element.getPrimaryKeyValue(formData) : null;
            FormRowSet data = binder.load(element, primaryKeyValue, formData);
            if (data != null) {
                formData.setLoadBinderData(binder, data);
            }
        }
        Collection<Element> children = element.getChildren(formData);
        if (children != null) {
            for (Element child : children) {
                FormUtil.executeLoadBinders(child, formData);
            }
        }
        return formData;
    }

    /**
     * Utility method to recursively find and invoke validators starting from an element.
     * @param element
     * @param formData
     * @return true if all validators are successful.
     */
    public static boolean executeValidators(Element element, FormData formData) {
        String id = FormUtil.getElementParameterName(element);
        formData.getPreviousFormErrors().remove(id);
                
        boolean result = true;
        if (!FormUtil.isReadonly(element, formData) && element.continueValidation(formData)) {
            Validator validator = (Validator) element.getValidator();
            if (validator != null) {

                String[] values = FormUtil.getElementPropertyValues(element, formData);
                result = validator.validate(element, formData, values) && result;
            }
            result = element.selfValidate(formData) && result;
            
            Collection<Element> children = element.getChildren(formData);
            if (children != null) {
                for (Element child : children) {
                    result = FormUtil.executeValidators(child, formData) && result;
                }
            }
        }
        //add empty error to fail the submission if the result is false and there are no error message exist.  
        if (!result && !element.hasError(formData) && element instanceof Form) {
            formData.addFormError(id, "");
        }
        return result;
    }

    /**
     * Utility method to recursively find and invoke the formatData method starting from an element.
     * @param element
     * @param formData
     * @param binderRowSetMap
     * @return A Map mapping a binder to FormRowSets containing formatted values from all elements.
     */
    public static FormData executeElementFormatData(Element element, FormData formData) {
        // get store binder and rowset for element
        FormStoreBinder binder = FormUtil.findStoreBinder(element);
        if (binder != null) {
            FormRowSet rowSet = formData.getStoreBinderData(binder);
            if (rowSet == null) {
                rowSet = new FormRowSet();
                formData.setStoreBinderData(binder, rowSet);
            }

            // get element formatted data
            FormRowSet elementResult = element.formatData(formData);
            if (elementResult != null) {
                if (!elementResult.isMultiRow()) {
                    // get single row
                    FormRow elementRow = elementResult.get(0);

                    // append to consolidated row set
                    if (rowSet.isEmpty()) {
                        rowSet.add(elementRow);
                    } else {
                        FormRow currentRow = rowSet.get(0);
                        currentRow.putAll(elementRow);
                    }
                } else {
                    //if the store binder of this element is null, store as single row in json format
                    if (element.getStoreBinder() == null) {
                        try {
                            // create json object
                            JSONArray jsonArray = new JSONArray();
                            for (FormRow row : elementResult) {
                                JSONObject jsonObject = new JSONObject();
                                for (@SuppressWarnings("rawtypes") Map.Entry entry : row.entrySet()) {
                                    String key = (String) entry.getKey();
                                    String value = (String) entry.getValue();
                                    jsonObject.put(key, value);
                                }
                                
                                //File upload is not support when no binder is set.
                                jsonArray.put(jsonObject);
                            }

                            // convert into json string
                            String json = jsonArray.toString();

                            // store in single row FormRowSet
                            String id = element.getPropertyString(FormUtil.PROPERTY_ID);
                            FormRow elementRow = new FormRow();
                            elementRow.put(id, json);

                            // append to consolidated row set
                            if (rowSet.isEmpty()) {
                                rowSet.add(elementRow);
                            } else {
                                FormRow currentRow = rowSet.get(0);
                                currentRow.putAll(elementRow);
                            }
                        } catch (JSONException ex) {
                            LogUtil.error(FormUtil.class.getName(), ex, "");
                        }
                    } else {
                        // multiple row result, append all to rowset
                        rowSet.addAll(elementResult);
                        rowSet.setMultiRow(true);
                    }
                }
            }
        }

        if (element.continueValidation(formData)) {
            // recurse into children
            Collection<Element> children = element.getChildren(formData);
            if (children != null) {
                for (Element child : children) {
                    FormUtil.executeElementFormatData(child, formData);
                }
            }
        }
        return formData;
    }

    /**
     * Utility method to recursively find and invoke the formatDataForValidation method starting from an element.
     * @param element
     * @param formData
     * @return the formatted data.
     */
    public static FormData executeElementFormatDataForValidation(Element element, FormData formData) {
        // recurse into children
        Collection<Element> children = element.getChildren(formData);
        if (children != null) {
            for (Element child : children) {
                formData = FormUtil.executeElementFormatDataForValidation(child, formData);
            }
        }
        formData = element.formatDataForValidation(formData);
        return formData;
    }

    /**
     * Utility method to recursively find and invoke actions starting from an element.
     * @param form
     * @param element
     * @param formData
     * @return
     */
    public static FormData executeActions(Form form, Element element, FormData formData) {
        FormData updatedFormData = formData;
        if (element == null) {
            element = form;
        }
        if (element.isAuthorize(formData)) {
            if (element instanceof FormAction) {
                FormAction action = (FormAction) element;
                if (action != null) {
                    boolean isActive = action.isActive(form, formData);
                    if (isActive) {
                        updatedFormData = action.actionPerformed(form, formData);
                    }
                }
            }
            // recurse into children
            Collection<Element> children = element.getChildren(formData);
            if (children != null) {
                for (Element child : children) {
                    updatedFormData = FormUtil.executeActions(form, child, formData);
                }
            }
        }
        return updatedFormData;
    }

    /**
     * Utility method to recursively find the nearest ancestor load binder for an element.
     * @param element
     * @return
     */
    public static FormLoadBinder findLoadBinder(Element element) {
        FormLoadBinder binder = null;
        Element el = element;
        while (el != null && binder == null) {
            binder = el.getLoadBinder();
            if (binder != null) {
                break;
            }
            el = el.getParent();
        }
        return binder;
    }

    /**
     * Utility method to recursively find the nearest ancestor options binder for an element.
     * @param element
     * @return
     */
    public static FormLoadBinder findOptionsBinder(Element element) {
        FormLoadBinder binder = null;
        Element el = element;
        while (el != null && binder == null) {
            binder = el.getOptionsBinder();
            if (binder != null) {
                break;
            }
            el = el.getParent();
        }
        return binder;
    }

    /**
     * Utility method to recursively find the nearest ancestor store binder for an element.
     * @param element
     * @return
     */
    public static FormStoreBinder findStoreBinder(Element element) {
        FormStoreBinder binder = null;
        Element el = element;
        while (el != null && binder == null) {
            binder = el.getStoreBinder();
            if (binder != null) {
                break;
            }
            el = el.getParent();
        }
        return binder;
    }

    /**
     * Utility method to recursively find the parent Form for an element.
     * @param element
     * @return
     */
    public static Form findRootForm(Element element) {
        Form form = null;
        Element el = element;
        while (el != null && form == null) {
            if (el instanceof Form) {
                form = (Form) el;
                break;
            }
            el = el.getParent();
        }
        return form;
    }

    /**
     * Utility method to recursively find an element by ID.
     * @param id
     * @param rootElement
     * @param formData
     * @return
     */
    public static Element findElement(String id, Element rootElement, FormData formData) {
        return findElement(id, rootElement, formData, false);
    }

    /**
     * Utility method to recursively find an element by ID.
     * @param id
     * @param rootElement
     * @param formData
     * @return
     */
    public static Element findElement(String id, Element rootElement, FormData formData, Boolean includeSubForm) {
        if (rootElement == null) {
            return null;
        }
        Element result = null;
        String elementId = rootElement.getPropertyString(FormUtil.PROPERTY_ID);
        if (elementId != null && elementId.equals(id)) {
            if (rootElement instanceof Form) {
                Collection<Element> children = rootElement.getChildren(formData);
                if (children != null) {
                    for (Element child : children) {
                        result = FormUtil.findElement(id, child, formData, includeSubForm);
                        if (result != null) {
                            break;
                        }
                    }
                }
            }
            if (result == null) {
                result = rootElement;
            }
            return result;
        } else if (!(rootElement instanceof AbstractSubForm) || ((rootElement instanceof AbstractSubForm) && includeSubForm)) {
            Collection<Element> children = rootElement.getChildren(formData);
            if (children != null) {
                for (Element child : children) {
                    result = FormUtil.findElement(id, child, formData, includeSubForm);
                    if (result != null) {
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Returns the parameter name for the element.
     * @param element
     * @return
     */
    public static String getElementParameterName(Element element) {
        String paramName = element.getCustomParameterName();
        if (paramName == null || paramName.trim().length() == 0) {
            paramName = element.getPropertyString(FormUtil.PROPERTY_ID);
        }
        return paramName;
    }

    /**
     * Returns the request parameter value for an element
     * @param element
     * @return
     */
    public static String getRequestParameter(Element element, FormData formData) {
        String value = null;
        String paramName = FormUtil.getElementParameterName(element);
        value = formData.getRequestParameter(paramName);
        return value;
    }

    /**
     * Returns the request parameter value for an element
     * @param element
     * @return
     */
    public static String[] getRequestParameterValues(Element element, FormData formData) {
        String[] values = null;
        String paramName = FormUtil.getElementParameterName(element);
        values = formData.getRequestParameterValues(paramName);
        return values;
    }

    /**
     * Retrieves the property value for an element, first from the element's load binder.
     * If no binder is available, the default value is used.
     * Overrides the binder/default value when value from request parameter is available.
     * @param element
     * @param formData
     * @param property
     * @return
     */
    public static String getElementPropertyValue(Element element, FormData formData) {
        // get value
        String id = element.getPropertyString(FormUtil.PROPERTY_ID);
        String value = "";
        if (FormUtil.isReadonly(element, formData) || !FormUtil.isFormSubmitted(element, formData)) {
            value = element.getPropertyString(FormUtil.PROPERTY_VALUE);
        }
        String paramName = FormUtil.getElementParameterName(element);
        
        if (formData != null) { // handle default value from options binder
            FormRowSet rowSet = formData.getOptionsBinderData(element, id);
            if (rowSet != null) {
                
                for (FormRow row : rowSet) {
                    Iterator<String> it = row.stringPropertyNames().iterator();
                    // get the key based on the "value" property
                    String optionValue = row.getProperty(PROPERTY_VALUE);
                    if (optionValue == null) {
                        // no "value" property, use first property instead
                        String key = it.next();
                        optionValue = row.getProperty(key);
                    }
                    
                    if(row.getProperty(PROPERTY_SELECTED) != null && (row.getProperty(PROPERTY_SELECTED).equalsIgnoreCase("true"))){
                        value = optionValue;
                        break;
                    }
                }
            }
        }

        // read from request if available, TODO: handle null values e.g. no options selected in a checkbox
        String paramValue = FormUtil.getRequestParameter(element, formData);
        if (paramValue != null && !FormUtil.isReadonly(element, formData)) {
            value = paramValue;
        } else if (FormUtil.isReadonly(element, formData) && formData != null && formData.getRequestParameter(FormService.PREFIX_FOREIGN_KEY + paramName) != null) {
            value = formData.getRequestParameter(FormService.PREFIX_FOREIGN_KEY + paramName);
        } else {
            // load from binder if available
            if (formData != null) {
                String binderValue = formData.getLoadBinderDataProperty(element, id);
                if (binderValue != null) {
                    value = binderValue;
                }
            }
        }

        return value;
    }

    /**
     * Retrieves the property value for an element, first from the element's load binder.
     * If no binder is available, the default value is used.
     * Overrides the binder/default value when value from request parameter is available.
     * This method supports multiple values for a property.
     * @param element
     * @param formData
     * @param property
     * @param nullToEmpty Set to true to default value to "" when there the request parameter value is null
     * @return
     */
    public static String[] getElementPropertyValues(Element element, FormData formData) {
        List<String> values = new ArrayList<String>();

        // get value
        String id = element.getPropertyString(FormUtil.PROPERTY_ID);
        String value = "";
        if (FormUtil.isReadonly(element, formData) || !FormUtil.isFormSubmitted(element, formData)) {
            value = element.getPropertyString(FormUtil.PROPERTY_VALUE);
        }
        String paramName = FormUtil.getElementParameterName(element);

        // handle multiple values
        if (value != null) {
            StringTokenizer st = new StringTokenizer(value, FormUtil.PROPERTY_OPTIONS_DELIMITER);
            while (st.hasMoreTokens()) {
                String val = st.nextToken();
                values.add(val);
            }
        }
        
        if (formData != null) { // handle default value from options binder
            FormRowSet rowSet = formData.getOptionsBinderData(element, id);
            if (rowSet != null) {
                
                for (FormRow row : rowSet) {
                    Iterator<String> it = row.stringPropertyNames().iterator();
                    // get the key based on the "value" property
                    String optionValue = row.getProperty(PROPERTY_VALUE);
                    if (optionValue == null) {
                        // no "value" property, use first property instead
                        String key = it.next();
                        optionValue = row.getProperty(key);
                    }
                    
                    if(row.getProperty(PROPERTY_SELECTED) != null && (row.getProperty(PROPERTY_SELECTED).equalsIgnoreCase("true"))){
                        values.add(optionValue);
                    }
                }
            }
        }
        
        // read from request if available, TODO: handle null values e.g. checkbox
        if (id != null) {
            String[] paramValues = FormUtil.getRequestParameterValues(element, formData);
            if (paramValues != null && !FormUtil.isReadonly(element, formData)) {
                values = Arrays.asList(paramValues);
            } else if (FormUtil.isReadonly(element, formData) && formData != null && formData.getRequestParameter(FormService.PREFIX_FOREIGN_KEY + paramName) != null) {
                paramValues = formData.getRequestParameterValues(FormService.PREFIX_FOREIGN_KEY + paramName);
                values = Arrays.asList(paramValues);
            } else {
                // load from binder if available
                if (formData != null) {
                    String binderValue = formData.getLoadBinderDataProperty(element, id);
                    if (binderValue != null) {
                        values = new ArrayList<String>();
                        StringTokenizer st = new StringTokenizer(binderValue, FormUtil.PROPERTY_OPTIONS_DELIMITER);
                        while (st.hasMoreTokens()) {
                            String val = st.nextToken();
                            values.add(val);
                        }
                    }
                }
            }
        }

        String[] result = (String[]) values.toArray(new String[0]);
        return result;
    }

    /**
     * Utility methods to check the value of an element is changed
     * @param element
     * @param formData
     * @param updatedValues
     * @return 
     */
    public static boolean isElementPropertyValuesChanges(Element element, FormData formData, String[] updatedValues) {
        // get value
        String id = element.getPropertyString(FormUtil.PROPERTY_ID);
        
        String primaryKeyValue = element.getPrimaryKeyValue(formData);
        String uniqueId = "";
        Form rootForm = findRootForm(element);
        if (rootForm.getParent() != null) {
            uniqueId = rootForm.getCustomParameterName();
        }
        if (primaryKeyValue != null && !primaryKeyValue.equals(formData.getRequestParameter(uniqueId + FormUtil.FORM_META_ORIGINAL_ID))) {
            return true;
        }

        List<String> values = new ArrayList<String>();

        @SuppressWarnings("unused")
		String value = element.getPropertyString(FormUtil.PROPERTY_VALUE);

        // load from binder if available
        if (formData != null) {
            String binderValue = formData.getLoadBinderDataProperty(element, id);
            if (binderValue != null) {
                StringTokenizer st = new StringTokenizer(binderValue, FormUtil.PROPERTY_OPTIONS_DELIMITER);
                while (st.hasMoreTokens()) {
                    String val = st.nextToken();
                    values.add(val);
                }
            }
        }
        String[] loadedValues = (String[]) values.toArray(new String[0]);

        if (loadedValues != null && updatedValues != null && loadedValues.length == updatedValues.length) {
            return !Arrays.equals(loadedValues, updatedValues);
        }

        return true;
    }

    /**
     * Retrieves the property options for an element, first from the element's options binder.
     * If no binder is available, the default options are used.
     * @param element
     * @param formData
     * @return
     */
    @SuppressWarnings("rawtypes")
	public static Collection<Map> getElementPropertyOptionsMap(Element element, FormData formData) {
        Collection<Map> optionsMap = new ArrayList<Map>();

        if (isAjaxOptionsSupported(element, formData)) {
            FormAjaxOptionsElement ajaxElement = (FormAjaxOptionsElement) element;
            
            Element controlElement = ajaxElement.getControlElement(formData);
            String[] controlValues = FormUtil.getElementPropertyValues(controlElement, formData);
            
            FormAjaxOptionsBinder binder = (FormAjaxOptionsBinder) element.getOptionsBinder();
            FormRowSet rowSet = binder.loadAjaxOptions(controlValues);
            
            if (rowSet != null) {
                optionsMap = new ArrayList<Map>();
                for (Map row : rowSet) {
                    optionsMap.add(row);
                }
            }
        } else {
            // load from "options" property
            Object optionProperty = element.getProperty(FormUtil.PROPERTY_OPTIONS);
            if (optionProperty != null && optionProperty instanceof Collection) {
                for (Map opt : (FormRowSet) optionProperty) {
                    optionsMap.add(opt);
                }
            }

            // load from binder if available
            if (formData != null) {
                String id = element.getPropertyString(FormUtil.PROPERTY_ID);
                FormRowSet rowSet = formData.getOptionsBinderData(element, id);
                if (rowSet != null) {
                    optionsMap = new ArrayList<Map>();
                    for (Map row : rowSet) {
                        optionsMap.add(row);
                    }
                }
            }
        }

        return optionsMap;
    }

    /**
     * Generates a delimited string from an array of Strings.
     * @param values
     * @return
     */
    public static String generateElementPropertyValues(String[] values) {
        String result = null;
        if (values != null && values.length > 0) {
            for (String val : values) {
                // TODO: replace delimiter?
                if (result == null) {
                    result = "";
                } else {
                    result += FormUtil.PROPERTY_OPTIONS_DELIMITER;
                }
                result += val;
            }
        }
        return result;
    }

    /**
     * Retrieve the error attached to the element
     * @param element
     * @param formData
     * @return null if there is no error.
     */
    public static String getElementError(Element element, FormData formData) {
        String id = FormUtil.getElementParameterName(element);
        String error = formData.getFormError(id);
        return error;
    }

    /**
     * Retrieve a decoration on an element by any attached validator, e.g. marking a required field.
     * @param element
     * @param formData
     * @return
     */
    public static String getElementValidatorDecoration(Element element, FormData formData) {
        String decoration = "";
        Validator validator = element.getValidator();
        if (validator != null) {
            decoration = validator.getElementDecoration(element, formData);
            if (decoration == null) {
                decoration = "";
            }
        }
        return decoration;
    }

    /**
     * Generates JSON representing an element.
     * @param element
     * @return
     */
    public static String generateElementJson(Element element) throws Exception {
        JSONObject jsonObject = FormUtil.generateElementJsonObject(element);
        String json = jsonObject.toString();
        return json;
    }

    /**
     * Generates JSON representing the properties of an element.
     * @param element
     * @return
     */
    public static String generateElementPropertyJson(Element element) {
        String json = null;
        try {
            Map<String, Object> properties = element.getProperties();
            JSONObject jsonObject = generatePropertyJsonObject(properties);
            json = jsonObject.toString();
        } catch (Exception ex) {
            LogUtil.error(FormUtil.class.getName(), ex, "");
        }
        return json;
    }

    /**
     * Generates a JSONObject to represent the properties of an element
     * @return
     */
    @SuppressWarnings("rawtypes")
	public static JSONObject generatePropertyJsonObject(Map<String, Object> properties) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (properties != null) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String propertyName = entry.getKey();
                Object objValue = entry.getValue();
                if (objValue != null && objValue instanceof FormRowSet) {
                    JSONArray jsonArray = new JSONArray();
                    for (FormRow row : (FormRowSet) objValue) {
                        Set<String> props = row.stringPropertyNames();
                        JSONObject jo = new JSONObject();
                        for (String key : props) {
                            String val = row.getProperty(key);
                            jo.accumulate(key, val);
                        }
                        jsonArray.put(jo);
                    }
                    jsonObject.put(propertyName, jsonArray);
                } else if (objValue != null && objValue instanceof Object[]) { 
                    Object[] mapArray = (Object[]) objValue;
                    JSONArray jsonArray = new JSONArray();
                    for (Object row :  mapArray) {
                        Map m = (Map) row;
                        JSONObject jo = new JSONObject(m);
                        jsonArray.put(jo);
                    }
                    jsonObject.put(propertyName, jsonArray);
                } else if (objValue != null && objValue instanceof Map) {
                    jsonObject.put(propertyName, (Map) objValue);
                } else {
                    String value = (objValue != null) ? objValue.toString() : "";
                    jsonObject.accumulate(propertyName, value);
                }
            }
        }
        return jsonObject;
    }

    /**
     * Generates a JSONObject to represent an element
     * @param element
     * @return
     * @throws Exception
     */
    public static JSONObject generateElementJsonObject(Element element) throws Exception {
        JSONObject jsonObj = new JSONObject();

        // set class name
        jsonObj.put(FormUtil.PROPERTY_CLASS_NAME, element.getClassName());

        // set properties
        JSONObject jsonProps = FormUtil.generatePropertyJsonObject(element.getProperties());
        jsonObj.put(FormUtil.PROPERTY_PROPERTIES, jsonProps);

        // set validator
        Validator validator = element.getValidator();
        if (validator != null) {
            JSONObject jsonValidatorProps = FormUtil.generatePropertyJsonObject(validator.getProperties());
            JSONObject jsonValidator = new JSONObject();
            jsonValidator.put(FormUtil.PROPERTY_CLASS_NAME, validator.getClassName());
            jsonValidator.put(FormUtil.PROPERTY_PROPERTIES, jsonValidatorProps);
            jsonProps.put(FormUtil.PROPERTY_VALIDATOR, jsonValidator);
        }

        // set load binder
        FormBinder loadBinder = (FormBinder) element.getLoadBinder();
        if (loadBinder != null) {
            JSONObject jsonLoadBinderProps = FormUtil.generatePropertyJsonObject(loadBinder.getProperties());
            JSONObject jsonLoadBinder = new JSONObject();
            jsonLoadBinder.put(FormUtil.PROPERTY_CLASS_NAME, loadBinder.getClassName());
            jsonLoadBinder.put(FormUtil.PROPERTY_PROPERTIES, jsonLoadBinderProps);
            jsonProps.put(FormBinder.FORM_LOAD_BINDER, jsonLoadBinder);
        }

        // set store binder
        FormBinder storeBinder = (FormBinder) element.getStoreBinder();
        if (storeBinder != null) {
            JSONObject jsonStoreBinderProps = FormUtil.generatePropertyJsonObject(storeBinder.getProperties());
            JSONObject jsonStoreBinder = new JSONObject();
            jsonStoreBinder.put(FormUtil.PROPERTY_CLASS_NAME, storeBinder.getClassName());
            jsonStoreBinder.put(FormUtil.PROPERTY_PROPERTIES, jsonStoreBinderProps);
            jsonProps.put(FormBinder.FORM_STORE_BINDER, jsonStoreBinder);
        }

        // set options binder
        FormBinder optionsBinder = (FormBinder) element.getOptionsBinder();
        if (optionsBinder != null) {
            JSONObject jsonOptionsBinderProps = FormUtil.generatePropertyJsonObject(optionsBinder.getProperties());
            JSONObject jsonOptionsBinder = new JSONObject();
            jsonOptionsBinder.put(FormUtil.PROPERTY_CLASS_NAME, optionsBinder.getClassName());
            jsonOptionsBinder.put(FormUtil.PROPERTY_PROPERTIES, jsonOptionsBinderProps);
            jsonProps.put(FormBinder.FORM_OPTIONS_BINDER, jsonOptionsBinder);
        }


        // set child elements
        JSONArray jsonChildren = new JSONArray();
        Collection<Element> children = element.getChildren();
        if (children != null) {
            for (Element child : children) {
                JSONObject childJson = FormUtil.generateElementJsonObject(child);
                jsonChildren.put(childJson);
            }
        }
        jsonObj.put(FormUtil.PROPERTY_ELEMENTS, jsonChildren);

        return jsonObj;
    }

    /**
     * Generates the HTML tag meta data for the element that is used by the form builder.
     * @param element
     * @return
     */
    public static String generateElementMetaData(Element element) {
        String properties = FormUtil.getElementProcessedJson(element);
        String escaped = StringEscapeUtils.escapeHtml(properties);
        String elementMetaData = " element-class=\"" + element.getClass().getName() + "\" element-property=\"" + escaped + "\" ";
        return elementMetaData;
    }

    /**
     * Generates HTML output using a FreeMarker template.
     * @param templatePath
     * @param dataModel
     * @return
     */
    public static String generateElementHtml(final Element element, final FormData formData, final String templatePath, @SuppressWarnings("rawtypes") Map dataModel) {
        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        String content = pluginManager.getPluginFreeMarkerTemplate(dataModel, element.getClassName(), "/templates/" + templatePath, "message/form/" + element.getName().replace(" ", ""));
        
        String readonly = "_EDITABLE";
        if (FormUtil.isReadonly(element, formData)) {
             readonly = "_READONLY";
        }
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            if (request != null) {
                request.setAttribute(element.getClassName() + readonly, true);
                request.setAttribute(element.getClassName(), true);
            }
        } catch (NoClassDefFoundError e) {
            // ignore if servlet request is not available
        }
        
        return content;
    }

    /**
     * Generates a standard map of data to be used within an element template.
     * @param element
     * @param formData
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map generateDefaultTemplateDataModel(Element element, FormData formData) {
        Map dataModel = new HashMap();

        // set element and form data
        dataModel.put("element", element);
        dataModel.put("formData", formData);

        // set param name
        String paramName = FormUtil.getElementParameterName(element);
        dataModel.put("elementParamName", paramName);

        // set validator decoration
        String decoration = FormUtil.getElementValidatorDecoration(element, formData);
        dataModel.put("decoration", decoration);

        // set error, if any
        String error = FormUtil.getElementError(element, formData);
        dataModel.put("error", error);

        // set metadata flag
        dataModel.put("includeMetaData", Boolean.FALSE);
        dataModel.put("elementMetaData", "");

        // add request into data model
        if (!dataModel.containsKey("request")) {
            try {
                HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
                if (request != null) {
                    dataModel.put("request", request);
                } else {
                    Map<String, String> r = new HashMap<String, String>();
                    r.put("contextPath", "/kecak");
                    dataModel.put("request", r);
                }
            } catch (NoClassDefFoundError e) {
                // ignore if servlet request is not available
            }
        }
        
        // sanitize label output
        String label = element.getPropertyString(FormUtil.PROPERTY_LABEL);
        if (label != null && !label.trim().isEmpty()) {
            label = StringUtil.stripHtmlRelaxed(label);
            element.setProperty(FormUtil.PROPERTY_LABEL, label);
        }
        
        return dataModel;
    }

    /**
     * Recursively set the readonly property for all descendent elements.
     * @param element
     */
    public static void setReadOnlyProperty(Element element) {
        setReadOnlyProperty(element, true, null);
    }
    
    /**
     * Recursively set the readonly property for all descendent elements.
     * @param element
     */
    public static void setReadOnlyProperty(Element element, Boolean readonly, Boolean label) {
        if (readonly != null && readonly) {
            element.setProperty(FormUtil.PROPERTY_READONLY, "true");
        }
        if (label != null && label) {
            element.setProperty(FormUtil.PROPERTY_READONLY_LABEL, "true");
        }
        Collection<Element> children = element.getChildren();
        for (Element child : children) {
            setReadOnlyProperty(child, readonly, label);
        }
    }

    /**
     * Check a form is submitted or not
     * @param formData
     */
    public static boolean isFormSubmitted(Element element, FormData formData) {
        Form form = findRootForm(element);
        if (form != null) {
            String paramName = FormUtil.getElementParameterName(form);
            if (formData != null && formData.getRequestParameter(paramName+"_SUBMITTED") != null) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check an element is readonly or not
     * @param formData
     */
    public static boolean isReadonly(Element element, FormData formData) {
        if ("true".equalsIgnoreCase(element.getPropertyString(FormUtil.PROPERTY_READONLY))) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets a running number a unique key
     * @return 
     */
    public static String getUniqueKey() {
        if (runningNumber == Long.MAX_VALUE) {
            runningNumber = 0L;
        }
        runningNumber++;
        return Long.toString(runningNumber);
    }
    
    /**
     * Set flag in request to indicate whether is currently in the form builder.
     */
    public static void setFormBuilderActive(Boolean active) {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            request.setAttribute(FORM_BUILDER_ACTIVE, active);
        }
    }

    /**
     * Check whether request is currently in the form builder.
     * @return 
     */
    public static boolean isFormBuilderActive() {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        boolean formBuilderActive = (request != null) ? Boolean.TRUE.equals(request.getAttribute(FORM_BUILDER_ACTIVE)) : false;
        return formBuilderActive;
    }
    
    /**
     * Used by system to sets the current processing form json in current thread
     * @param json 
     */
    @SuppressWarnings("unchecked")
	public static void setProcessedFormJson(String json) {
        processedFormJson.set(json);
    }
    
    /**
     * Used by system to gets the current processing form json in current thread
     * @return 
     */
    public static String getProcessedFormJson() {
        if (processedFormJson != null && processedFormJson.get() != null) {
            return (String) processedFormJson.get();
        }
        return null;
    }
    
    /**
     * Used by system to clears the current processing form json in current thread
     */
    @SuppressWarnings("unchecked")
	public static void clearProcessedFormJson() {
        if (processedFormJson != null && processedFormJson.get() != null) {
            processedFormJson.set(null);
        }
    }
    
    /**
     * Utility method to convert an element to json
     * @param element
     * @return 
     */
    public static String getElementProcessedJson(Element element) {
        String properties = "";
        try {
            // create json object
            JSONObject obj = new JSONObject(getProcessedFormJson());
            Element temp = element;
            
            // get the elements on the path to root element;
            Stack<Element> stack = new Stack<Element>();
            while (temp != null) {
                stack.push(temp);
                temp = temp.getParent();
            }
            
            // get the first element (Root element) in stack to match with root Json Object 
            temp = stack.pop();
            
            //if statck is not empty, continue find the matching json object
            while (!stack.isEmpty()) {
                temp = stack.pop();
                
                //travel in json object's child to find matching json object
                if (!obj.isNull(FormUtil.PROPERTY_ELEMENTS)) {
                    int position = 0;
                    
                    //get position
                    Element parent = temp.getParent();
                    Collection<Element> children = parent.getChildren();
                    if (children != null) {
                        for (Element c : children) {
                            if (c.equals(temp)) {
                                break;
                            }
                            position++;
                        }
                    }
                    
                    //get json object
                    JSONArray elements = obj.getJSONArray(FormUtil.PROPERTY_ELEMENTS);
                    if (elements != null && elements.length() > 0) {
                        obj = (JSONObject) elements.get(position);
                    }
                }
            }
            
            if (temp != null && obj != null) {
                //get properties obj and convert to json string
                if (!obj.isNull(FormUtil.PROPERTY_PROPERTIES)) {
                    JSONObject objProperty = obj.getJSONObject(FormUtil.PROPERTY_PROPERTIES);
                    properties = objProperty.toString();
                }
            }
            
        } catch (Exception e) {
            properties = FormUtil.generateElementPropertyJson(element);
        }
        return properties;
    }

    /**
     * Similar to loadFormData, returns results in JSON format.
     * @param appId
     * @param appVersion
     * @param formDefId
     * @param primaryKeyValue
     * @param includeSubformData
     * @param includeReferenceElements
     * @param flatten
     * @param assignment
     * @return 
     */
    public static String loadFormDataJson(String appId, String appVersion, String formDefId, String primaryKeyValue, boolean includeSubformData, boolean includeReferenceElements, boolean flatten, WorkflowAssignment assignment) throws JSONException {
        Map<String, Object> result = loadFormData(appId, appVersion, formDefId, primaryKeyValue, includeSubformData, includeReferenceElements, flatten, assignment);
        JSONObject jsonObject = new JSONObject(result);
        String json = jsonObject.toString(4);
        return json;
    }
    
    /**
     * Utility method to fetch submitted form data values including data from subforms, and reference fields.
     * Returns a key-value pair (optionally flattened) for all the data that is part of a form submission. 
     * The returned data includes top level form data, subform data (including recursive subforms), and data pointed by reference fields (like SelectBox pointing to a datalist item)
     * @param appId
     * @param appVersion
     * @param formDefId
     * @param primaryKeyValue
     * @param includeSubformData true to recursively include subform data
     * @param includeReferenceElements true to include data from reference elements e.g. selectbox, etc.
     * @param flatten true to flatten data into a one level key-value map
     * @param assignment Optional workflow assignment (for assignment hash variables)
     * @return a Map<String,Object> representing the form data. The key is the element ID, and the value is either a String for an element value, Map<String,Object> representing subform data, or Collection<Map<String,Object>> for reference fields.
     */
    public static Map<String, Object> loadFormData(final String appId, final String appVersion, final String formDefId, final String primaryKeyValue, final boolean includeSubformData, final boolean includeReferenceElements, final boolean flatten, final WorkflowAssignment assignment) {

        final Map<String, Object> result = new HashMap<String, Object>();
        
        // get service and DAO objects
        ApplicationContext ac = AppUtil.getApplicationContext();
        AppService appService = (AppService)ac.getBean("appService");
        final FormService formService = (FormService)ac.getBean("formService");
        final FormDefinitionDao formDefinitionDao = (FormDefinitionDao)ac.getBean("formDefinitionDao");
        final AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
        
        if (appDef != null && formDefId != null && !formDefId.isEmpty() && primaryKeyValue != null) {
            // get root form
            Form form = null;
            FormData formData = new FormData();
            formData.setPrimaryKeyValue(primaryKeyValue);
            FormDefinition formDef = formDefinitionDao.loadById(formDefId, appDef);
            if (formDef != null) {
                String formJson = formDef.getJson();
                if (formJson != null) {
                    formJson = AppUtil.processHashVariable(formJson, assignment, StringUtil.TYPE_JSON, null);
                    form = (Form) formService.loadFormFromJson(formJson, formData);
                }

                // load data
                int currentDepth = 0;
                recursiveLoadFormData(appId, appVersion, form, result, formData, includeSubformData, includeReferenceElements, flatten, assignment, currentDepth);
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
	protected static void recursiveLoadFormData(String appId, String appVersion, Element e, Map<String, Object> data, FormData formData, boolean includeSubformData, boolean includeReferenceElements, boolean flatten, WorkflowAssignment assignment, int currentDepth) {
        boolean recursive = currentDepth == 0 || includeSubformData;
        Map<String, Object> result = data;
        FormLoadBinder loadBinder = e.getLoadBinder();
        FormLoadBinder optionsBinder = e.getOptionsBinder();
        if (loadBinder != null) {
            if (recursive) {
                // load form data
                FormRowSet rowSet = formData.getLoadBinderData(e);
                if (rowSet != null && !rowSet.isEmpty()) {
                    FormRow row = rowSet.get(0);
                    boolean useSubMap = !flatten && !(e instanceof Form);
                    if (useSubMap) {
                        // it's data from a different form, so put data into submap
                        Map<String, Object> subMap = new HashMap<String, Object>();
                        for (@SuppressWarnings("rawtypes")
						Iterator i=row.keySet().iterator(); i.hasNext();) {
                            String key = (String)i.next();
                            Object value = row.get(key);
                            subMap.put(key, value.toString());
                        }
                        String elementKey = e.getPropertyString(FormUtil.PROPERTY_ID);
                        data.put(elementKey, subMap);
                        result = subMap;
                    } else {
                        // it's the same as the original form, so put data into original map
                        for (@SuppressWarnings("rawtypes")
						Iterator i=row.keySet().iterator(); i.hasNext();) {
                            String key = (String)i.next();
                            Object value = row.get(key);
                            data.put(key, value.toString());
                        }
                    }
                }
            }
        } else if (includeReferenceElements && e instanceof FormReferenceDataRetriever) {
            // handle reference fields for elements implementing FormReferenceDataRetriever
            Collection<Map<String, Object>> subResults = new ArrayList<Map<String, Object>>();

            // get values
            String[] valueArray = FormUtil.getElementPropertyValues(e, formData);
            FormRowSet options = ((FormReferenceDataRetriever)e).loadFormRows(valueArray, formData);
            for (@SuppressWarnings("rawtypes") Map opt: options) {
                @SuppressWarnings("rawtypes")
				Map optionRow = new HashMap(opt);
                subResults.add(optionRow);
            }
            if (!subResults.isEmpty()) {
                String elementKey = e.getPropertyString(FormUtil.PROPERTY_ID);
                data.put(elementKey, subResults);
            }
        } else if (optionsBinder != null) {
            // handle reference fields
            if (includeReferenceElements && optionsBinder instanceof FormLoadOptionsBinder) {
                Collection<Map<String, Object>> subResults = new ArrayList<Map<String, Object>>();
                if (optionsBinder instanceof FormOptionsBinder) {
                    // element is using FormOptionsBinder, so retrieve all form data for the row
                    String optionsFormDefId = ((FormOptionsBinder)optionsBinder).getPropertyString("formDefId");
                    if (optionsFormDefId != null && !optionsFormDefId.isEmpty()) {
                        String[] values = FormUtil.getElementPropertyValues(e, formData);
                        for (String value: values) {
                            Map<String, Object> optionRow = loadFormData(appId, appVersion, optionsFormDefId, value, includeSubformData, includeReferenceElements, flatten, assignment);
                                subResults.add(optionRow);
                            }
                        }
                    
                } else {
                    // other binder type is used, so just load available options
                    Map<String, String> optionMap = new HashMap<String, String>();
                    @SuppressWarnings("rawtypes")
					Collection<Map> options = FormUtil.getElementPropertyOptionsMap(e, formData);
                    for (Map<String, String> opt: options) {
                        String key = opt.get(FormUtil.PROPERTY_VALUE);
                        String label = opt.get(FormUtil.PROPERTY_LABEL);
                        optionMap.put(key, label);
                    }

                    // load reference data
                    String[] values = FormUtil.getElementPropertyValues(e, formData);
                    for (String value: values) {
                        String label = optionMap.get(value);
                        Map<String, Object> optionRow = new HashMap<String, Object>();
                        if (value != null && label != null) {
                            optionRow.put(value, label);
                            subResults.add(optionRow);
                        }
                    }
                }
                String elementKey = e.getPropertyString(FormUtil.PROPERTY_ID);
                data.put(elementKey, subResults);
            }
        }
        
        Collection<Element> children = e.getChildren();
        if (children != null && !children.isEmpty()) {
            currentDepth++;
            for (Element c : children) {
                recursiveLoadFormData(appId, appVersion, c, result, formData, includeSubformData, includeReferenceElements, flatten, assignment, currentDepth);
            }
        }
    } 
    
    /**
     * Check the element is using Ajax to load options 
     * @param element
     * @param formData
     * @return 
     */
    public static boolean isAjaxOptionsSupported(Element element, FormData formData) {
        boolean supported = true;
        
        //only support ajax when data encryption and nonce generator are available
        if (!(SecurityUtil.getDataEncryption() != null && SecurityUtil.getNonceGenerator() != null)) {
            supported = false;
        }
        
        //if control field not exist
        if (!(element instanceof FormAjaxOptionsElement && ((FormAjaxOptionsElement) element).getControlElement(formData) != null)) {
            supported = false;
        }
        
        //if option binder not support ajax
        if (!(element.getOptionsBinder() != null && element.getOptionsBinder() instanceof FormAjaxOptionsBinder && ((FormAjaxOptionsBinder) element.getOptionsBinder()).useAjax())) {
            supported = false;
        }
        return supported;
    }
    
    /**
     * Sets security data for multi options element which using Ajax call to load options
     * @param element
     * @param formData 
     */
    public static void setAjaxOptionsElementProperties(Element element, FormData formData) {
        if (isAjaxOptionsSupported(element, formData)) {
            FormBinder binder = (FormBinder) element.getOptionsBinder();
            
            String s = null;
            try {
                JSONObject jo = new JSONObject();
                jo.accumulate(FormUtil.PROPERTY_CLASS_NAME, binder.getClassName());
                jo.accumulate(FormUtil.PROPERTY_PROPERTIES, FormUtil.generatePropertyJsonObject(binder.getProperties()));
                
                s = jo.toString();
            } catch (Exception e) {}
            if (s != null) {
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                element.setProperty("appId", appDef.getAppId());
                element.setProperty("appVersion", appDef.getVersion());
                
                String nonce = SecurityUtil.generateNonce(new String[]{"AjaxOptions", appDef.getAppId(), s.substring(s.length() - 20)}, 1);
                element.setProperty("nonce", nonce);
                
                try {
                    //secure the data
                    s = SecurityUtil.encrypt(s);
                    s = URLEncoder.encode(s, "UTF-8");
                } catch (Exception e) {}
                element.setProperty("binderData", s);
            }
        }
    }
    
    /**
     * Gets data from Options Binder for AJAX call
     * @param dependencyValue
     * @param appDef
     * @param nonce
     * @param binderData
     * @return 
     */
    public static FormRowSet getAjaxOptionsBinderData(String dependencyValue, AppDefinition appDef, String nonce, String binderData) {
        FormRowSet rowSet = new FormRowSet();
        
        if (binderData != null && !binderData.isEmpty() && nonce != null && !nonce.isEmpty() && appDef != null) {
            try {
                binderData = URLDecoder.decode(binderData, "UTF-8");
                binderData = SecurityUtil.decrypt(binderData);
                
                if (SecurityUtil.verifyNonce(nonce, new String[] {"AjaxOptions", appDef.getAppId(), binderData.substring(binderData.length() - 20)})) {
                    FormBinder binder = null;
                    
                    JSONObject jo = new JSONObject(binderData);
                    // create binder object
                    if (!jo.isNull(FormUtil.PROPERTY_CLASS_NAME)) {
                        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
                        
                        String className = jo.getString(FormUtil.PROPERTY_CLASS_NAME);
                        if (className != null && className.trim().length() > 0) {
                            binder = (FormBinder) pluginManager.getPlugin(className);
                            if (binder != null) {
                                // set child properties
                                Map<String, Object> properties = FormUtil.parsePropertyFromJsonObject(jo);
                                binder.setProperties(properties);
                            }
                        }
                    }
                    
                    if (binder != null) {
                        FormAjaxOptionsBinder ab = (FormAjaxOptionsBinder) binder;
                        rowSet = ab.loadAjaxOptions(dependencyValue.split(";"));
                    }
                }
            } catch (Exception e) {}
        }
        
        return rowSet;
    }
    
    /**
     * Retrieve all form fields id & label in form data table
     * @param appDef
     * @param formId
     * @return 
     */
    @SuppressWarnings("unchecked")
	public static Collection<Map<String, String>> getFormColumns(AppDefinition appDef, String formId) {
        Collection<Map<String, String>> columns = new ArrayList<Map<String, String>>();
        try {
            FormRowDataListBinder binder = new FormRowDataListBinder();
            binder.setProperty("formDefId", formId);

            DataListColumn[] sourceColumns = binder.getColumns();
            if (sourceColumns != null && sourceColumns.length > 0) {
                // sort columns by label
                List<DataListColumn> binderColumnList = Arrays.asList(sourceColumns);
                Collections.sort(binderColumnList, new Comparator<DataListColumn>() {
                    public int compare(DataListColumn o1, DataListColumn o2) {
                        return o1.getLabel().toLowerCase().compareTo(o2.getLabel().toLowerCase());
                    }
                });

                for (DataListColumn c : binderColumnList) {
                    @SuppressWarnings("rawtypes")
					HashMap hm = new HashMap();
                    hm.put("value", c.getName());
                    hm.put("label", AppUtil.processHashVariable(c.getLabel(), null, null, null, appDef));
                    columns.add(hm);
                }
            }
        } catch (Exception e) {}
        return columns;
    }
    
    /**
     * Utility method used to creates a new form definition json
     * @param formId
     * @param formDef
     * @return 
     */
    public static String generateDefaultForm(String formId, FormDefinition formDef) {
        return generateDefaultForm(formId, formDef, null);
    }
    
    /**
     * Utility method used to creates a new form definition json based on another form definition
     * @param formId
     * @param formDef
     * @param copyFormDef
     * @return 
     */
    public static String generateDefaultForm(String formId, FormDefinition formDef, FormDefinition copyFormDef) {
        String formName = "";
        String tableName = "";
        String description = "";
        String json = "";

        if (formDef != null) {
            tableName = formDef.getTableName();
            description = formDef.getDescription();
            formName = formDef.getName();
        }
        if (tableName == null || tableName.isEmpty()) {
            tableName = formId;
        }
        if (copyFormDef != null) {
            String copyJson = copyFormDef.getJson();
            try {
                JSONObject obj = new JSONObject(copyJson);
                if (!obj.isNull(FormUtil.PROPERTY_PROPERTIES)) {
                    JSONObject objProperty = obj.getJSONObject(FormUtil.PROPERTY_PROPERTIES);
                    objProperty.put(FormUtil.PROPERTY_ID, formId);
                    objProperty.put(FormUtil.PROPERTY_TABLE_NAME, tableName);
                    objProperty.put("name", formName);
                    objProperty.put("description", description);
                }
                json = obj.toString();
            } catch (Exception e) {
            }
        }

        if (json.isEmpty()) {
            formName = StringEscapeUtils.escapeJavaScript(formName);
            description = StringEscapeUtils.escapeJavaScript(description);
            json = "{className: 'org.joget.apps.form.model.Form',  \"properties\":{ \"id\":\"" + formId + "\", \"name\":\"" + formName + "\", \"tableName\":\"" + tableName + "\", \"loadBinder\":{ \"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\" }, \"storeBinder\":{ \"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\" }, \"description\":\"" + description + "\" }}";
        }

        return json;
    }

    /**
     * Utility methods to execute tool after form submission
     * @param form
     * @param formData 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void executePostFormSubmissionProccessor(Form form, FormData formData) {
        try {
            Object proccessor = form.getProperty(FormUtil.PROPERTY_POST_PROCESSOR);
            if (!formData.getStay() && (formData.getFormErrors() == null || formData.getFormErrors().isEmpty())
                    && proccessor != null && proccessor instanceof Map) {

                String run = form.getPropertyString(FormUtil.PROPERTY_POST_PROCESSOR_RUN_ON);
                String status = "update";
                String primaryKey = form.getPrimaryKeyValue(formData);
                if (formData.getRequestParameter("saveAsDraft") != null) {
                    status = "draft";
                } else if (primaryKey != null && !primaryKey.equals(formData.getRequestParameter(FormUtil.FORM_META_ORIGINAL_ID))) {
                    status = "create";
                }

                if (run.equals(status) || ("both".equals(run) && !"draft".equals(status))) {
                    PluginManager pluginManager = (PluginManager) FormUtil.getApplicationContext().getBean("pluginManager");
                    Map temp = (Map) proccessor;
                    String className = temp.get("className").toString();
                    Plugin p = pluginManager.getPlugin(className);

                    if (p != null) {
                        WorkflowAssignment ass = null;
                        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                        if (formData.getActivityId() != null && !formData.getActivityId().isEmpty()) {
                            WorkflowManager workflowManager = (WorkflowManager) FormUtil.getApplicationContext().getBean("workflowManager");
                            ass = workflowManager.getAssignment(formData.getActivityId());
                        } else if (formData.getProcessId() != null && !formData.getProcessId().isEmpty()) {
                            //create an mock workflow assignment for run process form
                            ass = new WorkflowAssignment();
                            ass.setProcessId(formData.getProcessId());
                        } else if (primaryKey!= null && !primaryKey.isEmpty()) {
                            //create an mock workflow assignment to pass record id as process id as most of the existing tool as using it to retrieve record
                            ass = new WorkflowAssignment();
                            ass.setProcessId(primaryKey);
                        }
                        
						Map propertiesMap = null;
                        
                        //get form json again to retrieve plugin properties
                        FormDefinitionDao formDefinitionDao = (FormDefinitionDao) FormUtil.getApplicationContext().getBean("formDefinitionDao");
                        FormDefinition formDefinition = formDefinitionDao.loadById(form.getPropertyString(FormUtil.PROPERTY_ID), appDef);
                        if (formDefinition != null) {
                            String json = formDefinition.getJson();
                            JSONObject obj = new JSONObject(json);
                            JSONObject objProperty = obj.getJSONObject(FormUtil.PROPERTY_PROPERTIES);
                            if (objProperty.has(FormUtil.PROPERTY_POST_PROCESSOR)) {
                                JSONObject objProcessor = objProperty.getJSONObject(FormUtil.PROPERTY_POST_PROCESSOR);
                                json = objProcessor.getString(FormUtil.PROPERTY_PROPERTIES);
                                propertiesMap = AppPluginUtil.getDefaultProperties(p, json, appDef, ass);
                            }
                        }
                        
                        if (propertiesMap == null) {
                            propertiesMap = AppPluginUtil.getDefaultProperties(p, (Map) temp.get(FormUtil.PROPERTY_PROPERTIES), appDef, ass);
                        }
                        if (ass != null) {
                            propertiesMap.put("workflowAssignment", ass);
                        }
                        propertiesMap.put("recordId", formData.getPrimaryKeyValue());
                        propertiesMap.put("pluginManager", pluginManager);
                        propertiesMap.put("appDef", appDef);

                        // add HttpServletRequest into the property map
                        try {
                            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
                            if (request != null) {
                                propertiesMap.put("request", request);
                            }
                        } catch (Throwable e) {
                            // ignore if class is not found
                        }
                        
                        ApplicationPlugin appPlugin = (ApplicationPlugin) p;
                        
                        if (appPlugin instanceof PropertyEditable) {
                            ((PropertyEditable) appPlugin).setProperties(propertiesMap);
                        }
                        appPlugin.execute(propertiesMap);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(AppService.class.getName(), e, "Error executing Post Form Submission Processor");
        }
    }
}