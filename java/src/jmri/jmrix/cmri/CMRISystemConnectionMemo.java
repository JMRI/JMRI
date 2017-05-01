package jmri.jmrix.cmri;

import java.util.ResourceBundle;
import jmri.jmrix.SystemConnectionMemo;
import jmri.InstanceManager;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.LightManager;
import jmri.SensorManager;
import jmri.TurnoutManager;
import jmri.jmrix.cmri.serial.*;

/**
 * Minimal SystemConnectionMemo for C/MRI systems.
 *
 * @author Randall Wood
 */
public class CMRISystemConnectionMemo extends SystemConnectionMemo {

    private SerialTrafficController tc = null;

    public CMRISystemConnectionMemo() {
        super("C", CMRIConnectionTypeList.CMRI);
        register(); // registers general type
        jmri.InstanceManager.store(this, CMRISystemConnectionMemo.class); // also register as specific type

        // create and register the ComponentFactory for the GUI
        InstanceManager.store(cf = new jmri.jmrix.cmri.swing.CMRIComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /*
     * Set the traffic controller instance associated with this connection memo.
     * <p>
     * @param s jmri.jmrix.cmri.serial.SerialTrafficController object to use.
     */
    public void setTrafficController(SerialTrafficController s){
        tc = s;
    }

    /*
     * Get the traffic controller instance associated with this connection memo.
     */
    public SerialTrafficController  getTrafficController(){
        if (tc == null) {
            setTrafficController(new SerialTrafficController());
            log.debug("Auto create of SerialTrafficController for initial configuration");
        }
        return tc;
    }

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        } else if (type.equals(jmri.SensorManager.class)) {
            return true;
        } else if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        } else if (type.equals(jmri.LightManager.class)) {
            return true;
        } else {
            return false; // nothing, by default
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (T.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (T.equals(jmri.LightManager.class)) {
            return (T) getLightManager();
        }
        return null; // nothing by default
    }

    /**
     * Configure the common managers for CMRI connections. This puts the
     * common manager config in one place.
     */
    public void configureManagers() {
        InstanceManager.setSensorManager(
                getSensorManager());
        getTrafficController().setSensorManager(getSensorManager());

        InstanceManager.setTurnoutManager(
                getTurnoutManager());

        InstanceManager.setLightManager(
                getLightManager());
    }


    protected SerialTurnoutManager turnoutManager;

    public SerialTurnoutManager getTurnoutManager() {
        if (getDisabled()) {
            return null;
        }
        if (turnoutManager == null) {
            turnoutManager = new SerialTurnoutManager(this);
        }
        return turnoutManager;
    }

    protected SerialSensorManager sensorManager;

    public SerialSensorManager getSensorManager() {
        if (getDisabled()) {
            return null;
        }
        if (sensorManager == null) {
            sensorManager = new SerialSensorManager(this);
        }
        return sensorManager;
    }

    protected SerialLightManager lightManager;

    public SerialLightManager getLightManager() {
        if (getDisabled()) {
            return null;
        }
        if (lightManager == null) {
            lightManager = new SerialLightManager(this);
        }
        return lightManager;
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.cmri.CmriActionListBundle");
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, CMRISystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        if (turnoutManager != null) {
            InstanceManager.deregister(turnoutManager, jmri.jmrix.cmri.serial.SerialTurnoutManager.class);
        }
        if (lightManager != null) {
            InstanceManager.deregister(lightManager, jmri.jmrix.cmri.serial.SerialLightManager.class);
        }
        if (sensorManager != null) {
            InstanceManager.deregister(sensorManager, jmri.jmrix.cmri.serial.SerialSensorManager.class);
        }
        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CMRISystemConnectionMemo.class.getName());
}
