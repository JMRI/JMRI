package jmri.jmrit.swing.meter;

import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.*;

import jmri.*;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JmriJFrame;

/**
 * Frame providing a simple LCD-based display of track voltage.
 * <p>
 * Adapted from ammeter to display voltage and current.
 *
 * @author Ken Cameron        Copyright (C) 2007
 * @author Mark Underwood     Copyright (C) 2007
 * @author Andrew Crosland    Copyright (C) 2020
 * @author Daniel Bergqvist   Copyright (C) 2020
 * @author B. Milhaupt        Copyright (C) 2020
 */
public class MeterFrame extends JmriJFrame {

    public enum Unit {
        Percent(1.0),    // Not a unit, but here anyway as display option
        MicroVolt(1000*1000),
        MilliVolt(1000),
        Volt(1.0),
        KiloVolt(1/1000.0),
        MicroAmpere(1000*1000),
        MilliAmpere(1000),
        Ampere(1.0),
        KiloAmpere(1/1000.0);

        private final double multiply;

        Unit(double m) { multiply = m; }
    }

    private static final int MAX_INTEGER_DIGITS = 7;
    private static final int MAX_DECIMAL_DIGITS = 3;

    private final UUID uuid;

    private final List<Meter> voltageMeters = new ArrayList<>();
    private final List<Meter> currentMeters = new ArrayList<>();

    // GUI member declarations
    private JMenuBar menuBar;
    ArrayList<JLabel> integerDigitIcons;
    ArrayList<JLabel> decimalDigitIcons;
    JLabel decimal;
    boolean decimalDot = true;
    Map<Unit, JLabel> unitLabels = new HashMap<>();

    Map<Meter, JCheckBoxMenuItem> meter_MenuItemMap = new HashMap<>();
    Map<Unit, JCheckBoxMenuItem> units_MenuItemMap = new HashMap<>();
    Map<Integer, JCheckBoxMenuItem> integerDigits_MenuItemMap = new HashMap<>();
    Map<Integer, JCheckBoxMenuItem> decimalDigits_MenuItemMap = new HashMap<>();
    JMenuItem lastSelectedMeterMenuItem;
    JMenuItem lastSelectedIntegerDigitsMenuItem;
    JMenuItem lastSelectedDecimalDigitsMenuItem;
    int numIntegerDigits = 3;
    int numDecimalDigits = 0;
    int lastNumDecimalDigits = -1;
    int widthOfAllIconsToDisplay = 0;
    int oldWidthOfAllIconsToDisplay = -1;
    boolean frameIsInitialized = false;
    Unit selectedUnit = Unit.Volt;

    int digitIconWidth;
    int decimalIconWidth;
    int unitIconWidth;
    int iconHeight;

    private PropertyChangeListener propertyChangeListener;

    private Meter meter;
    
    private String initialMeterName=""; // remember the initially selected meter since it may not exist at frame creation

    NamedIcon[] integerDigits = new NamedIcon[10];
    NamedIcon[] decimalDigits = new NamedIcon[10];
    NamedIcon decimalIcon;
    NamedIcon microVoltIcon;
    NamedIcon milliVoltIcon;
    NamedIcon voltIcon;
    NamedIcon kiloVoltIcon;
    NamedIcon microAmpIcon;
    NamedIcon milliAmpIcon;
    NamedIcon ampIcon;
    NamedIcon kiloAmpIcon;
    NamedIcon percentIcon;
    NamedIcon errorIcon;

    JPanel pane1;
    JPanel meterPane;

    public MeterFrame() {
        this(UUID.randomUUID());
    }

    public MeterFrame(UUID uuid) {
        super(Bundle.getMessage("VoltageMeterTitle"));

        this.uuid = uuid;

        MeterManager mm = InstanceManager.getNullableDefault(MeterManager.class);
        if (mm == null) throw new RuntimeException("No meter manager exists");

        addAllMeters();

        if (!voltageMeters.isEmpty()) {
            setMeter(voltageMeters.get(0));
        } else if (!currentMeters.isEmpty()) {
            setMeter(currentMeters.get(0));
        } else {
            setTitle(Bundle.getMessage("VoltageMeterTitle"));
        }

        MeterFrameManager.getInstance().register(this);
    }

