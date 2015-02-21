package edu.columbia.rascal.business.service.review.iacuc;

import org.junit.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IacucDistributeReviewerForm extends IacucTaskForm {

    @Override
    public Map<String, Object> getTaskVariables() {
        List<String>reviewerList=getReviewerList();
        Assert.assertNotNull(reviewerList);
        Assert.assertNotEquals(true, reviewerList.isEmpty());

        Map<String,Object> map=new HashMap<String, Object>();
        map.put("T1_OUT", IacucStatus.DistributeReviewer.gatewayValue());
        map.put("numOfRvs", reviewerList.size());
        //
        for(int suffix=1; suffix<6; suffix++) {
            map.put("rv"+suffix, null);
        }
        int suffix=0;
        for(String rv: reviewerList) {
            suffix += 1;
            map.put("rv"+suffix, rv);
        }

        return map;
    }

}
