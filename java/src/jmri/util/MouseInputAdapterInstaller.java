package jmri.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.event.MouseInputAdapter;

/**
 * A Visitor class for installing a MouseInputAdapter on a container and all of
 * its subcomponents.
 *
 * This class is based on the KeyListenerInstaller class.
 *
 * @author Paul Bender Copyright 2005
 */
public class MouseInputAdapterInstaller {

    /**
     * Add a MouseInputAdapter to all components.
     *
     * @param m The MouseInputAdapter to add.
     * @param c The container to which all components are given this listener
     */
    public static void installMouseInputAdapterOnAllComponents(MouseInputAdapter m, Container c) {
        c.addMouseListener(m);
        c.addMouseMotionListener(m);
        Component[] components = c.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof Container) {
                MouseInputAdapterInstaller.installMouseInputAdapterOnAllComponents(m, (Container) components[i]);
            } else {
                c.addMouseListener(m);
                c.addMouseMotionListener(m);
            }
        }
    }

    /**
     * Add a MouseListener to all components.
     *
     * @param m The MouseListener to add.
     * @param c The container to which all components are given this listener
     */
    public static void installMouseListenerOnAllComponents(MouseListener m, Container c) {
        c.addMouseListener(m);
        Component[] components = c.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof Container) {
                MouseInputAdapterInstaller.installMouseListenerOnAllComponents(m, (Container) components[i]);
            } else {
                c.addMouseListener(m);
            }
        }
    }

    /**
     * Add a MouseMotionListener to all components.
     *
     * @param m The MouseMotionListener to add.
     * @param c The container to which all components are given this listener
     */
    public static void installMouseMotionListenerOnAllComponents(MouseMotionListener m, Container c) {
        c.addMouseMotionListener(m);
        Component[] components = c.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof Container) {
                MouseInputAdapterInstaller.installMouseMotionListenerOnAllComponents(m, (Container) components[i]);
            } else {
                c.addMouseMotionListener(m);
            }
        }
    }

}
