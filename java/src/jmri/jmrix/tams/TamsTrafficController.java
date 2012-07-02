// TamsTrafficController.java

package jmri.jmrix.tams;

import jmri.CommandStation;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;

/**
 * Converts Stream-based I/O to/from Tams messages.  The "TamsInterface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a TamsPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the
 * necessary state in each message.
 *
 * Based on work by Bob Jacobsen
 * @author	Kevin Dickerson  Copyright (C) 2012
 * @version			$Revision: 19084 $
 */
public class TamsTrafficController extends AbstractMRTrafficController implements TamsInterface, CommandStation {

	public TamsTrafficController() {
        super();
        if (log.isDebugEnabled()) log.debug("creating a new TamsTrafficController object");
        // set as command station too
        jmri.InstanceManager.setCommandStation(this);
        this.setAllowUnexpectedReply(true);
    }

    public void setAdapterMemo(TamsSystemConnectionMemo memo){
        adaptermemo = memo;
    }
    
    TamsSystemConnectionMemo adaptermemo;
    
    // The methods to implement the TamsInterface

    public synchronized void addTamsListener(TamsListener l) {
        this.addListener(l);
    }

    public synchronized void removeTamsListener(TamsListener l) {
        this.removeListener(l);
    }

    @Override
	protected int enterProgModeDelayTime() {
		// we should to wait at least a second after enabling the programming track
		return 1000;
	}

    /**
     * CommandStation implementation
     */
    public void sendPacket(byte[] packet,int count) { }
    
    /**
     * Forward a TamsMessage to all registered TamsInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((TamsListener)client).message((TamsMessage)m);
    }

    /**
     * Forward a TamsReply to all registered TamsInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        ((TamsListener)client).reply((TamsReply)r);
    }
    
    static class PollMessage {
        TamsListener tl;
        TamsMessage tm;
        
        PollMessage(TamsMessage tm, TamsListener tl){
            this.tm = tm;
            this.tl = tl;
        }
        
        TamsListener getListener(){
            return tl;
        }
        
        TamsMessage getMessage(){
            return tm;
        }
    }
    
    ConcurrentLinkedQueue<PollMessage> pollQueue = new ConcurrentLinkedQueue<PollMessage>();
    
    boolean disablePoll = false;
    public boolean getPollQueueDisabled() { return disablePoll; }
    public void setPollQueueDisabled(boolean poll) { disablePoll = poll; }
    
    /**
    * As we have to poll the tams system to get updates we put request into a queue and allow the
    * the abstrct traffic controller to handle them when it is free.
    */
    public void addPollMessage(TamsMessage tm, TamsListener tl){
        tm.setTimeout(100);
        for(PollMessage pm:pollQueue){
            if(pm.getListener()==tl && pm.getMessage().toString().equals(tm.toString())){
                log.debug("Message is already in the poll queue so will not add");
                return;
            }
        }
        PollMessage pm = new PollMessage(tm, tl);
        pollQueue.offer(pm);
    }
    
    /**
    * Removes a message that is used for polling from the queue.
    */
    public void removePollMessage(TamsMessage tm, TamsListener tl){
        for(PollMessage pm:pollQueue){
            if(pm.getListener()==tl && pm.getMessage().toString().equals(tm.toString())){
                pollQueue.remove(pm);
            }
        }
    }

    
    /**
	 * Check Tams MC for updates.
	 */
	protected AbstractMRMessage pollMessage() {
        if(disablePoll)
            return null;
        if(!pollQueue.isEmpty()){
            PollMessage pm = pollQueue.peek();
            if(pm!=null){
                log.info(pm.getMessage().toString());
                return pm.getMessage();
            }
        }
        return null;
	}
    
 
    protected AbstractMRListener pollReplyHandler() {
        if(disablePoll)
            return null;
        if(!pollQueue.isEmpty()){
            PollMessage pm = pollQueue.poll();
            if(pm!=null){
                pollQueue.offer(pm);
                return pm.getListener();
            }
        }
        return null;
    }

    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendTamsMessage(TamsMessage m, TamsListener reply) {
        sendMessage(m, reply);
    }

    @Override
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        super.forwardToPort(m, reply);
    }
    
    protected boolean unsolicitedSensorMessageSeen = false;
    
    protected AbstractMRMessage enterProgMode() {
        return null;
    }

    protected AbstractMRMessage enterNormalMode() {
        return null;
    }

    //This can be removed once multi-connection is complete
    public void setInstance(){}

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="MS_PKGPROTECT")
    // FindBugs wants this package protected, but we're removing it when multi-connection
    // migration is complete
    final static protected TamsTrafficController self = null;

    /**
     * Add header to the outgoing byte stream.
     * @param msg  The output byte stream
     * @return next location in the stream to fill
     */
    protected int addHeaderToOutput(byte[] msg, AbstractMRMessage m) {
        if (m.isBinary()){
            msg[0] = (byte) 0x58;
            return 1;
        }
        return 0;
    }
    
    protected AbstractMRReply newReply() { 
        TamsReply reply = new TamsReply();
        return reply;
    }
    
    protected boolean endOfMessage(AbstractMRReply msg) {
        int num = msg.getNumDataElements();
        if(num>2 && msg.getElement(num-2)==0x0d && msg.getElement(num-1)==0x5d){
            return true;
        }
        return false;
    }
    
    // Override the finalize method for this class
    
    public boolean sendWaitMessage(TamsMessage m, AbstractMRListener reply){
        if(log.isDebugEnabled()) log.debug("Send a message and wait for the response");
        if (ostream == null) return false;
        m.setTimeout(500);
        m.setRetries(10);
        synchronized(getSelfLock()) {
                forwardToPort(m, reply);
                // wait for reply
                try {
                    if (xmtRunnable!=null)
                        synchronized(xmtRunnable) {
                            xmtRunnable.wait(m.getTimeout());
                        }
                } catch (InterruptedException e) { 
                    Thread.currentThread().interrupt(); // retain if needed later
                    log.error("transmit interrupted"); 
                    return false;
                }
            }
        return true;
    }
    
    public String getUserName() { 
        if(adaptermemo==null) return "Tams";
        return adaptermemo.getUserName();
    }
    
    public String getSystemPrefix() { 
        if(adaptermemo==null) return "TM";
        return adaptermemo.getSystemPrefix();
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TamsTrafficController.class.getName());
}


/* @(#)TamsTrafficController.java */
