package jmri.jmrix.can.cbus.eventtable;

import java.util.Date;
import jmri.jmrix.can.CanSystemConnectionMemo;
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
    private int _sessoff;
    private int _sessin;
    private int _sessout;
    private String _stlonstring;
    private String _stloffstring;
    private Date _timestamp;
    
    public CbusTableEvent( int nn, int en, 
        EvState state, int canid, String name, String nodeName, String comment, 
        int sesson, int sessoff, int sessin, int sessout, Date timestamp ){
        
        super(nn,en);
        _state = state;
        _canid = canid;
        _name = name;
        _nodeName = nodeName;
        _comment = comment;
        _sesson = sesson;
        _sessoff = sessoff;
        _sessin = sessin;
        _sessout = sessout;
        _stlonstring ="";
        _stloffstring = "";
        _timestamp = timestamp;
        
    }
    
    protected Date getDate(){
        return _timestamp;
    }
    
    protected void setDate(Date newval) {
        _timestamp = newval;
    }
    
    protected String getStlOn(){
        return _stlonstring;
    }

    protected String getStlOff(){
        return _stloffstring;
    }
    
    protected void setStlOn(String newval){
        _stlonstring = newval;
    }

    protected void setStlOff(String newval){
        _stloffstring = newval;
    }

    protected int getEventCanId(){
        return _canid;
    }
    
    protected void setComment(String newval){
        _comment = newval;
    }

    protected String getComment(){
        return _comment;
    }

    protected void setCanId(int newval){
        _canid = newval;
    }

    protected int getSessionOn(){
        return _sesson;
    }

    protected int getSessionOff(){
        return _sessoff;
    }
    
    protected int getSessionIn(){
        return _sessin;
    }

    protected int getSessionOut(){
        return _sessout;
    }
    
    protected void bumpSessionOn(){
        _sesson++;
    }

    protected void bumpSessionOff(){
        _sessoff++;
    }
    
    protected void bumpSessionIn(){
        _sessin++;
    }    
    
    protected void bumpSessionOut(){
        _sessout++;
    }

}
