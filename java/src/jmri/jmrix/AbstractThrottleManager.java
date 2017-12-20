package jmri.jmrix;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import jmri.BasicRosterEntry;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of a ThrottleManager.
 * <P>
 * Based on Glen Oberhauser's original LnThrottleManager implementation.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author      Steve Rawlinson Copyright (C) 2016
 */
abstract public class AbstractThrottleManager implements ThrottleManager {

    public AbstractThrottleManager() {
    }

    public AbstractThrottleManager(SystemConnectionMemo memo) {
        adapterMemo = memo;
    }

    protected SystemConnectionMemo adapterMemo;

    protected String userName = "Internal";

    @Override
    public String getUserName() {
        if (adapterMemo != null) {
            return adapterMemo.getUserName();
        }
        return userName;
    }

    /**
     * By default, only DCC in this implementation
     */
    @Override
    public String[] getAddressTypes() {
        return new String[]{
            LocoAddress.Protocol.DCC.getPeopleName(),
            LocoAddress.Protocol.DCC_SHORT.getPeopleName(),
            LocoAddress.Protocol.DCC_LONG.getPeopleName()};
    }

    /**
     * By default, only DCC in this implementation
     */
    @Override
    public String getAddressTypeString(LocoAddress.Protocol prot) {
        return prot.getPeopleName();
    }

    @Override
    public LocoAddress.Protocol[] getAddressProtocolTypes() {
        return new LocoAddress.Protocol[]{LocoAddress.Protocol.DCC, LocoAddress.Protocol.DCC_SHORT, LocoAddress.Protocol.DCC_LONG};
    }

    @Override
    public LocoAddress getAddress(String value, LocoAddress.Protocol protocol) {
        if (value == null) {
            return null;
        }
        if (protocol == null) {
            return null;
        }
        int num = Integer.parseInt(value);

        // if DCC long and can't be, or short and can't be, fix
        if ((LocoAddress.Protocol.DCC == protocol || LocoAddress.Protocol.DCC_SHORT == protocol) && !canBeShortAddress(num)) {
            protocol = LocoAddress.Protocol.DCC_LONG;
        }
        if ((LocoAddress.Protocol.DCC == protocol || LocoAddress.Protocol.DCC_LONG == protocol) && !canBeLongAddress(num)) {
            protocol = LocoAddress.Protocol.DCC_SHORT;
        }

        // if still ambiguous, prefer short
        if (protocol == LocoAddress.Protocol.DCC) {
            protocol = LocoAddress.Protocol.DCC_SHORT;
        }

        return new DccLocoAddress(num, protocol);
    }

    @Override
    public LocoAddress getAddress(String value, String protocol) {
        if (value == null) {
            return null;
        }
        if (protocol == null) {
            return null;
        }
        LocoAddress.Protocol p = getProtocolFromString(protocol);

        return getAddress(value, p);
    }

    @Override
    public LocoAddress.Protocol getProtocolFromString(String selection) {
        return LocoAddress.Protocol.getByPeopleName(selection);
    }

    /**
     * throttleListeners is indexed by the address, and contains as elements an
     * ArrayList of WaitingThrottle objects, each of which has one ThrottleListner. 
     * This allows more than one ThrottleLister to request a throttle at a time, 
     * the entries in this Hashmap are only valid during the throttle setup process.
     */
    private HashMap<LocoAddress, ArrayList<WaitingThrottle>> throttleListeners = new HashMap<LocoAddress, ArrayList<WaitingThrottle>>(5);

    static class WaitingThrottle {

        ThrottleListener l;
        BasicRosterEntry re;
        PropertyChangeListener pl;

        WaitingThrottle(ThrottleListener _l, BasicRosterEntry _re) {
            l = _l;
            re = _re;
        }

        WaitingThrottle(PropertyChangeListener _pl, BasicRosterEntry _re) {
            pl = _pl;
            re = _re;
        }

