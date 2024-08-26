package jmri.jmrix.can.cbus.node;

import java.io.File;
import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.symbolicprog.tabbedframe.PaneServiceProgFrame;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.*;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JmriJOptionPane;

/**
 * Table data model for display of CBUS Nodes
 *
 * @author Steve Young (c) 2019
 * 
 */
public class CbusBasicNodeTable extends javax.swing.table.AbstractTableModel {

    protected ArrayList<CbusNode> _mainArray;
    protected final CanSystemConnectionMemo _memo;
    protected CbusDccProgrammerManager progMan;

    // column order needs to match list in column tooltips
    public static final int NODE_NUMBER_COLUMN = 0; 
    public static final int NODE_TYPE_NAME_COLUMN = 1; 
    public static final int NODE_USER_NAME_COLUMN = 2;
    public static final int NODE_RESYNC_BUTTON_COLUMN = 3;
    public static final int NODE_EDIT_BUTTON_COLUMN = 4;
    public static final int COMMAND_STAT_NUMBER_COLUMN = 5;
    public static final int CANID_COLUMN = 6;
    public static final int NODE_EVENTS_COLUMN = 7;
    public static final int BYTES_REMAINING_COLUMN = 8;
    public static final int NODE_TOTAL_BYTES_COLUMN = 9;
    public static final int NODE_IN_LEARN_MODE_COLUMN = 10;
    public static final int NODE_EVENT_INDEX_VALID_COLUMN = 11;
    public static final int SESSION_BACKUP_STATUS_COLUMN = 12;
    public static final int NUMBER_BACKUPS_COLUMN = 13;
    public static final int LAST_BACKUP_COLUMN = 14;
    public static final int MAX_COLUMN = 15;

    public CbusBasicNodeTable(@Nonnull CanSystemConnectionMemo memo, int row, int column) {
        _mainArray = new ArrayList<>();
        _memo = memo;
        var cfgMan = memo.get(CbusConfigurationManager.class);
        if ( cfgMan != null ) {
            progMan = cfgMan.get(GlobalProgrammerManager.class);
            if ( progMan == null ) {
                log.info("No Global Programmer available for NV programming");
            }
        } else {
            log.info("No CbusConfigurationManager available for NV programming");
        }
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
            case NODE_EDIT_BUTTON_COLUMN:
                return Bundle.getMessage("EditButton");
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
        return (col == NODE_USER_NAME_COLUMN || col == NODE_RESYNC_BUTTON_COLUMN
                || col == NODE_EDIT_BUTTON_COLUMN);
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
            case NODE_EDIT_BUTTON_COLUMN:
                return ("Edit");
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
                ThreadingUtil.runOnGUI( () -> fireTableCellUpdated(row, col) );
                break;
            case NODE_RESYNC_BUTTON_COLUMN:
                _mainArray.get(row).saveForResync();
                _mainArray.get(row).resetNodeAll();
                if (this instanceof CbusNodeTableDataModel) {
                    ((CbusNodeTableDataModel)this).setUrgentNode( _mainArray.get(row).getNodeNumber() );
                    ((CbusNodeTableDataModel)this).startBackgroundFetch();
                }
                break;
            case NODE_EDIT_BUTTON_COLUMN:
                // Try to load a local xml file
                String title = _mainArray.get(row).getName() + " (CBUS)";
                DecoderFile decoderFile = InstanceManager.getDefault(DecoderIndexFile.class).fileFromTitle(title);
                String userName = _mainArray.get(row).getUserName();
                String nodeNumber = "CBUS_Node_" + Integer.toString(_mainArray.get(row).getNodeNumber());
                if (!userName.equals("")) {
                    nodeNumber = nodeNumber.concat("_" + userName);
                }
                if ((decoderFile != null) && (progMan != null)) {
                    log.debug("decoder file: {}", decoderFile.getFileName()); // NOI18N
                    // Look for an existing roster entry
                    RosterEntry re = Roster.getDefault().getEntryForId(nodeNumber);
                    if (re == null) {
                        // Or create one
                        re = new RosterEntry(new RosterEntry(), nodeNumber);
                        re.setDecoderFamily("CBUS");
                        re.setMfg(decoderFile.getMfg());
                        re.setDecoderModel(decoderFile.getModel());
                        re.setRoadNumber(Integer.toString(_mainArray.get(row).getNodeNumber()));
                        re.setRoadName(userName);
                    }
                    String progTitle = "CBUS NV Programmer";
                    String progFile = "programmers" + File.separator + "Cbus" + ".xml";
                    JFrame p = new PaneServiceProgFrame(
                        decoderFile, re, progTitle, progFile, progMan.getGlobalProgrammer());
                    p.pack();
                    p.setVisible(true);
                } else {
                    log.info("No xml, or no programmer found for node {}", title);
                    JmriJOptionPane.showMessageDialog(null,
                        "<html><h3>No programmer or no decoder file</h3><p>Use Node Variables tab</p></html>",
                        "No xml, or no programmer for " + title, JmriJOptionPane.INFORMATION_MESSAGE);
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
        "Edit Node Variables",
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusBasicNodeTable.class);

}
