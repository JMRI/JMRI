// XNetInitilizationManager.java

package jmri.jmrix.lenz;

/**
 * This class performs Command Station dependant initilization for 
 * XPressNet.  
 * It adds the appropriate Managers via the Initilization Manager
 * based on the Command Station Type.
 *
 * @author			Paul Bender  Copyright (C) 2003
 * @version			$Revision: 2.3 $
 */
public class XNetInitilizationManager {

    private Thread initThread = null;
    private final static int InitTimeout = 30000;

    public XNetInitilizationManager() {
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

    private void init() {
	if(log.isDebugEnabled()) log.debug("Init called");
        float CSSoftwareVersion = XNetTrafficController.instance()
                                       .getCommandStation()
                                       .getCommandStationSoftwareVersion();
        int CSType = XNetTrafficController.instance()
                                          .getCommandStation()
                                          .getCommandStationType();

        if(CSSoftwareVersion<0)
        {
           log.warn("Command Station disconnected, or powered down assuming LZ100/LZV100 V3.x");
           jmri.InstanceManager.setPowerManager(new jmri.jmrix.lenz.XNetPowerManager());
           jmri.InstanceManager.setThrottleManager(new jmri.jmrix.lenz.XNetThrottleManager());
           jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager());
           jmri.InstanceManager.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager());
           jmri.InstanceManager.setProgrammerManager(new jmri.jmrix.lenz.XNetProgrammerManager(jmri.jmrix.lenz.XNetProgrammer.instance()));
           /* the "raw" Command Station only works on systems that support   
                 Ops Mode Programming */
           /* jmri.InstanceManager.setCommandStation(XNetTrafficController.instance()
                                             .getCommandStation());*/
	   /* the consist manager has to be set up AFTER the programmer, to 
	   prevent the default consist manager from being loaded on top of it */
	   jmri.InstanceManager.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager());
        } else if(CSSoftwareVersion<3.0) {
           log.error("Command Station does not support XPressNet Version 3 Command Set");
        } else {
            /* First, we load things that should work on all systems */
            jmri.InstanceManager.setPowerManager(new jmri.jmrix.lenz.XNetPowerManager());
            jmri.InstanceManager.setThrottleManager(new jmri.jmrix.lenz.XNetThrottleManager());
            
            /* Next we check the command station type, and add the 
            apropriate managers */
            if(CSType==0x02) {
	      if (log.isDebugEnabled()) log.debug("Command Station is Commpact/Commander/Other");
              jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager());
	      /* the consist manager has to be set up AFTER the programmer, to 
	      prevent the default consist manager from being loaded on top of it */
	      jmri.InstanceManager.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager());
            } else if(CSType==0x01) {
	      if (log.isDebugEnabled()) log.debug("Command Station is LH200");
            } else if(CSType==0x00) {
	      if (log.isDebugEnabled()) log.debug("Command Station is LZ100/LZV100");
              jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager());
              jmri.InstanceManager.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager());
              jmri.InstanceManager.setProgrammerManager(new jmri.jmrix.lenz.XNetProgrammerManager(jmri.jmrix.lenz.XNetProgrammer.instance()));
              /* the "raw" Command Station only works on systems that support   
                 Ops Mode Programming */
              jmri.InstanceManager.setCommandStation(XNetTrafficController.instance()
                                             .getCommandStation());
	      /* the consist manager has to be set up AFTER the programmer, to 
	      prevent the default consist manager from being loaded on top of it */
	      jmri.InstanceManager.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager());
            } else {
              /* If we still don't  know what we have, load everything */
	      if (log.isDebugEnabled()) log.debug("Command Station is Unknown type");
              jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager());
              jmri.InstanceManager.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager());
              jmri.InstanceManager.setProgrammerManager(new jmri.jmrix.lenz.XNetProgrammerManager(jmri.jmrix.lenz.XNetProgrammer.instance()));
              /* the "raw" Command Station only works on systems that support   
                 Ops Mode Programming */
              jmri.InstanceManager.setCommandStation(XNetTrafficController.instance()
                                             .getCommandStation());
	      /* the consist manager has to be set up AFTER the programmer, to 
	      prevent the default consist manager from being loaded on top of it */
	      jmri.InstanceManager.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager());
            }
        }
	if(log.isDebugEnabled()) log.debug("XPressNet Initilization Complete");
    }

    /* Interal class to configure the XNet implementation */
    class XNetInitilizer implements Runnable, XNetListener {

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
	    XNetTrafficController.instance().addXNetListener(~0,this);

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
          XNetTrafficController.instance().removeXNetListener(~0,this);
       }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetInitilizationManager.class.getName());

}
