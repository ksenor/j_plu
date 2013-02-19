package ru.otr.plugins.messagehandler;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.plugins.mail.webwork.AbstractEditHandlerDetailsWebAction;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.services.file.AbstractMessageHandlingService;
import com.atlassian.jira.service.util.ServiceUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginAccessor;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import ru.otr.plugins.CFValidator;
import ru.otr.plugins.IssueKeyValidator;
import ru.otr.plugins.IssueTypeValidator;
import ru.otr.plugins.UserValidator;

public class EditCreateSubTask extends AbstractEditHandlerDetailsWebAction {
    private final IssueKeyValidator issueKeyValidator;
    private final IssueTypeValidator issueTypeValidator;
    private final UserValidator userValidator;
    private final CFValidator cfValidator;
 
    public EditCreateSubTask(PluginAccessor pluginAccessor, IssueKeyValidator issueKeyValidator, IssueTypeValidator issueTypeValidator, UserValidator userValidator, CFValidator cfValidator) {
        super(pluginAccessor);
        this.issueKeyValidator = issueKeyValidator;
        this.issueTypeValidator = issueTypeValidator;
        this.userValidator = userValidator;
        this.cfValidator = cfValidator;
        
    }
 
    private String issueKey;
    private String issueType;
    private String assigneeId;
    private String reporterId;
    private String cfEmail;
 
    public String getIssueKey() {
        return issueKey;
    }
    
    public String getIssueType() {
        return issueType;
    }
    
    public String getAssigneeId() {
        return assigneeId;
    }
    
    public String getReporterId() {
        return reporterId;
    }
    
    public String getCfEmail() {
        return cfEmail;
    }
    
    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    }
    
    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }
    
    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }
    
    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }
    
    public void setCfEmail(String cfEmail) {
        this.cfEmail = cfEmail;
    }
    
    @Override
    protected void copyServiceSettings(JiraServiceContainer jiraServiceContainer) throws ObjectConfigurationException {
        final String params = jiraServiceContainer.getProperty(AbstractMessageHandlingService.KEY_HANDLER_PARAMS);
        final Map<String, String> parameterMap = ServiceUtils.getParameterMap(params);
        issueKey = parameterMap.get(CreateSubTask.KEY_ISSUE_KEY);
        issueType = parameterMap.get(CreateSubTask.KEY_ISSUE_TYPE);
        assigneeId = parameterMap.get(CreateSubTask.KEY_USER_ASSIGNEE);
        reporterId = parameterMap.get(CreateSubTask.KEY_USER_REPORTER);
        cfEmail = parameterMap.get(CreateSubTask.KEY_CF_EMAIL);
    }
 
 
    @Override
    protected Map<String, String> getHandlerParams() {
        MapBuilder mapBuilder = MapBuilder.newBuilder();
        mapBuilder.add(CreateSubTask.KEY_ISSUE_KEY, issueKey);
        mapBuilder.add(CreateSubTask.KEY_ISSUE_TYPE, issueType);
        mapBuilder.add(CreateSubTask.KEY_USER_ASSIGNEE, assigneeId);
        mapBuilder.add(CreateSubTask.KEY_USER_REPORTER, reporterId);
        mapBuilder.add(CreateSubTask.KEY_CF_EMAIL, cfEmail);
        return mapBuilder.toMap();
    }
 
    @Override
    protected void doValidation() {
        if (configuration == null) {
            return;
        }
        super.doValidation();
        issueKeyValidator.validateIssue(issueKey, new WebWorkErrorCollector());
        issueTypeValidator.validateType(issueType, new WebWorkErrorCollector());
        if (!StringUtils.isBlank(assigneeId)) {
            userValidator.validateUser(assigneeId, new WebWorkErrorCollector());
        }
        
        if (!StringUtils.isBlank(reporterId)) {
            userValidator.validateUser(reporterId, new WebWorkErrorCollector());
        }
    
        if (!StringUtils.isBlank(cfEmail)) {
            cfValidator.validateCf(cfEmail, new WebWorkErrorCollector());
        }
    }
}
