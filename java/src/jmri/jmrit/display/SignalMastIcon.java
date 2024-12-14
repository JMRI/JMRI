package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.palette.SignalMastItemPanel;
import jmri.jmrit.picker.PickListModel;
import jmri.swing.NamedBeanComboBox;
import jmri.util.SystemType;
import jmri.util.swing.*;

/**
 * An icon to display a status of a {@link jmri.SignalMast}.
 * <p>
 * The icons displayed are loaded from the {@link jmri.SignalAppearanceMap} in
 * the {@link jmri.SignalMast}.
 *
 * @see jmri.SignalMastManager
 * @see jmri.InstanceManager
 * @author Bob Jacobsen Copyright (C) 2009, 2014
 */
public class SignalMastIcon extends PositionableIcon implements java.beans.PropertyChangeListener {

    public SignalMastIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(editor);
        _control = true;
    }

    private NamedBeanHandle<SignalMast> namedMast;
    private NamedBeanHandle<Sensor> namedClickSensor;
    private NamedBeanHandle<Sensor> namedControlClickSensor;

    public void setShowAutoText(boolean state) {
        _text = state;
        _icon = !_text;
    }

    @Override
    public Positionable deepClone() {
        SignalMastIcon pos = new SignalMastIcon(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(SignalMastIcon pos) {
        pos.setSignalMast(getNamedSignalMast());
        pos._iconMap = cloneMap(_iconMap, pos);
        pos.setClickMode(getClickMode());
        pos.setControlClickMode(getControlClickMode());
        pos.setClickSensor( namedClickSensor == null ? "" : namedClickSensor.getBean().getDisplayName() );
        pos.setControlClickSensor( namedControlClickSensor == null ? "" : namedControlClickSensor.getBean().getDisplayName() );
        pos.setLitMode(getLitMode());
        pos.useIconSet(useIconSet());
        return super.finishClone(pos);
    }

    /**
     * Attached a signalmast element to this display item
     *
     * @param sh Specific SignalMast handle
     */
    public void setSignalMast(NamedBeanHandle<SignalMast> sh) {
        if (namedMast != null) {
            getSignalMast().removePropertyChangeListener(this);
        }
        namedMast = sh;
        if (namedMast != null) {
            getIcons();
            displayState(mastState());
            getSignalMast().addPropertyChangeListener(this, namedMast.getName(), "SignalMast Icon");
        }
    }

    /**
     * Taken from the layout editor Attached a numbered element to this display
     * item
     *
     * @param pName Used as a system/user name to lookup the SignalMast object
     */
    public void setSignalMast(String pName) {
        SignalMast mMast = InstanceManager.getDefault(SignalMastManager.class).getNamedBean(pName);
        if (mMast == null) {
            log.warn("did not find a SignalMast named {}", pName);
        } else {
            setSignalMast(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(pName, mMast));
        }
    }

    private void getIcons() {
        _iconMap = new java.util.HashMap<>();
        java.util.Enumeration<String> e = getSignalMast().getAppearanceMap().getAspects();
        boolean error = false;
        while (e.hasMoreElements()) {
            String aspect = e.nextElement();
            error = loadIcons(aspect);
        }
        if (error) {
            JmriJOptionPane.showMessageDialog(_editor.getTargetFrame(),
                    Bundle.getMessage("SignalMastIconLoadError", getSignalMast().getDisplayName() ),
                    Bundle.getMessage("SignalMastIconLoadErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
        }
        //Add in specific appearances for dark and held
        loadIcons("$dark");
        loadIcons("$held");
    }

    private boolean loadIcons(String aspect) {
        String s = getSignalMast().getAppearanceMap().getImageLink(aspect, useIconSet);
        if (s.isEmpty()) {
            if (aspect.startsWith("$")) {
                log.debug("No icon found for specific appearance {}", aspect);
            } else {
                log.error("No icon found for appearance {}", aspect);
            }
            return true;
        } else {
            if (!s.contains("preference:")) {
                s = s.substring(s.indexOf("resources"));
            }
            NamedIcon n;
            try {
                n = new NamedIcon(s, s);
            } catch ( NullPointerException e ) {
                JmriJOptionPane.showMessageDialog(_editor.getTargetFrame(), Bundle.getMessage("SignalMastIconLoadError2", new Object[]{aspect, s, getNameString()}),
                    Bundle.getMessage("SignalMastIconLoadErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                log.error("{} : Cannot load Icon", Bundle.getMessage("SignalMastIconLoadError2", aspect, s, getNameString()));
                return true;
            }
            _iconMap.put(s, n);
            if (_rotate != 0) {
                n.rotate(_rotate, this);
            }
            if (_scale != 1.0) {
                n.scale(_scale, this);
            }
        }
        return false;
    }

    public NamedBeanHandle<SignalMast> getNamedSignalMast() {
        return namedMast;
    }

    public SignalMast getSignalMast() {
        if (namedMast == null) {
            return null;
        }
        return namedMast.getBean();
    }

    @Override
    public NamedBean getNamedBean() {
        return getSignalMast();
    }

    /**
     * Get current appearance of the mast
     *
     * @return An aspect from the SignalMast
     */
    public String mastState() {
        if (getSignalMast() == null) {
            return "<empty>";
        } else {
            return getSignalMast().getAspect();
        }
    }

    // update icon as state of SignalMast changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("property change: {} current state: {}", e.getPropertyName(), mastState());
        displayState(mastState());
        _editor.getTargetPanel().repaint();
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_SignalMastIcon");
    }

    @Override
    public String getNameString() {
        String name;
        if (getSignalMast() == null) {
            name = Bundle.getMessage("NotConnected");
        } else {
            name = getSignalMast().getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
        }
        return name;
    }

    /**
     * {@inheritDoc }
     * Handles Control (right) clicks
     */
    @Override
    public boolean showPopUp(JPopupMenu popup) {
        log.info("showPopup");
        if (isEditable()) {
            addToEditingPopUp(popup);
        } else {
            return performControlClickNonEditAction(popup);
        }
        return true;
    }

    private void addToEditingPopUp(JPopupMenu popup) {
        JMenu clickMenu = new JMenu(Bundle.getMessage("WhenClicked"));
        ButtonGroup clickButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem r;
        r = new JRadioButtonMenuItem(Bundle.getMessage("ChangeAspect"));
        r.addActionListener(e -> setClickMode(0));
        clickButtonGroup.add(r);
        r.setSelected(clickMode == 0);
        clickMenu.add(r);

        r = new JRadioButtonMenuItem(Bundle.getMessage("AlternateLit"));
        r.addActionListener(e -> setClickMode(1));
        clickButtonGroup.add(r);
        r.setSelected(clickMode == 1);
        clickMenu.add(r);

        r = new JRadioButtonMenuItem(Bundle.getMessage("AlternateHeld"));
        r.addActionListener(e -> setClickMode(2));
        clickButtonGroup.add(r);
        r.setSelected(clickMode == 2);
        clickMenu.add(r);

        r = new JRadioButtonMenuItem(Bundle.getMessage("ActivateSensor", 
            getDisplaySensorName(getClickSensor())));
        r.addActionListener(e -> {
            String str = getSensorFromDialog( popup, getClickSensor() );
            if ( ! str.isEmpty() ) {
                setClickSensor(str);
                setClickMode(3);
            }
        });
        clickButtonGroup.add(r);
        clickMenu.add(r);
        r.setSelected(clickMode == 3);

        popup.add(clickMenu);

        JMenu rightClickMenu = new JMenu(getWhenControlClickActionText());
        ButtonGroup rightClickButtonGroup = new ButtonGroup();
        r = new JRadioButtonMenuItem(Bundle.getMessage("SelectAspect"));
        r.addActionListener(e -> setControlClickMode(0));
        rightClickButtonGroup.add(r);
        r.setSelected(controlClickMode == 0);
        rightClickMenu.add(r);

        r = new JRadioButtonMenuItem(Bundle.getMessage("ActivateSensor", 
            getDisplaySensorName(getControlClickSensor())));
        r.addActionListener(e ->  {
            String str = getSensorFromDialog( popup, getControlClickSensor() );
            if ( ! str.isEmpty() ) {
                setControlClickSensor(str);
                setControlClickMode(1);
            }
        });
        rightClickButtonGroup.add(r);
        rightClickMenu.add(r);
        r.setSelected(controlClickMode == 1);

        popup.add(rightClickMenu);

        // add menu to select handling of lit parameter
        JMenu litMenu = new JMenu(Bundle.getMessage("WhenNotLit"));
        ButtonGroup litButtonGroup = new ButtonGroup();
        r = new JRadioButtonMenuItem(Bundle.getMessage("ShowAppearance"));
        r.setIconTextGap(10);
        r.addActionListener(e -> {
            setLitMode(false);
            displayState(mastState());
        });
        litButtonGroup.add(r);
        r.setSelected(!litMode);
        litMenu.add(r);

        r = new JRadioButtonMenuItem(Bundle.getMessage("ShowDarkIcon"));
        r.setIconTextGap(10);
        r.addActionListener(e -> {
            setLitMode(true);
            displayState(mastState());
        });
        litButtonGroup.add(r);
        r.setSelected(litMode);
        litMenu.add(r);
        popup.add(litMenu);

        if (namedMast != null) {
            java.util.Enumeration<String> en = getSignalMast().getSignalSystem().getImageTypeList();
            if (en.hasMoreElements()) {
                JMenu iconSetMenu = new JMenu(Bundle.getMessage("SignalMastIconSet"));
                ButtonGroup iconTypeGroup = new ButtonGroup();
                setImageTypeList(iconTypeGroup, iconSetMenu, "default");
                while (en.hasMoreElements()) {
                    setImageTypeList(iconTypeGroup, iconSetMenu, en.nextElement());
                }
                popup.add(iconSetMenu);
            }
            popup.add(new jmri.jmrit.signalling.SignallingSourceAction(Bundle.getMessage("SignalMastLogic"), getSignalMast()));
            JMenu aspect = new JMenu(Bundle.getMessage("SelectAspect"));
            final java.util.Vector<String> aspects = getSignalMast().getValidAspects();
            for (int i = 0; i < aspects.size(); i++) {
                final int index = i;
                aspect.add(new AbstractAction(aspects.elementAt(index)) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        getSignalMast().setAspect(aspects.elementAt(index));
                    }
                });
            }
            popup.add(aspect);
        }
        addTransitPopup(popup);
    }

    private String getDisplaySensorName(Sensor sensorSystemName) {
        if ( sensorSystemName == null ) {
            return "";
        }
        return ": " + sensorSystemName.getDisplayName();
    }

    private String getSensorFromDialog(JPopupMenu popup, Sensor clickSensor){
        NamedBeanComboBox<Sensor> sensorCombo = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SensorManager.class),  clickSensor,
            DisplayOptions.USERNAME_SYSTEMNAME);
        sensorCombo.setAllowNull(false);
        JComboBoxUtil.setupComboBoxMaxRows(sensorCombo);

        int ok = JmriJOptionPane.showConfirmDialog(popup.getInvoker(), 
            new Object[]{ new JLabel(Bundle.getMessage("SelectSensActive")) , sensorCombo}, 
            Bundle.getMessage("SelectSensActive"), JmriJOptionPane.OK_CANCEL_OPTION);
        if ( ok == JmriJOptionPane.OK_OPTION ) {
            Sensor ff = sensorCombo.getSelectedItem();
            if ( ff != null ) {
                return ff.getDisplayName();
            }
        }
        return "";
    }

    private void addTransitPopup(JPopupMenu popup) {
        if ((InstanceManager.getDefault(SectionManager.class).getNamedBeanSet().size()) > 0
                && InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).isAdvancedRoutingEnabled()) {

            if (tct == null) {
                tct = new jmri.jmrit.display.layoutEditor.TransitCreationTool();
            }
            popup.addSeparator();
            String addString = Bundle.getMessage("MenuTransitCreate");
            if (tct.isToolInUse()) {
                addString = Bundle.getMessage("MenuTransitAddTo");
            }
            popup.add(new AbstractAction(addString) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        tct.addNamedBean(getSignalMast());
                    } catch (JmriException ex) {
                        JmriJOptionPane.showMessageDialog(popup.getInvoker(), ex.getMessage(),
                            Bundle.getMessage("TransitErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            if (tct.isToolInUse()) {
                popup.add(new AbstractAction(Bundle.getMessage("MenuTransitAddComplete")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Transit created;
                        try {
                            tct.addNamedBean(getSignalMast());
                            created = tct.createTransit();
                            JmriJOptionPane.showMessageDialog(popup.getInvoker(), Bundle.getMessage("TransitCreatedMessage", created.getDisplayName()),
                                Bundle.getMessage("TransitCreatedTitle"), JmriJOptionPane.INFORMATION_MESSAGE);
                        } catch (JmriException ex) {
                            JmriJOptionPane.showMessageDialog(popup.getInvoker(), ex.getMessage(),
                                Bundle.getMessage("TransitErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                popup.add(new AbstractAction(Bundle.getMessage("MenuTransitCancel")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tct.cancelTransitCreate();
                    }
                });
            }
            popup.addSeparator();
        }
    }

    private boolean performControlClickNonEditAction(JPopupMenu popup) {
        log.info("performing right click action ", new Exception("Trace"));
        switch ( controlClickMode ) {
            case 1:
                Sensor s = getControlClickSensor();
                if ( s == null ) {
                    log.error("{} : No Sensor set for Control Click action.", getSignalMast().getDisplayName());
                    return false; // do not display popup
                }
                try {
                    s.setKnownState(Sensor.ACTIVE);
                } catch ( JmriException ex ) {
                    log.warn("Could not set Mast Click Sensor Active: {}", ex.getMessage());
                }
                return false; // do not display popup
            case 0:
            default:
                final java.util.Vector<String> aspects = getSignalMast().getValidAspects();
                for (int i = 0; i < aspects.size(); i++) {
                    final int index = i;
                    popup.add(new AbstractAction(aspects.elementAt(index)) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            getSignalMast().setAspect(aspects.elementAt(index));
                        }
                    });
                }
                return true; // display popup
        }
    }

    static volatile jmri.jmrit.display.layoutEditor.TransitCreationTool tct;

    private void setImageTypeList(ButtonGroup iconTypeGroup, JMenu iconSetMenu, final String item) {
        JRadioButtonMenuItem im;
        im = new JRadioButtonMenuItem(item);
        im.addActionListener(e -> useIconSet(item));
        iconTypeGroup.add(im);
        im.setSelected(useIconSet.equals(item));
        iconSetMenu.add(im);

    }

    @Override
    public boolean setRotateOrthogonalMenu(JPopupMenu popup) {
        return false;
    }

    SignalMastItemPanel _itemPanel;

    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        popup.add(new AbstractAction(Bundle.getMessage("EditItem", Bundle.getMessage("BeanNameSignalMast"))) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editItem();
            }
        });
        return true;
    }

    protected void editItem() {
        _paletteFrame = makePaletteFrame(Bundle.getMessage("EditItem",
                Bundle.getMessage("BeanNameSignalMast")));
        _itemPanel = new SignalMastItemPanel(_paletteFrame, "SignalMast", getFamily(),
                PickListModel.signalMastPickModelInstance());
        ActionListener updateAction = a -> updateItem();
        // _iconMap keys with local names - Let SignalHeadItemPanel figure this out
        _itemPanel.init(updateAction, _iconMap);
        _itemPanel.setSelection(getSignalMast());
        initPaletteFrame(_paletteFrame, _itemPanel);
    }

    void updateItem() {
        setSignalMast(_itemPanel.getTableSelection().getSystemName());
        setFamily(_itemPanel.getFamilyName());
        finishItemUpdate(_paletteFrame, _itemPanel);
    }

    /**
     * Change the SignalMast aspect when the icon is clicked.
     *
     */
    @Override
    public void doMouseClicked(JmriMouseEvent e) {
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
    public void performMouseClicked(JmriMouseEvent e) {
        if (e.isMetaDown() || e.isAltDown()) {
            return;
        }
        SignalMast mast = getSignalMast();
        if ( mast == null) {
            log.error("No SignalMast connection, can't process click");
            return;
        }
        switch (clickMode) {
            case 0:
                java.util.Vector<String> aspects = mast.getValidAspects();
                int idx = aspects.indexOf(mast.getAspect()) + 1;
                if (idx >= aspects.size()) {
                    idx = 0;
                }
                mast.setAspect(aspects.elementAt(idx));
                return;
            case 1:
                mast.setLit(!mast.getLit());
                return;
            case 2:
                mast.setHeld(!mast.getHeld());
                return;
            case 3:
                Sensor s = getClickSensor();
                if ( s == null ) {
                    log.error("{} : No Sensor set for Click action.", mast.getDisplayName());
                    return;
                }
                try {
                    s.setKnownState(Sensor.ACTIVE);
                } catch ( JmriException ex ) {
                    log.error("Could not set Mast Click Sensor Active: {}", ex.getMessage());
                }
                return;
            default:
                log.error("Click in mode {}", clickMode);
        }
    }

    String useIconSet = "default";

    public void useIconSet(String icon) {
        if (icon == null) {
            icon = "default";
        }
        if (useIconSet.equals(icon)) {
            return;
        }
        //clear the old icon map out.
        _iconMap = null;
        useIconSet = icon;
        getIcons();
        displayState(mastState());
        _editor.getTargetPanel().repaint();
    }

    public String useIconSet() {
        return useIconSet;
    }

    /**
     * Set display of ClipBoard copied or duplicated mast
     */
    @Override
    public void displayState(int s) {
        displayState(mastState());
    }

    /**
     * Drive the current state of the display from the state of the underlying
     * SignalMast object.
     *
     * @param state the state to display
     */
    public void displayState(String state) {
        updateSize();
        if (log.isDebugEnabled()) { // Avoid signal lookup unless needed
            if (getSignalMast() == null) {
                log.debug("Display state {}, disconnected", state);
            } else {
                log.debug("Display state {} for {}", state, getSignalMast().getSystemName());
            }
        }
        if (isText()) {
            if (getSignalMast().getHeld()) {
                if (isText()) {
                    super.setText(Bundle.getMessage("Held"));
                }
                return;
            } else if (getLitMode() && !getSignalMast().getLit()) {
                super.setText(Bundle.getMessage("Dark"));
                return;
            }
            super.setText(state);
        }
        if (isIcon()) {
            if ((state != null) && (getSignalMast() != null)) {
                String s = getSignalMast().getAppearanceMap().getImageLink(state, useIconSet);
                if ((getSignalMast().getHeld()) && (getSignalMast().getAppearanceMap().getSpecificAppearance(SignalAppearanceMap.HELD) != null)) {
                    s = getSignalMast().getAppearanceMap().getImageLink("$held", useIconSet);
                } else if (getLitMode() && !getSignalMast().getLit() && (getSignalMast().getAppearanceMap().getImageLink("$dark", useIconSet) != null)) {
                    s = getSignalMast().getAppearanceMap().getImageLink("$dark", useIconSet);
                }
                if (s.equals("")) {
                    /*We have no appearance to set, therefore we will exit at this point.
                     This can be considered normal if we are requesting an appearance
                     that is not support or configured, such as dark or held */
                    return;
                }
                if (!s.contains("preference:")) {
                    s = s.substring(s.indexOf("resources"));
                }

                // tiny global cache, due to number of icons
                if (_iconMap == null) {
                    getIcons();
                }
                NamedIcon n = _iconMap.get(s);
                super.setIcon(n);
                updateSize();
                setSize(n.getIconWidth(), n.getIconHeight());
            }
        } else {
            super.setIcon(null);
        }
    }

    @Override
    public boolean setEditIconMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    protected void rotateOrthogonal() {
        super.rotateOrthogonal();
        // bug fix, must repaint icons that have same width and height
        displayState(mastState());
        repaint();
    }

    @Override
    public void rotate(int deg) {
        super.rotate(deg);
        if (getSignalMast() != null) {
            displayState(mastState());
        }
    }

    @Override
    public void setScale(double s) {
        super.setScale(s);
        if (getSignalMast() != null) {
            displayState(mastState());
        }
    }

    /**
     * What to do on click? 0 means sequence through aspects; 1 means alternate
     * the "lit" aspect; 2 means alternate the
     * {@link jmri.SignalAppearanceMap#HELD} aspect.
     */
    protected int clickMode = 0;

    private int controlClickMode = 0;

    /**
     * Set the Click Mode.
     * 0 - Sequence through aspects. (Default)
     * 1 - Alternate the "lit" aspect.
     * 2 - Alternate the {@link jmri.SignalAppearanceMap#HELD} aspect.
     * 3 - Set Sensor Active.
     * @param mode new mode.
     */
    public void setClickMode(int mode) {
        clickMode = mode;
        if ( mode != 3 ) {
            setClickSensor("");
        }
    }

    /**
     * Set the Control ( right ) Click Mode when not in edit mode.
     * 0 - Select Aspect to display. (Default)
     * 1 - Set Sensor Active.
     * @param mode new mode.
     */
    public void setControlClickMode(int mode) {
        controlClickMode = mode;
        if ( mode != 1 ) {
            setControlClickSensor("");
        }
    }

    /**
     * Get the Click Mode.
     * @return current click mode.
     */
    public int getClickMode() {
        return clickMode;
    }

    /**
     * Get the Control ( right ) Click Mode.
     * @return the current Control Click Mode.
     */
    public int getControlClickMode() {
        return controlClickMode;
    }

    /**
     * How to handle lit vs not lit?
     * <p>
     * False means ignore (always show R/Y/G/etc appearance on screen); True
     * means show {@link jmri.SignalAppearanceMap#DARK} if lit is set false.
     */
    protected boolean litMode = false;

    public void setLitMode(boolean mode) {
        litMode = mode;
    }

    public boolean getLitMode() {
        return litMode;
    }

    /**
     * Set the Name of the Click Sensor.
     * @param newSensor Name of the Sensor. Can be empty String to clear value.
     */
    public void setClickSensor(@Nonnull String newSensor){
        if ( newSensor.isBlank() ){
            namedClickSensor = null;
            return;
        }
        Sensor s = InstanceManager.getDefault(SensorManager.class).getNamedBean(newSensor);
        if (s == null) {
            log.warn("did not find a Click Sensor named {}", newSensor);
        } else {
            namedClickSensor = InstanceManager.getDefault(
                NamedBeanHandleManager.class).getNamedBeanHandle(newSensor, s);
        }
    }

    /**
     * Set the Name of the Control ( Right ) Click Sensor.
     * @param newSensor Name of the Sensor. Can be empty String to clear value.
     */
    public void setControlClickSensor(@Nonnull String newSensor){
        if ( newSensor.isBlank() ){
            namedControlClickSensor = null;
            return;
        }
        Sensor s = InstanceManager.getDefault(SensorManager.class).getNamedBean(newSensor);
        if (s == null) {
            log.warn("did not find a ControlClick Sensor named {}", newSensor);
        } else {
            namedControlClickSensor = InstanceManager.getDefault(
                NamedBeanHandleManager.class).getNamedBeanHandle(newSensor, s);
        }
    }

    /**
     * Get the Click Sensor
     * @return the Sensor.
     */
    @CheckForNull
    public Sensor getClickSensor() {
        if ( namedClickSensor == null ) {
            return null;
        }
        return namedClickSensor.getBean();
    }

    /**
     * Get the Control ( right ) Click Sensor
     * @return the Sensor.
     */
    @CheckForNull
    public Sensor getControlClickSensor() {
        if ( namedControlClickSensor == null ) {
            return null;
        }
        return namedControlClickSensor.getBean();
    }

    @CheckForNull
    public NamedBeanHandle<Sensor> getNamedClickSensor(){
        return namedClickSensor;
    }

    @CheckForNull
    public NamedBeanHandle<Sensor> getNamedControlClickSensor(){
        return namedControlClickSensor;
    }

    private static String getWhenControlClickActionText(){
        return Bundle.getMessage(SystemType.getType() == SystemType.MACOSX
            ? "WhenCtrlClick" : "WhenRightClick");
    }

    @Nonnull
    public List<NamedBeanUsageReport> getUsageReport(NamedBean bean) {
        List<NamedBeanUsageReport> report = new ArrayList<>();
        if (bean != null) {
            // SensorManager sm = InstanceManager.getDefault(SensorManager.class);
            if (bean.equals(getClickSensor())) {
                report.add(new NamedBeanUsageReport("ClickSensor", "Active when SignalMast Clicked"));
            }
            if (bean.equals(getControlClickSensor())) {
                report.add(new NamedBeanUsageReport("ControlClickSensor", "Active when SignalMast Control Clicked"));
            }
        }
        return report;
    }

    @Override
    public void dispose() {
        if (namedMast != null) {
            getSignalMast().removePropertyChangeListener(this);
        }
        super.dispose();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalMastIcon.class);

}
