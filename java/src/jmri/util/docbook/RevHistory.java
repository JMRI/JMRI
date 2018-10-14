package jmri.util.docbook;

import java.util.ArrayList;

/**
 * Memo class to remember a revision history.
 * <p>
 * These can be nested: A revision can come with a history.
 *
 * @author Bob Jacobsen Copyright (c) 2010
 */
public class RevHistory {

    ArrayList<Revision> list = new ArrayList<>();

    /**
     * Used to add a revision form complete information created elsewhere
     *
     * @param revnumber      the revision number
     * @param date           the revision date
     * @param authorinitials the author's initials
     * @param revremark      the revision
     */
    public void addRevision(int revnumber, String date, String authorinitials, String revremark) {
        Revision r = new Revision();
        r.revnumber = revnumber;
        r.date = date;
        r.authorinitials = authorinitials;
        r.revremark = revremark;

        list.add(r);
    }

    public void addRevision(Revision r) {
        list.add(r);
    }

    /**
     * Usual form.
     *
     * Dated now, with the next number.
     *
     * @param authorinitials the author's initials
     * @param revremark      the revision
     */
    public void addRevision(String authorinitials, String revremark) {
        addRevision(maxNumber() + 1, (new java.util.Date()).toString(), authorinitials, revremark);
    }

    public int maxNumber() {
        int retval = 0;  // zero if none yet
        for (int i = 0; i < list.size(); i++) {
            retval = (list.get(i).revnumber > retval) ? list.get(i).revnumber : retval;
        }
        return retval;
    }

    /**
     * Add a revision, credited to the current user
     *
     * @param revremark the revision
     */
    public void addRevision(String revremark) {
        addRevision(System.getProperty("user.name"), revremark);
    }

    public String toString(String prefix) {
        StringBuilder retval = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            Revision r = list.get(i);
            retval.append(prefix)
                    .append(r.revnumber)
                    .append(", ")
                    .append(r.date)
                    .append(", ")
                    .append(r.authorinitials)
                    .append(", ")
                    .append(r.revremark)
                    .append("\n");
        }
        return retval.toString();
    }

    @Override
    public String toString() {
        return toString("");
    }

    public ArrayList<Revision> getList() {
        return list;
    }

}
