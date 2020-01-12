package jmri.jmrix.can.cbus.node;

import java.util.Arrays;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of CBUS Node Variables
 *
 * @author Steve Young (c) 2019
 * 
 */
public class CbusNodeNVTableDataModel extends javax.swing.table.AbstractTableModel {

    private int[] newNVs;
    private CbusNode nodeOfInterest;
    
    // column order needs to match list in column tooltips
    static public final int NV_NUMBER_COLUMN = 0;
    static public final int NV_CURRENT_VAL_COLUMN = 1;
    static public final int NV_CURRENT_HEX_COLUMN = 2;
    static public final int NV_CURRENT_BIT_COLUMN = 3;
    static public final int NV_SELECT_COLUMN = 4;
    static public final int NV_SELECT_HEX_COLUMN = 5;
    static public final int NV_SELECT_BIT_COLUMN = 6;
    static public final int MAX_COLUMN = 7;

    public CbusNodeNVTableDataModel(CanSystemConnectionMemo memo, int row, int column ) {
        log.debug("Starting MERG CBUS Node NV Table");
    }
    
    /**
     * Return the number of rows to be displayed.
     * {@inheritDoc} 
     */
    @Override
    public int getRowCount() {
        try {
            return nodeOfInterest.getTotalNVs();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public int getColumnCount() {
        return MAX_COLUMN;
    }

    /**
     * Returns String of column name from column int
     * used in table header
     * {@inheritDoc}
     * @param col int col number
     */
    @Override
    public String getColumnName(int col) { // not in any order
        switch (col) {
            case NV_NUMBER_COLUMN:
                return ("NV");
            case NV_CURRENT_VAL_COLUMN:
                return ("Dec.");
            case NV_CURRENT_HEX_COLUMN:
                return ("Hex.");
            case NV_CURRENT_BIT_COLUMN:
                return ("Bin.");
            case NV_SELECT_COLUMN:
                return ("New Dec.");
            case NV_SELECT_HEX_COLUMN:
                return ("New Hex.");
            case NV_SELECT_BIT_COLUMN:
                return("New Bin.");
            default:
                return "unknown " + col; // NOI18N
        }
    }
    
    /**
     * Returns column class type.
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case NV_SELECT_HEX_COLUMN:
            case NV_SELECT_BIT_COLUMN:
            case NV_CURRENT_HEX_COLUMN:
            case NV_CURRENT_BIT_COLUMN:
                return String.class;
            default:
                return Integer.class;
        }
    }
    
    /**
    * boolean return to edit table cell or not
    * {@inheritDoc}
    * @return boolean
    */
    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case NV_SELECT_COLUMN:
                return true;
            default:
                return false;
        }
    }

