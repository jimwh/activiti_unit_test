package org.activiti;

import java.util.*;

import edu.columbia.rascal.business.service.review.iacuc.*;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

import org.junit.Test;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.columbia.rascal.business.service.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:org/activiti/test/springUsageTest-context.xml")
public class MyBusinessProcessTest {

    private static final Logger log = LoggerFactory.getLogger(MyBusinessProcessTest.class);

    @Autowired
    private TaskService taskService;
    @Autowired
    private IacucProtocolHeaderService headerService;

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
        headerService.startProtocolProcess(bizKey1, userId, processInput);
        printOpenTaskList(bizKey1);
        List<String> rvList = new ArrayList<String>();
        rvList.add("Sam");
        distributeToDesignatedReviewer(bizKey1, "admin", rvList);
        //log.info("allRvs={}", getAllRvs(bizKey1));
        //log.info("numOfRvs={}", getNumOfRvs(bizKey1));
        //log.info("hasReviewerAction={}", hasReviewerAction(bizKey1));
        printOpenTaskList(bizKey1);
        //
        IacucTaskForm taskForm=new IacucTaskForm();
        taskForm.setBizKey(bizKey1);
        taskForm.setAuthor("jj");
        taskForm.setTaskDefKey(IacucStatus.SOPreApproveA.taskDefKey());
        taskForm.setTaskName(IacucStatus.SOPreApproveA.statusName());
        headerService.completeTaskByTaskForm(taskForm);
        printOpenTaskList(bizKey1);


        taskForm=new IacucTaskForm();
        taskForm.setBizKey(bizKey1);
        taskForm.setAuthor("jj");
        taskForm.setTaskDefKey(IacucStatus.Redistribute.taskDefKey());
        taskForm.setTaskName(IacucStatus.Redistribute.statusName());
        headerService.completeTaskByTaskForm(taskForm);
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


    public void fooTest() {

        String bizKey="foo";
        String userId="bob";
        Map<String,Object>hasAppendix=new HashMap<String, Object>();
        hasAppendix.put("hasAppendixA", true);
        hasAppendix.put("hasAppendixB", true);

        headerService.startProtocolProcess(bizKey, userId, hasAppendix);

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
        headerService.startProtocolProcess(bizKey, userId, hasAppendix);
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
        headerService.startProtocolProcess(bizKey, "foo");

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
            headerService.startProtocolProcess(bk, "bob", processMap);


            distToSubcommittee(bk, "admin");
            //approveAppendixA(bizKey, "safetyOfficeDam");
            //holdAppendixB(bizKey, "safetyOfficeHolder");
            // subcommitteeReview(bizKey, "admin");
            //returnToPI(bizKey, "admin");
            //log.info("taskCount={}", taskCount(bizKey));
            //printOpenTaskList(bizKey);
        }
        Set<String>set=new HashSet<String>();
        Map<String,Date>map = headerService.getBizKeyMeetingDate(set);
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
        headerService.completeTaskByTaskForm(iacucTaskForm);
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
        headerService.completeTaskByTaskForm(iacucTaskForm);
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
        headerService.completeTaskByTaskForm(iacucTaskForm);
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
        headerService.completeTaskByTaskForm(iacucTaskForm);
    }

    void approveAppendixA(String bizKey, String user) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("reviewed and approve appendix-A...");
        iacucTaskForm.setTaskName(IacucStatus.SOPreApproveA.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.SOPreApproveA.taskDefKey());
        //completeTaskByTaskForm(iacucTaskForm);
    }

    void approveAppendixB(String bizKey, String user) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("reviewed and approve appendix-B...");
        iacucTaskForm.setTaskName(IacucStatus.SOPreApproveB.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.SOPreApproveB.taskDefKey());
        //completeTaskByTaskForm(iacucTaskForm);
    }

    void holdAppendixA(String bizKey, String user) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("reviewed but hold appendix-A...");
        iacucTaskForm.setTaskName(IacucStatus.SOHoldA.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.SOHoldA.taskDefKey());
        //completeTaskByTaskForm(iacucTaskForm);
    }

    void holdAppendixB(String bizKey, String user) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("reviewed but hold appendix-B...");
        iacucTaskForm.setTaskName(IacucStatus.SOHoldB.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.SOHoldB.taskDefKey());
        //completeTaskByTaskForm(iacucTaskForm);
    }


    void u1Approval(String bizKey, String u1) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(u1);
        iacucTaskForm.setComment("approval is given");
        iacucTaskForm.setTaskName(IacucStatus.Rv1Approval.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.Rv1Approval.taskDefKey());
        //Assert.assertNotNull(getAssigneeTaskByTaskDefKey(bizKey, IacucStatus.Rv1Approval.taskDefKey(), iacucTaskForm.getAuthor()));
        //completeTaskByTaskForm(iacucTaskForm);
    }

    void u2Approval(String bizKey, String u1) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(u1);
        iacucTaskForm.setComment("approval is given");
        iacucTaskForm.setTaskName(IacucStatus.Rv2Approval.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.Rv2Approval.taskDefKey());
        //Assert.assertNotNull(getAssigneeTaskByTaskDefKey(bizKey, IacucStatus.Rv2Approval.taskDefKey(), iacucTaskForm.getAuthor()));
        //completeTaskByTaskForm(iacucTaskForm);
    }

    /*
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

        //completeTaskByTaskForm(iacucTaskForm);
    }
    */

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
        //completeTaskByTaskForm(iacucTaskForm);
    }

    void undoApproval(String bizKey, String user) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("undo approval");
        iacucTaskForm.setTaskName(IacucStatus.UndoApproval.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.UndoApproval.taskDefKey());
        //
        //completeTaskByTaskForm(iacucTaskForm);
    }

    void animalOrder(String bizKey, String user) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("order animal now");
        iacucTaskForm.setTaskName(IacucStatus.AnimalOrder.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.AnimalOrder.taskDefKey());
        //
        //completeTaskByTaskForm(iacucTaskForm);
    }


    long taskCount(String bizKey) {
        return taskService
                .createTaskQuery()
                .processInstanceBusinessKey(bizKey).count();
    }

    void printOpenTaskList(String bizKey) {
        log.info("open tasks:");
        List<Task> taskList = taskService
                .createTaskQuery()
                //.processDefinitionKey(IacucProcessService.ProtocolProcessDefKey)
                .processInstanceBusinessKey(bizKey)
                .list();
        for (Task task : taskList) {
            log.info("taskDefKey={},taskName={}",task.getTaskDefinitionKey(), task.getName());
        }
    }

    void printHistory(String bizKey) {
        List<IacucTaskForm> iacucTaskFormList = headerService.getIacucProtocolHistory(bizKey);
        for (IacucTaskForm form : iacucTaskFormList) {
            log.info(form.toString());
        }
    }


/*
    void printCurrentApprovalStatus(String bizKey) {
        log.info("...............................................................\n");
        log.info("get all tasks from current process including open/closed tasks...");
        List<IacucTaskForm> list = getCurrentApprovalStatus(bizKey);
        for (IacucTaskForm form : list) {
            log.info(form.toString());
        }
        log.info("...............................................................\n");
    }
*/

}
