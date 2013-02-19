package ru.otr.plugins.messagehandler;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.service.util.handler.MessageHandler;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
import com.atlassian.jira.service.util.handler.MessageUserProcessor;
import com.atlassian.mail.MailUtils;
import java.util.Map;
import javax.mail.Message;
import javax.mail.MessagingException;
import org.apache.commons.lang.StringUtils;
import ru.otr.plugins.IssueKeyValidator;
import ru.otr.plugins.UserValidator;

public class AddCommentToIssue implements MessageHandler {
    private String issueKey;
    private String reporterId;
    private final IssueKeyValidator issueKeyValidator;
    private final UserValidator userValidator;
    private final MessageUserProcessor messageUserProcessor;
    public static final String KEY_ISSUE_KEY = "issueKey";
    public static final String KEY_USER_REPORTER = "reporterId";
 
    public AddCommentToIssue(MessageUserProcessor messageUserProcessor, IssueKeyValidator issueKeyValidator, UserValidator userValidator) {
        this.messageUserProcessor = messageUserProcessor;
        this.issueKeyValidator = issueKeyValidator;
        this.userValidator = userValidator;
    }
 
    @Override
    public void init(Map<String, String> params, MessageHandlerErrorCollector monitor) {
        issueKey = params.get(KEY_ISSUE_KEY);
        reporterId = params.get(KEY_USER_REPORTER);
        if (StringUtils.isBlank(issueKey)) {
            monitor.error("Issue key has not been specified ('" + KEY_ISSUE_KEY + "' parameter). This handler will not work correctly.");
        }
        issueKeyValidator.validateIssue(issueKey, monitor);
        if (!StringUtils.isBlank(reporterId)) { 
            userValidator.validateUser(reporterId, monitor);
        }
    }
 
    @Override
    public boolean handleMessage(Message message, MessageHandlerContext context) throws MessagingException {
        final Issue issue = issueKeyValidator.validateIssue(issueKey, context.getMonitor());
        if (issue == null) {
            return false;
        }
        User sender = messageUserProcessor.getAuthorFromSender(message);
        if (sender == null) {
            sender = userValidator.validateUser(reporterId, context.getMonitor());
            if (sender == null) {
            context.getMonitor().error("Message sender(s) '" + StringUtils.join(MailUtils.getSenders(message), ",")
                    + "' do not have corresponding users in JIRA. Message will be ignored");
            return false;
            }
        }
        
        final String body = MailUtils.getBody(message);
        final StringBuilder commentBody = new StringBuilder(message.getSubject());
        if (body != null) {
            commentBody.append("\n").append(StringUtils.abbreviate(body, 100000));
        }
        context.createComment(issue, sender, commentBody.toString(), false);
        return true; 
    }
}
