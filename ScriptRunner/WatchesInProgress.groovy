package WatchesInProgress

import com.atlassian.crowd.embedded.api.User
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.atlassian.jira.web.bean.PagerFilter
import java.math.RoundingMode

IssueManager issueManager = ComponentAccessor.getIssueManager()
SearchService searchService = ComponentAccessor.getComponent(SearchService.class)
User curUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()



def elapsedTime = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName("Часы (план)")
def scheduledTime = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName("Часы (факт)")
String jqlQueryProcessEpicLink = "(\"Epic Link\"=" + issue.toString() + ") OR issueFunction in subtasksOf(\"\\\"Epic Link\\\"=" + issue.toString() + "\")"
def parseResultEpicLink = searchService.parseQuery(curUser, jqlQueryProcessEpicLink)
def searchResultEpicLink = searchService.search(curUser, parseResultEpicLink.getQuery(), PagerFilter.getUnlimitedFilter())
def issuesByJqlEpicLink = searchResultEpicLink.issues.collect { issueManager.getIssueObject(it.id) }

Double spentTime = 0
Double plannedTime = 0
for (Issue e: issuesByJqlEpicLink){
    if (e.originalEstimate != null){
        spentTime += e.originalEstimate
    }
    if(e.timeSpent != null){
        plannedTime += e.timeSpent
    }
}

spentTime =(int) new BigDecimal(spentTime.doubleValue()/3600).setScale(3, RoundingMode.HALF_EVEN).doubleValue()
plannedTime = (int) new BigDecimal(plannedTime.doubleValue()/3600).setScale(3, RoundingMode.HALF_EVEN).doubleValue()

elapsedTime.updateValue(null, issue, new ModifiedValue(0,(Double) spentTime), new DefaultIssueChangeHolder())
scheduledTime.updateValue(null, issue, new ModifiedValue(0,(Double) plannedTime), new DefaultIssueChangeHolder())