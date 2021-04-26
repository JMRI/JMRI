package jmri.jmrix.can.cbus.node;

import java.util.ArrayList;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a Processing of CAN Frames for a CbusNode.
 *
 * @author Steve Young Copyright (C) 2019,2020
 */
public class CbusNodeEventManager {
    private final CbusBasicNodeWithManagers _node;
    private final CanSystemConnectionMemo _memo;

    protected int nextEvInArray;
    private ArrayList<CbusNodeEvent> _nodeEvents;
    private boolean _eventIndexValid;
    private ArrayList<CbusNodeEvent> eventsToTeachArray;
    private int nextEvVar;
    protected boolean TEACH_OUTSTANDING_EVS;
    
    /**
     * Create a new CbusNodeNVManager
     *
     * @param memo System connection
     * @param node The Node
     */
    public CbusNodeEventManager ( CanSystemConnectionMemo memo, CbusBasicNodeWithManagers node ){
        _node = node;
        _memo = memo;
        _nodeEvents = null;
        _eventIndexValid = false;
        TEACH_OUTSTANDING_EVS = false;
    }
    
    /**
     * Returns total number of node events,
     * including those with outstanding event variables.
     *
     * @return number of events, -1 if events not set
     */ 
    public int getTotalNodeEvents(){
        if (_nodeEvents == null) {
            return -1;
        }
        return _nodeEvents.size();
    }
    
    /**
     * Returns number of fully loaded events, ie no outstanding event variables.
     *
     * @return number of loaded events, -1 if events not set
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
     * @return number of outstanding index events
     */
    public int getOutstandingIndexNodeEvents(){
        return getTotalNodeEvents() - getLoadedNodeEvents();
    }
    
    /**
     * Add an event to the node, will not overwrite an existing event.
     * Resets Event Index as Invalid for All Node Events
     *
     * @param newEvent the new event to be added
     */
    public void addNewEvent( @Nonnull CbusNodeEvent newEvent ) {
        if (_nodeEvents == null) {
            _nodeEvents = new ArrayList<>();
        }
        _nodeEvents.add(newEvent);
        
        setEvIndexValid(false); // Also produces Property Change Event
    }

    /**
     * Remove an event from the CbusNode, does not update hardware.
     *
     * @param nn the event Node Number
     * @param en the event Event Number
     */
    public void removeEvent(int nn, int en){
        _nodeEvents.remove(getNodeEvent(nn, en));
        setEvIndexValid(false);  // Also produces Property Change Event
    }

    /**
     * Get a Node event from its Event and Node number combination
     *
     * @param en the Event event number
     * @param nn the Event node number
     * @return the node event else null if no Event / Node number combination.
     */
    @CheckForNull
    public CbusNodeEvent getNodeEvent(int nn, int en) {
        if (_nodeEvents==null){
            return null;
        }
        for (int i = 0; i < _nodeEvents.size(); i++) {
            if ( ( _nodeEvents.get(i).getNn() == nn ) && ( _nodeEvents.get(i).getEn() == en )) {
                return _nodeEvents.get(i);
            }
        }
        return null;
    }
    
    /**
     * Provide a Node event from its Event and Node number combination
     * <p>
     * If an event for this number pair does not already exist on the node
     * one will be created, else the existing will be returned.
     * <p>
     * Adds any new CbusNodeEvent to the node event array, 
     * which will also be created if it doesn't exist.
     *
     * @param en the Event event number
     * @param nn the Event node number
     * @return the node event
     */
    @Nonnull
    public CbusNodeEvent provideNodeEvent(int nn, int en) {
        if (_nodeEvents == null) {
            _nodeEvents = new ArrayList<>();
        }
        CbusNodeEvent newev = getNodeEvent(nn,en);
        if ( newev ==null){
            newev = new CbusNodeEvent(_memo,nn, en, _node.getNodeNumber(),
                -1, _node.getNodeParamManager().getParameter(5) );
            addNewEvent(newev);
        }
        setEvIndexValid(false); // Also produces Property Change Event
        return newev;
    }    
    
    /**
     * Update node with new Node Event.
     * 
     * @param nn Node Number
     * @param en Event Number
     * @param evvarindex Event Variable Index
     * @param evvarval Event Variable Value
     */
    protected void updateNodeFromLearn(int nn, int en, int evvarindex, int evvarval ){
        CbusNodeEvent nodeEv = provideNodeEvent( nn , en );
        nodeEv.setEvVar( evvarindex , evvarval );
        _node.notifyPropertyChangeListener("ALLEVUPDATE",null,null);
        
    }
    
