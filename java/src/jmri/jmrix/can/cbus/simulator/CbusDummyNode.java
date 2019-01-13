package jmri.jmrix.can.cbus.simulator;

import java.util.ArrayList;
import jmri.jmrix.can.CanFrame;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.jmrix.can.cbus.CbusSend;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.jmrix.can.cbus.swing.simulator.NdPane;
import jmri.jmrix.can.TrafficController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simultaing a MERG CBUS Node
 * @author Steve Young Copyright (C) 2018 2019
 * @see CbusSimulator
 * @since 4.15.2
 */
public class CbusDummyNode implements CanListener {
    
    private TrafficController tc;
    private CanSystemConnectionMemo memo;
    
    private int[] _parameters;
    private int[] _nvArray;
    private int _flags;
    private int _simType;
    private int _nn;
    private Boolean _inFLiM;
    private Boolean _inSetup;
    private Boolean _learnMode;
    private Boolean _sendsWRACKonNVSET;
    private NdPane _pane;
    private ArrayList<CbusNodeEvent> _ndEv;
    public static ArrayList<Integer> ndTypes;
    
    private int _networkDelay;
    private Boolean _processIn;
    private Boolean _processOut;
    private Boolean _sendIn;
    private Boolean _sendOut;
    private CbusSend send;
    
    public CbusDummyNode( CanSystemConnectionMemo sysmemo ){
        _simType = 0;
        memo = sysmemo;
        if (memo != null) {
            tc = memo.getTrafficController();
            tc.addCanListener(this);
        }
        init();
    }
    
    private void init(){
        send = new CbusSend(memo);
        _inFLiM = false;
        _inSetup = false;
        _learnMode = false;
        _nn=0;
        _networkDelay = 40;
        _pane = null;
        
        _ndEv = new ArrayList<CbusNodeEvent>();

        _processIn=false;
        _processOut=true;
        _sendIn=true;
        _sendOut=false;
        
        ndTypes = new ArrayList<Integer>();
        
        ndTypes.add(0); // 0 SlIM
        ndTypes.add(29); // 29 CANPAN
        
        setDummyType(_simType);
    }
    
