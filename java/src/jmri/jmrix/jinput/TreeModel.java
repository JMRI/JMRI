// TreeModel.java
package jmri.jmrix.jinput;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TreeModel represents the USB controllers and components
 * <P>
 * Accessed via the instance() member, as we expect to have only one of these
 * models talking to the USB subsystem.
 * <P>
 * The tree has three levels below the uninteresting root:
 * <ol>
 * <li>USB controller
 * <li>Components (input, axis)
 * </ol>
 * <P>
 * jinput requires that there be only one of these for a given USB system in a
 * given JVM so we use a pseudo-singlet "instance" approach
 *
 * Class is final because it starts a survey thread, which runs while
 * constructor is still active.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010
 * @version	$Revision$
 */
public final class TreeModel extends DefaultTreeModel {

    /**
     *
     */
    private static final long serialVersionUID = 2231559816159123031L;

    private TreeModel() {

        super(new DefaultMutableTreeNode("Root"));
        dRoot = (DefaultMutableTreeNode) getRoot();  // this is used because we can't store the DMTN we just made during the super() call

        // load initial USB objects
        loadSystem();

        // If you don't call loadSystem, the following line was 
        // needed to get the display to start
        // insertNodeInto(new UsbNode("System", null, null), dRoot, 0);
        // start the USB gathering
        (new Runner()).start();
    }

    /**
     * Add a node to the tree if it doesn't already exist
     *
     * @param pChild  Node to possibly be inserted; relies on equals() to avoid
     *                duplicates
     * @param pParent Node for the parent of the resource to be scanned, e.g.
     *                where in the tree to insert it.
     * @return node, regardless of whether needed or not
     */
    DefaultMutableTreeNode insertNode(DefaultMutableTreeNode pChild, DefaultMutableTreeNode pParent) {
        // if already exists, just return it
        int index;
        index = getIndexOfChild(pParent, pChild);
        if (index >= 0) {
            return (DefaultMutableTreeNode) getChild(pParent, index);
        }
        // represent this one
        index = pParent.getChildCount();
        insertNodeInto(pChild, pParent, index);
        return pChild;
    }

    DefaultMutableTreeNode dRoot;

    /**
     * Provide access to the model. There's only one, because access to the USB
     * subsystem is required
     */
    static public TreeModel instance() {
        if (instanceValue == null) {
            instanceValue = new TreeModel();
        }
        return instanceValue;
    }

    static private TreeModel instanceValue = null;

    class Runner extends Thread {

        /**
         * Continually poll for events. Report any found.
         */
        @Override
        public void run() {
            while (true) {
                Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
                if (controllers.length == 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // retain if needed later
                        return;  // interrupt kills the thread
                    }
                    continue;
                }

                for (int i = 0; i < controllers.length; i++) {
                    controllers[i].poll();

                    // Now we get hold of the event queue for this device.
                    EventQueue queue = controllers[i].getEventQueue();

                    // Create an event object to pass down to get populated with the information.
                    // The underlying system may not hold the data in a JInput friendly way, 
                    // so it only gets converted when asked for.
                    Event event = new Event();

                    // Now we read from the queue until it's empty. 
                    // The 3 main things from the event are a time stamp 
                    // (it's in nanos, so it should be accurate, 
                    // but only relative to other events. 
                    // It's purpose is for knowing the order events happened in. 
                    // Then we can get the component that this event relates to, and the new value.
                    while (queue.getNextEvent(event)) {
                        Component comp = event.getComponent();
                        float value = event.getValue();

                        if (log.isDebugEnabled()) {
                            StringBuffer buffer = new StringBuffer("Name [");
                            buffer.append(controllers[i].getName());
                            buffer.append("] Component [");
                            // buffer.append(event.getNanos()).append(", ");
                            buffer.append(comp.getName()).append("] changed to ");
                            if (comp.isAnalog()) {
                                buffer.append(value);
                            } else {
                                if (value == 1.0f) {
                                    buffer.append("On");
                                } else {
                                    buffer.append("Off");
                                }
                            }
                            log.debug(new String(buffer));
                        }

                        // ensure item exits
                        new Report(controllers[i], comp, value);
                    }
                }

                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    // interrupt kills the thread
                    return;
                }
            }
        }
    }

    // we build an array of USB controllers here
    // note they might not arrive for a while
    Controller[] ca;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP") // OK until Java 1.6 allows return of cheap array copy
    public Controller[] controllers() {
        return ca;
    }

    /**
     * Carry a single event to the Swing thread for processing
     */
    class Report implements Runnable {

        Controller controller;
        Component component;
        float value;

        Report(Controller controller, Component component, float value) {
            this.controller = controller;
            this.component = component;
            this.value = value;

            SwingUtilities.invokeLater(this);
        }

        /**
         * Handle report on Swing thread to ensure tree node exists and is
         * updated
         */
        @Override
        public void run() {
            // ensure controller node exists directly under root
            String cname = controller.getName() + " [" + controller.getType().toString() + "]";
            UsbNode cNode = UsbNode.getNode(cname, controller, null);
            cNode = (UsbNode) insertNode(cNode, dRoot);

            // Device (component) node
            String dname = component.getName() + " [" + component.getIdentifier().toString() + "]";
            UsbNode dNode = UsbNode.getNode(dname, controller, component);
            dNode = (UsbNode) insertNode(dNode, cNode);

            dNode.setValue(value);

            // report change to possible listeners
            pcs.firePropertyChange("Value", dNode, new Float(value));
        }
    }

    void loadSystem() {
        // Get a list of the controllers JInput knows about and can interact with
        ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
        log.info("Found " + ca.length + " controllers");

        for (int i = 0; i < ca.length; i++) {
            // Get this controllers components (buttons and axis)
            Component[] components = ca[i].getComponents();
            log.info("Controller " + ca[i].getName() + " has " + components.length + " components");
            for (int j = 0; j < components.length; j++) {

                Controller controller = ca[i];
                Component component = components[j];

                // ensure controller node exists directly under root
                String cname = controller.getName() + " [" + controller.getType().toString() + "]";
                UsbNode cNode = UsbNode.getNode(cname, controller, null);
                cNode = (UsbNode) insertNode(cNode, dRoot);

                // Device (component) node
                String dname = component.getName() + " [" + component.getIdentifier().toString() + "]";
                UsbNode dNode = UsbNode.getNode(dname, controller, component);
                dNode = (UsbNode) insertNode(dNode, cNode);

                dNode.setValue(0.0f);
            }
        }
    }

    PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private final static Logger log = LoggerFactory.getLogger(TreeModel.class.getName());
}
