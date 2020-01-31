package jmri.jmrix.can.cbus.simulator;

import java.util.ArrayList;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeConstants;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.jmrix.can.cbus.swing.simulator.NdPane;
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
    
    // private final CanSystemConnectionMemo memo;
    
    private NdPane _pane;
    public static ArrayList<Integer> ndTypes;
    
    private int _networkDelay;
    private Boolean _processIn;
    private Boolean _processOut;
    private Boolean _sendIn;
    private Boolean _sendOut;
    
    public CbusDummyNode( int nodenumber, int manufacturer, int nodeType, int canId, CanSystemConnectionMemo sysmemo ){
        super( sysmemo, nodenumber );
        _memo = sysmemo;
        setDummyType(manufacturer, nodeType);
        setCanId(canId);
        init();
    }
    
    /**
     * Uses in-class CanListener
     * {@inheritDoc}
     */    
    @Override
    public jmri.jmrix.can.cbus.node.CbusNodeCanListener getNewCanListener(){
        return new CbusDummyNodeCanListener(_memo,this);
    }
    
    private void init(){
        
        _networkDelay = 40;
        _pane = null;

        _processIn=false;
        _processOut=true;
        _sendIn=true;
        _sendOut=false;
        
        // get available simulated nodes
        ndTypes = new ArrayList<>();
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
    protected void sendNUMEV(){
        CanReply r = new CanReply(4);
        r.setElement(0, CbusConstants.CBUS_NUMEV);
        r.setElement(1, getNodeNumber() >> 8);
        r.setElement(2, getNodeNumber() & 0xff);
        r.setElement(3, getNodeEventManager().getTotalNodeEvents() );
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
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
            
            send.sendWithDelay(r,_sendIn,_sendOut,( _networkDelay + ( extraDelay * i ) ) );
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
            r.setElement(3, index);
            r.setElement(4, varIndex);
            r.setElement(5, _ndEv.getEvVar(varIndex) );
            send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
        }
    }
    
    protected void sendPARAMS() {
        CanReply r = new CanReply(8);
        r.setElement(0, CbusConstants.CBUS_PARAMS);
        r.setElement(1, getNodeParamManager().getParameter(1));
        r.setElement(2, getNodeParamManager().getParameter(2));
        r.setElement(3, getNodeParamManager().getParameter(3));
        r.setElement(4, getNodeParamManager().getParameter(4));
        r.setElement(5, getNodeParamManager().getParameter(5));
        r.setElement(6, getNodeParamManager().getParameter(6));
        r.setElement(7, getNodeParamManager().getParameter(7));
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }
    
    protected void sendPNN() {
        CanReply r = new CanReply(6);
        r.setElement(0, CbusConstants.CBUS_PNN);
        r.setElement(1, getNodeNumber() >> 8);
        r.setElement(2, getNodeNumber() & 0xff);
        r.setElement(3, getNodeParamManager().getParameter(1) );
        r.setElement(4, getNodeParamManager().getParameter(3) );
        r.setElement(5, getNodeParamManager().getParameter(8));
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }

    // Parameter answer
    protected void sendPARAN( int index ){
        
        try {
            CanReply r = new CanReply(5);
            r.setElement(0, CbusConstants.CBUS_PARAN);
            r.setElement(1, getNodeNumber() >> 8);
            r.setElement(2, getNodeNumber() & 0xff);
            r.setElement(3, index);
            r.setElement(4, getNodeParamManager().getParameter(index));
            send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
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
            r.setElement(3, index);
            r.setElement(4, getNodeNvManager().getNV(index));
            send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
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
        r.setElement(3, errorId );
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
    }
    
    protected void sendWRACK(){
        CanReply r = new CanReply(3);
        r.setElement(0, CbusConstants.CBUS_WRACK);
        r.setElement(1, getNodeNumber() >> 8);
        r.setElement(2, getNodeNumber() & 0xff);        
        send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
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
            send.sendWithDelay(r,_sendIn,_sendOut,_networkDelay);
        }
    }
    
    public final void setDummyType(int manu, int type){
        
        getNodeEventManager().resetNodeEvents();
        setNodeInFLiMMode(false);
        
        CbusNodeConstants.setDummyNodeParameters(this,manu,type);
        
        log.info("Simulated CBUS Node: {}", CbusNodeConstants.getModuleType(manu,type ) );
    }

    
    public void setPane(NdPane pane) {
        _pane = pane;
    }
    
    protected void setDNN(int nn){
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

    private static final Logger log = LoggerFactory.getLogger(CbusDummyNode.class);

}
