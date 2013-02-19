package ru.otr.plugins.messagehandler;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.plugins.mail.handlers.CreateIssueHandler;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
import com.atlassian.jira.service.util.handler.MessageHandlerExecutionMonitor;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.mail.Message;
import javax.mail.Address;
import javax.mail.MessagingException;

public class AdvCreateIssueHandler extends CreateIssueHandler {
    private String cfCC;
    //private final CFValidator cfValidator;
    private CustomFieldManager cfm = ComponentManager.getInstance().getCustomFieldManager();
    
    public AdvCreateIssueHandler() {
    }
    
    @Override
    public void init(Map<String, String> params, MessageHandlerErrorCollector monitor) {
        super.init(params, monitor);
        cfCC = "customfield_10370";
    }
    
    
    public static Collection<User> getAllUsersFromEmailsStatic(Address[] addresses) {
	CreateIssueHandler i = new CreateIssueHandler();
	return i.getAllUsersFromEmails(addresses);
    }

    public static User getFirstValidAssignee(Address[] addresses, Project project) {
	Collection user_to = getAllUsersFromEmailsStatic(addresses);
	Iterator u_to = user_to.iterator();
	User u_to_first = (User)u_to.next();
	System.out.println(u_to_first);
	System.out.println(u_to_first.getName());
	return u_to_first;
    }

    @Override
    public void addCcWatchersToIssue(Message message, Issue issue, User reporter, MessageHandlerContext context, MessageHandlerExecutionMonitor messageHandlerExecutionMonitor) throws MessagingException {
        super.addCcWatchersToIssue(message, issue, reporter, context, messageHandlerExecutionMonitor);
        IssueManager im = ComponentManager.getInstance().getIssueManager();
        MutableIssue i = im.getIssueObject(issue.getId());
        
        CustomField cc_obj = cfm.getCustomFieldObject(cfCC);
        Collection<User> users = getAllUsersFromEmails(message.getAllRecipients());
        List userlist;
        if (users instanceof List) { userlist = (List)users;
        } else { userlist = new ArrayList(users); }
        if (!userlist.isEmpty()) { i.setCustomFieldValue(cc_obj, userlist); }
        Map modifiedFields = i.getModifiedFields();
        DefaultIssueChangeHolder issueChangeHolder = new DefaultIssueChangeHolder();
        for (Iterator iterator = modifiedFields.keySet().iterator(); iterator.hasNext();)
        {
            String fieldId = (String) iterator.next();
            FieldManager fieldManager = ComponentAccessor.getFieldManager();
            FieldLayoutManager fieldLayoutManager = ComponentAccessor.getFieldLayoutManager();
            if (fieldManager .isOrderableField(fieldId)) {
                OrderableField field = fieldManager.getOrderableField(fieldId);
                FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(i).getFieldLayoutItem(field);
                final ModifiedValue modifiedValue = (ModifiedValue) modifiedFields.get(fieldId);
                field.updateValue(fieldLayoutItem, i, modifiedValue, issueChangeHolder);
            }
        }
        i.resetModifiedFields();
    }
}
