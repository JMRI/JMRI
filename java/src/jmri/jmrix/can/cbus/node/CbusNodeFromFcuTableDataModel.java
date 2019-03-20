package jmri.jmrix.can.cbus.node;

import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.CbusSend;
import jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolPane;
import jmri.util.ThreadingUtil;
import java.util.TimerTask;
import jmri.util.TimerUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Cbus Nodes imported from MERG FCU
 *
 * @author Steve Young (c) 2019
 * @see CbusNodeFromFcu
 * 
 */
public class CbusNodeFromFcuTableDataModel extends CbusNodeTableDataModel {

    private ArrayList<CbusNodeFromFcu> _mainArray;
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
        
        log.debug("Starting MERG CBUS Node Table");
        _mainArray = new ArrayList<CbusNodeFromFcu>();
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
     * </p>
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
            case NODE_NUMBER_COLUMN:
            case NODE_EVENTS_COLUMN:
            case NODE_NV_TOTAL_COLUMN:
            case NODE_TOTAL_BYTES_COLUMN:
                return Integer.class;
            case NODE_USER_NAME_COLUMN:
            case NODE_TYPE_NAME_COLUMN:
                return String.class;
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
        return false;
    }

     /**
     * Return table values
     * @param row int row number
     * @param col int col number
     */
    @Override
    public Object getValueAt(int row, int col) {
        // log.info("requesting row {} col {}",row,col);
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
                log.error("internal state inconsistent with table request for row {} col {}", row, col);
                return null;
        }
    }
    
    /**
     * Capture new comments or node names.
     * Button events
     * @param value object value
     * @param row int row number
     * @param col int col number
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
    }

    /**
     * Unused, even simulated nodes / command stations normally respond with CanReply
     * @param m canmessage
     */
    @Override
    public void message(CanMessage m) { // outgoing cbus message
    //    int opc = CbusMessage.getOpcode(m);
    }
    
    
     // returns the row number from a node number
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
     * Capture node and event, check isevent and send to parse from reply.
     * @param m canmessage
     */
    @Override
    public void reply(CanReply m) { // incoming cbus message
    }
    
    /**
     * Register new CbusNodeFromFcu node to table
     */
    public void addNode(CbusNodeFromFcu node ) {
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
    public CbusNodeFromFcu provideNodeByNodeNum( int nodenum ) {
        if ( nodenum < 1 ) {
            return null;
        }
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeNumber() == nodenum ) {
                return _mainArray.get(i);
            }
        }
        CbusNodeFromFcu cs = new CbusNodeFromFcu(_memo, nodenum);
        cs.startParamsLookup();
        addNode(cs);
        return cs;        
    }

    // returns an existing node by node number
    // null if not found
    @Override
    public CbusNodeFromFcu getNodeByNodeNum( int nodenum ) {
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeNumber() == nodenum ) {
                return _mainArray.get(i);
            }
        }
        return null;        
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
     * No need to disconnect from the tc, we should be connected in this type of node
     */
    @Override
    public void dispose() {
    }

    private final static Logger log = LoggerFactory.getLogger(CbusNodeFromFcuTableDataModel.class);
}
