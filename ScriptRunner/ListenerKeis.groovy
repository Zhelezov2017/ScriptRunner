package KeisListener

import com.atlassian.crowd.embedded.api.User
import com.atlassian.jira.ComponentManager
import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.link.IssueLinkManager
import com.atlassian.jira.issue.link.IssueLinkType
import com.atlassian.jira.issue.link.IssueLinkTypeManager

import java.text.SimpleDateFormat


IssueManager issueManager = ComponentAccessor.getIssueManager()

IssueService issueService = ComponentAccessor.getIssueService()
User curUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager()

IssueLinkTypeManager issueLinkTypeManager = (IssueLinkTypeManager) ComponentManager.getComponentInstanceOfType(IssueLinkTypeManager.class)
IssueLinkManager issueLinkManager = ComponentManager.getInstance().getIssueLinkManager()

MutableIssue issueMKeis = (MutableIssue) event.issue


CustomField cFieldProcessTimeConv = customFieldManager.getCustomFieldObjectByName("Время завершения")
CustomField cFieldIDKeis = customFieldManager.getCustomFieldObjectByName("ID Keйса")
CustomField epicLink = customFieldManager.getCustomFieldObjectByName("Epic Name")
CustomField cFieldProcessIDKeis = customFieldManager.getCustomFieldObjectByName("Тип процесса")
CustomField cFieldTymeCreate = customFieldManager.getCustomFieldObjectByName("Время начала")
CustomField cFieldСustomer = customFieldManager.getCustomFieldObjectByName("Заказчик")

String dateCreate = issueMKeis.getCustomFieldValue(cFieldTymeCreate).toString()
String dateTimeConv = issueMKeis.getCustomFieldValue(cFieldProcessTimeConv).toString()
String dueDate = issueMKeis.dueDate
SimpleDateFormat oldDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
SimpleDateFormat newDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
Date dateCreatePars = oldDateFormat.parse(dateCreate)
Date dateTimeConvPars = oldDateFormat.parse(dateTimeConv)
Date dueDatePars = oldDateFormat.parse(dueDate)

String dateCreateNew = newDateFormat.format(dateCreatePars)
String dateTimeConvNew = newDateFormat.format(dateTimeConvPars)
String dueDateNew = newDateFormat.format(dueDatePars)


Map<String, String> listTypeProcess = new HashMap<String, String>(){{
    put("11008", "Администрирование")
    put("11001", "Разработка")
    put("11004", "ОПЭ")
    put("11000", "Проектирование")
    put("11002", "Поддержка тестирования")
}}

List<String> duplicateLinkTypeColl = new ArrayList<String>() {{
    add("Admin")
    add("Development")
    add("Testing")
    add("Plan")
    add("Technical task")
}}

if (issueMKeis.getIssueTypeObject().name.equals("Кейс")) {
    Iterator it =listTypeProcess.entrySet().iterator()
        while (it.hasNext()) {
            IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
            Map.Entry pair = (Map.Entry) it.next()

            def cFieldIssueIDKeis = issueMKeis.getCustomFieldValue(cFieldIDKeis).toString().substring(1, issueMKeis.getCustomFieldValue(cFieldIDKeis).toString().length() - 1)

            String epicLinkString = "-\\\\" + issueMKeis.summary.toString() + "\\\\" + issueMKeis.summary.toString() + "\\\\" + pair.getValue()


            issueInputParameters.setProjectId(issueMKeis.projectId)
                .setIssueTypeId("10602") //задается процесс
                .setSummary(issueMKeis.summary)
                .setDueDate(dueDateNew)
                .addCustomFieldValue(cFieldTymeCreate.getId(), dateCreateNew)
                .addCustomFieldValue(cFieldProcessIDKeis.getId(), (String) pair.getKey())
                .addCustomFieldValue(cFieldProcessTimeConv.getId(), dateTimeConvNew)
                .addCustomFieldValue(cFieldIDKeis.getId(), cFieldIssueIDKeis)
                .addCustomFieldValue(epicLink.getId(), epicLinkString)


            IssueService.CreateValidationResult createValidationResult = issueService.validateCreate(curUser, issueInputParameters)

            if (createValidationResult.isValid()) {
                IssueService.IssueResult createResult = issueService.create(curUser, createValidationResult)

                for(String e: duplicateLinkTypeColl){
                    IssueLinkType duplicateLinkType = issueLinkTypeManager.getIssueLinkTypesByName(e).first()
                    if (e.equals("Admin") && pair.getValue().equals("Администрирование")){
                        issueLinkManager.createIssueLink(issueMKeis.id, createResult.issue.id, duplicateLinkType.id, null, curUser)
                    } else if (e.equals("Development") && pair.getValue().equals("Разработка")){
                        issueLinkManager.createIssueLink(issueMKeis.id, createResult.issue.id, duplicateLinkType.id, null, curUser)
                    } else if (e.equals("Testing") && pair.getValue().equals("Поддержка тестирования")){
                        issueLinkManager.createIssueLink(issueMKeis.id, createResult.issue.id, duplicateLinkType.id, null, curUser)
                    } else if (e.equals("Plan") && pair.getValue().equals("Проектирование")){
                        issueLinkManager.createIssueLink(issueMKeis.id, createResult.issue.id, duplicateLinkType.id, null, curUser)
                    } else if (e.equals("Technical task") && pair.getValue().equals("ОПЭ")){
                        issueLinkManager.createIssueLink(issueMKeis.id, createResult.issue.id, duplicateLinkType.id, null, curUser)
                    }
                }
            } else {
                log.error("Error")

            }
        }

    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
    issueInputParameters.setProjectId(issueMKeis.projectId)
        .setIssueTypeId("10303")
        .setSummary(issueMKeis.summary)
        .setPriorityId("4")
        .addCustomFieldValue(cFieldСustomer.getId(), " ")
        .setDueDate(dueDateNew)

    IssueService.CreateValidationResult createValidationResult = issueService.validateCreate(curUser, issueInputParameters)
    if (createValidationResult.isValid()) {
        IssueService.IssueResult createResult = issueService.create(curUser, createValidationResult)
        IssueLinkType duplicateLinkType = issueLinkTypeManager.getIssueLinkTypesByName("Sale").first()
        if(duplicateLinkType != null){
            issueLinkManager.createIssueLink(issueMKeis.id, createResult.issue.id, duplicateLinkType.id, null, curUser)
        }
    } else {
        log.error("Error")

    }
}