    /**
     * Get the UUID of this frame.
     * <p>
     * The UUID is used if two different panel files are loaded with the same
     * meter frame.
     *
     * @return the UUID of this frame
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Get the meter that is displayed.
     * @return the meter
     */
    public Meter getMeter() {
        return meter;
    }

    /**
     * Set the meter that is displayed.
     * @param m the meter or null if no meter is to be shown
     */
    public void setMeter(Meter m) {
        if (lastSelectedMeterMenuItem != null) lastSelectedMeterMenuItem.setSelected(false);

        if (meter != null) {
            meter.disable();
            meter.removePropertyChangeListener(NamedBean.PROPERTY_STATE, propertyChangeListener);
        }

        meter = m;

        if (meter == null) return;

        meter.addPropertyChangeListener(NamedBean.PROPERTY_STATE, propertyChangeListener);
        meter.enable();

        if (frameIsInitialized) {
            // Initially we want to scale the icons to fit the previously saved window size
            scaleImage();

            JCheckBoxMenuItem menuItem = meter_MenuItemMap.get(meter);
            menuItem.setSelected(true);
            lastSelectedMeterMenuItem = menuItem;

            updateMenuUnits();

            //set Units and Digits, except for restored settings
            if (!getInitialMeterName().equals(m.getSystemName())) {
                initSettingsMenu();
                setInitialMeterName(""); //clear out saved name after first use
            }
        }

        if (meter instanceof VoltageMeter) {
            setTitle(Bundle.getMessage("VoltageMeterTitle2", m.getDisplayName()));
        } else {
            setTitle(Bundle.getMessage("CurrentMeterTitle2", m.getDisplayName()));
        }
    }
    
    JMenu voltageMetersMenu = null;
    JMenu currentMetersMenu = null;
    
