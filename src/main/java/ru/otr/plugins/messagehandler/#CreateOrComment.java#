package ru.otr.plugins.messagehandler;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugins.mail.handlers.AbstractMessageHandler;
import com.atlassian.jira.plugins.mail.handlers.FullCommentHandler;
import com.atlassian.jira.plugins.mail.handlers.NonQuotedCommentHandler;
import com.atlassian.jira.service.util.ServiceUtils;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
import com.atlassian.mail.MailUtils;
import java.io.IOException;
import java.util.Map;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;

public class CreateOrComment extends AbstractMessageHandler {
    public String projectKey;
    public String issueType;
    public String stripquotes;
    private MessageHandlerErrorCollector monitor;
    public static final String KEY_PROJECT = "project";
    public static final String KEY_ISSUETYPE = "issuetype";
    public static final String KEY_QUOTES = "stripquotes";
    private static final String FALSE = "false";
    
    public CreateOrComment() {
    }
    
    @Override
    public void init(Map<String, String> params, MessageHandlerErrorCollector monitor) {
        super.init(params, monitor);
        this.monitor = monitor;
    }
 
    @Override
    public boolean handleMessage(Message message, MessageHandlerContext context) throws MessagingException {
        String subject = message.getSubject();
        Issue issue = ServiceUtils.findIssueObjectInString(subject);
        
        if (issue == null) {
            // If we cannot find the issue from the subject of the e-mail message
            // try finding the issue using the in-reply-to message id of the e-mail message
            issue = getAssociatedIssue(message);
        }

        if (issue != null) {
            boolean doDelete = false;

            // add the message as a comment to the issue
            if ((stripquotes == null) || FALSE.equalsIgnoreCase(stripquotes)) { // if stripquotes not defined in setup
                FullCommentHandler fc = new FullCommentHandler();
                fc.init(params, monitor);
                doDelete = fc.handleMessage(message, context); // get message with quotes
            } else {
                NonQuotedCommentHandler nq = new NonQuotedCommentHandler();
                nq.init(params, monitor);
                doDelete = nq.handleMessage(message, context); //get message without quotes
            }
            return doDelete;
        } else { // no issue found, so create new issue in default project
            AdvCreateIssueHandler createIssueHandler = new AdvCreateIssueHandler();
            createIssueHandler.init(params, monitor);
            return createIssueHandler.handleMessage(message, context);
        }
        
    }

    @Override
    protected boolean attachPlainTextParts(Part part) throws MessagingException, IOException {
        return false;
    }

    @Override
    protected boolean attachHtmlParts(Part part) throws MessagingException, IOException {
        return !MailUtils.isContentEmpty(part);
    }
}
