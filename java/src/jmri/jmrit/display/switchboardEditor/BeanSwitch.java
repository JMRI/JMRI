package jmri.jmrit.display.switchboardEditor;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import javax.annotation.CheckForNull;
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
import jmri.util.SystemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for a switchboard interface object.
 * <p>
 * Contains a JButton or JPanel to control existing turnouts, sensors and
 * lights.
 * Separated from SwitchboardEditor.java in 4.12.3
 *
 * @author Egbert Broerse Copyright (c) 2017, 2018, 2020, 2021
 */
public class BeanSwitch extends JPanel implements java.beans.PropertyChangeListener, ActionListener {

    private final JButton beanButton = new JButton();
    private IconSwitch iconSwitch;
    private final int _shape;
    private int square = 75; // outside dimension of graphic, normally < 2*radius
    private int radius = 50; // max distance in px from center of switch canvas, unit used for relative scaling
    private double popScale = 1.0d;
    private int showUserName = 1;
    private Color activeColor = Color.RED; // for testing a separate BeanSwitch
    private Color inactiveColor = Color.GREEN;
    Color textColor = Color.BLACK;
    protected String switchLabel;
    protected String switchTooltip;
    protected boolean _text;
    protected boolean _icon = false;
    protected boolean _control = false;
    protected int _showingState = 0;
    protected String _stateSign;
    protected String _color;
    protected String stateClosed = Bundle.getMessage("StateClosedShort");
    protected String stateThrown = Bundle.getMessage("StateThrownShort");

    private final SwitchboardEditor _editor;
    private char beanTypeChar = 'T'; // initialize now to allow testing
    private String switchTypeName = "Turnout";
    private String manuPrefix = "I";
    private final String _switchSysName;
    private String _switchDisplayName;
    boolean showToolTip = true;
    boolean allControlling = true;
    boolean panelEditable = false;
    // the associated Bean object
    private final NamedBean _bname;
    private NamedBeanHandle<?> namedBean = null; // can be Turnout, Sensor or Light
    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
    private String _uName = "unconnected";
    private String _uLabel = ""; // for display, empty if userName == null or showUserName != 1