    public void setDelay( int newval){
        _networkDelay = newval;
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

    private void setParameters(){
        // Para 1 The manufacturer ID
        // Para 2 Minor code version as an alphabetic character (ASCII)
        // Para 3 Manufacturer module identifier as a HEX numeric
        // Para 4 Number of supported events as a HEX numeric
        // Para 5 Number of Event Variables per event as a HEX numeric
        // Para 6 Number of supported Node Variables as a HEX numeric
        // Para 7 Major version
        // Para 8;  Node flags
        // Para 9;  Processor type
        // Para 10; Bus type
        // Para 11; load address, 4 bytes
        // Para 15; CPU manufacturer's id as read from the chip config space, 4 bytes
        // Para 19; CPU manufacturer code
        // Para 20; Beta revision (numeric), or 0 if release

        _sendsWRACKonNVSET = true;
    
        if (_simType == 29) { // CANPAN
            _sendsWRACKonNVSET = false;
            _parameters = new int[]{ 
            20, /* 0 num parameters   */
            165, /* 1 manufacturer ID   */
            89, /* 2 Minor code version   */
            29, /* 3 Manufacturer module identifier   */
            128, /* 4 Number of supported events   */
            13, /* 5 Number of Event Variables per event   */
            1, /* 6 Number of Node Variables   */
            1, /* 7 Major version   */
            13, /* 8 Node flags   */ 
            13, /* 9 Processor type   */
            1, /* 10 Bus type   */
            0, /* 11 load address, 1/4 bytes   */
            8, /* 12 load address, 2/4 bytes   */
            0, /* 13 load address, 3/4 bytes   */
            0, /* 14 load address, 4/4 bytes   */
            0, /* 15 CPU manufacturer's id 1/4  */
            0, /* 16 CPU manufacturer's id 2/4  */
            0, /* 17 CPU manufacturer's id 3/4  */
            0, /* 18 CPU manufacturer's id 4/4  */
            1, /* 19 CPU manufacturer code   */
            1, /* 20 Beta revision   */
            };
            
            // 0th NV is total NVs
            _nvArray = new int[]{ 1, 0 };
            return;
        }
        
        _parameters = new int[]{ 0 };
        _nvArray = new int[]{ 0 };
        _ndEv = null;
        _ndEv = new ArrayList<CbusNodeEvent>();
        _flags=7;
    }

    // total events on module
    private void sendNUMEV(){
        CanReply r = new CanReply(4);
        r.setElement(0, CbusConstants.CBUS_NUMEV);
        r.setElement(1, _nn >> 8);
        r.setElement(2, _nn & 0xff);
        r.setElement(3, _ndEv.size());
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }

    private void evLearn(int nn, int en, int index, int val){
        CbusNodeEvent nodeEvent = getNodeEvent(nn,en);
        if (nodeEvent==null){
            nodeEvent = new CbusNodeEvent(nn,en,_nn,getNextFreeIndex(),_parameters[5]);
            _ndEv.add(nodeEvent);
        }
        nodeEvent.setEvVar(index,val);
        sendWRACK();
    }
    
    private void evUnLearn(int nn, int en){
        _ndEv.remove(getNodeEvent(nn, en));
    }

    private CbusNodeEvent getNodeEvent(int nn, int en) {
        for (int i = 0; i < _ndEv.size(); i++) {
            if ( ( _ndEv.get(i).getNn() == nn ) && ( _ndEv.get(i).getEn() == en )) {
                return _ndEv.get(i);
            }
        }
        return null;
    }

    private CbusNodeEvent getNodeEventByIndex(int index) {
        for (int i = 0; i < _ndEv.size(); i++) {
            if ( _ndEv.get(i).getIndex() == index ) {
                return _ndEv.get(i);
            }
        }
        return null;
    }
    
    // finds the next event index to allocate
    private int getNextFreeIndex(){
        int newIndex = 1;
        for (int i = 0; i < _ndEv.size(); i++) {
            if ( newIndex <= _ndEv.get(i).getIndex() ) {
                newIndex = _ndEv.get(i).getIndex()+1;
            }
        }
        return newIndex;
    }
    
    private void sendENRSP(){
        for (int i = 0; i < _ndEv.size(); i++) {
            CanReply r = new CanReply(8);
            r.setElement(0, CbusConstants.CBUS_ENRSP);
            r.setElement(1, _nn >> 8);
            r.setElement(2, _nn & 0xff);
            r.setElement(3, _ndEv.get(i).getNn() >> 8);
            r.setElement(4, _ndEv.get(i).getNn() & 0xff);
            r.setElement(5, _ndEv.get(i).getEn() >> 8);
            r.setElement(6, _ndEv.get(i).getEn() & 0xff);
            r.setElement(7, _ndEv.get(i).getIndex() );
            send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
        }
    }
    
    private void sendNEVAL( int index, int varIndex ){
        CanReply r = new CanReply(6);
        r.setElement(0, CbusConstants.CBUS_NEVAL);
        r.setElement(1, _nn >> 8);
        r.setElement(2, _nn & 0xff);
        r.setElement(3, index);
        r.setElement(4, varIndex);
        r.setElement(5, getNodeEventByIndex(index).getEvVar(varIndex) );
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }
    
    // move to private in future
    public int getParamater(int number) {
        return _parameters[number];
    }
    
    private void sendPARAMS() {
        CanReply r = new CanReply(8);
        r.setElement(0, CbusConstants.CBUS_PARAMS);
        r.setElement(1, _parameters[1]);
        r.setElement(2, _parameters[2]);
        r.setElement(3, _parameters[3]);
        r.setElement(4, _parameters[4]);
        r.setElement(5, _parameters[5]);
        r.setElement(6, _parameters[6]);
        r.setElement(7, _parameters[7]);
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }
    
    private void sendPNN() {
        CanReply r = new CanReply(6);
        r.setElement(0, CbusConstants.CBUS_PNN);
        r.setElement(1, _nn >> 8);
        r.setElement(2, _nn & 0xff);
        r.setElement(3, _parameters[1]);
        r.setElement(4, _parameters[3]);
        r.setElement(5, _flags);
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }

    private void sendPARAN( int index ){
        CanReply r = new CanReply(5);
        r.setElement(0, CbusConstants.CBUS_PARAN);
        r.setElement(1, _nn >> 8);
        r.setElement(2, _nn & 0xff);
        r.setElement(3, index);
        r.setElement(4, _parameters[index]);
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }
    
    // NV Answer
    private void sendNVANS( int index ) {
        CanReply r = new CanReply(5);
        r.setElement(0, CbusConstants.CBUS_NVANS);
        r.setElement(1, _nn >> 8);
        r.setElement(2, _nn & 0xff);
        r.setElement(3, index);
        r.setElement(4, _nvArray[index]);
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }

    private void setNV(int index, int newval) {
        _nvArray[index]=newval;
        if (_sendsWRACKonNVSET){
            sendWRACK();
        }
    }
    
    private void sendWRACK(){
        CanReply r = new CanReply(3);
        r.setElement(0, CbusConstants.CBUS_WRACK);
        r.setElement(1, _nn >> 8);
        r.setElement(2, _nn & 0xff);        
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }
    
    // sim of FiLM Button
    public void flimButton() {
        // send request for node number
        if ( _simType >0 ) {
            _inSetup = true;
            CanReply r = new CanReply(3);
            r.setElement(0, CbusConstants.CBUS_RQNN);
            r.setElement(1, _nn >> 8);
            r.setElement(2, _nn & 0xff);
            send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
        }
    }
    
    public void setDummyType(int type){
        _simType = type;
        setParameters();
        log.info("Simulated CBUS Node: {}", CbusOpCodes.getModuleType(165,_simType) );
    }
    
    public int getDummyType() {
        return _simType;
    }
    
    public void setPane(NdPane pane) {
        _pane = pane;
    }
    
    private void setDNN(int nn){
        _nn = nn;
        _inFLiM = true;
        _inSetup = false;
        if (_pane != null) {
            _pane.setNodeNum(_nn);
        }
        CanReply r = new CanReply(3);
        r.setElement(0, CbusConstants.CBUS_NNACK);
        r.setElement(1, _nn >> 8);
        r.setElement(2, _nn & 0xff);
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }
    
    private void setLearnMode(Boolean newval) {
        _learnMode = newval;
    }

    public void passMessage(CanFrame m) {
        // log.warn("dummy node canframe {}",m);
        if ( _simType == 0 ) {
            return;
        }
        int opc = m.getElement(0);
        int nn = ( m.getElement(1) * 256 ) + m.getElement(2);
        if ( _inSetup ) {
            if ( opc == CbusConstants.CBUS_RQNP ) { // Request Node Parameters
                sendPARAMS();
            }
            if ( opc == CbusConstants.CBUS_SNN ) { // Set Node Number
                setDNN(nn);
            }
        }
        if ( _inFLiM ) {
            if ( opc == CbusConstants.CBUS_QNN ) { // Query Nodes
                sendPNN();
            }
            if (_nn == nn) {
                if ( opc == CbusConstants.CBUS_NNULN ) { // Node exit learn mode
                    setLearnMode(false);
                }
                if ( opc == CbusConstants.CBUS_NNLRN ) { // Node enter learn mode
                    setLearnMode(true);
                }
                if ( opc == CbusConstants.CBUS_RQNPN ) { // Request Node Parameters by Index
                    sendPARAN(m.getElement(3));
                }
                if ( opc == CbusConstants.CBUS_NVRD ) { // Request Node Variable by Index
                    sendNVANS(m.getElement(3));
                }
                if ( opc == CbusConstants.CBUS_NVSET ) { // Set Node Variable
                    setNV(m.getElement(3),m.getElement(4));
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
            
            if (_learnMode){
                if ( opc == CbusConstants.CBUS_EVLRN ) { // Teach node event
                    evLearn(
                    ( ( m.getElement(1) * 256 ) + m.getElement(2) ),
                    ( ( m.getElement(3) * 256 ) + m.getElement(4) ),
                    m.getElement(5),
                    m.getElement(6) );
                }
                if ( opc == CbusConstants.CBUS_EVULN ) { // Teach node event
                    evUnLearn( ( m.getElement(1) * 256 ) + m.getElement(2) ,
                        ( ( m.getElement(3) * 256 ) + m.getElement(4) ) );
                }
            }
        }
    }
    
    @Override
    public void message(CanMessage m) {
        if ( _processOut ) {
            CanFrame test = m;
            passMessage(test);
        }
    }

    @Override
    public void reply(CanReply r) {
        if ( _processIn ) {
            CanFrame test = r;
            passMessage(test);
        }
    }

    public void dispose(){
        if (tc != null) {
            tc.removeCanListener(this);
        }
        send = null;
    }

    private static final Logger log = LoggerFactory.getLogger(CbusDummyNode.class);
}
