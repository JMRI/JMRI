// XNetSensor.java

package jmri.jmrix.lenz;

import jmri.AbstractSensor;
import jmri.Sensor;

/**
 * Extend jmri.AbstractSensor for XPressNet layouts.
 * <P>
 * @author			Paul Bender Copyright (C) 2003
 * @version         $Revision: 2.4 $
 */
public class XNetSensor extends AbstractSensor implements XNetListener {

    private int address;
    private int baseaddress; /* The result of integer division of the 
                                sensor address by 8 */
    private int nibble;      /* Is this sensor in the upper or lower 
				nibble for the feedback encoder */
    private int nibblebit;   /* Which bit in the nibble represents this 
				sensor */
    private String systemName;

    public XNetSensor(String systemName, String userName) {
        super(systemName, userName);
        init(systemName);
    }

    public XNetSensor(String systemName) {
        super(systemName);
        init(systemName);
    }

    /**
     * Common initialization for both constructors
     */
    private void init(String id) {
        // store address
	systemName=new String(id);
        address = Integer.parseInt(id.substring(2,id.length()));
	// calculate the base address, the nibble, and the bit to examine
	baseaddress = ((address-1) / 8);
	int temp = (address-1) % 8;
	if(temp<4) {
	   // This address is in the lower nibble
	   nibble = 0x00;
	} else {
	   nibble = 0x10;
	}
	switch(temp%4) {
		case 0: nibblebit=0x01;
			break;
		case 1: nibblebit=0x02;
			break;
		case 2: nibblebit=0x04;
			break;
		case 3: nibblebit=0x08;
			break;
		default: nibblebit=0x00;
	}
        if (log.isDebugEnabled())
        	log.debug("Created Sensor " + systemName  + 
 				  " (Address " + baseaddress + 
                                  " possition " + (((address-1) % 8) + 1) +
				  ")");
        // At construction, register for messages
        XNetTrafficController.instance().addXNetListener(~0, this);
	// Request initial status from the layout
        this.requestUpdateFromLayout();
    }

    /**
     * request an update on status by sending an XPressNet message
     */
    public void requestUpdateFromLayout() {
       // To do this, we send an XpressNet Accessory Decoder Information 
       // Request.
       // The generated message works for Feedback modules and turnouts 
       // with feedback, but the address passed is translated as though it 
       // is a turnout address.  As a result, we substitute our base 
       // address in for the address. after the message is returned.
       XNetMessage msg = XNetMessage.getFeedbackRequestMsg(baseaddress,
                                                          (nibble==0x00));
       msg.setElement(1,baseaddress);
       XNetTrafficController.instance().sendXNetMessage(msg, this);
    }

    /**
     * implementing classes will typically have a function/listener to get
     * updates from the layout, which will then call
     *      public void firePropertyChange(String propertyName,
     *      					Object oldValue,
     *                                          Object newValue)
     * _once_ if anything has changed state (or set the commanded state directly)
     * @param l
     */
    public void message(XNetReply l) {
	   if(l.isFeedbackMessage() &&
             (l.getFeedbackMessageType()==2) &&
              baseaddress==l.getFeedbackEncoderMsgAddr() &&
	      nibble == (l.getElement(2) & 0x10)) {
              if(log.isDebugEnabled())
                        log.debug("Message for sensor " + systemName  + 
 				  " (Address " + baseaddress + 
                                  " position " + (address-(baseaddress*8)) +
				  ")");
		if((l.getElement(2) & nibblebit)!=0) {
			setOwnState(Sensor.ACTIVE);
		}
		else setOwnState(Sensor.INACTIVE);
	   }
        return;
    }

    // listen for the messages to the LI100/LI101
    public void message(XNetMessage l) {
    }


    public void dispose() {
        XNetTrafficController.instance().removeXNetListener(~0, this);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetSensor.class.getName());

}


/* @(#)XNetSensor.java */
