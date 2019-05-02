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
  *
 * Based on AbstractXNetInitializationManager
 */
abstract public class AbstractDCCppInitializationManager {

    protected Thread initThread = null;

    protected DCCppSystemConnectionMemo systemMemo = null;

    /**
     * Define timeout used during initialization
     */
    protected int getInitTimeout() {
        return 30000;
    }

    public AbstractDCCppInitializationManager(DCCppSystemConnectionMemo memo) {
        /* spawn a thread to request version information and wait for the 
           command station to respond */
        if (log.isDebugEnabled()) {
            log.debug("Starting DCC++ Initialization Process");
        }
        systemMemo = memo;
        /* the JMRI DCC++ code doesn't currently initialize based on version,
	 * so there is no need to start the initThread or the ensuing wait */
	/*initThread = new Thread(new DCCppInitializer(this));
        
        // Since we can't currently reconfigure the user interface after  
        // initilization, We need to wait for the initilization thread 
        // to finish before we can continue.  The wait  can be removed IF 
        // we revisit the GUI initilization process.
        synchronized (this) {
            if (log.isDebugEnabled()) {
                log.debug("start wait");
            }
            new jmri.util.WaitHandler(this);
            if (log.isDebugEnabled()) {
                log.debug("end wait");
            }
        }*/
        
        init();
    }
    
    abstract protected void init();
    
    /* Interal class to configure the DCC++ implementation */
    protected class DCCppInitializer implements Runnable, DCCppListener {
        
        private javax.swing.Timer initTimer; // Timer used to let he 
        // command station response time 
        // out, and configure the defaults.
        
        private Object parent = null;
        
        public DCCppInitializer(Object Parent) {
            
            parent = Parent;
            
            initTimer = setupInitTimer();
            
            // Register as an DCCppListener Listener
            systemMemo.getDCCppTrafficController().addDCCppListener(DCCppInterface.CS_INFO, this);
            
            //Send Information request to the Base Station
            //First, we need to send a request for the Command Station
            // hardware and software version 
            DCCppMessage msg = DCCppMessage.makeCSStatusMsg();
            //Then Send the version request to the controller
            systemMemo.getDCCppTrafficController().sendDCCppMessage(msg, this);
        }
        
        protected javax.swing.Timer setupInitTimer() {
            // Initialize and start initilization timeout timer.
            javax.swing.Timer retVal = new javax.swing.Timer(getInitTimeout(),
                                                             new java.awt.event.ActionListener() {
                                                                 @Override
                                                                 public void actionPerformed(
                                                                                             java.awt.event.ActionEvent e) {
                                                                     /* If the timer times out, notify any 
                                                                        waiting objects, and dispose of
                                                                        this thread */
                                                                     if (log.isDebugEnabled()) {
                                                                         log.debug("Timeout waiting for Command Station Response");
                                                                     }
                                                                     finish();
                                                                 }
                                                             });
            retVal.setInitialDelay(getInitTimeout());
            retVal.start();
            return retVal;
        }
        
        @Override
        public void run() {
        }
        
        private void finish() {
            initTimer.stop();
            // Notify the parent
            try {
                synchronized (parent) {
                    parent.notify();
                }
            } catch (Exception e) {
                log.error("Exception " + e + "while notifying initilization thread.");
            }
            if (log.isDebugEnabled()) {
                log.debug("Notification Sent");
            }
            // Then dispose of this object
            dispose();
        }
        
        // listen for the responses from the Base Station
        @Override
        public void message(DCCppReply l) {
            // Check to see if this is a response with the Command Station 
            // Version Info
            if (l.getElement(0) == DCCppConstants.STATUS_REPLY) {
                // This is the Command Station Software Version Response
                log.debug("Version Info Received: {}", l.toString());
                systemMemo.getDCCppTrafficController()
                    .getCommandStation()
                    .setCommandStationInfo(l);
                finish();
            }
        }
        
        // listen for the messages to the LI100/LI101
        @Override
        public void message(DCCppMessage l) {
        }
        
        // Handle a timeout notification
        @Override
        public void notifyTimeout(DCCppMessage msg) {
            if (log.isDebugEnabled()) {
                log.debug("Notified of timeout on message" + msg.toString());
            }
        }
        
        public void dispose() {
            systemMemo.getDCCppTrafficController().removeDCCppListener(DCCppInterface.CS_INFO, this);
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(AbstractDCCppInitializationManager.class);
    
}
