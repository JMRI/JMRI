package jmri.jmrix.can.cbus.simulator;

import java.util.ArrayList;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusSend;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeConstants;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.jmrix.can.cbus.swing.simulator.NdPane;
import jmri.jmrix.can.TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulating a MERG CBUS Node.
 *
 * @author Steve Young Copyright (C) 2018 2019
 * @see CbusSimulator
 * @since 4.15.2
 */
public class CbusDummyNode extends CbusNode {
    
    private TrafficController tc;
    private CanSystemConnectionMemo memo;
    
    private NdPane _pane;
    public static ArrayList<Integer> ndTypes;
    
    private int _networkDelay;
    private Boolean _processIn;
    private Boolean _processOut;
    private Boolean _sendIn;
    private Boolean _sendOut;
    
    public CbusDummyNode( int nodenumber, int manufacturer, int nodeType, int canId, CanSystemConnectionMemo sysmemo ){
        super( sysmemo, nodenumber );
        memo = sysmemo;
        if (memo != null) {
            tc = memo.getTrafficController();
            addTc(tc);
        }
        setDummyType(manufacturer, nodeType);
        setCanId(canId);
        init();
    }
    
    private void init(){
        
        _networkDelay = 40;
        _pane = null;

        _processIn=false;
        _processOut=true;
        _sendIn=true;
        _sendOut=false;
        
        // get available simulated nodes
        ndTypes = new ArrayList<Integer>();
        ndTypes.add(0); // 0 SlIM
        ndTypes.add(29); // 29 CANPAN
        ndTypes.add(255); // 255 CANTSTMAX
    }
    
    /**
     * Set the simulated node delay
     * @param nodeDelay Delay in ms
     */
    public void setDelay( int nodeDelay){
        _networkDelay = nodeDelay;
    }
    
    public int getDelay(){
        return _networkDelay;
    }
    
    public void setProcessIn( Boolean newval){
        _processIn = newval;
    }
    
    public void setProcessOut( Boolean newval){
        _processOut = newval;
    }

    public void setSendIn( Boolean newval){
        _sendIn = newval;
    }

    public void setSendOut( Boolean newval){
        _sendOut = newval;
    }
    
    public Boolean getProcessIn() {
        return _processIn;
    }
    
    public Boolean getProcessOut() {
        return _processOut;
    }    
    
    public Boolean getSendIn() {
        return _sendIn;
    }    
    
    public Boolean getSendOut() {
        return _sendOut;
    }

    // total events on module
    private void sendNUMEV(){
        CanReply r = new CanReply(4);
        r.setElement(0, CbusConstants.CBUS_NUMEV);
        r.setElement(1, getNodeNumber() >> 8);
        r.setElement(2, getNodeNumber() & 0xff);
        r.setElement(3, getTotalNodeEvents() );
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }

    private void evLearn(int nn, int en, int index, int val){
        
        if ( val < 0 || val > 255 ) {
            sendCMDERR(11);
            return;
        }
        if ( index < 0 || index > 255 ) {
            sendCMDERR(6);
            return;
        }
        provideNodeEvent(nn,en).setEvVar(index,val);
        if ( provideNodeEvent(nn,en).getIndex()< 0 ) {
            provideNodeEvent(nn,en).setIndex( getNextFreeIndex() );
        }
        sendWRACK();
    }
    
    // finds the next event index to allocate
    private int getNextFreeIndex(){
        int newIndex = 1;
        for (int i = 0; i < getTotalNodeEvents(); i++) {
            if ( newIndex <= getNodeEventByArrayID(i).getIndex() ) {
                newIndex = getNodeEventByArrayID(i).getIndex()+1;
            }
        }
        log.debug("dummy node sets index {}",newIndex);
        return newIndex;
    }
    
    /**
     * Add an event to the node, will not overwrite an existing event.
     *
     * @param newEvent the new event to be added
     */
    @Override
    public void addNewEvent( CbusNodeEvent newEvent ) {
        if (getTotalNodeEvents() == -1) {
            resetNodeEvents();
        }
        super.addNewEvent(newEvent);
    }
    
    private void sendENRSP(){
        int extraDelay = 5;
        for (int i = 0; i < getTotalNodeEvents(); i++) {
            
            if ( getNodeEventByArrayID(i).getIndex()<0 ) {
                getNodeEventByArrayID(i).setIndex( getNextFreeIndex() );
            }
            
            CanReply r = new CanReply(8);
            r.setElement(0, CbusConstants.CBUS_ENRSP);
            r.setElement(1, getNodeNumber() >> 8);
            r.setElement(2, getNodeNumber() & 0xff);
            r.setElement(3, getNodeEventByArrayID(i).getNn() >> 8);
            r.setElement(4, getNodeEventByArrayID(i).getNn() & 0xff);
            r.setElement(5, getNodeEventByArrayID(i).getEn() >> 8);
            r.setElement(6, getNodeEventByArrayID(i).getEn() & 0xff);
            r.setElement(7, getNodeEventByArrayID(i).getIndex() & 0xff);
            
            send.sendWithDelay(r,_sendIn,_sendOut,( _networkDelay + ( extraDelay * i ) ) );
        }
    }
    
