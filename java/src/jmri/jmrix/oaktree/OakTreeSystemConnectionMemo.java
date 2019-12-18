package jmri.jmrix.oaktree;

import java.util.Comparator;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.NamedBean;
import jmri.SensorManager;
import jmri.TurnoutManager;
import jmri.jmrix.SystemConnectionMemo;
import jmri.util.NamedBeanComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimum required SystemConnectionMemo.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class OakTreeSystemConnectionMemo extends SystemConnectionMemo {

    public OakTreeSystemConnectionMemo(@Nonnull String prefix, @Nonnull String userName) {
        super(prefix, userName);
        register(); // registers general type
        InstanceManager.store(this, OakTreeSystemConnectionMemo.class); // also register as specific type

        // create and register the ComponentFactory
        InstanceManager.store(new jmri.jmrix.oaktree.swing.OakTreeComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created OakTreeSystemConnectionMemo");
    }

    public OakTreeSystemConnectionMemo() {
        this("O", SerialConnectionTypeList.OAK);
        log.debug("Created nameless OakTreeSystemConnectionMemo");
    }

    private SerialTrafficController tc = null;

    /**
     * Set the traffic controller instance associated with this connection memo.
     *
     * @param s jmri.jmrix.oaktree.SerialTrafficController object to use.
     */
    public void setTrafficController(SerialTrafficController s){
        tc = s;
    }

    /**
     * Get the traffic controller instance associated with this connection memo.
     */
    public SerialTrafficController getTrafficController() {
        if (tc == null) {
            setTrafficController(new SerialTrafficController(this));
            log.debug("Auto create of OakTree SerialTrafficController for initial configuration");
        }
        return tc;
    }
    // would more stuff for traffic controller etc be useful here?

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    public void configureManagers(){
        setTurnoutManager(new SerialTurnoutManager(this));
        InstanceManager.setTurnoutManager(getTurnoutManager());

        setLightManager(new SerialLightManager(this));
        InstanceManager.setLightManager(getLightManager());

        setSensorManager(new SerialSensorManager(this));
        InstanceManager.setSensorManager(getSensorManager());
    }

    /**
     * Provide access to the SensorManager for this particular connection.
     * <p>
     * NOTE: SensorManager defaults to NULL
     */
    public SensorManager getSensorManager() {
        return sensorManager;
    }

    public void setSensorManager(SerialSensorManager s) {
        sensorManager = s;
        getTrafficController().setSensorManager(s);
    }

    private SensorManager sensorManager = null;


    /**
     * Provide access to the TurnoutManager for this particular connection.
     * <p>
     * NOTE: TurnoutManager defaults to NULL
     */
    public TurnoutManager getTurnoutManager() {
        return turnoutManager;

    }

    public void setTurnoutManager(SerialTurnoutManager t) {
        turnoutManager = t;
    }

    private TurnoutManager turnoutManager = null;

    /**
     * Provide access to the LightManager for this particular connection.
     * <p>
     * NOTE: LightManager defaults to NULL
     */
    public LightManager getLightManager() {
        return lightManager;

    }

    public void setLightManager(SerialLightManager l) {
        lightManager = l;
    }

    private LightManager lightManager = null;

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
        }
        return super.provides(type);
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
        return super.get(T);
    }


    private final static Logger log = LoggerFactory.getLogger(OakTreeSystemConnectionMemo.class);

}
