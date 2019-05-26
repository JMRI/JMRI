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
 */
public class CbusMultiMeter extends jmri.implementation.AbstractMultiMeter implements CanListener {

    private TrafficController tc;
    private int _nodeToListen;
    private int _eventToListen;
    
    public CbusMultiMeter(CanSystemConnectionMemo memo) {
        super(-1);  // no internal timer, since the command station controls the report frequency
        tc = memo.getTrafficController();
        log.debug("CbusMultiMeter constructor called");
    }

    @Override
    public boolean hasCurrent() {
        return true;
    }

    @Override
    public boolean hasVoltage() {
        return false;
    }

    @Override
    public void enable() {
        try {
            CbusNodeTableDataModel cs =  jmri.InstanceManager.getDefault(CbusNodeTableDataModel.class);
            CbusNode csnode = cs.getCsByNum(0);
            log.debug("csnode is {}",csnode);
            _nodeToListen = csnode.getNodeNumber();
            _eventToListen = 1; // hard coded at present
            tc.addCanListener(this);
            log.info("Enabled meter Long Ex2Data {}", new CbusNameService().getEventNodeString(_nodeToListen,_eventToListen));
        }
        catch ( NullPointerException e ){
            log.error("Unable to Locate Details for Master Command Station 0");
        }
    }

    @Override
    public void disable() {
        tc.removeCanListener(this);
        log.info("Disabled meter.");
    }

    @Override
    public void reply(CanReply r) {
        if ( r.isExtended() || r.isRtr() ) {
            return;
        }
        if ( CbusMessage.getOpcode(r) != CbusConstants.CBUS_ACON2  ) {
            return;
        }
        if ( CbusMessage.getNodeNumber(r) != _nodeToListen ) {
            return;
        }
        if ( CbusMessage.getEvent(r) != _eventToListen ) {
            return;
        }
        int currentInt = ( r.getElement(5) * 256 ) + r.getElement(6);
        log.debug("Setting current {}",currentInt);
        
        setCurrent(currentInt * 1.0f );

    }

    // ignore
    @Override
    public void message(CanMessage m) {
    }
    
    // Adjust Command station settings to change frequency of updates
    // there is no request update mechanism in cbus
    @Override
    protected void requestUpdateFromLayout() {
    }
    
    @Override
    public void initializeHardwareMeter() {
    }

    @Override
    public String getHardwareMeterName() {
        return ("CBUS");
    }

    private final static Logger log = LoggerFactory.getLogger(CbusMultiMeter.class);

}
