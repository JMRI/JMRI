package jmri.jmrix.can.cbus.swing.cbusslotmonitor;

import jmri.DccLocoAddress;

/**
 * Class to represent a session in the MERG CBUS Command Station Session Slot Monitor
 *
 * @see CbusSlotMonitorDataModel
 * @author Steve Young Copyright (C) 2019
 */
public class CbusSlotMonitorSession  {
    
    private DccLocoAddress _locoAddr;
    private int _sessionId;
    private int _speed;
    private String _speedSteps;
    private boolean[] _function;
    private int _flags;
    private int _consistId;
    
    /**
    * The table provides and maintains 1 row per loco address
    *
    */    
    protected CbusSlotMonitorSession( DccLocoAddress locoAddr ){
        _locoAddr = locoAddr;
        _sessionId = -1;
        _speed = 0;
        _speedSteps="";
        _function = new boolean[29];
        _flags = -1; // unset
        _consistId = 0;
    }
    
    protected DccLocoAddress getLocoAddr(){
        return _locoAddr;
    }
    
    protected void setSessionId( int session ) {
        _sessionId = session;
    }

    protected int getSessionId() {
        return _sessionId;
    }
    
    protected void setDccSpeed( int speed) {
        _speed = speed;
    }
    
    protected int getCommandedSpeed() {
        String speedflags = String.format("%8s", 
        Integer.toBinaryString( _speed & 0xFF)).replace(' ', '0');
        int directionSpeed = Integer.parseInt((speedflags.substring(1)), 2);
        if ( directionSpeed == 1 ) {
            return 0;
        }
        else {
            return directionSpeed;
        }
        
    }
    
    protected String getDirection() {
        if ( _speed == 1 ){
            return ( Bundle.getMessage("EStop" ) + Bundle.getMessage("REV") );
        }
        else if ( _speed == 129 ){
            return ( Bundle.getMessage("EStop" ) + Bundle.getMessage("FWD"));
        }
        else if ( getSpeedSteps().equals("14") ){
            if ( _speed > 13 ){
                return (Bundle.getMessage("FWD"));
            }
            else {
                return (Bundle.getMessage("REV"));
            }
        }
        else if ( getSpeedSteps().equals("28") || getSpeedSteps().equals("28I") ) {
            if ( _speed > 27 ){
                return (Bundle.getMessage("FWD"));
            }
            else {
                return (Bundle.getMessage("REV"));
            }
        }
        else { // default to 128 ( 126 ) steps
            if ( _speed > 127 ){
                return (Bundle.getMessage("FWD"));
            }
            else {
                return (Bundle.getMessage("REV"));
            }
        }
    }
    
    protected void setSpeedSteps ( String steps ) {
        _speedSteps = steps;
    }
    
    protected String getSpeedSteps() {
        if ( _speedSteps.isEmpty() ) {
            return ("128");
        }
        else {
            return _speedSteps;
        }
    }

    protected void setFunction( int fn, boolean tof ) {
        _function[fn] = tof;
    }
    
    protected String getFunctionString() {
        StringBuilder buf = new StringBuilder();
        for (int i=0; i<29; i++) {
            if ( _function[i] ==true ) {
                buf.append(i);
                buf.append(" ");
            }
        }
        return buf.toString();
    }
    
    protected void setFlags( int flags ){
        _flags = flags;
        boolean sm0 = ((flags >> 0 ) & 1) != 0;
        boolean sm1 = ((flags >> 1 ) & 1) != 0;
        if ((!sm0) && (!sm1)){
            _speedSteps="128";
        }
        else if ((!sm0) && (sm1)){
            _speedSteps="14";
        }        
        else if ((sm0) && (!sm1)){
            _speedSteps="28I";
        }        
        else if ((sm0) && (sm1)){
            _speedSteps="28";
        }
    }
    
    protected String getFlagString() {
        
        if ( _flags < 0 ){
            return ("");
        }
        
        StringBuilder flagstring = new StringBuilder();
        
        boolean esa = ((_flags >> 4 ) & 1) != 0; // bit4
        boolean esb = ((_flags >> 5 ) & 1) != 0; // bit5
        flagstring.append(Bundle.getMessage("EngineState"));
        if ((!esa) && (!esb)){
            flagstring.append(Bundle.getMessage("Active"));
        }
        else if ((!esa) && (esb)){
            flagstring.append(Bundle.getMessage("Consisted"));
        }        
        else if ((esa) && (!esb)){
            flagstring.append(Bundle.getMessage("Consistmaster"));
        }        
        else if ((esa) && (esb)){
            flagstring.append(Bundle.getMessage("Inactive"));
        }            
        flagstring.append(" ");            
        flagstring.append(Bundle.getMessage("Lights"));
        flagstring.append(((_flags >> 2 ) & 1)); // bit2
        flagstring.append(" ");
        flagstring.append(Bundle.getMessage("RelDirection"));
        flagstring.append(((_flags >> 3 ) & 1)); // bit3
        flagstring.append(" ");
        
        return flagstring.toString();
    }

    protected void setConsistId( int consistid ) {
        _consistId = consistid;
    }
    
    protected int getConsistId() {
        return _consistId;
    }

}
