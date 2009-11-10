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
import javax.swing.JLabel;
import java.awt.Color;

/**
 * An icon to display a status of a Sensor.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @version $Revision: 1.48 $
 */

public class SensorIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public SensorIcon() {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                            "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
        icon = true;
        text = false;
        //Next four from layoutSensor
        active = new NamedIcon(activeName, activeName);
        inactive = new NamedIcon(inactiveName, inactiveName);
        inconsistent = new NamedIcon(inconsistentName, inconsistentName);
        unknown = new NamedIcon(unknownName, unknownName);
        setDisplayLevel(PanelEditor.SENSORS);
        displayState(sensorState());

    }
    //Used by Layout Editor
    public SensorIcon(NamedIcon s) {
        // super ctor call to make sure this is an icon label
        super(s);
        icon = true;
        text = false;
        active = new NamedIcon(activeName, activeName);
        inactive = new NamedIcon(inactiveName, inactiveName);
        inconsistent = new NamedIcon(inconsistentName, inconsistentName);
        unknown = new NamedIcon(unknownName, unknownName);
        setDisplayLevel(LayoutEditor.SENSORS);
        setOpaque(false);
        displayState(sensorState());
    }

    //Used by Layout Editor
    public SensorIcon(String s){
        super(s);
        icon=false;
        text=true;
        setDisplayLevel(LayoutEditor.SENSORS);
        setHorizontalAlignment(JLabel.CENTER);
        displayState(sensorState());
    }

    // the associated Sensor object
    Sensor sensor = null;

    /**
     * Attached a named sensor to this display item
     * @param pName System/user name to lookup the sensor object
     */
    public void setSensor(String pName) {
        if (InstanceManager.sensorManagerInstance()!=null) {
            sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
            if (sensor != null) {
                displayState(sensorState());
                sensor.addPropertyChangeListener(this);
                setProperToolTip();
            } else {
                log.error("Sensor '"+pName+"' not available, icon won't see changes");
            }
        } else {
            log.error("No SensorManager for this protocol, icon won't see changes");
        }
        //Next if statement from Layout Sensor.
        if (text){
            if (sensor.getUserName()!=null){
                String userName=sensor.getUserName();
                if (activeText==null)
                    activeText=userName;
                if (inactiveText==null)
                    inactiveText = userName;
                if (inconsistentText==null)
                    inconsistentText=userName;
                if (unknownText==null)
                    unknownText=userName;
            } else{
                if (activeText==null)
                    activeText=rb.getString("SensorActive");
                if (inactiveText==null)
                    inactiveText = rb.getString("SensorInactive");
                if (inconsistentText==null)
                    inconsistentText=rb.getString("Inconsistent");
                if (unknownText==null)
                    unknownText=rb.getString("Unknown");
            }
            if (textColorActive==null)
                textColorActive=Color.red;
            if (textColorInActive==null)
                textColorInActive=Color.green;
            if (textColorUnknown==null)
                textColorUnknown=Color.black;
            if (textColorInconsistent==null)
                textColorInconsistent=Color.blue;
            displayState(sensorState());
        }

    }
    /**
     * Attached a named sensor to this display item
     * @param s the Sensor
     */
    public void setSensor(Sensor s) {
        if (sensor != null) {
            sensor.removePropertyChangeListener(this);
        }
        sensor = s;
        if (sensor != null) {
            displayState(sensorState());
            sensor.addPropertyChangeListener(this);
            setProperToolTip();
        }
        //Next if statement from Layout Sensor.
        if (text){
            if (sensor.getUserName()!=null){
                String userName=sensor.getUserName();
                if (activeText==null)
                    activeText=userName;
                if (inactiveText==null)
                    inactiveText = userName;
                if (inconsistentText==null)
                    inconsistentText=userName;
                if (unknownText==null)
                    unknownText=userName;
            } else{
                if (activeText==null)
                    activeText=rb.getString("SensorActive");
                if (inactiveText==null)
                    inactiveText = rb.getString("SensorInactive");
                if (inconsistentText==null)
                    inconsistentText=rb.getString("Inconsistent");
                if (unknownText==null)
                    unknownText=rb.getString("Unknown");
            }
            if (textColorActive==null)
                textColorActive=Color.red;
            if (textColorInActive==null)
                textColorInActive=Color.green;
            if (textColorUnknown==null)
                textColorUnknown=Color.black;
            if (textColorInconsistent==null)
                textColorInconsistent=Color.blue;
            displayState(sensorState());
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
        if (sensor != null) return sensor.getKnownState();
        else return Sensor.UNKNOWN;
    }

    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "+e);
        if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue()).intValue();
            displayState(now);
            if (getLayoutPanel()!=null){
                //super.layoutPanel.resetAwaitingIconChange();
                getLayoutPanel().resetAwaitingIconChange();
                getLayoutPanel().redrawPanel();
            }
            
        }
    }

    public void setProperToolTip() {
        setToolTipText(getNameString());
    }

    public String getNameString() {
        String name;
        if (sensor == null) name = rb.getString("NotConnected");
        else if (sensor.getUserName()!=null) {
            name = sensor.getUserName();
            if (sensor.getSystemName()!=null) name = name+" ("+sensor.getSystemName()+")";
        } else
            name = sensor.getSystemName();
        return name;
    }

    /**
     * Pop-up just displays the sensor name
     */
    protected void showPopUp(MouseEvent e) {
        if (!getEditable()) return;
        ours = this;
        popup = new JPopupMenu();

        popup.add(new JMenuItem(getNameString()));
        //This is if statement is from the layoutSensorIcon
        checkLocationEditable(popup, getNameString());
        if (getHidden()) popup.add(rb.getString("Hidden"));
        else popup.add(rb.getString("NotHidden"));
        popup.addSeparator();
        if (layoutPanel!=null){
            popup.add(new AbstractAction("Set x & y") {
                public void actionPerformed(ActionEvent e) {
                    String name = getText();
                    displayCoordinateEdit(name);
                }
            });
        }
        if (icon) {
            popup.add(new AbstractAction(rb.getString("Rotate")) {
                public void actionPerformed(ActionEvent e) {
                    active.setRotation(active.getRotation()+1, ours);
                    inactive.setRotation(inactive.getRotation()+1, ours);
                    unknown.setRotation(unknown.getRotation()+1, ours);
                    inconsistent.setRotation(inconsistent.getRotation()+1, ours);
                    displayState(sensorState());
                    //bug fix, must repaint icons that have same width and height
                    repaint();
                }
            });
            popup.add(new AbstractAction(rb.getString("EditIcon")) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
            if (getLayoutPanel()==null)
                addTextEditEntry(popup);
        } else { //This else statement is from the layoutSensorIcon
            popup.add(new AbstractAction(rb.getString("SetFixedSize")) {
				public void actionPerformed(ActionEvent e) {
					String name = getNameString();
					fixedSizeEdit(name);
				}
			});
            if(getFixedHeight()==0){
                popup.add(new AbstractAction(rb.getString("SetMarginSize")) {
                    public void actionPerformed(ActionEvent e) {
                        String name = getNameString();
                        marginSizeEdit(name);
                    }
                });
            }
            /*popup.add(new AbstractAction(rb.getString("SetSensorText")) {
				public void actionPerformed(ActionEvent e) {
					String name = getNameString();
					SensorTextEdit(name);
				}
			});*/
            //popup.add(makeFontSizeMenu());
            //popup.add(makeFontStyleMenu());
            addTextEditEntry(popup);
            JMenu stateColor = new JMenu(rb.getString("StateColors"));
                stateColor.add(stateMenu(rb.getString("Unknown"), 0x00)); //Unknown
                stateColor.add(stateMenu(rb.getString("SensorActive"), 0x02)); //Active
                stateColor.add(stateMenu(rb.getString("SensorInactive"), 0x04)); //Inactive
                stateColor.add(stateMenu(rb.getString("Inconsistent"), 0x06)); //Inconsistent
            popup.add(stateColor);

            popup.add(textBorderMenu(getNameString()));
            addFixedItem(popup);
            addShowTooltipItem(popup);
        }

        //checkLocationEditable(popup, getNameString());
        addFixedItem(popup);

        addDisableMenuEntry(popup);

        momentaryItem = new JCheckBoxMenuItem(rb.getString("Momentary"));
        popup.add(momentaryItem);
        momentaryItem.setSelected (getMomentary());
        momentaryItem.addActionListener(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setMomentary(momentaryItem.isSelected());
            }
        });



        popup.add(new AbstractAction(rb.getString("Remove")) {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });
        //This statement is from the layout editor.
        if(icon){
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
        popup.add(setHiddenMenu());

        popup.show(e.getComponent(), e.getX(), e.getY());
    }
    
    void addTextEditEntry(JPopupMenu popup) {
        JMenu edit = new JMenu(rb.getString("EditText"));
        popup.add(edit);
        edit.add(makeFontSizeMenu());
        edit.add(makeFontStyleMenu());
        edit.add(new AbstractAction(rb.getString("SetSensorText")) {
				public void actionPerformed(ActionEvent e) {
					String name = getNameString();
					SensorTextEdit(name);
				}
			});
    }

    void scale(int s) {
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

        /*updateSize();

        switch (state) {
        case Sensor.UNKNOWN:
            if (icon) super.setIcon(unknown);
            break;
        case Sensor.ACTIVE:
            if (icon) super.setIcon(active);
            break;
        case Sensor.INACTIVE:
            if (icon) super.setIcon(inactive);
            break;
        default:
            if (icon) super.setIcon(inconsistent);
            break;
        }
        setIconTextGap (-(getWidth()+getPreferredSize().width)/2);
        setSize(getPreferredSize().width, getPreferredSize().height);

        return;*/
        //Replacement code from layout editor
        updateSize();
        switch (state) {
            case Sensor.UNKNOWN:
                if (icon) super.setIcon(unknown);
                else if (text) {super.setText(unknownText);
                            super.setBackground(backgroundColorUnknown);
                            super.setForeground(textColorUnknown);
                            //super.setOpaque(true);
                            }
                break;
            case Sensor.ACTIVE:
                if (icon) super.setIcon(active);
                else if (text) {super.setText(activeText);
                            super.setBackground(backgroundColorActive);
                            super.setForeground(textColorActive);
                            //super.setOpaque(true);
                            }
                break;
            case Sensor.INACTIVE:
                if (icon) super.setIcon(inactive);
                else if (text) {super.setText(inactiveText);
                            super.setBackground(backgroundColorInActive);
                            super.setForeground(textColorInActive);
                            //super.setOpaque(true);
                            }
                break;
            default:
                if (icon) super.setIcon(inconsistent);
                else if (text) {super.setText(inconsistentText);
                            super.setBackground(backgroundColorInconsistent);
                            super.setForeground(textColorInconsistent);
                            //super.setOpaque(true);
                            }
                break;
        }
        if (getLayoutPanel()==null){
            setIconTextGap (-(getWidth()+getPreferredSize().width)/2);
            setSize(getPreferredSize().width, getPreferredSize().height);
            //setSize(maxWidth(), getPreferredSize().height);
        }
        return;
    }

    void edit() {
        if (_editorFrame != null) {
            _editorFrame.setLocationRelativeTo(null);
            _editorFrame.toFront();
            return;
        }
        _editor = new IconAdder();
        _editor.setIcon(3, "SensorStateActive", getActiveIcon());
        _editor.setIcon(2, "SensorStateInactive", getInactiveIcon());
        _editor.setIcon(0, "BeanStateInconsistent", getInconsistentIcon());
        _editor.setIcon(1, "BeanStateUnknown", getUnknownIcon());

        makeAddIconFrame("EditSensor", "addIconsToPanel", "SelectSensor", _editor);
        _editor.makeIconPanel();
        _editor.setPickList(PickListModel.sensorPickModelInstance());


        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateSensor();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _editor.addCatalog();
                    _editorFrame.pack();
                }
        };
        _editor.complete(addIconAction, changeIconAction, true, true);
        _editor.setSelection(sensor);
    }
    void updateSensor() {
        setActiveIcon(_editor.getIcon("SensorStateActive"));
        setInactiveIcon(_editor.getIcon("SensorStateInactive"));
        setInconsistentIcon(_editor.getIcon("BeanStateInconsistent"));
        setUnknownIcon(_editor.getIcon("BeanStateUnknown"));
        setSensor((Sensor)_editor.getTableSelection());
        _editorFrame.dispose();
        _editorFrame = null;
        _editor = null;
        invalidate();
    }
    /* Original text is used when changing between icon and text, this allows for a undo when reverting back. */
    String originalText;
    public void setOriginalText(String s) {
        originalText=s;
    }
    public String getOriginalText() { return originalText; }
    
    public void setText(String s) {
        text = true;
        super.setText(s);
    }

    //Replace with new code from the layout editor.
    /*protected int maxHeight() {
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
    }*/

    boolean momentary = false;
    public boolean getMomentary() { return momentary; }
    public void setMomentary(boolean m) { momentary = m; }

    boolean buttonLive() {
        if (!getControlling()) return false;
        if (getForceControlOff()) return false;
        if (sensor==null) {  // no sensor connected for this protocol
            log.error("No sensor connection, can't process click");
            return false;
        }
        return true;
    }

    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        if (e.isMetaDown() || e.isAltDown() ) return;
        if (getMomentary() && buttonLive()) {
            // this is a momentary button
            try {
                sensor.setKnownState(jmri.Sensor.ACTIVE);
            } catch (jmri.JmriException reason) {
                log.warn("Exception setting momentary sensor: "+reason);
            }
        }
        // do rest of mouse processing - From Layout Editor
        //super.mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
        if (getLayoutPanel()!=null){
            if (getMomentary() && buttonLive()) {
            // this is a momentary button
                try {
                    sensor.setKnownState(jmri.Sensor.INACTIVE);
                } catch (jmri.JmriException reason) {
                    log.warn("Exception setting momentary sensor: "+reason);
                }
            }
            // do rest of mouse processing
            super.mouseReleased(e);
            return;

        }
        super.mouseReleased(e);
        if (e.isMetaDown() || e.isAltDown() ) return;
        if (buttonLive()) {
            if (getMomentary()) {
                // this is a momentary button
                try {
                    sensor.setKnownState(jmri.Sensor.INACTIVE);
                } catch (jmri.JmriException reason) {
                    log.warn("Exception setting momentary sensor: "+reason);
                }
            } else {
                try {
                    if (sensor.getKnownState()==jmri.Sensor.INACTIVE)
                        sensor.setKnownState(jmri.Sensor.ACTIVE);
                    else
                        sensor.setKnownState(jmri.Sensor.INACTIVE);
                } catch (jmri.JmriException reason) {
                    log.warn("Exception flipping sensor: "+reason);
                }
            }
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

    //ButtonGroup colorButtonGroup = null;
    //ButtonGroup colorBackButtonGroup = null;

    ButtonGroup colorUnknownButtonGroup = null;
    ButtonGroup colorUnknownBackButtonGroup = null;
    ButtonGroup colorActiveButtonGroup = null;
    ButtonGroup colorActiveBackButtonGroup = null;
    ButtonGroup colorInActiveButtonGroup = null;
    ButtonGroup colorInActiveBackButtonGroup = null;
    ButtonGroup colorInconsistentButtonGroup = null;
    ButtonGroup colorInconsistentBackButtonGroup = null;
    //ButtonGroup colorBorderButtonGroup = null;

    JMenu stateMenu(final String name, int state) {
        JMenu menu = new JMenu(name);
        menu.add(makeFontColorMenu(state));
        menu.add(makeBackgroundFontColorMenu(state+1));
        return menu;
    }


    protected JMenu makeFontColorMenu(int state) {
        JMenu colorMenu = new JMenu(rb.getString("FontColor"));
        colorButtonGroup = new ButtonGroup();
        addColorMenuEntry(colorMenu, rb.getString("Black"), Color.black, state);
        addColorMenuEntry(colorMenu, rb.getString("DarkGray"),Color.darkGray, state);
        addColorMenuEntry(colorMenu, rb.getString("Gray"),Color.gray, state);
        addColorMenuEntry(colorMenu, rb.getString("LightGray"),Color.lightGray, state);
        addColorMenuEntry(colorMenu, rb.getString("White"),Color.white, state);
        addColorMenuEntry(colorMenu, rb.getString("Red"),Color.red, state);
        addColorMenuEntry(colorMenu, rb.getString("Orange"),Color.orange, state);
        addColorMenuEntry(colorMenu, rb.getString("Yellow"),Color.yellow, state);
        addColorMenuEntry(colorMenu, rb.getString("Green"),Color.green, state);
        addColorMenuEntry(colorMenu, rb.getString("Blue"),Color.blue, state);
        addColorMenuEntry(colorMenu, rb.getString("Magenta"),Color.magenta, state);
        return colorMenu;
    }

    protected JMenu makeBackgroundFontColorMenu(int state) {
        JMenu colorMenu = new JMenu(rb.getString("FontBackgroundColor"));
        colorBackButtonGroup = new ButtonGroup();
        addColorMenuEntry(colorMenu, rb.getString("Black"), Color.black, state);
        addColorMenuEntry(colorMenu, rb.getString("DarkGray"),Color.darkGray, state);
        addColorMenuEntry(colorMenu, rb.getString("Gray"),Color.gray, state);
        addColorMenuEntry(colorMenu, rb.getString("LightGray"),Color.lightGray, state);
        addColorMenuEntry(colorMenu, rb.getString("White"),Color.white, state);
        addColorMenuEntry(colorMenu, rb.getString("Red"),Color.red, state);
        addColorMenuEntry(colorMenu, rb.getString("Orange"),Color.orange, state);
        addColorMenuEntry(colorMenu, rb.getString("Yellow"),Color.yellow, state);
        addColorMenuEntry(colorMenu, rb.getString("Green"),Color.green, state);
        addColorMenuEntry(colorMenu, rb.getString("Blue"),Color.blue, state);
        addColorMenuEntry(colorMenu, rb.getString("Magenta"),Color.magenta, state);
        //addColorMenuEntry(colorMenu, rb.getString("Clear"),new Color(238, 238, 238), state);
        addColorMenuEntry(colorMenu, rb.getString("Clear"),null, state);
        return colorMenu;
    }

    protected JMenu makeBorderColorMenu() {
        JMenu colorMenu = new JMenu(rb.getString("ColorMenu"));
        colorBorderButtonGroup = new ButtonGroup();
        addColorMenuEntry(colorMenu, rb.getString("Black"), Color.black, 0x08);
        addColorMenuEntry(colorMenu, rb.getString("DarkGray"),Color.darkGray, 0x08);
        addColorMenuEntry(colorMenu, rb.getString("Gray"),Color.gray, 0x08);
        addColorMenuEntry(colorMenu, rb.getString("LightGray"),Color.lightGray, 0x08);
        addColorMenuEntry(colorMenu, rb.getString("White"),Color.white, 0x08);
        addColorMenuEntry(colorMenu, rb.getString("Red"),Color.red, 0x08);
        addColorMenuEntry(colorMenu, rb.getString("Orange"),Color.orange, 0x08);
        addColorMenuEntry(colorMenu, rb.getString("Yellow"),Color.yellow, 0x08);
        addColorMenuEntry(colorMenu, rb.getString("Green"),Color.green, 0x08);
        addColorMenuEntry(colorMenu, rb.getString("Blue"),Color.blue, 0x08);
        addColorMenuEntry(colorMenu, rb.getString("Magenta"),Color.magenta, 0x08);
        addColorMenuEntry(colorMenu, rb.getString("Clear"),null, 0x08);
        return colorMenu;
    }

    void addColorMenuEntry(JMenu menu, final String name, final Color color, final int state) {
        //state foreground - 0x00 Unknown, 0x02 Active, 0x04 InActive, 0x06 Inconsistant, 0x08 border,
        //background 0x01 Unknown, 0x03 Active, 0x05 Inactive, 0x07 Inconsistant.
        ActionListener a = new ActionListener() {
            //final String desiredName = name;
            final Color desiredColor = color;
            public void actionPerformed(ActionEvent e) {
                switch (state){
                    case 0x00 : setTextUnknown(desiredColor); break;
                    case 0x01 : setBackgroundUnknown(desiredColor); break;
                    case 0x02 : setTextActive(desiredColor); break;
                    case 0x03 : setBackgroundActive(desiredColor); break;
                    case 0x04 : setTextInActive(desiredColor); break;
                    case 0x05 : setBackgroundInActive(desiredColor); break;
                    case 0x06 : setTextInconsistent(desiredColor); break;
                    case 0x07 : setBackgroundInconsistent(desiredColor); break;
                    case 0x08 : setBorderColor(desiredColor); break;
                }
            }
        };
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
        r.addActionListener(a);

        switch (state) {
            case 0x00 : colorUnknownButtonGroup = new ButtonGroup();
                        colorUnknownButtonGroup.add(r);
                        if (getTextUnknown()!=null){
                            if (getTextUnknown().getRGB() == color.getRGB())  r.setSelected(true);
                            else r.setSelected(false);
                        }
                            break;
            case 0x01 : colorUnknownBackButtonGroup = new ButtonGroup();
                        colorUnknownBackButtonGroup.add(r);
                        if ((color!=null) && (getBackgroundUnknown()!=null)){
                            if (getBackgroundUnknown().getRGB() == color.getRGB()) r.setSelected(true);
                            else r.setSelected(false);
                        } else {
                            if ((getBackgroundUnknown() == null)&& (color==null)) r.setSelected(true);
                            else r.setSelected(false);
                        }
                        break;

            case 0x02 : colorActiveButtonGroup = new ButtonGroup();
                        colorActiveButtonGroup.add(r);
                        if(getTextActive()!=null){
                            if (getTextActive().getRGB() == color.getRGB()) r.setSelected(true);
                            else r.setSelected(false);
                        }
                        break;
            case 0x03 : colorActiveBackButtonGroup = new ButtonGroup();
                        colorActiveBackButtonGroup.add(r);
                        if ((color!=null) && (getBackgroundActive()!=null)){
                            if (getBackgroundActive().getRGB() == color.getRGB()) r.setSelected(true);
                            else r.setSelected(false);
                        } else {
                            if ((getBackgroundActive() == null)&& (color==null)) r.setSelected(true);
                            else r.setSelected(false);
                        }
                        break;

            case 0x04 : colorInActiveButtonGroup = new ButtonGroup();
                        colorInActiveButtonGroup.add(r);
                        if (getTextInActive()!=null){
                            if (getTextInActive().getRGB() == color.getRGB()) r.setSelected(true);
                            else r.setSelected(false);
                        }
                        break;

            case 0x05 : colorInActiveBackButtonGroup = new ButtonGroup();
                        colorInActiveBackButtonGroup.add(r);
                        if ((color!=null) && (getBackgroundInActive()!=null)){
                            if (getBackgroundInActive().getRGB() == color.getRGB()) r.setSelected(true);
                            else r.setSelected(false);
                        } else {
                            if ((getBackgroundInActive() == null)&& (color==null)) r.setSelected(true);
                            else r.setSelected(false);
                        }
                        break;

            case 0x06 : colorInconsistentButtonGroup = new ButtonGroup();
                        colorInconsistentButtonGroup.add(r);
                        if (getTextInconsistent()!=null){
                            if (getTextInconsistent().getRGB() == color.getRGB()) r.setSelected(true);
                            else r.setSelected(false);
                        }
                        break;

            case 0x07 : colorInconsistentBackButtonGroup = new ButtonGroup();
                        colorInconsistentBackButtonGroup.add(r);
                        if ((color!=null) && (getBackgroundInconsistent()!=null)){
                            if (getBackgroundInconsistent().getRGB() == color.getRGB()) r.setSelected(true);
                            else r.setSelected(false);
                        } else {
                            if ((getBackgroundInconsistent() == null) && (color==null)) r.setSelected(true);
                            else r.setSelected(false);
                        }
                        break;

            case 0x08 : colorBorderButtonGroup = new ButtonGroup();
                        colorBorderButtonGroup.add(r);
                        if( getBorderColor()!=null)
                            if (getBorderColor().getRGB() == color.getRGB()) r.setSelected(true);
                        else r.setSelected(false);
        }
        menu.add(r);
    }

    void ChangeLayoutSensorType(){
        if (getLayoutPanel()!=null)
            new SensorChangeType(this, getLayoutPanel());
        else
            new SensorChangeType(this, getPanelEditor());
    }

    public void SensorTextEdit(String name) {
		if (log.isDebugEnabled())
			log.debug("make new coordinate menu");
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

    protected int maxHeight() {
        if(icon) return Math.max(
                Math.max( (active!=null) ? active.getIconHeight() : 0,
                        (inactive!=null) ? inactive.getIconHeight() : 0),
                Math.max((unknown!=null) ? unknown.getIconHeight() : 0,
                        (inconsistent!=null) ? inconsistent.getIconHeight() : 0)
            );
        else{
            if ((getFixedHeight()==0) && (getMargin()==0))
                return ((javax.swing.JLabel)this).getMaximumSize().height; // defer to superclass
            else if ((getFixedHeight()==0) && (getMargin()!=0))
                return ((javax.swing.JLabel)this).getMaximumSize().height+(getMargin()*2);
            return getFixedHeight();
        //return ((javax.swing.JLabel)this).getMaximumSize().height;
        }
    }
    protected int maxWidth() {
        /*if((icon) && (text)) return Math.max(((javax.swing.JLabel)this).getMaximumSize().width,Math.max(
                Math.max((active!=null) ? active.getIconWidth() : 0,
                        (inactive!=null) ? inactive.getIconWidth() : 0),
                Math.max((unknown!=null) ? unknown.getIconWidth() : 0,
                        (inconsistent!=null) ? inconsistent.getIconWidth() : 0)
            ));*/
        if(icon) return Math.max(
                Math.max((active!=null) ? active.getIconWidth() : 0,
                        (inactive!=null) ? inactive.getIconWidth() : 0),
                Math.max((unknown!=null) ? unknown.getIconWidth() : 0,
                        (inconsistent!=null) ? inconsistent.getIconWidth() : 0)
            );
        else {
            if ((getFixedWidth()==0) && (getMargin()==0))
                return ((javax.swing.JLabel)this).getMaximumSize().width; // defer to superclass
            else if ((getFixedWidth()==0) && (getMargin()!=0))
                return ((javax.swing.JLabel)this).getMaximumSize().width+(getMargin()*2);
            return getFixedWidth();

        }//return
    }


    /**
     * (Temporarily) change occupancy on click
     * @param e
     */
    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (getLayoutPanel()!=null)
            super.layoutPanel.handleMouseClicked(e, getX(), getY());
	}

	protected void performMouseClicked(java.awt.event.MouseEvent e) {
        if(getLayoutPanel()!=null){
            if (e.isAltDown() || e.isMetaDown()) return;
            if (getMomentary()) return; // click is only for non-momentary
            if (!buttonLive()) return;
            super.layoutPanel.setAwaitingIconChange();
            try {
                if (sensor.getKnownState()==jmri.Sensor.INACTIVE) {
                    sensor.setKnownState(jmri.Sensor.ACTIVE);
                }
                else {
                    sensor.setKnownState(jmri.Sensor.INACTIVE);
                }
            } catch (jmri.JmriException reason) {
                log.warn("Exception flipping sensor: "+reason);
            }
        }
    }




    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SensorIcon.class.getName());
}
