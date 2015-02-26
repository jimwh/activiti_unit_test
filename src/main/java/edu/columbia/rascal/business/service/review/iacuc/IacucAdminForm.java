package edu.columbia.rascal.business.service.review.iacuc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IacucAdminForm {

    private String adminNote;

    private List<String> reviewerList = new ArrayList<String>();
    private List<String> noActionReviewerList = new ArrayList<String>();
    private IacucCorrespondence correspondence;
    private Date approvalDate;
    private Date effectiveDate;
    private Date endDate;
    private boolean validateEndDate = false;

    public List<String> getReviewerList() {
        return reviewerList;
    }

    public void setReviewerList(List<String> list) {
        reviewerList = list;
    }

    public List<String> getNoActionReviewerList() {
        return noActionReviewerList;
    }

    public void setNoActionReviewerList(List<String> list) {
        noActionReviewerList = list;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public IacucCorrespondence getCorrespondence() {
        return correspondence;
    }

    public void setCorrespondence(IacucCorrespondence correspondence) {
        this.correspondence = correspondence;
    }

    public void setValidateEndDate(boolean bool) {
        this.validateEndDate = bool;
    }

    public boolean validateEndDate() {
        return this.validateEndDate;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[adminNote=").append(adminNote)
                .append(",approvalDate=")
                .append(approvalDate)
                .append(",endDate=")
                .append(endDate)
                .append("]");
        return sb.toString();
    }
}
