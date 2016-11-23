package org.joget.apps.datalist.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.displaytag.decorator.CheckboxTableDecorator;
import org.displaytag.model.TableModel;
import org.displaytag.properties.MediaTypeEnum;
import org.displaytag.tags.TableTagParameters;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListAction;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormat;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.TimeZoneUtil;
import org.joget.workflow.util.WorkflowUtil;

/**
 * DisplayTag column decorator to modify columns e.g. format, add links, etc.
 */
public class DataListDecorator extends CheckboxTableDecorator {

    transient DataList dataList;
    // attributes from parent class to fix DisplayTag bug
    @SuppressWarnings("rawtypes")
	List checkedIds;
    String id;
    String fieldName;
    Boolean listRenderHtml = true;
    
    private int index = 0;

    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public void init(PageContext pageContext, Object decorated, TableModel tableModel) {
        super.init(pageContext, decorated, tableModel);

        this.dataList = (DataList) pageContext.findAttribute("dataList");

        // set values to fix DisplayTag bug later
        if (fieldName != null) {
            String[] params = pageContext.getRequest().getParameterValues(fieldName);
            checkedIds = params != null ? new ArrayList(Arrays.asList(params)) : new ArrayList(0); // used to fix DisplayTag bug
        } else {
            checkedIds = new ArrayList(0);
        }
        
        String disableListRenderHtml = WorkflowUtil.getSystemSetupValue("disableListRenderHtml");
        if (disableListRenderHtml != null && disableListRenderHtml.equals("true")) {
            listRenderHtml = false;
        } 
    }

    @Override
    public void setId(String id) {
        super.setId(id);
        this.id = id;
    }

    @Override
    public void setFieldName(String fieldName) {
        // Override this method to fix DisplayTag bug
        super.setFieldName(fieldName);
        this.fieldName = fieldName;
    }

