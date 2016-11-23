package org.joget.apps.app.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.displaytag.util.ParamEncoder;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListAction;
import org.joget.apps.datalist.model.DataListBinder;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListFilter;
import org.joget.apps.datalist.model.DataListFilterType;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.ext.ConsoleWebPlugin;
import org.joget.commons.util.CsvUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.ui.ModelMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DatalistBuilderWebController {

    public static final String PREFIX_SELECTED = "selected_";
    public static final String PREFIX_BINDER_PROPERTY = "binder_";
    @Autowired
    DataListService dataListService;
    @Autowired
    AppService appService;
    @Autowired
    DatalistDefinitionDao datalistDefinitionDao;
    @Autowired
    PluginManager pluginManager;

    @RequestMapping("/console/app/(*:appId)/(~:version)/datalist/builder/(*:id)")
    public String builder(ModelMap map, @RequestParam("appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam("id") String id, @RequestParam(required = false) String json) throws Exception {
        // verify app version
        ConsoleWebPlugin consoleWebPlugin = (ConsoleWebPlugin)pluginManager.getPlugin(ConsoleWebPlugin.class.getName());
        String page = consoleWebPlugin.verifyAppVersion(appId, version);
        if (page != null) {
            return page;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        DatalistDefinition datalist = datalistDefinitionDao.loadById(id, appDef);
        String listJson = null;
        if (json != null && !json.trim().isEmpty()) {
            try {
                // validate JSON
                new JSONObject(json);
                
                // read custom JSON from request
                listJson = json;
            } catch (JSONException ex) {
                listJson = "{}";
            }
        } else {
            // get JSON from form definition
            listJson = datalist.getJson();
        }

        map.addAttribute("id", id);
        map.addAttribute("filterParam", new ParamEncoder(id).encodeParameterName(DataList.PARAMETER_FILTER_PREFIX));
        map.addAttribute("datalist", datalist);
        map.addAttribute("json", PropertyUtil.propertiesJsonLoadProcessing(listJson));
        return "dbuilder/builder";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/datalist/builderSave/(*:id)", method = RequestMethod.POST)
    public String save(Writer writer, @RequestParam("appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam("id") String id, @RequestParam("json") String json) throws Exception {
        // verify app license
        ConsoleWebPlugin consoleWebPlugin = (ConsoleWebPlugin)pluginManager.getPlugin(ConsoleWebPlugin.class.getName());
        String page = consoleWebPlugin.verifyAppVersion(appId, version);
        if (page != null) {
            return page;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        DatalistDefinition datalist = datalistDefinitionDao.loadById(id, appDef);
        DataList dlist = dataListService.fromJson(json);
        datalist.setName(dlist.getName());
        datalist.setDescription(dlist.getDescription());
        datalist.setJson(PropertyUtil.propertiesJsonStoreProcessing(datalist.getJson(), json));

        boolean success = datalistDefinitionDao.update(datalist);
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("success", success);
        jsonObject.write(writer);
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = {"/console/app/(*:appId)/(~:appVersion)/datalist/builderPreview/(*:id)", "/client/app/(*:appId)/(*:appVersion)/datalist/(*:id)"})
    public String preview(ModelMap map, HttpServletRequest request, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam("id") String id, @RequestParam(required = false) String json) throws Exception {
        String view = "dbuilder/view";

        // get current app to set into thread
        AppDefinition appDef = appService.getAppDefinition(appId, appVersion);

        try {
            // get data list
            DataList dataList = new DataList();
            if (json != null && !json.trim().isEmpty()) {
                
                String tempJson = json;
                if (tempJson.contains(SecurityUtil.ENVELOPE) || tempJson.contains(PropertyUtil.PASSWORD_PROTECTED_VALUE)) {
                    DatalistDefinition datalistDef = datalistDefinitionDao.loadById(id, appDef);

                    if (datalistDef != null) {
                        tempJson = PropertyUtil.propertiesJsonStoreProcessing(datalistDef.getJson(), tempJson);
                    }
                }
                
                dataList = dataListService.fromJson(AppUtil.processHashVariable(tempJson, null, StringUtil.TYPE_JSON, null));
                map.addAttribute("json", json);
            } else {
                dataList = parseFromJsonParameter(map, dataList, id, request);
            }

            map.addAttribute("dataList", dataList);

        } catch (Exception ex) {
            StringWriter out = new StringWriter();
            ex.printStackTrace(new PrintWriter(out));
            String message = ex.toString();
            message += "\r\n<pre class=\"stacktrace\">" + out.getBuffer() + "</pre>";
            map.addAttribute("error", message);
        }

        // set map into model to be used in the JSP template
        map.addAttribute("properties", new HashMap(map));
        return view;
    }

    @SuppressWarnings("unchecked")
	@RequestMapping("/json/console/app/(*:appId)/(~:appVersion)/builder/actions")
    public void getBuilderDataActionList(ModelMap map, Writer writer, @RequestParam("appId") String appId, @RequestParam(required = false) String appVersion, HttpServletRequest request) throws Exception {
        appService.getAppDefinition(appId, appVersion);
        JSONObject jsonObject = new JSONObject();

        // get available binders
        DataListAction[] actions = dataListService.getAvailableActions();

        Collection<Object> collection = new ArrayList<Object>();
        for (DataListAction action : actions) {
            Plugin p = (Plugin) action;
            @SuppressWarnings("rawtypes")
			HashMap hm = new HashMap();
            hm.put("name", p.getName());
            hm.put("label", p.getI18nLabel());
            hm.put("className", action.getClassName());
            if (action instanceof PropertyEditable) {
                String propertyOptions = ((PropertyEditable) action).getPropertyOptions();
                if (propertyOptions != null && !propertyOptions.isEmpty()) {
                    hm.put("propertyOptions", propertyOptions);
                }
            }
            hm.put("type", "text");
            collection.add(hm);
        }
        jsonObject.accumulate("actions", collection);
        jsonObject.write(writer);
    }

    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/json/console/app/(*:appId)/(~:appVersion)/builder/binder/columns", method = RequestMethod.POST)
    public void getBuilderDataColumnList(ModelMap map, Writer writer, @RequestParam("appId") String appId, @RequestParam(required = false) String appVersion, @RequestParam String id, @RequestParam String binderId, HttpServletRequest request) throws Exception {
        AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
        JSONObject jsonObject = new JSONObject();

        // get data list
        DataList dataList = new DataList();

        // parse JSON from request if available
        dataList = parseFromJsonParameter(map, dataList, id, request);

        // get binder from request
        DataListBinder binder = createDataListBinderFromRequestInternal(appDef, id, binderId, request);
        if (binder != null) {
            dataList.setBinder(binder);
        }

        DataListColumn[] sourceColumns = (binder != null) ? binder.getColumns() : new DataListColumn[0];
 
        // sort columns by label
        List<DataListColumn> binderColumnList = Arrays.asList(sourceColumns);
        Collections.sort(binderColumnList, new Comparator<DataListColumn>() {

            public int compare(DataListColumn o1, DataListColumn o2) {
                return o1.getLabel().toLowerCase().compareTo(o2.getLabel().toLowerCase());
            }
        });
        
        Collection<String> columnNameList = new HashSet<String>();
        DataListColumn[] targetColumns = dataList.getColumns();
        if (targetColumns != null) {
            for (DataListColumn selectedColumn : targetColumns) {
                columnNameList.add(selectedColumn.getName());
            }
        }
        for (@SuppressWarnings("rawtypes")
		Iterator i = binderColumnList.iterator(); i.hasNext();) {
            DataListColumn column = (DataListColumn) i.next();
            if (columnNameList.contains(column.getName())) {
                i.remove();
            }
        }
        sourceColumns = (DataListColumn[]) binderColumnList.toArray(new DataListColumn[0]);
        DataList sourceDataList = new DataList();
        sourceDataList.setColumns(sourceColumns);

        Collection<Object> collection = new ArrayList<Object>();
        for (DataListColumn sourceColumn : sourceColumns) {
            @SuppressWarnings("rawtypes")
			HashMap hm = new HashMap();
            hm.put("name", sourceColumn.getName());
            hm.put("label", sourceColumn.getLabel());
            hm.put("displayLabel", AppUtil.processHashVariable(sourceColumn.getLabel(), null, null, null, appDef));
            hm.put("sortable", true);
            hm.put("filterable", true);
            hm.put("type", sourceColumn.getType());
            collection.add(hm);
        }
        jsonObject.accumulate("columns", collection);
        jsonObject.write(writer);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping("/dbuilder/getFilterTemplate")
    public String getBuilderFilterTemplate(ModelMap model, @RequestParam("json") String json) throws Exception {
        Map<String, Object> obj = PropertyUtil.getPropertiesValueFromJson(json);
        DataListFilter filter = new DataListFilter();
        filter.setLabel(obj.get("label").toString());
        filter.setName(obj.get("name").toString());
        if (obj.get("operator") != null) {
            filter.setOperator(obj.get("operator").toString());
        }
        if (obj.get("type") != null) {
            Map typeMap = (Map) obj.get("type");
            DataListFilterType type = (DataListFilterType) pluginManager.getPlugin(typeMap.get("className").toString());
            if (type != null) {
                if (!"{}".equals(typeMap.get("properties"))) {
                    type.setProperties((Map) typeMap.get("properties"));
                }
                filter.setType(type);
            }
        }

        model.addAttribute("template", filter.getType().getTemplate(new DataList(), filter.getName(), filter.getLabel()));
        return "dbuilder/filterTmplate";
    }

    protected DataListBinder createDataListBinderFromRequestInternal(AppDefinition appDef, String datalistId, String binderId, HttpServletRequest request) {
        DataListBinder binder = null;
        if (binderId != null && binderId.trim().length() > 0) {
            // create binder
            binder = dataListService.getBinder(binderId);

            if (request != null) {
                // get request params
                @SuppressWarnings("rawtypes")
				Enumeration e = request.getParameterNames();
                while (e.hasMoreElements()) {
                    String paramName = (String) e.nextElement();
                    if (paramName.startsWith(PREFIX_BINDER_PROPERTY)) {
                        String[] paramValue = (String[]) request.getParameterValues(paramName);
                        String propName = paramName.substring(PREFIX_BINDER_PROPERTY.length());
                        
                        String value = CsvUtil.getDeliminatedString(paramValue);
                        
                        if (value.contains(SecurityUtil.ENVELOPE) || value.contains(PropertyUtil.PASSWORD_PROTECTED_VALUE)) {
                            DatalistDefinition datalist = datalistDefinitionDao.loadById(datalistId, appDef);
                            
                            if (datalist != null) {
                                value = PropertyUtil.propertiesJsonStoreProcessing(datalist.getJson(), value);
                            }
                        }
                        
                        binder.setProperty(propName, AppUtil.processHashVariable(value, null, null, null));
                    }
                }
            }
        }
        return binder;
    }

    protected DataList parseFromJsonParameter(ModelMap map, DataList dataList, String id, HttpServletRequest request) {
        // get parameters

        String jsonParam = new ParamEncoder(id).encodeParameterName("json");
        String json = request.getParameter(jsonParam);

        // use preview json if available
        if (json != null && json.trim().length() > 0) {
            try {
                String tempJson = json;
                if (tempJson.contains(SecurityUtil.ENVELOPE) || tempJson.contains(PropertyUtil.PASSWORD_PROTECTED_VALUE)) {
                    AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                    DatalistDefinition datalist = datalistDefinitionDao.loadById(id, appDef);

                    if (datalist != null) {
                        tempJson = PropertyUtil.propertiesJsonStoreProcessing(datalist.getJson(), tempJson);
                    }
                }
                
                dataList = dataListService.fromJson(AppUtil.processHashVariable(tempJson, null, null, null));
                dataList.setId(id);
            } catch (Exception ex) {
                map.addAttribute("dataListError", ex.toString());
            }
        }/* else {
        json = dataListService.toJson(dataList);
        }*/

        String jsonEncoded = null;
        try {
            if (json != null) {
                jsonEncoded = URLEncoder.encode(json, "UTF-8");
            }
        } catch (Exception ex) {
            LogUtil.error(this.getClass().getName(), ex, "parseFromJsonParameter Error!");
        }

        // set for view
        map.addAttribute("json", json);
        map.addAttribute("jsonEncoded", jsonEncoded);
        map.addAttribute("jsonParam", jsonParam);
        return dataList;
    }
    
    @RequestMapping("/app/(*:appId)/(~:appVersion)/datalist/embed")
    public String embedDatalist(ModelMap model, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String version, HttpServletRequest request, @RequestParam("_submitButtonLabel") String buttonLabel, @RequestParam("_callback") String callback, @RequestParam("_setting") String callbackSetting, @RequestParam(required = false) String id, @RequestParam(value = "_listId", required = false) String listId, @RequestParam(value = "_type", required = false) String selectionType) throws JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        
        if (appDef == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        
        String nonce = request.getParameter("_nonce");
        if (!SecurityUtil.verifyNonce(nonce, new String[]{"EmbedList", appDef.getAppId(), appDef.getVersion().toString(), listId, nonce})) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        
        DatalistDefinition datalistDefinition = datalistDefinitionDao.loadById(listId, appDef);
        
        if (datalistDefinition == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        
        String json = datalistDefinition.getJson();
        String escapedJson = StringEscapeUtils.escapeJavaScript(json);
        DataList dataList = dataListService.fromJson(json);
        dataList.setSelectionType(selectionType);
        
        if (buttonLabel.isEmpty()) {
            buttonLabel = ResourceBundleUtil.getMessage("general.method.label.submit");
        }
        
        model.addAttribute("id", id);
        model.addAttribute("json", escapedJson);
        model.addAttribute("buttonLabel", buttonLabel);
        model.addAttribute("dataList", dataList);
        model.addAttribute("setting", callbackSetting);
        model.addAttribute("callback", callback);
        
        if (request.getParameter("_mapp") != null) {
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Content-type", "application/xml");
        
            return "mapp/embedDatalist";
        } else {   
            return "dbuilder/embedDatalist";
        }
    }
}
