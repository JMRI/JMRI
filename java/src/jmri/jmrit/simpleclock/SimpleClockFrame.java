package jmri.jmrit.simpleclock;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.CheckForNull;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
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
    //private boolean synchronize = true;
    //private boolean correct = true;
    private boolean changed = false;
    protected boolean showTime = false;
    DecimalFormat threeDigits = new DecimalFormat("0.000"); // 3 digit precision for speedup factor

    protected JComboBox<String> timeSourceBox = null;
    protected JComboBox<String> clockStartBox = null;
    protected JComboBox<String> startRunBox = null;
    // These are the indexes into the start run box.
    private final static int START_STOPPED = 0;
    private final static int START_RUNNING = 1;
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
    protected JButton setStartTimeButton = new JButton(
            Bundle.getMessage("ButtonSet"));
    protected JButton applyCloseButton = new JButton(Bundle.getMessage("ButtonApply"));
    protected JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));

    protected JLabel clockStatus = new JLabel();
    protected JLabel timeLabel = new JLabel();

    private int internalSourceIndex = 0;
    private int hardwareSourceIndex = 1;

    private int startNone = 0;
    private int startNixieClock = 1;
    private int startAnalogClock = 2;
    private int startLcdClock = 3;

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

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

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

        // Set up time source choice
        JPanel panel11 = new JPanel();
        panel11.add(new JLabel(Bundle.getMessage("TimeSource") + " "));
        timeSourceBox = new JComboBox<String>();
        panel11.add(timeSourceBox);
        timeSourceBox.addItem(Bundle.getMessage("ComputerClock"));
        hardwareName = InstanceManager.getDefault(jmri.ClockControl.class).getHardwareClockName();
        if (hardwareName != null) {
            timeSourceBox.addItem(hardwareName);
        }
        timeSourceBox.setToolTipText(Bundle.getMessage("TipTimeSource"));
        timeSourceBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTimeSourceChanged();
            }
        });
        contentPane.add(panel11);
        if (hardwareName != null) {
            if (clock.getInternalMaster()) {
                timeSourceBox.setSelectedIndex(internalSourceIndex);
            } else {
                timeSourceBox.setSelectedIndex(hardwareSourceIndex);
            }
            JPanel panel11x = new JPanel();
            synchronizeCheckBox = new JCheckBox(Bundle.getMessage("Synchronize") + " "
                    + hardwareName);
            synchronizeCheckBox.setToolTipText(Bundle.getMessage("TipSynchronize"));
            synchronizeCheckBox.setSelected(clock.getSynchronize());
            synchronizeCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    synchronizeChanged();
                }
            });
            panel11x.add(synchronizeCheckBox);
            contentPane.add(panel11x);
            if (InstanceManager.getDefault(jmri.ClockControl.class).canCorrectHardwareClock()) {
                JPanel panel11y = new JPanel();
                correctCheckBox = new JCheckBox(Bundle.getMessage("Correct"));
                correctCheckBox.setToolTipText(Bundle.getMessage("TipCorrect"));
                correctCheckBox.setSelected(clock.getCorrectHardware());
                correctCheckBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        correctChanged();
                    }
                });
                panel11y.add(correctCheckBox);
                contentPane.add(panel11y);
            }
            if (InstanceManager.getDefault(jmri.ClockControl.class).canSet12Or24HourClock()) {
                JPanel panel11z = new JPanel();
                displayCheckBox = new JCheckBox(Bundle.getMessage("Display12Hour"));
                displayCheckBox.setToolTipText(Bundle.getMessage("TipDisplay"));
                displayCheckBox.setSelected(clock.use12HourDisplay());
                displayCheckBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        displayChanged();
                    }
                });
                panel11z.add(displayCheckBox);
                contentPane.add(panel11z);
            }
        }

        // Set up speed up factor
        JPanel panel12 = new JPanel();
        panel12.add(new JLabel(Bundle.getMessage("SpeedUpFactor") + " "));
        panel12.add(factorField);
        factorField.setText(threeDigits.format(clock.userGetRate()));
        factorField.setToolTipText(Bundle.getMessage("TipFactorField"));
        panel12.add(new JLabel(":1 "));
        setRateButton.setToolTipText(Bundle.getMessage("TipSetRateButton"));
        setRateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setRateButtonActionPerformed();
            }
        });
        panel12.add(setRateButton);
        contentPane.add(panel12);

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
        setTimeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTimeButtonActionPerformed();
            }
        });
        panel2.add(setTimeButton);
        contentPane.add(panel2);

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
                jmri.util.Log4JUtil.warnOnce(log, "Unexpected initial run state = {}", clock.getClockInitialRunState());
                break;
        }
        startRunBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                startRunBoxChanged();
            }
        });
        panel61.add(startRunBox);
        startupOptionsPane.add(panel61);

        JPanel panel62 = new JPanel();
        startSetTimeCheckBox = new JCheckBox(Bundle.getMessage("StartSetTime"));
        startSetTimeCheckBox.setToolTipText(Bundle.getMessage("TipStartSetTime"));
        startSetTimeCheckBox.setSelected(clock.getStartSetTime());
        startSetTimeCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSetTimeChanged();
            }
        });
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
        setStartTimeButton.setToolTipText(Bundle.getMessage("TipSetStartTimeButton"));
        setStartTimeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSetTimeChanged();
            }
        });
        panel62.add(setStartTimeButton);
        startupOptionsPane.add(panel62);

        JPanel panelStartSetRate = new JPanel();
        startSetRateCheckBox = new JCheckBox(Bundle.getMessage("StartSetSpeedUpFactor") + " ");
        startSetRateCheckBox.setToolTipText(Bundle.getMessage("TipStartSetRate"));
        startSetRateCheckBox.setSelected(clock.getSetRateAtStart());
        startSetRateCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSetRateChanged();
            }
        });
        panelStartSetRate.add(startSetRateCheckBox);
        panelStartSetRate.add(startFactorField);
        startFactorField.setText(threeDigits.format(clock.getStartRate()));
        startFactorField.setToolTipText(Bundle.getMessage("TipFactorField"));
        startFactorField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                startFactorFieldChanged();
            }
        });
        startFactorField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                if (!focusEvent.isTemporary()) {
                    startFactorFieldChanged();
                }
                super.focusLost(focusEvent);
            }
        });
        panelStartSetRate.add(new JLabel(":1 "));
        startupOptionsPane.add(panelStartSetRate);


        JPanel panel63 = new JPanel();
        panel63.add(new JLabel(Bundle.getMessage("StartClock") + " "));
        clockStartBox = new JComboBox<String>();
        panel63.add(clockStartBox);
        clockStartBox.addItem(Bundle.getMessage("None"));
        clockStartBox.addItem(Bundle.getMessage("MenuItemNixieClock"));
        clockStartBox.addItem(Bundle.getMessage("MenuItemAnalogClock"));
        clockStartBox.addItem(Bundle.getMessage("MenuItemLcdClock"));
        clockStartBox.setSelectedIndex(startNone);
        if (clock.getStartClockOption() == Timebase.NIXIE_CLOCK) {
            clockStartBox.setSelectedIndex(startNixieClock);
        } else {
            if (clock.getStartClockOption() == Timebase.ANALOG_CLOCK) {
                clockStartBox.setSelectedIndex(startAnalogClock);
            } else {
                if (clock.getStartClockOption() == Timebase.LCD_CLOCK) {
                    clockStartBox.setSelectedIndex(startLcdClock);
                }
            }
        }
        clockStartBox.setToolTipText(Bundle.getMessage("TipClockStartOption"));
        clockStartBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setClockStartChanged();
            }
        });
        startupOptionsPane.add(panel63);
        JPanel panel64 = new JPanel();
        displayStartStopButton= new JCheckBox(Bundle.getMessage("DisplayOnOff"));
        displayStartStopButton.setSelected(clock.getShowStopButton());
        displayStartStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStopButtonChanged();
            }
        });
        panel64.add(displayStartStopButton);
        startupOptionsPane.add(panel64);

        Border panel6Border = BorderFactory.createEtchedBorder();
        Border panel6Titled = BorderFactory.createTitledBorder(panel6Border,
                Bundle.getMessage("BoxLabelStartUp"));
        startupOptionsPane.setBorder(panel6Titled);
        contentPane.add(startupOptionsPane);

        // Set up clock information panel
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));

        JPanel panel31 = new JPanel();
        panel31.add(clockStatus);
        // Set up Start and Stop buttons
        startButton.setToolTipText(Bundle.getMessage("TipStartButton"));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startButtonActionPerformed();
            }
        });
        panel31.add(startButton);
        stopButton.setToolTipText(Bundle.getMessage("TipStopButton"));
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopButtonActionPerformed();
            }
        });
        panel31.add(stopButton);
        panel3.add(panel31);

        JPanel panel32 = new JPanel();
        panel32.add(new JLabel(Bundle.getMessage("CurrentTime") + " "));
        setTimeLabel();
        panel32.add(timeLabel);
        panel3.add(panel32);

        Border panel3Border = BorderFactory.createEtchedBorder();
        Border panel3Titled = BorderFactory.createTitledBorder(panel3Border,
                Bundle.getMessage("BoxLabelClockState"));
        panel3.setBorder(panel3Titled);
        contentPane.add(panel3);

        // add save/close buttons
        JPanel panel4 = new JPanel();
        panel4.setLayout(new BoxLayout(panel4, BoxLayout.X_AXIS));
        panel4.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelButtonActionPerformed();
            }
        });
        panel4.add(applyCloseButton);
        applyCloseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveButtonActionPerformed();
            }
        });
        contentPane.add(panel4);

        // update contents for current status
        updateRunningButton();

        // add save menu item
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.configurexml.SaveMenu());

        setJMenuBar(menuBar);
        // add help menu to window
        addHelpMenu("package.jmri.jmrit.simpleclock.SimpleClockFrame", true);

        // pack for display
        pack();

        // listen for changes to the timebase parameters
        clock.addPropertyChangeListener(this);

        return;
    }

    private void startFactorFieldChanged() {
        Double v = parseRate(startFactorField.getText());
        if (v != null && !v.equals(clock.getStartRate())) {
            clock.setStartRate(v);
            changed = true;
        }
        startFactorField.setText(threeDigits.format(clock.getStartRate()));
    }

    private void startSetRateChanged() {
        clock.setSetRateAtStart(startSetRateCheckBox.isSelected());
        changed = true;
    }

    /**
     * Adjust to rate changes.
     */
    void updateRate() {
        factorField.setText(threeDigits.format(clock.userGetRate()));
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
    @CheckForNull Double parseRate(String fieldEntry) {
        double rate = 1.0;
        try {
            char decimalSeparator = threeDigits.getDecimalFormatSymbols().getDecimalSeparator() ;
            if (decimalSeparator != '.') {
                fieldEntry = fieldEntry.replace(decimalSeparator, '.') ;
            }
            rate = Double.valueOf(fieldEntry);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, (Bundle.getMessage("ParseRateError") + "\n" + e),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing user-entered rate: " + e);
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
     * Handle Set Rate button
     */
    public void setRateButtonActionPerformed() {
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
            log.error("Exception when setting timebase rate: " + e);
        }
        changed = true;
    }

    /**
     * Handle time source change
     *
     * Only changes the time source if the rate is OK (typically: Integer) for new source
     */
    private void setTimeSourceChanged() {
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
    private void synchronizeChanged() {
        clock.setSynchronize(synchronizeCheckBox.isSelected(), true);
        changed = true;
    }

    /**
     * Handle correct check box change
     */
    private void correctChanged() {
        clock.setCorrectHardware(correctCheckBox.isSelected(), true);
        changed = true;
    }

    /**
     * Handle 12-hour display check box change
     */
    private void displayChanged() {
        clock.set12HourDisplay(displayCheckBox.isSelected(), true);
        changed = true;
    }

    /**
     * Handle Set Time button
     */
    public void setTimeButtonActionPerformed() {
        int hours = 0;
        int minutes = 0;
        // get hours, reporting errors if any
        try {
            hours = Integer.parseInt(hoursField.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, (Bundle.getMessage("HoursError") + "\n" + e),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing hours Field: " + e);
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
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, (Bundle.getMessage("HoursError") + "\n" + e),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing hours Field: " + e);
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
    private void startRunBoxChanged() {
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
    private void showStopButtonChanged() {
        clock.setShowStopButton(displayStartStopButton.isSelected());
        changed = true;
    }

    /**
     * Handle start set time check box change
     */
    private void startSetTimeChanged() {
        int hours = 0;
        int minutes = 0;
        // get hours, reporting errors if any
        try {
            hours = Integer.parseInt(startHoursField.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, (Bundle.getMessage("HoursError") + "\n" + e),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing hours Field: " + e);
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
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, (Bundle.getMessage("HoursError") + "\n" + e),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing hours Field: " + e);
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
    private void setClockStartChanged() {
        int sel = Timebase.NONE;
        if (clockStartBox.getSelectedIndex() == startNixieClock) {
            sel = Timebase.NIXIE_CLOCK;
        } else if (clockStartBox.getSelectedIndex() == startAnalogClock) {
            sel = Timebase.ANALOG_CLOCK;
        } else if (clockStartBox.getSelectedIndex() == startLcdClock) {
            sel = Timebase.LCD_CLOCK;
        }
        clock.setStartClockOption(sel);
        changed = true;
    }

    /**
     * Handle Start Clock button
     */
    public void startButtonActionPerformed() {
        clock.setRun(true);
    }

    /**
     * Handle Stop Clock button
     */
    public void stopButtonActionPerformed() {
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
    }

    /**
     * Handle a change to clock properties
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        switch (event.getPropertyName()) {
            case "run":
                updateRunningButton();
                break;
            case "rate":
                updateRate();
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
     * Handle Setup Apply (Save) button.
     */
    public void saveButtonActionPerformed() {
        if (changed) {
            // remind to save
            Object[] options = {Bundle.getMessage("ButtonSaveUser"), Bundle.getMessage("ButtonSaveConfig"),
                    Bundle.getMessage("ButtonCancel")};
            int retval = javax.swing.JOptionPane.showOptionDialog(null,
                    Bundle.getMessage("ReminderSaveString", Bundle.getMessage("MenuClocks")),
                    Bundle.getMessage("ReminderTitle"),
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
        cancelButtonActionPerformed();
    }

    /**
     * Handle Setup Cancel button.
     */
    public void cancelButtonActionPerformed() {
        // Set buttons
        startButton.setVisible(false);
        stopButton.setVisible(true);

        changed = false;
        setVisible(false);
        dispose();
    }

    /**
     * Handle window closing event.
     */
    @Override
    public void windowClosing(WindowEvent e) {
        if (changed) {
            // remind to save  
            javax.swing.JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("ReminderSaveString", Bundle.getMessage("MenuClocks")),
                    Bundle.getMessage("ReminderTitle"),
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            changed = false;
        }
        setVisible(false);
        super.windowClosing(e);
    }

    private final static Logger log = LoggerFactory.getLogger(SimpleClockFrame.class);
    
}


