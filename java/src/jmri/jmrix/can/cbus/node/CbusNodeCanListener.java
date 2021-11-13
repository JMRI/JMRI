package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a Processing of CAN Frames for a CbusNode.
 *
 * @author Steve Young Copyright (C) 2019,2020
 */
public class CbusNodeCanListener implements jmri.jmrix.can.CanListener {
    public final CanSystemConnectionMemo memo;
    private final CbusBasicNodeWithManagers _node;
    private int[] tempSetupParams;
    
    /**
     * Create a new CbusNodeCanListener
     *
     * @param connmemo The CAN Connection to listen to.
     * @param node The Node
     */
    public CbusNodeCanListener ( CanSystemConnectionMemo connmemo, CbusBasicNodeWithManagers node ){
        memo = connmemo;
        _node = node;
        addTc(memo);
    }
    
    /**
     * Processes certain outgoing CAN Frames.
     * <p>
     * We don't know if it's this JMRI instance or something external teaching the node
     * so we monitor them the same
     *
     * {@inheritDoc} 
     */
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
    
    /**
     * Processes all incoming and certain outgoing CAN Frames
     *
     * {@inheritDoc} 
     */
    @Override
    public void reply(CanReply m) {
        if ( m.extendedOrRtr() ) {
            return;
        }
        int opc = CbusMessage.getOpcode(m);
        int nn = ( m.getElement(1) * 256 ) + m.getElement(2);
        
        // if the OPC is coming from a Node, update the CAN ID field
        // if the OPC is coming from software, do NOT update the CAN ID field
        
        // if node in learn mode 
        if ( _node.getNodeInLearnMode() ) {
           processInLearnMode(m);
        }
        
        if (nn != _node.getNodeNumber() ) {
            return;
        }
        
        _node.setCanId(CbusMessage.getId(m));

        switch (CbusMessage.getOpcode(m)) {
            case CbusConstants.CBUS_CMDERR:
                // response from node with an error message
                
                // if in middle of a learn process we do not re-kick the node here,
                // as it may be another software sending the learn.
                // If it is JMRI doing the learn, the timer for the learn will
                // sort out any abort / resume logic.
                
                if (m.getElement(3)==5){
                    // node reporting that last requested and further event variables for single event
                    // are not required by the node, no need to request them
                    _node.getNodeEventManager().remainingEvVarsNotNeeded();
                }
                else {
                    if ((m.getElement(3) > 0 ) && (m.getElement(3) < 13 )) {
                        log.error("Node {} reporting {}",_node,Bundle.getMessage("CMDERR"+m.getElement(3)) );
                    } else {
                        log.error("Node {} Reporting Error Code {} (decimal)",_node,m.getElement(3) );
                    }
                }   break;
            case CbusConstants.CBUS_NNACK:
                // response from node acknowledging something
                if ( _node.getNodeTimerManager().sendEnumTask != null ) {
                    _node.getNodeTimerManager().clearSendEnumTimeout();
                }   break;
            case CbusConstants.CBUS_PARAN:
                // response from node
                
                processParam(m);
                break;
            case CbusConstants.CBUS_NUMEV:
                // response from node
                
                int newEventsOnNode = m.getElement(3);
                _node.getNodeTimerManager().clearNumEvTimeout();
                _node.getNodeEventManager().resetNodeEventsToZero();
                if ( _node.getNodeParamManager().getParameter(5)<0 ){
                    return;
                }   for (int i = 0; i < newEventsOnNode; i++) {
                    CbusNodeEvent newev = new CbusNodeEvent(memo,-1, -1, _node.getNodeNumber(), -1,
                            _node.getNodeParamManager().getParameter(5) );
                    // (int nn, int en, int thisnode, int index, int maxEvVar);
                    _node.getNodeEventManager().addNewEvent(newev);
                }
                _node.notifyPropertyChangeListener("ALLEVUPDATE",null,null);
                break;
            case CbusConstants.CBUS_ENRSP:
                // response from node with a stored event, node + index
                
                int evnode = ( m.getElement(3) * 256 ) + m.getElement(4);
                int evev = ( m.getElement(5) * 256 ) + m.getElement(6);
                // get next node event which is empty
                _node.getNodeEventManager().setNextEmptyNodeEvent(evnode,evev,m.getElement(7));
                if ( ( _node.getNodeTimerManager().allEvTimerTask !=null ) && ( _node.getNodeEventManager().getOutstandingIndexNodeEvents() == 0 ) ) {
                    // all events returned ok, this is the only
                    // point ANYWHERE that the event index is set valid
                    _node.getNodeTimerManager().clearAllEvTimeout();
                    _node.getNodeEventManager().setEvIndexValid(true);
                }   break;
            case CbusConstants.CBUS_NEVAL: // response from node with event variable
                _node.getNodeTimerManager().clearNextEvVarTimeout();
                _node.getNodeEventManager().setEvVarByIndex(m.getElement(3),m.getElement(4),m.getElement(5));
                break;
            case CbusConstants.CBUS_NVANS: // response from node with node variable
                // stop timer
                _node.getNodeTimerManager().clearNextNvVarTimeout();
                _node.getNodeNvManager().setNV(m.getElement(3),m.getElement(4));
                break;
            case CbusConstants.CBUS_NVSET:
                // sent from software
                _node.getNodeNvManager().setNV(m.getElement(3),m.getElement(4));
                break;
            case CbusConstants.CBUS_NNLRN:
                // sent from software
                // [AC] Ignore setting servo modules into learn mode during NV GUI edit
                if (((CbusNode)_node).getnvWriteInLearnOnly() == false) {
                    _node.setNodeInLearnMode(true);
                }
                break;
            case CbusConstants.CBUS_NNULN:
                // sent from software
                _node.setNodeInLearnMode(false);
                break;
            case CbusConstants.CBUS_ENUM:
                // sent from software
                _node.setCanId(-1);
                // now waiting for a NNACK confirmation or error message 7
                // start a timer waiting for the response
                _node.getNodeTimerManager().setsendEnumTimeout();
                break;
            case CbusConstants.CBUS_CANID:
                // sent from software
                _node.setCanId(-1);
                // no response expected from node ( ? )
                break;
            default:
                break;
        }
        
        if ( _node.getNodeNvManager().teachOutstandingNvs() ) {
        
            if ( opc == CbusConstants.CBUS_WRACK ) { // response from node
                _node.getNodeTimerManager().clearsendEditNvTimeout();
                _node.getNodeNvManager().sendNextNvToNode();
            }
            else if ( opc == CbusConstants.CBUS_CMDERR ) { // response from node
                _node.getNodeTimerManager()._sendNVErrorCount++;
                log.warn("Node reports NV Write Error");
                _node.getNodeTimerManager().clearsendEditNvTimeout();
                _node.getNodeNvManager().sendNextNvToNode();
            }
        }
        
        if ( _node.getTableModel() != null ) {
            _node.getTableModel().triggerUrgentFetch(); // 
        }
        
    }
    
