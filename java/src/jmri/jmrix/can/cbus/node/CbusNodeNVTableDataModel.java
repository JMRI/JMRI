package jmri.jmrix.can.cbus.node;

import java.util.Arrays;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.swing.nodeconfig.CbusNodeEditNVarFrame;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Cbus Node Variables
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
     */
    @Override
    public int getRowCount() {
        try {
            return nodeOfInterest.getTotalNVs();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    @Override
    public int getColumnCount() {
        return MAX_COLUMN;
    }

    /**
     * Configure a table to have our standard rows and columns.
     * <p>
     * This is optional, in that other table formats can use this table model.
     * But we put it here to help keep it consistent.
     */
    public void configureTable(JTable eventTable) {
        // allow reordering of the columns
        eventTable.getTableHeader().setReorderingAllowed(true);

        // shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        eventTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // resize columns as requested
        for (int i = 0; i < eventTable.getColumnCount(); i++) {
            int width = getPreferredWidth(i);
            eventTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        eventTable.sizeColumnsToFit(-1);
    }

    /**
     * Returns String of column name from column int
     * used in table header
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
    * Returns int of startup column widths
    * @param col int col number
    */
    public static int getPreferredWidth(int col) {
        switch (col) {
            case NV_NUMBER_COLUMN:
            case NV_CURRENT_BIT_COLUMN:
            case NV_SELECT_COLUMN:
            case NV_SELECT_HEX_COLUMN:
            case NV_SELECT_BIT_COLUMN:
                return new JTextField(6).getPreferredSize().width;
            case NV_CURRENT_VAL_COLUMN:
            case NV_CURRENT_HEX_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            default:
                return new JTextField(" <unknown> ").getPreferredSize().width; // NOI18N
        }
    }
    
    public int getTotalNumOfNvs() {
        return nodeOfInterest.getTotalNVs();
    }
    
    /**
    * Returns column class type.
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
    * Boolean return to edit table cell or not
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
                if ( newNVs[(row+1)] > -1 ) {
                    return newNVs[(row+1)];
                } else {
                    return nodeOfInterest.getNV(row+1);
                }
            case NV_SELECT_HEX_COLUMN:
                if ( newNVs[(row+1)] != nodeOfInterest.getNV(row+1) ) {
                    return String.valueOf(Integer.toHexString(newNVs[(row+1)])); 
                } else {
                    return "";
                }
            case NV_SELECT_BIT_COLUMN:
                if ( newNVs[(row+1)] != nodeOfInterest.getNV(row+1) ) {
                    return (String.format("%8s", Integer.toBinaryString(newNVs[(row+1)])).replace(' ', '0')).substring(0,4) + " " +
                        (String.format("%8s", Integer.toBinaryString(newNVs[(row+1)])).replace(' ', '0')).substring(4,8);
                }
                else {
                    return "";
                }
            default:
                return null;
        }
    }
    
    /**
     *
     *
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        log.debug("set value {} row {} col {}",value,row,col);
        if (col == NV_SELECT_COLUMN) {
            newNVs[(row+1)] = (int) value;
            updateFromNode(row,col);
        }
    }
    
    public void setViewFrame(){
        nodeOfInterest.setNodeNVTable(this);
    }
    
    public void setEditFrame(){
        nodeOfInterest.setEditNodeNVTable(this);
    }
    
    public void setNode( CbusNode node){
        log.debug("setting array for node {}",node);
        
        if ( nodeOfInterest != null ) {
            nodeOfInterest.setNodeNVTable(null);
            nodeOfInterest.setEditNodeNVTable(null);
        }
        
        nodeOfInterest = node;
        
        if ( nodeOfInterest == null ) {
            return;
        }
        
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
    }
    
    public void updateFromNode( int arrayid, int col){
        ThreadingUtil.runOnGUI( ()->{
            // fireTableCellUpdated(arrayid, col);
            fireTableDataChanged();
        });
    }
    
    public Boolean isTableLoaded(){
        
        if ( getRowCount() < 1 ) {
            return false;
        }
        try {
            for (int i = 0; i < getRowCount(); i++) {
                if ( ( (int) getValueAt(i,NV_CURRENT_VAL_COLUMN) ) < 0 ) {
                    return false;
                }
            }
            return true;
        }
        catch ( NullPointerException e ){
            return false;
        }
    }
    
    public Boolean isSingleNvDirty( int nvToCheck ) {
        if ( ( (int) getValueAt(nvToCheck,NV_CURRENT_VAL_COLUMN) ) != (
            (int) getValueAt(nvToCheck,NV_SELECT_COLUMN) ) ) {
            return true;
        }
        return false;
    }
    
    public Boolean isTableDirty() {
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
    
    public int getCountDirty() {
        int count = 0;
        for (int i = 0; i < getRowCount(); i++) {            
            if ( isSingleNvDirty(i) ) {
                count++;
            }
        }
        return count;
    }
    
    public void resetNewNvs() {
        for (int i = 0; i < getRowCount(); i++) {
            setValueAt( getValueAt(i,NV_CURRENT_VAL_COLUMN), i, NV_SELECT_COLUMN);
        }
    }
    
    public void passChangedNvsToNode ( CbusNodeEditNVarFrame frame ) {
        
        // log.info(" pass changes arr length {} ",newNVs.length);
        nodeOfInterest.sendNvsToNode( newNVs, frame, null);
        
    }
    
    public void dispose(){
        if ( nodeOfInterest != null ) {
            nodeOfInterest.setNodeNVTable(null);
            nodeOfInterest.setEditNodeNVTable(null);
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusNodeNVTableDataModel.class);
}
