package edu.columbia.rascal.business.service.review.iacuc;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;

public class IacucTaskForm implements IacucTaskFormBase, Comparable<IacucTaskForm> {

    // all these lookup is for show business

    private static final ImmutableMap<String, String> AppendixTypeAuthoritySuffixLookup =
            new ImmutableMap.Builder<String, String>()
                    .put("soPreApproveA", "APPROVE_A")
                    .put("soPreApproveB", "APPROVE_B")
                    .put("soPreApproveC", "APPROVE_C")
                    .put("soPreApproveD", "APPROVE_D")
                    .put("soPreApproveE", "APPROVE_E")
                    .put("soPreApproveF", "APPROVE_F")
                    .put("soPreApproveG", "APPROVE_G")
                    .put("soPreApproveI", "APPROVE_I")
                    .put("soHoldA", "HOLD_A")
                    .put("soHoldB", "HOLD_B")
                    .put("soHoldC", "HOLD_C")
                    .put("soHoldD", "HOLD_D")
                    .put("soHoldE", "HOLD_E")
                    .put("soHoldF", "HOLD_F")
                    .put("soHoldG", "HOLD_G")
                    .put("soHoldI", "HOLD_I")
                    .build();

    private static final ImmutableSet<String> ReviewerTaskDefKeySet =
            new ImmutableSet.Builder<String>()
                    .add(IacucStatus.Rv1Approval.taskDefKey())
                    .add(IacucStatus.Rv1Hold.taskDefKey())
                    .add(IacucStatus.Rv1ReqFullReview.taskDefKey())
                    .add(IacucStatus.Rv2Approval.taskDefKey())
                    .add(IacucStatus.Rv2Hold.taskDefKey())
                    .add(IacucStatus.Rv2ReqFullReview.taskDefKey())
                    .add(IacucStatus.Rv3Approval.taskDefKey())
                    .add(IacucStatus.Rv3Hold.taskDefKey())
                    .add(IacucStatus.Rv3ReqFullReview.taskDefKey())
                    .add(IacucStatus.Rv4Approval.taskDefKey())
                    .add(IacucStatus.Rv4Hold.taskDefKey())
                    .add(IacucStatus.Rv4ReqFullReview.taskDefKey())
                    .add(IacucStatus.Rv5Approval.taskDefKey())
                    .add(IacucStatus.Rv5Hold.taskDefKey())
                    .add(IacucStatus.Rv5ReqFullReview.taskDefKey())
                    .build();

    private static final ImmutableSet<String> AppendixTaskDefKeySet =
            new ImmutableSet.Builder<String>()
                    .add(IacucStatus.SOPreApproveA.taskDefKey())
                    .add(IacucStatus.SOPreApproveB.taskDefKey())
                    .add(IacucStatus.SOPreApproveC.taskDefKey())
                    .add(IacucStatus.SOPreApproveD.taskDefKey())
                    .add(IacucStatus.SOPreApproveE.taskDefKey())
                    .add(IacucStatus.SOPreApproveF.taskDefKey())
                    .add(IacucStatus.SOPreApproveG.taskDefKey())
                    .add(IacucStatus.SOPreApproveI.taskDefKey())
                    .add(IacucStatus.SOHoldA.taskDefKey())
                    .add(IacucStatus.SOHoldB.taskDefKey())
                    .add(IacucStatus.SOHoldC.taskDefKey())
                    .add(IacucStatus.SOHoldD.taskDefKey())
                    .add(IacucStatus.SOHoldE.taskDefKey())
                    .add(IacucStatus.SOHoldF.taskDefKey())
                    .add(IacucStatus.SOHoldG.taskDefKey())
                    .add(IacucStatus.SOHoldI.taskDefKey())
                    .build();

    private String bizKey;
    private String author;
    private String taskId;
    private String taskDefKey;
    private String taskName;
    private String comment;
    private String commentId;
    private String snapshotId;
    private Date endTime;
    private IacucCorrespondence iacucCorrespondence;
    private List<String> reviewerList = new ArrayList<String>();
    private Date date;
    private boolean showNormalUser;

    public List<String> getReviewerList() {
        return reviewerList;
    }

    public void setReviewerList(final List<String> list) {
        reviewerList = list;
    }

    public String getBizKey() {
        return bizKey;
    }

    public void setBizKey(final String bizKey) {
        this.bizKey = bizKey;
    }

    public Date getEndTime() {
        return this.endTime;
    }

