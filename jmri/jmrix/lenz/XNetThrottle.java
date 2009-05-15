package jmri.jmrix.lenz;

import jmri.jmrix.AbstractThrottle;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.DccLocoAddress;

/**
 * An implementation of DccThrottle with code specific to a
 * XpressnetNet connection.
 * @author  Paul Bender (C) 2002-2009
 * @author  Giorgio Terdina (C) 2007
 * @version    $Revision: 2.22 $
 */

public class XNetThrottle extends AbstractThrottle implements XNetListener
{
    protected boolean isAvailable;  // Flag  stating if the throttle is in 
                                  // use or not.
    protected javax.swing.Timer statTimer; // Timer used to periodically get 
                                         // current status of the throttle 
                                         // when throttle not available.
    protected static final int statTimeoutValue = 1000; // Interval to check the 
    // status of the throttle
    
    protected static final int THROTTLEIDLE=0;  // Idle Throttle
    protected static final int THROTTLESTATSENT=1;  // Sent Status request
    protected static final int THROTTLESPEEDSENT=2;  // Sent speed/dir command to locomotive
    protected static final int THROTTLEFUNCSENT=4;   // Sent a function command to locomotive.

    public int requestState=THROTTLEIDLE;
    
    protected int address;
    
    /**
     * Constructor
     */
    public XNetThrottle()
    {
        super();
        XNetTrafficController.instance().addXNetListener(XNetInterface.COMMINFO |
                                                         XNetInterface.CS_INFO |
                                                         XNetInterface.THROTTLE, this);
        if (log.isDebugEnabled()) { log.debug("XnetThrottle constructor"); }
    }
    
