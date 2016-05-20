// ProxySensorManager.java

package jmri.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Sensor;
import jmri.SensorManager;

/**
 * Implementation of a SensorManager that can serves as a proxy
 * for multiple system-specific implementations. 
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2010
 * @version	$Revision$
 */
public class ProxySensorManager extends AbstractProxyManager
                            implements SensorManager {

    public ProxySensorManager() {
        super();
    }
    
    protected AbstractManager makeInternalManager() {
        return new InternalSensorManager();
    }

    /**
     * Locate via user name, then system name if needed.
     *
     * @param name
     * @return Null if nothing by that name exists
     */
    public Sensor getSensor(String name) {
        return (Sensor)super.getNamedBean(name);
    }

    protected Sensor makeBean(int i, String systemName, String userName) {
        return ((SensorManager)getMgr(i)).newSensor(systemName, userName);
    }

    public Sensor provideSensor(String sName) {
        return (Sensor) super.provideNamedBean(sName);
    }



    /**
     * Locate an instance based on a system name.  Returns null if no
     * instance already exists.
     * @return requested Turnout object or null if none exists
     */
    public Sensor getBySystemName(String sName) {
        return (Sensor) super.getBeanBySystemName(sName);
    }

    /**
     * Locate an instance based on a user name.  Returns null if no
     * instance already exists.
     * @return requested Turnout object or null if none exists
     */
    public Sensor getByUserName(String userName) {
        return (Sensor) super.getBeanByUserName(userName);
    }


    /**
     * Return an instance with the specified system and user names.
     * Note that two calls with the same arguments will get the same instance;
     * there is only one Sensor object representing a given physical turnout
     * and therefore only one with a specific system or user name.
     *<P>
     * This will always return a valid object reference for a valid request;
     * a new object will be
     * created if necessary. In that case:
     *<UL>
     *<LI>If a null reference is given for user name, no user name will be associated
     *    with the Turnout object created; a valid system name must be provided
     *<LI>If a null reference is given for the system name, a system name
     *    will _somehow_ be inferred from the user name.  How this is done
     *    is system specific.  Note: a future extension of this interface
     *    will add an exception to signal that this was not possible.
     *<LI>If both names are provided, the system name defines the
     *    hardware access of the desired turnout, and the user address
     *    is associated with it.
     *</UL>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects.  This is a problem, and we don't have a
     * good solution except to issue warnings.
     * This will mostly happen if you're creating Sensors when you should
     * be looking them up.
     * @return requested Sensor object (never null)
     */
    public Sensor newSensor(String systemName, String userName) {
        return (Sensor) newNamedBean(systemName, userName);
    }

	// null implementation to satisfy the SensorManager interface
	public void updateAll() {  }
    
    public boolean allowMultipleAdditions(String systemName) {
        int i = matchTentative(systemName);
        if (i >= 0)
            return ((SensorManager)getMgr(i)).allowMultipleAdditions(systemName);
        return ((SensorManager)getMgr(0)).allowMultipleAdditions(systemName);
    }
    
    public String createSystemName(String curAddress, String prefix) throws jmri.JmriException{
        for (int i=0; i<nMgrs(); i++) {
            if ( prefix.equals(
                    ((SensorManager)getMgr(i)).getSystemPrefix()) ) {
                try {
                    return ((SensorManager)getMgr(i)).createSystemName(curAddress, prefix);
                } catch (jmri.JmriException ex) {
                    log.error(ex.toString());
                    throw ex;
                }
            }
        }
        throw new jmri.JmriException("Sensor Manager could not be found for System Prefix " + prefix);
    }
    
    public String getNextValidAddress(String curAddress, String prefix) throws jmri.JmriException{
        for (int i=0; i<nMgrs(); i++) {
            if ( prefix.equals( 
                    ((SensorManager)getMgr(i)).getSystemPrefix()) ) {
                try {
                    return ((SensorManager)getMgr(i)).getNextValidAddress(curAddress, prefix);
                } catch (jmri.JmriException ex) {
                    throw ex;
                }
            }
        }
        return null;
    }
    
    public long getDefaultSensorDebounceGoingActive(){
        return ((SensorManager)getMgr(0)).getDefaultSensorDebounceGoingActive();
    }
    public long getDefaultSensorDebounceGoingInActive(){
        return ((SensorManager)getMgr(0)).getDefaultSensorDebounceGoingInActive();
    }
    
    public void setDefaultSensorDebounceGoingActive(long timer){
        for (int i=0; i<nMgrs(); i++) {
            ((SensorManager)getMgr(i)).setDefaultSensorDebounceGoingActive(timer);
        }
    }
    
    public void setDefaultSensorDebounceGoingInActive(long timer){
        for (int i=0; i<nMgrs(); i++) {
            ((SensorManager)getMgr(i)).setDefaultSensorDebounceGoingInActive(timer);
        }
    }
    
    public int getXMLOrder(){
        return jmri.Manager.SENSORS;
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(ProxySensorManager.class.getName());
}

/* @(#)ProxySensorManager.java */
