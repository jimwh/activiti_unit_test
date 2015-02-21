package org.activiti;

import java.util.*;

import edu.columbia.rascal.business.service.review.iacuc.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyUnitTest {

    private static final Logger log = LoggerFactory.getLogger(MyUnitTest.class);
    private static final String ProcessDefKey = "IacucApprovalProcess";
    private static final String START_GATEWAY = "START_GATEWAY";

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test
    @Deployment(resources = {"org/activiti/test/IacucApprovalProcess.bpmn20.xml"})
    public void test() {
        testRedistribute();
    }
    public void testRedistribute() {
        String bizKey1="foo";
        String userId="bob";
        Map<String, Object>processInput=new HashMap<String, Object>();
        processInput.put("hasAppendixA", true);
        processInput.put("hasAppendix", true);
        startProtocolProcess(bizKey1, userId, processInput);
        printOpenTaskList(bizKey1);
        //
        List<String> rvList = new ArrayList<String>();
        rvList.add("Sam");
        distributeToDesignatedReviewer(bizKey1, "admin", rvList);
        log.info("allRvs={}", getAllRvs(bizKey1));
        log.info("numOfRvs={}", getNumOfRvs(bizKey1));
        log.info("hasReviewerAction={}", hasReviewerAction(bizKey1));
        printOpenTaskList(bizKey1);

        /*
        IacucTaskForm taskForm=new IacucTaskForm();
        taskForm.setBizKey(bizKey1);
        taskForm.setAuthor("jj");
        taskForm.setTaskDefKey(IacucStatus.SOPreApproveA.taskDefKey());
        taskForm.setTaskName(IacucStatus.SOPreApproveA.statusName());
        completeTaskByTaskForm(taskForm);
        printOpenTaskList(bizKey1);
        */

        IacucTaskForm taskForm=new IacucTaskForm();
        taskForm.setBizKey(bizKey1);
        taskForm.setAuthor("jj");
        taskForm.setTaskDefKey(IacucStatus.Redistribute.taskDefKey());
        taskForm.setTaskName(IacucStatus.Redistribute.statusName());
        completeTaskByTaskForm(taskForm);
        printOpenTaskList(bizKey1);

        /*
        returnToPI(bizKey1, "admin");
        //
        String bizKey2="foo1";
        String userId1="Tom";
        startProtocolProcess(bizKey2, userId1);
        rvList = new ArrayList<String>();
        rvList.add("Sam");
        distributeToDesignatedReviewer(bizKey2, "admin", rvList);
        u1Approval(bizKey2, "Sam");
        log.info("foo={}", getAllRvs(bizKey2));
        returnToPI(bizKey2, "admin");

        HistoricProcessInstance hp1=activitiRule.getHistoryService()
                .createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(bizKey1)
                .finished()
                .singleResult();
        log.info("startUser1={}", hp1.getStartUserId());

        HistoricProcessInstance hp2=activitiRule.getHistoryService()
                .createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(bizKey2)
                .finished()
                .singleResult();
        log.info("startUser2={}", hp2.getStartUserId());
        */

        /*
        u2Approval(bizKey, "Dave");
        printOpenTaskList(bizKey);
        finalApproval(bizKey, "admin");
        printOpenTaskList(bizKey);
        // undoApproval(bizKey, "admin");
        animalOrder(bizKey, "admin");
        */
        //printOpenTaskList(bizKey);
        //printHistory(bizKey);

    }


    boolean getAllRvs(String bizKey) {
        ProcessInstance instance=activitiRule.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceBusinessKey(bizKey)
                .includeProcessVariables()
                .singleResult();
        Map<String,Object>map=instance.getProcessVariables();
        return (Boolean)map.get("allRvs");
    }

    int getNumOfRvs(String bizKey) {
        ProcessInstance instance=activitiRule.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceBusinessKey(bizKey)
                .includeProcessVariables()
                .singleResult();
        Map<String,Object>map=instance.getProcessVariables();
        return (Integer)map.get("numOfRvs");
    }

    boolean hasReviewerAction(String bizKey) {
        String processInstanceId=getCurrentProcessInstanceId(bizKey);
        HistoricTaskInstanceQuery query=
        activitiRule.getHistoryService().createHistoricTaskInstanceQuery()
                .finished()
                .processInstanceBusinessKey(bizKey)
                .processInstanceId(processInstanceId)
                .taskDefinitionKeyLike("rv%");
        return query!=null && !query.list().isEmpty() && query.list().isEmpty();
    }

    public void fooTest() {

        String bizKey="foo";
        String userId="bob";
        Map<String,Object>hasAppendix=new HashMap<String, Object>();
        hasAppendix.put("hasAppendixA", true);
        hasAppendix.put("hasAppendixB", true);

        startProtocolProcess(bizKey, userId, hasAppendix);

        // distribute it to reviewers
        List<String> rvList = new ArrayList<String>();
        rvList.add("Sam");
        rvList.add("Dave");
        distributeToDesignatedReviewer(bizKey, "admin", rvList);
        //printOpenTaskList(bizKey);

        // reviewer approval
        // u1Approval(bizKey, "Sam");
        approveAppendixA(bizKey, "safetyOfficeDam");
        //holdAppendixB(bizKey, "safetyOfficeHolder");
        approveAppendixB(bizKey, "safetyOfficeHolder");
        // u2Approval(bizKey, "Dave");

        printOpenTaskList(bizKey);

        // u2Hold(bizKey, "Dave");

        //finalApproval(bizKey, "admin");
        // undoApproval(bizKey, "admin");
        // returnToPI(bizKey, "admin");

        //printCurrentApprovalStatus(bizKey);

        // try{ Thread.sleep(5000);}catch(InterruptedException e){}

        //printCurrentApprovalStatus(bizKey);

        //log.info("taskCount={}", taskCount(bizKey));

        //printHistory(bizKey);

    }

    public void dist2SubcommitteeWithAppendix() {
        String bizKey="foo";
        String userId="bob";
        Map<String,Object>hasAppendix=new HashMap<String, Object>();
        hasAppendix.put("hasAppendixA", true);
        startProtocolProcess(bizKey, userId, hasAppendix);
        distToSubcommittee(bizKey, "admin");
        log.info("after distribute subcommittee open tasks:");
        printOpenTaskList(bizKey);
        //approveAppendixA(bizKey, "safetyOfficeDam");
        //holdAppendixB(bizKey, "safetyOfficeHolder");
        //subcommitteeReview(bizKey, "admin");
        returnToPI(bizKey, "admin");
        log.info("taskCount={}", taskCount(bizKey));
        log.info("after return to PI open tasks:");
        printOpenTaskList(bizKey);
    }

    public void noAppendix() {
        String bizKey="foo";
        startProtocolProcess(bizKey, "foo");

        distToSubcommittee(bizKey, "admin");
        //approveAppendixA(bizKey, "safetyOfficeDam");
        //holdAppendixB(bizKey, "safetyOfficeHolder");
        // subcommitteeReview(bizKey, "admin");
        //returnToPI(bizKey, "admin");
        log.info("taskCount={}", taskCount(bizKey));
        printOpenTaskList(bizKey);
    }

    public void foo() {

        String[] bizKeys ={"1", "2", "3", "4", "5"};
        for(String bk: bizKeys) {
            // for appendix if any
            Map<String, Object> processMap = new HashMap<String, Object>();
            processMap.put("hasAppendix", false);
            processMap.put("hasAppendixA", false);
            processMap.put("hasAppendixB", false);
            processMap.put("hasAppendixC", false);
            processMap.put("hasAppendixD", false);
            processMap.put("hasAppendixE", false);
            processMap.put("hasAppendixF", false);
            processMap.put("hasAppendixG", false);
            processMap.put("hasAppendixI", false);
            startProtocolProcess(bk, "bob", processMap);


            distToSubcommittee(bk, "admin");
            //approveAppendixA(bizKey, "safetyOfficeDam");
            //holdAppendixB(bizKey, "safetyOfficeHolder");
            // subcommitteeReview(bizKey, "admin");
            //returnToPI(bizKey, "admin");
            //log.info("taskCount={}", taskCount(bizKey));
            //printOpenTaskList(bizKey);
        }
        Set<String>set=new HashSet<String>();
        Map<String,Date>map = getBizKeyMeetingDate(set);
        for(String bizKey: set){
            log.info("bizKey={}", bizKey);
        }
        for(Map.Entry<String,Date>e: map.entrySet()){
            log.info("bizKey={}, meetingDate={}", e.getKey(),e.getValue());
        }
        //
    }

    void submit(String bizKey) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor("bob");
        iacucTaskForm.setTaskName(IacucStatus.Submit.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.Submit.taskDefKey());
        completeTaskByTaskForm(iacucTaskForm);
    }

    void returnToPI(String bizKey, String user) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("no choice but back to PI...");
        iacucTaskForm.setTaskName(IacucStatus.ReturnToPI.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.ReturnToPI.taskDefKey());
        //
        IacucCorrespondence corr1 = new IacucCorrespondence();
        corr1.setFrom(user);
        corr1.setRecipient("PI");
        corr1.setCarbonCopy("Co-PI");
        corr1.setSubject("Notification of Return to PI from David");
        corr1.setText("Question about your protocol bla bla ...");
        corr1.apply();
        iacucTaskForm.setCorrespondence(corr1);
        //
        completeTaskByTaskForm(iacucTaskForm);
        // completed return-2-pi, there is no task and no instance
        // Assert.assertEquals(0, taskCount(bizKey));
        // Assert.assertNull(getProtocolProcessInstance(bizKey));
    }

    void distToSubcommittee(String bizKey, String user) {
        IacucDistributeSubcommitteeForm iacucTaskForm = new IacucDistributeSubcommitteeForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("distribute protocol to subcommittee");
        iacucTaskForm.setTaskName(IacucStatus.DistributeSubcommittee.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.DistributeSubcommittee.taskDefKey());
        iacucTaskForm.setDate(new Date());
        completeTaskByTaskForm(iacucTaskForm);
    }


    void distributeToDesignatedReviewer(String bizKey, String user, List<String> reviewerList) {
        IacucDistributeReviewerForm iacucTaskForm = new IacucDistributeReviewerForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("distribute to " + reviewerList);
        iacucTaskForm.setTaskName(IacucStatus.DistributeReviewer.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.DistributeReviewer.taskDefKey());
        iacucTaskForm.setReviewerList(reviewerList);
        //
        IacucCorrespondence corr = new IacucCorrespondence();
        corr.setFrom("Freemen");
        corr.setRecipient("sam");
        corr.setCarbonCopy("cameron");
        corr.setSubject("notification of distribution");
        corr.setText("complete review asap ...");
        corr.apply();
        iacucTaskForm.setCorrespondence(corr);
        completeTaskByTaskForm(iacucTaskForm);
    }

    void approveAppendixA(String bizKey, String user) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("reviewed and approve appendix-A...");
        iacucTaskForm.setTaskName(IacucStatus.SOPreApproveA.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.SOPreApproveA.taskDefKey());
        completeTaskByTaskForm(iacucTaskForm);
    }

    void approveAppendixB(String bizKey, String user) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("reviewed and approve appendix-B...");
        iacucTaskForm.setTaskName(IacucStatus.SOPreApproveB.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.SOPreApproveB.taskDefKey());
        completeTaskByTaskForm(iacucTaskForm);
    }

    void holdAppendixA(String bizKey, String user) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("reviewed but hold appendix-A...");
        iacucTaskForm.setTaskName(IacucStatus.SOHoldA.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.SOHoldA.taskDefKey());
        completeTaskByTaskForm(iacucTaskForm);
    }

    void holdAppendixB(String bizKey, String user) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("reviewed but hold appendix-B...");
        iacucTaskForm.setTaskName(IacucStatus.SOHoldB.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.SOHoldB.taskDefKey());
        completeTaskByTaskForm(iacucTaskForm);
    }


    void u1Approval(String bizKey, String u1) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(u1);
        iacucTaskForm.setComment("approval is given");
        iacucTaskForm.setTaskName(IacucStatus.Rv1Approval.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.Rv1Approval.taskDefKey());
        Assert.assertNotNull(getAssigneeTaskByTaskDefKey(bizKey, IacucStatus.Rv1Approval.taskDefKey(), iacucTaskForm.getAuthor()));
        completeTaskByTaskForm(iacucTaskForm);
    }

    void u2Approval(String bizKey, String u1) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(u1);
        iacucTaskForm.setComment("approval is given");
        iacucTaskForm.setTaskName(IacucStatus.Rv2Approval.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.Rv2Approval.taskDefKey());
        Assert.assertNotNull(getAssigneeTaskByTaskDefKey(bizKey, IacucStatus.Rv2Approval.taskDefKey(), iacucTaskForm.getAuthor()));
        completeTaskByTaskForm(iacucTaskForm);
    }

    void u2Hold(String bizKey, String u) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(u);
        iacucTaskForm.setComment("u2 is hold");
        iacucTaskForm.setTaskName(IacucStatus.Rv2Hold.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.Rv2Hold.taskDefKey());
        log.info("u2...................{}", iacucTaskForm.getAuthor());
        printOpenTaskList(bizKey);
        Task task = getAssigneeTaskByTaskDefKey(bizKey, IacucStatus.Rv2Hold.taskDefKey(), iacucTaskForm.getAuthor());
        if (task == null)
            log.error("task is null");

        Assert.assertNotNull(getAssigneeTaskByTaskDefKey(bizKey, IacucStatus.Rv2Hold.taskDefKey(), iacucTaskForm.getAuthor()));

        completeTaskByTaskForm(iacucTaskForm);
    }

    void finalApproval(String bizKey, String user) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("final approval");
        iacucTaskForm.setTaskName(IacucStatus.FinalApproval.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.FinalApproval.taskDefKey());
        //
        IacucCorrespondence corr = new IacucCorrespondence();
        corr.setFrom(user);
        corr.setRecipient("pi");
        corr.setCarbonCopy("co-pi");
        corr.setSubject("notification of approval");
        corr.setText("Your protocol has been approved.");
        corr.apply();
        iacucTaskForm.setCorrespondence(corr);
        completeTaskByTaskForm(iacucTaskForm);
    }

    void undoApproval(String bizKey, String user) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("undo approval");
        iacucTaskForm.setTaskName(IacucStatus.UndoApproval.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.UndoApproval.taskDefKey());
        //
        completeTaskByTaskForm(iacucTaskForm);
    }

    void animalOrder(String bizKey, String user) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("order animal now");
        iacucTaskForm.setTaskName(IacucStatus.AnimalOrder.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.AnimalOrder.taskDefKey());
        //
        completeTaskByTaskForm(iacucTaskForm);
    }

    void completeTaskByTaskForm(IacucTaskForm iacucTaskForm) {
        Assert.assertNotNull(iacucTaskForm);
        Task task = getTaskByTaskDefKey(iacucTaskForm.getBizKey(), iacucTaskForm.getTaskDefKey());
        Assert.assertNotNull(task);
        TaskService taskService = activitiRule.getTaskService();
        String taskId = task.getId();
        taskService.claim(taskId, iacucTaskForm.getAuthor());

        // if you want to store comment in activity task comment, then... otherwise do nothing
        String content = iacucTaskForm.getComment();
        if (content != null) {
            Comment comment = taskService.addComment(taskId, task.getProcessInstanceId(), iacucTaskForm.getTaskDefKey(), content);
            iacucTaskForm.setCommentId(comment.getId());
        }

        // attach attribute to this task
        Map<String, String> attribute = iacucTaskForm.getProperties();
        if (attribute != null && !attribute.isEmpty())
            taskService.setVariableLocal(taskId, "iacucTaskForm" + taskId, attribute);

        if( IacucStatus.DistributeSubcommittee.isDefKey(iacucTaskForm.getTaskDefKey())) {
            if (iacucTaskForm instanceof IacucDistributeSubcommitteeForm) {
                log.info("meeting data: {}", iacucTaskForm.getDate());
                taskService.setVariable(taskId,"meetingDate", iacucTaskForm.getDate());

            }
        }
        // attach corr to this task
        IacucCorrespondence corr = iacucTaskForm.getCorrespondence();
        if (corr != null) {
            Map<String, String> corrProperties = corr.getProperties();
            if (!corrProperties.isEmpty()) {
                taskService.setVariableLocal(taskId, "IacucCorrespondence" + taskId, corrProperties);
            }
        }

        // determine the direction
        Map<String, Object> map = iacucTaskForm.getTaskVariables();
        if (map != null && !map.isEmpty())
            taskService.complete(taskId, map); // go left/right/middle or go ...
        else
            taskService.complete(taskId); // go straight
    }

    List<IacucTaskForm> getHistoricTaskByBizKey(String bizKey) {
        List<IacucTaskForm> listIacucTaskForm = new ArrayList<IacucTaskForm>();
        // from main process
        List<IacucTaskForm> mainList = getListIacucTaskForm(getFromMainProcess(bizKey));
        listIacucTaskForm.addAll(mainList);
        // from sub-process
        List<IacucTaskForm> subList = getListIacucTaskForm(getFromSubProcess(bizKey));
        listIacucTaskForm.addAll(subList);
        //
        Collections.sort(listIacucTaskForm, Collections.reverseOrder());
        return listIacucTaskForm;
    }


    List<HistoricTaskInstance> getFromMainProcess(String bizKey) {
        HistoryService historyService = activitiRule.getHistoryService();
        HistoricTaskInstanceQuery query = historyService
                .createHistoricTaskInstanceQuery()
                .processDefinitionKey(ProcessDefKey)
                .processInstanceBusinessKey(bizKey)
                .finished()
                .taskDeleteReason("completed")
                .includeTaskLocalVariables()
                .orderByHistoricTaskInstanceEndTime()
                .desc();
        return query.list();

    }

    List<HistoricTaskInstance> getFromSubProcess(String bizKey) {
        HistoryService historyService = activitiRule.getHistoryService();
        HistoricTaskInstanceQuery query = historyService
                .createHistoricTaskInstanceQuery()
                .processDefinitionKey("appendix-process") // sub-process def key
                        //.processInstanceBusinessKey(bizKey) not for sub process
                .processVariableValueEquals("BusinessKey", bizKey) // use proc var
                .finished()
                .taskDeleteReason("completed")
                .includeTaskLocalVariables()
                .orderByHistoricTaskInstanceEndTime()
                .desc();
        return query.list();
    }


    List<IacucTaskForm> getListIacucTaskForm(List<HistoricTaskInstance> list) {

        List<IacucTaskForm> listIacucTaskForm = new ArrayList<IacucTaskForm>();
        for (HistoricTaskInstance hs : list) {
            IacucTaskForm iacucTaskForm = new IacucTaskForm();
            iacucTaskForm.setTaskId(hs.getId());
            iacucTaskForm.setEndTime(hs.getEndTime());

            //
            // iacucTaskForm.setTaskDefKey(hs.getTaskDefinitionKey());
            // iacucTaskForm.setTaskName(hs.getName());
            //
            Map<String, Object> localMap = hs.getTaskLocalVariables();
            //noinspection unchecked
            Map<String, String> taskMap = (Map<String, String>) localMap.get("iacucTaskForm" + hs.getId());

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

            // restore the original correspondence if any
            //noinspection unchecked
            Map<String, String> corrMap = (Map<String, String>) localMap.get("IacucCorrespondence" + hs.getId());
            if (corrMap != null && !corrMap.isEmpty()) {
                IacucCorrespondence corr = new IacucCorrespondence();
                corr.setProperties(corrMap);
                iacucTaskForm.setCorrespondence(corr);
            }
            listIacucTaskForm.add(iacucTaskForm);
        }
        return listIacucTaskForm;
    }

    String getCommentText(String commentId) {
        if (commentId == null) return null;
        Comment comment = activitiRule.getTaskService().getComment(commentId);
        return comment != null ? comment.getFullMessage() : null;
    }


    Task getTaskByTaskDefKey(String bizKey, String defKey) {
        TaskService taskService = activitiRule.getTaskService();
        return taskService.createTaskQuery()
                .processInstanceBusinessKey(bizKey)
                .taskDefinitionKey(defKey)
                .singleResult();
    }

    Task getAssigneeTaskByTaskDefKey(String bizKey, String defKey, String assignee) {
        TaskService taskService = activitiRule.getTaskService();
        return taskService.createTaskQuery()
                .processInstanceBusinessKey(bizKey)
                .taskDefinitionKey(defKey)
                .taskAssignee(assignee)
                .singleResult();
    }

    Task getAssigneeTaskByTaskName(String bizKey, String taskName, String assignee) {
        TaskService taskService = activitiRule.getTaskService();
        return taskService.createTaskQuery()
                .processInstanceBusinessKey(bizKey)
                .taskName(taskName)
                .taskAssignee(assignee)
                .singleResult();
    }

    long taskCount(String bizKey) {
        return activitiRule.getTaskService()
                .createTaskQuery()
                .processInstanceBusinessKey(bizKey).count();
    }

    void printOpenTaskList(String bizKey) {
        log.info("open tasks:");
        List<Task> taskList = activitiRule.getTaskService()
                .createTaskQuery()
                .processDefinitionKey(ProcessDefKey)
                .processInstanceBusinessKey(bizKey)
                .list();
        for (Task task : taskList) {
            log.info("taskDefKey={},taskName={}",task.getTaskDefinitionKey(), task.getName());
        }
    }

    void printHistory(String bizKey) {
        List<IacucTaskForm> iacucTaskFormList = getHistoricTaskByBizKey(bizKey);
        for (IacucTaskForm form : iacucTaskFormList) {
            log.info(form.toString());
        }
    }

    // protocol approval process
    void startProtocolProcess(String bizKey, String userId) {
        activitiRule.getIdentityService().setAuthenticatedUserId(userId);
        ProcessInstance instance = getProtocolProcessInstance(bizKey);
        Assert.assertNull("dude i am expecting null", instance);
        Map<String, Object>processMap=new HashMap<String, Object>();
        processMap.put(START_GATEWAY, IacucStatus.Submit.gatewayValue());
        // for call activity
        processMap.put("BusinessKey", bizKey);
        ProcessInstance processInstance = starProcess(bizKey,userId, processMap, IacucStatus.Submit.name());
        Assert.assertNotNull(processInstance);
        submit(bizKey);
    }

    void startProtocolProcess(String bizKey, String userId, Map<String, Object> processMap) {
        Assert.assertNotNull("dude can't be null", processMap);

        ProcessInstance instance = getProtocolProcessInstance(bizKey);
        Assert.assertNull("dude i am expecting null", instance);

        processMap.put(START_GATEWAY, IacucStatus.Submit.gatewayValue());
        // for call activity
        processMap.put("BusinessKey", bizKey);
        ProcessInstance processInstance = starProcess(bizKey,userId, processMap, IacucStatus.Submit.name());
        Assert.assertNotNull(processInstance);
        submit(bizKey);
    }

    ProcessInstance getProtocolProcessInstance(String bizKey) {
        return getProcessInstanceByName(bizKey, IacucStatus.Submit.name());
    }

    // add correspondence process
    void startCorrProcess(String bizKey, String userId) {
        ProcessInstance instance = getCorrProcessInstance(bizKey);
        Assert.assertNull("dude", instance);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(START_GATEWAY, IacucStatus.AddCorrespondence.gatewayValue());
        starProcess(bizKey, userId, map, IacucStatus.AddCorrespondence.name());
    }

    ProcessInstance getCorrProcessInstance(String bizKey) {
        return getProcessInstanceByName(bizKey, IacucStatus.AddCorrespondence.name());
    }

    // add note process
    void startAddNoteProcess(String bizKey, String userId) {
        ProcessInstance instance = getNoteProcessInstance(bizKey);
        Assert.assertNull("dude", instance);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(START_GATEWAY, IacucStatus.AddNote.gatewayValue());
        starProcess(bizKey, userId, map, IacucStatus.AddNote.name());
    }

    ProcessInstance getNoteProcessInstance(String bizKey) {
        return getProcessInstanceByName(bizKey, IacucStatus.AddNote.name());
    }

    ProcessInstance getProcessInstanceByName(String bizKey, String instanceName) {
        return activitiRule.getRuntimeService()
                .createProcessInstanceQuery()
                .processDefinitionKey(ProcessDefKey)
                .processInstanceBusinessKey(bizKey)
                .processInstanceName(instanceName)
                .includeProcessVariables()
                .singleResult();
    }

    // terminal protocol
    void startTerminateProtocolProcess(String bizKey, String userId) {
        ProcessInstance instance = getProtocolProcessInstance(bizKey);
        Assert.assertNull("dude ", instance);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(START_GATEWAY, IacucStatus.Terminate.gatewayValue());
        starProcess(bizKey, userId,map, IacucStatus.Terminate.name());
    }

    ProcessInstance starProcess(String bizKey, String userId, Map<String, Object> map, String instanceName) {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        activitiRule.getIdentityService().setAuthenticatedUserId(userId);
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(ProcessDefKey, bizKey, map);
        runtimeService.setProcessInstanceName(instance.getProcessInstanceId(), instanceName);
        return instance;
    }

    String getCurrentProcessInstanceId(String bizKey) {
        ProcessInstance instance = getProtocolProcessInstance(bizKey);

        return instance != null ? instance.getProcessInstanceId() : null;
    }


    List<IacucTaskForm> getCurrentApprovalStatus(String bizKey) {
        List<IacucTaskForm> listIacucTaskForm = new ArrayList<IacucTaskForm>();
        String processId = getCurrentProcessInstanceId(bizKey);
        if (processId == null) return listIacucTaskForm;

        HistoryService historyService = activitiRule.getHistoryService();
        HistoricTaskInstanceQuery query = historyService
                .createHistoricTaskInstanceQuery()
                        // .processDefinitionKey(ProcessDefKey)
                .processInstanceBusinessKey(bizKey)
                // for sub-process
                //.processVariableValueEquals("BusinessKey", bizKey)
                        // .processInstanceId(processId)
                .includeTaskLocalVariables()
                .orderByHistoricTaskInstanceEndTime()
                .orderByTaskCreateTime()
                .desc();
        List<HistoricTaskInstance> list = query.list();

        for (HistoricTaskInstance hs : list) {
            if ("deleted".equals(hs.getDeleteReason())) continue;
            IacucTaskForm iacucTaskForm = new IacucTaskForm();
            iacucTaskForm.setTaskId(hs.getId());
            iacucTaskForm.setEndTime(hs.getEndTime());
            iacucTaskForm.setTaskDefKey(hs.getTaskDefinitionKey());
            iacucTaskForm.setTaskName(hs.getName());
            //
            Map<String, Object> localMap = hs.getTaskLocalVariables();
            //noinspection unchecked
            Map<String, String> taskMap = (Map<String, String>) localMap.get("iacucTaskForm" + hs.getId());

            // restore the original attribute
            iacucTaskForm.setProperties(taskMap);
            iacucTaskForm.setComment(getCommentText(iacucTaskForm.getCommentId()));
            listIacucTaskForm.add(iacucTaskForm);
        }
        return listIacucTaskForm;
    }

    void printCurrentApprovalStatus(String bizKey) {
        log.info("...............................................................\n");
        log.info("get all tasks from current process including open/closed tasks...");
        List<IacucTaskForm> list = getCurrentApprovalStatus(bizKey);
        for (IacucTaskForm form : list) {
            log.info(form.toString());
        }
        log.info("...............................................................\n");
    }

    /**
     *
     * @param bizKeys fill out protocolId
     * @return Map<String,Date> protocolId, meeting date
     */
    Map<String,Date> getBizKeyMeetingDate(Set<String> bizKeys) {
        Map<String,Date>bizKeyMeetingDate=new HashMap<String, Date>();
        List<ProcessInstance> list=activitiRule.getRuntimeService()
                .createProcessInstanceQuery()
                .processDefinitionKey(ProcessDefKey)
                .processInstanceName(IacucStatus.Submit.name())
                .includeProcessVariables()
                .list();
        for (ProcessInstance instance:list) {
            String bizKey=instance.getBusinessKey();
            bizKeys.add(bizKey);
            Map<String,Object>map=instance.getProcessVariables();
            if( map==null || map.isEmpty()) continue;
            if( map.get("meetingDate")!=null ) {
                Date date = (Date) map.get("meetingDate");
                bizKeyMeetingDate.put(bizKey, date);
            }
        }
        return bizKeyMeetingDate;
    }

}
