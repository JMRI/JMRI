package jmri.jmrix.can.cbus.node;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.jmrix.can.*;
import jmri.jmrix.can.cbus.*;
import jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolPane;
import jmri.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Cbus Nodes
 *
 * @author Steve Young (c) 2019
 * 
 */
public class CbusNodeTableDataModel extends javax.swing.table.AbstractTableModel
        implements CanListener, PropertyChangeListener {

    protected ArrayList<CbusNode> _mainArray;
    protected final CanSystemConnectionMemo _memo;
    private final CbusSend send;
    private CbusPreferences preferences;
    private ArrayList<Integer> _nodesFound;
    private CbusAllocateNodeNumber allocate;
    private CbusNodeTrickleFetch trickleFetch;
    
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

    public CbusNodeTableDataModel(@Nonnull CanSystemConnectionMemo memo, int row, int column) {
        
        log.debug("Starting MERG CBUS Node Table");
        _mainArray = new ArrayList<>();
        _nodesFound = new ArrayList<>();
        _memo = memo;
        // connect to the CanInterface
        addTc(memo);
        
        send = new CbusSend(memo);

    }
    
    public void startup(){
        
        preferences = jmri.InstanceManager.getDefault(CbusPreferences.class);
        
        setBackgroundAllocateListener( preferences.getAllocateNNListener() );
        if ( preferences.getStartupSearchForCs() ) {
            send.searchForCommandStations();
        }
        if ( preferences.getStartupSearchForNodes() ) {
            send.searchForNodes();
            setSearchForNodesTimeout( 5000 );
        } else
        if ( preferences.getSearchForNodesBackupXmlOnStartup() ) {
            // it's preferable to do this AFTER the network search timeout, 
            // however we also test here in case there is no timeout
            startupSearchNodeXmlFile();
        }
    }
    
    // start listener for nodes requesting a new node number
    public void setBackgroundAllocateListener( boolean newState ){
        if (newState  && !java.awt.GraphicsEnvironment.isHeadless() ) {
            if (allocate == null) {
                allocate = new CbusAllocateNodeNumber( _memo, this );
            } else {
            }
        } else {
            if ( allocate != null ) {
                allocate.dispose();
            }
            allocate = null;
        }
    }
    
    
    // order needs to match column list top of dtabledatamodel
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
    
    /**
     * Return the number of rows to be displayed.
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

    @Override
    public int getColumnCount() {
        return MAX_COLUMN;
    }

    /**
     * Configure a table to have standard rows and columns.
     * <p>
     * 
     * @param table the JTable to have common defaults
     */
    public void configureTable(JTable table) {
        // allow reordering of the columns
        table.getTableHeader().setReorderingAllowed(true);

        // shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // resize columns as requested
        for (int i = 0; i < table.getColumnCount(); i++) {
            int width = getPreferredWidth(i);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        table.sizeColumnsToFit(-1);
    }

    /**
     * Returns String of column name from column int
     * used in table header
     * @param col int col number
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
    * Returns int of startup column widths
    * @param col int col number
    * @return default column width
    */
    public static int getPreferredWidth(int col) {
        switch (col) {
            case CANID_COLUMN:
            case NODE_EVENTS_COLUMN:
            case COMMAND_STAT_NUMBER_COLUMN:
            case NODE_IN_LEARN_MODE_COLUMN:
            case NODE_EVENT_INDEX_VALID_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            case NODE_TOTAL_BYTES_COLUMN:
                return new JTextField(5).getPreferredSize().width;
            case NODE_NUMBER_COLUMN:
                return new JTextField(6).getPreferredSize().width;
            case NODE_RESYNC_BUTTON_COLUMN:
                return new JTextField(8).getPreferredSize().width;
            case NODE_TYPE_NAME_COLUMN:
                return new JTextField(10).getPreferredSize().width;
            case NODE_USER_NAME_COLUMN:
            case BYTES_REMAINING_COLUMN:
                return new JTextField(13).getPreferredSize().width;
            default:
                return new JTextField(" <unknown> ").getPreferredSize().width; // NOI18N
        }
    }
    
    /**
    * Returns column class type.
     * @param col Node Table Column number
    */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case CANID_COLUMN:
            case NODE_NUMBER_COLUMN:
            case COMMAND_STAT_NUMBER_COLUMN:
            case NODE_EVENTS_COLUMN:
            case NODE_TOTAL_BYTES_COLUMN:
            case BYTES_REMAINING_COLUMN:
                return Integer.class;
            case NODE_USER_NAME_COLUMN:
            case NODE_TYPE_NAME_COLUMN:
                return String.class;
            case NODE_IN_LEARN_MODE_COLUMN:
            case NODE_EVENT_INDEX_VALID_COLUMN:
                return Boolean.class;
            case NODE_RESYNC_BUTTON_COLUMN:
                return javax.swing.JButton.class;
            case SESSION_BACKUP_STATUS_COLUMN:
                return CbusNodeConstants.BackupType.class;
            case NUMBER_BACKUPS_COLUMN:
                return Integer.class;
            case LAST_BACKUP_COLUMN:
                return java.util.Date.class;
            default:
                return null;
        }
    }
    
    /**
    * Boolean return to edit table cell or not
     * @param row Table Row number
     * @param col Table Column number
    * @return UserName and Resync Button columns true, else false
    */
    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case NODE_USER_NAME_COLUMN:
            case NODE_RESYNC_BUTTON_COLUMN:
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
        // log.info("requesting row {} col {}",row,col);
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
     * Edit node Username,
     * Button events
     * @param value object value
     * @param row int row number
     * @param col int col number
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
                _mainArray.get(row).resetNodeAll();
                setUrgentNode( _mainArray.get(row).getNodeNumber() );
                startBackgroundFetch();
                break;
            default:
                log.debug("invalid column");
                break;
        }
    }

    /**
     * Unused, even simulated nodes / command stations normally respond with CanReply
     * @param m canmessage
     */
    @Override
    public void message(CanMessage m) { // outgoing cbus message
    }
    
    private int csFound=0;
    private int ndFound = 0;
    
    /**
     * Listen on the network for incoming STAT and PNN OPC's
     * @param m incoming CanReply
     */
    @Override
    public void reply(CanReply m) { // incoming cbus message
        if ( m.extendedOrRtr() ) {
            return;
        }
        int opc = CbusMessage.getOpcode(m);
        int nodenum = ( m.getElement(1) * 256 ) + m.getElement(2);
        if (opc==CbusConstants.CBUS_STAT) {
            // log.debug("Command Station Updates Status {}",m);
            
            if ( preferences.getAddCommandStations() ) {
                
                int csnum = m.getElement(3);
                // provides a command station by cs number, NOT node number
                CbusNode cs = provideCsByNum(csnum,nodenum);
                cs.setFW(m.getElement(5),m.getElement(6),m.getElement(7));
                cs.setCsFlags(m.getElement(4));
                cs.setCanId(CbusMessage.getId(m));
                
            }
            _nodesFound.add(nodenum);
            csFound++;
        }
        else if (opc==CbusConstants.CBUS_PNN && searchForNodesTask != null) {
            log.debug("Node Report message {}",m);
            if ( preferences.getAddNodes() ) {
                // provides a node by node number
                CbusNode nd = provideNodeByNodeNum(nodenum);
                nd.setManuModule(m.getElement(3),m.getElement(4));
                nd.setNodeFlags(m.getElement(5));
                nd.setCanId(CbusMessage.getId(m));
            }
            _nodesFound.add(nodenum);
            ndFound++;
        }
        else if (opc==CbusConstants.CBUS_NNREL) { // from node advising releasing node number
            if ( getNodeRowFromNodeNum(nodenum) >-1 ) {
                log.info( Bundle.getMessage("NdRelease", getNodeName(nodenum), nodenum ) );
                removeRow( getNodeRowFromNodeNum(nodenum),false );
            }
        }
    }
    
    /**
     * Register new node to table
     * @param node The CbusNode to add to the table
     */
    public void addNode(CbusNode node ) {
        _mainArray.add(node);
        node.setTableModel(this);
        node.addPropertyChangeListener(this);
        // notify the JTable object that a row has changed; do that in the Swing thread!
        ThreadingUtil.runOnGUI( ()->{
            fireTableRowsInserted((getRowCount()-1), (getRowCount()-1));
        });
        startBackgroundFetch();
    }
    
    /**
     * Returns an existing command station by cs number, NOT node number
     * @param csnum The Command Station Number ( the default in CBUS is 0 )
     * @return the Node which has the command station number, else null
     */
    @CheckForNull
    public CbusNode getCsByNum( int csnum) {
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getCsNum() == csnum ) {
                return _mainArray.get(i);
            }
        }
        return null;
    }

    /**
     * Returns a new or existing command station by cs number, NOT node number
     * 
     * @param csnum The Command Station Number to provide by
     * @param nodenum if existing CS sets node num to this, else node with this number and starts param lookup
     * 
     * @return the Node which has the command station number
     */
    private CbusNode provideCsByNum( int csnum, int nodenum) {
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getCsNum() == csnum ) {
                _mainArray.get(i).setNodeNumber(nodenum);
                return _mainArray.get(i);
            }
        }
        CbusNode cs = new CbusNode(_memo, nodenum);
        cs.setCsNum(csnum);
        addNode(cs);
        return cs;
    }
    
    /**
     * Returns a new or existing node by node number
     * 
     * @param nodenum number to search nodes by, else creates node with this number and starts param lookup
     * 
     * @return the Node which has the node number
     */    
    public CbusNode provideNodeByNodeNum( int nodenum ) {
        if ( nodenum < 1 || nodenum > 65535 ) {
            log.error("Node number should be between 1 and 65535");
            return null;
        }
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeNumber() == nodenum ) {
                return _mainArray.get(i);
            }
        }
        CbusNode cs = new CbusNode(_memo, nodenum);
        addNode(cs);
        return cs;        
    }

    /**
     * Returns an existing node by node number
     * @param nodenum The Node Number ( min 1, max 65535 )
     * @return the Node which has the node number, else null
     */
    @CheckForNull
    public CbusNode getNodeByNodeNum( int nodenum ) {
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
    public CbusNode getNodeByRowNum( int rowNum ) {
        return _mainArray.get(rowNum);
    }
    
    /**
     * Returns the table row number by node number
     * @param nodenum The Node Number ( min 1, max 65535 )
     * @return the Model Row which has the node number, else -1
     */    
    public int getNodeRowFromNodeNum( int nodenum ) {
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeNumber() == nodenum ) {
                return i;
            }
        }
        return -1;
    }

    /**
     * For a given CAN ID, if in use, return formatted Node Name and number
     * else returns zero length string
     * @param canId the CAN ID to search the table for
     * @return Node Number and name
     */
    public String getNodeNameFromCanId ( int canId ) {
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeCanId() == canId ) {
                return _mainArray.get(i).getNodeStats().getNodeNumberName();
            }
        }
        return ("");
    }

    /**
     * Returns formatted Node Number and User Name by node number
     * @param nodenum The Node Number ( min 1, max 65535 )
     * @return Node Number + either node model or Username.
     */    
    public String getNodeNumberName( int nodenum ) {
        
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeNumber() == nodenum ) {
                return _mainArray.get(i).getNodeStats().getNodeNumberName();
            }
        }
        return ("");
    }

    /**
     * Returns a string ArrayList of all Node Number and User Names on the table
     * @return Node Number + either node model or Username.
     */       
    public ArrayList<String> getListOfNodeNumberNames(){
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < getRowCount(); i++) {
            list.add( _mainArray.get(i).getNodeStats().getNodeNumberName() );
        }
        return list;
    }
    
    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent ev){
        if (!(ev.getSource() instanceof CbusNode)) {
            return;
        }
        
        int evRow = getNodeRowFromNodeNum((( CbusNode ) ev.getSource()).getNodeNumber());

        switch (ev.getPropertyName()) {
            case "NVUPDATE":
            case "ALLNVUPDATE":
                fireTableCellUpdated(evRow, BYTES_REMAINING_COLUMN);
                fireTableCellUpdated(evRow, NODE_TOTAL_BYTES_COLUMN);
                break;
            case "ALLEVUPDATE":
            case "SINGLEEVUPDATE":
                fireTableCellUpdated(evRow, NODE_EVENT_INDEX_VALID_COLUMN);
                fireTableCellUpdated(evRow, NODE_EVENTS_COLUMN);
                fireTableCellUpdated(evRow, BYTES_REMAINING_COLUMN);
                fireTableCellUpdated(evRow, NODE_TOTAL_BYTES_COLUMN);
                break;
            case "BACKUPS":
                fireTableCellUpdated(evRow, SESSION_BACKUP_STATUS_COLUMN);
                fireTableCellUpdated(evRow, NUMBER_BACKUPS_COLUMN);
                fireTableCellUpdated(evRow, LAST_BACKUP_COLUMN);
                break;
            case "PARAMETER":
                fireTableRowsUpdated(evRow,evRow);
                break;
            case "LEARNMODE":
                fireTableCellUpdated(evRow,NODE_IN_LEARN_MODE_COLUMN);
                break;
            case "NAMECHANGE":
                fireTableCellUpdated(evRow,NODE_USER_NAME_COLUMN);
                break;
            case "CANID":
                fireTableCellUpdated(evRow,CANID_COLUMN);
                break;
            default:
                break;
        }
    }
    
    /**
     * Single Node User Name
     * @param nn Node Number, NOT row number
     * @return Node Username, if unset returns node type name, else empty String
     */
    public String getNodeName( int nn ) {
        int rownum = getNodeRowFromNodeNum(nn);
        if ( rownum < 0 ) {
            return "";
        }
        if ( !_mainArray.get(rownum).getUserName().isEmpty() ) {
            return _mainArray.get(rownum).getUserName();
        }
        if ( !_mainArray.get(rownum).getNodeStats().getNodeTypeName().isEmpty() ) {
            return _mainArray.get(rownum).getNodeStats().getNodeTypeName();
        }        
        return "";
    }

    /**
     * Returns the next available Node Number
     * @param higherthan Node Number
     * @return calculated next available number, else original value
     */
    public int getNextAvailableNodeNumber( int higherthan ) {
        if ( getRowCount() > 0 ) {
            for (int i = 0; i < getRowCount(); i++) {
                // log.debug("get next available i {} rowcount {}",i,getRowCount() );
                if ( _mainArray.get(i).getNodeNumber() < 65534 ) {
                    if ( _mainArray.get(i).getNodeNumber() >= higherthan ) {
                        higherthan = _mainArray.get(i).getNodeNumber() + 1;
                    }
                }
            }
        }
        return higherthan;
    }
    
    int urgentTab = 0;
    int urgentNode = -1;
    int nodebefore = -1;
    int nodeafter = -1;
    boolean urgentActive=false; // feature active, set false for background fetch
    
    /**
     * Notify the table that the Node data fetch is more urgent
     */
    public void startUrgentFetch() {
        urgentActive = true;
        startBackgroundFetch();
    }
    
    /**
     * Notify the table that the Node data fetch is more urgent
     * @param nodeNum the Node to prioritise in the fetch
     */
    private void setUrgentNode( int nodeNum ){
        urgentNode = nodeNum;
        urgentActive = true;
        startBackgroundFetch();
    }
    
    /**
     * Fetch data in order of priority based on what user is currently viewing
     * @param tabindex of the tab being displayed in the main NodeConfigToolPane
     * @param nodenum number of Node to prioritise in the fetch
     * @param urgentNodeBefore number of the Node in main table row above
     * @param urgentNodeAfter number of the Node in main table row below
     */
    public void setUrgentFetch(int tabindex, int nodenum, int urgentNodeBefore, int urgentNodeAfter){
        urgentTab = tabindex;
        urgentNode = nodenum;
        nodebefore = urgentNodeBefore;
        nodeafter = urgentNodeAfter;
        urgentActive = true;
        startBackgroundFetch();
    }
    
    /**
     * Request the table send the next urgent fetch
     */
    public void triggerUrgentFetch(){
        if (!urgentActive) {
        }
        else {
            sendNextBackgroundFetch();
        }
    }
    
    /**
     * Starts background fetching for all table data as per user prefs
     * Call whenever a node has been added to table or node edited
     */ 
    public void startBackgroundFetch(){

        // reset if already running
        if ( trickleFetch != null ){
                trickleFetch.dispose();
        }
        trickleFetch = null;
        if ( ( preferences != null ) && (preferences.getNodeBackgroundFetchDelay() > 0 ) ) {
            trickleFetch = new CbusNodeTrickleFetch( _memo, this, preferences.getNodeBackgroundFetchDelay() );
        }
    }

    /**
     * Send the next parameter request, ev var request or nv request.
     * Triggered from either background or active fetch.
     * Triggers loading the node backup xml file
     * Triggers the check for node data fetch complete
     *
     * The order of the fetch changes depending on
     * If node is a Command station
     * If a node is currently selected in a node table pane
     * The node above or below the currently selected row
     * If event or nv tab is displayed in a node table pane
     *
     * Default order is Params 0,1,3,6,5,7,2, event total, 
     * remaining parameters, NVs, event index, event vars.
     */ 
    protected void sendNextBackgroundFetch(){
        
        if ( getAnyNodeInLearnMode()>0 ){
            return;
        }
        for (int i = 0; i < getRowCount(); i++) {
            // _mainArray.get(i).startLoadFromXml();
            _mainArray.get(i).getNodeBackupManager().doLoad();
            if ( _mainArray.get(i).getNodeTimerManager().hasActiveTimers() ){
                return;
            }
            _mainArray.get(i).getNodeStats().checkNodeFinishedLoad();
        }
        
        // prioritise command station node variables 1-16
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getCsNum() > -1 ) { // is a command station
            
                if ( _mainArray.get(i).getNodeParamManager().getOutstandingParams() > 0 ) {
                    _mainArray.get(i).getNodeParamManager().sendRequestNextParam();
                    return;
                }
            
                if ( _mainArray.get(i).getNodeParamManager().getParameter(6) > 15 ) { // If CS does has have more than 15 NV's
                    if ( _mainArray.get(i).getNodeNvManager().getNV(16) < 0 ) {
                        _mainArray.get(i).getNodeNvManager().sendNextNVToFetch();
                        return;
                    }
                }
            }
        }
        
        // Get all node parameters fetched so basic node details ( type, num nv's etc. ) are known
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeParamManager().getOutstandingParams() > 0 ) { // this param
                _mainArray.get(i).getNodeParamManager().sendRequestNextParam();
                return;
            }
        }
        
        // If a node is selected in the node manager the details for this are fetched next
        
        CbusNode _urgentNode = getNodeByNodeNum(urgentNode);
        
        if ( _urgentNode != null ) {
            
            if (urgentTab==2 ) { // NV's selected
                if ( _urgentNode.getNodeNvManager().getOutstandingNvCount() > 0 ){ // this nv
                    _urgentNode.getNodeNvManager().sendNextNVToFetch();
                    return;
                }
                if ( _urgentNode.getNodeEventManager().getOutstandingEvVars() > 0 ) { // this events
                    _urgentNode.getNodeEventManager().sendNextEvVarToFetch();
                    return;
                }
            } else {
                if ( _urgentNode.getNodeEventManager().getOutstandingEvVars() > 0 ) { // this events
                    _urgentNode.getNodeEventManager().sendNextEvVarToFetch();
                    return;
                }
                if ( _urgentNode.getNodeNvManager().getOutstandingNvCount() > 0 ){ // this nv
                    _urgentNode.getNodeNvManager().sendNextNVToFetch();
                    return;
                }
            }
            
            CbusNode _beforeNode = getNodeByNodeNum(nodebefore);
            CbusNode _afterNode = getNodeByNodeNum(nodeafter);
            
            if (urgentTab==2) { // NV's selected
                if ( _afterNode != null ) {
                    if ( _afterNode.getNodeNvManager().getOutstandingNvCount() > 0 ){ // below nv
                        _afterNode.getNodeNvManager().sendNextNVToFetch();
                        return;
                    }
                }
                if ( _beforeNode != null ) {
                    if ( _beforeNode.getNodeNvManager().getOutstandingNvCount() > 0 ){ // above nv
                        _beforeNode.getNodeNvManager().sendNextNVToFetch();
                        return;
                    }
                }
            }
            else { // events selected
                if ( _afterNode != null ) {
                    if ( _afterNode.getNodeEventManager().getOutstandingEvVars() > 0 ) { // below events
                        _afterNode.getNodeEventManager().sendNextEvVarToFetch();
                        return;
                    } 
                }
                if ( _beforeNode != null ) {
                    if ( _beforeNode.getNodeEventManager().getOutstandingEvVars() > 0 ) { // above events
                        _beforeNode.getNodeEventManager().sendNextEvVarToFetch();
                        return;
                    } 
                }
            }
        }
        
        // the node selected in table has been synched, 
        // along with the row above and below in case user scolls
        urgentActive=false;
        
        // default lookup routine
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeNvManager().getOutstandingNvCount() > 0 ){ // this nv
                _mainArray.get(i).getNodeNvManager().sendNextNVToFetch();
                return;
            }
            if ( _mainArray.get(i).getNodeEventManager().getOutstandingEvVars() > 0 ) { // above events
                _mainArray.get(i).getNodeEventManager().sendNextEvVarToFetch();
                return;
            }
        }
        
        // if all done dispose trickle fetch
        if ( trickleFetch != null ) {
            trickleFetch.dispose();
        }
        trickleFetch = null;
    }
    
    /**
     * Sends a system-wide reset OPC
     */ 
    public void sendSystemReset(){
        send.aRST();
    }
    
    private NodeConfigToolPane searchFeedbackPanel;
    
    /**
     * Sends a search for Nodes with timeout
     * @param panel Feedback pane, can be null
     * @param timeout in ms
     */ 
    public void startASearchForNodes( NodeConfigToolPane panel, int timeout ){
        searchFeedbackPanel = panel;
        csFound=0;
        ndFound=0;
        setSearchForNodesTimeout( timeout );
        send.searchForCommandStations();
        send.searchForNodes();
    }

    private TimerTask searchForNodesTask;
    
    /**
     * Loop through main table, add a not found note to any nodes
     * which are on the table but not on this list.
     */
    private void checkOnlineNodesVsTable(){
        log.debug("{} Nodes found, {}",_nodesFound.size(),_nodesFound);
        for (int i = 0; i < getRowCount(); i++) {
            if ( ! _nodesFound.contains(_mainArray.get(i).getNodeNumber() )) {
                log.debug("No network response from Node {}",_mainArray.get(i));
                _mainArray.get(i).nodeOnNetwork(false);
            }
        }
        // if node heard but flagged as off-network, reset
        _nodesFound.stream().map((foundNodeNum) -> getNodeByNodeNum(foundNodeNum)).filter((foundNode) 
                -> ( foundNode != null && foundNode.getNodeBackupManager().getSessionBackupStatus() == CbusNodeConstants.BackupType.NOTONNETWORK )).map((foundNode) -> {
            foundNode.resetNodeAll();
            return foundNode;
        }).forEachOrdered((_item) -> {
            startBackgroundFetch();
        });
    }
    
    /**
     * Clears Node Search Timer
     */
    private void clearSearchForNodesTimeout(){
        if (searchForNodesTask != null ) {
            searchForNodesTask.cancel();
            searchForNodesTask = null;
        }
    }
    
    /**
     * Starts Search for Nodes Timer
     * @param timeout value in msec to wait for responses
     */
    private void setSearchForNodesTimeout( int timeout) {
        _nodesFound = new ArrayList<>();
        searchForNodesTask = new TimerTask() {
            @Override
            public void run() {
                // searchForNodesTask = null;
                // log.info("Node search complete " );
                if ( searchFeedbackPanel !=null ) {
                    searchFeedbackPanel.notifyNodeSearchComplete(csFound,ndFound);
                }
                
                // it's preferable to perform this check here, AFTER the network search timeout
                // as JMRI may be starting up and this is not time sensitive.
                if ( preferences.getSearchForNodesBackupXmlOnStartup() ) {
                    startupSearchNodeXmlFile();
                }
                
                checkOnlineNodesVsTable();
                clearSearchForNodesTimeout();
            }
        };
        TimerUtil.schedule(searchForNodesTask, timeout);
    }
    
    /**
     * Returns Node number of any node currently in Learn Mode
     * @return Node Num, else -1 if no nodes known to be in learn mode
     */ 
    public int getAnyNodeInLearnMode(){
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeInLearnMode() ) {
                return _mainArray.get(i).getNodeNumber();
            }
        }
        return -1;
    }
    
    /**
     * Remove Row from table and dispose of it
     * @param row int row number
     * @param removeXml true to also remove the Node xml file
     */
    public void removeRow(int row, boolean removeXml) {
        CbusNode toRemove = getNodeByNodeNum( _mainArray.get(row).getNodeNumber() );
        _mainArray.remove(row);
        if (toRemove != null) {
            toRemove.removePropertyChangeListener(this);
            if (removeXml) {
                // delete xml file
                if (!(toRemove.getNodeBackupManager().removeNode(true))){
                    log.error("Unable to delete node xml file");
                }
            }
            ThreadingUtil.runOnGUI( ()->{ fireTableRowsDeleted(row,row); });
            toRemove.dispose();
        }
    }
    
    /**
     * Search the directory for nodes, ie userPref/cbus/123.xml
     * Add any found to the Node Manager Table
     * (Modelled after a method in jmri.jmrit.dispatcher.TrainInfoFile )
     */
    public void startupSearchNodeXmlFile() {
        // ensure preferences will be found for read
        FileUtil.createDirectory(CbusNodeBackupFile.getFileLocation());
        // create an array of file names from node dir in preferences, then loop
        List<String> names = new ArrayList<>();
        File fp = new File(CbusNodeBackupFile.getFileLocation());
        if (fp.exists()) {
            String[] fpList = fp.list(new XmlFilenameFilter());
            if (fpList !=null ) {
                names.addAll(Arrays.asList(fpList));
            }
        }
        names.forEach((nb) -> {
            log.debug("Node: {}",nb);
            int nodeNum =  jmri.util.StringUtil.getFirstIntFromString(nb);
            if (nodeNum>0 && nodeNum <65536) { // nn -1 is invalid, 0 is SLiM mode, 65535 is max hex. 0xFF
                CbusNode nd = provideNodeByNodeNum(nodeNum);
                nd.getNodeBackupManager().doLoad();
                log.debug("CbusNode {} added to table",nd);
            }
        });
    }
    
    /**
     * Disconnect from the network
     * <p>
     * Close down any background listeners
     * <p>
     * Cancel outstanding Timers
     */
    public void dispose() {
        
        clearSearchForNodesTimeout();
        if ( trickleFetch != null ) {
            trickleFetch.dispose();
            trickleFetch = null;
        }
        
        setBackgroundAllocateListener(false); // stop listening for node number requests
        
        removeTc(_memo);
        
        for (int i = 0; i < getRowCount(); i++) {
            _mainArray.get(i).removePropertyChangeListener(this);
            _mainArray.get(i).dispose();
        }
        // _mainArray = null;
        
    }

    private final static Logger log = LoggerFactory.getLogger(CbusNodeTableDataModel.class);
}
