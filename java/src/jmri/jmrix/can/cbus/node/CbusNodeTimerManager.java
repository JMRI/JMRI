package jmri.jmrix.can.cbus.node;

import java.util.TimerTask;
import jmri.util.TimerUtil;

/**
 * Class to handle Timers for a CbusNode.
 *
 * @author Steve Young Copyright (C) 2019,2020
 */
public class CbusNodeTimerManager {
    private final CbusBasicNodeWithManagers _node;

    protected int fetchNvTimeoutCount;
    private TimerTask nextNvTimerTask;
    protected int fetchEvVarTimeoutCount;
    private TimerTask nextEvTimerTask;
    protected int numEvTimeoutCount;
    private TimerTask numEvTimerTask;
    protected int allEvTimeoutCount;
    protected TimerTask allEvTimerTask;
    protected int paramRequestTimeoutCount;
    private TimerTask allParamTask;
    private TimerTask sendEditNvTask;
    private TimerTask sendEditEvTask;
    protected TimerTask sendEnumTask;    
    protected int sendEvErrorCount;
    protected int _sendNVErrorCount;

    public static int SINGLE_MESSAGE_TIMEOUT_TIME = 1500;

    /**
     * Create a new CbusNodeTimers
     *
     * @param node The Node
     */
    public CbusNodeTimerManager ( CbusBasicNodeWithManagers node ){
        _node = node;
        resetTimeOutCounts();
    }

    /**
     * See if any timers are running, ie waiting for a response from a physical Node.
     *
     * @return true if timers are running else false
     */
    protected boolean hasActiveTimers(){

        return allParamTask != null
            || allEvTimerTask != null
            || nextEvTimerTask != null
            || nextNvTimerTask != null
            || sendEnumTask != null
            || sendEditEvTask != null
            || sendEditNvTask != null
            || numEvTimerTask != null;
    }

    // stop any timers running
    protected void cancelTimers(){
        clearSendEnumTimeout();
        clearsendEditEvTimeout();
        clearsendEditNvTimeout();
        clearAllParamTimeout();
        clearAllEvTimeout();
        clearNextEvVarTimeout();
        clearNextNvVarTimeout();
        clearNumEvTimeout();
    }

    protected final void resetTimeOutCounts(){
        fetchNvTimeoutCount = 0;
        fetchEvVarTimeoutCount = 0;
        numEvTimeoutCount = 0;
        allEvTimeoutCount = 0;
        paramRequestTimeoutCount = 0;
        sendEvErrorCount = 0;
    }

    /**
     * Stop timer for a single NV fetch request.
     */
    protected void clearNextNvVarTimeout(){
        if (nextNvTimerTask != null ) {
            nextNvTimerTask.cancel();
            nextNvTimerTask = null;
            fetchNvTimeoutCount = 0;
        }
    }

    /**
     * Start timer for a single Node Variable request.
     * 
     * If 10 failed requests aborts loop and sets number of NV's to unknown
     */
    protected void setNextNvVarTimeout() {
        nextNvTimerTask = new TimerTask() {
            @Override
            public void run() {
                nextNvTimerTask = null;
                fetchNvTimeoutCount++;
                if ( fetchNvTimeoutCount == 1 ) {
                    log.info("NV Fetch from node {} timed out",_node.getNodeNumber() ); // 
                }
                else if ( fetchNvTimeoutCount == 10 ) {
                    log.error("Aborting NV Fetch from node {}",_node.getNodeNumber() ); //
                    _node.getNodeNvManager().reset();
                    _node.getNodeParamManager().setParameter(5,-1); // reset number of NV's to unknown and force refresh
                }
                
                _node.getTableModel().triggerUrgentFetch();
                
            }
        };
        TimerUtil.schedule(nextNvTimerTask, SINGLE_MESSAGE_TIMEOUT_TIME);
    }

