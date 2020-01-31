package jmri.jmrix.can.cbus.node;

import java.beans.PropertyChangeListener;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.annotation.Nonnull;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusSend;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a node.
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNode {
    protected CanSystemConnectionMemo _memo;
    
    private int _nodeNumber;
    private String _nodeUserName;
    private String _nodeNameFromName;
    
    private int _canId;
    
    private int _flags;
    protected int _fwMaj;
    protected int _fwMin;
    protected int _fwBuild;
    protected int _manu;
    protected int _type;
    private boolean _inSetupMode;
    private boolean _inlearnMode;
    private boolean _inFLiMMode;
    private boolean _sendsWRACKonNVSET;
    
    public CbusSend send;
    private CbusNodeTableDataModel tableModel;
    private final CbusNodeTimerManager _timers;
    private final CbusNodeParameterManager _nodeParameters;
    private final CbusNodeNVManager _nvManager;
    private final CbusNodeEventManager _evManager;
    private final CbusNodeStats _nodeStats;
    
    private int _csNum;
    private boolean _StatResponseFlagsAccurate;
    
    private CbusNodeCanListener _canListener;
    
    private String _userComment = "";
    
    private final CbusNodeBackupManager thisNodeBackupFile;
    
    // data members to hold contact with the property listeners
    protected final CopyOnWriteArraySet<PropertyChangeListener> _listeners;
    
    /**
     * Create a new CbusNode
     *
     * @param connmemo The CAN Connection to use
     * @param nodenumber The Node Number
     */
    public CbusNode ( CanSystemConnectionMemo connmemo, int nodenumber ){
        _memo = connmemo;
        _nodeNumber = nodenumber;
        _nodeUserName = "";
        _nodeNameFromName = "";
        setFW( -1, -1, -1);
        _canId = -1;
        _inSetupMode = false;
        _inlearnMode = false;
        _inFLiMMode = true;
        _sendsWRACKonNVSET = true;
        _csNum = -1;
        _StatResponseFlagsAccurate=false;
        _manu = -1;
        _type = -1;
        
        _flags = -1;
        if (_memo != null) {
            log.debug("New CanListener {}",this.getCanListener());
        }
        send = new CbusSend(_memo);
        _timers = new CbusNodeTimerManager(this);
        _nodeParameters = new CbusNodeParameterManager(this);
        _nvManager = new CbusNodeNVManager(this);
        _evManager = new CbusNodeEventManager(_memo,this);
        thisNodeBackupFile = new CbusNodeBackupManager(this);
        _nodeStats = new CbusNodeStats(this);
        _listeners = new CopyOnWriteArraySet<>();
        tableModel = null;
    }
    
    @Nonnull
    public CbusNodeCanListener getNewCanListener(){
        return new CbusNodeCanListener(_memo,this);
    }
    
    @Nonnull
    public final CbusNodeCanListener getCanListener(){
        if (_canListener==null){
            _canListener = getNewCanListener();
        }
        return _canListener;
    }
    
    @Nonnull
    public final CbusNodeTimerManager getNodeTimerManager(){
        return _timers;
    }
    
    @Nonnull
    public final CbusNodeParameterManager getNodeParamManager() {
        return _nodeParameters;
    }
    
    @Nonnull
    public final CbusNodeNVManager getNodeNvManager() {
        return _nvManager;
    }
    
    @Nonnull
    public final CbusNodeEventManager getNodeEventManager() {
        return _evManager;
    }
    
    /**
     * Get the CbusNodeXml for Node Backup Details and operations
     * <p>
     * @return the CbusNodeXml instance for the node
     */
    @Nonnull
    public final CbusNodeBackupManager getNodeBackupManager() {
        return thisNodeBackupFile;
    }
    
    /**
     * Get Node Statistics
     * <p>
     * @return the CbusNodeXml instance for the node
     */
    @Nonnull
    public final CbusNodeStats getNodeStats() {
        return _nodeStats;
    }
    
    /**
     * Register for notification if any of the properties change
     *
     * @param l The Listener to attach to Node
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        // add only if not already registered
        if (!_listeners.contains(l)) {
            _listeners.add(l);
            // log.info("Added listener {}, new size is {}", l,_listeners.size());
        }
    }
    
    /**
     * Remove notification listener
     * @param l Listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (_listeners.contains(l)) {
            _listeners.remove(l);
            // log.info("Removed listener {}, new size is {}", l,_listeners.size());
        }
    }

    /**
     * Trigger the notification of Node PropertyChangeListeners.
     * 
     * Properties include 
     * PARAMETER, 
     * BACKUPS, 
     * SINGLENVUPDATE ( newValue NV index (0 is NV1, 5 is NV6) )
     * ALLNVUPDATE
     * SINGLEEVUPDATE ( newValue event row )
     * ALLEVUPDATE
     * DELETEEVCOMPLETE ( newValue Error String else null )
     * ADDEVCOMPLETE ( newValue Error String else null )
     * ADDALLEVCOMPLETE ( Event Teach Loop Completed, newValue error count )
     * TEACHNVCOMPLETE ( newValue error count )
     * NAMECHANGE
     * 
     * @param property Node property
     * @param oldValue Old Value
     * @param newValue New Value
     */
    protected void notifyPropertyChangeListener(String property, Object oldValue, Object newValue) {
        if ((oldValue != null && oldValue.equals(newValue)) || (newValue != null && newValue.equals(oldValue))) {
            log.error("notifyPropertyChangeListener without change");
            return;
        }
        _listeners.forEach((listener) -> {
            ThreadingUtil.runOnGUI( ()->{
                listener.propertyChange(new java.beans.PropertyChangeEvent(this, property, oldValue, newValue));
            });
        });
    }

    /**
     * Set the main Node Table Model
     * @param model the Node Table Model
     */
    protected void setTableModel( CbusNodeTableDataModel model){
        tableModel = model;
    }
    
    protected CbusNodeTableDataModel getTableModel() {
        return tableModel;
    }
    
    /**
     * Returns Node Number
     *
     * @return Node Number,1-65535
     */
    public int getNodeNumber() {
        return _nodeNumber;
    }
    
    /**
     * Returns node Username 
     * @return eg. "John Smith"
     *
     */
    public String getUserName() {
        return _nodeUserName;
    }
    
    /**
     * Set Node UserName
     * Updates Node XML File
     * @param newName UserName of the node
     */
    public void setUserName( String newName ) {
        _nodeUserName = newName;
        notifyPropertyChangeListener("NAMECHANGE", null, newName);
        if (getNodeBackupManager().getBackupStarted()) {
            if (!getNodeBackupManager().doStore(false,getNodeStats().hasLoadErrors()) ) {
                log.error("Unable to save Node Name to Node {} Backup File",this);
            }
        }
    }
    
    /**
     * Set Node UserName only if UserName not currently set
     * used in RestoreFromFCU
     * @param newName UserName of the node
     */
    public void setNameIfNoName( String newName ) {
        if ( getUserName().isEmpty() ){
            _nodeUserName = newName;
            notifyPropertyChangeListener("NAMECHANGE", null, _nodeUserName);
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
        _nodeNameFromName = newName;
        notifyPropertyChangeListener("NAMECHANGE", null, _nodeUserName);
    }
    
    /**
     * Set Node Number
     * @param newnumber Node Number, should be 1-65535
     */
    public void setNodeNumber ( int newnumber ) {
        _nodeNumber = newnumber;
        notifyPropertyChangeListener("PARAMETER", null, null);
    }

    /**
     * Set Node CAN ID
     * @param newcanid CAN ID of the node
     */
    public final void setCanId ( int newcanid ) {
        _canId = newcanid;
        notifyPropertyChangeListener("CANID", null, _canId);
    }
    
    /**
     * Node CAN ID, min 1 , ( max 128? )
     * @return CAN ID of the node, -1 if unset
     */
    public int getNodeCanId() {
        return _canId;
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
     * Temporarily store Node Firmware version obtained from a CBUS_STAT Response
     * <p>
     * Parameter array is not created until total number of parameters is known.
     * This saves asking the Node for them.
     *
     * @param fwMaj Major Firmware Type
     * @param fwMin Minor Firmware Type
     * @param fwBuild Firmware Build Number
     */
    public final void setFW( int fwMaj, int fwMin, int fwBuild ){
        _fwMaj = fwMaj;
        _fwMin = fwMin;
        _fwBuild = fwBuild;
    }

    /**
     * Temporarily store Node Manufacturer and Module Type obtained from a PNN Response
     * <p>
     * Parameter array is not created until total number of parameters is known.
     * This saves asking the Node for them.
     *
     * @param manu Manufacturer
     * @param modtype Module Type
     */
    protected void setManuModule(int manu, int modtype){
        _manu = manu;
        _type = modtype;
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
     * Set flag for this Node in Setup Mode
     * <p>
     * Does NOT send instruction to actual node
     *
     * @param setup use true if in Setup, else false
     */
    public void setNodeInSetupMode( boolean setup ) {
        _inSetupMode = setup;
        notifyPropertyChangeListener("PARAMETER", null, null);
    }
    
    /**
     * Get if this Node is in Setup Mode
     *
     * @return true if in Setup, else false
     */
    public boolean getNodeInSetupMode() {
        return _inSetupMode;
    }
    
    /**
     * Set if the Node is in Learn Mode
     * Used to track node status, does NOT update Physical Node
     * 
     * @param inlearnmode set true if in Learn else false
     */
    public void setNodeInLearnMode( boolean inlearnmode) {
        boolean oldLearnmode = _inlearnMode;
        _inlearnMode = inlearnmode;
        if (oldLearnmode != _inlearnMode) {
            notifyPropertyChangeListener("LEARNMODE",oldLearnmode,_inlearnMode);
        }
    }
    
    /**
     * Get if the Node is in Learn Mode
     * <p>
     * Defaults to false if unset
     * 
     * @return true if in Learn else false
     */
    public boolean getNodeInLearnMode() {
        return _inlearnMode;
    }

    /**
     * Set if the Node is in FLiM Mode
     * <p>
     * Defaults to true if unset
     * 
     * @param inFlimMode set true if in FlIM else false
     */
    public void setNodeInFLiMMode( boolean inFlimMode ) {
        _inFLiMMode = inFlimMode;
    }    
    
    /**
     * Get if the Node is in FLiM Mode
     * <p>
     * Defaults to true if unset
     * 
     * @return true if in FlIM else false
     */
    public boolean getNodeInFLiMMode() {
        return _inFLiMMode;
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
    public Boolean getsendsWRACKonNVSET() {
        return _sendsWRACKonNVSET;
    }
    
    /**
     * Set a Command Station Number for this Node
     *
     * @param csnum Command station Number, normally 0 if using a single command station
     */
    public void setCsNum( int csnum ) {
        _csNum = csnum;
    }
    
    /**
     * Get Command station number.
     * <p>
     * 0 is normally default for a command station
     *
     * @return -1 if node is NOT a Command Station, else CS Number.
     */    
    public int getCsNum() {
        return _csNum;
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
     * Disable Command Station Flag Reporting
     * 
     * @param StatResponseFlagsAccurate set false to ignore the Command Station Flags
     */
    public void setStatResponseFlagsAccurate ( boolean StatResponseFlagsAccurate) {
        _StatResponseFlagsAccurate = StatResponseFlagsAccurate;
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
     * Set the flags reported by a Command Station
     * <p>
     * This will update Track Power On / Off, etc. as per the values passed.
     * Currently unused by CANCMD v4 which sets the setStatResponseFlagsAccurate(false)
     *
     * @param flags the int value of the Command Station flags
     */
    public void setCsFlags( int flags ) {
        log.debug("flags value {}",flags);
        
        // flags value in STAT response not accurate for CANCMD v4
        if (!_StatResponseFlagsAccurate) {
            return;
        }
        
        // 0 - Hardware Error (self test)
        // 1 - Track Error
        // 2 - Track On/ Off
        // 3 - Bus On/ Halted
        // 4 - EM. Stop all performed
        // 5 - Reset done
        // 6 - Service mode (programming) On/ Off
        
        if ( ( ( flags ) & 1 ) == 1 ){
            log.warn("Command Station {} Reporting Hardware Error (self test)", getCsNum() );
        }
        if ( ( ( flags >> 1 ) & 1 ) == 1 ){
            log.warn("Command Station {} Reporting Track Error", getCsNum() );
        }
        
        // flag 2 handled by CbusPowerManager
        
        // listening for RSTAT flag bit 2 here rather than power manager in case in future 
        // we can direct to power zones rather than whole layout power
        // it's also a per command station report than a per layout report
        if ( ( ( flags >> 2 ) & 1 ) == 1 ){
            log.debug("Command Station {} Reporting Track On", getCsNum() );
            try {
                jmri.InstanceManager.getDefault(jmri.PowerManager.class).setPower(jmri.PowerManager.ON);
            } catch (jmri.JmriException e) {
                log.error("unable to set Power On {}",e);
            }
        } else {
            log.debug("Command Station {} Reporting Track Off", getCsNum() );
            try {
                jmri.InstanceManager.getDefault(jmri.PowerManager.class).setPower(jmri.PowerManager.OFF);
            } catch (jmri.JmriException e) {
                log.error("unable to set Power Off {}",e);
            }
        }

        if ( ( ( flags >> 3 ) & 1 ) == 0 ){
            log.info("Command Station {} Reporting Bus Halted", getCsNum() );
        }
        
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
            getNodeEventManager().resetNodeEvents();
            
            // if not already set as not on network, set that now
            if ( !(getNodeBackupManager().getSessionBackupStatus() == CbusNodeConstants.BackupType.NOTONNETWORK )) {
                getNodeBackupManager().nodeNotOnNetwork();
            }
            notifyPropertyChangeListener("BACKUPS", null, null);
        }
    }

    /**
     * Stops any timers and disconnects from network
     *
     */
    public void dispose(){
        getCanListener().dispose();
        getNodeTimerManager().cancelTimers(); // cancel timers
    }
    
    private static final Logger log = LoggerFactory.getLogger(CbusNode.class);
    
}
