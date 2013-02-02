// SRCPSensor.java

package jmri.jmrix.srcp;

import org.apache.log4j.Logger;
import jmri.implementation.AbstractSensor;
import jmri.Sensor;

/**
 * SRCPSensor implementation of the Sensor interface.
 * <P>
 *
 * Description:		extend jmri.AbstractSensor for SRCP layouts
 * @author			Bob Jacobsen Copyright (C) 2001, 2008
 * @author			Paul Bender Copyright (C) 2010
 * @version			$Revision$
 */
public class SRCPSensor extends AbstractSensor implements SRCPListener {

	// data members
	private int _number;   // sensor number
	private int _bus;      // bus number
        private SRCPTrafficController tc=null;

	/**
	 * SRCP sensors use the sensor number on the remote host.
	 */
	public SRCPSensor(int number,SRCPSystemConnectionMemo memo)        {
            super(memo.getSystemPrefix()+"s"+number);
            _number = number;
            _bus = 1; // for now
            tc = memo.getTrafficController();
            // At construction, register for messages
            tc.addSRCPListener(this);
            // Then request status.
            requestUpdateFromLayout(); 
	}

	public int getNumber() { return _number; }

	// Handle a request to change state by sending a formatted packet
        // to the server.
	public void setKnownState(int s) throws jmri.JmriException{
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
                if (_knownState != s) {
                    int oldState = _knownState;
                    _knownState = s;
                    firePropertyChange("KnownState", Integer.valueOf(oldState), Integer.valueOf(_knownState));
 	        }
	}

	public void requestUpdateFromLayout() {
		// get the message text
        String text = "GET " + _bus +" FB "  + _number + "\n";
            
        // create and send the message itself
	tc.sendSRCPMessage(new SRCPMessage(text),this);
	}


	protected void sendMessage(boolean active) {
		// get the message text
        String text;
        if (active) 
            text = "SET " + _bus +" FB "  + _number + " 1\n";
        else // thrown
            text = "SET " + _bus +" FB "  + _number + " 0\n";
            
        // create and send the message itself
        tc.sendSRCPMessage(new SRCPMessage(text), this);
	}

       // to listen for status changes from SRCP system
        public void reply(SRCPReply m) {
               String message=m.toString();
               log.debug("Message Received: " +m);
               if(!message.contains(_bus +" FB " +_number)) return; // not for us

               if(m.toString().contains(_bus + " FB " +_number +" 0"))
                  setOwnState(!getInverted()?jmri.Sensor.INACTIVE:jmri.Sensor.ACTIVE);
               else if(m.toString().contains(_bus + " FB " +_number +" 1"))
                  setOwnState(!getInverted()?jmri.Sensor.ACTIVE:jmri.Sensor.INACTIVE);
               else
                  setOwnState(jmri.Sensor.UNKNOWN);
        }

        public void reply(jmri.jmrix.srcp.parser.SimpleNode n) {	
           if(log.isDebugEnabled())
              log.debug("reply called with simpleNode " + n.jjtGetValue());
           reply(new SRCPReply(n));
        }

        public void message(SRCPMessage m) {
        }





	static Logger log = Logger.getLogger(SRCPSensor.class.getName());

}


/* @(#)JMRIClientSensor.java */
