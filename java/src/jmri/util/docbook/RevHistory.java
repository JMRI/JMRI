package jmri.util.docbook;

import java.util.ArrayList;

/**
 * Memo class to remember a revision history.
 * <p>
 * These can be nested: A revision can come with a history.
 *
 * @author Bob Jacobsen Copyright (c) 2010
 * @version $Revision$
 */
public class RevHistory {

    ArrayList<Revision> list = new ArrayList<Revision>();

    /**
     * Used to add a revision form complete information created elsewhere
     */
    public void addRevision(
            int revnumber,
            String date,
            String authorinitials,
            String revremark
    ) {
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
     */
    public void addRevision(
            String authorinitials,
            String revremark
    ) {
        addRevision(maxNumber() + 1, (new java.util.Date()).toString(),
                authorinitials, revremark);
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
     */
    public void addRevision(String revremark) {
        addRevision(
                System.getProperty("user.name"),
                revremark);
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION")
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    public String toString(String prefix) {
        String retval = "";
        for (int i = 0; i < list.size(); i++) {
            Revision r = list.get(i);
            retval += prefix + r.revnumber + ", " + r.date + ", " + r.authorinitials + ", " + r.revremark + "\n";
        }
        return retval;
    }

    public String toString() {
        return toString("");
    }

    public ArrayList<Revision> getList() {
        return list;
    }

}
