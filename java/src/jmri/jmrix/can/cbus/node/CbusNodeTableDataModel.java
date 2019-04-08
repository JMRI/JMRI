package jmri.jmrix.can.cbus.node;

import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
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
 * Table data model for display of Cbus Nodes
 *
 * @author Steve Young (c) 2019
 * 
 */
public class CbusNodeTableDataModel extends javax.swing.table.AbstractTableModel implements CanListener {

    protected ArrayList<CbusNode> _mainArray;
    private CanSystemConnectionMemo _memo;
    private TrafficController tc;
    private CbusSend send;
    private CbusPreferences preferences;
    
    private CbusAllocateNodeNumber allocate;
    private CbusNodeTrickleFetch trickleFetch;
    
    // column order needs to match list in column tooltips
    static public final int NODE_NUMBER_COLUMN = 0; 
    static public final int NODE_TYPE_NAME_COLUMN = 1; 
    static public final int NODE_USER_NAME_COLUMN = 2;
    static public final int COMMAND_STAT_NUMBER_COLUMN = 3;
    static public final int CANID_COLUMN = 4;
    static public final int NODE_EVENTS_COLUMN = 5;
    static public final int BYTES_REMAINING_COLUMN = 6;
    static public final int NODE_TOTAL_BYTES_COLUMN = 7;
    static public final int NODE_IN_LEARN_MODE_COLUMN = 8;
    static public final int MAX_COLUMN = 9;

