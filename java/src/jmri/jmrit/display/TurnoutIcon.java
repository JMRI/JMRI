package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.TableItemPanel;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status of a turnout.<P>
 * This responds to only KnownState, leaving CommandedState to some other
 * graphic representation later.
 * <P>
 * A click on the icon will command a state change. Specifically, it will set
 * the CommandedState to the opposite (THROWN vs CLOSED) of the current
 * KnownState.
 * <P>
 * The default icons are for a left-handed turnout, facing point for east-bound
 * traffic.
 *
 * @author Bob Jacobsen Copyright (c) 2002
 * @author PeteCressman Copyright (C) 2010, 2011
 */
public class TurnoutIcon extends PositionableIcon implements java.beans.PropertyChangeListener {

    protected HashMap<Integer, NamedIcon> _iconStateMap;     // state int to icon
    protected HashMap<String, Integer> _name2stateMap;       // name to state
    protected HashMap<Integer, String> _state2nameMap;       // state to name

    public TurnoutIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif",
                "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif"), editor);
        _control = true;
        setPopupUtility(null);
    }

    @Override
    public Positionable deepClone() {
        TurnoutIcon pos = new TurnoutIcon(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(TurnoutIcon pos) {
        pos.setTurnout(getNamedTurnout().getName());
        pos._iconStateMap = cloneMap(_iconStateMap, pos);
        pos.setTristate(getTristate());
        pos.setMomentary(getMomentary());
        pos.setDirectControl(getDirectControl());
        pos._iconFamily = _iconFamily;
        return super.finishClone(pos);
    }

    // the associated Turnout object
    //Turnout turnout = null;
    private NamedBeanHandle<Turnout> namedTurnout = null;

    /**
     * Attach a named turnout to this display item.
     *
     * @param pName Used as a system/user name to lookup the turnout object
     */
    public void setTurnout(String pName) {
        if (InstanceManager.getNullableDefault(jmri.TurnoutManager.class) != null) {
            try {
                Turnout turnout = InstanceManager.turnoutManagerInstance().provideTurnout(pName);
                setTurnout(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, turnout));
            } catch (IllegalArgumentException ex) {
                log.error("Turnout '" + pName + "' not available, icon won't see changes");
            }
        } else {
            log.error("No TurnoutManager for this protocol, icon won't see changes");
        }
    }

    public void setTurnout(NamedBeanHandle<Turnout> to) {
        if (namedTurnout != null) {
            getTurnout().removePropertyChangeListener(this);
        }
        namedTurnout = to;
        if (namedTurnout != null) {
            _iconStateMap = new HashMap<Integer, NamedIcon>();
            _name2stateMap = new HashMap<String, Integer>();
            _name2stateMap.put("BeanStateUnknown", Integer.valueOf(Turnout.UNKNOWN));
            _name2stateMap.put("BeanStateInconsistent", Integer.valueOf(Turnout.INCONSISTENT));
            _name2stateMap.put("TurnoutStateClosed", Integer.valueOf(Turnout.CLOSED));
            _name2stateMap.put("TurnoutStateThrown", Integer.valueOf(Turnout.THROWN));
            _state2nameMap = new HashMap<Integer, String>();
            _state2nameMap.put(Integer.valueOf(Turnout.UNKNOWN), "BeanStateUnknown");
            _state2nameMap.put(Integer.valueOf(Turnout.INCONSISTENT), "BeanStateInconsistent");
            _state2nameMap.put(Integer.valueOf(Turnout.CLOSED), "TurnoutStateClosed");
            _state2nameMap.put(Integer.valueOf(Turnout.THROWN), "TurnoutStateThrown");
            displayState(turnoutState());
            getTurnout().addPropertyChangeListener(this, namedTurnout.getName(), "Panel Editor Turnout Icon");
        }
    }

    public Turnout getTurnout() {
        return namedTurnout.getBean();
    }

    public NamedBeanHandle<Turnout> getNamedTurnout() {
        return namedTurnout;
    }

    @Override
    public jmri.NamedBean getNamedBean() {
        return getTurnout();
    }

    /**
     * Place icon by its localized bean state name.
     *
     * @param name the state name
     * @param icon the icon to place
     */
    public void setIcon(String name, NamedIcon icon) {
        if (log.isDebugEnabled()) {
            log.debug("setIcon for name \"" + name
                    + "\" state= " + _name2stateMap.get(name));
        }
        _iconStateMap.put(_name2stateMap.get(name), icon);
        displayState(turnoutState());
    }

    /**
     * Get icon by its localized bean state name.
     */
    @Override
    public NamedIcon getIcon(String state) {
        return _iconStateMap.get(_name2stateMap.get(state));
    }

    public NamedIcon getIcon(int state) {
        return _iconStateMap.get(Integer.valueOf(state));
    }

    @Override
    public int maxHeight() {
        int max = 0;
        Iterator<NamedIcon> iter = _iconStateMap.values().iterator();
        while (iter.hasNext()) {
            max = Math.max(iter.next().getIconHeight(), max);
        }
        return max;
    }

    @Override
    public int maxWidth() {
        int max = 0;
        Iterator<NamedIcon> iter = _iconStateMap.values().iterator();
        while (iter.hasNext()) {
            max = Math.max(iter.next().getIconWidth(), max);
        }
        return max;
    }

    /**
     * Get current state of attached turnout
     *
     * @return A state variable from a Turnout, e.g. Turnout.CLOSED
     */
    int turnoutState() {
        if (namedTurnout != null) {
            return getTurnout().getKnownState();
        } else {
            return Turnout.UNKNOWN;
        }
    }

    // update icon as state of turnout changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("property change: " + getNameString() + " " + e.getPropertyName() + " is now "
                    + e.getNewValue());
        }

        // when there's feedback, transition through inconsistent icon for better
        // animation
        if (getTristate()
                && (getTurnout().getFeedbackMode() != Turnout.DIRECT)
                && (e.getPropertyName().equals("CommandedState"))) {
            if (getTurnout().getCommandedState() != getTurnout().getKnownState()) {
                int now = Turnout.INCONSISTENT;
                displayState(now);
            }
            // this takes care of the quick double click
            if (getTurnout().getCommandedState() == getTurnout().getKnownState()) {
                int now = ((Integer) e.getNewValue()).intValue();
                displayState(now);
            }
        }

        if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue()).intValue();
            displayState(now);
        }
    }

    public String getStateName(int state) {
        return _state2nameMap.get(Integer.valueOf(state));

    }

    @Override
    public String getNameString() {
        String name;
        if (namedTurnout == null) {
            name = Bundle.getMessage("NotConnected");
        } else if (getTurnout().getUserName() != null) {
            name = getTurnout().getUserName() + " (" + getTurnout().getSystemName() + ")";
        } else {
            name = getTurnout().getSystemName();
        }
        return name;
    }

    public void setTristate(boolean set) {
        tristate = set;
    }

    public boolean getTristate() {
        return tristate;
    }
    private boolean tristate = false;

    boolean momentary = false;

    public boolean getMomentary() {
        return momentary;
    }

    public void setMomentary(boolean m) {
        momentary = m;
    }

    boolean directControl = false;

    public boolean getDirectControl() {
        return directControl;
    }

    public void setDirectControl(boolean m) {
        directControl = m;
    }

    JCheckBoxMenuItem momentaryItem = new JCheckBoxMenuItem(Bundle.getMessage("Momentary"));
    JCheckBoxMenuItem directControlItem = new JCheckBoxMenuItem(Bundle.getMessage("DirectControl"));

    /**
     * Pop-up displays unique attributes of turnouts
     */
    @Override
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            // add tristate option if turnout has feedback
            if (namedTurnout != null && getTurnout().getFeedbackMode() != Turnout.DIRECT) {
                addTristateEntry(popup);
            }

            popup.add(momentaryItem);
            momentaryItem.setSelected(getMomentary());
            momentaryItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setMomentary(momentaryItem.isSelected());
                }
            });

            popup.add(directControlItem);
            directControlItem.setSelected(getDirectControl());
            directControlItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setDirectControl(directControlItem.isSelected());
                }
            });
        } else if (getDirectControl()) {
            getTurnout().setCommandedState(jmri.Turnout.THROWN);
        }
        return true;
    }

    javax.swing.JCheckBoxMenuItem tristateItem = null;

    void addTristateEntry(JPopupMenu popup) {
        tristateItem = new javax.swing.JCheckBoxMenuItem(Bundle.getMessage("Tristate"));
        tristateItem.setSelected(getTristate());
        popup.add(tristateItem);
        tristateItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setTristate(tristateItem.isSelected());
            }
        });
    }

    /**
     * ****** popup AbstractAction method overrides ********
     */
    @Override
    protected void rotateOrthogonal() {
        Iterator<Entry<Integer, NamedIcon>> it = _iconStateMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, NamedIcon> entry = it.next();
            entry.getValue().setRotation(entry.getValue().getRotation() + 1, this);
        }
        displayState(turnoutState());
        // bug fix, must repaint icons that have same width and height
        repaint();
    }

    @Override
    public void setScale(double s) {
        _scale = s;
        Iterator<Entry<Integer, NamedIcon>> it = _iconStateMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, NamedIcon> entry = it.next();
            entry.getValue().scale(s, this);
        }
        displayState(turnoutState());
    }

    @Override
    public void rotate(int deg) {
        Iterator<Entry<Integer, NamedIcon>> it = _iconStateMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, NamedIcon> entry = it.next();
            entry.getValue().rotate(deg, this);
        }
        setDegrees(deg);
        displayState(turnoutState());
    }

    /**
     * Drive the current state of the display from the state of the turnout.
     */
    @Override
    public void displayState(int state) {
        if (getNamedTurnout() == null) {
            log.debug("Display state " + state + ", disconnected");
        } else {
//            log.debug(getNameString() +" displayState "+_state2nameMap.get(state));
            if (isText()) {
                super.setText(_state2nameMap.get(state));
            }
            if (isIcon()) {
                NamedIcon icon = getIcon(state);
                if (icon != null) {
                    super.setIcon(icon);
                }
            }
        }
        updateSize();
    }

    TableItemPanel _itemPanel;

    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameTurnout"));
        popup.add(new javax.swing.AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editItem();
            }
        });
        return true;
    }

    protected void editItem() {
        makePaletteFrame(java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameTurnout")));
        _itemPanel = new TableItemPanel(_paletteFrame, "Turnout", _iconFamily,
                PickListModel.turnoutPickModelInstance(), _editor); // NOI18N
        ActionListener updateAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        };
        // duplicate icon map with state names rather than int states and unscaled and unrotated
        HashMap<String, NamedIcon> strMap = new HashMap<String, NamedIcon>();
        Iterator<Entry<Integer, NamedIcon>> it = _iconStateMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, NamedIcon> entry = it.next();
            NamedIcon oldIcon = entry.getValue();
            NamedIcon newIcon = cloneIcon(oldIcon, this);
            newIcon.rotate(0, this);
            newIcon.scale(1.0, this);
            newIcon.setRotation(4, this);
            strMap.put(_state2nameMap.get(entry.getKey()), newIcon);
        }
        _itemPanel.init(updateAction, strMap);
        _itemPanel.setSelection(getTurnout());
        _paletteFrame.add(_itemPanel);
        _paletteFrame.pack();
        _paletteFrame.setVisible(true);
    }

    void updateItem() {
        HashMap<Integer, NamedIcon> oldMap = cloneMap(_iconStateMap, this);
        setTurnout(_itemPanel.getTableSelection().getSystemName());
        _iconFamily = _itemPanel.getFamilyName();
        HashMap<String, NamedIcon> iconMap = _itemPanel.getIconMap();
        if (iconMap != null) {
            Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                if (log.isDebugEnabled()) {
                    log.debug("key= " + entry.getKey());
                }
                NamedIcon newIcon = entry.getValue();
                NamedIcon oldIcon = oldMap.get(_name2stateMap.get(entry.getKey()));
                newIcon.setLoad(oldIcon.getDegrees(), oldIcon.getScale(), this);
                newIcon.setRotation(oldIcon.getRotation(), this);
                setIcon(entry.getKey(), newIcon);
            }
        }   // otherwise retain current map
