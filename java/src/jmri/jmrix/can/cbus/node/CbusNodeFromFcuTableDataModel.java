package jmri.jmrix.can.cbus.node;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Cbus Nodes imported from MERG FCU
 *
 * @author Steve Young (c) 2019
 * @see CbusNodeFromBackup
 * 
 */
public class CbusNodeFromFcuTableDataModel extends CbusNodeTableDataModel {

    // column order needs to match list in column tooltips
    static public final int FCU_NODE_NUMBER_COLUMN = 0; 
    static public final int FCU_NODE_TYPE_NAME_COLUMN = 1; 
    static public final int FCU_NODE_USER_NAME_COLUMN = 2;
    static public final int FCU_NODE_EVENTS_COLUMN = 3;
    static public final int NODE_NV_TOTAL_COLUMN = 4;
    static public final int FCU_NODE_TOTAL_BYTES_COLUMN = 5;
    static public final int FCU_MAX_COLUMN = 6;

    public CbusNodeFromFcuTableDataModel(CanSystemConnectionMemo memo, int row, int column) {
        super (memo, row, column);
        
        _mainArray = new ArrayList<>();
        
    }
    
    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent ev){
        if (!(ev.getSource() instanceof CbusNode)) {
            return;
        }
        this.fireTableDataChanged();
    }
    
    // order needs to match column list top of dtabledatamodel
    public static final String[] FCUTABLETIPS = {
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null

    }; // Length = number of items in array should (at least) match number of columns
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowCount() {
        return _mainArray.size();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return FCU_MAX_COLUMN;
    }

    /**
     * Configure a table to have our standard rows and columns.
     * <p>
     * This is optional, in that other table formats can use this table model.
     * But we put it here to help keep it consistent.
     * @param eventTable table to configure
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int col) { // not in any order
        switch (col) {
            case FCU_NODE_NUMBER_COLUMN:
                return ("Node Num");
            case FCU_NODE_USER_NAME_COLUMN:
                return ("User Name");
            case FCU_NODE_TYPE_NAME_COLUMN:
                return ("Node Type");
            case FCU_NODE_EVENTS_COLUMN:
                return ("Events");
            case NODE_NV_TOTAL_COLUMN:
                return("Total NV's");
            case FCU_NODE_TOTAL_BYTES_COLUMN:
                return("Tot. Bytes");
            default:
                return "unknown " + col; // NOI18N
        }
    }

    /**
     * Returns int of startup column widths
     * @param col int col number
     * @return preferred width
     */
    public static int getPreferredWidth(int col) {
        switch (col) {
            case FCU_NODE_EVENTS_COLUMN:
            case FCU_NODE_NUMBER_COLUMN:
            case NODE_NV_TOTAL_COLUMN:
            case FCU_NODE_TOTAL_BYTES_COLUMN:
                return new JTextField(6).getPreferredSize().width;
            case FCU_NODE_TYPE_NAME_COLUMN:
            case FCU_NODE_USER_NAME_COLUMN:
                return new JTextField(13).getPreferredSize().width;
            default:
                return new JTextField(" <unknown> ").getPreferredSize().width; // NOI18N
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case FCU_NODE_NUMBER_COLUMN:
            case FCU_NODE_EVENTS_COLUMN:
            case NODE_NV_TOTAL_COLUMN:
            case FCU_NODE_TOTAL_BYTES_COLUMN:
                return Integer.class;
            case FCU_NODE_USER_NAME_COLUMN:
            case FCU_NODE_TYPE_NAME_COLUMN:
                return String.class;
            default:
                return null;
        }
    }
    
    /**
     * Boolean return to edit table cell or not
     * @return boolean
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case FCU_NODE_NUMBER_COLUMN:
                return _mainArray.get(row).getNodeNumber();
            case FCU_NODE_USER_NAME_COLUMN:
                return _mainArray.get(row).getUserName();
            case FCU_NODE_TYPE_NAME_COLUMN:
                return _mainArray.get(row).getNodeStats().getNodeTypeName();
            case FCU_NODE_EVENTS_COLUMN:
                return _mainArray.get(row).getNodeEventManager().getTotalNodeEvents();
            case NODE_NV_TOTAL_COLUMN:
                return _mainArray.get(row).getNodeNvManager().getTotalNVs();
            case FCU_NODE_TOTAL_BYTES_COLUMN:
                return _mainArray.get(row).getNodeStats().totalNodeBytes();
            default:
                return null;
        }
    }
    
    /**
     * Ignored as data from file.
     * @param value object value
     * @param row int row number
     * @param col int col number
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
    }

    /**
     * Ignored as data from file
     * @param m canmessage
     */
    @Override
    public void message(CanMessage m) { // outgoing cbus message
    }
    
    /**
     * Ignored as data from file
     * @param m CanMessage
     */
    @Override
    public void reply(CanReply m) { // incoming cbus message
    }
    
    /**
     * Returns a new or existing Backup node by node number
     * @param nodenum Node Number
     */
    @Override
    public CbusNodeFromBackup provideNodeByNodeNum( int nodenum ) {
        if ( nodenum < 1 || nodenum > 65535 ) {
            throw new IllegalArgumentException("Node number should be between 1 and 65535");
        }
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeNumber() == nodenum ) {
                return (CbusNodeFromBackup) _mainArray.get(i);
            }
        }
        CbusNodeFromBackup cs = new CbusNodeFromBackup(_memo, nodenum);
        // cs.startParamsLookup();
        addNode(cs);
        return cs;        
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusNodeFromFcuTableDataModel.class);
}
