package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.SignalHeadItemPanel;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status of a SignalHead.
 * <p>
 * SignalHeads are located via the SignalHeadManager, which in turn is located
 * via the InstanceManager.
 *
 * @see jmri.SignalHeadManager
 * @see jmri.InstanceManager
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
public class SignalHeadIcon extends PositionableIcon implements java.beans.PropertyChangeListener {

    private String[] _validKeys;

    public SignalHeadIcon(Editor editor) {
        super(editor);
        _control = true;
    }

    @Override
    public Positionable deepClone() {
        SignalHeadIcon pos = new SignalHeadIcon(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(SignalHeadIcon pos) {
        pos.setSignalHead(getNamedSignalHead().getName());
        for (Entry<String, NamedIcon> entry : _iconMap.entrySet()) {
            pos.setIcon(entry.getKey(), entry.getValue());
        }
        pos.setClickMode(getClickMode());
        pos.setLitMode(getLitMode());
        return super.finishClone(pos);
    }

    // private SignalHead mHead;
    private NamedBeanHandle<SignalHead> namedHead;

    private HashMap<String, NamedIcon> _saveMap;

    /**
     * Attach a SignalHead element to this display item by bean.
     *
     * @param sh the specific SignalHead object to attach
     */
    public void setSignalHead(NamedBeanHandle<SignalHead> sh) {
        if (namedHead != null) {
            getSignalHead().removePropertyChangeListener(this);
        }
        namedHead = sh;
        if (namedHead != null) {
            _iconMap = new HashMap<>();
            _validKeys = getSignalHead().getValidStateKeys();
            displayState(headState());
            getSignalHead().addPropertyChangeListener(this, namedHead.getName(), "SignalHead Icon");
        }
    }

    /**
     * Attach a SignalHead element to this display item by name. Taken from the
     * Layout Editor.
     *
     * @param pName Used as a system/user name to lookup the SignalHead object
     */
    public void setSignalHead(String pName) {
        SignalHead mHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getNamedBean(pName);
        if (mHead == null) {
            log.warn("did not find a SignalHead named {}", pName);
        } else {
            setSignalHead(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, mHead));
        }
    }

    public NamedBeanHandle<SignalHead> getNamedSignalHead() {
        return namedHead;
    }

    public SignalHead getSignalHead() {
        if (namedHead == null) {
            return null;
        }
        return namedHead.getBean();
    }

    @Override
    public jmri.NamedBean getNamedBean() {
        return getSignalHead();
    }

    /**
     * Place icon by its non-localized bean state name.
     *
     * @param state the non-localized state
     * @param icon  the icon to place
     */
    public void setIcon(String state, NamedIcon icon) {
        log.debug("setIcon for {}", state);
        if (isValidState(state)) {
            _iconMap.put(state, icon);
            displayState(headState());
        }
    }

    /**
     * Check that device supports the state. Valid state names returned by the
     * bean are (non-localized) property key names.
     */
    private boolean isValidState(String key) {
        if (key == null) {
            return false;
        }
        if (key.equals("SignalHeadStateDark") || key.equals("SignalHeadStateHeld")) {
            log.debug("{} is a valid state.", key);
            return true;
        }
        for (String valid : _validKeys) {
            if (key.equals(valid)) {
                log.debug("{} is a valid state.", key);
                return true;
            }
        }
        log.debug("{} is NOT a valid state.", key);
        return false;
    }

    /**
     * Get current appearance of the head.
     *
     * @return an appearance variable from a SignalHead, e.g. SignalHead.RED
     */
    public int headState() {
        if (getSignalHead() == null) {
            return 0;
        } else {
            return getSignalHead().getAppearance();
        }
    }

    // update icon as state of turnout changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("property change: {} current state: {}", e.getPropertyName(), headState());
        displayState(headState());
        _editor.getTargetPanel().repaint();
    }

    @Override
    public @Nonnull
    String getNameString() {
        if (namedHead == null) {
            return Bundle.getMessage("NotConnected");
        }
        return namedHead.getName(); // short NamedIcon name
    }

    private ButtonGroup litButtonGroup = null;