    /**
     * Ctor.
     *
     * @param index       ordinal of this switch on Switchboard.
     * @param bean        layout object to connect to.
     * @param switchName  descriptive name corresponding with system name to
     *                    display in switch tooltip, i.e. LT1.
     * @param shapeChoice Button, Slider, Key (all drawn on screen) or Icon (sets of graphic files).
     * @param editor      main switchboard editor.
     */
    public BeanSwitch(int index, @CheckForNull NamedBean bean, @Nonnull String switchName, int shapeChoice, @CheckForNull SwitchboardEditor editor) {
        log.debug("Name = [{}]", switchName);
        _switchSysName = switchName;
        _switchDisplayName = switchName; // updated later on if a user name is available
        _editor = editor;
        _bname = bean;
        _shape = shapeChoice;
        //if (_switchSysName.length() < 3) { // causes unexpected effects?
        //    log.error("Switch name {} too short for a valid system name", switchName);
        //    return;
        //}
        sysNameTextBox.setText(switchName); // setting name here allows test of AddNew()
        boolean hideUnconnected = false;
        Color backgroundColor = Color.LIGHT_GRAY;
        if (editor != null) {
            // get connection
            manuPrefix = editor.getSwitchManu(); // connection/manufacturer prefix i.e. default) M for MERG
            switchTypeName = _editor.getSwitchTypeName();
            // get display settings
            hideUnconnected = editor.hideUnconnected();
            allControlling = editor.allControlling();
            panelEditable = editor.isEditable();
            showToolTip = editor.showToolTip();
            showUserName = editor.nameDisplay();
            radius = editor.getTileSize()/2; // max WxH of canvas inside cell, used as relative unit to draw
            square = editor.getIconScale();
            // get colors
            textColor = editor.getDefaultTextColorAsColor();
            backgroundColor = editor.getDefaultBackgroundColor();
            activeColor = editor.getActiveColorAsColor();
            inactiveColor = editor.getInactiveColorAsColor();
            popScale = _editor.getPaintScale();
        }
        if (bean != null) {
            _uName = bean.getUserName();
            log.debug("Switch userName from bean: {}", _uName);
            if (_uName == null) {
                _uName = Bundle.getMessage("NoUserName");
            } else if (showUserName == 1) {
                _uLabel = _uName;
            } else if (showUserName == 2) {
                _switchDisplayName = _uName;
                //switchLabel = _uName; // replace system name (menu option setting)
            }
        }

        switchTooltip = switchName + " (" + _uName + ")";
        this.setLayout(new BorderLayout()); // makes JButtons expand to the whole grid cell

        beanTypeChar = _switchSysName.charAt(manuPrefix.length()); // bean type, i.e. L, usually at char(1)
        // check for space char which might be caused by connection name > 2 chars and/or space in name
        if (beanTypeChar != 'T' && beanTypeChar != 'S' && beanTypeChar != 'L') { // add if more bean types are supported
            log.error("invalid char in Switchboard Button \"{}\". Check connection name.", _switchSysName);
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorSwitchAddFailed"),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        log.debug("BeanSwitch graphic tilesize/2  r={} scale={}", radius, square);

        // look for bean to connect to by name
        log.debug("beanconnect = {}, beantype = {}", manuPrefix, beanTypeChar);
        try {
            if (bean != null) {
                namedBean = nbhm.getNamedBeanHandle(switchName, bean);
            }
        } catch (IllegalArgumentException e) {
            log.error("invalid bean name= \"{}\" in Switchboard Button", switchName);
        }

        _text = true; // not actually used, web server supports in-browser drawn switches check in
        _icon = true; // panel.js assigns only text OR icon for a single class such as BeanSwitch
        // attach shape specific code to this beanSwitch
        switch (_shape) {
            case SwitchboardEditor.SLIDER: // slider shape
                iconSwitch = new IconSwitch(_shape, beanTypeChar); // draw as Graphic2D
                iconSwitch.setPreferredSize(new Dimension(2*radius, 2*radius)); // tweak layout
                iconSwitch.positionLabel(0, 5*radius/-8, Component.CENTER_ALIGNMENT, Math.max(12, radius/4));
                iconSwitch.positionSubLabel(0, radius/-5, Component.CENTER_ALIGNMENT, Math.max(9, radius/5)); // smaller (system name)
                this.add(iconSwitch);
                break;
            case SwitchboardEditor.KEY: // Maerklin style keyboard
                iconSwitch = new IconSwitch(_shape, beanTypeChar); // draw as Graphic2D
                iconSwitch.setPreferredSize(new Dimension(2*radius, 2*radius)); // tweak layout
                iconSwitch.positionLabel(0, 0, Component.CENTER_ALIGNMENT, Math.max(12, radius/4));
                iconSwitch.positionSubLabel(0, 3*radius/10, Component.CENTER_ALIGNMENT, Math.max(9, radius/5)); // smaller (system name)
                // provide x, y offset, depending on image size and free space
                this.add(iconSwitch);
                break;
            case SwitchboardEditor.SYMBOL:
                // turnout/sensor/light symbol using image files (selecting image by letter in switch name/label)
                iconSwitch = new IconSwitch(
                        rootPath + beanTypeChar + "-on-s.png",
                        rootPath + beanTypeChar + "-off-s.png", backgroundColor);
                iconSwitch.setPreferredSize(new Dimension(2*radius, 2*radius));
                switch (beanTypeChar) {
                    case 'T' :
                        iconSwitch.positionLabel(0, 5*radius/-8, Component.CENTER_ALIGNMENT, Math.max(12, radius/4));
                        iconSwitch.positionSubLabel(0, radius/-4, Component.CENTER_ALIGNMENT, Math.max(9, radius/5));
                        break;
                    case 'S' :
                    case 'L' :
                    default :
                        iconSwitch.positionLabel(0, 5*radius/-8, Component.CENTER_ALIGNMENT, Math.max(12, radius/4));
                        iconSwitch.positionSubLabel(0, radius/-3, Component.CENTER_ALIGNMENT, Math.max(9, radius/5));
                }
                // common (in)activecolor etc defined in SwitchboardEditor, retrieved by Servlet
                this.setBorder(BorderFactory.createLineBorder(backgroundColor, 3));
                this.add(iconSwitch);
                break;
            case SwitchboardEditor.BUTTON: // 0 = "Button" shape
            default:
                _icon = false;
                beanButton.setText(getSwitchButtonLabel(_switchDisplayName + ": ?")); // initial text to display
                beanButton.setForeground(textColor);
                beanButton.setOpaque(true); // to show color from the start
                this.setBorder(BorderFactory.createLineBorder(backgroundColor, 2));
                beanButton.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        if ((showUserName == 1) && (beanButton.getHeight() < 50)) {
                            beanButton.setVerticalAlignment(JLabel.TOP);
                        } else {
                            beanButton.setVerticalAlignment(JLabel.CENTER); //default
                        }
                    }
                });
                beanButton.addMouseListener(new MouseAdapter() { // pass on mouseEvents
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        redispatchToParent(e);
                    }
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        redispatchToParent(e);
                    }
                    @Override
                    public void mousePressed(MouseEvent e) {
                        redispatchToParent(e);
                    }
                });
                beanButton.setMargin(new Insets(4, 1, 2, 1));
                this.add(beanButton);
                break;
        }
        // common configuration of graphic switches
        addMouseListener(new MouseAdapter() { // handled by JPanel
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
                    log.debug("what's clicking?");
                    showPopUp(me); // display the popup
                }
            }
        });
        if (showToolTip) {
            setToolTipText(switchTooltip);
        }
        if (iconSwitch != null) {
            iconSwitch.setBackground(backgroundColor);
            iconSwitch.setLabels(switchLabel, _uLabel);
        }
        // connect to object or dim switch
        if (bean == null) {
            if (!hideUnconnected) {
                // to dim unconnected symbols TODO make graphics see through, now icons just become bleak
                //float dim = 100f;
                switch (_shape) {
                    case SwitchboardEditor.BUTTON:
                        beanButton.setEnabled(false);
                        break;
                    case SwitchboardEditor.SLIDER:
                    case SwitchboardEditor.KEY:
                    case SwitchboardEditor.SYMBOL:
                    default:
                        // iconSwitch.setOpacity(dim); // activate for graphic painted switches
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
            displayState(bean.getState());
        }
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
            log.error("invalid bean name= \"{}\" in Switchboard Button \"{}\"", _switchSysName, _switchDisplayName);
        }
        _uName = bean.getUserName();
        if (_uName == null) {
            _uName = Bundle.getMessage("NoUserName");
        } else {
            if (showUserName == 1) {
                _uLabel = _uName;
            } else if (showUserName == 2) {
                switchLabel = _uName;
            }
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
        if (beanTypeChar == 'T') {
            _stateSign = stateClosed; // +
        } else {
            // Light, Sensor
            _stateSign = "+";         // 1 char abbreviation for StateOff not clear
        }
        return _switchDisplayName + ": " + _stateSign;
    }

    /**
     * Get text to display on this switch on Switchboard and in Web Server panel when attached
     * object is Inactive.
     *
     * @return text to show on inactive state (differs per type of objects)
     */
    public String getInactiveText() {
        // fetch bean specific abbreviation
        if (beanTypeChar == 'T') {
            _stateSign = stateThrown; // +
        } else {
            // Light, Sensor
            _stateSign = "-";         // 1 char abbreviation for StateOff not clear
        }
        return _switchDisplayName + ": " + _stateSign;
    }

    /**
     * Get text to display on this switch in Web Server panel when attached
     * object is Unknown (initial state displayed).
     *
     * @return text to show on unknown state (used on all types of objects)
     */
    public String getUnknownText() {
        return _switchDisplayName + ": ?";
    }

    public String getInconsistentText() {
        return _switchDisplayName + ": X";
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
     * @return display name not including current state
     */
    public String getNameString() {
        return _switchDisplayName;
    }

    public String getUserNameString() {
        return _uLabel;
    }

    private String getSwitchButtonLabel(String label) {
        if ((showUserName == 0) || (_uLabel.equals(""))) {
            return label;
        } else if (showUserName == 2) {
            return _uLabel.substring(0, (Math.min(_uLabel.length(), 35)));
        } else {
            String subLabel = _uLabel.substring(0, (Math.min(_uLabel.length(), 35))); // reasonable max. to display 2 lines on tile
            return "<html><center>" + label + "</center><br><center><i>" + subLabel + "</i></center></html>"; // 2 lines of text
        }
    }

    /**
     * Drive the current state of the display from the state of the
     * connected bean.
     *
     * @param newState integer representing the new state e.g. Turnout.CLOSED
     */
    public void displayState(int newState) {
        String switchLabel;
        Color switchColor;
        if (getNamedBean() == null) {
            switchLabel = _switchDisplayName; // unconnected, doesn't show state using : and ?
            switchColor = Color.GRAY;
            log.debug("Switch label {} state {}, disconnected", switchLabel, newState);
        } else {
            if (newState == _showingState) {
                return; // prevent redrawing on repeated identical commands
            }
            // display abbreviated name of state instead of state index, fine for unconnected switches too
            switch (newState) {
                case 1:
                    switchLabel = getUnknownText();
                    switchColor = Color.GRAY;
                    break;
                case 2:
                    switchLabel = getActiveText();
                    switchColor = activeColor;
                    break;
                case 4:
                    switchLabel = getInactiveText();
                    switchColor = inactiveColor;
                    break;
                default:
                    switchLabel = getInconsistentText();
                    switchColor = Color.WHITE;
                    //log.warn("SwitchState INCONSISTENT"); // normal for unconnected switchboard
                    log.debug("Switch label {} state: {}, connected", switchLabel, newState);
            }
        }
        if (isText() && !isIcon()) { // to allow text buttons on web switchboard.
            log.debug("Label = {}", getSwitchButtonLabel(switchLabel));
            beanButton.setText(getSwitchButtonLabel(switchLabel));
            beanButton.setBackground(switchColor); // only the color is visible on macOS
            // TODO get access to bg color of JButton?
            beanButton.setOpaque(true);
        } else if (isIcon() && (iconSwitch != null)) {
            iconSwitch.showSwitchIcon(newState);
            iconSwitch.setLabels(switchLabel, _uLabel);
        }
        _showingState = newState;
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
     * Update switch as state of bean changes.
     *
     * @param e the PropertyChangeEvent heard
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("property change: {} {} is now: {}", _switchSysName, e.getPropertyName(), e.getNewValue());
        }
        if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue());
            displayState(now);
            log.debug("Item state changed");
        }
        if (e.getPropertyName().equals("UserName")) {
            // update tooltip
            String newUserName;
            if (showToolTip) {
                newUserName = ((String) e.getNewValue());
                _uLabel = (newUserName == null ? "" : newUserName); // store for display on icon
                if (newUserName == null || newUserName.equals("")) {
                    newUserName = Bundle.getMessage("NoUserName"); // longer for tooltip
                }
                setToolTipText(_switchSysName + " (" + newUserName + ")");
                log.debug("User Name changed to {}", newUserName);
            }
        }
    }

    void cleanup() {
        if (namedBean != null) {
            switch (beanTypeChar) {
                case 'T':
                    getTurnout().removePropertyChangeListener(this);
                    break;
                case 'S':
                    getSensor().removePropertyChangeListener(this);
                    break;
                default: // light
                    getLight().removePropertyChangeListener(this);
            }
        }
        namedBean = null;
    }

    JPopupMenu switchPopup;
    JMenuItem connectNewMenu = new JMenuItem(Bundle.getMessage("ConnectNewMenu", "..."));

    /**
     * Show pop-up on a switch with its unique attributes including the
     * (un)connected bean. Derived from
     * {@link jmri.jmrit.display.switchboardEditor.SwitchboardEditor#showPopUp(Positionable, MouseEvent)}
     *
     * @param e unused because we now our own location
     * @return true when pop up displayed
     */
    public boolean showPopUp(MouseEvent e) {
        if (switchPopup != null) {
            switchPopup.removeAll();
        } else {
            switchPopup = new JPopupMenu();
        }

        switchPopup.add(getNameString());

        if (panelEditable && allControlling) {
            if (namedBean != null) {
                addEditUserName(switchPopup);
                switch (beanTypeChar) {
                    case 'T':
                        if (getTurnout().canInvert()) { // check whether supported by this turnout
                            addInvert(switchPopup);
                        }
                        // tristate and momentary (see TurnoutIcon) can't be set per switch
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
                connectNewMenu.addActionListener((java.awt.event.ActionEvent e1) -> connectNew());
            }
        }
        // display the popup
        switchPopup.show(this, this.getWidth()/3 + (int) ((popScale - 1.0) * this.getX()),
                this.getHeight()/3 + (int) ((popScale - 1.0) * this.getY()));

        return true;
    }

    javax.swing.JMenuItem editItem = null;

    void addEditUserName(JPopupMenu popup) {
        editItem = new javax.swing.JMenuItem(Bundle.getMessage("EditNameTitle", "..."));
        popup.add(editItem);
        editItem.addActionListener((java.awt.event.ActionEvent e) -> renameBeanDialog());
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
    public void renameBeanDialog() {
        String oldName = _uName;
        JTextField name = new JTextField(oldName);
        // show input dialog, build by hand so that Jemmy can reach it in test
        JOptionPane pane = new JOptionPane(
                new Object[]{Bundle.getMessage("EnterNewName", _switchSysName), name},
                JOptionPane.INFORMATION_MESSAGE, JOptionPane.YES_NO_OPTION, null,
                new Object[]{Bundle.getMessage("ButtonOK"), Bundle.getMessage("ButtonCancel")}, 0);
        JDialog dialog = pane.createDialog(null, Bundle.getMessage("EditNameTitle", ""));
        dialog.setVisible(true);
        if (pane.getValue().equals(Bundle.getMessage("ButtonCancel"))) {
            return;
        }
        String newUserName = name.getText();
        if (newUserName == null || newUserName.equals(Bundle.getMessage("NoUserName")) || newUserName.isEmpty()) { // user cancelled
            log.debug("new user name was empty");
            JOptionPane.showMessageDialog(null, Bundle.getMessage("WarningEmptyUserName"), Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        renameBean(newUserName, oldName);
    }

    /**
     * Edit user name on a switch.
     *
     * @param newUserName string to use as user name replacement
     * @param oldName current user name (used to prevent useless change)
     */
    protected void renameBean(String newUserName, String oldName) {
        NamedBean nb;
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
            String msg = Bundle.getMessage("UpdateToUserName", switchTypeName, newUserName, _switchSysName);
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
        if (_editor != null) {
            _editor.updatePressed(); // but we redraw whole switchboard
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
        if (allControlling) {
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
     * switch label (T, S or L).
     */
    protected void connectNew() {
        log.debug("Request new bean");
        userName.setText(""); // this method is only available on unconnected switches, so no useful content to fill in yet
        // provide etc.
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("ConnectNewMenu", ""), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.display.switchboardEditor.SwitchboardEditor", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener okListener = this::okAddPressed;
            ActionListener cancelListener = this::cancelAddPressed;
            AddNewDevicePanel switchConnect = new AddNewDevicePanel(sysNameTextBox, userName, "ButtonOK", okListener, cancelListener);
            switchConnect.setSystemNameFieldIneditable(); // prevent user interference with switch label (proposed system name)
            switchConnect.setOK(); // activate OK button on Add new device pane
            addFrame.add(switchConnect);
        }
        addFrame.pack();
        addFrame.setLocationRelativeTo(this);
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
     * Check the switch label currently displayed.
     * Used in test.
     *
     * @return line 1 of the label of this switch
     */
    protected String getIconLabel() {
        switch (_shape) {
            case SwitchboardEditor.BUTTON: // button
                String lbl = beanButton.getText();
                if (!lbl.startsWith("<")) {
                    return lbl;
                } else { // 2 line label, "<html><center>" + label + "</center>..."
                    return lbl.substring(14, lbl.indexOf("</center>"));
                }
            case SwitchboardEditor.SLIDER:
            case SwitchboardEditor.KEY:
            case SwitchboardEditor.SYMBOL:
                return iconSwitch.getIconLabel();
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

    /**
     * Class to display individual bean state switches on a JMRI Switchboard
     * using 2DGraphic drawing code or alternating 2 image files.
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
        private int _shape = SwitchboardEditor.BUTTON;
        private int _state = 0;
        private RescaleOp rop;

        /**
         * Create an icon from 2 alternating png images. shape is assumed SwitchboardEditor.SYMBOL
         *
         * @param filepath1 the ON image
         * @param filepath2 the OFF image
         * @param back the background color set on the Switchboard, used to fill in empty parts of rescaled image
         */
        public IconSwitch(String filepath1, String filepath2, Color back) {
            // load image files
            try {
                image1 = ImageIO.read(new File(filepath1));
                image2 = ImageIO.read(new File(filepath2));
                if ((square != 100) && (square >= 25) && (square <= 150)) {
                    image1 = resizeImage(image1, square, back);
                    image2 = resizeImage(image2, square, back);
                }
                image = image2; // start off as showing inactive/closed
            } catch (IOException ex) {
                log.error("error reading image from {}-{}", filepath1, filepath2, ex);
            }
            _shape = SwitchboardEditor.SYMBOL;
            if (radius > 10) r = radius;
            log.debug("radius={} size={}", r, getWidth());
        }

        /**
         * Ctor with fixed scale of image at 100%. Original public ctor.
         *
         * @param filepath1 the ON image
         * @param filepath2 the OFF image
         */
        @Deprecated
        public IconSwitch(String filepath1, String filepath2) {
            this(filepath1, filepath2, Color.GRAY);
        }

        /**
         * Ctor to draw graphic fully in Graphics.
         *
         * @param shape int to specify switch shape {@link SwitchboardEditor} constants
         * @param type beanType to draw (optionally ignored depending on shape, eg. for slider)
         */
        public IconSwitch(int shape, int type) {
            if ((shape == SwitchboardEditor.BUTTON) || (shape == SwitchboardEditor.SYMBOL)) {
                return; // when SYMBOL is migrated, leave in place for 0 = BUTTON (drawn as JButtons, not graphics)
            }
            _shape = shape;
            if (radius > 10) r = radius;
            log.debug("DrawnIcon type={}", type);
        }

        public void setOpacity(float offset) {
            ropOffset = offset;
            float ropScale = 1.0f;
            rop = new RescaleOp(ropScale, ropOffset, null);
        }

        protected void showSwitchIcon(int stateIndex) {
            log.debug("showSwitchIcon {}", stateIndex);
            if ((_shape == SwitchboardEditor.SLIDER) || (_shape == SwitchboardEditor.KEY)) {
                //redraw (colors are already set above
                _state = stateIndex;
            } else {
                if (image1 != null && image2 != null) {
                    switch (stateIndex) {
                        case 2:
                            image = image1; // on/Thrown/Active
                            break;
                        case 1:
                        default:
                            image = image2; // off, also for connected & unknown
                            break;
                    }
                    this.repaint();
                }
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
            // set antialiasing hint for macOS and Windows
            // note: antialiasing has performance problems on some variants of Linux (Raspberry pi)
            if (SystemType.isMacOSX() || SystemType.isWindows()) {
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                        RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            // now the image
            g.translate(r, r); // set origin to center
            if (_shape == SwitchboardEditor.SLIDER) { // slider
                // Draw symbol on the beanswitch widget canvas
                // see panel.js for vector drawing: var $drawWidgetSymbol = function(id, state), ctx is same as g2d
                //  clear for alternating text and 'moving' items not covered by new paint
                if (_state == 4) {
                    g.setColor(inactiveColor); // simple change in color
                } else if (_state == 2) {
                    g.setColor(activeColor);
                } else {
                    g.setColor(Color.GRAY);
                }
                // slider, same shape for all beanTypes (S, T, L)
                // the sliderspace
                g2d.fillRoundRect(-r/2, 0, r, r/2, r/2, r/2);
                g.setColor((_state == 2 || _state == 4) ? Color.BLACK : Color.GRAY);
                g2d.drawRoundRect(-r/2, 0, r, r/2, r/2, r/2);
                // the knob
                int knobX = (_state == 2 ? 0 : -r/2);
                g.setColor(Color.WHITE);
                g2d.fillOval(knobX, 0, r/2, r/2);
                g.setColor(Color.BLACK);
                g2d.drawOval(knobX, 0, r/2, r/2);
                //g2d.drawRect(-r, -r, 2*r, 2*r); // debug tile size outline
            } else if (_shape == SwitchboardEditor.KEY) {
                // key, same shape for all beanTypes (S, T, L)
                // red = upper rounded rect
                g.setColor(_state == 2 ? activeColor : SwitchboardEditor.darkActiveColor); // simple change in color
                g2d.fillRoundRect(-3*r/8, -2*r/3, 3*r/4, r/3, r/6, r/6);
                // green = lower rounded rect
                g.setColor(_state == 4 ? inactiveColor : SwitchboardEditor.darkInactiveColor); // simple change in color
                g2d.fillRoundRect(-3*r/8, r/3, 3*r/4, r/3, r/6, r/6);
                // add round LED at top (only part defined as floats)
                Point2D center = new Point2D.Float(0.05f*r, -7.0f*r/8.0f);
                float radius = r/6.0f;
                float[] dist = {0.0f, 0.8f};
                Color[] colors = {Color.WHITE, (_state == 2 ? activeColor : Color.GRAY)};
                RadialGradientPaint pnt = new RadialGradientPaint(center, radius, dist, colors);
                g2d.setPaint(pnt);
                g2d.fillOval(-r/8, -r, r/4, r/4);
                // with black outline
                g.setColor(Color.BLACK);
                g2d.drawOval(-r/8, -r, r/4, r/4);
                //g2d.drawRect(-r, -r, 2*r, 2*r); // debug tile size outline
            } else {
                // use image file
                g2d.drawImage(image, rop, image.getWidth()/-2, image.getHeight()/-2); // center bitmap
                //g2d.drawRect(-r, -r, 2*r, 2*r); // debug tile size outline
            }
            g.setFont(getFont());
            if (ropOffset > 0f) {
                g.setColor(Color.GRAY); // dimmed
            } else {
                g.setColor(textColor);
            }

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, textSize));

            if (Math.abs(textAlign - Component.CENTER_ALIGNMENT) < .0001) {
                FontMetrics metrics = g.getFontMetrics(); // figure out where the center of the string is
                labelX = metrics.stringWidth(tag)/-2;
            }
            g.drawString(tag, labelX, labelY); // draw name on top of button image (vertical, horizontal offset from top left)

            if (showUserName == 1) {
                g.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, Math.max(subTextSize, 6)));
                if (Math.abs(subTextAlign - Component.CENTER_ALIGNMENT) < .0001) {
                    FontMetrics metrics2 = g.getFontMetrics(); // figure out where the center of the string is
                    subLabelX = metrics2.stringWidth(subTag)/-2;
                }
                g.drawString(subTag, subLabelX, subLabelY); // draw user name at bottom
            }
        }
    }

    private void redispatchToParent(MouseEvent e){
        Component source = (Component) e.getSource();
        MouseEvent parentEvent = SwingUtilities.convertMouseEvent(source, e, source.getParent());
        source.getParent().dispatchEvent(parentEvent);
    }

    /**
     * Get a resized copy of the image.
     *
     * @param image the image to rescale
     * @param scale scale percentage as int (will be divided by 100 in operation)
     * @param background background color to paint on resized image, prevents null value (black)
     * @return a reduced/enlarged pixel image
     */
    public static BufferedImage resizeImage(final Image image, int scale, Color background) {
        int newWidth = scale*(image.getWidth(null))/100;
        int newHeight = scale*image.getHeight(null)/100;
        final BufferedImage bimg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2d = bimg.createGraphics();
        g2d.setColor(background);
        log.debug("BGCOLOR={}", background);
        g2d.fillRect(0, 0, newWidth, newHeight);
        //below three lines are for RenderingHints for better image quality at cost of higher processing time
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        return bimg;
    }

    private final static Logger log = LoggerFactory.getLogger(BeanSwitch.class);

}
