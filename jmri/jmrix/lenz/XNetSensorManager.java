// XNetSensorManager.java

package jmri.jmrix.lenz;

import jmri.Sensor;

/**
 * Manage the XPressNet specific Sensor implementation.
 *
 * System names are "XSnnn", where nnn is the sensor number without padding.
 *
 * @author			Paul Bender Copyright (C) 2003
 * @version			$Revision: 2.4 $
 */
public class XNetSensorManager extends jmri.AbstractSensorManager implements XNetListener {

    public char systemLetter() { return 'X'; }

    static public XNetSensorManager instance() {
        if (mInstance == null) new XNetSensorManager();
        return mInstance;
    }
    static private XNetSensorManager mInstance = null;

    // to free resources when no longer used
    public void dispose() {
        XNetTrafficController.instance().removeXNetListener(XNetInterface.FEEDBACK, this);
    }

    // XPressNet specific methods

    public Sensor createNewSensor(String systemName, String userName) {
        return new XNetSensor(systemName, userName);
    }

    // ctor has to register for XNetNet events
    public XNetSensorManager() {
        XNetTrafficController.instance().addXNetListener(XNetInterface.FEEDBACK,this);
        mInstance = this;
    }

    // listen for sensors, creating them as needed
    public void message(XNetReply l) {
	if(log.isDebugEnabled()) log.debug("recieved message: " +l);
	if(l.isFeedbackBroadcastMessage()) {
	   int numDataBytes = l.getElement(0) & 0x0f;
	   for(int i=1; i<numDataBytes; i+=2) {
	      if(l.getFeedbackMessageType(i)==2) {
                 // This is a feedback encoder message. The address of the 
	         // Feedback sensor is byte two of the message.
                 int address=l.getFeedbackEncoderMsgAddr(i); 
	         if(log.isDebugEnabled()) 
			log.debug("Message for feedback encoder " + address); 

	         int firstaddress=((address)*8)+1;
	         // Each Feedback encoder includes 8 addresses, so register 
	         // a sensor for each address.
	         for(int j=0;j<8;j++) {
	   	     String s = "XS" + (firstaddress+j);
	   	     if(null == getBySystemName(s)) {
	   	        provideSensor(s);
	             }
                 }
              }
           }
	}
    }

    // listen for the messages to the LI100/LI101
    public void message(XNetMessage l) {
    }


    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetSensorManager.class.getName());

}

/* @(#)XNetSensorManager.java */
