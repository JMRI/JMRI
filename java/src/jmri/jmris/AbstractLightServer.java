//AbstractLightServer.java

package jmri.jmris;

import org.apache.log4j.Logger;
import java.io.*;
import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.Light;

/**
 * Abstract interface between the a JMRI Light and a 
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision$
 */

abstract public class AbstractLightServer {

   public AbstractLightServer(){
      lights= new ArrayList<String>();
   }

    /*
     * Protocol Specific Abstract Functions
     */

     abstract public void sendStatus(String lightName,int Status) throws IOException; 
     abstract public void sendErrorStatus(String lightName) throws IOException;
     abstract public void parseStatus(String statusString) throws jmri.JmriException,java.io.IOException;

    synchronized protected void addLightToList(java.lang.String lightName){
       if(!lights.contains(lightName)){
         lights.add(lightName);
         InstanceManager.lightManagerInstance().provideLight(lightName)
            .addPropertyChangeListener(new LightListener(lightName));
       }
    }

 
   synchronized protected void removeLightFromList(java.lang.String lightName) {
      if(lights.contains(lightName)){
         lights.remove(lightName);
      }
   }


    public void lightOff(java.lang.String lightName) {
                Light light=null;
		// load address from switchAddrTextField
		try {

                        addLightToList(lightName); 
			light= InstanceManager.lightManagerInstance().provideLight(lightName);
			if (light == null) {
				log.error("Light " + lightName
						+ " is not available");
			} else {
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
                Light light=null;
		// load address from switchAddrTextField
		try {
                        addLightToList(lightName); 
			light= InstanceManager.lightManagerInstance().provideLight(lightName);

			if (light== null) {
				log.error("Light " + lightName
						+ " is not available");
			} else {
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

    class LightListener implements java.beans.PropertyChangeListener {


       LightListener(String lightName) {
           name = lightName;
           light = InstanceManager.lightManagerInstance().provideLight(lightName);
       }

       // update state as state of light changes
       public void propertyChange(java.beans.PropertyChangeEvent e) {
      	   // If the Commanded State changes, show transition state as "<inconsistent>" 
           if (e.getPropertyName().equals("KnownState")) {
               int now = ((Integer) e.getNewValue()).intValue();
               try {
                  sendStatus(name,now);
               } catch(java.io.IOException ie) {
                  log.debug("Error Sending Status");
                  // if we get an error, de-register
                  light.removePropertyChangeListener(this);
                  removeLightFromList(name);
               }
           }
        }



       Light light = null;
       String name = null;

    }    

    protected ArrayList<String> lights = null;

    String newState = "";


    static Logger log = Logger.getLogger(AbstractLightServer.class.getName());

}
