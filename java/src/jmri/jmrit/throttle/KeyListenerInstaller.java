package jmri.jmrit.throttle;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyListener;

/**
 * A Visitor class for installing a KeyListener on a container and all of its
 * subcomponents.
 *
 * @author glen
 */
public class KeyListenerInstaller {

    /**
     * Add a KeyListener to all components.
     *
     * @param k The KeyListener to add.
     * @param c The container to which all components are given this listener
     */
    public static void installKeyListenerOnAllComponents(KeyListener k, Container c) {
        c.addKeyListener(k);
        Component[] components = c.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof Container) {
                KeyListenerInstaller.installKeyListenerOnAllComponents(k, (Container) components[i]);
            } else {
                components[i].addKeyListener(k);
            }
        }
    }

}
