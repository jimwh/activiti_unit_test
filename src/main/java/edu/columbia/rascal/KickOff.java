package edu.columbia.rascal;

import edu.columbia.rascal.business.service.IacucProcessService;
import edu.columbia.rascal.business.service.IacucProtocolHeaderService;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KickOff {

    @Resource
    private IacucProcessService processService;

    @Resource
    private IacucProtocolHeaderService headerService;

    private static final Logger log = LoggerFactory.getLogger(KickOff.class);
    public void startUp() {
        log.info("startup now ...{}, {}", processService!=null, headerService!=null);
    }
}
