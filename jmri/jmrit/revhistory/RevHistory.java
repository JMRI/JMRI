package jmri.jmrit.revhistory;

import java.util.ArrayList;

/**
 * Memo class to remember a revision history.
 * <p>
 * These can be nested:  A revision can come with a history.
 *
 * @author Bob Jacobsen  Copyright (c) 2010
 * @version $Revision: 1.1 $
 */
    
public class RevHistory {
    
    ArrayList<Revision> list = new ArrayList<Revision>();

    /**
     * Used to add a revision form
     * complete information created elsewhere
     */
    public void addRevision(
        int revnumber,
        String date,
        String authorinitials,
        String revremark,
        RevHistory history
        )
    {
        Revision r = new Revision();
        r.revnumber = revnumber;
        r.date = date;
        r.authorinitials = authorinitials;
        r.revremark = revremark;
        r.history = history;
        
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
        )
    {
        addRevision(maxNumber()+1, (new java.util.Date()).toString(),
                    authorinitials, revremark, null);
    }
    
    public int maxNumber() {
        int retval = 0;  // zero if none yet
        for (int i = 0; i < list.size(); i++) {
            retval = ( list.get(i).revnumber > retval ) ? list.get(i).revnumber : retval;
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
    public void addRevision(String revremark, RevHistory h) {
        addRevision(maxNumber()+1, (new java.util.Date()).toString(),
                    System.getProperty("user.name"), revremark, h);
    }
    
    public String toString(String prefix) {
        String retval = "";
        for (int i = 0; i < list.size(); i++) {
            Revision r = list.get(i);
            retval += prefix+r.revnumber+", "+r.date+", "+r.authorinitials+", "+r.revremark+"\n";
            if (r.history != null) {
                retval += r.history.toString(prefix+"    ");
            }
        }
        return retval;
    }
    
    public String toString() {
        return toString("");
    }
    
    public ArrayList<Revision> getList() { return list; }
    
    /**
     * Memo class for each revision itself
     */
    public class Revision {
        public int revnumber;
        public String date;
        public String authorinitials;
        public String revremark;
        public RevHistory history;
    }

}
    
