// JMRIClientReporterManager.java

package jmri.jmrix.jmriclient;

import org.apache.log4j.Logger;
import jmri.Reporter;

/**
 * Implement reporter manager for JMRIClient systems
 * <P>
 * System names are "prefixnnn", where prefix is the system prefix and
 * nnn is the reporter number without padding.
 *
 * @author	Paul Bender Copyright (C) 2011
 * @version	$Revision$
 */

public class JMRIClientReporterManager extends jmri.managers.AbstractReporterManager {

    private JMRIClientSystemConnectionMemo memo=null;
    private String prefix = null;

    public JMRIClientReporterManager(JMRIClientSystemConnectionMemo memo) {
        this.memo=memo;
        this.prefix=memo.getSystemPrefix();
    }

    public String getSystemPrefix() { return prefix; }

    public Reporter createNewReporter(String systemName, String userName) {
        Reporter t;
        int addr = Integer.valueOf(systemName.substring(prefix.length()+1)).intValue();
        t = new JMRIClientReporter(addr,memo);
        t.setUserName(userName);
        return t;
    }

    static Logger log = Logger.getLogger(JMRIClientReporterManager.class.getName());

}

/* @(#)JMRIClientReporterManager.java */
