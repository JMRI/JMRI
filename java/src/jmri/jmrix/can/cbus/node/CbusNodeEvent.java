package jmri.jmrix.can.cbus.node;

import java.util.Arrays;
import java.util.Objects;
import jmri.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent an event stored on a node.
 * <p>
 * Custom Equals method
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeEvent extends CbusBasicNodeEvent implements Comparable<CbusNodeEvent> {
    
    private int[] _evVarArr;
    
    /**
     * Set the value of the event variable array by index
     *
     * @param memo CAN System Connection
     * @param nn Event node Number
     * @param en Event event or device number
     * @param thisnode Host node number
     * @param index number assigned by node, -1 if unknown
     * @param maxEvVar Maximum event variables for the event
     */
    public CbusNodeEvent( jmri.jmrix.can.CanSystemConnectionMemo memo, int nn, int en, int thisnode, int index, int maxEvVar){
        super(memo,nn,en,thisnode,index);
        _evVarArr = new int[maxEvVar];
        java.util.Arrays.fill(_evVarArr,-1);
    }
    
    protected CbusNodeEvent( int nn, int en, int thisnode, String eventString ){
        super(null,nn,en,thisnode,-1);
        _evVarArr = StringUtil.intBytesWithTotalFromNonSpacedHexString(eventString,false);
        setTempFcuNodeName(null);
    }
    
    protected CbusNodeEvent( CbusNodeEvent existing ) {
        super(null,existing.getNn(),existing.getEn(),existing.getParentNn(),existing.getIndex());
        setEvArr( Arrays.copyOf(
            existing.getEvVarArray(),
            existing.getEvVarArray().length) );
        setTempFcuNodeName ( existing.getTempFcuNodeName());
        
    }

    /**
     * Set the value of the event variable array by index
     *
     * @param index event variable index, minimum 1
     * @param value min 0 max 255
     */
    public void setEvVar(int index, int value) {
        if ( index < 1 ) {
            log.error("Event Index needs to be more than 0");
            return;
        }
        if (value > 255 ) {
            log.error("Event Variable value needs to be less than 255 (oxff)");
            return;
        }
        _evVarArr[(index-1)]=value;
        notifyModel();
        
    }
    
    /**
     * Set the value of the event variable array by existing array
     *
     * @param newArray event variable array, 1st value index 0 should be 1st event value, NOT total
     */    
    public final void setEvArr( int[] newArray ){
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
    
    public int[] getEvVarArray() {
        return _evVarArr;
    }
    
    /**
     * Returns all event variables as a single string
     * <p>
     * eg. /"1, 13, 1, 0, 0/"
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
     * Returns all event variables as a single hex string
     * <p>
     * eg. returns 0104D6A0
     *
     * @return the hex string for of the array
     */ 
    public String getHexEvVarString() {
        if (getEvVarArray() != null) {
            return StringUtil.hexStringFromInts(getEvVarArray()).replaceAll("\\s", "");
        }
        return "";
    }

    /**
     * Returns the number of unknown event variables
     *
     * @return the decimal outstanding total
     */    
    public int getOutstandingVars() {
        if ( getEvVarArray() == null ){
            return 0;
        }
        int count = 0;
        for (int val : getEvVarArray()){
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
     * Get the number of event variables
     * by Array Length
     * 
     * @return number of event variables
     */      
    public int getNumEvVars() {
        return _evVarArr.length;
    }
    
    /**
     * Sets unknown event variables to 0
     * 
     */
    protected void allOutstandingEvVarsNotNeeded(){
        for (int i = 0; i < _evVarArr.length; i++) {
            if ( _evVarArr[i] == -1) {
                _evVarArr[i] = 0;
            }
        }
    }
    
    /** 
     * {@inheritDoc} 
     * <p>
     * Custom method to compare Node Num, Ev Num, Parent Node Num, Event Variables
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CbusNodeEvent)) {
            return false;
        }
        CbusNodeEvent t = (CbusNodeEvent) o;
        if ( this.getEn()!=t.getEn() || this.getNn()!=t.getNn() ) {
            return false;
        }
        if ( this.getParentNn()!=t.getParentNn() ) {
            return false;
        }
        return this.getHexEvVarString().equals(t.getHexEvVarString());
    }
    
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(getEn(), getNn(), getParentNn(), getHexEvVarString());
    }
    
    /** 
     * {@inheritDoc} 
     * Compares to the Node / Event numbers of the Event
     */
    @Override
    public int compareTo(CbusNodeEvent o) {
        return Integer.compare(this.listOrder(),o.listOrder());
    }
    
    private int listOrder(){
        return (getNn()*65535+getEn())+100+(Objects.hash(getHexEvVarString())%100);
    }
    
    private static final Logger log = LoggerFactory.getLogger(CbusNodeEvent.class);

}
