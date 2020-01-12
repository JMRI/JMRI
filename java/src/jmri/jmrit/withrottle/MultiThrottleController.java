package jmri.jmrit.withrottle;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jmri.*;
import jmri.jmrit.roster.RosterEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brett Hoffman Copyright (C) 2011
 */
public class MultiThrottleController extends ThrottleController {
    
    protected boolean isStealAddress;

    public MultiThrottleController(char id, String key, ThrottleControllerListener tcl, ControllerInterface ci) {
        super(id, tcl, ci);
        log.debug("New MT controller");
        locoKey = key;
        isStealAddress = false;
    }

    /**
     * Builds a header to send to the wi-fi device for use in a message.
     * Includes a separator - {@literal <;>}
     *
     * @param chr the character indicating what action is performed
     * @return a pre-assembled header for this DccThrottle
     */
    public String buildPacketWithChar(char chr) {
        return ("M" + whichThrottle + chr + locoKey + "<;>");
    }


    /*
     * Send a message to the wi-fi device that a bound property of a DccThrottle
     * has changed.  Currently only handles function state.
     * Current Format:  Header + F(0 or 1) + function number
     *
     * Event may be from regular throttle or consist throttle, but is handled the same.
     *
     * Bound params: SpeedSteps, IsForward, SpeedSetting, F##, F##Momentary
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String eventName = event.getPropertyName();
        log.debug("property change: {}",eventName);
        if (eventName.startsWith("F")) {
            if (eventName.contains("Momentary")) {
                return;
            }
            StringBuilder message = new StringBuilder(buildPacketWithChar('A'));

            try {
                if ((Boolean) event.getNewValue()) {
                    message.append("F1");
                } else {
                    message.append("F0");
                }
                message.append(eventName.substring(1));
            } catch (ClassCastException cce) {
                log.debug("Invalid event value. {}", cce.getMessage());
            } catch (IndexOutOfBoundsException oob) {
                log.debug("Invalid event name. {}", oob.getMessage());
            }

            for (ControllerInterface listener : controllerListeners) {
                listener.sendPacketToDevice(message.toString());
            }
        }
        if (eventName.matches(Throttle.SPEEDSTEPS)) {
            StringBuilder message = new StringBuilder(buildPacketWithChar('A'));
            message.append("s");
            message.append(encodeSpeedStepMode((SpeedStepMode)event.getNewValue()));
            for (ControllerInterface listener : controllerListeners) {
                listener.sendPacketToDevice(message.toString());
            }
        }
        if (eventName.matches(Throttle.ISFORWARD)) {
            StringBuilder message = new StringBuilder(buildPacketWithChar('A'));
            message.append("R");
            message.append((Boolean) event.getNewValue() ? "1" : "0");
            for (ControllerInterface listener : controllerListeners) {
               listener.sendPacketToDevice(message.toString());
            }
        }
        if (eventName.matches(Throttle.SPEEDSETTING)) {
            float currentSpeed = ((Float) event.getNewValue()).floatValue();
            log.debug("Speed Setting: {} head of queue {}",currentSpeed, lastSentSpeed.peek());
            if(lastSentSpeed.isEmpty()) { 
               StringBuilder message = new StringBuilder(buildPacketWithChar('A'));
               message.append("V");
               message.append(Math.round(currentSpeed / speedMultiplier));
               for (ControllerInterface listener : controllerListeners) {
                   listener.sendPacketToDevice(message.toString());
               }
            } else {
               if( Math.abs(lastSentSpeed.peek().floatValue()-currentSpeed)<0.0005 ) {
                  Float f = lastSentSpeed.poll(); // remove the value from the list.
                  log.debug("removed value {} from queue",f);
               }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendFunctionLabels(RosterEntry re) {

        if (re != null) {
            StringBuilder functionString = new StringBuilder(buildPacketWithChar('L'));

            int i;
            for (i = 0; i < 29; i++) {
                functionString.append("]\\[");
                if ((re.getFunctionLabel(i) != null)) {
                    functionString.append(re.getFunctionLabel(i));
                }
            }
            for (ControllerInterface listener : controllerListeners) {
                listener.sendPacketToDevice(functionString.toString());
            }
        }
    }

    /**
     * This replaces the previous method of sending a string of function states,
     * and now sends them individually, the same as a property change would.
     *
     * @param t the throttle to send the staes of
     */
    @Override
    public void sendAllFunctionStates(DccThrottle t) {
        log.debug("Sending state of all functions");

        try {
            for (int cnt = 0; cnt < 29; cnt++) {
                Method getF = t.getClass().getMethod("getF" + cnt, (Class[]) null);

                StringBuilder message = new StringBuilder(buildPacketWithChar('A'));
                if ((Boolean) getF.invoke(t, (Object[]) null)) {
                    message.append("F1");
                } else {
                    message.append("F0");
                }
                message.append(cnt);
                for (ControllerInterface listener : controllerListeners) {
                    listener.sendPacketToDevice(message.toString());
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ea) {
            log.warn(ea.getLocalizedMessage(), ea);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized protected void sendCurrentSpeed(DccThrottle t) {
        float currentSpeed = t.getSpeedSetting();
        StringBuilder message = new StringBuilder(buildPacketWithChar('A'));
        message.append("V");
        message.append(Math.round(currentSpeed / speedMultiplier));
        for (ControllerInterface listener : controllerListeners) {
            listener.sendPacketToDevice(message.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendCurrentDirection(DccThrottle t) {
        StringBuilder message = new StringBuilder(buildPacketWithChar('A'));
        message.append("R");
        message.append(t.getIsForward() ? "1" : "0");
        for (ControllerInterface listener : controllerListeners) {
            listener.sendPacketToDevice(message.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendSpeedStepMode(DccThrottle t) {
        StringBuilder message = new StringBuilder(buildPacketWithChar('A'));
        message.append("s");
        message.append(encodeSpeedStepMode(throttle.getSpeedStepMode()));
        for (ControllerInterface listener : controllerListeners) {
            listener.sendPacketToDevice(message.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendAllMomentaryStates(DccThrottle t) {
        log.debug("Sending momentary state of all functions");

        try {
            for (int cnt = 0; cnt < 29; cnt++) {
                Method getF = t.getClass().getMethod("getF" + cnt + "Momentary", (Class[]) null);

                StringBuilder message = new StringBuilder(buildPacketWithChar('A'));
                if ((Boolean) getF.invoke(t, (Object[]) null)) {
                    message.append("m1");
                } else {
                    message.append("m0");
                }
                message.append(cnt);
                for (ControllerInterface listener : controllerListeners) {
                    listener.sendPacketToDevice(message.toString());
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ea) {
            log.warn(ea.getLocalizedMessage(), ea);
        }
    }

    /**
     * {@inheritDoc} A + indicates the address was acquired, - indicates
     * released
     */
    @Override
    public void sendAddress() {
        for (ControllerInterface listener : controllerListeners) {
            if (isAddressSet) {
                listener.sendPacketToDevice(buildPacketWithChar('+'));
            } else {
                listener.sendPacketToDevice(buildPacketWithChar('-'));
            }
        }
    }

    /**
     * Send a message to a device that steal is needed. This message can be sent 
     * back to JMRI verbatim to complete a steal.
     */
    public void sendStealAddress() {
        StringBuilder message = new StringBuilder(buildPacketWithChar('S'));
        message.append(locoKey);
        for (ControllerInterface listener : controllerListeners) {
            listener.sendPacketToDevice(message.toString());
        }
    }
    
    /**
     * {@inheritDoc}
     * @deprecated since 4.15.7; use #notifyDecisionRequired
     */
    @Override
    @Deprecated
    public void notifyStealThrottleRequired(jmri.LocoAddress address) {
        notifyDecisionRequired(address, DecisionType.STEAL);
    }

    /**
     * A decision is required for Throttle creation to continue.
     * <p>
     * Steal / Cancel, Share / Cancel, or Steal / Share Cancel
     * <p>
     * Callback of a request for an address that is in use.
     * Will initiate a steal only if this MTC is flagged to do so.
     * Otherwise, it will remove the request for the address.
     *
     * {@inheritDoc}
     */
    @Override
    public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
        if ( question == DecisionType.STEAL ){
            if (isStealAddress) {
                //  Address is now staged in ThrottleManager and has been requested as a steal
                //  Complete the process
                InstanceManager.throttleManagerInstance().responseThrottleDecision(address, this, DecisionType.STEAL);
                isStealAddress = false;
            } else {
                //  Address has not been requested as a steal yet
                sendStealAddress();
                notifyFailedThrottleRequest(address, "Steal Required");
            }
        }
        else if ( question == DecisionType.STEAL_OR_SHARE ){ // using the same process as a Steal
            if (isStealAddress) {
                //  Address is now staged in ThrottleManager and has been requested as a steal
                //  Complete the process
                InstanceManager.throttleManagerInstance().responseThrottleDecision(address, this, DecisionType.STEAL);
                isStealAddress = false;
            } else {
                //  Address has not been requested as a steal yet
                sendStealAddress();
                notifyFailedThrottleRequest(address, "Steal Required");
            }
        }
        else { // if encountered likely to be DecisionType.SHARE
            log.info("{} question not supported by WiThrottle.",question );
        }
        
        
    }

    // Encode a SpeedStepMode to a string.
    private static String encodeSpeedStepMode(SpeedStepMode mode) {
        switch(mode) {
            // NOTE: old speed step modes use the original numeric values
            // from when speed step modes were in DccThrottle. New speed step
            // modes use the mode name.
            case NMRA_DCC_128:
                return "1";
            case NMRA_DCC_28:
                 return "2";
            case NMRA_DCC_27:
                return "4";
            case NMRA_DCC_14:
                return "8";
            case MOTOROLA_28:
                return "16";
            default:
                return mode.name;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(MultiThrottleController.class);

}
