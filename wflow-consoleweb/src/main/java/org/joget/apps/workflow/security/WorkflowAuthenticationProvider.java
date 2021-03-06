package org.joget.apps.workflow.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class WorkflowAuthenticationProvider implements AuthenticationProvider, MessageSourceAware {

    transient
    @Autowired
    @Qualifier("main")
    private DirectoryManager directoryManager;
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    public DirectoryManager getDirectoryManager() {
        return directoryManager;
    }

    public void setDirectoryManager(DirectoryManager directoryManager) {
        this.directoryManager = directoryManager;
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // reset profile and set hostname
        HostManager.initHost();
            
        // Determine username
        String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();
        String password = authentication.getCredentials().toString();

        // check credentials
        boolean validLogin = false;
        try {
            validLogin = directoryManager.authenticate(username, password);
        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage());
        }
        if (!validLogin) {
            LogUtil.info(getClass().getName(), "Authentication for user " + username + ": " + false);
            WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
            workflowHelper.addAuditTrail(this.getClass().getName(), "authenticate", "Authentication for user " + username + ": " + false, new Class[]{String.class}, new Object[]{username}, false);
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }

        // add audit trail
        LogUtil.info(getClass().getName(), "Authentication for user " + username + ": " + true);
        WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
        workflowHelper.addAuditTrail(this.getClass().getName(), "authenticate", "Authentication for user " + username + ": " + true, new Class[]{String.class}, new Object[]{username}, true);

        // get authorities
        Collection<Role> roles = directoryManager.getUserRoles(username);
        List<GrantedAuthority> gaList = new ArrayList<GrantedAuthority>();
        if (roles != null && !roles.isEmpty()) {
            for (Role role : roles) {
                GrantedAuthority ga = new SimpleGrantedAuthority(role.getId());
                gaList.add(ga);
            }
        }

        // return result
        User user = directoryManager.getUserByUsername(username);
        UserDetails details = new WorkflowUserDetails(user);
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(details, password, gaList);
        result.setDetails(details);
        return result;
    }

    public boolean supports(@SuppressWarnings("rawtypes") Class authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }
}
