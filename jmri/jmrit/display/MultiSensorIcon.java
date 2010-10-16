package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;


import jmri.util.NamedBeanHandle;

import java.util.ArrayList;

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
 * @version $Revision: 1.40 $
 */

public class MultiSensorIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public MultiSensorIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                            "resources/icons/smallschematics/tracksegments/circuit-error.gif"), editor);
        setDisplayLevel(Editor.SENSORS);
        _control = true;
        displayState();
        setPopupUtility(null);
    }

    boolean updown = false;
    // if not updown, is rightleft
    public void setUpDown(boolean b) { updown = b; }
    public boolean getUpDown() { return updown; }
    
    ArrayList<Entry> entries = new ArrayList<Entry>();
    
    public Positionable clone() {
        MultiSensorIcon pos = new MultiSensorIcon(_editor);
        pos.setInactiveIcon(cloneIcon(getInactiveIcon(), pos));
        pos.setInconsistentIcon(cloneIcon(getInconsistentIcon(), pos));
        pos.setUnknownIcon(cloneIcon(getUnknownIcon(), pos));
        for (int i=0; i<entries.size(); i++) {
            addEntry(getSensorName(i), cloneIcon(getSensorIcon(i), pos));
        }
        finishClone(pos);
        return pos;
    }
                                        
    public void addEntry(NamedBeanHandle<Sensor> sensor, NamedIcon icon) {
        if (sensor != null) {
            Entry e = new Entry();
            sensor.getBean().addPropertyChangeListener(this);
            e.namedSensor = sensor;
            e.icon = icon;
            entries.add(e);
            displayState();
        } else {
            log.error("Sensor not available, icon won't see changes");
        }
    }

    public void addEntry(String pName, NamedIcon icon) {
        NamedBeanHandle<Sensor> sensor;
        if (InstanceManager.sensorManagerInstance()!=null) {
            sensor = new NamedBeanHandle<Sensor>(pName, InstanceManager.sensorManagerInstance().provideSensor(pName));
            addEntry(sensor, icon);
        } else {
            log.error("No SensorManager for this protocol, icon won't see changes");
        }
    }
    public int getNumEntries() { return entries.size(); }
    public String getSensorName(int i) { 
        return entries.get(i).namedSensor.getName();
    }
    public NamedIcon getSensorIcon(int i) { 
        return entries.get(i).icon;
    }
    
    // display icons
    String inactiveName = "resources/icons/USS/plate/levers/l-inactive.gif";
    NamedIcon inactive = new NamedIcon(inactiveName, inactiveName);

    String inconsistentName = "resources/icons/USS/plate/levers/l-inconsistent.gif";
    NamedIcon inconsistent = new NamedIcon(inconsistentName, inconsistentName);

    String unknownName = "resources/icons/USS/plate/levers/l-unknown.gif";
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
        if (log.isDebugEnabled()) {
            String prop = e.getPropertyName();
            Sensor sen = (Sensor)e.getSource();
             log.debug("property change("+prop+") Sensor state= "+sen.getKnownState()+
                       " - old= "+e.getOldValue()+", New= "+e.getNewValue());
        }
        if (e.getPropertyName().equals("KnownState")) {
            displayState();
            _editor.repaint();
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION") 
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    public String getNameString() {
        String name = "";
        if ((entries == null) || (entries.size() < 1)) 
            name = rb.getString("NotConnected");
        else {
            name = entries.get(0).namedSensor.getName();
            for (int i = 1; i<entries.size(); i++) {
                name += ","+entries.get(i).namedSensor.getName();
            }
        }
        return name;
    }

    /******** popup AbstractAction.actionPerformed method overrides *********/

    protected void rotateOrthogonal() {
        for (int i = 0; i<entries.size(); i++) {
            NamedIcon icon = entries.get(i).icon;
            icon.setRotation(icon.getRotation()+1, this);
        }
        inactive.setRotation(inactive.getRotation()+1, this);
        unknown.setRotation(unknown.getRotation()+1, this);
        inconsistent.setRotation(inconsistent.getRotation()+1, this);
        displayState();
        // bug fix, must repaint icons that have same width and height
        repaint();
    }


    public void setScale(double s) {
        for (int i = 0; i<entries.size(); i++) {
            NamedIcon icon = entries.get(i).icon;
            icon.scale(s, this);
        }
        inactive.scale(s, this);
        unknown.scale(s, this);
        inconsistent.scale(s, this);
        displayState();
    }

    public void rotate(int deg) {
        for (int i = 0; i<entries.size(); i++) {
            NamedIcon icon = entries.get(i).icon;
            icon.rotate(deg, this);
        }
        inactive.rotate(deg, this);
        unknown.rotate(deg, this);
        inconsistent.rotate(deg, this);
        displayState();
    }
    

    MultiSensorIconAdder _iconEditor;
    protected void edit() {
        if (showIconEditorFrame(this)) {
            return;
        }
        _iconEditor = new MultiSensorIconAdder("MultiSensorEditor");
        _iconEditor.setIcon(2, "SensorStateInactive", inactive);
        _iconEditor.setIcon(0, "BeanStateInconsistent", inconsistent);
        _iconEditor.setIcon(1, "BeanStateUnknown", unknown);
        _iconEditor.setMultiIcon(entries);
        _iconEditorFrame = makeAddIconFrame("EditMultiSensor", "addIconsToPanel", 
                                           "SelectMultiSensor", _iconEditor, this);
        _iconEditor.makeIconPanel();
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.sensorPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateSensor();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _iconEditor.addCatalog();
                    _iconEditorFrame.pack();
                }
        };
        _iconEditor.complete(addIconAction, changeIconAction, true, true);
    }

    void updateSensor() {
        setInactiveIcon(_iconEditor.getIcon("SensorStateInactive"));
        setInconsistentIcon(_iconEditor.getIcon("BeanStateInconsistent"));
        setUnknownIcon(_iconEditor.getIcon("BeanStateUnknown"));
        for (int i = 0; i<entries.size(); i++) {
            entries.get(i).namedSensor.getBean()
                .removePropertyChangeListener(this);
        }
        int numPositions = _iconEditor.getNumIcons();
        entries = new ArrayList<Entry>(numPositions);
        for (int i=3; i<numPositions; i++) {
            NamedIcon icon = _iconEditor.getIcon(i);
            NamedBeanHandle<Sensor> namedSensor = _iconEditor.getSensor(i);
            addEntry(namedSensor, icon);
        }
        setUpDown(_iconEditor.getUpDown());
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }
    /************* end popup action methods ****************/

    int displaying = -1;
    
    /**
     * Drive the current state of the display from the state of the
     * turnout.
     */
    public void displayState() {

        updateSize();

        // run the entries
        boolean foundActive = false;
        
        for (int i = 0; i<entries.size(); i++) {
            Entry e = entries.get(i);
            
            int state = e.namedSensor.getBean().getKnownState();

            switch (state) {
            case Sensor.ACTIVE:
                if (isText()) super.setText(rb.getString("Active"));
                if (isIcon()) super.setIcon(e.icon);
                foundActive = true;
                displaying = i;
                break;  // look at the next ones too
            case Sensor.UNKNOWN:
                if (isText()) super.setText(rb.getString("UnKnown"));
                if (isIcon()) super.setIcon(unknown);
                return;  // this trumps all others
            case Sensor.INCONSISTENT:
                if (isText()) super.setText(rb.getString("Inconsistent"));
                if (isIcon()) super.setIcon(inconsistent);
                break;
            default:
                break;
            }
        }
        // loop has gotten to here
        if (foundActive) return;  // set active
        // only case left is all inactive
        if (isText()) super.setText(rb.getString("Inactive"));
        if (isIcon()) super.setIcon(inactive);     
        return;
    }    

    // Use largest size. If icons are not same size, 
    // this can result in drawing artifacts.
    public int maxHeight() {
        int size = Math.max(
                        ((inactive!=null) ? inactive.getIconHeight() : 0),
                Math.max((unknown!=null) ? unknown.getIconHeight() : 0,
                        (inconsistent!=null) ? inconsistent.getIconHeight() : 0)
            );
        if (entries != null) {
            for (int i = 0; i<entries.size(); i++)
                size = Math.max(size, entries.get(i).icon.getIconHeight());
        }
        return size;
    }
    
    // Use largest size. If icons are not same size, 
    // this can result in drawing artifacts.
    public int maxWidth() {
        int size = Math.max(
                        ((inactive!=null) ? inactive.getIconWidth() : 0),
                Math.max((unknown!=null) ? unknown.getIconWidth() : 0,
                        (inconsistent!=null) ? inconsistent.getIconWidth() : 0)
            );
        if (entries != null) {
            for (int i = 0; i<entries.size(); i++)
                size = Math.max(size, entries.get(i).icon.getIconWidth());
        }
        return size;
    }
    		
	public void performMouseClicked(java.awt.event.MouseEvent e, int xx, int yy) {
        if (log.isDebugEnabled()) log.debug("performMouseClicked: buttonLive= "+buttonLive()+", click from ("+
                 xx+", "+yy+") displaying="+displaying); 
        if (!buttonLive()) return;
        if (entries == null || entries.size() < 1) return;
        
        // find if we want to increment or decrement
        boolean dec = false;
/*        if (updown) {
            if (yy > (inactive.getIconHeight()/2)) dec = true;
        } else {
            if (xx < (inactive.getIconWidth()/2)) dec = true;
        }   */
        if (updown) {
            if ((yy-getY()) > maxHeight()/2) dec = true;
        } else {
            if ((xx-getX()) < maxWidth()/2) dec = true;
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
        if (log.isDebugEnabled()) log.debug("dec= "+dec+" displaying="+displaying+" next= "+next );       
        try {
            entries.get(next).namedSensor.getBean().setKnownState(Sensor.ACTIVE);
            if (drop >= 0 && drop != next) entries.get(drop).namedSensor.getBean().setKnownState(Sensor.INACTIVE);
        } catch (jmri.JmriException ex) {
            log.error("Click failed to set sensor: "+ex);
        }
    }

    boolean buttonLive() {
        if (!_editor.getFlag(Editor.OPTION_CONTROLS, isControlling())) return false;
        return true;        
    }

    public void doMouseClicked(MouseEvent e) {
        if ( !e.isAltDown() &&  !e.isMetaDown() ) {
            performMouseClicked(e, e.getX(), e.getY() );
        }
    }
 
    public void dispose() {
        // remove listeners
        for (int i = 0; i<entries.size(); i++) {
            entries.get(i).namedSensor.getBean()
                .removePropertyChangeListener(this);
        }
        super.dispose();
    }

    static class Entry {
        NamedBeanHandle<Sensor> namedSensor;
        NamedIcon icon; 
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MultiSensorIcon.class.getName());
}
