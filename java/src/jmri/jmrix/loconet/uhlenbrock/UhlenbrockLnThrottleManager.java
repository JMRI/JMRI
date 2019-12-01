package jmri.jmrix.loconet.uhlenbrock;

import java.util.Hashtable;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.jmrix.loconet.LnThrottleManager;
import jmri.jmrix.loconet.LocoNetSlot;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.LocoNetThrottle;
import jmri.jmrix.loconet.SlotListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocoNet implementation of a ThrottleManager for Uhlenbrock.
 * <p>
 * Works in cooperation with the SlotManager, which actually handles the
 * communications.
 *
 * @see jmri.jmrix.loconet.SlotManager
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class UhlenbrockLnThrottleManager extends LnThrottleManager {

    public UhlenbrockLnThrottleManager(UhlenbrockSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * The Intellibox-II doesn't always respond to a request loco message,
     * therefore the system will not allow further requests to create a throttle
     * to that address as one is already in process.
     *
     * With the Intellibox throttle, it will make three attempts to connect if
     * no response is received from the command station after 2 seconds.
     * Otherwise it will send a failthrottlerequest message out.
     */
    @Override
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        if (!(address instanceof DccLocoAddress)){
            log.error("{} is not a DCCLocoAddress",address);
            failedThrottleRequest(address, "Address" + address + " is not a DccLocoAddress");
            return;
        }
        slotManager.slotFromLocoAddress(((DccLocoAddress) address).getNumber(), this);

        class RetrySetup implements Runnable {

            DccLocoAddress address;
            SlotListener list;

            RetrySetup(DccLocoAddress address, SlotListener list) {
                this.address = address;
                this.list = list;
            }

            @Override
            public void run() {
                int count = 0;
                while (count < 3) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        return;
                    }
                    slotManager.slotFromLocoAddress(address.getNumber(), list);
                    log.warn("No response to requesting loco {}, will try again {}", address, count);
                    count++;
                }
                log.error("No response to requesting loco {} after {} attempts; will cancel the request", address, count);
                failedThrottleRequest(address, "Failed to get response from command station");
            }
        }
        Thread thr = new Thread(new RetrySetup((DccLocoAddress) address, this));
        thr.start();
        waitingForNotification.put(((DccLocoAddress) address).getNumber(), thr);
    }

    Hashtable<Integer, Thread> waitingForNotification = new Hashtable<Integer, Thread>(5);

    /**
     * SlotListener contract. Get notification that an address has changed slot.
     * This method creates a throttle for all ThrottleListeners of that address
     * and notifies them via the ThrottleListener.notifyThrottleFound method.
     */
    @Override
    public void notifyChangedSlot(LocoNetSlot s) {
        DccThrottle throttle = new LocoNetThrottle((LocoNetSystemConnectionMemo) adapterMemo, s);
        notifyThrottleKnown(throttle, new DccLocoAddress(s.locoAddr(), isLongAddress(s.locoAddr())));
        if (waitingForNotification.containsKey(s.locoAddr())) {
            Thread r = waitingForNotification.get(s.locoAddr());
            synchronized (r) {
                r.interrupt();
            }
            waitingForNotification.remove(s.locoAddr());
        }
    }

    @Override
    public void failedThrottleRequest(LocoAddress address, String reason) {
        if (waitingForNotification.containsKey(address.getNumber())) {
            waitingForNotification.get(address.getNumber()).interrupt();
            waitingForNotification.remove(address.getNumber());
        }
        super.failedThrottleRequest(address, reason);
    }

    /**
     * Cancel a request for a throttle.
     *
     * @param address The decoder address desired.
     * @param l       The ThrottleListener cancelling request for a throttle.
     */
    @Override
    public void cancelThrottleRequest(LocoAddress address, ThrottleListener l) {
        int loconumber = address.getNumber();
        if (waitingForNotification.containsKey(loconumber)) {
            waitingForNotification.get(loconumber).interrupt();
            waitingForNotification.remove(loconumber);
        }
        super.cancelThrottleRequest(address, l);
    }

    private final static Logger log = LoggerFactory.getLogger(UhlenbrockLnThrottleManager.class);

}
