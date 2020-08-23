package jmri.jmrix.can.cbus.simulator;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusSend;

/**
 * Simulating event request responses.
 *
 * @author Steve Young Copyright (C) 2018
 * @see CbusSimulator
 * @since 4.15.2
 */
public class CbusSimCanListener extends jmri.jmrix.can.cbus.node.CbusNodeCanListener {
    
    private int _networkDelay;
    private boolean _processIn;
    private boolean _processOut;
    private boolean _sendIn;
    private boolean _sendOut;
    public CbusSend send;
    
    /**
     * Create a CanListener with Common Simulation setting attributes.
     * @param memo System Connection
     * @param node Node ( if a CbusDummyNode ), else use null
     */
    public CbusSimCanListener( CanSystemConnectionMemo memo, jmri.jmrix.can.cbus.node.CbusNode node ){
        super(memo,node);
        send = new CbusSend(memo);
        _processIn=false;
        _processOut=true;
        _sendIn=true;
        _sendOut=false;
        _networkDelay = 50;
    }
    
    /**
     * Set the simulated network delay.
     * @param delay Delay in ms
     */
    public final void setDelay( int delay){
        _networkDelay = delay;
    }
    
    /**
     * Get the simulated network delay.
     * Defaults to 50ms
     * @return delay in ms
     */
    public final int getDelay(){
        return _networkDelay;
    }

    /**
     * Set if to Listen for CanReply Frames incoming to JMRI.
     * @param newval true to listen, else false
     */
    public final void setProcessIn( boolean newval){
        _processIn = newval;
    }
    
    /**
     * Set if to Listen for CanMessage Frames outgoing to JMRI.
     * @param newval true to listen, else false
     */
    public final void setProcessOut( boolean newval){
        _processOut = newval;
    }

    /**
     * Set if to Send Frames from the Sim as Incoming CanReply.
     * @param newval true to send as incoming
     */
    public final void setSendIn( boolean newval){
        _sendIn = newval;
    }

    /**
     * Set if to Send Frames from the Sim as Outgoing CanMessage.
     * @param newval true to send as outgoing
     */
    public final void setSendOut( boolean newval){
        _sendOut = newval;
    }
    
    /**
     * Get if to Listen for CanReply Frames incoming to JMRI.
     * Defaults to false
     * @return true to listen, else false
     */
    public final boolean getProcessIn() {
        return _processIn;
    }
    
    /**
     * Get if to Listen for CanMessage Frames outgoing to JMRI.
     * Defaults to true
     * @return true to listen, else false
     */
    public final boolean getProcessOut() {
        return _processOut;
    }    
    
    /**
     * Get if to Send Frames from the Sim as Incoming CanReply.
     * Defaults to true
     * @return true to send as incoming
     */
    public final boolean getSendIn() {
        return _sendIn;
    }    
    
    /**
     * Get if to Send Frames from the Sim as Outgoing CanMessage.
     * Defaults to false
     * @return true to send as outgoing
     */
    public final boolean getSendOut() {
        return _sendOut;
    }

    /**
     * Method to be overridden by extending methods.
     * @param m CanFrame or CanReply to process
     */
    protected void startProcessFrame(AbstractMessage m){}
    
    /**
     * Forwards non-extended CanMessage according to #getProcessOut
     * {@inheritDoc}
     */
    @Override
    public final void message(CanMessage m) {
        if ( !m.extendedOrRtr() && _processOut ) {
            startProcessFrame(m);
        }
    }

    /**
     * Forwards non-extended CanReply according to #getProcessIn
     * {@inheritDoc}
     */
    @Override
    public final void reply(CanReply r) {
        if ( !r.extendedOrRtr() && _processIn ) {
            startProcessFrame(r);
        }
    }

    /**
     * {@inheritDoc}
     */
    @OverridingMethodsMustInvokeSuper
    @Override
    public void dispose(){
        removeTc(memo);
        send = null;
    }
    
}
