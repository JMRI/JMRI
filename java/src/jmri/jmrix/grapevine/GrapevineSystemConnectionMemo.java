package jmri.jmrix.grapevine;

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
 * Minimum required SystemConnectionMemo for Grapevine.
 * Expanded for multichar/multiconnection support. Nodes are handled bij superclass.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class GrapevineSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    public GrapevineSystemConnectionMemo() {
        this("G", Bundle.getMessage("MenuSystem"));
    }

    public GrapevineSystemConnectionMemo(@Nonnull String prefix, @Nonnull String name) {
        super(prefix, name);

        InstanceManager.store(this, GrapevineSystemConnectionMemo.class);

        // create and register the ComponentFactory for the GUI (menu)
        InstanceManager.store(cf = new jmri.jmrix.grapevine.swing.GrapevineComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created GrapevineSystemConnectionMemo, prefix = {}", prefix);
    }

    private SerialTrafficController tc = null;
    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Set the traffic controller instance associated with this connection memo.
     *
     * @param tc jmri.jmrix.grapevine.SerialTrafficController object to use.
     */
    public void setTrafficController(SerialTrafficController tc){
        this.tc = tc;
        log.debug("Memo {} set GrapevineTrafficController {}", getUserName(), tc);
    }

    /**
     * Get the traffic controller instance associated with this connection memo.
     * @return traffic controller, one is provided if null.
     */
    public SerialTrafficController getTrafficController(){
        if (tc == null) {
            setTrafficController(new SerialTrafficController(this));
            log.debug("Auto create of SerialTrafficController for initial configuration");
        }
        return tc;
    }

    /**
     * Provide Grapevine menu strings.
     *
     * @return bundle file containing action - menuitem pairs
     */
    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.grapevine.GrapevineActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    /**
    * Configure the common managers for Grapevine connections. This puts the
    * common manager config in one place.
    */
    public void configureManagers() {
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
        log.debug(get(SensorManager.class) != null ? "getSensorManager OK": "getSensorManager returned NULL");
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
        log.debug(get(TurnoutManager.class) != null ? "getTurnoutManager OK": "getTurnoutManager returned NULL");
        return get(TurnoutManager.class);
    }

    public void setTurnoutManager(SerialTurnoutManager t) {
        store(t,TurnoutManager.class);
        // not accessible, needed?
    }

    /**
     * Provide access to the LightManager for this particular connection.
     * <p>
     * NOTE: LightManager defaults to NULL
     * @return light manager.
     */
    public LightManager getLightManager() {
        log.debug(get(LightManager.class) != null ? "getLightManager OK": "getLightManager returned NULL");
        return get(LightManager.class);

    }

    public void setLightManager(SerialLightManager l) {
        store(l,LightManager.class);
        // not accessible, needed?
    }

    @Override
    public void dispose() {
        tc = null;
        InstanceManager.deregister(this, GrapevineSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(GrapevineSystemConnectionMemo.class);

}