    /**
     * Get a Node event from its Index Field
     * <p>
     * This is NOT the node array index.
     *
     * @param index the Node event index, as set by a node from a NERD request
     * @return the node event, else null if the index is not located
     */
    @CheckForNull
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

    /**
     * Get the Node event by ArrayList Index.
     *
     * @param index the index of the CbusNodeEvent within the ArrayList
     * @return the Node Event
     */
    @CheckForNull
    public CbusNodeEvent getNodeEventByArrayID(int index) {
        return _nodeEvents.get(index);
    }
    
    /**
     * Get the Node event ArrayList
     *
     * @return the list of Node Events
     */
    @CheckForNull
    public ArrayList<CbusNodeEvent> getEventArray(){
        return _nodeEvents;
    }
    
    /**
     * Get the Number of Outstanding Event Variables
     * <p>
     * Sometimes, the Event Variables have to be initialised with an unknown
     * status, this returns a count of unknown Event Variables for the whole Node.
     *
     * @return -1 if main node events array null, else number Outstanding Ev Vars
     */
    public int getOutstandingEvVars(){
        int count = 0;
        ArrayList<CbusNodeEvent> _evs = getEventArray();
        if (  _evs == null ){
            return -1;
        }
        for (int i = 0; i < _evs.size(); i++) {
            count = count + _evs.get(i).getOutstandingVars();
        }
        return count;
    }
    
    /**
     * The last message from the node CMDERR5 indicates that all remaining event variables
     * for a particular event are not required.
     * This sets the remaining ev vars to 0 so are not requested
     */
    protected void remainingEvVarsNotNeeded(){
        ArrayList<CbusNodeEvent> _evs = getEventArray();
        if (_evs!=null) {
            for (int i = 0; i < _evs.size(); i++) {
                if ( _evs.get(i).getNextOutstanding() > 0 ) {
                    _evs.get(i).allOutstandingEvVarsNotNeeded();

                    // cancel Timer
                    _node.getNodeTimerManager().clearNextEvVarTimeout();
                    // update GUI
                    _node.notifyPropertyChangeListener("SINGLEEVUPDATE",null,i);
                    return;
                }
            }
        }
    }
    
    /**
     * Send a request for the next unknown Event Variable to the Physical Node
     * <p>
     * If events are unknown, sends the NERD request and starts that timer,
     * else requests the next Ev Var with unknown status ( represented as int value -1 )
     * Will NOT send if any Node is in Learn Mode or if there are any outstanding requests from the Node.
     */
    protected void sendNextEvVarToFetch() {
        ArrayList<CbusNodeEvent> _evs = getEventArray();
        // do not request if node is learn mode
        if (( _node.getTableModel().getAnyNodeInLearnMode() > -1 )
        || ( _evs == null )
        || ( _node.getNodeTimerManager().hasActiveTimers()) ){
            return;
        }
        
        // if events on module, get their event, node and node index
        // *** This could produce up to 255 responses per node ***
        if ( ( getTotalNodeEvents() > 0 ) && getOutstandingIndexNodeEvents()>0 ) {
            _node.send.nERD( _node.getNodeNumber() );
            // starts timeout 
            _node.getNodeTimerManager().setAllEvTimeout();
            return;
        }
        
        for (int i = 0; i < _evs.size(); i++) {
            if ( _evs.get(i).getOutstandingVars() > 0 ) {
                int index = _evs.get(i).getIndex();
                int nextevvar = _evs.get(i).getNextOutstanding();
                
                // index from NERD / ENRSP indexing may start at 0
                if ( index > -1 ) {
                
                    // start timer
                    _node.getNodeTimerManager().setNextEvVarTimeout( nextevvar,_evs.get(i).toString() );
                    _node.send.rEVAL( _node.getNodeNumber(), index, nextevvar );
                    return;
                }
                else { // if index < 0 event index is invalid so attempt refetch.
                    // reset events
                    log.info("Invalid index, resetting events for node {}", _node );
                    _nodeEvents = null;
                    return;
                }
            }
        }
    }
    
