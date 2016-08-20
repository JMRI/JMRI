package jmri.jmrix.cmri;

import java.util.ResourceBundle;
import jmri.jmrix.SystemConnectionMemo;
import jmri.InstanceManager;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.LightManager;
import jmri.SensorManager;
import jmri.TurnoutManager;

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

        // create and register the CMRIComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.cmri.swing.CMRIComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);


    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.cmri.CmriActionListBundle");
    }

    public void dispose() {
        InstanceManager.deregister(this, CMRISystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

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
       return tc;
    }

    /*
     * Provides access to the Sensor Manager for this particular connection.
     * NOTE: Sensor manager defaults to NULL
     */
    public SensorManager getSensorManager() {
        return sensorManager;

    }

    public void setSensorManager(SensorManager s) {
        sensorManager = s;
    }

    private SensorManager sensorManager = null;

    /*
     * Provides access to the Turnout Manager for this particular connection.
     * NOTE: Turnout manager defaults to NULL
     */
    public TurnoutManager getTurnoutManager() {
        return turnoutManager;

    }

    public void setTurnoutManager(TurnoutManager t) {
        turnoutManager = t;
    }

    private TurnoutManager turnoutManager = null;

    /*
     * Provides access to the Light Manager for this particular connection.
     * NOTE: Light manager defaults to NULL
     */
    public LightManager getLightManager() {
        return lightManager;
    }

    public void setLightManager(LightManager l) {
        lightManager = l;
    }

    private LightManager lightManager = null;

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
       jmri.jmrix.cmri.serial.SerialTurnoutManager t = new jmri.jmrix.cmri.serial.SerialTurnoutManager(this);
        jmri.InstanceManager.setTurnoutManager(t);
        setTurnoutManager(t);

        jmri.jmrix.cmri.serial.SerialLightManager l = new jmri.jmrix.cmri.serial.SerialLightManager(this);
        jmri.InstanceManager.setLightManager(l);
        setLightManager(l);

        jmri.jmrix.cmri.serial.SerialSensorManager s = new jmri.jmrix.cmri.serial.SerialSensorManager(this);
        jmri.InstanceManager.setSensorManager(s);
        tc.setSensorManager(s);
        setSensorManager(s);

    }


}
