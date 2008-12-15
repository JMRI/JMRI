package jmri.jmrix.lenz.hornbyelite;

import jmri.jmrix.AbstractThrottle;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.lenz.XNetInterface;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.jmrix.lenz.XNetThrottle;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetConstants;


/**
 * An implementation of DccThrottle with code specific to a
 * XpressnetNet connection on the Hornby Elite
 * @author  Paul Bender (C) 2008
 * @version    $Revision: 1.2 $
 */

public class EliteXNetThrottle extends jmri.jmrix.lenz.XNetThrottle
{

    protected static final int statTimeoutValue = 5000;//Interval to check the
                                                        //status of the throttle

    /**
     * Constructor
     */
    public EliteXNetThrottle()
    {
        super();
        XNetTrafficController.instance().addXNetListener(XNetInterface.COMMINFO |
                                                         XNetInterface.CS_INFO |
                                                         XNetInterface.THROTTLE, this);
        if (log.isDebugEnabled()) { log.debug("Elite XNetThrottle constructor"); }
    }
    
    /**
     * Constructor
     */
    public EliteXNetThrottle(LocoAddress address)
    {
        super();
        this.setDccAddress(((DccLocoAddress)address).getNumber());
        this.speedIncrement=XNetConstants.SPEED_STEP_128_INCREMENT;
        this.speedStepMode=DccThrottle.SpeedStepMode128;
        //       this.isForward=true;
        setIsAvailable(false);
        
        f0Momentary = f1Momentary = f2Momentary = f3Momentary = f4Momentary =   
            f5Momentary = f6Momentary = f7Momentary = f8Momentary = f9Momentary =
            f10Momentary = f11Momentary = f12Momentary = false;
        
        XNetTrafficController.instance().addXNetListener(XNetInterface.COMMINFO |
                                                         XNetInterface.CS_INFO |
                                                         XNetInterface.THROTTLE, this);
        sendStatusInformationRequest();
        if (log.isDebugEnabled()) { log.debug("Elite XNetThrottle constructor called for address " + address ); }
        // because the elite is not sending out a broadcast message when 
        // another throttle takes over, we need to send a status request 
        // periodically to find out if we are still in control.
        startStatusTimer();
    }
    
    /**
     * Send the XpressNet message to set the Momentary state of locomotive
     * functions F0, F1, F2, F3, F4
     */
    protected void sendMomentaryFunctionGroup1()
    {
          if(log.isDebugEnabled())
		log.debug("Momentary function request not supported by Elite.");
          return;
    }
    
    /**
     * Send the XpressNet message to set the momentary state of
     * functions F5, F6, F7, F8
     */
    protected void sendMomentaryFunctionGroup2()
    {
          if(log.isDebugEnabled())
		log.debug("Momentary function request not supported by Elite.");
          return;
    }
 
    /**
     * Send the XpressNet message to set the momentary state of
     * functions F9, F10, F11, F12
     */
    protected void sendMomentaryFunctionGroup3()
    {
          if(log.isDebugEnabled())
		log.debug("Momentary function request not supported by Elite.");
          return;
    }
    
    // sendFunctionStatusInformation sends a request to get the status
    // of functions from the command station
    synchronized protected void sendFunctionStatusInformationRequest()
    {
          if(log.isDebugEnabled())
		log.debug("Momentary function request not supported by Elite.");
          return;
    }
 

   // Status Information processing routines
   // Used for return values from Status requests.

   // Get SpeedStep and availability information
   protected void parseSpeedandAvailability(int b1)
   {
       /* the first data bite indicates the speed step mode, and
          if the locomotive is being controlled by another throttle */

          if((b1 & 0x08)==0x08 && this.isAvailable)
          {
              log.info("Loco " +getDccAddress() + " In use by another device");               setIsAvailable(false);
          } else if ((b1&0x08)==0x00 && !this.isAvailable) {
              if(log.isDebugEnabled()) { log.debug("Loco Is Available"); }
              setIsAvailable(true);
          }                                              
          // The Hornby Elite ALWAYS sends 14 speed steps in its responses,
          // so ignore the Speed Step portion of this byte (i.e. use the 
          // speed step mode set by the user in the throttle front end).
    }

    /*
     * Set up the status timer, and start it.
     */
    protected void startStatusTimer() {
        if(statTimer==null) {
            statTimer = new javax.swing.Timer(statTimeoutValue,new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        /* If the timer times out, just send a status
                           request message */
                        sendStatusInformationRequest();
                    }
                });
        }
        statTimer.stop();
        statTimer.setInitialDelay(statTimeoutValue);
        statTimer.setRepeats(true);
        statTimer.start();
    }

    /*
     * Stop the Status Timer
     * NOTE: This overrides the default behavior, because we don't 
     * want to stop the status timer with the Elite.
     */
    protected void stopStatusTimer() 
    {
	//no-op
    }

   /**
    * Dispose when finished with this object.  After this, further usage of
    * this Throttle object will result in a JmriException.
    *
    * This is quite problematic, because a using object doesn't know when
    * it's the last user.
    */
    public void dispose()
    {
       if(statTimer!=null) statTimer.stop();
       super.dispose();
    }

    // register for notification
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EliteXNetThrottle.class.getName());
}

