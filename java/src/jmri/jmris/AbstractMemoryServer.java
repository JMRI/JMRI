//AbstractMemoryServer.java

package jmri.jmris;

import java.io.*;
import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.Memory;

/**
 * Abstract interface between a JMRI memory and a 
 * network connection
 * @author          mstevetodd Copyright (C) 2012 (copied from AbstractSensorServer)
 * @version         $Revision:  $
 */

abstract public class AbstractMemoryServer {

   public AbstractMemoryServer(){
      memories= new ArrayList<String>();
   }

    /*
     * Protocol Specific Abstract Functions
     */

     abstract public void sendStatus(String memory, String Status) throws IOException; 
     abstract public void sendErrorStatus(String memory) throws IOException;
     abstract public void parseStatus(String statusString) throws jmri.JmriException,java.io.IOException;

    synchronized protected void addMemoryToList(java.lang.String memoryName) {
         if (!memories.contains(memoryName) ) {
             memories.add(memoryName);
             InstanceManager.memoryManagerInstance().provideMemory(memoryName)
                     .addPropertyChangeListener(new MemoryListener(memoryName));
         }
    }

    synchronized protected void removeMemoryFromList(java.lang.String memoryName) {
         if (memories.contains(memoryName) ) {
             memories.remove(memoryName);
         }
    }

	
    public void setMemoryValue(java.lang.String memoryName, java.lang.String memoryValue) {
    	Memory memory = null;
    	try {
    		addMemoryToList(memoryName);
    		memory= InstanceManager.memoryManagerInstance().provideMemory(memoryName);
    		if (memory == null) {
    			log.error("Memory " + memoryName
    					+ " is not available");
            } else {
                if (memory.getValue() != memoryValue) {
                	memory.setValue(memoryValue);
                } else {
                    try {
                        sendStatus(memoryName, memoryValue);
                    } catch (IOException ex) {
                        log.error("Error sending appearance", ex);
                    }
                }
            }
    	} catch (Exception ex) {
    		log.error("error setting memory value, exception: "
    				+ ex.toString());
    	}
    }


    class MemoryListener implements java.beans.PropertyChangeListener {

       MemoryListener(String memoryName) {
          name=memoryName;
          memory= InstanceManager.memoryManagerInstance().provideMemory(memoryName);
       }

       // update state as state of memory changes
       public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("value")) {
            String state =  (String) e.getNewValue();
            try {
               sendStatus(name, state);
            } catch(java.io.IOException ie) {
                  log.debug("Error sending status, removing listener from memory: " + name);
                  // if we get an error, de-register
                  memory.removePropertyChangeListener(this);
                  removeMemoryFromList(name);
            }
         }
      }

      String name = null;
      Memory memory=null;
 
    }

    protected ArrayList<String> memories = null;

    String newState = "";


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractMemoryServer.class.getName());

}
