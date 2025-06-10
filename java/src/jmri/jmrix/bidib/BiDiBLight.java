package jmri.jmrix.bidib;

import jmri.implementation.AbstractVariableLight;
import jmri.util.MathUtil;

import org.bidib.jbidibc.messages.BidibLibrary; //new
import org.bidib.jbidibc.messages.LcConfigX;
import org.bidib.jbidibc.messages.enums.LcOutputType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Light Control Object for BiDiB.
 *
 * @author Paul Bender Copyright (C) 2008-2010
 * @author Eckart Meyer Copyright (C) 2019-2025
 */
public class BiDiBLight extends AbstractVariableLight implements BiDiBNamedBeanInterface {

    private BiDiBAddress addr;
    private final char typeLetter;
    private BiDiBTrafficController tc = null;
    protected BiDiBOutputMessageHandler messageHandler = null;
    //private LcConfigX portConfigx;
    private LcOutputType portType; //cached type from portConfigX or fixed in type based address

    /**
     * Create a Light object from system name.
     *
     * @param systemName System name of light to be created
     * @param mgr Light Manager, we get the memo object and the type letter (L) from the manager
     */
    public BiDiBLight(String systemName, BiDiBLightManager mgr) {
        super(systemName);
        tc = mgr.getMemo().getBiDiBTrafficController();
        log.debug("New Light: {}", systemName);
        addr = new BiDiBAddress(systemName, mgr.typeLetter(), mgr.getMemo());
        log.info("New LIGHT created: {} -> {}", systemName, addr);
        typeLetter = mgr.typeLetter();
        init();
    }
    
    private void init() {
        // portConfigx = new LcConfigX(addr.makeBidibPort(), new LinkedHashMap<>() );

        createLightListener();
        
        if (addr.isValid()  &&  addr.isPortAddr()) {
            if (addr.isPortTypeBasedModel()) {
                portType = addr.getPortType();
            }
        }

//        // DEBUG
//        portType = LcOutputType.LIGHTPORT;
//        portType = LcOutputType.SWITCHPORT;
//        portType = LcOutputType.BACKLIGHTPORT;

        messageHandler.sendQueryConfig();
    }
    
    @Override
    public BiDiBAddress getAddr() {
        return addr;
    }
    
    /**
     * Helper function that will be invoked after construction once the type has been
     * set. Used specifically for preventing double initialization when loading turnouts from XML.
     */
    @Override
    public void finishLoad() {
        messageHandler.sendQuery();
    }
    
    @Override
    public void nodeNew() {
        //create a new BiDiBAddress
        addr = new BiDiBAddress(getSystemName(), typeLetter, tc.getSystemConnectionMemo());
        if (addr.isValid()) {
            log.info("new light address created: {} -> {}", getSystemName(), addr);
            messageHandler.sendQueryConfig();
            messageHandler.waitQueryConfig();
            log.debug("current state is {}", getState());
            setState(getCommandedState());
        }
    }

    @Override
    public void nodeLost() {
        notifyStateChange(mState, UNKNOWN);
    }

    /**
     * Get connection memo from a light object
     * 
     * @return BiDiB connection memo instance
     */
    public BiDiBSystemConnectionMemo getMemo() {
        return tc.getSystemConnectionMemo();
    }
    
    /**
     * Get addres object from a light object
     * 
     * @return BiDiB address instance
     */
    public BiDiBAddress getAddress() {
        return addr;
    }

    /**
     * Dispose of the light object.
     * 
     * Remove the Message Listener for this light object
     */
    @Override
    public void dispose() {
        if (messageHandler != null) {
            tc.removeMessageListener(messageHandler);        
            messageHandler = null;
        }
        super.dispose();
    }

    /**
     * Check if this object can handle variable intensity.
     *
     * @return true for some LC output types, false for others
     */
    public boolean isIntensityVariable() {
        if (portType != null) {
            switch (portType) {
                case LIGHTPORT: //we misuse the intensity value for encoding the output state value
                case SERVOPORT:
                case BACKLIGHTPORT:
                case MOTORPORT: //not really supported so far
                case ANALOGPORT: //not specified!
                    return true;
                default:
                    // drop through and return false
                    break;
            }
        }
        return false;
    }

    /**
     * Can the Light change its intensity setting slowly?
     * <p>
     * If true, this Light supports a non-zero value of the transitionTime
     * property, which controls how long the Light will take to change from one
     * intensity level to another.
     * BiDiB LIGHTPORTs have internal dimming via port configuration.
     * <p>
     * Unbound property
     * @return true if isIntensityVariable() is true but for a LIGHTPORT return false.
     */
    @Override
    public boolean isTransitionAvailable() {
        return (portType != LcOutputType.LIGHTPORT  &&  isIntensityVariable());
//        return isIntensityVariable();
    }

