package ru.otr.plugins.messagehandler;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.plugins.mail.handlers.AbstractMessageHandler;
import com.atlassian.jira.plugins.mail.handlers.FullCommentHandler;
import com.atlassian.jira.plugins.mail.handlers.NonQuotedCommentHandler;
import com.atlassian.jira.service.util.ServiceUtils;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
import com.atlassian.jira.service.util.handler.MessageHandlerExecutionMonitor;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.mail.MailUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Address;
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

    public static class MyMessageHandlerContext implements MessageHandlerContext {

        private ArrayList<String> helpdesk;
        MessageHandlerContext mhc;
        Message message;
        Address[] addresses;

        MyMessageHandlerContext(MessageHandlerContext mhc, Message message) {
            this.mhc = mhc;
            this.message = message;
            helpdesk = new ArrayList<String>(Arrays.asList(new String[]{"jira_test@otr.ru", "it@otr.ru", "helpdesk@otr.ru"}));
        }

        public User createUser(String string, String string1, String string2, String string3, Integer intgr) throws PermissionException, CreateException {
            return this.mhc.createUser(string, string1, string2, string3, intgr);
        }

        public Comment createComment(Issue issue, User user, String string, boolean bln) {
            return this.mhc.createComment(issue, user, string, bln);
        }

        public Issue createIssue(User user, Issue issue) throws CreateException {
            final Issue createdIssue = this.mhc.createIssue(user, issue);
            IssueManager im = ComponentManager.getInstance().getIssueManager();
            MutableIssue i = im.getIssueObject(createdIssue.getId());

            try {
                addresses = message.getRecipients(Message.RecipientType.TO);
            } catch (MessagingException e) {
                e.printStackTrace();
                throw new CreateException("Не найдены получатели письма из поля TO", e);
            }

            String addressConcatenated = "";
            for (int j=0; j<addresses.length; j++) {
                addressConcatenated = addressConcatenated + "                                                  " +
                        "                                                                                      "
                        + addresses[j].toString();
            }
            Pattern pattern = Pattern.compile("<(.{1,100})>");
            Matcher matcher = pattern.matcher(addressConcatenated);
            String matched = null;
            while (matcher.find()) {
                matched = matcher.group(1);
                System.out.println(matched);
                if (!(helpdesk.get(0).equals(matched))&&!(helpdesk.get(1).equals(matched))&&!(helpdesk.get(2).equals(matched))
                && (UserUtils.getUserByEmail(matched) != null)) {
                    System.out.println("Current matched is: " + matched + "" +
                            " мы его сравнивали с: " + helpdesk.get(0) + "; "
                            + helpdesk.get(1) + "; "
                            + helpdesk.get(2));
                    break;
                }
            }
            System.out.println("helpdesk.get(0): " + helpdesk.get(0) + "; len: "  + helpdesk.get(0).length() + "; " + helpdesk.get(0).getClass().getName());
            System.out.println("matched: " + matched + " len: " + matched.length() + " class: " + matched.getClass().getName());
            System.out.println(matched.equals(helpdesk.get(0)));

            if ((matched != null)) {
                System.out.println("Мы наконец-то установили валидного assignee.");
                User u = UserUtils.getUserByEmail(matched);
                if (u == null) {
                    System.out.println("Пустотень в UserUtils.getUserByEmail(matched)");
                    i.setAssigneeId(i.getProjectObject().getLeadUserName());
                    return i;
                }
                i.setAssignee(UserUtils.getUserByEmail(matched));
                return i;
            }
            else {
                System.out.println("Lead project: " + i.getProjectObject().getLeadUserName());
                i.setAssigneeId(i.getProjectObject().getLeadUserName());
                return i;
            }
        }

        public ChangeItemBean createAttachment(File file, String string, String string1, User user, Issue issue) throws AttachmentException {
            return this.mhc.createAttachment(file, string, string1, user, issue);
        }

        public boolean isRealRun() {
            return this.mhc.isRealRun();
        }

        public MessageHandlerExecutionMonitor getMonitor() {
            return this.mhc.getMonitor();
        }

    }

    @Override
    public void init(Map<String, String> params, MessageHandlerErrorCollector monitor) {
        super.init(params, monitor);
        this.monitor = monitor;
    }

    @Override
    public boolean handleMessage(Message message, MessageHandlerContext context) throws MessagingException {
        String subject = message.getSubject();
        MutableIssue issue = (MutableIssue) ServiceUtils.findIssueObjectInString(subject);

        if (issue == null) {
            // If we cannot find the issue from the subject of the e-mail message
            // try finding the issue using the in-reply-to message id of the e-mail message
            issue = (MutableIssue) getAssociatedIssue(message);
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
            MyMessageHandlerContext myMessageHandlerContext = new MyMessageHandlerContext(context, message);

            return createIssueHandler.handleMessage(message, myMessageHandlerContext);
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