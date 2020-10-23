package jmri.jmrit.display.switchboardEditor;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrit.beantable.AddNewDevicePanel;
import jmri.jmrit.display.Positionable;
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
 * @author Egbert Broerse Copyright (c) 2017, 2018, 2020
 */
public class BeanSwitch extends JPanel implements java.beans.PropertyChangeListener, ActionListener {

    private final JButton beanButton;
    private final int _shape;
    private final String _switchSysName;
    private String _uName = "unconnected";
    private String _uLabel = ""; // for display, empty if userName == null
    private final Boolean showUserName;
    protected String switchLabel;
    protected String switchButtonLabel;
    protected String switchTooltip;
    protected boolean _text;
    protected boolean _icon = false;
    protected boolean _control = false;
    protected String _state;
    protected String _color;
    protected String stateClosed = Bundle.getMessage("StateClosedShort");
    protected String stateThrown = Bundle.getMessage("StateThrownShort");

    // the associated Bean object
    private final NamedBean _bname;
    private NamedBeanHandle<?> namedBean = null; // could be Turnout, Sensor or Light
    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
    private IconSwitch beanIcon;
    private IconSwitch beanKey;
    private IconSwitch beanSymbol;
    private final char beanTypeChar;
    private final Color defaultActiveColor = Color.RED;
    private final Color defaultInactiveColor = Color.GREEN;
    //private final Color defaultUnknownColor = Color.WHITE; // often hard to see
    private final SwitchboardEditor _editor;