    /**
     * Set the current state of this Light. This routine requests the hardware
     * to change.
     * 
     * @param newState new requested state - must be ON or OFF
     */
    @Override
    synchronized public void setState(int newState) {
        log.trace("BiDiBLight setState: new: {}, old: {}", newState, mState);
        if (newState != ON && newState != OFF) {
            throw new IllegalArgumentException("cannot set state value " + newState);
        }
//        super.setState(newState);
        setTargetIntensity(newState == ON ? mMaxIntensity : mMinIntensity);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setTargetIntensity(double intensity) {
        log.trace("BiDiBLight setTargetIntensity: new: {}", intensity);
//        super.setTargetIntensity(intensity);
        if (portType == LcOutputType.LIGHTPORT) {
            sendIntensity(intensity);
            // update value and tell listeners
            notifyTargetIntensityChange(intensity);

            // decide if this is a state change operation
            int state = (int) (intensity * 100);
            if (state == BidibLibrary.BIDIB_PORT_DIMM_OFF  ||  state == BidibLibrary.BIDIB_PORT_TURN_OFF) {
                notifyStateChange(mState, OFF);
            } else {
                notifyStateChange(mState, ON);
            }
        }
        else {
            if (intensity < 0.0 || intensity > 1.0) {
                throw new IllegalArgumentException("Target intensity value " + intensity + " not in legal range");
            }

            // limit
            if (intensity > mMaxIntensity) {
                intensity = mMaxIntensity;
            }
            if (intensity < mMinIntensity) {
                intensity = mMinIntensity;
            }

            // see if there's a transition in use
            if (getTransitionTime() > 0.0  &&  portType != LcOutputType.LIGHTPORT) {
                startTransition(intensity);
            } else {
                // No transition in use, move immediately

                // Set intensity and intermediate state
                sendIntensity(intensity);
                // update value and tell listeners
                notifyTargetIntensityChange(intensity);

                // decide if this is a state change operation
                if (intensity >= mMaxIntensity) {
                    notifyStateChange(mState, ON);
                } else if (intensity <= mMinIntensity) {
                    notifyStateChange(mState, OFF);
                } else {
                    notifyStateChange(mState, INTERMEDIATE);
                }
            }
        }
    }
    
    /**
     * Send request to traffic controller
     * 
     * @param portstat BiDiB portstat value (see protocol description for valid values)
     */
    protected void sendLcOutput(int portstat) {
        messageHandler.sendOutput(portstat);
    }
    
    /**
     * Send a Dim/Bright commands to the hardware to reach a specific intensity.
     * 
     * @param intensity new intensity
     */
    @Override
    protected void sendIntensity(double intensity) {
        log.trace("sendIntensity: {}", intensity);
        if (portType == LcOutputType.LIGHTPORT) {
            sendLcOutput( (int) (intensity * 100));
        }
        else if (isIntensityVariable()) {
            sendLcOutput( (int) MathUtil.pin(intensity * 255.0, 0.0, 255.0));
        }
        else {
            sendLcOutput(intensity > mMinIntensity ? BidibLibrary.BIDIB_PORT_TURN_ON : BidibLibrary.BIDIB_PORT_TURN_OFF);
        }
    }

    /**
     * Transfer incoming change event to JMRI
     * 
     * @param portstat BiDiB portstat value (see protocol description for valid values)
     */
    public void receiveIntensity(int portstat) {
        log.trace("receiveIntensity: {}", portstat);
        if (portType == LcOutputType.LIGHTPORT) {
//            notifyTargetIntensityChange( (double)portstat / 100);
            double intensity = 0;
            switch(portstat) {
                case BidibLibrary.BIDIB_PORT_TURN_OFF:
                    intensity = 0;
                    break;
                case BidibLibrary.BIDIB_PORT_DIMM_OFF:
                    intensity = mMinIntensity;
                    break;
                case BidibLibrary.BIDIB_PORT_TURN_ON:
                    intensity = 1.0;
                    break;
                case BidibLibrary.BIDIB_PORT_DIMM_ON:
                    intensity = mMaxIntensity;
                    break;
                default:
                    intensity = 0;
                    break;
            }
            notifyTargetIntensityChange(intensity);
            if (portstat == BidibLibrary.BIDIB_PORT_DIMM_OFF  ||  portstat == BidibLibrary.BIDIB_PORT_TURN_OFF) {
                notifyStateChange(mState, OFF);
            } else {
                notifyStateChange(mState, ON);
            }
        }
        else if (isIntensityVariable()) {
            double intensity = MathUtil.pin( (double)portstat / 255, 0.0, 1.0);
            notifyTargetIntensityChange(intensity);
            notifyStateChange(mState, intensity <= mMinIntensity ? OFF : ON);
        }
        else {
            notifyTargetIntensityChange( portstat == BidibLibrary.BIDIB_PORT_TURN_OFF ? mMinIntensity : mMaxIntensity);
            notifyStateChange(mState, portstat == BidibLibrary.BIDIB_PORT_TURN_OFF ? OFF : ON);
        }
    }
    
    @Override
    protected void sendOnOffCommand(int newState) {
        // not used, but must be implemented
        log.trace("sendOnOffCommand: {}", newState);
    }

    @Override
    protected int getNumberOfSteps() {
        return 256; //TODO What is this used for?
    }


    private void createLightListener() {
        //messageHandler = new BiDiBOutputMessageHandler("LIGHT", addr, tc) {
        messageHandler = new BiDiBOutputMessageHandler(this, "LIGHT", tc) {
            @Override
            public void newOutputState(int state) {
                log.debug("LIGHT new state: {}", state);
                //newKnownState( (state == 0) ? CLOSED : THROWN);
                receiveIntensity(state);
            }
            @Override
            public void outputWait(int time) {
                log.debug("LIGHT wait: {}", time);
                if (time > 0) {
                    notifyStateChange(getState(), INCONSISTENT);
                }
            }
            @Override
            public void errorState(int err) {
                log.warn("LIGHT error: {} addr: {}", err, addr);
                notifyStateChange(getState(), INCONSISTENT);
            }
            @Override
            public void newLcConfigX(LcConfigX lcConfigX, LcOutputType _lcType) {
                //this.portConfigx = lcConfigX;
                portType = _lcType;
            }
        };
        tc.addMessageListener(messageHandler);        
    }

    private final static Logger log = LoggerFactory.getLogger(BiDiBLight.class);

}
