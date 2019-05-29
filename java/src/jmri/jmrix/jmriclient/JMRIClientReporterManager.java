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

    private JMRIClientSystemConnectionMemo memo = null;
    private String prefix = null;

    public JMRIClientReporterManager(JMRIClientSystemConnectionMemo memo) {
        this.memo = memo;
        this.prefix = memo.getSystemPrefix();
    }

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    @Override
    public Reporter createNewReporter(String systemName, String userName) {
        Reporter t;
        int addr = Integer.parseInt(systemName.substring(prefix.length() + 1));
        t = new JMRIClientReporter(addr, memo);
        t.setUserName(userName);
        return t;
    }

}
