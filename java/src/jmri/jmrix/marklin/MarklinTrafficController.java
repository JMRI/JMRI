// MarklinTrafficController.java

package jmri.jmrix.marklin;

import org.apache.log4j.Logger;
import jmri.CommandStation;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Converts Stream-based I/O to/from Marklin CS2 messages.  The "MarklinInterface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a MarklinPortController is via a pair of UDP Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the
 * necessary state in each message.
 *
 * Based on work by Bob Jacobsen
 * @author			Kevin Dickerson Copyright (C) 2012
 * @version			$Revision: 19084 $
 */
public class MarklinTrafficController extends AbstractMRTrafficController implements MarklinInterface, CommandStation {

	public MarklinTrafficController() {
        super();
        if (log.isDebugEnabled()) log.debug("creating a new MarklinTrafficController object");
        // set as command station too
        jmri.InstanceManager.setCommandStation(this);
        this.setAllowUnexpectedReply(true);
    }

    public void setAdapterMemo(MarklinSystemConnectionMemo memo){
        adaptermemo = memo;
    }
    
    MarklinSystemConnectionMemo adaptermemo;
    
    // The methods to implement the MarklinInterface
    public synchronized void addMarklinListener(MarklinListener l) {
        this.addListener(l);
    }

    public synchronized void removeMarklinListener(MarklinListener l) {
        this.removeListener(l);
    }

    @Override
	protected int enterProgModeDelayTime() {
		// we should to wait at least a second after enabling the programming track
		return 1000;
	}

    /**
     * CommandStation implementation, not yet supported
     */
    public void sendPacket(byte[] packet,int count) {

    }
    
    /**
     * Forward a MarklinMessage to all registered MarklinInterface listeners.
     */
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((MarklinListener)client).message((MarklinMessage)m);
    }

    /**
     * Forward a MarklinReply to all registered MarklinInterface listeners.
     */
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        ((MarklinListener)client).reply((MarklinReply)r);
    }
    
    /**
     * Forward a preformatted message to the actual interface.
     */
    public void sendMarklinMessage(MarklinMessage m, MarklinListener reply) {
        sendMessage(m, reply);
    }

    /*@Override
    protected void forwardToPort(AbstractMRMessage m, AbstractMRListener reply) {
        super.forwardToPort(m, reply);
    }*/
    
    //Marklin doesn't support this function.
    protected AbstractMRMessage enterProgMode() {
        return MarklinMessage.getProgMode();
    }
    //Marklin doesn't support this function!
    protected AbstractMRMessage enterNormalMode() {
        return MarklinMessage.getExitProgMode();
    }

        /**
     * static function returning the MarklinTrafficController instance to use.
     * @return The registered MarklinTrafficController instance for general use,
     *         if need be creating one.
     */
    static public MarklinTrafficController instance() {
        return self;
    }

    //This can be removed once multi-connection is complete
    public void setInstance(){}

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="MS_PKGPROTECT")
    // FindBugs wants this package protected, but we're removing it when multi-connection
    // migration is complete
    final static protected MarklinTrafficController self = null;
    
    protected AbstractMRReply newReply() { 
        MarklinReply reply = new MarklinReply();
        return reply;
    }
    
    // for now, receive always OK
    @Override
	protected boolean canReceive() {
        return true;
  	}

    //In theory the replies should only be 13bytes long, so the EOM is completed when the reply can take no more data
    protected boolean endOfMessage(AbstractMRReply msg) {
        return false;
    }
    
        static class PollMessage {
        MarklinListener ml;
        MarklinMessage mm;
        
        PollMessage(MarklinMessage mm, MarklinListener ml){
            this.mm = mm;
            this.ml = ml;
        }
        
        MarklinListener getListener(){
            return ml;
        }
        
        MarklinMessage getMessage(){
            return mm;
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
    public void addPollMessage(MarklinMessage mm, MarklinListener ml){
        mm.setTimeout(500);
        for(PollMessage pm:pollQueue){
            if(pm.getListener()==ml && pm.getMessage().toString().equals(mm.toString())){
                log.debug("Message is already in the poll queue so will not add");
                return;
            }
        }
        PollMessage pm = new PollMessage(mm, ml);
        pollQueue.offer(pm);
    }
    
    /**
    * Removes a message that is used for polling from the queue.
    */
    public void removePollMessage(MarklinMessage mm, MarklinListener ml){
        for(PollMessage pm:pollQueue){
            if(pm.getListener()==ml && pm.getMessage().toString().equals(mm.toString())){
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
    
    public String getUserName() { 
        if(adaptermemo==null) return "Marklin-CS2";
        return adaptermemo.getUserName();
    }
    
    public String getSystemPrefix() { 
        if(adaptermemo==null) return "MC";
        return adaptermemo.getSystemPrefix();
    }
    
    static Logger log = Logger.getLogger(MarklinTrafficController.class.getName());
}


/* @(#)MarklinTrafficController.java */