    public CbusNodeTableDataModel(CanSystemConnectionMemo memo, int row, int column) {
        
        log.debug("Starting MERG CBUS Node Table");
        _mainArray = new ArrayList<CbusNode>();
        _memo = memo;
        
        // connect to the CanInterface
        tc = memo.getTrafficController();
        if (tc != null ) {
            tc.addCanListener(this);
        }
        
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
        }        
        
    }
    
    // start listener for nodes requesting a new node number
    public void setBackgroundAllocateListener( boolean newState ){
        if (newState  && !java.awt.GraphicsEnvironment.isHeadless() ) {
            if (allocate == null) {
                allocate = new CbusAllocateNodeNumber( _memo, this );
            } else {
                return;
            }
        } else {
            if ( allocate != null ) {
                allocate.dispose();
            }
            allocate = null;
        }
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
            case CANID_COLUMN:
                return Bundle.getMessage("CanID");
            case NODE_NUMBER_COLUMN:
                return ("Node Num");
            case NODE_USER_NAME_COLUMN:
                return ("User Name");
            case NODE_TYPE_NAME_COLUMN:
                return ("Node Type");
            case COMMAND_STAT_NUMBER_COLUMN:
                return ("CS Num");
            case NODE_EVENTS_COLUMN:
                return ("Events");
            case NODE_TOTAL_BYTES_COLUMN:
                return("Tot. Bytes");
            case BYTES_REMAINING_COLUMN:
                return("Fetch Progress");
            case NODE_IN_LEARN_MODE_COLUMN:
                return("Learn Mode");
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
            case CANID_COLUMN:
            case NODE_EVENTS_COLUMN:
            case COMMAND_STAT_NUMBER_COLUMN:
            case NODE_IN_LEARN_MODE_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            case NODE_NUMBER_COLUMN:
            case NODE_TOTAL_BYTES_COLUMN:
                return new JTextField(5).getPreferredSize().width;
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
                return Boolean.class;
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
        switch (col) {
            case NODE_USER_NAME_COLUMN:
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
                return _mainArray.get(row).getNodeTypeName();
            case CANID_COLUMN:
                return _mainArray.get(row).getNodeCanId();
            case COMMAND_STAT_NUMBER_COLUMN:
                return _mainArray.get(row).getCsNum();
            case NODE_EVENTS_COLUMN:
                return _mainArray.get(row).getTotalNodeEvents();
            case NODE_TOTAL_BYTES_COLUMN:
                return _mainArray.get(row).totalNodeBytes();
            case BYTES_REMAINING_COLUMN:
                return _mainArray.get(row).floatPercentageRemaining();
            case NODE_IN_LEARN_MODE_COLUMN:
                return _mainArray.get(row).getNodeInLearnMode();
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
        if (col == NODE_USER_NAME_COLUMN) {        
            _mainArray.get(row).setUserName( (String) value );
            ThreadingUtil.runOnGUI( ()->{
                fireTableCellUpdated(row, col);
            });
            
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
        int opc = CbusMessage.getOpcode(m);
        int nodenum = ( m.getElement(1) * 256 ) + m.getElement(2);
        if (opc==CbusConstants.CBUS_STAT) {
            // log.debug("Command Station Updates Status {}",m);
            jmri.InstanceManager.getDefault(jmri.jmrix.can.cbus.CbusConfigurationManager.class).enableMultiMeter();
            
            if ( preferences.getAddCommandStations() ) {
                
                int csnum = m.getElement(3);
                // provides a command station by cs number, NOT node number
                CbusNode cs = provideCsByNum(csnum,nodenum);
                cs.setFW(m.getElement(5),m.getElement(6),m.getElement(7));
                cs.setCsFlags(m.getElement(4));
                cs.setCanId(CbusMessage.getId(m));
            }
            csFound++;
        }
        if (opc==CbusConstants.CBUS_PNN) {
            log.debug("Node Report message {}",m);
            if ( preferences.getAddNodes() ) {
                // provides a node by node number
                CbusNode nd = provideNodeByNodeNum(nodenum);
                nd.setManuModule(m.getElement(3),m.getElement(4));
                nd.setNodeFlags(m.getElement(5));
                nd.sendExitLearnMode();
                nd.setCanId(CbusMessage.getId(m));
            }
            ndFound++;
        }
    }
    
    /**
     * Register new node to table
     * @param node The CbusNode to add to the table
     */
    public void addNode(CbusNode node ) {
        _mainArray.add(node);
        node.setTableModel(this);
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
    public CbusNode getCsByNum( int csnum) {
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getCsNum() == csnum ) {
                return _mainArray.get(i);
            }
        }
        return null;
    }

    // returns a new or existing command station by cs number, NOT node number


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
        cs.startParamsLookup();
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
        if ( nodenum < 1 ) {
            return null;
        }
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeNumber() == nodenum ) {
                return _mainArray.get(i);
            }
        }
        CbusNode cs = new CbusNode(_memo, nodenum);
        cs.startParamsLookup();
        addNode(cs);
        return cs;        
    }

    /**
     * Returns an existing node by node number
     * @param nodenum The Node Number ( min 1, max 65535 )
     * @return the Node which has the node number, else null
     */
    public CbusNode getNodeByNodeNum( int nodenum ) {
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeNumber() == nodenum ) {
                return _mainArray.get(i);
            }
        }
        return null;        
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
     */
    public String getNodeNameFromCanId ( int canId ) {
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeCanId() == canId ) {
                return _mainArray.get(i).getNodeNumberName();
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
                return _mainArray.get(i).getNodeNumberName();
            }
        }
        return ("");
    }

    /**
     * Returns a string ArrayList of all Node Number and User Names on the table
     * 
     * @return Node Number + either node model or Username.
     */       
    public ArrayList<String> getListOfNodeNumberNames(){
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < getRowCount(); i++) {
            list.add( _mainArray.get(i).getNodeNumberName() );
        }
        return list;
    }
    
    /**
     * Returns a string ArrayList of all Node Number and User Names on the table
     * @param node Node Number, NOT row number
     * @param col Table Column Number
     */
    public void updateFromNode( int node, int col){
        
        //  log.info("table update from node {} column {}",node,col);
        ThreadingUtil.runOnGUI( ()->{
            fireTableCellUpdated(getNodeRowFromNodeNum(node), col);
        });
    }
    
    /**
     * Single Node User Name
     * @param nn Node Number, NOT row number
     * @return Node Username, if unset returns node type name
     */
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
    Boolean urgentScheduled=false; // a message is already in the out queue
    Boolean urgentActive=false; // feature active, set false for background fetch
    
    Boolean urgentFetchRequested = false;
    
    public void startUrgentFetch() {
        urgentActive = true;
        triggerUrgentFetch();
    }
    
    // fetch data in order of priority based on what user is currently viewing
    public void setUrgentFetch(int tabindex, int nodenum, int urgentNodeBefore, int urgentNodeAfter){
        urgentTab = tabindex;
        urgentNode = nodenum;
        nodebefore = urgentNodeBefore;
        nodeafter = urgentNodeAfter;
        urgentActive = true;
        triggerUrgentFetch();
    }
    
    public void triggerUrgentFetch(){
        
        if (!urgentActive) {
            return;
        }
        
        if (urgentFetchTimerTask == null) {
            setUrgentFetchTimer();
        }
        else {
            urgentFetchRequested = true;
        }
    }
    
    private TimerTask urgentFetchTimerTask;
    
    private void clearUrgentFetchTimer(){
        if (urgentFetchTimerTask != null ) {
            urgentFetchTimerTask.cancel();
            urgentFetchTimerTask = null;
        }
    }
    
    private void setUrgentFetchTimer() {
        if (urgentFetchTimerTask != null ) {
            return;
        }
        urgentFetchTimerTask = new TimerTask() {
            @Override
            public void run() {
                urgentFetchTimerTask = null;
              //  log.info("urgent fetch from timer" );
                sendNextUrgentFetch(); // the only place this should be called
                if (urgentFetchRequested) { // during timer running
                    urgentFetchRequested=false;
                    setUrgentFetchTimer();
                }
            }
        };
        TimerUtil.schedule(urgentFetchTimerTask, 20);
    }
    
    // sends the next parameter, nv or ev var
    private void sendNextUrgentFetch() {
        
        if ( getNodeByNodeNum(urgentNode) == null ) {
            clearUrgentFetchTimer();
            return;
        }
        
        // get this param
        // get below param
        // get this nv
        // get above param
        // get this events
        // get below nv
        // get below events
        // get above nv
        // get above events
        if (urgentTab==0 || urgentTab==3 || urgentTab==4 ) { // node parameters selected
            
            if ( getNodeByNodeNum(urgentNode).getOutstandingParams() > 0 ) { // this param
                getNodeByNodeNum(urgentNode).sendRequestNextParam();
                return;
            }
            
            if ( nodeafter > -1 ) {
                if ( getNodeByNodeNum(nodeafter).getOutstandingParams() > 0 ) { // below param
                    getNodeByNodeNum(nodeafter).sendRequestNextParam();
                    return;
                }
            }
            
            if ( getNodeByNodeNum(urgentNode).getOutstandingNvCount() > 0 ){ // this nv
                getNodeByNodeNum(urgentNode).sendNextNVToFetch();
                return;
            }
            
            if ( nodebefore > -1 ) {
                if ( getNodeByNodeNum(nodebefore).getOutstandingParams() > 0 ) { // above param
                    getNodeByNodeNum(nodebefore).sendRequestNextParam();
                    return;
                }
            }
            
            if ( getNodeByNodeNum(urgentNode).getOutstandingEvVars() > 0 ) { // this events
                getNodeByNodeNum(urgentNode).sendNextEvVarToFetch();
                return;
            }            
            
            if ( nodeafter > -1 ) {
                if ( getNodeByNodeNum(nodeafter).getOutstandingNvCount() > 0 ){ // below nv
                    getNodeByNodeNum(nodeafter).sendNextNVToFetch();
                    return;
                }
                
                if ( getNodeByNodeNum(nodeafter).getOutstandingEvVars() > 0 ) { // below events
                    getNodeByNodeNum(nodeafter).sendNextEvVarToFetch();
                    return;
                } 
            }

            if ( nodebefore > -1 ) {
                if ( getNodeByNodeNum(nodebefore).getOutstandingNvCount() > 0 ){ // above nv
                    getNodeByNodeNum(nodebefore).sendNextNVToFetch();
                    return;
                }
                
                if ( getNodeByNodeNum(nodebefore).getOutstandingEvVars() > 0 ) { // above events
                    getNodeByNodeNum(nodebefore).sendNextEvVarToFetch();
                    return;
                } 
            }
        }
        
        // this nv
        // below nv
        // this param
        // above nv
        // this events
        // below param
        // above param
        // below events
        // above events
        if (urgentTab==1) { // NV's selected
        
            if ( getNodeByNodeNum(urgentNode).getOutstandingNvCount() > 0 ){ // this nv
                getNodeByNodeNum(urgentNode).sendNextNVToFetch();
                return;
            }
            
            if ( nodeafter > -1 ) {
                if ( getNodeByNodeNum(nodeafter).getOutstandingNvCount() > 0 ){ // below nv
                    getNodeByNodeNum(nodeafter).sendNextNVToFetch();
                    return;
                }
            }
            
            if ( getNodeByNodeNum(urgentNode).getOutstandingParams() > 0 ) { // this param
                getNodeByNodeNum(urgentNode).sendRequestNextParam();
                return;
            }
            
            if ( nodebefore > -1 ) {
                if ( getNodeByNodeNum(nodebefore).getOutstandingNvCount() > 0 ){ // above nv
                    getNodeByNodeNum(nodebefore).sendNextNVToFetch();
                    return;
                }
            }
            
            if ( getNodeByNodeNum(urgentNode).getOutstandingEvVars() > 0 ) { // this ev vars
                getNodeByNodeNum(urgentNode).sendNextEvVarToFetch();
                return;
            }
            
            if ( nodeafter > -1 ) {
                if ( getNodeByNodeNum(nodeafter).getOutstandingParams() > 0 ) { // below param
                    getNodeByNodeNum(nodeafter).sendRequestNextParam();
                    return;
                }
            }

            if ( nodebefore > -1 ) {
                if ( getNodeByNodeNum(nodebefore).getOutstandingParams() > 0 ) { // above param
                    getNodeByNodeNum(nodebefore).sendRequestNextParam();
                    return;
                }
            }

            if ( nodeafter > -1 ) {
                if ( getNodeByNodeNum(nodeafter).getOutstandingEvVars() > 0 ) { // below events
                    getNodeByNodeNum(nodeafter).sendNextEvVarToFetch();
                    return;
                } 
            }

            if ( nodebefore > -1 ) {
                if ( getNodeByNodeNum(nodebefore).getOutstandingEvVars() > 0 ) { // above events
                    getNodeByNodeNum(nodebefore).sendNextEvVarToFetch();
                    return;
                } 
            }

        }
        
        // this events
        // this param
        // this nv
        // below events
        // above events
        // below nv
        // below param
        // above param
        // above nv
        if (urgentTab==2) { // ev vars selected
                
            if ( getNodeByNodeNum(urgentNode).getOutstandingEvVars() > 0 ) { // this events
                getNodeByNodeNum(urgentNode).sendNextEvVarToFetch();
                return;
            }

            if ( getNodeByNodeNum(urgentNode).getOutstandingParams() > 0 ) { // this param
                getNodeByNodeNum(urgentNode).sendRequestNextParam();
                return;
            }
            
            if ( getNodeByNodeNum(urgentNode).getOutstandingNvCount() > 0 ){ // this nv
                getNodeByNodeNum(urgentNode).sendNextNVToFetch();
                return;
            }
            
            if ( nodeafter > -1 ) {
                if ( getNodeByNodeNum(nodeafter).getOutstandingEvVars() > 0 ) { // below events
                    getNodeByNodeNum(nodeafter).sendNextEvVarToFetch();
                    return;
                } 
            }

            if ( nodebefore > -1 ) {
                if ( getNodeByNodeNum(nodebefore).getOutstandingEvVars() > 0 ) { // above events
                    getNodeByNodeNum(nodebefore).sendNextEvVarToFetch();
                    return;
                } 
            }

            if ( nodeafter > -1 ) {
                if ( getNodeByNodeNum(nodeafter).getOutstandingNvCount() > 0 ){ // below nv
                    getNodeByNodeNum(nodeafter).sendNextNVToFetch();
                    return;
                }

                if ( getNodeByNodeNum(nodeafter).getOutstandingParams() > 0 ) { // below param
                    getNodeByNodeNum(nodeafter).sendRequestNextParam();
                    return;
                }
            }

            if ( nodebefore > -1 ) {
                if ( getNodeByNodeNum(nodebefore).getOutstandingParams() > 0 ) { // above param
                    getNodeByNodeNum(nodebefore).sendRequestNextParam();
                    return;
                }
                if ( getNodeByNodeNum(nodebefore).getOutstandingNvCount() > 0 ){ // above nv
                    getNodeByNodeNum(nodebefore).sendNextNVToFetch();
                    return;
                }
            }
        }
        
        log.debug("all urgent fetching done");
        // all urgent fetching done
        urgentActive=false;
        
    }
    
    
    /**
     * Starts background fetching for all table data as per user prefs
     * Call whenever a node has been added to table or node edited
     * 
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
    
    protected void sendNextBackgroundFetch(){
        
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getOutstandingParams() > 0 ) {
                _mainArray.get(i).sendRequestNextParam();
                return;
            }
            if ( _mainArray.get(i).getOutstandingNvCount() > 0 ){ // this nv
                _mainArray.get(i).sendNextNVToFetch();
                return;
            }
            if ( _mainArray.get(i).getOutstandingEvVars() > 0 ) { // above events
                _mainArray.get(i).sendNextEvVarToFetch();
                return;
            }
        }
        
        // if all done dispose trickle fetch
        trickleFetch.dispose();
        trickleFetch = null;
    }
    
    /**
     * Sends a system-wide reset OPC
     * 
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
        send.searchForCommandStations();
        send.searchForNodes();
        // start timer
        setSearchForNodesTimeout( timeout );
        
    }

    private TimerTask searchForNodesTask;
    
    private void clearSearchForNodesTimeout(){
        if (searchForNodesTask != null ) {
            searchForNodesTask.cancel();
            searchForNodesTask = null;
        }
    }
    
    private void setSearchForNodesTimeout( int timeout) {
        searchForNodesTask = new TimerTask() {
            @Override
            public void run() {
                searchForNodesTask = null;
                // log.info("Node search complete " );
                
                searchFeedbackPanel.notifyNodeSearchComplete(csFound,ndFound);
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
     * Remove Row from table
     * @param row int row number
     */    
    public void removeRow(int row) {
        _mainArray.remove(row);
        ThreadingUtil.runOnGUI( ()->{ fireTableRowsDeleted(row,row); });
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
        clearUrgentFetchTimer();
        if ( trickleFetch != null ) {
            trickleFetch.dispose();
            trickleFetch = null;
        }
        
        setBackgroundAllocateListener(false); // stop listening for node number requests
        if (tc != null) {
            tc.removeCanListener(this);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CbusNodeTableDataModel.class);
}
