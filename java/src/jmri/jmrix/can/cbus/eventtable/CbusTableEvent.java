package jmri.jmrix.can.cbus.eventtable;

import java.util.Date;
import jmri.jmrix.can.cbus.CbusEvent;

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
    private String _stlonstring;
    private String _stloffstring;
    private Date _timestamp;
    
    public CbusTableEvent( int nn, int en, 
        EvState state, int canid, String name, String comment, 
        int sesson, int sessoff, int sessin, int sessout, Date timestamp ){
        
        super(nn,en);
        _state = state;
        _canid = canid;
        _name = name;
        _comment = comment;
        _sesson = sesson;
        _sessoff = sessoff;
        _sessin = sessin;
        _sessout = sessout;
        _stlonstring ="";
        _stloffstring = "";
        _timestamp = timestamp;
        
    }
    
    /**
     * Get the last-seen date time.
     * @return The last time the event was heard on the network
     */    
    protected Date getDate(){
        return _timestamp;
    }

    /**
     * Set the last-seen date time
     */       
    protected void setDate(Date newval) {
        _timestamp = newval;
    }
    
    /**
     * Get the Sensor Turnout and Light user names associated with event on
     */   
    protected String getStlOn(){
        return _stlonstring;
    }

    /**
     * Get the Sensor Turnout and Light user names associated with event off
     */   
    protected String getStlOff(){
        return _stloffstring;
    }
    
    /**
     * Set the Sensor Turnout and Light user names associated with event on
     */   
    protected void setStlOn(String newval){
        _stlonstring = newval;
    }

    /**
     * Set the Sensor Turnout and Light user names associated with event on
     */   
    protected void setStlOff(String newval){
        _stloffstring = newval;
    }

    /**
     * Get the CAN ID to last send the event
     */
    protected int getEventCanId(){
        return _canid;
    }

    /**
     * Set the event comment
     */    
    protected void setComment(String newval){
        _comment = newval;
    }

    /**
     * Get the event comment
     */
    protected String getComment(){
        return _comment;
    }

    /**
     * Set the CAN ID to last send the event
     */
    protected void setCanId(int newval){
        _canid = newval;
    }

    /**
     * Number of times event on for current session
     */
    protected int getSessionOn(){
        return _sesson;
    }
    
    /**
     * Number of times event on all sessions
     */
    protected int getTotalOn(){
        return _toton;
    }

    /**
     * Number of times event off for current session
     */
    protected int getSessionOff(){
        return _sessoff;
    }
    
    /**
     * Number of times event off all sessions
     */
    protected int getTotalOff(){
        return _totoff;
    }
    
    /**
     * Number of times event heard coming in to JMRI this session
     */
    protected int getSessionIn(){
        return _sessin;
    }
    
    /**
     * Number of times event heard coming in to JMRI all sessions
     */
    protected int getTotalIn(){
        return _totin;
    }

    /**
     * Number of times event heard being sent from JMRI this session
     */
    protected int getSessionOut(){
        return _sessout;
    }
    
    /**
     * Number of times event heard being sent from JMRI all sessions
     */
    protected int getTotalOut(){
        return _totout;
    }
    
    /**
     * Set Total On Events
     */
    protected void setTotalOn(int newVal) {
        _toton = newVal;
    }
    
    /**
     * Set Total Off Events
     */
    protected void setTotalOff(int newVal) {
        _totoff = newVal;
    }
    
    /**
     * Set Total Inward Events
     */
    protected void setTotalIn( int newVal) {
        _totin = newVal;
    }
    
    /**
     * Set Total Outward Events
     */
    protected void setTotalOut( int newVal ){
        _totout = newVal;
    }
    
    /**
     * Increase session count for on events
     */
    protected void bumpSessionOn(){
        _sesson++;
        _toton++;
    }

    /**
     * Increase session count for off events
     */
    protected void bumpSessionOff(){
        _sessoff++;
        _totoff++;
    }
    
    /**
     * Increase session count for inbound events
     */
    protected void bumpSessionIn(){
        _sessin++;
        _totin++;
    }    

    /**
     * Increase session count for outbound events
     */
    protected void bumpSessionOut(){
        _sessout++;
        _totout++;
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

}
