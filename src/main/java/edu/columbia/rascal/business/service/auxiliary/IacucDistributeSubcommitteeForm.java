package edu.columbia.rascal.business.service.auxiliary;

import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class IacucDistributeSubcommitteeForm extends IacucTaskForm {

    @Override
    public Map<String,Object>getTaskVariables() {
        Assert.assertNotNull(getDate());
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("T1_OUT", IacucStatus.DistributeSubcommittee.gatewayValue());
        return map;
    }

}