    private void sendNEVAL( int index, int varIndex ){
        
        if ( index < 0 || index > 255 ){
            sendCMDERR(7);
            return;
        }
        
        // for future sim. of CMDERR5 support
        // if (varIndex>40) {
        //    
        //    sendCMDERR(5);
        //    return;
        // }
        
        try {
            CanReply r = new CanReply(6);
            r.setElement(0, CbusConstants.CBUS_NEVAL);
            r.setElement(1, getNodeNumber() >> 8);
            r.setElement(2, getNodeNumber() & 0xff);
            r.setElement(3, index);
            r.setElement(4, varIndex);
            r.setElement(5, getNodeEventByIndex(index).getEvVar(varIndex) );
            send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
        }
        catch ( NullPointerException e ){
            // log.warn("unable to send node event {} variable index {}", index , varIndex );
            sendCMDERR(12);
        }
        
    }
    
    private void sendPARAMS() {
        CanReply r = new CanReply(8);
        r.setElement(0, CbusConstants.CBUS_PARAMS);
        r.setElement(1, getParameter(1));
        r.setElement(2, getParameter(2));
        r.setElement(3, getParameter(3));
        r.setElement(4, getParameter(4));
        r.setElement(5, getParameter(5));
        r.setElement(6, getParameter(6));
        r.setElement(7, getParameter(7));
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }
    
    private void sendPNN() {
        CanReply r = new CanReply(6);
        r.setElement(0, CbusConstants.CBUS_PNN);
        r.setElement(1, getNodeNumber() >> 8);
        r.setElement(2, getNodeNumber() & 0xff);
        r.setElement(3, getParameter(1) );
        r.setElement(4, getParameter(3) );
        r.setElement(5, getParameter(8));
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }

    // Parameter answer
    private void sendPARAN( int index ){
        
        try {
            CanReply r = new CanReply(5);
            r.setElement(0, CbusConstants.CBUS_PARAN);
            r.setElement(1, getNodeNumber() >> 8);
            r.setElement(2, getNodeNumber() & 0xff);
            r.setElement(3, index);
            r.setElement(4, getParameter(index));
            send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            sendCMDERR(9);
        }
    }
    
    // NV Answer
    private void sendNVANS( int index ) {
        
        try {
            CanReply r = new CanReply(5);
            r.setElement(0, CbusConstants.CBUS_NVANS);
            r.setElement(1, getNodeNumber() >> 8);
            r.setElement(2, getNodeNumber() & 0xff);
            r.setElement(3, index);
            r.setElement(4, getNV(index));
            send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            sendCMDERR(10);
        }
    }

    private void setDummyNV(int index, int newval) {
        
        if ( newval<0 || newval > 255 ) {
            sendCMDERR(12);
            return;
        }
        
        if (index==0) {
            sendCMDERR(10);
            return;
        }
        try {
            setNV(index,newval);
            if ( getsendsWRACKonNVSET() ){
                sendWRACK();
            }
        }
        catch(ArrayIndexOutOfBoundsException e){
            sendCMDERR(10);
        }
    }
    
    private void sendCMDERR(int errorId) {
        CanReply r = new CanReply(4);
        r.setElement(0, CbusConstants.CBUS_CMDERR);
        r.setElement(1, getNodeNumber() >> 8);
        r.setElement(2, getNodeNumber() & 0xff);
        r.setElement(3, errorId );
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }
    
    private void sendWRACK(){
        CanReply r = new CanReply(3);
        r.setElement(0, CbusConstants.CBUS_WRACK);
        r.setElement(1, getNodeNumber() >> 8);
        r.setElement(2, getNodeNumber() & 0xff);        
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }
    
    // sim of FiLM Button
    public void flimButton() {
        // send request for node number
        if ( getParameter(3) >0 ) { // module type set
            setNodeInSetupMode(true);
            CanReply r = new CanReply(3);
            r.setElement(0, CbusConstants.CBUS_RQNN);
            r.setElement(1, getNodeNumber() >> 8);
            r.setElement(2, getNodeNumber() & 0xff);
            send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
        }
    }
    
    public void setDummyType(int manu, int type){
        
        resetNodeEvents();
        setNodeInFLiMMode(false);
        
        CbusNodeConstants.setDummyNodeParameters(this,manu,type);
        
        log.info("Simulated CBUS Node: {}", CbusNodeConstants.getModuleType(manu,type ) );
    }

    
    public void setPane(NdPane pane) {
        _pane = pane;
    }
    