    /**
     * Stop timer for a single event variable request.
     */
    protected void clearNextEvVarTimeout(){
        if (nextEvTimerTask != null ) {
            nextEvTimerTask.cancel();
            nextEvTimerTask = null;
            fetchEvVarTimeoutCount = 0;
        }
    }

    /**
     * Start timer for a single event variable request.
     * 
     * If 10 failed requests aborts loop and sets events to 0
     * @param eventVarIndex Event Variable Index
     * @param eventString User Friendly Event Text
     */
    protected void setNextEvVarTimeout(int eventVarIndex, String eventString) {
        nextEvTimerTask = new TimerTask() {
            @Override
            public void run() {
                nextEvTimerTask = null;
                fetchEvVarTimeoutCount++;
                if ( fetchEvVarTimeoutCount == 1 ) {
                    log.info("Event Var fetch Timeout from Node {} event {}index {}",
                        _node.getNodeStats().getNodeNumberName(),eventString,eventVarIndex);
                }
                if ( fetchEvVarTimeoutCount == 10 ) {
                    log.error("Aborting Event Variable fetch from Node {} Event {}Index {}",
                        _node.getNodeStats().getNodeNumberName(),eventString,eventVarIndex);
                    _node.getNodeEventManager().resetNodeEvents();
                    fetchEvVarTimeoutCount = 0;
                }
                
                _node.getTableModel().triggerUrgentFetch();
            }
        };
        TimerUtil.schedule(nextEvTimerTask, SINGLE_MESSAGE_TIMEOUT_TIME);
    }

    /**
     * Stop timer for event total RQEVN request.
     */
    protected void clearNumEvTimeout(){
        if (numEvTimerTask != null ) {
            numEvTimerTask.cancel();
            numEvTimerTask = null;
        }
        numEvTimeoutCount = 0;
    }

    /**
     * Start timer for event total RQEVN request.
     * 
     * If 10 failed requests aborts loop and sets event number to 0
     */
    protected void setNumEvTimeout() {
        numEvTimerTask = new TimerTask() {
            @Override
            public void run() {
                numEvTimerTask = null;
                if ( _node.getNodeEventManager().getTotalNodeEvents() < 0 ) {
                    
                    numEvTimeoutCount++;
                    // the process will be re-attempted by the background fetch routine,
                    // we don't start it here to give a little bit more time for network / node to recover.
                    if ( numEvTimeoutCount == 1 ) {
                        log.info("No reponse to RQEVN ( Get Total Events ) from node {}", _node );
                    }
                    if ( numEvTimeoutCount == 10 ) {
                        log.info("Aborting requests for Total Events from node {}", _node );
                        _node.getNodeEventManager().resetNodeEvents();
                        numEvTimeoutCount = 0;
                    }
                }
            }
        };
        TimerUtil.schedule(numEvTimerTask, ( 5000 ) );
    }

    /**
     * Stop timer for an ALL event by index fetch request.
     */
    protected void clearAllEvTimeout(){
        if (allEvTimerTask != null ) {
            allEvTimerTask.cancel();
            allEvTimerTask = null;
        }
    }

    /**
     * Starts timer for an ALL event by index fetch request.
     * <p>
     * This has a higher chance of failing as 
     * we could be expecting up to 255 CAN Frames in response.
     *
     * If fails, re-sends the NERD to the physical node
     * Aborts on 10 failed requests
     */
    protected void setAllEvTimeout() {
        allEvTimerTask = new TimerTask() {
            @Override
            public void run() {
                clearAllEvTimeout();
                if ( _node.getNodeEventManager().getOutstandingIndexNodeEvents() > 0 ) {
                    allEvTimeoutCount++;
                    
                    if ( allEvTimeoutCount < 10 ) {
                        log.warn("Re-attempting event index fetch from node {}", _node );
                        log.warn("NUMEV reports {} events, {} outstanding via ENRSP.",
                            _node.getNodeEventManager().getTotalNodeEvents(),
                            _node.getNodeEventManager().getOutstandingIndexNodeEvents());
                        setAllEvTimeout();
                        _node.send.nERD( _node.getNodeNumber() );
                    }
                    else {
                        log.warn("Aborting whole event / node / index fetch from node {}", _node );
                        _node.getNodeEventManager().resetNodeEvents();
                    }
                }
            }
        };
        TimerUtil.schedule(allEvTimerTask, ( 5000 ) );
    }

