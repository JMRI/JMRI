package jmri.jmrit.simpleclock;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.CheckForNull;
import javax.swing.*;
import jmri.InstanceManager;
import jmri.Timebase;
import jmri.TimebaseRateException;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user configuration of Simple Timebase.
 * <p>
 * The current implementation (2007) handles the internal clock and one hardware
 * clock.
 *
 * @author Dave Duchamp Copyright (C) 2004, 2007
 */
public class SimpleClockFrame extends JmriJFrame implements PropertyChangeListener {

    private Timebase clock;
    private String hardwareName = null;
    private boolean changed = false;
    protected boolean showTime = false;
    private final DecimalFormat threeDigits = new DecimalFormat("0.000"); // 3 digit precision for speedup factor

    protected JComboBox<String> timeSourceBox = null;
    protected JComboBox<String> clockStartBox = null;
    protected JComboBox<String> startRunBox = null;
    // These are the indexes into the start run box.
    private final static int START_RUNNING = 0;
    private final static int START_STOPPED = 1;
    private final static int START_NORUNCHANGE = 2;

    protected JCheckBox synchronizeCheckBox = null;
    protected JCheckBox correctCheckBox = null;
    protected JCheckBox displayCheckBox = null;
    protected JCheckBox startSetTimeCheckBox = null;
    protected JCheckBox startSetRateCheckBox = null;
    protected JCheckBox displayStartStopButton = null;

    protected JTextField factorField = new JTextField(5);
    protected JTextField startFactorField = new JTextField(5);
    protected JTextField hoursField = new JTextField(2);
    protected JTextField minutesField = new JTextField(2);
    protected JTextField startHoursField = new JTextField(2);
    protected JTextField startMinutesField = new JTextField(2);

    protected JButton setRateButton = new JButton(Bundle.getMessage("ButtonSet"));
    protected JButton setTimeButton = new JButton(Bundle.getMessage("ButtonSet"));
    protected JButton startButton = new JButton(Bundle.getMessage("ButtonStart"));
    protected JButton stopButton = new JButton(Bundle.getMessage("ButtonStop"));
    protected JButton applyCloseButton = new JButton(Bundle.getMessage("ButtonStoreClock"));

    protected JLabel clockStatus = new JLabel();
    protected JLabel timeLabel = new JLabel();

    private final int internalSourceIndex = 0;
    private final int hardwareSourceIndex = 1;

    private final int startNone = 0;
    private final int startNixieClock = 1;
    private final int startAnalogClock = 2;
    private final int startLcdClock = 3;
    private final int startPragotronClock = 4 ;

    /**
     * Constructor method.
     */
    public SimpleClockFrame() {
        super();
    }

    /**
     * Initialize the Clock config window.
     */
    @Override
    public void initComponents() {
        setTitle(Bundle.getMessage("SimpleClockWindowTitle"));

        // Determine current state of the clock
        clock = InstanceManager.getNullableDefault(jmri.Timebase.class);
        if (clock == null) {
            // could not initialize clock
            log.error("Could not obtain a Timebase instance.");
            setVisible(false);
            dispose();
        }
        if (!clock.getIsInitialized()) {
            // if clocks have not been initialized at start up, do so now
            clock.initializeHardwareClock();
        }

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        contentPane.add(getClockStatePanel());

        JPanel saveContainerPanel = new JPanel();
        saveContainerPanel.setBorder( BorderFactory.createRaisedBevelBorder() );
        saveContainerPanel.setLayout(new BoxLayout(saveContainerPanel, BoxLayout.Y_AXIS));

        saveContainerPanel.add(getSourcePane());
        saveContainerPanel.add(getStartupOptionsPane());

        // add save/close buttons
        JPanel panel4 = new JPanel();
        panel4.setLayout(new BoxLayout(panel4, BoxLayout.X_AXIS));
        panel4.add(applyCloseButton);
        applyCloseButton.addActionListener(this::saveButtonActionPerformed);
        saveContainerPanel.add(panel4);


        contentPane.add(saveContainerPanel);

        // update contents for current status
        updateRunningButton();

        // add save menu item
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.configurexml.StoreMenu());

