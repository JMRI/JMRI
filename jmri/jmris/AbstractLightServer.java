//AbstractLightServer.java

package jmri.jmris;

import java.io.*;

import jmri.InstanceManager;
import jmri.Light;

/**
 * Abstract interface between the a JMRI Light and a 
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision: 1.3 $
 */

abstract public class AbstractLightServer implements java.beans.PropertyChangeListener {

   public AbstractLightServer(){
   }

    /*
     * Protocol Specific Abstract Functions
     */

     abstract public void sendStatus(int Status) throws IOException; 
     abstract public void sendErrorStatus() throws IOException;
     abstract public void parseStatus(String statusString) throws jmri.JmriException,java.io.IOException;
	
    public void lightOff(java.lang.String lightName) {
		// load address from switchAddrTextField
		try {
			if (light != null)
				light.removePropertyChangeListener(this);
			light= InstanceManager.lightManagerInstance().provideLight(lightName);
			if (light == null) {
				log.error("Light " + lightName
						+ " is not available");
			} else {
				light.addPropertyChangeListener(this);
				if (log.isDebugEnabled())
					log.debug("about to command OFF");
				// and set state to OFF 
				light.setState(jmri.Light.OFF);
			}
		} catch (Exception ex) {
			log.error("light off, exception: "
							+ ex.toString());
		}
	}

        public void lightOn(java.lang.String lightName) {
		// load address from switchAddrTextField
		try {
			if (light!= null)
				light.removePropertyChangeListener(this);
			light= InstanceManager.lightManagerInstance().provideLight(lightName);

			if (light== null) {
				log.error("Light " + lightName
						+ " is not available");
			} else {
				light.addPropertyChangeListener(this);
				if (log.isDebugEnabled())
					log.debug("about to command ON");
				// and set state to ON
				light.setState(jmri.Light.ON);
			}
		} catch (Exception ex) {
			log.error("light ON, exception: "
							+ ex.toString());
		}
	}

    // update state as state of light changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	// If the Commanded State changes, show transition state as "<inconsistent>" 
        if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue()).intValue();
            try {
               sendStatus(now);
            } catch(java.io.IOException ie) {
                  log.error("Error Sending Status");
            }
        }
     }
    
    protected Light light = null;

    String newState = "";


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractLightServer.class.getName());

}
