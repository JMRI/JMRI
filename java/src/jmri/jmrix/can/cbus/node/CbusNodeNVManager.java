package jmri.jmrix.can.cbus.node;

import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a Processing of CAN Frames for a CbusNode.
 *
 * @author Steve Young Copyright (C) 2019,2020
 */
public class CbusNodeNVManager {
    private final CbusBasicNodeWithManagers _node;
    private int[] _nvArray;
    private int[] newNvsToTeach;
    private int nextNvInLoop;
    private boolean TEACH_OUTSTANDING_NVS = false;
    
    /**
     * Create a new CbusNodeNVManager
     *
     * @param node The Node
     */
    public CbusNodeNVManager ( CbusBasicNodeWithManagers node ){
        _node = node;

    }
    
    /**
     * Reset this CbusNodeNVManager.
     * Array is set to null and NV SendError Count 0.
     */
    protected void reset() {
        _nvArray = null;
        _node.getNodeTimerManager()._sendNVErrorCount = 0;
    }
    
    /**
     * Get Flag for if any outstanding Teach NV operations are due.
     * @return true if outstanding teaches, else false.
     */
    protected boolean teachOutstandingNvs() {
        return TEACH_OUTSTANDING_NVS;
    }
    
    /**
     * Set the Node Variables
     * <p>
     * 0th NV is total NVs
     * so length of newnvs should already be num. of NV's +1
     * 
     * @param newnvs an int array, the first value being the total number
     * 
     */
    public void setNVs( @Nonnull int[] newnvs ) {
        
        _nvArray = new int [(newnvs.length)]; // no need to compensate for index 0 being total
        for (int i = 0; i < newnvs.length; i++) {
            setNV(i,newnvs[i]);
        }
        _node.notifyPropertyChangeListener("ALLNVUPDATE", null, null);
    }
    
    /**
     * Set a single Node Variable
     * <p>
     * so Index 1 is NV1 .. Index 255 is NV255
     * Index 0 is set by Node Parameter
     * 
     * @param index NV Index
     * @param newnv min 1, max 255
     * 
     */
    public void setNV( int index, int newnv ) {
        
        if ( _nvArray == null ){
            log.error("Attempted to set NV {} on a null NV Array on Node {}",index,_node);
            return;
        }
        if (index == 0 ) {
            if ( newnv != _node.getNodeParamManager().getParameter(6)
                    && _node.getNodeParamManager().getParameter(6) != -1
                    ){
                log.error("Node {} NV Count mismatch. Parameters report {} NVs, received set for {} NVs",
                        _node, _node.getNodeParamManager().getParameter(6),
                        newnv);
            }
        }
        if (index < 0 || index > 255) { // 0 is total
            log.error("Attempted to set Invalid NV {} on Node {}",index,_node);
            return;
        }
        if (newnv < -1 || newnv > 255) { // -1 is unset
            log.error("Attempted to set NV {} Invalid Value {} on Node {}",index,newnv,_node);
            return;
        }
        _nvArray[index]=newnv;
        _node.notifyPropertyChangeListener("SINGLENVUPDATE",null,( index -1));
        
    }
    
    /**
     * Get the Node Variable int Array
     * <p>
     * 0th Index is total NVs
     * so Index 1 is NV1 .. Index 255 is NV255
     * 
     * @return Array of NV's, first in index is Total NV's
     */
    public int[] getNvArray() {
        return _nvArray;
    }
    
    /**
     * Number of Node Variables on the node.
     * @return 0 if number of NV's unknown, else number of NV's.
     */
    public int getTotalNVs() {
        if ( _nvArray==null){
            return 0;
        }
        return _nvArray[0];
    }
    
    /**
     * Get a specific Node Variable
     * @param index NV Index
     * @return -1 if NV's unknown, else Node Variable value.
     *
     */
    public int getNV ( int index ) {
        if ( getTotalNVs() < 1 ){
            return -1;
        }
        return _nvArray[index];
    }
    
    /**
     * Get number of difference between this and another Nodes Node Variables
     * @param testAgainst The CBUS Node to test against
     * @return number of different NV's
     *
     */
    public int getNvDifference(CbusNode testAgainst){
        int count = 0;
        for (int i = 0; i < _nvArray.length; i++) {
            if (getNV(i) != testAgainst.getNodeNvManager().getNV(i)){
                count++;
            }
        }
        return count;
    }
    