    private void setDNN(int nn){
        setNodeNumber(nn);
        setNodeInFLiMMode(true);
        setNodeInSetupMode(false);
        if (_pane != null) {
            _pane.setNodeNum(getNodeNumber());
        }
        CanReply r = new CanReply(3);
        r.setElement(0, CbusConstants.CBUS_NNACK);
        r.setElement(1, getNodeNumber() >> 8);
        r.setElement(2, getNodeNumber() & 0xff);
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }

    private void passMessage(CanMessage m) {
        // log.debug("dummy node canMessage {}",m);
        if ( getParameter(3) == 0 ) { // module type unset
            return;
        }
        int opc = m.getElement(0);
        if ( getNodeInSetupMode() ) {
            if ( opc == CbusConstants.CBUS_RQNP ) { // Request Node Parameters
                sendPARAMS();
            }
            if ( opc == CbusConstants.CBUS_SNN ) { // Set Node Number
                setDNN( ( m.getElement(1) * 256 ) + m.getElement(2) );
            }
            if ( opc == CbusConstants.CBUS_RQMN ) { // Request Module Name
                
                byte[] byteArr = jmri.util.StringUtil.fullTextToHexArray( getNodeNameFromName(),7 );
                CanReply r = new CanReply(8);
                r.setElement(0, CbusConstants.CBUS_NAME);
                r.setElement(1, byteArr[0] & 0xff);
                r.setElement(2, byteArr[1] & 0xff);
                r.setElement(3, byteArr[2] & 0xff);
                r.setElement(4, byteArr[3] & 0xff);
                r.setElement(5, byteArr[4] & 0xff);
                r.setElement(6, byteArr[5] & 0xff);
                r.setElement(7, byteArr[6] & 0xff);
                send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
                
            }
        }
        if ( getNodeInFLiMMode() ) {
            if ( opc == CbusConstants.CBUS_QNN ) { // Query Nodes
                sendPNN();
            }
            if (getNodeNumber() == ( m.getElement(1) * 256 ) + m.getElement(2) ) {
                if ( opc == CbusConstants.CBUS_NNULN ) { // Node exit learn mode
                    setNodeInLearnMode(false);
                }
                if ( opc == CbusConstants.CBUS_NNLRN ) { // Node enter learn mode
                    setNodeInLearnMode(true);
                }
                if ( opc == CbusConstants.CBUS_RQNPN ) { // Request Node Parameters by Index
                    sendPARAN(m.getElement(3));
                }
                if ( opc == CbusConstants.CBUS_NVRD ) { // Request Node Variable by Index
                    sendNVANS(m.getElement(3));
                }
                if ( opc == CbusConstants.CBUS_NVSET ) { // Set Node Variable
                    setDummyNV(m.getElement(3),m.getElement(4));
                }
                if ( opc == CbusConstants.CBUS_RQEVN ) { // Request number of events
                    sendNUMEV();
                }
                if ( opc == CbusConstants.CBUS_NERD ) { // Readback all stored events in node
                    sendENRSP();
                }
                if ( opc == CbusConstants.CBUS_REVAL ) { // Request for read of an event variable
                    sendNEVAL(m.getElement(3),m.getElement(4));
                }
            }
            
            if ( getNodeInLearnMode() ){
                if ( opc == CbusConstants.CBUS_EVLRN ) { // Teach node event
                    evLearn(
                    ( ( m.getElement(1) * 256 ) + m.getElement(2) ),
                    ( ( m.getElement(3) * 256 ) + m.getElement(4) ),
                    m.getElement(5),
                    m.getElement(6) );
                }
                if ( opc == CbusConstants.CBUS_EVULN ) { // Node Unlearn event
                    removeEvent( ( m.getElement(1) * 256 ) + m.getElement(2) ,
                        ( ( m.getElement(3) * 256 ) + m.getElement(4) ) );
                }
                if ( opc ==  CbusConstants.CBUS_NNCLR) { // clear all events
                    if ( ( ( m.getElement(1) * 256 ) + m.getElement(2) ) ==  getNodeNumber() ) {
                        // no response expected
                        resetNodeEvents();
                    }
                }
            }
        }
    }
    
    @Override
    public void message(CanMessage m) {
        if ( m.isExtended() || m.isRtr() ) {
            return;
        }
        if ( _processOut ) {
            passMessage(m);
        }
    }

    @Override
    public void reply(CanReply r) {
        if ( r.isExtended() || r.isRtr() ) {
            return;
        }
        if ( _processIn ) {
            CanMessage msg = new CanMessage(r);
            passMessage(msg);
        }
    }

    @Override
    public void dispose(){
        if (tc != null) {
            tc.removeCanListener(this);
        }
        send = null;
    }

    private static final Logger log = LoggerFactory.getLogger(CbusDummyNode.class);

}