        setJMenuBar(menuBar);
        // add help menu to window
        addHelpMenu("package.jmri.jmrit.simpleclock.SimpleClockFrame", true);

        // pack for display
        pack();

        // listen for changes to the timebase parameters
        clock.addPropertyChangeListener(this);
    }

    private JPanel getClockStatePanel() {

            // Set up clock information panel
        JPanel clockStatePanel = new JPanel();
        clockStatePanel.setLayout(new BoxLayout(clockStatePanel, BoxLayout.Y_AXIS));
        clockStatePanel.setBorder(BorderFactory.createTitledBorder(
            Bundle.getMessage("BoxLabelClockState")));

        JPanel panel31 = new JPanel();
        panel31.add(clockStatus);

        JPanel panel32 = new JPanel();
        panel32.add(new JLabel(Bundle.getMessage("CurrentTime") + " "));
        setTimeLabel();
        panel32.add(timeLabel);
        clockStatePanel.add(panel32);

        // Set up Start and Stop buttons
        startButton.setToolTipText(Bundle.getMessage("TipStartButton"));
        startButton.addActionListener(this::startButtonActionPerformed);
        panel31.add(startButton);
        stopButton.setToolTipText(Bundle.getMessage("TipStopButton"));
        stopButton.addActionListener(this::stopButtonActionPerformed);
        panel31.add(stopButton);
        clockStatePanel.add(panel31);

        // Set up time setup information
        JPanel panel2 = new JPanel();
        panel2.add(new JLabel(Bundle.getMessage("NewTime") + " "));
        panel2.add(hoursField);
        hoursField.setText("00");
        hoursField.setToolTipText(Bundle.getMessage("TipHoursField"));
        panel2.add(new JLabel(":"));
        panel2.add(minutesField);
        minutesField.setText("00");
        minutesField.setToolTipText(Bundle.getMessage("TipMinutesField"));
        setTimeButton.setToolTipText(Bundle.getMessage("TipSetTimeButton"));
        setTimeButton.addActionListener(this::setTimeButtonActionPerformed);
        panel2.add(setTimeButton);
        clockStatePanel.add(panel2);

        // Set up speed up factor
        JPanel panel12 = new JPanel();
        panel12.add(new JLabel(Bundle.getMessage("SpeedUpFactor") + " "));
        panel12.add(factorField);
        factorField.setText(threeDigits.format(clock.userGetRate()));
        factorField.setToolTipText(Bundle.getMessage("TipFactorField"));
        panel12.add(new JLabel(":1 "));
        setRateButton.setToolTipText(Bundle.getMessage("TipSetRateButton"));
        setRateButton.addActionListener(this::setRateButtonActionPerformed);
        panel12.add(setRateButton);
        clockStatePanel.add(panel12);

        JPanel clockStatePanelContainer = new JPanel();
        clockStatePanelContainer.setBorder( BorderFactory.createRaisedBevelBorder() );
        clockStatePanelContainer.setLayout(new BoxLayout(clockStatePanelContainer, BoxLayout.X_AXIS));
        clockStatePanelContainer.add(clockStatePanel);
        return clockStatePanelContainer;

    }

    private JPanel getSourcePane(){

        JPanel sourcePanel = new JPanel();
        sourcePanel.setBorder( BorderFactory.createTitledBorder( Bundle.getMessage("TimeSource")));
        sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.Y_AXIS));

        // Set up time source choice
        JPanel panel11 = new JPanel();
        // panel11.add(new JLabel(Bundle.getMessage("TimeSource") + " "));
        timeSourceBox = new JComboBox<>();
        panel11.add(timeSourceBox);
        timeSourceBox.addItem(Bundle.getMessage("ComputerClock"));
        hardwareName = InstanceManager.getDefault(jmri.ClockControl.class).getHardwareClockName();
        if (hardwareName != null) {
            timeSourceBox.addItem(hardwareName);
        }
        timeSourceBox.setToolTipText(Bundle.getMessage("TipTimeSource"));
        timeSourceBox.addActionListener(this::setTimeSourceChanged);
        sourcePanel.add(panel11);

        if (hardwareName != null) {
            timeSourceBox.setSelectedIndex(clock.getInternalMaster() ? internalSourceIndex : hardwareSourceIndex);
            JPanel panel11x = new JPanel();
            synchronizeCheckBox = new JCheckBox(Bundle.getMessage("Synchronize") + " "
                    + hardwareName);
            synchronizeCheckBox.setToolTipText(Bundle.getMessage("TipSynchronize"));
            synchronizeCheckBox.setSelected(clock.getSynchronize());
            synchronizeCheckBox.addActionListener(this::synchronizeChanged);
            panel11x.add(synchronizeCheckBox);
            sourcePanel.add(panel11x);
            if (InstanceManager.getDefault(jmri.ClockControl.class).canCorrectHardwareClock()) {
                JPanel panel11y = new JPanel();
                correctCheckBox = new JCheckBox(Bundle.getMessage("Correct"));
                correctCheckBox.setToolTipText(Bundle.getMessage("TipCorrect"));
                correctCheckBox.setSelected(clock.getCorrectHardware());
                correctCheckBox.addActionListener(this::correctChanged);
                panel11y.add(correctCheckBox);
                sourcePanel.add(panel11y);
            }
            if (InstanceManager.getDefault(jmri.ClockControl.class).canSet12Or24HourClock()) {
                JPanel panel11z = new JPanel();
                displayCheckBox = new JCheckBox(Bundle.getMessage("Display12Hour"));
                displayCheckBox.setToolTipText(Bundle.getMessage("TipDisplay"));
                displayCheckBox.setSelected(clock.use12HourDisplay());
                displayCheckBox.addActionListener(this::displayChanged);
                panel11z.add(displayCheckBox);
                sourcePanel.add(panel11z);
            }
        }

        return sourcePanel;

    }

    private JPanel getStartupOptionsPane(){

        // Set up startup options panel
        JPanel startupOptionsPane = new JPanel();
        startupOptionsPane.setLayout(new BoxLayout(startupOptionsPane, BoxLayout.Y_AXIS));
        JPanel panel61 = new JPanel();
        panel61.add(new JLabel(Bundle.getMessage("StartBoxLabel") + " "));
        startRunBox = new JComboBox<>();
        startRunBox.addItem(Bundle.getMessage("StartSelectRunning"));
        startRunBox.addItem(Bundle.getMessage("StartSelectStopped"));
        startRunBox.addItem(Bundle.getMessage("StartSelectNoChange"));
        startRunBox.setToolTipText(Bundle.getMessage("TipStartRunSelect"));
        switch (clock.getClockInitialRunState()) {
            case DO_STOP:
                startRunBox.setSelectedIndex(START_STOPPED);
                break;
            case DO_START:
                startRunBox.setSelectedIndex(START_RUNNING);
                break;
            case DO_NOTHING:
                startRunBox.setSelectedIndex(START_NORUNCHANGE);
                break;
            default:
                jmri.util.LoggingUtil.warnOnce(log, "Unexpected initial run state = {}", clock.getClockInitialRunState());
                break;
        }
        startRunBox.addActionListener(this::startRunBoxChanged);
        panel61.add(startRunBox);
        startupOptionsPane.add(panel61);

        JPanel panel62 = new JPanel();
        startSetTimeCheckBox = new JCheckBox(Bundle.getMessage("StartSetTime"));
        startSetTimeCheckBox.setToolTipText(Bundle.getMessage("TipStartSetTime"));
        startSetTimeCheckBox.setSelected(clock.getStartSetTime());
        startSetTimeCheckBox.addActionListener(this::startSetTimeChanged);
        panel62.add(startSetTimeCheckBox);
        Calendar cal = Calendar.getInstance();
        cal.setTime(clock.getStartTime());
        startHoursField.setText("" + cal.get(Calendar.HOUR_OF_DAY));
        startHoursField.setToolTipText(Bundle.getMessage("TipStartHours"));
        panel62.add(startHoursField);
        panel62.add(new JLabel(":"));
        startMinutesField.setText("" + cal.get(Calendar.MINUTE));
        startMinutesField.setToolTipText(Bundle.getMessage("TipStartMinutes"));
        panel62.add(startMinutesField);

        startMinutesField.addFocusListener(getStartUpSetTimeChangedAdapter());
        startHoursField.addFocusListener(getStartUpSetTimeChangedAdapter());
        startupOptionsPane.add(panel62);

        JPanel panelStartSetRate = new JPanel();
        startSetRateCheckBox = new JCheckBox(Bundle.getMessage("StartSetSpeedUpFactor") + " ");
        startSetRateCheckBox.setToolTipText(Bundle.getMessage("TipStartSetRate"));
        startSetRateCheckBox.setSelected(clock.getSetRateAtStart());
        startSetRateCheckBox.addActionListener(this::startSetRateChanged);
        panelStartSetRate.add(startSetRateCheckBox);
        panelStartSetRate.add(startFactorField);
        startFactorField.setText(threeDigits.format(clock.getStartRate()));
        startFactorField.setToolTipText(Bundle.getMessage("TipFactorField"));
        startFactorField.addActionListener(this::startFactorFieldChanged);
        startFactorField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                if (!focusEvent.isTemporary()) {
                    startFactorFieldChanged(null);
                }
                super.focusLost(focusEvent);
            }
        });
        panelStartSetRate.add(new JLabel(":1 "));
        startupOptionsPane.add(panelStartSetRate);

        JPanel panel63 = new JPanel();
        panel63.add(new JLabel(Bundle.getMessage("StartClock") + " "));
        clockStartBox = new JComboBox<>();
        panel63.add(clockStartBox);
        clockStartBox.addItem(Bundle.getMessage("None"));
        clockStartBox.addItem(Bundle.getMessage("MenuItemNixieClock"));
        clockStartBox.addItem(Bundle.getMessage("MenuItemAnalogClock"));
        clockStartBox.addItem(Bundle.getMessage("MenuItemLcdClock"));
        clockStartBox.addItem(Bundle.getMessage("MenuItemPragotronClock"));
        clockStartBox.setSelectedIndex(startNone);
        if (clock.getStartClockOption() == Timebase.NIXIE_CLOCK) {
            clockStartBox.setSelectedIndex(startNixieClock);
        } else {
            if (clock.getStartClockOption() == Timebase.ANALOG_CLOCK) {
                clockStartBox.setSelectedIndex(startAnalogClock);
            } else {
                if (clock.getStartClockOption() == Timebase.LCD_CLOCK) {
                    clockStartBox.setSelectedIndex(startLcdClock);
                } else {
                    if (clock.getStartClockOption() == Timebase.PRAGOTRON_CLOCK) {
                        clockStartBox.setSelectedIndex(startPragotronClock);
                    }
                }
            }
        }
        clockStartBox.setToolTipText(Bundle.getMessage("TipClockStartOption"));
        clockStartBox.addActionListener(this::setClockStartChanged);
        startupOptionsPane.add(panel63);
        JPanel panel64 = new JPanel();
        displayStartStopButton= new JCheckBox(Bundle.getMessage("DisplayOnOff"));
        displayStartStopButton.setSelected(clock.getShowStopButton());
        displayStartStopButton.addActionListener(this::showStopButtonChanged);
        panel64.add(displayStartStopButton);
        startupOptionsPane.add(panel64);

        startupOptionsPane.setBorder(BorderFactory.createTitledBorder(
                Bundle.getMessage("BoxLabelStartUp")));

        return startupOptionsPane;

    }

    private FocusAdapter getStartUpSetTimeChangedAdapter(){
        return new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                if (!focusEvent.isTemporary()) {
                    startSetTimeChanged(null);
                }
                super.focusLost(focusEvent);
            }
        };
    }

    private void startFactorFieldChanged(ActionEvent e) {
        Double v = parseRate(startFactorField.getText());
        if (v != null && !v.equals(clock.getStartRate())) {
            clock.setStartRate(v);
            changed = true;
        }
        startFactorField.setText(threeDigits.format(clock.getStartRate()));
    }

    private void startSetRateChanged(ActionEvent e) {
        clock.setSetRateAtStart(startSetRateCheckBox.isSelected());
        changed = true;
    }

    /**
     * Adjust to running state changes
     */
    void updateRunningButton() {
        boolean running = clock.getRun();
        if (running) {
            clockStatus.setText(Bundle.getMessage("ClockRunning"));
            startButton.setVisible(false);
            stopButton.setVisible(true);
        } else {
            clockStatus.setText(Bundle.getMessage("ClockStopped"));
            startButton.setVisible(true);
            stopButton.setVisible(false);
        }
        clockStatus.setVisible(true);
    }

    /**
     * Converts a user-entered rate to a double, possibly throwing up warning dialogs.
     * @param fieldEntry value from text field where the user entered a rate.
     * @return null if the rate could not be parsed, negative, or an unsupported fraction.
     * Otherwise the fraction value.
     */
    @CheckForNull
    Double parseRate(String fieldEntry) {
        double rate;
        try {
            char decimalSeparator = threeDigits.getDecimalFormatSymbols().getDecimalSeparator() ;
            if (decimalSeparator != '.') {
                fieldEntry = fieldEntry.replace(decimalSeparator, '.') ;
            }
            rate = Double.parseDouble(fieldEntry);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, (Bundle.getMessage("ParseRateError") + "\n" + e),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing user-entered rate", e);
            return null;
        }
        if (rate < 0.0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("NegativeRateError"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (InstanceManager.getDefault(jmri.ClockControl.class).requiresIntegerRate() && !clock.getInternalMaster()) {
            double frac = rate - (int) rate;
            if (frac > 0.001) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("NonIntegerError"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        return rate;
    }

    /**
     * Handle Set Rate button.
     * @param ev unused
     */
    public void setRateButtonActionPerformed(ActionEvent ev) {
        Double parsedRate = parseRate(factorField.getText());
        if (parsedRate == null) {
            factorField.setText(threeDigits.format(clock.userGetRate()));
            return;
        }
        try {
            clock.userSetRate(parsedRate);
        } catch (TimebaseRateException e) {
            JOptionPane.showMessageDialog(this, (Bundle.getMessage("SetRateError") + "\n" + e),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.error("Exception when setting timebase rate", e);
        }
        changed = true;
    }

    /**
     * Handle time source change
     *
     * Only changes the time source if the rate is OK (typically: Integer) for new source
     */
    private void setTimeSourceChanged(ActionEvent e) {
        int index = timeSourceBox.getSelectedIndex();
        int oldIndex = internalSourceIndex;
        if (!clock.getInternalMaster()) {
            oldIndex = hardwareSourceIndex;
        }
        // return if nothing changed
        if (oldIndex == index) {
            return;
        }
        // change the time source master
        if (index == internalSourceIndex) {
            clock.setInternalMaster(true, true);
        } else {
            // only change if new source is okay with current rate
            if (InstanceManager.getDefault(jmri.ClockControl.class).requiresIntegerRate()) {
                double rate = clock.userGetRate();
                double frac = rate - (int) rate;
                if (frac > 0.001) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("NonIntegerErrorCantChangeSource"),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    timeSourceBox.setSelectedIndex(internalSourceIndex);
                    return;
                }
            }
            clock.setInternalMaster(false, true);
        }
        changed = true;
    }

    /**
     * Handle synchronize check box change
     */
    private void synchronizeChanged(ActionEvent e) {
        clock.setSynchronize(synchronizeCheckBox.isSelected(), true);
        changed = true;
    }

    /**
     * Handle correct check box change
     */
    private void correctChanged(ActionEvent e) {
        clock.setCorrectHardware(correctCheckBox.isSelected(), true);
        changed = true;
    }

    /**
     * Handle 12-hour display check box change
     */
    private void displayChanged(ActionEvent e) {
        clock.set12HourDisplay(displayCheckBox.isSelected(), true);
        changed = true;
    }

    /**
     * Handle Set Time button.
     * @param ex unused
     */
    public void setTimeButtonActionPerformed(ActionEvent ex) {
        int hours;
        int minutes;
        // get hours, reporting errors if any
        try {
            hours = Integer.parseInt(hoursField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, (Bundle.getMessage("HoursError") + "\n" + e),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing hours Field", e);
            return;
        }
        if ((hours < 0) || (hours > 23)) {
            JOptionPane.showMessageDialog(this, (Bundle.getMessage("HoursRangeError")),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        // get minutes, reporting errors if any
        try {
            minutes = Integer.parseInt(minutesField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, (Bundle.getMessage("MinutesError") + "\n" + e),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing Minutes Field", e);
            return;
        }
        if ((minutes < 0) || (minutes > 59)) {
            JOptionPane.showMessageDialog(this, (Bundle.getMessage("MinutesRangeError")),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        // set time of the fast clock
        long mSecPerHour = 3600000;
        long mSecPerMinute = 60000;
        Calendar cal = Calendar.getInstance();
        cal.setTime(clock.getTime());
        int cHours = cal.get(Calendar.HOUR_OF_DAY);
        long cNumMSec = cal.getTime().getTime();
        long nNumMSec = ((cNumMSec / mSecPerHour) * mSecPerHour) - (cHours * mSecPerHour)
                + (hours * mSecPerHour) + (minutes * mSecPerMinute);
        clock.userSetTime(new Date(nNumMSec));
        showTime = true;
        updateTime();
    }

    /**
     * Handle start run combo box change
     */
    private void startRunBoxChanged(ActionEvent e) {
        switch (startRunBox.getSelectedIndex()) {
            case START_STOPPED:
                clock.setClockInitialRunState(Timebase.ClockInitialRunState.DO_STOP);
                break;
            case START_RUNNING:
                clock.setClockInitialRunState(Timebase.ClockInitialRunState.DO_START);
                break;
            default:
            case START_NORUNCHANGE:
                clock.setClockInitialRunState(Timebase.ClockInitialRunState.DO_NOTHING);
                break;
        }
        changed = true;
    }

    /**
     * Handle Show on/off button check box change
     */
    private void showStopButtonChanged(ActionEvent e) {
        clock.setShowStopButton(displayStartStopButton.isSelected());
        changed = true;
    }

    /**
     * Handle start set time check box change
     */
    private void startSetTimeChanged(ActionEvent ev) {
        int hours;
        int minutes;
        // get hours, reporting errors if any
        try {
            hours = Integer.parseInt(startHoursField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, (Bundle.getMessage("HoursError") + "\n" + e),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing hours Field", e);
            return;
        }
        if ((hours < 0) || (hours > 23)) {
            JOptionPane.showMessageDialog(this, (Bundle.getMessage("HoursRangeError")),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        // get minutes, reporting errors if any
        try {
            minutes = Integer.parseInt(startMinutesField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, (Bundle.getMessage("MinutesError") + "\n" + e),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing Minutes Field", e);
            return;
        }
        if ((minutes < 0) || (minutes > 59)) {
            JOptionPane.showMessageDialog(this, (Bundle.getMessage("MinutesRangeError")),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        // set time of the fast clock
        long mSecPerHour = 3600000;
        long mSecPerMinute = 60000;
        Calendar cal = Calendar.getInstance();
        int cHours = cal.get(Calendar.HOUR_OF_DAY);
        long cNumMSec = cal.getTime().getTime();
        long nNumMSec = ((cNumMSec / mSecPerHour) * mSecPerHour) - (cHours * mSecPerHour)
                + (hours * mSecPerHour) + (minutes * mSecPerMinute);
        clock.setStartSetTime(startSetTimeCheckBox.isSelected(), new Date(nNumMSec));
        changed = true;
    }

    /**
     * Handle start clock combo box change
     */
    private void setClockStartChanged(ActionEvent e) {
        int sel = Timebase.NONE;
        switch (clockStartBox.getSelectedIndex()) {
            case startNixieClock:
                sel = Timebase.NIXIE_CLOCK;
                break;
            case startAnalogClock:
                sel = Timebase.ANALOG_CLOCK;
                break;
            case startLcdClock:
                sel = Timebase.LCD_CLOCK;
                break;
            case startPragotronClock:
                sel = Timebase.PRAGOTRON_CLOCK;
                break;
            default:
                break;
        }
        clock.setStartClockOption(sel);
        changed = true;
    }

    /**
     * Handle Start Clock button
     * @param e unused
     */
    public void startButtonActionPerformed(ActionEvent e) {
        clock.setRun(true);
    }

    /**
     * Handle Stop Clock button.
     * @param e unused
     */
    public void stopButtonActionPerformed(ActionEvent e) {
        clock.setRun(false);
    }

    /**
     * Update clock state information
     */
    void updateTime() {
        if (clock.getRun() || showTime) {
            showTime = false;
            setTimeLabel();
            timeLabel.setVisible(true);
        }
    }

    /**
     * Set the current Timebase time into timeLabel
     */
    void setTimeLabel() {
        // Get time
        Calendar cal = Calendar.getInstance();
        cal.setTime(clock.getTime());
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        // Format and display the time
        timeLabel.setText(" " + (hours / 10) + (hours - (hours / 10) * 10) + ":"
                + (minutes / 10) + (minutes - (minutes / 10) * 10));
        timeLabel.setToolTipText(clock.getTime().toString());
    }

    /**
     * Handle a change to clock properties.
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        switch (event.getPropertyName()) {
            case "run":
                updateRunningButton();
                break;
            case "rate":
                factorField.setText(threeDigits.format(clock.userGetRate()));
                break;
            case "time":
                updateTime();
                break;
            default:
                // ignore all other properties
        }
    }

    @Override
    protected void handleModified() {
        // ignore super routine
    }

    /**
     * Handle Store button.
     * @param e null if a save reminder, not null then from save button action.
     */
    public void saveButtonActionPerformed(ActionEvent e) {

        String messageString = (e==null ? Bundle.getMessage("ReminderSaveString", Bundle.getMessage("MenuClocks"))
                : Bundle.getMessage("StoreClockString") );

        // remind to save
        Object[] options = {Bundle.getMessage("ButtonSaveConfig"), Bundle.getMessage("ButtonSaveUser"),
                Bundle.getMessage("ButtonCancel")};
        int retval = javax.swing.JOptionPane.showOptionDialog(this,
                messageString,
                Bundle.getMessage((e==null ? "ReminderTitle" : "MenuItemStore")),
                0,
                javax.swing.JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        switch (retval) {
            case 0:
                new jmri.configurexml.StoreXmlConfigAction().actionPerformed(null); // Config only
                break;
            case 1:
                new jmri.configurexml.StoreXmlUserAction().actionPerformed(null); // Config + Panels
                break;
            default:
                log.debug("cancel");
        }
        changed = false;
    }

    /**
     * If data changed, prompt to store.
     * {@inheritDoc}
     */
    @Override
    public void windowClosing(WindowEvent e) {
        if (changed) { // remind to save
            saveButtonActionPerformed(null);
        }
        setVisible(false);
        super.windowClosing(e);
    }

    private final static Logger log = LoggerFactory.getLogger(SimpleClockFrame.class);

}
