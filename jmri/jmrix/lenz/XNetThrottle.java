package jmri.jmrix.lenz;

import jmri.jmrix.AbstractThrottle;
import javax.swing.JOptionPane;

/**
 * An implementation of DccThrottle with code specific to a
 * XpressnetNet connection.
 * @author     Paul Bender (C) 2002,2003
 * @version    $Revision: 1.22 $
 */

public class XNetThrottle extends AbstractThrottle implements XNetListener
{
    private boolean isAvailable;  // Flag  stating if the throttle is in 
                                  // use or not.

    static final int THROTTLEIDLE=0;  // Idle Throttle
    static final int THROTTLESTATSENT=1;  // Sent Status request
    static final int THROTTLECMDSENT=2;  // Sent command to locomotive

    private int requestState=THROTTLEIDLE;

    /**
     * Constructor
     */
    public XNetThrottle()
    {
       super();
       XNetTrafficController.instance().addXNetListener(~0, this);
       if (log.isDebugEnabled()) { log.debug("XnetThrottle constructor"); }
    }

    /**
     * Constructor
     */
    public XNetThrottle(int address)
    {
       super();
       this.setDccAddress(address);
       this.speedIncrement=XNetConstants.SPEED_STEP_128_INCREMENT;
//       this.isForward=true;
       this.isAvailable=false;
       XNetTrafficController.instance().addXNetListener(~0, this);
       sendStatusInformationRequest();
       if (log.isDebugEnabled()) { log.debug("XnetThrottle constructor called for address " + address ); }
    }

    /**
     * Send the XpressNet message to set the state of locomotive
     * direction and functions F0, F1, F2, F3, F4
     */
    protected void sendFunctionGroup1()
    {
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
       requestState=THROTTLECMDSENT;
    }

    /**
     * Send the XpressNet message to set the state of
     * functions F5, F6, F7, F8
     */
    protected void sendFunctionGroup2()
    {
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
       requestState=THROTTLECMDSENT;
    }

    /**
     * Send the XpressNet message to set the state of
     * functions F9, F10, F11, F12
     */
    protected void sendFunctionGroup3()
    {
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
       requestState=THROTTLECMDSENT;
    }

   /** speed - expressed as a value 0.0 -> 1.0. Negative means emergency stop.
     * This is an bound parameter.
     */
    public float getSpeedSetting()
    {
        return speedSetting;
    }

    public void setSpeedSetting(float speed)
    {
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
	 if(getSpeedIncrement()==XNetConstants.SPEED_STEP_128_INCREMENT) {
		 // We're in 128 speed step mode
		 msg.setElement(1,XNetConstants.LOCO_SPEED_128);
                 // Now, we need to figure out what to send in element 4
                 // Remember, the speed steps are identified as 0-127 (in 
	         // 128 step mode), not 1-128.
                 int speedVal=java.lang.Math.round(speed*126);
                 // speed step 1 is reserved to indicate emergency stop, 
                 // so we need to step over speed step 1
                 if(element4value>=1) { element4value+=1; }
	 } else if(getSpeedIncrement()==XNetConstants.SPEED_STEP_28_INCREMENT) {
		 // We're in 28 speed step mode
		 msg.setElement(1,XNetConstants.LOCO_SPEED_28);
                 // Now, we need to figure out what to send in element 4
                 int speedVal=java.lang.Math.round(speed*28);
                 // The first speed step used is actually at 4 for 28 
                 // speed step mode.
                 if(speedVal>=1) { element4value+=3; }
                 // We have to re-arange the bits, since bit 4 is the LSB,
                 // but other bits are in order from 0-3
                 element4value=((speedVal&0x1e)>>1) + 
                                   ((speedVal & 0x01) <<16);
	 } else if(getSpeedIncrement()==XNetConstants.SPEED_STEP_27_INCREMENT) {
		 // We're in 27 speed step mode
		 msg.setElement(1,XNetConstants.LOCO_SPEED_27);
                 // Now, we need to figure out what to send in element 4
                 int speedVal=java.lang.Math.round(speed*27);
                 // The first speed step used is actually at 4 for 27 
                 // speed step mode.
                 if(speedVal>=1) { element4value+=3; }
                 // We have to re-arange the bits, since bit 4 is the LSB,
                 // but other bits are in order from 0-3
                 element4value=((speedVal&0x1e)>>1) + 
                                   ((speedVal & 0x01) <<16);
	 } else {
		 // We're in 14 speed step mode
		 msg.setElement(1,XNetConstants.LOCO_SPEED_14);
                 // Now, we need to figure out what to send in element 4
                 element4value=(int)(speed*14);
                 int speedVal=java.lang.Math.round(speed*14);
                 // The first speed step used is actually at 2 for 14 
                 // speed step mode.
                 if(element4value>=1) { element4value+=1; }
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
        requestState=THROTTLECMDSENT;
	}
    }

