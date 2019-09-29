package jmri.jmrix;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import javax.annotation.Nonnull;
import jmri.BasicRosterEntry;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.Throttle;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of a ThrottleManager.
 * <p>
 * Based on Glen Oberhauser's original LnThrottleManager implementation.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Steve Rawlinson Copyright (C) 2016
 */
abstract public class AbstractThrottleManager implements ThrottleManager {

    public AbstractThrottleManager() {
    }

    public AbstractThrottleManager(SystemConnectionMemo memo) {
        adapterMemo = memo;
    }

    protected SystemConnectionMemo adapterMemo;

    protected String userName = "Internal";

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public LocoAddress.Protocol[] getAddressProtocolTypes() {
        return new LocoAddress.Protocol[]{
            LocoAddress.Protocol.DCC,
            LocoAddress.Protocol.DCC_SHORT,
            LocoAddress.Protocol.DCC_LONG};
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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
    private final HashMap<LocoAddress, ArrayList<WaitingThrottle>> throttleListeners = new HashMap<>(5);

    static class WaitingThrottle {

        ThrottleListener l;
        BasicRosterEntry re;
        PropertyChangeListener pl;
        boolean canHandleDecisions;

        WaitingThrottle(ThrottleListener _l, BasicRosterEntry _re, boolean _canHandleDecisions) {
            l = _l;
            re = _re;
            canHandleDecisions = _canHandleDecisions;
        }

        WaitingThrottle(PropertyChangeListener _pl, BasicRosterEntry _re, boolean _canHandleDecisions) {
            pl = _pl;
            re = _re;
            canHandleDecisions = _canHandleDecisions;
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
        
        boolean canHandleDecisions() {
            return canHandleDecisions;
        }
        
    }
    
    /**
     * listenerOnly is indexed by the address, and contains as elements an
     * ArrayList of propertyChangeListeners objects that have requested
     * notification of changes to a throttle that hasn't yet been created/ The
     * entries in this Hashmap are only valid during the throttle setup process.
     */
    private final HashMap<LocoAddress, ArrayList<WaitingThrottle>> listenerOnly = new HashMap<>(5);

    /**
     * Keeps a map of all the current active DCC loco Addresses that are in use.
     * <p>
     * addressThrottles is indexed by the address, and contains as elements a
     * subclass of the throttle assigned to an address and the number of
     * requests and active users for this address.
     */
    private final Hashtable<LocoAddress, Addresses> addressThrottles = new Hashtable<>();

    /**
     * Does this DCC system allow a Throttle (e.g. an address) to be used by
     * only one user at a time?
     * @return true or false
     */
    protected boolean singleUse() {
        return true;
    }
    
    /**
     * @deprecated since 4.15.7; use
     * #requestThrottle(BasicRosterEntry, ThrottleListener, boolean) instead
     */
    @Deprecated
    @Override
    public boolean requestThrottle(BasicRosterEntry re, ThrottleListener l) {
        return requestThrottle(re, l, false);
    }
    
    /**
     * @deprecated since 4.15.7; use
     * #requestThrottle(BasicRosterEntry, ThrottleListener, boolean) instead
     */
    @Deprecated
    @Override
    public boolean requestThrottle(int address, boolean isLongAddress, ThrottleListener l) {
        DccLocoAddress la = new DccLocoAddress(address, isLongAddress);
        return requestThrottle(la, l, false);
    }
    
    /**
     * @deprecated since 4.15.7; use
     * #requestThrottle(LocoAddress, ThrottleListener, boolean) instead
     */
    @Deprecated
    @Override
    public boolean requestThrottle(LocoAddress la, ThrottleListener l) {
        return requestThrottle(la, l, false);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requestThrottle(int address, boolean isLongAddress, ThrottleListener l, boolean canHandleDecisions) {
        DccLocoAddress la = new DccLocoAddress(address, isLongAddress);
        return requestThrottle(la, null, l, canHandleDecisions);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requestThrottle(@Nonnull BasicRosterEntry re, ThrottleListener l, boolean canHandleDecisions) {
        return requestThrottle(re.getDccLocoAddress(), re, l, canHandleDecisions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requestThrottle(LocoAddress la, ThrottleListener l, boolean canHandleDecisions) {
        return requestThrottle(la, null, l, canHandleDecisions);
    }
    
    /**
     * @deprecated since 4.15.7; use
     * #requestThrottle(LocoAddress, ThrottleListener, boolean) instead
     */
    @Deprecated
    @Override
    public boolean requestThrottle(LocoAddress la, BasicRosterEntry re, ThrottleListener l) {
        return requestThrottle(re, l, false);
    }

    /**
     * Request a throttle, given a decoder address. When the decoder address is
     * located, the ThrottleListener gets a callback via the
     * ThrottleListener.notifyThrottleFound method.
     *
     * @param la LocoAddress of the decoder desired.
     * @param l  The ThrottleListener awaiting notification of a found throttle.
     * @param re A BasicRosterEntry can be passed, this is attached to a throttle after creation.
     * @return True if the request will continue, false if the request will not
     *         be made. False may be returned if a the throttle is already in
     *         use.
     */
    protected boolean requestThrottle(LocoAddress la, BasicRosterEntry re, ThrottleListener l, boolean canHandleDecisions) {
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
            log.debug("A throttle to address {} already exists, so will return that throttle",la.getNumber());
            a.add(new WaitingThrottle(l, re, canHandleDecisions));
            notifyThrottleKnown(addressThrottles.get(la).getThrottle(), la);
            return throttleFree;
        } else {
            log.debug("{} has not been created before",la.getNumber() );
        }

        log.debug("After request in ATM: {}",a.size());
        
        // check length
        if (singleUse() && (a.size() > 0)) {
            throttleFree = false;
            log.debug("singleUser() is true, and the list of WaitingThrottles isn't empty, returning false");
        } else if (a.size() == 0) {
            a.add(new WaitingThrottle(l, re, canHandleDecisions));
            log.debug("list of WaitingThrottles is empty: {}; {}",la, a);
            log.debug("calling requestThrottleSetup()");
            requestThrottleSetup(la, true);
        } else {
            a.add(new WaitingThrottle(l, re, canHandleDecisions));
            log.debug("singleUse() returns false and there are existing WaitThrottles, adding a one to the list");
        }
        return throttleFree;
    }


    /**
     * Request Throttle with no Steal / Share Callbacks
     * {@inheritDoc}
     * Request a throttle, given a decoder address. When the decoder address is
     * located, the ThrottleListener gets a callback via the
     * ThrottleListener.notifyThrottleFound method.
     * <p>
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
        return requestThrottle(new DccLocoAddress(address,isLong), null, l, false);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requestThrottle(int address, ThrottleListener l, boolean canHandleDecisions) {
        boolean isLong = true;
        if (canBeShortAddress(address)) {
            isLong = false;
        }
        return requestThrottle(new DccLocoAddress(address,isLong), null, l, canHandleDecisions);
    }

    /**
     * Abstract member to actually do the work of configuring a new throttle,
     * usually via interaction with the DCC system
     * @param a  address
     * @param control  false  - read only.
     */
    abstract public void requestThrottleSetup(LocoAddress a, boolean control);

    /**
     * Abstract member to actually do the work of configuring a new throttle,
     * usually via interaction with the DCC system
     * @param a  address.
     */
    public void requestThrottleSetup(LocoAddress a) {
        requestThrottleSetup(a, true);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelThrottleRequest(int address, boolean isLong, ThrottleListener l) {
        DccLocoAddress la = new DccLocoAddress(address, isLong);
        cancelThrottleRequest(la, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelThrottleRequest(BasicRosterEntry re, ThrottleListener l) {
            cancelThrottleRequest(re.getDccLocoAddress(), l);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelThrottleRequest(LocoAddress la, ThrottleListener l) {
        // failedThrottleRequest(la, "Throttle request was cancelled."); // needs I18N
        if (throttleListeners != null) {
            ArrayList<WaitingThrottle> a = throttleListeners.get(la);
            if (a == null || l == null ) {
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
     * {@inheritDoc}
     * Cancel a request for a throttle.
     * <p>
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
     * <p>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form.
     *
     * @deprecated since 4.15.7; use #responseThrottleDecision
     *
     */
    @Deprecated
    @Override
    public void stealThrottleRequest(BasicRosterEntry re, ThrottleListener l,boolean steal){
        if (steal) {
            responseThrottleDecision(re.getDccLocoAddress(), l, ThrottleListener.DecisionType.STEAL);
        }
        else {
            cancelThrottleRequest(re.getDccLocoAddress(), l);
        }
    }
    
    /**
     * Steal a requested throttle.
     * <p>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form.
     *
     * @deprecated since 4.15.7; use #responseThrottleDecision
     *
     */
    @Deprecated
    @Override
    public void stealThrottleRequest(int address, ThrottleListener l,boolean steal){
        boolean isLong = true;
        if (canBeShortAddress(address)) {
            isLong = false;
        }
        DccLocoAddress la = new DccLocoAddress(address, isLong);
        if (steal) {
            responseThrottleDecision(la, l, ThrottleListener.DecisionType.STEAL);
        }
        else {
            cancelThrottleRequest(la, l);
        }
    }

    /**
     * Steal a requested throttle.
     *
     * @deprecated since 4.15.7; use #responseThrottleDecision
     *
     * @param address desired decoder address
     * @param isLong  true if requesting a DCC long (extended) address
     * @param l  ThrottleListener requesting the throttle steal occur.
     * @param steal true if the request should continue, false otherwise.
     * @since 4.9.2
     */
    @Deprecated
    @Override
    public void stealThrottleRequest(int address, boolean isLong, ThrottleListener l,boolean steal){
        DccLocoAddress la = new DccLocoAddress(address, isLong);
        if (steal) {
            responseThrottleDecision(la, l, ThrottleListener.DecisionType.STEAL);
        }
        else {
            cancelThrottleRequest(la, l);
        }
    }
    
    /**
     * @deprecated since 4.15.7; use #responseThrottleDecision
     */
    @Deprecated
    @Override
    public void stealThrottleRequest(LocoAddress address, ThrottleListener l, boolean steal){
        if (steal) {
            responseThrottleDecision(address, l, ThrottleListener.DecisionType.STEAL);
        }
        else {
            cancelThrottleRequest(address, l);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void responseThrottleDecision(int address, ThrottleListener l, ThrottleListener.DecisionType decision) {
        boolean isLong = true;
        if (canBeShortAddress(address)) {
            isLong = false;
        }
        responseThrottleDecision(address, isLong, l, decision);

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void responseThrottleDecision(int address, boolean isLong, ThrottleListener l, ThrottleListener.DecisionType decision) {
        DccLocoAddress la = new DccLocoAddress(address, isLong);
        responseThrottleDecision(la,l,decision);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void responseThrottleDecision(LocoAddress address, ThrottleListener l, ThrottleListener.DecisionType decision) {
        log.debug("Received response from ThrottleListener, this method should be overridden by a hardware type");
    }

    /**
     * If the system-specific ThrottleManager has been unable to create the DCC
     * throttle then it needs to be removed from the throttleListeners,
     * otherwise any subsequent request for that address results in the address
     * being reported as already in use, if singleUse is set. This also sends a
     * notification message back to the requestor with a string reason as to why
     * the request has failed.
     *
     * @param address The Loco Address that the request failed on.
     * @param reason  A text string passed by the ThrottleManager as to why
     */
    public void failedThrottleRequest(LocoAddress address, String reason) {
        ArrayList<WaitingThrottle> a = throttleListeners.get(address);
        if (a == null) {
            log.warn("failedThrottleRequest with zero-length listeners: {}", address);
        } else {
            for (int i = 0; i < a.size(); i++) {
                ThrottleListener l = a.get(i).getListener();
                l.notifyFailedThrottleRequest(address, reason);
            }
        }
        throttleListeners.remove(address);
        ArrayList<WaitingThrottle> p = listenerOnly.get(address);
        if (p == null) {
            log.debug("failedThrottleRequest with zero-length PropertyChange listeners: {}", address);
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
     * <p>
     * This method creates a throttle for all ThrottleListeners of that address
     * and notifies them via the ThrottleListener.notifyThrottleFound method.
     * @param throttle  throttle object
     * @param addr  address.
     */
    public void notifyThrottleKnown(DccThrottle throttle, LocoAddress addr) {
        log.debug("notifyThrottleKnown for {}", addr);
        Addresses ads = null;
        if (!addressThrottles.containsKey(addr)) {
            log.debug("Address {} doesn't already exists so will add",addr);
            ads = new Addresses(throttle);
            addressThrottles.put(addr, ads);
        } else {
            addressThrottles.get(addr).setThrottle(throttle);
        }
        ArrayList<WaitingThrottle> a = throttleListeners.get(addr);
        if (a == null) {
            log.debug("notifyThrottleKnown with zero-length listeners: {}", addr);
        } else {
            for (int i = 0; i < a.size(); i++) {
                ThrottleListener l = a.get(i).getListener();
                log.debug("Notify listener {} of {}",(i + 1),a.size() );
                l.notifyThrottleFound(throttle);
                addressThrottles.get(addr).incrementUse();
                addressThrottles.get(addr).addListener(l);
                if (ads != null && a.get(i).getRosterEntry() != null && throttle.getRosterEntry() == null) {
                    throttle.setRosterEntry(a.get(i).getRosterEntry());
                }
                updateNumUsers(addr,addressThrottles.get(addr).getUseCount());
            }
            throttleListeners.remove(addr);
        }
        ArrayList<WaitingThrottle> p = listenerOnly.get(addr);
        if (p == null) {
            log.debug("notifyThrottleKnown with zero-length propertyChangeListeners: {}", addr);
        } else {
            for (int i = 0; i < p.size(); i++) {
                PropertyChangeListener l = p.get(i).getPropertyChangeListener();
                log.debug("Notify propertyChangeListener");
                l.propertyChange(new PropertyChangeEvent(this, "throttleAssigned", null, addr));
                if (ads != null && p.get(i).getRosterEntry() != null && throttle.getRosterEntry() == null) {
                    throttle.setRosterEntry(p.get(i).getRosterEntry());
                }
                throttle.addPropertyChangeListener(l);
            }
            listenerOnly.remove(addr);
        }
    }


    /**
     * For when a steal / share decision is needed and the ThrottleListener has delegated
     * this decision to the ThrottleManager.
     * <p>
     * Responds to the question by requesting a Throttle "Steal" by default.
     * <p>
     * Can be overridden by hardware types which do not wish the default behaviour to Steal.
     * <p>
     * This applies only to those systems where "stealing" or "sharing" applies, such as LocoNet.
     * <p>
     * @param address The LocoAddress the steal / share question relates to
     * @param question The Question to be put to the ThrottleListener
     */
    protected void makeHardwareDecision(LocoAddress address, ThrottleListener.DecisionType question){
        responseThrottleDecision(address, null, ThrottleListener.DecisionType.STEAL );
    }

    /**
     * When the system-specific ThrottleManager has been unable to create the DCC
     * throttle because it is already in use and must be "stolen" or "shared" to take control,
     * it needs to notify the listener of this situation.
     * <p>
     * This applies only to those systems where "stealing" or "sharing" applies, such as LocoNet.
     * <p>
     * @param address The LocoAddress the steal / share question relates to
     * @param question The Question to be put to the ThrottleListener
     * This applies only to those systems where "stealing" applies, such as LocoNet.
     */
    protected void notifyDecisionRequest(LocoAddress address, ThrottleListener.DecisionType question) {
        
        if (throttleListeners != null) {
            ArrayList<WaitingThrottle> a = throttleListeners.get(address);
            if (a == null) {
                log.debug("Cannot issue question, No throttle listeners registered for address {}",address.getNumber());
                return;
            }
            ThrottleListener l;
            log.debug("{} listener(s) registered for address {}",a.size(),address.getNumber());
            for (int i = 0; i < a.size(); i++) {
                if (a.get(i).canHandleDecisions() ){
                    l = a.get(i).getListener();
                    log.debug("Notifying a throttle listener (address {}) of the steal share situation", address.getNumber());
                    l.notifyDecisionRequired(address,question);
                }
                else {
                    log.debug("Passing {} to hardware steal / share decision making", address.getNumber());
                    makeHardwareDecision(address,question);
                }
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
    public EnumSet<SpeedStepMode> supportedSpeedModes() {
        return EnumSet.of(SpeedStepMode.NMRA_DCC_128);
    }
    
    /**
     * Hardware that uses the Silent Steal preference
     * will need to override
     * {@inheritDoc}
     */
    @Override
    public boolean enablePrefSilentStealOption() {
        return false;
    }
    
    /**
     * Hardware that uses the Silent Share preference
     * will need to override
     * {@inheritDoc}
     */
    @Override
    public boolean enablePrefSilentShareOption() {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void attachListener(LocoAddress la, java.beans.PropertyChangeListener p) {
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
            a.add(new WaitingThrottle(p, null, false));
            //Only request that the throttle is set up if it hasn't already been
            //requested.
            if ((!throttleListeners.containsKey(la)) && (a.size() == 1)) {
                requestThrottleSetup(la, false);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(LocoAddress la, java.beans.PropertyChangeListener p) {
        if (addressThrottles.containsKey(la)) {
            addressThrottles.get(la).getThrottle().removePropertyChangeListener(p);
            p.propertyChange(new PropertyChangeEvent(this, "throttleRemoved", la, null));
            return;
        }
        p.propertyChange(new PropertyChangeEvent(this, "throttleNotFoundInRemoval", la, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addressStillRequired(LocoAddress la) {
        if (addressThrottles.containsKey(la)) {
            log.debug("usage count is {}", addressThrottles.get(la).getUseCount());
            if (addressThrottles.get(la).getUseCount() > 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addressStillRequired(int address, boolean isLongAddress) {
        DccLocoAddress la = new DccLocoAddress(address, isLongAddress);
        return addressStillRequired(la);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addressStillRequired(int address) {
        boolean isLong = true;
        if (canBeShortAddress(address)) {
            isLong = false;
        }
        return addressStillRequired(address, isLong);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addressStillRequired(BasicRosterEntry re) {
        return addressStillRequired(re.getDccLocoAddress());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseThrottle(DccThrottle t, ThrottleListener l) {
        log.debug("AbstractThrottleManager.releaseThrottle: {}, {}", t, l);
        disposeThrottle(t, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean disposeThrottle(DccThrottle t, ThrottleListener l) {
        log.debug("AbstractThrottleManager.disposeThrottle: {}, {}", t, l);

//        if (!active) log.error("Dispose called when not active");  <-- might need to control this in the sub class
        LocoAddress la = t.getLocoAddress();
        if (addressReleased(la, l)) {
            log.debug("Address {} still has active users",t.getLocoAddress());
            return false;
        }
        if (t.getListeners().size() > 0) {
            log.debug("Throttle {} still has active propertyChangeListeners registered to the throttle",t.getLocoAddress());
            return false;
        }
        if (addressThrottles.containsKey(la)) {
            addressThrottles.remove(la);
            log.debug("Loco Address {} removed from the stack ", la);
        } else {
            log.debug("Loco Address {} not found in the stack ", la);
        }
        return true;
    }
    
    /**
     * Throttle can no longer be relied upon,
     * potentially from an external forced steal or hardware error.
     * <p>
     * Normally, #releaseThrottle should be used to close throttles.
     * <p>
     * Removes locoaddress from list to force new throttle requests
     * to request new sessions where the Command station model
     * implements a dynamic stack, not a static stack.
     * 
     * <p>
     * Managers still need to advise listeners that the session has 
     * been cancelled and actually dispose of the throttle
     */
    protected void forceDisposeThrottle(LocoAddress la) {
        log.debug("force dispose address {}",la);
        if (addressThrottles.containsKey(la)) {
            addressThrottles.remove(la);
            log.debug("Loco Address {} removed from the stack ", la);
        } else {
            log.debug("Loco Address {} not found in the stack ", la);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispatchThrottle(DccThrottle t, ThrottleListener l) {
        releaseThrottle(t, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getThrottleUsageCount(LocoAddress la) {
        if (addressThrottles.containsKey( la)) {
            return addressThrottles.get(la).getUseCount();
        }
        return 0;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getThrottleUsageCount(int address, boolean isLongAddress) {
        DccLocoAddress la = new DccLocoAddress(address, isLongAddress);
        return getThrottleUsageCount(la);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getThrottleUsageCount(int address) {
        boolean isLong = true;
        if (canBeShortAddress(address)) {
            isLong = false;
        }
        return getThrottleUsageCount(address, isLong);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getThrottleUsageCount(BasicRosterEntry re) {
        return getThrottleUsageCount(re.getDccLocoAddress());
    }

    /**
     * Release a Throttle from a ThrottleListener.
     * @return True if throttle still has listeners or a positive use count, else False.
     */
    protected boolean addressReleased(LocoAddress la, ThrottleListener l) {
        if (addressThrottles.containsKey(la)) {
            if (addressThrottles.get(la).containsListener(l)) {
                log.debug("decrementUse called with listener {}", l);
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
                updateNumUsers(la,addressThrottles.get(la).getUseCount());
                log.debug("addressReleased still has at least one listener");
                return true;
            }
        }
        return false;
    }
    
    /**
     * The number of users of this throttle has been updated
     * <p>
     * Typically used to update dispatch / release availablility
     * specific implementations can override this function to get updates
     *
     * @param la the Loco Address which has been updated
     */
    protected void updateNumUsers( LocoAddress la, int numUsers ){
        log.debug("Throttle {} now has {} users",la,numUsers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getThrottleInfo(LocoAddress la, String item) {
        DccThrottle t;
        if (addressThrottles.containsKey(la)) {
            t = addressThrottles.get(la).getThrottle();
        } else {
            return null;
        }
        if (item.equals(Throttle.ISFORWARD)) {
            return t.getIsForward();
        } else if (item.startsWith("Speed")) {
            if (item.equals(Throttle.SPEEDSETTING)) {
                return t.getSpeedSetting();
            } else if (item.equals(Throttle.SPEEDINCREMENT)) {
                return t.getSpeedIncrement();
            } else if (item.equals(Throttle.SPEEDSTEPMODE)) {
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

    private boolean _hideStealNotifications = false;

    /**
     * If not headless, display a session stolen dialogue box with
     * checkbox to hide notifications for rest of JMRI session
     *
     * @param address the LocoAddress of the stolen / cancelled Throttle
     */
    protected void showSessionCancelDialogue(LocoAddress address){
        if ((!java.awt.GraphicsEnvironment.isHeadless()) && (!_hideStealNotifications)){
            jmri.util.ThreadingUtil.runOnGUI(() -> {
                javax.swing.JCheckBox checkbox = new javax.swing.JCheckBox(
                    Bundle.getMessage("HideFurtherAlerts"));
                Object[] params = {Bundle.getMessage("LocoStolen",address), checkbox};
                javax.swing.JOptionPane pane = new javax.swing.JOptionPane(params);
                pane.setMessageType(javax.swing.JOptionPane.WARNING_MESSAGE);
                javax.swing.JDialog dialog = pane.createDialog(null, Bundle.getMessage("LocoStolen", address));
                dialog.setModal(false);
                dialog.setVisible(true);
                dialog.requestFocus();
                dialog.toFront();
                java.awt.event.ActionListener stolenpopupcheckbox = (java.awt.event.ActionEvent evt) -> {
                    this.hideStealNotifications(checkbox.isSelected());
                };
                checkbox.addActionListener(stolenpopupcheckbox);
            });
        }
    }
    
    /**
     * Receive notification from a throttle dialogue
     * to display steal dialogues for rest of the JMRI instance session.
     * False by default to show notifications
     *
     * @param hide set True to hide notifications, else False.
     */
    public void hideStealNotifications(boolean hide){
        _hideStealNotifications = hide;
    }

    /**
     * This subClass, keeps track of which loco address have been requested and
     * by whom, it primarily uses a increment count to keep track of all the
     * Addresses in use as not all external code will have been refactored over
     * to use the new disposeThrottle.
     */
    protected static class Addresses {

        int useActiveCount = 0;
        DccThrottle throttle = null;
        ArrayList<ThrottleListener> listeners = new ArrayList<>();
        BasicRosterEntry re = null;

        protected Addresses(DccThrottle throttle) {
            this.throttle = throttle;
        }

        void incrementUse() {
            useActiveCount++;
            log.debug("{} increased Use Size to {}",throttle.getLocoAddress(),useActiveCount);
        }

        void decrementUse() {
            // Do not want to go below 0 on the usage front!
            if (useActiveCount > 0) {
                useActiveCount--;
            }
            log.debug("{} decreased Use Size to {}",throttle.getLocoAddress(),useActiveCount);
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
            log.debug("Throttle assigned {} has been changed, need to notify throttle users", throttle.getLocoAddress() );

            this.throttle = throttle;
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).notifyThrottleFound(throttle);
            }
            //This handles moving the listeners from the old throttle to the new one
            LocoAddress la = this.throttle.getLocoAddress();
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
                log.debug("this Addresses listeners already includes listener {}",l);
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
