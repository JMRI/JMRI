// UsbNode.java

package jmri.jmrix.jinput;

import jmri.*;

import javax.swing.tree.DefaultMutableTreeNode;

import net.java.games.input.*;

/**
 * UsbNode represents the USB controllers or component.
 * <P>
 * Generally accessed via the TreeModel.
 * <P>
 * Can be connected to a JMRI Sensor or Memory.
 * 
 * @author			Bob Jacobsen  Copyright 2008
 * @version			$Revision: 1.4 $
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
    
    public int hashCode() { 
        if (component != null) return component.hashCode();
        if (controller == null) return super.hashCode();
        else return controller.hashCode();
    }
    
    public Controller getController() { return controller;}
    public Component getComponent() { return component; }
    
    public boolean equals(Object a) {
        if (! (a.getClass().equals(UsbNode.class))) return false;
        UsbNode opp = (UsbNode) a;
        return (name.equals(opp.name))&&(controller==opp.controller)&&(component==opp.component);
    }

    public void setValue(float val) { 
        this.val = val;
        // if attached, set value
        try {
            if ((attachedSensor != null) && (!attachedSensor.equals("")))
                InstanceManager.sensorManagerInstance()
                        .provideSensor(attachedSensor).setKnownState(
                            val>0.0 ? Sensor.ACTIVE : Sensor.INACTIVE);
        } catch (Exception e1) {
            log.error("Can't set sensor: "+e1);
        }
        try {
            if ((attachedMemory != null) && (!attachedMemory.equals("")))
                InstanceManager.memoryManagerInstance()
                        .provideMemory(attachedMemory).setValue(""+val);
        } catch (Exception e2) {
            log.error("Can't set memory: "+e2);
        }
    }
    public float getValue() { return val; }
    float val = -1;

    String attachedSensor = null;
    public void setAttachedSensor(String sensor) {
        attachedSensor = sensor;
    }
    public String getAttachedSensor() { return attachedSensor; }
    
    String attachedMemory = null;
    public void setAttachedMemory(String memory) {
        attachedMemory = memory;
    }
    public String getAttachedMemory() { return attachedMemory; }
    
    public String getName() { return name; }
    /**
     * Get a specific node. This is used instead of a ctor
     * to ensure that node objects for a given 
     * USB object are unique.
     */
    static public UsbNode getNode(String name, Controller controller, Component component) {
        Object key = controller;
        if (component != null) key = component;
        
        Object temp = map.get(key);
        if (temp!=null) return (UsbNode) temp;
        UsbNode node = new UsbNode(name, controller, component);
        map.put(key, node);
        return node;
    }

    static private java.util.HashMap map = new java.util.HashMap();

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(UsbNode.class.getName());
}

