package ru.otr.plugins.messagehandler;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.plugins.mail.webwork.AbstractEditHandlerDetailsWebAction;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.services.file.AbstractMessageHandlingService;
import com.atlassian.jira.service.util.ServiceUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginAccessor;
import java.util.Map;
import ru.otr.plugins.IssueKeyValidator;
import ru.otr.plugins.UserValidator;

public class EditAddCommentToIssue extends AbstractEditHandlerDetailsWebAction {
    private final IssueKeyValidator issueKeyValidator;
    private final UserValidator userValidator;
 
    public EditAddCommentToIssue(PluginAccessor pluginAccessor, IssueKeyValidator issueKeyValidator, UserValidator userValidator) {
        super(pluginAccessor);
        this.issueKeyValidator = issueKeyValidator;
        this.userValidator = userValidator;     
    }
 
    private String issueKey;
    private String reporterId;
 
    public String getIssueKey() {
        return issueKey;
    }
    
    public String getReporterId() {
        return reporterId;
    }
    
    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    }
    
    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }
    
    @Override
    protected void copyServiceSettings(JiraServiceContainer jiraServiceContainer) throws ObjectConfigurationException {
        final String params = jiraServiceContainer.getProperty(AbstractMessageHandlingService.KEY_HANDLER_PARAMS);
        final Map<String, String> parameterMap = ServiceUtils.getParameterMap(params);
        issueKey = parameterMap.get(AddCommentToIssue.KEY_ISSUE_KEY);
        reporterId = parameterMap.get(AddCommentToIssue.KEY_USER_REPORTER);
    }
 
 
    @Override
    protected Map<String, String> getHandlerParams() {
        return MapBuilder.build(AddCommentToIssue.KEY_ISSUE_KEY, issueKey, AddCommentToIssue.KEY_USER_REPORTER, reporterId);
    }
 
 
    @Override
    protected void doValidation() {
        if (configuration == null) {
            return;
        }
        super.doValidation();
        issueKeyValidator.validateIssue(issueKey, new WebWorkErrorCollector());
        userValidator.validateUser(reporterId, new WebWorkErrorCollector());
    }
}
