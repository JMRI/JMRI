package jmri.jmrix.oaktree;

import java.util.Comparator;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.util.NamedBeanComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimum required SystemConnectionMemo.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class OakTreeSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    public OakTreeSystemConnectionMemo(@Nonnull String prefix, @Nonnull String userName) {
        super(prefix, userName);
        InstanceManager.store(this, OakTreeSystemConnectionMemo.class);

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
     * @return traffic controller, new instance created if null.
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
        register();
    }

    /**
     * Provide access to the SensorManager for this particular connection.
     * <p>
     * NOTE: SensorManager defaults to NULL
     * @return sensor manager.
     */
    public SensorManager getSensorManager() {
        return get(SensorManager.class);
    }

    public void setSensorManager(SerialSensorManager s) {
        store(s,SensorManager.class);
        getTrafficController().setSensorManager(s);
    }

    /**
     * Provide access to the TurnoutManager for this particular connection.
     * <p>
     * NOTE: TurnoutManager defaults to NULL
     * @return turnout manager.
     */
    public TurnoutManager getTurnoutManager() {
        return get(TurnoutManager.class);

    }

    public void setTurnoutManager(SerialTurnoutManager t) {
        store(t,TurnoutManager.class);
    }

    /**
     * Provide access to the LightManager for this particular connection.
     * <p>
     * NOTE: LightManager defaults to NULL
     * @return light manager.
     */
    public LightManager getLightManager() {
        return get(LightManager.class);

    }

    public void setLightManager(SerialLightManager l) {
        store(l,LightManager.class);
    }

    private final static Logger log = LoggerFactory.getLogger(OakTreeSystemConnectionMemo.class);

}
