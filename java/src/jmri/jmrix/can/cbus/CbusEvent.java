package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public class CbusEvent extends CbusEventDataElements {
    
    private int _nn;
    private int _en;
    protected EvState _state;
    protected String _name;
    private final CanSystemConnectionMemo _memo;
    
    /**
     * Create a new event
     * <p>
     * New events have an unknown on or off status
     *
     * @param nn Node Number
     * @param en Event Number
     */
    public CbusEvent( int nn, int en){
        super();
        this._nn = nn;
        this._en = en;
        this._state = EvState.UNKNOWN;
        this._name = "";
        this._memo = null;
    }
    
    /**
     * Create a new event by Connection
     * <p>
     * New events have an unknown on or off status
     *
     * @param memo System Connection
     * @param nn Node Number
     * @param en Event Number
     */
    public CbusEvent( CanSystemConnectionMemo memo, int nn, int en){
        super();
        this._nn = nn;
        this._en = en;
        this._state = EvState.UNKNOWN;
        this._name = "";
        this._memo = memo;
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
        return new CbusNameService(_memo).getNodeName( getNn() );
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
        return (nn == _nn) && (en == _en);
    }
    
    /** 
     * {@inheritDoc} 
     * <p>
     * Custom method to compare Node Number and Event Number.
     */
    @Override
    public boolean equals(Object o) {
        return ((o instanceof CbusEvent) && matches(((CbusEvent) o).getNn(),((CbusEvent) o).getEn()));
    }
    
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(getEn(), getNn());
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
        CanSystemConnectionMemo memo;
        if (_memo==null) {
            memo = jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class);
        } else {
            memo = _memo;
        }
        if ( state == EvState.TOGGLE ) {
            if ( _state == EvState.OFF )  {
                state =EvState.ON;
            }
            else {
                state =EvState.OFF;
            }
        }
        _state = state;
        
        jmri.util.ThreadingUtil.runOnLayout( () -> {
            memo.getTrafficController().sendCanMessage(getCanMessage(memo.getTrafficController().getCanid(),_nn,_en,_state), null);
        } );
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
            addevbuf.append (Bundle.getMessage("OPC_NN")).append (":");
            addevbuf.append (_nn).append (" ");
            if ( !getNodeName().isEmpty() ) {
                addevbuf.append (getNodeName()).append (" ");
            }
        }
        addevbuf.append (Bundle.getMessage("OPC_EN")).append (":");
        addevbuf.append (_en).append (" ");
        if ( !getName().isEmpty() ) {
            addevbuf.append ( getName() ).append (" ");
        }
        return addevbuf.toString();
    }

    // private static final Logger log = LoggerFactory.getLogger(CbusEvent.class);

}
