package jmri.jmrit.swing.meter;

import java.awt.event.*;
import java.beans.PropertyChangeListener;
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
 * @author Ken Cameron        Copyright (C) 2007
 * @author Mark Underwood     Copyright (C) 2007
 * @author Andrew Crosland    Copyright (C) 2020
 * @author Daniel Bergqvist   Copyright (C) 2020
 *
 * Adapted from ampmeter to display voltage and current.
 */
public class MeterFrame extends JmriJFrame {

    private enum Unit {
        PERCENT(1.0),    // Not a unit, but here anyway
        MICRO_VOLT(1000*1000),
        MILLI_VOLT(1000),
        VOLT(1.0),
        KILO_VOLT(1/1000.0),
        MICRO_AMPERE(1000*1000),
        MILLI_AMPERE(1000),
        AMPERE(1.0),
        KILO_AMPERE(1/1000.0);
        
        private final double multiply;
        
        private Unit(double m) { multiply = m; }
    };
    
    private final int MAX_INTEGER_DIGITS = 7;
    private final int MAX_DECIMAL_DIGITS = 2;
    
    private final UUID uuid;
    
    private final List<Meter> voltageMeters = new ArrayList<>();
    private final List<Meter> currentMeters = new ArrayList<>();
    
    // GUI member declarations
    private JMenuBar menuBar;
    ArrayList<JLabel> integerDigitIcons;
    ArrayList<JLabel> decimalDigitIcons;
    JLabel decimal;
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
    Unit selectedUnit = Unit.VOLT;
    
    int digitIconWidth;
    int decimalIconWidth;
    int unitIconWidth;
    int iconHeight;

    private PropertyChangeListener propertyChangeListener;

    private Meter meter;

    NamedIcon integerDigits[] = new NamedIcon[10];
    NamedIcon decimalDigits[] = new NamedIcon[10];
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

    JPanel pane1;
    JPanel meterPane;
    
    public MeterFrame() {
        this(UUID.randomUUID());
    }
    
    public MeterFrame(UUID uuid) {
        super(Bundle.getMessage("TrackVoltageMeterTitle"));
        
        this.uuid = uuid;
        
        MeterManager mm = InstanceManager.getNullableDefault(MeterManager.class);
        if (mm == null) throw new RuntimeException("No meter manager exists");
        
        for (Meter m : mm.getNamedBeanSet()) {
            if ((m != null) && (m instanceof VoltageMeter)) voltageMeters.add(m);
            if ((m != null) && (m instanceof CurrentMeter)) currentMeters.add(m);
        }
        
        if (!voltageMeters.isEmpty()) {
            setMeter(voltageMeters.get(0));
        } else if (!currentMeters.isEmpty()) {
            setMeter(currentMeters.get(0));
        } else {
            setTitle(Bundle.getMessage("TrackVoltageMeterTitle"));
        }
        
        MeterFrameManager.getInstance().register(this);
    }
    
    /**
     * Get the UUID of this frame.
     * <P>
     * The UUID is used if two different panel files are loaded with the same
     * meter frame.
     * 
     * @return the UUID of this frame
     */
    public UUID getUUID() {
        return uuid;
    }
    
