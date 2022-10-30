package jmri.jmrix.acela;

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
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class AcelaSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    public AcelaSystemConnectionMemo() {
        this("A", AcelaConnectionTypeList.CTI); // default to A
    }

    public AcelaSystemConnectionMemo(@Nonnull String prefix, @Nonnull String userName) {
        super(prefix, userName);

        InstanceManager.store(this, AcelaSystemConnectionMemo.class);

        // create and register the AcelaComponentFactory for the GUI
        InstanceManager.store(cf = new jmri.jmrix.acela.swing.AcelaComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
        log.debug("Created AcelaSystemConnectionMemo");
    }

    public AcelaSystemConnectionMemo(AcelaTrafficController tc) {
        super("A", AcelaConnectionTypeList.CTI); // default to A
        this.tc = tc;

        InstanceManager.store(this, AcelaSystemConnectionMemo.class);

        // create and register the AcelaComponentFactory for the GUI
        InstanceManager.store(cf = new jmri.jmrix.acela.swing.AcelaComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
        log.debug("Created AcelaSystemConnectionMemo");
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     * @return traffic controller, provided if null.
     */
    public AcelaTrafficController getTrafficController() {
        if (tc == null) {
            setAcelaTrafficController(new AcelaTrafficController());
            log.debug("Auto create of AcelaTrafficController for initial configuration");
        }
        return tc;
    }

    /**
     * Set the traffic controller instance associated with this connection memo.
     *
     * @param tc jmri.jmrix.acela.AcelaTrafficController object to use
     */
    public void setAcelaTrafficController(AcelaTrafficController tc) {
        this.tc = tc;
    }

    private AcelaTrafficController tc;

    /**
     * Configure the common managers for Acela connections. This puts the
     * common manager config in one place.
     */
    public void configureManagers() {

        InstanceManager.setLightManager(getLightManager());

        InstanceManager.setSensorManager(getSensorManager());
        getTrafficController().setSensorManager(getSensorManager());

        InstanceManager.setTurnoutManager(getTurnoutManager());
        getTrafficController().setTurnoutManager(getTurnoutManager());

        register(); // registers general type
    }

    public AcelaTurnoutManager getTurnoutManager() {
        if (getDisabled()) {
            return null;
        }
        return (AcelaTurnoutManager) classObjectMap.computeIfAbsent(TurnoutManager.class, (Class<?> c) -> new AcelaTurnoutManager(this));
    }

    public AcelaSensorManager getSensorManager() {
        if (getDisabled()) {
            return null;
        }
        return (AcelaSensorManager) classObjectMap.computeIfAbsent(SensorManager.class,(Class<?> c) -> new AcelaSensorManager(this));
    }

    public AcelaLightManager getLightManager() {
        if (getDisabled()) {
            return null;
        }
        return (AcelaLightManager) classObjectMap.computeIfAbsent(LightManager.class,(Class<?> c) -> new AcelaLightManager(this));
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.acela.AcelaActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        tc = null;
        InstanceManager.deregister(this, AcelaSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaSystemConnectionMemo.class);

}
