<%@page import="org.joget.apps.userview.model.UserviewMenu"%>
<%@page import="org.joget.apps.app.service.AppUtil"%>
<%@page import="org.springframework.util.StopWatch"%>
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>
<%@ page import="org.joget.apps.app.service.MobileUtil"%>
<%@ page import="org.joget.apps.app.service.AppUtil"%>
<%@ page import="org.joget.apps.userview.model.Userview"%>
<%@ page contentType="text/html" pageEncoding="utf-8"%>

<%
if (!MobileUtil.isMobileDisabled() && MobileUtil.isMobileUserAgent(request)) {
    pageContext.setAttribute("mobileUserAgent", Boolean.TRUE);
}
%>
<c:set var="mobileViewDisabled" value="${userview.setting.properties.mobileViewDisabled}"/>
<c:if test="${mobileUserAgent && !mobileViewDisabled && (empty cookie['desktopSite'].value || cookie['desktopSite'].value != 'true')}">
    <c:redirect url="/web/mobile/${appId}/${userview.properties.id}/${key}"/>
</c:if>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
    
    StopWatch sw = new StopWatch(request.getRequestURI());
    sw.start("userview");
%>
<% response.setHeader("P3P", "CP=\"This is not a P3P policy\""); %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<c:set var="qs"><ui:decodeurl value="${queryString}"/></c:set>
<c:if test="${empty menuId && !empty userview.properties.homeMenuId}">
    <c:set var="homeRedirectUrl" scope="request" value="/web/"/>
    <c:if test="${embed}">
        <c:set var="homeRedirectUrl" scope="request" value="${homeRedirectUrl}embed/"/>
    </c:if>
    <c:if test="${empty key}">
        <c:set var="key" scope="request" value="<%= Userview.USERVIEW_KEY_EMPTY_VALUE %>"/>
    </c:if>
    <c:set var="homeRedirectUrl" scope="request" value="${homeRedirectUrl}userview/${appId}/${userview.properties.id}/${key}/${userview.properties.homeMenuId}"/>
    <c:redirect url="${homeRedirectUrl}?${qs}"/>
</c:if>

<c:set var="isAnonymous" value="<%= WorkflowUtil.isCurrentUserAnonymous() %>"/>
<c:if test="${((!empty userview.setting.permission && !userview.setting.permission.authorize) || empty userview.current) && isAnonymous}">
    <c:set var="redirectUrl" scope="request" value="/web/"/>
    <c:choose>
        <c:when test="${embed}">
            <c:set var="redirectUrl" scope="request" value="${redirectUrl}embed/ulogin/${appId}/${userview.properties.id}/"/>
        </c:when>
        <c:otherwise>
            <c:set var="redirectUrl" scope="request" value="${redirectUrl}ulogin/${appId}/${userview.properties.id}/"/>
        </c:otherwise>
    </c:choose>
    <c:choose>
        <c:when test="${!empty key}">
            <c:set var="redirectUrl" scope="request" value="${redirectUrl}${key}"/>
        </c:when>
        <c:otherwise>
            <c:set var="key" scope="request" value="<%= Userview.USERVIEW_KEY_EMPTY_VALUE %>"/>
            <c:if test="${!empty menuId}">
                <c:set var="redirectUrl" scope="request" value="${redirectUrl}${key}"/>
            </c:if>    
        </c:otherwise>    
    </c:choose>
    <c:if test="${!empty menuId}">
        <c:set var="redirectUrl" scope="request" value="${redirectUrl}/${menuId}"/>
    </c:if>
    <c:redirect url="${redirectUrl}?${qs}"/>
</c:if>

<c:if test="${empty key}">
    <c:set var="key" scope="request" value="<%= Userview.USERVIEW_KEY_EMPTY_VALUE %>"/>
</c:if>