        PropertyChangeListener getPropertyChangeListener() {
            return pl;
        }

        ThrottleListener getListener() {
            return l;
        }

        BasicRosterEntry getRosterEntry() {
            return re;
        }
    }
    /**
     * listenerOnly is indexed by the address, and contains as elements an
     * ArrayList of propertyChangeListeners objects that have requested
     * notification of changes to a throttle that hasn't yet been created/ The
     * entries in this Hashmap are only valid during the throttle setup process.
     */
    private HashMap<DccLocoAddress, ArrayList<WaitingThrottle>> listenerOnly = new HashMap<DccLocoAddress, ArrayList<WaitingThrottle>>(5);

    //This keeps a map of all the current active DCC loco Addresses that are in use.
    /**
     * addressThrottles is indexed by the address, and contains as elements a
     * subclass of the throttle assigned to an address and the number of
     * requests and active users for this address.
     */
    private Hashtable<DccLocoAddress, Addresses> addressThrottles = new Hashtable<DccLocoAddress, Addresses>();

    /**
     * Does this DCC system allow a Throttle (e.g. an address) to be used by
     * only one user at a time?
     */
    protected boolean singleUse() {
        return true;
    }

    @Override
    public boolean requestThrottle(BasicRosterEntry re, ThrottleListener l) {
        return requestThrottle(re.getDccLocoAddress(), re, l);
    }

    @Override
    public boolean requestThrottle(int address, boolean isLongAddress, ThrottleListener l) {
        DccLocoAddress la = new DccLocoAddress(address, isLongAddress);
        return requestThrottle(la, null, l);
    }

    @Override
    public boolean requestThrottle(LocoAddress la, ThrottleListener l) {
        return requestThrottle(la, null, l);
    }

    /**
     * Request a throttle, given a decoder address. When the decoder address is
     * located, the ThrottleListener gets a callback via the
     * ThrottleListener.notifyThrottleFound method.
     *
     * @param la LocoAddress of the decoder desired.
     * @param l  The ThrottleListener awaiting notification of a found throttle.
     * @return True if the request will continue, false if the request will not
     *         be made. False may be returned if a the throttle is already in
     *         use.
     */
    @Override
    public boolean requestThrottle(LocoAddress la, BasicRosterEntry re, ThrottleListener l) {
        boolean throttleFree = true;

        // check for a valid throttle address
        if (!canBeLongAddress(la.getNumber()) && !canBeShortAddress(la.getNumber())) {
            return false;
        }

        // put the list in if not present
        if (!throttleListeners.containsKey(la)) {
            throttleListeners.put(la, new ArrayList<WaitingThrottle>());
        }
        // get the corresponding list to check length
        ArrayList<WaitingThrottle> a = throttleListeners.get(la);

        if (addressThrottles.containsKey(la)) {
            log.debug("A throttle to address " + la.getNumber() + " already exists, so will return that throttle");
            a.add(new WaitingThrottle(l, re));
            notifyThrottleKnown(addressThrottles.get(la).getThrottle(), la, true);
            return throttleFree;
        } else {
            log.debug(la.getNumber() + " has not been created before");
        }

        if (log.isDebugEnabled()) {
            log.debug("After request in ATM: " + a.size());
        }
        // check length
        if (singleUse() && (a.size() > 0)) {
            throttleFree = false;
            if (log.isDebugEnabled()) {
                log.debug("singleUser() is true, and the list of WaitingThrottles isn't empty, returning false");
            }
        } else if (a.size() == 0) {
            a.add(new WaitingThrottle(l, re));
            if (log.isDebugEnabled()) {
                log.debug("list of WaitingThrottles is empty: " + la + ";" + a);
            }
            log.debug("calling requestThrottleSetup()");
            requestThrottleSetup(la, true);
        } else {
            a.add(new WaitingThrottle(l, re));
            if (log.isDebugEnabled()) {
                log.debug("singleUse() returns false and there are existing WaitThrottles, adding a one to the list");
            }
        }
        return throttleFree;
    }

