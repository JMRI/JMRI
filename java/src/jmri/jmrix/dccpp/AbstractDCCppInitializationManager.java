package jmri.jmrix.dccpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a base implementation for Command Station/interface
 * dependent initilization for DCC++. It adds the appropriate Managers via
 * the Initialization Manager based on the Command Station Type.
 *
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Mark Underwood Copyright (C) 2015
 * @author Harald Barth Copyright (C) 2019
 *
 * Based on AbstractXNetInitializationManager
 */
public abstract class AbstractDCCppInitializationManager {

    protected Thread initThread = null;

    protected DCCppSystemConnectionMemo systemMemo = null;

    // If there is no answer from an Arduino after 10 sec it's not there
    // Normal answer time is ~2 sec
    protected static int INITIALTIMEOUT = 10000;

    protected int getInitTimeout() {
        return INITIALTIMEOUT;  // this method is overriden for tests.
    }

    public AbstractDCCppInitializationManager(DCCppSystemConnectionMemo memo) {

        // spawn a thread to request version information 
        // and wait for the command station to respond
	log.debug("Starting DCC++ Initialization Process");
        systemMemo = memo;
	initThread = new Thread(new DCCppInitializer(this));

        // We need to wait for the initilization thread 
        // to finish before we can continue. 

        synchronized (this) {
            new jmri.util.WaitHandler(this);
        }
	// Continue with the non-abstract init
        init();
    }
    
    protected abstract void init();
    
    /* Interal class to configure the DCC++ implementation */
    protected class DCCppInitializer implements Runnable, DCCppListener {
        
        private javax.swing.Timer initTimer; // Timer used to let he 
        // command station response time 
        // out, and configure the defaults.

	// Flags to check if data has arrived
	private boolean gotVersion = false;
	private boolean gotMaxNumSlots= false;
        
        private Object parent = null;
        
        public DCCppInitializer(Object Parent) {
            
	    DCCppMessage msg;
            parent = Parent;
	    initTimer = setupInitTimer();
            
            // Register as an DCCppListener Listener
            systemMemo.getDCCppTrafficController().addDCCppListener(DCCppInterface.CS_INFO, this);
            
            //Send Information request to the Base Station

	    //If DCC++ just has started, it sends the
	    //the status message anyway,
	    //no matter what we send as the first request
            //(we could ask for current or whatever)
	    //
            // msg = new DCCppMessage(DCCppConstants.READ_TRACK_CURRENT, DCCppConstants.READ_TRACK_CURRENT_REGEX);
            // systemMemo.getDCCppTrafficController().sendDCCppMessage(msg, this);
	    //
            //Request hardware and software version 
            msg = DCCppMessage.makeCSStatusMsg();
            //Then Send the version request to the controller
            systemMemo.getDCCppTrafficController().sendDCCppMessage(msg, this);
            //Request number of available slots
            msg = DCCppMessage.makeCSMaxNumSlotsMsg();
            //Then Send the version request to the controller
            systemMemo.getDCCppTrafficController().sendDCCppMessage(msg, this);

	    log.debug("DCCppInitializer: MaxNumSlots and Status message sent");
        }
        
        protected javax.swing.Timer setupInitTimer() {
            // Initialize and start initilization timeout timer.
            javax.swing.Timer retVal = 
		new javax.swing.Timer(INITIALTIMEOUT,
				      (java.awt.event.ActionEvent e) -> {
					  /* If the timer times out, notify any
					     waiting objects, and dispose of
					     this thread */
					  log.debug("Timeout waiting for Command Station Response");
					  // This means that there was no answer on the MaxNumSlots question
					  systemMemo.getDCCppTrafficController().getCommandStation().setCommandStationMaxNumSlots(DCCppConstants.MAX_MAIN_REGISTERS);
					  finish();
				      }
		    );
            retVal.setInitialDelay(INITIALTIMEOUT);
            retVal.start();
            return retVal;
        }
        
        @Override
        public void run() {
            // we may not need a thread here...
        }
        
        private void finish() {
            initTimer.stop();
            // Notify the parent
            try {
                synchronized (parent) {
                    parent.notifyAll();
                }
            } catch (Exception e) {
                log.error("Exception {} while notifying initilization thread.",e);
            }
	    log.debug("Notification Sent");
            // Then dispose of this object
            dispose();
        }
        
        // listen for the responses from the Base Station
        @Override
        public void message(DCCppReply l) {
	    log.debug("Received DCCppReply: {}",l);
            // Check to see if this is a response with the number of slots
            if (l.getElement(0) == DCCppConstants.MAXNUMSLOTS_REPLY) {
                log.debug("MaxNumSlots Info Received: {}", l);
                systemMemo.getDCCppTrafficController()
                    .getCommandStation()
                    .setCommandStationMaxNumSlots(l);
		gotMaxNumSlots = true;
            }
            // Check to see if this is a response with the Command Station 
            // Version Info
            if (l.getElement(0) == DCCppConstants.STATUS_REPLY) {
                // This is the Command Station Software Version Response
                log.debug("Version Info Received: {}", l);
                systemMemo.getDCCppTrafficController()
                    .getCommandStation()
                    .setCommandStationInfo(l);
		gotVersion = true;
            }
            //If number of slots is not supported, by the DCC++ version
            //we'll have to wait for the timeout instead, sorry.
	    if (gotVersion && gotMaxNumSlots) {
		finish();
	    }
        }
        
        @Override
        public void message(DCCppMessage l) {
            // no need to process outgoing messages
        }
        
        // Handle a timeout notification
        @Override
        public void notifyTimeout(DCCppMessage msg) {
            if (log.isDebugEnabled()) {
                log.debug("Notified of timeout on message {}",msg);
            }
        }
        
        public void dispose() {
            systemMemo.getDCCppTrafficController().removeDCCppListener(DCCppInterface.CS_INFO, this);
        }
    }
    
    private static final Logger log = LoggerFactory.getLogger(AbstractDCCppInitializationManager.class);
    
}
