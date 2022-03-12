package jmri.jmrix.can.cbus.node;

import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to get Node Statistics.
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeStats {

    private final CbusBasicNodeWithManagers _node;
    
    /**
     * Create a new CbusNodeStats
     *
     * @param node Node to provide stats for
     */
    public CbusNodeStats ( CbusBasicNodeWithManagers node ){
        _node = node;
    }
    
    /**
     * Node Type Name, if available. 
     * @return If Node Parameters 1 and 3 are set eg. "CANPAN", else potentially a NAME OPC return, else empty string.
     *
     */
    @Nonnull
    public String getNodeTypeName() {
        if (!CbusNodeConstants.getModuleType(_node.getNodeParamManager().getParameter(1),_node.getNodeParamManager().getParameter(3)).isEmpty() ){
            return CbusNodeConstants.getModuleType(_node.getNodeParamManager().getParameter(1),_node.getNodeParamManager().getParameter(3));
        }
        else if ( _node instanceof CbusNode ){
            return ((CbusNode)_node).getNodeNameFromName();
        }
        else {
            return "";
        }
    }
    
    /**
     * Get Node Number and name
     * @return string eg "1234 UserName", no trailing space.
     */
    @Nonnull
    public String getNodeNumberName() {
        if ( !_node.getUserName().isEmpty() ){
            return "" + _node.getNodeNumber() + " " + _node.getUserName();
        }
        else if ( !getNodeTypeName().isEmpty() ){
            return "" + _node.getNodeNumber() + " " + getNodeTypeName();
        }
        else {
            return String.valueOf(_node.getNodeNumber());
        }
    }
    
    /**
     * Get the total number of bytes to store in a backup file
     *
     * @return total number, else 0 if still waiting for a total number of events
     */
    public int totalNodeFileBytes(){
        return Math.max(0,_node.getNodeParamManager().getParameter(0)) + 
            Math.max(0,_node.getNodeNvManager().getNV(0)) + 
            Math.max(0,_node.getNodeParamManager().getParameter(5) * _node.getNodeEventManager().getTotalNodeEvents());
    }

    /**
     * Get the total bytes to transfer all data currently on module
     *
     * @return total number, else -1 if still waiting for a total number of events
     */
    public int totalNodeBytes() {
        if ( ( _node.getNodeParamManager().getParameter(0) < 0 ) 
            || ( _node.getNodeParamManager().getParameter(6) < 0 ) 
            || ( _node.getNodeParamManager().getParameter(5) < 0 )
            || ( _node.getNodeEventManager().getTotalNodeEvents() < 0 ) ){
                return -1;
            }
        return _node.getNodeParamManager().getParameter(0) + /* Total Parameters */
            _node.getNodeParamManager().getParameter(6) + /* Total NV's */
            ( _node.getNodeParamManager().getParameter(5) * _node.getNodeEventManager().getTotalNodeEvents() ) + /* Ev Variables for All Events */
            ( _node.getNodeEventManager().getTotalNodeEvents()  ); /* Events from index return */
    }
    
    /**
     * Get the number of data bytes outstanding to fetch from a node
     *
     * @return total number, else -1 if still waiting for a total number of events
     */
    public int totalRemainingNodeBytes(){
        if ( ( _node.getNodeParamManager().getOutstandingParams() < 0 ) 
            || ( _node.getNodeNvManager().getOutstandingNvCount() < 0 ) 
            || ( _node.getNodeEventManager().getOutstandingEvVars() < 0 )
            || ( _node.getNodeEventManager().getOutstandingIndexNodeEvents() < 0 ) ){
            return -1;
        }
        
        return _node.getNodeParamManager().getOutstandingParams() + /* Total Parameters */
        _node.getNodeNvManager().getOutstandingNvCount() + /* Total NV's */
        _node.getNodeEventManager().getOutstandingEvVars() + 
        ( _node.getNodeEventManager().getOutstandingIndexNodeEvents() ); /* Events from index return */
        
    }
    
    /**
     * Get the amount of Node data known to JMRI
     * in terms of percentage of total data fetch done so far.
     *
     * @return float min 0 max 1
     */
    public float floatPercentageRemaining(){
        float soFar = ( 1.0f * ( totalNodeBytes() - totalRemainingNodeBytes() ) ) / ( totalNodeBytes() );
        if ( soFar > 0 && soFar < 1.000001 ) {
            return soFar;
        }
        return 0.0f;
    }
    
    /**
     * Check if node has finished loading all available data
     * 
     * The first time that all data is loaded, saves new backup.
     *
     */
    protected void checkNodeFinishedLoad(){
        if ((!_node.getNodeBackupManager().getBackupStarted()) && totalRemainingNodeBytes() == 0) {
            if (!_node.getNodeBackupManager().doStore(true,hasLoadErrors()) ) {
                log.error("Unable to save Finished Load to Node Backup File");
            }
            _node.notifyPropertyChangeListener("BACKUPS", null, null);
        }
        // reset value if node comes online after being offline
        if (_node.getNodeBackupManager().getBackupStarted() && totalRemainingNodeBytes()>0) {
            _node.getNodeBackupManager().setBackupStarted(false);
        }
    }
    
    // 8 timers, 8 errors ?
    public boolean hasLoadErrors() {
        return _node.getNodeTimerManager().numEvTimeoutCount + _node.getNodeTimerManager().paramRequestTimeoutCount + _node.getNodeTimerManager().allEvTimeoutCount > 0;
    }
    
    private static final Logger log = LoggerFactory.getLogger(CbusNodeStats.class);
    
}
