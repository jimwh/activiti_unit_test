package edu.columbia.rascal.business.service;

import edu.columbia.rascal.business.service.review.iacuc.IacucStatus;
import edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class IacucProtocolHeaderService {

    @Resource
    private IacucProcessService processService;

    public Map<String, Date> getBizKeyMeetingDate(Set<String> set) {
        return processService.getBizKeyMeetingDate(set);
    }

    public void completeTaskByTaskForm(IacucTaskForm taskForm) {
        processService.completeTaskByTaskForm(IacucProcessService.ProtocolProcessDefKey, taskForm);
    }

    public void completeTaskByTaskForm(String processDefKey, IacucTaskForm taskForm) {
        processService.completeTaskByTaskForm(processDefKey, taskForm);
    }

    public List<IacucTaskForm> getIacucProtocolHistory(String protocolId) {
        return processService.getIacucProtocolHistory(protocolId);
    }

    public void startProtocolProcess(String bizKey, String userId) {
        processService.startProtocolProcess(bizKey, userId, new HashMap<String, Object>());
    }

    public void startProtocolProcess(String bizKey, String userId, Map<String, Object> processInput) {
        processService.startProtocolProcess(bizKey, userId, processInput);
        IacucTaskForm taskForm=new IacucTaskForm();
        taskForm.setBizKey(bizKey);
        taskForm.setAuthor(userId);
        taskForm.setTaskDefKey(IacucStatus.Submit.taskDefKey());
        taskForm.setTaskName(IacucStatus.Submit.statusName());
        processService.completeTaskByTaskForm(IacucProcessService.ProtocolProcessDefKey,taskForm);
    }

    public boolean canRedistribute(String bizKey) {
        return processService.canRedistribute(bizKey);
    }
}
