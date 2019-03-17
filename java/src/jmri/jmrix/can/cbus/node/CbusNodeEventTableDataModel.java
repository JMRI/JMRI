package jmri.jmrix.can.cbus.node;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.util.ArrayList;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusNameService;
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
public class CbusNodeEventTableDataModel extends javax.swing.table.AbstractTableModel {

    private CbusNameService nameService;
    private CbusNode nodeOfInterest;
    
    // column order needs to match list in column tooltips
    static public final int NODE_NUMBER_COLUMN = 0;
    static public final int EVENT_NUMBER_COLUMN = 1;
  //  static public final int EVENT_PRODUCER_COLUMN = 2;
    static public final int NODE_EDIT_BUTTON_COLUMN = 2;
    static public final int NODE_NAME_COLUMN = 3;
    static public final int EVENT_NAME_COLUMN = 4;
    static public final int EV_VARS_COLUMN = 5;
    static public final int MAX_COLUMN = 6;
    
    CanSystemConnectionMemo _memo;

    public CbusNodeEventTableDataModel(CanSystemConnectionMemo memo, int row, int column) {
        
        log.debug("Starting MERG CBUS Node Event Table");
        
        _memo = memo;
        nameService = new CbusNameService();
    }
    
    /**
     * Return the number of rows to be displayed.
     */
    @Override
    public int getRowCount() {

        // nodetable event size
        try {
            return nodeOfInterest.getTotalNodeEvents();
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
     * </p>
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
            case EVENT_NUMBER_COLUMN:
                return ("Event Number");
            case NODE_NUMBER_COLUMN:
                return ("Node Number");
            case NODE_EDIT_BUTTON_COLUMN:
                return ("Edit");
            case NODE_NAME_COLUMN:
                return ("Node Name");
            case EVENT_NAME_COLUMN:
                return ("Event Name");
            case EV_VARS_COLUMN:
                return ("Event Variables");
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
            case NODE_EDIT_BUTTON_COLUMN:
            case EVENT_NUMBER_COLUMN:
            case NODE_NUMBER_COLUMN:
                return new JTextField(5).getPreferredSize().width;
            case NODE_NAME_COLUMN:
            case EVENT_NAME_COLUMN:
                return new JTextField(11).getPreferredSize().width;
            case EV_VARS_COLUMN:
                return new JTextField(16).getPreferredSize().width;
            default:
                log.warn("width {} undefined",col);
                return new JLabel(" <unknown> ").getPreferredSize().width; // NOI18N
        }
    }
    
    
    /**
    * Returns column class type.
    */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case EVENT_NUMBER_COLUMN:
            case NODE_NUMBER_COLUMN:
                return Integer.class;
            case NODE_NAME_COLUMN:
            case EVENT_NAME_COLUMN:
            case EV_VARS_COLUMN:
                return String.class;
            case NODE_EDIT_BUTTON_COLUMN:
                return JButton.class;
            default:
                log.warn("no class set col {}",col);
                return null;
        }
    }
    
    /**
    * Boolean return to edit table cell or not
    * @return boolean
    */
    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case NODE_EDIT_BUTTON_COLUMN:
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
        
        switch (col) {
            case NODE_NUMBER_COLUMN:
                try {
                    return nodeOfInterest.getNodeEventByArrayID(row).getNn();
                } catch ( NullPointerException e ) {
                    return -1;
                }
            case EVENT_NUMBER_COLUMN:
                try {
                    return nodeOfInterest.getNodeEventByArrayID(row).getEn();
                } catch ( NullPointerException e ) {
                    return -1;
                }
            case NODE_EDIT_BUTTON_COLUMN:
                return "Edit";
            case NODE_NAME_COLUMN:
                try {
                    int nnc = nodeOfInterest.getNodeEventByArrayID(row).getNn();
                    return nameService.getNodeName( nnc  );
                } catch ( NullPointerException e ) {
                    return "";
                }
                
            case EVENT_NAME_COLUMN:
                try {
                    int nnc = nodeOfInterest.getNodeEventByArrayID(row).getNn();
                    int enc = nodeOfInterest.getNodeEventByArrayID(row).getEn();
                    return nameService.getEventName( nnc, enc  );
                } catch ( NullPointerException e ) {
                    return "";
                }            
            case EV_VARS_COLUMN:
                try {
                    return nodeOfInterest.getNodeEventByArrayID(row).getEvVarString();
                } catch ( NullPointerException e ) {
                    return("");
                }
            default:
                log.error("internal state inconsistent with table request for row {} col {}", row, col);
                return null;
        }
    }
    
    /**
     *
     *
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == NODE_EDIT_BUTTON_COLUMN) {
            
            ThreadingUtil.runOnGUI( ()->{
                
                CbusNodeEditEventFrame editEvFrame = new CbusNodeEditEventFrame(null,
                    nodeOfInterest.getNodeEventByArrayID(row));
                    
                editEvFrame.initComponents(_memo);
                
            });
            
            
          //  _mainArray.get(row).setUserName( (String) value );
            
        }
    }
    
    public void setNode( CbusNode node){
        
        nodeOfInterest = node;
        
        node.setNodeEventTable(this);
       // fireTableDataChanged();
        
    }
    
    
    public void updateFromNode( int arrayid, int col){
        
        ThreadingUtil.runOnGUI( ()->{
            // fireTableCellUpdated(arrayid, col);
            fireTableDataChanged();
        });
    }
    
    /**
     * Remove Row from table
     * @param row int row number
     */    
    void removeRow(int row) {
    //   _mainArray.remove(row);
        ThreadingUtil.runOnGUI( ()->{ fireTableRowsDeleted(row,row); });
    }
    
    /**
     * disconnect from the CBUS
     */
    public void dispose() {
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusNodeEventTableDataModel.class);
}
