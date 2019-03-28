import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.crowd.embedded.api.User
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.MutableIssue
import java.sql.Timestamp;
import com.atlassian.jira.event.type.EventDispatchOption
IssueManager issueManager = ComponentAccessor.getIssueManager()
SearchService searchService = ComponentAccessor.getComponent(SearchService.class)
User curUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager()


MutableIssue issueMKeis = (MutableIssue) issue
if (issueMKeis.getIssueTypeObject().name.equals("Кейс")) {
    Timestamp rightTimeKeis = issueMKeis.dueDate
    CustomField cFieldIDKeis = customFieldManager.getCustomFieldObjectByName("ID Keйса")
    def cFieldIssueIDKeis = issueMKeis.getCustomFieldValue(cFieldIDKeis).toString()
    String jqlQueryProcess = "cf[11601] = " + cFieldIssueIDKeis.substring(1, cFieldIssueIDKeis.length() - 1)
    def parseResultProcess = searchService.parseQuery(curUser, jqlQueryProcess)
    def searchResultProcess = searchService.search(curUser, parseResultProcess.getQuery(), PagerFilter.getUnlimitedFilter())
    def issuesByJql = searchResultProcess.issues.collect { issueManager.getIssueObject(it.id) }
    for (Issue e : issuesByJql) {
        MutableIssue issueM = (MutableIssue) e
        if (issueM.getIssueTypeObject().name.equals("Процесс")) {
            if (!issueM.getStatusObject().getName().equals("Закрыт")) {
                Timestamp rightTime = issueM.dueDate
                String jqlQueryProcessEpicLink = "(\"Epic Link\"=" + issueM.toString() + ") OR issueFunction in subtasksOf(\"\\\"Epic Link\\\"=" + issueM.toString() + "\")"
                def parseResultEpicLink = searchService.parseQuery(curUser, jqlQueryProcessEpicLink)
                def searchResultEpicLink = searchService.search(curUser, parseResultEpicLink.getQuery(), PagerFilter.getUnlimitedFilter())
                def issuesByJqlEpicLink = searchResultEpicLink.issues.collect { issueManager.getIssueObject(it.id) }
                for (Issue t : issuesByJqlEpicLink) {
                    CustomField epicLink = customFieldManager.getCustomFieldObjectByName("Epic Link")
                    def epicIssue = t.getCustomFieldValue(epicLink).toString()
                    if (epicIssue.equals(issueM.key)) {
                        if (t.dueDate > rightTime) {
                            rightTime = t.dueDate
                        }
                    }
                    if(t.equals(issuesByJqlEpicLink.get(issuesByJqlEpicLink.size()-1))){
                        if(rightTime != issueM.dueDate){
                            issueM.setDueDate(rightTime)
                            issueManager.updateIssue(curUser, issueM, EventDispatchOption.ISSUE_UPDATED, false)
                        }
                    }
                }
                if (rightTime > rightTimeKeis) {
                    rightTimeKeis = rightTime
                }
            }
        }
        if (issueM.getIssueTypeObject().name.equals("Оплата")) {
            if (issueM.dueDate > rightTimeKeis) {
                rightTimeKeis = issueM.dueDate
            }
        }
        if (issueM.equals(issuesByJql.get(issuesByJql.size()-1))) {
            if (rightTimeKeis != issueMKeis.dueDate) {
                issueMKeis.setDueDate(rightTimeKeis)
                issueManager.updateIssue(curUser, issueMKeis, EventDispatchOption.ISSUE_UPDATED, false)
            }
        }
    }
}
