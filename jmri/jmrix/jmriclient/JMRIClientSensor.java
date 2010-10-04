// JMRIClientSensor.java

package jmri.jmrix.jmriclient;

import jmri.implementation.AbstractSensor;
import jmri.Sensor;

/**
 * JMRIClient implementation of the Sensor interface.
 * <P>
 *
 * Description:		extend jmri.AbstractSensor for JMRIClient layouts
 * @author			Bob Jacobsen Copyright (C) 2001, 2008
 * @author			Paul Bender Copyright (C) 2010
 * @version			$Revision: 1.2 $
 */
public class JMRIClientSensor extends AbstractSensor implements JMRIClientListener {

	// data members
	private int _number;   // sensor number
        private JMRIClientTrafficController tc=null;

	/**
	 * JMRIClient sensors use the sensor number on the remote host.
	 */
	public JMRIClientSensor(int number,JMRIClientSystemConnectionMemo memo)        {
            super(memo.getSystemPrefix()+"s"+number);
            _number = number;
            tc = memo.getJMRIClientTrafficController();
            // At construction, register for messages
            tc.addJMRIClientListener(this);
	}

	public int getNumber() { return _number; }

	// Handle a request to change state by sending a formatted packet
        // to the server.
	protected void forwardCommandChangeToLayout(int s) {
		// sort out states
		if ( (s & Sensor.ACTIVE ) > 0) {
			// first look for the double case, which we can't handle
			if ( (s & Sensor.INACTIVE) > 0) {
				// this is the disaster case!
				log.error("Cannot command both ACTIVE and INACTIVE "+s);
				return;
			} else {
				// send an ACTIVE command
				sendMessage(true^getInverted());
			}
		} else {
			// send a INACTIVE command
			sendMessage(false^getInverted());
		}
	}

	public void requestUpdateFromLayout() {
		// get the message text
        String text = "SENSOR "+ getSystemName() + "\n";
            
        // create and send the message itself
		tc.sendJMRIClientMessage(new JMRIClientMessage(text), null);
	}


	protected void sendMessage(boolean active) {
		// get the message text
        String text;
        if (active) 
            text = "SENSOR "+ getSystemName() + " ACTIVE\n";
        else // thrown
            text = "SENSOR "+ getSystemName() +" INACTIVE\n";
            
        // create and send the message itself
		tc.sendJMRIClientMessage(new JMRIClientMessage(text), null);
	}

       // to listen for status changes from JMRIClient system
        public void reply(JMRIClientReply m) {
               String message=m.toString();
               if(!message.contains(getSystemName())) return; // not for us

               if(m.toString().contains("ACTIVE"))
                  setOwnState(jmri.Sensor.ACTIVE);
               else if(m.toString().contains("INACTIVE"))
                  setOwnState(jmri.Sensor.INACTIVE);
               else
                  setOwnState(jmri.Sensor.UNKNOWN);
        }

        public void message(JMRIClientMessage m) {
        }





	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JMRIClientSensor.class.getName());

}


/* @(#)JMRIClientSensor.java */
