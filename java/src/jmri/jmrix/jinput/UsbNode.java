package jmri.jmrix.jinput;

import java.util.HashMap;
import javax.swing.tree.DefaultMutableTreeNode;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UsbNode represents the USB controllers or component.
 * <p>
 * Generally accessed via the TreeModel.
 * <p>
 * Can be connected to a JMRI Sensor or Memory.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class UsbNode extends DefaultMutableTreeNode {

    String name;
    Controller controller;
    Component component;

    UsbNode(String name, Controller controller, Component component) {
        super(name);
        this.name = name;
        this.controller = controller;
        this.component = component;
    }

    @Override
    public int hashCode() {
        if (controller == null) {
            return super.hashCode();
        } else {
            return controller.hashCode();
        }
    }

    public Controller getController() {
        return controller;
    }

    public Component getComponent() {
        return component;
    }

    @Override
    public boolean equals(Object a) {
        boolean result = false;
        if ((a != null) && (a instanceof UsbNode)) {
            UsbNode usbNode = (UsbNode) a;
            result = (name.equals(usbNode.name)
                    && (controller == usbNode.controller)
                    && (component == usbNode.component));
        }
        return result;
    }

    public void setValue(float val) {
        this.val = val;
        // if attached, set value
        try {
            if ((attachedSensor != null) && (!attachedSensor.equals(""))) {
                InstanceManager.sensorManagerInstance()
                        .provideSensor(attachedSensor).setKnownState(
                        val > 0.0 ? Sensor.ACTIVE : Sensor.INACTIVE);
            }
        } catch (IllegalArgumentException | JmriException e1) {
            log.error("Can't set sensor: " + e1);
        }
        try {
            if ((attachedMemory != null) && (!attachedMemory.equals(""))) {
                InstanceManager.memoryManagerInstance()
                        .provideMemory(attachedMemory).setValue("" + val);
            }
        } catch (IllegalArgumentException e2) {
            log.error("Can't set memory: " + e2);
        }
    }

    public float getValue() {
        return val;
    }
    float val = -1;

    String attachedSensor = null;

    public void setAttachedSensor(String sensor) {
        attachedSensor = sensor;
    }

    public String getAttachedSensor() {
        return attachedSensor;
    }

    String attachedMemory = null;

    public void setAttachedMemory(String memory) {
        attachedMemory = memory;
    }

    public String getAttachedMemory() {
        return attachedMemory;
    }

    public String getName() {
        return name;
    }

    /**
     * Get a specific node. This is used instead of a constructor to ensure that
     * node objects for a given USB object are unique.
     *
     * @param name       the node name
     * @param controller the input controller
     * @param component  the input component
     * @return the node, either an existing node with the same controller or
     *         component, or newly created
     */
    static public UsbNode getNode(String name, Controller controller, Component component) {
        Object key = (component != null) ? component : controller;
        UsbNode result = NODES.get(key);
        if (result == null) {
            result = new UsbNode(name, controller, component);
            NODES.put(key, result);
        }
        return result;
    }

    private static final HashMap<Object, UsbNode> NODES = new HashMap<>();

    private final static Logger log = LoggerFactory.getLogger(UsbNode.class);
}
