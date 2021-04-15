package jmri.jmrix.can.cbus;

import jmri.*;
import jmri.implementation.DefaultMeter;
import jmri.implementation.MeterUpdateTask;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;

/**
 * Provide access to current meter from a MERG CBUS Command Station
 *
 * @author Steve Young (C) 2019
 * 
 * @author Andrew Crosland 2020
 * Added voltage capability to use with new jmrit.voltmeter class
 * 
 * @author Daniel Bergqvist Copyright (C) 2020
  * 
 * @author Andrew Crosland 2021
 * Added extra current meter for systems with two track outputs
*/
public class CbusPredefinedMeters implements CanListener {

    private final TrafficController tc;
    private int _nodeToListen;
    private int _eventToListenCurrent;
    private int _eventToListenCurrentExtra;
    private int _eventToListenVoltage;
    private final CanSystemConnectionMemo _memo;
    final MeterUpdateTask updateTask;
    final Meter currentMeter;
    final Meter currentMeterExtra;
    final Meter voltageMeter;
    
    public CbusPredefinedMeters(CanSystemConnectionMemo memo) {
        
        tc = memo.getTrafficController();
        _memo = memo;
        
        updateTask = new UpdateTask(-1);
        
        currentMeter = new DefaultMeter.DefaultCurrentMeter(
                memo.getSystemPrefix() + InstanceManager.getDefault(MeterManager.class).typeLetter() + "CBUSCurrentMeter",
                Meter.Unit.Milli, 0, 65535.0, 1.0, updateTask);
        
        currentMeterExtra = new DefaultMeter.DefaultCurrentMeter(
                memo.getSystemPrefix() + InstanceManager.getDefault(MeterManager.class).typeLetter() + "CBUSCurrentMeter2",
                Meter.Unit.Milli, 0, 65535.0, 1.0, updateTask);
        
        voltageMeter = new DefaultMeter.DefaultVoltageMeter(
                memo.getSystemPrefix() + InstanceManager.getDefault(MeterManager.class).typeLetter() + "CBUSVoltageMeter",
                Meter.Unit.NoPrefix, 0, 6553.5, 0.1, updateTask);
        
        InstanceManager.getDefault(MeterManager.class).register(currentMeter);
        InstanceManager.getDefault(MeterManager.class).register(currentMeterExtra);
        InstanceManager.getDefault(MeterManager.class).register(voltageMeter);
        
        log.debug("CbusMultiMeter constructor called");
    }
    
    /**
     * Listen for CAN Frames sent by Command Station 0
     * Typically sent every 4-5 seconds.
     *
     * {@inheritDoc}
     */
    @Override
    public void reply(CanReply r) {
        if ( r.extendedOrRtr()
            || CbusMessage.getOpcode(r) != CbusConstants.CBUS_ACON2
            || CbusMessage.getNodeNumber(r) != _nodeToListen 
            || ((CbusMessage.getEvent(r) != _eventToListenCurrent) 
                && (CbusMessage.getEvent(r) != _eventToListenVoltage)
                && (CbusMessage.getEvent(r) != _eventToListenCurrentExtra))) {
            return;
        }
        try {
            if (CbusMessage.getEvent(r) == _eventToListenCurrent) {
                int currentInt = ( r.getElement(5) * 256 ) + r.getElement(6);
                currentMeter.setCommandedAnalogValue(currentInt * 1.0f );  // mA value, min 0, max 65535, NOT percentage
            } else if (CbusMessage.getEvent(r) == _eventToListenCurrentExtra) {
                int currentInt = ( r.getElement(5) * 256 ) + r.getElement(6);
                currentMeterExtra.setCommandedAnalogValue(currentInt * 1.0f );  // mA value, min 0, max 65535, NOT percentage
            } else {
                // Voltage from the command station is scaled by a factor of 10 to allow one decimal place
                int voltageInt = ( r.getElement(5) * 256 ) + r.getElement(6);
                voltageMeter.setCommandedAnalogValue(voltageInt / 10.0f ); // V value, min 0, max 6553.5, NOT percentage
            }
        } catch (JmriException e) {
            log.error("exception thrown by setCurrent or setVoltage", e);
        }
    }

    /**
     * Outgoing CAN Frames ignored
     *
     * {@inheritDoc}
     */
    @Override
    public void message(CanMessage m) {
    }
    
    public void dispose() {
        updateTask.disable(currentMeter);
        updateTask.disable(currentMeterExtra);
        updateTask.disable(voltageMeter);
        InstanceManager.getDefault(MeterManager.class).deregister(currentMeter);
        InstanceManager.getDefault(MeterManager.class).deregister(currentMeterExtra);
        InstanceManager.getDefault(MeterManager.class).deregister(voltageMeter);
        updateTask.dispose(currentMeter);
        updateTask.dispose(currentMeterExtra);
        updateTask.dispose(voltageMeter);
    }
    
    
    private class UpdateTask extends MeterUpdateTask {
    
        public UpdateTask(int interval) {
            super(interval);
        }
    
        /**
         * Starts listening for ExData2 CAN Frames using the Node of the Master Command Station
         *
         * {@inheritDoc}
         */
        @Override
        public void enable() {
            _nodeToListen = 65534;
            _eventToListenCurrent = 1; // hard coded at present
            _eventToListenVoltage = 2; // hard coded at present
            _eventToListenCurrentExtra = 3; // hard coded at present
            CbusNodeTableDataModel cs =  jmri.InstanceManager.getNullableDefault(CbusNodeTableDataModel.class);
            if (cs != null) {
                CbusNode csnode = cs.getCsByNum(0);
                if (csnode!=null) {
                    _nodeToListen = csnode.getNodeNumber();
                }
            } else {
                log.info("Unable to fetch Master Command Station from Node Manager");
            }
            tc.addCanListener(CbusPredefinedMeters.this);
            log.info("Enabled meter Long Ex2Data {} {} {}", 
                new CbusNameService(_memo).getEventNodeString(_nodeToListen,_eventToListenCurrent), 
                new CbusNameService(_memo).getEventNodeString(_nodeToListen,_eventToListenVoltage),
                new CbusNameService(_memo).getEventNodeString(_nodeToListen,_eventToListenCurrentExtra));
            
            super.enable();
        }

        /**
         * Stops listening for updates
         *
         * {@inheritDoc}
         */
        @Override
        public void disable() {
            super.disable();
            tc.removeCanListener(CbusPredefinedMeters.this);
            log.info("Disabled meter.");
        }
        
        /**
         * Adjust CBUS Command station settings to change frequency of updates
         * No local action performed
         *
         * {@inheritDoc}
         */
        @Override
        public void requestUpdateFromLayout() {
        }
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusPredefinedMeters.class);
}
