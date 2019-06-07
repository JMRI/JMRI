package jmri.jmrix.can.cbus.node;

import java.util.Arrays;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.swing.nodeconfig.CbusNodeEditEventFrame;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Cbus Nodes
 *
 * @author Steve Young (c) 2019
 * 
 */
public class CbusNodeSingleEventTableDataModel extends javax.swing.table.AbstractTableModel {

    private CbusNodeTableDataModel nodeModel = null;
    public int[] newEVs;
    private CbusNodeEvent _ndEv;
    
    // column order needs to match list in column tooltips
    static public final int EV_NUMBER_COLUMN = 0;
    static public final int EV_CURRENT_VAL_COLUMN = 1;
    static public final int EV_CURRENT_HEX_COLUMN = 2;
    static public final int EV_CURRENT_BIT_COLUMN = 3;
    static public final int EV_SELECT_COLUMN = 4;
    static public final int EV_SELECT_HEX_COLUMN = 5;
    static public final int EV_SELECT_BIT_COLUMN = 6;
    static public final int MAX_COLUMN = 7;

    public CbusNodeSingleEventTableDataModel(CanSystemConnectionMemo memo, int row, int column , CbusNodeEvent ndEv) {
        
        log.debug("Starting a Single Node Event Variable Model");

        _ndEv = ndEv;
        if ( _ndEv._evVarArr == null ) {
            newEVs = new int[0];
        }
        else {
            newEVs = new int[ ( _ndEv._evVarArr.length ) ];
            log.debug(" set node newEVs length {} ",newEVs.length);
        
            newEVs = Arrays.copyOf(
                _ndEv._evVarArr,
                _ndEv._evVarArr.length);
            log.debug(" set ev var arr length {} data {}",newEVs.length, newEVs);
        }
        _ndEv.setEditTableModel(this);
    }
    
