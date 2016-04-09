package edu.columbia.rascal;

import edu.columbia.rascal.business.service.IacucProcessService;
import edu.columbia.rascal.business.service.IacucProtocolHeaderService;
import edu.columbia.rascal.business.service.review.iacuc.IacucTaskForm;
import java.util.List;
import javax.annotation.Resource;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AppTaskQuery {

    @Resource
    private IacucProcessService processService;

    @Resource
    private IacucProtocolHeaderService headerService;

    private static final Logger log = LoggerFactory.getLogger(AppTaskQuery.class);
    public void startUp() {
        final String bizKey = "1050";
        log.info("startup now ...{}, {}", processService!=null, headerService!=null);
        final List<IacucTaskForm> list = headerService.getIacucProtocolHistory(bizKey);
        for(final IacucTaskForm form: list) {
            log.info("task:{}, {}", form.getTaskDefKey(), form.getEndTimeString());
        }

        final List<Task> taskList = processService.getOpenTaskByBizKey(bizKey);
        for(final Task task: taskList) {
            log.info("taskName={}, id={}", task.getName(), task.getId());
        }
    }
}
