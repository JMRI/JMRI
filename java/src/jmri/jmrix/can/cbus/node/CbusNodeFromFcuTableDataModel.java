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

    private ArrayList<CbusNodeFromBackup> _mainArray;
    private CanSystemConnectionMemo _memo;
    
    // column order needs to match list in column tooltips
    static public final int NODE_NUMBER_COLUMN = 0; 
    static public final int NODE_TYPE_NAME_COLUMN = 1; 
    static public final int NODE_USER_NAME_COLUMN = 2;
    static public final int NODE_EVENTS_COLUMN = 3;
    static public final int NODE_NV_TOTAL_COLUMN = 4;
    static public final int NODE_TOTAL_BYTES_COLUMN = 5;
    static public final int MAX_COLUMN = 6;

    public CbusNodeFromFcuTableDataModel(CanSystemConnectionMemo memo, int row, int column) {
        super (memo, row, column);
        
        _mainArray = new ArrayList<CbusNodeFromBackup>();
        _memo = memo;
        
    }
    
    // order needs to match column list top of dtabledatamodel
    public static final String[] columnToolTips = {
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
     * Return the number of rows to be displayed.
     */
    @Override
    public int getRowCount() {
        return _mainArray.size();
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
     * @param col int col number
     */
    @Override
    public String getColumnName(int col) { // not in any order
        switch (col) {
            case NODE_NUMBER_COLUMN:
                return ("Node Num");
            case NODE_USER_NAME_COLUMN:
                return ("User Name");
            case NODE_TYPE_NAME_COLUMN:
                return ("Node Type");
            case NODE_EVENTS_COLUMN:
                return ("Events");
            case NODE_NV_TOTAL_COLUMN:
                return("Total NV's");
            case NODE_TOTAL_BYTES_COLUMN:
                return("Tot. Bytes");
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
            case NODE_EVENTS_COLUMN:
            case NODE_NUMBER_COLUMN:
            case NODE_NV_TOTAL_COLUMN:
            case NODE_TOTAL_BYTES_COLUMN:
                return new JTextField(6).getPreferredSize().width;
            case NODE_TYPE_NAME_COLUMN:
            case NODE_USER_NAME_COLUMN:
                return new JTextField(13).getPreferredSize().width;
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
            case NODE_NUMBER_COLUMN:
            case NODE_EVENTS_COLUMN:
            case NODE_NV_TOTAL_COLUMN:
            case NODE_TOTAL_BYTES_COLUMN:
                return Integer.class;
            case NODE_USER_NAME_COLUMN:
            case NODE_TYPE_NAME_COLUMN:
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
     * Return table values
     * @param row int row number
     * @param col int col number
     */
    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case NODE_NUMBER_COLUMN:
                return _mainArray.get(row).getNodeNumber();
            case NODE_USER_NAME_COLUMN:
                return _mainArray.get(row).getUserName();
            case NODE_TYPE_NAME_COLUMN:
                return _mainArray.get(row).getNodeTypeName();
            case NODE_EVENTS_COLUMN:
                return _mainArray.get(row).getTotalNodeEvents();
            case NODE_NV_TOTAL_COLUMN:
                return _mainArray.get(row).getTotalNVs();
            case NODE_TOTAL_BYTES_COLUMN:
                return _mainArray.get(row).totalNodeBytes();
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
            if ( _mainArray.get(i).getNodeNumber() == nodenum ) {
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
     */
    public void addNode(CbusNodeFromBackup node ) {
        _mainArray.add(node);
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
            if ( _mainArray.get(i).getNodeNumber() == nodenum ) {
                return _mainArray.get(i);
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
            if ( _mainArray.get(i).getNodeNumber() == nodenum ) {
                return _mainArray.get(i);
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
        return _mainArray.get(rowNum);
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
        if ( !_mainArray.get(rownum).getUserName().isEmpty() ) {
            return _mainArray.get(rownum).getUserName();
        }
        if ( !_mainArray.get(rownum).getNodeTypeName().isEmpty() ) {
            return _mainArray.get(rownum).getNodeTypeName();
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
