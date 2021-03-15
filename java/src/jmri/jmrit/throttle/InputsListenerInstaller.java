package jmri.jmrit.throttle;

import java.awt.Component;
import java.awt.Container;

/**
 * A Visitor class for installing a KeyListener on a container and all of its
 * subcomponents.
 *
 * @author glen
 */
public class InputsListenerInstaller {

    /**
     * Add a KeyListener to all components.
     *
     * @param l The ThrottleWindowInputsListener to add.
     * @param c The Container to which all components are given this listener
     */
    public static void installInputsListenerOnAllComponents(ThrottleWindowInputsListener l, Container c) {
        c.addKeyListener(l);
        c.addMouseWheelListener(l);
        Component[] components = c.getComponents();
        for (Component component : components) {
            if (component instanceof Container) {
                InputsListenerInstaller.installInputsListenerOnAllComponents(l, (Container) component);
            } else {
                component.addKeyListener(l);
                component.addMouseWheelListener(l);
            }
        }
    }
    
}