    /**
     * Get the meter that is displayed
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
            initSelectedUnit();
        }

        if (meter instanceof VoltageMeter) {
            setTitle(Bundle.getMessage("TrackVoltageMeterTitle2", m.getDisplayName()));
        } else {
            setTitle(Bundle.getMessage("TrackCurrentMeterTitle2", m.getDisplayName()));
        }
    }
    
    @Override
    public void initComponents() {
        // Create menu bar
        menuBar = new JMenuBar();
        JMenu voltageMetersMenu = new JMenu(Bundle.getMessage("MenuVoltageMeters"));
        menuBar.add(voltageMetersMenu);
        for (Meter m : voltageMeters) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(new SelectMeterAction(m.getDisplayName(), m));
            voltageMetersMenu.add(item);
            meter_MenuItemMap.put(m, item);
        }
        
        JMenu currentMetersMenu = new JMenu(Bundle.getMessage("MenuCurrentMeters"));
        menuBar.add(currentMetersMenu);
        for (Meter m : currentMeters) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(new SelectMeterAction(m.getDisplayName(), m));
            currentMetersMenu.add(item);
            meter_MenuItemMap.put(m, item);
        }
        
        JMenu settingsMenu = new JMenu(Bundle.getMessage("MenuMeterSettings"));
        menuBar.add(settingsMenu);
        
        for (Unit unit : Unit.values()) {
            final Unit u = unit;
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("MenuMeter_"+unit.name())){
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
            settingsMenu.add(item);
        }
        
        settingsMenu.addSeparator();
        
        for (int i=1; i <= 7; i++) {
            final int ii = i;
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("MenuMeterIntegerDigits", i)){
                @Override
                public void actionPerformed(ActionEvent e) {
                    integerDigits_MenuItemMap.get(numIntegerDigits).setSelected(false);
                    numIntegerDigits = ii;
                    update();
                }
            });
            integerDigits_MenuItemMap.put(ii, item);
            settingsMenu.add(item);
            if (ii == numIntegerDigits) item.setSelected(true);
        }
        settingsMenu.addSeparator();
        for (int i=0; i <= 2; i++) {
            final int ii = i;
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("MenuMeterDecimalDigits", i)){
                @Override
                public void actionPerformed(ActionEvent e) {
                    decimalDigits_MenuItemMap.get(numDecimalDigits).setSelected(false);
                    decimalDigits_MenuItemMap.get(ii).setSelected(true);
                    numDecimalDigits = ii;
                    update();
                }
            });
            decimalDigits_MenuItemMap.put(ii, item);
            settingsMenu.add(item);
            if (ii == numDecimalDigits) item.setSelected(true);
        }
        
        setJMenuBar(menuBar);

        // clear the contents
        getContentPane().removeAll();

        pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));
        
        meterPane = new JPanel();
        meterPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder()));

        // build the actual multimeter display.
        meterPane.setLayout(new BoxLayout(meterPane, BoxLayout.X_AXIS));

        //Load the images (these are now the larger version of the original gifs
        for (int i = 0; i < 10; i++) {
            integerDigits[i] = new NamedIcon("resources/icons/misc/LCD/Lcd_" + i + "b.GIF", "resources/icons/misc/LCD/Lcd_" + i + "b.GIF");
        }
        for (int i = 0; i < 10; i++) {
            decimalDigits[i] = new NamedIcon("resources/icons/misc/LCD/Lcd_" + i + "b.GIF", "resources/icons/misc/LCD/Lcd_" + i + "b.GIF");
        }
        decimalIcon = new NamedIcon("resources/icons/misc/LCD/decimalb.gif", "resources/icons/misc/LCD/decimalb.gif");
        microVoltIcon = new NamedIcon("resources/icons/misc/LCD/uvoltb.gif", "resources/icons/misc/LCD/uvoltb.gif");
        milliVoltIcon = new NamedIcon("resources/icons/misc/LCD/mvoltb.gif", "resources/icons/misc/LCD/mvoltb.gif");
        voltIcon = new NamedIcon("resources/icons/misc/LCD/voltb.gif", "resources/icons/misc/LCD/voltb.gif");
        kiloVoltIcon = new NamedIcon("resources/icons/misc/LCD/kvoltb.gif", "resources/icons/misc/LCD/kvoltb.gif");
        microAmpIcon = new NamedIcon("resources/icons/misc/LCD/uampb.gif", "resources/icons/misc/LCD/uampb.gif");
        milliAmpIcon = new NamedIcon("resources/icons/misc/LCD/mampb.gif", "resources/icons/misc/LCD/mampb.gif");
        ampIcon = new NamedIcon("resources/icons/misc/LCD/ampb.gif", "resources/icons/misc/LCD/ampb.gif");
        kiloAmpIcon = new NamedIcon("resources/icons/misc/LCD/kampb.gif", "resources/icons/misc/LCD/kampb.gif");
        percentIcon = new NamedIcon("resources/icons/misc/LCD/percentb.gif", "resources/icons/misc/LCD/percentb.gif");
        
        decimal = new JLabel(decimalIcon);
        unitLabels.put(Unit.PERCENT, new JLabel(percentIcon));
        unitLabels.put(Unit.MICRO_VOLT, new JLabel(microVoltIcon));
        unitLabels.put(Unit.MILLI_VOLT, new JLabel(milliVoltIcon));
        unitLabels.put(Unit.VOLT, new JLabel(voltIcon));
        unitLabels.put(Unit.KILO_VOLT, new JLabel(kiloVoltIcon));
        unitLabels.put(Unit.MICRO_AMPERE, new JLabel(microAmpIcon));
        unitLabels.put(Unit.MILLI_AMPERE, new JLabel(milliAmpIcon));
        unitLabels.put(Unit.AMPERE, new JLabel(ampIcon));
        unitLabels.put(Unit.KILO_AMPERE, new JLabel(kiloAmpIcon));
        
        for (Unit unit : Unit.values()) unitLabels.get(unit).setVisible(false);
        
        integerDigitIcons = new ArrayList<>(MAX_INTEGER_DIGITS);
        for(int i = 0; i < MAX_INTEGER_DIGITS; i++) {
            integerDigitIcons.add(i,new JLabel(integerDigits[i]));
            meterPane.add(integerDigitIcons.get(i));
        }
        
        meterPane.add(decimal);
        
        decimalDigitIcons = new ArrayList<>(MAX_DECIMAL_DIGITS);
        for(int i = 0; i < MAX_DECIMAL_DIGITS; i++) {
            decimalDigitIcons.add(i,new JLabel(decimalDigits[i]));
            meterPane.add(decimalDigitIcons.get(i));
        }
        
        for (JLabel label : unitLabels.values()) meterPane.add(label);
        
        iconHeight = integerDigits[0].getIconHeight();
        digitIconWidth = integerDigits[0].getIconWidth();
        decimalIconWidth = decimalIcon.getIconWidth();
        unitIconWidth = milliVoltIcon.getIconWidth();
        
        // Initially we want to scale the icons to fit the previously saved window size
        scaleImage();
        
        meter.enable();

        updateMenuUnits();
        initSelectedUnit();

        // Request callback to update time
        propertyChangeListener = (java.beans.PropertyChangeEvent e) -> {
            update();
        };
        meter.addPropertyChangeListener(NamedBean.PROPERTY_STATE, propertyChangeListener);

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

    private void initSelectedUnit() {
        boolean isPercent = (meter != null) && (meter.getUnit() == Meter.Unit.Percent);
        boolean isVoltage = (meter != null) && (meter instanceof VoltageMeter) && !isPercent;
        boolean isCurrent = (meter != null) && (meter instanceof CurrentMeter) && !isPercent;
        
        units_MenuItemMap.get(selectedUnit).setSelected(false);
        unitLabels.get(selectedUnit).setVisible(false);
        
        if (isPercent) selectedUnit = Unit.PERCENT;
        else if (isVoltage && (meter.getUnit() == Meter.Unit.Milli)) selectedUnit = Unit.MILLI_VOLT;
        else if (isVoltage) selectedUnit = Unit.VOLT;
        else if (isCurrent && (meter.getUnit() == Meter.Unit.Milli)) selectedUnit = Unit.MILLI_AMPERE;
        else selectedUnit = Unit.AMPERE;
        
        units_MenuItemMap.get(selectedUnit).setSelected(true);
        unitLabels.get(selectedUnit).setVisible(true);
        update();
    }

    // Added method to scale the clock digit images to fit the
    // size of the display window
    synchronized public void scaleImage() {
        
        int frameHeight = this.getContentPane().getHeight()
                - meterPane.getInsets().top - meterPane.getInsets().bottom;
        int frameWidth = this.getContentPane().getWidth()
                - meterPane.getInsets().left - meterPane.getInsets().right;
        
        double hscale = ((double)frameHeight)/((double)iconHeight);
        double wscale = ((double)frameWidth)/((double)widthOfAllIconsToDisplay);
        double scale = hscale < wscale? hscale:wscale;

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

        meterPane.revalidate();
        this.getContentPane().revalidate();
    }

    private void updateMenuUnits() {
        boolean isPercent = (meter != null) && (meter.getUnit() == Meter.Unit.Percent);
        boolean isVoltage = (meter != null) && (meter instanceof VoltageMeter) && !isPercent;
        boolean isCurrent = (meter != null) && (meter instanceof CurrentMeter) && !isPercent;
        
        units_MenuItemMap.get(Unit.PERCENT).setVisible(isPercent);
        
        units_MenuItemMap.get(Unit.MICRO_VOLT).setVisible(isVoltage);
        units_MenuItemMap.get(Unit.MILLI_VOLT).setVisible(isVoltage);
        units_MenuItemMap.get(Unit.VOLT).setVisible(isVoltage);
        units_MenuItemMap.get(Unit.KILO_VOLT).setVisible(isVoltage);
        
        units_MenuItemMap.get(Unit.MICRO_AMPERE).setVisible(isCurrent);
        units_MenuItemMap.get(Unit.MILLI_AMPERE).setVisible(isCurrent);
        units_MenuItemMap.get(Unit.AMPERE).setVisible(isCurrent);
        units_MenuItemMap.get(Unit.KILO_AMPERE).setVisible(isCurrent);
    }
    
    /**
     * Update the displayed value.
     * 
     * Assumes an integer value has an extra, non-displayed decimal digit.
     */
    synchronized void update() {
        Meter.Unit meterUnit = (meter != null) ? meter.getUnit() : Meter.Unit.NoPrefix;
        
        double meterValue = meter.getKnownAnalogValue() * selectedUnit.multiply;
        
        if (meterUnit == Meter.Unit.Milli) meterValue /= 1000.0;
        
        // We want at least one decimal digit so we cut the last digit later.
        // The problem is that the format string %05.0f will not add the dot
        // and we always want the dot to be able to split the string by the dot.
        int numChars = numIntegerDigits + numDecimalDigits + 2;
        String formatStr = String.format("%%0%d.%df", numChars, numDecimalDigits+1);
        String valueStr = String.format(formatStr, meterValue);
        
        String[] valueParts = valueStr.split("\\.");
        
        for (int i=0; i < MAX_INTEGER_DIGITS; i++) {
            JLabel label = integerDigitIcons.get(i);
            if (i < valueParts[0].length()) {
                label.setIcon(integerDigits[valueParts[0].charAt(i)-'0']);
                label.setVisible(true);
            } else {
                label.setVisible(false);
            }
        }
        
        decimal.setVisible(numDecimalDigits > 0);
        
        for (int i=0; i < MAX_DECIMAL_DIGITS; i++) {
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
        
        // Add decimal point
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
