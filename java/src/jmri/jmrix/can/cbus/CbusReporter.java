package jmri.jmrix.can.cbus;

import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.implementation.AbstractReporter;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractReporter for CBUS controls.
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
 *
 */
public class CbusReporter extends AbstractReporter implements CanListener {

    private final int _number;

    @SuppressWarnings("LeakingThisInConstructor")
    public CbusReporter(int number, TrafficController tc, String prefix) {  // a human-readable Reporter number must be specified!
        super(prefix + "R" + number);  // can't use prefix here, as still in construction
        _number = number;
        // At construction, register for messages
        tc.addCanListener(this);
        log.debug("Added new reporter " + prefix + "R" + number);
    }

    private int state = UNKNOWN;

    @Override
    public void setState(int s) {
        state = s;
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public void message(CanMessage m) {
        log.debug("CbusReporter: message " + m.getOpCode());
        if (m.getOpCode() == CbusConstants.CBUS_DDES) {
            RFIDReport(m);
        } // nothing
    }

    @Override
    public void reply(CanReply m) {
        log.debug("CbusReporter: reply " + m.getOpCode());
        if (m.getOpCode() == CbusConstants.CBUS_DDES || m.getOpCode() == CbusConstants.CBUS_ACDAT) {
            int addr = (m.getElement(1) << 8) + m.getElement(2);
            if (addr != _number) {
                log.debug("CBusReporter incorrect node number: " + addr + ", expected " + _number + "in reply");
                return;
            }
            String buf;

            buf = toTag(m.getElement(3), m.getElement(4), m.getElement(5), m.getElement(6), m.getElement(7));
            log.debug("Report RFID tag read of tag: " + buf);
            IdTag tag = InstanceManager.getDefault(IdTagManager.class).getIdTag(buf);
            if (tag != null) {
                CbusReporter r;
                if ((r = (CbusReporter) tag.getWhereLastSeen()) != null) {
                    log.debug("Previous reporter: " + r.mSystemName);
                    if (r != this && r.getCurrentReport() == tag) {
                        log.debug("Notify previous");
                        r.clear();
                    } else {
                        log.debug("Current report was: " + r.getCurrentReport());
                    }
                }
                tag.setWhereLastSeen(this);
            } else {
                log.error("Failed to find tag for RFID:" + buf);
                InstanceManager.getDefault(IdTagManager.class).newIdTag(buf, null);
            }
            setReport(tag);
            setState(tag != null ? IdTag.SEEN : IdTag.UNSEEN);
        } // nothing

    }

    public void clear() {
        setReport(null);
        setState(IdTag.UNSEEN);
    }

    private void RFIDReport(CanMessage l) {
        // check address
        int addr = CbusMessage.getNodeNumber(l);
        if (addr != _number) {
            log.debug("CBusReporter incorrect node number: " + addr + ", expected node number: " + _number);
            return;
        }
        String buf;
        buf = toTag(l.getElement(3), l.getElement(4), l.getElement(5), l.getElement(6), l.getElement(7));
        log.debug("Report RFID tag read of tag: " + buf);
        IdTag tag = InstanceManager.getDefault(IdTagManager.class).getIdTag(buf);
        if (tag == null) {
            log.error("Failed to find tag for RFID:" + buf);
        }
        setReport(tag);
        setState(tag != null ? IdTag.SEEN : IdTag.UNSEEN);
    }

    private String toTag(int b1, int b2, int b3, int b4, int b5) {
        String rval;
        rval = String.format("%02X", b1) + String.format("%02X", b2) + String.format("%02X", b3)
                + String.format("%02X", b4) + String.format("%02X", b5);
        return rval;
    }

    private static final Logger log = LoggerFactory.getLogger(CbusReporter.class.getName());
}
