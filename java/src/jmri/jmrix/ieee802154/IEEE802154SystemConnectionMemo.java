package jmri.jmrix.ieee802154;

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
 * @author Bob Jacobsen Copyright (C) 2010 copied from NCE into PowerLine for
 * multiple connections by
 * @author Ken Cameron Copyright (C) 2011 copied from PowerLine into IEEE802154
 * by
 * @author Paul Bender Copyright (C) 2013
 */
public class IEEE802154SystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public IEEE802154SystemConnectionMemo() {
        this("Z", "IEEE802.15.4");
    }

    public IEEE802154SystemConnectionMemo(String prefix, String userName) {
        super(prefix, userName);
        register(); // registers general type
        InstanceManager.store(this, IEEE802154SystemConnectionMemo.class); // also register as specific type
        init();
    }

    /*
     * Override the init function for any subtype specific
     * registration into init.  init is called by the generic contstructor.
     */
    protected void init() {
        // create and register the ComponentFactory
        InstanceManager.store(componentFactory = new jmri.jmrix.ieee802154.swing.IEEE802154ComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

    }

    jmri.jmrix.swing.ComponentFactory componentFactory = null;

    /**
     * Traffic Controller for this instance.
     * @param newtc tc to save for connection
     */
    public void setTrafficController(IEEE802154TrafficController newtc) {
        _tc = newtc;
    }

    public IEEE802154TrafficController getTrafficController() {
        return _tc;
    }
    private IEEE802154TrafficController _tc = null;

    /**
     * Tells which managers this class provides.
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        return false; // nothing, by default
    }

    /**
     * Provide manager by class
     */
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        return null; // nothing, by default
    }

    /**
     * Configure the common managers for IEEE802154 connections. This puts the
     * common manager config in one place.
     */
    public void configureManagers() {
        // now does nothing here, it's done by the specific class
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.ieee802154.IEEE802154ActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, IEEE802154SystemConnectionMemo.class);
        super.dispose();
    }

}
