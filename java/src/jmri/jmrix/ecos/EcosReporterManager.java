// EcosReporterManager.java
package jmri.jmrix.ecos;

import jmri.Reporter;

/**
 * EcosReporterManager implements the ReporterManager.
 * <P>
 * Description:	Implement Reporter manager for ecos
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 */
public class EcosReporterManager extends jmri.managers.AbstractReporterManager {

    // ctor has to register for LocoNet events
    public EcosReporterManager(EcosSystemConnectionMemo memo) {
        this.memo = memo;
    }

    EcosSystemConnectionMemo memo;

    public String getSystemPrefix() {
        return memo.getSystemPrefix();
    }

    public void dispose() {
        super.dispose();
    }

    public Reporter createNewReporter(String systemName, String userName) {
        Reporter r = new EcosReporter(systemName, userName);
        register(r);
        return r;
    }
}

/* @(#)EcosReporterManager.java */