    /**
     * Stop timer for a single parameter fetch
     */
    protected void clearAllParamTimeout(){
        if (allParamTask != null ) {
            allParamTask.cancel();
            allParamTask = null;
        }
    }

    /**
     * Start timer for a Parameter request
     * If 10 timeouts are counted, aborts loop, sets 8 parameters to 0
     * and node events array to 0
     * @param index Parameter Index
     */
    protected void setAllParamTimeout( int index) {
        clearAllParamTimeout(); // resets if timer already running
        allParamTask = new TimerTask() {
            @Override
            public void run() {
                allParamTask = null;
                if ( paramRequestTimeoutCount == 0 ) {
                    log.warn("No response to parameter {} request from node {}", index ,_node );
                }
                paramRequestTimeoutCount++;
                if ( paramRequestTimeoutCount == 10 ) {
                    log.warn("Aborting requests to parameter {} for node {}",index,_node );
                    if (_node instanceof CbusNode) {
                        ((CbusNode) _node).nodeOnNetwork(false);
                    }
                }
            }
        };
        TimerUtil.schedule(allParamTask, ( SINGLE_MESSAGE_TIMEOUT_TIME ) );
    }

    /**
     * Stop timer for Teaching NV Node Variables
     */
    protected void clearsendEditNvTimeout(){
        if (sendEditNvTask != null ) {
            sendEditNvTask.cancel();
            sendEditNvTask = null;
        }
    }

    /**
     * Start timer for Teaching NV Node Variables
     * If no response received, increases error count and resumes loop to teach next NV
     * which handles the error
     */
    protected void setsendEditNvTimeout() {
        if (!(_node instanceof CbusNode )){
            return;
        }

        sendEditNvTask = new TimerTask() {
            @Override
            public void run() {
                sendEditNvTask = null;
                //  log.info(" getsendsWRACKonNVSET {} ",getsendsWRACKonNVSET()  ); 
                if ( ((CbusNode)_node).getsendsWRACKonNVSET() ) {
                    log.warn("teach nv timeout");
                    _sendNVErrorCount++;
                }
                _node.getNodeNvManager().sendNextNvToNode();
            }
        };
        TimerUtil.schedule(sendEditNvTask, ( SINGLE_MESSAGE_TIMEOUT_TIME ) );
    }

    /**
     * Stops timer for Teaching Events
     */
    protected void clearsendEditEvTimeout(){
        if (sendEditEvTask != null ) {
            sendEditEvTask.cancel();
            sendEditEvTask = null;
        }
    }

    /**
     * Start timer for Teaching Events
     * On timeout, ie Node does not Respond with a success message,
     * stops Learn Loop and takes node out of Learn Mode.
     */
    protected void setsendEditEvTimeout() {
        sendEditEvTask = new TimerTask() {
            @Override
            public void run() {
                log.info("Late / no response from node while teaching event");
                sendEditEvTask = null;
                sendEvErrorCount++;
                
                // stop loop and take node out of learn mode
                _node.getNodeEventManager().nextEvInArray=999;
                _node.getNodeEventManager().teachNewEvLoop();
            }
        };
        TimerUtil.schedule(sendEditEvTask, ( SINGLE_MESSAGE_TIMEOUT_TIME ) );
    }

    /**
     * Stops timer for CAN ID Self Enumeration Timeout
     */
    protected void clearSendEnumTimeout(){
        if (sendEnumTask != null ) {
            sendEnumTask.cancel();
            sendEnumTask = null;
        }
    }

    /**
     * Starts timer for CAN ID Self Enumeration Timeout
     * If no response adds warning to console log
     */
    protected void setsendEnumTimeout() {
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusNodeTimerManager.class);

}
