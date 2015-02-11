package edu.columbia.rascal.business.auxiliary;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IacucDistributeReviewerForm extends IacucTaskForm {

    private static final Logger log= LoggerFactory.getLogger(IacucDistributeReviewerForm.class);
    @Override
    public Map<String, Object> getTaskVariables() {
        List<String>reviewerList=getReviewerList();
        Assert.assertNotNull(reviewerList);
        Assert.assertNotEquals(true, reviewerList.isEmpty());

        Map<String,Object> map=new HashMap<String, Object>();
        map.put("T1_OUT", IacucStatus.DistributeReviewer.gatewayValue());
        for(int suffix=1; suffix<6; suffix++) {
            map.put("rv"+suffix, null);
        }
        int suffix=0;
        for(String rv: reviewerList) {
            suffix += 1;
            map.put("rv"+suffix, rv);
        }

        for(Map.Entry<String,Object>me: map.entrySet()) {
            log.info("key={}, value={}", me.getKey(),me.getValue());
        }
        return map;
    }

}
