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
import javax.swing.JMenu;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JLabel;

import java.awt.Color;

import java.util.ResourceBundle;

/**
 * This module provides an icon to display a status of a Sensor on a LayoutEditor panel.
 *   This routine is almost identical to SensorIcon.java, written by Bob Jacobsen.  
 *   Differences are related to the hard interdependence between SensorIconXml.java and 
 *   PanelEditor.java, which made it impossible to use SensorIcon.java directly with 
 *   LayoutEditor. Rectifying these differences is especially important when storing and
 *   loading a saved panel. 
 * <P>
 * This module has been chaanged (from SensorIcon.java) to use a resource bundle for 
 *	its user-seen text, like other Layout Editor modules.
 *
 * @author David J. Duchamp Copyright (C) 2007
 * @version $Revision: 1.12 $
 *
 *  (Copied with minor changes from SensorIcon.java)
 */

public class LayoutSensorIcon extends LayoutPositionableLabel implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.LayoutEditorBundle");
    Color clear = new Color(238,238,238);
    public LayoutSensorIcon(NamedIcon s) {
        // super ctor call to make sure this is an icon label
        super(s);
        icon = true;
        text = false;
        active = new NamedIcon(activeName, activeName);
        inactive = new NamedIcon(inactiveName, inactiveName);
        inconsistent = new NamedIcon(inconsistentName, inconsistentName);
        unknown = new NamedIcon(unknownName, unknownName);
        setDisplayLevel(LayoutEditor.SENSORS);
        displayState(sensorState());
    }
    
    public LayoutSensorIcon(String s){
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
    
    Color textColorActive;
    
    public void setTextActive(Color color){
        textColorActive=color;
    }
    
    public Color getTextActive(){
        return textColorActive;
    }
    
    Color textColorInActive;
    
    public void setTextInActive(Color color){
        textColorInActive=color;
    }

    public Color getTextInActive(){
        return textColorInActive;
    }
    
    Color textColorUnknown;
    public void setTextUnknown(Color color){
        textColorUnknown=color;
    }
    
    public Color getTextUnknown(){
        return textColorUnknown;
    }
    
    Color textColorInconsistent;
    public void setTextInconsistent(Color color){
        //super.setText(color);
        textColorInconsistent=color;
    }
    
    public Color getTextInconsistent(){
        return textColorInconsistent;
    }
    
    Color backgroundColorActive = clear;
    
    public void setBackgroundActive(Color color){
        if (color==clear)
            setOpaque(false);
        else
            setOpaque(true);
        //super.setBackground(color);
        backgroundColorActive=color;
    }
    
    public Color getBackgroundActive(){
        return backgroundColorActive;
    }
    
    Color backgroundColorInActive = clear;
    
    public void setBackgroundInActive(Color color){
        if (color==clear)
            setOpaque(false);
        else
            setOpaque(true);
        //super.setBackground(color);
        backgroundColorInActive=color;
    }

    public Color getBackgroundInActive(){
        return backgroundColorInActive;
    }
    
    Color backgroundColorUnknown = clear;
    public void setBackgroundUnknown(Color color){
        if (color==clear)
            setOpaque(false);
        else
            setOpaque(true);
        //super.setBackground(color);
        backgroundColorUnknown=color;
    }
    
    public Color getBackgroundUnknown(){
        return backgroundColorUnknown;
    }
    
    Color backgroundColorInconsistent = clear;
    public void setBackgroundInconsistent(Color color){
        if (color==clear)
            setOpaque(false);
        else
            setOpaque(true);
        //super.setBackground(color);
        backgroundColorInconsistent=color;
    }
    
    public Color getBackgroundInconsistent(){
        return backgroundColorInconsistent;
    }


    // display icons
    String activeName = "resources/icons/smallschematics/tracksegments/circuit-occupied.gif";
    NamedIcon active;

    String inactiveName = "resources/icons/smallschematics/tracksegments/circuit-empty.gif";
    NamedIcon inactive;

    String inconsistentName = "resources/icons/smallschematics/tracksegments/circuit-error.gif";
    NamedIcon inconsistent;

    String unknownName = "resources/icons/smallschematics/tracksegments/circuit-error.gif";
    NamedIcon unknown;

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
			super.layoutPanel.resetAwaitingIconChange();
			super.layoutPanel.redrawPanel();
        }
    }

    public void setProperToolTip() {
        setToolTipText(getNameString());
    }

    String getNameString() {
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
     * Display the pop-up menu
     */
    protected void showPopUp(MouseEvent e) {
        if (!getEditable()) return;
        ours = this;
		popup = new JPopupMenu();            
		popup.add(new JMenuItem(getNameString()));
		popup.add("x= " + this.getX());
		popup.add("y= " + this.getY());
		popup.add(new AbstractAction(rb.getString("SetXY")) {
				public void actionPerformed(ActionEvent e) {
					String name = getNameString();
					displayCoordinateEdit(name);
				}
			});
		if (icon) {
			popup.add(new AbstractAction(rb.getString("Rotate")) {
                    public void actionPerformed(ActionEvent e) {
                        active.setRotation(active.getRotation()+1, ours);
                        inactive.setRotation(inactive.getRotation()+1, ours);
                        unknown.setRotation(unknown.getRotation()+1, ours);
                        inconsistent.setRotation(inconsistent.getRotation()+1, ours);
                        displayState(sensorState());
                    }
			});
		} else {
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
            popup.add(new AbstractAction(rb.getString("SetSensorText")) {
				public void actionPerformed(ActionEvent e) {
					String name = getNameString();
					layoutSensorTextEdit(name);
				}
			});
            popup.add(makeFontSizeMenu());
            popup.add(makeFontStyleMenu());
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
        
        
		addDisableMenuEntry(popup);            
		momentaryItem = new JCheckBoxMenuItem(rb.getString("Momentary"));
		popup.add(momentaryItem);
		momentaryItem.setSelected (getMomentary());
		momentaryItem.addActionListener(new ActionListener() {
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
        
        // end creation of popup menu

        popup.show(e.getComponent(), e.getX(), e.getY());
    }
    
    ButtonGroup colorButtonGroup = null;
    ButtonGroup colorBackButtonGroup = null;
    ButtonGroup colorUnknownButtonGroup = null;
    ButtonGroup colorUnknownBackButtonGroup = null;
    ButtonGroup colorActiveButtonGroup = null;
    ButtonGroup colorActiveBackButtonGroup = null;
    ButtonGroup colorInActiveButtonGroup = null;
    ButtonGroup colorInActiveBackButtonGroup = null;
    ButtonGroup colorInconsistentButtonGroup = null;
    ButtonGroup colorInconsistentBackButtonGroup = null;
    ButtonGroup colorBorderButtonGroup = null;
    
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
        addColorMenuEntry(colorMenu, rb.getString("Clear"),new Color(238, 238, 238), state);
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
        addColorMenuEntry(colorMenu, rb.getString("Clear"),new Color(238, 238, 238), 0x08);
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
                        if (getTextUnknown().getRGB() == color.getRGB())  r.setSelected(true);
                        else r.setSelected(false);
                        break;
                        
            case 0x01 : colorUnknownBackButtonGroup = new ButtonGroup();
                        colorUnknownBackButtonGroup.add(r);
                        if (getBackgroundUnknown().getRGB() == color.getRGB())  r.setSelected(true);
                        else r.setSelected(false);
                        break;          

            case 0x02 : colorActiveButtonGroup = new ButtonGroup();
                        colorActiveButtonGroup.add(r);
                        if (getTextActive().getRGB() == color.getRGB())  r.setSelected(true);
                        else r.setSelected(false);
                        break;
            case 0x03 : colorActiveBackButtonGroup = new ButtonGroup();
                        colorActiveBackButtonGroup.add(r);
                        if (getBackgroundActive().getRGB() == color.getRGB())  r.setSelected(true);
                        else r.setSelected(false);
                        break;
            
            case 0x04 : colorInActiveButtonGroup = new ButtonGroup();
                        colorInActiveButtonGroup.add(r);
                        if (getTextInActive().getRGB() == color.getRGB())  r.setSelected(true);
                        else r.setSelected(false);
                        break;
                        
            case 0x05 : colorInActiveBackButtonGroup = new ButtonGroup();
                        colorInActiveBackButtonGroup.add(r);
                        if (getBackgroundInActive().getRGB() == color.getRGB())  r.setSelected(true);
                        else r.setSelected(false);
                        break;
            
            case 0x06 : colorInconsistentButtonGroup = new ButtonGroup();
                        colorInconsistentButtonGroup.add(r);
                        if (getTextInconsistent().getRGB() == color.getRGB())  r.setSelected(true);
                        else r.setSelected(false);
                        break;

            case 0x07 : colorInconsistentBackButtonGroup = new ButtonGroup();
                        colorInconsistentBackButtonGroup.add(r);
                        if (getBackgroundInconsistent().getRGB() == color.getRGB())  r.setSelected(true);
                        else r.setSelected(false);
                        break;
                  
            case 0x08 : colorBorderButtonGroup = new ButtonGroup();
                        colorBorderButtonGroup.add(r);
                        if( getBorderColor()!=null)
                            if (getBorderColor().getRGB() == color.getRGB())  r.setSelected(true);
                        else r.setSelected(false);
        }
        menu.add(r);
    }

    void ChangeLayoutSensorType(){
        new LayoutSensorChangeType(this, getPanel());
    }
    
    public void layoutSensorTextEdit(String name) {
		if (log.isDebugEnabled())
			log.debug("make new coordinate menu");
		LayoutSensorTextEdit f = new LayoutSensorTextEdit();
		f.addHelpMenu("package.jmri.jmrit.display.layoutSensorTextEdit", true);
		try {
			f.initComponents(this, name);
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
		f.setVisible(true);	
	}
    
    JCheckBoxMenuItem momentaryItem;
    
    /**
     * Drive the current state of the display from the state of the
     * turnout.
     */
    void displayState(int state) {

        switch (state) {
        case Sensor.UNKNOWN:
            if (text) {super.setText(unknownText);
                        super.setBackground(backgroundColorUnknown);
                        super.setForeground(textColorUnknown);
                        super.setOpaque(true);
                        }
            if (icon) super.setIcon(unknown);
            break;
        case Sensor.ACTIVE:
            if (text) {super.setText(activeText);
                        super.setBackground(backgroundColorActive);
                        super.setForeground(textColorActive);
                        super.setOpaque(true);
                        }
            if (icon) super.setIcon(active);
            break;
        case Sensor.INACTIVE:
            if (text) {super.setText(inactiveText);
                        super.setBackground(backgroundColorInActive);
                        super.setForeground(textColorInActive);
                        super.setOpaque(true);
                        }
            if (icon) super.setIcon(inactive);
            break;
        default:
            if (text) {super.setText(inconsistentText);
                        super.setBackground(backgroundColorInconsistent);
                        super.setForeground(textColorInconsistent);
                        super.setOpaque(true);
                        }
            if (icon) super.setIcon(inconsistent);
            break;
        }
        updateSize();
        return;
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

    boolean momentary = false;
    public boolean getMomentary() { return momentary; }
    public void setMomentary(boolean m) { momentary = m; }
    
    /**
     * (Temporarily) change occupancy on click
     * @param e
     */
    public void mouseClicked(java.awt.event.MouseEvent e) {
		super.layoutPanel.handleMouseClicked(e, getX(), getY());	
	}
	
	protected void performMouseClicked(java.awt.event.MouseEvent e) {
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
        if (getMomentary() && buttonLive()) {
            // this is a momentary button
            try {
                sensor.setKnownState(jmri.Sensor.ACTIVE);
            } catch (jmri.JmriException reason) {
                log.warn("Exception setting momentary sensor: "+reason);
            }        
        }
        // do rest of mouse processing
        super.mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
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
    }
 
    public void dispose() {
        sensor.removePropertyChangeListener(this);
        sensor = null;

        active = null;
        inactive = null;
        inconsistent = null;
        unknown = null;
        activeText = null;
        inactiveText = null;
        inconsistentText = null;
        unknownText = null;

        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutSensorIcon.class.getName());
}
