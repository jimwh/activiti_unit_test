package edu.columbia.rascal.business.service.review.iacuc;


import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

public class IacucDistributeSubcommitteeForm extends IacucTaskForm {

    @Override
    public Map<String,Object>getTaskVariables() {
        Assert.notNull(getDate());
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("T1_OUT", IacucStatus.DistributeSubcommittee.gatewayValue());
        return map;
    }

}
