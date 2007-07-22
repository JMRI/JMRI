package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBoxMenuItem;

import com.sun.java.util.collections.ArrayList;

/**
 * An icon to display a status of set of Sensors.
 *<P>
 * Each sensor has an associated image.  Normally, only one
 * sensor will be active at a time, and in that case the
 * associated image will be shown.  If more than one is active,
 * one of the corresponding images will be shown, but which one is
 * not guaranteed.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2007
 * @version $Revision: 1.3 $
 */

public class MultiSensorIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public MultiSensorIcon() {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                            "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
        icon = true;
        text = false;

        setDisplayLevel(PanelEditor.SENSORS);
        displayState();
    }


    boolean updown = false;
    // if not updown, is rightleft
    public void setUpDown(boolean b) { updown = b; }
    public boolean getUpDown() { return updown; }
    
    ArrayList entries = new ArrayList();
    
    public void addEntry(String pName, NamedIcon icon) {
        Entry e = new Entry();
        Sensor sensor;
        if (InstanceManager.sensorManagerInstance()!=null) {
            sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
            if (sensor != null) {
                sensor.addPropertyChangeListener(this);
                e.sensor = sensor;
                e.icon = icon;
                entries.add(e);
                setProperToolTip();
                displayState();
            } else {
                log.error("Sensor '"+pName+"' not available, icon won't see changes");
            }
        } else {
            log.error("No SensorManager for this protocol, icon won't see changes");
        }
    }
    public int getNumEntries() { return entries.size(); }
    public String getSensorName(int i) { 
        return ((Entry) entries.get(i)).sensor.getSystemName();
    }
    public NamedIcon getSensorIcon(int i) { 
        return ((Entry) entries.get(i)).icon;
    }
    
    // display icons
    String inactiveName = "resources/icons/USS/plate/levers/l-inactive.gif";
    NamedIcon inactive = new NamedIcon(inactiveName, inactiveName);

    String inconsistentName = "resources/icons/USS/plate/levers/l-unknown.gif";
    NamedIcon inconsistent = new NamedIcon(inconsistentName, inconsistentName);

    String unknownName = "resources/icons/USS/plate/levers/l-inconsistent.gif";
    NamedIcon unknown = new NamedIcon(unknownName, unknownName);

    public NamedIcon getInactiveIcon() { return inactive; }
    public void setInactiveIcon(NamedIcon i) {
        inactive = i;
    }

    public NamedIcon getInconsistentIcon() { return inconsistent; }
    public void setInconsistentIcon(NamedIcon i) {
        inconsistent = i;
    }

    public NamedIcon getUnknownIcon() { return unknown; }
    public void setUnknownIcon(NamedIcon i) {
        unknown = i;
    }

    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "+e);
        if (e.getPropertyName().equals("KnownState")) {
            displayState();
        }
    }

    public void setProperToolTip() {
        setToolTipText(getNameString());
    }

    String getNameString() {
        String name = "";
        if ((entries == null) || (entries.size() < 1)) 
            name = "<Not connected>";
        else {
            name = ((Entry)entries.get(0)).sensor
                    .getSystemName();
            for (int i = 1; i<entries.size(); i++) {
                name += ","+((Entry)entries.get(i)).sensor
                    .getSystemName();
            }
        }
        return name;
    }

    /**
     * Pop-up just displays the sensor name
     */
    protected void showPopUp(MouseEvent e) {
        if (!getEditable()) return;
        ours = this;
        if (popup==null) {
            popup = new JPopupMenu();
            popup.add(new JMenuItem(getNameString()));
            if (icon) popup.add(new AbstractAction("Rotate") {
                    public void actionPerformed(ActionEvent e) {
                        for (int i = 0; i<entries.size(); i++) {
                            NamedIcon icon = ((Entry)entries.get(i)).icon;
                            icon.setRotation(icon.getRotation()+1, ours);
                        }
                        inactive.setRotation(inactive.getRotation()+1, ours);
                        unknown.setRotation(unknown.getRotation()+1, ours);
                        inconsistent.setRotation(inconsistent.getRotation()+1, ours);
                        displayState();
                    }
                });

            addDisableMenuEntry(popup);
            
            popup.add(new AbstractAction("Remove") {
                    public void actionPerformed(ActionEvent e) {
                        remove();
                        dispose();
                    }
                });
        }  // end creation of popup menu

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    int displaying = -1;
    
    /**
     * Drive the current state of the display from the state of the
     * turnout.
     */
    void displayState() {

        updateSize();

        // run the entries
        boolean foundActive = false;
        
        for (int i = 0; i<entries.size(); i++) {
            Entry e = ((Entry)entries.get(i));
            
            int state = e.sensor.getKnownState();

            switch (state) {
            case Sensor.ACTIVE:
                if (text) super.setText("Active");
                if (icon) super.setIcon(e.icon);
                foundActive = true;
                displaying = i;
                break;  // look at the next ones too
            case Sensor.UNKNOWN:
                if (text) super.setText("<unknown>");
                if (icon) super.setIcon(unknown);
                return;  // this trumps all others
            case Sensor.INCONSISTENT:
                if (text) super.setText("<inconsistent>");
                if (icon) super.setIcon(inconsistent);
                break;
            default:
                break;
            }
        }
        // loop has gotten to here
        if (foundActive) return;  // set active
        // only case left is all inactive
        if (text) super.setText("Inactive");
        if (icon) super.setIcon(inactive);     
        return;
    }

    // Note that this does not include the active icons;
    // these need to all be the same size
    protected int maxHeight() {
        return Math.max(
                        ((inactive!=null) ? inactive.getIconHeight() : 0),
                Math.max((unknown!=null) ? unknown.getIconHeight() : 0,
                        (inconsistent!=null) ? inconsistent.getIconHeight() : 0)
            );
    }
    
    // Note that this does not include the active icons;
    // these need to all be the same size
    protected int maxWidth() {
        return Math.max(
                        ((inactive!=null) ? inactive.getIconWidth() : 0),
                Math.max((unknown!=null) ? unknown.getIconWidth() : 0,
                        (inconsistent!=null) ? inconsistent.getIconWidth() : 0)
            );
    }
    
    /**
     * (Temporarily) change occupancy on click
     * @param e
     */
    public void mouseClicked(java.awt.event.MouseEvent e) {
        super.mouseClicked(e);
        if (e.isAltDown() || e.isMetaDown()) return;
        if (!buttonLive()) return;
        if (entries == null || entries.size() < 1) return;
        
        // here, find the click and change state
        
        // find if we want to increment or decrement
        boolean dec = false;
        if (updown) {
            if (e.getY() > (inactive.getIconHeight()/2)) dec = true;
        } else {
            if (e.getX() < (inactive.getIconWidth()/2)) dec = true;
        }
        
        // get new index
        int next;
        if (dec) {
            next = displaying-1;
        } else {
            next = displaying+1;
        }
        if (next < 0) next = 0;
        if (next >= entries.size()) next = entries.size()-1;
        int drop = displaying;
        
        try {
            ((Entry)entries.get(next)).sensor.setKnownState(Sensor.ACTIVE);
            if (drop >= 0 && drop != next) ((Entry)entries.get(drop)).sensor.setKnownState(Sensor.INACTIVE);
        } catch (jmri.JmriException ex) {
            log.error("Click failed to set sensor: "+ex);
        }
    }

    boolean buttonLive() {
        if (!getControlling()) return false;
        if (getForceControlOff()) return false;
        return true;        
    }

    public void mousePressed(MouseEvent e) {
        // do rest of mouse processing
        super.mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
        // do rest of mouse processing
        super.mouseReleased(e);
    }
 
    public void dispose() {
        // remove listeners
        for (int i = 0; i<entries.size(); i++) {
            ((Entry)entries.get(i)).sensor
                .removePropertyChangeListener(this);
        }

        super.dispose();
    }

    class Entry {
        Sensor sensor;
        NamedIcon icon; 
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MultiSensorIcon.class.getName());
}