    /**
     * Return the number of rows to be displayed.
     */
    @Override
    public int getRowCount() {
        return _ndEv.getNumEvVars();
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
            case EV_NUMBER_COLUMN:
                return ("EV Var");
            case EV_CURRENT_VAL_COLUMN:
                return ("Dec.");
            case EV_CURRENT_HEX_COLUMN:
                return ("Hex.");
            case EV_CURRENT_BIT_COLUMN:
                return ("Bin.");
            case EV_SELECT_COLUMN:
                return ("New Dec.");
            case EV_SELECT_HEX_COLUMN:
                return ("New Hex.");
            case EV_SELECT_BIT_COLUMN:
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
            case EV_NUMBER_COLUMN:
            case EV_CURRENT_BIT_COLUMN:
            case EV_SELECT_COLUMN:
            case EV_SELECT_HEX_COLUMN:
            case EV_SELECT_BIT_COLUMN:
                return new JTextField(6).getPreferredSize().width;
            case EV_CURRENT_VAL_COLUMN:
            case EV_CURRENT_HEX_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            default:
                return new JTextField(" <unknown> ").getPreferredSize().width; // NOI18N
        }
    }
    
    /**
    * Returns column class type.
    */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case EV_SELECT_HEX_COLUMN:
            case EV_SELECT_BIT_COLUMN:
            case EV_CURRENT_HEX_COLUMN:
            case EV_CURRENT_BIT_COLUMN:
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
            case EV_SELECT_COLUMN:
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
        
        int currEvVal = _ndEv.getEvVar(row+1);
        
        switch (col) {
            case EV_NUMBER_COLUMN:
                return (row +1);
            case EV_CURRENT_VAL_COLUMN:
                if ( ( newEVs[(row)] < 0 ) && ( currEvVal > -1 )){
                    newEVs[(row)] = currEvVal;
                }
                return currEvVal;
            case EV_CURRENT_HEX_COLUMN:
                if ( currEvVal > -1 ) {
                    return String.valueOf(Integer.toHexString(currEvVal));
                }
                else {
                    return currEvVal;
                }
            case EV_CURRENT_BIT_COLUMN:
                if ( currEvVal > -1 ) {
                    return (String.format("%8s", Integer.toBinaryString(currEvVal)).replace(' ', '0')).substring(0,4) + " " +
                    (String.format("%8s", Integer.toBinaryString(currEvVal)).replace(' ', '0')).substring(4,8);
                }
                else {
                    return currEvVal;
                }
            case EV_SELECT_COLUMN:
                if ( newEVs[(row)] > -1 ) {
                    return newEVs[(row)];
                } else {
                    return currEvVal;
                }
            case EV_SELECT_HEX_COLUMN:
                if ( newEVs[(row)] != currEvVal ) {
                    return String.valueOf(Integer.toHexString(newEVs[(row)])); 
                } else {
                    return "";
                }
            case EV_SELECT_BIT_COLUMN:
                if ( newEVs[(row)] != currEvVal ) {
                    return (String.format("%8s", Integer.toBinaryString(newEVs[(row)])).replace(' ', '0')).substring(0,4) + " " +
                        (String.format("%8s", Integer.toBinaryString(newEVs[(row)])).replace(' ', '0')).substring(4,8);
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
        if (col == EV_SELECT_COLUMN) {
            newEVs[(row)] = (int) value;
            updateFromNode(row,col);
        }
    }
    
    public void updateFromNode( int arrayid, int col){
        ThreadingUtil.runOnGUI( ()->{
            // fireTableCellUpdated(arrayid, col);
            fireTableDataChanged();
        });
    }
    
    public boolean isTableLoaded(){
        
        if ( getRowCount() < 1 ) {
            return false;
        }
        try {
            for (int i = 0; i < getRowCount(); i++) {            
                if ( ( (int) getValueAt(i,EV_CURRENT_VAL_COLUMN) ) < 0 ) {
                    return false;
                }
            }
            return true;
        }
        catch ( NullPointerException e ){
            return false;
        }
    }
    
    public boolean isSingleEvDirty( int evToCheck ) {
        
        if ( ( (int) getValueAt(evToCheck,EV_CURRENT_VAL_COLUMN) ) != (
            (int) getValueAt(evToCheck,EV_SELECT_COLUMN) ) ) {
            return true;
        }
        return false;
        
    }
    
    public boolean isTableDirty() {
        try {
            for (int i = 0; i < getRowCount(); i++) {            
                if ( isSingleEvDirty(i) ) {
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
            if ( isSingleEvDirty(i) ) {
                count++;
            }
        }
        return count;
    }
    
    public void resetnewEVs() {
        
        for (int i = 0; i < getRowCount(); i++) {
            setValueAt( getValueAt(i,EV_CURRENT_VAL_COLUMN), i, EV_SELECT_COLUMN);
        }
        
    }
    
    public void passNewEvToNode ( CbusNodeEditEventFrame frame ) {
        
        CbusNodeEvent newevent = new CbusNodeEvent(
                frame.getNodeVal(),
                frame.getEventVal(),
                _ndEv.getParentNn(),
                -1,
                _ndEv.getNumEvVars() );
        
        newevent.setEvArr(newEVs);
        
        ArrayList<CbusNodeEvent> eventArray = new ArrayList<CbusNodeEvent>(1);
        eventArray.add(newevent);
        log.debug(" pass changes arr length {} ",newEVs.length);
        try {
            nodeModel = jmri.InstanceManager.getDefault(CbusNodeTableDataModel.class);
            nodeModel.getNodeByNodeNum( _ndEv.getParentNn() ).sendNewEvSToNode( eventArray, frame, null);
        } catch (NullPointerException e) {
            log.error("Unable to get Node Table from Instance Manager");
        }
        
    }

    public void passEditEvToNode( CbusNodeEditEventFrame frame ) {

        if ( frame.spinnersDirty() ) {
            
            try {
                nodeModel = jmri.InstanceManager.getDefault(CbusNodeTableDataModel.class);
                // learn mode - timeout, no feedback from node
                // unlearn event - timeout, no feedback from node
                // this should take 100ms
                nodeModel.getNodeByNodeNum( _ndEv.getParentNn() ).deleteEvOnNode(_ndEv.getNn(), _ndEv.getEn(), null );
            } catch (NullPointerException e) {
                log.error("Unable to get Node Table from Instance Manager");
            }
            
            // learn mode - to reset after unlearn, timeout, no feedback from node
            // teach new event ( as brand new event )
            // notify frame
            ThreadingUtil.runOnLayoutDelayed( () -> {
            
                passNewEvToNode(frame);
            
            }, 200 );
            
        } else {
            
            // loop through each ev var and send
            passNewEvToNode(frame);
            
        }
    }
    
    public void dispose(){
        _ndEv.setEditTableModel(null);
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusNodeSingleEventTableDataModel.class);
}
