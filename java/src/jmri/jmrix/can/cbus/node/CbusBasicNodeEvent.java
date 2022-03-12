package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.cbus.CbusEvent;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Base Class of CbusNodeEvent
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusBasicNodeEvent extends CbusEvent {
    private final int _thisnode;
    private int _index;
    private String _fcuNodeName;
    private CbusNodeSingleEventTableDataModel eventDataModel;
    
    /**
     * Set the value of the event variable array by index
     *
     * @param memo CAN System Connection
     * @param nn Event node Number
     * @param en Event event or device number
     * @param thisnode Host node number
     * @param index number assigned by node, -1 if unknown
     */
    public CbusBasicNodeEvent( jmri.jmrix.can.CanSystemConnectionMemo memo, 
                int nn, int en, int thisnode, int index){
        super(memo,nn,en);
        _thisnode = thisnode;
        _index = index;
        _fcuNodeName = "";
    }
    
    protected void setEditTableModel( CbusNodeSingleEventTableDataModel model ) {
        eventDataModel = model;
    }
    
    protected void notifyModel(){
        if ( eventDataModel != null ) {
            eventDataModel.fireTableDataChanged();
        }
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
    public final void setIndex(int index){
        _index = index;
    }

    /**
     * Get the index number of this event on a node
     * 
     * @return index number, -1 if unset
     */  
    public final int getIndex(){
        return _index;
    }
    
    /**
     * Set a temporary node name
     * @param tempName the name to use
     */
    public final void setTempFcuNodeName( String tempName){
        if ( tempName !=null ){
            _fcuNodeName = tempName;
        }
    }
    
    /**
     * Get the temporary node name
     * @return the name
     */
    public final String getTempFcuNodeName(){
        return _fcuNodeName;
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
    
    // private static final Logger log = LoggerFactory.getLogger(CbusBasicNodeEvent.class);

}
