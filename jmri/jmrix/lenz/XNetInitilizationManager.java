// XNetInitilizationManager.java

package jmri.jmrix.lenz;

/**
 * This class performs Command Station dependant initilization for 
 * XPressNet.  
 * It adds the appropriate Managers via the Initilization Manager
 * based on the Command Station Type.
 *
 * @author			Paul Bender  Copyright (C) 2003
 * @version			$Revision: 1.3 $
 */
public class XNetInitilizationManager implements XNetListener {

    private boolean done = false;

    public XNetInitilizationManager() {
      // Register as a Listener
	 XNetTrafficController.instance().addXNetListener(~0,this);
      //Send Information request to LI100/LI100
      /* First, we need to send a request for the Command Station
         hardware and software version */
	 XNetMessage msg=XNetTrafficController.instance()
                                             .getCommandStation()
                                             .getCSVersionRequestMessage();
        // The constructor MUST wait until we get the 
        // initilization to exit.

        //Then Send the version request to the controller
        XNetTrafficController.instance().sendXNetMessage(msg,this);

        // We'll check for a fixed time period (30 seconds) to see if 
        // we've gotten a response to our message. This way the program 
        // will start even if the layout is not powered.
        for(int i=0;i<30;i++) {
        // The done variable forces us to quit when we get a response to 
        // our XPressnet Message.  
          if(done) break;
           if (log.isDebugEnabled()) log.debug("start wait");
           try {
                  synchronized(this) {
                  wait(1000);
                  }
               } catch (java.lang.InterruptedException ei) {}
           if (log.isDebugEnabled()) log.debug("end wait");
        }

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
            } else if(CSType==0x01) {
	      if (log.isDebugEnabled()) log.debug("Command Station is LH200");
            } else if(CSType==0x00) {
	      if (log.isDebugEnabled()) log.debug("Command Station is LZ100/LZV100");
              jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager());
              jmri.InstanceManager.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager());
              jmri.InstanceManager.setProgrammerManager(new jmri.jmrix.lenz.XNetProgrammerManager(jmri.jmrix.lenz.XNetProgrammer.instance()));
            } else {
              /* If we still don't  know what we have, load everything */
	      if (log.isDebugEnabled()) log.debug("Command Station is Unknown type");
              jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager());
              jmri.InstanceManager.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager());
              jmri.InstanceManager.setProgrammerManager(new jmri.jmrix.lenz.XNetProgrammerManager(jmri.jmrix.lenz.XNetProgrammer.instance()));
            }
        }
        // After the Constructor runs, we can dispose of this class
        dispose();
    }

    // listen for the responses from the LI100/LI101
    public void message(XNetMessage l) {
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
              }
              done=true;
              //this.notify();
       }
    }
   
    public void dispose() {
       XNetTrafficController.instance().removeXNetListener(~0,this);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetInitilizationManager.class.getName());

}