    /**
     * Number of unknown Node Variables.
     * i.e. not yet fetched from a physical node.
     * @return -1 if number of NV's unknown, 0 if all NV's known, else number of outstanding.
     *
     */
    public int getOutstandingNvCount(){
        int count = 0;
        if ( _nvArray == null ){
            return -1;
        }
        for (int i = 0; i < _nvArray.length; i++) {
            if ( _nvArray[i] < 0 ) {
                count ++;
            }
        }
        return count;
    }
    
    /**
     * Send a request for the next unknown Node Variable.
     * <p>
     * Only triggered from CBUS Node Manager.
     * <p>
     * Does NOT send if the node has existing outstanding requests.
     * Expected response from node NVANS
     */
    protected void sendNextNVToFetch(){
        
        if ( _node.getNodeTimerManager().hasActiveTimers() ) {
            return;
        }
        
        for (int i = 0; i < _nvArray.length; i++) {
            if ( _nvArray[i] < 0 ) {
                // start NV request timer
                
                _node.getNodeTimerManager().setNextNvVarTimeout();
                _node.send.nVRD( _node.getNodeNumber(), i );
                return;
            }
        }
    }
    
    /**
     * Send and teach updated Node Variables to this node
     *
     * @param newnv array of variables, index 0 i the array is total variables
     */
    public void sendNvsToNode( int[] newnv ) {
        
      //  log.info("start loop to send nv's , nv 1 is {}",newnv[1]);
        newNvsToTeach = newnv;
        nextNvInLoop = 1; // start from 1 not 0 as 0 is the total num. nv's
        TEACH_OUTSTANDING_NVS = true;
        _node.getNodeTimerManager()._sendNVErrorCount = 0 ;
        
        // check length of new array
      //  log.info("array size {}",newNvsToTeach.length);
        
        sendNextNvToNode();
        
    }
    
    /**
     * Loop for NV teaching
     */
    protected void sendNextNvToNode() {
        
        if ( _node.getNodeTimerManager().hasActiveTimers() ) {
            return;
        }
        
        for (int i = nextNvInLoop; i < _nvArray.length; i++) {
            if ( newNvsToTeach[i] != _nvArray[i] ) {
                _node.getNodeTimerManager().setsendEditNvTimeout();
                _node.send.nVSET( _node.getNodeNumber() ,i, newNvsToTeach[i] );
                nextNvInLoop = i;
                return;
            }
        }
        
        log.info( Bundle.getMessage("NdCompleteNVar", String.valueOf(_node.getNodeTimerManager()._sendNVErrorCount) , _node ) );
        TEACH_OUTSTANDING_NVS = false;
        _node.notifyPropertyChangeListener("TEACHNVCOMPLETE", null, _node.getNodeTimerManager()._sendNVErrorCount);
        
        // refresh all nvs from node if error
        if ( _node.getNodeTimerManager()._sendNVErrorCount > 0 ){ // user notified in _mainPane
            
            int [] myarray = new int[(_node.getNodeParamManager().getParameter(6)+1)]; // +1 to account for index 0 being the NV count
            java.util.Arrays.fill(myarray, -1);
            myarray[0] = _node.getNodeParamManager().getParameter(6);
            setNVs(myarray);
            
            _node.getTableModel().startUrgentFetch();
            
        }
        _node.getNodeTimerManager()._sendNVErrorCount = 0;
    }
    
    /**
     * Get the NV String in Hex Byte Format
     * <p>
     * eg. for NV array [3,1,2,255] returns "0102FF"
     * 
     * @return Full NV String WITHOUT leading number of NVs
     */  
    public String getNvHexString() {
        if ( getNvArray() == null ) {
            return "";
        } else {
            return jmri.util.StringUtil.hexStringFromInts(getNvArray()).replaceAll("\\s","").substring(2);
        }
    }
    
    /**
     * @return descriptive string
     */
    @Override
    public String toString() {
        return "Node Variables";
    }
    
    private static final Logger log = LoggerFactory.getLogger(CbusNodeNVManager.class);
    
}
