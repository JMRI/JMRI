package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.TrafficController;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public class CbusEvent {
    
    private int _nn;
    private int _en;
    protected EvState _state;
    protected String _name;
    protected String _nodeName; // this to be provided by a node table eventually
    
    public enum EvState{
        ON, OFF, UNKNOWN, REQUEST, TOGGLE;
    }
    
    public CbusEvent( int nn, int en){
        this._nn = nn;
        this._en = en;
        this._state = EvState.UNKNOWN;
        this._name = "";
        this._nodeName = "";
    }

    public EvState getState() {
        return _state;
    }
    
    public void setState( EvState newval ) {
        _state = newval;
    }    
    
    public int getEn() {
        return _en;
    }

    public int getNn(){
        return _nn;
    }
    
    public void setEn ( int en ) {
        _en = en;
    }

    public void setNn ( int nn ) {
        _nn = nn;
    }
    
    public void setName( String name ) {
        _name = name;
    }
    
    public String getName() {
        return _name;
    }
    
    // will not exist when there's a node table
    public void setNodeName( String name ) {
        _nodeName = name;
    }
    
    public String getNodeName() {
        return _nodeName;
    }
    
    public Boolean matches(int nn, int en) {
        if ( (nn == _nn) && (en == _en) ) {
            return true;
        }
        return false;
    }
    
    public void sendOn(){
        sendEvent(EvState.ON);
    }
    
    public void sendOff(){
        sendEvent(EvState.OFF);
    }

    public void sendRequest(){
        sendEvent(EvState.REQUEST);
    }

    public void sendEvent(EvState state) {
        CanSystemConnectionMemo memo = jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class);
        TrafficController _tc = memo.getTrafficController();
        if ( state == EvState.TOGGLE ) {
            if ( _state == EvState.OFF )  {
                state =EvState.ON;
            }
            else {
                state =EvState.OFF;
            }
        }
        _state = state;
        CanMessage m = new CanMessage(_tc.getCanid());
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setNumDataElements(5);
        if (state==EvState.ON) {
            if (_nn > 0) {
                m.setElement(0, CbusConstants.CBUS_ACON);
            } else {
                m.setElement(0, CbusConstants.CBUS_ASON);
            }
        } else if (state==EvState.OFF) {
            if (_nn > 0) {
                m.setElement(0, CbusConstants.CBUS_ACOF);
            } else {
                m.setElement(0, CbusConstants.CBUS_ASOF);
            }
        } else if (state==EvState.REQUEST) {
            if (_nn > 0) {
                m.setElement(0, CbusConstants.CBUS_AREQ);
            } else {
                m.setElement(0, CbusConstants.CBUS_ASRQ);
            }
        }
        m.setElement(1, _nn >> 8);
        m.setElement(2, _nn & 0xff);
        m.setElement(3, _en >> 8);
        m.setElement(4, _en & 0xff);
        jmri.util.ThreadingUtil.runOnLayout( () -> { _tc.sendCanMessage(m, null); } );
    }
    
    @Override
    public String toString() {
        StringBuilder addevbuf = new StringBuilder(50);
        if ( _nn > 0 ) {
            addevbuf.append (Bundle.getMessage("OPC_NN"));
            addevbuf.append (":");
            addevbuf.append (_nn);
            addevbuf.append (" ");
            if (!_nodeName.equals("")) {
                addevbuf.append (_nodeName);
                addevbuf.append (" ");
            }
        }
        addevbuf.append (Bundle.getMessage("OPC_EN"));
        addevbuf.append (":");
        addevbuf.append (_en);
        addevbuf.append (" ");
        if (!_name.equals("")) {
            addevbuf.append (_name);
            addevbuf.append (" ");
        }
        return addevbuf.toString();
    }

    // private static final Logger log = LoggerFactory.getLogger(CbusEvent.class);
}
