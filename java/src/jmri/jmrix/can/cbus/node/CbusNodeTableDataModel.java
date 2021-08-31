package jmri.jmrix.can.cbus.node;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import javax.annotation.Nonnull;
import jmri.jmrix.can.*;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.CbusSend;
import jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolPane;
import jmri.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of CBUS Nodes
 *
 * @author Steve Young (c) 2019
 * 
 */
public class CbusNodeTableDataModel extends CbusBasicNodeTableFetch implements CanListener, PropertyChangeListener {

    private final CbusSend send;
    private ArrayList<Integer> _nodesFound;
    private CbusAllocateNodeNumber allocate;
    protected CbusPreferences preferences;

    public CbusNodeTableDataModel(@Nonnull CanSystemConnectionMemo memo, int row, int column) {
        super(memo,row,column);
        log.debug("Starting MERG CBUS Node Table");
        preferences = jmri.InstanceManager.getDefault(CbusPreferences.class);
        
        _mainArray = new ArrayList<>();
        _nodesFound = new ArrayList<>();

        // connect to the CanInterface
        addTc(memo);
        
        send = new CbusSend(memo);

    }
    
    public void startup(){
        
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
        int nodenum = ( m.getElement(1) * 256 ) + m.getElement(2);
        switch (CbusMessage.getOpcode(m)) {
            case CbusConstants.CBUS_STAT:
                // log.debug("Command Station Updates Status {}",m);
                
                if ( preferences.getAddCommandStations() ) {
                    
                    int csnum = m.getElement(3);
                    // provides a command station by cs number, NOT node number
                    CbusNode cs = provideCsByNum(csnum,nodenum);
                    cs.setFW(m.getElement(5),m.getElement(6),m.getElement(7));
                    cs.setCsFlags(m.getElement(4));
                    cs.setCanId(CbusMessage.getId(m));
                    
                }   _nodesFound.add(nodenum);
                csFound++;
                break;
            case CbusConstants.CBUS_PNN:
                log.debug("Node Report message {}",m);
                if ( searchForNodesTask != null && preferences.getAddNodes() ) {
                    // provides a node by node number
                    CbusNode nd = provideNodeByNodeNum(nodenum);
                    nd.setManuModule(m.getElement(3),m.getElement(4));
                    nd.setNodeFlags(m.getElement(5));
                    nd.setCanId(CbusMessage.getId(m));
                }   _nodesFound.add(nodenum);
                ndFound++;
                break;
            case CbusConstants.CBUS_NNREL:
                // from node advising releasing node number
                if ( getNodeRowFromNodeNum(nodenum) >-1 ) {
                    log.info( Bundle.getMessage("NdRelease", getNodeName(nodenum), nodenum ) );
                    removeRow( getNodeRowFromNodeNum(nodenum),false );
                }
                break;
            default:
                break;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent ev){
        if (!(ev.getSource() instanceof CbusNode)) {
            return;
        }
        
        int evRow = getNodeRowFromNodeNum((( CbusNode ) ev.getSource()).getNodeNumber());
        if (evRow<0){
            return;
        }
        ThreadingUtil.runOnGUIEventually( ()->{
            switch (ev.getPropertyName()) {
                case "SINGLENVUPDATE":
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
        });
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
    
    private boolean searchXmlComplete = false;
    
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
            CbusNode nd = provideNodeByNodeNum(nodeNum);
            nd.getNodeBackupManager().doLoad();
            log.debug("CbusNode {} added to table",nd);
        });
        searchXmlComplete = true;
    }
    
    public boolean startupComplete(){
        return !(!searchXmlComplete && searchForNodesTask != null);
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
