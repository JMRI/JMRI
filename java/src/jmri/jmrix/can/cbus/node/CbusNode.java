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
    private String _userComment;
    private ArrayList<CbusNodeEvent> _nodeEvents;//  _ndEv;
    private int _canId;
    public int[] _nvArray;
    public int[] _parameters;
    private int _flags;
    private int _fwMaj;
    private int _fwMin;
    private int _fwBuild;
    private int _manu;
    private int _type;
    private Boolean _inSetupMode;
    private Boolean _inlearnMode;
    private Boolean _inFLiMMode;
    public Boolean _startupDataNeeded;
    private Boolean _sendsWRACKonNVSET;
    public CbusSend send;
    private CbusNodeTableDataModel tableModel;
    private CbusNodeEventTableDataModel nodeEventTableModel;
    private CbusNodeNVTableDataModel nodeNVTableModel;
    private CbusNodeNVTableDataModel nodeEditNVTableModel;
    
    private int _csNum;
    // private CsType _csIdentified;
    private Boolean _StatResponseFlagsAccurate;
    private Boolean _commandStationIdentified;
    private int _identifyRunaway;
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
        _userComment = "";
        setFW( -1, -1, -1);
        _parameters = null;
        _canId = -1;
        _nodeEvents = null;
        _inSetupMode = false;
        _inlearnMode = false;
        _inFLiMMode = true;
        _startupDataNeeded = false;
        _sendsWRACKonNVSET = true;
        _csNum = -1;
        // _csIdentified = CsType.UNKNOWN;
        _StatResponseFlagsAccurate=false;
        _commandStationIdentified = false;
        _identifyRunaway=0;
        _manu = -1;
        _type = -1;
        
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
    
    public String getNodeTypeName() {
        try {
            return CbusNodeConstants.getModuleType(getParameter(1),getParameter(3));
        } catch (NullPointerException e) {
            return("");
        }
    }

    public int getNodeNumber() {
        return _nodeNumber;
    }
    
    public String getUserName() {
        return _nodeUserName;
    }
    
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
    
    public void setUserComment( String comment ) {
        _userComment = comment;
    }
    
    public String getUserComment() {
        return _userComment;
    }
    
    public int getManufacturer() {
        return getParameter(1);
    }
    
    public void setNodeNumber ( int newnumber ) {
        _nodeNumber = newnumber;
    }

    public void setCanId ( int newcanid ) {
        _canId = newcanid;
        notifyModelIfExists(CbusNodeTableDataModel.CANID_COLUMN);
    }
    
    public int getNodeCanId() {
        return _canId;
    }
    
    public void setNodeFlags(int flags) {
        _flags = flags;
    }
    
    public int getNodeFlags() {
        return _flags;
    }
    
    /**
     * Node Parameters
     *
     * Para 0 Number of parameters
     * Para 1 The manufacturer ID
     * Para 2 Minor code version as an alphabetic character (ASCII)
     * Para 3 Manufacturer module identifier as a HEX numeric
     * Para 4 Number of supported events as a HEX numeric
     * Para 5 Number of Event Variables per event as a HEX numeric
     * Para 6 Number of supported Node Variables as a HEX numeric
     * Para 7 Major version
     * Para 8 Node flags
     * Para 9 Processor type
     * Para 10 Bus type
     * Para 11 load address, 4 bytes
     * Para 15 CPU manufacturer's id as read from the chip config space, 4 bytes
     * Para 19 CPU manufacturer code
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
    }
    
    /**
     * Send messages to get the minimum basic node details
     *
     */
    public void setParameter( int index, int newval ) {
        log.debug("set parameter tot:{} index:{} newval:{}",_parameters.length,index,newval);
        if ( index <= _parameters.length ) {
        
            _parameters[index] = newval;
            //      log.info("set ok to {}",newval);
      
            notifyModelIfExists(CbusNodeTableDataModel.NODE_TYPE_NAME_COLUMN);
          //  notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
            notifyModelIfExists(CbusNodeTableDataModel.BYTES_REMAINING_COLUMN);
            
        }
    }

    public int getOutstandingParams(){
        
        if (_parameters == null){
            return 1;
        }
        
        int count = 0;
        for (int i = 1; i < _parameters.length; i++) {
            if ( _parameters[i] == -1 ) {
                count++;
            }
        }
        return count;
    }
    
    public void sendRequestNextParam(){
        if ( _parameters != null ) {
            for (int i = 1; i < _parameters.length; i++) {
                if ( _parameters[i] == -1 ) {
                    requestParam(i);
                    return;
                }
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
            ( getTotalNodeEvents() * 5 ); /* Events from index return */

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
        ( getOutstandingIndexNodeEvents() * 5 ); /* Events from index return */
        
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
        if (nodeEventTableModel != null) {
            nodeEventTableModel.updateFromNode ( arrayID, col );
        }
    }
    
    private void notifyNodeNvTable( int row, int col ) {
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
    public void setManuModule(int manu, int modtype){
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
        
        notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
    }
    
    public void setNV( int index, int newnv ) {
        _nvArray[index]=newnv;
        notifyNodeNvTable(( index -1),CbusNodeNVTableDataModel.NV_CURRENT_VAL_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.BYTES_REMAINING_COLUMN);
        
    }
    
    public int[] getNvArray() {
        return _nvArray;
    }
    
    public int getTotalNVs() {
      //  log.info("getting total nvs");
        
        if ( _nvArray==null){
            log.trace("array null");
            return 0;
        }
      //  log.info("returning nv array 0 val {}", _nvArray[0] );
        return _nvArray[0];
    }
    
    public int getNV ( int index ) {
        if ( _nvArray==null){
            return -1;
        }
        return _nvArray[index];
    }
    
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
        
        if ( nextNvTimerTask != null ) {
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
        
        log.info("Completed NV Write with {} error(s), node {}.", sendNVErrorCount , getNodeNumberName() );
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
        
        if ( nextEvInArray == eventsToTeachArray.size() ) {
            log.debug("all done");
            
            TEACH_OUTSTANDING_EVS = false;
            send.nodeExitLearnEvMode( getNodeNumber() );
            
            log.info("Completed Event Write with {} error(s), node {}.", sendEvErrorCount , getNodeNumberName() );
            
            // notify ui's
            if ( evEditFrame != null ) {
                ThreadingUtil.runOnGUIDelayed( () -> {
                    evEditFrame.notifyLearnEvoutcome(1,"Node completes teaching events with " + sendEvErrorCount + " errors" );
                    evEditFrame = null;
                },50 );
            }
            if ( evEditFcuFrame != null ) {
                ThreadingUtil.runOnGUIDelayed( () -> {
                    // nvEditFcuFrame.notifyLearnEvoutcome(1,"Node completes teaching events with " + sendEvErrorCount + " errors" );
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
    
    public void resetNodeEvents() {
        _nodeEvents = null;
        _nodeEvents = new ArrayList<CbusNodeEvent>();
        notifyModelIfExists(CbusNodeTableDataModel.NODE_EVENTS_COLUMN);
    }
    
    public int getTotalNodeEvents(){
        if (_nodeEvents == null) {
            return -1;
        }
        return _nodeEvents.size();
    }
    
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
        _nodeEvents.add(newEvent);
        notifyModelIfExists(CbusNodeTableDataModel.NODE_EVENTS_COLUMN);
    }
    
    public void removeEvent(int nn, int en){
        _nodeEvents.remove(getNodeEvent(nn, en));
        notifyModelIfExists(CbusNodeTableDataModel.NODE_EVENTS_COLUMN);
        notifyNodeEventTable(-1,CbusNodeEventTableDataModel.EV_VARS_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
        
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
    
    public CbusNodeEvent provideNodeEvent(int nn, int en) {
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
        
      //  log.info("updating node table from learn");
        
        CbusNodeEvent nodeEv = provideNodeEvent( nn , en );
        
        nodeEv.setEvVar( evvarindex , evvarval );
        
        notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.NODE_EVENTS_COLUMN);
        
        notifyNodeEventTable(-1,CbusNodeEventTableDataModel.EV_VARS_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.BYTES_REMAINING_COLUMN);
        
    }
    

    // do not use in module table?? , only for use by node simulator
    public CbusNodeEvent getNodeEventByIndex(int index) {
        for (int i = 0; i < _nodeEvents.size(); i++) {
            if ( _nodeEvents.get(i).getIndex() == index ) {
                return _nodeEvents.get(i);
            }
        }
        return null;
    }
    
    public int getRowFromIndex(int index ){
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
    
    public int getOutstandingEvVars(){
        int count = 0;
        if ( _nodeEvents == null ){
            return 0;
        }
        for (int i = 0; i < _nodeEvents.size(); i++) {
            count = count + _nodeEvents.get(i).getOutstandingVars();
        }
        return count;
    }

    private TimerTask nextNvTimerTask;
    
    private void clearNextNvVarTimeout(){
        if (nextNvTimerTask != null ) {
            nextNvTimerTask.cancel();
            nextNvTimerTask = null;
        }
    }
    
    private void setNextNvVarTimeout() {
        nextNvTimerTask = new TimerTask() {
            @Override
            public void run() {
                nextNvTimerTask = null;
                log.info("NV Fetch from node {} timeout",getNodeNumber() ); // 
                tableModel.triggerUrgentFetch();
            }
        };
        TimerUtil.schedule(nextNvTimerTask, SINGLE_MESSAGE_TIMEOUT_TIME);
    }

    private TimerTask nextEvTimerTask;
    
    private void clearNextEvVarTimeout(){
        if (nextEvTimerTask != null ) {
            nextEvTimerTask.cancel();
            nextEvTimerTask = null;
        }
    }
    
    private void setNextEvVarTimeout() {
        nextEvTimerTask = new TimerTask() {
            @Override
            public void run() {
                nextEvTimerTask = null;
                log.debug("resuming event variable fetch from node {}",getNodeNumber() );
                tableModel.triggerUrgentFetch();
            }
        };
        TimerUtil.schedule(nextEvTimerTask, SINGLE_MESSAGE_TIMEOUT_TIME);
    }


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
                // allEvTimerTask = null;
                clearAllEvTimeout();
                if ( getOutstandingIndexNodeEvents() > 0 ) {
                    log.info("Re-attempting whole event / node / index fetch from node {}", getNodeNumber() );
                    
                    // send NERD
                    send.nERD( getNodeNumber() );
                    // starts timeout 
                    setAllEvTimeout();
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
    
    private void setAllParamTimeout( int index) {
        clearAllParamTimeout(); // resets if timer already running
        allParamTask = new TimerTask() {
            @Override
            public void run() {
                allParamTask = null;
                
              //  int outstanding = getOutstandingParams();
              //  log.info("in timeout, waiting for {} events from node {}",outstanding,getNodeNumber() );

                log.info("no response to parameter {} request from node {}", index ,getNodeNumber() );
                    
                
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
                log.info("Late response from node while teaching event");
                sendEditEvTask = null;
                sendEvErrorCount++;
               // teachNewEvLoop();
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
        
        for (int i = 0; i < _nodeEvents.size(); i++) {
            if ( _nodeEvents.get(i).getOutstandingVars() > 0 ) {
                int index = _nodeEvents.get(i).getIndex();
                int nextevvar = _nodeEvents.get(i).getNextOutstanding();
                
                // start timer
                setNextEvVarTimeout();
                
                send.rEVAL( getNodeNumber(), index, nextevvar );
                
                return;
            }
        }
    }
    
    private void setEvVarByIndex(int eventIndex, int eventVarIndex, int newVal) {
        
        getNodeEventByIndex(eventIndex).setEvVar(eventVarIndex,newVal);
        
        int tableRow = getRowFromIndex(eventIndex);
        
        notifyNodeEventTable(tableRow,CbusNodeEventTableDataModel.EV_VARS_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
        notifyModelIfExists(CbusNodeTableDataModel.BYTES_REMAINING_COLUMN);
    }
    
    
    private void setNextEmptyNodeEvent(int nn, int en, int index){
        
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
        log.error("Issue setting node event");
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
    
    public void setNodeInLearnMode( Boolean inlearnmode) {
        _inlearnMode = inlearnmode;
        notifyModelIfExists(CbusNodeTableDataModel.NODE_IN_LEARN_MODE_COLUMN);
    }
    
    public Boolean getNodeInLearnMode() {
        return _inlearnMode;
    }

    public void setNodeInFLiMMode( Boolean inflimmode ) {
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
        
        if ( allParamTask != null ){
            return;
        }
        
        setAllParamTimeout(param);
        
        send.rQNPN( getNodeNumber(), param );
    }
    
    public void setCsNum( int csnum ) {
        _csNum = csnum;
    }
    
    public int getCsNum() {
        return _csNum;
    }
    
    public void setStatResponseFlagsAccurate ( Boolean StatResponseFlagsAccurate) {
        _StatResponseFlagsAccurate = StatResponseFlagsAccurate;
    }
    
    Boolean nodeTraitsSet=false;
    
    /**
     * Send messages to get the minimum basic node details
     *
     */
    public void startParamsLookup() {
        
        if ( allParamTask != null ) { // already requested a parameter
            return;
        }
        
        _startupDataNeeded=true;
        _identifyRunaway++;
        
        if ( _identifyRunaway > 40 ) {
            log.warn("Unable to Fully Capture Config Details for Node {}", getNodeNumber() );
            _startupDataNeeded = false;
            return;
        }
        
        if ( _parameters == null ) {
          //  log.info("requesting param 0");
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
        
        // get number event variables
        if ( getParameter(5) < 0 ) {
        //     log.info("requesting param 6");
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

        // get number current events ( not the max events )
        if ( getTotalNodeEvents() < 0 ) {
        //     log.info("requesting param 6");
            send.rQEVN( getNodeNumber() );
            return;
        }
        
        _startupDataNeeded=false;
        
        // if events on module, get their event, node and node index
        // This could produce up to 255 responses per node
        if ( getTotalNodeEvents() > 0 ) {
            send.nERD( getNodeNumber() );
            
            // starts timeout 
            setAllEvTimeout();
            return;
        }
        
        //  log.info ("param0:{} param1:{} param3:{} ",getParameter(0),getParameter(1),getParameter(3));

    }
    
    
    
    // eg returns MERG Command Station CANCMD Firmware 4d Node 65534
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
        int opc = CbusMessage.getOpcode(m);
        int nn = ( m.getElement(1) * 256 ) + m.getElement(2);
        
        // if the OPC is coming from a Node, update the CAN ID field
        // if the OPC is coming from software, do NOT update the CAN ID field
        
        // if node in learn mode 
        if ( getNodeInLearnMode() ) {
            
            //   log.info("reply learn mode");
            if ( opc == CbusConstants.CBUS_NNCLR ) { // instruction to delete all node events
                resetNodeEvents();
            }
            
            if ( opc == CbusConstants.CBUS_EVLRN ) {
                
                //   log.info("reply CBUS_EVLRN");
                // update node database with event
                updateNodeFromLearn(
                    nn, 
                    ( m.getElement(3) * 256 ) + m.getElement(4), 
                    m.getElement(5), 
                    m.getElement(6) );
            }
            
            if ( opc == CbusConstants.CBUS_EVULN ) {
                
                log.debug("node hears evuln");
                removeEvent( ( m.getElement(1) * 256 ) + m.getElement(2), ( m.getElement(3) * 256 ) + m.getElement(4) );

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
            log.warn("Node Reporting Error");
            
            // TODO error codes + stop timers
            
        }
        
        if ( opc == CbusConstants.CBUS_NNACK ) { // response from node acknowledging something
            
            setCanId(CbusMessage.getId(m));
            
            if ( sendEnumTask != null ) {
                clearSendEnumTimeout();
            }
        }        
        
        if ( opc == CbusConstants.CBUS_PARAN) { // response from node
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
        if ( opc == CbusConstants.CBUS_NUMEV) { // response from node
            setCanId(CbusMessage.getId(m));
            int newEventsOnNode = m.getElement(3);
         //   if  ( newEventsOnNode !=getTotalNodeEvents() ) {
                
             //   log.info("resetting node events to {}",newEventsOnNode);
                resetNodeEvents();
                for (int i = 0; i < newEventsOnNode; i++) {
                    CbusNodeEvent newev = new CbusNodeEvent(-1, -1, getNodeNumber(), -1, getParameter(5) );
                    // (int nn, int en, int thisnode, int index, int maxEvVar);
                    addNewEvent(newev);
                }
        //    }
            notifyModelIfExists(CbusNodeTableDataModel.NODE_TOTAL_BYTES_COLUMN);
            notifyModelIfExists(CbusNodeTableDataModel.BYTES_REMAINING_COLUMN);
            notifyModelIfExists(CbusNodeTableDataModel.NODE_EVENTS_COLUMN);
        }
        
        if ( opc == CbusConstants.CBUS_ENRSP ) { // response from node with a stored event, node + index
            setCanId(CbusMessage.getId(m));
            int evnode = ( m.getElement(3) * 256 ) + m.getElement(4);
            int evev = ( m.getElement(5) * 256 ) + m.getElement(6);
            int index = m.getElement(7);
            
            // get next node event which is empty
            setNextEmptyNodeEvent(evnode,evev,index);
        }
        
        if ( opc == CbusConstants.CBUS_NEVAL ) { // response from node with event variable
        
            clearNextEvVarTimeout();
            setCanId(CbusMessage.getId(m));
            int eventIndex = m.getElement(3);
            int eventVarIndex =m.getElement(4);
            int newVal = m.getElement(5);
          //  log.info("event index {} var {} newval {}",eventIndex,eventVarIndex,newVal);
            
            setEvVarByIndex(eventIndex,eventVarIndex,newVal);
            
        }
        
        if ( opc == CbusConstants.CBUS_NVANS ) { // response from node with node variable
            
            // stop timer
            clearNextNvVarTimeout();
            setCanId(CbusMessage.getId(m));
            
            setNV(m.getElement(3),m.getElement(4));
        }
        
        if ( opc == CbusConstants.CBUS_NVSET ) { // sent from software
            setNV(m.getElement(3),m.getElement(4));
        }
        
        if ( opc == CbusConstants.CBUS_NNLRN ) { // sent from software
            setNodeInLearnMode(true);
        }
        
        if ( opc == CbusConstants.CBUS_NNULN ) { // sent from software
            setNodeInLearnMode(false);
        }
        
        if ( opc == CbusConstants.CBUS_ENUM ) { // sent from software
            setCanId(-1);
            // now waiting for a NNACK confirmation or error message 7
            // start a timer waiting for the response
            setsendEnumTimeout();
        }
        
        if ( opc == CbusConstants.CBUS_CANID ) { // sent from software
            setCanId(-1);
            // no response expected from node ( ? )
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
        
        if ( _startupDataNeeded ) { // still need node type, manufacturer, num. nvs, num ev vars per event, events index
                startParamsLookup();
        } else {
            tableModel.triggerUrgentFetch(); // outstanding params, nv's, event vars 
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
    
    public void dispose(){
        if (tc != null ) {
            tc.removeCanListener(this);
        }
    }
    
    private static final Logger log = LoggerFactory.getLogger(CbusNode.class);
    
}
