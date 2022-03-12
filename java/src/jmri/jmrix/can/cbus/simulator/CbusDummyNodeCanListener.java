package jmri.jmrix.can.cbus.simulator;

import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Class to represent a Processing of CAN Frames for a CbusDummyNode.
 *
 * @author Steve Young Copyright (C) 2019,2020
 */
public class CbusDummyNodeCanListener extends CbusSimCanListener {

    private final CbusDummyNode _dummyNode;
    
    /**
     * Create a new CbusDummyNodeCanListener
     *
     * @param connmemo The CAN Connection to listen to.
     * @param node The Node
     */
    public CbusDummyNodeCanListener ( CanSystemConnectionMemo connmemo, CbusDummyNode node ){
        super(connmemo,node);
        _dummyNode = node;
        setDelay (40);
    }
    
    @Override
    protected void startProcessFrame(AbstractMessage m) {
        // extended or module type unset
        if ( _dummyNode.getNodeParamManager().getParameter(3) == 0 ) {
            return;
        }

        if ( _dummyNode.getNodeInSetupMode() ) {
            processDummyCanSetupMode(m);
        }
        else if ( _dummyNode.getNodeInFLiMMode() ) {
            if ( m.getElement(0) == CbusConstants.CBUS_QNN ) { // Query Nodes
                _dummyNode.sendPNN();
            }
            if (_dummyNode.getNodeNumber() == ( m.getElement(1) * 256 ) + m.getElement(2) ) {
                processDummyCanFLimMode(m);
            }
            if ( _dummyNode.getNodeInLearnMode() ){
                processDummyCanLearnMode(m);
            }
        }
    }
    
    private void processDummyCanFLimMode(AbstractMessage m){
        switch (m.getElement(0)) {
            case CbusConstants.CBUS_NNULN:
                // Node exit learn mode
                _dummyNode.setNodeInLearnMode(false);
                break;
            case CbusConstants.CBUS_NNLRN:
                // Node enter learn mode
                _dummyNode.setNodeInLearnMode(true);
                break;
            case CbusConstants.CBUS_RQNPN:
                // Request Node Parameters by Index
                _dummyNode.sendPARAN(m.getElement(3));
                break;
            case CbusConstants.CBUS_NVRD:
                // Request Node Variable by Index
                _dummyNode.sendNVANS(m.getElement(3));
                break;
            case CbusConstants.CBUS_NVSET:
                // Set Node Variable
                _dummyNode.setDummyNV(m.getElement(3),m.getElement(4));
                break;
            default:
                processDummyCanFLimModeEventStuff(m);
        }
    }
    
    private void processDummyCanFLimModeEventStuff(AbstractMessage m){
        switch (m.getElement(0)) {    
            case CbusConstants.CBUS_RQEVN:
                // Request number of events
                _dummyNode.sendNUMEV();
                break;
            case CbusConstants.CBUS_NERD:
                // Readback all stored events in node
                _dummyNode.sendENRSP();
                break;
            case CbusConstants.CBUS_REVAL:
                // Request for read of an event variable
                _dummyNode.sendNEVAL(m.getElement(3),m.getElement(4));
                break;
            default:
                break;
        }
    }
    
    private void processDummyCanSetupMode(AbstractMessage m){
        
        switch (m.getElement(0)) {
            case CbusConstants.CBUS_RQNP:
                // Request Node Parameters
                _dummyNode.sendPARAMS();
                break;
            case CbusConstants.CBUS_SNN:
                // Set Node Number
                _dummyNode.setDNN( ( m.getElement(1) * 256 ) + m.getElement(2) );
                break;
            case CbusConstants.CBUS_RQMN:
                // Request Module Name

                byte[] byteArr = jmri.util.StringUtil.fullTextToHexArray( _dummyNode.getNodeNameFromName(),7 );
                CanReply r = new CanReply(8);
                r.setElement(0, CbusConstants.CBUS_NAME);
                r.setElement(1, byteArr[0] & 0xff);
                r.setElement(2, byteArr[1] & 0xff);
                r.setElement(3, byteArr[2] & 0xff);
                r.setElement(4, byteArr[3] & 0xff);
                r.setElement(5, byteArr[4] & 0xff);
                r.setElement(6, byteArr[5] & 0xff);
                r.setElement(7, byteArr[6] & 0xff);
                send.sendWithDelay(r,getSendIn(),getSendOut(),getDelay());
                break;
            default:
                break;
        }
        
    }
    
    private void processDummyCanLearnMode(AbstractMessage m){
        switch (m.getElement(0)) {
            case CbusConstants.CBUS_EVLRN:
                // Teach node event
                evLearn(
                        ( ( m.getElement(1) * 256 ) + m.getElement(2) ),
                        ( ( m.getElement(3) * 256 ) + m.getElement(4) ),
                        m.getElement(5),
                        m.getElement(6) );
                break;
            case CbusConstants.CBUS_EVULN:
                // Node Unlearn event
                _dummyNode.getNodeEventManager().removeEvent( ( m.getElement(1) * 256 ) + m.getElement(2) ,
                        ( ( m.getElement(3) * 256 ) + m.getElement(4) ) );
                break;
            case CbusConstants.CBUS_NNCLR:
                // clear all events
                if ( ( ( m.getElement(1) * 256 ) + m.getElement(2) ) ==  _dummyNode.getNodeNumber() ) {
                    // no response expected
                    _dummyNode.getNodeEventManager().resetNodeEventsToZero();
                }   break;
            default:
                break;
        }
    }
    
    private void evLearn(int nn, int en, int index, int val){
        
        if ( val < 0 || val > 255 ) {
            _dummyNode.sendCMDERR(11);
            return;
        }
        if ( index < 0 || index > 255 ) {
            _dummyNode.sendCMDERR(6);
            return;
        }
        _dummyNode.getNodeEventManager().provideNodeEvent(nn,en).setEvVar(index,val);
        if ( _dummyNode.getNodeEventManager().provideNodeEvent(nn,en).getIndex()< 0 ) {
            _dummyNode.getNodeEventManager().provideNodeEvent(nn,en).setIndex(
                _dummyNode.getNodeEventManager().getNextFreeIndex() );
        }
        _dummyNode.sendWRACK();
    }

    // private static final Logger log = LoggerFactory.getLogger(CbusDummyNodeCanListener.class);
    
}
