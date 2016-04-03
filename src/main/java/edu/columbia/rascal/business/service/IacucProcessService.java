package edu.columbia.rascal.business.service;

import edu.columbia.rascal.business.service.review.iacuc.IacucCorrespondence;
import edu.columbia.rascal.business.service.review.iacuc.IacucDistributeSubcommitteeForm;
import edu.columbia.rascal.business.service.review.iacuc.IacucStatus;
import edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm;
import edu.columbia.rascal.business.service.review.iacuc.ProcessConst;
import edu.columbia.rascal.business.service.review.iacuc.Reminder;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Resource;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

@Service
final class IacucProcessService {

    static final Logger log = LoggerFactory.getLogger(IacucProcessService.class);

    @Resource
    private RuntimeService runtimeService;
    @Resource
    private TaskService taskService;
    @Resource
    private HistoryService historyService;
    @Resource
    private IdentityService identityService;
    @Resource
    private ManagementService managementService;


    @Transactional
    boolean startProtocolProcess(final String protocolId,
                                 final String userId,
                                 final Map<String, Object> processInput) {
        Assert.notNull(processInput);
        if (isProtocolProcessStarted(protocolId)) {
            log.warn("Process was already started for protocolId=" + protocolId);
            return false;
        }
        processInput.put(ProcessConst.START_GATEWAY, IacucStatus.Submit.gatewayValue());
        final ProcessInstance instance = runtimeService.startProcessInstanceByKey(ProcessConst.PROTOCOL_PROCESS_DEF, protocolId, processInput);
        identityService.setAuthenticatedUserId(userId);
        runtimeService.setProcessInstanceName(instance.getProcessInstanceId(), IacucStatus.Submit.name());
        log.info("protocolId={}, activityId={}, processId={}", protocolId, instance.getActivityId(), instance.getId());
        return true;
    }

    @Transactional
    String startKaputProcess(final String protocolId, final String userId, final Map<String, Object> processInput) {
        Assert.notNull(processInput);
        /*
        if (isProtocolProcessStarted(protocolId)) {
            log.warn("Process was already started for protocolId=" + protocolId);
            return false;
        }
        */
        processInput.put(ProcessConst.START_GATEWAY, IacucStatus.Kaput.gatewayValue());
        final ProcessInstance instance = runtimeService.startProcessInstanceByKey(ProcessConst.PROTOCOL_PROCESS_DEF, protocolId, processInput);
        identityService.setAuthenticatedUserId(userId);
        runtimeService.setProcessInstanceName(instance.getProcessInstanceId(), IacucStatus.Kaput.name());
        log.info("protocolId={}, activityId={}, processId={}", protocolId, instance.getActivityId(), instance.getId());
        return instance.getProcessInstanceId();
    }

