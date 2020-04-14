package jmri.jmrix.bachrus;

import java.util.Comparator;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.util.NamedBeanComparator;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class SpeedoSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public SpeedoSystemConnectionMemo(SpeedoTrafficController tc) {
        super("A", "Speedo");
        this.tc = tc;
        register();
        InstanceManager.store(cf = new jmri.jmrix.bachrus.swing.SpeedoComponentFactory(this), 
         jmri.jmrix.swing.ComponentFactory.class);
    }

    public SpeedoSystemConnectionMemo() {
        super("A", "Speedo");
        register(); // registers general type
        InstanceManager.store(this, SpeedoSystemConnectionMemo.class); // also register as specific type
        //Needs to be implemented
        InstanceManager.store(cf = new jmri.jmrix.bachrus.swing.SpeedoComponentFactory(this), 
         jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provide access to the TrafficController for this particular connection.
     */
    public SpeedoTrafficController getTrafficController() {
        return tc;
    }

    public void setSpeedoTrafficController(SpeedoTrafficController tc) {
        this.tc = tc;
    }
    private SpeedoTrafficController tc;

    /**
     * Configure the common managers for Internal connections. This puts the
     * common manager config in one place. This method is static so that it can
     * be referenced from classes that don't inherit.
     */
    public void configureManagers() {
        // None to configure
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        // No Actions at start up to return
        return null;
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        tc = null;
        InstanceManager.deregister(this, SpeedoSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

}