    /**
     * Request a throttle, given a decoder address. When the decoder address is
     * located, the ThrottleListener gets a callback via the
     * ThrottleListener.notifyThrottleFound method.
     * <P>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form.
     *
     * @param address The decoder address desired.
     * @param l       The ThrottleListener awaiting notification of a found
     *                throttle.
     * @return True if the request will continue, false if the request will not
     *         be made. False may be returned if a the throttle is already in
     *         use.
     */
    @Override
    public boolean requestThrottle(int address, ThrottleListener l) {
        boolean isLong = true;
        if (canBeShortAddress(address)) {
            isLong = false;
        }
        return requestThrottle(address, isLong, l);
    }

    /**
     * Abstract member to actually do the work of configuring a new throttle,
     * usually via interaction with the DCC system
     */
    abstract public void requestThrottleSetup(LocoAddress a, boolean control);

    /**
     * Abstract member to actually do the work of configuring a new throttle,
     * usually via interaction with the DCC system
     */
    public void requestThrottleSetup(LocoAddress a) {
        requestThrottleSetup(a, true);
    }

    /**
     * Cancel a request for a throttle
     *
     * @param address The decoder address desired.
     * @param isLong  True if this is a request for a DCC long (extended)
     *                address.
     * @param l       The ThrottleListener cancelling request for a throttle.
     */
    @Override
    public void cancelThrottleRequest(int address, boolean isLong, ThrottleListener l) {
        if (throttleListeners != null) {
            DccLocoAddress la = new DccLocoAddress(address, isLong);
            cancelThrottleRequest(la, l);
        }
        /*if (addressThrottles.contains(la)){
         addressThrottles.get(la).decrementUse();
         }*/
    }

    @Override
    public void cancelThrottleRequest(BasicRosterEntry re, ThrottleListener l) {
        if (throttleListeners != null) {
            cancelThrottleRequest(re.getDccLocoAddress(), l);
        }
    }

    private void cancelThrottleRequest(DccLocoAddress la, ThrottleListener l) {
        if (throttleListeners != null) {
            ArrayList<WaitingThrottle> a = throttleListeners.get(la);
            if (a == null) {
                return;
            }
            for (int i = 0; i < a.size(); i++) {
                if (l == a.get(i).getListener()) {
                    a.remove(i);
                }
            }
        }
    }

    /**
     * Cancel a request for a throttle.
     * <P>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form.
     *
     * @param address The decoder address desired.
     * @param l       The ThrottleListener cancelling request for a throttle.
     */
    @Override
    public void cancelThrottleRequest(int address, ThrottleListener l) {
        boolean isLong = true;
        if (canBeShortAddress(address)) {
            isLong = false;
        }
        cancelThrottleRequest(address, isLong, l);
    }

    /**
     * Steal a requested throttle.
     * <P>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form.
     *
     * @param re desired Roster Entry
     * @param l  ThrottleListener requesting the throttle steal occur.
     * @param steal true if the request should continue, false otherwise.
     * @since 4.9.2
     */
    @Override
    public void stealThrottleRequest(BasicRosterEntry re, ThrottleListener l,boolean steal){
        stealThrottleRequest(re.getDccLocoAddress(), l,steal);
    }

    /**
     * Steal a requested throttle.
     * <P>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form.
     *
     * @param address desired decoder address
     * @param l  ThrottleListener requesting the throttle steal occur.
     * @param steal true if the request should continue, false otherwise.
     * @since 4.9.2
     */
    @Override
    public void stealThrottleRequest(int address, ThrottleListener l,boolean steal){
        boolean isLong = true;
        if (canBeShortAddress(address)) {
            isLong = false;
        }
        stealThrottleRequest(address, isLong, l,steal);
    }

