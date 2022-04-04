package jmri.jmrix.can.cbus.simulator;

import javax.annotation.CheckForNull;

import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;

import jmri.jmrix.can.cbus.node.*;
import jmri.jmrix.can.cbus.swing.simulator.NdPane;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Simulating a MERG CBUS Node.
 *
 * @author Steve Young Copyright (C) 2018 2019
 * @see CbusSimulator
 * @since 4.15.2
 */
public class CbusDummyNode extends CbusNode {
    
    private NdPane _pane;
    
    /**
     * Create a new CbusDummyNode.
     * @param sysmemo System Connection to use, can be null.
     * @param nodenumber the initial Node Number.
     */
    public CbusDummyNode( @CheckForNull CanSystemConnectionMemo sysmemo, int nodenumber ){
        super( sysmemo, nodenumber );
        super.setNodeInFLiMMode(false);
        setCanId(sysmemo);
        _pane = null;
    }
    
    /**
     * Uses in-class CanListener
     * {@inheritDoc}
     */    
    @Override
    public CbusNodeCanListener getNewCanListener(){
        canListener = new CbusDummyNodeCanListener(_memo,this);
        return canListener;
    }
    
    private CbusDummyNodeCanListener canListener;
    
    // total events on module
    protected void sendNUMEV(){
        CanReply r = new CanReply(4);
        r.setElement(0, CbusConstants.CBUS_NUMEV);
        r.setElement(1, getNodeNumber() >> 8);
        r.setElement(2, getNodeNumber() & 0xff);
        r.setElement(3, (Math.max(0, (getNodeEventManager().getTotalNodeEvents())) & 0xff ) );
        send.sendWithDelay(r,canListener.getSendIn(),canListener.getSendOut(),canListener.getDelay());
    }

    protected void sendENRSP(){
        int extraDelay = 5;
        for (int i = 0; i < getNodeEventManager().getTotalNodeEvents(); i++) {
            
            CbusNodeEvent ndEv = getNodeEventManager().getNodeEventByArrayID(i);
            if (ndEv==null){
                break;
            }
            
            if ( ndEv.getIndex()<0 ) {
                ndEv.setIndex( getNodeEventManager().getNextFreeIndex() );
            }
            
            CanReply r = new CanReply(8);
            r.setElement(0, CbusConstants.CBUS_ENRSP);
            r.setElement(1, getNodeNumber() >> 8);
            r.setElement(2, getNodeNumber() & 0xff);
            r.setElement(3, ndEv.getNn() >> 8);
            r.setElement(4, ndEv.getNn() & 0xff);
            r.setElement(5, ndEv.getEn() >> 8);
            r.setElement(6, ndEv.getEn() & 0xff);
            r.setElement(7, ndEv.getIndex() & 0xff);
            send.sendWithDelay(r,canListener.getSendIn(),canListener.getSendOut(),canListener.getDelay() + ( extraDelay * i ) );
        }
    }
    
    protected void sendNEVAL( int index, int varIndex ){
        
        CbusNodeEvent _ndEv = getNodeEventManager().getNodeEventByIndex(index);
        
        if ( index < 0 || index > 255 ){
            sendCMDERR(7);
        }
        
        // for future sim. of CMDERR5 support
        // if (varIndex>40) {
        //    
        //    sendCMDERR(5);
        //    return;
        // }
        
        else if (_ndEv==null){
            sendCMDERR(12);
        }
        else {
            CanReply r = new CanReply(6);
            r.setElement(0, CbusConstants.CBUS_NEVAL);
            r.setElement(1, getNodeNumber() >> 8);
            r.setElement(2, getNodeNumber() & 0xff);
            r.setElement(3, index & 0xff);
            r.setElement(4, varIndex & 0xff);
            r.setElement(5, _ndEv.getEvVar(varIndex) & 0xff);
            send.sendWithDelay(r,canListener.getSendIn(),canListener.getSendOut(),canListener.getDelay());
        }
    }
    
    protected void sendPARAMS() {
        CanReply r = new CanReply(8);
        r.setElement(0, CbusConstants.CBUS_PARAMS);
        r.setElement(1, getNodeParamManager().getParameter(1) & 0xff );
        r.setElement(2, getNodeParamManager().getParameter(2) & 0xff );
        r.setElement(3, getNodeParamManager().getParameter(3) & 0xff);
        r.setElement(4, getNodeParamManager().getParameter(4) & 0xff);
        r.setElement(5, getNodeParamManager().getParameter(5) & 0xff);
        r.setElement(6, getNodeParamManager().getParameter(6) & 0xff);
        r.setElement(7, getNodeParamManager().getParameter(7) & 0xff);
        send.sendWithDelay(r,canListener.getSendIn(),canListener.getSendOut(),canListener.getDelay());
    }
    