    @Transactional
    ProcessInstance startReminderProcess(final String protocolId, final String userId,
                                         final Map<String, Object> processInput,
                                         final Reminder reminder) {
        Assert.notNull(processInput);
        processInput.put(ProcessConst.START_GATEWAY, reminder.gatewayValue());
        final ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                ProcessConst.REMINDER_PROCESS_DEF_KEY,
                protocolId,
                processInput);
        identityService.setAuthenticatedUserId(userId);
        runtimeService.setProcessInstanceName(instance.getProcessInstanceId(),
                reminder.name());
        log.info("protocolId={}, activityId={}, processId={}",
                protocolId, instance.getActivityId(), instance.getId());
        return instance;
    }


    boolean isProtocolProcessStarted(final String bizKey) {
        return getProtocolProcessInstance(bizKey, IacucStatus.Submit.name()) != null;
    }

    boolean hasReviewerTask(final String bizKey) {
        final java.util.List<org.activiti.engine.task.Task> list = taskService.createTaskQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .processInstanceBusinessKey(bizKey)
                .taskDefinitionKeyLike("rv%").list();
        return list != null && !list.isEmpty();
    }

    // if all reviewer given the same vote, then return the vote
    // otherwise return null, meaning different vote
    String getReviewVote(final String bizKey) {
        final String processInstanceId = getCurrentProtocolProcessInstanceId(bizKey);
        final java.util.List<org.activiti.engine.history.HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .processInstanceBusinessKey(bizKey)
                .processInstanceId(processInstanceId)
                .taskDefinitionKeyLike("rv%")
                .finished()
                .taskDeleteReason(ProcessConst.TASK_COMPLETED)
                .list();
        final java.util.Set<String> nameSet = new java.util.HashSet<String>();
        for (final HistoricTaskInstance hs : list) {
            nameSet.add(hs.getName());
        }
        if (nameSet.size() > 1) {
            return null;
        }
        for (final String name : nameSet) {
            return name;
        }
        return null;
    }

    @Transactional
    String attachSnapshotToTask(final String protocolId, final String taskDefKey, final InputStream content) {
        final Task task = getTaskByBizKeyAndTaskDefKey(protocolId, taskDefKey);
        if (task == null) {
            log.error("can't find task=" + taskDefKey);
            return null;
        }
        final String attachmentType = "IACUC " + taskDefKey + " " + ProcessConst.SNAPSHOT;
        // name: taskDefKey.protocolId.yyyyMMddHHmmss.pdf
        final String attachmentName = taskDefKey + "." + protocolId + "." + getCurrentDateString() + ".pdf";
        final String attachmentDescription = taskDefKey + " " + ProcessConst.SNAPSHOT;

        return attachSnapshot(attachmentType,
                task.getId(),
                task.getProcessInstanceId(),
                attachmentName,
                attachmentDescription,
                content);
    }


    private String attachSnapshot(final String attachmentType, final String taskId, final String procId,
                                  final String attachmentName, final String description, final InputStream content) {
        final Attachment attachment = taskService.createAttachment(attachmentType,
                taskId,
                procId,
                attachmentName,
                description,
                content);
        return attachment == null ? null : attachment.getId();
    }

    private String getCurrentDateString() {
        return DateTime.now().toString("yyyyMMddHHmmss");
    }

    boolean hasTaskByTaskDefKey(final String protocolId, final String taskDefKey) {
        return getTaskByBizKeyAndTaskDefKey(protocolId, taskDefKey) != null;
    }

    InputStream getSnapshotContent(final String attachmentId) {
        final Attachment attachment = taskService.getAttachment(attachmentId);
        return attachment == null ? null : taskService.getAttachmentContent(attachmentId);
    }

    IacucTaskForm getPreviousApprovedData(final String protocolId) {

        final java.util.List<org.activiti.engine.history.HistoricTaskInstance> list = historyService
                .createHistoricTaskInstanceQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .processInstanceBusinessKey(protocolId)
                .taskDefinitionKey(IacucStatus.FinalApproval.taskDefKey())
                .finished()
                .includeTaskLocalVariables()
                .orderByHistoricTaskInstanceEndTime()
                .desc().list();
        if (list == null || list.isEmpty()) {
            log.warn("couldn't get hs for protocolId={}", protocolId);
            return null;
        }
        final HistoricTaskInstance hs = list.get(0);
        final IacucTaskForm form = new IacucTaskForm();
        form.setTaskDefKey(IacucStatus.FinalApproval.taskDefKey());
        form.setTaskName(hs.getName());
        form.setEndTime(hs.getEndTime());
        form.setAuthor(hs.getAssignee());
        form.setTaskId(hs.getId());
        //
        final java.util.Map<String, Object> localMap = hs.getTaskLocalVariables();
        @SuppressWarnings("unchecked")
        final Map<String, String> taskMap = (Map<String, String>) localMap.get(ProcessConst.TASK_FORM_LOOKUP_PREFIX + hs.getId());
        if (taskMap != null) {
            form.setProperties(taskMap);
            form.setComment(getCommentText(form.getCommentId()));
        }
        //
        @SuppressWarnings("unchecked")
        final Map<String, String> corrMap = (Map<String, String>) localMap.get(ProcessConst.CORRESPONDENCE + hs.getId());
        if (corrMap != null && !corrMap.isEmpty()) {
            final IacucCorrespondence corr = new IacucCorrespondence();
            corr.setProperties(corrMap);
            form.setCorrespondence(corr);
        }
        return form;
    }


    private String getAttachmentId(final String taskId) {
        final java.util.List<org.activiti.engine.task.Attachment> list = taskService.getTaskAttachments(taskId);
        if (list == null || list.isEmpty()) {
            return null;
        }
        final Attachment attachment = list.get(0);
        return attachment.getId();
    }

    IacucTaskForm getHistoryByTaskIdForPdfComparison(final String taskId) {
        Assert.notNull(taskId, "undefined taskId");

        final HistoricTaskInstance hs = historyService
                .createHistoricTaskInstanceQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .taskId(taskId).singleResult();
        if (hs == null) {
            log.error("cannot get HistoricTaskInstance by taskId={}", taskId);
            return null;
        }
        final String taskDefKey = hs.getTaskDefinitionKey();
        final String procInstanceId = hs.getProcessInstanceId();
        final String protocolId = getBizKeyFromHistory(procInstanceId);
        final IacucTaskForm history = new IacucTaskForm();
        history.setBizKey(protocolId);
        history.setTaskId(taskId);
        history.setTaskDefKey(taskDefKey);
        history.setSnapshotId(getAttachmentId(hs.getId()));

        return history;
    }

    String getBizKeyFromHistory(final String processInstanceId) {
        final HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        return instance == null ? null : instance.getBusinessKey();
    }

    String getBizKeyFromRuntime(final String processInstanceId) {
        final ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        return instance == null ? null : instance.getBusinessKey();
    }

    java.util.Set<String> getBizKeyFromOpenTasksByAssignee(final String uni) {
        Assert.notNull(uni);
        final java.util.Set<String> list = new java.util.TreeSet<String>();
        final java.util.List<org.activiti.engine.task.Task> taskList = taskService.createTaskQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .includeProcessVariables()
                .taskDefinitionKeyLike("rv%")
                .orderByTaskCreateTime()
                .desc().list();

        if (taskList == null) {
            return list;
        }
        for (final Task task : taskList) {
            if (!uni.equals(getUserIdFromIdentityLink(task.getId()))) {
                continue;
            }
            final String bizKey = getBizKeyFromRuntime(task.getProcessInstanceId());
            if (bizKey != null) {
                list.add(bizKey);
            }
        }
        return list;
    }

    /*
    Set<String> getBizKeyFromClosedTasksByAssignee(String uni) {
        Set<String> bizKeys = new TreeSet<String>();
        List<HistoricTaskInstance> fetchList = historyService
                .createHistoricTaskInstanceQuery()
                .processDefinitionKey(IacucProcessService.PROTOCOL_PROCESS_DEF)
                .taskDefinitionKeyLike("rv%")
                .finished()
                .taskAssignee(uni)
                .orderByHistoricTaskInstanceEndTime()
                .desc().list();

        if (fetchList == null) return bizKeys;
        for (HistoricTaskInstance hs : fetchList) {
            String protocolId = getBizKeyFromHistory(hs.getProcessInstanceId());
            if (protocolId != null) {
                bizKeys.add(protocolId);
            }
        }
        return bizKeys;
    }
	*/

    java.util.Set<String> getBizKeyFromClosedTasksByAssignee(final String uni) {
        final java.util.Set<String> bizKeys = new java.util.TreeSet<String>();
        final java.util.Set<String> processIdSet = getProcessIdSetFromRuntime();
        for (final String processInstanceId : processIdSet) {
            final java.util.List<org.activiti.engine.history.HistoricTaskInstance> fetchList = historyService
                    .createHistoricTaskInstanceQuery()
                    .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                    .processInstanceId(processInstanceId)
                    .taskDefinitionKeyLike("rv%")
                    .taskAssignee(uni)
                    .finished()
                    .orderByHistoricTaskInstanceEndTime()
                    .desc().list();
            if (fetchList.isEmpty()) {
                continue;
            }
            final String protocolId = getBizKeyFromRuntime(processInstanceId);
            if (protocolId != null) {
                bizKeys.add(protocolId);
            }
        }
        return bizKeys;
    }

    @Transactional
    boolean terminateProtocol(final String protocolId, final String userId) {
        if (isProtocolProcessStarted(protocolId)) {
            log.error("cannot terminate this protocol because it is process, protocolId={}", protocolId);
            return false;
        }
        final java.util.Map<String, Object> processInput = new java.util.HashMap<String, Object>();
        processInput.put(ProcessConst.START_GATEWAY, IacucStatus.Terminate.gatewayValue());
        identityService.setAuthenticatedUserId(userId);
        final ProcessInstance instance = runtimeService.startProcessInstanceByKey(ProcessConst.PROTOCOL_PROCESS_DEF, protocolId, processInput);
        runtimeService.setProcessInstanceName(instance.getProcessInstanceId(), IacucStatus.Terminate.name());

        log.info("protocolId={}, activityId={}, processId={}", protocolId, instance.getActivityId(), instance.getId());
        return true;
    }

    @Transactional
    boolean suspendProtocol(final String protocolId, final String userId) {
        if (isProtocolProcessStarted(protocolId)) {
            log.error("cannot suspend this protocol because it is process, protocolId={}", protocolId);
            return false;
        }
        final java.util.Map<String, Object> processInput = new java.util.HashMap<String, Object>();
        processInput.put(ProcessConst.START_GATEWAY, IacucStatus.Suspend.gatewayValue());
        identityService.setAuthenticatedUserId(userId);
        final ProcessInstance instance = runtimeService.startProcessInstanceByKey(ProcessConst.PROTOCOL_PROCESS_DEF, protocolId, processInput);
        runtimeService.setProcessInstanceName(instance.getProcessInstanceId(), IacucStatus.Suspend.name());

        log.info("protocolId={}, activityId={}, processId={}", protocolId, instance.getActivityId(), instance.getId());
        return true;
    }

    @Transactional
    boolean reinstateProtocol(final String protocolId, final String userId) {
        if (isProtocolProcessStarted(protocolId)) {
            log.error("cannot reinstate this protocol because it is in process, protocolId={}", protocolId);
            return false;
        }
        final java.util.Map<String, Object> processInput = new java.util.HashMap<String, Object>();
        processInput.put(ProcessConst.START_GATEWAY, IacucStatus.Reinstate.gatewayValue());
        identityService.setAuthenticatedUserId(userId);
        final ProcessInstance instance = runtimeService.startProcessInstanceByKey(ProcessConst.PROTOCOL_PROCESS_DEF, protocolId, processInput);
        runtimeService.setProcessInstanceName(instance.getProcessInstanceId(), IacucStatus.Reinstate.name());

        log.info("protocolId={}, activityId={}, processId={}", protocolId, instance.getActivityId(), instance.getId());
        return true;
    }

    @Transactional
    boolean withdrawProtocol(final String protocolId, final String userId) {
        if (isProtocolProcessStarted(protocolId)) {
            log.error("cannot withdraw this protocol because it is process, protocolId={}", protocolId);
            return false;
        }
        final java.util.Map<String, Object> processInput = new java.util.HashMap<String, Object>();
        processInput.put(ProcessConst.START_GATEWAY, IacucStatus.Withdraw.gatewayValue());
        identityService.setAuthenticatedUserId(userId);
        final ProcessInstance instance = runtimeService.startProcessInstanceByKey(ProcessConst.PROTOCOL_PROCESS_DEF, protocolId, processInput);
        runtimeService.setProcessInstanceName(instance.getProcessInstanceId(), IacucStatus.Reinstate.name());

        log.info("protocolId={}, activityId={}, processId={}", protocolId, instance.getActivityId(), instance.getId());
        return true;
    }


    java.util.Set<String> getBizKeyHasDesignatedReviewTask() {
        final java.util.Set<String> set = new java.util.TreeSet<String>();
        final java.util.List<org.activiti.engine.task.Task> taskList = taskService.createTaskQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .taskDefinitionKeyLike("rv%").list();
        if (taskList == null) {
            return set;
        }
        for (final Task task : taskList) {
            final String protocolId = getBizKeyFromRuntime(task.getProcessInstanceId());
            if (protocolId != null) {
                set.add(protocolId);
            }
        }
        log.info("BizKey has designated review task: size={}", set.size());
        return set;
    }


    Map<String, Date> getHistoricSuspendedBizKeyAndDate() {
        final Map<String, Date> map = new TreeMap<String, Date>();
        final java.util.List<org.activiti.engine.history.HistoricTaskInstance> list = historyService
                .createHistoricTaskInstanceQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .taskDefinitionKey(IacucStatus.Suspend.taskDefKey())
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .desc().list();
        if (list == null) {
            return map;
        }
        for (final HistoricTaskInstance hs : list) {
            final String protocolId = getBizKeyFromHistory(hs.getProcessInstanceId());
            if (protocolId != null) {
                map.put(protocolId, hs.getEndTime());
            }
        }
        return map;
    }


    Map<String, Date> getAdverseEventIdSubmitDate() {
        final Map<String, Date> map = new TreeMap<String, Date>();
        final java.util.List<org.activiti.engine.runtime.ProcessInstance> instanceList = runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey(ProcessConst.ADVERSE_EVENT_DEF_KEY)
                .list();
        if (instanceList == null || instanceList.isEmpty()) {
            return map;
        }
        for (final ProcessInstance instance : instanceList) {
            final String businessKey = instance.getBusinessKey();
            final HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery()
                    .processDefinitionKey(ProcessConst.ADVERSE_EVENT_DEF_KEY)
                    .processInstanceId(instance.getProcessInstanceId())
                    .taskDefinitionKey(IacucStatus.Submit.taskDefKey())
                    .singleResult();
            if (task != null) {
                map.put(businessKey, task.getCreateTime());
            }
        }
        return map;
    }

    private String getCommentText(final String commentId) {
        if (commentId == null) {
            return null;
        }
        final Comment comment = taskService.getComment(commentId);
        return comment == null ? null : comment.getFullMessage();
    }

    // add correspondence process
    boolean addCorrespondence(final IacucTaskForm taskForm) {
        if (startAddCorrespondenceProcess(taskForm.getBizKey()) != null) {
            completeTaskByTaskForm(ProcessConst.PROTOCOL_PROCESS_DEF, taskForm);
            return true;
        }
        return false;
    }

    boolean addNote(final IacucTaskForm taskForm) {
        if (startAddNoteProcess(taskForm.getBizKey()) == null) {
            return false;
        }
        completeTaskByTaskForm(ProcessConst.PROTOCOL_PROCESS_DEF, taskForm);
        return true;
    }

    private ProcessInstance startAddCorrespondenceProcess(final String bizKey) {
        ProcessInstance instance = getCorrProcessInstance(bizKey);
        if (instance != null) {
            log.error("add correspondence process is still running, protocolId={}", bizKey);
            return null;
        }
        final java.util.Map<String, Object> map = new java.util.HashMap<String, Object>();
        map.put(ProcessConst.START_GATEWAY, IacucStatus.AddCorrespondence.gatewayValue());
        instance = runtimeService.startProcessInstanceByKey(ProcessConst.PROTOCOL_PROCESS_DEF, bizKey, map);
        runtimeService.setProcessInstanceName(instance.getProcessInstanceId(), IacucStatus.AddCorrespondence.name());
        return instance;
    }

    private ProcessInstance startAddNoteProcess(final String bizKey) {
        ProcessInstance instance = getNoteProcessInstance(bizKey);
        if (instance != null) {
            log.error("add note process is still running, protocolId={}", bizKey);
            return null;
        }
        final java.util.Map<String, Object> map = new java.util.HashMap<String, Object>();
        map.put(ProcessConst.START_GATEWAY, IacucStatus.AddNote.gatewayValue());
        instance = runtimeService.startProcessInstanceByKey(ProcessConst.PROTOCOL_PROCESS_DEF, bizKey, map);
        runtimeService.setProcessInstanceName(instance.getProcessInstanceId(),
                IacucStatus.AddNote.name());
        return instance;
    }


    private ProcessInstance getCorrProcessInstance(final String bizKey) {
        return getProcessInstanceByName(bizKey, IacucStatus.AddCorrespondence.name());
    }

    private ProcessInstance getNoteProcessInstance(final String bizKey) {
        return getProcessInstanceByName(bizKey, IacucStatus.AddNote.name());
    }

    private ProcessInstance getProcessInstanceByName(final String bizKey, final String instanceName) {
        return runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .processInstanceBusinessKey(bizKey)
                .processInstanceName(instanceName)
                .singleResult();
    }

    private ProcessInstance getProcessInstanceByProcessDefKeyAndName(
            final String bizKey, final String defKey, final String instanceName) {
        return runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey(defKey)
                .processInstanceBusinessKey(bizKey)
                .processInstanceName(instanceName)
                .singleResult();
    }

    private HistoricProcessInstance getHistoriceProcessInstanceByDefKeyAndName(
            final String bizKey, final String defKey, final String instanceName) {
        return historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(defKey)
                .processInstanceBusinessKey(bizKey)
                .processInstanceName(instanceName)
                .singleResult();
    }

    private String getHistoricProcessInstanceId(
            final String bizKey, final String processDefKey, final String instanceName) {
        final HistoricProcessInstance hs = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefKey)
                .processInstanceBusinessKey(bizKey)
                .processInstanceName(instanceName)
                .singleResult();
        return hs == null ? null : hs.getId();
    }

    private ProcessInstance getProtocolProcessInstance(final String bizKey, final String instanceName) {
        return getProcessInstanceByName(bizKey, instanceName);
    }

    // if want to have same vote thing
    // Map<String, Date> getBizKeyMeetingDate(Set<String> bizKeys, Map<String, String> bizKeyRvStatus) {
    Map<String, Date> getBizKeyMeetingDate(final java.util.Set<String> bizKeys) {
        Assert.notNull(bizKeys);
        final Map<String, Date> bizKeyMeetingDate = new HashMap<String, Date>();
        final java.util.List<org.activiti.engine.runtime.ProcessInstance> list = runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .processInstanceName(IacucStatus.Submit.name())
                .includeProcessVariables().list();

        for (final ProcessInstance instance : list) {
            final String protocolId = instance.getBusinessKey();
            if (hasTaskByTaskDefKey(protocolId, IacucStatus.UndoApproval.taskDefKey())) {
                // a timered task, don't show it in admin queue
                continue;
            } else if (hasReviewerTask(protocolId)) {
                // don't show it in admin-queue
                continue;
            }
            /* 
            String vote = getReviewVote(protocolId);
            log.info("vote={}", vote);
            if (vote != null) {
                bizKeyRvStatus.put(protocolId, vote);
            }
            */
            bizKeys.add(protocolId);
            final java.util.Map<String, Object> map = instance.getProcessVariables();
            if (map != null) {
                final Object obj = map.get("meetingDate");
                if (obj != null) {
                    bizKeyMeetingDate.put(protocolId, (Date) obj);
                }
            }
        }
        return bizKeyMeetingDate;
    }

    private java.util.Set<String> getProcessIdSetFromRuntime() {
        final java.util.Set<String> procIdSet = new java.util.TreeSet<String>();
        final java.util.List<org.activiti.engine.runtime.ProcessInstance> list = runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .processInstanceName(IacucStatus.Submit.name())
                .includeProcessVariables().list();
        if (list.isEmpty()) {
            return procIdSet;
        }
        for (final ProcessInstance instance : list) {
            procIdSet.add(instance.getProcessInstanceId());
        }
        return procIdSet;
    }


    Date getMeetingDateByBizKey(final String bizKey) {
        final ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .processInstanceName(IacucStatus.Submit.name())
                .processInstanceBusinessKey(bizKey)
                .includeProcessVariables().singleResult();
        if (instance == null) {
            return null;
        }
        final java.util.Map<String, Object> map = instance.getProcessVariables();
        return map.get("meetingDate") == null ? null : (Date) map.get("meetingDate");
    }


    void deleteProcess(final String processDefKey, final String bizKey, final String reason) {
        final ProcessInstance instance = runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey(processDefKey)
                .processInstanceBusinessKey(bizKey)
                .singleResult();
        if (instance != null) {
            runtimeService.deleteProcessInstance(instance.getProcessInstanceId(), reason);
        }
    }


    String getTaskAssignee(final String taskDefKey, final String bizKey) {
        final Task task = taskService.createTaskQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .processInstanceBusinessKey(bizKey)
                .taskDefinitionKey(taskDefKey)
                .singleResult();
        if (task == null) {
            return null;
        }
        return getUserIdFromIdentityLink(task.getId());
    }


    java.util.List<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm> getPreviousNote(final String bizKey) {
        final java.util.List<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm> list = new java.util.ArrayList<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm>();
        final java.util.List<org.activiti.engine.history.HistoricTaskInstance> hsList = historyService.createHistoricTaskInstanceQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .processInstanceBusinessKey(bizKey)
                .taskDefinitionKey(IacucStatus.AddNote.taskDefKey())
                .finished()
                .includeTaskLocalVariables()
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();

        if (hsList == null) {
            return list;
        }

        for (final HistoricTaskInstance hs : hsList) {
            final java.util.Map<String, Object> localMap = hs.getTaskLocalVariables();
            if (localMap == null) {
                continue;
            }

            @SuppressWarnings("unchecked")
            final Map<String, String> map = (Map<String, String>) localMap.get(ProcessConst.TASK_FORM_LOOKUP_PREFIX + hs.getId());
            if (map == null) {
                continue;
            }
            final IacucTaskForm taskForm = newIacucTaskForm();
            taskForm.setTaskId(hs.getId());
            taskForm.setEndTime(hs.getEndTime());
            taskForm.setProperties(map);
            taskForm.setComment(getCommentText(taskForm.getCommentId()));
            list.add(taskForm);
        }
        return list;
    }

    @Transactional
    boolean startAdverseEventProcess(final String adverseEventId, final String userId) {
        if (getAdverseEventProcessInstance(adverseEventId) != null) {
            log.error("Process was already started for adverseEventId={}, userId={}", adverseEventId, userId);
            return false;
        }
        final ProcessInstance instance = runtimeService.startProcessInstanceByKey(ProcessConst.ADVERSE_EVENT_DEF_KEY, adverseEventId);
        log.info("adverseEventId={}, activityId={}, processId={}", adverseEventId, instance.getActivityId(), instance.getId());
        runtimeService.setProcessInstanceName(instance.getProcessInstanceId(), IacucStatus.AdverseEvent.name());
        return true;
    }

    private ProcessInstance getAdverseEventProcessInstance(final String bizKey) {
        return runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey(ProcessConst.ADVERSE_EVENT_DEF_KEY)
                .processInstanceBusinessKey(bizKey)
                .singleResult();
    }


    @Transactional
    String completeTaskByTaskForm(final String processDefKey, final IacucTaskForm iacucTaskForm) {
        Assert.notNull(iacucTaskForm);
        final String bizKey = iacucTaskForm.getBizKey();
        Assert.notNull(bizKey);
        final String taskDefKey = iacucTaskForm.getTaskDefKey();
        final Task task = getTask(processDefKey, bizKey, taskDefKey);
        Assert.notNull(task);
        final String taskId = task.getId();
        if (task.getAssignee() == null) {
            task.setAssignee(iacucTaskForm.getAuthor());
            taskService.claim(taskId, iacucTaskForm.getAuthor());
        }

        // if you want to store comment in activity task comment, then... otherwise do nothing
        final String content = iacucTaskForm.getComment();
        if (content != null) {
            final Comment comment = taskService.addComment(taskId, task.getProcessInstanceId(), taskDefKey, content);
            iacucTaskForm.setCommentId(comment.getId());
        }

        // attach attribute to this task
        final java.util.Map<String, String> attribute = iacucTaskForm.getProperties();
        if (attribute != null && !attribute.isEmpty()) {
            taskService.setVariableLocal(taskId, ProcessConst.TASK_FORM_LOOKUP_PREFIX + taskId, attribute);
        }

        // attach correspondence to this task
        final IacucCorrespondence corr = iacucTaskForm.getCorrespondence();
        if (corr != null) {
            corr.apply();
            final java.util.Map<String, String> corrProperties = corr.getProperties();
            if (!corrProperties.isEmpty()) {
                taskService.setVariableLocal(taskId, ProcessConst.CORRESPONDENCE + taskId, corrProperties);
            }
        }

        // for show business
        if (IacucStatus.DistributeSubcommittee.isDefKey(taskDefKey) &&
            iacucTaskForm instanceof IacucDistributeSubcommitteeForm) {
                taskService.setVariable(taskId, "meetingDate", iacucTaskForm.getDate());
        }
        /*
        // determine the direction
        final Map<String, Object> map = iacucTaskForm.getTaskVariables();
        if (map == null || map.isEmpty()) {
            taskService.complete(taskId); // go straight
        }else {
            taskService.complete(taskId, map); // go left/right/middle or go ...
        }
        */
        completeTask(taskId, iacucTaskForm);
        return taskId;
    }

    private void completeTask(final String taskId, final IacucTaskForm iacucTaskForm) {
        final java.util.Map<String, Object> map = iacucTaskForm.getTaskVariables();
        if (map == null || map.isEmpty()) {
            taskService.complete(taskId); // go straight
        }else {
            taskService.complete(taskId, map); // go left/right/middle or go ...
        }
    }

    @Transactional
    String attachSnapshotToAdverseEventTask(final String adverseEvtId, final String taskDefKey, final InputStream content) {
        final Task task = getTask(ProcessConst.ADVERSE_EVENT_DEF_KEY, adverseEvtId, taskDefKey);
        if (task == null) {
            log.error("no task taskDefKey={}, adverseEvtId={}", taskDefKey, adverseEvtId);
            return null;
        }
        final String attachmentType = "IACUC_ADVERSE_EVT_" + taskDefKey + "_" + ProcessConst.SNAPSHOT;
        // name: taskDefKey.adverseEvtid.yyyyMMddHHmmss.pdf
        final String attachmentName = taskDefKey + ".adverse.evt." + adverseEvtId + "." + getCurrentDateString() + ".pdf";
        final String attachmentDescription = taskDefKey + " " + ProcessConst.SNAPSHOT;
        return attachSnapshot(attachmentType,
                task.getId(),
                task.getProcessInstanceId(),
                attachmentName,
                attachmentDescription,
                content);
    }


    private Task getTask(final String processDefKey, final String bizKey, final String taskDefKey) {
        final java.util.List<org.activiti.engine.task.Task> list = taskService.createTaskQuery()
                .processDefinitionKey(processDefKey)
                .processInstanceBusinessKey(bizKey)
                .taskDefinitionKey(taskDefKey).list(); //.singleResult();
        return list.isEmpty() ? null : list.get(0);
    }


    java.util.List<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm> getIacucProtocolHistory(final String protocolId) {
        return getHistory(ProcessConst.PROTOCOL_PROCESS_DEF, protocolId);
    }

    java.util.List<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm> getIacucAdverseHistory(final String aevtId) {
        return getHistory(ProcessConst.ADVERSE_EVENT_DEF_KEY, aevtId);
    }

    private java.util.List<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm> getHistory(final String processDefKey, final String bizKey) {

        final java.util.List<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm> listIacucTaskForm = new java.util.ArrayList<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm>();
        final java.util.List<org.activiti.engine.history.HistoricTaskInstance> list = getHistoriceTaskInstance(processDefKey, bizKey);
        if (CollectionUtils.isEmpty(list)) {
            return listIacucTaskForm;
        }

        for (final HistoricTaskInstance hs : list) {
            final IacucTaskForm iacucTaskForm = newIacucTaskForm();
            iacucTaskForm.setTaskId(hs.getId());
            iacucTaskForm.setEndTime(hs.getEndTime());
            //
            final java.util.Map<String, Object> localMap = hs.getTaskLocalVariables();
            @SuppressWarnings("unchecked")
            final Map<String, String> taskMap = (Map<String, String>) localMap.get(ProcessConst.TASK_FORM_LOOKUP_PREFIX + hs.getId());

            // restore the original attribute
            iacucTaskForm.setProperties(taskMap);

            // two options:
            // if comment is stored in variable, then do nothing
            // if comment is stored in task comment, then as follow
            iacucTaskForm.setComment(getCommentText(iacucTaskForm.getCommentId()));

            // two options:
            // if the snapshot id is retrieved from here, then bla bla ...
            // iacucTaskForm.setSnapshotId(snapshotId);
            // if the snapshot id is pre-stored in properties, then do nothing
            if (StringUtils.isBlank(iacucTaskForm.getSnapshotId())) {
                final String attachmentId = getAttachmentId(hs.getId());
                if (attachmentId != null) {
                    iacucTaskForm.setSnapshotId(attachmentId);
                }
            }

            // restore the original correspondence if any
            @SuppressWarnings("unchecked")
            final Map<String, String> corrMap = (Map<String, String>) localMap.get(ProcessConst.CORRESPONDENCE + hs.getId());
            if (corrMap != null && !corrMap.isEmpty()) {
                final IacucCorrespondence corr = newIacucCorrespondence();
                corr.setProperties(corrMap);
                iacucTaskForm.setCorrespondence(corr);
            }

            // for the sake of old data
            if (iacucTaskForm.getTaskDefKey() == null) {
                iacucTaskForm.setTaskDefKey(hs.getTaskDefinitionKey());
            }
            if (iacucTaskForm.getTaskName() == null) {
                iacucTaskForm.setTaskName(hs.getName());
            }
            if (iacucTaskForm.getAuthor() == null) {
                iacucTaskForm.setAuthor(hs.getAssignee());
            }

            // for old imported data
            if (IacucStatus.Kaput.isDefKey(hs.getTaskDefinitionKey())) {
                final String name = hs.getName();
                final String key = ProcessConst.nameToKey(name);
                if (key != null) {
                    iacucTaskForm.setTaskDefKey(key);
                }
            }
            listIacucTaskForm.add(iacucTaskForm);
        }

        final java.util.List<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm> reminderList = getReminderHistory(bizKey);
        listIacucTaskForm.addAll(reminderList);
        java.util.Collections.sort(listIacucTaskForm);
        java.util.Collections.reverse(listIacucTaskForm);
        return listIacucTaskForm;
    }

    
    private java.util.List<org.activiti.engine.history.HistoricTaskInstance> getHistoriceTaskInstance(final String processDefKey, final String bizKey) {
        // if taskDeleteReason="deleted", that task was closed by activity.
        // if taskDeleteReason="completed", that task was closed by user action
        return historyService
                .createHistoricTaskInstanceQuery()
                .processDefinitionKey(processDefKey)
                .processInstanceBusinessKey(bizKey).finished()
                .taskDeleteReason(ProcessConst.TASK_COMPLETED)
                .includeTaskLocalVariables()
                .orderByHistoricTaskInstanceEndTime().desc().list();
    }


    private java.util.List<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm> getReminderHistory(final String bizKey) {

        final java.util.List<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm> list = new java.util.ArrayList<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm>();
        final java.util.List<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm> reminder90History=getReminderServiceHistory(bizKey, Reminder.Day90);
        if( !reminder90History.isEmpty() ) {
            list.addAll(reminder90History);
        }
        final java.util.List<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm> reminder60History=getReminderServiceHistory(bizKey, Reminder.Day60);
        if( !reminder60History.isEmpty() ) {
            list.addAll(reminder60History);
        }
        final java.util.List<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm> reminder30History=getReminderServiceHistory(bizKey, Reminder.Day30);
        if( !reminder30History.isEmpty() ) {
            list.addAll(reminder30History);
        }
        return list;
    }



    private java.util.List<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm> getReminderServiceHistory(final String bizKey, final Reminder reminder) {

        final java.util.List<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm> list = new java.util.ArrayList<edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm>();

        final String reminderInstanceId = getHistoricProcessInstanceId(
                bizKey, ProcessConst.REMINDER_PROCESS_DEF_KEY, reminder.name());

        final HistoricActivityInstance hai = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(reminderInstanceId)
                .activityId(reminder.serviceTaskId())
                .finished()
                .singleResult();
        if (hai != null) {
            final IacucTaskForm form = new IacucTaskForm();
            form.setBizKey(bizKey);
            form.setTaskName(hai.getActivityName());
            form.setDate(hai.getStartTime());
            form.setEndTime(hai.getEndTime());
            form.setTaskDefKey(hai.getActivityId());
            form.setAuthor("system");
            list.add(form);
        }

        final HistoricActivityInstance catchErr = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(reminderInstanceId)
                .activityId(reminder.catchErrorId())
                .finished()
                .singleResult();
        if (catchErr != null) {
            final IacucTaskForm form = new IacucTaskForm();
            form.setTaskName(catchErr.getActivityName());
            form.setDate(catchErr.getEndTime());
            form.setTaskDefKey(catchErr.getActivityId());
            form.setAuthor("system");
            list.add(form);
        }

        return list;
    }

    boolean isAllReviewersApproved(final String bizKey) {
        // show business
        // first test if there are these tasks
        // if so, don't bother further
        if (hasTaskByTaskDefKey(bizKey, IacucStatus.DistributeSubcommittee.taskDefKey())) {
            return false;
        } else if (hasTaskByTaskDefKey(bizKey, IacucStatus.DistributeReviewer.taskDefKey())) {
            return false;
        } else if (hasTaskByTaskDefKey(bizKey, IacucStatus.UndoApproval.taskDefKey())) {
            return true;
        } else if (hasTaskByTaskDefKey(bizKey, IacucStatus.FinalApproval.taskDefKey())) {
            return true;
        }

        final ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .processInstanceBusinessKey(bizKey)
                .includeProcessVariables()
                .singleResult();
        if (instance == null) {
            log.error("no instance for bizKey={}", bizKey);
            return false;
        }
        final java.util.Map<String, Object> map = instance.getProcessVariables();
        boolean allRvsApproved = false;
        if (map.get("allRvs") != null) {
            allRvsApproved = (Boolean) map.get("allRvs");
            log.info("allRvs={}", allRvsApproved);
        }
        boolean allAppendicesApproved = false;
        if (map.get("allAppendicesApproved") != null) {
            allAppendicesApproved = (Boolean) map.get("allAppendicesApproved");
            log.info("allAppendicesApproved={}", allAppendicesApproved);
        }

        return allRvsApproved && allAppendicesApproved;
    }

    String getCurrentProtocolProcessInstanceId(final String bizKey) {
        final ProcessInstance instance = getProtocolProcessInstance(bizKey, IacucStatus.Submit.name());
        return instance == null ? null : instance.getProcessInstanceId();
    }

    String getCurrentKaputProcessInstanceId(final String bizKey) {
        final ProcessInstance instance = getProtocolProcessInstance(bizKey, IacucStatus.Kaput.name());
        return instance == null ? null : instance.getProcessInstanceId();
    }

    java.util.List<org.activiti.engine.task.Task> getOpenTasksByBizKeyAndCandidateGroup(final String bizKey,
                                                                                        final String userId,
                                                                                        final java.util.List<String> candidateGroup) {
        Assert.notNull(bizKey, "undefined bizKey");
        final java.util.List<org.activiti.engine.task.Task> retList = new java.util.ArrayList<org.activiti.engine.task.Task>();

        final java.util.List<org.activiti.engine.task.Task> list = taskService.createTaskQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .processInstanceBusinessKey(bizKey)
                .taskCandidateGroupIn(candidateGroup).list();
        if (list == null) {
            return retList;
        }
        Task returnToPai = null;
        boolean hasUndoApproval = false;
        final boolean hasRvAction = hasReviewerAction(bizKey);
        for (final Task task : list) {
            final String taskDefKey = task.getTaskDefinitionKey();
            if (IacucStatus.ReturnToPI.isDefKey(taskDefKey)) {
                returnToPai = task;
                continue;
            } else if (IacucStatus.UndoApproval.isDefKey(taskDefKey)) {
                hasUndoApproval = true;
            } else if (IacucStatus.Redistribute.isDefKey(taskDefKey)) {
                if (hasRvAction) {
                    continue;
                }
            } else if (taskDefKey.startsWith("rv") && !userId.equals(getUserIdFromIdentityLink(task.getId()))) {
                    continue;
            }
            retList.add(task);
        }

        if (!hasUndoApproval && returnToPai != null) {
            retList.add(returnToPai);
        }
        return retList;
    }

    boolean hasReviewerAction(final String bizKey) {
        final String processInstanceId = getCurrentProtocolProcessInstanceId(bizKey);
        final java.util.List<org.activiti.engine.history.HistoricTaskInstance> list = historyService
                .createHistoricTaskInstanceQuery()
                .processInstanceBusinessKey(bizKey)
                .processInstanceId(processInstanceId)
                .taskDefinitionKeyLike("rv%")
                .finished()
                .taskDeleteReason(ProcessConst.TASK_COMPLETED).list();
        return list != null && !list.isEmpty();
    }

    java.util.Set<String> getReviewerUserId(final String bizKey) {
        final java.util.Set<String> reviewerUserId = new java.util.TreeSet<String>();
        final java.util.List<org.activiti.engine.task.Task> list = taskService.createTaskQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .processInstanceBusinessKey(bizKey)
                .taskDefinitionKeyLike("rv%")
                .list();
        if (list == null || list.isEmpty()) {
            return reviewerUserId;
        }
        for (final Task task : list) {
            final String userId = getUserIdFromIdentityLink(task.getId());
            if (userId != null) {
                reviewerUserId.add(userId);
            }
        }
        return reviewerUserId;
    }

    private String getUserIdFromIdentityLink(final String taskId) {
        final java.util.List<org.activiti.engine.task.IdentityLink> list = taskService.getIdentityLinksForTask(taskId);
        if (list == null) {
            return null;
        }
        for (final IdentityLink link : list) {
            final String userId = link.getUserId();
            if (userId != null) {
                return userId;
            }
        }
        return null;
    }

    java.util.Set<String> getActionedReviewerUserId(final String bizKey) {
        final java.util.Set<String> rvUserId = new java.util.TreeSet<String>();
        final String processInstanceId = getCurrentProtocolProcessInstanceId(bizKey);
        if (processInstanceId == null) {
            return rvUserId;
        }
        final java.util.List<org.activiti.engine.history.HistoricTaskInstance> list = historyService
                .createHistoricTaskInstanceQuery()
                .processInstanceBusinessKey(bizKey)
                .processInstanceId(processInstanceId)
                .taskDefinitionKeyLike("rv%")
                .finished()
                .taskDeleteReason(ProcessConst.TASK_COMPLETED).list();
        if (list == null) {
            return rvUserId;
        }
        for (final HistoricTaskInstance task : list) {
            final String userId = task.getAssignee();
            if (userId != null) {
                rvUserId.add(userId);
            }
        }
        return rvUserId;
    }

    void replaceReviewer(final String bizKey, final String newUserId, final String oldUserId) {
        final java.util.List<org.activiti.engine.task.Task> taskList = taskService.createTaskQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .processInstanceBusinessKey(bizKey)
                .taskDefinitionKeyLike("rv%")
                .includeTaskLocalVariables()
                .list();
        if (taskList == null) {
            log.error("unable to replace reviewer: bizKey={},newerUser={},oldUserId={}", bizKey, newUserId, oldUserId);
            return;
        }
        for (final Task task : taskList) {
            final String taskId = task.getId();
            if (oldUserId.equals(getUserIdFromIdentityLink(taskId))) {
                taskService.deleteCandidateUser(taskId, oldUserId);
                taskService.addCandidateUser(taskId, newUserId);
            }
        }
    }

    Task getTaskByBizKeyAndTaskDefKey(final String bizKey, final String taskDefKey) {
        Assert.notNull(bizKey, "undefined bizKey");
        Assert.notNull(taskDefKey, "undefined taskDefKey");
        final java.util.List<org.activiti.engine.task.Task> list = taskService.createTaskQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .processInstanceBusinessKey(bizKey)
                .taskDefinitionKey(taskDefKey).list();
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    Map<String, java.util.Set<String>> getBizKeyAndReviewer() {
        final java.util.Map<String, java.util.Set<String>> map = new java.util.HashMap<String, java.util.Set<String>>();
        final java.util.List<org.activiti.engine.runtime.ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(ProcessConst.PROTOCOL_PROCESS_DEF)
                .processInstanceName(IacucStatus.Submit.name())
                .list();
        if (instanceList == null || instanceList.isEmpty()) {
            return map;
        }
        for (final ProcessInstance instance : instanceList) {
            final String bizKey = instance.getBusinessKey();
            final java.util.Set<String> user = getReviewerUserId(bizKey);
            log.info("bizKey={}, user={}", bizKey, user);
            map.put(bizKey, user);
        }
        return map;
    }

    // for user submit a modification case
    void interruptTimerDuration(final String bizKey) {
        final String processInstanceId = getCurrentProtocolProcessInstanceId(bizKey);
        if (processInstanceId != null) {
            final Job timer = managementService
                    .createJobQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            managementService.executeJob(timer.getId());
        }
    }

    String reminderInstanceId(final String bizKey, final Reminder reminder) {
        final ProcessInstance instance = getProcessInstanceByProcessDefKeyAndName(bizKey, ProcessConst.REMINDER_PROCESS_DEF_KEY, reminder.name());
        return instance == null ? null : instance.getId();
    }

    private IacucCorrespondence newIacucCorrespondence() {
        return new IacucCorrespondence();
    }
    private final IacucTaskForm newIacucTaskForm() {
        return new IacucTaskForm();
    }
}
