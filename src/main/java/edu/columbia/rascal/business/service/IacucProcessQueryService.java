package edu.columbia.rascal.business.service;

import edu.columbia.rascal.business.service.review.iacuc.IacucStatus;
import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;

@Service
class IacucProcessQueryService {
    private static final Logger log = LoggerFactory.getLogger(IacucProcessQueryService.class);

    @Resource
    private TaskService taskService;
    @Resource
    private HistoryService historyService;

    Task getTaskByBizKeyAndTaskDefKey(String bizKey, String taskDefKey) {
    	Assert.notNull(bizKey, "undefined bizKey");
    	Assert.notNull(taskDefKey, "undefined taskDefKey");
        TaskQuery query = taskService.createTaskQuery()
                .processDefinitionKey(IacucProcessService.ProtocolProcessDefKey)
                .processInstanceBusinessKey(bizKey)
                .taskDefinitionKey(taskDefKey)
                .includeProcessVariables();
                
        if (query == null) return null;
        List<Task> list = query.list();
        if (list == null) return null;
        return list.isEmpty() ? null : list.get(0);
    }

    List<Task> getOpenTasksByBizKey(String bizKey) {
    	Assert.notNull(bizKey, "undefined bizKey");
    	TaskQuery query = taskService.createTaskQuery()
                .processDefinitionKey(IacucProcessService.ProtocolProcessDefKey)
                .processInstanceBusinessKey(bizKey)
                .includeProcessVariables();
        return query != null ? query.list() : null;
    }


    List<Task> getDesignatedReviewTasks() {
        TaskQuery query = taskService.createTaskQuery()
                .processDefinitionKey(IacucProcessService.ProtocolProcessDefKey)
                .taskDefinitionKeyLike("rv%");
        return query != null ? query.list() : null;
    }

    List<Task> getOpenReviewerTasks() {
        TaskQuery query = taskService.createTaskQuery()
                .processDefinitionKey(IacucProcessService.ProtocolProcessDefKey)
                .includeProcessVariables()
                .taskDefinitionKeyLike("rv%")
                .orderByTaskCreateTime()
                .desc();
        return query != null ? query.list() : null;
    }

    List<HistoricTaskInstance> getHistoricTaskInstanceListByAssignee(String assignee) {
    	Assert.notNull(assignee,"undefined assignee");
        try {
            HistoricTaskInstanceQuery query = historyService
                    .createHistoricTaskInstanceQuery()
                    .processDefinitionKey(IacucProcessService.ProtocolProcessDefKey)
                    .taskDefinitionKeyLike("rv%")
                    .finished().includeProcessVariables()
                    .taskAssignee(assignee)
                    .orderByHistoricTaskInstanceEndTime()
                    .desc();
            return query != null ? query.list() : null;
        } catch (Exception e) {
            log.error("caught error in query:", e);
            return null;
        }
    }


    HistoricTaskInstance getHistoricTaskInstanceByTaskId(String taskId) {
    	Assert.notNull(taskId, "undefined taskId");
        HistoricTaskInstanceQuery query = historyService
                .createHistoricTaskInstanceQuery()
                .processDefinitionKey(IacucProcessService.ProtocolProcessDefKey)
                .includeProcessVariables()
                .taskId(taskId);
        if (query == null) return null;
        List<HistoricTaskInstance> list = query.list();
        if (list == null) return null;
        return list.isEmpty() ? null : list.get(0);
    }

    HistoricTaskInstance getHistoricApprovalTaskInstance(String bizKey) {
    	Assert.notNull(bizKey, "undefined bizKey");
        try {
            HistoricTaskInstanceQuery query = historyService
                    .createHistoricTaskInstanceQuery()
                    .processDefinitionKey(IacucProcessService.ProtocolProcessDefKey)
                    .processInstanceBusinessKey(bizKey)
                    .taskDefinitionKey(IacucStatus.FinalApproval.taskDefKey())
                    .finished()
                    .includeTaskLocalVariables()
                    .orderByHistoricTaskInstanceEndTime()
                    .desc();
            if (query == null) return null;
            List<HistoricTaskInstance> list = query.list();
            if (list == null) return null;
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            // most likely deseriallize trouble in old data set
            log.error("caught exception:", e);
            return null;
        }
    }

    
    List<HistoricTaskInstance> getHistoricSuspendedRecord() {
        HistoricTaskInstanceQuery query = historyService
                .createHistoricTaskInstanceQuery()
                .processDefinitionKey(IacucProcessService.ProtocolProcessDefKey)
                .taskDefinitionKey(IacucStatus.Suspend.taskDefKey())
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .desc();
        return query != null ? query.list() : null;
    }



}
