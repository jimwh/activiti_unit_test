package edu.columbia.rascal.business.service.review.iacuc;

import java.util.HashMap;
import java.util.Map;

public final class ProcessConst {

    public static final String PROTOCOL_PROCESS_DEF = "IacucApprovalProcess";
    public static final String REMINDER_PROCESS_DEF_KEY = "IacucExpirationReminder";

    public static final String ADVERSE_EVENT_DEF_KEY = "IacucAdverseEvent";
    public static final String START_GATEWAY = "START_GATEWAY";
    public static final String SNAPSHOT = "snapshot";
    public static final String CORRESPONDENCE = "IacucCorrespondence";
    public static final String TASK_FORM_LOOKUP_PREFIX = "iacucTaskForm";
    public static final String TASK_COMPLETED = "completed";

    private static final Map<String, String> AppendixMap = new HashMap<String, String>();
    /**
     * in data migration, the name already converted
     * private static final Map<String, String> CodeToName = new HashMap<String, String>();
     * static {
     * CodeToName.put("ACCMemberApprov", IacucStatus.Rv1Approval.statusName());
     * CodeToName.put("ACCMemberHold", IacucStatus.Rv1Hold.statusName());
     * CodeToName.put("FullReviewReq", IacucStatus.Rv1ReqFullReview.statusName());
     * CodeToName.put("SOPreApproveA", IacucStatus.SOPreApproveA.statusName());
     * CodeToName.put("SOPreApproveB", IacucStatus.SOPreApproveB.statusName());
     * CodeToName.put("SOPreApproveC", IacucStatus.SOPreApproveC.statusName());
     * CodeToName.put("SOPreApproveD", IacucStatus.SOPreApproveD.statusName());
     * CodeToName.put("SOPreApproveE", IacucStatus.SOPreApproveE.statusName());
     * CodeToName.put("SOPreApproveF", IacucStatus.SOPreApproveF.statusName());
     * CodeToName.put("SOPreApproveG", IacucStatus.SOPreApproveG.statusName());
     * CodeToName.put("SOPreApproveI", IacucStatus.SOPreApproveI.statusName());
     * CodeToName.put("SOHoldA", IacucStatus.SOHoldA.statusName());
     * CodeToName.put("SOHoldB", IacucStatus.SOHoldB.statusName());
     * CodeToName.put("SOHoldC", IacucStatus.SOHoldC.statusName());
     * CodeToName.put("SOHoldD", IacucStatus.SOHoldD.statusName());
     * CodeToName.put("SOHoldE", IacucStatus.SOHoldE.statusName());
     * CodeToName.put("SOHoldF", IacucStatus.SOHoldF.statusName());
     * CodeToName.put("SOHoldG", IacucStatus.SOHoldG.statusName());
     * CodeToName.put("SOHoldI", IacucStatus.SOHoldI.statusName());
     * CodeToName.put("ReturnToPI", IacucStatus.ReturnToPI.statusName());
     * }
     */

    // this is for old status lookup in data migration
    private static final Map<String, String> NameToKey = new HashMap<String, String>();

    static {
        AppendixMap.put("A", "hasAppendixA");
        AppendixMap.put("B", "hasAppendixB");
        AppendixMap.put("C", "hasAppendixC");
        AppendixMap.put("D", "hasAppendixD");
        AppendixMap.put("E", "hasAppendixE");
        AppendixMap.put("F", "hasAppendixF");
        AppendixMap.put("G", "hasAppendixG");
        AppendixMap.put("I", "hasAppendixI");
    }

    static {
        NameToKey.put(IacucStatus.Rv1Approval.statusName(), IacucStatus.Rv1Approval.taskDefKey());
        NameToKey.put(IacucStatus.Rv1Hold.statusName(), IacucStatus.Rv1Hold.taskDefKey());
        NameToKey.put(IacucStatus.Rv1ReqFullReview.statusName(), IacucStatus.Rv1ReqFullReview.taskDefKey());
        NameToKey.put(IacucStatus.SOPreApproveA.statusName(), IacucStatus.SOPreApproveA.taskDefKey());
        NameToKey.put(IacucStatus.SOPreApproveB.statusName(), IacucStatus.SOPreApproveB.taskDefKey());
        NameToKey.put(IacucStatus.SOPreApproveC.statusName(), IacucStatus.SOPreApproveC.taskDefKey());
        NameToKey.put(IacucStatus.SOPreApproveD.statusName(), IacucStatus.SOPreApproveD.taskDefKey());
        NameToKey.put(IacucStatus.SOPreApproveE.statusName(), IacucStatus.SOPreApproveE.taskDefKey());
        NameToKey.put(IacucStatus.SOPreApproveF.statusName(), IacucStatus.SOPreApproveF.taskDefKey());
        NameToKey.put(IacucStatus.SOPreApproveG.statusName(), IacucStatus.SOPreApproveG.taskDefKey());
        NameToKey.put(IacucStatus.SOPreApproveI.statusName(), IacucStatus.SOPreApproveI.taskDefKey());
        NameToKey.put(IacucStatus.SOHoldA.statusName(), IacucStatus.SOHoldA.taskDefKey());
        NameToKey.put(IacucStatus.SOHoldB.statusName(), IacucStatus.SOHoldA.taskDefKey());
        NameToKey.put(IacucStatus.SOHoldC.statusName(), IacucStatus.SOHoldA.taskDefKey());
        NameToKey.put(IacucStatus.SOHoldD.statusName(), IacucStatus.SOHoldA.taskDefKey());
        NameToKey.put(IacucStatus.SOHoldE.statusName(), IacucStatus.SOHoldA.taskDefKey());
        NameToKey.put(IacucStatus.SOHoldF.statusName(), IacucStatus.SOHoldA.taskDefKey());
        NameToKey.put(IacucStatus.SOHoldG.statusName(), IacucStatus.SOHoldA.taskDefKey());
        NameToKey.put(IacucStatus.SOHoldI.statusName(), IacucStatus.SOHoldA.taskDefKey());
        NameToKey.put(IacucStatus.ReturnToPI.statusName(), IacucStatus.ReturnToPI.taskDefKey());
    }

    private ProcessConst() {}

    public static String getAppendixMapKey(final String appendixType) {
        return AppendixMap.get(appendixType);
    }

    public static String nameToKey(final String name) {
        return NameToKey.get(name);
    }
}