    /* Since xpressnet has a seperate Opcode for emergency stop,
     * We're setting this up as a seperate private function
     */
     private void sendEmergencyStop()
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
        requestState=THROTTLECMDSENT;
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

    // functions - note that we use the naming for DCC, though that's not the implication;
    // see also DccThrottle interface
    public boolean getF0()
    {
        return f0;
    }

    public void setF0(boolean f0)
    {
        this.f0 = f0;
        sendFunctionGroup1();
    }

    public boolean getF1()
    {
        return f1;
    }

    public void setF1(boolean f1)
    {
        this.f1 = f1;
        sendFunctionGroup1();

    }

    public boolean getF2()
    {
        return f2;
    }

    public void setF2(boolean f2)
    {
        this.f2 = f2;
        sendFunctionGroup1();
    }

    public boolean getF3()
    {
        return f3;
    }

    public void setF3(boolean f3)
    {
        this.f3 = f3;
        sendFunctionGroup1();
    }

    public boolean getF4()
    {
        return f4;
    }

    public void setF4(boolean f4)
    {
        this.f4 = f4;
        sendFunctionGroup1();
    }


    public boolean getF5()
    {
        return f5;
    }

    public void setF5(boolean f5)
    {
        this.f5 = f5;
        sendFunctionGroup2();
    }

    public boolean getF6()
    {
        return f6;
    }

    public void setF6(boolean f6)
    {
        this.f6 = f6;
        sendFunctionGroup2();
    }


    public boolean getF7()
    {
        return f7;
    }

    public void setF7(boolean f7)
    {
        this.f7 = f7;
        sendFunctionGroup2();

    }


    public boolean getF8()
    {
        return f8;
    }

    public void setF8(boolean f8)
    {
        this.f8 = f8;
        sendFunctionGroup2();
    }

    public boolean getF9()
    {
        return f9;
    }

    public void setF9(boolean f9)
    {
        this.f9 = f9;
        sendFunctionGroup3();
    }

    public boolean getF10()
    {
        return f10;
    }

    public void setF10(boolean f10)
    {
        this.f10 = f10;
        sendFunctionGroup3();
    }

    public boolean getF11()
    {
        return f11;
    }

    public void setF11(boolean f11)
    {
        this.f11 = f11;
        sendFunctionGroup3();
    }

    public boolean getF12()
    {
        return f12;
    }

    public void setF12(boolean f12)
    {
        this.f12 = f12;
        sendFunctionGroup3();
    }


    /**
     * Locomotive identification.  The exact format is defined by the
     * specific implementation, but its intended that this is a user-specified
     * name like "UP 777", or whatever convention the user wants to employ.
     *
     * This is an unbound parameter.
     */
    public String getLocoIdentification()
    {
        return "";
    }


