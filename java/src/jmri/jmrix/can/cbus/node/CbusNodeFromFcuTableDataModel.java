package jmri.jmrix.can.cbus.node;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
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
     * Returns String of column name from column int
     * used in table header
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int col) { // not in any order
        switch (col) {
            case FCU_NODE_NUMBER_COLUMN:
                return Bundle.getMessage("NodeNumberCol");
            case FCU_NODE_USER_NAME_COLUMN:
                return Bundle.getMessage("UserName");
            case FCU_NODE_TYPE_NAME_COLUMN:
                return Bundle.getMessage("ColumnType");
            case FCU_NODE_EVENTS_COLUMN:
                return Bundle.getMessage("CbusEvents");
            case NODE_NV_TOTAL_COLUMN:
                return Bundle.getMessage("ColumnNVs");
            case FCU_NODE_TOTAL_BYTES_COLUMN:
                return Bundle.getMessage("TotalBytes");
            default:
                return "unknown " + col; // NOI18N
        }
    } 
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int col) {
        if (_mainArray.isEmpty()) {
            return Object.class;
        }
        return getValueAt(0, col).getClass();
    }
    
    /**
     * Always False as backup Node.
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
    }

    /**
     * Ignored as data from file
     * {@inheritDoc}
     */
    @Override
    public void message(CanMessage m) { // outgoing cbus message
    }
    
    /**
     * Ignored as data from file
     * {@inheritDoc}
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
