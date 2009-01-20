// NceTurnoutManager.java

package jmri.jmrix.nce;

import jmri.Turnout;

/**
 * Implement turnout manager for NCE systems.
 * <P>
 * System names are "NTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 1.10 $
 */
public class NceTurnoutManager extends jmri.AbstractTurnoutManager {

	final java.util.ResourceBundle rbt = java.util.ResourceBundle.getBundle("jmri.jmrix.nce.NceBundle");
	
    public NceTurnoutManager() {
        _instance = this;
    }

    public char systemLetter() { return 'N'; }

    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        Turnout t = new NceTurnout(addr);
        t.setUserName(userName);

        return t;
    }

    static public NceTurnoutManager instance() {
        if (_instance == null) _instance = new NceTurnoutManager();
        return _instance;
    }
    static NceTurnoutManager _instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTurnoutManager.class.getName());
    
    /**
     * Get text to be used for the Turnout.CLOSED state in user communication.
     * Allows text other than "CLOSED" to be use with certain hardware system
     * to represent the Turnout.CLOSED state.
     */
    public String getClosedText() { return rbt.getString("TurnoutStateClosed"); }

     /**
      * Get text to be used for the Turnout.THROWN state in user communication.
      * Allows text other than "THROWN" to be use with certain hardware system
      * to represent the Turnout.THROWN state.
      */
     public String getThrownText() { return rbt.getString("TurnoutStateThrown"); }

}

/* @(#)NceTurnoutManager.java */
