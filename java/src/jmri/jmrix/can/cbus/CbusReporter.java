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
 * @author Steve Young Copyright (c) 2019
 *
 */
public class CbusReporter extends AbstractReporter implements CanListener {

    private final int _number;
    private TrafficController _tc;
    private CbusPreferences p;
    private IdTagManager tagManager;

    @SuppressWarnings("LeakingThisInConstructor")
    public CbusReporter(int number, TrafficController tc, String prefix) {  // a human-readable Reporter number must be specified!
        super(prefix + "R" + number);  // can't use prefix here, as still in construction
        _number = number;
        tagManager = InstanceManager.getDefault(IdTagManager.class);
        p = InstanceManager.getDefault(jmri.jmrix.can.cbus.CbusPreferences.class);
        if ( p.getAllowAutoSensorCreation() ) {
            jmri.SensorManager sm = InstanceManager.getDefault(jmri.SensorManager.class);
            jmri.Sensor sensor = sm.provideSensor("+" + _number);
            if (sensor.getReporter()==null) {
                sensor.setReporter(this);
            }
            checkForTagsAfterDelay();
        }
        // Register for messages
        if (tc !=null) {
            tc.addCanListener(this);
        }
        _tc=tc;
        log.debug("Added new reporter {}R{}",prefix, number);
    }
    
    private void checkForTagsAfterDelay(){
        
        jmri.util.ThreadingUtil.runOnLayoutDelayed( () -> {
            java.util.List<IdTag> list = tagManager.getTagsForReporter(this,999999999L);
            log.debug("Reporter startup tag list is {}",list.toString() );
            if ( list.size() < 1 ) {
                setState(IdTag.UNSEEN);
            } else {
                setState(IdTag.SEEN);
            }
        },2000 );
    }

    // unseen = 2
    // seen = 3

    private int state = UNKNOWN;

    @Override
    public void setState(int s) {
        firePropertyChange("state", state, s);
        state = s;
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public void message(CanMessage m) {
        if (m.getOpCode() == CbusConstants.CBUS_DDES) {
            // if mode > 0 could be an RFID write command sent from JMRI
            if ( p.getReporterMode()==0 ) { 
                RFIDReport(m,false);
            }
        }
    }

    @Override
    public void reply(CanReply m) {
        if (m.getOpCode() == CbusConstants.CBUS_DDES || m.getOpCode() == CbusConstants.CBUS_ACDAT) {
            CanMessage l = new CanMessage(m);
            RFIDReport(l,true);
        }
    }

    private void RFIDReport(CanMessage l, Boolean incoming) {
        log.debug("CbusReporter: message " + l.getOpCode());
        // check address
        int addr = (l.getElement(1) << 8) + l.getElement(2);
        if (addr != _number) {
            log.debug("CBusReporter {} does not match node or device number: {}",_number,addr);
            return;
        }
        String buf;
        if ( p.getReporterMode()==1 ) {
            // may need changing from nmra dcc to number? may be ok as is.
            buf = "" + (l.getElement(3) << 8) + l.getElement(4);
            // l.getElement(5)
            // l.getElement(6)
            // l.getElement(7));
        }
        else if ( p.getReporterMode()==2 ) {
            // may need changing from nmra dcc to number? may be ok as is.
            buf = "" + (l.getElement(3) << 8) + l.getElement(4);
            String tagdata = String.format("%02X", l.getElement(5)) + 
                String.format("%02X", l.getElement(6)) + String.format("%02X", l.getElement(7));
            InstanceManager.getDefault(jmri.MemoryManager.class).provideMemory("LOCO"+buf).setValue("" + tagdata);            
        }
        else { // default AND mode 0
            buf = toTag(l.getElement(3), l.getElement(4), l.getElement(5), l.getElement(6), l.getElement(7));
        }
        log.debug("Report RFID tag read of tag: {}", buf);
        IdTag tag = tagManager.getByTagID(buf);
        if (tag != null) {
            if (incoming) {
                clearPreviousReporter(tag);
            }
            tag.setWhereLastSeen(this);
        } else {
            tag = tagManager.newIdTag(buf, null);
            log.info("Failed to find tag for RFID: {}, creating new one {}.",buf,tag);
            tag.setWhereLastSeen(this);
        }
        setReport(tag);
        setState(IdTag.SEEN);
    }

    private void clearPreviousReporter(IdTag tag) {
        CbusReporter r;
        if ((r = (CbusReporter) tag.getWhereLastSeen()) != null) {
            if (r == this) {
                return;
            }
            if ( r.getCurrentReport() == tag ) {
                r.setReport(null);
            }
            
            java.util.List<IdTag> list = tagManager.getTagsForReporter(r,999999999L);
            log.debug("reporter {} now has {} tags with data {}",r.mSystemName,list.size(),list.toString());
            
            // if the list has just 1 tag, this tag is about to be removed
            if ( list.size() < 2 ) {
                log.debug("Reporter {} going unseen",r.mSystemName);
                r.setState(IdTag.UNSEEN); 
            }
        }
    }

    /** 
     * Converts 5x byte integers to a single string
     */
    private String toTag(int b1, int b2, int b3, int b4, int b5) {
        StringBuilder rval = new StringBuilder();
        rval.append(String.format("%02X", b1));
        rval.append(String.format("%02X", b2));
        rval.append(String.format("%02X", b3));
        rval.append(String.format("%02X", b4));
        rval.append(String.format("%02X", b5));
        return rval.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (_tc != null) {
            _tc.removeCanListener(this);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(CbusReporter.class);
}