<c:set var="bodyId" scope="request" value=""/>
<c:choose>
    <c:when test="${!empty userview.setting.permission && !userview.setting.permission.authorize}">
        <c:set var="bodyId" scope="request" value="unauthorize"/>
    </c:when>
    <c:when test="${!empty userview.current}">
        <c:choose>
            <c:when test="${!empty userview.current.properties.customId}">
                <c:set var="bodyId" scope="request" value="${userview.current.properties.customId}"/>
            </c:when>
            <c:otherwise>
                <c:set var="bodyId" scope="request" value="${userview.current.properties.id}"/>
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise>
        <c:set var="bodyId" scope="request" value="pageNotFound"/>
    </c:otherwise>
</c:choose>

<c:catch var="bodyError">
<c:set var="bodyContent">
    <c:choose>
        <c:when test="${!empty userview.current}">
            <c:set var="isQuickEditEnabled" value="<%= AppUtil.isQuickEditEnabled() %>"/>
            <c:if test="${isQuickEditEnabled}">
            <div class="quickEdit" style="display: none">
                <a href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/userview/builder/${userview.properties.id}?menuId=${userview.current.properties.id}" target="_blank"><i class="icon-edit"></i> <fmt:message key="adminBar.label.page"/>: <c:out value="${userview.current.properties.label}"/></a>
            </div>            
            </c:if>
            <c:set var="properties" scope="request" value="${userview.current.properties}"/>
            <c:set var="requestParameters" scope="request" value="${userview.current.requestParameters}"/>
            <c:set var="readyJspPage" value="${userview.current.readyJspPage}"/>
            <c:choose>
                <c:when test="${!empty readyJspPage}">
                    <jsp:include page="../${readyJspPage}" flush="true"/>
                </c:when>
                <c:otherwise>
                    ${userview.current.readyRenderPage}
                </c:otherwise>
            </c:choose>
        </c:when>
        <c:otherwise>
            <h3><fmt:message key="ubuilder.pageNotFound"/></h3>

            <fmt:message key="ubuilder.pageNotFound.message"/>
            <br><br>
            <fmt:message key="ubuilder.pageNotFound.explanation"/>
            <p>&nbsp;</p>
            <p>&nbsp;</p>
            <p>
                <a href="${pageContext.request.contextPath}/web/userview/${appId}/${userview.properties.id}/<c:out value="${key}"/>/${userview.properties.homeMenuId}"><fmt:message key="ubuilder.pageNotFound.backToMain"/></a>
            </p>
        </c:otherwise>
    </c:choose>
</c:set>

<c:set var="alertMessageProperty" value="<%= UserviewMenu.ALERT_MESSAGE_PROPERTY %>"/>
<c:set var="alertMessageValue" value="${userview.current.properties[alertMessageProperty]}"/>
<c:set var="redirectUrlProperty" value="<%= UserviewMenu.REDIRECT_URL_PROPERTY %>"/>
<c:set var="redirectUrlValue" value="${userview.current.properties[redirectUrlProperty]}"/>
<c:set var="redirectParentProperty" value="<%= UserviewMenu.REDIRECT_PARENT_PROPERTY %>"/>
<c:set var="redirectParentValue" value="${userview.current.properties[redirectParentProperty]}"/>
<c:choose>
<c:when test="${!empty alertMessageValue}">
    <script>
        alert("<ui:escape value="${alertMessageValue}" format="javascript"/>");
    <c:if test="${!empty redirectUrlValue}">
        <c:if test="${redirectParentValue}">parent.</c:if>location.href = "${redirectUrlValue}";
    </c:if>
    </script>
