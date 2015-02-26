package iacuc;

import edu.columbia.rascal.business.service.IacucProtocolHeaderService;
import edu.columbia.rascal.business.service.review.iacuc.*;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:activiti/springUsageTest-context.xml")
public class MyBusinessProcessTest {

    private static final Logger log = LoggerFactory.getLogger(MyBusinessProcessTest.class);

    @Autowired
    private TaskService taskService;
    @Autowired
    private IacucProtocolHeaderService headerService;

    @Test
    // @Deployment(resources = {"org/activiti/test/IacucApprovalProcess.bpmn20.xml"})
    public void test() {
        testRedistribute();
    }

    public void testRedistribute() {

        String bizKey1 = "foo";
        String userId = "bob";
        Map<String, Object> processInput = new HashMap<String, Object>();
        processInput.put("hasAppendixA", true);
        processInput.put("hasAppendix", true);
        headerService.startProtocolProcess(bizKey1, userId, processInput);
        printOpenTaskList(bizKey1);

        List<String> rvList = new ArrayList<String>();
        rvList.add("Sam");
        distributeToDesignatedReviewer(bizKey1, "admin", rvList);
        completeTask(bizKey1, userId, IacucStatus.SOPreApproveA.taskDefKey(), IacucStatus.SOPreApproveA.statusName(), "foo");
        printOpenTaskList(bizKey1);

        completeTask(bizKey1, userId, IacucStatus.Rv1Approval.taskDefKey(), IacucStatus.Rv1Approval.statusName(), "foo bar");

        if (headerService.canRedistribute(bizKey1)) {
            completeTask(bizKey1, userId, IacucStatus.Redistribute.taskDefKey(), IacucStatus.Redistribute.statusName(), "redistribution now");
        }

        printOpenTaskList(bizKey1);
        returnToPI(bizKey1, "admin");

    }


    public void dist2SubcommitteeWithAppendix() {
        String bizKey = "foo";
        String userId = "bob";
        Map<String, Object> hasAppendix = new HashMap<String, Object>();
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

    public void foo() {
        String[] bizKeys = {"1", "2", "3", "4", "5"};
        for (String bk : bizKeys) {
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
        Set<String> set = new HashSet<String>();
        Map<String, Date> map = headerService.getBizKeyMeetingDate(set);
        for (String bizKey : set) {
            log.info("bizKey={}", bizKey);
        }
        for (Map.Entry<String, Date> e : map.entrySet()) {
            log.info("bizKey={}, meetingDate={}", e.getKey(), e.getValue());
        }
    }

    void returnToPI(String bizKey, String user) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("no choice but back to PI...");
        iacucTaskForm.setTaskName(IacucStatus.ReturnToPI.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.ReturnToPI.taskDefKey());
        IacucCorrespondence corr1 = new IacucCorrespondence();
        corr1.setFrom(user);
        corr1.setRecipient("PI");
        corr1.setCarbonCopy("Co-PI");
        corr1.setSubject("Notification of Return to PI from David");
        corr1.setText("Question about your protocol bla bla ...");
        corr1.apply();
        iacucTaskForm.setCorrespondence(corr1);
        headerService.completeTaskByTaskForm(iacucTaskForm);
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

    void finalApproval(String bizKey, String user) {
        IacucTaskForm iacucTaskForm = new IacucTaskForm();
        iacucTaskForm.setBizKey(bizKey);
        iacucTaskForm.setAuthor(user);
        iacucTaskForm.setComment("final approval");
        iacucTaskForm.setTaskName(IacucStatus.FinalApproval.statusName());
        iacucTaskForm.setTaskDefKey(IacucStatus.FinalApproval.taskDefKey());
        IacucCorrespondence corr = new IacucCorrespondence();
        corr.setFrom(user);
        corr.setRecipient("pi");
        corr.setCarbonCopy("co-pi");
        corr.setSubject("notification of approval");
        corr.setText("Your protocol has been approved.");
        corr.apply();
        iacucTaskForm.setCorrespondence(corr);
        headerService.completeTaskByTaskForm(iacucTaskForm);
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
                .processInstanceBusinessKey(bizKey)
                .list();
        for (Task task : taskList) {
            log.info("taskDefKey={},taskName={}", task.getTaskDefinitionKey(), task.getName());
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

    private void completeTask(String bizKey, String userId, String taskDefKey, String taskName, String comment) {
        IacucTaskForm taskForm = new IacucTaskForm();
        taskForm.setBizKey(bizKey);
        taskForm.setAuthor(userId);
        taskForm.setTaskDefKey(taskDefKey);
        taskForm.setTaskName(taskName);
        taskForm.setComment(comment);
        headerService.completeTaskByTaskForm(taskForm);
    }

}