    @Override
    public String getCheckbox() {
        // Override this method to fix DisplayTag bug
        String evaluatedId = "";
        boolean checked = false;
        if (id != null) {
            evaluatedId = ObjectUtils.toString(evaluate(id));
            checked = checkedIds.contains(evaluatedId);
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("<input type=\"checkbox\" name=\"");
        buffer.append(fieldName);
        buffer.append("\" value=\"");
        buffer.append(StringEscapeUtils.escapeHtml(evaluatedId));
        buffer.append("\"");
        if (checked) {
            checkedIds.remove(evaluatedId);
            buffer.append(" checked=\"checked\"");
        }
        buffer.append("/>");

        return buffer.toString();
    }
    
    public String getRadio() {
        // Override this method to fix DisplayTag bug
        String evaluatedId = "";
        boolean checked = false;
        if (id != null) {
            evaluatedId = ObjectUtils.toString(evaluate(id));
            checked = checkedIds.contains(evaluatedId);
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("<input type=\"radio\" name=\"");
        buffer.append(fieldName);
        buffer.append("\" value=\"");
        buffer.append(StringEscapeUtils.escapeHtml(evaluatedId));
        buffer.append("\"");
        if (checked) {
            checkedIds.remove(evaluatedId);
            buffer.append(" checked=\"checked\"");
        }
        buffer.append("/>");

        return buffer.toString();
    }

    /**
     * Decorator method for a column to display links. TODO: formatting?
     * @param columnName
     * @return
     */
    public Object getColumn(String columnName) {
        Object row = getCurrentRowObject();
        Object columnValue = evaluate(columnName);
        DataListColumn column = findColumn(columnName);

        // handle formatting
        String text = formatColumn(column, row, columnValue);

        // strip tags if media type is not HTML
        if (!MediaTypeEnum.HTML.equals(tableModel.getMedia())) {
            text = StringUtil.stripAllHtmlTag(text);
            text = StringEscapeUtils.unescapeHtml(text);
        }

        // handle links
        DataListAction action = column.getAction();
        if (text != null && action != null && action.getHref() != null && action.getHref().trim().length() > 0 && MediaTypeEnum.HTML.equals(tableModel.getMedia())) {
            String href = action.getHref();
            String target = action.getTarget();
            String hrefParam = (action.getHrefParam() != null && action.getHrefParam().trim().length() > 0) ? action.getHrefParam() : "";
            String hrefColumn = (action.getHrefColumn() != null && action.getHrefColumn().trim().length() > 0) ? action.getHrefColumn() : "";
            String confirm = action.getConfirmation();
            String link = generateLink(href, target, hrefParam, hrefColumn, text.toString(), confirm);
            text = link;
        }

        return text;
    }

    /**
     * Decorator method to display row actions as links
     * @return
     */
    public Object getActions() {
        String output = "";
        DataListAction[] actions = dataList.getRowActions();
        if (actions != null) {
            for (DataListAction action : actions) {
                String label = StringUtil.stripHtmlRelaxed(action.getLinkLabel());
                String link = "";
                if (isRowActionVisible(action)) {
                    link = generateLink(action.getHref(), action.getTarget(), action.getHrefParam(), action.getHrefColumn(), label, action.getConfirmation());
                }
                output += " " + link + " </td><td class=\"row_action\"> ";
            }
            output = output.substring(0, output.length() - 30);
        }
        return output;
    }

    protected DataListColumn findColumn(String columnName) {
        boolean skipHidden = false;
        String export =  dataList.getDataListParamString(TableTagParameters.PARAMETER_EXPORTING);
        String exportType = dataList.getDataListParamString(TableTagParameters.PARAMETER_EXPORTTYPE);
        if (("1".equals(export) && (exportType.equals("1") || exportType.equals("2") || exportType.equals("3") || exportType.equals("5")))) {
            skipHidden = true;
        }
        
        // get column, temporarily just iterate thru to find
        DataListColumn column = null;
        DataListColumn[] columns = dataList.getColumns();
        column = columns[index];
        if (index == columns.length - 1) {
            index = 0;
        } else {
            index++;
        }
        if (!column.getName().equals(columnName) && ((skipHidden && column.isHidden()) || (!column.isHidden() && "true".equals(column.getPropertyString("exclude_export"))))) {
            column = findColumn(columnName);
        }
        
        return column;
    }

    protected String generateLink(String href, String target, String hrefParam, String hrefColumn, String text, String confirmation) {
        // add links
        String link = href;
        String targetString = "";
        String confirmationString = "";

        if (link == null || text == null || text.isEmpty()) {
            link = text;
        } else {
            if (hrefParam != null && hrefColumn != null && !hrefColumn.isEmpty()) {
                String[] params = hrefParam.split(";");
                String[] columns = hrefColumn.split(";");
                
                for (int i = 0; i < columns.length; i++ ) {
                    if (columns[i] != null && !columns[i].isEmpty()) {
                        boolean isValid = false;
                        if (params.length > i && params[i] != null && !params[i].isEmpty()) {
                            if (link.contains("?")) {
                                link += "&";
                            } else {
                                link += "?";
                            }
                            link += StringEscapeUtils.escapeHtml(params[i]);
                            link += "=";
                            isValid = true;
                        } if (!link.contains("?")) {
                            if (!link.endsWith("/")) {
                                link += "/";
                            }
                            isValid = true;
                        }
                        
                        if (isValid) {
                            Object paramValue =evaluate(columns[i]);
                            if (paramValue == null) {
                                paramValue = StringEscapeUtils.escapeHtml(columns[i]);
                            }
                            try {
                                link += (paramValue != null) ? URLEncoder.encode(paramValue.toString(), "UTF-8") : null;
                            } catch (UnsupportedEncodingException ex) {
                                link += paramValue;
                            }
                        }
                    }
                }
            }
            
            if (target != null && "popup".equalsIgnoreCase(target)) {
                if (confirmation == null) {
                    confirmation = "";
                }
                confirmation = StringUtil.stripAllHtmlTag(confirmation);
                targetString = "onclick=\"return dlPopupAction(this, '" + confirmation + "')\"";
            } else if (target != null && "post".equalsIgnoreCase(target)) {
                if (confirmation == null) {
                    confirmation = "";
                }
                confirmation = StringUtil.stripAllHtmlTag(confirmation);
                targetString = "onclick=\"return dlPostAction(this, '" + confirmation + "')\"";
            } else {
                if (target != null && target.trim().length() > 0) {
                    targetString = " target=\"" + target + "\"";
                }
                if (confirmation != null && confirmation.trim().length() > 0) {
                    confirmation = StringUtil.stripAllHtmlTag(confirmation);
                    confirmationString = " onclick=\"return confirm('" + confirmation + "')\"";
                }
            }
            link = "<a href=\"" + link + "\"" + targetString + confirmationString + ">" + text + "</a>";
        }
        return link;
    }

    protected String formatColumn(DataListColumn column, Object row, Object value) {
        Object result = value;
        
        // decrypt protected data 
        if (result != null && result instanceof String) {
            result = SecurityUtil.decrypt(result.toString());
            
            // sanitize output
            String export =  dataList.getDataListParamString(TableTagParameters.PARAMETER_EXPORTING);
            String exportType = dataList.getDataListParamString(TableTagParameters.PARAMETER_EXPORTTYPE);
            if (!("1".equals(export) && (exportType.equals("1") || exportType.equals("2") || exportType.equals("3") || exportType.equals("5")))) {
                if (isRenderHtml(column)) {
                    result = StringUtil.stripHtmlRelaxed(result.toString());
                } else {
                    result = StringEscapeUtils.escapeHtml(result.toString());
                }
            }
        }

        Collection<DataListColumnFormat> formats = column.getFormats();
        if (formats != null) {
            for (DataListColumnFormat format : formats) {
                if (format != null) {
                    result = format.format(dataList, column, row, result);
                }
            }
        }

        String text = (result != null) ? result.toString() : null;
        return text;
    }
    
    @Override
    protected Object evaluate(String propertyName) {
        if (propertyName != null && !propertyName.isEmpty()) {
            try {
                Object value = super.evaluate(propertyName);

                //handle for lowercase propertyName
                if (value == null) {
                    value = super.evaluate(propertyName.toLowerCase());
                }
                if (value != null && value instanceof Date) {
                    value = TimeZoneUtil.convertToTimeZone((Date) value, null, AppUtil.getAppDateFormat());
                }
                return value;
            } catch (Exception e) {}
        }
        return null;
    }
    
    protected boolean isRowActionVisible(DataListAction rowAction) {
        boolean visible = true;
        
        Object[] rules = (Object[]) rowAction.getProperty("rules");
        if (rules != null && rules.length > 0) {
            for (Object o : rules) {
                @SuppressWarnings("rawtypes")
				Map ruleMap = (HashMap) o;
                
                String join = ruleMap.get("join").toString();
                String fieldId = ruleMap.get("field").toString();
                String operator = ruleMap.get("operator").toString();
                String compareValue = ruleMap.get("value").toString();
                Object tempValue = evaluate(fieldId);
                String value = (tempValue != null)?tempValue.toString():"";
                
                boolean result = false;
                if (value != null && !value.isEmpty()) {
                    if ("=".equals(operator) && compareValue.equals(value)) {
                        result = true;
                    } else if ("<>".equals(operator) && !compareValue.equals(value)) {
                        result = true;
                    } else if (">".equals(operator) || "<".equals(operator) || ">=".equals(operator) || "<=".equals(operator)) {
                        try {
                            double valueNum = Double.parseDouble(value);
                            double compareValueNum = Double.parseDouble(compareValue);
                            
                            if (">".equals(operator) && valueNum > compareValueNum) {
                                result = true;
                            } else if ("<".equals(operator) && valueNum < compareValueNum) {
                                result = true;
                            } else if (">=".equals(operator) && valueNum >= compareValueNum) {
                                result = true;
                            } else if ("<=".equals(operator) && valueNum <= compareValueNum) {
                                result = true;
                            }
                        } catch (NumberFormatException e) {}
                    } else if ("LIKE".equals(operator) && value.toLowerCase().contains(compareValue.toLowerCase())) {
                        result = true;
                    } else if ("NOT LIKE".equals(operator) && !value.toLowerCase().contains(compareValue.toLowerCase())) {
                        result = true;
                    } else if ("IN".equals(operator) || "NOT IN".equals(operator)) {
                        String[] compareValues = compareValue.split(";");
                        List<String> compareValuesList = new ArrayList<String>(Arrays.asList(compareValues));
                        if ("IN".equals(operator) && compareValuesList.contains(value)) {
                            result = true;
                        } else if ("NOT IN".equals(operator) && !compareValuesList.contains(value)) {
                            result = true;
                        }
                    } else if ("IS TRUE".equals(operator) || "IS FALSE".equals(operator)) {
                        try {
                            boolean valueBoolean = Boolean.parseBoolean(value);
                            
                            if ("IS TRUE".equals(operator) && valueBoolean) {
                                result = true;
                            } else if ("IS FALSE".equals(operator) && !valueBoolean) {
                                result = true;
                            }
                        } catch(Exception e) {}
                    } else if ("IS NOT EMPTY".equals(operator)) {
                        result = true;
                    }
                } else if ("IS EMPTY".equals(operator)) {
                    result = true;
                }
                
                if ("AND".equals(join)) {
                    visible = visible && result;
                } else if ("OR".equals(join)){
                    visible = visible || result; 
                }
            }
        }
        
        return visible;
    }
    
    protected boolean isRenderHtml(DataListColumn column) {
        if (column != null && column.isRenderHtml() != null) {
            return column.isRenderHtml();
        } else {
            return listRenderHtml;
        }
    }
}
