// LnReporterManager.java

package jmri.jmrix.loconet;

import jmri.JmriException;
import jmri.Reporter;

/**
 * LnReporterManager implements the ReporterManager.
 * <P>
 * System names are "LRnnn", where nnn is the Reporter number without padding.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 * <P>
 * Description:		Implement Reporter manager for loconet
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version         $Revision: 1.1 $
 */

public class LnReporterManager extends jmri.AbstractReporterManager implements LocoNetListener {

    // ctor has to register for LocoNet events
    public LnReporterManager() {
        _instance = this;
        if (LnTrafficController.instance() != null)
            LnTrafficController.instance().addLocoNetListener(~0, this);
        else
            log.error("No layout connection, Reporter manager can't function");
    }

    public char systemLetter() { return 'L'; }

    public void dispose() {
        if (LnTrafficController.instance() != null)
            LnTrafficController.instance().removeLocoNetListener(~0, this);
        super.dispose();
    }

    public Reporter createNewReporter(String systemName, String userName) {
        Reporter t;
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        t = new LnReporter(addr);
        t.setUserName(userName);
        t.addPropertyChangeListener(this);

        return t;
    }

    // listen for transponder messages, creating Reporters as needed
    public void message(LocoNetMessage l) {
         // check message type
		if (l.getOpCode() != 0xD0) return;
		if ( (l.getElement(1) & 0xC0) != 0) return;

		// message type OK, check address
        int addr = (l.getElement(1)&0x1F)*128 + l.getElement(2) +1;
		
		LnReporter r = (LnReporter) provideReporter("LR"+addr);
		r.message(l);	// make sure it got the message
    }

    static public LnReporterManager instance() {
        if (_instance == null) _instance = new LnReporterManager();
        return _instance;
    }
    static LnReporterManager _instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnReporterManager.class.getName());
}

/* @(#)LnReporterManager.java */
