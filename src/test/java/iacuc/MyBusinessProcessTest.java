package iacuc;

import edu.columbia.rascal.business.service.IacucProtocolHeaderService;
import edu.columbia.rascal.business.service.review.iacuc.*;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.task.Task;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static junit.framework.TestCase.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:activiti/springUsageTest-context.xml")
public class MyBusinessProcessTest {

    private static final Logger log = LoggerFactory.getLogger(MyBusinessProcessTest.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private IacucProtocolHeaderService headerService;

    @Test
    // @Deployment(resources = {"org/activiti/test/IacucApprovalProcess.bpmn20.xml"})
    public void test() {

        // log.info("isIacuc={}", AuthMatcher.foo.matchIacucAuthority("IACUC_FOO_BAR"));
        // log.info("isIacuc={}", IacucAuthMatcher.matchIacucAuthority("IACUC_FOO_FOOBAR"));
        // if it doesn't have a valid taskId, getIdentityLinksForTask will throw an exception
        // List<IdentityLink>list=taskService.getIdentityLinksForTask("123");
        // testRedistribute();
        // testKaput();
        dist2SubcommitteeWithAppendix();

    }

    void testKaput() {
        String bizKey = "testKaput";
        String userId = "BobKaput";
        Map<String, Object> processInput = new HashMap<String, Object>();
        processInput.put("START_GATEWAY", IacucStatus.Kaput.gatewayValue());
        processInput.put("kaputCount", 3);
        headerService.startKaputProcess(bizKey, userId, processInput);
        printOpenTaskList(bizKey);
        completeTask(bizKey, userId, IacucStatus.Kaput.taskDefKey(),
                IacucStatus.Kaput.statusName(), "foo1");

        completeTask(bizKey, userId, IacucStatus.Kaput.taskDefKey(),
                IacucStatus.Kaput.statusName(), "foo2");

        completeTask(bizKey, userId, IacucStatus.Kaput.taskDefKey(),
                IacucStatus.Kaput.statusName(), "foo3");

        printOpenTaskList(bizKey);
        printHistory(bizKey);
        assertNull(headerService.getKaputProcessInstanceId(bizKey));
    }

    void testRedistribute() {

        String bizKey1 = "foo";
        String userId = "bob";
        Map<String, Object> processInput = new HashMap<String, Object>();
        processInput.put("hasAppendixA", true);
        processInput.put("hasAppendix", true);
        headerService.startProtocolProcess(bizKey1, userId, processInput);
        printOpenTaskList(bizKey1);
        //
        List<String> rvList = new ArrayList<String>();
        rvList.add("Sam");
        distributeToDesignatedReviewer(bizKey1, "admin", rvList);

        completeTask(bizKey1, userId, IacucStatus.SOPreApproveA.taskDefKey(), IacucStatus.SOPreApproveA.statusName(), "foo");
        printOpenTaskList(bizKey1);

        completeTask(bizKey1, userId, IacucStatus.Rv1Approval.taskDefKey(), IacucStatus.Rv1Approval.statusName(), "foo bar");

        if (headerService.canRedistribute(bizKey1)) {
            completeTask(bizKey1, userId, IacucStatus.Redistribute.taskDefKey(), IacucStatus.Redistribute.statusName(), "redistribution now");
        } else {
            finalApproval(bizKey1, userId);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }


            Map<String, Object> map = new HashMap<String, Object>();
            map.put("remindDate", new Date());
            map.put("START_GATEWAY", Reminder.Day30.gatewayValue());
            headerService.startReminderProcess(bizKey1, userId, map, Reminder.Day30);

            List<Job> timerList = managementService
                    .createJobQuery()
                    .processInstanceId(headerService.getReminderInstanceId(bizKey1, Reminder.Day30))
                    .list();
            Assert.assertNotNull(timerList);
            Job job = timerList.get(0);
            log.info("due date: {}", job.getDuedate());
            // managementService.executeJob(timerList.get(0).getId());
            // have to give some time for unit testing

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
        }

        printOpenTaskList(bizKey1);
        printHistory(bizKey1);
    }


    public void dist2SubcommitteeWithAppendix() {
        String bizKey = "foo";
        String userId = "bob";
        Map<String, Object> hasAppendix = new HashMap<String, Object>();
        hasAppendix.put("hasAppendixA", true);
        headerService.startProtocolProcess(bizKey, userId, hasAppendix);
        //
        distToSubcommittee(bizKey, "admin");
        log.info("after distribute subcommittee open tasks:");
        log.info("taskCount={}", taskCount(bizKey));
        printOpenTaskList(bizKey);
        // approveAppendixA(bizKey, "safetyOfficeDam");
        // holdAppendixB(bizKey, "safetyOfficeHolder");
        // subcommitteeReview(bizKey, "admin");
        // log.info("taskCount={}", taskCount(bizKey));
        // log.info("after return to PI open tasks:");
        // printOpenTaskList(bizKey);
        completeTask(bizKey,
                     "safety officer1",
                     "soPreApproveA",
                     "Safety Office Pre-approve Appendix-A",
                     "testing");
        log.info("after safety officer approved, taskCount={}", taskCount(bizKey));
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


    void distToSubcommittee(final String bizKey, final String user) {
        final IacucDistributeSubcommitteeForm iacucTaskForm = new IacucDistributeSubcommitteeForm();
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

    void printOpenTaskList(final String bizKey) {
        final List<Task> taskList = taskService
                .createTaskQuery()
                .processInstanceBusinessKey(bizKey)
                .list();
        if(taskList.isEmpty()) {
            log.info("No open tasks");
            return;
        }
        log.info("open tasks:");
        for (final Task task : taskList) {
            log.info("taskDefKey={},taskName={}", task.getTaskDefinitionKey(), task.getName());
        }
    }

    void printHistory(String bizKey) {
        log.info("history:");
        List<IacucTaskForm> iacucTaskFormList = headerService.getIacucProtocolHistory(bizKey);
        for (IacucTaskForm form : iacucTaskFormList) {
            log.info(form.toString());
        }
    }

    private void completeTask(final String bizKey,
                              final String userId,
                              final String taskDefKey,
                              final String taskName,
                              final String comment) {
        final IacucTaskForm taskForm = new IacucTaskForm();
        taskForm.setBizKey(bizKey);
        taskForm.setAuthor(userId);
        taskForm.setTaskDefKey(taskDefKey);
        taskForm.setTaskName(taskName);
        taskForm.setComment(comment);
        headerService.completeTaskByTaskForm(taskForm);
    }

}
