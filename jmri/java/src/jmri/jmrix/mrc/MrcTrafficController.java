// MrcTrafficController.java

package jmri.jmrix.mrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.util.Vector;

/**
 * Converts Stream-based I/O to/from MRC messages.  The "MrcInterface"
 * side sends/receives message objects.
 * <P>
 * The connection to
 * a MrcPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.     Note that this processing is
 * handled in an independent thread.
 * <P>
 * This handles the state transistions, based on the
 * necessary state in each message.
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Revision$
 */
public abstract class MrcTrafficController implements MrcInterface{

    public MrcTrafficController() {
        super();
    }

    public void setCabNumber(int x){
        cabAddress = x;
    }
    
    int cabAddress = 0;
    
    public int getCabNumber(){
        return cabAddress;
    }
    
    // Abstract methods for the MrcInterface
    abstract public boolean status();
    
    abstract public void sendMrcMessage(MrcMessage m);
    
    // The methods to implement adding and removing listeners
    protected Vector<MrcTrafficListenerFilter> trafficListeners = new Vector<MrcTrafficListenerFilter>();

    public synchronized void addTrafficListener(int mask, MrcTrafficListener l) {
        if (l == null) throw new java.lang.NullPointerException();

        // add only if not already registered
    	MrcTrafficListenerFilter adapter = new MrcTrafficListenerFilter(mask, l);
        if (!trafficListeners.contains(adapter)) {
            trafficListeners.addElement(adapter);
        }
    }

    public synchronized void removeTrafficListener(int mask, MrcTrafficListener l) {
        if (l == null) throw new java.lang.NullPointerException();

        MrcTrafficListenerFilter filter = new MrcTrafficListenerFilter(mask, l);
    	if (trafficListeners.contains(filter)) {
    		trafficListeners.remove(trafficListeners.indexOf(filter)).setFilter(mask);
    	}
    }

    public synchronized void changeTrafficListener(int mask, MrcTrafficListener l) {
        if (l == null) throw new java.lang.NullPointerException();

        MrcTrafficListenerFilter filter = new MrcTrafficListenerFilter(mask, l);
    	if (trafficListeners.contains(filter)) {
    		trafficListeners.get(trafficListeners.indexOf(filter)).setFilter(mask);
    	}
    }
    
	@SuppressWarnings("unchecked")
	public void notifyRcv(Date timestamp, MrcMessage m) {
        
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<MrcTrafficListenerFilter> v;
        synchronized(this) {
            v = (Vector<MrcTrafficListenerFilter>) trafficListeners.clone();
        }
        if (log.isDebugEnabled()) log.debug("notify of incoming Mrc packet: " + m.toString());//IN18N
        
        // forward to all listeners
        for (MrcTrafficListenerFilter adapter : v) {
        	adapter.fireRcv(timestamp, m);
        }
    }

	@SuppressWarnings("unchecked")
	public void notifyXmit(Date timestamp, MrcMessage m) {
        
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<MrcTrafficListenerFilter> v;
        synchronized(this) {
            v = (Vector<MrcTrafficListenerFilter>) trafficListeners.clone();
        }
        if (log.isDebugEnabled()) log.debug("notify of send Mrc packet: " + m.toString());//IN18N
        
        // forward to all listeners
        for (MrcTrafficListenerFilter adapter : v) {
        	adapter.fireXmit(timestamp, m);
        }
    }

	/**
     * Is there a backlog of information for the outbound link?
     * This includes both in the program (e.g. the outbound queue)
     * and in the command station interface (e.g. flow control from the port)
     * @return true if busy, false if nothing waiting to send
     */
    abstract public boolean isXmtBusy();

    /**
     * Reset statistics (received message count, transmitted message count,
     * received byte count)
     */
    public void resetStatistics() {
        receivedMsgCount = 0;
        transmittedMsgCount = 0;
        receivedByteCount = 0;
    }
    
    /**
     * Monitor the number of MRC messaages received across the interface.
     * This includes the messages this client has sent.
     */
    public int getReceivedMsgCount() {
        return receivedMsgCount;
    }
    protected int receivedMsgCount = 0;
    
    /**
     * Monitor the number of bytes in MRC messaages received across the interface.
     * This includes the messages this client has sent.
     */
    public int getReceivedByteCount() {
        return receivedByteCount;
    }
    protected int receivedByteCount = 0;
    
    /**
     * Monitor the number of MRC messaages transmitted across the interface.
     */
    public int getTransmittedMsgCount() {
        return transmittedMsgCount;
    }
    protected int transmittedMsgCount = 0;
    
    public void setAdapterMemo(MrcSystemConnectionMemo memo){
        adaptermemo = memo;
    }
    
    MrcSystemConnectionMemo adaptermemo;
    
    public String getUserName() { 
        if(adaptermemo==null) return "MRC"; //IN18N
        return adaptermemo.getUserName();
    }
    
    public String getSystemPrefix() { 
        if(adaptermemo==null) return "MR"; //IN18N
        return adaptermemo.getSystemPrefix();
    }

    static Logger log = LoggerFactory.getLogger(MrcTrafficController.class.getName());
}


/* @(#)MrcTrafficController.java */