    /**
     * Locomotive address.  The exact format is defined by the
     * specific implementation, but for DCC systems it is intended that this
     * will be the DCC address in the form "nnnn" (extended) vs "nnn" or "nn" (short).
     * Non-DCC systems may use a different form.
     *
     * This is an unbound parameter.
     */
    public String getLocoAddress()
    {
        return "";
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
       XNetTrafficController.instance().removeXNetListener(~0, this);
        super.dispose();
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

    private int getDccAddressHigh()
    {
	return XNetTrafficController.instance()
                                    .getCommandStation()
                                    .getDCCAddressHigh(this.address);
    }

    private int getDccAddressLow()
    {
	return XNetTrafficController.instance()
                                    .getCommandStation()
                                    .getDCCAddressLow(this.address);
    }

    // getStatusInformation sends a request to get the status
    // speed and function status from the command station
    private void sendStatusInformationRequest()
    {
       /* First, send the request for status */
       XNetMessage msg=new XNetMessage(5);
       msg.setElement(0,XNetConstants.LOCO_STATUS_REQ);
       msg.setElement(1,XNetConstants.LOCO_INFO_REQ_V3);
       msg.setElement(2,this.getDccAddressHigh());// set to the upper
						    // byte of the  DCC address
       msg.setElement(3,this.getDccAddressLow()); // set to the lower byte
						    //of the DCC address
       msg.setParity(); // Set the parity bit
       // now, we send the message to the command station
       XNetTrafficController.instance().sendXNetMessage(msg,this);
       requestState=THROTTLESTATSENT;


       /* next, send the request for function values */
       msg.setElement(0,XNetConstants.LOCO_STATUS_REQ);
       msg.setElement(1,XNetConstants.LOCO_INFO_REQ_FUNC);
       msg.setElement(2,this.getDccAddressHigh());// set to the upper
						    // byte of the  DCC address
       msg.setElement(3,this.getDccAddressLow()); // set to the lower byte
						    //of the DCC address
       msg.setParity(); // Set the parity bit
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

    // Handle incoming messages for This throttle.
    public void message(XNetMessage l) {
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
			//Set the Is available flag to "False"
	              log.info("Loco " +getDccAddress() + " In use by another device");
	              if(this.isAvailable) {
        	         notifyPropertyChangeListener("IsAvailable",
                	                         new Boolean(this.isAvailable),
                        	                 new Boolean(this.isAvailable = false));
		      }
                      // popup a message box that will trigger a status request
		      int select=JOptionPane.showConfirmDialog(null,"Throttle for address " +this.getDccAddress() + " Taken Over, reaquire?","Taken Over",JOptionPane.YES_NO_OPTION);
		      if(select==JOptionPane.YES_OPTION)
		      {
                        // Send a request for status
  		        sendStatusInformationRequest();
		      } else {
                        // Remove the throttle
                        // TODO
		      }
		   }
	       }
           }
	} else if (requestState==THROTTLECMDSENT) {
	    	 if(log.isDebugEnabled()) { log.debug("Current throttle status is THROTTLECMDSENT"); }
	    // For a Throttle Command, we're just looking for a return 
            // acknowledgment, Either a Success or Failure message.
	    if(XNetTrafficController.instance().getCommandStation().isOkMessage(l)) 
		{
	    	 if(log.isDebugEnabled()) { log.debug("Last Command processed successfully."); }
                 // Since we recieved an "ok",  we want to make sure "isAvailable reflects we are in control
	         if(!this.isAvailable) {
                    notifyPropertyChangeListener("IsAvailable",
                                                 new Boolean(this.isAvailable),
                                                 new Boolean(this.isAvailable = true));
                 }
		requestState=THROTTLEIDLE;
	    } else if(l.getElement(0)==XNetConstants.LI_MESSAGE_RESPONSE_HEADER &&
                     ((l.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_UNKNOWN_DATA_ERROR ||
               	       l.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_CS_DATA_ERROR ||
                       l.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_PC_DATA_ERROR ||
                       l.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR))) {
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
                     log.warn("Recieved unhandled response: " + l);
              }

	} else if(requestState==THROTTLESTATSENT) {
	     if (log.isDebugEnabled()) { log.debug("Current throttle status is THROTTLESTATSENT"); }
	     // This throttle has requested status information, so we need 
             // to process those messages.	
	     if (l.getElement(0)==XNetConstants.LOCO_INFO_NORMAL_UNIT) {
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

                //We've processed this request, so set the status to Idle.
		requestState=THROTTLEIDLE;

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

                //We've processed this request, so set the status to Idle.
		requestState=THROTTLEIDLE;

	    } else if (l.getElement(0)==XNetConstants.LOCO_INFO_MU_ADDRESS) {
                if(log.isDebugEnabled()) {log.debug("Throttle - message is LOCO_INFO_MU ADDRESS "); }
                /* there is no address sent with this information */
		int b1=l.getElement(1);
		int b2=l.getElement(2);

		parseSpeedandAvailability(b1);
		parseSpeedandDirection(b2);

                //We've processed this request, so set the status to Idle.
		requestState=THROTTLEIDLE;

	    } else if (l.getElement(0)==XNetConstants.LOCO_INFO_RESPONSE) {
                if(log.isDebugEnabled()) {log.debug("Throttle - message is LOCO_INFO_RESPONSE "); }
		if(l.getElement(1)==XNetConstants.LOCO_NOT_AVAILABLE) {
	           /* the address is in bytes 3 and 4*/
		   if(getDccAddressHigh()==l.getElement(2) && getDccAddressLow()==l.getElement(3)) {
			//Set the Is available flag to "False"
                      log.info("Loco "+ getDccAddress() + " In use by another device");
	              if(this.isAvailable) {
        	          notifyPropertyChangeListener("IsAvailable",
                	                           new Boolean(this.isAvailable),
                        	                   new Boolean(this.isAvailable = false));
		      }
		   }
		} else if(l.getElement(1)==XNetConstants.LOCO_FUNCTION_STATUS) {
		    /* Bytes 3 and 4 contain function status information */
		    int b3=l.getElement(3);
		    int b4=l.getElement(4);
	            parseFunctionInformation(b3,b4);
		}
                //We've processed this request, so set the status to Idle.
		requestState=THROTTLEIDLE;
	    }
	}
	requestState=THROTTLEIDLE;
    }

    // Status Information processing routines
    // Used for return values from Status requests.

    //Get SpeedStep and availability information
    private void parseSpeedandAvailability(int b1)
    {
	/* the first data bite indicates the speed step mode, and
	   if the locomotive is being controlled by another throttle */

	if((b1 & 0x08)==0x08 && this.isAvailable)
	{
	   log.info("Loco " +getDccAddress() + " In use by another device");
           notifyPropertyChangeListener("IsAvailable",
                                         new Boolean(this.isAvailable),
                                         new Boolean(this.isAvailable = false));
	} else if ((b1&0x08)==0x00 && !this.isAvailable) {
           if(log.isDebugEnabled()) { log.debug("Loco Is Available"); }
	   notifyPropertyChangeListener("IsAvailable",
					new Boolean(this.isAvailable),
					new Boolean(this.isAvailable = true));
	}
	if((b1 & 0x01)==0x01)
	{
           if(log.isDebugEnabled()) { log.debug ("Speed Step setting 27"); }
	   this.speedIncrement=XNetConstants.SPEED_STEP_27_INCREMENT;
	} else if((b1 & 0x02)==0x02) {
           if(log.isDebugEnabled()) { log.debug("Speed Step setting 28"); }
           this.speedIncrement=XNetConstants.SPEED_STEP_28_INCREMENT;
	} else if((b1 & 0x04)==0x04) {
           if(log.isDebugEnabled()) { log.debug("Speed Step setting 128"); }
	   this.speedIncrement=XNetConstants.SPEED_STEP_128_INCREMENT;;
	} else {
           if(log.isDebugEnabled()) { log.debug("Speed Step setting 14"); }
    	   this.speedIncrement=XNetConstants.SPEED_STEP_14_INCREMENT;
	}
    }

    //Get Speed and Direction information
    private void parseSpeedandDirection(int b2)
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
	if(this.speedIncrement==XNetConstants.SPEED_STEP_128_INCREMENT) {
	   if(this.getSpeedSetting()!=(float)((b2 & 0x7f)-2)/126) {
	      notifyPropertyChangeListener("SpeedSetting",
                      		            new Float(this.speedSetting),
					    new Float(this.speedSetting = 
						(float)((b2 & 0x7f)-2)/126));
	   }
	} else {
	   if(this.getSpeedSetting()!=(float)(((b2 & 0x7f)-4)/(126/this.speedIncrement))) {
  	      notifyPropertyChangeListener("SpeedSetting",
                     	                   new Float(this.speedSetting),
			  	  	   new Float(this.speedSetting = (float)(((b2 & 0x7f)-4)/(126/this.speedIncrement))));
	   }
	}
    }

    public void parseFunctionInformation(int b3,int b4)
    {
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

    // register for notification

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetThrottle.class.getName());
}