    /**
     * Steal a requested throttle.
     *
     * @param address desired decoder address
     * @param isLong  true if requesting a DCC long (extended) address
     * @param l  ThrottleListener requesting the throttle steal occur.
     * @param steal true if the request should continue, false otherwise.
     * @since 4.9.2
     */
    @Override
    public void stealThrottleRequest(int address, boolean isLong, ThrottleListener l,boolean steal){
        DccLocoAddress la = new DccLocoAddress(address, isLong);
        stealThrottleRequest(la, l,steal);
    }

    /**
     * Steal a requested throttle.
     *
     * @param address desired LocoAddress 
     * @param l  ThrottleListener requesting the throttle steal occur.
     * @param steal true if the request should continue, false otherwise.
     * @since 4.9.2
     */
    @Override
    public void stealThrottleRequest(LocoAddress address, ThrottleListener l,boolean steal){
       // the default implementation does nothing.
       log.debug("empty stealThrottleRequest() has been activated for address {}, with steal boolean = {}",address.getNumber(),steal);
    }



    /**
     * If the system-specific ThrottleManager has been unable to create the DCC
     * throttle then it needs to be removed from the throttleListeners,
     * otherwise any subsequent request for that address results in the address
     * being reported as already in use, if singleUse is set. This also sends a
     * notification message back to the requestor with a string reason as to why
     * the request has failed.
     *
     * @param address The DCC Loco Address that the request failed on.
     * @param reason  A text string passed by the ThrottleManae as to why
     */
    public void failedThrottleRequest(DccLocoAddress address, String reason) {
        ArrayList<WaitingThrottle> a = throttleListeners.get(address);
        if (a == null) {
            log.warn("failedThrottleRequest with zero-length listeners: " + address);
        } else {
            for (int i = 0; i < a.size(); i++) {
                ThrottleListener l = a.get(i).getListener();
                l.notifyFailedThrottleRequest(address, reason);
            }
        }
        throttleListeners.remove(address);
        ArrayList<WaitingThrottle> p = listenerOnly.get(address);
        if (p == null) {
            log.debug("failedThrottleRequest with zero-length PropertyChange listeners: " + address);
        } else {
            for (int i = 0; i < p.size(); i++) {
                PropertyChangeListener l = p.get(i).getPropertyChangeListener();
                l.propertyChange(new PropertyChangeEvent(this, "attachFailed", address, null));
            }
        }
        listenerOnly.remove(address);
    }

    /**
     * Handle throttle information when it's finally available, e.g. when a new
     * Throttle object has been created.
     * <P>
     * This method creates a throttle for all ThrottleListeners of that address
     * and notifies them via the ThrottleListener.notifyThrottleFound method.
     */
    public void notifyThrottleKnown(DccThrottle throttle, LocoAddress addr, boolean suppressUseIncrements) {
        log.debug("notifyThrottleKnown for " + addr);
        DccLocoAddress dla = (DccLocoAddress) addr;
        Addresses ads = null;
        if (!addressThrottles.containsKey(dla)) {
            log.debug("Address " + addr + "doesn't already exists so will add");
            ads = new Addresses(throttle);
            addressThrottles.put(dla, ads);
        } else {
            addressThrottles.get(dla).setThrottle(throttle);
        }
        ArrayList<WaitingThrottle> a = throttleListeners.get(dla);
        if (a == null) {
            log.debug("notifyThrottleKnown with zero-length listeners: " + addr);
        } else {
            for (int i = 0; i < a.size(); i++) {
                ThrottleListener l = a.get(i).getListener();
                log.debug("Notify listener " + (i + 1) + " of " + a.size() );
                l.notifyThrottleFound(throttle);
                if (suppressUseIncrements == false) {
                    // this is a new throttle
                   addressThrottles.get(dla).incrementUse();
                } else {
                    // requestThrottle() found an existing throttle, we're re-using that one
                    log.debug("incrementUse suppressed");
                }
                addressThrottles.get(dla).addListener(l);
                if (ads != null && a.get(i).getRosterEntry() != null && throttle.getRosterEntry() == null) {
                    throttle.setRosterEntry(a.get(i).getRosterEntry());
                }
            }
            throttleListeners.remove(dla);
        }
        ArrayList<WaitingThrottle> p = listenerOnly.get(dla);
        if (p == null) {
            log.debug("notifyThrottleKnown with zero-length propertyChangeListeners: " + addr);
        } else {
            for (int i = 0; i < p.size(); i++) {
                PropertyChangeListener l = p.get(i).getPropertyChangeListener();
                log.debug("Notify propertyChangeListener");
                l.propertyChange(new PropertyChangeEvent(this, "throttleAssigned", null, dla));
                if (ads != null && p.get(i).getRosterEntry() != null && throttle.getRosterEntry() == null) {
                    throttle.setRosterEntry(p.get(i).getRosterEntry());
                }
                throttle.addPropertyChangeListener(l);
            }
            listenerOnly.remove(dla);
        }
    }
    
