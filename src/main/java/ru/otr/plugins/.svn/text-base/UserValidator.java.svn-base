package ru.otr.plugins;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
import com.atlassian.jira.user.util.UserManager;
 
public class UserValidator
{
    private final UserManager userManager;
 
    public UserValidator(UserManager userManager) {
        this.userManager = userManager;
    }
 
    public User validateUser(String userId, MessageHandlerErrorCollector collector) {
 
        final User user = userManager.getUser(userId);
        if (user == null) {
            collector.error("User '" + userId + "' does not exist.");
            return null;
        }
                
        return user;  
    }
}