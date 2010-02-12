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
//From layout editor

import javax.swing.JMenu;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
//import javax.swing.JLabel;
import java.awt.Color;
import jmri.util.NamedBeanHandle;

/**
 * An icon to display a status of a Sensor.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @version $Revision: 1.60 $
 */

public class SensorIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public SensorIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        this(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                            "resources/icons/smallschematics/tracksegments/circuit-error.gif"), editor);
        _control = true;
    }

    public SensorIcon(NamedIcon s, Editor editor) {
        // super ctor call to make sure this is an icon label
        super(s, editor);
        active = new NamedIcon(activeName, activeName);
        inactive = new NamedIcon(inactiveName, inactiveName);
        inconsistent = new NamedIcon(inconsistentName, inconsistentName);
        unknown = new NamedIcon(unknownName, unknownName);
        setOpaque(false);
        _control = true;
        displayState(sensorState());
    }

    public SensorIcon(String s, Editor editor){
        super(s, editor);
        _control = true;
        displayState(sensorState());
    }

    // the associated Sensor object
    //Sensor sensor = null;
    private NamedBeanHandle<Sensor> namedSensor;

    /**
     * Attached a named sensor to this display item
     * @param pName System/user name to lookup the sensor object
     */
    public void setSensor(String pName) {
        if (InstanceManager.sensorManagerInstance()!=null) {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
            if (sensor != null) {
                setSensor(new NamedBeanHandle<Sensor>(pName, sensor));                
            } else {
                log.error("Sensor '"+pName+"' not available, icon won't see changes");
            }
        } else {
            log.error("No SensorManager for this protocol, icon won't see changes");
        }
    }
    /**
     * Attached a named sensor to this display item
     * @param s the Sensor
     */
    public void setSensor(NamedBeanHandle<Sensor> s) {
        if (namedSensor != null) {
            getSensor().removePropertyChangeListener(this);
        }
        
        namedSensor = s;
        if (namedSensor != null) {
            displayState(sensorState());
            getSensor().addPropertyChangeListener(this);
        }
        if (isText()) {
            if (namedSensor!=null){
                if (getSensor().getUserName()!=null)
                {
                    String userName=getSensor().getUserName();
                    if (activeText==null)
                        activeText=userName;
                    if (inactiveText==null)
                        inactiveText = userName;
                    if (inconsistentText==null)
                        inconsistentText=userName;
                    if (unknownText==null)
                        unknownText=userName;
                } else {
                    if (activeText==null)
                        activeText=rb.getString("SensorActive");
                    if (inactiveText==null)
                        inactiveText = rb.getString("SensorInactive");
                    if (inconsistentText==null)
                        inconsistentText=rb.getString("Inconsistent");
                    if (unknownText==null)
                        unknownText=rb.getString("Unknown");
                }
            }
            if (activeText==null) {
                activeText=rb.getString("SensorActive");
                textColorActive=Color.red;
            }
            if (inactiveText==null) {
                inactiveText = rb.getString("SensorInactive");
                textColorInActive=Color.yellow;
            }
                //inactiveText = rb.getString("SensorInactive");
            if (inconsistentText==null)
                inconsistentText=rb.getString("Inconsistent");
            if (unknownText==null)
                unknownText=rb.getString("Unknown");
            if (textColorActive==null)
                textColorActive=Color.red;
            if (textColorInActive==null)
                textColorInActive=Color.yellow;
            if (textColorUnknown==null)
                textColorUnknown=Color.black;
            if (textColorInconsistent==null)
                textColorInconsistent=Color.blue;
            setOpaque(true);
        } else {
            setOpaque(false);
        }
        displayState(sensorState());
        if (debug) log.debug("setSensor: namedSensor= "+
                             ((namedSensor==null)?"null": getNameString())+
                             " isIcon= "+isIcon()+", isText= "+isText()+", activeText= "+activeText);
        repaint();
    }

    public Sensor getSensor() {
        if (debug) log.debug("getSensor: namedSensor= "+
                             ((namedSensor==null)?"null": getNameString())+
                             " isIcon= "+isIcon()+", isText= "+isText()+", activeText= "+activeText);
        return namedSensor.getBean();
    }
    
    public NamedBeanHandle <Sensor> getNamedSensor() {
        return namedSensor;
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
        displayState(sensorState());
    }

    public NamedIcon getInactiveIcon() { return inactive; }
    public void setInactiveIcon(NamedIcon i) {
        inactive = i;
        displayState(sensorState());
    }

    public NamedIcon getInconsistentIcon() { return inconsistent; }
    public void setInconsistentIcon(NamedIcon i) {
        inconsistent = i;
        displayState(sensorState());
    }

    public NamedIcon getUnknownIcon() { return unknown; }
    public void setUnknownIcon(NamedIcon i) {
        unknown = i;
        displayState(sensorState());
    }

    /**
     * Get current state of attached sensor
     * @return A state variable from a Sensor, e.g. Sensor.ACTIVE
     */
    int sensorState() {
        if (namedSensor != null) return getSensor().getKnownState();
        else return Sensor.UNKNOWN;
    }

    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "+e);
        if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue()).intValue();
            displayState(now);
            _editor.repaint();
        }
    }

    public String getNameString() {
        String name = rb.getString("NotConnected");
        if (namedSensor!=null) name = namedSensor.getName();
        return name;
    }

    /**
     * Pop-up just displays the sensor name
     */
    public void showPopUp(JPopupMenu popup) {

        if (isIcon() && !isText()) {
            setTextEditMenu(popup);
        }
        if (isText() && !isIcon()) {
            JMenu stateColor = new JMenu(rb.getString("StateColors"));
                stateColor.add(stateMenu(rb.getString("Unknown"), UNKOWN_FONT_COLOR)); //Unknown
                stateColor.add(stateMenu(rb.getString("SensorActive"), ACTIVE_FONT_COLOR)); //Active
                stateColor.add(stateMenu(rb.getString("SensorInactive"), INACTIVE_FONT_COLOR)); //Inactive
                stateColor.add(stateMenu(rb.getString("Inconsistent"), INCONSISTENT_FONT_COLOR)); //Inconsistent
            popup.add(stateColor);
        }

        momentaryItem = new JCheckBoxMenuItem(rb.getString("Momentary"));
        popup.add(momentaryItem);
        momentaryItem.setSelected (getMomentary());
        momentaryItem.addActionListener(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setMomentary(momentaryItem.isSelected());
            }
        });

        if (!isIcon() || !isText()) {
            if(isIcon()){
                popup.add(new AbstractAction(rb.getString("ChangeToText")) {
                    public void actionPerformed(ActionEvent e) {
                        ChangeLayoutSensorType();
                    }
                });
            } else {
                popup.add(new AbstractAction(rb.getString("ChangeToIcon")) {
                    public void actionPerformed(ActionEvent e) {
                        ChangeLayoutSensorType();
                    }
                });
            }
        }
    }
    /******** popup AbstractAction.actionPerformed method overrides *********/

    protected void rotateOrthogonal() {
        active.setRotation(active.getRotation()+1, this);
        inactive.setRotation(inactive.getRotation()+1, this);
        unknown.setRotation(unknown.getRotation()+1, this);
        inconsistent.setRotation(inconsistent.getRotation()+1, this);
        displayState(sensorState());
        //bug fix, must repaint icons that have same width and height
        repaint();
    }

    public void setScale(double s) {
        active.scale(s, this);
        inactive.scale(s, this);
        unknown.scale(s, this);
        inconsistent.scale(s, this);
        displayState(sensorState());
    }

    void rotate(int deg) {
        active.rotate(deg, this);
        inactive.rotate(deg, this);
        unknown.rotate(deg, this);
        inconsistent.rotate(deg, this);
        displayState(sensorState());
    }

    JCheckBoxMenuItem momentaryItem;

    /**
     * Drive the current state of the display from the state of the
     * turnout.
     */
    void displayState(int state) {
        switch (state) {
            case Sensor.UNKNOWN:
                if (isIcon()) super.setIcon(unknown);
                else if (isText()) {super.setText(unknownText);
                            super.setBackgroundColor(backgroundColorUnknown);
                            super.setForeground(textColorUnknown);
                            //super.setOpaque(true);
                            }
                break;
            case Sensor.ACTIVE:
                //if (isIcon()) updateIcon(active);
                if (isIcon()) super.setIcon(active);
                else if (isText()) {super.setText(activeText);
                            super.setBackgroundColor(backgroundColorActive);
                            super.setForeground(textColorActive);
                            //super.setOpaque(true);
                            }
                break;
            case Sensor.INACTIVE:
                //if (isIcon()) updateIcon(inactive);
                if (isIcon()) super.setIcon(inactive);
                else if (isText()) {super.setText(inactiveText);
                            super.setBackgroundColor(backgroundColorInActive);
                            super.setForeground(textColorInActive);
                            //super.setOpaque(true);
                            }
                break;
            default:
                if (isIcon()) super.setIcon(inconsistent);
                else if (isText()) {super.setText(inconsistentText);
                            super.setBackgroundColor(backgroundColorInconsistent);
                            super.setForeground(textColorInconsistent);
                            //super.setOpaque(true);
                            }
                break;
        }
        updateSize();
    }

    protected void edit() {
        if (showIconEditorFrame(this)) {
            return;
        }
        _iconEditor = new IconAdder();
        _iconEditor.setIcon(3, "SensorStateActive", getActiveIcon());
        _iconEditor.setIcon(2, "SensorStateInactive", getInactiveIcon());
        _iconEditor.setIcon(0, "BeanStateInconsistent", getInconsistentIcon());
        _iconEditor.setIcon(1, "BeanStateUnknown", getUnknownIcon());

        _iconEditorFrame = makeAddIconFrame("EditSensor", "addIconsToPanel", 
                                            "SelectSensor", _iconEditor, this);
        _iconEditor.makeIconPanel();
        _iconEditor.setPickList(PickListModel.sensorPickModelInstance());


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
        _iconEditor.setSelection(getSensor());
    }
    void updateSensor() {
        setActiveIcon(_iconEditor.getIcon("SensorStateActive"));
        setInactiveIcon(_iconEditor.getIcon("SensorStateInactive"));
        setInconsistentIcon(_iconEditor.getIcon("BeanStateInconsistent"));
        setUnknownIcon(_iconEditor.getIcon("BeanStateUnknown"));
        setSensor(_iconEditor.getTableSelection().getDisplayName());
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }
    /* Original text is used when changing between icon and text, this allows for a undo when reverting back. */
    String originalText;
    public void setOriginalText(String s) {
        originalText=s;
    }
    public String getOriginalText() { return originalText; }
    
    public void setText(String s) {
        _text = (s!=null && s.length()>0);
        super.setText(s);
        updateSize();
    }

    boolean momentary = false;
    public boolean getMomentary() { return momentary; }
    public void setMomentary(boolean m) { momentary = m; }

    public boolean buttonLive() {
        if (!isControlling()) return false;
        if (getForceControlOff()) return false;
        if (namedSensor==null) {  // no sensor connected for this protocol
            log.error("No sensor connection, can't process click");
            return false;
        }
        return true;
    }

    public void doMousePressed(MouseEvent e) {
        if (e.isMetaDown() || e.isAltDown() ) return;
        if (getMomentary() && buttonLive()) {
            // this is a momentary button
            try {
                getSensor().setKnownState(jmri.Sensor.ACTIVE);
            } catch (jmri.JmriException reason) {
                log.warn("Exception setting momentary sensor: "+reason);
            }
        }
    }

    public void doMouseReleased(MouseEvent e) {
        if (e.isMetaDown() || e.isAltDown() ) return;
        if (buttonLive()) {
            if (getMomentary()) {
                // this is a momentary button
                try {
                    getSensor().setKnownState(jmri.Sensor.INACTIVE);
                } catch (jmri.JmriException reason) {
                    log.warn("Exception setting momentary sensor: "+reason);
                }
            } else {
                try {
                    if (getSensor().getKnownState()==jmri.Sensor.INACTIVE)
                        getSensor().setKnownState(jmri.Sensor.ACTIVE);
                    else
                        getSensor().setKnownState(jmri.Sensor.INACTIVE);
                } catch (jmri.JmriException reason) {
                    log.warn("Exception flipping sensor: "+reason);
                }
            }
        }
    }

    public void dispose() {
        getSensor().removePropertyChangeListener(this);
        namedSensor = null;

        active = null;
        inactive = null;
        inconsistent = null;
        unknown = null;

        super.dispose();
    }

    // The code below here is from the layoutsensoricon.

    Color textColorActive = Color.black;
    //Color clear = new Color(238,238,238);

    public void setTextActive(Color color){
        textColorActive=color;
        displayState(sensorState());
    }

    public Color getTextActive(){
        return textColorActive;
    }

    Color textColorInActive = Color.black;

    public void setTextInActive(Color color){
        textColorInActive=color;
        displayState(sensorState());
    }

    public Color getTextInActive(){
        return textColorInActive;
    }

    Color textColorUnknown = Color.black;
    public void setTextUnknown(Color color){
        textColorUnknown=color;
        displayState(sensorState());
    }

    public Color getTextUnknown(){
        return textColorUnknown;
    }

    Color textColorInconsistent = Color.black;
    public void setTextInconsistent(Color color){
        textColorInconsistent=color;
        displayState(sensorState());
    }

    public Color getTextInconsistent(){
        return textColorInconsistent;
    }

    Color backgroundColorActive = null;

    public void setBackgroundActive(Color color){
        if (color==null)
            setOpaque(false);
        else
            setOpaque(true);
        backgroundColorActive=color;
        displayState(sensorState());
    }

    public Color getBackgroundActive(){
        return backgroundColorActive;
    }

    Color backgroundColorInActive = null;

    public void setBackgroundInActive(Color color){
        if (color==null)
            setOpaque(false);
        else
            setOpaque(true);
        backgroundColorInActive=color;
        displayState(sensorState());
    }

    public Color getBackgroundInActive(){
        return backgroundColorInActive;
    }

    Color backgroundColorUnknown = null;
    public void setBackgroundUnknown(Color color){
        if (color==null)
            setOpaque(false);
        else
            setOpaque(true);
        backgroundColorUnknown=color;
        displayState(sensorState());
    }

    public Color getBackgroundUnknown(){
        return backgroundColorUnknown;
    }

    Color backgroundColorInconsistent = null;
    public void setBackgroundInconsistent(Color color){
        if (color==null)
            setOpaque(false);
        else
            setOpaque(true);
        backgroundColorInconsistent=color;
        displayState(sensorState());
    }

    public Color getBackgroundInconsistent(){
        return backgroundColorInconsistent;
    }

    String activeText;

    String inactiveText;

    String inconsistentText;

    String unknownText;

    public String getActiveText() { return activeText; }
    public void setActiveText(String i) {
        activeText = i;
        displayState(sensorState());
    }

    public String getInactiveText() { return inactiveText; }
    public void setInactiveText(String i) {
        inactiveText = i;
        displayState(sensorState());
    }

    public String getInconsistentText() { return inconsistentText; }
    public void setInconsistentText(String i) {
        inconsistentText = i;
        displayState(sensorState());
    }

    public String getUnknownText() { return unknownText; }
    public void setUnknownText(String i) {
        unknownText = i;
        displayState(sensorState());
    }

    JMenu stateMenu(final String name, int state) {
        JMenu menu = new JMenu(name);
        JMenu colorMenu = new JMenu(rb.getString("FontColor"));
        makeColorMenu(colorMenu, state);
        menu.add(colorMenu);
        colorMenu = new JMenu(rb.getString("FontBackgroundColor"));
        makeColorMenu(colorMenu, state+1);
        menu.add(colorMenu);
        return menu;
    }

    protected void addColorMenuEntry(JMenu menu, ButtonGroup colorButtonGroup,
                           final String name, final Color color, final int colorType) {
        ActionListener a = new ActionListener() {
            //final String desiredName = name;
            final Color desiredColor = color;
            public void actionPerformed(ActionEvent e) {
                switch (colorType){
                    case FONT_COLOR : 
                        setForeground(desiredColor); 
                        break;
                    case BACKGROUND_COLOR : 
                        if(color==null){
                            setOpaque(false);
                            //We need to force a redisplay when going to clear as the area
                            //doesn't always go transparent on the first click.
                            java.awt.Point p = getLocation();
                            int w = getWidth();
                            int h = getHeight();
                            java.awt.Container parent = getParent();
                            // force redisplay
                            parent.validate();
                            parent.repaint(p.x,p.y,w,h);
                        }
                        else
                            setBackgroundColor(desiredColor);
                        break;
                    case BORDER_COLOR : 
                        setBorderColor(desiredColor); 
                        break;
                    case UNKOWN_FONT_COLOR : 
                        setTextUnknown(desiredColor); 
                        break;
                    case UNKOWN_BACKGROUND_COLOR : 
                        setBackgroundUnknown(desiredColor); 
                        break;
                    case ACTIVE_FONT_COLOR : 
                        setTextActive(desiredColor); 
                        break;
                    case ACTIVE_BACKGROUND_COLOR : 
                        setBackgroundActive(desiredColor); 
                        break;
                    case INACTIVE_FONT_COLOR : 
                        setTextInActive(desiredColor); 
                        break;
                    case INACTIVE_BACKGROUND_COLOR : 
                        setBackgroundInActive(desiredColor); 
                        break;
                    case INCONSISTENT_FONT_COLOR : 
                        setTextInconsistent(desiredColor); 
                        break;
                    case INCONSISTENT_BACKGROUND_COLOR : 
                        setBackgroundInconsistent(desiredColor); 
                        break;
                }
            }
        };
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);

        switch (colorType) {
            case FONT_COLOR:
                setColorButton(getForeground(), color, r);
                break;
            case BACKGROUND_COLOR:
                setColorButton(getBackground(), color, r);
                break;
            case BORDER_COLOR:
                setColorButton(getBorderColor(), color, r);
                break;
            case UNKOWN_FONT_COLOR:
                setColorButton(getTextUnknown(), color, r);
                break;
            case UNKOWN_BACKGROUND_COLOR:
                setColorButton(getBackgroundUnknown(), color, r);
                break;
            case ACTIVE_FONT_COLOR:
                setColorButton(getTextActive(), color, r);
                break;
            case ACTIVE_BACKGROUND_COLOR:
                setColorButton(getBackgroundActive(), color, r);
                break;
            case INACTIVE_FONT_COLOR:
                setColorButton(getTextInActive(), color, r);
                break;
            case INACTIVE_BACKGROUND_COLOR:
                setColorButton(getBackgroundInActive(), color, r);
                break;
            case INCONSISTENT_FONT_COLOR:
                setColorButton(getTextInconsistent(), color, r);
                break;
            case INCONSISTENT_BACKGROUND_COLOR:
                setColorButton(getBackgroundInconsistent(), color, r);
                break;
        }
        colorButtonGroup.add(r);
        menu.add(r);
    }
                
    void ChangeLayoutSensorType(){
        NamedBeanHandle <Sensor> handle = getNamedSensor();
        if (isIcon()) {
            _icon = false;
            _text = true;
            setIcon(null);
        } else if (isText()) {
            _icon = true;
            _text = false;
            setText(null);
        }
        setSensor(handle);
    }

    // overide
    public void setTextEditMenu(JPopupMenu popup) {
        if (isIcon()) {
            super.setTextEditMenu(popup, "OverlayText");
        } else if (isText()) {
            popup.add(new AbstractAction(rb.getString("SetSensorText")) {
				public void actionPerformed(ActionEvent e) {
					String name = getNameString();
					SensorTextEdit(name);
				}
			});

        }
    }

    public void SensorTextEdit(String name) {
		if (log.isDebugEnabled())
			log.debug("make text edit menu");

		SensorTextEdit f = new SensorTextEdit();
		f.addHelpMenu("package.jmri.jmrit.display.SensorTextEdit", true);
		try {
			f.initComponents(this, name);
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);
	}

    public int maxHeight() {
        //We use the super class method to get the greatest value for the text
            return Math.max(super.maxHeight(), Math.max(
            Math.max((active!=null) ? active.getIconHeight() : 0,
                    (inactive!=null) ? inactive.getIconHeight() : 0),
            Math.max((unknown!=null) ? unknown.getIconHeight() : 0,
                    (inconsistent!=null) ? inconsistent.getIconHeight() : 0)
        ));
    }
    public int maxWidth() {
        return Math.max(super.maxWidth(), Math.max(
            Math.max((active!=null) ? active.getIconWidth() : 0,
                    (inactive!=null) ? inactive.getIconWidth() : 0),
            Math.max((unknown!=null) ? unknown.getIconWidth() : 0,
                    (inconsistent!=null) ? inconsistent.getIconWidth() : 0)
        ));
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SensorIcon.class.getName());
}
