package jmri.jmrix.can.cbus.simulator;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Random;
import jmri.jmrix.can.CanFrame;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.simulator.CbusSimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CbusEventResponder {
    
    private CbusSimulator _sim;
    private int _simId;
    private int _node;
    private int _mode;
    private int _networkDelay;
    
    public CbusEventResponder( int mode, CbusSimulator sim, int simId ){
        _sim = sim;
        _simId = simId;
        _mode = mode;
        _node = -1;
        _networkDelay = CbusSimulator.DEFAULT_DELAY;
        if ( _sim != null ) {
            log.info("Simulated Event Responses: {}",CbusSimulator.evModes.get(_mode) );
        }
    }

    protected int getSimId(){
        return _simId;
    }
    
    public void setMode(int mode){
        _mode = mode;
    }
    
    protected int getMode() {
        return _mode;
    }

    public void setNode(int node){
        _node = node;
    }
    
    protected int getNode() {
        return _node;
    }
    
    public void processEventforResponse(CanFrame m) {
        if ( _mode == 0 ) {
            return;
        }
        
        int opc = m.getElement(0);
        int nn = ( m.getElement(1) * 256 ) + m.getElement(2);
        log.debug("canframe {} received node {} spinner {} to process with mode {}",m,nn,_node,_mode); 
        
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
            _sim.sendReplyWithDelay( r,_networkDelay );
        }
    }
    private static final Logger log = LoggerFactory.getLogger(CbusEventResponder.class);
}
