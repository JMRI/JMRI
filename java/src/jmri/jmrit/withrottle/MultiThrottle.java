package jmri.jmrit.withrottle;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
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
    Hashtable<String, MultiThrottleController> throttles;

    public MultiThrottle(char id, ThrottleControllerListener tcl, ControllerInterface ci) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new MultiThrottle for id: " + id);
        }
        new Hashtable<String, MultiThrottleController>(1);
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
        log.debug("MT handleMessage: " + message);
        List<String> unit = Arrays.asList(message.substring(1).split("<;>"));
        String key = unit.get(0);
        String action = unit.get(1);
        if ((key == null) || (action == null)) {
            return;
        }

        switch (message.charAt(0)) {
            case 'A': {  //  'A'ction
                passActionsToControllers(key, action);
                break;
            }

            case '+': {  //  add loco
                addThrottleController(key, action);
                break;
            }

            case '-': {  //  remove loco
                removeThrottleController(key, action);
                break;
            }
        }   //  end switch

    }

    public boolean addThrottleController(String key, String action) {   //  key is address format L#### or S##
        if (throttles == null) {
            throttles = new Hashtable<String, MultiThrottleController>(1);
        }

        if (throttles.containsKey(key)) {
            if (log.isDebugEnabled()) {
                log.debug("Throttle: " + key + " already in MultiThrottle consist.");
            }
            return false;
        }
        MultiThrottleController mtc = new MultiThrottleController(whichThrottle, key, parentTCL, parentController);
        throttles.put(key, mtc);

        //  This will request the loco as a DccTrottle
        mtc.sort(action);

        if (log.isDebugEnabled()) {
            log.debug("Throttle: " + key + " added to MultiThrottle consist.");
        }
        return true;
    }

    public boolean removeThrottleController(String key, String action) {

        if (throttles == null) {
            log.debug("No MultiThrottle to remove " + key + " from.");
            return false;
        }
        if (key.equals("*")) {
            for (Enumeration<String> e = throttles.keys(); e.hasMoreElements();) {
                removeThrottleController(e.nextElement(), action);
                //  Runs each loco through this method individually
            }
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

    public void passActionsToControllers(String key, String action) {
        if (throttles == null) {
            log.debug("No throttles in MultiThrottle to receive action.");
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("MultiThrottle key: " + key + ", action: " + action);
        }

        if (key.equals("*")) {
            for (Enumeration<String> e = throttles.keys(); e.hasMoreElements();) {
                passActionsToControllers(e.nextElement(), action);
                //  Runs each loco through this method individually
            }
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
        for (Enumeration<String> e = throttles.keys(); e.hasMoreElements();) {
            removeThrottleController(e.nextElement(), "r");
        }
    }

    public void eStop() {
        if (throttles == null) {
            return;
        }
        for (Enumeration<String> e = throttles.keys(); e.hasMoreElements();) {
            passActionsToControllers(e.nextElement(), "X");
        }
    }

    private static Logger log = LoggerFactory.getLogger(MultiThrottle.class.getName());
}