    protected void sendPNN() {
        CanReply r = new CanReply(6);
        r.setElement(0, CbusConstants.CBUS_PNN);
        r.setElement(1, getNodeNumber() >> 8);
        r.setElement(2, getNodeNumber() & 0xff);
        r.setElement(3, getNodeParamManager().getParameter(1) & 0xff );
        r.setElement(4, getNodeParamManager().getParameter(3) & 0xff );
        r.setElement(5, getNodeParamManager().getParameter(8) & 0xff );
        send.sendWithDelay(r,canListener.getSendIn(),canListener.getSendOut(),canListener.getDelay());
    }

    // Parameter answer
    protected void sendPARAN( int index ){
        
        try {
            CanReply r = new CanReply(5);
            r.setElement(0, CbusConstants.CBUS_PARAN);
            r.setElement(1, getNodeNumber() >> 8);
            r.setElement(2, getNodeNumber() & 0xff);
            r.setElement(3, index & 0xff);
            r.setElement(4, getNodeParamManager().getParameter(index) & 0xff);
            send.sendWithDelay(r,canListener.getSendIn(),canListener.getSendOut(),canListener.getDelay());
        }
        catch (ArrayIndexOutOfBoundsException e) {
            sendCMDERR(9);
        }
    }
    
    // NV Answer
    protected void sendNVANS( int index ) {
        
        try {
            CanReply r = new CanReply(5);
            r.setElement(0, CbusConstants.CBUS_NVANS);
            r.setElement(1, getNodeNumber() >> 8);
            r.setElement(2, getNodeNumber() & 0xff);
            r.setElement(3, index & 0xff );
            r.setElement(4, getNodeNvManager().getNV(index) & 0xff );
            send.sendWithDelay(r,canListener.getSendIn(),canListener.getSendOut(),canListener.getDelay());
        }
        catch (ArrayIndexOutOfBoundsException e) {
            sendCMDERR(10);
        }
    }

    protected void setDummyNV(int index, int newval) {
        
        if ( newval<0 || newval > 255 ) {
            sendCMDERR(12);
            return;
        }
        
        if (index==0) {
            sendCMDERR(10);
            return;
        }
        try {
            getNodeNvManager().setNV(index,newval);
            if ( getsendsWRACKonNVSET() ){
                sendWRACK();
            }
        }
        catch(ArrayIndexOutOfBoundsException e){
            sendCMDERR(10);
        }
    }
    
    protected void sendCMDERR(int errorId) {
        CanReply r = new CanReply(4);
        r.setElement(0, CbusConstants.CBUS_CMDERR);
        r.setElement(1, getNodeNumber() >> 8);
        r.setElement(2, getNodeNumber() & 0xff);
        r.setElement(3, errorId & 0xff);
        send.sendWithDelay(r,canListener.getSendIn(),canListener.getSendOut(),canListener.getDelay());
    }
    
    protected void sendWRACK(){
        CanReply r = new CanReply(3);
        r.setElement(0, CbusConstants.CBUS_WRACK);
        r.setElement(1, getNodeNumber() >> 8);
        r.setElement(2, getNodeNumber() & 0xff);        
        send.sendWithDelay(r,canListener.getSendIn(),canListener.getSendOut(),canListener.getDelay());
    }
    
    // sim of FiLM Button
    public void flimButton() {
        // send request for node number
        if ( getNodeParamManager().getParameter(3) >0 ) { // module type set
            setNodeInSetupMode(true);
            CanReply r = new CanReply(3);
            r.setElement(0, CbusConstants.CBUS_RQNN);
            r.setElement(1, getNodeNumber() >> 8);
            r.setElement(2, getNodeNumber() & 0xff);
            send.sendWithDelay(r,canListener.getSendIn(),canListener.getSendOut(),canListener.getDelay());
        }
    }

    public void setPane(NdPane pane) {
        _pane = pane;
    }
    
    protected void setDNN(int nn){
        setNodeNumber(nn);
        setNodeInFLiMMode(true);
        setNodeInSetupMode(false);
        if (_pane != null) {
            _pane.updateNodeGui();
        }
        CanReply r = new CanReply(3);
        r.setElement(0, CbusConstants.CBUS_NNACK);
        r.setElement(1, getNodeNumber() >> 8);
        r.setElement(2, getNodeNumber() & 0xff);
        send.sendWithDelay(r,canListener.getSendIn(),canListener.getSendOut(),canListener.getDelay());
    }

    // private static final Logger log = LoggerFactory.getLogger(CbusDummyNode.class);

}