    public void notifyThrottleKnown(DccThrottle throttle, LocoAddress addr) {
        notifyThrottleKnown(throttle, addr, false);
    }

    /**
     * When the system-specific ThrottleManager has been unable to create the DCC
     * throttle because it is already in use and must be "stolen" to take control,
     * it needs to notify the listener of this situation.
     * <p>
     * This applies only to those systems where "stealing" applies, such as LocoNet.
     * <p>
     * @param address The DCC Loco Address where controlling requires a steal
     */
    public void notifyStealRequest(DccLocoAddress address) {
        if (throttleListeners != null) {
            ArrayList<WaitingThrottle> a = throttleListeners.get(address);
            if (a == null) {
                log.debug("Cannot issue a steal request to a throttle listener because No throttle listeners registered for address {}",address.getNumber());
                return;
            }
            ThrottleListener l;
            log.debug("{} listener(s) registered for address {}",a.size(),address.getNumber());
            for (int i = 0; i < a.size(); i++) {
                l = a.get(i).getListener();
                log.debug("Notifying a throttle listener (address {}) of the steal situation", address.getNumber());
                l.notifyStealThrottleRequired(address);
            }
        }
    }

    /**
     * Check to see if the Dispatch Button should be enabled or not Default to
     * true, override if necessary
     *
     */
    @Override
    public boolean hasDispatchFunction() {
        return true;
    }

    /**
     * What speed modes are supported by this system? value should be xor of
     * possible modes specifed by the DccThrottle interface
     */
    @Override
    public int supportedSpeedModes() {
        return (DccThrottle.SpeedStepMode128);
    }

    public void attachListener(BasicRosterEntry re, java.beans.PropertyChangeListener p) {
        attachListener(re.getDccLocoAddress(), re, p);
    }

    @Override
    public void attachListener(LocoAddress la, java.beans.PropertyChangeListener p) {
        attachListener((DccLocoAddress) la, null, p);
    }

    public void attachListener(DccLocoAddress la, BasicRosterEntry re, java.beans.PropertyChangeListener p) {
        if (addressThrottles.containsKey(la)) {
            addressThrottles.get(la).getThrottle().addPropertyChangeListener(p);
            p.propertyChange(new PropertyChangeEvent(this, "throttleAssigned", null, la));
            return;
        } else {
            if (!listenerOnly.containsKey(la)) {
                listenerOnly.put(la, new ArrayList<WaitingThrottle>());
            }

            // get the corresponding list to check length
            ArrayList<WaitingThrottle> a = listenerOnly.get(la);
            a.add(new WaitingThrottle(p, re));
            //Only request that the throttle is set up if it hasn't already been
            //requested.
            if ((!throttleListeners.containsKey(la)) && (a.size() == 1)) {
                requestThrottleSetup(la, false);
            }
        }
    }

