package edu.columbia.rascal.business.service.review.iacuc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.Assert;

public class IacucDistributeReviewerForm extends IacucTaskForm {

    @Override
    public Map<String, Object> getTaskVariables() {
        final List<String>reviewerList=getReviewerList();
        Assert.notNull(reviewerList);
        Assert.notEmpty(reviewerList);

        final Map<String,Object> map=new HashMap<String, Object>();
        map.put("T1_OUT", IacucStatus.DistributeReviewer.gatewayValue());
        for(int suffix=1; suffix<6; suffix++) {
            map.put("rv"+suffix, null);
        }
        int suffix=0;
        for(final String rv: reviewerList) {
            suffix += 1;
            map.put("rv"+suffix, rv);
        }
        return map;
    }

}