    /**
     * Constructor
     */
    public XNetThrottle(LocoAddress address)
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
        if (log.isDebugEnabled()) { log.debug("XnetThrottle constructor called for address " + address ); }
    }
    
    /**
     * Send the XpressNet message to set the state of locomotive
     * direction and functions F0, F1, F2, F3, F4
     */
    protected void sendFunctionGroup1()
    {
       if((requestState&(THROTTLESPEEDSENT|THROTTLESTATSENT|THROTTLEFUNCSENT))
		==THROTTLESTATSENT) 
       {
                log.warn("Outstanding request, Function Group 1 change ignored");
		return;
       }
       XNetMessage msg=new XNetMessage(6);
       msg.setElement(0,XNetConstants.LOCO_OPER_REQ);
       msg.setElement(1,XNetConstants.LOCO_SET_FUNC_GROUP1);
       msg.setElement(2,this.getDccAddressHigh());// set to the upper
						    // byte of the  DCC address
       msg.setElement(3,this.getDccAddressLow()); // set to the lower byte
						    //of the DCC address
       // Now, we need to figure out what to send in element 3
       int element4value=0;
       if(f0)
	{
	  element4value += 16;
	}
       if(f1)
	{
	  element4value += 1;
	}
       if(f2)
	{
	  element4value += 2;
	}
       if(f3)
	{
	  element4value += 4;
	}
       if(f4)
	{
	  element4value += 8;
	}
       msg.setElement(4,element4value);
       msg.setParity(); // Set the parity bit
       // now, we send the message to the command station
       XNetTrafficController.instance().sendXNetMessage(msg,this);
       if(!isAvailable) 
          requestState=THROTTLEFUNCSENT;
    }
    
    /**
     * Send the XpressNet message to set the state of
     * functions F5, F6, F7, F8
     */
    protected void sendFunctionGroup2()
    {
       if((requestState&(THROTTLESPEEDSENT|THROTTLESTATSENT|THROTTLEFUNCSENT))
		==THROTTLESTATSENT) 
       {
                log.warn("Outstanding request, Function Group 2 change ignored");
		return;
       }
       XNetMessage msg=new XNetMessage(6);
       msg.setElement(0,XNetConstants.LOCO_OPER_REQ);
       msg.setElement(1,XNetConstants.LOCO_SET_FUNC_GROUP2);
       msg.setElement(2,this.getDccAddressHigh());// set to the upper
						    // byte of the  DCC address
       msg.setElement(3,this.getDccAddressLow()); // set to the lower byte
						    //of the DCC address
       // Now, we need to figure out what to send in element 3
       int element4value=0;
       if(f5)
	{
	  element4value += 1;
	}
       if(f6)
	{
	  element4value += 2;
	}
       if(f7)
	{
	  element4value += 4;
	}
       if(f8)
	{
	  element4value += 8;
	}
       msg.setElement(4,element4value);
       msg.setParity(); // Set the parity bit
       // now, we send the message to the command station
       XNetTrafficController.instance().sendXNetMessage(msg,this);
       if(!isAvailable) 
          requestState=THROTTLEFUNCSENT;
    }
    
    /**
     * Send the XpressNet message to set the state of
     * functions F9, F10, F11, F12
     */
    protected void sendFunctionGroup3()
    {
       if((requestState&(THROTTLESPEEDSENT|THROTTLESTATSENT|THROTTLEFUNCSENT))
		==THROTTLESTATSENT) 
       {
                log.warn("Outstanding request, Function Group 3 change ignored");
		return;
       }
       XNetMessage msg=new XNetMessage(6);
       msg.setElement(0,XNetConstants.LOCO_OPER_REQ);
       msg.setElement(1,XNetConstants.LOCO_SET_FUNC_GROUP3);
       msg.setElement(2,this.getDccAddressHigh());// set to the upper
						    // byte of the  DCC address
       msg.setElement(3,this.getDccAddressLow()); // set to the lower byte
						    //of the DCC address
       // Now, we need to figure out what to send in element 3
       int element4value=0;
       if(f9)
	{
	  element4value += 1;
	}
       if(f10)
	{
	  element4value += 2;
	}
       if(f11)
	{
	  element4value += 4;
	}
       if(f12)
	{
	  element4value += 8;
	}
       msg.setElement(4,element4value);
       msg.setParity(); // Set the parity bit
       // now, we send the message to the command station
       XNetTrafficController.instance().sendXNetMessage(msg,this);
       if(!isAvailable) 
          requestState=THROTTLEFUNCSENT;
    }

    /**
     * Send the XpressNet message to set the state of
     * functions F13, F14, F15, F16, F17, F18, F19, F20
     */
    protected void sendFunctionGroup4()
    {
       if(XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationSoftwareVersion()>=3.6){
           log.info("Functions F13-F28 unavailable in CS software version " +
                    XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationSoftwareVersion());
           return;
        } 
       if((requestState&(THROTTLESPEEDSENT|THROTTLESTATSENT|THROTTLEFUNCSENT))
		==THROTTLESTATSENT) 
       {
                log.warn("Outstanding request, Function Group 4 change ignored");
		return;
       }
       XNetMessage msg=new XNetMessage(6);
       msg.setElement(0,XNetConstants.LOCO_OPER_REQ);
       msg.setElement(1,XNetConstants.LOCO_SET_FUNC_GROUP4);
       msg.setElement(2,this.getDccAddressHigh());// set to the upper
						    // byte of the  DCC address
       msg.setElement(3,this.getDccAddressLow()); // set to the lower byte
						    //of the DCC address
       // Now, we need to figure out what to send in element 3
       int element4value=0;
       if(f13)
	{
	  element4value += 1;
	}
       if(f14)
	{
	  element4value += 2;
	}
       if(f15)
	{
	  element4value += 4;
	}
       if(f16)
	{
	  element4value += 8;
	}
       if(f17)
	{
	  element4value += 16;
	}
       if(f18)
	{
	  element4value += 32;
	}
       if(f19)
	{
	  element4value += 64;
	}
       if(f20)
	{
	  element4value += 128;
	}
       msg.setElement(4,element4value);
       msg.setParity(); // Set the parity bit
       // now, we send the message to the command station
       XNetTrafficController.instance().sendXNetMessage(msg,this);
       if(!isAvailable) 
          requestState=THROTTLEFUNCSENT;
    }
    
    /**
     * Send the XpressNet message to set the state of
     * functions F21, F22, F23, F24, F25, F26, F27, F28
     */
    protected void sendFunctionGroup5()
    {
       if(XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationSoftwareVersion()>=3.6){
           log.info("Functions F13-F28 unavailable in CS software version " +
                    XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationSoftwareVersion());
           return;
        } 
       if((requestState&(THROTTLESPEEDSENT|THROTTLESTATSENT|THROTTLEFUNCSENT))
		==THROTTLESTATSENT) 
       {
                log.warn("Outstanding request, Function Group 5 change ignored");
		return;
       }
       XNetMessage msg=new XNetMessage(6);
       msg.setElement(0,XNetConstants.LOCO_OPER_REQ);
       msg.setElement(1,XNetConstants.LOCO_SET_FUNC_GROUP5);
       msg.setElement(2,this.getDccAddressHigh());// set to the upper
						    // byte of the  DCC address
       msg.setElement(3,this.getDccAddressLow()); // set to the lower byte
						    //of the DCC address
       // Now, we need to figure out what to send in element 3
       int element4value=0;
       if(f21)
	{
	  element4value += 1;
	}
       if(f22)
	{
	  element4value += 2;
	}
       if(f23)
	{
	  element4value += 4;
	}
       if(f24)
	{
	  element4value += 8;
	}
       if(f25)
	{
	  element4value += 16;
	}
       if(f26)
	{
	  element4value += 32;
	}
       if(f27)
	{
	  element4value += 64;
	}
       if(f28)
	{
	  element4value += 128;
	}
       msg.setElement(4,element4value);
       msg.setParity(); // Set the parity bit
       // now, we send the message to the command station
       XNetTrafficController.instance().sendXNetMessage(msg,this);
       if(!isAvailable) 
          requestState=THROTTLEFUNCSENT;
    }
    
    /**
     * Send the XpressNet message to set the Momentary state of locomotive
     * functions F0, F1, F2, F3, F4
     */
    protected void sendMomentaryFunctionGroup1()
    {
       if(XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationType()==0x10) {
          // if the command station is multimouse, ignore
          if(log.isDebugEnabled())
		log.debug("Command station does not support Momentary functions");
                return;
       }
       if((requestState&(THROTTLESPEEDSENT|THROTTLESTATSENT|THROTTLEFUNCSENT))
		==THROTTLESTATSENT) 
       {
                log.warn("Outstanding request, Function Group 1 momentary status change ignored");
		return;
       }  
       XNetMessage msg=new XNetMessage(6);
       msg.setElement(0,XNetConstants.LOCO_OPER_REQ);
       msg.setElement(1,XNetConstants.LOCO_SET_FUNC_Group1);
       msg.setElement(2,this.getDccAddressHigh());// set to the upper
						    // byte of the  DCC address
       msg.setElement(3,this.getDccAddressLow()); // set to the lower byte
						    //of the DCC address
       // Now, we need to figure out what to send in element 3
       int element4value=0;
       if(f0Momentary)
	{
	  element4value += 16;
	}
       if(f1Momentary)
	{
	  element4value += 1;
	}
       if(f2Momentary)
	{
	  element4value += 2;
	}
       if(f3Momentary)
	{
	  element4value += 4;
	}
       if(f4Momentary)
	{
	  element4value += 8;
	}
       msg.setElement(4,element4value);
       msg.setParity(); // Set the parity bit
       // now, we send the message to the command station
       XNetTrafficController.instance().sendXNetMessage(msg,this);
       if(!isAvailable) 
          requestState=THROTTLEFUNCSENT;
    }
    
    /**
     * Send the XpressNet message to set the momentary state of
     * functions F5, F6, F7, F8
     */
    protected void sendMomentaryFunctionGroup2()
    {
       if(XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationType()==0x10) {
          // if the command station is multimouse, ignore
          if(log.isDebugEnabled())
		log.debug("Command station does not support Momentary functions");
                return;
       }
       if((requestState&(THROTTLESPEEDSENT|THROTTLESTATSENT|THROTTLEFUNCSENT))
		==THROTTLESTATSENT) 
       {
                log.warn("Outstanding request, Function Group 2 momentary status change ignored");
		return;
            }
        XNetMessage msg=new XNetMessage(6);
        msg.setElement(0,XNetConstants.LOCO_OPER_REQ);
        msg.setElement(1,XNetConstants.LOCO_SET_FUNC_Group2);
       msg.setElement(2,this.getDccAddressHigh());// set to the upper
						    // byte of the  DCC address
       msg.setElement(3,this.getDccAddressLow()); // set to the lower byte
						    //of the DCC address
       // Now, we need to figure out what to send in element 3
       int element4value=0;
       if(f5Momentary)
	{
	  element4value += 1;
	}
       if(f6Momentary)
	{
	  element4value += 2;
	}
       if(f7Momentary)
	{
	  element4value += 4;
	}
       if(f8Momentary)
	{
	  element4value += 8;
	}
       msg.setElement(4,element4value);
       msg.setParity(); // Set the parity bit
       // now, we send the message to the command station
       XNetTrafficController.instance().sendXNetMessage(msg,this);
       if(!isAvailable) 
          requestState=THROTTLEFUNCSENT;
    }
    
    /**
     * Send the XpressNet message to set the momentary state of
     * functions F9, F10, F11, F12
     */
    protected void sendMomentaryFunctionGroup3()
    {
       if(XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationType()==0x10) {
          // if the command station is multimouse, ignore
          if(log.isDebugEnabled())
		log.debug("Command station does not support Momentary functions");
                return;
       }
       if((requestState&(THROTTLESPEEDSENT|THROTTLESTATSENT|THROTTLEFUNCSENT))
		==THROTTLESTATSENT) 
       {
                log.warn("Outstanding request, Function Group 3 momentary status change ignored");
		return;
       }
       XNetMessage msg=new XNetMessage(6);
       msg.setElement(0,XNetConstants.LOCO_OPER_REQ);
       msg.setElement(1,XNetConstants.LOCO_SET_FUNC_Group3);
       msg.setElement(2,this.getDccAddressHigh());// set to the upper
						    // byte of the  DCC address
       msg.setElement(3,this.getDccAddressLow()); // set to the lower byte
						    //of the DCC address
       // Now, we need to figure out what to send in element 3
       int element4value=0;
       if(f9Momentary)
	{
	  element4value += 1;
	}
       if(f10Momentary)
	{
	  element4value += 2;
	}
       if(f11Momentary)
	{
	  element4value += 4;
	}
       if(f12Momentary)
	{
	  element4value += 8;
	}
       msg.setElement(4,element4value);
       msg.setParity(); // Set the parity bit
       // now, we send the message to the command station
       XNetTrafficController.instance().sendXNetMessage(msg,this);
       if(!isAvailable) 
         requestState=THROTTLEFUNCSENT;
    }
    
    /**
     * Send the XpressNet message to set the momentary state of
     * functions F13, F14, F15, F16 F17 F18 F19 F20
     */
    protected void sendMomentaryFunctionGroup4()
    {
       if(XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationSoftwareVersion()>=3.6){
           log.info("Functions F13-F28 unavailable in CS software version " +
                    XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationSoftwareVersion());
           return;
        } 
       if(XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationType()==0x10) {
          // if the command station is multimouse, ignore
          if(log.isDebugEnabled())
		log.debug("Command station does not support Momentary functions");
                return;
       }
       if((requestState&(THROTTLESPEEDSENT|THROTTLESTATSENT|THROTTLEFUNCSENT))
		==THROTTLESTATSENT) 
       {
                log.warn("Outstanding request, Function Group 4 momentary status change ignored");
		return;
       }
       XNetMessage msg=new XNetMessage(6);
       msg.setElement(0,XNetConstants.LOCO_OPER_REQ);
       msg.setElement(1,XNetConstants.LOCO_SET_FUNC_Group4);
       msg.setElement(2,this.getDccAddressHigh());// set to the upper
						    // byte of the  DCC address
       msg.setElement(3,this.getDccAddressLow()); // set to the lower byte
						    //of the DCC address
       // Now, we need to figure out what to send in element 3
       int element4value=0;
       if(f13Momentary)
	{
	  element4value += 1;
	}
       if(f14Momentary)
	{
	  element4value += 2;
	}
       if(f15Momentary)
	{
	  element4value += 4;
	}
       if(f16Momentary)
	{
	  element4value += 8;
	}
       if(f17Momentary)
	{
	  element4value += 16;
	}
       if(f18Momentary)
	{
	  element4value += 32;
	}
       if(f19Momentary)
	{
	  element4value += 64;
	}
       if(f20Momentary)
	{
	  element4value += 128;
	}
       msg.setElement(4,element4value);
       msg.setParity(); // Set the parity bit
       // now, we send the message to the command station
       XNetTrafficController.instance().sendXNetMessage(msg,this);
       if(!isAvailable) 
         requestState=THROTTLEFUNCSENT;
    }
    
    /**
     * Send the XpressNet message to set the momentary state of
     * functions F21, F22, F23, F24 F25 F26 F27 F28
     */
    protected void sendMomentaryFunctionGroup5()
    {
       if(XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationSoftwareVersion()>=3.6){
           log.info("Functions F13-F28 unavailable in CS software version " +
                    XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationSoftwareVersion());
           return;
        } 
       if(XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationType()==0x10) {
          // if the command station is multimouse, ignore
          if(log.isDebugEnabled())
		log.debug("Command station does not support Momentary functions");
                return;
       }
       if((requestState&(THROTTLESPEEDSENT|THROTTLESTATSENT|THROTTLEFUNCSENT))
		==THROTTLESTATSENT) 
       {
                log.warn("Outstanding request, Function Group 5 momentary status change ignored");
		return;
       }
       XNetMessage msg=new XNetMessage(6);
       msg.setElement(0,XNetConstants.LOCO_OPER_REQ);
       msg.setElement(1,XNetConstants.LOCO_SET_FUNC_Group5);
       msg.setElement(2,this.getDccAddressHigh());// set to the upper
						    // byte of the  DCC address
       msg.setElement(3,this.getDccAddressLow()); // set to the lower byte
						    //of the DCC address
       // Now, we need to figure out what to send in element 3
       int element4value=0;
       if(f21Momentary)
	{
	  element4value += 1;
	}
       if(f22Momentary)
	{
	  element4value += 2;
	}
       if(f23Momentary)
	{
	  element4value += 4;
	}
       if(f24Momentary)
	{
	  element4value += 8;
	}
       if(f25Momentary)
	{
	  element4value += 16;
	}
       if(f26Momentary)
	{
	  element4value += 32;
	}
       if(f27Momentary)
	{
	  element4value += 64;
	}
       if(f28Momentary)
	{
	  element4value += 128;
	}
       msg.setElement(4,element4value);
       msg.setParity(); // Set the parity bit
       // now, we send the message to the command station
       XNetTrafficController.instance().sendXNetMessage(msg,this);
       if(!isAvailable) 
         requestState=THROTTLEFUNCSENT;
    }
    
    /** speed - expressed as a value 0.0 -> 1.0. Negative means emergency stop.
     * This is an bound parameter.
     */
    public float getSpeedSetting()
    {
        return speedSetting;
    }
    
    synchronized public void setSpeedSetting(float speed)
    {
	if(log.isDebugEnabled()) log.debug("set Speed to: " + speed +
					  " Current step mode is: " + this.speedStepMode );

        if((requestState&(THROTTLESPEEDSENT|THROTTLESTATSENT|THROTTLEFUNCSENT))
		==THROTTLESTATSENT) 
        {
                log.warn("Outstanding request, Speed/Direction Setting Change ignored");
		return;
            }
        this.speedSetting = speed;
	if (speed<0)
            {
                /* we're sending an emergency stop to this locomotive only */
                sendEmergencyStop();
            }
	else
	{
        if(speed>(float)1)
		speed=(float)1.0;
	/* we're sending a speed to the locomotive */
       	 XNetMessage msg=new XNetMessage(6);
         msg.setElement(0,XNetConstants.LOCO_OPER_REQ);
         int element4value=0;   /* this is for holding the speed and 
                                 direction setting */
	 if(this.speedStepMode == DccThrottle.SpeedStepMode128) {
		 // We're in 128 speed step mode
		 msg.setElement(1,XNetConstants.LOCO_SPEED_128);
                 // Now, we need to figure out what to send in element 4
                 // Remember, the speed steps are identified as 0-127 (in 
	         // 128 step mode), not 1-128.
                 int speedVal=java.lang.Math.round(speed*126);
                 // speed step 1 is reserved to indicate emergency stop, 
                 // so we need to step over speed step 1
                 if(speedVal>=1) { element4value=speedVal+1; }
	 } else if(this.speedStepMode == DccThrottle.SpeedStepMode28) {
		 // We're in 28 speed step mode
		 msg.setElement(1,XNetConstants.LOCO_SPEED_28);
                 // Now, we need to figure out what to send in element 4
                 int speedVal=java.lang.Math.round(speed*28);
                 // The first speed step used is actually at 4 for 28 
                 // speed step mode.
                 if(speedVal>=1) { speedVal+=3; }
                 // We have to re-arange the bits, since bit 4 is the LSB,
                 // but other bits are in order from 0-3
                 element4value=((speedVal&0x1e)>>1) + 
                                   ((speedVal & 0x01) <<4);
	 } else if(this.speedStepMode == DccThrottle.SpeedStepMode27) {
		 // We're in 27 speed step mode
		 msg.setElement(1,XNetConstants.LOCO_SPEED_27);
                 // Now, we need to figure out what to send in element 4
                 int speedVal=java.lang.Math.round(speed*27);
                 // The first speed step used is actually at 4 for 27 
                 // speed step mode.
                 if(speedVal>=1) { speedVal+=3; }
                 // We have to re-arange the bits, since bit 4 is the LSB,
                 // but other bits are in order from 0-3
                 element4value=((speedVal&0x1e)>>1) + 
                                   ((speedVal & 0x01) <<4);
	 } else {
		 // We're in 14 speed step mode
		 msg.setElement(1,XNetConstants.LOCO_SPEED_14);
                 // Now, we need to figure out what to send in element 4
                 element4value=(int)(speed*14);
                 int speedVal=java.lang.Math.round(speed*14);
                 // The first speed step used is actually at 2 for 14 
                 // speed step mode.
                 if(element4value>=1) { speedVal+=1; }
	 }
      	 msg.setElement(2,this.getDccAddressHigh());// set to the upper
						    // byte of the  DCC address
         msg.setElement(3,this.getDccAddressLow()); // set to the lower byte
						    //of the DCC address
         if(isForward)
 	 {
	    /* the direction bit is always the most significant bit */
	    element4value+=128;
	 }
        msg.setElement(4,element4value);
        msg.setParity(); // Set the parity bit

        // now, we send the message to the command station
        XNetTrafficController.instance().sendXNetMessage(msg,this);
        if (!isAvailable) 
	   requestState=THROTTLESPEEDSENT;
	}
    }
    
    /* Since xpressnet has a seperate Opcode for emergency stop,
     * We're setting this up as a seperate protected function
     */
    protected void sendEmergencyStop()
    {
        /* Emergency stop sent */
        XNetMessage msg=new XNetMessage(4);
        msg.setElement(0,XNetConstants.EMERGENCY_STOP);
        msg.setElement(1,this.getDccAddressHigh());// set to the upper
        // byte of the  DCC address
        msg.setElement(2,this.getDccAddressLow()); // set to the lower byte
        //of the DCC address
        msg.setParity(); // Set the parity bit
        // now, we send the message to the command station
        XNetTrafficController.instance().sendXNetMessage(msg,this);
        if(!isAvailable) 
            requestState=THROTTLESPEEDSENT;
    }

    /** direction
     * This is an bound parameter.
     */
    public boolean getIsForward()
    {
        return isForward;
    }
    
    /* When we set the direction, we're going to set the speed to
       zero as well */
    public void setIsForward(boolean forward)
    {
        isForward = forward;
	setSpeedSetting(this.speedSetting);
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
        XNetTrafficController.instance().removeXNetListener(XNetInterface.COMMINFO |
                                                            XNetInterface.CS_INFO |
                                                            XNetInterface.THROTTLE, this);
	stopStatusTimer();
	    super.dispose();
        //     super.dispose(); // GT 2007/11/6 - This statement results in a warning at run time
    }
    
    public int setDccAddress(int newaddress)
    {
        address=newaddress;
        return address;
    }
    
    public int getDccAddress()
    {
        return address;
    }
    
    protected int getDccAddressHigh()
    {
	return XNetTrafficController.instance()
            .getCommandStation()
            .getDCCAddressHigh(this.address);
    }
    
    protected int getDccAddressLow()
    {
	return XNetTrafficController.instance()
            .getCommandStation()
            .getDCCAddressLow(this.address);
    }
    
    // sendStatusInformation sends a request to get the speed,direction
    // and function status from the command station
    synchronized protected void sendStatusInformationRequest()
    {
        if(requestState==THROTTLESTATSENT){
           if(log.isDebugEnabled()){
              log.debug("Status Information Requested when request outstanding, Timeout?");
           }
           requestState=THROTTLEIDLE;
        }
        /* Send the request for status */
        XNetMessage msg=XNetMessage.getLocomotiveInfoRequestMsg(this.address);
        msg.setRetries(1); // Since we repeat this ourselves, don't ask the 
        // traffic controller to do this for us.
        // now, we send the message to the command station
        XNetTrafficController.instance().sendXNetMessage(msg,this);
        requestState=THROTTLESTATSENT;     
        return;
    }

    // sendFunctionStatusInformation sends a request to get the status
    // of functions from the command station
    synchronized protected void sendFunctionStatusInformationRequest()
    {
       if(XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationType()==0x10) {
          // if the command station is multimouse, ignore
          if(log.isDebugEnabled())
		log.debug("Command station does not support Momentary functions");
                return;
       }
        if(log.isDebugEnabled())log.debug("Throttle " +address +" sending request for function momentary status.");
        /* Send the request for Function status */
        XNetMessage msg=XNetMessage.getLocomotiveFunctionStatusMsg(this.address);
        // now, we send the message to the command station
        XNetTrafficController.instance().sendXNetMessage(msg,this);
        requestState=THROTTLESTATSENT;
        return;
    }
    
    // sendFunctionHighInformationRequest sends a request to get the on/off
    // status of functions 13-28 from the command station
    synchronized protected void sendFunctionHighInformationRequest()
    {
       if(XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationSoftwareVersion()>=3.6){
           log.warn("Functions F13-F28 unavailable in CS software version " +
                    XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationSoftwareVersion());
           return;
        } 
        if(log.isDebugEnabled())log.debug("Throttle " +address +" sending request for function momentary status.");
        /* Send the request for Function status */
        XNetMessage msg=XNetMessage.getLocomotiveFunctionHighOnStatusMsg(this.address);
        // now, we send the message to the command station
        XNetTrafficController.instance().sendXNetMessage(msg,this);
        requestState=THROTTLESTATSENT;
        return;
    }

    // sendFunctionHighomentaryStatusRequest sends a request to get the status
    // of functions from the command station
    synchronized protected void sendFunctionHighMomentaryStatusRequest()
    {
       if(XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationSoftwareVersion()>=3.6){
           log.warn("Functions F13-F28 unavailable in CS software version " +
                    XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationSoftwareVersion());
           return;
        } 
       if(XNetTrafficController.instance()
                               .getCommandStation()
                               .getCommandStationType()==0x10) {
          // if the command station is multimouse, ignore
          if(log.isDebugEnabled())
		log.debug("Command station does not support Momentary functions");
                return;
       }
        if(log.isDebugEnabled())log.debug("Throttle " +address +" sending request for function momentary status.");
        /* Send the request for Function status */
        XNetMessage msg=XNetMessage.getLocomotiveFunctionHighMomStatusMsg(this.address);
        // now, we send the message to the command station
        XNetTrafficController.instance().sendXNetMessage(msg,this);
        requestState=THROTTLESTATSENT;
        return;
    }
    
    // to handle quantized speed. Note this can change! Valued returned is
    // always positive.
    public float getSpeedIncrement()
    {
        return speedIncrement;
    }
    
    /*
     * setSpeedStepMode - set the speed step value.
     * <P>
     * @param Mode - the current speed step mode - default should be 128
     *              speed step mode in most cases
     */
    synchronized public void setSpeedStepMode(int Mode) {
	if(log.isDebugEnabled()) log.debug("Speed Step Mode Change to Mode: " + Mode +
                                           " Current mode is: " + this.speedStepMode);
        this.speedStepMode = Mode;
	if(Mode == DccThrottle.SpeedStepMode128) {
            this.speedIncrement=XNetConstants.SPEED_STEP_128_INCREMENT;
	} else if(Mode == DccThrottle.SpeedStepMode28) {
            this.speedIncrement=XNetConstants.SPEED_STEP_28_INCREMENT;
	} else if(Mode == DccThrottle.SpeedStepMode27) {
            this.speedIncrement=XNetConstants.SPEED_STEP_27_INCREMENT;
	} else if(Mode == DccThrottle.SpeedStepMode14) {
            this.speedIncrement=XNetConstants.SPEED_STEP_14_INCREMENT;
	} else {
            this.speedIncrement=XNetConstants.SPEED_STEP_128_INCREMENT;
            this.speedStepMode = DccThrottle.SpeedStepMode128;
	}
	setSpeedSetting(this.speedSetting);
    }
    
    
    // Handle incoming messages for This throttle.
    public void message(XNetReply l) {
	// First, we want to see if this throttle is waiting for a message 
        //or not.
        if (log.isDebugEnabled()) { log.debug("Throttle - recieved message "); }
	if (requestState==THROTTLEIDLE) {
	    if (log.isDebugEnabled()) { log.debug("Current throttle status is THROTTLEIDLE"); }
	    // We haven't sent anything, but we might be told someone else 
	    // has taken over this address
	    if (l.getElement(0)==XNetConstants.LOCO_INFO_RESPONSE) {
                if (log.isDebugEnabled()) { log.debug("Throttle - message is LOCO_INFO_RESPONSE "); }
		if(l.getElement(1)==XNetConstants.LOCO_NOT_AVAILABLE) {
                    /* the address is in bytes 3 and 4*/
                    if(getDccAddressHigh()==l.getElement(2) && getDccAddressLow()==l.getElement(3)) {
                        if(isAvailable){
			   //Set the Is available flag to "False"
                           log.info("Loco " +getDccAddress() + " In use by another device");
                           setIsAvailable(false);
                           // popup a message box that will trigger a status request
                           //int select=JOptionPane.showConfirmDialog(null,"Throttle for address " +this.getDccAddress() + " Taken Over, reaquire?","Taken Over",JOptionPane.YES_NO_OPTION);
                           //if(select==JOptionPane.YES_OPTION)
                           //{
                           // Send a request for status
  		           //sendStatusInformationRequest();
			   //return;
                           //} else {
                           // Remove the throttle
                           // TODO
		           //}
                        }
		     }
	       }
           }
	} else if ((requestState&THROTTLESPEEDSENT)==THROTTLESPEEDSENT ||
                   (requestState&THROTTLEFUNCSENT)==THROTTLEFUNCSENT) {
	     if(log.isDebugEnabled()) { log.debug("Current throttle status is THROTTLESPEEDSENT"); }
	     // For a Throttle Command, we're just looking for a return 
             // acknowledgment, Either a Success or Failure message.
	     if(l.isOkMessage()) 
		{
                    if(log.isDebugEnabled()) { log.debug("Last Command processed successfully."); }
                    // Since we recieved an "ok",  we want to make sure 
                    // "isAvailable reflects we are in control
                    setIsAvailable(true);
                    requestState=THROTTLEIDLE;
                } else if(l.isCommErrorMessage()) {
                    /* this is a communications error */
                    log.error("Communications error occured - message recieved was: " + l);
                    requestState=THROTTLEIDLE;
                } else if(l.getElement(0)==XNetConstants.CS_INFO &&
                          l.getElement(2)==XNetConstants.CS_NOT_SUPPORTED) {
                    /* The Command Station does not support this command */
                    log.error("Unsupported Command Sent to command station");
                    requestState=THROTTLEIDLE;
                } else {
                    /* this is an unknown error */
                    requestState=THROTTLEIDLE;                        
                    log.warn("Received unhandled response: " + l);
                }
	} else if(requestState==THROTTLESTATSENT) {
            if (log.isDebugEnabled()) { log.debug("Current throttle status is THROTTLESTATSENT"); }
            // This throttle has requested status information, so we need 
            // to process those messages.	
            if (l.getElement(0)==XNetConstants.LOCO_INFO_NORMAL_UNIT) {
                if(l.getElement(1)==XNetConstants.LOCO_FUNCTION_STATUS_HIGH_MOM){
                   /* handle information response about F13-F28 Momentary 
                      Status*/
                   if(log.isDebugEnabled()) {log.debug("Throttle - message is LOCO_FUNCTION_STATUS_HIGH_MOM"); }
                   int b3=l.getElement(3);
                   int b4=l.getElement(4);
                   parseFunctionHighMomentaryInformation(b3,b4);
                   //We've processed this request, so set the status to Idle.
                   requestState=THROTTLEIDLE;
                } else {
                   if(log.isDebugEnabled()) {log.debug("Throttle - message is LOCO_INFO_NORMAL_UNIT"); }
                   /* there is no address sent with this information */
                   int b1=l.getElement(1);
                   int b2=l.getElement(2);
                   int b3=l.getElement(3);
                   int b4=l.getElement(4);
		   
                   parseSpeedandAvailability(b1);
                   parseSpeedandDirection(b2);
                   parseFunctionInformation(b3,b4);
                   
                   //We've processed this request, so set the status to Idle.
                   requestState=THROTTLEIDLE;
                   // And then we want to request the Function Momentary Status
                   sendFunctionStatusInformationRequest();
                   return;
               }
	    } else if (l.getElement(0)==XNetConstants.LOCO_INFO_MUED_UNIT) {
                if(log.isDebugEnabled()) {log.debug("Throttle - message is LOCO_INFO_MUED_UNIT "); }
                /* there is no address sent with this information */
                int b1=l.getElement(1);
                int b2=l.getElement(2);
                int b3=l.getElement(3);
                int b4=l.getElement(4);
                // Element 5 is the consist address, it can only be in the 
                // range 1-99
                int b5=l.getElement(5);
                
                parseSpeedandAvailability(b1);
                parseSpeedandDirection(b2);
                parseFunctionInformation(b3,b4);
                
                // We've processed this request, so set the status to Idle.
                requestState=THROTTLEIDLE;
                // And then we want to request the Function Momentary Status
                sendFunctionStatusInformationRequest();
                return;
	    } else if (l.getElement(0)==XNetConstants.LOCO_INFO_DH_UNIT) {
                if(log.isDebugEnabled()) {log.debug("Throttle - message is LOCO_INFO_DH_UNIT "); }
                /* there is no address sent with this information */
                int b1=l.getElement(1);
                int b2=l.getElement(2);
                int b3=l.getElement(3);
                int b4=l.getElement(4);
                
                // elements 5 and 6 contain the address of the other unit 
                // in the DH
                int b5=l.getElement(5);		
                int b6=l.getElement(6);		
                
                parseSpeedandAvailability(b1);
                parseSpeedandDirection(b2);
                parseFunctionInformation(b3,b4);
                
                // We've processed this request, so set the status to Idle.
                requestState=THROTTLEIDLE;
                // And then we want to request the Function Momentary Status
                sendFunctionStatusInformationRequest();
                return;
	    } else if (l.getElement(0)==XNetConstants.LOCO_INFO_MU_ADDRESS) {
                if(log.isDebugEnabled()) {log.debug("Throttle - message is LOCO_INFO_MU ADDRESS "); }
                /* there is no address sent with this information */
                int b1=l.getElement(1);
                int b2=l.getElement(2);
                
                parseSpeedandAvailability(b1);
                parseSpeedandDirection(b2);
                
                //We've processed this request, so set the status to Idle.
                requestState=THROTTLEIDLE;
                // And then we want to request the Function Momentary Status
                sendFunctionStatusInformationRequest();
                return;
	    } else if (l.getElement(0)==XNetConstants.LOCO_INFO_RESPONSE) {
                if(log.isDebugEnabled()) {log.debug("Throttle - message is LOCO_INFO_RESPONSE "); }
                if(l.getElement(1)==XNetConstants.LOCO_NOT_AVAILABLE) {
                    /* the address is in bytes 3 and 4*/
                    if(getDccAddressHigh()==l.getElement(2) && getDccAddressLow()==l.getElement(3)) {
                        //Set the Is available flag to "False"
                        log.info("Loco "+ getDccAddress() + " In use by another device");
                        setIsAvailable(false);
                    }
                    // We've processed this request, so set the status to Idle.
                    requestState=THROTTLEIDLE;
                } else if(l.getElement(1)==XNetConstants.LOCO_FUNCTION_STATUS) {
                    /* Bytes 3 and 4 contain function momentary status information */
                    int b3=l.getElement(2);
                    int b4=l.getElement(3);
                    parseFunctionMomentaryInformation(b3,b4);
                    // We've processed this request, so set the status to Idle.
                    requestState=THROTTLEIDLE;
                    // And then we want to request the Function Status for F13-F28
                    sendFunctionHighInformationRequest();

                } else if(l.getElement(1)==XNetConstants.LOCO_FUNCTION_STATUS_HIGH) {
                    /* Bytes 3 and 4 contain function status information for F13-F28*/
                    int b3=l.getElement(2);
                    int b4=l.getElement(3);
                    parseFunctionHighInformation(b3,b4);
                    //We've processed this request, so set the status to Idle.
                    requestState=THROTTLEIDLE;
                    // And then we want to request the Function Momentary Status
                    // for functions F13-F28
                    sendFunctionHighMomentaryStatusRequest();
                }
            } else if(l.isCommErrorMessage()) {
                /* this is a communications error */
                log.error("Communications error occured - message received was: " + l);
                requestState=THROTTLEIDLE;
            } else if(l.getElement(0)==XNetConstants.CS_INFO &&
                      l.getElement(1)==XNetConstants.CS_NOT_SUPPORTED) {
                /* The Command Station does not support this command */
                log.error("Unsupported Command Sent to command station");
                requestState=THROTTLEIDLE;
	    }
	}
	//requestState=THROTTLEIDLE;
    }
    
    // listen for the messages to the LI100/LI101
    public void message(XNetMessage l) {
    }
    
    
    // Status Information processing routines
    // Used for return values from Status requests.
    
    //Get SpeedStep and availability information
    protected void parseSpeedandAvailability(int b1)
    {
	/* the first data bite indicates the speed step mode, and
	   if the locomotive is being controlled by another throttle */
        
	if((b1 & 0x08)==0x08 && this.isAvailable)
            {
                log.info("Loco " +getDccAddress() + " In use by another device");
                setIsAvailable(false);
            } else if ((b1&0x08)==0x00 && !this.isAvailable) {
                if(log.isDebugEnabled()) { log.debug("Loco Is Available"); }
                setIsAvailable(true);
            }
	if((b1 & 0x01)==0x01)
            {
                if(log.isDebugEnabled()) { log.debug ("Speed Step setting 27"); }
                this.speedIncrement=XNetConstants.SPEED_STEP_27_INCREMENT;
                if(this.speedStepMode!=DccThrottle.SpeedStepMode27)
                    notifyPropertyChangeListener("SpeedSteps",
                                                 new Integer(this.speedStepMode),
                                                 new Integer(this.speedStepMode=DccThrottle.SpeedStepMode27));
            } else if((b1 & 0x02)==0x02) {
                if(log.isDebugEnabled()) { log.debug("Speed Step setting 28"); }
                this.speedIncrement=XNetConstants.SPEED_STEP_28_INCREMENT;
                if(this.speedStepMode!=DccThrottle.SpeedStepMode28)
                    notifyPropertyChangeListener("SpeedSteps",
                                                 new Integer(this.speedStepMode),
                                                 new Integer(this.speedStepMode=DccThrottle.SpeedStepMode28));
            } else if((b1 & 0x04)==0x04) {
                if(log.isDebugEnabled()) { log.debug("Speed Step setting 128"); }
                this.speedIncrement=XNetConstants.SPEED_STEP_128_INCREMENT;;
                if(this.speedStepMode!=DccThrottle.SpeedStepMode128)
                    notifyPropertyChangeListener("SpeedSteps",
                                                 new Integer(this.speedStepMode),
                                                 new Integer(this.speedStepMode=DccThrottle.SpeedStepMode128));
            } else {
                if(log.isDebugEnabled()) { log.debug("Speed Step setting 14"); }
                this.speedIncrement=XNetConstants.SPEED_STEP_14_INCREMENT;
                if(this.speedStepMode!=DccThrottle.SpeedStepMode14)
                    notifyPropertyChangeListener("SpeedSteps",
                                                 new Integer(this.speedStepMode),
                                                 new Integer(this.speedStepMode=DccThrottle.SpeedStepMode14));
            }
    }
    
    //Get Speed and Direction information
    protected void parseSpeedandDirection(int b2)
    {
	/* the second byte indicates the speed and direction setting */
        
	if ((b2 & 0x80)==0x80 && this.isForward==false) {
            if (log.isDebugEnabled ()) { log.debug("Throttle - Direction Forward Locomotive:" +address); }
            notifyPropertyChangeListener("IsForward",
                                         new Boolean(this.isForward),
                                         new Boolean(this.isForward=true));
            if(this.isForward==true) {
		if(log.isDebugEnabled()) { log.debug("Throttle - Changed direction to Forward Locomotive:"+address); }
            }
	} else if ((b2 & 0x80)==0x00 && this.isForward==true) {
            if(log.isDebugEnabled()) { log.debug("Throttle - Direction Reverse Locomotive:" +address); }
            notifyPropertyChangeListener("IsForward",
                                         new Boolean(this.isForward),
                                         new Boolean(this.isForward=false));
            if(this.isForward==false) {
		if(log.isDebugEnabled()) { log.debug("Throttle - Changed direction to Reverse Locomotive:" +address);}
            }
	}
        
	if(this.speedStepMode == DccThrottle.SpeedStepMode128) {
            // We're in 128 speed step mode
            int speedVal=b2 & 0x7f;
            // The first speed step used is actually at 2 for 128 
            // speed step mode.
            if(speedVal>=1) { speedVal-=1; }
            else speedVal=0;
            if(java.lang.Math.abs(
                                  this.getSpeedSetting()-((float)speedVal/(float)126))>=0.0079){
                notifyPropertyChangeListener("SpeedSetting",
                                             new Float(this.speedSetting),
                                             new Float(this.speedSetting = 
                                                       (float)speedVal/(float)126));
            }
        } else if(this.speedStepMode == DccThrottle.SpeedStepMode28) {
            // We're in 28 speed step mode
            // We have to re-arange the bits, since bit 4 is the LSB,
            // but other bits are in order from 0-3
            int speedVal =((b2 & 0x0F)<<1) + 
                ((b2 & 0x10) >>4);
            // The first speed step used is actually at 4 for 28 
            // speed step mode.
            if(speedVal>=3) { speedVal-=3; }
            else speedVal=0;
            if(java.lang.Math.abs(
                                  this.getSpeedSetting()-((float)speedVal/(float)28))>=0.035) {
                notifyPropertyChangeListener("SpeedSetting",
                                             new Float(this.speedSetting),
                                             new Float(this.speedSetting = 
                                                       (float)speedVal/(float)28));
            }
        } else if(this.speedStepMode == DccThrottle.SpeedStepMode27) {
            // We're in 27 speed step mode
            // We have to re-arange the bits, since bit 4 is the LSB,
            // but other bits are in order from 0-3
            int speedVal =((b2 & 0x0F)<<1) + 
                ((b2 & 0x10) >>4);
            // The first speed step used is actually at 4 for 27 
            // speed step mode.
            if(speedVal>=3) { speedVal-=3; }
            else speedVal=0;
            if(java.lang.Math.abs(
                                  this.getSpeedSetting()-((float)speedVal/(float)27))>=0.037) {
                notifyPropertyChangeListener("SpeedSetting",
                                             new Float(this.speedSetting),
                                             new Float(this.speedSetting = 
                                                       (float)speedVal/(float)27));
            }
        } else {
            // Assume we're in 14 speed step mode.
            int speedVal=(b2 & 0x0F);
            if(speedVal>=1) { speedVal-=1; }
            else speedVal=0;
            if(java.lang.Math.abs(
                                  this.getSpeedSetting()-((float)speedVal/(float)14))>=0.071) {
                notifyPropertyChangeListener("SpeedSetting",
                                             new Float(this.speedSetting),
                                             new Float(this.speedSetting = 
                                                       (float)speedVal/(float)14));
            }
        }
    }
    
    protected void parseFunctionInformation(int b3,int b4)
    {
	if(log.isDebugEnabled()) log.debug("Parsing Function F0-F12 status, function bytes: " +b3 +" and " +b4);
	/* data byte 3 is the status of F0 F4 F3 F2 F1 */
	if((b3 & 0x10)==0x10 && getF0()==false) {
            notifyPropertyChangeListener("F0",
                                         new Boolean(this.f0),
                                         new Boolean(this.f0 = true));
	} else if ((b3 &0x10)==0x00 && getF0()==true) {
            notifyPropertyChangeListener("F0",
                                         new Boolean(this.f0),
                                         new Boolean(this.f0 = false));
	}
        
	if((b3 & 0x01)==0x01 && getF1()==false) {
            notifyPropertyChangeListener("F1",
                                         new Boolean(this.f1),
                                         new Boolean(this.f1 = true));
	} else if ((b3 &0x01)==0x00 && getF1()==true) {
            notifyPropertyChangeListener("F1",
                                         new Boolean(this.f1),
                                         new Boolean(this.f1 = false));
	}
        
	if((b3 & 0x02)==0x02 && getF2()==false) {
            notifyPropertyChangeListener("F2",
                                         new Boolean(this.f2),
                                         new Boolean(this.f2 = true));
	} else if ((b3 &0x02)==0x00 && getF2()==true) {
            notifyPropertyChangeListener("F2",
                                         new Boolean(this.f2),
                                         new Boolean(this.f2 = false));
	}
	
	if((b3 & 0x04)==0x04 && getF3()==false) {
            notifyPropertyChangeListener("F3",
                                         new Boolean(this.f3),
                                         new Boolean(this.f3 = true));
	} else if ((b3 &0x04)==0x00 && getF3()==true) {
            notifyPropertyChangeListener("F3",
                                         new Boolean(this.f3),
                                         new Boolean(this.f3 = false));
	}
        
	if((b3 & 0x08)==0x08 && getF4()==false) {
            notifyPropertyChangeListener("F4",
                                         new Boolean(this.f4),
                                         new Boolean(this.f4 = true));
	} else if ((b4 &0x08)==0x00 && getF4()==true) {
            notifyPropertyChangeListener("F4",
                                         new Boolean(this.f4),
                                         new Boolean(this.f4 = false));
	}
        
	/* data byte 4 is the status of F12 F11 F10 F9 F8 F7 F6 F5 */
        
	if((b4 & 0x01)==0x01 && getF5()==false)	{
            notifyPropertyChangeListener("F5",
                                         new Boolean(this.f5),
                                         new Boolean(this.f5 = true));
	} else if ((b4 &0x01)==0x00 && getF5()==true) {
            notifyPropertyChangeListener("F5",
                                         new Boolean(this.f5),
                                         new Boolean(this.f5 = false));
	}
        
	if((b4 & 0x02)==0x02 && getF6()==false) {
            notifyPropertyChangeListener("F6",
                                         new Boolean(this.f6),
                                         new Boolean(this.f6 = true));
	} else if ((b4 &0x02)==0x00 && getF6()==true) {
            notifyPropertyChangeListener("F6",
                                         new Boolean(this.f6),
                                         new Boolean(this.f6 = false));
	} 
        
	if((b4 & 0x04)==0x04 && getF7()==false) {
            notifyPropertyChangeListener("F7",
                                         new Boolean(this.f7),
                                         new Boolean(this.f7 = true));
	} else if ((b4 &0x04)==0x00 && getF7()==true) {
            notifyPropertyChangeListener("F7",
                                         new Boolean(this.f7),
                                         new Boolean(this.f7 = false));
	}
        
	if((b4 & 0x08)==0x08 && getF8()==false) {
            notifyPropertyChangeListener("F8",
                                         new Boolean(this.f8),
                                         new Boolean(this.f8 = true));
	} else if ((b4 &0x08)==0x00 && getF8()==true) {
            notifyPropertyChangeListener("F8",
                                         new Boolean(this.f8),
                                         new Boolean(this.f8 = false));
	}
        
	if((b4 & 0x10)==0x10 && getF9()==false) {
            notifyPropertyChangeListener("F9",
                                         new Boolean(this.f9),
                                         new Boolean(this.f9 = true));
	} else if ((b4 &0x10)==0x00 && getF9()==true) {
            notifyPropertyChangeListener("F9",
                                         new Boolean(this.f9),
                                         new Boolean(this.f9 = false));
	}
        
	if((b4 & 0x20)==0x20 && getF10()==false) {
            notifyPropertyChangeListener("F10",
                                         new Boolean(this.f10),
                                         new Boolean(this.f10 = true));
	} else if ((b4 &0x20)==0x00 && getF10()==true) {
            notifyPropertyChangeListener("F10",
                                         new Boolean(this.f10),
                                         new Boolean(this.f10 = false));
	}
        
	if((b4 & 0x40)==0x40 && getF11()==false) {
            notifyPropertyChangeListener("F11",
                                         new Boolean(this.f11),
                                         new Boolean(this.f11 = true));
	} else if ((b4 &0x40)==0x00 && getF11()==true) {
            notifyPropertyChangeListener("F11",
                                         new Boolean(this.f11),
                                         new Boolean(this.f11 = false));
	}
        
	if((b4 & 0x80)==0x80 && getF12()==false) {
            notifyPropertyChangeListener("F12",
                                         new Boolean(this.f12),
                                         new Boolean(this.f12 = true));
	} else if ((b4 &0x80)==0x00 && getF12()==true) {
            notifyPropertyChangeListener("F12",
                                         new Boolean(this.f12),
                                         new Boolean(this.f12 = false));
	}
    }
    
    protected void parseFunctionHighInformation(int b3,int b4)
    {
	if(log.isDebugEnabled()) log.debug("Parsing Function F13-F28 status, function bytes: " +b3 +" and " +b4);
	/* data byte 3 is the status of F20 F19 F18 F17 F16 F15 F14 F13 */
	if((b3 & 0x01)==0x01 && getF13()==false) {
            notifyPropertyChangeListener("F13",
                                         new Boolean(this.f13),
                                         new Boolean(this.f13 = true));
	} else if ((b3 &0x01)==0x00 && getF14()==true) {
            notifyPropertyChangeListener("F14",
                                         new Boolean(this.f13),
                                         new Boolean(this.f13 = false));
	}
        
	if((b3 & 0x02)==0x02 && getF14()==false) {
            notifyPropertyChangeListener("F14",
                                         new Boolean(this.f14),
                                         new Boolean(this.f14 = true));
	} else if ((b3 &0x02)==0x00 && getF14()==true) {
            notifyPropertyChangeListener("F14",
                                         new Boolean(this.f14),
                                         new Boolean(this.f14 = false));
	}
	
	if((b3 & 0x04)==0x04 && getF15()==false) {
            notifyPropertyChangeListener("F15",
                                         new Boolean(this.f15),
                                         new Boolean(this.f15 = true));
	} else if ((b3 &0x04)==0x00 && getF15()==true) {
            notifyPropertyChangeListener("F15",
                                         new Boolean(this.f15),
                                         new Boolean(this.f15 = false));
	}
        
	if((b3 & 0x08)==0x08 && getF16()==false) {
            notifyPropertyChangeListener("F16",
                                         new Boolean(this.f16),
                                         new Boolean(this.f16 = true));
	} else if ((b4 &0x08)==0x00 && getF16()==true) {
            notifyPropertyChangeListener("F16",
                                         new Boolean(this.f16),
                                         new Boolean(this.f16 = false));
	}

	if((b3 & 0x10)==0x10 && getF17()==false) {
            notifyPropertyChangeListener("F17",
                                         new Boolean(this.f17),
                                         new Boolean(this.f17 = true));
	} else if ((b4 &0x10)==0x00 && getF17()==true) {
            notifyPropertyChangeListener("F17",
                                         new Boolean(this.f17),
                                         new Boolean(this.f17 = false));
	}
        
	if((b3 & 0x20)==0x20 && getF18()==false) {
            notifyPropertyChangeListener("F18",
                                         new Boolean(this.f18),
                                         new Boolean(this.f18 = true));
	} else if ((b4 &0x20)==0x00 && getF18()==true) {
            notifyPropertyChangeListener("F18",
                                         new Boolean(this.f18),
                                         new Boolean(this.f18 = false));
	}
	
        if((b3 & 0x40)==0x40 && getF19()==false) {
            notifyPropertyChangeListener("F19",
                                         new Boolean(this.f19),
                                         new Boolean(this.f19 = true));
	} else if ((b4 &0x40)==0x00 && getF19()==true) {
            notifyPropertyChangeListener("F19",
                                         new Boolean(this.f19),
                                         new Boolean(this.f19 = false));
	}
        
        if((b3 & 0x80)==0x80 && getF20()==false) {
            notifyPropertyChangeListener("F20",
                                         new Boolean(this.f20),
                                         new Boolean(this.f20 = true));
	} else if ((b4 &0x80)==0x00 && getF20()==true) {
            notifyPropertyChangeListener("F20",
                                         new Boolean(this.f20),
                                         new Boolean(this.f20 = false));
	}
	/* data byte 4 is the status of F28 F27 F26 F25 F24 F23 F22 F21 */
        
	if((b4 & 0x01)==0x01 && getF21()==false)	{
            notifyPropertyChangeListener("F21",
                                         new Boolean(this.f21),
                                         new Boolean(this.f21 = true));
	} else if ((b4 &0x01)==0x00 && getF21()==true) {
            notifyPropertyChangeListener("F21",
                                         new Boolean(this.f21),
                                         new Boolean(this.f21 = false));
	}
        
	if((b4 & 0x02)==0x02 && getF22()==false) {
            notifyPropertyChangeListener("F22",
                                         new Boolean(this.f22),
                                         new Boolean(this.f22 = true));
	} else if ((b4 &0x02)==0x00 && getF22()==true) {
            notifyPropertyChangeListener("F22",
                                         new Boolean(this.f22),
                                         new Boolean(this.f22 = false));
	} 
        
	if((b4 & 0x04)==0x04 && getF23()==false) {
            notifyPropertyChangeListener("F23",
                                         new Boolean(this.f23),
                                         new Boolean(this.f23 = true));
	} else if ((b4 &0x04)==0x00 && getF23()==true) {
            notifyPropertyChangeListener("F23",
                                         new Boolean(this.f23),
                                         new Boolean(this.f23 = false));
	}
        
	if((b4 & 0x08)==0x08 && getF24()==false) {
            notifyPropertyChangeListener("F24",
                                         new Boolean(this.f24),
                                         new Boolean(this.f24 = true));
	} else if ((b4 &0x08)==0x00 && getF24()==true) {
            notifyPropertyChangeListener("F24",
                                         new Boolean(this.f24),
                                         new Boolean(this.f24 = false));
	}
        
	if((b4 & 0x10)==0x10 && getF25()==false) {
            notifyPropertyChangeListener("F25",
                                         new Boolean(this.f25),
                                         new Boolean(this.f25 = true));
	} else if ((b4 &0x10)==0x00 && getF25()==true) {
            notifyPropertyChangeListener("F25",
                                         new Boolean(this.f25),
                                         new Boolean(this.f25 = false));
	}
        
	if((b4 & 0x20)==0x20 && getF26()==false) {
            notifyPropertyChangeListener("F26",
                                         new Boolean(this.f26),
                                         new Boolean(this.f26 = true));
	} else if ((b4 &0x20)==0x00 && getF26()==true) {
            notifyPropertyChangeListener("F26",
                                         new Boolean(this.f26),
                                         new Boolean(this.f26 = false));
	}
        
	if((b4 & 0x40)==0x40 && getF27()==false) {
            notifyPropertyChangeListener("F27",
                                         new Boolean(this.f27),
                                         new Boolean(this.f27 = true));
	} else if ((b4 &0x40)==0x00 && getF27()==true) {
            notifyPropertyChangeListener("F27",
                                         new Boolean(this.f27),
                                         new Boolean(this.f27 = false));
	}
        
	if((b4 & 0x80)==0x80 && getF28()==false) {
            notifyPropertyChangeListener("F28",
                                         new Boolean(this.f28),
                                         new Boolean(this.f28 = true));
	} else if ((b4 &0x80)==0x00 && getF28()==true) {
            notifyPropertyChangeListener("F28",
                                         new Boolean(this.f28),
                                         new Boolean(this.f28 = false));
	}
    }
    
    protected void parseFunctionMomentaryInformation(int b3,int b4)
    {
	if(log.isDebugEnabled()) log.debug("Parsing Function Momentary status, function bytes: " +b3 +" and " +b4);
	/* data byte 3 is the momentary status of F0 F4 F3 F2 F1 */
	if((b3 & 0x10)==0x10 && this.f0Momentary==false) {
            notifyPropertyChangeListener("F0Momentary",
                                         new Boolean(this.f0Momentary),
                                         new Boolean(this.f0Momentary = true));
	} else if ((b3 &0x10)==0x00 && this.f0Momentary==true) {
            notifyPropertyChangeListener("F0Momentary",
                                         new Boolean(this.f0Momentary),
                                         new Boolean(this.f0Momentary = false));
	}
        
	if((b3 & 0x01)==0x01 && this.f1Momentary==false) {
            notifyPropertyChangeListener("F1Momentary",
                                         new Boolean(this.f1Momentary),
                                         new Boolean(this.f1Momentary = true));
	} else if ((b3 &0x01)==0x00 && this.f1Momentary==true) {
            notifyPropertyChangeListener("F1Momentary",
                                         new Boolean(this.f1Momentary),
                                         new Boolean(this.f1Momentary = false));
	}
        
	if((b3 & 0x02)==0x02 && this.f2Momentary==false) {
            notifyPropertyChangeListener("F2Momentary",
                                         new Boolean(this.f2Momentary),
                                         new Boolean(this.f2Momentary = true));
	} else if ((b3 &0x02)==0x00 && this.f2Momentary==true) {
            notifyPropertyChangeListener("F2Momentary",
                                         new Boolean(this.f2Momentary),
                                         new Boolean(this.f2Momentary = false));
	}
	
	if((b3 & 0x04)==0x04 && this.f3Momentary==false) {
            notifyPropertyChangeListener("F3Momentary",
                                         new Boolean(this.f3Momentary),
                                         new Boolean(this.f3Momentary = true));
	} else if ((b3 &0x04)==0x00 && this.f3Momentary==true) {
            notifyPropertyChangeListener("F3Momentary",
                                         new Boolean(this.f3Momentary),
                                         new Boolean(this.f3Momentary = false));
	}
        
	if((b3 & 0x08)==0x08 && this.f4Momentary==false) {
            notifyPropertyChangeListener("F4Momentary",
                                         new Boolean(this.f4Momentary),
                                         new Boolean(this.f4Momentary = true));
	} else if ((b4 &0x08)==0x00 && this.f4Momentary==true) {
            notifyPropertyChangeListener("F4Momentary",
                                         new Boolean(this.f4Momentary),
                                         new Boolean(this.f4Momentary = false));
	}
        
	/* data byte 4 is the momentary status of F12 F11 F10 F9 F8 F7 F6 F5 */
        
	if((b4 & 0x01)==0x01 && this.f5Momentary==false)	{
            notifyPropertyChangeListener("F5Momentary",
                                         new Boolean(this.f5Momentary),
                                         new Boolean(this.f5Momentary = true));
	} else if ((b4 &0x01)==0x00 && this.f5Momentary==true) {
            notifyPropertyChangeListener("F5Momentary",
                                         new Boolean(this.f5Momentary),
                                         new Boolean(this.f5Momentary = false));
	}
        
	if((b4 & 0x02)==0x02 && this.f6Momentary==false) {
            notifyPropertyChangeListener("F6Momentary",
                                         new Boolean(this.f6Momentary),
                                         new Boolean(this.f6Momentary = true));
	} else if ((b4 &0x02)==0x00 && this.f6Momentary==true) {
            notifyPropertyChangeListener("F6Momentary",
                                         new Boolean(this.f6Momentary),
                                         new Boolean(this.f6Momentary = false));
	} 
        
	if((b4 & 0x04)==0x04 && this.f7Momentary==false) {
            notifyPropertyChangeListener("F7Momentary",
                                         new Boolean(this.f7Momentary),
                                         new Boolean(this.f7Momentary = true));
	} else if ((b4 &0x04)==0x00 && this.f7Momentary==true) {
            notifyPropertyChangeListener("F7Momentary",
                                         new Boolean(this.f7Momentary),
                                         new Boolean(this.f7Momentary = false));
	}
        
	if((b4 & 0x08)==0x08 && this.f8Momentary==false) {
            notifyPropertyChangeListener("F8Momentary",
                                         new Boolean(this.f8Momentary),
                                         new Boolean(this.f8Momentary = true));
	} else if ((b4 &0x08)==0x00 && this.f8Momentary==true) {
            notifyPropertyChangeListener("F8Momentary",
                                         new Boolean(this.f8Momentary),
                                         new Boolean(this.f8Momentary = false));
	}
        
	if((b4 & 0x10)==0x10 && this.f9Momentary==false) {
            notifyPropertyChangeListener("F9Momentary",
                                         new Boolean(this.f9Momentary),
                                         new Boolean(this.f9Momentary = true));
	} else if ((b4 &0x10)==0x00 && this.f9Momentary==true) {
            notifyPropertyChangeListener("F9Momentary",
                                         new Boolean(this.f9Momentary),
                                         new Boolean(this.f9Momentary = false));
	}
        
	if((b4 & 0x20)==0x20 && this.f10Momentary==false) {
            notifyPropertyChangeListener("F10Momentary",
                                         new Boolean(this.f10Momentary),
                                         new Boolean(this.f10Momentary = true));
	} else if ((b4 &0x20)==0x00 && this.f10Momentary==true) {
            notifyPropertyChangeListener("F10Momentary",
                                         new Boolean(this.f10Momentary),
                                         new Boolean(this.f10Momentary = false));
	}
        
	if((b4 & 0x40)==0x40 && this.f11Momentary==false) {
            notifyPropertyChangeListener("F11Momentary",
                                         new Boolean(this.f11Momentary),
                                         new Boolean(this.f11Momentary = true));
	} else if ((b4 &0x40)==0x00 && this.f11Momentary==true) {
            notifyPropertyChangeListener("F11Momentary",
                                         new Boolean(this.f11Momentary),
                                         new Boolean(this.f11Momentary = false));
	}
        
	if((b4 & 0x80)==0x80 && this.f12Momentary==false) {
            notifyPropertyChangeListener("F12Momentary",
                                         new Boolean(this.f12Momentary),
                                         new Boolean(this.f12Momentary = true));
	} else if ((b4 &0x80)==0x00 && this.f12Momentary==true) {
            notifyPropertyChangeListener("F12Momentary",
                                         new Boolean(this.f12Momentary),
                                         new Boolean(this.f12Momentary = false));
	}
    }

    protected void parseFunctionHighMomentaryInformation(int b3,int b4)
    {
	if(log.isDebugEnabled()) log.debug("Parsing Function F13-F28 Momentary status, function bytes: " +b3 +" and " +b4);
	/* data byte 3 is the momentary status of F20 F19 F17 F16 F15 F14 F13 */
        
	if((b3 & 0x01)==0x01 && this.f13Momentary==false) {
            notifyPropertyChangeListener("F13Momentary",
                                         new Boolean(this.f13Momentary),
                                         new Boolean(this.f13Momentary = true));
	} else if ((b3 &0x01)==0x00 && this.f13Momentary==true) {
            notifyPropertyChangeListener("F13Momentary",
                                         new Boolean(this.f13Momentary),
                                         new Boolean(this.f13Momentary = false));
	}
        
	if((b3 & 0x02)==0x02 && this.f2Momentary==false) {
            notifyPropertyChangeListener("F14Momentary",
                                         new Boolean(this.f14Momentary),
                                         new Boolean(this.f14Momentary = true));
	} else if ((b3 &0x02)==0x00 && this.f14Momentary==true) {
            notifyPropertyChangeListener("F14Momentary",
                                         new Boolean(this.f14Momentary),
                                         new Boolean(this.f14Momentary = false));
	}
	
	if((b3 & 0x04)==0x04 && this.f15Momentary==false) {
            notifyPropertyChangeListener("F15Momentary",
                                         new Boolean(this.f15Momentary),
                                         new Boolean(this.f15Momentary = true));
	} else if ((b3 &0x04)==0x00 && this.f15Momentary==true) {
            notifyPropertyChangeListener("F15Momentary",
                                         new Boolean(this.f15Momentary),
                                         new Boolean(this.f15Momentary = false));
	}
        
	if((b3 & 0x08)==0x08 && this.f16Momentary==false) {
            notifyPropertyChangeListener("F16Momentary",
                                         new Boolean(this.f16Momentary),
                                         new Boolean(this.f16Momentary = true));
	} else if ((b4 &0x08)==0x00 && this.f16Momentary==true) {
            notifyPropertyChangeListener("F16Momentary",
                                         new Boolean(this.f16Momentary),
                                         new Boolean(this.f16Momentary = false));
	}
	
        if((b3 & 0x10)==0x10 && this.f17Momentary==false) {
            notifyPropertyChangeListener("F17Momentary",
                                         new Boolean(this.f17Momentary),
                                         new Boolean(this.f17Momentary = true));
	} else if ((b3 &0x10)==0x00 && this.f17Momentary==true) {
            notifyPropertyChangeListener("F17Momentary",
                                         new Boolean(this.f17Momentary),
                                         new Boolean(this.f17Momentary = false));
	}

        if((b3 & 0x20)==0x20 && this.f18Momentary==false) {
            notifyPropertyChangeListener("F18Momentary",
                                         new Boolean(this.f18Momentary),
                                         new Boolean(this.f18Momentary = true));
	} else if ((b3 &0x20)==0x00 && this.f18Momentary==true) {
            notifyPropertyChangeListener("F18Momentary",
                                         new Boolean(this.f18Momentary),
                                         new Boolean(this.f18Momentary = false));
	}

        if((b3 & 0x40)==0x40 && this.f19Momentary==false) {
            notifyPropertyChangeListener("F19Momentary",
                                         new Boolean(this.f19Momentary),
                                         new Boolean(this.f19Momentary = true));
	} else if ((b3 &0x40)==0x00 && this.f19Momentary==true) {
            notifyPropertyChangeListener("F19Momentary",
                                         new Boolean(this.f19Momentary),
                                         new Boolean(this.f19Momentary = false));
	}
        
        if((b3 & 0x80)==0x80 && this.f20Momentary==false) {
            notifyPropertyChangeListener("F20Momentary",
                                         new Boolean(this.f20Momentary),
                                         new Boolean(this.f20Momentary = true));
	} else if ((b3 &0x80)==0x00 && this.f20Momentary==true) {
            notifyPropertyChangeListener("F20Momentary",
                                         new Boolean(this.f20Momentary),
                                         new Boolean(this.f20Momentary = false));
	}

	/* data byte 4 is the momentary status of F28 F27 F26 F25 F24 F23 F22 F21 */
        
	if((b4 & 0x01)==0x01 && this.f21Momentary==false)	{
            notifyPropertyChangeListener("F5Momentary",
                                         new Boolean(this.f21Momentary),
                                         new Boolean(this.f21Momentary = true));
	} else if ((b4 &0x01)==0x00 && this.f21Momentary==true) {
            notifyPropertyChangeListener("F21Momentary",
                                         new Boolean(this.f21Momentary),
                                         new Boolean(this.f21Momentary = false));
	}
        
	if((b4 & 0x02)==0x02 && this.f22Momentary==false) {
            notifyPropertyChangeListener("F22Momentary",
                                         new Boolean(this.f22Momentary),
                                         new Boolean(this.f22Momentary = true));
	} else if ((b4 &0x02)==0x00 && this.f22Momentary==true) {
            notifyPropertyChangeListener("F22Momentary",
                                         new Boolean(this.f22Momentary),
                                         new Boolean(this.f22Momentary = false));
	} 
        
	if((b4 & 0x04)==0x04 && this.f23Momentary==false) {
            notifyPropertyChangeListener("F23Momentary",
                                         new Boolean(this.f23Momentary),
                                         new Boolean(this.f23Momentary = true));
	} else if ((b4 &0x04)==0x00 && this.f23Momentary==true) {
            notifyPropertyChangeListener("F23Momentary",
                                         new Boolean(this.f23Momentary),
                                         new Boolean(this.f23Momentary = false));
	}
        
	if((b4 & 0x08)==0x08 && this.f24Momentary==false) {
            notifyPropertyChangeListener("F24Momentary",
                                         new Boolean(this.f24Momentary),
                                         new Boolean(this.f24Momentary = true));
	} else if ((b4 &0x08)==0x00 && this.f24Momentary==true) {
            notifyPropertyChangeListener("F24Momentary",
                                         new Boolean(this.f24Momentary),
                                         new Boolean(this.f24Momentary = false));
	}
        
	if((b4 & 0x10)==0x10 && this.f25Momentary==false) {
            notifyPropertyChangeListener("F25Momentary",
                                         new Boolean(this.f25Momentary),
                                         new Boolean(this.f25Momentary = true));
	} else if ((b4 &0x10)==0x00 && this.f25Momentary==true) {
            notifyPropertyChangeListener("F25Momentary",
                                         new Boolean(this.f25Momentary),
                                         new Boolean(this.f25Momentary = false));
	}
        
	if((b4 & 0x20)==0x20 && this.f26Momentary==false) {
            notifyPropertyChangeListener("F26Momentary",
                                         new Boolean(this.f26Momentary),
                                         new Boolean(this.f26Momentary = true));
	} else if ((b4 &0x20)==0x00 && this.f26Momentary==true) {
            notifyPropertyChangeListener("F26Momentary",
                                         new Boolean(this.f26Momentary),
                                         new Boolean(this.f26Momentary = false));
	}
        
	if((b4 & 0x40)==0x40 && this.f27Momentary==false) {
            notifyPropertyChangeListener("F27Momentary",
                                         new Boolean(this.f27Momentary),
                                         new Boolean(this.f27Momentary = true));
	} else if ((b4 &0x40)==0x00 && this.f27Momentary==true) {
            notifyPropertyChangeListener("F27Momentary",
                                         new Boolean(this.f27Momentary),
                                         new Boolean(this.f27Momentary = false));
	}
        
	if((b4 & 0x80)==0x80 && this.f28Momentary==false) {
            notifyPropertyChangeListener("F28Momentary",
                                         new Boolean(this.f28Momentary),
                                         new Boolean(this.f28Momentary = true));
	} else if ((b4 &0x80)==0x00 && this.f28Momentary==true) {
            notifyPropertyChangeListener("F28Momentary",
                                         new Boolean(this.f28Momentary),
                                         new Boolean(this.f28Momentary = false));
	}
    }
    
    /*
     * Set the internal isAvailable property
     */ 
    protected void setIsAvailable(boolean Available) {
        if(this.isAvailable!=Available) {
            notifyPropertyChangeListener("IsAvailable",
                                         new Boolean(this.isAvailable),
                                         new Boolean(this.isAvailable = Available));
        }
        /* if we're setting this to true, stop the timer,
           otherwise start the timer. */
        if(Available==true) {
            stopStatusTimer();
        } else startStatusTimer();
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
     */
    protected void stopStatusTimer() {
        if(statTimer!=null) statTimer.stop();
        //           requestState=THROTTLEIDLE;
    }
    
    public LocoAddress getLocoAddress() {
        return new DccLocoAddress(address, XNetThrottleManager.isLongAddress(address));
    }
    
    // register for notification
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetThrottle.class.getName());
}