    @Override
    public void removeListener(LocoAddress la, java.beans.PropertyChangeListener p) {
        if (addressThrottles.containsKey(la)) {
            addressThrottles.get(la).getThrottle().removePropertyChangeListener(p);
            p.propertyChange(new PropertyChangeEvent(this, "throttleRemoved", la, null));
            return;
        }
        p.propertyChange(new PropertyChangeEvent(this, "throttleNotFoundInRemoval", la, null));
    }

    @Override
    public boolean addressStillRequired(LocoAddress la) {
        if (addressThrottles.containsKey(la)) {
            log.debug("usage count is " + addressThrottles.get(la).getUseCount());
            if (addressThrottles.get(la).getUseCount() > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void releaseThrottle(DccThrottle t, ThrottleListener l) {
        disposeThrottle(t, l);
    }

    @Override
    public boolean disposeThrottle(DccThrottle t, ThrottleListener l) {
//        if (!active) log.error("Dispose called when not active");  <-- might need to control this in the sub class
        DccLocoAddress la = (DccLocoAddress) t.getLocoAddress();
        if (addressReleased(la, l)) {
            log.debug("Address " + t.getLocoAddress() + " still has active users");
            return false;
        }
        if (t.getListeners().size() > 0) {
            log.debug("Throttle " + t.getLocoAddress() + " still has active propertyChangeListeners registered to the throttle");
            return false;
        }
        if (addressThrottles.containsKey(la)) {
            addressThrottles.remove(la);
            log.debug("Loco Address removed from the stack " + la);
        } else {
            log.debug("Loco Address not found in the stack " + la);
        }
        return true;
    }

    @Override
    public void dispatchThrottle(DccThrottle t, ThrottleListener l) {
        releaseThrottle(t, l);
    }

    protected boolean addressReleased(DccLocoAddress la, ThrottleListener l) {
        if (addressThrottles.containsKey(la)) {
            if (addressThrottles.get(la).containsListener(l)) {
                log.debug("decrementUse called with listener " + l);
                addressThrottles.get(la).decrementUse();
                addressThrottles.get(la).removeListener(l);
            } else if (l == null) {
                log.debug("decrementUse called withOUT listener");
                /*The release release has been called, but as no listener has 
                 been specified, we can only decrement the use flag*/
                addressThrottles.get(la).decrementUse();
            }
        }
        if (addressThrottles.containsKey(la)) {
            if (addressThrottles.get(la).getUseCount() > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getThrottleInfo(LocoAddress la, String item) {
        DccThrottle t;
        if (addressThrottles.containsKey(la)) {
            t = addressThrottles.get(la).getThrottle();
        } else {
            return null;
        }
        if (item.equals("IsForward")) {
            return t.getIsForward();
        } else if (item.startsWith("Speed")) {
            if (item.equals("SpeedSetting")) {
                return t.getSpeedSetting();
            } else if (item.equals("SpeedIncrement")) {
                return t.getSpeedIncrement();
            } else if (item.equals("SpeedStepMode")) {
                return t.getSpeedStepMode();
            }
        } else if (item.equals(Throttle.F0)) {
            return t.getF0();
        } else if (item.startsWith(Throttle.F1)) {
            if (item.equals(Throttle.F1)) {
                return t.getF1();
            } else if (item.equals(Throttle.F10)) {
                return t.getF10();
            } else if (item.equals(Throttle.F11)) {
                return t.getF11();
            } else if (item.equals(Throttle.F12)) {
                return t.getF12();
            } else if (item.equals(Throttle.F13)) {
                return t.getF13();
            } else if (item.equals(Throttle.F14)) {
                return t.getF14();
            } else if (item.equals(Throttle.F15)) {
                return t.getF15();
            } else if (item.equals(Throttle.F16)) {
                return t.getF16();
            } else if (item.equals(Throttle.F17)) {
                return t.getF17();
            } else if (item.equals(Throttle.F18)) {
                return t.getF18();
            } else if (item.equals(Throttle.F19)) {
                return t.getF19();
            }
        } else if (item.startsWith(Throttle.F2)) {
            if (item.equals(Throttle.F2)) {
                return t.getF2();
            } else if (item.equals(Throttle.F20)) {
                return t.getF20();
            } else if (item.equals(Throttle.F21)) {
                return t.getF21();
            } else if (item.equals(Throttle.F22)) {
                return t.getF22();
            } else if (item.equals(Throttle.F23)) {
                return t.getF23();
            } else if (item.equals(Throttle.F24)) {
                return t.getF24();
            } else if (item.equals(Throttle.F25)) {
                return t.getF25();
            } else if (item.equals(Throttle.F26)) {
                return t.getF26();
            } else if (item.equals(Throttle.F27)) {
                return t.getF27();
            } else if (item.equals(Throttle.F28)) {
                return t.getF28();
            }
        } else if (item.equals(Throttle.F3)) {
            return t.getF3();
        } else if (item.equals(Throttle.F4)) {
            return t.getF4();
        } else if (item.equals(Throttle.F5)) {
            return t.getF5();
        } else if (item.equals(Throttle.F6)) {
            return t.getF6();
        } else if (item.equals(Throttle.F7)) {
            return t.getF7();
        } else if (item.equals(Throttle.F8)) {
            return t.getF8();
        } else if (item.equals(Throttle.F9)) {
            return t.getF9();
        }
        return null;
    }

    /**
     * This subClass, keeps track of which loco address have been requested and
     * by whom, it primarily uses a increment count to keep track of all the the
     * Addresses in use as not all external code will have been refactored over
     * to use the new disposeThrottle.
     */
    protected static class Addresses {

        int useActiveCount = 0;
        DccThrottle throttle = null;
        ArrayList<ThrottleListener> listeners = new ArrayList<ThrottleListener>();
        BasicRosterEntry re = null;

        protected Addresses(DccThrottle throttle) {
            this.throttle = throttle;
        }

        void incrementUse() {
            useActiveCount++;
            log.debug(throttle.getLocoAddress() + " increased Use Size to " + useActiveCount);
        }

        void decrementUse() {
            // Do not want to go below 0 on the usage front!
            if (useActiveCount > 0) {
                useActiveCount--;
            }
            log.debug(throttle.getLocoAddress() + " decreased Use Size to " + useActiveCount);
        }

        int getUseCount() {
            return useActiveCount;
        }

        DccThrottle getThrottle() {
            return throttle;
        }

        void setThrottle(DccThrottle throttle) {
            DccThrottle old = this.throttle;
            this.throttle = throttle;
            if ((old == null) || (old == throttle)) {
                return;
            }

            //As the throttle has changed, we need to inform the listeners
            //However if a throttle hasn't used the new code, it will not have been
            //removed and will get a notification.
            log.debug(throttle.getLocoAddress() + " throttle assigned "
                    + "has been changed need to notify throttle users");

            this.throttle = throttle;
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).notifyThrottleFound(throttle);
            }
            //This handles moving the listeners from the old throttle to the new one
            DccLocoAddress la = (DccLocoAddress) this.throttle.getLocoAddress();
            Vector<PropertyChangeListener> v = old.getListeners();
            for (PropertyChangeListener prop : v) {
                this.throttle.addPropertyChangeListener(prop);
                prop.propertyChange(new PropertyChangeEvent(this, "throttleAssignmentChanged", null, la));
            }
        }

        void setRosterEntry(BasicRosterEntry _re) {
            re = _re;
        }

        BasicRosterEntry getRosterEntry() {
            return re;
        }

        void addListener(ThrottleListener l) {
            // Check for duplication here
            if (listeners.contains(l))
                log.debug("this Addresses listeners already includes listener" + l);
            else 
                listeners.add(l);
        }

        void removeListener(ThrottleListener l) {
            listeners.remove(l);
        }

        boolean containsListener(ThrottleListener l) {
            return listeners.contains(l);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractThrottleManager.class);
}