    /**
     * Ctor.
     *
     * @param index       DCC address.
     * @param bean        layout object to connect to.
     * @param switchName  descriptive name corresponding with system name to
     *                    display in switch tooltip, i.e. LT1.
     * @param shapeChoice Button, Icon (static) or Drawing (vector graphics).
     * @param editor      main switchboard editor.
     */
    public BeanSwitch(int index, NamedBean bean, String switchName, int shapeChoice, SwitchboardEditor editor) {
        log.debug("Name = [{}]", switchName);
        _switchSysName = switchName;
        sysNameTextBox.setText(switchName); // setting name here allows test of AddNew()
        _editor = editor;
        _bname = bean;
        showUserName = (_editor.showUserName().equals("yes"));
        if (bean != null) {
            _uName = bean.getUserName();
            log.debug("Switch userName from bean: {}", _uName);
            if (_uName == null) {
                _uName = Bundle.getMessage("NoUserName");
            } else if (showUserName) {
                _uLabel = _uName;
            }
        }

        beanButton = new JButton();
        beanButton.setText(getSwitchButtonLabel(_switchSysName + ": ?")); // initial text to display

        switchTooltip = switchName + " (" + _uName + ")";
        this.setLayout(new BorderLayout()); // makes JButtons expand to the whole grid cell
        _shape = shapeChoice;
        String beanManuPrefix = _editor.getSwitchManu(); // connection/manufacturer prefix i.e. M for MERG
        beanTypeChar = _switchSysName.charAt(beanManuPrefix.length()); // bean type, i.e. L, usually at char(1)
        // check for space char which might be caused by connection name > 2 chars and/or space in name
        if (beanTypeChar != 'T' && beanTypeChar != 'S' && beanTypeChar != 'L') { // add if more bean types are supported
            log.error("invalid char in Switchboard Button \"{}\". Check connection name.", _switchSysName);
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSwitchAddFailed"),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int r = _editor.getTileSize()/2; // max WxH of canvas inside cell, used as relative unit to draw
        log.debug("BeanSwitch graphic tilesize/2  r = {}", r);
        beanIcon = new IconSwitch(iconOnPath, iconOffPath, r);
        beanKey = new IconSwitch(keyOnPath, keyOffPath, r);
        beanSymbol = new IconSwitch(rootPath + beanTypeChar + "-on-s.png", rootPath + beanTypeChar + "-off-s.png", r);

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
            case 1: // slider shape
                beanIcon.addMouseListener(new MouseAdapter() { // handled by JPanel
                    @Override
                    public void mouseClicked(MouseEvent me) {
                        operate(me, switchName);
                    }

                    @Override
                    public void mouseReleased(MouseEvent me) { // for Windows
                        if (me.isPopupTrigger()) {
                            showPopUp(me); // display the popup
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent me) { // for macOS, Linux
                        if (me.isPopupTrigger()) {
                            showPopUp(me); // display the popup
                        }
                    }
                });
                _text = true; // not actually used
                _icon = true;
                beanIcon.setPreferredSize(new Dimension(2*r, 2*r));
                beanIcon.setLabels(switchLabel, _uLabel);
                beanIcon.positionLabel(0, 5*r/-8, Component.CENTER_ALIGNMENT, Math.max(12,r/4));
                beanIcon.positionSubLabel(0, r/3, Component.CENTER_ALIGNMENT, Math.max(9,r/5)); // smaller (system name)
                if (_editor.showToolTip()) {
                    beanIcon.setToolTipText(switchTooltip);
                }
                beanIcon.setBackground(_editor.getDefaultBackgroundColor());
                //remove the line around icon switches?
                this.add(beanIcon);
                break;
            case 2: // Maerklin style keyboard
                beanKey.addMouseListener(new MouseAdapter() { // handled by JPanel
                    @Override
                    public void mouseClicked(MouseEvent me) {
                        operate(me, switchName);
                    }

                    @Override
                    public void mouseReleased(MouseEvent me) { // for Windows
                        if (me.isPopupTrigger()) {
                            showPopUp(me); // display the popup
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent me) { // for macOS, Linux
                        if (me.isPopupTrigger()) {
                            showPopUp(me); // display the popup
                        }
                    }
                });
                _text = true; // not actually used for Switchboards
                _icon = true;
                beanKey.setPreferredSize(new Dimension(new Dimension(2*r, 2*r)));
                beanKey.setLabels(switchLabel, _uLabel);
                beanKey.positionLabel(0, r/16, Component.CENTER_ALIGNMENT, Math.max(12,r/4));
                beanKey.positionSubLabel(0, r/4, Component.CENTER_ALIGNMENT, Math.max(9,r/5)); // smaller (system name)
                // provide x, y offset, depending on image size and free space
                if (_editor.showToolTip()) {
                    beanKey.setToolTipText(switchTooltip);
                }
                beanKey.setBackground(_editor.getDefaultBackgroundColor());
                //remove the line around icon switches?
                this.add(beanKey);
                break;
            case 3: // turnout/sensor/light Icon (selecting image by letter in switch name/label)
                beanSymbol.addMouseListener(new MouseAdapter() { // handled by JPanel
                    @Override
                    public void mouseClicked(MouseEvent me) {
                        operate(me, switchName);
                    }

                    @Override
                    public void mouseReleased(MouseEvent me) { // for Windows
                        if (me.isPopupTrigger()) {
                            showPopUp(me); // display the popup
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent me) { // for macOS, Linux
                        if (me.isPopupTrigger()) {
                            showPopUp(me); // display the popup
                        }
                    }
                });
                _text = true; // web server supports in-browser drawn switches
                _icon = true; // panel.js assigns only text OR icon for a single class such as BeanSwitch
                beanSymbol.setPreferredSize(new Dimension(2*r, 2*r));
                beanSymbol.setLabels(switchLabel, _uLabel);
                switch (beanTypeChar) {
                    case 'T' :
                        beanSymbol.positionLabel(0, -3*r/5, Component.CENTER_ALIGNMENT, Math.max(12,r/4));
                        beanSymbol.positionSubLabel(0, r/-5, Component.CENTER_ALIGNMENT, Math.max(9,r/5));
                        break;
                    case 'S' :
                    case 'L' :
                    default :
                        beanSymbol.positionLabel(0, r/-3, Component.CENTER_ALIGNMENT, Math.max(12,r/4));
                        beanSymbol.positionSubLabel(0, 7*r/8, Component.CENTER_ALIGNMENT, Math.max(9,r/5));
                }
                if (_editor.showToolTip()) {
                    beanSymbol.setToolTipText(switchTooltip);
                }
                beanSymbol.setBackground(_editor.getDefaultBackgroundColor());
                // common (in)activecolor etc defined in SwitchboardEditor, retrieved by Servlet
                // remove the line around icon switches?
                this.setBorder(BorderFactory.createLineBorder(_editor.getDefaultBackgroundColor(), 3));
                this.add(beanSymbol);
                break;
            case 0: // 0 = "Button" shape
            default:
                beanButton.addMouseListener(new MouseAdapter() { // handled by JPanel
                    @Override
                    public void mouseClicked(MouseEvent me) {
                        operate(me, switchName);
                    }

                    @Override
                    public void mouseReleased(MouseEvent me) { // for Windows
                        if (me.isPopupTrigger()) {
                            showPopUp(me); // display the popup
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent me) { // for macOS, Linux
                        if (me.isPopupTrigger()) {
                            showPopUp(me); // display the popup
                        }
                    }
                });
                _text = true;
                _icon = false;
                beanButton.setForeground(_editor.getDefaultTextColorAsColor());
                beanButton.setOpaque(true); // to show color from the start
                this.setBorder(BorderFactory.createLineBorder(_editor.getDefaultBackgroundColor(), 2));
                if (_editor.showToolTip()) {
                    beanButton.setToolTipText(switchTooltip);
                }
                beanButton.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        if (showUserName && beanButton.getHeight() < 50) {
                            beanButton.setVerticalAlignment(JLabel.TOP);
                        } else {
                            beanButton.setVerticalAlignment(JLabel.CENTER); //default
                        }
                    }
                });


                beanButton.setMargin(new Insets(4, 1, 2, 1));
                this.add(beanButton);
                break;
        }

        // connect to object or dim switch
        if (bean == null) {
            if (!_editor.hideUnconnected()) {
                // to dim unconnected symbols TODO make graphics see through, now icons just become bleak
                //float dim = 100f;
                switch (_shape) {
                    case 0:
                        beanButton.setEnabled(false);
                        break;
                    case 1:
//                        beanIcon.setOpacity(dim);
//                        break;
                    case 2:
//                        beanKey.setOpacity(dim);
//                        break;
                    case 3:
                    default:
//                        beanSymbol.setOpacity(dim);
                }
                displayState(0); // show unconnected as unknown/greyed
            }
        } else {
            _control = true;
            switch (beanTypeChar) {
                case 'T':
                    getTurnout().addPropertyChangeListener(this, _switchSysName, "Switchboard Editor Turnout Switch");
                    if (getTurnout().canInvert()) {
                        this.setInverted(getTurnout().getInverted()); // only add and set when supported by object/connection
                    }
                    break;
                case 'S':
                    getSensor().addPropertyChangeListener(this, _switchSysName, "Switchboard Editor Sensor Switch");
                    if (getSensor().canInvert()) {
                        this.setInverted(getSensor().getInverted()); // only add and set when supported by object/connection
                    }
                    break;
                default: // light
                    getLight().addPropertyChangeListener(this, _switchSysName, "Switchboard Editor Light Switch");
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
     * Store an object as NamedBeanHandle, using _label as the display
     * name.
     *
     * @param bean the object (either a Turnout, Sensor or Light) to attach
     *             to this switch
     */
    public void setNamedBean(@Nonnull NamedBean bean) {
        try {
            namedBean = nbhm.getNamedBeanHandle(_switchSysName, bean);
        } catch (IllegalArgumentException e) {
            log.error("invalid bean name= \"{}\" in Switchboard Button", _switchSysName);
        }
        _uName = bean.getUserName();
        if (_uName == null) {
            _uName = Bundle.getMessage("NoUserName");
        } else {
            if (showUserName) _uLabel = _uName;
        }
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
    public int getShape() {
        return _shape;
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
        return _switchSysName + ": " + _state;
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
        return _switchSysName + ": " + _state;
    }

    /**
     * Get text to display on this switch in Web Server panel when attached
     * object is Unknown (initial state displayed).
     *
     * @return text to show on unknown state (used on all types of objects)
     */
    public String getUnknownText() {
        return _switchSysName + ": ?";
    }

    public String getInconsistentText() {
        return _switchSysName + ": X";
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
     * @return display name including current state
     */
    public String getNameString() {
        return _switchSysName;
    }

    public String getUserNameString() {
        return _uLabel;
    }

    private String getSwitchButtonLabel(String label) {
        if (!showUserName || _uLabel.equals("")) {
            return label;
        } else {
            String subLabel = _uLabel.substring(0, (Math.min(_uLabel.length(), 35))); // reasonable max. to display 2 lines on tile
            return "<html><center>" + label + "</center><br><center><i>" + subLabel + "</i></center></html>"; // 2 lines of text
        }
    }

    /**
     * Drive the current state of the display from the state of the
     * connected bean.
     *
     * @param state integer representing the new state e.g. Turnout.CLOSED
     */
    public void displayState(int state) {
        String switchLabel;
        Color switchColor;
        log.debug("Change heard. STATE={}", state);
        // display abbreviated name of state instead of state index, fine for unconnected switches too
        switch (state) {
            case 1:
                switchLabel = getUnknownText();
                switchColor = Color.GRAY;
                break;
            case 2:
                switchLabel = getActiveText();
                switchColor = defaultActiveColor;
                break;
            case 4:
                switchLabel = getInactiveText();
                switchColor = defaultInactiveColor;
                break;
            default:
                switchLabel = getInconsistentText();
                switchColor = Color.WHITE;
                //log.warn("SwitchState INCONSISTENT"); // normal for unconnected switchboard
        }
        if (getNamedBean() == null) {
            switchLabel = _switchSysName; // unconnected, doesn't show state using : and ?
            log.debug("Switch label {} state {}, disconnected", switchLabel, state);
        } else {
            log.debug("Switch label {} state: {}, connected", switchLabel, state);
        }
        if (isText() && !isIcon()) { // to allow text buttons on web switchboard. always add graphic switches on web
            log.debug("Label = {}", getSwitchButtonLabel(switchLabel));
            beanButton.setText(getSwitchButtonLabel(switchLabel));
            beanButton.setBackground(switchColor); // only the color is visible TODO get access to bg color of JButton?
            beanButton.setOpaque(true);
        }
        if (isIcon() && beanIcon != null && beanKey != null && beanSymbol != null) {
            beanIcon.showSwitchIcon(state);
            beanIcon.setLabels(switchLabel, _uLabel);
            beanKey.showSwitchIcon(state);
            beanKey.setLabels(switchLabel, _uLabel);
            beanSymbol.showSwitchIcon(state);
            beanSymbol.setLabels(switchLabel, _uLabel);
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
            log.debug("property change: {} {} is now: {}", _switchSysName, e.getPropertyName(), e.getNewValue());
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
                _uLabel = (newUserName == null ? "" : newUserName); // store for display on icon
                if (newUserName == null || newUserName.equals("")) {
                    newUserName = Bundle.getMessage("NoUserName"); // longer for tooltip
                }
                beanButton.setToolTipText(_switchSysName + " (" + newUserName + ")");
                beanIcon.setToolTipText(_switchSysName + " (" + newUserName + ")");
                beanKey.setToolTipText(_switchSysName + " (" + newUserName + ")");
                beanSymbol.setToolTipText(_switchSysName + " (" + newUserName + ")");
                log.debug("User Name changed to {}", newUserName);
            }
        }
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
     * @param e unused
     * @return true when pop up displayed
     */
    public boolean showPopUp(MouseEvent e) {
        if (switchPopup != null) {
            switchPopup.removeAll();
        } else {
            switchPopup = new JPopupMenu();
        }

        switchPopup.add(getNameString());

        if (_editor.isEditable() && _editor.allControlling()) {
            // add tristate option if turnout has feedback
            if (namedBean != null) {
                //addTristateEntry(switchPopup); // beanswitches don't do anything with this property
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
                connectNewMenu.addActionListener((java.awt.event.ActionEvent e1) -> connectNew(_switchSysName));
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
        editItem.addActionListener((java.awt.event.ActionEvent e) -> renameBean());
    }

    javax.swing.JCheckBoxMenuItem invertItem = null;

    void addInvert(JPopupMenu popup) {
        invertItem = new javax.swing.JCheckBoxMenuItem(Bundle.getMessage("MenuInvertItem", _switchSysName));
        invertItem.setSelected(getInverted());
        popup.add(invertItem);
        invertItem.addActionListener((java.awt.event.ActionEvent e) -> setBeanInverted(invertItem.isSelected()));
    }

    /**
     * Edit user name on a switch.
     */
    public void renameBean() {
        NamedBean nb;
        String oldName = _uName;
        // show input dialog
        String newUserName = (String) JOptionPane.showInputDialog(null,
                Bundle.getMessage("EnterNewName", _switchSysName),
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
                String msg = Bundle.getMessage("WarningUserName", newUserName);
                JOptionPane.showMessageDialog(null, msg,
                        Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        _bname.setUserName(newUserName);
        if (oldName == null || oldName.equals("")) {
            if (!nbhm.inUse(_switchSysName, _bname)) {
                return; // no problem, so stop
            }
            String msg = Bundle.getMessage("UpdateToUserName", _editor.getSwitchTypeName(), newUserName, _switchSysName);
            int optionPane = JOptionPane.showConfirmDialog(null,
                    msg, Bundle.getMessage("UpdateToUserNameTitle"),
                    JOptionPane.YES_NO_OPTION);
            if (optionPane == JOptionPane.YES_OPTION) {
                //This will update the bean reference from the systemName to the userName
                try {
                    nbhm.updateBeanFromSystemToUser(_bname);
                } catch (JmriException ex) {
                    // We should never get an exception here as we already check that the username is not valid
                }
            }

        } else {
            nbhm.renameBean(oldName, newUserName, _bname); // will pick up name change in label
        }
        _editor.updatePressed(); // but we must redraw whole board
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
     *
     * @param set new inverted state, true for inverted, false for normal.
     */
    public void setBeanInverted(boolean set) {
        switch (beanTypeChar) {
            case 'T':
                if (getTurnout() != null && getTurnout().canInvert()) { // if supported
                    this.setInverted(set);
                    getTurnout().setInverted(set);
                }
                break;
            case 'S':
                if (getSensor() != null && getSensor().canInvert()) { // if supported
                    this.setInverted(set);
                    getSensor().setInverted(set);
                }
                break;
            case 'L':
                // Lights cannot be inverted, so never called
                return;
            default:
                log.error("Invert item: cannot parse bean name. userName = {}", _switchSysName);
        }
    }

    /**
     * Process mouseClick on this switch, passing in name for debug.
     *
     * @param e    the event heard
     * @param name ID of this button (identical to name of suggested bean
     *             object)
     */
    public void operate(MouseEvent e, String name) {
        log.debug("Button {} clicked", name);
        if (namedBean == null || e == null || e.isMetaDown()) {
            return;
        }
        alternateOnClick();
    }

    /**
     * Process mouseClick on this switch.
     * Similar to {@link #operate(MouseEvent, String)}.
     *
     * @param e the event heard
     */
    public void doMouseClicked(java.awt.event.MouseEvent e) {
        log.debug("Switch clicked");
        if (namedBean == null || e == null || e.isMetaDown()) {
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
                    log.error("invalid char in Switchboard Button \"{}\". State not set.", _switchSysName);
            }
        }
    }

    /**
     * Only for lights. Used for All Off/All On.
     * Skips unconnected switch icons.
     *
     * @param state On = 1, Off = 0
     */
    public void switchLight(int state) {
        if (namedBean != null) {
            getLight().setState(state);
        }
    }

    public void setBackgroundColor(Color bgcolor) {
        this.setBackground(bgcolor);
    }

    JmriJFrame addFrame = null;
    JTextField sysNameTextBox = new JTextField(12);
    JTextField userName = new JTextField(15);

    /**
     * Create new bean and connect it to this switch. Use type letter from
     * switch label (S, T or L).
     *
     * @param systemName system name of bean.
     */
    protected void connectNew(String systemName) {
        log.debug("Request new bean");
        userName.setText(""); // only available on unconnected switches, so no useful content yet
        // provide etc.
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("ConnectNewMenu", ""), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.display.switchboardEditor.SwitchboardEditor", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener okListener = this::okAddPressed;
            ActionListener cancelListener = this::cancelAddPressed;
            AddNewDevicePanel switchConnect = new AddNewDevicePanel(sysNameTextBox, userName, "ButtonOK", okListener, cancelListener);
            switchConnect.setSystemNameFieldIneditable(); // prevent user interference with switch label
            switchConnect.setOK(); // activate OK button on Add new device pane
            addFrame.add(switchConnect);
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    protected void cancelAddPressed(ActionEvent e) {
        if (addFrame != null) {
            addFrame.setVisible(false);
            addFrame.dispose();
            addFrame = null;
        }
    }

    protected void okAddPressed(ActionEvent e) {
        NamedBean nb;
        String manuPrefix = _editor.getSwitchManu();
        String user = userName.getText();
        if (user.trim().equals("")) {
            user = null;
        }
        // systemName can't be changed, fixed
        if (addFrame != null) {
            addFrame.setVisible(false);
            addFrame.dispose();
            addFrame = null;
        }
        switch (_switchSysName.charAt(manuPrefix.length())) {
            case 'T':
                Turnout t;
                try {
                    // add turnout to JMRI (w/appropriate manager)
                    t = InstanceManager.turnoutManagerInstance().provideTurnout(_switchSysName);
                    t.setUserName(user);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(_switchSysName);
                    return; // without creating
                }
                nb = jmri.InstanceManager.turnoutManagerInstance().getTurnout(_switchSysName);
                break;
            case 'S':
                Sensor s;
                try {
                    // add Sensor to JMRI (w/appropriate manager)
                    s = InstanceManager.sensorManagerInstance().provideSensor(_switchSysName);
                    s.setUserName(user);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(_switchSysName);
                    return; // without creating
                }
                nb = jmri.InstanceManager.sensorManagerInstance().getSensor(_switchSysName);
                break;
            case 'L':
                Light l;
                try {
                    // add Light to JMRI (w/appropriate manager)
                    l = InstanceManager.lightManagerInstance().provideLight(_switchSysName);
                    l.setUserName(user);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(_switchSysName);
                    return; // without creating
                }
                nb = jmri.InstanceManager.lightManagerInstance().getLight(_switchSysName);
                break;
            default:
                log.error("connectNew - okAddPressed: cannot parse bean name. sName = {}", _switchSysName);
                return;
        }
        if (nb == null) {
            log.warn("failed to connect switch to item {}", _switchSysName);
        } else {
            // set switch on Switchboard to display current state of just connected bean
            log.debug("sName state: {}", nb.getState());
            try {
                if (_editor.getSwitch(_switchSysName) == null) {
                    log.warn("failed to update switch to state of {}", _switchSysName);
                } else {
                    _editor.updatePressed();
                }
            } catch (NullPointerException npe) {
                handleCreateException(_switchSysName);
                // exit without updating
            }
        }
    }

    /**
     * Chack the switch label currently displayed.
     * Used in test.
     *
     * @return line 1 of the label of this switch
     */
    protected String getIconLabel() {
        switch (_shape) {
            case 0 : // button
                String lbl = beanButton.getText();
                if (!lbl.startsWith("<")) {
                    return lbl;
                } else { // 2 line label, "<html><center>" + label + "</center>..."
                    return lbl.substring(14, lbl.indexOf("</center>"));
                }
            case 1:
                return beanIcon.getIconLabel();
            case 2:
                return beanKey.getIconLabel();
            case 3:
                return beanSymbol.getIconLabel();
            default:
                return "";
        }
    }

    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                        Bundle.getMessage("ErrorSwitchAddFailed"), sysName),
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
        private String subTag = "";
        private int labelX = 16;
        private int labelY = 53;
        private int subLabelX = 16;
        private int subLabelY = 53;
        private int textSize = 12;
        private int subTextSize = 10;
        private float textAlign = 0.0f;
        private float subTextAlign = 0.0f;
        private float ropOffset = 0f;
        private int r = 10; // radius of circle fitting inside tile rect in px drawing units
        private RescaleOp rop;

        /**
         * Create an icon from 2 alternating png images.
         *
         * @param filepath1 the ON image
         * @param filepath2 the OFF image
         * @param drawingRadius max distance in px from center of switch canvas, unit used for relative scaling
         */
        public IconSwitch(String filepath1, String filepath2, int drawingRadius) {
            // load image files
            try {
                image1 = ImageIO.read(new File(filepath1));
                image2 = ImageIO.read(new File(filepath2));
                image = image2; // start off as showing inactive/closed
            } catch (IOException ex) {
                log.error("error reading image from {}-{}", filepath1, filepath2, ex);
            }
            if (drawingRadius > 10) r = drawingRadius;
            log.debug("radius={} size={}", r, getWidth());
        }

        public void setOpacity(float offset) {
            ropOffset = offset;
            float ropScale = 1f;
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
         * @param sName string to display (system name)
         * @param uName secondary string to display (user name)
         */
        protected void setLabels(String sName, String uName) {
            tag = sName;
            subTag = uName;
            this.repaint();
        }

        private String getIconLabel() {
            return tag;
        }

        /**
         * Position (sub)label on switch.
         *
         * @param x horizontal offset from top left corner, positive to the
         *          right
         * @param y vertical offset from top left corner, positive down
         * @param align one of: JComponent.LEFT_ALIGNMENT (0.0f), CENTER_ALIGNMENT (0.5f),
         *              RIGHT_ALIGNMENT (1.0f)
         * @param fontsize size in points for label text display
         */
        protected void positionLabel(int x, int y, float align, int fontsize) {
            labelX = x;
            labelY = y;
            textAlign = align;
            textSize = fontsize;
        }

        protected void positionSubLabel(int x, int y, float align, int fontsize) {
            subLabelX = x;
            subLabelY = y;
            subTextAlign = align;
            subTextSize = fontsize;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g.translate(r, r); // set origin to center
            //int imgZoom = Math.min(2*r/image.getWidth(), 2*r/image.getHeight()); // how to enlarge icon on painting?
            g2d.drawImage(image, rop, image.getWidth()/-2, image.getHeight()/-2); // center bitmap
            //g.drawImage(image, 0, 0, null);
            g.setFont(getFont());
            if (ropOffset > 0f) {
                g.setColor(Color.GRAY); // dimmed
            } else {
                g.setColor(_editor.getDefaultTextColorAsColor());
            }

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, textSize));
            g2d.setRenderingHint( // smoother text display
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (Math.abs(textAlign - Component.CENTER_ALIGNMENT) < .0001) {
                FontMetrics metrics = g.getFontMetrics(); // figure out where the center of the string is
                labelX = metrics.stringWidth(tag) / -2;
            }
            g.drawString(tag, labelX, labelY); // draw name on top of button image (vertical, horizontal offset from top left)

            if (showUserName) {
                g.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, Math.max(subTextSize, 6)));
                if (Math.abs(subTextAlign - Component.CENTER_ALIGNMENT) < .0001) {
                    FontMetrics metrics2 = g.getFontMetrics(); // figure out where the center of the string is
                    subLabelX = metrics2.stringWidth(subTag) / -2;
                }
                g.drawString(subTag, subLabelX, subLabelY); // draw user name at bottom
            }
            this.repaint();
        }

    }

    private final static Logger log = LoggerFactory.getLogger(BeanSwitch.class);

}
