package jmri.jmrix.can.cbus.node;

import java.util.ArrayList;
import javax.annotation.Nonnull;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of CBUS Nodes
 *
 * @author Steve Young (c) 2019
 * 
 */
public class CbusBasicNodeTable extends javax.swing.table.AbstractTableModel {

    protected ArrayList<CbusNode> _mainArray;
    protected final CanSystemConnectionMemo _memo;
    
    // column order needs to match list in column tooltips
    static public final int NODE_NUMBER_COLUMN = 0; 
    static public final int NODE_TYPE_NAME_COLUMN = 1; 
    static public final int NODE_USER_NAME_COLUMN = 2;
    static public final int NODE_RESYNC_BUTTON_COLUMN = 3;
    static public final int COMMAND_STAT_NUMBER_COLUMN = 4;
    static public final int CANID_COLUMN = 5;
    static public final int NODE_EVENTS_COLUMN = 6;
    static public final int BYTES_REMAINING_COLUMN = 7;
    static public final int NODE_TOTAL_BYTES_COLUMN = 8;
    static public final int NODE_IN_LEARN_MODE_COLUMN = 9;
    static public final int NODE_EVENT_INDEX_VALID_COLUMN = 10;
    static public final int SESSION_BACKUP_STATUS_COLUMN = 11;
    static public final int NUMBER_BACKUPS_COLUMN = 12;
    static public final int LAST_BACKUP_COLUMN = 13;
    static public final int MAX_COLUMN = 14;

    public CbusBasicNodeTable(@Nonnull CanSystemConnectionMemo memo, int row, int column) {
        _mainArray = new ArrayList<>();
        _memo = memo;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowCount() {
        if ( _mainArray == null ) {
            log.error("Node Table Array _mainArray not initialised");
            return 0;
        }
        else {
            return _mainArray.size();
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
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int col) { // not in any order
        switch (col) {
            case CANID_COLUMN:
                return Bundle.getMessage("CanID");
            case NODE_NUMBER_COLUMN:
                return Bundle.getMessage("NodeNumberCol");
            case NODE_USER_NAME_COLUMN:
                return Bundle.getMessage("UserName");
            case NODE_RESYNC_BUTTON_COLUMN:
                return Bundle.getMessage("ReSynchronizeButton");
            case NODE_TYPE_NAME_COLUMN:
                return Bundle.getMessage("ColumnType");
            case COMMAND_STAT_NUMBER_COLUMN:
                return Bundle.getMessage("CommandStationNumber");
            case NODE_EVENTS_COLUMN:
                return Bundle.getMessage("CbusEvents");
            case NODE_TOTAL_BYTES_COLUMN:
                return Bundle.getMessage("TotalBytes");
            case BYTES_REMAINING_COLUMN:
                return Bundle.getMessage("FetchProgress");
            case NODE_IN_LEARN_MODE_COLUMN:
                return Bundle.getMessage("LearnMode");
            case NODE_EVENT_INDEX_VALID_COLUMN:
                return Bundle.getMessage("EventIndexValid");
            case SESSION_BACKUP_STATUS_COLUMN:
                return("Backup Status");
            case NUMBER_BACKUPS_COLUMN:
                return("Num. Backups");
            case LAST_BACKUP_COLUMN:
                return("Last Backup");
            default:
                return "unknown " + col; // NOI18N
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int col) {
        if (_mainArray.isEmpty() || getValueAt(0, col)==null) {
            return Object.class;
        }
        return getValueAt(0, col).getClass();
    }
    
    /**
     * {@inheritDoc}
     * @return UserName and Resync Button columns true, else false
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        return (col == NODE_USER_NAME_COLUMN || col == NODE_RESYNC_BUTTON_COLUMN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case NODE_NUMBER_COLUMN:
                return _mainArray.get(row).getNodeNumber();
            case NODE_USER_NAME_COLUMN:
                return _mainArray.get(row).getUserName();
            case NODE_TYPE_NAME_COLUMN:
                return _mainArray.get(row).getNodeStats().getNodeTypeName();
            case CANID_COLUMN:
                return _mainArray.get(row).getNodeCanId();
            case COMMAND_STAT_NUMBER_COLUMN:
                return _mainArray.get(row).getCsNum();
            case NODE_EVENTS_COLUMN:
                return _mainArray.get(row).getNodeEventManager().getTotalNodeEvents();
            case NODE_TOTAL_BYTES_COLUMN:
                return _mainArray.get(row).getNodeStats().totalNodeBytes();
            case BYTES_REMAINING_COLUMN:
                return _mainArray.get(row).getNodeStats().floatPercentageRemaining();
            case NODE_IN_LEARN_MODE_COLUMN:
                return _mainArray.get(row).getNodeInLearnMode();
            case NODE_RESYNC_BUTTON_COLUMN:
                return ("Re-Sync");
            case NODE_EVENT_INDEX_VALID_COLUMN:
                return _mainArray.get(row).getNodeEventManager().isEventIndexValid();
            case SESSION_BACKUP_STATUS_COLUMN:
                return _mainArray.get(row).getNodeBackupManager().getSessionBackupStatus();
            case NUMBER_BACKUPS_COLUMN:
                return _mainArray.get(row).getNodeBackupManager().getNumCompleteBackups();
            case LAST_BACKUP_COLUMN:
                return _mainArray.get(row).getNodeBackupManager().getLastBackupTime();
            default:
                return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case NODE_USER_NAME_COLUMN:
                _mainArray.get(row).setUserName( (String) value );
                ThreadingUtil.runOnGUI( ()->{
                    fireTableCellUpdated(row, col);
                }); break;
            case NODE_RESYNC_BUTTON_COLUMN:
                _mainArray.get(row).saveForResync();
                _mainArray.get(row).resetNodeAll();
                if (this instanceof CbusNodeTableDataModel) {
                    ((CbusNodeTableDataModel)this).setUrgentNode( _mainArray.get(row).getNodeNumber() );
                    ((CbusNodeTableDataModel)this).startBackgroundFetch();
                }
                break;
            default:
                log.debug("invalid column");
                break;
        }
    }
    
    /**
     * String array of Column Tool tips.
     * Order needs to match column list.
     */
    public static final String[] COLUMNTOOLTIPS = {
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "Index invalid when an event addition or deletion has taken place since last fetch.",
        "Session Backup Status",
        "Total backups in xml file",
        null

    }; // Length = number of items in array should (at least) match number of columns
    
    private final static Logger log = LoggerFactory.getLogger(CbusBasicNodeTable.class);
}