    /**
     * Used in CBUS_NEVAL response from Node.
     * Set the value of an event variable by event Index and event Variable Index
     * @param eventIndex Event Index
     * @param eventVarIndex Event Variable Index
     * @param newVal New Value
     */
    protected void setEvVarByIndex(int eventIndex, int eventVarIndex, int newVal) {
        CbusNodeEvent nodeEvByIndex = getNodeEventByIndex(eventIndex);
        if ( nodeEvByIndex != null ) {
            nodeEvByIndex.setEvVar(eventVarIndex,newVal);
            _node.notifyPropertyChangeListener("SINGLEEVUPDATE",null,getEventRowFromIndex(eventIndex));
        }
    }
    
    /**
     * Used to process a CBUS_ENRSP response from node
     *
     * If existing index known, use that slot in the event array,
     * else if event array has empty slot for that index, use that slot.
     * @param nn Node Number
     * @param en Event Number
     * @param index Index Number
     */
    protected void setNextEmptyNodeEvent(int nn, int en, int index){
        ArrayList<CbusNodeEvent> _evs = getEventArray();
        if ( _evs == null ){
            log.error("Indexed events are not expected as total number of events unknown");
            return;
        } else {
            for (int i = 0; i < _evs.size(); i++) {
                if ( _evs.get(i).getIndex() == index ) {
                    _evs.get(i).setNn(nn);
                    _evs.get(i).setEn(en);
                    _node.notifyPropertyChangeListener("SINGLEEVUPDATE",null,i);
                    return;
                }
            }
        }
        
        for (int i = 0; i < _nodeEvents.size(); i++) {
            if ( ( _nodeEvents.get(i).getNn() == -1 ) && ( _nodeEvents.get(i).getEn() == -1 ) ) {
                _nodeEvents.get(i).setNn(nn);
                _nodeEvents.get(i).setEn(en);
                _nodeEvents.get(i).setIndex(index);
                
                _node.notifyPropertyChangeListener("SINGLEEVUPDATE",null,i);
                return;
            }
        }
        log.error("Issue setting node event, index {} not valid",index);
        _nodeEvents = null;
    }
    
        /**
     * Get if the Node event index is valid
     * 
     * @return true if event index is valid, else false if invalid or no events on node.
     */
    protected boolean isEventIndexValid(){
        return _eventIndexValid;
    }
    
    /**
     * Set the Node event index flag as valid or invalid.
     * <p>
     * Resets all Node Event Indexes to -1 if invalid.
     * @param newval true if Event Index Valid, else false
     */
    protected void setEvIndexValid( boolean newval ) {
        _eventIndexValid = newval;
        if (!newval){ // event index no longer valid so clear values in individual events
            for (int i = 0; i < _nodeEvents.size(); i++) {
                _nodeEvents.get(i).setIndex(-1);
            }
        }
        _node.notifyPropertyChangeListener("ALLEVUPDATE",null,null);
    }
    
    /**
     * Send and teach updated Events to this node
     *
     * @param evArray array of CbusNodeEvents to be taught
     */
    public void sendNewEvSToNode( @Nonnull ArrayList<CbusNodeEvent> evArray ){
        eventsToTeachArray = evArray;
        
        if (eventsToTeachArray==null){
            _node.getNodeTimerManager().sendEvErrorCount=1;
            teachEventsComplete();
            return;
        }
        
        if (eventsToTeachArray.isEmpty()){
            teachEventsComplete();
            return;
        }
        
        // check other nodes in learn mode
        if ( _node.getTableModel().getAnyNodeInLearnMode() > -1 ) {
            String err = "Cancelling teach event.  Node " + _node.getTableModel().getAnyNodeInLearnMode() + " is already in Learn Mode";
            log.warn(err);
            _node.notifyPropertyChangeListener("ADDEVCOMPLETE", null, err);
            return;
        }
        
        TEACH_OUTSTANDING_EVS = true;
        nextEvInArray = 0;
        nextEvVar = 1; // start at 1 as 0 is used for total ev vars
        _node.getNodeTimerManager().sendEvErrorCount = 0;
        
        _node.send.nodeEnterLearnEvMode( _node.getNodeNumber() ); // no response expected but we add a mini delay for other traffic
        
        log.debug("sendNewEvSToNode {}",evArray);
        
        ThreadingUtil.runOnLayoutDelayed( () -> {
            teachNewEvLoop();
        }, 50 );
        
    }
    
