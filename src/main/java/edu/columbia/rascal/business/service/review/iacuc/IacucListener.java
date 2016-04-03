package edu.columbia.rascal.business.service.review.iacuc;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import edu.columbia.rascal.business.service.IacucProtocolHeaderService;
import javax.annotation.Resource;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IacucListener implements TaskListener, ExecutionListener {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(IacucListener.class);

    private static final String AllRvs = "allRvs";
    private static final String hasAppendixA = "hasAppendixA";
    private static final String hasAppendixB = "hasAppendixB";
    private static final String hasAppendixC = "hasAppendixC";
    private static final String hasAppendixD = "hasAppendixD";
    private static final String hasAppendixE = "hasAppendixE";
    private static final String hasAppendixF = "hasAppendixF";
    private static final String hasAppendixG = "hasAppendixG";
    private static final String hasAppendixI = "hasAppendixI";

    private static final String AllAppendicesApproved = "allAppendicesApproved";
    private static final String appendixAApproved = "aApproved";
    private static final String appendixBApproved = "bAApproved";
    private static final String appendixCApproved = "cApproved";
    private static final String appendixDApproved = "dApproved";
    private static final String appendixEApproved = "eApproved";
    private static final String appendixFApproved = "fApproved";
    private static final String appendixGApproved = "gApproved";
    private static final String appendixIApproved = "iApproved";

    private static final String[] approvedName = {
            appendixAApproved,
            appendixBApproved,
            appendixCApproved,
            appendixDApproved,
            appendixEApproved,
            appendixFApproved,
            appendixGApproved,
            appendixIApproved};

    private static final String CanRedistribute = "canRedistribute";
    private static final String Redistribute = "redistribute";
    private static final String UndoApproval = "undoApproval";


    private static final ImmutableMap<String, Boolean> UndoMap =
            new ImmutableMap.Builder<String, Boolean>()
                    .put(IacucStatus.ReturnToPI.taskDefKey(), false)
                    .put(IacucStatus.UndoApproval.taskDefKey(), true)
                    .put(IacucStatus.FinalApproval.taskDefKey(), false)
                    .build();

    private static final ImmutableSet<String> RvApprovalCannotDistributeSet =
            new ImmutableSet.Builder<String>()
                    .add(IacucStatus.Rv1Approval.taskDefKey())
                    .add(IacucStatus.Rv2Approval.taskDefKey())
                    .add(IacucStatus.Rv3Approval.taskDefKey())
                    .add(IacucStatus.Rv4Approval.taskDefKey())
                    .add(IacucStatus.Rv5Approval.taskDefKey())
                    .build();

    private static final ImmutableMap<String, String> SoApproveMap =
            new ImmutableMap.Builder<String, String>()
                    .put(IacucStatus.SOPreApproveA.taskDefKey(), appendixAApproved)
                    .put(IacucStatus.SOPreApproveB.taskDefKey(), appendixBApproved)
                    .put(IacucStatus.SOPreApproveC.taskDefKey(), appendixCApproved)
                    .put(IacucStatus.SOPreApproveD.taskDefKey(), appendixDApproved)
                    .put(IacucStatus.SOPreApproveE.taskDefKey(), appendixEApproved)
                    .put(IacucStatus.SOPreApproveF.taskDefKey(), appendixFApproved)
                    .put(IacucStatus.SOPreApproveG.taskDefKey(), appendixGApproved)
                    .put(IacucStatus.SOPreApproveI.taskDefKey(), appendixIApproved)
                    .build();

    private static final ImmutableSet<String> SoHoldSet =
            new ImmutableSet.Builder<String>()
                    .add(IacucStatus.SOHoldA.taskDefKey())
                    .add(IacucStatus.SOHoldB.taskDefKey())
                    .add(IacucStatus.SOHoldC.taskDefKey())
                    .add(IacucStatus.SOHoldD.taskDefKey())
                    .add(IacucStatus.SOHoldE.taskDefKey())
                    .add(IacucStatus.SOHoldF.taskDefKey())
                    .add(IacucStatus.SOHoldG.taskDefKey())
                    .add(IacucStatus.SOHoldI.taskDefKey()).build();

    private static final ImmutableSet<String> RvHoldOrReqFullRvSet =
            new ImmutableSet.Builder<String>()
                    .add(IacucStatus.Rv1Hold.taskDefKey())
                    .add(IacucStatus.Rv1ReqFullReview.taskDefKey())
                    .add(IacucStatus.Rv2Hold.taskDefKey())
                    .add(IacucStatus.Rv2ReqFullReview.taskDefKey())
                    .add(IacucStatus.Rv3Hold.taskDefKey())
                    .add(IacucStatus.Rv3ReqFullReview.taskDefKey())
                    .add(IacucStatus.Rv4Hold.taskDefKey())
                    .add(IacucStatus.Rv4ReqFullReview.taskDefKey())
                    .add(IacucStatus.Rv5Hold.taskDefKey())
                    .add(IacucStatus.Rv5ReqFullReview.taskDefKey())
                    .build();

    @Resource
    private IacucProtocolHeaderService headerService;

    /**
     * Task listener will be called  by activity
     */
    @Override
    public void notify(final DelegateTask delegateTask) {

        if (!delegateTask.getProcessDefinitionId().contains("IacucApprovalProcess")) {
            return;
        }

        final String eventName = delegateTask.getEventName();
        if (EVENTNAME_CREATE.equals(eventName)) {
            onCreate(delegateTask);
        } else if (EVENTNAME_COMPLETE.equals(eventName)) {
            try {
                onComplete(delegateTask);
            } catch (Exception e) {
                log.error("caught: ", e);
                throw new ActivitiIllegalArgumentException(e.getMessage());
            }
        }
    }

    private void onCreate(final DelegateTask delegateTask) {
        final DelegateExecution taskExecution = delegateTask.getExecution();
        final String bizKey = taskExecution.getProcessBusinessKey();
        final String processId = taskExecution.getProcessInstanceId();
        final String taskId = delegateTask.getId();
        final String taskDefKey = delegateTask.getTaskDefinitionKey();
        log.info("create: bizKey={}, taskDefKey={}, taskId={}, processId={}",
                bizKey, taskDefKey, taskId, processId);
    }

    private void onComplete(final DelegateTask delegateTask) throws Exception {

        final DelegateExecution taskExecution = delegateTask.getExecution();
        final String bizKey = taskExecution.getProcessBusinessKey();
        final String taskDefKey = delegateTask.getTaskDefinitionKey();
        attachSnapshot(taskDefKey, bizKey);
        /*
        if (!IacucStatus.FinalApproval.isDefKey(taskDefKey) && headerService!=null) {
            // header service may be null during unit test
                headerService.attachSnapshot(bizKey, taskDefKey);
        }
        */
        /*
        if (IacucStatus.DistributeReviewer.isDefKey(taskDefKey)) {
            taskExecution.setVariable("hasReviewer", true);
            taskExecution.setVariable(CanRedistribute, true);
        }
        */
        updateReviewer(taskDefKey, taskExecution);

        if (IacucStatus.Redistribute.isDefKey(taskDefKey)) {
            if (!(Boolean) taskExecution.getVariable(CanRedistribute)) {
                // enforce you can't complete this task
                throw new ActivitiIllegalArgumentException("Illegal action.");
            }
            taskExecution.setVariable(Redistribute, true);
        } else if (UndoMap.containsKey(taskDefKey)) {
            // do nothing if user action
            if (taskExecution.getVariable("userClosed") == null) {
                taskExecution.setVariable(UndoApproval, UndoMap.get(taskDefKey));
            }
        } else if (RvHoldOrReqFullRvSet.contains(taskDefKey)) {
            taskExecution.setVariable(AllRvs, false);
            taskExecution.setVariable(CanRedistribute, false);
            taskExecution.setVariable(Redistribute, false);
        } else if (RvApprovalCannotDistributeSet.contains(taskDefKey)) {
            taskExecution.setVariable(CanRedistribute, false);
            taskExecution.setVariable(Redistribute, false);
        } else if (SoApproveMap.containsKey(taskDefKey)) {
            taskExecution.setVariable(SoApproveMap.get(taskDefKey), true);
            updateAppendixApproveStatus(delegateTask);
        } else if (SoHoldSet.contains(taskDefKey)) {
            taskExecution.setVariable(AllAppendicesApproved, false);
        }
    }

    private void updateReviewer(final String taskDefKey, final DelegateExecution taskExecution) {
        if (IacucStatus.DistributeReviewer.isDefKey(taskDefKey)) {
            taskExecution.setVariable("hasReviewer", true);
            taskExecution.setVariable(CanRedistribute, true);
        }
    }

    private void attachSnapshot(final String taskDefKey, final String bizKey) {
        if (!IacucStatus.FinalApproval.isDefKey(taskDefKey) && headerService != null) {
            // header service may be null during unit test
            headerService.attachSnapshot(bizKey, taskDefKey);
        }
    }

    private void updateAppendixApproveStatus(final DelegateTask delegateTask) {
        for (final String name : approvedName) {
            if (!(Boolean) delegateTask.getVariable(name)) {
                return;
            }
        }
        delegateTask.setVariable(AllAppendicesApproved, true);
    }


    /**
     * Execution listener will be called  by activity
     */
    @Override
    public void notify(final DelegateExecution delegateExecution) throws Exception {
        if (!delegateExecution.getProcessDefinitionId().contains("IacucApprovalProcess")) {
            return;
        }
        final String eventName = delegateExecution.getEventName();
        if (EVENTNAME_START.equals(eventName)) {
            onStart(delegateExecution);
        }
    }

    // on process start
    private void onStart(final DelegateExecution delegateExecution) throws Exception {

        final ExecutionEntity thisEntity = (ExecutionEntity) delegateExecution;
        final ExecutionEntity superExecEntity = thisEntity.getSuperExecution();
        final String eventName = delegateExecution.getEventName();

        if (superExecEntity == null) {
            setUpAppendixApproveStatus(delegateExecution);
            // get the business key of the main process
            log.info("main process: eventName={}, bizKey={}, procDefId={}", eventName, thisEntity.getBusinessKey(), thisEntity.getProcessDefinitionId());
            // used by designatedReviews output
            thisEntity.setVariable(AllRvs, true);
            thisEntity.setVariable("redistribute", false);

        } else {
            // in a sub-process so get the BusinessKey variable set by the caller.
            final String key = (String) superExecEntity.getVariable("BusinessKey");
            final boolean hasAppendix = (Boolean) superExecEntity.getVariable("hasAppendix");
            log.info("sub-process: eventName={}, bizKey={}, procDefId={}, hasAppendix={}",
                    eventName, key, thisEntity.getProcessDefinitionId(), hasAppendix);
            thisEntity.setVariable("BusinessKey", key);
            // for get task by business key
            thisEntity.setBusinessKey(key);
        }
    }

    private boolean updateAppendixStatus(final boolean bool,
                                         final DelegateExecution exe,
                                         final String hasAppendix,
                                         final String approved) {
        boolean retBool = bool;
        if (exe.getVariable(hasAppendix) == null) {
            exe.setVariable(hasAppendix, false);
            exe.setVariable(approved, true);
        } else if ((Boolean) exe.getVariable(hasAppendix)) {
            exe.setVariable(approved, false);
            retBool = false;
        } else {
            exe.setVariable(approved, true);
        }
        return retBool;
    }

    private void setUpAppendixApproveStatus(final DelegateExecution exe) {
        boolean bool = true;
        bool = updateAppendixStatus(bool, exe, hasAppendixA, appendixAApproved);
        bool = updateAppendixStatus(bool, exe, hasAppendixB, appendixBApproved);
        bool = updateAppendixStatus(bool, exe, hasAppendixC, appendixCApproved);
        bool = updateAppendixStatus(bool, exe, hasAppendixD, appendixDApproved);
        bool = updateAppendixStatus(bool, exe, hasAppendixE, appendixEApproved);
        bool = updateAppendixStatus(bool, exe, hasAppendixF, appendixFApproved);
        bool = updateAppendixStatus(bool, exe, hasAppendixG, appendixGApproved);
        bool = updateAppendixStatus(bool, exe, hasAppendixI, appendixIApproved);

        exe.setVariable(AllAppendicesApproved, bool);
        exe.setVariable("hasAppendix", !bool);
        log.info("{}={}", AllAppendicesApproved, bool);
    }


    public void expirationReminder(final DelegateExecution execution) throws Exception {
        if (Reminder.Day30.isServiceTaskId(execution.getCurrentActivityId())) {
            reminder30(execution);
        }
    }


    private void reminder30(final DelegateExecution execution) throws Exception {
        int retries = 0;
        while (true) {
            try {
                log.info("retries={}, bizKey={}, currentActivityId={}",
                        retries,
                        execution.getProcessBusinessKey(),
                        execution.getCurrentActivityId()
                );
                headerService.reminder30(execution.getProcessBusinessKey());
                log.info("call successful...");
                return;
            } catch (Exception e) {
                if (retries == 3) {
                    throw new BpmnError("ReminderException");
                }
                retries += 1;
            }
            //
            try {
                Thread.sleep(8);
            } catch (InterruptedException e) {
            }
        }
    }


}
