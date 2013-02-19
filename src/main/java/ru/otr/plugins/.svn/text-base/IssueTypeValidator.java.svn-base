package ru.otr.plugins;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
import org.apache.commons.lang.StringUtils;
 
public class IssueTypeValidator
{
    private final IssueTypeManager issueTypeManager;
 
    public IssueTypeValidator(IssueTypeManager issueTypeManager) {
        this.issueTypeManager = issueTypeManager;
    }
 
    public String validateType(String issueType, MessageHandlerErrorCollector collector) {
        if (StringUtils.isBlank(issueType)) {
            collector.error("Issue type cannot be undefined.");
            return null;
        }
 
        final IssueType type = issueTypeManager.getIssueType(issueType);
        if (type == null) {
            collector.error("Issuetype '" + issueType + "'. does not exist.");
            return null;
        } else {
            if (!type.isSubTask()) {
                collector.error("Issuetype '" + issueType + "'. does not subtask.");
                return null;
            }
        }
        
        return issueType;
    }
 
}