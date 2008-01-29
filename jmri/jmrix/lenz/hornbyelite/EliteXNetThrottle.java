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
 * @version    $Revision: 1.1 $
 */

public class EliteXNetThrottle extends jmri.jmrix.lenz.XNetThrottle
{
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
    
    // register for notification
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EliteXNetThrottle.class.getName());
}

