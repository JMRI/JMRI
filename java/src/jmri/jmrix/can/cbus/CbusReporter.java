package jmri.jmrix.can.cbus;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.implementation.AbstractRailComReporter;
import jmri.jmrix.can.*;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractRailComReporter for CBUS controls.
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
 * CBUS Reporters can accept
 * 5-byte unique Classic RFID on DDES or ACDAT OPCs,
 * CANRC522 / CANRCOM DDES OPCs.
 *
 * @author Mark Riddoch Copyright (C) 2015
 * @author Steve Young Copyright (c) 2019, 2020
 *
 */
public class CbusReporter extends AbstractRailComReporter implements CanListener {

    private final int _number;
    private final TrafficController tc; // can be removed when former constructor removed
    private final CanSystemConnectionMemo _memo;

    /**
     * Create a new CbusReporter.
     * <p>
     *
     * @param address Reporter address, currently in String number format. No system prefix or type letter.
     * @param memo System connection.
     */
    public CbusReporter(String address, CanSystemConnectionMemo memo) {  // a human-readable Reporter number must be specified!
        super(memo.getSystemPrefix() + "R" + address);  // can't use prefix here, as still in construction
        _number = Integer.parseInt(  address);
        _memo = memo;
        // At construction, register for messages
        tc = memo.getTrafficController(); // can be removed when former constructor removed
        addTc(memo.getTrafficController());
        log.debug("Added new reporter {}R{}", memo.getSystemPrefix(), address);
    }

    /**
     * Set the CbusReporter State.
     * <p>
     * May also provide / update a CBUS Sensor State, depending on property.
     * {@inheritDoc}
     */
    @Override
    public void setState(int s) {
        super.setState(s);
        if ( getMaintainSensor() ) {
            SensorManager sm = (SensorManager) _memo.get(SensorManager.class);
            sm.provide("+"+_number).setCommandedState( s==IdTag.SEEN ? Sensor.ACTIVE : Sensor.INACTIVE );
        }
    }

    /**
     * {@inheritDoc}
     * Resets report briefly back to null so Sensor Listeners are updated.
     */
    @Override
    public void notify(IdTag id){
        if ( this.getCurrentReport()!=null && id!=null ){
            super.notify(null); //
        }
        super.notify(id);
    }

    /**
     * {@inheritDoc}
     * CBUS Reporters can respond to ACDAT or DDES OPC's.
     */
    @Override
    public void message(CanMessage m) {
        reply(new CanReply(m));
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
        if ((m.getElement(1) << 8) + m.getElement(2) == _number) { // correct reporter number
            if (m.getOpCode() == CbusConstants.CBUS_DDES && !getCbusReporterType().equals(CbusReporterManager.CBUS_REPORTER_TYPE_CLASSIC)  ) {
                ddesReport(m);
            } else {
                classicRFIDReport(m);
            }
        }
    }

    private void ddesReport(CanReply m) {
        int least_significant_bit = m.getElement(3) & 1;
        if ( least_significant_bit ==0 ) {
            canRc522Report(m);
        } else {
            canRcomReport(m);
        }
    }

    private void classicRFIDReport(CanReply m) {
        String buf = toClassicTag(m.getElement(3), m.getElement(4), m.getElement(5), m.getElement(6), m.getElement(7));
        log.debug("Reporter {} {} RFID tag read of tag: {}", this,getCbusReporterType(),buf);
        IdTag tag = InstanceManager.getDefault(IdTagManager.class).provideIdTag(buf);
        notify(tag);
        startTimeout(tag);
    }

    // no DCC address correction to allow full 0-65535 range of tags on rolling stock
    private void canRc522Report(CanReply m){
        String tagId = String.valueOf((m.getElement(4)<<8)+ m.getElement(5));
        log.debug("Reporter {} RFID tag read of tag: {}",this, tagId);
        IdTag tag = InstanceManager.getDefault(IdTagManager.class).provideIdTag("ID"+tagId);
        tag.setProperty("DDES Dat3", m.getElement(6));
        tag.setProperty("DDES Dat4", m.getElement(7));
        notify(tag);
        startTimeout(tag);
    }

    // DCC address correction 0-10239 range
    private void canRcomReport(CanReply m) {
        int railcom_id = (m.getElement(3)>>4);
        log.warn("CANRCOM support still in development.");
        log.info("{} detected RailCom ID {}",this,railcom_id);
    }

    private String toClassicTag(int b1, int b2, int b3, int b4, int b5) {
        return String.format("%02X", b1) + String.format("%02X", b2) + String.format("%02X", b3)
            + String.format("%02X", b4) + String.format("%02X", b5);
    }

    /**
     * Get the Reporter Listener format type.
     * <p>
     * Defaults to Classic RfID, 5 byte unique.
     * @return reporter format type.
     */
    @Nonnull
    public String getCbusReporterType() {
        Object returnVal = getProperty(CbusReporterManager.CBUS_REPORTER_DESCRIPTOR_KEY);
        return (returnVal==null ? CbusReporterManager.CBUS_DEFAULT_REPORTER_TYPE : returnVal.toString());
    }

    /**
     * Get if the Reporter should provide / update a CBUS Sensor, following Reporter Status.
     * <p>
     * Defaults to false.
     * @return true if the reporter should maintain the Sensor.
     */
    public boolean getMaintainSensor() {
        Boolean returnVal = (Boolean) getProperty(CbusReporterManager.CBUS_MAINTAIN_SENSOR_DESCRIPTOR_KEY);
        return (returnVal==null ? false : returnVal);
    }

    // delay can be set to non-null memo when older constructor fully deprecated.
    private void startTimeout(IdTag tag){
        int delay = (_memo==null ? 2000 : ((CbusReporterManager)_memo.get(jmri.ReporterManager.class)).getTimeout() );
        ThreadingUtil.runOnLayoutDelayed( () -> {
            if (!disposed && getCurrentReport() == tag) {
                notify(null);
            }
        },delay);
    }

    private boolean disposed = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        disposed = true;
        tc.removeCanListener(this);
        super.dispose();
    }

    private static final Logger log = LoggerFactory.getLogger(CbusReporter.class);
}
