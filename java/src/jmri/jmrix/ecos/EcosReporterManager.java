package jmri.jmrix.ecos;

import jmri.Reporter;

/**
 * EcosReporterManager implements the ReporterManager for ECoS
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class EcosReporterManager extends jmri.managers.AbstractReporterManager {

    // ctor has to register for ECoS events
    public EcosReporterManager(EcosSystemConnectionMemo memo) {
        this.memo = memo;
    }

    EcosSystemConnectionMemo memo;

    @Override
    public String getSystemPrefix() {
        return memo.getSystemPrefix();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public Reporter createNewReporter(String systemName, String userName) {
        Reporter r = new EcosReporter(systemName, userName);
        register(r);
        return r;
    }

}
