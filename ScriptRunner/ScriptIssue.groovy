import com.atlassian.crowd.embedded.api.User
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.IssueManager

IssueManager issueManager = ComponentAccessor.getIssueManager()
SearchService searchService = ComponentAccessor.getComponent(SearchService.class)
User curUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
Date rightTime = new Date()
if (issue.status.name != "Закрыт"){
    if(issue.getPriority().name != "Возврат" ){
        if (issue.getPriority().name != "Планирование"){
            if(issue.getPriority().name != "Блокирующий"){
                issue.setPriorityId("10100")
                issue.setDueDate(rightTime.toTimestamp())
                issueManager.updateIssue(curUser, issue, EventDispatchOption.ISSUE_UPDATED, false)
            }
        }
    }
}