//        jmri.jmrit.catalog.InstanceManager.getDefault(ImageIndexEditor.class).checkImageIndex();
        _paletteFrame.dispose();
        _paletteFrame = null;
        _itemPanel.dispose();
        _itemPanel = null;
        invalidate();
    }

    @Override
    public boolean setEditIconMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameTurnout"));
        popup.add(new javax.swing.AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit();
            }
        });
        return true;
    }

    @Override
    protected void edit() {
        makeIconEditorFrame(this, "Turnout", true, null); // NOI18N
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.turnoutPickModelInstance());
        Iterator<Integer> e = _iconStateMap.keySet().iterator();
        int i = 0;
        while (e.hasNext()) {
            Integer key = e.next();
            _iconEditor.setIcon(i++, _state2nameMap.get(key), _iconStateMap.get(key));
        }
        _iconEditor.makeIconPanel(false);

        // set default icons, then override with this turnout's icons
        ActionListener addIconAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                updateTurnout();
            }
        };
        _iconEditor.complete(addIconAction, true, true, true);
        _iconEditor.setSelection(getTurnout());
    }

    void updateTurnout() {
        HashMap<Integer, NamedIcon> oldMap = cloneMap(_iconStateMap, this);
        setTurnout(_iconEditor.getTableSelection().getDisplayName());
        Hashtable<String, NamedIcon> iconMap = _iconEditor.getIconMap();

        Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            if (log.isDebugEnabled()) {
                log.debug("key= " + entry.getKey());
            }
            NamedIcon newIcon = entry.getValue();
            NamedIcon oldIcon = oldMap.get(_name2stateMap.get(entry.getKey()));
            newIcon.setLoad(oldIcon.getDegrees(), oldIcon.getScale(), this);
            newIcon.setRotation(oldIcon.getRotation(), this);
            setIcon(entry.getKey(), newIcon);
        }
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    public boolean buttonLive() {
        if (namedTurnout == null) {
            log.error("No turnout connection, can't process click");
            return false;
        }
        return true;
    }

    @Override
    public void doMousePressed(MouseEvent e) {
        if (getMomentary() && buttonLive() && !e.isMetaDown() && !e.isAltDown()) {
            // this is a momentary button press
            getTurnout().setCommandedState(jmri.Turnout.THROWN);
        }
        super.doMousePressed(e);
    }

    @Override
    public void doMouseReleased(MouseEvent e) {
        if (getMomentary() && buttonLive() && !e.isMetaDown() && !e.isAltDown()) {
            // this is a momentary button release
            getTurnout().setCommandedState(jmri.Turnout.CLOSED);
        }
        super.doMouseReleased(e);
    }

    @Override
    public void doMouseClicked(java.awt.event.MouseEvent e) {
        if (!_editor.getFlag(Editor.OPTION_CONTROLS, isControlling())) {
            return;
        }
        if (e.isMetaDown() || e.isAltDown() || !buttonLive() || getMomentary()) {
            return;
        }

        if (getDirectControl() && !isEditable()) {
            getTurnout().setCommandedState(jmri.Turnout.CLOSED);
        } else {
            alternateOnClick();
        }
    }

    void alternateOnClick() {
        if (getTurnout().getKnownState() == jmri.Turnout.CLOSED) {  // if clear known state, set to opposite
            getTurnout().setCommandedState(jmri.Turnout.THROWN);
        } else if (getTurnout().getKnownState() == jmri.Turnout.THROWN) {
            getTurnout().setCommandedState(jmri.Turnout.CLOSED);
        } else if (getTurnout().getCommandedState() == jmri.Turnout.CLOSED) {
            getTurnout().setCommandedState(jmri.Turnout.THROWN);  // otherwise, set to opposite of current commanded state if known
        } else {
            getTurnout().setCommandedState(jmri.Turnout.CLOSED);  // just force closed.
        }
    }

    @Override
    public void dispose() {
        if (namedTurnout != null) {
            getTurnout().removePropertyChangeListener(this);
        }
        namedTurnout = null;
        _iconStateMap = null;
        _name2stateMap = null;
        _state2nameMap = null;

        super.dispose();
    }

    protected HashMap<Integer, NamedIcon> cloneMap(HashMap<Integer, NamedIcon> map,
            TurnoutIcon pos) {
        HashMap<Integer, NamedIcon> clone = new HashMap<Integer, NamedIcon>();
        if (map != null) {
            Iterator<Entry<Integer, NamedIcon>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Integer, NamedIcon> entry = it.next();
                clone.put(entry.getKey(), cloneIcon(entry.getValue(), pos));
                if (pos != null) {
                    pos.setIcon(_state2nameMap.get(entry.getKey()), _iconStateMap.get(entry.getKey()));
                }
            }
        }
        return clone;
    }

    private final static Logger log = LoggerFactory.getLogger(TurnoutIcon.class);
}
