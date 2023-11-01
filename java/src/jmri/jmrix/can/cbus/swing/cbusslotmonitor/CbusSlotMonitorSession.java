package jmri.jmrix.can.cbus.swing.cbusslotmonitor;

import jmri.DccLocoAddress;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusOpCodes;

/**
 * Class to represent a session in the MERG CBUS Command Station Session Slot Monitor
 *
 * @see CbusSlotMonitorDataModel
 * @author Steve Young Copyright (C) 2019
 */
public class CbusSlotMonitorSession  {

    private final DccLocoAddress _locoAddr;
    private int _sessionId;
    private int _speed;
    private String _speedSteps;
    private boolean[] function;
    private int _flags;
    private int _consistId;

    /**
     * The table provides and maintains 1 row per loco address
     *
     * @param locoAddr Loco Address to be monitored
     */    
    protected CbusSlotMonitorSession( DccLocoAddress locoAddr ){
        _locoAddr = locoAddr;
        _sessionId = -1; // unset
        _speed = 0;
        _speedSteps="";
        function = new boolean[29];
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

    protected String getCommandedSpeed() {
        return CbusOpCodes.getSpeedFromByte(_speed);
    }

    protected String getDirection() {
        return CbusOpCodes.getDirectionFromByte(_speed );
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
        if (fn >= function.length) {
            boolean[] newArray = new boolean[fn+1];
            System.arraycopy(function, 0, newArray, 0, function.length);
            function = newArray;
        }
        function[fn] = tof;
    }

    protected String getFunctionString() {
        StringBuilder buf = new StringBuilder();
        for (int i=0; i<function.length; i++) {
            if ( function[i] ) {
                buf.append(i);
                buf.append(" ");
            }
        }
        return buf.toString().trim();
    }

    protected void setFlags( int flags ){
        _flags = flags;
        int mask = 0b11; // last 2 bits
        switch (flags & mask){
            case CbusConstants.CBUS_SS_14:
                _speedSteps="14";
                break;
            case CbusConstants.CBUS_SS_28_INTERLEAVE:
                _speedSteps="28I";
                break;
            case CbusConstants.CBUS_SS_28:
                _speedSteps="28";
                break;
            default:
                _speedSteps="128";
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