    public void setEndTime(final Date date) {
        this.endTime = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(final String taskId) {
        this.taskId = taskId;
    }

    public String getTaskDefKey() {
        return taskDefKey;
    }

    public void setTaskDefKey(final String taskDefKey) {
        this.taskDefKey = taskDefKey;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(final String taskName) {
        this.taskName = taskName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(final String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public IacucCorrespondence getCorrespondence() {
        return this.iacucCorrespondence;
    }

    public void setCorrespondence(final IacucCorrespondence corr) {
        this.iacucCorrespondence = corr;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public String getCommentId() {
        return this.commentId;
    }

    public void setCommentId(final String cid) {
        this.commentId = cid;
    }

    public String getMeetingDateString() {
        if (date == null) {
            return null;
        }
        final DateTime dateTime = new DateTime(date);
        return dateTime.toString("MM/dd/yyyy");
    }

    public String getEndTimeString() {
        if (endTime == null) {
            return null;
        }
        final DateTime dateTime = new DateTime(endTime);
        return dateTime.toString("MM/dd/yyyy HH:mm:ss");
    }

    @Override
    public Map<String, String> getProperties() {
        final Map<String, String> map = new HashMap<String, String>();

        map.put("bizKey", bizKey);

        map.put("snapshotId", snapshotId);
        map.put("commentId", commentId);

        map.put("author", author);
        map.put("taskName", taskName);
        map.put("taskDefKey", taskDefKey);

        if (date != null) {
            final DateTime dateTime = new DateTime(this.date);
            map.put("date", dateTime.toString());
        }
        if (!reviewerList.isEmpty()) {
            for (int i = 1; i < reviewerList.size() + 1; i++) {
                map.put("rv" + i, reviewerList.get(i - 1));
            }
        }
        //...
        return map;
    }

    @Override
    public void setProperties(final Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return;
        }

        taskName = map.get("taskName");
        taskDefKey = map.get("taskDefKey");
        // this.comment = map.get("comment");
        snapshotId = map.get("snapshotId");
        commentId = map.get("commentId");
        author = map.get("author");
        bizKey = map.get("bizKey");
        if (map.get("date") != null) {
            final String ms = map.get("date");
            final DateTime dateTime = new DateTime(ms);
            this.date = dateTime.toDate();
        }

        for (int i = 1; i < 6; i++) {
            final String rv = map.get("rv" + i);
            if (rv != null) {
                reviewerList.add(rv);
            }
        }
    }

    @Override
    public Map<String, Object> getTaskVariables() {
        return null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("[taskName=").append(taskName)
                .append(",taskDefKey=").append(taskDefKey).append(",snapshotId=").append(snapshotId).append(",bizKey=").append(bizKey)
                .append(",author=").append(author);

        if (comment != null) {
            sb.append(",comment=").append(comment);
        }
        if (date != null) {
            sb.append(",date=").append(getMeetingDateString());
        }
        if (!reviewerList.isEmpty()) {
            sb.append(",reviewerList").append(reviewerList.toString());
        }

        sb.append(String.format(",endTime=%s]", getEndTimeString()));
        if (iacucCorrespondence == null) {
            return sb.toString();
        }
        sb.append(String.format("%n%s%n", iacucCorrespondence.toString()));
        return sb.toString();
    }

    @Override
    public int compareTo(final IacucTaskForm itf) {
        if (this.endTime == null && itf.getEndTime() != null) {
            return -1;
        }
        if (this.endTime != null && itf.getEndTime() == null) {
            return 1;
        }

        return this.endTime.compareTo(itf.getEndTime());
    }

    public boolean getShowNormalUser() {
        return showNormalUser;
    }

    public void setShowNormalUser(final boolean bool) {
        showNormalUser = bool;
    }

    public boolean getIsDesignatedReview() {
        return ReviewerTaskDefKeySet.contains(taskDefKey);
    }

    public boolean getIsAddCorrespondence() {
        return IacucStatus.AddCorrespondence.isDefKey(taskDefKey);
    }

    public boolean getIsSubmission() {
        return IacucStatus.Submit.isDefKey(this.taskDefKey);
    }

    public boolean getIsDistributeReivewer() {
        return IacucStatus.DistributeReviewer.isDefKey(taskDefKey);
    }

    public boolean getIsDistributeSubcommittee() {
        return IacucStatus.DistributeSubcommittee.isDefKey(taskDefKey);
    }

    public boolean getIsSubcommitteeReview() {
        return getIsDistributeSubcommittee();
    }

    public boolean isAddNote() {
        return IacucStatus.AddNote.isDefKey(taskDefKey);
    }

    public String getReviewerListAsString() {
        return reviewerList == null || reviewerList.isEmpty() ? "" :
                reviewerList.toString().replaceAll("\\[|\\]", "");
    }

    public boolean getShowResearcher() {
        if (isAddNote()) {
            return false;
        } else if (getIsDesignatedReview()) {
            return false;
        } else if (getIsAddCorrespondence()) {
            return false;
        }
        return true;
    }


    // all for show business
    public String getAppendixTypeAuthoritySuffix() {
        if (this.taskDefKey == null) {
            return null;
        }
        for (final Map.Entry<String, String> e : AppendixTypeAuthoritySuffixLookup.entrySet()) {
            if (this.taskDefKey.equals(e.getKey())) {
                return e.getValue();
            }
        }
        return null;
    }

    public boolean getIsAppendixTask() {
        return AppendixTaskDefKeySet.contains(taskDefKey);
    }
}
