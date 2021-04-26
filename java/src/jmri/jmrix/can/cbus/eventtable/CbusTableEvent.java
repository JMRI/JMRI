package jmri.jmrix.can.cbus.eventtable;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import jmri.NamedBean;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusEvent;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Class to represent an event in the MERG CBUS event table
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusTableEvent extends CbusEvent {
    
    private int _canid;
    private String _comment;
    private int _sesson;
    private int _toton;
    private int _sessoff;
    private int _totoff;
    private int _sessin;
    private int _totin;
    private int _sessout;
    private int _totout;
    private Set<NamedBean> _nbOnActiveA;
    private Set<NamedBean> _nbOffActiveA;
    private Set<NamedBean> _nbOnActiveB;
    private Set<NamedBean> _nbOffActiveB;
    private Date _timestamp;
    
    public CbusTableEvent( jmri.jmrix.can.CanSystemConnectionMemo memo, int nn, int en ){
        
        super(memo,nn,en);
        _canid = -1;
        _name = "";
        _comment = "";
        _sesson = 0;
        _sessoff = 0;
        _sessin = 0;
        _sessout = 0;
        _timestamp = null;
        resetBeans();
        
    }
    
    /**
     * Updates Event State and session / total on and off's.
     * {@inheritDoc}
     */
    @Override
    public void setState( EvState newval ) {
        super.setState(newval);
        if (newval == CbusTableEvent.EvState.ON) {
            _sesson++;
            _toton++;
            setDate( new Date() );
        } 
        else if (newval == CbusTableEvent.EvState.OFF) {
            _sessoff++;
            _totoff++;
            setDate( new Date() );
        }
    }

    /**
     * Get the last-seen date time.
     * @return The last time the event was heard on the network
     */    
    protected Date getDate(){
        if (_timestamp!=null) {
            return new Date(_timestamp.getTime());
        }
        return null;
    }

    /**
     * Set the last-seen date time
     * @param newval the last-seen date time
     */       
    protected void setDate(Date newval) {
        _timestamp = newval;
    }
        
    /**
     * Get the Sensor Turnout and Light user names associated with event on or off.
     * @param state CbusEvent State of ON or OFF
     * @return Sensor Turnout and Light set.
     */   
    protected CbusEventBeanData getBeans(EvState state){
        return new CbusEventBeanData( _nbOnActiveA, _nbOnActiveB, _nbOffActiveA, _nbOffActiveB, state);
    }
    
    protected final void resetBeans(){
        _nbOnActiveA = new HashSet<>();
        _nbOnActiveB = new HashSet<>();
        _nbOffActiveA = new HashSet<>();
        _nbOffActiveB = new HashSet<>();
    }
    
    public void appendOnOffBean(NamedBean nb, boolean beanState, EvState evState ){
        if (evState==EvState.ON) {
            ( beanState ? _nbOnActiveA : _nbOnActiveB ).add(nb);
        } else if (evState==EvState.OFF) {
            ( beanState ? _nbOffActiveA : _nbOffActiveB ).add(nb);
        }
    }

    /**
     * Get the CAN ID to last send the event
     * @return CAN ID
     */
    protected int getEventCanId(){
        return _canid;
    }

    /**
     * Set the event comment
     * @param newval Comment String
     */    
    public void setComment(String newval){
        _comment = newval;
    }

    /**
     * Get the event comment
     * @return Comment String
     */
    protected String getComment(){
        return _comment;
    }

    /**
     * Set the CAN ID to last send the event
     * @param newval CAN ID
     */
    protected void setCanId(int newval){
        _canid = newval;
    }
    
    /**
     * Set Event Counts.
     * @param on Total On
     * @param off Total Off
     * @param in Total In
     * @param out Total Out
     */
    protected void setCounts( int on, int off, int in, int out) {
        _toton = on;
        _totoff = off;
        _totin = in;
        _totout = out;
    }

    /**
     * Number of times event on or off for current session.
     * @param on true for on, false for off
     * @return Number of times event on for current session
     */
    protected int getSessionOnOff(boolean on){
        return (on ? _sesson : _sessoff );
    }
    
    /**
     * Number of times event on or off all sessions.
     * @param on true for on, false for off.
     * @return Number of times event on or off all sessions.
     */
    protected int getTotalOnOff(boolean on){
        return (on ? _toton : _totoff );
    }
    
    /**
     * Number of times event heard coming in to JMRI this session.
     * @param in true for in, false for out.
     * @return Number of times event heard coming in to JMRI this session
     */
    protected int getSessionInOut(boolean in){
        return (in ? _sessin : _sessout );
    }
    
    /**
     * Number of times event heard, all sessions.
     * @param in true for in, false for out.
     * @return Number of times event heard coming in or out to JMRI all sessions
     */
    protected int getTotalInOut(boolean in){
        return (in ? _totin : _totout );
    }
    
    /**
     * Increase Direction session and total counts.
     * @param direction CbusConstant of EVENT_DIR_IN or EVENT_DIR_OUT
     */
    protected void bumpDirection(int direction){
        if ( direction == CbusConstants.EVENT_DIR_IN ){
            _sessin++;
            _totin++;
        }
        else {
            _sessout++;
            _totout++;
        }
    }
    
    /**
     * Reset on, off, in and out session counts to 0
     */
    protected void resetSessionTotals(){
        _sesson = 0;
        _sessoff = 0;
        _sessin = 0;
        _sessout = 0;
    }
    
    /** 
     * {@inheritDoc} 
     */
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
    
    
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusTableEvent.class);

}
