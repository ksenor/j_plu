package ru.otr.plugins.messagehandler;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.service.util.handler.MessageHandler;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
import com.atlassian.jira.service.util.handler.MessageUserProcessor;
import com.atlassian.mail.MailUtils;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.lang.StringUtils;
import ru.otr.plugins.CFValidator;
import ru.otr.plugins.IssueKeyValidator;
import ru.otr.plugins.IssueTypeValidator;
import ru.otr.plugins.UserValidator;

public class CreateSubTask implements MessageHandler {
    private String issueKey;
    private String issueType;
    private String assigneeId;
    private String reporterId;
    private String cfEmail;
    private final IssueKeyValidator issueKeyValidator;
    private final IssueTypeValidator issueTypeValidator;
    private final UserValidator userValidator;
    private final CFValidator cfValidator;
    private final MessageUserProcessor messageUserProcessor;
    public static final String KEY_ISSUE_KEY = "issueKey";
    public static final String KEY_ISSUE_TYPE = "issueType";
    public static final String KEY_USER_ASSIGNEE = "assigneeId";
    public static final String KEY_USER_REPORTER = "reporterId";
    public static final String KEY_CF_EMAIL = "cfEmail";
 
    private SubTaskManager subTaskManager = ComponentManager.getInstance().getSubTaskManager();
    private CustomFieldManager cfm = ComponentManager.getInstance().getCustomFieldManager();
    
    public CreateSubTask(MessageUserProcessor messageUserProcessor, IssueKeyValidator issueKeyValidator, IssueTypeValidator issueTypeValidator, UserValidator userValidator, CFValidator cfValidator) {
        this.messageUserProcessor = messageUserProcessor;
        this.issueKeyValidator = issueKeyValidator;
        this.issueTypeValidator = issueTypeValidator;
        this.userValidator = userValidator;
        this.cfValidator = cfValidator;
    }
 
    @Override
    public void init(Map<String, String> params, MessageHandlerErrorCollector monitor) {
        issueKey = params.get(KEY_ISSUE_KEY);
        issueType = params.get(KEY_ISSUE_TYPE);
        assigneeId = params.get(KEY_USER_ASSIGNEE);
        reporterId = params.get(KEY_USER_REPORTER);
        cfEmail = params.get(KEY_CF_EMAIL);
        if (StringUtils.isBlank(issueKey)) {
            monitor.error("Issue key has not been specified ('" + KEY_ISSUE_KEY + "' parameter). This handler will not work correctly.");
        }
        if (StringUtils.isBlank(issueType)) {
            monitor.error("Issue type has not been specified ('" + KEY_ISSUE_TYPE + "' parameter). This handler will not work correctly.");
        }
        
        issueTypeValidator.validateType(issueType, monitor);
        issueKeyValidator.validateIssue(issueKey, monitor);
        if (!StringUtils.isBlank(assigneeId))
            userValidator.validateUser(assigneeId, monitor);
        
        if (!StringUtils.isBlank(reporterId))
            userValidator.validateUser(reporterId, monitor);
        
        if (!StringUtils.isBlank(cfEmail))
            cfValidator.validateCf(cfEmail, monitor);
    }
 
    @Override
    public boolean handleMessage(Message message, MessageHandlerContext context) throws MessagingException {
        final Issue issue = issueKeyValidator.validateIssue(issueKey, context.getMonitor());
        final String issuetype = issueTypeValidator.validateType(issueType, context.getMonitor());
        final CustomField email = cfValidator.validateCf(cfEmail, context.getMonitor());
        
        if (issue == null) {
            return false;
        }
        User sender = messageUserProcessor.getAuthorFromSender(message);
        if (sender == null) {
            if (!StringUtils.isBlank(reporterId))
                sender = userValidator.validateUser(reporterId, context.getMonitor());
            if (sender == null) {
            context.getMonitor().error("Message sender(s) '" + StringUtils.join(MailUtils.getSenders(message), ",")
                    + "' do not have corresponding users in JIRA. Message will be ignored");
            return false;
            }
        }
                
        final String body = MailUtils.getBody(message);
        
        MutableIssue issueObject = ComponentManager.getInstance().getIssueFactory().getIssue();
        issueObject.setProjectId(issue.getProjectObject().getId());
        issueObject.setIssueTypeId(issuetype);
        issueObject.setSummary(message.getSubject());
        if (!StringUtils.isBlank(assigneeId)) {
            User assignee = userValidator.validateUser(assigneeId, context.getMonitor());
            if (assignee != null)
                issueObject.setAssignee(assignee);
        } else {
            issueObject.setAssignee(sender);
        }
        issueObject.setReporter(sender);
        issueObject.setParentObject(issue);
        
        if (body != null)
            issueObject.setDescription(StringUtils.abbreviate(body, 100000));
        
        // Set customfield
        System.out.println("Set email\n");
        if (email != null) {
            String from = InternetAddress.toString(message.getFrom());
            int start = from.indexOf("<");
            int end = from.indexOf(">");
            if (start > -1 && end > -1)
                from = from.substring(start + 1, end);
            System.out.println(from);
            issueObject.setCustomFieldValue(email, from);
            System.out.println("Set email ok");
        }
        
        try {
            Issue newSubTask = context.createIssue(sender, issueObject);
            subTaskManager.createSubTaskIssueLink(issue, newSubTask, sender);
        } catch (CreateException ex) {
            System.out.println(ex.toString());
            Logger.getLogger(CreateSubTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true; 
    }
}
