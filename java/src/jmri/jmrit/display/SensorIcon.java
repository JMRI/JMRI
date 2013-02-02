package jmri.jmrit.display;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.NamedBeanHandle;
import jmri.jmrit.display.palette.TableItemPanel;
import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.catalog.NamedIcon;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.Color;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.Timer;
//import javax.swing.JLabel;

/**
 * An icon to display a status of a Sensor.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author PeteCressman Copyright (C) 2010, 2011
 * @version $Revision$
 */

public class SensorIcon extends PositionableIcon implements java.beans.PropertyChangeListener {

    static final public int UNKOWN_FONT_COLOR =      0x03;
    static final public int UNKOWN_BACKGROUND_COLOR =    0x04;
    static final public int ACTIVE_FONT_COLOR =          0x05;
    static final public int ACTIVE_BACKGROUND_COLOR =    0x06;
    static final public int INACTIVE_FONT_COLOR =        0x07;
    static final public int INACTIVE_BACKGROUND_COLOR =  0x08;
    static final public int INCONSISTENT_FONT_COLOR =        0x0A;
    static final public int INCONSISTENT_BACKGROUND_COLOR =  0x0B;
    private boolean debug = false;

    protected Hashtable <String, Integer> _name2stateMap;       // name to state
    protected Hashtable <Integer, String> _state2nameMap;       // state to name

