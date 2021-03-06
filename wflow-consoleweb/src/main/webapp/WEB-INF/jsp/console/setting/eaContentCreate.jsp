<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

    <div id="main-body-header">
        <fmt:message key="console.setting.eaContent.create.label.title"/>
    </div>

    <div id="main-body-content">
        <form:form id="createEAContent" action="${pageContext.request.contextPath}/web/console/setting/eaContent/submit/create" method="POST" commandName="eaContent" cssClass="form">
            <form:errors path="*" cssClass="form-errors"/>
            <c:if test="${!empty errors}">
                <span class="form-errors" style="display:block">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error}"/>
                    </c:forEach>
                </span>
            </c:if>
            <fieldset>
                <legend><fmt:message key="console.setting.eaContent.common.label.details"/></legend>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.setting.eaContent.common.label.processId"/></label>
                    <span class="form-input"><form:input path="processId" cssErrorClass="form-input-error" size="30"/> *</span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.setting.eaContent.common.label.activityId"/></label>
                    <span class="form-input"><form:input path="activityId" cssErrorClass="form-input-error" size="30" /> *</span>
                </div>
                <div class="form-row">
                    <label for="field1"><fmt:message key="console.setting.eaContent.common.label.content"/></label>
                    <span class="form-input"><form:textarea path="content" cssErrorClass="form-input-error" rows="10" cols="40"/> *</span>
                </div>
            </fieldset>
            <div class="form-buttons">
                <input class="form-button" type="button" value="<fmt:message key="general.method.label.save"/>"  onclick="validateField()"/>
                <input class="form-button" type="button" value="<fmt:message key="general.method.label.cancel"/>" onclick="closeDialog()"/>
            </div>
        </form:form>
    </div>

    <script type="text/javascript">
        function validateField(){
        	var processIdMatch = /^[0-9a-zA-Z:#_-]+$/.test($("#processId").attr("value"));
        	var activityIdMatch = /^[0-9a-zA-Z:#_-]+$/.test($("#activityId").attr("value"));
        	
            if(!processIdMatch && activityIdMatch){
                var alertString = '<fmt:message key="console.setting.eaContent.error.label.processIdInvalid"/>';
                $("#processId").focus();
                alert(alertString);
            } else if (processIdMatch && !activityIdMatch) {
            	var alertString = '<fmt:message key="console.setting.eaContent.error.label.activityIdInvalid"/>';
                $("#activityId").focus();
                alert(alertString);
            } else if (!processIdMatch && !activityIdMatch) {
            	var alertString = '<fmt:message key="console.setting.eaContent.error.label.processIdAndActivityIdInvalid"/>';
            	$("#processId").focus();
                $("#activityId").focus();
                alert(alertString);
            } else{
                $("#createEAContent").submit();
            }
        }

        function closeDialog() {
            if (parent && parent.PopupDialog.closeDialog) {
                parent.PopupDialog.closeDialog();
            }
            return false;
        }
    </script>
<commons:popupFooter />
