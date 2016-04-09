package edu.columbia.rascal.business.service.review.iacuc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("StringBufferReplaceableByString")
public class IacucAdminForm {

    private String adminNote;
    
    private List<String> reviewerList = new ArrayList<String>();
    private List<String> noActionReviewerList = new ArrayList<String>();
    private IacucCorrespondence correspondence;
    private Date approvalDate;
    private Date effectiveDate;
    private Date endDate;
    private boolean validateEndDate;

    public List<String> getReviewerList() { return reviewerList; }

    // ATTENSION: don't remove the set method, it is used in JSP check-box to collect data
    public void setReviewerList(final List<String> list) { reviewerList = list; }

    public List<String> getNoActionReviewerList() { return noActionReviewerList; }

    // ATTENSION: don't remove the set method, it is used in JSP check-box to collect data
    public void setNoActionReviewerList(final List<String> list) { noActionReviewerList = list; }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(final String adminNote) {
        this.adminNote = adminNote;
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(final Date approvalDate) {
        this.approvalDate = approvalDate;
    }



    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(final Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public IacucCorrespondence getCorrespondence() {
        return correspondence;
    }

    public void setCorrespondence(final IacucCorrespondence correspondence) {
        this.correspondence = correspondence;
    }

    public void setValidateEndDate(final boolean bool) {
        this.validateEndDate = bool;
    }

    public boolean validateEndDate() {
        return this.validateEndDate;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[adminNote=").append(adminNote)
                .append(",approvalDate=")
                .append(approvalDate)
                .append(",endDate=")
                .append(endDate)
                .append("]");
        return sb.toString();
    }
}
