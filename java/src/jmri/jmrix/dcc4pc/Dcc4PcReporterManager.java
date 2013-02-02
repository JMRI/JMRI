// Dcc4PcReporterManager.java

package jmri.jmrix.dcc4pc;

import org.apache.log4j.Logger;
import jmri.Reporter;

/**
 * Dcc4PcReporterManager implements the ReporterManager.
 * <P>
 * Description:		Implement Reporter manager for dcc4pc
 * @author			Kevin Dickerson Copyright (C) 2012
 * @version         $Revision: 17977 $
 */

public class Dcc4PcReporterManager extends jmri.managers.AbstractReporterManager {

    // ctor has to register for LocoNet events
    public Dcc4PcReporterManager(Dcc4PcTrafficController tc, Dcc4PcSystemConnectionMemo memo) {
        this.memo = memo;
        this.tc = tc;
    }

    Dcc4PcTrafficController tc;
    Dcc4PcSystemConnectionMemo memo;
    
    public String getSystemPrefix() { return memo.getSystemPrefix(); }

    public void dispose() {
        super.dispose();
    }

    public Reporter createNewReporter(String systemName, String userName) {
        Reporter r = new Dcc4PcReporter(systemName, userName);
        register(r);
        return r;
    }

    static Logger log = Logger.getLogger(Dcc4PcReporterManager.class.getName());
}

/* @(#)Dcc4PcReporterManager.java */
