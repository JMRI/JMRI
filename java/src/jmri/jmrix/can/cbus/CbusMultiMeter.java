package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to current meter from a MERG CBUS Command Station
 *
 * @author Steve Young (C) 2019
 * 
 * @author Andrew Crosland 2020
 * Added voltage capability to use with new jmrit.voltmeter class
 */
public class CbusMultiMeter extends jmri.implementation.AbstractMultiMeter implements CanListener {

    private final TrafficController tc;
    private int _nodeToListen;
    private int _eventToListenCurrent;
    private int _eventToListenVoltage;
    private final CanSystemConnectionMemo _memo;
    
    public CbusMultiMeter(CanSystemConnectionMemo memo) {
        super(-1);  // no internal timer, since the command station controls the report frequency
        tc = memo.getTrafficController();
        _memo = memo;
        log.debug("CbusMultiMeter constructor called");
    }

    /**
     * CBUS does have Amperage reporting
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean hasCurrent() {
        return true;
    }

    /**
     * CBUS might have Voltage reporting
     *
     * {@inheritDoc}
     */
    @Override
    public boolean hasVoltage() {
        return true;
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
        CbusNodeTableDataModel cs =  jmri.InstanceManager.getNullableDefault(CbusNodeTableDataModel.class);
        if (cs != null) {
            CbusNode csnode = cs.getCsByNum(0);
            if (csnode!=null) {
                _nodeToListen = csnode.getNodeNumber();
            }
        } else {
            log.info("Unable to fetch Master Command Station from Node Manager");
        }
        tc.addCanListener(this);
        log.info("Enabled meter Long Ex2Data {} {}", 
            new CbusNameService(_memo).getEventNodeString(_nodeToListen,_eventToListenCurrent), 
            new CbusNameService(_memo).getEventNodeString(_nodeToListen,_eventToListenVoltage));
    }

    /**
     * Stops listening for updates
     *
     * {@inheritDoc}
     */
    @Override
    public void disable() {
        tc.removeCanListener(this);
        log.info("Disabled meter.");
    }

    @Override
    public CurrentUnits getCurrentUnits() {
        return  CurrentUnits.CURRENT_UNITS_MILLIAMPS;
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
            || (CbusMessage.getEvent(r) != _eventToListenCurrent 
                && CbusMessage.getEvent(r) != _eventToListenVoltage )) {
            return;
        }
        if (CbusMessage.getEvent(r) == _eventToListenCurrent) {
            int currentInt = ( r.getElement(5) * 256 ) + r.getElement(6);
            setCurrent(currentInt * 1.0f ); // mA value, min 0, max 65535, NOT percentage
        } else {
            // Voltage from the command station is scaled by a factor of 10 to allow one decimal place
            int voltageInt = ( r.getElement(5) * 256 ) + r.getElement(6);
            setVoltage(voltageInt / 10.0f ); // V value, min 0, max 6553.5, NOT percentage
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
    
    /**
     * Adjust CBUS Command station settings to change frequency of updates
     * No local action performed
     *
     * {@inheritDoc}
     */
    @Override
    protected void requestUpdateFromLayout() {
    }
    
    /**
     * Performs no local action, Meter is setup by #enable()
     *
     * {@inheritDoc}
     */
    @Override
    public void initializeHardwareMeter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHardwareMeterName() {
        return ("CBUS");
    }

    private final static Logger log = LoggerFactory.getLogger(CbusMultiMeter.class);

}
