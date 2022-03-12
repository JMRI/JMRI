package jmri.jmrix.can.cbus.simulator;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusOpCodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulating event request responses.
 *
 * @author Steve Young Copyright (C) 2018
 * @see CbusSimulator
 * @since 4.15.2
 */
public class CbusEventResponder extends CbusSimCanListener {
    
    private int _node;
    private int _mode;
    
    public ArrayList<String> evModes;
    public ArrayList<String> evModesTip;
    
    public CbusEventResponder( CanSystemConnectionMemo memod ){
        super(memod,null);
        init();
    }
    
    private void init(){
        _mode = 1;
        _node = -1;
        setDelay(50);
        
        evModes = new ArrayList<>();
        evModesTip = new ArrayList<>();

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

    @Override
    protected void startProcessFrame(AbstractMessage m) {
        if (!CbusOpCodes.isEventRequest(m.getElement(0))
            || ( _mode == 0 )
            || ( _node != -1  && _node != CbusMessage.getNodeNumber(m) ) ) {
            return;
        }
                
        int newopc = CbusConstants.CBUS_AROF;
        if ( CbusMessage.getOpcode(m) == CbusConstants.CBUS_ASRQ ) {
            if (getOnOrOffFromMode(m)) {
                newopc = CbusConstants.CBUS_ARSON;
            } else {
                newopc = CbusConstants.CBUS_ARSOF;
            }
        }
        else if (getOnOrOffFromMode(m)) {
            newopc = CbusConstants.CBUS_ARON;
        }
        sendResponse(newopc,m);
    }
    
    private void sendResponse( int newopc, AbstractMessage m){
        CanReply r = new CanReply(5);
        r.setNumDataElements(5);
        r.setElement(0, newopc);
        r.setElement(1, m.getElement(1));
        r.setElement(2, m.getElement(2));
        r.setElement(3, m.getElement(3));
        r.setElement(4, m.getElement(4));
        send.sendWithDelay(r,getSendIn(),getSendOut(),getDelay());
    }
    
    private boolean getOnOrOffFromMode( AbstractMessage m ){
        boolean sendOn = false;
        switch (_mode) {
            case 1: // random
                sendOn = ThreadLocalRandom.current().nextBoolean();
                break;
            case 2: // Odd On / Even Off
                sendOn = m.getElement(4) % 2 != 0;
                break;
            case 3: // on
                sendOn = true;
                break;
            case 4: // off
                sendOn = false;
                break;
            default:
                break;
        }
        return sendOn;
    }
    
    private static final Logger log = LoggerFactory.getLogger(CbusEventResponder.class);

}