</c:when>
<c:when test="${!empty redirectUrlValue}">
    <c:choose>
        <c:when test="${redirectParentValue}">
            <script>
                parent.location.href = "${redirectUrlValue}";
            </script>
        </c:when>
        <c:otherwise>
            <c:if test="${!fn:containsIgnoreCase(redirectUrlValue, 'http') && !fn:startsWith(redirectUrlValue, '/')}">
                <c:set var="redirectBaseUrlValue" scope="request" value="/web/"/>
                <c:if test="${embed}">
                    <c:set var="redirectBaseUrlValue" scope="request" value="${redirectBaseUrlValue}embed/"/>
                </c:if>
                <c:set var="redirectBaseUrlValue" scope="request" value="${redirectBaseUrlValue}userview/${appId}/${userview.properties.id}/${key}/"/>
                <c:set var="redirectUrlValue" value="${redirectBaseUrlValue}${redirectUrlValue}"/>
            </c:if>
            <c:if test="${fn:startsWith(redirectUrlValue, pageContext.request.contextPath)}">
                <c:set var="redirectUrlValue" value="${fn:substring(redirectUrlValue, fn:length(pageContext.request.contextPath), fn:length(redirectUrlValue))}"/>
            </c:if>
            <c:redirect url="${redirectUrlValue}"/>
        </c:otherwise>
    </c:choose>
</c:when>
</c:choose>
</c:catch>
            
