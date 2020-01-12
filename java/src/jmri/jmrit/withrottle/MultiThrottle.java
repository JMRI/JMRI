package jmri.jmrit.withrottle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of what locos are being controlled by a throttle, and passes the
 * control messages on to them. Creates a new MultiThrottleController for each
 * loco requested on this throttle. Each loco will then be able to be controlled
 * individually. '*' is a wildcard loco key. Forwards to all locos on this
 * MultiThrottle.
 * <p>
 * Sample messages:<ul>
 * <li> {@literal MT+L757<;>L757} On T throttle, add loco L757.
 * <li> {@literal MT+L1234<;>L1234} On T throttle, add loco L1234.
 * <li> {@literal MTSL1234<;>L1234} On T throttle, steal loco L1234.
 * <li> {@literal MTAL757<;>R1} On T throttle, loco L757, set direction to
 * forward.
 * <li> {@literal MTAL1234<;>R0} On T throttle, loco L1234, set direction to
 * reverse.
 * <li> {@literal MTAL757<;>V42} On T throttle, loco L757, set speed to 42.
 * <li> {@literal MTAL1234<;>V42} On T throttle, loco L1234, set speed to 42.
 * <li> {@literal MTA*<;>V16} On T throttle, all locos, set speed to 16.
 * <li> {@literal MT-L757<;>L757} On T throttle, remove loco L757. (Still has
 * L1234)
 * </ul>
 *
 * @author Brett Hoffman Copyright (C) 2011
 */
public class MultiThrottle {

    private ThrottleControllerListener parentTCL = null;
    private ControllerInterface parentController = null;
    char whichThrottle;
    HashMap<String, MultiThrottleController> throttles;

