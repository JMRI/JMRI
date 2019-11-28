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
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 *
 * @author Mark Riddoch Copyright (C) 2015
 * @author Steve Young Copyright (c) 2019
 *
 */
public class CbusReporter extends AbstractReporter implements CanListener {

    private final int _number;
    private final TrafficController tc;

    public CbusReporter(int number, TrafficController tco, String prefix) {  // a human-readable Reporter number must be specified!
        super(prefix + "R" + number);  // can't use prefix here, as still in construction
        _number = number;
        // At construction, register for messages
        tc = tco;
        addTc(tc);
        log.debug("Added new reporter " + prefix + "R" + number);
    }

    private int state = UNKNOWN;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setState(int s) {
        state = s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getState() {
        return state;
    }

    /**
     * {@inheritDoc}
     * CBUS Reporters can respond to ACDAT or DDES OPC's.
     */
    @Override
    public void message(CanMessage m) {
        CanReply mNew = new CanReply(m);
        reply(mNew);
    }

    /**
     * {@inheritDoc}
     * CBUS Reporters can respond to ACDAT or DDES OPC's
     */
    @Override
    public void reply(CanReply m) {
        if ( m.extendedOrRtr() ) {
            return;
        }
        if ( m.getOpCode() != CbusConstants.CBUS_DDES && m.getOpCode() != CbusConstants.CBUS_ACDAT) {
            return;
        }
        RFIDReport(m);
    }

    public void clear() {
        log.debug("reporter {} set to clear",toString());
        setReport(null);
        setState(IdTag.UNSEEN);
    }

    private void RFIDReport(CanReply m) {
        int addr = (m.getElement(1) << 8) + m.getElement(2);
        if (addr == _number) {
            log.debug("CBusReporter found for addr:{}", addr);
            String buf = toTag(m.getElement(3), m.getElement(4), m.getElement(5), m.getElement(6), m.getElement(7));
            log.debug("Report RFID tag read of tag: {}", buf);
            IdTag tag = InstanceManager.getDefault(IdTagManager.class).provideIdTag(buf);
            clearPreviousReporter(tag);
            tag.setWhereLastSeen(this);
            setReport(tag);
            setState(IdTag.SEEN);
        }
    }

    private String toTag(int b1, int b2, int b3, int b4, int b5) {
        String rval;
        rval = String.format("%02X", b1) + String.format("%02X", b2) + String.format("%02X", b3)
                + String.format("%02X", b4) + String.format("%02X", b5);
        return rval;
    }
    
    // clear all previous reporter of the ID tag location
    // there is no "exit" area message in CBUS
    private void clearPreviousReporter(IdTag tag) {
        log.debug("clear previous reporter for tag {}",tag);
        CbusReporter r = (CbusReporter) tag.getWhereLastSeen();
        if (r != null) {
            log.debug("previous reporter {} found",r);
            if (r != this && r.getCurrentReport() == tag) {
                r.clear();
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        tc.removeCanListener(this);
    }

    private static final Logger log = LoggerFactory.getLogger(CbusReporter.class);
}
