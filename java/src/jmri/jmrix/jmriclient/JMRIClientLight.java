// JMRIClientLight.java

package jmri.jmrix.jmriclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.implementation.AbstractLight;
import jmri.Light;

/**
 * JMRIClient implementation of the Light interface.
 * <P>
 *
 * Description:		extend jmri.AbstractLight for JMRIClient layouts
 * @author			Bob Jacobsen Copyright (C) 2001, 2008
 * @author			Paul Bender Copyright (C) 2010
 * @version			$Revision$
 */
public class JMRIClientLight extends AbstractLight implements JMRIClientListener {

	// data members
	private int _number;   // light number
        private JMRIClientTrafficController tc=null;

	/**
	 * JMRIClient lights use the light number on the remote host.
	 */
	public JMRIClientLight(int number,JMRIClientSystemConnectionMemo memo)        {
            super(memo.getSystemPrefix()+"l"+number);
            _number = number;
            tc = memo.getJMRIClientTrafficController();
            // At construction, register for messages
            tc.addJMRIClientListener(this);
            // then request status
            requestUpdateFromLayout();
	}

	public int getNumber() { return _number; }

        //request a status update from the layout
        protected void requestUpdateFromLayout(){
            // create the message
            String text="LIGHT " + getSystemName() + "\n";
            // create and send the message
            tc.sendJMRIClientMessage(new JMRIClientMessage(text),this);
        }


	// Handle a request to change state by sending a formatted packet
        // to the server.
	public synchronized void doNewState(int oldState, int s) {
                if(oldState==s) return; //no change, just quit.
		// sort out states
		if ( (s & Light.ON) > 0) {
			// first look for the double case, which we can't handle
			if ( (s & Light.OFF) > 0) {
				// this is the disaster case!
				log.error("Cannot command both ON and OFF "+s);
				return;
			} else {
				// send a ON command
				sendMessage(true);
			}
		} else {
			// send a OFF command
			sendMessage(false);
		}

                notifyStateChange(oldState,s);

	}

	protected void sendMessage(boolean on) {
		// get the message text
        String text;
        if (on) 
            text = "LIGHT "+ getSystemName() + " ON\n";
        else // thrown
            text = "LIGHT "+ getSystemName() +" OFF\n";
            
        // create and send the message itself
		tc.sendJMRIClientMessage(new JMRIClientMessage(text), this);
	}

       // to listen for status changes from JMRIClient system
        public synchronized void reply(JMRIClientReply m) {
               String message=m.toString();
               if(!message.contains(getSystemName()+" ")) return; // not for us

               if(m.toString().contains("OFF"))
                  notifyStateChange(mState,jmri.Light.OFF);
               else if(m.toString().contains("ON"))
                  notifyStateChange(mState,jmri.Light.ON);
               else
                  notifyStateChange(mState,jmri.Light.UNKNOWN);
        }

        public void message(JMRIClientMessage m) {
        }


	static Logger log = LoggerFactory.getLogger(JMRIClientLight.class.getName());

}


/* @(#)JMRIClientLight.java */
