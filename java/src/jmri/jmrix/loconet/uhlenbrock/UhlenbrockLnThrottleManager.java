package jmri.jmrix.loconet.uhlenbrock;

import org.apache.log4j.Logger;
import jmri.DccThrottle;
import jmri.ThrottleManager;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.ThrottleListener;
import jmri.jmrix.loconet.LnThrottleManager;
import jmri.jmrix.loconet.LocoNetThrottle;
import jmri.jmrix.loconet.LocoNetSlot;
import jmri.jmrix.loconet.SlotListener;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import java.util.Hashtable;

/**
 * LocoNet implementation of a ThrottleManager.
 * <P>
 * Works in cooperation with the SlotManager, which actually
 * handles the communications.
 *
 * @see jmri.jmrix.loconet.SlotManager
 * @author		Bob Jacobsen  Copyright (C) 2001
 * @version 		$Revision: 19121 $
 */
public class UhlenbrockLnThrottleManager extends LnThrottleManager implements ThrottleManager, SlotListener {
    
    public UhlenbrockLnThrottleManager(UhlenbrockSystemConnectionMemo memo) {
    	super(memo);
    }

	/** 
     * The Intellibox-II doesn't always respond to a request loco message,
     * therefore the system will not allow further requests to create a 
     * throttle to that address as one is already in process.
     *
     * With the Intellibox throttle, it will make three attempts to connect 
     * if no response is received from the command station after 2 seconds.
     * Otherwise it will send a failthrottlerequest message out.
	 */
	public void requestThrottleSetup(LocoAddress address, boolean control) {
        slotManager.slotFromLocoAddress(((DccLocoAddress)address).getNumber(), this);
        
        class RetrySetup implements Runnable {
            DccLocoAddress address;
            SlotListener list;
            RetrySetup(DccLocoAddress address, SlotListener list){
                this.address=address;
                this.list=list;
            }
          public void run() {
            int count = 0;
            while(count<3){
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    return;
                }
                slotManager.slotFromLocoAddress(address.getNumber(), list);
                log.warn("No response to requesting loco " + address + ", will try again " + count);
                count++;
            }
            log.error("No response to requesting loco " + address + " after " + count + " attempts, will cancel the request");
            failedThrottleRequest(address, "Failed to get response from command station");
          }
        }
        Thread thr = new Thread(new RetrySetup((DccLocoAddress)address, this));
        thr.start();
        waitingForNotification.put(((DccLocoAddress)address).getNumber(), thr);
	}
    
    Hashtable<Integer, Thread> waitingForNotification = new Hashtable<Integer, Thread>(5);
	
    /**
     * SlotListener contract. Get notification that an address has changed slot.
     * This method creates a throttle for all ThrottleListeners of that address
     * and notifies them via the ThrottleListener.notifyThrottleFound method.
     */
    public void notifyChangedSlot(LocoNetSlot s) {
    	DccThrottle throttle = new LocoNetThrottle((LocoNetSystemConnectionMemo)adapterMemo, s);
    	notifyThrottleKnown(throttle, new DccLocoAddress(s.locoAddr(),isLongAddress(s.locoAddr()) ) );
        if(waitingForNotification.containsKey(s.locoAddr())){
            Thread r = waitingForNotification.get(s.locoAddr());
            synchronized(r){
                r.interrupt();
            }
            waitingForNotification.remove(s.locoAddr());
        }
    }
    @Override
    public void failedThrottleRequest(DccLocoAddress address, String reason){
        if(waitingForNotification.containsKey(address.getNumber())){
            waitingForNotification.get(address.getNumber()).interrupt();
            waitingForNotification.remove(address.getNumber());
        }
        super.failedThrottleRequest(address, reason);
    }
    
        /**
     * Cancel a request for a throttle
     * @param address The decoder address desired.
     * @param isLong True if this is a request for a DCC long (extended) address.
     * @param l The ThrottleListener cancelling request for a throttle.
     */
    @Override
    public void cancelThrottleRequest(int address, boolean isLong, ThrottleListener l) {
        if(waitingForNotification.containsKey(address)){
            waitingForNotification.get(address).interrupt();
            waitingForNotification.remove(address);
        }
        super.cancelThrottleRequest(address, isLong, l);
    }

    static Logger log = Logger.getLogger(UhlenbrockLnThrottleManager.class.getName());
}
