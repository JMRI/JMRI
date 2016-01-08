package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.managers.AbstractReporterManager;
import jmri.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the CBUS-specific Reporter implementation.
 *
 * System names are "MRnnn", where nnn is the reporter number without padding.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author Mark Riddoch Copyright (C) 2015
 * @version $Revision$
 */
public class CbusReporterManager extends AbstractReporterManager implements
        CanListener {

    @SuppressWarnings("LeakingThisInConstructor")
    public CbusReporterManager(CanSystemConnectionMemo memo) {
        this.memo = memo;
        this.tc = memo.getTrafficController();
        this.prefix = memo.getSystemPrefix();
        this.memo = memo;
        if (tc != null) {
            tc.addCanListener(this);
        } else {
            log.error("CbusReporterManager: Failed to register listener");
        }
    }

    CanSystemConnectionMemo memo;
    TrafficController tc;
    String prefix;

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    @Override
    public void dispose() {
        if (tc != null) {
            tc.removeCanListener(this);
        }
        super.dispose();
    }

    @Override
    public Reporter createNewReporter(String systemName, String userName) {
        Reporter t;
        log.debug("ReporterManager create new CbusReporter: " + systemName);
        int addr = Integer.parseInt(systemName.substring(prefix.length() + 1));
        t = new CbusReporter(addr, tc, prefix);
        t.setUserName(userName);
        t.addPropertyChangeListener(this);

        return t;
    }

    /* (non-Javadoc)
     * @see jmri.jmrix.can.CanListener#message(jmri.jmrix.can.CanMessage)
     */
    @Override
    public void message(CanMessage m) {
        // TODO Auto-generated method stub
        log.debug("CbusReporterManager: handle message: " + m.getOpCode());
        if (m.getOpCode() != CbusConstants.CBUS_DDES) {
            return;
        }
        // message type OK, check address
        int addr = CbusMessage.getNodeNumber(m);

        CbusReporter r = (CbusReporter) provideReporter("MR" + addr);
        r.message(m);       // make sure it got the message

    }

    /* (non-Javadoc)
     * @see jmri.jmrix.can.CanListener#reply(jmri.jmrix.can.CanReply)
     */
    @Override
    public void reply(CanReply m) {
        // TODO Auto-generated method stub
        log.debug("CbusReporterManager: handle reply: " + m.getOpCode() + " node: " + CbusMessage.getNodeNumber(m));
        if (m.getOpCode() != CbusConstants.CBUS_DDES || m.getOpCode() != CbusConstants.CBUS_ACDAT) {
            return;
        }
        // message type OK, check address
        int addr = m.getElement(1) * 256 + m.getElement(2);

        CbusReporter r = (CbusReporter) provideReporter("MR" + addr);
        r.reply(m);     // make sure it got the message

    }

    private static final Logger log = LoggerFactory.getLogger(CbusReporterManager.class.getName());
}
