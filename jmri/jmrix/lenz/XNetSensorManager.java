// XNetSensorManager.java

package jmri.jmrix.lenz;

import jmri.Sensor;

/**
 * Manage the XPressNet specific Sensor implementation.
 *
 * System names are "XSnnn", where nnn is the sensor number without padding.
 *
 * @author			Paul Bender Copyright (C) 2003
 * @version			$Revision: 2.3 $
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
        XNetTrafficController.instance().removeXNetListener(~0, this);
    }

    // XPressNet specific methods

    public Sensor createNewSensor(String systemName, String userName) {
        return new XNetSensor(systemName, userName);
    }

    // ctor has to register for XNetNet events
    public XNetSensorManager() {
        XNetTrafficController.instance().addXNetListener(~0, this);
        mInstance = this;
    }

    // listen for sensors, creating them as needed
    public void message(XNetReply l) {
	   if(l.isFeedbackMessage() && (l.getFeedbackMessageType()==2)) {
              // This is a feedback encoder message. The address of the 
	      // Feedback sensor is byte two of the message.
              int address=l.getFeedbackEncoderMsgAddr(); 
	      if(log.isDebugEnabled()) 
			log.debug("Message for feedback encoder " + address); 

	      int firstaddress=((address)*8)+1;
	      // Each Feedback encoder includes 8 addresses, so register 
	      // a sensor for each address.
	      for(int i=0;i<8;i++) {
	   	  String s = "XS" + (firstaddress+i);
	   	  if(null == getBySystemName(s)) {
	   	     provideSensor(s);
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
