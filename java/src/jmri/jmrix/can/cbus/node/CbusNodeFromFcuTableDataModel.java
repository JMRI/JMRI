package jmri.jmrix.can.cbus.node;

import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.ThreadingUtil;

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

    private final ArrayList<CbusNodeFromBackup> __mainArray;
    private final CanSystemConnectionMemo _memo;
    
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
        
        __mainArray = new ArrayList<>();
        _memo = memo;
        
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
        return __mainArray.size();
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
                return __mainArray.get(row).getNodeNumber();
            case FCU_NODE_USER_NAME_COLUMN:
                return __mainArray.get(row).getUserName();
            case FCU_NODE_TYPE_NAME_COLUMN:
                return __mainArray.get(row).getNodeTypeName();
            case FCU_NODE_EVENTS_COLUMN:
                return __mainArray.get(row).getTotalNodeEvents();
            case NODE_NV_TOTAL_COLUMN:
                return __mainArray.get(row).getTotalNVs();
            case FCU_NODE_TOTAL_BYTES_COLUMN:
                return __mainArray.get(row).totalNodeBytes();
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
     * returns the row number from a node number
     * @param nodenum Node Number to search for
     * @return Row number, else -1 if not on table
     */
    @Override
    public int getNodeRowFromNodeNum( int nodenum ) {
        for (int i = 0; i < getRowCount(); i++) {
            if ( __mainArray.get(i).getNodeNumber() == nodenum ) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Ignored as data from file
     * @param m canframe
     */
    @Override
    public void reply(CanReply m) { // incoming cbus message
    }
    
    /**
     * Register new CbusNodeFromBackup node to table
     * @param node the node to add
     */
    public void addNode(CbusNodeFromBackup node ) {
        __mainArray.add(node);
        node.setTableModel(this);
        // notify the JTable object that a row has changed; do that in the Swing thread!
        ThreadingUtil.runOnGUI( ()->{
            fireTableRowsInserted((getRowCount()-1), (getRowCount()-1));
        });
    }
    
    // returns a new or existing node by node number
    // node number must be > 0
    @Override
    public CbusNodeFromBackup provideNodeByNodeNum( int nodenum ) {
        if ( nodenum < 1 ) {
            return null;
        }
        for (int i = 0; i < getRowCount(); i++) {
            if ( __mainArray.get(i).getNodeNumber() == nodenum ) {
                return __mainArray.get(i);
            }
        }
        CbusNodeFromBackup cs = new CbusNodeFromBackup(_memo, nodenum);
        // cs.startParamsLookup();
        addNode(cs);
        return cs;        
    }

    // returns an existing node by node number
    // null if not found
    @Override
    public CbusNodeFromBackup getNodeByNodeNum( int nodenum ) {
        for (int i = 0; i < getRowCount(); i++) {
            if ( __mainArray.get(i).getNodeNumber() == nodenum ) {
                return __mainArray.get(i);
            }
        }
        return null;        
    }
    
    /**
     * Returns an existing node by table row number
     * @param rowNum The Row Number
     * @return the Node
     */
    @Override
    public CbusNode getNodeByRowNum( int rowNum ) {
        return __mainArray.get(rowNum);
    }
    
    /**
     * Notify GUI that a table cell has been updated
     * @param node Node Number, not node row number
     * @param col Table Column number
     */
    @Override
    public void updateFromNode( int node, int col){
        // log.debug("table update from node {} column {}",node,col);
        if ( col < getColumnCount() ) {
        
            ThreadingUtil.runOnGUI( ()->{
                fireTableCellUpdated(getNodeRowFromNodeNum(node), col);
            });
        }
    }
    
    /**
     * Single Node User Name
     * @param nn Node Number, NOT row number
     * @return Node Username, if unset returns node type name
     */
    @Override
    public String getNodeName( int nn ) {
        int rownum = getNodeRowFromNodeNum(nn);
        if ( rownum < 0 ) {
            return "";
        }
        if ( !__mainArray.get(rownum).getUserName().isEmpty() ) {
            return __mainArray.get(rownum).getUserName();
        }
        if ( !__mainArray.get(rownum).getNodeTypeName().isEmpty() ) {
            return __mainArray.get(rownum).getNodeTypeName();
        }        
        return "";
    }
    
    /**
     * No need to disconnect from the tc, we should not be connected in this type of node
     */
    @Override
    public void dispose() {
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeFromFcuTableDataModel.class);
}
