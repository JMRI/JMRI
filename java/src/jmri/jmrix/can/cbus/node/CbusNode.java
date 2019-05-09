package jmri.jmrix.can.cbus.node;

import java.util.ArrayList;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusSend;
import jmri.jmrix.can.cbus.swing.nodeconfig.CbusNodeEditEventFrame;
import jmri.jmrix.can.cbus.swing.nodeconfig.CbusNodeEditNVarFrame;
import jmri.jmrix.can.cbus.swing.nodeconfig.CbusNodeRestoreFcuFrame;
import jmri.jmrix.can.TrafficController;
import jmri.util.ThreadingUtil;
import java.util.TimerTask;
import jmri.util.TimerUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a node.
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNode implements CanListener {
    private CanSystemConnectionMemo memo;
    private TrafficController tc;
    
    private int _nodeNumber;
    private String _nodeUserName;
    private String _nodeNameFromName;
    protected ArrayList<CbusNodeEvent> _nodeEvents;//  _ndEv;
    private int _canId;
    private int[] _nvArray;
    private int[] _parameters;
    private int _flags;
    private int _fwMaj;
    private int _fwMin;
    private int _fwBuild;
    private int _manu;
    private int _type;
    private boolean nodeTraitsSet;
    private boolean _inSetupMode;
    private boolean _inlearnMode;
    private boolean _inFLiMMode;
    private boolean _sendsWRACKonNVSET;
    private boolean _eventIndexValid;
    public CbusSend send;
    private CbusNodeTableDataModel tableModel;
    private CbusNodeEventTableDataModel nodeEventTableModel;
    private CbusNodeNVTableDataModel nodeNVTableModel;
    private CbusNodeNVTableDataModel nodeEditNVTableModel;
    
    private int _csNum;
    private boolean _StatResponseFlagsAccurate;
    private boolean _commandStationIdentified;
    public static int SINGLE_MESSAGE_TIMEOUT_TIME = 800;
    
    public CbusNode ( CanSystemConnectionMemo connmemo, int nodenumber ){
        memo = connmemo;
        if (memo != null) {
            tc = memo.getTrafficController();
            tc.addCanListener(this);
        }
        send = new CbusSend(memo);
        _nodeNumber = nodenumber;
        _nodeUserName = "";
        _nodeNameFromName = "";
        setFW( -1, -1, -1);
        _parameters = null;
        _canId = -1;
        _nodeEvents = null;
        _inSetupMode = false;
        _inlearnMode = false;
        _inFLiMMode = true;
        _sendsWRACKonNVSET = true;
        _csNum = -1;
        _StatResponseFlagsAccurate=false;
        _commandStationIdentified = false;
        nodeTraitsSet = false;
        _manu = -1;
        _type = -1;
        _eventIndexValid = false;
        _flags = -1;
        
    }
    
    protected void setTableModel( CbusNodeTableDataModel model){
        tableModel = model;
    }
    
    protected void setNodeEventTable (CbusNodeEventTableDataModel model) {
        nodeEventTableModel = model;
    }

    protected void setNodeNVTable( CbusNodeNVTableDataModel model ) {
        nodeNVTableModel = model;
    }
    
    protected void setEditNodeNVTable( CbusNodeNVTableDataModel model ) {
        nodeEditNVTableModel = model;
    }

    public int getNodeType() {
        return getParameter(3);
    }
    
    /**
     * Node Type Name, if available. 
     * @return If Node Parameters 1 and 3 are set eg. "CANPAN", else potentially a NAME OPC return, else empty string.
     *
     */
    public String getNodeTypeName() {
        
        if (!CbusNodeConstants.getModuleType(getParameter(1),getParameter(3)).isEmpty() ){
            return CbusNodeConstants.getModuleType(getParameter(1),getParameter(3));
        }
        else {
            return getNodeNameFromName();
        }
    }

    /**
     * Returns Node Number
     *
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
     * Node Number and name
     * @return string eg "1234 UserName", no trailing space.
     */
    public String getNodeNumberName() {
        if ( !getUserName().isEmpty() ){
            return getNodeNumber() + " " + getUserName();
        }
        else {
            return getNodeNumber() + " " + getNodeTypeName();
        }
    }
    
    public void setUserName( String newname ) {
        _nodeUserName = newname;
    }
    
    public void setNameIfNoName( String newName ) {
        if ( getUserName().isEmpty() ){
            _nodeUserName = newName;
            notifyModelIfExists(CbusNodeTableDataModel.NODE_USER_NAME_COLUMN);
        }
    }
    
    public String getNodeNameFromName(){
        return _nodeNameFromName;
    }
    
    public void setNodeNameFromName( String newName ){
        _nodeNameFromName = newName;
        notifyModelIfExists(CbusNodeTableDataModel.NODE_TYPE_NAME_COLUMN);   
    }
    
    public void setNodeNumber ( int newnumber ) {
        _nodeNumber = newnumber;
    }

    /**
     * Set Node CAN ID
     * @param newcanid CAN ID of the node
     */
    public void setCanId ( int newcanid ) {
        _canId = newcanid;
        notifyModelIfExists(CbusNodeTableDataModel.CANID_COLUMN);
    }
    
    /**
     * Node CAN ID, min 1 , ( max 128? )
     * @return CAN ID of the node, -1 if unset
     */
    public int getNodeCanId() {
        return _canId;
    }
    
    public void setNodeFlags(int flags) {
        _flags = flags;
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
     * Node Parameters
     * <p>
     * Para 0 Number of parameters
     * <p>
     * Para 1 The manufacturer ID
     * <p>
     * Para 2 Minor code version as an alphabetic character (ASCII)
     * <p>
     * Para 3 Manufacturer module identifier as a HEX numeric
     * <p>
     * Para 4 Number of supported events as a HEX numeric
     * <p>
     * Para 5 Number of Event Variables per event as a HEX numeric
     * <p>
     * Para 6 Number of supported Node Variables as a HEX numeric
     * <p>
     * Para 7 Major version
     * <p>
     * Para 8 Node flags
     * <p>
     * Para 9 Processor type
     * <p>
     * Para 10 Bus type
     * <p>
     * Para 11-14 load address, 4 bytes
     * <p>
     * Para 15-18 CPU manufacturer's id as read from the chip config space, 4 bytes
     * <p>
     * Para 19 CPU manufacturer code
     * <p>
     * Para 20 Beta revision (numeric), or 0 if release
     * 
     * @param newparams set the node parameters
     *                
     */
    public void setParameters( int[] newparams ) {
        
        //  log.warn("new params {}",newparams);
        _parameters = new int [(newparams[0]+1)];
        for (int i = 0; i < _parameters.length; i++) {
            setParameter(i,newparams[i]);
        }
        
        if ( getParameter(6) > -1 ) {
            int [] myarray = new int[(getParameter(6)+1)]; // +1 to account for index 0 being the NV count
            java.util.Arrays.fill(myarray, -1);
            myarray[0] = getParameter(6);
            setNVs(myarray);
        }
        
    }
    
    /**
     * 
     *
     */
    public void setParameter( int index, int newval ) {
        if ( _parameters == null ){
            return;
        }
        log.debug("set parameter tot:{} index:{} newval:{}",_parameters.length,index,newval);
        if ( index <= _parameters.length ) {
        
            _parameters[index] = newval;
            //      log.info("set ok to {}",newval);
      
            notifyModelIfExists(CbusNodeTableDataModel.NODE_TYPE_NAME_COLUMN);
            notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
            notifyModelIfExists(CbusNodeTableDataModel.BYTES_REMAINING_COLUMN);
            
        }
    }

    /**
     * 
     *
     */
    public int getOutstandingParams(){
        
        if (_parameters == null){
            return 8; // CBUS Spec minimum 8 parameters, likely value 20
        }
        
        int count = 0;
        for (int i = 1; i < _parameters.length; i++) {
            if ( _parameters[i] == -1 ) {
                count++;
            }
        }
        return count;
    }
    
    protected void sendRequestNextParam(){
        
        if ( _parameters == null ) {
            requestParam(0);
            return;
        }
        if ( getParameter(1) < 0 ) {
          //  log.info("requesting param 1");
            requestParam(1);
            return;
        }
        if ( getParameter(3) < 0 ) {
        //      log.info("requesting param 3");
            requestParam(3);
            return;
        }
        
        if ( ( getCsNum() > -1 ) && ( _commandStationIdentified == false ) ) {
            // notify command station located
            log.info("{}",getNodeTypeString() );
            _commandStationIdentified = true;
        }
        // initialise NV's
        if ( getParameter(6) < 0 ) {
        //     log.info("requesting param 6");
            requestParam(6);
            return;
        }
        
        // get number event variables per event
        if ( getParameter(5) < 0 ) {
        //     log.info("requesting param 5");
            requestParam(5);
            return;
        }        
        
        // get firmware pt1
        if ( getParameter(7) < 0 ) {
        //     log.info("requesting param 7");
            requestParam(7);
            return;
        }
        
        // get firmware pt2
        if ( getParameter(2) < 0 ) {
        //     log.info("requesting param 2");
            requestParam(2);
            return;
        }

        // set node traits, eg CANPAN v1 send wrack on nv set, CANCMD v4 numevents 0
        // only do this once
        if (!nodeTraitsSet) {
            CbusNodeConstants.setTraits(this);
            nodeTraitsSet = true;
        }
        
        // now traits are known request num. of events
        if ( getTotalNodeEvents()<0 ){
            setNumEvTimeout();
            send.rQEVN( getNodeNumber() );
            return;
        }
        
        for (int i = 1; i < _parameters.length; i++) {
            if ( _parameters[i] == -1 ) {
                requestParam(i);
                return;
            }
        }
        
    }

    // total bytes to transfer all data currently on module
    // if still awaiting parameters returns -1
    public int totalNodeBytes() {
        if ( ( getParameter(0) < 0 ) 
            || ( getParameter(6) < 0 ) 
            || ( getParameter(5) < 0 )
            || ( getTotalNodeEvents() < 0 ) ){
                return -1;
            }
        return getParameter(0) + /* Total Parameters */
            getParameter(6) + /* Total NV's */
            ( getParameter(5) * getTotalNodeEvents() ) + /* Ev Variables for All Events */
            ( getTotalNodeEvents()  ); /* Events from index return */
    }
    
    public int totalRemainingNodeBytes(){
        if ( ( getOutstandingParams() < 0 ) 
            || ( getOutstandingNvCount() < 0 ) 
            || ( getOutstandingEvVars() < 0 )
            || ( getOutstandingIndexNodeEvents() < 0 ) ){
            return -1;
        }
        
        return getOutstandingParams() + /* Total Parameters */
        getOutstandingNvCount() + /* Total NV's */
        getOutstandingEvVars() + 
        ( getOutstandingIndexNodeEvents() ); /* Events from index return */
        
    }
    
    // returns value from 0 to 1 to update progress
    // with full node fetch, using remaining / total data bytes on node
    public float floatPercentageRemaining(){
        float remain = ( 1.0f * ( totalNodeBytes() - totalRemainingNodeBytes() ) ) / ( totalNodeBytes() );
        if ( remain > 0 && remain < 1.000001 ) {
            return remain;
        }
        return 0.0f;
    }
    
    private void notifyModelIfExists ( int col) {
            if ( tableModel != null ) {
                tableModel.updateFromNode( getNodeNumber(), col);
            }
    }
    
    private void notifyNodeEventTable( int arrayID, int col ) {
        log.debug("node notifys event model {} arrayID {} col {}",nodeEventTableModel,arrayID,col);
        if (nodeEventTableModel != null) {
            nodeEventTableModel.updateFromNode ( arrayID, col );
        }
    }
    
    private void notifyNodeNvTable( int row, int col ) {
        log.debug("node notifys nv model {} nv edit model {} arrayID {} col {}",
            nodeNVTableModel,nodeEditNVTableModel,row,col);
        if (nodeNVTableModel != null) {
            nodeNVTableModel.updateFromNode ( row, col );
        }
        
        if (nodeEditNVTableModel != null) {
            nodeEditNVTableModel.updateFromNode ( row, col );
        }
        
    }
    
    public int getParameter(int index) {
        if ( _parameters == null ) {
            return -1;
        }
        return _parameters[index];
    }

    public int[] getParameters() {
        return _parameters;
    }
    
    // // <Major rev><Minor rev><Build no.>
    // used to hold these details if parameters not initialised
    public void setFW( int fwMaj, int fwMin, int fwBuild ){
        _fwMaj = fwMaj;
        _fwMin = fwMin;
        _fwBuild = fwBuild;
    }
    
    // temporary store while initialising parameters
    protected void setManuModule(int manu, int modtype){
        _manu = manu;
        _type = modtype;
    }
    
    private int[] tempSetupParams;
    
    // temporary store while initialising parameters
    public void setParamsFromSetup(int[] setupParams) {
        
        log.debug("setup parameters received {}",setupParams);
        tempSetupParams = setupParams;
        
    }
    
    
    // 0th NV is total NVs
    // so length of newnvs should already be num. of NV's +1
    public void setNVs( int[] newnvs ) {
        
        log.debug(" setNVs newnvs arr length {} ",newnvs.length);
        
        _nvArray = new int [(newnvs.length)]; // no need to compensate for index 0 being total
        for (int i = 0; i < newnvs.length; i++) {
            setNV(i,newnvs[i]);
        }
        notifyModelIfExists(CbusNodeTableDataModel.BYTES_REMAINING_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
    }
    
    public void setNV( int index, int newnv ) {
        
        if ( _nvArray == null ){
            return;
        }
        _nvArray[index]=newnv;
        notifyNodeNvTable(( index -1),CbusNodeNVTableDataModel.NV_CURRENT_VAL_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.BYTES_REMAINING_COLUMN);
        
    }
    
    public int[] getNvArray() {
        return _nvArray;
    }
    
    /**
     * Number of Node Variables on the node.
     * @return 0 if number of NV's unknown, else number of NV's.
     *
     */
    public int getTotalNVs() {
        if ( _nvArray==null){
            return 0;
        }
        return _nvArray[0];
    }
    
    /**
     * Get a specific Node Variable
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
     * Number of unknown Node Variables, ie not yet fetched from a physical node.
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
    
    // expected response NVANS
    protected void sendNextNVToFetch(){
        
        if ( hasActiveTimers() ) {
            return;
        }
        
        for (int i = 0; i < _nvArray.length; i++) {
            if ( _nvArray[i] < 0 ) {
                // start NV request timer
                
                setNextNvVarTimeout();
                send.nVRD( getNodeNumber(), i );
                return;
            }
        }
    }
    
    
    private CbusNodeEditNVarFrame nvEditFrame; // for 
    private CbusNodeRestoreFcuFrame nvEditFcuFrame;
    int[] newNvsToTeach;
    int nextNvInLoop;
    Boolean TEACH_OUTSTANDING_NVS = false;
    int sendNVErrorCount;
    
    /**
     * Send and teach updated Node Variables to this node
     *
     * @param newnv array of variables, index 0 i the array is total variables
     * @param nVframe CbusNodeEditNVarFrame for feedback, can be null
     * @param fcuFrame for feedback, can be null
     */
    public void sendNvsToNode( int[] newnv , CbusNodeEditNVarFrame nVframe , CbusNodeRestoreFcuFrame fcuFrame ) {
        
      //  log.info("start loop to send nv's , nv 1 is {}",newnv[1]);
        
        nvEditFrame = nVframe; // may be null
        nvEditFcuFrame = fcuFrame; // may be null
        newNvsToTeach = newnv;
        nextNvInLoop = 1; // start from 1 not 0 as 0 is the total num. nv's
        TEACH_OUTSTANDING_NVS = true;
        sendNVErrorCount = 0 ;
        
        // check length of new array
      //  log.info("array size {}",newNvsToTeach.length);
        
        sendNextNvToNode();
        
    }
    
    private void sendNextNvToNode() {
        
        for (int i = nextNvInLoop; i < _nvArray.length; i++) {
            if ( newNvsToTeach[i] != _nvArray[i] ) {
                setsendEditNvTimeout();
                send.nVSET( getNodeNumber() ,i, newNvsToTeach[i] );
                nextNvInLoop = i;
                return;
            }
        }
        
        log.info( Bundle.getMessage("NdCompleteNVar", String.valueOf(sendNVErrorCount) , getNodeNumberName() ) );
        TEACH_OUTSTANDING_NVS = false;
        
        if ( nvEditFrame != null ){
            nvEditFrame.nVTeachComplete(sendNVErrorCount);
            nvEditFrame = null;
        }
        
        if ( nvEditFcuFrame != null ){
            nvEditFcuFrame.nVTeachComplete(sendNVErrorCount);
            nvEditFcuFrame = null;
        }
        
        // refresh all nvs from node if error
        if ( sendNVErrorCount > 0 ){ // user notified in nvEditFrame
            
            int [] myarray = new int[(getParameter(6)+1)]; // +1 to account for index 0 being the NV count
            java.util.Arrays.fill(myarray, -1);
            myarray[0] = getParameter(6);
            setNVs(myarray);
            
            tableModel.startUrgentFetch();
            
        }
    }

    
    protected boolean isEventIndexValid(){
        return _eventIndexValid;
    }
    
    private void setEvIndexValid( boolean newval ) {
        _eventIndexValid = newval;
        if (!newval){ // event index no longer valid so clear values in individual events
            for (int i = 0; i < _nodeEvents.size(); i++) {
                _nodeEvents.get(i).setIndex(-1);
            }
        }
        notifyModelIfExists(CbusNodeTableDataModel.NODE_EVENT_INDEX_VALID_COLUMN);
        notifyNodeEventTable(-1,CbusNodeEventTableDataModel.EV_INDEX_COLUMN);
    }
    
    private CbusNodeEditEventFrame evEditFrame;
    private CbusNodeRestoreFcuFrame evEditFcuFrame;
    private ArrayList<CbusNodeEvent> eventsToTeachArray;
    private int nextEvInArray;
    private int nextEvVar;
    private Boolean TEACH_OUTSTANDING_EVS = false;
    private int sendEvErrorCount;
    
    /**
     * Send and teach updated Events to this node
     *
     * @param evArray array of CbusNodeEvent to be taught
     * @param frame CbusNodeEditEventFrame for feedback, can be null
     * @param fcuFrame for feedback, can be null
     */
    public void sendNewEvSToNode( ArrayList<CbusNodeEvent> evArray, 
        CbusNodeEditEventFrame frame, 
        CbusNodeRestoreFcuFrame fcuFrame ){
        
      //  log.info("send new events to node");
        
        evEditFrame = frame;
        evEditFcuFrame = fcuFrame; // may be null
        eventsToTeachArray = evArray;
        
        // check other nodes in learn mode
        if ( tableModel.getAnyNodeInLearnMode() > -1 ) {
            log.warn("Cancelling teach events, a node is already in learn mode");
            evEditFrame.notifyLearnEvoutcome(-1,"Node " + tableModel.getAnyNodeInLearnMode() + " is already in Learn Mode" );
            return;
        }
        
        TEACH_OUTSTANDING_EVS = true;
        nextEvInArray = 0;
        nextEvVar = 1; // start at 1 as 0 is used for total ev vars
        sendEvErrorCount = 0;
        
        send.nodeEnterLearnEvMode( getNodeNumber() ); // no response expected but we add a mini delay for other traffic
        
        ThreadingUtil.runOnLayoutDelayed( () -> {
            teachNewEvLoop();
        }, 50 );
        
    }
    
    /**
     * Send a message to delete an event stored on this node
     *
     * @param nn event node number
     * @param en event event number
     * @param frame CbusNodeEditEventFrame for feedback, can be null
     */
    public void deleteEvOnNode( int nn, int en, CbusNodeEditEventFrame frame ){
        
        evEditFrame = frame;
        
        // check other nodes in learn mode
        if ( tableModel.getAnyNodeInLearnMode() > -1 ) {
            log.warn("Cancelling delete event, a node is already in learn mode");
            evEditFrame.notifyDeleteEvoutcome(-1,"Node " + tableModel.getAnyNodeInLearnMode() + " is already in Learn Mode" );
            return;
        }
        
        send.nodeEnterLearnEvMode( getNodeNumber() ); 
        // no response expected but we add a mini delay for other traffic
        ThreadingUtil.runOnLayoutDelayed( () -> {
            send.nodeUnlearnEvent( nn, en );
            setEvIndexValid(false);
        }, 50 );
        ThreadingUtil.runOnGUIDelayed( () -> {
            send.nodeExitLearnEvMode( getNodeNumber() );
            // notify ui
            if ( evEditFrame != null ) {
                evEditFrame.notifyDeleteEvoutcome(1,"Delete event on node sent" );
            }
            return;
            
        }, 100 );
    }

    private void teachNewEvLoop(){
        
        if ( nextEvVar > getParameter(5) ) {
     //       log.info("Next Event in Array");
            nextEvVar = 1;
            nextEvInArray++;
        }
        
        if ( nextEvInArray >= eventsToTeachArray.size() ) {
            log.debug("all done");
            
            TEACH_OUTSTANDING_EVS = false;
            send.nodeExitLearnEvMode( getNodeNumber() );
            if ( sendEvErrorCount==0 ) {
                log.info("Completed Event Write with No errors, node {}.", getNodeNumberName() );
            }
            else {
                log.warn("Aborted Event Write with errors, node {}.", getNodeNumberName() );
            }
            // notify ui's
            if ( evEditFrame != null ) {
                ThreadingUtil.runOnGUIDelayed( () -> {
                    evEditFrame.notifyLearnEvoutcome(1,"Node completes teaching events with " + sendEvErrorCount + " errors" );
                    evEditFrame = null;
                },50 );
            }
            if ( evEditFcuFrame != null ) {
                ThreadingUtil.runOnGUIDelayed( () -> {
                    evEditFcuFrame.teachEventsComplete(sendEvErrorCount);
                    evEditFcuFrame = null;
                },50 );
            }
            
            return;
            
        }
        
        CbusNodeEvent wholeEvent = eventsToTeachArray.get(nextEvInArray);
        
        log.debug("teach event var {}  val {} ",nextEvVar,wholeEvent.getEvVar(nextEvVar));
        CbusNodeEvent existingEvent = getNodeEvent( wholeEvent.getNn(), wholeEvent.getEn() );
        
        // increment and restart loop if no change
        if ((existingEvent!=null) && (existingEvent.getEvVar(nextEvVar)==wholeEvent.getEvVar(nextEvVar))){
            nextEvVar++;
            teachNewEvLoop();
            return;
        }
        
        // start timeout , send message, increment for next run, only sent when WRACK received
        setsendEditEvTimeout();
        send.nodeTeachEventLearnMode(wholeEvent.getNn(),wholeEvent.getEn(),nextEvVar,wholeEvent.getEvVar(nextEvVar));
        nextEvVar++;
        
    }
    
    /**
     * Resets Node Events with blank array
     *
     */
    public void resetNodeEvents() {
        _nodeEvents = null;
        _nodeEvents = new ArrayList<CbusNodeEvent>();
        notifyModelIfExists(CbusNodeTableDataModel.NODE_EVENTS_COLUMN);
    }
    
    /**
     * Resets NV's, Events and Parameters
     *
     */
    protected void resetNodeAll() {
        _nvArray = null;
        _nodeEvents = null;
        _parameters = null;
        numEvTimeoutCount = 0;
        paramRequestTimeoutCount = 0;
        allEvTimeoutCount = 0;
        nodeTraitsSet = false;
        notifyModelIfExists(CbusNodeTableDataModel.NODE_EVENTS_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.NODE_TYPE_NAME_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.CANID_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.BYTES_REMAINING_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
    }
    
    public int getTotalNodeEvents(){
        if (_nodeEvents == null) {
            return -1;
        }
        return _nodeEvents.size();
    }
    
    /**
     * Returns outstanding events from initial event fetch.
     *
     */    
    public int getLoadedNodeEvents(){
        if (_nodeEvents == null) {
            return -1;
        }
        int count = 0;
        for (int i = 0; i < _nodeEvents.size(); i++) {
            if ( ( _nodeEvents.get(i).getNn() != -1 ) && ( _nodeEvents.get(i).getEn() != -1 )) {
                count ++;
            }
        }
        return count;
    }
    
    /**
     * Returns outstanding events from initial event fetch.
     *
     */
    public int getOutstandingIndexNodeEvents(){
        return getTotalNodeEvents() - getLoadedNodeEvents();
    }
    
    /**
     * Add an event to the node, will not overwrite an existing event.
     *
     * @param newEvent the new event to be added
     */
    public void addNewEvent( CbusNodeEvent newEvent ) {
        
        if (_nodeEvents == null) {
            resetNodeEvents();
        }
        _nodeEvents.add(newEvent);
        notifyModelIfExists(CbusNodeTableDataModel.NODE_EVENTS_COLUMN);
        setEvIndexValid(false);
    }

    /**
     * Remove an event from the CbusNode, does not update hardware.
     *
     * @param nn the event Node Number
     * @param en the event Event Number
     */
    public void removeEvent(int nn, int en){
        _nodeEvents.remove(getNodeEvent(nn, en));
        notifyModelIfExists(CbusNodeTableDataModel.NODE_EVENTS_COLUMN);
        notifyNodeEventTable(-1,CbusNodeEventTableDataModel.EV_VARS_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
        setEvIndexValid(false);
    }

    // returns null if node / event combination not found on node
    public CbusNodeEvent getNodeEvent(int nn, int en) {
        for (int i = 0; i < _nodeEvents.size(); i++) {
            if ( ( _nodeEvents.get(i).getNn() == nn ) && ( _nodeEvents.get(i).getEn() == en )) {
                return _nodeEvents.get(i);
            }
        }
        return null;
    }
    
    // provides a CbusNodeEvent
    // New one will be created if not existing
    // adds to node event array, which will also be created if doesn't exist
    public CbusNodeEvent provideNodeEvent(int nn, int en) {
        if (_nodeEvents == null) {
            resetNodeEvents();
        }
        for (int i = 0; i < _nodeEvents.size(); i++) {
            if ( ( _nodeEvents.get(i).getNn() == nn ) && ( _nodeEvents.get(i).getEn() == en )) {
                return _nodeEvents.get(i);
            }
        }
        CbusNodeEvent newev = new CbusNodeEvent(nn, en, getNodeNumber(), -1, getParameter(5) );
        addNewEvent(newev);
        return newev;
    }    
    
    private void updateNodeFromLearn(int nn, int en, int evvarindex, int evvarval ){
        
        CbusNodeEvent nodeEv = provideNodeEvent( nn , en );
        
        nodeEv.setEvVar( evvarindex , evvarval );
        
        notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.NODE_EVENTS_COLUMN);
        
        notifyNodeEventTable(-1,CbusNodeEventTableDataModel.EV_VARS_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.BYTES_REMAINING_COLUMN);
        
    }
    
    /**
     * Get a Node event from its Index Field
     * <p>
     * This is NOT the node array index.
     *
     * @param index the Node event index, as set by a node from a NERD request
     * @return the node event, else null if the index is not located
     */
    public CbusNodeEvent getNodeEventByIndex(int index) {
        
        if ( _nodeEvents == null ){
            return null;
        }
        for (int i = 0; i < _nodeEvents.size(); i++) {
            if ( _nodeEvents.get(i).getIndex() == index ) {
                return _nodeEvents.get(i);
            }
        }
        return null;
    }
    
    /**
     * Get the Node event Array index from its Index Field
     *
     * @param index the Node event index, as set by a node from a NERD request
     * @return the array index, else -1 if event index number not found in array
     */
    protected int getEventRowFromIndex(int index ){
        for (int i = 0; i < _nodeEvents.size(); i++) {
            if ( _nodeEvents.get(i).getIndex() == index ) {
                return i;
            }
        }
        return -1;
    }

    public CbusNodeEvent getNodeEventByArrayID(int index) {
        return _nodeEvents.get(index);
    }
    
    public ArrayList<CbusNodeEvent> getEventArray(){
        return _nodeEvents;
    }
    
    // returns -1 if main node events array null
    public int getOutstandingEvVars(){
        int count = 0;
        if ( _nodeEvents == null ){
            return -1;
        }
        for (int i = 0; i < _nodeEvents.size(); i++) {
            count = count + _nodeEvents.get(i).getOutstandingVars();
        }
        return count;
    }

    private int fetchNvTimeoutCount = 0;
    private TimerTask nextNvTimerTask;
    
    private void clearNextNvVarTimeout(){
        if (nextNvTimerTask != null ) {
            nextNvTimerTask.cancel();
            nextNvTimerTask = null;
            fetchNvTimeoutCount = 0;
        }
    }
    
    private void setNextNvVarTimeout() {
        nextNvTimerTask = new TimerTask() {
            @Override
            public void run() {
                nextNvTimerTask = null;
                fetchNvTimeoutCount++;
                if ( fetchNvTimeoutCount == 1 ) {
                    log.info("NV Fetch from node {} timed out",getNodeNumber() ); // 
                }
                if ( fetchNvTimeoutCount == 10 ) {
                    log.info("Aborting NV Fetch from node {}",getNodeNumber() ); //
                    _nvArray=null;
                    setParameter(5,-1); // reset number of NV's to unknown and force refresh
                }
                
                tableModel.triggerUrgentFetch();
                
            }
        };
        TimerUtil.schedule(nextNvTimerTask, SINGLE_MESSAGE_TIMEOUT_TIME);
    }

    private int fetchEvVarTimeoutCount = 0;
    private TimerTask nextEvTimerTask;
    
    private void clearNextEvVarTimeout(){
        if (nextEvTimerTask != null ) {
            nextEvTimerTask.cancel();
            nextEvTimerTask = null;
            fetchEvVarTimeoutCount = 0;
        }
    }
    
    private void setNextEvVarTimeout() {
        nextEvTimerTask = new TimerTask() {
            @Override
            public void run() {
                nextEvTimerTask = null;
                fetchEvVarTimeoutCount++;
                if ( fetchEvVarTimeoutCount == 1 ) {
                    log.info("Event variable fetch from node {} timeout",getNodeNumberName() );
                }
                if ( fetchEvVarTimeoutCount == 10 ) {
                    log.info("Aborting Event variable fetch from node {} timeout",getNodeNumberName() );
                    resetNodeEvents();
                    fetchEvVarTimeoutCount = 0;
                }
                
                tableModel.triggerUrgentFetch();
            }
        };
        TimerUtil.schedule(nextEvTimerTask, SINGLE_MESSAGE_TIMEOUT_TIME);
    }


    private int numEvTimeoutCount = 0;
    private TimerTask numEvTimerTask;
    
    private void clearNumEvTimeout(){
        if (numEvTimerTask != null ) {
            numEvTimerTask.cancel();
            numEvTimerTask = null;
        }
        numEvTimeoutCount = 0;
    }
    
    private void setNumEvTimeout() {
        numEvTimerTask = new TimerTask() {
            @Override
            public void run() {
                numEvTimerTask = null;
                if ( getTotalNodeEvents() < 0 ) {
                    
                    numEvTimeoutCount++;
                    // the process will be re-attempted by the background fetch routine,
                    // we don't start it here to give a little bit more time for network / node to recover.
                    if ( numEvTimeoutCount == 1 ) {
                        log.info("No reponse to RQEVN ( Get Total Events ) from node {}", getNodeNumberName() );
                    }
                    if ( numEvTimeoutCount == 10 ) {
                        log.info("Aborting requests for Total Events from node {}", getNodeNumberName() );
                        resetNodeEvents();
                        numEvTimeoutCount = 0;
                    }
                }
            }
        };
        TimerUtil.schedule(numEvTimerTask, ( 5000 ) );
    }

    private int allEvTimeoutCount = 0;
    private TimerTask allEvTimerTask;
    
    private void clearAllEvTimeout(){
        if (allEvTimerTask != null ) {
            allEvTimerTask.cancel();
            allEvTimerTask = null;
        }
    }
    
    private void setAllEvTimeout() {
        allEvTimerTask = new TimerTask() {
            @Override
            public void run() {
                clearAllEvTimeout();
                if ( getOutstandingIndexNodeEvents() > 0 ) {
                    allEvTimeoutCount++;
                    // this has one of the highest chances of failing as 
                    // we could be expecting up to 255 CAN Frames in response.
                    
                    if ( allEvTimeoutCount < 10 ) {
                        log.warn("Re-attempting whole event / node / index fetch from node {}", getNodeNumberName() );
                        setAllEvTimeout();
                        send.nERD( getNodeNumber() );
                    }
                    else {
                        log.warn("Aborting whole event / node / index fetch from node {}", getNodeNumberName() );
                        resetNodeEvents();
                    }
                }
            }
        };
        TimerUtil.schedule(allEvTimerTask, ( 5000 ) );
    }
    
    private TimerTask allParamTask;
    
    private void clearAllParamTimeout(){
        if (allParamTask != null ) {
            allParamTask.cancel();
            allParamTask = null;
        }
    }
    
    private int paramRequestTimeoutCount = 0;
    
    private void setAllParamTimeout( int index) {
        clearAllParamTimeout(); // resets if timer already running
        allParamTask = new TimerTask() {
            @Override
            public void run() {
                allParamTask = null;
                if ( paramRequestTimeoutCount == 0 ) {
                    log.info("No response to parameter {} request from node {}", index ,getNodeNumberName() );
                }
                paramRequestTimeoutCount++;
                if ( paramRequestTimeoutCount == 10 ) {
                    log.info("Aborting parameter requests for node {}",getNodeNumberName() );
                    setParameters( new int[]{ 8,0,0,0,0,0,0,0,0 } );
                }
            }
        };
        TimerUtil.schedule(allParamTask, ( SINGLE_MESSAGE_TIMEOUT_TIME ) );
    }
    
    private TimerTask sendEditNvTask;
    
    private void clearsendEditNvTimeout(){
        if (sendEditNvTask != null ) {
            sendEditNvTask.cancel();
            sendEditNvTask = null;
        }
    }
    
    private void setsendEditNvTimeout() {
        sendEditNvTask = new TimerTask() {
            @Override
            public void run() {
                sendEditNvTask = null;
                //  log.info(" getsendsWRACKonNVSET {} ",getsendsWRACKonNVSET()  ); 
                if ( getsendsWRACKonNVSET() ) {
                    log.warn("teach nv timeout");
                    sendNVErrorCount++;
                }
                sendNextNvToNode();
            }
        };
        TimerUtil.schedule(sendEditNvTask, ( SINGLE_MESSAGE_TIMEOUT_TIME ) );
    }
    
    private TimerTask sendEditEvTask;
    
    private void clearsendEditEvTimeout(){
        if (sendEditEvTask != null ) {
            sendEditEvTask.cancel();
            sendEditEvTask = null;
        }
    }
    
    private void setsendEditEvTimeout() {
        sendEditEvTask = new TimerTask() {
            @Override
            public void run() {
                log.info("Late / no response from node while teaching event");
                sendEditEvTask = null;
                sendEvErrorCount++;
                
                // stop loop and take node out of learn mode
                nextEvInArray=999;
                teachNewEvLoop();
            }
        };
        TimerUtil.schedule(sendEditEvTask, ( SINGLE_MESSAGE_TIMEOUT_TIME ) );
    }
    
    private TimerTask sendEnumTask;
    
    private void clearSendEnumTimeout(){
        if (sendEnumTask != null ) {
            sendEnumTask.cancel();
            sendEnumTask = null;
        }
    }
    
    private void setsendEnumTimeout() {
        sendEnumTask = new TimerTask() {
            @Override
            public void run() {
                log.warn("Late response from node while request CAN ID Self Enumeration");
                sendEnumTask = null;
                // popup dialogue?
                
               
               
            }
        };
        TimerUtil.schedule(sendEnumTask, ( SINGLE_MESSAGE_TIMEOUT_TIME ) );
    }
    
    protected void sendNextEvVarToFetch() {
        
        // do not request if node is learn mode
        if ( tableModel.getAnyNodeInLearnMode() > -1 ) {
            return;
        }
        
        if ( hasActiveTimers() ){
            return;
        }
        
        // if events on module, get their event, node and node index
        // *** This could produce up to 255 responses per node ***
        if ( ( getTotalNodeEvents() > 0 ) && getOutstandingIndexNodeEvents()>0 ) {
            send.nERD( getNodeNumber() );
            // starts timeout 
            setAllEvTimeout();
            return;
        }
        
        for (int i = 0; i < _nodeEvents.size(); i++) {
            if ( _nodeEvents.get(i).getOutstandingVars() > 0 ) {
                int index = _nodeEvents.get(i).getIndex();
                int nextevvar = _nodeEvents.get(i).getNextOutstanding();
                
                // index from NERD / ENRSP indexing may start at 0
                if ( index > -1 ) {
                
                    // start timer
                    setNextEvVarTimeout();
                    send.rEVAL( getNodeNumber(), index, nextevvar );
                    return;
                }
                else { // if index < 0 event index is invalid so attempt refetch.
                    // reset events
                    log.info("Invalid index, resetting events for node {}", getNodeNumberName() );
                    _nodeEvents = null;
                    return;
                }
            }
        }
    }
    
    private void setEvVarByIndex(int eventIndex, int eventVarIndex, int newVal) {
        if ( getNodeEventByIndex(eventIndex) != null ) {
            getNodeEventByIndex(eventIndex).setEvVar(eventVarIndex,newVal);
            
            int tableRow = getEventRowFromIndex(eventIndex);
            
            notifyNodeEventTable(tableRow,CbusNodeEventTableDataModel.EV_VARS_COLUMN);
            notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
            notifyModelIfExists(CbusNodeTableDataModel.BYTES_REMAINING_COLUMN);
        }
    }
    
    private void setNextEmptyNodeEvent(int nn, int en, int index){
        if ( _nodeEvents == null ){
            return;
        }
        for (int i = 0; i < _nodeEvents.size(); i++) {
            if ( _nodeEvents.get(i).getIndex() == index ) {
                _nodeEvents.get(i).setNn(nn);
                _nodeEvents.get(i).setEn(en);
                notifyNodeEventTable(i,CbusNodeEventTableDataModel.EVENT_NUMBER_COLUMN);
                notifyNodeEventTable(i,CbusNodeEventTableDataModel.NODE_NUMBER_COLUMN);
                notifyNodeEventTable(i,CbusNodeEventTableDataModel.NODE_NAME_COLUMN);
                notifyNodeEventTable(i,CbusNodeEventTableDataModel.EVENT_NAME_COLUMN);
                
                notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
                notifyModelIfExists(CbusNodeTableDataModel.BYTES_REMAINING_COLUMN);
                return;
            }
        }
        
        for (int i = 0; i < _nodeEvents.size(); i++) {
            if ( ( _nodeEvents.get(i).getNn() == -1 ) && ( _nodeEvents.get(i).getEn() == -1 ) ) {
                _nodeEvents.get(i).setNn(nn);
                _nodeEvents.get(i).setEn(en);
                _nodeEvents.get(i).setIndex(index);
                notifyNodeEventTable(i,CbusNodeEventTableDataModel.EVENT_NUMBER_COLUMN);
                notifyNodeEventTable(i,CbusNodeEventTableDataModel.NODE_NUMBER_COLUMN);
                notifyNodeEventTable(i,CbusNodeEventTableDataModel.NODE_NAME_COLUMN);
                notifyNodeEventTable(i,CbusNodeEventTableDataModel.EVENT_NAME_COLUMN);
                
                
                notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
                notifyModelIfExists(CbusNodeTableDataModel.BYTES_REMAINING_COLUMN);
                return;
            }
        }
        log.error("Issue setting node event, index not valid");
        _nodeEvents = null;
    }
    
    public void setNodeInSetupMode( Boolean setup ) {
        _inSetupMode = setup;
    }
    
    public Boolean getNodeInSetupMode() {
        return _inSetupMode;
    }
    
    public void sendExitLearnMode() {
        send.nodeExitLearnEvMode( getNodeNumber() );
    }
    
    public void setNodeInLearnMode( boolean inlearnmode) {
        _inlearnMode = inlearnmode;
        notifyModelIfExists(CbusNodeTableDataModel.NODE_IN_LEARN_MODE_COLUMN);
    }
    
    public Boolean getNodeInLearnMode() {
        return _inlearnMode;
    }

    public void setNodeInFLiMMode( boolean inflimmode ) {
        _inFLiMMode = inflimmode;
    }    
    
    public Boolean getNodeInFLiMMode() {
        return _inFLiMMode;
    }
    
    public void setsendsWRACKonNVSET( Boolean sendsWRACK ){
        _sendsWRACKonNVSET = sendsWRACK;
    }
    
    public Boolean getsendsWRACKonNVSET() {
        return _sendsWRACKonNVSET;
    }
    
    public void requestParam(int param){
        
        if ( hasActiveTimers() ){
            return;
        }
        
        setAllParamTimeout(param);
        
        send.rQNPN( getNodeNumber(), param );
    }
    
    public void setCsNum( int csnum ) {
        _csNum = csnum;
    }
    
    /**
     * Returns Command station number
     * <p>
     * -1 if node is NOT a Command Station,
     * 0 is normally default for a command station
     *
     */    
    public int getCsNum() {
        return _csNum;
    }
    
    public void setStatResponseFlagsAccurate ( Boolean StatResponseFlagsAccurate) {
        _StatResponseFlagsAccurate = StatResponseFlagsAccurate;
    }
    
    
    /**
     * returns eg. MERG Command Station CANCMD Firmware 4d Node 65534
     *
     */
    public String getNodeTypeString(){
        StringBuilder n = new StringBuilder(100);
        n.append (CbusNodeConstants.getManu(getParameter(1)));
        n.append (" ");
        n.append( CbusNodeConstants.getModuleTypeExtra(getParameter(1),getParameter(3)));
        n.append(" ");
        n.append( CbusNodeConstants.getModuleType(getParameter(1),getParameter(3)));
        n.append (" ");
        n.append (Bundle.getMessage("FirmwareVer"));
        n.append (getParameter(7));
        n.append(Character.toString((char) getParameter(2) ));
        if ((getParameter(0)>19) && (getParameter(20)>0) ){
            n.append (" "); 
            n.append (Bundle.getMessage("FWBeta")); 
            n.append (getParameter(20));
        }
        n.append (" ");
        n.append (Bundle.getMessage("CbusNode"));
        n.append (getNodeNumber());
        return n.toString();
        
    }
    
    // passes outgoing message as a reply,
    // we don't know if it's this JMRI or something external teaching the node
    // so we monitor them the same
    @Override
    public void message(CanMessage m) {
        if ( m.isExtended() || m.isRtr() ) {
            return;
        }
        switch ( CbusMessage.getOpcode(m) ) {
            case CbusConstants.CBUS_NVSET:
            case CbusConstants.CBUS_NNREL:
            case CbusConstants.CBUS_NNLRN:
            case CbusConstants.CBUS_NNULN:
            case CbusConstants.CBUS_EVLRN:
            case CbusConstants.CBUS_EVULN:
            case CbusConstants.CBUS_ENUM:
            case CbusConstants.CBUS_CANID:
            case CbusConstants.CBUS_NNCLR:
                CanReply r = new CanReply(m);
                reply(r);
                break;
            default:
                break;
        }
    }
    
    // also parses outgoing messages
    @Override
    public void reply(CanReply m) {
        if ( m.isExtended() || m.isRtr() ) {
            return;
        }
        int opc = CbusMessage.getOpcode(m);
        int nn = ( m.getElement(1) * 256 ) + m.getElement(2);
        
        // if the OPC is coming from a Node, update the CAN ID field
        // if the OPC is coming from software, do NOT update the CAN ID field
        
        // if node in learn mode 
        if ( getNodeInLearnMode() ) {
            
            //   log.info("reply learn mode");
            if ( opc == CbusConstants.CBUS_NNCLR ) { // instruction to delete all node events
                if ( nn == getNodeNumber() ) {
                    resetNodeEvents();
                }
            }
            else if ( opc == CbusConstants.CBUS_EVLRN ) {
                // update node database with event
                updateNodeFromLearn(
                    nn, 
                    ( m.getElement(3) * 256 ) + m.getElement(4), 
                    m.getElement(5), 
                    m.getElement(6) );
            }
            else if ( opc == CbusConstants.CBUS_EVULN ) {
                log.debug("node hears evuln");
                removeEvent( ( m.getElement(1) * 256 ) + m.getElement(2), ( m.getElement(3) * 256 ) + m.getElement(4) );
            }
            else if ( opc == CbusConstants.CBUS_EVLRNI ) {
                // check if current index is valid
                if ( !isEventIndexValid() ){
                    log.warn("EVRLNI OPC heard while Event Index Invalid for Node {}",toString() );
                }
                else {
                    // find existing event , m.getElement(5) is event index number being edited
                    CbusNodeEvent toEdit = getNodeEventByIndex( m.getElement(5) );
                    if (toEdit == null) {
                        log.warn("No event with index {} found on node {}",m.getElement(5),toString() );
                        return;
                    }
                    else {
                        // event found with correct index number
                        toEdit.setNn( ( m.getElement(1) * 256 ) + m.getElement(2) );
                        toEdit.setEn( ( m.getElement(3) * 256 ) + m.getElement(4) );
                        toEdit.setEvVar( ( m.getElement(6) * 256 ), m.getElement(7) );
                    }
                }
            }
           
            if ( TEACH_OUTSTANDING_EVS ) {
                if ( opc == CbusConstants.CBUS_WRACK ) {
                    // cancel timer
                    clearsendEditEvTimeout();
                    // start next in loop
                    teachNewEvLoop();
                }
                if ( opc == CbusConstants.CBUS_CMDERR ) {
                    // cancel timer
                    clearsendEditEvTimeout();
                    sendEvErrorCount++;
                    // start next in loop
                    teachNewEvLoop();
                }
            }
        }
        
        if (nn != getNodeNumber() ) {
            return;
        }

        if ( opc == CbusConstants.CBUS_CMDERR ) { // response from node with an error message
            setCanId(CbusMessage.getId(m));
            
            // if in middle of a learn process we do not re-kick the node here,
            // as it may be another software sending the learn.
            // If it is JMRI doing the learn, the timer for the learn will
            // sort out any abort / resume logic.
            
            if ((m.getElement(3) > 0 ) && (m.getElement(3) < 13 )) {
                log.error("Node {}Reporting {}",toString(),Bundle.getMessage("CMDERR"+m.getElement(3)) );
            } else {
                log.error("Node {}Reporting Error Code {} (decimal)",toString(),m.getElement(3) );
            }
        }
        
        else if ( opc == CbusConstants.CBUS_NNACK ) { // response from node acknowledging something
            
            setCanId(CbusMessage.getId(m));
            
            if ( sendEnumTask != null ) {
                clearSendEnumTimeout();
            }
        }        
        
        else if ( opc == CbusConstants.CBUS_PARAN) { // response from node
            clearAllParamTimeout();
            setCanId(CbusMessage.getId(m));
            if (m.getElement(3)==0) { // reset parameters
                
                int [] myarray = new int[(m.getElement(4)+1)]; // +1 to account for index 0 being the parameter count
                java.util.Arrays.fill(myarray, -1);
                // node may already be aware of some params via the initial PNN or STAT
                
                myarray[1] = _manu;
                myarray[2] = _fwMin;
                myarray[3] = _type;
                myarray[7] = _fwMaj;
                
                if ( tempSetupParams !=null ) {
                    
                    log.debug("tempSetupParams {}",tempSetupParams);
                    
                    myarray[1] = tempSetupParams[0];
                    myarray[2] = tempSetupParams[1];
                    myarray[3] = tempSetupParams[2];
                    myarray[4] = tempSetupParams[3];
                    myarray[5] = tempSetupParams[4];
                    myarray[6] = tempSetupParams[5];
                    myarray[7] = tempSetupParams[6];
                    
                    // reset NV array
                    if ( myarray[6] > -1 ){
                        
                        int [] myParray = new int[(myarray[6]+1)]; // +1 to account for index 0 being the NV count
                        java.util.Arrays.fill(myParray, -1);
                        myParray[0] = myarray[6];
                        setNVs(myParray);
                    }
                }
                
                myarray[0] = m.getElement(4);
                
                // log.info("parameter 0 is {}",myarray[0]);
                setParameters(myarray);
                
                // setting them via setParameter to avoid nulls if number of parameters is v low
                // most modules report up to 20, but some may not.
                setParameter( 20, _fwBuild );
                
            } else {
                setParameter( m.getElement(3), m.getElement(4) );
                if ( m.getElement(3) == 6 ) { // reset NV's
                    int [] myarray = new int[(m.getElement(4)+1)]; // +1 to account for index 0 being the NV count
                    java.util.Arrays.fill(myarray, -1);
                    myarray[0] = m.getElement(4);
                    setNVs(myarray);
                }
            }
        }
        else if ( opc == CbusConstants.CBUS_NUMEV) { // response from node
            
            setCanId(CbusMessage.getId(m));
            int newEventsOnNode = m.getElement(3);
            resetNodeEvents();
            clearNumEvTimeout();
            
            if ( getParameter(5)<0 ){
                return;
            }
            
            for (int i = 0; i < newEventsOnNode; i++) {
                CbusNodeEvent newev = new CbusNodeEvent(-1, -1, getNodeNumber(), -1, getParameter(5) );
                // (int nn, int en, int thisnode, int index, int maxEvVar);
                addNewEvent(newev);
            }
            
            notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
            notifyModelIfExists(CbusNodeTableDataModel.BYTES_REMAINING_COLUMN);
            notifyModelIfExists(CbusNodeTableDataModel.NODE_EVENTS_COLUMN);
        }
        
        else if ( opc == CbusConstants.CBUS_ENRSP ) { // response from node with a stored event, node + index
            setCanId(CbusMessage.getId(m));
            int evnode = ( m.getElement(3) * 256 ) + m.getElement(4);
            int evev = ( m.getElement(5) * 256 ) + m.getElement(6);
            
            // get next node event which is empty
            setNextEmptyNodeEvent(evnode,evev,m.getElement(7));
            
            if ( ( allEvTimerTask !=null ) && ( getOutstandingIndexNodeEvents() == 0 ) ) {
                // all events returned ok, this is the only 
                // point ANYWHERE that the event index is set valid
                clearAllEvTimeout();
                setEvIndexValid(true);
            }
        }
        
        else if ( opc == CbusConstants.CBUS_NEVAL ) { // response from node with event variable
        
            clearNextEvVarTimeout();
            setCanId(CbusMessage.getId(m));
            int eventIndex = m.getElement(3);
            int eventVarIndex =m.getElement(4);
            int newVal = m.getElement(5);
          //  log.info("event index {} var {} newval {}",eventIndex,eventVarIndex,newVal);
            
            setEvVarByIndex(eventIndex,eventVarIndex,newVal);
            
        }
        
        else if ( opc == CbusConstants.CBUS_NVANS ) { // response from node with node variable
            
            // stop timer
            clearNextNvVarTimeout();
            setCanId(CbusMessage.getId(m));
            
            setNV(m.getElement(3),m.getElement(4));
        }
        
        else if ( opc == CbusConstants.CBUS_NVSET ) { // sent from software
            setNV(m.getElement(3),m.getElement(4));
        }
        
        else if ( opc == CbusConstants.CBUS_NNLRN ) { // sent from software
            setNodeInLearnMode(true);
        }
        
        else if ( opc == CbusConstants.CBUS_NNULN ) { // sent from software
            setNodeInLearnMode(false);
        }
        
        else if ( opc == CbusConstants.CBUS_ENUM ) { // sent from software
            setCanId(-1);
            // now waiting for a NNACK confirmation or error message 7
            // start a timer waiting for the response
            setsendEnumTimeout();
        }
        
        else if ( opc == CbusConstants.CBUS_CANID ) { // sent from software
            setCanId(-1);
            // no response expected from node ( ? )
        }
        else {
            // ignoring OPC
        }
        
        if ( TEACH_OUTSTANDING_NVS ) {
        
            if ( opc == CbusConstants.CBUS_WRACK ) { // response from node
                clearsendEditNvTimeout();
                sendNextNvToNode();
            }

            if ( opc == CbusConstants.CBUS_CMDERR ) { // response from node
                sendNVErrorCount++;
                log.warn("Node reports NV Write Error");
                clearsendEditNvTimeout();
                sendNextNvToNode();
            }
        }
        
        if ( tableModel != null ) {
            tableModel.triggerUrgentFetch(); // 
        }
        
    }
    
    @Override
    public String toString(){
        return getNodeNumberName();
    }
    
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
        
        if ( ( ( flags >> 0 ) & 1 ) == 1 ){
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
    
    protected boolean hasActiveTimers(){
        
        if ( 
            allParamTask != null
            || allEvTimerTask != null
            || nextEvTimerTask != null
            || nextNvTimerTask != null
            || sendEnumTask != null
            || sendEditEvTask != null
            || sendEditNvTask != null
            || numEvTimerTask != null
        ) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Stops any timers and disconnects from network
     *
     */
    public void dispose(){
        
        // stop any timers running
        clearSendEnumTimeout();
        clearsendEditEvTimeout();
        clearsendEditNvTimeout();
        clearAllParamTimeout();
        clearAllEvTimeout();
        clearNextEvVarTimeout();
        clearNextNvVarTimeout();
        clearNumEvTimeout();
        
        tc.removeCanListener(this);
    }
    
    private static final Logger log = LoggerFactory.getLogger(CbusNode.class);
    
}