    /**
     * Pop-up just displays the name
     */
    @Override
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            // add menu to select action on click
            JMenu clickMenu = new JMenu(Bundle.getMessage("WhenClicked"));
            ButtonGroup clickButtonGroup = new ButtonGroup();
            JRadioButtonMenuItem r;
            r = new JRadioButtonMenuItem(Bundle.getMessage("ChangeAspect"));
            r.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setClickMode(3);
                }
            });
            clickButtonGroup.add(r);
            if (clickMode == 3) {
                r.setSelected(true);
            } else {
                r.setSelected(false);
            }
            clickMenu.add(r);
            r = new JRadioButtonMenuItem(Bundle.getMessage("Cycle3Aspects"));
            r.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setClickMode(0);
                }
            });
            clickButtonGroup.add(r);
            if (clickMode == 0) {
                r.setSelected(true);
            } else {
                r.setSelected(false);
            }
            clickMenu.add(r);
            r = new JRadioButtonMenuItem(Bundle.getMessage("AlternateLit"));
            r.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setClickMode(1);
                }
            });
            clickButtonGroup.add(r);
            if (clickMode == 1) {
                r.setSelected(true);
            } else {
                r.setSelected(false);
            }
            clickMenu.add(r);
            r = new JRadioButtonMenuItem(Bundle.getMessage("AlternateHeld"));
            r.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setClickMode(2);
                }
            });
            clickButtonGroup.add(r);
            if (clickMode == 2) {
                r.setSelected(true);
            } else {
                r.setSelected(false);
            }
            clickMenu.add(r);
            popup.add(clickMenu);

            // add menu to select handling of lit parameter
            JMenu litMenu = new JMenu(Bundle.getMessage("WhenNotLit"));
            litButtonGroup = new ButtonGroup();
            r = new JRadioButtonMenuItem(Bundle.getMessage("ShowAppearance"));
            r.setIconTextGap(10);
            r.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setLitMode(false);
                }
            });
            litButtonGroup.add(r);
            if (!litMode) {
                r.setSelected(true);
            } else {
                r.setSelected(false);
            }
            litMenu.add(r);
            r = new JRadioButtonMenuItem(Bundle.getMessage("ShowDarkIcon"));
            r.setIconTextGap(10);
            r.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setLitMode(true);
                }
            });
            litButtonGroup.add(r);
            if (litMode) {
                r.setSelected(true);
            } else {
                r.setSelected(false);
            }
            litMenu.add(r);
            popup.add(litMenu);

            popup.add(new AbstractAction(Bundle.getMessage("EditLogic")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    jmri.jmrit.blockboss.BlockBossFrame f = new jmri.jmrit.blockboss.BlockBossFrame();
                    String name = getNameString();
                    f.setTitle(java.text.MessageFormat.format(Bundle.getMessage("SignalLogic"), name));
                    f.setSignal(getSignalHead());
                    f.setVisible(true);
                }
            });
            return true;
        }
        return false;
    }

    /**
     * ************* popup AbstractAction.actionPerformed method overrides
     * ***********
     */
    @Override
    protected void rotateOrthogonal() {
        super.rotateOrthogonal();
        displayState(headState());
    }

    @Override
    public void setScale(double s) {
        super.setScale(s);
        displayState(headState());
    }

    @Override
    public void rotate(int deg) {
        super.rotate(deg);
        displayState(headState());
    }

    /**
     * Drive the current state of the display from the state of the underlying
     * SignalHead object.
     * <ul>
     * <li>If the signal is held, display that.
     * <li>If set to monitor the status of the lit parameter and lit is false,
     * show the dark icon ("dark", when set as an explicit appearance, is
     * displayed anyway)
     * <li>Show the icon corresponding to one of the (max seven) appearances.
     * </ul>
     */
    @Override
    public void displayState(int state) {
        updateSize();
        if (getSignalHead() == null) {
            log.debug("Display state {}, disconnected", state);
        } else {
            log.debug("Display state {} for {}", state, getNameString());
            if (getSignalHead().getHeld()) {
                if (isText()) {
                    super.setText(Bundle.getMessage("Held"));
                }
                if (isIcon()) {
                    super.setIcon(_iconMap.get("SignalHeadStateHeld"));
                }
            } else if (getLitMode() && !getSignalHead().getLit()) {
                if (isText()) {
                    super.setText(Bundle.getMessage("Dark"));
                }
                if (isIcon()) {
                    super.setIcon(_iconMap.get("SignalHeadStateDark"));
                }
            }
        }
        if (isText()) {
            super.setText(Bundle.getMessage(getSignalHead().getAppearanceKey(state)));
        }
        if (isIcon()) {
            NamedIcon icon = _iconMap.get(getSignalHead().getAppearanceKey(state));
            if (icon != null) {
                super.setIcon(icon);
            }
        }
    }

    private SignalHeadItemPanel _itemPanel;

    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"),
                Bundle.getMessage("BeanNameSignalHead"));
        popup.add(new AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editItem();
            }
        });
        return true;
    }

    protected void editItem() {
        _paletteFrame = makePaletteFrame(java.text.MessageFormat.format(Bundle.getMessage("EditItem"),
                Bundle.getMessage("BeanNameSignalHead")));
        _itemPanel = new SignalHeadItemPanel(_paletteFrame, "SignalHead", getFamily(),
                PickListModel.signalHeadPickModelInstance(), _editor); // NOI18N
        ActionListener updateAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        };
        // _iconMap keys with non-localized keys
        // duplicate _iconMap map with unscaled and unrotated icons
        HashMap<String, NamedIcon> map = new HashMap<>();
        for (Entry<String, NamedIcon> entry : _iconMap.entrySet()) {
            NamedIcon oldIcon = entry.getValue();
            NamedIcon newIcon = cloneIcon(oldIcon, this);
            newIcon.rotate(0, this);
            newIcon.scale(1.0, this);
            newIcon.setRotation(4, this);
            map.put(entry.getKey(), newIcon);
        }
        _itemPanel.init(updateAction, map);
        _itemPanel.setSelection(getSignalHead());
        initPaletteFrame(_paletteFrame, _itemPanel);
    }

    void updateItem() {
        if (!_itemPanel.oktoUpdate()) {
            return;
        }
        _saveMap = _iconMap;  // setSignalHead() clears _iconMap. We need a copy for setIcons()
        setSignalHead(_itemPanel.getTableSelection().getSystemName());
        setFamily(_itemPanel.getFamilyName());
        HashMap<String, NamedIcon> map1 = _itemPanel.getIconMap();
        if (map1 != null) {
            // map1 may be keyed with NamedBean names. Convert to local name keys.
            Hashtable<String, NamedIcon> map2 = new Hashtable<>();
            for (Entry<String, NamedIcon> entry : map1.entrySet()) {
                // TODO I18N using existing NamedBeanBundle keys before calling convertText(entry.getKey())?
                map2.put(jmri.jmrit.display.palette.ItemPalette.convertText(entry.getKey()), entry.getValue());
            }
            setIcons(map2);
        }   // otherwise retain current map
        displayState(getSignalHead().getAppearance());
        finishItemUpdate(_paletteFrame, _itemPanel);
    }

    @Override
    public boolean setEditIconMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameSignalHead"));
        popup.add(new AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit();
            }
        });
        return true;
    }

    @Override
    protected void edit() {
        makeIconEditorFrame(this, "SignalHead", true, null);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.signalHeadPickModelInstance());
        int i = 0;
        for (Entry<String, NamedIcon> entry : _iconMap.entrySet()) {
            _iconEditor.setIcon(i++, entry.getKey(), new NamedIcon(entry.getValue()));
        }
        _iconEditor.makeIconPanel(false);

        ActionListener addIconAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                updateSignal();
            }
        };
        _iconEditor.complete(addIconAction, true, false, true);
        _iconEditor.setSelection(getSignalHead());
    }

    /**
     * Replace the icons in _iconMap with those from map, but preserve the scale
     * and rotation.
     */
    private void setIcons(Hashtable<String, NamedIcon> map) {
        HashMap<String, NamedIcon> tempMap = new HashMap<>();
        Iterator<Entry<String, NamedIcon>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            String name = entry.getKey();
            NamedIcon icon = entry.getValue();
            NamedIcon oldIcon = _saveMap.get(name); // setSignalHead() has cleared _iconMap
            log.debug("key= {}, localKey= {}, newIcon= {}, oldIcon= {}", entry.getKey(), name, icon, oldIcon);
            if (oldIcon != null) {
                icon.setLoad(oldIcon.getDegrees(), oldIcon.getScale(), this);
                icon.setRotation(oldIcon.getRotation(), this);
            }
            tempMap.put(name, icon);
        }
        _iconMap = tempMap;
    }

    void updateSignal() {
        _saveMap = _iconMap;  // setSignalHead() clears _iconMap. We need a copy for setIcons()
        if (_iconEditor != null) {
            setSignalHead(_iconEditor.getTableSelection().getDisplayName());
            setIcons(_iconEditor.getIconMap());
            _iconEditorFrame.dispose();
            _iconEditorFrame = null;
            _iconEditor = null;
            invalidate();
        }
        displayState(headState());
    }

    /**
     * What to do on click? 0 means sequence through aspects; 1 means alternate
     * the "lit" aspect; 2 means alternate the "held" aspect.
     */
    protected int clickMode = 3;

    public void setClickMode(int mode) {
        clickMode = mode;
    }

    public int getClickMode() {
        return clickMode;
    }

    /**
     * How to handle lit vs not lit?
     * <p>
     * False means ignore (always show R/Y/G/etc appearance on screen); True
     * means show "dark" if lit is set false.
     * <p>
     * Note that setting the appearance "DARK" explicitly will show the dark
     * icon regardless of how this is set.
     */
    protected boolean litMode = false;

    public void setLitMode(boolean mode) {
        litMode = mode;
    }

    public boolean getLitMode() {
        return litMode;
    }

    /**
     * Change the SignalHead state when the icon is clicked. Note that this
     * change may not be permanent if there is logic controlling the signal
     * head.
     * <p>
     */
    @Override
    public void doMouseClicked(java.awt.event.MouseEvent e) {
        if (!_editor.getFlag(Editor.OPTION_CONTROLS, isControlling())) {
            return;
        }
        performMouseClicked(e);
    }

    /**
     * Handle mouse clicks when no modifier keys are pressed. Mouse clicks with
     * modifier keys pressed can be processed by the containing component.
     *
     * @param e the mouse click event
     */
    public void performMouseClicked(java.awt.event.MouseEvent e) {
        if (e.isMetaDown() || e.isAltDown()) {
            return;
        }
        if (getSignalHead() == null) {
            log.error("No turnout connection, can't process click");
            return;
        }
        switch (clickMode) {
            case 0:
                switch (getSignalHead().getAppearance()) {
                    case jmri.SignalHead.RED:
                    case jmri.SignalHead.FLASHRED:
                        getSignalHead().setAppearance(jmri.SignalHead.YELLOW);
                        break;
                    case jmri.SignalHead.YELLOW:
                    case jmri.SignalHead.FLASHYELLOW:
                        getSignalHead().setAppearance(jmri.SignalHead.GREEN);
                        break;
                    case jmri.SignalHead.GREEN:
                    case jmri.SignalHead.FLASHGREEN:
                    default:
                        getSignalHead().setAppearance(jmri.SignalHead.RED);
                        break;
                }
                return;
            case 1:
                getSignalHead().setLit(!getSignalHead().getLit());
                return;
            case 2:
                getSignalHead().setHeld(!getSignalHead().getHeld());
                return;
            case 3:
                SignalHead sh = getSignalHead();
                int[] states = sh.getValidStates();
                int state = sh.getAppearance();
                for (int i = 0; i < states.length; i++) {
                    if (state == states[i]) {
                        i++;
                        if (i >= states.length) {
                            i = 0;
                        }
                        state = states[i];
                        break;
                    }
                }
                sh.setAppearance(state);
                log.debug("Set state= {}", state);
                return;
            default:
                log.error("Click in mode {}", clickMode);
        }
    }

    //private static boolean warned = false;
    @Override
    public void dispose() {
        if (getSignalHead() != null) {
            getSignalHead().removePropertyChangeListener(this);
        }
        namedHead = null;
        _iconMap = null;
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(SignalHeadIcon.class);

}
