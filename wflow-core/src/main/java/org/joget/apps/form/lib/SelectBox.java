package org.joget.apps.form.lib;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormAjaxOptionsElement;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;

public class SelectBox extends Element implements FormBuilderPaletteElement, FormAjaxOptionsElement {
    private Element controlElement;
    
    public String getName() {
        return "Select Box";
    }

    public String getVersion() {
        return "5.1.0";
    }

    public String getDescription() {
        return "Select Box Element";
    }

    /**
     * Returns the option key=value pairs for this select box.
     * @param formData
     * @return
     */
    @SuppressWarnings("rawtypes")
	public Collection<Map> getOptionMap(FormData formData) {
        Collection<Map> optionMap = FormUtil.getElementPropertyOptionsMap(this, formData);
        return optionMap;
    }
    
    @Override
    public FormData formatDataForValidation(FormData formData) {
        String[] paramValues = FormUtil.getRequestParameterValues(this, formData);
        if ((paramValues == null || paramValues.length == 0) && FormUtil.isFormSubmitted(this, formData)) {
            String paramName = FormUtil.getElementParameterName(this);
            formData.addRequestParameterValues(paramName, new String[]{""});
        }
        return formData;
    }

    @Override
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = null;

        // get value
        String id = getPropertyString(FormUtil.PROPERTY_ID);
        if (id != null) {
            String[] values = FormUtil.getElementPropertyValues(this, formData);
            if (values != null && values.length > 0) {
                // check for empty submission via parameter
                String[] paramValues = FormUtil.getRequestParameterValues(this, formData);
                if ((paramValues == null || paramValues.length == 0) && FormUtil.isFormSubmitted(this, formData)) {
                    values = new String[]{""};
                }

                // formulate values
                String delimitedValue = FormUtil.generateElementPropertyValues(values);

                // set value into Properties and FormRowSet object
                FormRow result = new FormRow();
                result.setProperty(id, delimitedValue);
                rowSet = new FormRowSet();
                rowSet.add(result);
            }
            
        	// remove duplicate based on label (because list is sorted by label by default)
            if("true".equals(getProperty("removeDuplicates")) && rowSet != null) {
            	FormRowSet newResults = new FormRowSet();
            	String currentValue = null;
            	for(FormRow row : rowSet) {
            		String label = row.getProperty(FormUtil.PROPERTY_LABEL);
            		if(currentValue == null || !currentValue.equals(label)) {
            			currentValue = label;
            			newResults.add(row);
            		}
            	}
            	
            	rowSet = newResults;
            }
        }

        return rowSet;
    }

    @SuppressWarnings("unchecked")
	@Override
    public String renderTemplate(FormData formData, @SuppressWarnings("rawtypes") Map dataModel) {
        String template = "selectBox.ftl";
        
        dynamicOptions(formData);

        // set value
        String[] valueArray = FormUtil.getElementPropertyValues(this, formData);
        List<String> values = Arrays.asList(valueArray);
        dataModel.put("values", values);

        // set options
        @SuppressWarnings("rawtypes")
		Collection<Map> optionMap = getOptionMap(formData);
        dataModel.put("options", optionMap);

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getFormBuilderTemplate() {
        return "<label class='label'>Select Box</label><select><option>Option</option></select>";
    }

    public String getLabel() {
        return "Select Box";
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/selectBox.json", null, true, "message/form/SelectBox");
    }

    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_GENERAL;
    }

    public int getFormBuilderPosition() {
        return 300;
    }

    public String getFormBuilderIcon() {
        return null;
    }
    
    protected void dynamicOptions(FormData formData) {
        if (getControlElement(formData) != null) {
            setProperty("controlFieldParamName", FormUtil.getElementParameterName(getControlElement(formData)));
            
            FormUtil.setAjaxOptionsElementProperties(this, formData);
        }
    }

    public Element getControlElement(FormData formData) {
        if (controlElement == null) {
            if (getPropertyString("controlField") != null && !getPropertyString("controlField").isEmpty()) {
                Form form = FormUtil.findRootForm(this);
                controlElement = FormUtil.findElement(getPropertyString("controlField"), form, formData);
            }
        }
        return controlElement;
    }
}

