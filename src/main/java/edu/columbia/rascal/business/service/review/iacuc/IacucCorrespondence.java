package edu.columbia.rascal.business.service.review.iacuc;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@SuppressWarnings("StringBufferReplaceableByString")
public class IacucCorrespondence {

    private static final Logger log = LoggerFactory.getLogger(IacucCorrespondence.class);
    private String id;
    private String from;
    private String recipient;
    private String carbonCopy;
    private String subject;
    private String text;
    private Date creationDate;
    private String fromFirstLastNameUni;
    // just for front show purpose
    private boolean showCorrToUser;

    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(final String fromUni) {
        this.from = fromUni;
    }

    public boolean isValidFrom() {
        return !StringUtils.isBlank(this.from);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public boolean isValidSubject() {
        return !StringUtils.isBlank(this.subject);
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public Date getCreationDate() {
        return this.creationDate == null ? null : new Date(creationDate.getTime());
    }

    public void apply() {
        if (creationDate == null) {
            creationDate = new Date();
            id = String.valueOf(creationDate.getTime());
        }
    }

    public String getFromFirstLastNameUni() {
        return fromFirstLastNameUni;
    }

    public void setFromFirstLastNameUni(final String flu) {
        fromFirstLastNameUni = flu;
    }

    // it is for activity use, not for you
    public boolean isValid() {
        if (StringUtils.isBlank(this.id)) {
            return false;
        }
        if (!isValidFrom()) {
            return false;
        }
        if (!isValidRecipient()) {
            return false;
        }
        return isValidSubject();
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(final String recipient) {
        this.recipient = recipient;
    }

    public boolean isValidRecipient() {
        final List<String> list = getRecipientAsList();
        return !list.isEmpty();
    }

    public List<String> getRecipientAsList() {
        final List<String> list = new ArrayList<String>();
        if (StringUtils.isBlank(recipient)) {
            return list;
        }
        final String noSpaces = removeSpaces(recipient);
        list.addAll(Arrays.asList(noSpaces.split(",")));
        return list;
    }

    private String removeSpaces(final String foo) {
        return foo.replaceAll("\\s+", "");
    }

    public List<String> getCarbonCopyAsList() {
        final List<String> list = new ArrayList<String>();
        if (StringUtils.isBlank(carbonCopy)) {
            return list;
        }
        final String noSpaces = removeSpaces(carbonCopy);
        list.addAll(Arrays.asList(noSpaces.split(",")));
        return list;
    }

    public boolean recipientContains(final String uni) {
        return this.recipient != null && this.recipient.contains(uni);
    }

    public String getCarbonCopy() {
        return carbonCopy;
    }

    public void setCarbonCopy(final String carbonCopy) {
        this.carbonCopy = carbonCopy;
    }

    public boolean carbonCopyContains(final String uni) {
        return this.carbonCopy != null && this.carbonCopy.contains(uni);
    }

    // save data to activity table
    public Map<String, String> getProperties() {
        final Map<String, String> map = new HashMap<String, String>();
        if (StringUtils.isBlank(id)) {
            apply();
        }
        if (StringUtils.isBlank(from)) {
            log.error("empty from uni");
            return map;
        } else if (StringUtils.isBlank(recipient)) {
            log.error("empty recipient");
            return map;
        } else if (StringUtils.isBlank(subject)) {
            log.error("empty subject");
            return map;
        } else if (StringUtils.isBlank(text)) {
            log.error("empty text");
            return map;
        }

        map.put("id", id);
        map.put("from", from);
        map.put("recipient", recipient);
        map.put("subject", subject);
        map.put("text", text);
        map.put("carbonCopy", carbonCopy);
        final DateTime dateTime = new DateTime(creationDate);
        // attention: this is joda DateTime string
        map.put("creationDate", dateTime.toString());
        return map;
    }

    public boolean setProperties(final Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            log.error("empty map");
            return false;
        }
        boolean bool = true;
        this.id = map.get("id");
        if (id == null) {
            log.error("no id");
            bool = false;
        }
        from = map.get("from");
        if (from == null) {
            log.error("no from");
            bool = false;
        }
        recipient = map.get("recipient");
        if (recipient == null) {
            log.error("no recipient");
            bool = false;
        }

        carbonCopy = map.get("carbonCopy");

        subject = map.get("subject");
        if (subject == null) {
            log.error("no subject");
            bool = false;
        }
        text = map.get("text");
        if (text == null) {
            log.error("no body");
            bool = false;
        }
        final String jodaDateTimeString = map.get("creationDate");
        if (jodaDateTimeString == null) {
            log.error("no date");
            bool = false;
        } else {
            creationDate = new DateTime(jodaDateTimeString).toDate();

        }

        return bool;
    }

    public String getDateString() {
        if (creationDate == null) {
            return "";
        }
        final DateTime dateTime = new DateTime(creationDate);
        return dateTime.toString("MM/dd/yyyy HH:mm:ss");
    }

    public boolean getShowCorrToUser() {
        return showCorrToUser;
    }

    public void setShowCorrToUser(final String userId) {
        if (!StringUtils.isBlank(from) && from.contains(userId)) {
            showCorrToUser = true;
        }
        if (!StringUtils.isBlank(recipient) && recipient.contains(userId)) {
            showCorrToUser = true;
        }
        if (!StringUtils.isBlank(carbonCopy) && carbonCopy.contains(userId)) {
            showCorrToUser = true;
        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("id=").append(id)
                .append(",from=").append(from)
                .append(",to=").append(recipient)
                .append(",cc=").append(carbonCopy)
                .append(",subject=").append(subject)
                .append(",body=").append(text)
                .append(",date=").append(creationDate);
        return sb.toString();
    }

}