    @Override
    public void initComponents() {
        MeterManager mm = InstanceManager.getNullableDefault(MeterManager.class);
        if (mm == null) {
            return;
        }
        mm.addDataListener(new MeterFrame.BeanListListener(this));
        // Create menu bar

        menuBar = new JMenuBar();
        voltageMetersMenu = new JMenu(Bundle.getMessage("MenuVoltageMeters"));
        menuBar.add(voltageMetersMenu);
        if (voltageMeters.size() == 0) {
            voltageMetersMenu.add(new JMenuItem(Bundle.getMessage("NoMeters")));
        } else {
            for (Meter m : voltageMeters) {
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(new SelectMeterAction(m.getDisplayName(), m));
                voltageMetersMenu.add(item);
                meter_MenuItemMap.put(m, item);
            }
        }

        currentMetersMenu = new JMenu(Bundle.getMessage("MenuCurrentMeters"));
        menuBar.add(currentMetersMenu);
        if (currentMeters.size() == 0) {
            currentMetersMenu.add(new JMenuItem(Bundle.getMessage("NoMeters")));
        } else {
            for (Meter m : currentMeters) {
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(new SelectMeterAction(m.getDisplayName(), m));
                currentMetersMenu.add(item);
                meter_MenuItemMap.put(m, item);
            }
        }

        JMenu settingsMenu = new JMenu(Bundle.getMessage("MenuMeterSettings"));
        menuBar.add(settingsMenu);

        JMenu unitsMenu = new JMenu(Bundle.getMessage("MenuMeterUnitMenu"));
        settingsMenu.add(unitsMenu);
        for (Unit unit : Unit.values()) {
            final Unit u = unit;
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("MenuMeter_" + unit.name())) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    units_MenuItemMap.get(selectedUnit).setSelected(false);
                    unitLabels.get(selectedUnit).setVisible(false);
                    units_MenuItemMap.get(u).setSelected(true);
                    unitLabels.get(u).setVisible(true);
                    selectedUnit = u;
                    update();
                }
            });
            units_MenuItemMap.put(unit, item);
            unitsMenu.add(item);
        }

        settingsMenu.addSeparator();
        JMenu digitsMenu = new JMenu(Bundle.getMessage("MenuMeterIntegerDigits"));
        settingsMenu.add(digitsMenu);
        for (int i = 1; i <= MAX_INTEGER_DIGITS; i++) {
            final int ii = i;
            final String label = i + "";
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(new AbstractAction(label) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    integerDigits_MenuItemMap.get(numIntegerDigits).setSelected(false);
                    numIntegerDigits = ii;
                    update();
                }
            });
            integerDigits_MenuItemMap.put(ii, item);
            digitsMenu.add(item);
            if (ii == numIntegerDigits)
                item.setSelected(true);
        }

        JMenu decimalMenu = new JMenu(Bundle.getMessage("MenuMeterDecimalDigits"));
        settingsMenu.add(decimalMenu);
        for (int i = 0; i <= MAX_DECIMAL_DIGITS; i++) {
            final int ii = i;
            final String label2 = i + "";
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(new AbstractAction(label2) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    decimalDigits_MenuItemMap.get(numDecimalDigits).setSelected(false);
                    decimalDigits_MenuItemMap.get(ii).setSelected(true);
                    numDecimalDigits = ii;
                    update();
                }
            });
            decimalDigits_MenuItemMap.put(ii, item);
            decimalMenu.add(item);
            if (ii == numDecimalDigits)
                item.setSelected(true);
        }

        setJMenuBar(menuBar);

        // clear the contents
        getContentPane().removeAll();

        pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));

        meterPane = new JPanel();
        meterPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));

        // build the actual multimeter display.
        meterPane.setLayout(new BoxLayout(meterPane, BoxLayout.X_AXIS));

        // decimal separator is ',' instead of '.' in some locales
        DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        if (symbols.getDecimalSeparator() == ',') {
            decimalDot = false;
        }
        //Load the images (these are now the larger version of the original gifs
        for (int i = 0; i < 10; i++) {
            integerDigits[i] = new NamedIcon("resources/icons/misc/LCD/Lcd_" + i + "b.GIF", "resources/icons/misc/LCD/Lcd_" + i + "b.GIF");
        }
        for (int i = 0; i < 10; i++) {
            decimalDigits[i] = new NamedIcon("resources/icons/misc/LCD/Lcd_" + i + "b.GIF", "resources/icons/misc/LCD/Lcd_" + i + "b.GIF");
        }
        if (decimalDot) {
            decimalIcon = new NamedIcon("resources/icons/misc/LCD/decimalb.gif", "resources/icons/misc/LCD/decimalb.gif");
        } else {
            decimalIcon = new NamedIcon("resources/icons/misc/LCD/decimalc.gif", "resources/icons/misc/LCD/decimalc.gif");
        }
        microVoltIcon = new NamedIcon("resources/icons/misc/LCD/uvoltb.gif", "resources/icons/misc/LCD/uvoltb.gif");
        milliVoltIcon = new NamedIcon("resources/icons/misc/LCD/mvoltb.gif", "resources/icons/misc/LCD/mvoltb.gif");
        voltIcon = new NamedIcon("resources/icons/misc/LCD/voltb.gif", "resources/icons/misc/LCD/voltb.gif");
        kiloVoltIcon = new NamedIcon("resources/icons/misc/LCD/kvoltb.gif", "resources/icons/misc/LCD/kvoltb.gif");
        microAmpIcon = new NamedIcon("resources/icons/misc/LCD/uampb.gif", "resources/icons/misc/LCD/uampb.gif");
        milliAmpIcon = new NamedIcon("resources/icons/misc/LCD/mampb.gif", "resources/icons/misc/LCD/mampb.gif");
        ampIcon = new NamedIcon("resources/icons/misc/LCD/ampb.gif", "resources/icons/misc/LCD/ampb.gif");
        kiloAmpIcon = new NamedIcon("resources/icons/misc/LCD/kampb.gif", "resources/icons/misc/LCD/kampb.gif");
        percentIcon = new NamedIcon("resources/icons/misc/LCD/percentb.gif", "resources/icons/misc/LCD/percentb.gif");
        errorIcon = new NamedIcon("resources/icons/misc/LCD/Lcd_Error.GIF", "resources/icons/misc/LCD/Lcd_Error.GIF");

        decimal = new JLabel(decimalIcon);
        unitLabels.put(Unit.Percent, new JLabel(percentIcon));
        unitLabels.put(Unit.MicroVolt, new JLabel(microVoltIcon));
        unitLabels.put(Unit.MilliVolt, new JLabel(milliVoltIcon));
        unitLabels.put(Unit.Volt, new JLabel(voltIcon));
        unitLabels.put(Unit.KiloVolt, new JLabel(kiloVoltIcon));
        unitLabels.put(Unit.MicroAmpere, new JLabel(microAmpIcon));
        unitLabels.put(Unit.MilliAmpere, new JLabel(milliAmpIcon));
        unitLabels.put(Unit.Ampere, new JLabel(ampIcon));
        unitLabels.put(Unit.KiloAmpere, new JLabel(kiloAmpIcon));

        for (Unit unit : Unit.values()) unitLabels.get(unit).setVisible(false);

        integerDigitIcons = new ArrayList<>(MAX_INTEGER_DIGITS);
        for (int i = 0; i < MAX_INTEGER_DIGITS; i++) {
            integerDigitIcons.add(i, new JLabel(integerDigits[i]));
            meterPane.add(integerDigitIcons.get(i));
        }

        meterPane.add(decimal);

        decimalDigitIcons = new ArrayList<>(MAX_DECIMAL_DIGITS);
        for (int i = 0; i < MAX_DECIMAL_DIGITS; i++) {
            decimalDigitIcons.add(i, new JLabel(decimalDigits[i]));
            meterPane.add(decimalDigitIcons.get(i));
        }

        for (JLabel label : unitLabels.values()) meterPane.add(label);

        iconHeight = integerDigits[0].getIconHeight();
        digitIconWidth = integerDigits[0].getIconWidth();
        decimalIconWidth = decimalIcon.getIconWidth();
        unitIconWidth = milliVoltIcon.getIconWidth();

        // Initially we want to scale the icons to fit the previously saved window size
        scaleImage();

        if (meter != null) {
            meter.enable();
        }

        updateMenuUnits();
        initSettingsMenu();

        // Request callback to update time
        propertyChangeListener = (java.beans.PropertyChangeEvent e) -> update();
        if (meter != null) {
            meter.addPropertyChangeListener(NamedBean.PROPERTY_STATE, propertyChangeListener);
        }

        // Add component listener to handle frame resizing event
        this.addComponentListener(
                new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                scaleImage();
            }
        });

        pane1.add(meterPane);
        getContentPane().add(pane1);

        getContentPane().setPreferredSize(meterPane.getPreferredSize());

        pack();

        frameIsInitialized = true;
    }

    /* Set default Units, Digits and Decimals for Settings menu
     *   based on initial/selected meter configuration.                */
    private void initSettingsMenu() {
        
        boolean isPercent = (meter != null) && (meter.getUnit() == Meter.Unit.Percent);
        boolean isVoltage = (meter != null) && (meter instanceof VoltageMeter) && !isPercent;
        boolean isCurrent = (meter != null) && (meter instanceof CurrentMeter) && !isPercent;

        units_MenuItemMap.get(selectedUnit).setSelected(false);
        unitLabels.get(selectedUnit).setVisible(false);

        if (isPercent) selectedUnit = Unit.Percent;
        else if (isVoltage && (meter.getUnit() == Meter.Unit.Milli)) selectedUnit = Unit.MilliVolt;
        else if (isVoltage) selectedUnit = Unit.Volt;
        else if (isCurrent && (meter.getUnit() == Meter.Unit.Milli)) selectedUnit = Unit.MilliAmpere;
        else selectedUnit = Unit.Ampere;

        units_MenuItemMap.get(selectedUnit).setSelected(true);
        unitLabels.get(selectedUnit).setVisible(true);
        log.debug("selectedUnit set to '{}' for '{}'", selectedUnit, uuid);               
        update();

        if (meter == null) return; // skip if meter not set
        
        double max = meter.getMax();
        int iDigits = (int) (Math.log10(max) + 1);
        log.debug("integer digits set to {} for max={} for '{}'", iDigits, max, uuid);               
        setNumIntegerDigits(iDigits);
        
        double res = meter.getResolution();
        int dDigits = 0; //assume no decimals
        if (res % 1 != 0) { //not a whole number
          dDigits = new java.math.BigDecimal(String.valueOf(res)).scale(); // get decimal places used by resolution
        }
        log.debug("decimal digits set to {} for resolution={} for '{}'", dDigits, res, uuid);               
        setNumDecimalDigits(dDigits);
    }

    // Scale the clock digit images to fit the size of the display window.
    synchronized public void scaleImage() {

        int frameHeight = this.getContentPane().getHeight()
                - meterPane.getInsets().top - meterPane.getInsets().bottom;
        int frameWidth = this.getContentPane().getWidth()
                - meterPane.getInsets().left - meterPane.getInsets().right;

        double hscale = ((double)frameHeight)/((double)iconHeight);
        double wscale = ((double)frameWidth)/((double)widthOfAllIconsToDisplay);
        double scale = Math.min(hscale, wscale);

        for (int i = 0; i < 10; i++) {
            integerDigits[i].scale(scale,this);
        }
        for (int i = 0; i < 10; i++) {
            decimalDigits[i].scale(scale,this);
        }
        decimalIcon.scale(scale,this);
        microVoltIcon.scale(scale,this);
        milliVoltIcon.scale(scale,this);
        voltIcon.scale(scale,this);
        kiloVoltIcon.scale(scale,this);
        microAmpIcon.scale(scale,this);
        milliAmpIcon.scale(scale,this);
        ampIcon.scale(scale, this);
        kiloAmpIcon.scale(scale,this);
        percentIcon.scale(scale, this);
        errorIcon.scale(scale, this);

        meterPane.revalidate();
        this.getContentPane().revalidate();
    }

    private void updateMenuUnits() {
        boolean isPercent = (meter != null) && (meter.getUnit() == Meter.Unit.Percent);
        boolean isVoltage = (meter != null) && (meter instanceof VoltageMeter) && !isPercent;
        boolean isCurrent = (meter != null) && (meter instanceof CurrentMeter) && !isPercent;

        units_MenuItemMap.get(Unit.Percent).setVisible(isPercent);

        units_MenuItemMap.get(Unit.MicroVolt).setVisible(isVoltage);
        units_MenuItemMap.get(Unit.MilliVolt).setVisible(isVoltage);
        units_MenuItemMap.get(Unit.Volt).setVisible(isVoltage);
        units_MenuItemMap.get(Unit.KiloVolt).setVisible(isVoltage);

        units_MenuItemMap.get(Unit.MicroAmpere).setVisible(isCurrent);
        units_MenuItemMap.get(Unit.MilliAmpere).setVisible(isCurrent);
        units_MenuItemMap.get(Unit.Ampere).setVisible(isCurrent);
        units_MenuItemMap.get(Unit.KiloAmpere).setVisible(isCurrent);
    }

    private void showError() {
        for (int i=0; i < MAX_INTEGER_DIGITS; i++) {
            JLabel label = integerDigitIcons.get(i);
            if (i < numIntegerDigits) {
                label.setIcon(errorIcon);
                label.setVisible(true);
            } else {
                label.setVisible(false);
            }
        }

        decimal.setVisible(numDecimalDigits > 0);

        for (int i=0; i < MAX_DECIMAL_DIGITS; i++) {
            JLabel label = decimalDigitIcons.get(i);
            if (i < numDecimalDigits) {
                label.setIcon(errorIcon);
                label.setVisible(true);
            } else {
                label.setVisible(false);
            }
        }

        // Add width of integer digits
        widthOfAllIconsToDisplay = digitIconWidth * numIntegerDigits;

        // Add decimal point
        if (numDecimalDigits > 0) widthOfAllIconsToDisplay += decimalIconWidth;

        // Add width of decimal digits
        widthOfAllIconsToDisplay += digitIconWidth * numDecimalDigits;

        // Add one for the unit icon
        widthOfAllIconsToDisplay += unitIconWidth;

        if (widthOfAllIconsToDisplay != oldWidthOfAllIconsToDisplay){
            // clear the content pane and rebuild it.
            scaleImage();
            oldWidthOfAllIconsToDisplay = widthOfAllIconsToDisplay;
        }
    }

    /**
     * Update the displayed value.
     *
     * Assumes an integer value has an extra, non-displayed decimal digit.
     */
    synchronized void update() {
        if (meter == null) {
            showError();
            return;
        }

        double meterValue = meter.getKnownAnalogValue() * selectedUnit.multiply;

        switch (meter.getUnit()) {
            case Kilo:
                meterValue *= 1000.0;
                break;

            case Milli:
                meterValue /= 1000.0;
                break;

            case Micro:
                meterValue /= 1000_000.0;
                break;

            case NoPrefix:
            case Percent:
            default:
                // Do nothing
        }

        // We want at least one decimal digit so we cut the last digit later.
        // The problem is that the format string %05.0f will not add the dot
        // and we always want the dot to be able to split the string at the dot.
        int numChars = numIntegerDigits + numDecimalDigits + 2;
        String formatStr = String.format("%%0%d.%df", numChars, numDecimalDigits+1);
        String valueStr = String.format(formatStr, meterValue);
        String rgx = "\\.";
        if (!decimalDot) {
            rgx = ","; // decimal separator is "," in some locales
        }
        String[] valueParts = valueStr.split(rgx);
        if (valueParts.length < 2) {
            log.warn("cannot parse meter value using locale decimal separator");
            return;
        }
        // Show error if we don't have enough integer digits to show the result
        if (valueParts[0].length() > MAX_INTEGER_DIGITS) {
            showError();
            return;
        }

        for (int i = 0; i < MAX_INTEGER_DIGITS; i++) {
            JLabel label = integerDigitIcons.get(i);
            if (i < valueParts[0].length()) {
                label.setIcon(integerDigits[valueParts[0].charAt(i)-'0']);
                label.setVisible(true);
            } else {
                label.setVisible(false);
            }
        }

        decimal.setVisible(numDecimalDigits > 0);

        for (int i = 0; i < MAX_DECIMAL_DIGITS; i++) {
            JLabel label = decimalDigitIcons.get(i);
            if (i < valueParts[1].length()-1) {     // the decimal part has one digit too much
                label.setIcon(integerDigits[valueParts[1].charAt(i)-'0']);
                label.setVisible(true);
            } else {
                label.setVisible(false);
            }
        }


        // Add width of integer digits
        widthOfAllIconsToDisplay = digitIconWidth * valueParts[0].length();

        // Add width of decimal point (or other delimiter as set in system locale?)
        if (numDecimalDigits > 0) widthOfAllIconsToDisplay += decimalIconWidth;

        // Add width of decimal digits
        widthOfAllIconsToDisplay += digitIconWidth * (valueParts[1].length()-1);

        // Add one for the unit icon
        widthOfAllIconsToDisplay += unitIconWidth;

        if (widthOfAllIconsToDisplay != oldWidthOfAllIconsToDisplay){
            // clear the content pane and rebuild it.
            scaleImage();
            oldWidthOfAllIconsToDisplay = widthOfAllIconsToDisplay;
        }
    }

    @Override
    public void dispose() {
        if (meter != null) {
            meter.disable();
            meter.removePropertyChangeListener(propertyChangeListener);
        }
        MeterFrameManager.getInstance().deregister(this);
        super.dispose();
    }

    /**
     * Get the number of integer digits.
     *
     * @return the number of integer digits
     */
    public int getNumIntegerDigits() {
        return numIntegerDigits;
    }

    /**
     * Set the number of integer digits.
     *
     * @param digits the number of integer digits
     */
    public void setNumIntegerDigits(int digits) {
        integerDigits_MenuItemMap.get(numIntegerDigits).setSelected(false);
        integerDigits_MenuItemMap.get(digits).setSelected(true);
        numIntegerDigits = digits;
        update();
    }

    /**
     * Get the number of decimal digits.
     *
     * @return the number of decimal digits
     */
    public int getNumDecimalDigits() {
        return numDecimalDigits;
    }

    /**
     * Set the number of decimal digits.
     *
     * @param digits the number of decimal digits
     */
    public void setNumDecimalDigits(int digits) {
        decimalDigits_MenuItemMap.get(numDecimalDigits).setSelected(false);
        decimalDigits_MenuItemMap.get(digits).setSelected(true);
        numDecimalDigits = digits;
        update();
    }

    /**
     * Get the unit.
     *
     * @return the unit
     */
    public Unit getUnit() {
        return selectedUnit;
    }

    /**
     * Set the unit.
     *
     * @param unit the unit
     */
    public void setUnit(Unit unit) {
        units_MenuItemMap.get(selectedUnit).setSelected(false);
        unitLabels.get(selectedUnit).setVisible(false);
        units_MenuItemMap.get(unit).setSelected(true);
        unitLabels.get(unit).setVisible(true);
        selectedUnit = unit;
        update();
    }

    public String getInitialMeterName() {
        return initialMeterName;
    }

    public void setInitialMeterName(String initialMeterName) {
        this.initialMeterName = initialMeterName;
    }

    /**
     * Update the list of available meters.
     */
    private void addAllMeters() {
        MeterManager mm = InstanceManager.getNullableDefault(MeterManager.class);
        if (mm == null) {
            return;
        }
        if (log.isTraceEnabled()) { 
            log.trace("attempting to add all meters.  There are {} meters to add.",
                    mm.getNamedBeanSet().size());
        }
        mm.getNamedBeanSet().forEach((m) -> {
            String n = m.getDisplayName();
            if (m instanceof VoltageMeter) {
                if (voltageMeters.contains(m)) {
                    log.trace("voltage meter '{}' is already present", n);
                } else {
                    voltageMeters.add(m);
                    log.trace("Added voltage meter '{}'", n);
                }
            } else if (m instanceof CurrentMeter) {
                if (currentMeters.contains(m)) {
                    log.trace("current meter '{}' is already present", n);
                } else {
                    currentMeters.add(m);
                    log.trace("Added current meter '{}'", n);
                }
            }
        });

        if ((menuBar != null) && (voltageMetersMenu != null)) {
            updateMetersMenu(voltageMetersMenu, voltageMeters);
        }

        if ((menuBar != null) && (currentMetersMenu != null)) {
            updateMetersMenu(currentMetersMenu, currentMeters);
        }
    }

    /**
     * Update specified menu with specified list of meters
     *
     * @param menu - Menu to be updated
     * @param meters - list of Meters
     * 
     */
    private void updateMetersMenu(JMenu menu, List<Meter> meters) {
        for (Meter meter : meters) {            
            String n = meter.getDisplayName();
            log.trace("need to add a new checkbox for meter '{}'?", n);
            boolean found = false;

            if (menu.getItemCount() > 0) {
                for (int i =0; (i < menu.getItemCount()) && (!found);++i) {
                    JMenuItem jim = menu.getItem(i);
                    if (jim instanceof JCheckBoxMenuItem) {
                        if (jim.getText().compareTo(meter.getDisplayName()) == 0 ) {
                            log.trace("item '{}' is already in menu", n);
                            found = true;
                        } else {
                            log.trace("item '{}' is not already in menu", n);
                        }
                    }
                }
            }
            if (!found) {
                log.trace("Adding item '{}' to menu for frame {}", n, uuid);
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(new SelectMeterAction(n, meter));
                menu.add(item);
                meter_MenuItemMap.put(meter, item);
                //if this new meter was selected when panel stored, activate it
                if (getInitialMeterName().equals(n)) {
                    setMeter(meter);
                }
            }
        }
        // if more menu options than meters, remove any "NoMeters" menu item.
        if (menu.getItemCount() > meters.size()) {
            for (int i =0; (i < menu.getItemCount());++i) {
                JMenuItem jim = menu.getItem(i);
                if (jim.getText().compareTo(Bundle.getMessage("NoMeters")) == 0 ) {
                    menu.remove(jim);
                    log.trace("item '{}' removed from this menu for frame {}", jim.getText(), uuid);
                    break;
                }                
            }
        }    
    }

    /**
     * Update the menu items in the voltage meters and current meters menus.
     */
    private void updateCheckboxList() {
        log.trace("Updating the checkbox lists of meters.");
        addAllMeters();
        currentMetersMenu.repaint();
        voltageMetersMenu.repaint();
    }

    /**
     * Provide hooks so that menus of available meters may be updated "on-the-fly"
     * as new meters are created and/or old meters are disposed of.
     */
    private static class BeanListListener implements jmri.Manager.ManagerDataListener<Meter> {

        private BeanListListener(MeterFrame mf) {
            this.mf = mf;
        }
        MeterFrame mf;

        @Override
        public void contentsChanged(Manager.ManagerDataEvent<Meter> e) {
            log.warn("contents of the bean list changed.");
            mf.updateCheckboxList();
        }

        @Override
        public void intervalRemoved(Manager.ManagerDataEvent<Meter> e) {
            mf.updateCheckboxList();
        }

        @Override
        public void intervalAdded(Manager.ManagerDataEvent<Meter> e) {
            mf.updateCheckboxList();
        }
    }

    /**
     * Mechanism for acting upon selection of a meter from one of the menu items.
     */
    public class SelectMeterAction extends AbstractAction {

        private final Meter m;

        public SelectMeterAction(String actionName, Meter meter) {
            super(actionName);
            this.m = meter;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setMeter(m);

            JMenuItem selectedItem = (JMenuItem) e.getSource();
            selectedItem.setSelected(true);
            lastSelectedMeterMenuItem = selectedItem;
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MeterFrame.class);

}
