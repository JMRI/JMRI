package jmri.jmrit.display.switchboardEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrit.beantable.AddNewDevicePanel;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.switchboardEditor.SwitchboardEditor;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for a switchboard interface object.
 * <p>
 * Contains a JButton or JPanel to control existing turnouts, sensors and
 * lights.
 * Separated from SwitchboardEditor.java in 4.12.3
 *
 * @author Egbert Broerse Copyright (c) 2017, 2018
 */
public class BeanSwitch extends JPanel implements java.beans.PropertyChangeListener, ActionListener {

    private JButton beanButton;
    //private final boolean connected = false;
    private int _shape;
    private String _label;
    private String _uname = "unconnected";
    protected String switchLabel;
    protected String switchTooltip;
    protected boolean _text;
    protected boolean _icon = false;
    protected boolean _control = false;
    protected String _state;
    protected String stateClosed = Bundle.getMessage("StateClosedShort");
    protected String stateThrown = Bundle.getMessage("StateThrownShort");

    // the associated Bean object
    private NamedBean _bname;
    private NamedBeanHandle<?> namedBean = null; // could be Turnout, Sensor or Light
    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
    private IconSwitch beanIcon;
    private IconSwitch beanKey;
    private IconSwitch beanSymbol;
    private String beanManuPrefix;
    private char beanTypeChar;
    private float dim = 100f; // to dim unconnected symbols
    private SwitchboardEditor _editor;

