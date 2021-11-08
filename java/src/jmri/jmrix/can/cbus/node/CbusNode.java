package jmri.jmrix.can.cbus.node;

// import javax.annotation.Nonnull;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a node.
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNode extends CbusBasicNodeWithMgrsCommandStation {
    
    private int _flags;
    private String _userComment;
    private boolean _sendsWRACKonNVSET;
    private boolean _nvWriteInLearnOnly;
    public static int SINGLE_MESSAGE_TIMEOUT_TIME = 1500;
    public static int BOOT_PAUSE_TIMEOUT_TIME = 1000;
    public static int BOOT_ENTRY_TIMEOOUT_TIME = 500;
    public static int BOOT_SINGLE_MESSAGE_TIMEOUT_TIME = 500;
    public static int BOOT_PROG_TIMEOUT_FAST = 10;
    public static int BOOT_PROG_TIMEOUT_SLOW = 50;
    public static int BOOT_CONFIG_TIMEOUT_TIME = 50;
    private String _nodeNameFromName;
    private String resyncName = null;
    
    /**
     * Create a new CbusNode
     *
     * @param connmemo The CAN Connection to use
     * @param nodenumber The Node Number
     */
    public CbusNode ( CanSystemConnectionMemo connmemo, int nodenumber ){
        super(connmemo,nodenumber);
        
        _sendsWRACKonNVSET = true;
        _nvWriteInLearnOnly = false;
        _flags = -1;
        _userComment = "";
        _nodeNameFromName = "";
    }
    
    /**
     * Set Node UserName only if UserName not currently set
     * used in RestoreFromFCU
     * @param newName UserName of the node
     */
    public void setNameIfNoName( String newName ) {
        if ( getUserName().isEmpty() ){
            setUserName(newName);
        }
    }
    
    /**
     * Get Module Type Name from a previous NAME OPC response
     * Used when a module type is not identified within JMRI.
     * @return Module type name, NOT prefixed with CAN or ETH, may be empty.
     */
    public String getNodeNameFromName(){
        return _nodeNameFromName;
    }
    
    /**
     * Set Module Type Name
     * Used when a module is not identified within JMRI so the NAME is requested.
     * @param newName The module type name, should NOT be prefixed with CAN or ETH
     */
    public void setNodeNameFromName( String newName ){
        if (newName==null) {
            newName = "";
        }
        _nodeNameFromName = newName;
        notifyPropertyChangeListener("NAMECHANGE", null, null);
    }
    
    /**
     * Set the Node Flags
     * <p>
     * Bit 0: Consumer
     * Bit 1: Producer
     * Bit 2: FLiM Mode
     * Bit 3: The module supports bootloading
     * Bit 4: The module can consume its own produced events
     * Bit 5: Module is in learn mode - CBUS Spec 6c
     *
     * @param flags the int value of the flags.
     */
    public void setNodeFlags(int flags) {
        _flags = flags;
        // if ( ( ( _flags >> 0 ) & 1 ) == 1 ){
        //     log.debug("Consumer node");
        // }
        
        if ( ( ( _flags >> 2 ) & 1 ) == 0 ){
            getNodeBackupManager().setNodeInSlim();
            getNodeEventManager().resetNodeEventsToZero(); // sets num events to 0
        }
        
        if ( ( ( _flags >> 5 ) & 1 ) == 1 ){
            log.debug("Node in learn mode");
            setNodeInLearnMode(true);
        }
        notifyPropertyChangeListener("PARAMETER", null, null);
    }
    
    /**
     * Get Node Flags
     * <p>
     * Captured from a PNN node response
     * @return flags in int form, will need decoding to bit, -1 if unset
     */
    public int getNodeFlags() {
        return _flags;
    }
    
    /**
     * Get name of node being resync'ed
     * 
     * @return String node name
     */
    public String getResyncName() {
        return resyncName;
    }
    
    /**
     * Save module name when Resync is in progress
     *
     * Resync resets all node parameters, etc. To re0display the NV edit GUI
     * before the resync completes we save a copy of the name of the node.
     */
    protected void saveForResync() {
        if (!CbusNodeConstants.getModuleType(this.getNodeParamManager().getParameter(1),this.getNodeParamManager().getParameter(3)).isEmpty() ){
            resyncName =  CbusNodeConstants.getModuleType(this.getNodeParamManager().getParameter(1),this.getNodeParamManager().getParameter(3));
        } else {
            resyncName = null;
        }
    }
    
    /**
     * Resets NV's, Events and Parameters
     *
     */
    protected void resetNodeAll() {
        getNodeNvManager().reset();
        getNodeEventManager().resetNodeEvents();
        getNodeParamManager().clearParameters();
        getNodeTimerManager().resetTimeOutCounts();
        notifyPropertyChangeListener("PARAMETER", null, null);
    }

    /**
     * Get a Node User Comment
     * @return user generated comment string
     *
     */
    public String getUserComment() {
        return _userComment;
    }
    
    /**
     * Set a Node User Comment
     * <p>
     * Typically output from JTextArea
     * <p>
     * If a backup has completed for this session, updates the xml file
     *
     * @param comment user comment
     */
    public void setUserComment(String comment) {
        _userComment = comment;
        if (getNodeBackupManager().getBackupStarted()) {
            if (!getNodeBackupManager().doStore(false,getNodeStats().hasLoadErrors()) ) {
                log.error("Unable to save User Comment to Node Backup File");
            }
        }
    }
    
    /**
     * Custom toString reports the Node Number Name
     * <p>
     * @return string eg "1234 UserName" or "256 CANPAN" if no UserName. No trailing space.
     *
     * {@inheritDoc} 
     */
    @Override
    public String toString(){
        return getNodeStats().getNodeNumberName();
    }
    
    /**
     * Reports just the name, not the user name.
     * <p>
     * @return string eg "CANPAN"
     *
     */
    public String getName(){
        return getNodeStats().getNodeTypeName();
    }
    
    /**
     * Set node on network
     * @param isFound false if not on network
     */
    protected void nodeOnNetwork( boolean isFound ) {
        
        if (!isFound) {
            getNodeTimerManager().cancelTimers();
            getNodeParamManager().setParameters( new int[]{0} );
            
            // set events to 0 as if parameters cannot be fetched, 
            // no point in attempting anything else
            getNodeEventManager().resetNodeEventsToZero();
            // if not already set as not on network, set that now
            if ( !(getNodeBackupManager().getSessionBackupStatus() == CbusNodeConstants.BackupType.NOTONNETWORK )) {
                getNodeBackupManager().nodeNotOnNetwork();
            }
            notifyPropertyChangeListener("BACKUPS", null, null);
        }
    }
    
    /**
     * DO NOT RELY ON, TO BE REMOVED IN FUTURE RELEASE
     * when the NV will automatically be queried if no NVSET is received
     *
     * @param sendsWRACK true if sends
     */
    public void setsendsWRACKonNVSET( Boolean sendsWRACK ){
        _sendsWRACKonNVSET = sendsWRACK;
    }
    
    
    
    /**
     * DO NOT RELY ON, TO BE REMOVED IN FUTURE RELEASE
     * when the NV will automatically be queried if no NVSET is received
     *
     * @return true if sends WRACK, else false
     */
    public boolean getsendsWRACKonNVSET() {
        return _sendsWRACKonNVSET;
    }
    
    
    /**
     * CANSERVO8C and related modules only accept NV writes in learn mode.
     * 
     * This was used by the MERG FCU to update servo positions in "real time" in
     * response to interaction with the GUI. Call this method to indicate that a
     * node must be in learn mode to support NV writes.
     * 
     * @param nvWriteInLearn true if node requires to be in learn mode for NV writes
     */
    public void setnvWriteInLearnOnly( Boolean nvWriteInLearn ){
        _nvWriteInLearnOnly = nvWriteInLearn;
    }
    
    /**
     * CANSERVO8C and related modules only accept NV writes in learn mode.
     * 
     * This was used by the MERG FCU to update servo positions in "real time" in
     * response to interaction with the GUI. Call this method to query if a
     * node must be in learn mode to support NV writes.
     * 
     * @return true if sends WRACK, else false
     */
    public boolean getnvWriteInLearnOnly() {
        return _nvWriteInLearnOnly;
    }
    
    private static final Logger log = LoggerFactory.getLogger(CbusNode.class);
    
}