    public MultiThrottle(char id, ThrottleControllerListener tcl, ControllerInterface ci) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new MultiThrottle for id: " + id);
        }
        whichThrottle = id;
        parentTCL = tcl;
        parentController = ci;
    }

    /**
     * Handle a message sent from the device. A key is used to send an action to
     * the correct loco. '*' is a wildcard key, sends action to all locos in
     * this MultiThrottle.
     *
     * @param message Consists of a control character, the loco's key, a
     *                separator {@literal "<;>"}, and the action to forward to
     *                the MultiThrottleController.
     */
    public void handleMessage(String message) {
        log.debug("MT handleMessage: {}", message);
        List<String> unit = Arrays.asList(message.substring(1).split("<;>"));
        String key = unit.get(0);
        String action = unit.get(1);
        if ((key == null) || (action == null)) {
            return;
        }

        switch (message.charAt(0)) {
            case 'A':  //  'A'ction
                passActionsToControllers(key, action);
                break;
            case '+':  //  add loco
                addThrottleController(key, action);
                break;
            case '-':  //  remove loco
                removeThrottleController(key, action);
                break;
            case 'S':   //  Steal loco
                stealThrottleController(key, action);
                break;
            default:
                log.warn("Unhandled code: {}", message.charAt(0));
                break;
        }   //  end switch

    }

    private MultiThrottleController createThrottleController(String key) {
        if (!isValidAddr(key) ) { //make sure address is acceptable before proceeding
            return null;
        }
        if (throttles == null) {
            throttles = new HashMap<>(1);
        }

        if (throttles.containsKey(key)) {
            log.debug("Throttle: {} already in MultiThrottle consist.", key);
            return null;
        }
        MultiThrottleController mtc = new MultiThrottleController(whichThrottle, key, parentTCL, parentController);
        throttles.put(key, mtc);
        log.debug("Throttle: {} added to MultiThrottle consist.", key);
        return mtc;
    }
    
    protected void addThrottleController(String key, String action) {   //  key is address format L#### or S##
        MultiThrottleController mtc = createThrottleController(key);
        if (mtc != null) {
            //  This will request the loco as a DccTrottle
            mtc.sort(action);
        }
    }
    
    protected void stealThrottleController(String key, String action) {
        MultiThrottleController mtc = createThrottleController(key);
        if (mtc != null) {
            //  This will request the loco as a DccTrottle
            mtc.isStealAddress = true;
            mtc.sort(action);
        }
        log.debug("Throttle: {} stolen to MultiThrottle consist.", key);
    }

    /**
     * Validate that address is going to be allowed by throttle controller. 
     *   If not, send an error string to client.
     *
     * @param key address to be validated, of form Lnnnn or Snnn
     */
    private boolean isValidAddr(String key) {
        if (key.length() < 2) {
            String msg = Bundle.getMessage("ErrorInvalidAddressFormat", key);
            log.warn(msg);
            parentController.sendAlertMessage(msg);
            return false;
        }
        try {
            int addr = Integer.parseInt(key.substring(1));
            if (key.charAt(0) == 'L') {
                if (jmri.InstanceManager.throttleManagerInstance().canBeLongAddress(addr)) {
                    return true;
                } else {
                    String msg = Bundle.getMessage("ErrorLongAddress", key);
                    log.warn(msg);
                    parentController.sendAlertMessage(msg);
                    return false;
                }
            } else if (key.charAt(0) == 'S') {
                if (jmri.InstanceManager.throttleManagerInstance().canBeShortAddress(addr)) {
                    return true;
                } else {
                    String msg = Bundle.getMessage("ErrorShortAddress", key);
                    log.warn(msg);
                    parentController.sendAlertMessage(msg);
                    return false;
                }
            }
            String msg = Bundle.getMessage("ErrorInvalidAddressFormat", key);
            parentController.sendAlertMessage(msg);
            log.warn(msg);
            return false;
        } catch (NumberFormatException e) {
            String msg = Bundle.getMessage("ErrorInvalidAddressFormat", key);
            parentController.sendAlertMessage(msg);
            log.warn(msg);
            return false;
        }
    }

    protected boolean removeThrottleController(String key, String action) {

        if (throttles == null) {
            log.debug("No MultiThrottle to remove {} from.", key);
            return false;
        }
        if (key.equals("*")) {
            ArrayList<String> throttleKeys = new ArrayList<String>(throttles.keySet());  //copy to avoid concurrentModificationException
            throttleKeys.forEach((throttle) -> {
                removeThrottleController(throttle, action);
                //  Runs each loco through this method individually
            });
            return true;
        }
        if (!throttles.containsKey(key)) {
            if (log.isDebugEnabled()) {
                log.debug("Throttle: " + key + " not in MultiThrottle.");
            }
            return false;
        }
        MultiThrottleController mtc = throttles.get(key);
        mtc.sort(action);
        mtc.shutdownThrottle();
        mtc.removeControllerListener(parentController);
        mtc.removeThrottleControllerListener(parentTCL);
        throttles.remove(key);
        if (log.isDebugEnabled()) {
            log.debug("Throttle: " + key + " removed from MultiThrottle.");
        }
        return true;
    }

    protected void passActionsToControllers(String key, String action) {
        if (throttles == null) {
            log.debug("No throttles in MultiThrottle to receive action.");
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("MultiThrottle key: " + key + ", action: " + action);
        }

        if (key.equals("*")) {
            ArrayList<String> throttleKeys = new ArrayList<String>(throttles.keySet());  //copy to avoid concurrentModificationException
            throttleKeys.forEach((throttle) -> {
                passActionsToControllers(throttle, action);
                //  Runs each loco through this method individually
            });
            return;
        }
        if (throttles.containsKey(key)) {
            throttles.get(key).sort(action);
        }
    }

    public void dispose() {
        if (throttles == null) {
            return;
        }
        ArrayList<String> throttleKeys = new ArrayList<String>(throttles.keySet());  //copy to avoid concurrentModificationException
        throttleKeys.forEach((throttle) -> {
            removeThrottleController(throttle, "r");
        });
    }

    public void eStop() {
        if (throttles == null) {
            return;
        }
        ArrayList<String> throttleKeys = new ArrayList<String>(throttles.keySet());  //copy to avoid concurrentModificationException
        throttleKeys.forEach((throttle) -> {
            passActionsToControllers(throttle, "X");
        });
    }

    /**
     * A request for a this address has been cancelled, clean up the waiting
     * MultiThrottleController. If the MTC is marked as a steal, this cancel needs 
     * to not happen.
     *
     * @param key The string to use as a key to remove the proper
     *            MultiThrottleController
     */
    public void canceledThrottleRequest(String key) {
        if (throttles == null) {
            log.warn("No MultiThrottle to remove {} from.", key);
            return;
        }
        if (!throttles.containsKey(key)) {
            if (log.isDebugEnabled()) {
                log.debug("Throttle: {} not in MultiThrottle.", key);
            }
            return;
        }
        MultiThrottleController mtc = throttles.get(key);
        if (!mtc.isStealAddress) {
            mtc.removeControllerListener(parentController);
            throttles.remove(key);
            log.debug("Throttle: {} cancelled from MultiThrottle.", key);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(MultiThrottle.class);
}
