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
    
    /**
     * Enum of the event state.
     * <p>
     * Events generally have on, off or unknown status.
     * <p>
     * They can also be asked to request their current status via the network,
     * or toggled to the opposite state that it is currently at.
     */
    public enum EvState{
        ON, OFF, UNKNOWN, REQUEST, TOGGLE;
    }
    
    /**
     * Create a new event
     * <p>
     * New events have an unknown on or off status
     *
     * @param nn Node Number
     * @param en Event Number
     */
    public CbusEvent( int nn, int en){
        this._nn = nn;
        this._en = en;
        this._state = EvState.UNKNOWN;
        this._name = "";
    }

    /**
     * Get state of the event
     *
     * @return the enum event state, on off or unknown.
     *
     */
    public EvState getState() {
        return _state;
    }
    
    /**
     * Set current state of the event.
     * <p>
     * Does NOT send update to layout.
     *
     * @param newval the enum event state ie ON, OFF, UNKNOWN
     */
    public void setState( EvState newval ) {
        _state = newval;
    }    
    
    /**
     * Get event event number
     *
     * @return event Number
     *
     */
    public int getEn() {
        return _en;
    }

    /**
     * Get event node number.
     *
     * @return node Number
     */
    public int getNn(){
        return _nn;
    }
    
    /**
     * Set event event number.
     *
     * @param en Event Number, not restricted so can be -1 for unknown
     */
    public void setEn ( int en ) {
        _en = en;
    }

    /**
     * Set event node number.
     *
     * @param nn Node Number, not restricted so can be -1 for unknown
     */
    public void setNn ( int nn ) {
        _nn = nn;
    }
    
    /**
     * Set event name.
     *
     * @param name Event Name
     */
    public void setName( String name ) {
        _name = name;
    }
    
    /**
     * Get event name.
     *
     * @return the Event Name
     */
    public String getName() {
        return _name;
    }
    
    /**
     * Get Node name.
     * <p>
     * Helper method, node name not stored in event, retrieved via @CbusNameService
     *
     * @return Node Name
     */
    public String getNodeName() {
        return new CbusNameService().getNodeName( getNn() );
    }
    
    /**
     * Test if a node and event number combination matches this event.
     * 
     * @param nn Node Number
     * @param en Event Number
     *
     * @return true on match, else false
     */
    public boolean matches(int nn, int en) {
        if ( (nn == _nn) && (en == _en) ) {
            return true;
        }
        return false;
    }
    
    /**
     * Send ON event CAN frame.
     * <p>
     * Long event if Node num greater than 0, else short.
     */
    public void sendOn(){
        sendEvent(EvState.ON);
    }
    
    /**
     * Send OFF event CAN frame.
     * <p>
     * Long event if Node num greater than 0, else short.
     */
    public void sendOff(){
        sendEvent(EvState.OFF);
    }
    
    /**
     * Send event status request CAN frame.
     * <p>
     * Long request if Node num greater than 0, else short.
     */
    public void sendRequest(){
        sendEvent(EvState.REQUEST);
    }

    /**
     * Send event CAN frame via ENUM.
     * <p>
     * Also updates the event status as per the enum value.
     * <p>
     * If current state unknown, toggle sends event off.
     * <p>
     * Long event if Node num greater than 0, else short.
     *
     * @param state The enum state requested to be sent, ie ON, OFF, REQUEST, TOGGLE
     */
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
    
    /**
     * Get a String with event overview.
     * 
     * @return includes event name and node name if known
     */
    @Override
    public String toString() {
        StringBuilder addevbuf = new StringBuilder(50);
        if ( _nn > 0 ) {
            addevbuf.append (Bundle.getMessage("OPC_NN"));
            addevbuf.append (":");
            addevbuf.append (_nn);
            addevbuf.append (" ");
            if ( !getNodeName().isEmpty() ) {
                addevbuf.append ( getNodeName() );
                addevbuf.append (" ");
            }
        }
        addevbuf.append (Bundle.getMessage("OPC_EN"));
        addevbuf.append (":");
        addevbuf.append (_en);
        addevbuf.append (" ");
        if ( !getName().isEmpty() ) {
            addevbuf.append ( getName() );
            addevbuf.append (" ");
        }
        return addevbuf.toString();
    }

    // private static final Logger log = LoggerFactory.getLogger(CbusEvent.class);

}