<c:choose>
    <c:when test="${userview.setting.theme.name == 'Kecak-Ace Theme'}"> 
        <!--HTML Baru-->
        <html>
            <head>
                <meta charset="utf-8" />
                <title>
                    <c:set var="html">
                        ${userview.properties.name} &nbsp;&gt;&nbsp;
                        <c:if test="${!empty userview.current}">
                            ${userview.current.properties.label}
                        </c:if>
                    </c:set>
                    <ui:stripTag html="${html}"/>    
                </title>
                <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />
                <script src='${pageContext.request.contextPath}/js/jquery-2.0.3.min.js'></script>
                <script src='${pageContext.request.contextPath}/js/bootstrap/js/bootstrap.min.js'></script>
                <script src='${pageContext.request.contextPath}/js/ace/ace-elements.min.js'></script>
                <script src='${pageContext.request.contextPath}/js/ace/ace.min.js'></script>
                
                <script type="text/javascript">
                    function userviewPrint(){
                        $('head').append('<link id="userview_print_css" rel="stylesheet" href="${pageContext.request.contextPath}/css/userview_print.css" type="text/css" media="print"/>');
                        $('body').addClass("userview_print");
                        setTimeout("do_print()", 1000); 
                    }

                    function do_print(){
                        window.print();
                        $('body').removeClass("userview_print");
                        $('#userview_print_css').remove();
                    }

                    ${userview.setting.theme.javascript}
                    UI.base = "${pageContext.request.contextPath}";
                    UI.userview_app_id = '${appId}';
                    UI.userview_id = '${userview.properties.id}';
                </script>
                
                <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon_uv.ico"/>
                <link href="${pageContext.request.contextPath}/js/bootstrap/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
                <link href="${pageContext.request.contextPath}/js/bootstrap/css/bootstrap-responsive.min.css" rel="stylesheet" type="text/css"/>
                <link href="${pageContext.request.contextPath}/css/ace/ace.min.css" rel="stylesheet" type="text/css"/>
                <link href="${pageContext.request.contextPath}/css/ace/ace-responsive.min.css" rel="stylesheet" type="text/css"/>
                <link href="${pageContext.request.contextPath}/css/ace/ace-skins.min.css" rel="stylesheet" type="text/css"/>
                <link href="${pageContext.request.contextPath}/js/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css"/>
                <style type="text/css">
                    ${userview.setting.theme.css}
                    .label{
                        background-color:#FFF;
                    }
                    #submit{
                        background-color: #6fb3e0 !important;
                        border-color: #6fb3e0;
                        background-image: none !important;
                        border: 5px solid;
                        border-radius: 0;
                        box-shadow: none !important;
                        color: #fff !important;
                        cursor: pointer;
                        display: inline-block;
                        font-size: 14px;
                        line-height: 32px;
                        margin: 0;
                        padding: 0 12px 1px;
                        position: relative;
                        text-shadow: 0 -1px 0 rgba(0, 0, 0, 0.25) !important;
                        transition: all 0.15s ease 0s;
                        vertical-align: middle;
                    }
                </style>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
            </head>
            <body>
                <div class="navbar">
                    <div class="navbar-inner">
                        <div class="container-fluid">
                            <a href="#" class="brand">
                                <small>
                                    <i class="icon-leaf"></i>
                                    <c:choose>
                                        <c:when test="${!empty userview.setting.theme.header}">
                                            ${userview.setting.theme.header}
                                        </c:when>
                                        <c:otherwise>
                                            <ui:stripTag html="${userview.properties.name}" relaxed="true"/>
                                        </c:otherwise>
                                    </c:choose>
                                </small>
                            </a><!--/.brand-->
                            <ul class="nav ace-nav pull-right">
                                <li class="light-blue">
                                    <a data-toggle="dropdown" href="#" class="dropdown-toggle">
                                            <img class="nav-user-photo" src="${pageContext.request.contextPath}/images/avatars/avatar5.png" alt="Jason's Photo" />
                                            <span class="user-info">
                                                    <small>Welcome,</small>
                                                    Jason
                                            </span>

                                            <i class="icon-caret-down"></i>
                                    </a>

                                    <ul class="user-menu pull-right dropdown-menu dropdown-yellow dropdown-caret dropdown-closer">
                                            <li>
                                                    <a href="#">
                                                            <i class="icon-cog"></i>
                                                            Settings
                                                    </a>
                                            </li>

                                            <li>
                                                    <a href="#">
                                                            <i class="icon-user"></i>
                                                            Profile
                                                    </a>
                                            </li>

                                            <li class="divider"></li>

                                            <li>
                                                    <a href="#">
                                                            <i class="icon-off"></i>
                                                            Logout
                                                    </a>
                                            </li>
                                    </ul>
                                </li>
                            </ul><!--/.ace-nav-->
                        </div><!--/.container-fluid-->
                    </div><!--/.navbar-inner-->
                </div>
                <div class="main-container container-fluid">
                    <a class="menu-toggler" id="menu-toggler" href="#">
                        <span class="menu-text"></span>
                    </a>
                    <div class="sidebar" id="sidebar">
                        <div class="sidebar-shortcuts" id="sidebar-shortcuts">
                            <div class="sidebar-shortcuts-large" id="sidebar-shortcuts-large">
                                    <button class="btn btn-small btn-success">
                                            <i class="icon-signal"></i>
                                    </button>

                                    <button class="btn btn-small btn-info">
                                            <i class="icon-pencil"></i>
                                    </button>

                                    <button class="btn btn-small btn-warning">
                                            <i class="icon-group"></i>
                                    </button>

                                    <button class="btn btn-small btn-danger">
                                            <i class="icon-cogs"></i>
                                    </button>
                            </div>

                            <div class="sidebar-shortcuts-mini" id="sidebar-shortcuts-mini">
                                    <span class="btn btn-success"></span>

                                    <span class="btn btn-info"></span>

                                    <span class="btn btn-warning"></span>

                                    <span class="btn btn-danger"></span>
                            </div>
                        </div><!--#sidebar-shortcuts-->
                        <ul class="nav nav-list">
                            <c:forEach items="${userview.categories}" var="category" varStatus="cStatus">
                                <c:if test="${category.properties.hide ne 'yes'}">
                                    <c:set var="c_class" value=""/>
                                    <c:if test="${cStatus.first}">
                                        <c:set var="c_class" value="${c_class} first"/>
                                    </c:if>
                                    <c:if test="${cStatus.last}">
                                        <c:set var="c_class" value="${c_class} last"/>
                                    </c:if>
                                    <c:if test="${!empty userview.currentCategory && category.properties.id eq userview.currentCategory.properties.id}">
                                        <c:set var="c_class" value="${c_class} current-category"/>
                                    </c:if>
                                    <li>
                                        <a href="#" class="dropdown-toggle">
                                            <i class="icon-desktop"></i>
                                            <span class="menu-text"> <ui:stripTag html="${category.properties.label}" relaxed="true"/> </span>

                                            <b class="arrow icon-angle-down"></b>
                                        </a>
                                        <ul class="submenu">
                                            <c:forEach items="${category.menus}" var="menu" varStatus="mStatus">
                                                <c:set var="m_class" value=""/>
                                                <c:if test="${mStatus.first}">
                                                    <c:set var="m_class" value="${m_class} first"/>
                                                </c:if>
                                                <c:if test="${mStatus.last}">
                                                    <c:set var="m_class" value="${m_class} last"/>
                                                </c:if>
                                                <c:if test="${!empty userview.current && menu.properties.id eq userview.current.properties.id}">
                                                    <c:set var="m_class" value="${m_class} current"/>
                                                </c:if>
                                            <li>
                                                <a href="${menu.url}">
                                                    <i class="icon-double-angle-right"></i>
                                                    ${menu.menu}
                                                </a>
                                            </li>
                                            </c:forEach>
                                        </ul>    
                                    </li>
                                </c:if>
                            </c:forEach>
                        </ul><!--/.nav nav-list-->
                        <div class="sidebar-collapse" id="sidebar-collapse">
                            <i class="icon-double-angle-left"></i>
                        </div>
                    </div><!--/.sidebar-->
                    
                    <div class="main-content">
                        <div class="breadcrumbs" id="breadcrumbs">
                            <ul class="breadcrumb">
                                <li>
                                        <i class="icon-home home-icon"></i>
                                        <a href="#">Home</a>

                                        <span class="divider">
                                                <i class="icon-angle-right arrow-icon"></i>
                                        </span>
                                </li>

                                <li>
                                        <a href="#">Other Pages</a>

                                        <span class="divider">
                                                <i class="icon-angle-right arrow-icon"></i>
                                        </span>
                                </li>
                                <li class="active">Blank Page</li>
                            </ul><!--.breadcrumb-->

                            <div class="nav-search" id="nav-search">
                                <form class="form-search"/>
                                        <span class="input-icon">
                                            <input type="text" placeholder="Search ..." class="input-small nav-search-input" id="nav-search-input" autocomplete="off" />
                                            <i class="icon-search nav-search-icon"></i>
                                        </span>
                                </form>
                            </div><!--#nav-search-->
                        </div>
                        
                        <div class="page-content">
                            <div class="row-fluid">
                                <div class="span12">
                                        <!--PAGE CONTENT BEGINS-->
                                    <c:if test="${!empty userview.setting.theme.beforeContent}">
                                        ${userview.setting.theme.beforeContent}
                                    </c:if>
                                    ${bodyContent}
                                    <c:if test="${!empty bodyError}"><c:out value="${bodyError}" escapeXml="true"/></c:if>
                                    <c:if test="${!empty userview.setting.theme.pageBottom}">
                                        ${userview.setting.theme.pageBottom}
                                    </c:if>        
                                        <!--PAGE CONTENT ENDS-->
                                </div><!--/.span-->
                            </div><!--/.row-fluid-->
                        </div><!--/.page-content-->
                    </div><!--/.main-content-->
                </div><!--/.main-container-->
            </body>
        </html>
    </c:when>
    <c:otherwise>
    <!--Mulai HTLM ORI-->
    <html>
        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
            <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
            <title>
                <c:set var="html">
                    ${userview.properties.name} &nbsp;&gt;&nbsp;
                    <c:if test="${!empty userview.current}">
                        ${userview.current.properties.label}
                    </c:if>
                </c:set>
                <ui:stripTag html="${html}"/>    
            </title>

            <jsp:include page="/WEB-INF/jsp/includes/scripts.jsp" />

            <style type="text/css">
                .quickEdit, #form-canvas .quickEdit {
                    display: none;
                }
            </style>

            <script type="text/javascript">
                function userviewPrint(){
                    $('head').append('<link id="userview_print_css" rel="stylesheet" href="${pageContext.request.contextPath}/css/userview_print.css" type="text/css" media="print"/>');
                    $('body').addClass("userview_print");
                    setTimeout("do_print()", 1000); 
                }

                function do_print(){
                    window.print();
                    $('body').removeClass("userview_print");
                    $('#userview_print_css').remove();
                }

                ${userview.setting.theme.javascript}
                UI.base = "${pageContext.request.contextPath}";
                UI.userview_app_id = '${appId}';
                UI.userview_id = '${userview.properties.id}';
            </script>

            <link href="${pageContext.request.contextPath}/css/userview.css?build=<fmt:message key="build.number"/>" rel="stylesheet" type="text/css" />
            <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon_uv.ico"/>
            <style type="text/css">
                ${userview.setting.theme.css}
            </style>
        </head>

        <body id="${bodyId}" class="<c:if test="${embed}">embeded</c:if><c:if test="${rightToLeft == 'true' || fn:startsWith(currentLocale, 'ar') == true}"> rtl</c:if>">
            <div id="page">
                <div id="header">

                    <c:choose>
                        <c:when test="${!empty userview.setting.theme.header}">
                            <div id="header-inner">${userview.setting.theme.header}</div>
                        </c:when>
                        <c:otherwise>
                            <div id="header-info">
                                <div id="header-name">
                                    <a href="${pageContext.request.contextPath}/web/userview/${appId}/${userview.properties.id}/<c:out value="${key}"/>/${userview.properties.homeMenuId}" id="header-link"><span id="name"><ui:stripTag html="${userview.properties.name}" relaxed="true"/></span></a>
                                </div>
                                <div id="header-description">
                                    <span id="description"><ui:stripTag html="${userview.properties.description}" relaxed="true"/></span>
                                </div>
                                <div class="clear"></div>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div id="header-message">
                        <div id="header-welcome-message">
                            <span id="welcomeMessage"><ui:stripTag html="${userview.properties.welcomeMessage}" relaxed="true"/></span>
                        </div>
                        <div id="header-logout-text">
                            <c:choose>
                                <c:when test="${isAnonymous}">
                                    <a href="${pageContext.request.contextPath}/web/ulogin/${appId}/${userview.properties.id}/<c:out value="${key}"/>"><span id="loginText"><fmt:message key="ubuilder.login"/></span></a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/j_spring_security_logout"><span id="logoutText"><ui:stripTag html="${userview.properties.logoutText}" relaxed="true"/></span></a>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="clear"></div>
                    </div>
                </div>
                <div id="main">
                    <c:choose>
                        <c:when test="${!empty userview.setting.permission && !userview.setting.permission.authorize}">
                            <div id="content" class="unauthorize">
                                <h3><fmt:message key="ubuilder.noAuthorize"/></h3>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <c:if test="${!empty userview.setting.theme.pageTop}">
                                ${userview.setting.theme.pageTop}
                            </c:if>
                            <c:if test="${!embed}">
                            <div id="navigation">
                                <c:if test="${isQuickEditEnabled}">
                                <div class="quickEdit" style="display: none">
                                    <a href="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/userview/builder/${userview.properties.id}" target="_blank"><i class="icon-edit"></i> <fmt:message key="adminBar.label.menu"/>: <c:out value="${userview.properties.name}"/></a>
                                </div>
                                </c:if>
                                <div id="category-container">
                                    <c:forEach items="${userview.categories}" var="category" varStatus="cStatus">
                                        <c:if test="${category.properties.hide ne 'yes'}">
                                            <c:set var="c_class" value=""/>

                                            <c:if test="${cStatus.first}">
                                                <c:set var="c_class" value="${c_class} first"/>
                                            </c:if>
                                            <c:if test="${cStatus.last}">
                                                <c:set var="c_class" value="${c_class} last"/>
                                            </c:if>
                                            <c:if test="${!empty userview.currentCategory && category.properties.id eq userview.currentCategory.properties.id}">
                                                <c:set var="c_class" value="${c_class} current-category"/>
                                            </c:if>

                                            <div id="${category.properties.id}" class="category ${c_class}">
                                                <div class="category-label">
                                                    <c:set var="firstMenuItem" value="${category.menus[0]}"/>
                                                    <c:choose>
                                                        <c:when test="${!empty firstMenuItem && firstMenuItem.homePageSupported}">
                                                            <c:set var="menuItemId" value="${firstMenuItem.properties.menuId}"/>
                                                            <a href="${firstMenuItem.url}"><span><ui:stripTag html="${category.properties.label}" relaxed="true"/></span></a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span><ui:stripTag html="${category.properties.label}" relaxed="true"/></span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <div class="clear"></div>
                                                <div class="menu-container">
                                                    <c:forEach items="${category.menus}" var="menu" varStatus="mStatus">
                                                        <c:set var="m_class" value=""/>

                                                        <c:if test="${mStatus.first}">
                                                            <c:set var="m_class" value="${m_class} first"/>
                                                        </c:if>
                                                        <c:if test="${mStatus.last}">
                                                            <c:set var="m_class" value="${m_class} last"/>
                                                        </c:if>
                                                        <c:if test="${!empty userview.current && menu.properties.id eq userview.current.properties.id}">
                                                            <c:set var="m_class" value="${m_class} current"/>
                                                        </c:if>

                                                        <div id="${menu.properties.id}" class="menu ${m_class}">
                                                            ${menu.menu}
                                                        </div>
                                                    </c:forEach>
                                                </div>
                                            </div>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </div>
                            </c:if>
                            <div id="content">
                                <c:if test="${!empty userview.setting.theme.beforeContent}">
                                    ${userview.setting.theme.beforeContent}
                                </c:if>
                                ${bodyContent}
                                <c:if test="${!empty bodyError}"><c:out value="${bodyError}" escapeXml="true"/></c:if>
                                <c:if test="${!empty userview.setting.theme.pageBottom}">
                                    ${userview.setting.theme.pageBottom}
                                </c:if>
                            </div>
                        </c:otherwise>
                    </c:choose>
                    <div class="clear"></div>
                </div>
                <div id="footer">

                    <c:choose>
                        <c:when test="${!empty userview.setting.theme.footer}">
                            <div id="footer-inner">${userview.setting.theme.footer}</div>
                        </c:when>
                        <c:otherwise>
                            <div id="footer-message">
                                <span id="footerMessage"><ui:stripTag html="${userview.properties.footerMessage}" relaxed="true"/></span>
                            </div>
                        </c:otherwise>
                    </c:choose>

                </div>
            </div>
            <script type="text/javascript">
                HelpGuide.base = "${pageContext.request.contextPath}"
                HelpGuide.attachTo = "#header";
                HelpGuide.key = "help.web.userview.${appId}.${userview.properties.id}.${bodyId}";
                HelpGuide.show();
            </script>

            <%= AppUtil.getSystemAlert() %>   

            <%
                sw.stop();
                long duration = sw.getTotalTimeMillis();
                pageContext.setAttribute("duration", duration);
            %>    
            <%--div class="small">[${duration}ms]</div--%>

            <jsp:include page="/WEB-INF/jsp/console/apps/adminBar.jsp" flush="true">
                <jsp:param name="appId" value="${appId}"/>
                <jsp:param name="appVersion" value="${appVersion}"/>
                <jsp:param name="userviewId" value="${userview.properties.id}"/>
            </jsp:include>    
        </body>

    </html>
    <!--END Mulai HTLM ORI-->
    </c:otherwise>
</c:choose>