    private void processParam(CanReply m){
        _node.getNodeTimerManager().clearAllParamTimeout();
        if (m.getElement(3)==0) { // reset parameters
            int [] myarray = new int[(m.getElement(4)+1)]; // +1 to account for index 0 being the parameter count
            java.util.Arrays.fill(myarray, -1);
            // node may already be aware of some params via the initial PNN or STAT

            myarray[1] = _node.getPnnManufacturer();
            myarray[2] = _node._fwMin;
            myarray[3] = _node.getPnnModule();
            myarray[7] = _node._fwMaj;

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
                    _node.getNodeNvManager().setNVs(myParray);
                }
            }

            myarray[0] = m.getElement(4);

            // log.info("parameter 0 is {}",myarray[0]);
            _node.getNodeParamManager().setParameters(myarray);

            // setting them via setParameter to avoid nulls if number of parameters is v low
            // most modules report up to 20, but some may not.
            _node.getNodeParamManager().setParameter( 20, _node._fwBuild );

        } else {
            _node.getNodeParamManager().setParameter( m.getElement(3), m.getElement(4) );
            if ( m.getElement(3) == 6 ) { // reset NV's
                int [] myarray = new int[(m.getElement(4)+1)]; // +1 to account for index 0 being the NV count
                java.util.Arrays.fill(myarray, -1);
                myarray[0] = m.getElement(4);
                _node.getNodeNvManager().setNVs(myarray);
            }
        }
    }
    
    private void processInLearnMode(CanReply m){
    
        int opc = CbusMessage.getOpcode(m);
        int nn = ( m.getElement(1) * 256 ) + m.getElement(2);
        
        switch (opc) {
            case CbusConstants.CBUS_NNCLR:
                // instruction to delete all node events
                if ( nn == _node.getNodeNumber() ) {
                    _node.getNodeEventManager().resetNodeEventsToZero();
                }
                break;
            case CbusConstants.CBUS_EVLRN:
                // update node database with event
                _node.getNodeEventManager().updateNodeFromLearn(
                        nn,
                        ( m.getElement(3) * 256 ) + m.getElement(4),
                        m.getElement(5),
                        m.getElement(6) );
                break;
            case CbusConstants.CBUS_EVULN:
                _node.getNodeEventManager().removeEvent( ( m.getElement(1) * 256 ) + m.getElement(2), ( m.getElement(3) * 256 ) + m.getElement(4) );
                break;
            case CbusConstants.CBUS_EVLRNI:
                processEvlrni(m);
                break;
            default:
                break;
        }
        
        if ( _node.getNodeEventManager().TEACH_OUTSTANDING_EVS ) {
            if ( opc == CbusConstants.CBUS_WRACK ) {
                // cancel timer
                _node.getNodeTimerManager().clearsendEditEvTimeout();
                // start next in loop
                _node.getNodeEventManager().teachNewEvLoop();
            }
            if ( opc == CbusConstants.CBUS_CMDERR ) {
                // cancel timer
                _node.getNodeTimerManager().clearsendEditEvTimeout();
                _node.getNodeTimerManager().sendEvErrorCount++;
                // start next in loop
                _node.getNodeEventManager().teachNewEvLoop();
            }
        }
    }
    
    private void processEvlrni(CanReply m){
        // check if current index is valid
        if ( !_node.getNodeEventManager().isEventIndexValid() ){
            log.warn("EVRLNI OPC heard while Event Index Invalid for Node {}",_node );
        }
        else {
            // find existing event , m.getElement(5) is event index number being edited
            CbusNodeEvent toEdit = _node.getNodeEventManager().getNodeEventByIndex( m.getElement(5) );
            if (toEdit == null) {
                log.warn("No event with index {} found on node {}",m.getElement(5),toString() );
            } else {
                // event found with correct index number
                toEdit.setNn( ( m.getElement(1) * 256 ) + m.getElement(2) );
                toEdit.setEn( ( m.getElement(3) * 256 ) + m.getElement(4) );
                toEdit.setEvVar( ( m.getElement(6) * 256 ), m.getElement(7) );
            }
        }
    }
    
    /**
     * Temporarily store Node Parameters obtained from a Node requesting a Node Number
     * <p>
     * Parameter array is not created until total number of parameters is known.
     * This saves asking the Node for them.
     *
     * @param setupParams an int array in order of final 7 bytes of the CBUS_PARAMS node response
     */
    public void setParamsFromSetup(int[] setupParams) {
        log.debug("setup parameters received {}",setupParams);
        tempSetupParams = setupParams;
    }
    
    /**
     * Disconnects from network
     */
    public void dispose(){
        removeTc(memo);
    }
    
    private static final Logger log = LoggerFactory.getLogger(CbusNodeCanListener.class);
    
}
