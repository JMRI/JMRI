package jmri.jmrix.jinput;

import java.util.HashMap;
import javax.swing.tree.DefaultMutableTreeNode;
import jmri.InstanceManager;
import jmri.Sensor;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UsbNode represents the USB controllers or component.
 * <P>
 * Generally accessed via the TreeModel.
 * <P>
 * Can be connected to a JMRI Sensor or Memory.
 *
 * @author	Bob Jacobsen Copyright 2008
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
        if (component != null) {
            return component.hashCode();
        }
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
        if (a == null) {
            return false;
        }
        if (!(a instanceof UsbNode)) {
            return false;
        }
        UsbNode opp = (UsbNode) a;
        return (name.equals(opp.name)) && (controller == opp.controller) && (component == opp.component);
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
        } catch (Exception e1) {
            log.error("Can't set sensor: " + e1);
        }
        try {
            if ((attachedMemory != null) && (!attachedMemory.equals(""))) {
                InstanceManager.memoryManagerInstance()
                        .provideMemory(attachedMemory).setValue("" + val);
            }
        } catch (Exception e2) {
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
     * Get a specific node. This is used instead of a ctor to ensure that node
     * objects for a given USB object are unique.
     */
    static public UsbNode getNode(String name, Controller controller, Component component) {
        Object key = controller;
        if (component != null) {
            key = component;
        }

        UsbNode temp = map.get(key);
        if (temp != null) {
            return temp;
        }
        UsbNode node = new UsbNode(name, controller, component);
        map.put(key, node);
        return node;
    }

    static private HashMap<Object, UsbNode> map = new HashMap<Object, UsbNode>();

    private final static Logger log = LoggerFactory.getLogger(UsbNode.class.getName());
}
