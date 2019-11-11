package jmri.jmrix.jinput;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import jmri.util.SystemType;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TreeModel represents the USB controllers and components
 * <p>
 * Accessed via the instance() member, as we expect to have only one of these
 * models talking to the USB subsystem.
 * <p>
 * The tree has three levels below the uninteresting root:
 * <ol>
 * <li>USB controller
 * <li>Components (input, axis)
 * </ol>
 * <p>
 * jinput requires that there be only one of these for a given USB system in a
 * given JVM so we use a pseudo-singlet "instance" approach
 * <p>
 * Class is final because it starts a survey thread, which runs while
 * constructor is still active.
 *
 * @author Bob Jacobsen Copyright 2008, 2010
 */
public final class TreeModel extends DefaultTreeModel {

    private TreeModel() {

        super(new DefaultMutableTreeNode("Root"));
        dRoot = (DefaultMutableTreeNode) getRoot();  // this is used because we can't store the DMTN we just made during the super() call

        // load initial USB objects
        boolean pass = loadSystem();
        if (!pass) {
            log.error("loadSystem failed");
        }

        // If you don't call loadSystem, the following line was
        // needed to get the display to start
        // insertNodeInto(new UsbNode("System", null, null), dRoot, 0);
        // start the USB gathering
        runner = new Runner();
        runner.setName("jinput.TreeModel loader");
        runner.start();
    }

    Runner runner;

    /**
     * Add a node to the tree if it doesn't already exist
     *
     * @param pChild  Node to possibly be inserted; relies on equals() to avoid
     *                duplicates
     * @param pParent Node for the parent of the resource to be scanned, e.g.
     *                where in the tree to insert it.
     * @return node, regardless of whether needed or not
     */
    private DefaultMutableTreeNode insertNode(DefaultMutableTreeNode pChild, DefaultMutableTreeNode pParent) {
        // if already exists, just return it
        int index;
        index = getIndexOfChild(pParent, pChild);
        if (index >= 0) {
            return (DefaultMutableTreeNode) getChild(pParent, index);
        }
        // represent this one
        index = pParent.getChildCount();
        try {
            insertNodeInto(pChild, pParent, index);
        } catch (IllegalArgumentException e) {
            log.error("insertNode({}, {}) Exception {}", pChild, pParent, e);
        }
        return pChild;
    }

    DefaultMutableTreeNode dRoot;

    /**
     * Provide access to the model. There's only one, because access to the USB
     * subsystem is required.
     *
     * @return the default instance of the TreeModel; creating it if necessary
     */
    static public TreeModel instance() {
        if (instanceValue == null) {
            instanceValue = new TreeModel();
        }
        return instanceValue;
    }

    // intended for test routines only
    public void terminateThreads() throws InterruptedException {
        if (runner == null) {
            return;
        }
        runner.interrupt();
        runner.join();
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

    public Controller[] controllers() {
        return Arrays.copyOf(ca, ca.length);
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
            try {
                cNode = (UsbNode) insertNode(cNode, dRoot);
            } catch (IllegalArgumentException e) {
                log.error("insertNode({}, {}) Exception {}", cNode, dRoot, e);
            }
            // Device (component) node
            String dname = component.getName() + " [" + component.getIdentifier().toString() + "]";
            UsbNode dNode = UsbNode.getNode(dname, controller, component);
            try {
                dNode = (UsbNode) insertNode(dNode, cNode);
            } catch (IllegalArgumentException e) {
                log.error("insertNode({}, {}) Exception {}", dNode, cNode, e);
            }

            dNode.setValue(value);

            // report change to possible listeners
            pcs.firePropertyChange("Value", dNode, Float.valueOf(value));
        }
    }

    /**
     * @return true for success
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SF_SWITCH_NO_DEFAULT",
                    justification = "This is due to a documented false-positive source")
    boolean loadSystem() {
        // Get a list of the controllers JInput knows about and can interact with
        log.debug("start looking for controllers");
        try {
            ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
            log.debug("Found " + ca.length + " controllers");
        } catch (Throwable ex) {
            log.debug("Handling Throwable", ex);
            // this is probably ClassNotFoundException, but that's not part of the interface
            if (ex instanceof ClassNotFoundException) {
                switch (SystemType.getType()) {
                    case SystemType.WINDOWS :
                        log.error("Failed to find expected library", ex);
                        //$FALL-THROUGH$
                    default:
                        log.info("Did not find an implementation of a class needed for the interface; not proceeding");
                        log.info("This is normal, because support isn't available for {}", SystemType.getOSName());
                }
            } else {
                log.error("Encountered Throwable while getting controllers", ex);
            }
            
            // could not load some component(s)
            ca = null;
            return false;
        }

        for (Controller controller : controllers()) {
            UsbNode controllerNode = null;
            UsbNode deviceNode = null;
            // Get this controllers components (buttons and axis)
            Component[] components = controller.getComponents();
            log.info("Controller " + controller.getName() + " has " + components.length + " components");
            for (Component component : components) {
                try {
                    if (controllerNode == null) {
                        // ensure controller node exists directly under root
                        String controllerName = controller.getName() + " [" + controller.getType().toString() + "]";
                        controllerNode = UsbNode.getNode(controllerName, controller, null);
                        controllerNode = (UsbNode) insertNode(controllerNode, dRoot);
                    }
                    // Device (component) node
                    String componentName = component.getName();
                    String componentIdentifierString = component.getIdentifier().toString();
                    // Skip unknown components
                    if (!componentName.equals("Unknown") && !componentIdentifierString.equals("Unknown")) {
                        String deviceName = componentName + " [" + componentIdentifierString + "]";
                        deviceNode = UsbNode.getNode(deviceName, controller, component);
                        deviceNode = (UsbNode) insertNode(deviceNode, controllerNode);
                        deviceNode.setValue(0.0f);
                    }
                } catch (IllegalStateException e) {
                    // node does not allow children
                    break;  // skip this controller
                } catch (IllegalArgumentException e) {
                    // ignore components that throw IllegalArgumentExceptions
                    log.error("insertNode({}, {}) Exception {}", deviceNode, controllerNode, e);
                } catch (Exception e) {
                    // log all others
                    log.error("Exception " + e);
                }
            }
        }
        return true;
    }

    PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private final static Logger log = LoggerFactory.getLogger(TreeModel.class);
}