    /**
     * Ctor
     *
     * @param index       DCC address
     * @param bean        layout object to connect to
     * @param switchName  descriptive name corresponding with system name to
     *                    display in switch tooltip, i.e. LT1
     * @param shapeChoice Button, Icon (static) or Drawing (vector graphics)
     */
    public BeanSwitch(int index, NamedBean bean, String switchName, int shapeChoice, SwitchboardEditor editor) {
        _label = switchName;
        _editor = editor;
        log.debug("Name = [{}]", switchName);
        beanButton = new JButton(_label + ": ?"); // initial state unknown
        switchLabel = _label + ": ?"; // initial state unknown, used on icons
        _bname = bean;
        if (bean != null) {
            _uname = bean.getUserName();
            log.debug("UserName: {}", _uname);
            if (_uname == null) {
                _uname = Bundle.getMessage("NoUserName");
            }
        }
        switchTooltip = switchName + " (" + _uname + ")";
        this.setLayout(new BorderLayout()); // makes JButtons expand to the whole grid cell
        if (shapeChoice != 0) {
            _shape = shapeChoice; // Button
        } else {
            _shape = 0;
        }
        beanManuPrefix = _editor.getSwitchManu(); // connection/manufacturer i.e. M for MERG
        beanTypeChar = _label.charAt(beanManuPrefix.length()); // bean type, i.e. L, usually at char(1)
        // check for space char which might be caused by connection name > 2 chars and/or space in name
        if (beanTypeChar != 'T' && beanTypeChar != 'S' && beanTypeChar != 'L') { // add if more bean types are supported
            log.error("invalid char in Switchboard Button \"{}\". Check connection name.", _label);
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSwitchAddFailed"),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        beanIcon = new IconSwitch(iconOnPath, iconOffPath);
        beanKey = new IconSwitch(keyOnPath, keyOffPath);
        beanSymbol = new IconSwitch(rootPath + beanTypeChar + "-on-s.png", rootPath + beanTypeChar + "-off-s.png");

        // look for bean to connect to by name
        log.debug("beanconnect = {}, beantype = {}", beanManuPrefix, beanTypeChar);
        try {
            if (bean != null) {
                namedBean = nbhm.getNamedBeanHandle(switchName, bean);
            }
        } catch (IllegalArgumentException e) {
            log.error("invalid bean name= \"{}\" in Switchboard Button", switchName);
        }
        // attach shape specific code to this beanSwitch
        switch (_shape) {
            case 1: // icon shape
                log.debug("create Icon");
                beanIcon.addMouseListener(new MouseAdapter() { // handled by JPanel
                    @Override
                    public void mouseClicked(MouseEvent me) {
                        operate(me, switchName);
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        _editor.setBoardToolTip(null); // ends tooltip if displayed
                        if (e.isPopupTrigger()) {
                            // display the popup:
                            showPopUp(e);
                        }
                    }
                });
                _text = true; // TODO when web supports graphic switches: replace true by false;
                _icon = true;
                beanIcon.setLabel(switchLabel);
                beanIcon.positionLabel(17, 45); // provide x, y offset, depending on image size and free space
                if (_editor.showToolTip()) {
                    beanIcon.setToolTipText(switchTooltip);
                }
                beanIcon.setBackground(_editor.getDefaultBackgroundColor());
                //remove the line around icon switches?
                this.add(beanIcon);
                break;
            case 2: // Maerklin style keyboard
                log.debug("create Key");
                beanKey.addMouseListener(new MouseAdapter() { // handled by JPanel
                    @Override
                    public void mouseClicked(MouseEvent me) {
                        operate(me, switchName);
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        _editor.setBoardToolTip(null); // ends tooltip if displayed
                        if (e.isPopupTrigger()) {
                            // display the popup:
                            showPopUp(e);
                        }
                    }
                });
                _text = true; // TODO when web supports graphic switches: replace true by false;
                _icon = true;
                beanKey.setLabel(switchLabel);
                beanKey.positionLabel(14, 60); // provide x, y offset, depending on image size and free space
                if (_editor.showToolTip()) {
                    beanKey.setToolTipText(switchTooltip);
                }
                beanKey.setBackground(_editor.getDefaultBackgroundColor());
                //remove the line around icon switches?
                this.add(beanKey);
                break;
            case 3: // drawing turnout/sensor/light symbol (selecting image by letter in switch name/label)
                log.debug("create Symbols");
                beanSymbol.addMouseListener(new MouseAdapter() { // handled by JPanel
                    @Override
                    public void mouseClicked(MouseEvent me) {
                        operate(me, switchName);
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        _editor.setBoardToolTip(null); // ends tooltip if displayed
                        if (e.isPopupTrigger()) {
                            // display the popup:
                            showPopUp(e);
                        }
                    }
                });
                _text = true; // TODO when web supports graphic switches: replace true by false;
                _icon = true;
                beanSymbol.setLabel(switchLabel);
                beanSymbol.positionLabel(24, 20); // provide x, y offset, depending on image size and free space
                if (_editor.showToolTip()) {
                    beanSymbol.setToolTipText(switchTooltip);
                }
                beanSymbol.setBackground(_editor.getDefaultBackgroundColor());
                //remove the line around icon switches?
                this.setBorder(BorderFactory.createLineBorder(_editor.getDefaultBackgroundColor(), 3));
                this.add(beanSymbol);
                break;
            default: // 0 = "Button" shape
                log.debug("create Button");
                beanButton.addMouseListener(new MouseAdapter() { // handled by JPanel
                    @Override
                    public void mouseClicked(MouseEvent me) {
                        operate(me, switchName);
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        _editor.setBoardToolTip(null); // ends tooltip if displayed
                        if (e.isPopupTrigger()) {
                            // display the popup:
                            showPopUp(e);
                        }
                    }
                });
                _text = true;
                _icon = false;
                beanButton.setBackground(_editor.getDefaultBackgroundColor());
                beanButton.setOpaque(false);
                if (_editor.showToolTip()) {
                    beanButton.setToolTipText(switchTooltip);
                }
                this.add(beanButton);
                break;
        }
        // connect to object or dim switch
        if (bean == null) {
            if (!_editor.hideUnconnected()) {
                switch (_shape) {
                    case 0:
                        beanButton.setEnabled(false);
                        break;
                    default:
                        beanIcon.setOpacity(dim);
                }
            }
        } else {
            _control = true;
            switch (beanTypeChar) {
                case 'T':
                    getTurnout().addPropertyChangeListener(this, _label, "Switchboard Editor Turnout Switch");
                    if (getTurnout().canInvert()) {
                        this.setInverted(getTurnout().getInverted()); // only add and set when suppported by object/connection
                    }
                    break;
                case 'S':
                    getSensor().addPropertyChangeListener(this, _label, "Switchboard Editor Sensor Switch");
                    if (getSensor().canInvert()) {
                        this.setInverted(getSensor().getInverted()); // only add and set when suppported by object/connection
                    }
                    break;
                default: // light
                    getLight().addPropertyChangeListener(this, _label, "Switchboard Editor Light Switch");
                // Lights do not support Invert
            }
        }
        // from finishClone
        setTristate(getTristate());
        setMomentary(getMomentary());
        log.debug("Created switch {}", index);
    }

    public NamedBean getNamedBean() {
        return _bname;
    }

    /**
     * Stores an object as NamedBeanHandle, using _label as the display
     * name.
     *
     * @param bean the object (either a Turnout, Sensor or Light) to attach
     *             to this switch
     */
    public void setNamedBean(@Nonnull NamedBean bean) {
        try {
            namedBean = nbhm.getNamedBeanHandle(_label, bean);
        } catch (IllegalArgumentException e) {
            log.error("invalid bean name= \"{}\" in Switchboard Button", _label);
        }
        _uname = bean.getUserName();
        _control = true;
    }

    public Turnout getTurnout() {
        if (namedBean == null) {
            return null;
        }
        return (Turnout) namedBean.getBean();
    }

    public Sensor getSensor() {
        if (namedBean == null) {
            return null;
        }
        return (Sensor) namedBean.getBean();
    }

    public Light getLight() {
        if (namedBean == null) {
            return null;
        }
        return (Light) namedBean.getBean();
    }

    /**
     * Get the user selected switch shape (e.g. 3 for Slider)
     *
     * @return the index of the selected item in Shape comboBox
     */
    public int getType() {
        return _shape;
    }

    /**
     * Get text to display on this switch on Switchboard and in Web Server panel when attached
     * object is Inactive.
     *
     * @return text to show on inactive state (differs per type of objects)
     */
    public String getInactiveText() {
        // fetch bean specific abbreviation
        switch (beanTypeChar) {
            case 'T':
                _state = stateThrown; // +
                break;
            default: // Light, Sensor
                _state = "-";         // 1 char abbreviation for StateOff not clear
        }
        return _label + ": " + _state;
    }

    /**
     * Get text to display on this switch on Switchboard and in Web Server panel when attached
     * object is Active.
     *
     * @return text to show on active state (differs per type of object)
     */
    public String getActiveText() {
        // fetch bean specific abbreviation
        switch (beanTypeChar) {
            case 'T':
                _state = stateClosed; // +
                break;
            default: // Light, Sensor
                _state = "+";         // 1 char abbreviation for StateOff not clear
        }
        return _label + ": " + _state;
    }

    /**
     * Get text to display on this switch in Web Server panel when attached
     * object is Unknown (initial state displayed).
     *
     * @return text to show on unknown state (used on all types of objects)
     */
    public String getUnknownText() {
        return _label + ": ?";
    }

    public String getInconsistentText() {
        return _label + ": X";
    }

    /**
     * Get text to display as switch tooltip in Web Server panel.
     * Used in jmri.jmrit.display.switchboardEditor.configureXml.BeanSwitchXml#store(Object)
     *
     * @return switch tooltip text
     */
    public String getToolTip() {
        return switchTooltip;
    }

    // ******************* Display ***************************

    @Override
    public void actionPerformed(ActionEvent e) {
        //updateBean();
    }

    /**
     * Get the label of this switch.
     *
     * @return display name plus current state
     */
    public String getNameString() {
        return _label;
    }

    /**
     * Drive the current state of the display from the state of the
     * connected bean.
     *
     * @param state integer representing the new state e.g. Turnout.CLOSED
     */
    public void displayState(int state) {
        String switchLabel;
        log.debug("heard change");
        if (getNamedBean() == null) {
            log.debug("Display state {}, disconnected", state);
        } else {
            // display abbreviated name of state instead of state index
            switch (state) {
                case 2:
                    switchLabel = getActiveText();
                    break;
                case 4:
                    switchLabel = getInactiveText();
                    break;
                case 1:
                    switchLabel = getUnknownText();
                    break;
                default:
                    switchLabel = getInconsistentText();
                    log.warn("invalid char in Switchboard Button \"{}\". ERROR state shown.", _label);
            }
            log.debug("Switch label {} state: {} ", switchLabel, state);
            if (isText() && !isIcon()) { // to allow text buttons on web switchboard. TODO add graphic switches on web
                beanButton.setText(switchLabel);
            }
            if (isIcon() && beanIcon != null && beanKey != null && beanSymbol != null) {
                beanIcon.showSwitchIcon(state);
                beanIcon.setLabel(switchLabel);
                beanKey.showSwitchIcon(state);
                beanKey.setLabel(switchLabel);
                beanSymbol.showSwitchIcon(state);
                beanSymbol.setLabel(switchLabel);
            }
        }
    }

    /**
     * Switch presentation is graphic image based.
     *
     * @see #displayState(int)
     * @return true when switch shape other than 'Button' is selected
     */
    public final boolean isIcon() {
        return _icon;
    }

    /**
     * Switch presentation is text based.
     *
     * @see #displayState(int)
     * @return true when switch shape 'Button' is selected (and also for the
     *         other, graphic switch types until SwitchboardServlet directly
     *         supports their graphic icons)
     */
    public final boolean isText() {
        return _text;
    }

    /**
     * Get current state of attached turnout.
     *
     * @return A state variable from a Turnout, e.g. Turnout.CLOSED
     */
    int turnoutState() {
        if (namedBean != null) {
            return getTurnout().getKnownState();
        } else {
            return Turnout.UNKNOWN;
        }
    }

    /**
     * Update switch as state of turnout changes.
     *
     * @param e the PropertyChangeEvent heard
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("property change: {} {} is now: {}", _label, e.getPropertyName(), e.getNewValue());
        }
        // when there's feedback, transition through inconsistent icon for better animation
        if (getTristate()
                && (getTurnout().getFeedbackMode() != Turnout.DIRECT)
                && (e.getPropertyName().equals("CommandedState"))) {
            if (getTurnout().getCommandedState() != getTurnout().getKnownState()) {
                int now = Turnout.INCONSISTENT;
                displayState(now);
            }
            // this takes care of the quick double click
            if (getTurnout().getCommandedState() == getTurnout().getKnownState()) {
                int now = ((Integer) e.getNewValue());
                displayState(now);
            }
        }
        if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue());
            displayState(now);
            log.debug("Item state changed");
        }
        if (e.getPropertyName().equals("UserName")) {
            // update tooltip
            String newUserName;
            if (_editor.showToolTip()) {
                newUserName = ((String) e.getNewValue());
                if (newUserName == null || newUserName.equals("")) {
                    newUserName = Bundle.getMessage("NoUserName");
                }
                beanButton.setToolTipText(_label + " (" + newUserName + ")");
                beanIcon.setToolTipText(_label + " (" + newUserName + ")");
                beanKey.setToolTipText(_label + " (" + newUserName + ")");
                beanSymbol.setToolTipText(_label + " (" + newUserName + ")");
                log.debug("User Name changed to {}", newUserName);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        _editor.setBoardToolTip(null); // ends tooltip if displayed
        if (e.isPopupTrigger()) {
            // display the popup:
            showPopUp(e);
        }
    }

    public void mouseExited(MouseEvent e) {
        //super.mouseExited(e);
    }

    /**
     * Process mouseClick on this switch.
     *
     * @param e    the event heard
     * @param name ID of this button (identical to name of suggested bean
     *             object)
     */
    public void operate(MouseEvent e, String name) {
        log.debug("Button {} clicked", name);
        //if (!_editor.getFlag(Editor.OPTION_CONTROLS, isControlling())) {
        //    return;
        //}
        if (namedBean == null || e.isMetaDown()) { // || e.isAltDown() || !buttonLive() || getMomentary()) {
            return;
        }
        alternateOnClick();
    }

    void cleanup() {
        if (namedBean != null) {
            getTurnout().removePropertyChangeListener(this);
        }
        namedBean = null;
    }

    private boolean tristate = false;

    public void setTristate(boolean set) {
        tristate = set;
    }

    public boolean getTristate() {
        return tristate;
    }

    boolean momentary = false;

    public boolean getMomentary() {
        return momentary;
    }

    public void setMomentary(boolean m) {
        momentary = m;
    }

    JPopupMenu switchPopup;
    JMenuItem connectNewMenu = new JMenuItem(Bundle.getMessage("ConnectNewMenu", "..."));

    /**
     * Show pop-up on a switch with its unique attributes including the
     * (un)connected bean. Derived from
     * {@link jmri.jmrit.display.switchboardEditor.SwitchboardEditor#showPopUp(Positionable, MouseEvent)}
     *
     * @param e the event
     */
    public boolean showPopUp(MouseEvent e) {
        if (switchPopup != null) {
            switchPopup.removeAll();
        } else {
            switchPopup = new JPopupMenu();
        }

        switchPopup.add(getNameString());

        if (_editor.isEditable()) {
            // add tristate option if turnout has feedback
            if (namedBean != null) {
                //addTristateEntry(switchPopup); // switches don't do anything with this property
                addEditUserName(switchPopup);
                switch (beanTypeChar) {
                    case 'T':
                        if (getTurnout().canInvert()) { // check whether supported by this turnout
                            addInvert(switchPopup);
                        }
                        break;
                    case 'S':
                        if (getSensor().canInvert()) { // check whether supported by this sensor
                            addInvert(switchPopup);
                        }
                        break;
                    default:
                    // invert is not supported by Lights, so skip
                }
            } else {
                // show option to attach a new bean
                switchPopup.add(connectNewMenu);
                connectNewMenu.addActionListener((java.awt.event.ActionEvent e1) -> {
                    connectNew(_label);
                });
            }
        }
        // display the popup
        switchPopup.show(this, this.getWidth() / 3 + (int) ((_editor.getPaintScale() - 1.0) * this.getX()),
                this.getHeight() / 3 + (int) ((_editor.getPaintScale() - 1.0) * this.getY()));

        return true;
    }

    javax.swing.JMenuItem editItem = null;

    void addEditUserName(JPopupMenu popup) {
        editItem = new javax.swing.JMenuItem(Bundle.getMessage("EditNameTitle", "..."));
        popup.add(editItem);
        editItem.addActionListener((java.awt.event.ActionEvent e) -> {
            renameBean();
        });
    }

    javax.swing.JCheckBoxMenuItem invertItem = null;

    void addInvert(JPopupMenu popup) {
        invertItem = new javax.swing.JCheckBoxMenuItem(Bundle.getMessage("MenuInvertItem", _label));
        invertItem.setSelected(getInverted());
        popup.add(invertItem);
        invertItem.addActionListener((java.awt.event.ActionEvent e) -> {
            setBeanInverted(invertItem.isSelected());
        });
    }

    /**
     * Edit user name on a switch.
     */
    public void renameBean() {
        NamedBean nb;
        String oldName = _uname;
        // show input dialog
        String newUserName = (String) JOptionPane.showInputDialog(null,
                Bundle.getMessage("EnterNewName", _label),
                Bundle.getMessage("EditNameTitle", ""), JOptionPane.PLAIN_MESSAGE, null, null, oldName);
        if (newUserName == null) { // user cancelled
            log.debug("NewName dialog returned Null, cancelled");
            return;
        }
        log.debug("New name: {}", newUserName);
        if (newUserName.length() == 0) {
            log.debug("new user name is empty");
            JOptionPane.showMessageDialog(null, Bundle.getMessage("WarningEmptyUserName"),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (newUserName.equals(oldName)) { // name was not changed by user
            return;
        } else { // check if name is already in use
            switch (beanTypeChar) {
                case 'T':
                    nb = jmri.InstanceManager.turnoutManagerInstance().getTurnout(newUserName);
                    break;
                case 'S':
                    nb = jmri.InstanceManager.sensorManagerInstance().getSensor(newUserName);
                    break;
                case 'L':
                    nb = jmri.InstanceManager.lightManagerInstance().getLight(newUserName);
                    break;
                default:
                    log.error("Check userName: cannot parse bean name. userName = {}", newUserName);
                    return;
            }
            if (nb != null) {
                log.error("User name is not unique {}", newUserName);
                String msg = Bundle.getMessage("WarningUserName", new Object[]{("" + newUserName)});
                JOptionPane.showMessageDialog(null, msg,
                        Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        _bname.setUserName(newUserName);
        if (!newUserName.equals("")) {
            if (oldName == null || oldName.equals("")) {
                if (!nbhm.inUse(_label, _bname)) {
                    return; // no problem, so stop
                }
                String msg = Bundle.getMessage("UpdateToUserName", new Object[]{_editor.getSwitchTypeName(), newUserName, _label});
                int optionPane = JOptionPane.showConfirmDialog(null,
                        msg, Bundle.getMessage("UpdateToUserNameTitle"),
                        JOptionPane.YES_NO_OPTION);
                if (optionPane == JOptionPane.YES_OPTION) {
                    //This will update the bean reference from the systemName to the userName
                    try {
                        nbhm.updateBeanFromSystemToUser(_bname);
                    } catch (JmriException ex) {
                        //We should never get an exception here as we already check that the username is not valid
                    }
                }

            } else {
                nbhm.renameBean(oldName, newUserName, _bname);
            }

        } else {
            //This will update the bean reference from the old userName to the SystemName
            nbhm.updateBeanFromUserToSystem(_bname);
        }
    }

    private boolean inverted = false;

    public void setInverted(boolean set) {
        inverted = set;
    }

    public boolean getInverted() {
        return inverted;
    }

    /**
     * Invert attached object on the layout, if supported by its connection.
     */
    public void setBeanInverted(boolean set) {
        switch (beanTypeChar) {
            case 'T':
                if (getTurnout().canInvert()) { // if supported
                    this.setInverted(set);
                    getTurnout().setInverted(set);
                } else {
                    // show error message?
                }
                break;
            case 'S':
                if (getSensor().canInvert()) { // if supported
                    this.setInverted(set);
                    getSensor().setInverted(set);
                } else {
                    // show error message?
                }
                break;
            case 'L':
                // Lights cannot be inverted, so never called
                return;
            default:
                log.error("Invert item: cannot parse bean name. userName = {}", _label);
        }
    }

    public void doMouseClicked(java.awt.event.MouseEvent e) {
        log.debug("Switch clicked");
        //if (!_editor.getFlag(Editor.OPTION_CONTROLS, isControlling())) {
        //    return;
        //}
        if (namedBean == null || e.isMetaDown()) { //|| e.isAltDown() || !buttonLive() || getMomentary()) {
            return;
        }
        alternateOnClick();
    }

    /**
     * Change the state of attached Turnout, Light or Sensor on the layout
     * unless menu option Panel Items Control Layout is set to off.
     */
    void alternateOnClick() {
        if (_editor.allControlling()) {
            switch (beanTypeChar) {
                case 'T': // Turnout
                    log.debug("T clicked");
                    if (getTurnout().getKnownState() == jmri.Turnout.CLOSED) // if clear known state, set to opposite
                    {
                        getTurnout().setCommandedState(jmri.Turnout.THROWN);
                    } else if (getTurnout().getKnownState() == jmri.Turnout.THROWN) {
                        getTurnout().setCommandedState(jmri.Turnout.CLOSED);
                    } else if (getTurnout().getCommandedState() == jmri.Turnout.CLOSED) {
                        getTurnout().setCommandedState(jmri.Turnout.THROWN);  // otherwise, set to opposite of current commanded state if known
                    } else {
                        getTurnout().setCommandedState(jmri.Turnout.CLOSED);  // just force Closed
                    }
                    break;
                case 'L': // Light
                    log.debug("L clicked");
                    if (getLight().getState() == jmri.Light.OFF) {
                        getLight().setState(jmri.Light.ON);
                    } else {
                        getLight().setState(jmri.Light.OFF);
                    }
                    break;
                case 'S': // Sensor
                    log.debug("S clicked");
                    try {
                        if (getSensor().getKnownState() == jmri.Sensor.INACTIVE) {
                            getSensor().setKnownState(jmri.Sensor.ACTIVE);
                        } else {
                            getSensor().setKnownState(jmri.Sensor.INACTIVE);
                        }
                    } catch (jmri.JmriException reason) {
                        log.warn("Exception flipping sensor: {}", (Object) reason);
                    }
                    break;
                default:
                    log.error("invalid char in Switchboard Button \"{}\". State not set.", _label);
            }
        }
    }

    public void setBackgroundColor(Color bgcolor) {
        this.setBackground(bgcolor);
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(12);
    JTextField userName = new JTextField(15);

    /**
     * Create new bean and connect it to this switch. Use type letter from
     * switch label (S, T or L).
     */
    protected void connectNew(String systemName) {
        log.debug("Request new bean");
        sysName.setText(systemName);
        userName.setText("");
        // provide etc.
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("ConnectNewMenu", ""), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.display.switchboardEditor.SwitchboardEditor", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener okListener = (ActionEvent ev) -> {
                okAddPressed(ev);
            };
            ActionListener cancelListener = (ActionEvent ev) -> {
                cancelAddPressed(ev);
            };
            AddNewDevicePanel switchConnect = new AddNewDevicePanel(sysName, userName, "ButtonOK", okListener, cancelListener);
            switchConnect.setSystemNameFieldIneditable(); // prevent user interference with switch label
            switchConnect.setOK(); // activate OK button on Add new device pane
            addFrame.add(switchConnect);
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    protected void cancelAddPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    protected void okAddPressed(ActionEvent e) {
        NamedBean nb;
        String manuPrefix = _editor.getSwitchManu();
        String user = userName.getText();
        if (user.trim().equals("")) {
            user = null;
        }
        String sName = sysName.getText(); // can't be changed, but pick it up from panel

        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;

        switch (sName.charAt(manuPrefix.length())) {
            case 'T':
                Turnout t;
                try {
                    // add turnout to JMRI (w/appropriate manager)
                    t = InstanceManager.turnoutManagerInstance().provideTurnout(sName);
                    t.setUserName(user);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(sName);
                    return; // without creating
                }
                nb = jmri.InstanceManager.turnoutManagerInstance().getTurnout(sName);
                break;
            case 'S':
                Sensor s;
                try {
                    // add Sensor to JMRI (w/appropriate manager)
                    s = InstanceManager.sensorManagerInstance().provideSensor(sName);
                    s.setUserName(user);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(sName);
                    return; // without creating
                }
                nb = jmri.InstanceManager.sensorManagerInstance().getSensor(sName);
                break;
            case 'L':
                Light l;
                try {
                    // add Light to JMRI (w/appropriate manager)
                    l = InstanceManager.lightManagerInstance().provideLight(sName);
                    l.setUserName(user);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(sName);
                    return; // without creating
                }
                nb = jmri.InstanceManager.lightManagerInstance().getLight(sName);
                break;
            default:
                log.error("connectNew - OKpressed: cannot parse bean name. sName = {}", sName);
                return;
        }
        if (nb == null) {
            log.warn("failed to connect switch to item {}", sName);
        } else {
            // set switch on Switchboard to display current state of just connected bean
            log.debug("sName state: {}", nb.getState());
            try {
                if (_editor.getSwitch(sName) == null) {
                    log.warn("failed to update switch to state of {}", sName);
                } else {
                    _editor.updatePressed();
                }
            } catch (NullPointerException npe) {
                handleCreateException(sName);
                // exit without updating
            }
        }
    }

    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                        Bundle.getMessage("ErrorSwitchAddFailed"),
                        new Object[]{sysName}),
                Bundle.getMessage("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    String rootPath = "resources/icons/misc/switchboard/";
    String iconOffPath = rootPath + "appslide-off-s.png";
    String iconOnPath = rootPath + "appslide-on-s.png";
    String keyOffPath = rootPath + "markl-off-s.png";
    String keyOnPath = rootPath + "markl-on-s.png";
    String symbolOffPath; // = rootPath + "T-off-s.png"; // default for Turnout, replace T by S or L
    String symbolOnPath; // = rootPath + "T-on-s.png";

    /**
     * Class to display individual bean state switches on a JMRI Switchboard
     * using 2 image files.
     */
    public class IconSwitch extends JPanel {

        private BufferedImage image;
        private BufferedImage image1;
        private BufferedImage image2;
        private String tag = "tag";
        private int labelX = 16;
        private int labelY = 53;
        private float ropScale = 1f;
        private float ropOffset = 0f;
        private RescaleOp rop;

        /**
         * Create an icon from 2 alternating png images.
         *
         * @param filepath1 the ON image
         * @param filepath2 the OFF image
         */
        public IconSwitch(String filepath1, String filepath2) {
            // resize to maximum 100
            try {
                image1 = ImageIO.read(new File(filepath1));
                image2 = ImageIO.read(new File(filepath2));
                image = image1;
            } catch (IOException ex) {
                log.error("error reading image from {}-{}", filepath1, filepath2, ex);
            }
        }

        public void setOpacity(float offset) {
            ropOffset = offset;
            rop = new RescaleOp(ropScale, ropOffset, null);
        }

        protected void showSwitchIcon(int stateIndex) {
            log.debug("showSwitchIcon {}", stateIndex);
            if (image1 != null && image2 != null) {
                switch (stateIndex) {
                    case 2:
                        image = image1; // on/Thrown/Active
                        break;
                    default:
                        image = image2; // off, also for connected & unknown
                        break;
                }
                this.repaint();
            }
        }

        protected void setImage1(String newImagePath) {
            try {
                image1 = ImageIO.read(new File(newImagePath));
            } catch (IOException ex) {
                log.error("error reading image from {}", newImagePath, ex);
            }
        }

        /**
         * Set or change label text on switch.
         *
         * @param text string to display
         */
        protected void setLabel(String text) {
            tag = text;
            this.repaint();
        }

        /**
         * Position label on switch.
         *
         * @param x horizontal offset from top left corner, positive to the
         *          right
         * @param y vertical offset from top left corner, positive down
         */
        protected void positionLabel(int x, int y) {
            labelX = x;
            labelY = y;
            this.repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D)g;
            g2d.drawImage(image, rop, 0, 0); // dim switch if unconnected TODO scale image to fit panel?
            //g.drawImage(image, 0, 0, null);
            g.setFont(getFont());
            if (ropOffset > 0f) {
                g.setColor(Color.GRAY); // dimmed
            } else {
                g.setColor(_editor.getDefaultTextColorAsColor());
            }
            g.drawString(tag, labelX, labelY); // draw name on top of button image (vertical, horizontal offset from top left)
        }

    }

    private final static Logger log = LoggerFactory.getLogger(BeanSwitch.class);

}
