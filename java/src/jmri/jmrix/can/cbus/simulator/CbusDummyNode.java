package jmri.jmrix.can.cbus.simulator;

import java.util.ArrayList;
import jmri.jmrix.can.CanFrame;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.jmrix.can.cbus.simulator.CbusSimulator;
import jmri.jmrix.can.cbus.swing.simulator.SimulatorPane.NdPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CbusDummyNode {
    
    private CbusSimulator _sim;
    private int _simId;
    private int[] _parameters;
    private int[] _nvArray;
    private int _flags;
    private int _simType;
    private int _nn;
    private int _networkDelay;
    private Boolean _inFLiM;
    private Boolean _inSetup;
    private Boolean _learnMode;
    private Boolean _sendsWRACKonNVSET;
    private NdPane _pane;
    public ArrayList<CbusNodeEvent> _ndEv;
    
    public CbusDummyNode( int type, CbusSimulator sim, int simId ){
        _sim = sim;
        _simId = simId;
        _simType = type;
        _inFLiM = false;
        _inSetup = false;
        _learnMode = false;
        _nn=0;
        _networkDelay = 40;
        _pane = null;
        setDummyType(_simType);
        _ndEv = new ArrayList<CbusNodeEvent>();
    }
    
    protected int getSimId(){
        return _simId;
    }

    public void setParameters(){
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
    public void sendNUMEV(){
        CanReply r = new CanReply(4);
        r.setElement(0, CbusConstants.CBUS_NUMEV);
        r.setElement(1, _nn >> 8);
        r.setElement(2, _nn & 0xff);
        r.setElement(3, _ndEv.size());
        _sim.sendReplyWithDelay( r,_networkDelay );
    }

    public void evLearn(int nn, int en, int index, int val){
        CbusNodeEvent nodeEvent = getNodeEvent(nn,en);
        if (nodeEvent==null){
            nodeEvent = new CbusNodeEvent(nn,en,_nn,getNextFreeIndex(),_parameters[5]);
            _ndEv.add(nodeEvent);
        }
        nodeEvent.setEvVar(index,val);
        sendWRACK();
    }
    
    public void evUnLearn(int nn, int en){
        _ndEv.remove(getNodeEvent(nn, en));
    }

    public CbusNodeEvent getNodeEvent(int nn, int en) {
        for (int i = 0; i < _ndEv.size(); i++) {
            if ( ( _ndEv.get(i).getNn() == nn ) && ( _ndEv.get(i).getEn() == en )) {
                return _ndEv.get(i);
            }
        }
        return null;
    }

    public CbusNodeEvent getNodeEventByIndex(int index) {
        for (int i = 0; i < _ndEv.size(); i++) {
            if ( _ndEv.get(i).getIndex() == index ) {
                return _ndEv.get(i);
            }
        }
        return null;
    }
    
    // finds the next event index to allocate
    public int getNextFreeIndex(){
        int newIndex = 1;
        for (int i = 0; i < _ndEv.size(); i++) {
            if ( newIndex <= _ndEv.get(i).getIndex() ) {
                newIndex = _ndEv.get(i).getIndex()+1;
            }
        }
        return newIndex;
    }
    
    public void sendENRSP(){
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
            _sim.sendReplyWithDelay( r,_networkDelay );
        }
    }
    
    public void sendNEVAL( int index, int varIndex ){
        CanReply r = new CanReply(6);
        r.setElement(0, CbusConstants.CBUS_NEVAL);
        r.setElement(1, _nn >> 8);
        r.setElement(2, _nn & 0xff);
        r.setElement(3, index);
        r.setElement(4, varIndex);
        r.setElement(5, getNodeEventByIndex(index).getEvVar(varIndex) );
        _sim.sendReplyWithDelay( r,_networkDelay );        
    }
    
    public int getParamater(int number) {
        return _parameters[number];
    }
    
    public void sendPARAMS() {
        CanReply r = new CanReply(8);
        r.setElement(0, CbusConstants.CBUS_PARAMS);
        r.setElement(1, _parameters[1]);
        r.setElement(2, _parameters[2]);
        r.setElement(3, _parameters[3]);
        r.setElement(4, _parameters[4]);
        r.setElement(5, _parameters[5]);
        r.setElement(6, _parameters[6]);
        r.setElement(7, _parameters[7]);
        _sim.sendReplyWithDelay( r,_networkDelay );        
    }
    
    public void sendPNN() {
        CanReply r = new CanReply(6);
        r.setElement(0, CbusConstants.CBUS_PNN);
        r.setElement(1, _nn >> 8);
        r.setElement(2, _nn & 0xff);
        r.setElement(3, _parameters[1]);
        r.setElement(4, _parameters[3]);
        r.setElement(5, _flags);
        _sim.sendReplyWithDelay( r,_networkDelay );        
    }

    public void sendPARAN( int index ){
        CanReply r = new CanReply(5);
        r.setElement(0, CbusConstants.CBUS_PARAN);
        r.setElement(1, _nn >> 8);
        r.setElement(2, _nn & 0xff);
        r.setElement(3, index);
        r.setElement(4, _parameters[index]);
        _sim.sendReplyWithDelay( r,_networkDelay );
    }
    
    // NV Answer
    public void sendNVANS( int index ) {
        CanReply r = new CanReply(5);
        r.setElement(0, CbusConstants.CBUS_NVANS);
        r.setElement(1, _nn >> 8);
        r.setElement(2, _nn & 0xff);
        r.setElement(3, index);
        r.setElement(4, _nvArray[index]);
        _sim.sendReplyWithDelay( r,_networkDelay );        
    }

    public void setNV(int index, int newval) {
        _nvArray[index]=newval;
        if (_sendsWRACKonNVSET){
            sendWRACK();
        }
    }
    
    public void sendWRACK(){
        CanReply r = new CanReply(3);
        r.setElement(0, CbusConstants.CBUS_WRACK);
        r.setElement(1, _nn >> 8);
        r.setElement(2, _nn & 0xff);        
        _sim.sendReplyWithDelay( r,_networkDelay ); 
    }
    
    // sim of FiLM Button
    public void resetNode() {
        // send request for node number
        if ( _simType >0 ) {
            _inSetup = true;
            CanReply r = new CanReply(3);
            r.setElement(0, CbusConstants.CBUS_RQNN);
            r.setElement(1, _nn >> 8);
            r.setElement(2, _nn & 0xff);
            _sim.sendReplyWithDelay( r,_networkDelay );
        }
    }
    
    public void setDummyType(int type){
        _simType = type;
        setParameters();
        if (_sim != null) {
            log.info("Simulated Node: {}", CbusOpCodes.getModuleType(165,_simType) );
        }
    }
    
    protected int getDummyType() {
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
        _sim.sendReplyWithDelay( r,_networkDelay );
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
    private static final Logger log = LoggerFactory.getLogger(CbusDummyNode.class);
}
