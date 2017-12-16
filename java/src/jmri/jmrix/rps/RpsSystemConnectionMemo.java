package jmri.jmrix.rps;

import java.util.ResourceBundle;
import jmri.jmrix.SystemConnectionMemo;
import jmri.InstanceManager;

/**
 * Minimal implementation of SystemConnectionMemo.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class RpsSystemConnectionMemo extends SystemConnectionMemo {

    jmri.jmrix.swing.ComponentFactory cf = null;

    public RpsSystemConnectionMemo() {
        super("R", "RPS");

        // create and register the XNetComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.rps.swing.RpsComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

}
