package jmri.jmrix.can.cbus.node;

// import javax.annotation.Nonnull;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a node.
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusBasicNodeWithManagers extends CbusBasicNode {
    
    private final CbusNodeTimerManager _timers;
    private final CbusNodeParameterManager _nodeParameters;
    private final CbusNodeNVManager _nvManager;
    private final CbusNodeEventManager _evManager;
    private final CbusNodeStats _nodeStats;
    private final CbusNodeBackupManager thisNodeBackupFile;
    private CbusNodeCanListener _canListener;
    private CbusNodeTableDataModel tableModel;
    
    protected int _fwMaj;
    protected int _fwMin;
    protected int _fwBuild;
    private int _manu;
    private int _type;
    private boolean _nvWriteInLearn;
    
    private String _nodeUserName;
    
    /**
     * Create a new CbusBasicNodeWithManagers
     *
     * @param connmemo The CAN Connection to use
     * @param nodenumber The Node Number
     */
    public CbusBasicNodeWithManagers ( CanSystemConnectionMemo connmemo, int nodenumber ){
        super(connmemo,nodenumber);
        
        
        _nodeUserName = "";
        
        if (_memo != null) {
            log.debug("New CanListener {}",this.getCanListener());
        }
        _timers = new CbusNodeTimerManager(this);
        _nodeParameters = new CbusNodeParameterManager(this);
        _nvManager = new CbusNodeNVManager(this);
        _evManager = new CbusNodeEventManager(_memo,this);
        thisNodeBackupFile = new CbusNodeBackupManager(this);
        _nodeStats = new CbusNodeStats(this);
        _nvWriteInLearn = false;
        
        _manu = -1;
        _type = -1;
        setFW( -1, -1, -1);
        
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
    
    protected int getPnnManufacturer() {
        return _manu;
    }
    
    protected int getPnnModule() {
        return _type;
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
     * Set if node must be in learn mode to write NVs
     * 
     * @param nvwil true or false for node requires learn mode
     */
    public void setNvWriteInLearn(boolean nvwil) {
        _nvWriteInLearn = nvwil;
    }
    
    /**
     * Get state of write in learn flag
     * 
     * @return true if node must be in learn to write NVs
     */
    public boolean getNvWriteInLearn() {
        return _nvWriteInLearn;
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
     * Stops any timers and disconnects from network
     *
     */
    @OverridingMethodsMustInvokeSuper
    public void dispose(){
        getCanListener().dispose();
        getNodeTimerManager().cancelTimers(); // cancel timers
    }
    
    private static final Logger log = LoggerFactory.getLogger(CbusBasicNodeWithManagers.class);
    
}
