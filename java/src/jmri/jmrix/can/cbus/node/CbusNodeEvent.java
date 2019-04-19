package jmri.jmrix.can.cbus.node;

import java.util.Arrays;
import javax.annotation.Nonnull;
import jmri.jmrix.can.cbus.CbusEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent an event stored on a node.
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeEvent extends CbusEvent {
    private int _thisnode;
    private int _index;
    public int[] _evVarArr;
    private String _fcuNodeName;
    private CbusNodeSingleEventTableDataModel eventDataModel;
    
    /**
     * Set the value of the event variable array by index
     *
     * @param nn Event node Number
     * @param en Event event or device number
     * @param thisnode Host node number
     * @param index number assigned by node, -1 if unknown
     * @param maxEvVar Maximum event variables for the event
     */
    public CbusNodeEvent( int nn, int en, int thisnode, int index, int maxEvVar){
        super(nn,en);
        _thisnode = thisnode;
        _index = index;
        _evVarArr = new int[maxEvVar];
        java.util.Arrays.fill(_evVarArr,-1);
        _fcuNodeName = "";
    }
    
    protected void setEditTableModel( CbusNodeSingleEventTableDataModel model ) {
        eventDataModel = model;
    }
    
    private void notifyModel(){
        if ( eventDataModel != null ) {
            jmri.util.ThreadingUtil.runOnGUI( ()->{
                eventDataModel.fireTableDataChanged();
        });
        }
    }

    /**
     * Set the value of the event variable array by index
     *
     * @param index event variable index, minimum 1
     * @param value min 0 max 255
     */
    public void setEvVar(int index, int value) {
        if ( index < 1 ) {
            log.error("Event Index needs to be > 0");
        } else {
            _evVarArr[(index-1)]=value;
            notifyModel();
        }
    }
    
    /**
     * Set the value of the event variable array by existing array
     *
     * @param newArray event variable array, 1st value index 0 should be 1st event value, NOT total
     */    
    public void setEvArr( int[] newArray ){
        _evVarArr = newArray;
        notifyModel();
    }
    
    /**
     * Returns the value of an event variable
     *
     * @param index of the variable, no array offset needed, 1 is 1
     * @return the decimal event indexed variable value
     */
    public int getEvVar(int index) {
        return _evVarArr[(index-1)];
    }
    
    /**
     * Returns all event variables as a single string
     *
     * @return the decimal string for of the array, unknown values are blanked
     */    
    public String getEvVarString(){
        StringBuilder n = new StringBuilder();
        // n.append("[ ");
        for(int i = 0; i< _evVarArr.length; i++){
            if ( _evVarArr[i] > -1 ) {
                n.append( _evVarArr[i] );
            }
            else {
                n.append( " " );
            }
            if ( i != ( _evVarArr.length-1 ) ) {
                n.append( ", " );
            }
        }
        // n.append(" ]");
        return n.toString();
    }

    /**
     * Returns the number of unknown event variables
     *
     * @return the decimal outstanding total
     */    
    public int getOutstandingVars() {
        if ( _evVarArr == null ){
            return 0;
        }
        int count = 0;
        for (int val : _evVarArr){
            if (val == -1) {
                count ++;
            }
        }
        return count;
    }
    
    /**
     * Returns the index of the next unknown event variable
     * @return the decimal index value else 0 if all known
     */     
    public int getNextOutstanding() {
        for (int i = 0; i < _evVarArr.length; i++) {
            if ( _evVarArr[i] == -1) {
                return i+1;
            }
        }
        return 0;
    }

    /**
     * Returns the parent host node of the node event
     *
     * @return decimal node number
     */     
    public int getParentNn(){
        return _thisnode;
    }

    /**
     * Set the index number of this event on a node
     * <p>
     * Index number not valid after any event has been written to or deleted from the node
     * 
     * @param index number, -1 if unset
     */  
    public void setIndex(int index){
        _index = index;
    }

    /**
     * Get the index number of this event on a node
     * 
     * @return index number, -1 if unset
     */  
    public int getIndex(){
        return _index;
    }

    /**
     * Get the number of event variables
     * 
     */      
    public int getNumEvVars() {
        return _evVarArr.length;
    }
    
    /**
     * Set a temporary node name
     * 
     */
    public void setTempFcuNodeName( String tempName){
        _fcuNodeName = tempName;
    }
    
    /**
     * Get a temporary node name
     * 
     */
    public String getTempFcuNodeName(){
        return _fcuNodeName;
    }
    
    private static final Logger log = LoggerFactory.getLogger(CbusNodeEvent.class);

}
