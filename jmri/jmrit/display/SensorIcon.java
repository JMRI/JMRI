package jmri.jmrit.display;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import jmri.*;
import jmri.jmrit.catalog.*;

/**
 * SensorIcon provides a small icon to display a status of a Sensor.</p>
 * @author Bob Jacobsen Copyright (C) 2001
 * @version $Revision: 1.14 $
 */

public class SensorIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public SensorIcon() {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                            "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
        icon = true;
        text = false;

        displayState(sensorState());
    }

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
            if (sensor != null) {
                displayState(sensorState());
                sensor.addPropertyChangeListener(this);
            } else {
                log.error("Sensor '"+pSystemName+"' not available, icon won't see changes");
            }
        } else {
            log.error("No SensorManager for this protocol, icon won't see changes");
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
    public void setActiveIcon(NamedIcon i) {
        active = i;
        updateSize();
        displayState(sensorState());
    }

    public NamedIcon getInactiveIcon() { return inactive; }
    public void setInactiveIcon(NamedIcon i) {
        inactive = i;
        updateSize();
        displayState(sensorState());
    }

    public NamedIcon getInconsistentIcon() { return inconsistent; }
    public void setInconsistentIcon(NamedIcon i) {
        inconsistent = i;
        updateSize();
        displayState(sensorState());
    }

    public NamedIcon getUnknownIcon() { return unknown; }
    public void setUnknownIcon(NamedIcon i) {
        unknown = i;
        updateSize();
        displayState(sensorState());
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

    /**
     * Pop-up just displays the sensor name
     */
    protected void showPopUp(MouseEvent e) {
        ours = this;
        if (popup==null) {
            String name;
            name = sensor.getID();
            popup = new JPopupMenu();
            popup.add(new JMenuItem(name));
            if (icon) popup.add(new AbstractAction("Rotate") {
                    public void actionPerformed(ActionEvent e) {
                        active.setRotation(active.getRotation()+1, ours);
                        inactive.setRotation(inactive.getRotation()+1, ours);
                        unknown.setRotation(unknown.getRotation()+1, ours);
                        inconsistent.setRotation(inconsistent.getRotation()+1, ours);
                        displayState(sensorState());
                    }
                });
        }

        popup.add(new AbstractAction("Remove") {
            public void actionPerformed(ActionEvent e) {
                remove();
                dispose();
            }
        });

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Drive the current state of the display from the state of the
     * turnout.
     */
    void displayState(int state) {

        switch (state) {
        case Sensor.UNKNOWN:
            if (text) super.setText("<unknown>");
            if (icon) super.setIcon(unknown);
            break;
        case Sensor.ACTIVE:
            if (text) super.setText("Active");
            if (icon) super.setIcon(active);
            break;
        case Sensor.INACTIVE:
            if (text) super.setText("Inactive");
            if (icon) super.setIcon(inactive);
            break;
        default:
            if (text) super.setText("<inconsistent>");
            if (icon) super.setIcon(inconsistent);
            break;
        }

        return;
    }

    protected int maxHeight() {
        return Math.max(
                Math.max( (active!=null) ? active.getIconHeight() : 0,
                        (inactive!=null) ? inactive.getIconHeight() : 0),
                Math.max((unknown!=null) ? unknown.getIconHeight() : 0,
                        (inconsistent!=null) ? inconsistent.getIconHeight() : 0)
            );
    }
    protected int maxWidth() {
        return Math.max(
                Math.max((active!=null) ? active.getIconWidth() : 0,
                        (inactive!=null) ? inactive.getIconWidth() : 0),
                Math.max((unknown!=null) ? unknown.getIconWidth() : 0,
                        (inconsistent!=null) ? inconsistent.getIconWidth() : 0)
            );
    }

    /**
     * (Temporarily) change occupancy on click
     * @param e
     */
    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (e.isAltDown() || e.isMetaDown()) return;
        if (sensor==null) {  // no sensor connected for this protocol
            log.error("No sensor connection, can't process click");
            return;
        }
        try {
            if (sensor.getKnownState()==jmri.Sensor.INACTIVE)
                sensor.setKnownState(jmri.Sensor.ACTIVE);
            else
                sensor.setKnownState(jmri.Sensor.INACTIVE);
        } catch (jmri.JmriException reason) {
            log.warn("Exception changing sensor: "+reason);
        }
    }

    public void dispose() {
        sensor.removePropertyChangeListener(this);
        sensor = null;

        active = null;
        inactive = null;
        inconsistent = null;
        unknown = null;

        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SensorIcon.class.getName());
}
