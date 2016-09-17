package jmri.jmrix.acela;

import java.util.ResourceBundle;
import jmri.InstanceManager;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class AcelaSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public AcelaSystemConnectionMemo(AcelaTrafficController tc) {
        super("A", "Acela");
        this.tc = tc;
        register();
        // create and register the AcelaComponetFactory
        InstanceManager.store(cf = new jmri.jmrix.acela.swing.AcelaComponentFactory(this), 
         jmri.jmrix.swing.ComponentFactory.class);
    }

    public AcelaSystemConnectionMemo() {
        super("A", "Acela");
        register(); // registers general type
        InstanceManager.store(this, AcelaSystemConnectionMemo.class); // also register as specific type
        // create and register the AcelaComponetFactory
        InstanceManager.store(cf = new jmri.jmrix.acela.swing.AcelaComponentFactory(this), 
         jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     */
    public AcelaTrafficController getTrafficController() {
        return tc;
    }

    public void setAcelaTrafficController(AcelaTrafficController tc) {
        this.tc = tc;
    }
    private AcelaTrafficController tc;

    /**
     * Configure the common managers for Internal connections. This puts the
     * common manager config in one place. This method is static so that it can
     * be referenced from classes that don't inherit.
     */
    public void configureManagers() {

        jmri.InstanceManager.setLightManager(new AcelaLightManager(this));

        AcelaSensorManager s;
        jmri.InstanceManager.setSensorManager(s = new AcelaSensorManager(this));
        tc.setSensorManager(s);

        AcelaTurnoutManager t;
        jmri.InstanceManager.setTurnoutManager(t = new AcelaTurnoutManager(this));
        AcelaTrafficController.instance().setTurnoutManager(t);
    }

    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.acela.AcelaActionListBundle");
    }

    public void dispose() {
        tc = null;
        InstanceManager.deregister(this, AcelaSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }
}