     /**
     * Return table values
     * {@inheritDoc}
     * @param row int row number
     * @param col int col number
     */
    @Override
    public Object getValueAt(int row, int col) {
        
        if ( nodeOfInterest.getTotalNVs() < 1 ) {
            return null;
        }
        
        switch (col) {
            case NV_NUMBER_COLUMN:
                return (row +1);
            case NV_CURRENT_VAL_COLUMN:
                return nodeOfInterest.getNV(row+1);
            case NV_CURRENT_HEX_COLUMN:
                if ( nodeOfInterest.getNV(row+1) > -1 ) {
                    return String.valueOf(Integer.toHexString(nodeOfInterest.getNV(row+1)));
                }
                else {
                    return "";
                }
            case NV_CURRENT_BIT_COLUMN:
                int num =  nodeOfInterest.getNV(row+1);
                if ( num > -1 ) {
                    return (String.format("%8s", Integer.toBinaryString(num)).replace(' ', '0')).substring(0,4) + " " +
                    (String.format("%8s", Integer.toBinaryString(num)).replace(' ', '0')).substring(4,8);
                }
                else {
                    return "";
                }
            case NV_SELECT_COLUMN:
                if ( newNVs.length < row+1) {
                    return 0;
                }
                if ( newNVs[(row+1)] > -1 ) {
                    return newNVs[(row+1)];
                } else {
                    return nodeOfInterest.getNV(row+1);
                }
            case NV_SELECT_HEX_COLUMN:
                if ( newNVs.length <= row+1) {
                    return "";
                }
                if (newNVs[(row+1)]>-1) {
                    return String.valueOf(Integer.toHexString(newNVs[(row+1)])); 
                }
                else {
                    return "";
                }
            case NV_SELECT_BIT_COLUMN:
                if ( newNVs.length <= row+1) {
                    return "";
                }
                if (newNVs[(row+1)]>-1) {
                    return (String.format("%8s", Integer.toBinaryString(newNVs[(row+1)])).replace(' ', '0')).substring(0,4) + " " +
                        (String.format("%8s", Integer.toBinaryString(newNVs[(row+1)])).replace(' ', '0')).substring(4,8);
                } else {
                    return "";
                }
            default:
                return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        log.debug("set value {} row {} col {}",value,row,col);
        if (col == NV_SELECT_COLUMN) {
            
            int newval = (int) value;
            if ( newNVs.length ==0) {
                return;
            }
            
            newNVs[(row+1)] = newval;
            ThreadingUtil.runOnGUIEventually( ()->{ 
                fireTableCellUpdated(row,col);
                fireTableCellUpdated(row,NV_SELECT_HEX_COLUMN);
                fireTableCellUpdated(row,NV_SELECT_BIT_COLUMN);
            });
        }
    }
    
    /**
     * @param node the CbusNode of Interest to the NV Table
     */
    public void setNode( CbusNode node){
        log.debug("setting array for node {}",node);
        
        if ( nodeOfInterest != null ) {
            nodeOfInterest.removeNodeNVTable(this);
        }
        
        nodeOfInterest = node;
        
        if ( nodeOfInterest == null ) {
            return;
        }
        
        resetNewNvs();
        nodeOfInterest.setNodeNVTable(this);
        ThreadingUtil.runOnGUIEventually( ()->{ 
            fireTableDataChanged();
        });
        
    }
    
    /**
     * Checks if a single NV has been edited to a new value
     * @param nvToCheck the single NV to check
     * @return true if dirty, else false
     */
    public boolean isSingleNvDirty( int nvToCheck ) {
        return ( (int) getValueAt(nvToCheck,NV_CURRENT_VAL_COLUMN) ) != (
                (int) getValueAt(nvToCheck,NV_SELECT_COLUMN) );
    }
    
    /**
     * Checks if any NV has been edited to a new value
     * @return true if any NV has been edited, else false
     */
    public boolean isTableDirty() {
        try {
            for (int i = 0; i < getRowCount(); i++) {            
                if ( isSingleNvDirty(i) ) {
                    return true;
                }
            }
            return false;
        }
        catch ( NullPointerException e ){
            return false;
        }
    }
    
    /**
     * Get count of changed NVs
     * @return number of changed NVs
     */
    public int getCountDirty() {
        int count = 0;
        for (int i = 0; i < getRowCount(); i++) {            
            if ( isSingleNvDirty(i) ) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Resets the edit NV value to match the actual NV value
     */
    public void resetNewNvs() {
        
        // setup a new fixed length array to hold new nv values
        if ( nodeOfInterest.getNvArray() == null ) {
            newNVs = new int[0];
        }
        else {
            newNVs = new int[ ( nodeOfInterest.getNvArray().length ) ];        
            newNVs = Arrays.copyOf(
                nodeOfInterest.getNvArray(),
                nodeOfInterest.getNvArray().length);
        }
        
        for (int i = 0; i < getRowCount(); i++) {
            
            setValueAt( getValueAt(i,NV_CURRENT_VAL_COLUMN), i, NV_SELECT_COLUMN);
        }
    }
    
    /**
     * Get a backup node containing the edited NVs
     * @return a node which has the new NV's
     */
    public CbusNodeFromBackup getChangedNode(){
        CbusNodeFromBackup temp = new CbusNodeFromBackup(nodeOfInterest,null);
        temp.setNVs(newNVs);
        return temp;
    }
    
    /**
     * Deregisters the NV Table from receiving updates from the CbusNode
     */
    public void dispose(){
        if ( nodeOfInterest != null ) {
            nodeOfInterest.removeNodeNVTable(this);
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusNodeNVTableDataModel.class);
}
