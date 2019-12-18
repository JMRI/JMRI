package jmri.jmrix.can.cbus.simulator;

import java.util.ArrayList;
import java.util.Random;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.jmrix.can.cbus.CbusSend;
import jmri.jmrix.can.TrafficController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulating event request responses.
 *
 * @author Steve Young Copyright (C) 2018
 * @see CbusSimulator
 * @since 4.15.2
 */
public class CbusEventResponder implements CanListener {
    
    private TrafficController tc;
    private CanSystemConnectionMemo memo;
    private int _node;
    private int _mode;
    private int _networkDelay;
    private Boolean _processIn;
    private Boolean _processOut;
    private Boolean _sendIn;
    private Boolean _sendOut;
    private CbusSend send;
    
    public ArrayList<String> evModes = new ArrayList<String>();
    public ArrayList<String> evModesTip = new ArrayList<String>();
    
    public CbusEventResponder( CanSystemConnectionMemo memod ){
        memo = memod;
        if (memo != null) {
            tc = memo.getTrafficController();
            addTc(tc);
        }
        init();
    }
    
    private void init(){
        send = new CbusSend(memo);
        _mode = 1;
        _node = -1;
        _networkDelay = 50;
        
        _processIn=false;
        _processOut=true;
        _sendIn=true;
        _sendOut=false;
        
        
        evModes.add(Bundle.getMessage("HighlightDisabled"));
        evModesTip.add(null);
        
        evModes.add(Bundle.getMessage("onOffRand"));
        evModesTip.add(null);          
        
        evModes.add(Bundle.getMessage("odOnEvOff"));
        evModesTip.add(null);         
        
        evModes.add(Bundle.getMessage("CbusEventOn"));
        evModesTip.add(null);         
        
        evModes.add(Bundle.getMessage("CbusEventOff"));
        evModesTip.add(null);
        
        log.info("Simulated Event Responses: {}", evModes.get(_mode) );
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

    public void setMode(int mode){
        _mode = mode;
    }
    
    public int getMode() {
        return _mode;
    }

    public void setNode(int node){
        _node = node;
    }
    
    public int getNode() {
        return _node;
    }

    private void processEventforResponse(CanMessage m) {
        
        if (!CbusOpCodes.isEventRequest(m.getElement(0))) {
            return;
        }
        
        if ( _mode == 0 ) {
            return;
        }
        
        int opc = m.getElement(0);
        int nn = ( m.getElement(1) * 256 ) + m.getElement(2);
        // log.debug("canframe {} received node {} spinner {} to process with mode {}",m,nn,_node,_mode); 
        
        if ( ( _node == -1 ) || ( _node == nn ) ) {
            Boolean sendOn = false;
            
            if ( _mode == 1 ){ // random
                Random random = new Random();
                sendOn = random.nextBoolean();
            }
            else if ( _mode == 2 ){ // Odd On / Even Off
                if ( m.getElement(4) % 2 == 0 ) {
                    sendOn = false;
                } else {
                    sendOn = true;
                }
            }
            else if ( _mode == 3 ){ // on
                sendOn = true;
            }
            else if ( _mode == 4 ){ // off
                sendOn = false;
            }
            
            int newopc = CbusConstants.CBUS_AROF;
            if ( ( opc == CbusConstants.CBUS_ASRQ ) || ( nn == 0 ) ){
                if (sendOn) {
                    newopc = CbusConstants.CBUS_ARSON;
                } else {
                    newopc = CbusConstants.CBUS_ARSOF;
                }
            }
            else if ( opc == CbusConstants.CBUS_AREQ ){
                
                if (sendOn) {
                    newopc = CbusConstants.CBUS_ARON;
                } else {
                    newopc = CbusConstants.CBUS_AROF;
                }
            }            
            CanReply r = new CanReply(5);
            r.setElement(0, newopc);
            r.setElement(1, m.getElement(1));
            r.setElement(2, m.getElement(2));
            r.setElement(3, m.getElement(3));
            r.setElement(4, m.getElement(4));
            send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
        }
    }
    
    @Override
    public void message(CanMessage m) {
        if ( m.isExtended() || m.isRtr() ) {
            return;
        }
        if ( _processOut ) {
            processEventforResponse(m);
        }
    }

    @Override
    public void reply(CanReply r) {
        if ( r.isExtended() || r.isRtr() ) {
            return;
        }
        if ( _processIn ) {
            CanMessage m = new CanMessage(r);
            processEventforResponse(m);
        }
    }

    public void dispose(){
        if (tc != null) {
            tc.removeCanListener(this);
        }
        send = null;
    }
    
    private static final Logger log = LoggerFactory.getLogger(CbusEventResponder.class);

}
