package jmri.jmrit.display;

import java.awt.event.*;
import javax.swing.*;
import jmri.*;
import jmri.jmrit.catalog.*;

/**
 * SensorIcon provides a small icon to display a status of a Sensor.</p>
 * @author Bob Jacobsen Copyright (C) 2001
 * @version $Revision: 1.9 $
 */

public class SensorIcon extends PositionableLabel implements java.beans.PropertyChangeListener {
    
    public SensorIcon() {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                            "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
        displayState(sensorState());
    }
    
    // what to display - at least one should be true!
    boolean showText = false;
    boolean showIcon = true;
    
    // the associated Sensor object
    Sensor sensor = null;
    
    /**
     * Attached a named sensor to this display item
     * @param pUserName User name to lookup the sensor object
     * @param pSystemName System name to lookup the sensor object
     */
    public void setSensor(String pUserName, String pSystemName) {
        if (InstanceManager.sensorManagerInstance()!=null) {
            sensor = InstanceManager.sensorManagerInstance().
                newSensor(pUserName,pSystemName);
            displayState(sensorState());
            sensor.addPropertyChangeListener(this);
        } else {
            log.error("No SensorManager for this protocol, sensor won't see changes");
        }
    }
    public Sensor getSensor() {
        return sensor;
    }
    
    // display icons
    String activeName = "resources/icons/smallschematics/tracksegments/circuit-occupied.gif";
    NamedIcon active = new NamedIcon(activeName, activeName);
    
    String inactiveName = "resources/icons/smallschematics/tracksegments/circuit-empty.gif";
    NamedIcon inactive = new NamedIcon(inactiveName, inactiveName);
    
    String inconsistentName = "resources/icons/smallschematics/tracksegments/circuit-error.gif";
    NamedIcon inconsistent = new NamedIcon(inconsistentName, inconsistentName);
    
    String unknownName = "resources/icons/smallschematics/tracksegments/circuit-error.gif";
    NamedIcon unknown = new NamedIcon(unknownName, unknownName);
    
    public NamedIcon getActiveIcon() { return active; }
    public void setActiveIcon(NamedIcon i) { active = i; displayState(sensorState()); }
    
    public NamedIcon getInactiveIcon() { return inactive; }
    public void setInactiveIcon(NamedIcon i) { inactive = i; displayState(sensorState()); }
    
    public NamedIcon getInconsistentIcon() { return inconsistent; }
    public void setInconsistentIcon(NamedIcon i) { inconsistent = i; displayState(sensorState()); }
    
    public NamedIcon getUnknownIcon() { return unknown; }
    public void setUnknownIcon(NamedIcon i) { unknown = i; displayState(sensorState()); }
    
    public int getHeight() {
        return Math.max(
                        Math.max(active.getIconHeight(), inactive.getIconHeight()),
                        Math.max(inconsistent.getIconHeight(), unknown.getIconHeight())
                        );
    }
    
    public int getWidth() {
        return Math.max(
                        Math.max(active.getIconWidth(), inactive.getIconWidth()),
                        Math.max(inconsistent.getIconWidth(), unknown.getIconWidth())
                        );
    }
    
    /**
     * Get current state of attached sensor
     * @return A state variable from a Sensor, e.g. Sensor.ACTIVE
     */
    int sensorState() {
        if (sensor != null) return sensor.getKnownState();
        else return Sensor.UNKNOWN;
    }
    
    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "+e);
        if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue()).intValue();
            displayState(now);
        }
    }
    
    JPopupMenu popup = null;
    SensorIcon ours = this;
    /**
     * Pop-up just displays the sensor name
     */
    protected void showPopUp(MouseEvent e) {
        if (popup==null) {
            String name;
            name = sensor.getID();
            popup = new JPopupMenu();
            popup.add(new JMenuItem(name));
            if (showIcon) popup.add(new AbstractAction("Rotate") {
                    public void actionPerformed(ActionEvent e) {
                        active.setRotation(active.getRotation()+1, ours);
                        inactive.setRotation(inactive.getRotation()+1, ours);
                        unknown.setRotation(unknown.getRotation()+1, ours);
                        inconsistent.setRotation(inconsistent.getRotation()+1, ours);
                        displayState(sensorState());
                        ours.setSize(ours.getPreferredSize().width, ours.getPreferredSize().height);
                    }
                });
        }
        popup.show(e.getComponent(), e.getX(), e.getY());
    }
    
    /**
     * Drive the current state of the display from the state of the
     * turnout.
     */
    void displayState(int state) {
        switch (state) {
        case Sensor.UNKNOWN:
            if (showText) super.setText("<unknown>");
            if (showIcon) super.setIcon(unknown);
            return;
        case Sensor.ACTIVE:
            if (showText) super.setText("Active");
            if (showIcon) super.setIcon(active);
            return;
        case Sensor.INACTIVE:
            if (showText) super.setText("Inactive");
            if (showIcon) super.setIcon(inactive);
            return;
        default:
            if (showText) super.setText("<inconsistent>");
            if (showIcon) super.setIcon(inconsistent);
            return;
        }
    }
    
    /**
     * (Temporarily) change occupancy on click
     * @param e
     */
    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (e.isAltDown() || e.isMetaDown()) return;
        try {
            if (sensor==null) return;   // no sensor connected for this protocol
            if (sensor.getKnownState()==jmri.Sensor.INACTIVE)
                sensor.setKnownState(jmri.Sensor.ACTIVE);
            else
                sensor.setKnownState(jmri.Sensor.INACTIVE);
        } catch (jmri.JmriException reason) {
            log.warn("Exception changing sensor: "+reason);
        }
    }
    
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SensorIcon.class.getName());
}
