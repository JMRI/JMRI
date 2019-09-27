package jmri.jmrix.jmriclient;

import jmri.Reporter;

/**
 * Implement reporter manager for JMRIClient systems
 * <p>
 * System names are "prefixnnn", where prefix is the system prefix and nnn is
 * the reporter number without padding.
 *
 * @author Paul Bender Copyright (C) 2011
 */
public class JMRIClientReporterManager extends jmri.managers.AbstractReporterManager {

    public JMRIClientReporterManager(JMRIClientSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JMRIClientSystemConnectionMemo getMemo() {
        return (JMRIClientSystemConnectionMemo) memo;
    }

    @Override
    public Reporter createNewReporter(String systemName, String userName) {
        Reporter t;
        int addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        t = new JMRIClientReporter(addr, getMemo());
        t.setUserName(userName);
        return t;
    }

}
