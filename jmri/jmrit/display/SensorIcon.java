package jmri.jmrit.display;

import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.Icon;

import jmri.*;

/**
 * SensorIcon provides a small icon to display a status of a Sensor.</p>
 * @author Bob Jacobsen
 * @version $Revision: 1.1 $
 */

public class SensorIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public SensorIcon() {
        // super ctor call to make sure this is an icon label
        super(new ImageIcon(ClassLoader.getSystemResource("resources/images19x16/X-red.gif")),
            "sensor icon name");
        displayState(sensorState());
    }

    // what to display - at least one should be true!
    boolean showText = false;
    boolean showIcon = true;

    // the associated Sensor object
    Sensor sensor = null;

    /**
     * Attached a named sensor to this display item
     * @param name Used as a user name to lookup the sensor object
     */
    public void setSensor(String name) {
        sensor = InstanceManager.sensorManagerInstance().
            newSensor(null,name);
        sensor.addPropertyChangeListener(this);
    }

    // display icons
    Icon active = new ImageIcon(ClassLoader.getSystemResource("resources/TrackSegments/GriffenSet/Turnouts44x40/RHNORMUP.gif"));
    Icon inactive = new ImageIcon(ClassLoader.getSystemResource("resources/TrackSegments/GriffenSet/Turnouts44x40/RHREVUP.gif"));
    Icon inconsistent = new ImageIcon(ClassLoader.getSystemResource("resources/images19x16/X-red.gif"));
    Icon unknown = new ImageIcon(ClassLoader.getSystemResource("resources/images19x16/Question-black.gif"));

    public Icon getActiveIcon() { return active; }
    public void setActiveIcon(Icon i) { active = i; displayState(sensorState()); }

    public Icon getInactiveIcon() { return inactive; }
    public void setInactiveIcon(Icon i) { inactive = i; displayState(sensorState()); }

    public Icon getInconsistentIcon() { return inconsistent; }
    public void setInconsistentIcon(Icon i) { inconsistent = i; displayState(sensorState()); }

    public Icon getUnknownIcon() { return unknown; }
    public void setUnknownIcon(Icon i) { unknown = i; displayState(sensorState()); }

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
     * Get current state of attached turnout
     * @return A state variable from a Turnout, e.g. Turnout.CLOSED
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SensorIcon.class.getName());
}