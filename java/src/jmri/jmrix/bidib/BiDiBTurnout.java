package jmri.jmrix.bidib;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import jmri.NamedBean;
import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.bidib.jbidibc.messages.enums.LcOutputType;
import org.bidib.jbidibc.simulation.comm.SimulationBidib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BiDiB implementation of the Turnout interface.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Eckart Meyer Copyright (C) 2019-2023
 */
public class BiDiBTurnout extends AbstractTurnout implements BiDiBNamedBeanInterface {

    // data members
    BiDiBAddress addr;
    private final char typeLetter;

    static String[] modeNames = null;
    static int[] modeValues = null;
    
    private BiDiBTrafficController tc = null;
    //MessageListener messageListener = null;
    private BiDiBOutputMessageHandler messageHandler = null;

    /**
     * Create a turnout. 
     * 
     * @param systemName to be created
     * @param mgr Turnout Manager, we get the memo object and the type letter (T) from the manager
     */
//    @SuppressWarnings("OverridableMethodCallInConstructor")
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",justification = "Write safe by design")
    public BiDiBTurnout(String systemName, BiDiBTurnoutManager mgr) {
        super(systemName);
        tc = mgr.getMemo().getBiDiBTrafficController();
        addr = new BiDiBAddress(systemName, mgr.typeLetter(), mgr.getMemo());
        log.info("New TURNOUT created: {} -> {}", systemName, addr);
        typeLetter = mgr.typeLetter();
        
        // new mode list
        if (_validFeedbackNames.length != _validFeedbackModes.length) {
            log.error("int and string feedback arrays different length");
        }
        modeNames = new String[_validFeedbackNames.length + 1];
        modeValues = new int[_validFeedbackNames.length + 1];
        for (int i = 0; i < _validFeedbackNames.length; i++) {
            modeNames[i] = _validFeedbackNames[i];
            modeValues[i] = _validFeedbackModes[i];
        }
        modeNames[_validFeedbackNames.length] = "MONITORING";
        modeValues[_validFeedbackNames.length] = MONITORING;
        _validFeedbackTypes |= MONITORING;
        _validFeedbackNames = modeNames;
        _validFeedbackModes = modeValues;
        
        _activeFeedbackType = MONITORING; //default for new Turnouts
        
        createTurnoutListener();
        
        messageHandler.sendQueryConfig();
    }
    
    @Override
    public BiDiBAddress getAddr() {
        return addr;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void finishLoad() {
        sendQuery();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void nodeNew() {
        //create a new BiDiBAddress
        addr = new BiDiBAddress(getSystemName(), typeLetter, tc.getSystemConnectionMemo());
        if (addr.isValid()) {
            log.info("new turnout address created: {} -> {}", getSystemName(), addr);
            messageHandler.sendQueryConfig();
            messageHandler.waitQueryConfig();
            log.debug("current known state is {}, commanded state is {}", getKnownState(), getCommandedState());
//            if (getKnownState() == NamedBean.UNKNOWN  ||  getKnownState() == NamedBean.INCONSISTENT) {
//                log.debug("state is unknown, so query from node");
//                sendQuery();
//            }
//            else {
//                log.debug("state is known, so set node");
//                forwardCommandChangeToLayout(getKnownState());
//            }
            forwardCommandChangeToLayout(getCommandedState());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void nodeLost() {
        newKnownState(NamedBean.UNKNOWN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canInvert() {
        // Turnouts do support inversion
        //log.trace("canInvert");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void forwardCommandChangeToLayout(int s) {
        if ((tc.getBidib() instanceof SimulationBidib)  &&  addr.getAddrString().equals("Xfb7600C602:a0")) { // **** TEST hack only on for simulation
            tc.TEST((s & Turnout.THROWN) != 0);/////DEBUG
        }
        // Handle a request to change state
        // sort out states
        log.trace("forwardCommandChangeToLayout: {}, addr: {}", s, addr);
        if ((s & Turnout.UNKNOWN) != 0) {
            // what to do here?
        }
        else {
            if ((s & Turnout.CLOSED) != 0) {
                // first look for the double case, which we can't handle
                if ((s & Turnout.THROWN) != 0) {
                    // this is the disaster case!
                    log.error("Cannot command both CLOSED and THROWN {}", s);
                } else {
                    // send a CLOSED command
                    sendMessage(true ^ getInverted());
                }
            } else {
                // send a THROWN command
                sendMessage(false ^ getInverted());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestUpdateFromLayout() {
        log.trace("requestUpdateFromLayout");
        if (_activeFeedbackType == MONITORING) {
            sendQuery();
        }
        super.requestUpdateFromLayout(); //query sensors for ONESENSOR and TWOSENSOR turnouts
    }

    @Override
    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout) {
        // unsupported in BiDiB, but must be implemented
    }
    
    /**
     * Request the state of the accessory from the layout.
     * The listener gets the answer.
     */
    public void sendQuery() {
        messageHandler.sendQuery();
    }

    /**
     * Send a thrown/closed message to BiDiB
     * 
     * @param closed true of the turnout should be switched to CLOSED, false to switch to THROWN
     */
    protected void sendMessage(boolean closed) {
        // TODO: check FEATURE_GEN_SWITCH_ACK
        int state = closed ? 0 : 1;
        if (addr.isPortAddr()) {
            switch (messageHandler.getLcType()) {
                case LIGHTPORT:
                    state = closed ? 2 : 3; //use Dim function - we can't configure this so far...
                    break;
                case SERVOPORT:
                case ANALOGPORT:
                case BACKLIGHTPORT:
                    state = closed ? 0 : 255;
                    break;
                case MOTORPORT:
                    state = closed ? 0 : 126;
                    break;
                case INPUTPORT:
                    log.warn("output to INPUT port is not possible, addr: {}", addr);
                    return;
                default:
                    // just drop through
                    break;
            }
        }
        if (getFeedbackMode() == MONITORING) {
            newKnownState(INCONSISTENT);
        }
        messageHandler.sendOutput(state);
    }
        
    
    private void createTurnoutListener() {
        //messageHandler = new BiDiBOutputMessageHandler("TURNOUT", addr, tc) {
        messageHandler = new BiDiBOutputMessageHandler(this, "TURNOUT", tc) {
            @Override
            public void newOutputState(int state) {
                
                int newState = (state == 0) ? CLOSED : THROWN;
                
                if (addr.isPortAddr()  &&  getLcType() == LcOutputType.LIGHTPORT) {
                    if (state == 2) {
                        newState = CLOSED; //BIDIB_PORT_DIMM_OFF - does not make much sense though...
                    }
                }
                if (getInverted()) {
                    newState = (newState == THROWN) ? CLOSED : THROWN;
                }
                log.debug("TURNOUT new state: {} addr: {}", newState, addr);
                newKnownState(newState);
            }
            @Override
            public void outputWait(int time) {
                log.debug("TURNOUT wait: {} addr: {}", time, addr);
                //newKnownState(getCommandedState());
            }
            @Override
            public void errorState(int err) {
                log.warn("TURNOUT error: {} addr: {}", err, addr);
                newKnownState(INCONSISTENT);
            }
        };
        tc.addMessageListener(messageHandler);        
    }
    
    /**
     * {@inheritDoc}
     * 
     * Remove the Message Listener for this turnout
     */
    @Override
    public void dispose() {
        if (messageHandler != null) {
            tc.removeMessageListener(messageHandler);        
            messageHandler = null;
        }
        super.dispose();
    }


    private final static Logger log = LoggerFactory.getLogger(BiDiBTurnout.class);

}
