// AbstractXNetInitilizationManager.java

package jmri.jmrix.lenz;

/**
 * This class provides a base implementation for Command Station/interface 
 * dependent initilization for XPressNet.  
 * It adds the appropriate Managers via the Initilization Manager
 * based on the Command Station Type.
 *
 * @author			Paul Bender  Copyright (C) 2003
 * @version			$Revision: 2.1 $
 */
abstract public class AbstractXNetInitilizationManager {

    protected Thread initThread = null;
    protected final static int InitTimeout = 30000;

    public AbstractXNetInitilizationManager() {
	/* spawn a thread to request version information and wait for the 
	   command station to respond */
	if(log.isDebugEnabled()) log.debug("Starting XPressNet Initilization Process");
	initThread= new Thread(new XNetInitilizer(this));

	// Since we can't currently reconfigure the user interface after  
	// initilization, We need to wait for the initilization thread 
	// to finish before we can continue.  The wait  can be removed IF 
	// we revisit the GUI initilization process.
	synchronized(this) {
           if (log.isDebugEnabled()) log.debug("start wait");
              try {
	         this.wait();
               } catch (java.lang.InterruptedException ei) {}
               if (log.isDebugEnabled()) log.debug("end wait");
        }

	init();
    }

    abstract protected void init();

    /* Interal class to configure the XNet implementation */
    protected class XNetInitilizer implements Runnable, XNetListener {

       private javax.swing.Timer initTimer; // Timer used to let he 
					    // command station response time 
					    // out, and configure the defaults.
	
       private Object parent = null;

       public XNetInitilizer(Object Parent) {

	  parent = Parent;

	  // Initilize and start initilization timeout timer.
	  initTimer = new javax.swing.Timer(InitTimeout,
				new java.awt.event.ActionListener() {
				   public void actionPerformed(
					java.awt.event.ActionEvent e) {
					/* If the timer times out, notify any 
					   waiting objects, and dispose of
					   this thread */
					if(log.isDebugEnabled()) 
						log.debug("Timeout waiting for Command Station Response");
					finish();
				      }
	      			   });
          initTimer.setInitialDelay(InitTimeout);
          initTimer.start();

         // Register as an XPressNet Listener
	    XNetTrafficController.instance().addXNetListener(XNetInterface.CS_INFO,this);

         //Send Information request to LI100/LI100
         /* First, we need to send a request for the Command Station
            hardware and software version */
	    XNetMessage msg=XNetTrafficController.instance()
                                                 .getCommandStation()
                                                 .getCSVersionRequestMessage();

          //Then Send the version request to the controller
          XNetTrafficController.instance().sendXNetMessage(msg,this);	  

	}

       public void run() {
       }

       private void finish() {
		initTimer.stop();
	        // Notify the parent
	        try {
	     	  synchronized(parent) {
	  	     parent.notify();
		  }
  		} catch(Exception e) {
		   log.error("Exception " +e + "while notifying initilization thread.");
		}
	        if(log.isDebugEnabled()) 
		   log.debug("Notification Sent");
		// Then dispose of this object
		dispose();
       }

       // listen for the responses from the LI100/LI101
       public void message(XNetReply l) {
          // Check to see if this is a response with the Command Station 
          // Version Info
          if(l.getElement(0)==XNetConstants.CS_SERVICE_MODE_RESPONSE)
          {
              // This is the Command Station Software Version Response
              if(l.getElement(1)==XNetConstants.CS_SOFTWARE_VERSION)
	      {
   	        XNetTrafficController.instance()
                                     .getCommandStation()
                                     .setCommandStationSoftwareVersion(l);
  	        XNetTrafficController.instance()
                                     .getCommandStation()
                                     .setCommandStationType(l);
		finish();
	      }
          }
       }

       // listen for the messages to the LI100/LI101
       public void message(XNetMessage l) {
       }
   
       public void dispose() {
          XNetTrafficController.instance().removeXNetListener(XNetInterface.CS_INFO,this);
       }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractXNetInitilizationManager.class.getName());

}
