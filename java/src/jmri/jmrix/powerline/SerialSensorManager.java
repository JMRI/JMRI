// SerialSensorManager.java

package jmri.jmrix.powerline;

import jmri.Sensor;

/**
 * Manage the system-specific Sensor implementation.
 * <P>
 * System names are:
 * 		Powerline - "PSann", where a is the house code and nn is the unit number without padding.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @author			Ken Cameron, (C) 2009, sensors from poll replies
 * Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * @version			$Revision$
 */
abstract public class SerialSensorManager extends jmri.managers.AbstractSensorManager implements SerialListener {

	SerialTrafficController tc = null;
	
    public SerialSensorManager(SerialTrafficController tc) {
        super();
        this.tc = tc;
        tc.addSerialListener(this);
    }

	/**
     * Return the system letter
     */
    public String getSystemPrefix() { return tc.getAdapterMemo().getSystemPrefix(); }

    // to free resources when no longer used
    public void dispose() {
        super.dispose();
    }

    /**
     * Create a new sensor if all checks are passed
     *    System name is normalized to ensure uniqueness.
     */
    public Sensor createNewSensor(String systemName, String userName) {
        Sensor s;
        // validate the system name, and normalize it
        String sName = tc.getAdapterMemo().getSerialAddress().normalizeSystemName(systemName);
        if (sName=="") {
            // system name is not valid
            log.error("Invalid Sensor system name - "+systemName);
            return null;
        }
        // does this Sensor already exist
        s = getBySystemName(sName);
        if (s!=null) {
            log.error("Sensor with this name already exists - "+systemName);
            return null;
        }
        // Sensor system name is valid and Sensor doesn't exist, make a new one
        if (userName == null)
            s = new SerialSensor(sName, tc);
        else
            s = new SerialSensor(sName, tc, userName);

        return s;
    }
    
    /**
     * Dummy routine
     */
    public void message(SerialMessage r) {
    	// this happens during some polls from sensor messages
        //log.warn("unexpected message");
    }

    /**
     *  Process a reply to a poll of Sensors of one node
     */
    abstract public void reply(SerialReply r);
        
    public boolean allowMultipleAdditions(String systemName) { return false;  }
    
    public String getNextValidAddress(String curAddress, String prefix){
        
        //If the hardware address past does not already exist then this can
        //be considered the next valid address.
        Sensor s = getBySystemName(prefix + typeLetter() + curAddress);
        if(s==null){
            return curAddress;
        }
        
        // This bit deals with handling the curAddress, and how to get the next address.
        int iName = 0;
        //Address starts with a single letter called a house code.
        String houseCode = curAddress.substring(0,1);
        try {
            iName = Integer.parseInt(curAddress.substring(1));
        } catch (NumberFormatException ex) {
            log.error("Unable to convert " + curAddress + " Hardware Address to a number");
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                                showInfoMessage("Error","Unable to convert " + curAddress + " to a valid Hardware Address",""+ex, "",true, false, org.apache.log4j.Level.ERROR);
            return null;
        }
        
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        s = getBySystemName(prefix+typeLetter()+curAddress);
        if(s!=null){
            for(int x = 1; x<10; x++){
                iName++;
                s = getBySystemName(prefix+typeLetter()+houseCode+(iName));
                if(s==null)
                    return houseCode+iName;
            }
            return null;
        } else {
            return houseCode+iName;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialSensorManager.class.getName());
}

/* @(#)SerialSensorManager.java */