    /**
     * Send a message to delete an event stored on this node
     *
     * @param nn event node number
     * @param en event event number
     */
    public void deleteEvOnNode( int nn, int en){
        
        // check other nodes in learn mode
        if ( _node.getTableModel().getAnyNodeInLearnMode() > -1 ) {
            String err = "Cancelling delete event.  Node " + _node.getTableModel().getAnyNodeInLearnMode() + " is already in Learn Mode";
            log.warn(err);
            _node.notifyPropertyChangeListener("DELETEEVCOMPLETE", null, err);
            return;
        }
        
        _node.send.nodeEnterLearnEvMode( _node.getNodeNumber() ); 
        // no response expected but we add a mini delay for other traffic
        ThreadingUtil.runOnLayoutDelayed( () -> {
            _node.send.nodeUnlearnEvent( nn, en );
            setEvIndexValid(false);
        }, 50 );
        ThreadingUtil.runOnGUIDelayed( () -> {
            _node.send.nodeExitLearnEvMode( _node.getNodeNumber() );
            // notify ui
            _node.notifyPropertyChangeListener("DELETEEVCOMPLETE", null, null);
        }, 100 );
    }
    
    private void teachEventsComplete(){
    
        TEACH_OUTSTANDING_EVS = false;
            _node.send.nodeExitLearnEvMode( _node.getNodeNumber() );
            String err;
            if ( _node.getNodeTimerManager().sendEvErrorCount==0 ) {
                log.info("Completed Event Write with No errors, node {}.", _node );
                err = "";
            }
            else {
                err = "Event Write Failed with "+ _node.getNodeTimerManager().sendEvErrorCount +" errors.";
                log.error("{} Node {}.", err, _node);
                
            }
            // notify ui's
            ThreadingUtil.runOnGUIDelayed( () -> {
                _node.notifyPropertyChangeListener("ADDEVCOMPLETE", null, err);
                 _node.notifyPropertyChangeListener("ADDALLEVCOMPLETE", null, _node.getNodeTimerManager().sendEvErrorCount);
                _node.getNodeTimerManager().sendEvErrorCount=0;
            },50 );
    }

    /**
     * Loop for event teaching, triggered from various places
     */
    protected void teachNewEvLoop(){
        
        if ( nextEvVar > _node.getNodeParamManager().getParameter(5) ) {
            nextEvVar = 1;
            nextEvInArray++;
        }
        
        if ( nextEvInArray >= eventsToTeachArray.size() ) {
            log.debug("all done");
            teachEventsComplete();
            return;
        }
        
        CbusNodeEvent wholeEvent = eventsToTeachArray.get(nextEvInArray);
        
        log.debug("teach event var {}  val {} ",nextEvVar,wholeEvent.getEvVar(nextEvVar));
        CbusNodeEvent existingEvent = getNodeEvent( wholeEvent.getNn(), wholeEvent.getEn() );
        
        log.debug("teach event {} with existing event {}",wholeEvent,existingEvent);
        
        // increment and restart loop if no change
        if ((existingEvent!=null) && (existingEvent.getEvVar(nextEvVar)==wholeEvent.getEvVar(nextEvVar))){
            nextEvVar++;
            teachNewEvLoop();
            return;
        }
        
        // start timeout , send message, increment for next run, only sent when WRACK received
        _node.getNodeTimerManager().setsendEditEvTimeout();
        _node.send.nodeTeachEventLearnMode(wholeEvent.getNn(),wholeEvent.getEn(),nextEvVar,wholeEvent.getEvVar(nextEvVar));
        nextEvVar++;
        
    }
    
    /**
     * Resets Node Events with null array.
     * For when a CbusNode is reset to unknown events.
     */
    public void resetNodeEvents() {
        _nodeEvents = null;
        _node.notifyPropertyChangeListener("ALLEVUPDATE",null,null);
    }
    
    /**
     * Resets Node Events with zero length array.
     * For when a CbusNode is reset to 0 events
     *
     */
    public void resetNodeEventsToZero() {
        _nodeEvents = null;
        _nodeEvents = new ArrayList<>();
        _node.notifyPropertyChangeListener("ALLEVUPDATE",null,null);
    }
    
    /**
     * the next event index for a CbusDummyNode NODE to allocate, 
     * NOT a software tool.
     * @return next available event index
     */
    public int getNextFreeIndex(){
        int newIndex = 1;
        for (int i = 0; i < getTotalNodeEvents(); i++) {
            CbusNodeEvent a = getNodeEventByArrayID(i);
            if ( (a!=null) && newIndex <= a.getIndex() ) {
                newIndex = a.getIndex()+1;
            }
        }
        log.debug("dummy node sets index {}",newIndex);
        return newIndex;
    }
    
    private static final Logger log = LoggerFactory.getLogger(CbusNodeEventManager.class);
    
}
