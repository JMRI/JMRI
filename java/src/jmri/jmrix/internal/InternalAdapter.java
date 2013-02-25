// InternalDriverAdapter.java

package jmri.jmrix.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a dummy Adapter to allow the system connection memo and multiple
 * Internal managers to be handled.
 * <P>
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision$
 */
public class InternalAdapter extends jmri.jmrix.AbstractSerialPortController
    implements jmri.jmrix.PortAdapter{

	// private control members
	private boolean opened = false;
    
    public InternalAdapter (){
        super();
        opened = true;
        adaptermemo = new InternalSystemConnectionMemo();
    }
    
    InternalSystemConnectionMemo adaptermemo;

    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
    }
	
	public String openPort(String portName, String appName) {
        return "true";
	}

	public void configure() {
        adaptermemo.configureManagers();
        
	}

	public boolean status() {
		return opened;
	}

	/**
	 * Get an array of valid baud rates.
	 */
	public String[] validBaudRates() {
		log.debug("validBaudRates should not have been invoked");
		return null;
	}

	public String getCurrentBaudRate() {
		return "";
	}

    
    public java.io.DataInputStream getInputStream() {
        return null;
    }

    public java.io.DataOutputStream getOutputStream() {
     	return null;
    }
    
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }
    
    @Override
    public jmri.jmrix.SystemConnectionMemo getSystemConnectionMemo() { 
    	return adaptermemo; 
    }

    String manufacturerName = jmri.jmrix.DCCManufacturerList.NONE;
    public String getManufacturer() { return manufacturerName; }
    public void setManufacturer(String manu) { manufacturerName=manu; }
    
    public void recover(){
    
    }
    
	static Logger log = LoggerFactory
			.getLogger(InternalAdapter.class.getName());

}
