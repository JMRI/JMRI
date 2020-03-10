package jmri.jmrix.can.cbus.node;

import javax.annotation.Nonnull;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusPreferences;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Table data model for display of CBUS Nodes
 *
 * @author Steve Young (c) 2019
 * 
 */
public class CbusBasicNodeTableFetch extends CbusBasicNodeTableOperations {

    protected CbusNodeTrickleFetch trickleFetch;
    
    public CbusBasicNodeTableFetch(@Nonnull CanSystemConnectionMemo memo, int row, int column) {
        super(memo,row,column);
    }
    
    private int urgentNode = -1;
    private int nodebefore = -1;
    private int nodeafter = -1;
    private boolean urgentActive=false; // feature active, set false for background fetch
    
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
    protected void setUrgentNode( int nodeNum ){
        urgentNode = nodeNum;
        urgentActive = true;
        startBackgroundFetch();
    }
    
    /**
     * Fetch data in order of priority based on what user is currently viewing
     * @param nodenum number of Node to prioritise in the fetch
     * @param urgentNodeBefore number of the Node in main table row above
     * @param urgentNodeAfter number of the Node in main table row below
     */
    public void setUrgentFetch(int nodenum, int urgentNodeBefore, int urgentNodeAfter){
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
        if (urgentActive) {
            sendNextBackgroundFetch();
        }
    }
    
    /**
     * Starts background fetching for all table data as per user prefs
     * Call whenever a node has been added to table or node edited
     */ 
    public void startBackgroundFetch(){
        if (!(this instanceof CbusNodeTableDataModel)){
            return;
        }
        CbusPreferences pref = ((CbusNodeTableDataModel)this).preferences;
        // reset if already running
        if ( trickleFetch != null ){
                trickleFetch.dispose();
        }
        trickleFetch = null;
        if ( ( pref != null ) && (pref.getNodeBackgroundFetchDelay() > 0 ) ) {
            trickleFetch = new CbusNodeTrickleFetch( _memo, this, pref.getNodeBackgroundFetchDelay() );
        }
    }
    
    private boolean hasActiveTimers(){
        for (int i = 0; i < getRowCount(); i++) {
            // _mainArray.get(i).startLoadFromXml();
            _mainArray.get(i).getNodeBackupManager().doLoad();
            if ( _mainArray.get(i).getNodeTimerManager().hasActiveTimers() ){
                return true;
            }
            _mainArray.get(i).getNodeStats().checkNodeFinishedLoad();
        }
        return false;
    }
    
    // prioritise command station 
    // get parameters
    // get node variables 1-16
    private boolean sentCommandStationFetch(){
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getCsNum() > -1 ) { // is a command station
                if ( sentOutstandingParam(_mainArray.get(i)) ) { // this param
                    return true;
                }
                if (( _mainArray.get(i).getNodeParamManager().getParameter(6) > 15 ) // If CS does has have more than 15 NV's
                    && ( _mainArray.get(i).getNodeNvManager().getNV(16) < 0 )) {
                    _mainArray.get(i).getNodeNvManager().sendNextNVToFetch();
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean sentAnAllNodeParam(){
        // Get all node parameters fetched so basic node details ( type, num nv's etc. ) are known
        for (int i = 0; i < getRowCount(); i++) {
            if ( sentOutstandingParam(_mainArray.get(i)) ) { // this param
                return true;
            }
        }
        return false;
    }
    
    private boolean sentOutstandingNvs(CbusNode thisnode){
        if ( thisnode!=null && thisnode.getNodeNvManager().getOutstandingNvCount() > 0 ){ // this nv
            thisnode.getNodeNvManager().sendNextNVToFetch();
            return true;
        }
        return false;
    }
    
    private boolean sentOutstandingEvs(CbusNode thisnode) {
        if ( thisnode!=null && thisnode.getNodeEventManager().getOutstandingEvVars() > 0 ) { // this events
            thisnode.getNodeEventManager().sendNextEvVarToFetch();
            return true;
        }
        return false;
    }
    
    private boolean sentOutstandingNvsOrEvs(CbusNode thisnode) {
        return sentOutstandingNvs(thisnode) || sentOutstandingEvs(thisnode);
    }
    
    private boolean sentOutstandingParam(CbusNode thisnode) {
        if ( thisnode!=null && thisnode.getNodeParamManager().getOutstandingParams() > 0 ) { // this params
            thisnode.getNodeParamManager().sendRequestNextParam();
            return true;
        }
        return false;
    }
    
    private boolean sentUrgentFetches() {
    
        // If a node is selected in the node manager the details for this are fetched next
        CbusNode _urgentNode = getNodeByNodeNum(urgentNode);
        CbusNode _beforeNode = getNodeByNodeNum(nodebefore);
        CbusNode _afterNode = getNodeByNodeNum(nodeafter);
        
        if (sentOutstandingNvsOrEvs(_urgentNode)) {
            return true;
        }
        
        if ( sentOutstandingNvs(_afterNode) || sentOutstandingNvs(_beforeNode) ){
            return true;
        }

        if (sentOutstandingEvs(_afterNode) || sentOutstandingEvs(_beforeNode) ) {
            return true;
        }
            
        // the node selected in table has been synched, 
        // along with the row above and below in case user scolls
        urgentActive=false;
    
        return false;
    
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
        
        if ( getAnyNodeInLearnMode()>0 || hasActiveTimers()){
            return;
        }
        
        if (sentCommandStationFetch() || sentAnAllNodeParam()){
            return;
        }
        
        if (sentUrgentFetches()){
            return;
        }
        
        // default lookup routine for NV's then Events.
        // parameters should already be known
        for (int i = 0; i < getRowCount(); i++) {
            if (sentOutstandingNvsOrEvs(_mainArray.get(i))){
                return;
            }
        }
        
        // if all done dispose trickle fetch
        if ( trickleFetch != null ) {
            trickleFetch.dispose();
        }
        trickleFetch = null;
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusBasicNodeTableFetch.class);
}
