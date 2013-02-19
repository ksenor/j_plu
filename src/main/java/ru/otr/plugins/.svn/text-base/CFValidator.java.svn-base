package ru.otr.plugins;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
 
public class CFValidator
{
    private final CustomFieldManager cfm;
 
    public CFValidator(CustomFieldManager cfm) {
        this.cfm = cfm;
    }
 
    public CustomField validateCf(String cfId, MessageHandlerErrorCollector collector) {
 
        final CustomField cf = cfm.getCustomFieldObject(cfId);
        if (cf == null) {
            collector.error("CustomField '" + cfId + "' does not exist.");
            return null;
        }
                
        return cf;  
    }
}