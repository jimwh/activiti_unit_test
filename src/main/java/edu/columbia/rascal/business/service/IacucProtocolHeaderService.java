package edu.columbia.rascal.business.service;

import edu.columbia.rascal.business.service.review.iacuc.IacucStatus;
import edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm;
import edu.columbia.rascal.business.service.review.iacuc.ProcessConst;
import edu.columbia.rascal.business.service.review.iacuc.Reminder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class IacucProtocolHeaderService {
    private static final Logger log = LoggerFactory.getLogger(IacucProtocolHeaderService.class);

    @Resource
    private IacucProcessService processService;

    public Map<String, Date> getBizKeyMeetingDate(final Set<String> set) {
        return processService.getBizKeyMeetingDate(set);
    }

    public void completeTaskByTaskForm(final IacucTaskForm taskForm) {
        processService.completeTaskByTaskForm(ProcessConst.PROTOCOL_PROCESS_DEF, taskForm);
    }

    public void completeTaskByTaskForm(final String processDefKey, IacucTaskForm taskForm) {
        processService.completeTaskByTaskForm(processDefKey, taskForm);
    }

    public List<IacucTaskForm> getIacucProtocolHistory(final String protocolId) {
        return processService.getIacucProtocolHistory(protocolId);
    }

    public void startProtocolProcess(final String bizKey, final String userId) {
        processService.startProtocolProcess(bizKey, userId, new HashMap<String, Object>());
    }

    public void startProtocolProcess(final String bizKey,
                                     final String userId,
                                     final Map<String, Object> processInput) {
        processService.startProtocolProcess(bizKey, userId, processInput);
        IacucTaskForm taskForm = new IacucTaskForm();
        taskForm.setBizKey(bizKey);
        taskForm.setAuthor(userId);
        taskForm.setTaskDefKey(IacucStatus.Submit.taskDefKey());
        taskForm.setTaskName(IacucStatus.Submit.statusName());
        processService.completeTaskByTaskForm(ProcessConst.PROTOCOL_PROCESS_DEF, taskForm);
    }

    public String startKaputProcess(final String protocolId,
                                    final String userId,
                                    final Map<String, Object> processInput) {
        return processService.startKaputProcess(protocolId, userId, processInput);
    }


    public void startReminderProcess(final String bizKey, final String userId,
                                     final Map<String, Object> processInput,
                                     final Reminder reminder) {
        processService.startReminderProcess(bizKey, userId, processInput, reminder);
    }

    public boolean canRedistribute(final String bizKey) {
        return !processService.hasReviewerAction(bizKey);
    }


    public String getProcessInstanceId(final String bizKey) {
        return processService.getCurrentProtocolProcessInstanceId(bizKey);
    }

    public String getKaputProcessInstanceId(final String bizKey) {
        return processService.getCurrentKaputProcessInstanceId(bizKey);
    }

    public void attachSnapshot(final String bizKey, final String taskDefKey) {

    }

    public String getReminderInstanceId(final String bizKey, final Reminder reminder) {
        return processService.reminderInstanceId(bizKey, reminder);
    }

    public void reminder30(final String protocolId) {
        log.info("reminder called ...{}", protocolId);
    }

}