    public SensorIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        this(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                            "resources/icons/smallschematics/tracksegments/circuit-error.gif"), editor);
    }

    public SensorIcon(NamedIcon s, Editor editor) {
        // super ctor call to make sure this is an icon label
        super(s, editor);
        setOpaque(false);
        _control = true;
        debug = log.isDebugEnabled();
        setPopupUtility(new SensorPopupUtil(this, this));
    }

    public SensorIcon(String s, Editor editor){
        super(s, editor);
        _control = true;
        debug = log.isDebugEnabled();
        displayState(sensorState());
        setPopupUtility(new SensorPopupUtil(this, this));
    }

    @Override
    public Positionable deepClone() {
    	SensorIcon pos = new SensorIcon(_editor);
        return finishClone(pos);
    }

    @Override
    public Positionable finishClone(Positionable p) {
        SensorIcon pos = (SensorIcon)p;
        pos.setSensor(getNamedSensor().getName());
        pos.makeIconMap();
        pos._iconMap = cloneMap(_iconMap, pos);
        pos.setMomentary(getMomentary());
        pos.originalText = originalText;
        pos.setText(getText());
        pos.setIcon(null);
        pos._namedIcon = null;
        pos.activeText = activeText;
        pos.inactiveText = inactiveText;
        pos.inconsistentText = inconsistentText;
        pos.unknownText = unknownText;
        pos.textColorInconsistent = textColorInconsistent;
        pos.textColorUnknown = textColorUnknown;
        pos.textColorInActive = textColorInActive;
        pos.textColorActive = textColorActive;
        pos.backgroundColorInActive = backgroundColorInActive;
        pos.backgroundColorActive = backgroundColorActive;
        pos.backgroundColorUnknown = backgroundColorUnknown;
        pos.backgroundColorInconsistent = backgroundColorInconsistent;
        return super.finishClone(pos);
    }

    // the associated Sensor object
    private NamedBeanHandle<Sensor> namedSensor;

    /**
     * Attached a named sensor to this display item
     * @param pName System/user name to lookup the sensor object
     */
    public void setSensor(String pName) {
        if (InstanceManager.sensorManagerInstance()!=null) {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
            if (sensor != null) {
                setSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor));
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
            if (_iconMap==null) {
                makeIconMap();
            }
 //           displayState(sensorState());
            getSensor().addPropertyChangeListener(this, s.getName(), "SensorIcon on Panel " + _editor.getName());
            setName(namedSensor.getName());  // Swing name for e.g. tests
        }
        setAttributes();
    }
    
    private void setAttributes() {
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
                        activeText=Bundle.getMessage("SensorActive");
                    if (inactiveText==null)
                        inactiveText = Bundle.getMessage("SensorInactive");
                    if (inconsistentText==null)
                        inconsistentText=Bundle.getMessage("Inconsistent");
                    if (unknownText==null)
                        unknownText=Bundle.getMessage("Unknown");
                }
            }
            if (activeText==null) {
                activeText=Bundle.getMessage("SensorActive");
                textColorActive=Color.red;
            }
            if (inactiveText==null) {
                inactiveText = Bundle.getMessage("SensorInactive");
                textColorInActive=Color.yellow;
            }
                //inactiveText = Bundle.getMessage("SensorInactive");
            if (inconsistentText==null)
                inconsistentText=Bundle.getMessage("Inconsistent");
            if (unknownText==null)
                unknownText=Bundle.getMessage("Unknown");
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
        if (namedSensor==null) {
            return null;
        }
        return namedSensor.getBean();
    }
    
    public jmri.NamedBean getNamedBean(){
        return getSensor();
    }
    
    public NamedBeanHandle <Sensor> getNamedSensor() {
        return namedSensor;
    }

    void makeIconMap() {
        _iconMap = new Hashtable <String, NamedIcon>();
        _name2stateMap = new Hashtable <String, Integer>();
        _name2stateMap.put("BeanStateUnknown", Integer.valueOf(Sensor.UNKNOWN));
        _name2stateMap.put("BeanStateInconsistent", Integer.valueOf(Sensor.INCONSISTENT));
        _name2stateMap.put("SensorStateActive", Integer.valueOf(Sensor.ACTIVE));
        _name2stateMap.put("SensorStateInactive", Integer.valueOf(Sensor.INACTIVE));
        _state2nameMap = new Hashtable <Integer, String>();
        _state2nameMap.put(Integer.valueOf(Sensor.UNKNOWN), "BeanStateUnknown");
        _state2nameMap.put(Integer.valueOf(Sensor.INCONSISTENT), "BeanStateInconsistent");
        _state2nameMap.put(Integer.valueOf(Sensor.ACTIVE), "SensorStateActive");
        _state2nameMap.put(Integer.valueOf(Sensor.INACTIVE), "SensorStateInactive");
    }
    /**
    * Place icon by its bean state name key found in jmri.NamedBeanBundle.properties
    * That is, by its localized bean state name
    */
    public void setIcon(String name, NamedIcon icon) {
        if (log.isDebugEnabled()) log.debug("setIcon for name \""+name+"\"");
        if (_iconMap==null) {
            makeIconMap();
        }
        _iconMap.put(name, icon);
        displayState(sensorState());
    }

    /**
    * Get icon by its localized bean state name
    */
    @Override
    public NamedIcon getIcon(String state) {
        return _iconMap.get(state);
    }
    public NamedIcon getIcon(int state) {
        return _iconMap.get(_state2nameMap.get(state));
    }

    @Override
    public String getFamily() {
        return _iconFamily;
    }
    @Override
    public void setFamily(String family) {
        _iconFamily = family;
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
        if (debug) log.debug("property change: "+e);
        if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue()).intValue();
            displayState(now);
            _editor.repaint();
        }
    }

    @Override
    public String getNameString() {
        String name;
        if (namedSensor == null) name = Bundle.getMessage("NotConnected");
        else if (getSensor().getUserName()==null)
            name = getSensor().getSystemName();
        else
            name = getSensor().getUserName()+" ("+getSensor().getSystemName()+")";
        return name;
    }

    JCheckBoxMenuItem  momentaryItem = new JCheckBoxMenuItem(Bundle.getMessage("Momentary"));
    /**
     * Pop-up just displays the sensor name
     */
    @Override
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            if(isIcon()){
                popup.add(new AbstractAction(Bundle.getMessage("ChangeToText")) {
                    public void actionPerformed(ActionEvent e) {
                        changeLayoutSensorType();
                    }
                });
            } else {
                popup.add(new AbstractAction(Bundle.getMessage("ChangeToIcon")) {
                    public void actionPerformed(ActionEvent e) {
                        changeLayoutSensorType();
                    }
                });
            }

            popup.add(momentaryItem);
            momentaryItem.setSelected (getMomentary());
            momentaryItem.addActionListener(new ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setMomentary(momentaryItem.isSelected());
                }
            });
        } else {
            if(getPopupUtility()!=null)
                getPopupUtility().setAdditionalViewPopUpMenu(popup);
        }
        return true;
    }
    /******** popup AbstractAction.actionPerformed method overrides *********/

    // overide
    @Override
    public boolean setTextEditMenu(JPopupMenu popup) {
        if (debug) log.debug("setTextEditMenu isIcon="+isIcon()+", isText="+isText());
        if (isIcon()) {
            popup.add(CoordinateEdit.getTextEditAction(this, "OverlayText"));
        } else {
            popup.add(new AbstractAction(Bundle.getMessage("SetSensorText")) {
                public void actionPerformed(ActionEvent e) {
                    String name = getNameString();
                    sensorTextEdit(name);
                }
            });
            if (isText() && !isIcon()) {
                JMenu stateColor = new JMenu(Bundle.getMessage("StateColors"));
                    stateColor.add(stateMenu(Bundle.getMessage("Unknown"), UNKOWN_FONT_COLOR)); //Unknown
                    stateColor.add(stateMenu(Bundle.getMessage("SensorActive"), ACTIVE_FONT_COLOR)); //Active
                    stateColor.add(stateMenu(Bundle.getMessage("SensorInactive"), INACTIVE_FONT_COLOR)); //Inactive
                    stateColor.add(stateMenu(Bundle.getMessage("Inconsistent"), INCONSISTENT_FONT_COLOR)); //Inconsistent
                popup.add(stateColor);
            }
        }
        return true;
    }

    public void sensorTextEdit(String name) {
        if (debug) log.debug("make text edit menu");

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
    
    /**
     * Drive the current state of the display from the state of the
     * turnout.
     */
    public void displayState(int state) {
        if (getNamedSensor() == null) {
            log.debug("Display state "+state+", disconnected");
        } else if (isIcon()) {
            NamedIcon icon = getIcon(state);
            if (icon!=null) {
                super.setIcon(icon);
            }
        } else if (isText()) {
            switch (state) {
                case Sensor.UNKNOWN:
                    super.setText(unknownText);
                    getPopupUtility().setBackgroundColor(backgroundColorUnknown);
                    getPopupUtility().setForeground(textColorUnknown);
                    break;
                case Sensor.ACTIVE:
                    super.setText(activeText);
                    getPopupUtility().setBackgroundColor(backgroundColorActive);
                    getPopupUtility().setForeground(textColorActive);
                    break;
                case Sensor.INACTIVE:
                    super.setText(inactiveText);
                    getPopupUtility().setBackgroundColor(backgroundColorInActive);
                    getPopupUtility().setForeground(textColorInActive);
                    break;
                default:
                    super.setText(inconsistentText);
                    getPopupUtility().setBackgroundColor(backgroundColorInconsistent);
                    getPopupUtility().setForeground(textColorInconsistent);
                    break;
            }
            rotate(getDegrees());
        }

        updateSize();
    }

    TableItemPanel _itemPanel;

    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("Sensor"));
        popup.add(new AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                    editItem();
                }
            });
        return true;
    }
    
    protected void editItem() {
        makePalettteFrame(java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("Sensor")));
        _itemPanel = new TableItemPanel(_paletteFrame, "Sensor", _iconFamily,
                                       PickListModel.sensorPickModelInstance(), _editor);
        ActionListener updateAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        };
        // duplicate _iconMap map with unscaled and unrotated icons
        Hashtable<String, NamedIcon> map = new Hashtable<String, NamedIcon>();
        Iterator<Entry<String, NamedIcon>> it = _iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            NamedIcon oldIcon = entry.getValue();
            NamedIcon newIcon = cloneIcon(oldIcon, this);
            newIcon.rotate(0, this);
            newIcon.scale(1.0, this);
            newIcon.setRotation(4, this);
            map.put(entry.getKey(), newIcon);
        }
        _itemPanel.init(updateAction, map);
        _itemPanel.setSelection(getSensor());
        _paletteFrame.add(_itemPanel);
        _paletteFrame.pack();
        _paletteFrame.setVisible(true);
    }

    void updateItem() {
        Hashtable<String, NamedIcon> oldMap = cloneMap(_iconMap, this);
        setSensor(_itemPanel.getTableSelection().getSystemName());
        _iconFamily = _itemPanel.getFamilyName();
        Hashtable <String, NamedIcon> iconMap = _itemPanel.getIconMap();
        if (iconMap!=null) {
            Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                if (log.isDebugEnabled()) log.debug("key= "+entry.getKey());
                NamedIcon newIcon = entry.getValue();
                NamedIcon oldIcon = oldMap.get(entry.getKey());
                newIcon.setLoad(oldIcon.getDegrees(), oldIcon.getScale(), this);
                newIcon.setRotation(oldIcon.getRotation(), this);
                setIcon(entry.getKey(), newIcon);
            }
        }   // otherwise retain current map
//        jmri.jmrit.catalog.ImageIndexEditor.checkImageIndex();
        _paletteFrame.dispose();
        _paletteFrame = null;
        _itemPanel.dispose();
        _itemPanel = null;
        invalidate();
    }

    @Override
    public boolean setEditIconMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("Sensor"));
        popup.add(new AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
        return true;
    }

    @Override
    protected void edit() {
        makeIconEditorFrame(this, "Sensor", true, null);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.sensorPickModelInstance());
        Enumeration <String> e = _iconMap.keys();
        int i = 0;
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            _iconEditor.setIcon(i++, /*_state2nameMap.get(key)*/ key, _iconMap.get(key));
        }
        _iconEditor.makeIconPanel(false);

        // set default icons, then override with this turnout's icons
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateSensor();
            }
        };
        _iconEditor.complete(addIconAction, true, true, true);
        _iconEditor.setSelection(getSensor());
    }
    
    void updateSensor() {
        Hashtable<String, NamedIcon> oldMap = cloneMap(_iconMap, this);
        setSensor(_iconEditor.getTableSelection().getDisplayName());
        Hashtable <String, NamedIcon> iconMap = _iconEditor.getIconMap();

        Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            if (log.isDebugEnabled()) log.debug("key= "+entry.getKey());
            NamedIcon newIcon = entry.getValue();
            NamedIcon oldIcon = oldMap.get(entry.getKey());
            newIcon.setLoad(oldIcon.getDegrees(), oldIcon.getScale(), this);
            newIcon.setRotation(oldIcon.getRotation(), this);
            setIcon(entry.getKey(), newIcon);
        }
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }
    
    // Original text is used when changing between icon and text, this allows for a undo when reverting back. 
    String originalText;
    public void setOriginalText(String s) {
        originalText=s;
    }
    public String getOriginalText() { return originalText; }

    @Override
    public void setText(String s) {
        setOpaque(false);
       if (super._rotateText && !_icon) {
        	return;
        }
        _text = (s!=null && s.length()>0);
        super.setText(s);
        updateSize();
    }

    boolean momentary = false;
    public boolean getMomentary() { return momentary; }
    public void setMomentary(boolean m) { momentary = m; }

    public boolean buttonLive() {
        if (namedSensor==null) {  // no sensor connected for this protocol
            log.error("No sensor connection, can't process click");
            return false;
        }
        return _editor.getFlag(Editor.OPTION_CONTROLS, isControlling());
    }

    @Override
    public void doMousePressed(MouseEvent e) {
        if (debug) log.debug("doMousePressed buttonLive="+buttonLive()+", getMomentary="+getMomentary());
        if (getMomentary() && buttonLive() && !e.isMetaDown() && !e.isAltDown()) {
            // this is a momentary button press
            try {
                getSensor().setKnownState(jmri.Sensor.ACTIVE);
            } catch (jmri.JmriException reason) {
                log.warn("Exception setting momentary sensor: "+reason);
            }
        }
        super.doMousePressed(e);
    }

    @Override
    public void doMouseReleased(MouseEvent e) {
        if (getMomentary() && buttonLive() && !e.isMetaDown() && !e.isAltDown()) {
            // this is a momentary button release
            try {
                getSensor().setKnownState(jmri.Sensor.INACTIVE);
            } catch (jmri.JmriException reason) {
                log.warn("Exception setting momentary sensor: "+reason);
            }
        }
        super.doMouseReleased(e);
    }

    @Override
    public void doMouseClicked(MouseEvent e) {
        if (buttonLive() && !getMomentary()) {
            // this button responds to clicks
            if ( !e.isMetaDown() && !e.isAltDown() ) {
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
        super.doMouseClicked(e);
    }

    @Override
    public void dispose() {
        if (namedSensor != null) {
            getSensor().removePropertyChangeListener(this);
        }
        namedSensor = null;
        _iconMap = null;
        _name2stateMap = null;
        _state2nameMap = null;

        super.dispose();
    }

    protected Hashtable<Integer, NamedIcon> cloneMap(Hashtable<Integer, NamedIcon> map,
                                                             SensorIcon pos) {
        Hashtable<Integer, NamedIcon> clone = new Hashtable<Integer, NamedIcon>();
        if (map!=null) {
            Iterator<Entry<Integer, NamedIcon>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Integer, NamedIcon> entry = it.next();
                clone.put(entry.getKey(), cloneIcon(entry.getValue(), pos));
                if (pos!=null) {
                    pos.setIcon(pos._state2nameMap.get(entry.getKey()), _iconMap.get(entry.getKey()));
                }
            }
        }
        return clone;
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
        JMenu colorMenu = new JMenu(Bundle.getMessage("FontColor"));
        getPopupUtility().makeColorMenu(colorMenu, state);
        menu.add(colorMenu);
        colorMenu = new JMenu(Bundle.getMessage("FontBackgroundColor"));
        getPopupUtility().makeColorMenu(colorMenu, state+1);
        menu.add(colorMenu);
        return menu;
    }

    void changeLayoutSensorType(){
//        NamedBeanHandle <Sensor> handle = getNamedSensor();
        if (isIcon()) {
            _icon = false;
            _text = true;
            setIcon(null);
            setOriginalText(getUnRotatedText());
            setSuperText(null);
            setOpaque(true);        	
        } else if (isText()) {
            _icon = true;
            _text = (originalText!=null && originalText.length()>0);
            setSuperText(getOriginalText());
            setOpaque(false);        	
        }
        _namedIcon = null;
        displayState(sensorState());
//        setAttributes();
//        setSensor(handle);
        int deg = getDegrees();
        rotate(deg);
        if (deg!=0 && _text && !_icon) {
            setSuperText(null);        	
        }
    }
    
    int flashStateOn = -1;
    int flashStateOff = -1;
    boolean flashon = false;
    ActionListener taskPerformer;
    Timer flashTimer;
    
    synchronized public void flashSensor(int tps, int state1, int state2){
        if((flashTimer!=null) && flashTimer.isRunning())
            return;
        //Set the maximum number of state changes to 10 per second
        if(tps >10){
            tps=10;
        } else if (tps<=0) {
            return;
        }
        if((_state2nameMap.get(state1)==null) || _state2nameMap.get(state2)==null){
            log.error("one or other of the states passed for flash is null");
            return;
        } else if (state1==state2){
            log.info("Both states to flash between are the same, therefore no flashing will occur");
            return;
        }
        int interval = (1000/tps)/2;
        flashStateOn = state1;
        flashStateOff = state2;
        if(taskPerformer==null){
            taskPerformer = new ActionListener() {
                  public void actionPerformed(ActionEvent evt) {
                      if(flashon){
                        flashon=false;
                        displayState(flashStateOn);
                      } else {
                        flashon=true;
                        displayState(flashStateOff);
                      }
                  }
            };
        }
        flashTimer = new Timer(interval, taskPerformer);
        flashTimer.start();
    }
    
    synchronized public void stopFlash(){
        if(flashTimer!=null)
            flashTimer.stop();
        displayState(sensorState());
    }
    
    class SensorPopupUtil extends PositionablePopupUtil {

        SensorPopupUtil(Positionable parent, javax.swing.JComponent textComp) {
            super(parent, textComp);
        }
        @Override
        public void setTextJustificationMenu(JPopupMenu popup) {
            if (isText()) { super.setTextJustificationMenu(popup); }
        }
        @Override
        public void setTextOrientationMenu(JPopupMenu popup) {
            if (isText()) { super.setTextOrientationMenu(popup); }
        }
        @Override
        public void setFixedTextMenu(JPopupMenu popup) {
            if (isText()) { super.setFixedTextMenu(popup); }
        }
        @Override
        public void setTextMarginMenu(JPopupMenu popup) {
            if (isText()) { super.setTextMarginMenu(popup); }
        }
        @Override
        public void setTextBorderMenu(JPopupMenu popup) {
            if (isText()) { super.setTextBorderMenu(popup); }
        }
        @Override
        public void setTextFontMenu(JPopupMenu popup) {
            if (isText()) { super.setTextFontMenu(popup); }
        }
        
        @Override
        @SuppressWarnings("fallthrough")
        protected void makeColorMenu(JMenu colorMenu, int type) {
            ButtonGroup buttonGrp = new ButtonGroup();
            addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Black"), Color.black, type);
            addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("DarkGray"),Color.darkGray, type);
            addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Gray"),Color.gray, type);
            addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("LightGray"),Color.lightGray, type);
            addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("White"),Color.white, type);
            addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Red"),Color.red, type);
            addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Orange"),Color.orange, type);
            addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Yellow"),Color.yellow, type);
            addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Green"),Color.green, type);
            addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Blue"),Color.blue, type);
            addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Magenta"),Color.magenta, type);
            switch(type){
                case UNKOWN_BACKGROUND_COLOR :
                case ACTIVE_BACKGROUND_COLOR :
                case INACTIVE_BACKGROUND_COLOR :
                case INCONSISTENT_BACKGROUND_COLOR : addColorMenuEntry(colorMenu, buttonGrp, Bundle.getMessage("Clear"), null, type);
            }
        }
        
        @Override
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
                        default : break;
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
                default : break;
            }
            colorButtonGroup.add(r);
            menu.add(r);
        }
    }

    static Logger log = Logger.getLogger(SensorIcon.class.getName());
}
