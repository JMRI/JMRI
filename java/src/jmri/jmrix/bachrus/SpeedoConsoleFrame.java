package jmri.jmrix.bachrus;

//<editor-fold defaultstate="collapsed" desc="Imports">
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.*;
import javax.swing.border.*;

import jmri.AddressedProgrammer;
import jmri.AddressedProgrammerManager;
import jmri.CommandStation;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.SpeedStepMode;
import jmri.ThrottleListener;
import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterEntrySelector;
import jmri.jmrit.roster.swing.GlobalRosterEntryComboBox;
import jmri.jmrix.bachrus.speedmatcher.*;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriJOptionPane;

//</editor-fold>
/**
 * Frame for Speedo Console for Bachrus running stand reader interface
 *
 * @author Andrew Crosland Copyright (C) 2010
 * @author Dennis Miller Copyright (C) 2015
 * @author Todd Wegter Copyright (C) 2019-2022
 */
public class SpeedoConsoleFrame extends JmriJFrame implements SpeedoListener,
        ThrottleListener,
        ProgListener,
        PropertyChangeListener {

    /**
     * TODO: Complete the help file
     */
    //<editor-fold defaultstate="collapsed" desc="Enums">
    protected enum DisplayType {
        NUMERIC, DIAL
    }

    protected enum ProfileState {
        IDLE, WAIT_FOR_THROTTLE, RUNNING
    }

    protected enum ProfileDirection {
        FORWARD, REVERSE
    }

    protected enum ProgState {
        IDLE,
        READ1,
        READ3,
        READ4,
        READ17,
        READ18,
        READ29,
    }

    static final int SPEEDMATCHWARMUPTIME = 60;

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Member Variables">
    //<editor-fold defaultstate="collapsed" desc="General GUI Elements">
    protected JLabel scaleLabel = new JLabel();
    protected JLabel customScaleLabel = new JLabel();
    protected JTextField customScaleField = new JTextField(3);
    protected int customScale = 148;
    protected JTextField speedTextField = new JTextField(12);
    protected JPanel displayCards = new JPanel();

    protected ButtonGroup modeGroup = new ButtonGroup();
    protected JRadioButton progButton = new JRadioButton(Bundle.getMessage("ProgTrack"));
    protected JRadioButton mainButton = new JRadioButton(Bundle.getMessage("OnMain"));

    protected ButtonGroup speedGroup = new ButtonGroup();
    protected JRadioButton mphButton = new JRadioButton(Bundle.getMessage("MPH"));
    protected JRadioButton kphButton = new JRadioButton(Bundle.getMessage("KPH"));
    protected ButtonGroup displayGroup = new ButtonGroup();
    protected JRadioButton numButton = new JRadioButton(Bundle.getMessage("Numeric"));
    protected JRadioButton dialButton = new JRadioButton(Bundle.getMessage("Dial"));
    protected SpeedoDial speedoDialDisplay = new SpeedoDial();
    protected JCheckBox dirFwdButton = new JCheckBox(Bundle.getMessage("ScanForward"));
    protected JCheckBox dirRevButton = new JCheckBox(Bundle.getMessage("ScanReverse"));
    protected JCheckBox toggleGridButton = new JCheckBox(Bundle.getMessage("ToggleGrid"));

    protected JLabel statusLabel = new JLabel(" ");
    protected javax.swing.JLabel readerLabel = new javax.swing.JLabel();
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="General Member Variables">
    protected static final int DEFAULT_SCALE = 8;

    protected float selectedScale = 0;
    protected int series = 0;
    protected float sampleSpeed = 0;
    protected float targetSpeed = 0;
    protected float currentSpeed = 0;
    protected float incSpeed = 0;
    protected float oldSpeed = 0;
    protected float acc = 0;
    protected float avSpeed = 0;
    protected int range = 1;
    protected float circ = 0;
    protected float count = 1;
    protected float freq;
    protected static final int DISPLAY_UPDATE = 500;
    protected static final int FAST_DISPLAY_RATIO = 5;

    /*
     * At low speed, readings arrive less often and less filtering
     * is applied to minimize the delay in updating the display
     *
     * Speed measurement is split into 4 ranges with an overlap, to
     * prevent "hunting" between the ranges.
     */
    protected static final int RANGE1LO = 0;
    protected static final int RANGE1HI = 9;
    protected static final int RANGE2LO = 7;
    protected static final int RANGE2HI = 31;
    protected static final int RANGE3LO = 29;
    protected static final int RANGE3HI = 62;
    protected static final int RANGE4LO = 58;
    protected static final int RANGE4HI = 9999;
    static final int[] FILTER_LENGTH = {0, 3, 6, 10, 20};

    String selectedScalePref = this.getClass().getName() + ".SelectedScale"; // NOI18N
    String customScalePref = this.getClass().getName() + ".CustomScale"; // NOI18N
    String speedUnitsKphPref = this.getClass().getName() + ".SpeedUnitsKph"; // NOI18N
    String dialTypePref = this.getClass().getName() + ".DialType"; // NOI18N
    jmri.UserPreferencesManager prefs;

    // members for handling the Speedo interface
    SpeedoTrafficController tc = null;

    protected String[] scaleStrings = new String[]{
        Bundle.getMessage("ScaleZ"),
        Bundle.getMessage("ScaleEuroN"),
        Bundle.getMessage("ScaleNFine"),
        Bundle.getMessage("ScaleJapaneseN"),
        Bundle.getMessage("ScaleBritishN"),
        Bundle.getMessage("Scale3mm"),
        Bundle.getMessage("ScaleTT"),
        Bundle.getMessage("Scale00"),
        Bundle.getMessage("ScaleH0"),
        Bundle.getMessage("ScaleS"),
        Bundle.getMessage("Scale048"),
        Bundle.getMessage("Scale045"),
        Bundle.getMessage("Scale043"),
        Bundle.getMessage("ScaleOther")
    };

    protected float[] scales = new float[]{
        220,
        160,
        152,
        150,
        148,
        120,
        101.6F,
        76,
        87,
        64,
        48,
        45,
        43,
        -1
    };

    //Create the combo box, and assign the scales to it
    JComboBox<String> scaleList = new JComboBox<>(scaleStrings);

    private SpeedoSystemConnectionMemo _memo = null;

    protected DisplayType display = DisplayType.NUMERIC;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="DCC Services">
    /*
     * Keep track of the DCC services available
     */
    protected int dccServices;
    protected static final int BASIC = 0;
    protected static final int PROG = 1;
    protected static final int COMMAND = 2;
    protected static final int THROTTLE = 4;

    protected boolean timerRunning = false;

    protected ProgState progState = ProgState.IDLE;

    protected float throttleIncrement;
    protected Programmer prog = null;
    protected AddressedProgrammer ops_mode_prog = null;
    protected CommandStation commandStation = null;

    private PowerManager pm = null;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Address Selector GUI Elements">
    //protected JLabel profileAddressLabel = new JLabel(Bundle.getMessage("LocoAddress"));
    //protected JTextField profileAddressField = new JTextField(6);
    protected JButton readAddressButton = new JButton(Bundle.getMessage("Read"));

    private final DccLocoAddressSelector addrSelector = new DccLocoAddressSelector();
    private JButton setButton;
    private GlobalRosterEntryComboBox rosterBox;
    protected RosterEntry rosterEntry;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Address Selector Member Variables">
    private final boolean disableRosterBoxActions = false;
    private DccLocoAddress locomotiveAddress = new DccLocoAddress(0, false);

    //protected int profileAddress = 0;
    protected int readAddress = 0;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Speed Profile GUI Elements">
    protected JButton trackPowerButton = new JButton(Bundle.getMessage("PowerUp"));
    protected JButton startProfileButton = new JButton(Bundle.getMessage("Start"));
    protected JButton stopProfileButton = new JButton(Bundle.getMessage("Stop"));
    protected JButton exportProfileButton = new JButton(Bundle.getMessage("Export"));
    protected JButton printProfileButton = new JButton(Bundle.getMessage("Print"));
    protected JButton resetGraphButton = new JButton(Bundle.getMessage("ResetGraph"));
    protected JButton loadProfileButton = new JButton(Bundle.getMessage("LoadRef"));
    protected JTextField printTitleText = new JTextField();

    GraphPane profileGraphPane;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Speed Profile Member Variables">
    protected DccSpeedProfile spFwd;
    protected DccSpeedProfile spRev;
    protected DccSpeedProfile spRef;

    protected ProfileDirection profileDir = ProfileDirection.FORWARD;
    protected DccThrottle throttle = null;
    protected int profileStep = 0;
    protected float profileSpeed;

    protected ProfileState profileState = ProfileState.IDLE;
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Speed Matching GUI Elements">

    protected SpinnerNumberModel accelerationSM = new SpinnerNumberModel(0, 0, 255, 1);
    protected SpinnerNumberModel decelerationSM = new SpinnerNumberModel(0, 0, 255, 1);

    //<editor-fold defaultstate="collapsed" desc="Basic">
    //TODO: TRW - reformat for I18N
    protected JLabel basicSpeedMatchInfo = new JLabel("<html><p>"
            + "You may need to adjust some of the provided settings since different decoder manufacturers interpret the NMRA standards differently."
            + "<br/><br/>Settings for some common manufactures:"
            + "<br/><ul>"
            + "<li>NCE - Simple CVs or speed table, disable Trim Reverse Speed</li>"
            + "<li>Digitrax - Speed Table only, Trim Reverse Speed can be enabled</li>"
            + "<li>ESU - Simple CVs or ESU speed table, Trim Reverse Speed can be enabled</li>"
            + "<li>SoundTraxx - Simple CVs or Speed Table, Trim Reverse Speed can be enabled</li>"
            + "</ul>"
            + "It is recommended to enable Warm Up Locomotive if your locomotive isn't already warmed up to help achieve a more accurate result."
            + "<br/><br/>Momentum is always set, so be sure to read or set the desired momentum values before speed matching."
            + "<br/><br/></p></html>");

    //TODO: TRW - I18N
    //TODO: TRW - Add ability to either read momentum or skip setting it
    protected JLabel basicSpeedMatchAccelerationLabel = new JLabel("Acceleration: ");
    protected JSpinner basicSpeedMatchAccelerationField = new JSpinner(accelerationSM);
    
    protected JLabel basicSpeedMatchDecelerationLabel = new JLabel("Deceleration: ");
    protected JSpinner basicSpeedMatchDecelerationField = new JSpinner(decelerationSM);
    
    protected JButton basicSpeedMatchReadMomentumButton = new JButton("Read Momentum");    
    
    protected JCheckBox basicSpeedMatchReverseCheckbox = new JCheckBox("Trim Reverse Speed");
    protected ButtonGroup basicSpeedMatcherTypeGroup = new ButtonGroup();
    protected JRadioButton basicSimpleCVSpeedMatchButton = new JRadioButton("Simple CVs (CV 2, CV 6, and CV 5)");
    protected JRadioButton basicSpeedTableSpeedMatchButton = new JRadioButton("Speed Table");
    protected JRadioButton basicESUSpeedMatchButton = new JRadioButton("ESU Speed Table");

    protected JLabel startSpeedTargetLabel = new JLabel("Start Speed: ");
    protected SpinnerNumberModel startSpeedSM = new SpinnerNumberModel(3, 1, 255, 1);
    protected JSpinner basicSpeedMatchTargetStartSpeedField = new JSpinner(startSpeedSM);
    protected JLabel basicSpeedMatchTargetStartSpeedUnit = new JLabel(" MPH");

    protected JLabel highSpeedTargetLabel = new JLabel("Top Speed: ");
    protected SpinnerNumberModel highSpeedSM = new SpinnerNumberModel(55, 1, 255, 1);
    protected JSpinner basicSpeedMatchTargetHighSpeedField = new JSpinner(highSpeedSM);
    protected JLabel basicSpeedMatchTargetHighSpeedUnit = new JLabel(" MPH");

    protected JCheckBox basicSpeedMatchWarmUpCheckBox = new JCheckBox("Warm Up Locomotive");
    protected JButton basicSpeedMatchStartStopButton = new JButton("Start Speed Match");
    
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Advanced">
    //TODO: TRW - AdvancedSpeedMatcherPane advancedSpeedMatcherPane;
    //</editor-fold>
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Speed Matching Member Variables">
    protected SpeedMatcher speedMatcher;

    //</editor-fold>
    //</editor-fold>
    // For testing only, must be 1 for normal use
    protected static final int SPEED_TEST_SCALE_FACTOR = 1;

    /**
     * Constructor for the SpeedoConsoleFrame
     *
     * @param memo the memo for the connection the Speedo is using
     */
    public SpeedoConsoleFrame(SpeedoSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    /**
     * Grabs the title for the SpeedoConsoleFrame
     *
     * @return the frame's title
     */
    protected String title() {
        return Bundle.getMessage("SpeedoConsole");
    }

    /**
     * Sets the description for the speed profile
     */
    private void setTitle() {
        Date today;
        String result;
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault());
        today = new Date();
        result = formatter.format(today);
        String annotate = Bundle.getMessage("ProfileFor") + " "
                + locomotiveAddress.getNumber() + " " + Bundle.getMessage("CreatedOn")
                + " " + result;
        printTitleText.setText(annotate);
    }

    /**
     * Override for the JmriJFrame's dispose function
     */
    @Override
    public void dispose() {
        if (prefs != null) {
            prefs.setComboBoxLastSelection(selectedScalePref, (String) scaleList.getSelectedItem());
            prefs.setProperty(customScalePref, "customScale", customScale);
            prefs.setSimplePreferenceState(speedUnitsKphPref, kphButton.isSelected());
            prefs.setSimplePreferenceState(dialTypePref, dialButton.isSelected());
        }
        _memo.getTrafficController().removeSpeedoListener(this);
        super.dispose();
    }

    // FIXME: Why does the if statement in this method include a direct false?
    /**
     * Override for the JmriJFrame's initComponents function
     */
    @Override
    public void initComponents() {
        prefs = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);

        setTitle(title());
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // What services do we have?
        dccServices = BASIC;
        if (InstanceManager.getNullableDefault(GlobalProgrammerManager.class) != null) {
            if (InstanceManager.getDefault(GlobalProgrammerManager.class).isGlobalProgrammerAvailable()) {
                prog = InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
                dccServices |= PROG;
            }
        }
        if (InstanceManager.getNullableDefault(jmri.ThrottleManager.class) != null) {
            // otherwise we'll send speed commands
            log.info("Using Throttle interface for profiling");
            dccServices |= THROTTLE;
        }

        if (InstanceManager.getNullableDefault(jmri.PowerManager.class) != null) {
            pm = InstanceManager.getDefault(jmri.PowerManager.class);
            pm.addPropertyChangeListener(this);
        }

        //<editor-fold defaultstate="collapsed" desc="GUI Layout and Button Handlers">
        //<editor-fold defaultstate="collapsed" desc="Basic Setup Panel">
        /*
         * Setup pane for basic operations
         */
        JPanel basicPane = new JPanel();
        basicPane.setLayout(new BoxLayout(basicPane, BoxLayout.Y_AXIS));

        // Scale panel to hold the scale selector
        JPanel scalePanel = new JPanel();
        scalePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("SelectScale")));
        scalePanel.setLayout(new FlowLayout());

        scaleList.setToolTipText(Bundle.getMessage("SelectScaleToolTip"));
        String lastSelectedScale = prefs.getComboBoxLastSelection(selectedScalePref);
        if (lastSelectedScale != null && !lastSelectedScale.equals("")) {
            try {
                scaleList.setSelectedItem(lastSelectedScale);
            } catch (ArrayIndexOutOfBoundsException e) {
                scaleList.setSelectedIndex(DEFAULT_SCALE);
            }
        } else {
            scaleList.setSelectedIndex(DEFAULT_SCALE);
        }

        if (scaleList.getSelectedIndex() > -1) {
            selectedScale = scales[scaleList.getSelectedIndex()];
        }

        // Listen to selection of scale
        scaleList.addActionListener(e -> {
            selectedScale = scales[scaleList.getSelectedIndex()];
            checkCustomScale();
        });

        scaleLabel.setText(Bundle.getMessage("Scale"));
        scaleLabel.setVisible(true);

        readerLabel.setText(Bundle.getMessage("UnknownReader"));
        readerLabel.setVisible(true);

        scalePanel.add(scaleLabel);
        scalePanel.add(scaleList);
        scalePanel.add(readerLabel);

        // Custom Scale panel to hold the custome scale selection
        JPanel customScalePanel = new JPanel();
        customScalePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("CustomScale")));
        customScalePanel.setLayout(new FlowLayout());

        customScaleLabel.setText("1: ");
        customScaleLabel.setVisible(true);
        customScaleField.setVisible(true);
        try {
            customScaleField.setText(prefs.getProperty(customScalePref, "customScale").toString());
        } catch (java.lang.NullPointerException npe) {
            customScaleField.setText("1");
        }
        checkCustomScale();
        getCustomScale();

        // Let user press return to enter custom scale
        customScaleField.addActionListener(e -> getCustomScale());

        customScalePanel.add(customScaleLabel);
        customScalePanel.add(customScaleField);

        basicPane.add(scalePanel);
        basicPane.add(customScalePanel);

        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Mode Panel">
        // Mode panel for selection of profile mode
        JPanel modePanel = new JPanel();
        modePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("SelectMode")));
        modePanel.setLayout(new FlowLayout());

        // Buttons to select the mode
        modeGroup.add(progButton);
        modeGroup.add(mainButton);
        progButton.setSelected(true);
        progButton.setToolTipText(Bundle.getMessage("TTProg"));
        mainButton.setToolTipText(Bundle.getMessage("TTMain"));
        modePanel.add(progButton);
        modePanel.add(mainButton);

        // Listen to change of profile mode
        progButton.addActionListener(e -> {
            if (((dccServices & PROG) == PROG)) {
                // Programmer is available to read back CVs
                readAddressButton.setEnabled(true);
                //TODO: TRW - readMomentumButton.setEnabled(true);
                statusLabel.setText(Bundle.getMessage("StatProg"));
            }
        });

        mainButton.addActionListener(e -> {
            // no programmer available to read back CVs
            readAddressButton.setEnabled(false);
            //TODO: TRW - readMomentumButton.setEnabled(false);
            statusLabel.setText(Bundle.getMessage("StatMain"));
        });
        // added to left side later

        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Speedometer Panel">
        // Speed panel for the dial or digital speed display
        JPanel speedPanel = new JPanel();
        speedPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("MeasuredSpeed")));
        speedPanel.setLayout(new BoxLayout(speedPanel, BoxLayout.X_AXIS));

        // Display Panel which is a card layout with cards to show
        // numeric or dial type speed display
        displayCards.setLayout(new CardLayout());

        // Numeric speed card
        JPanel numericSpeedPanel = new JPanel();
        numericSpeedPanel.setLayout(new BoxLayout(numericSpeedPanel, BoxLayout.X_AXIS));
        Font f = new Font("", Font.PLAIN, 96);
        speedTextField.setFont(f);
        speedTextField.setHorizontalAlignment(JTextField.RIGHT);
        speedTextField.setColumns(3);
        speedTextField.setText("0.0");
        speedTextField.setVisible(true);
        speedTextField.setToolTipText(Bundle.getMessage("SpeedHere"));
        numericSpeedPanel.add(speedTextField);

        // Dial speed card
        JPanel dialSpeedPanel = new JPanel();
        dialSpeedPanel.setLayout(new BoxLayout(dialSpeedPanel, BoxLayout.X_AXIS));
        dialSpeedPanel.add(speedoDialDisplay);
        speedoDialDisplay.update(0.0F);

        // Add cards to panel
        displayCards.add(dialSpeedPanel, "DIAL");
        displayCards.add(numericSpeedPanel, "NUMERIC");
        CardLayout cl = (CardLayout) displayCards.getLayout();
        cl.show(displayCards, "DIAL");

        // button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        speedGroup.add(mphButton);
        speedGroup.add(kphButton);
        mphButton.setToolTipText(Bundle.getMessage("TTDisplayMPH"));
        kphButton.setToolTipText(Bundle.getMessage("TTDisplayKPH"));
        mphButton.setSelected(!prefs.getSimplePreferenceState(speedUnitsKphPref));
        kphButton.setSelected(prefs.getSimplePreferenceState(speedUnitsKphPref));
        displayGroup.add(numButton);
        displayGroup.add(dialButton);
        numButton.setToolTipText(Bundle.getMessage("TTDisplayNumeric"));
        dialButton.setToolTipText(Bundle.getMessage("TTDisplayDial"));
        numButton.setSelected(!prefs.getSimplePreferenceState(dialTypePref));
        dialButton.setSelected(prefs.getSimplePreferenceState(dialTypePref));
        buttonPanel.add(mphButton);
        buttonPanel.add(kphButton);
        buttonPanel.add(numButton);
        buttonPanel.add(dialButton);

        speedPanel.add(displayCards);
        speedPanel.add(buttonPanel);

        // Listen to change of units, convert current average and update display
        mphButton.addActionListener(e -> setUnits());
        kphButton.addActionListener(e -> setUnits());

        // Listen to change of display
        numButton.addActionListener(e -> setDial());
        dialButton.addActionListener(e -> setDial());

        basicPane.add(speedPanel);

        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Address, Speed Profiling, Speed Matching, and Title Panel">
        JPanel profileAndSpeedMatchingPane = new JPanel();
        profileAndSpeedMatchingPane.setLayout(new BorderLayout());

        //<editor-fold defaultstate="collapsed" desc="Address Panel">
        JPanel addrPane = new JPanel();
        GridBagLayout gLayout = new GridBagLayout();
        GridBagConstraints gConstraints = new GridBagConstraints();
        gConstraints.insets = new Insets(3, 3, 3, 3);
        Border addrPaneBorder = javax.swing.BorderFactory.createEtchedBorder();
        TitledBorder addrPaneTitle = javax.swing.BorderFactory.createTitledBorder(addrPaneBorder, Bundle.getMessage("LocoSelection"));
        addrPane.setLayout(gLayout);
        addrPane.setBorder(addrPaneTitle);

        setButton = new JButton(Bundle.getMessage("ButtonSet"));
        setButton.addActionListener(e -> changeOfAddress());
        addrSelector.setAddress(null);

        rosterBox = new GlobalRosterEntryComboBox();
        rosterBox.setNonSelectedItem(Bundle.getMessage("NoLocoSelected"));
        rosterBox.setToolTipText(Bundle.getMessage("TTSelectLocoFromRoster"));

        /*
         Using an ActionListener didn't select a loco from the ComboBox properly
         so changed it to a PropertyChangeListener approach modeled on the code
         in CombinedLocoSelPane class, layoutRosterSelection method, which is known to work.
         Not sure why the ActionListener didn't work properly, but this fixes the bug
         */
        rosterBox.addPropertyChangeListener(RosterEntrySelector.SELECTED_ROSTER_ENTRIES, pce -> {
            if (!disableRosterBoxActions) { //Have roster box actions been disabled?
                rosterItemSelected();
            }
        });

        readAddressButton.setToolTipText(Bundle.getMessage("ReadLoco"));

        addrPane.add(addrSelector.getCombinedJPanel(), gConstraints);
        addrPane.add(new JLabel(" "), gConstraints);
        addrPane.add(setButton, gConstraints);
        addrPane.add(new JLabel(" "), gConstraints);
        addrPane.add(rosterBox, gConstraints);
        addrPane.add(new JLabel(" "), gConstraints);
        addrPane.add(readAddressButton, gConstraints);

        if (((dccServices & PROG) != PROG) || (mainButton.isSelected())) {
            // No programming facility so user must enter address
            addrSelector.setEnabled(false);
            readAddressButton.setEnabled(false);
        } else {
            addrSelector.setEnabled(true);
            readAddressButton.setEnabled(true);
        }

        // Listen to read button
        readAddressButton.addActionListener(e -> readAddress());

        // set up top panel of modePanel and addrPane
        var topLeftPane = new JPanel();
        topLeftPane.setLayout(new BorderLayout());
        topLeftPane.add(modePanel, BorderLayout.NORTH);
        topLeftPane.add(addrPane, BorderLayout.SOUTH);

        profileAndSpeedMatchingPane.add(topLeftPane, BorderLayout.NORTH);

        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Speed Matching and Profiling Panel">
        JTabbedPane profileAndSpeedMatchingTabs = new JTabbedPane();

        //<editor-fold defaultstate="collapsed" desc="Speed Profiling Tab">
        // Pane for profiling loco speed curve
        JPanel profilePane = new JPanel();
        profilePane.setLayout(new BorderLayout());

        // pane to hold the graph
        spFwd = new DccSpeedProfile(29);       // 28 step plus step 0
        spRev = new DccSpeedProfile(29);       // 28 step plus step 0
        spRef = new DccSpeedProfile(29);       // 28 step plus step 0
        profileGraphPane = new GraphPane(spFwd, spRev, spRef);
        profileGraphPane.setPreferredSize(new Dimension(600, 300));
        profileGraphPane.setXLabel(Bundle.getMessage("SpeedStep"));
        profileGraphPane.setUnitsMph();

        profilePane.add(profileGraphPane, BorderLayout.CENTER);

        // pane to hold the buttons
        JPanel profileButtonPane = new JPanel();
        profileButtonPane.setLayout(new FlowLayout());
        profileButtonPane.add(trackPowerButton);
        trackPowerButton.setToolTipText(Bundle.getMessage("TTPower"));
        profileButtonPane.add(startProfileButton);
        startProfileButton.setToolTipText(Bundle.getMessage("TTStartProfile"));
        profileButtonPane.add(stopProfileButton);
        stopProfileButton.setToolTipText(Bundle.getMessage("TTStopProfile"));
        profileButtonPane.add(exportProfileButton);
        exportProfileButton.setToolTipText(Bundle.getMessage("TTSaveProfile"));
        profileButtonPane.add(printProfileButton);
        printProfileButton.setToolTipText(Bundle.getMessage("TTPrintProfile"));
        profileButtonPane.add(resetGraphButton);
        resetGraphButton.setToolTipText(Bundle.getMessage("TTResetGraph"));
        profileButtonPane.add(loadProfileButton);
        loadProfileButton.setToolTipText(Bundle.getMessage("TTLoadProfile"));

        // pane to wrap buttons and title
        JPanel profileSouthPane = new JPanel();
        profileSouthPane.setLayout(new BoxLayout(profileSouthPane, BoxLayout.Y_AXIS));
        profileSouthPane.add(profileButtonPane);
        profilePane.add(profileSouthPane, BorderLayout.SOUTH);

        // Pane to hold controls
        JPanel profileControlPane = new JPanel();
        profileControlPane.setLayout(new BoxLayout(profileControlPane, BoxLayout.Y_AXIS));
        dirFwdButton.setSelected(true);
        dirFwdButton.setToolTipText(Bundle.getMessage("TTMeasFwd"));
        dirRevButton.setToolTipText(Bundle.getMessage("TTMeasRev"));
        dirFwdButton.setForeground(Color.RED);
        dirRevButton.setForeground(Color.BLUE);
        profileControlPane.add(dirFwdButton);
        profileControlPane.add(dirRevButton);
        toggleGridButton.setSelected(true);
        profileControlPane.add(toggleGridButton);
        profileGraphPane.showGrid(toggleGridButton.isSelected());

        profilePane.add(profileControlPane, BorderLayout.EAST);

        profileAndSpeedMatchingTabs.addTab("Speed Profile", profilePane);

        //<editor-fold defaultstate="collapsed" desc="Speed Profiling Button Handlers">
        // Listen to track Power button
        trackPowerButton.addActionListener(e -> trackPower());

        // Listen to start profile button
        startProfileButton.addActionListener(e -> {
            getCustomScale();
            startProfile();
        });

        // Listen to stop profile button
        stopProfileButton.addActionListener(e -> stopProfileAndSpeedMatch());

        // Listen to grid button
        toggleGridButton.addActionListener(e -> {
            profileGraphPane.showGrid(toggleGridButton.isSelected());
            profileGraphPane.repaint();
        });

        // Listen to export button
        exportProfileButton.addActionListener(e -> {
            if (dirFwdButton.isSelected() && dirRevButton.isSelected()) {
                DccSpeedProfile[] sp = {spFwd, spRev};
                DccSpeedProfile.export(sp, locomotiveAddress.getNumber(), profileGraphPane.getUnits());
            } else if (dirFwdButton.isSelected()) {
                DccSpeedProfile.export(spFwd, locomotiveAddress.getNumber(), "fwd", profileGraphPane.getUnits());
            } else if (dirRevButton.isSelected()) {
                DccSpeedProfile.export(spRev, locomotiveAddress.getNumber(), "rev", profileGraphPane.getUnits());
            }
        });

        // Listen to print button
        printProfileButton.addActionListener(e -> profileGraphPane.printProfile(printTitleText.getText()));

        // Listen to reset graph button
        resetGraphButton.addActionListener(e -> {
            spFwd.clear();
            spRev.clear();
            spRef.clear();
            speedoDialDisplay.reset();
            profileGraphPane.repaint();
        });

        // Listen to Load Reference button
        loadProfileButton.addActionListener(e -> {
            spRef.clear();
            int response = spRef.importDccProfile(profileGraphPane.getUnits());
            if (response == -1) {
                statusLabel.setText(Bundle.getMessage("StatFileError"));
            } else {
                statusLabel.setText(Bundle.getMessage("StatFileSuccess"));
            }
            profileGraphPane.repaint();
        });

        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Basic Speed Matching Tab">
       basicSpeedMatchReverseCheckbox.setSelected(true);
        
        //TODO: TRW - I18N
        basicSimpleCVSpeedMatchButton.setToolTipText("Set VStart (CV 2), VMid (CV 6), and VHigh (CV 5). Faster than setting the speed table.");
        basicSpeedTableSpeedMatchButton.setToolTipText("Set the speed table. Some decoders will only respect the trim CVs if the complex speed table is used.");
        basicESUSpeedMatchButton.setToolTipText("Set the speed table along with VStart (CV 2) and VHigh (CV 5). This is necessary to use the speed table in ESU decoders.");
        basicSpeedMatcherTypeGroup.add(basicSimpleCVSpeedMatchButton);
        basicSpeedMatcherTypeGroup.add(basicSpeedTableSpeedMatchButton);
        basicSpeedMatcherTypeGroup.add(basicESUSpeedMatchButton);        
        basicSimpleCVSpeedMatchButton.setSelected(true);
        
        basicSpeedMatchTargetStartSpeedUnit.setPreferredSize(new Dimension(35, 16));
        basicSpeedMatchTargetHighSpeedUnit.setPreferredSize(new Dimension(35, 16));
        basicSpeedMatchWarmUpCheckBox.setSelected(true);
        
        //TODO: TRW - set tooltips for spinners

        JPanel basicSpeedMatcherPane = new JPanel();
        basicSpeedMatcherPane.setLayout(new BorderLayout());
        JPanel basicSpeedMatchSettingsPane = new JPanel();
        basicSpeedMatchSettingsPane.setLayout(new BoxLayout(basicSpeedMatchSettingsPane, BoxLayout.PAGE_AXIS));

        //Important Information
        //TODO: TRW - I18N
        JPanel speedMatchImportantInfoPane = new JPanel();
        speedMatchImportantInfoPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Important Information"));
        speedMatchImportantInfoPane.setLayout(new BoxLayout(speedMatchImportantInfoPane, BoxLayout.LINE_AXIS));
        speedMatchImportantInfoPane.add(basicSpeedMatchInfo);
        //TODO: TRW - Remove?
//        speedMatchImportantInfoPane.add(basicSpeedMatchInfo2);
//        speedMatchImportantInfoPane.add(basicSpeedMatchInfoNCE);
//        speedMatchImportantInfoPane.add(basicSpeedMatchInfoDigitrax);
//        speedMatchImportantInfoPane.add(basicSpeedMatchInfoESU);
//        speedMatchImportantInfoPane.add(basicSpeedMatchInfoSoundtraxx);
        basicSpeedMatchSettingsPane.add(speedMatchImportantInfoPane);

        //Speed Matcher Mode
        //TODO: TRW - I18N
        JPanel speedMatchModePane = new JPanel();
        speedMatchModePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Select the Desired Mode"));
        speedMatchModePane.setLayout(new FlowLayout());
        speedMatchModePane.add(basicSimpleCVSpeedMatchButton);
        speedMatchModePane.add(basicSpeedTableSpeedMatchButton);
        speedMatchModePane.add(basicESUSpeedMatchButton);
        basicSpeedMatchSettingsPane.add(speedMatchModePane);

        //Other Settings
        //TODO: TRW - I18N
        JPanel speedMatchOtherSettingsPane = new JPanel();
        speedMatchOtherSettingsPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Other Speed Matching Settings"));
        speedMatchOtherSettingsPane.setLayout(new FlowLayout());
        speedMatchOtherSettingsPane.add(basicSpeedMatchWarmUpCheckBox);
        speedMatchOtherSettingsPane.add(basicSpeedMatchReverseCheckbox);
        basicSpeedMatchSettingsPane.add(speedMatchOtherSettingsPane);

        //Momentum
        //TODO: TRW - I18N
        JPanel speedMatchMomentumPane = new JPanel();
        speedMatchMomentumPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Set Momentum"));
        speedMatchMomentumPane.setLayout(new FlowLayout());
        speedMatchMomentumPane.add(basicSpeedMatchAccelerationLabel);
        speedMatchMomentumPane.add(basicSpeedMatchAccelerationField);
        speedMatchMomentumPane.add(basicSpeedMatchDecelerationLabel);
        speedMatchMomentumPane.add(basicSpeedMatchDecelerationField);
        speedMatchMomentumPane.add(basicSpeedMatchReadMomentumButton);
        basicSpeedMatchSettingsPane.add(speedMatchMomentumPane);

        //Speed Settings
        JPanel speedMatchSpeedPane = new JPanel();
        speedMatchSpeedPane.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        speedMatchSpeedPane.add(startSpeedTargetLabel, gbc);
        speedMatchSpeedPane.add(basicSpeedMatchTargetStartSpeedField, gbc);
        speedMatchSpeedPane.add(basicSpeedMatchTargetStartSpeedUnit, gbc);
        speedMatchSpeedPane.add(highSpeedTargetLabel, gbc);
        speedMatchSpeedPane.add(basicSpeedMatchTargetHighSpeedField, gbc);
        speedMatchSpeedPane.add(basicSpeedMatchTargetHighSpeedUnit, gbc);
        speedMatchSpeedPane.add(basicSpeedMatchStartStopButton, gbc);

        basicSpeedMatcherPane.add(basicSpeedMatchSettingsPane, BorderLayout.NORTH);
        basicSpeedMatcherPane.add(speedMatchSpeedPane, BorderLayout.CENTER);

        profileAndSpeedMatchingTabs.add("Basic Speed Matcher", basicSpeedMatcherPane);

        //<editor-fold defaultstate="collapsed" desc="Basic Speed Matcher Button Handlers">
        // Listen to speed match button
        basicSpeedMatchStartStopButton.addActionListener(e -> {
            int targetStartSpeed;
            int targetHighSpeed;
            boolean speedMatchReverse;
            boolean warmUpLoco;
            int acceleration;
            int deceleration;

            if ((speedMatcher == null) && (profileState == ProfileState.IDLE)) {
                targetStartSpeed = startSpeedSM.getNumber().intValue();
                targetHighSpeed = highSpeedSM.getNumber().intValue();
                acceleration = accelerationSM.getNumber().intValue();
                deceleration = decelerationSM.getNumber().intValue();
                
                //TODO: TRW - get complex/simple
                speedMatchReverse = basicSpeedMatchReverseCheckbox.isSelected();
                warmUpLoco = basicSpeedMatchWarmUpCheckBox.isSelected();

                speedMatcher = SpeedMatcherFactory.getSpeedMatcher(
                    new SpeedMatcherConfig(
                        SpeedMatcherConfig.SpeedMatcherType.BASIC,
                        SpeedMatcherConfig.SpeedTable.SIMPLE,
                        locomotiveAddress,
                        targetStartSpeed,
                        targetHighSpeed,
                        mphButton.isSelected() ? Speed.Unit.MPH : Speed.Unit.KPH,
                        warmUpLoco,
                        speedMatchReverse,
                        acceleration,
                        deceleration,
                        pm,
                        log,
                        statusLabel
                    )
                );

                if (speedMatcher.StartSpeedMatch()) {
                    //TODO: TRW - I18N
                     basicSpeedMatchStartStopButton.setText("Stop Speed Match"); 
                }
                else {
                    speedMatcher = null;
                }
            } else {
                stopProfileAndSpeedMatch();
            }
        });
        
        // Listen to read momentum button
        basicSpeedMatchReadMomentumButton.addActionListener(e -> readMomentum());
        
        //</editor-fold>
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Advanced Speed Matcher Tab">

        //TODO: TRW - add advanced speed matching pane tab
        //</editor-fold>
        profileAndSpeedMatchingPane.add(profileAndSpeedMatchingTabs, BorderLayout.CENTER);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Title Panel">
        // pane to hold the title
        JPanel titlePane = new JPanel();
        titlePane.setLayout(new BoxLayout(titlePane, BoxLayout.X_AXIS));
        titlePane.setBorder(new EmptyBorder(3, 0, 3, 0));
        //JTextArea profileTitle = new JTextArea("Title: ");
        //profileTitlePane.add(profileTitle);
        printTitleText.setToolTipText(Bundle.getMessage("TTPrintTitle"));
        printTitleText.setText(Bundle.getMessage("TTText1"));
        titlePane.add(printTitleText);

        profileAndSpeedMatchingPane.add(titlePane, BorderLayout.SOUTH);
        //</editor-fold>

        //</editor-fold>
        //</editor-fold>
        // Create the main pane and add the sub-panes
        JPanel mainPane = new JPanel();
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.X_AXIS));
        // make basic panel
        mainPane.add(basicPane);

        //TODO: TRW - REMOVE - add profilePane without condition to test/debug without DCC system
        //if (((dccServices & THROTTLE) == THROTTLE)
        //        || ((dccServices & COMMAND) == COMMAND)) {
        mainPane.add(profileAndSpeedMatchingPane);
        //} else {
        //   log.info("{} Connection:{}", Bundle.getMessage("StatNoDCC"), _memo.getUserName());
        //   statusLabel.setText(Bundle.getMessage("StatNoDCC"));
        //}

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.bachrus.SpeedoConsoleFrame", true);

        // Create a wrapper with a status line and add the main content
        JPanel statusWrapper = new JPanel();
        statusWrapper.setLayout(new BorderLayout());
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);

        statusPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        statusWrapper.add(mainPane, BorderLayout.CENTER);
        statusWrapper.add(statusPanel, BorderLayout.SOUTH);

        getContentPane().add(statusWrapper);
        //</editor-fold>

        // connect to TrafficController
        tc = _memo.getTrafficController();
        tc.addSpeedoListener(this);

        setUnits();
        setDial();

        // pack for display
        pack();

        speedoDialDisplay.scaleFace();
    }

    //<editor-fold defaultstate="collapsed" desc="Speed Reader and Calculations">
    /**
     * Handle "replies" from the hardware. In fact, all the hardware does is
     * send a constant stream of unsolicited speed updates.
     *
     * @param l the reply to handle
     */
    @Override
    public synchronized void reply(SpeedoReply l) {  // receive a reply message and log it
        //log.debug("Speedo reply " + l.toString());
        count = l.getCount();
        series = l.getSeries();
        if (count > 0) {
            switch (series) {
                case 4:
                    circ = 12.5664F;
                    readerLabel.setText(Bundle.getMessage("Reader40"));
                    break;
                case 5:
                    circ = 18.8496F;
                    readerLabel.setText(Bundle.getMessage("Reader50"));
                    break;
                case 6:
                    circ = 50.2655F;
                    readerLabel.setText(Bundle.getMessage("Reader60"));
                    break;
                case 103:
                    circ = (float) ((5.95 + 0.9) * Math.PI);
                    readerLabel.setText(Bundle.getMessage("Reader103"));
                    break;
                default:
                    speedTextField.setText(Bundle.getMessage("ReaderErr"));
                    log.error("Invalid reader type");
                    break;
            }

            // Update speed
            calcSpeed();
        }
        if (timerRunning == false) {
            // first reply starts the timer
            startReplyTimer();
            startDisplayTimer();
            startFastDisplayTimer();
            timerRunning = true;
        } else {
            // subsequent replies restart it
            replyTimer.restart();
        }
    }

    /**
     * Calculates the scale speed in KPH
     */
    protected void calcSpeed() {
        float thisScale = (selectedScale == -1) ? customScale : selectedScale;
        if (series == 103) {
            // KPF-Zeller
            // calculate kph: r/sec * circumference converted to hours and kph in scaleFace()
            sampleSpeed = (float) ((count / 8.) * circ * 3600 / 1.0E6 * thisScale * SPEED_TEST_SCALE_FACTOR);
            // data arrives at constant rate, so we don't average nor switch range
            avSpeed = sampleSpeed;
            log.debug("New KPF-Zeller sample: {} Average: {}", sampleSpeed, avSpeed);

        } else if (series > 0 && series <= 6) {
            // Bachrus
            // Scale the data and calculate kph
            try {
                freq = 1500000 / count;
                sampleSpeed = (freq / 24) * circ * thisScale * 3600 / 1000000 * SPEED_TEST_SCALE_FACTOR;
            } catch (ArithmeticException ae) {
                log.error("Exception calculating sampleSpeed", ae);
            }
            avFn(sampleSpeed);
            log.debug("New Bachrus sample: {} Average: {}", sampleSpeed, avSpeed);
            log.debug("Acc: {} range: {}", acc, range);
            switchRange();
        }
    }

    /**
     * Calculates the average speed using a filter
     *
     * @param speed the speed of the latest interation
     */
    protected void avFn(float speed) {
        // Averaging function used for speed is
        // S(t) = S(t-1) - [S(t-1)/N] + speed
        // A(t) = S(t)/N
        //
        // where S is an accumulator, N is the length of the filter (i.e.,
        // the number of samples included in the rolling average), and A is
        // the result of the averaging function.
        //
        // Re-arranged
        // S(t) = S(t-1) - A(t-1) + speed
        // A(t) = S(t)/N
        acc = acc - avSpeed + speed;
        avSpeed = acc / FILTER_LENGTH[range];
    }

    /**
     * Clears the average speed calculation
     */
    protected void avClr() {
        acc = 0;
        avSpeed = 0;
    }

    /**
     * Switches the filter used for averaging speed based on the measured speed
     */
    protected void switchRange() {
        // When we switch range we must compensate the current accumulator
        // value for the longer filter.
        switch (range) {
            case 1:
                if (sampleSpeed > RANGE1HI) {
                    range++;
                    acc = acc * FILTER_LENGTH[2] / FILTER_LENGTH[1];
                }
                break;
            case 2:
                if (sampleSpeed < RANGE2LO) {
                    range--;
                    acc = acc * FILTER_LENGTH[1] / FILTER_LENGTH[2];
                } else if (sampleSpeed > RANGE2HI) {
                    range++;
                    acc = acc * FILTER_LENGTH[3] / FILTER_LENGTH[2];
                }
                break;
            case 3:
                if (sampleSpeed < RANGE3LO) {
                    range--;
                    acc = acc * FILTER_LENGTH[2] / FILTER_LENGTH[3];
                } else if (sampleSpeed > RANGE3HI) {
                    range++;
                    acc = acc * FILTER_LENGTH[4] / FILTER_LENGTH[3];
                }
                break;
            case 4:
                if (sampleSpeed < RANGE4LO) {
                    range--;
                    acc = acc * FILTER_LENGTH[3] / FILTER_LENGTH[4];
                }
                break;
            default:
                log.debug("range {} unsupported, range unchanged.", range);
        }
    }

    /**
     * Displays the speed in the SpeedoConsoleFrame's digital/analog speedometer
     */
    protected void showSpeed() {
        float speedForText = currentSpeed;
        if (mphButton.isSelected()) {
            speedForText = Speed.kphToMph(speedForText);
        }
        if (series > 0) {
            if ((currentSpeed < 0) || (currentSpeed > 999)) {
                log.error("Calculated speed out of range: {}", currentSpeed);
                speedTextField.setText("999");
            } else {
                // Final smoothing as applied by Bachrus Console. Don't update display
                // unless speed has changed more than 2%
                if ((currentSpeed > oldSpeed * 1.02) || (currentSpeed < oldSpeed * 0.98)) {
                    speedTextField.setText(MessageFormat.format("{0,number,##0.0}", speedForText));
                    speedTextField.setHorizontalAlignment(JTextField.RIGHT);
                    oldSpeed = currentSpeed;
                    speedoDialDisplay.update(currentSpeed);
                }
            }
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Speedometer Helper Functions">
    /**
     * Check if custom scale selected and enable the custom scale entry field.
     */
    protected void checkCustomScale() {
        if (selectedScale == -1) {
            customScaleField.setEnabled(true);
        } else {
            customScaleField.setEnabled(false);
        }
    }

    /**
     * Set the speed to be displayed as a dial or numeric
     */
    protected void setDial() {
        CardLayout cl = (CardLayout) displayCards.getLayout();
        if (numButton.isSelected()) {
            display = DisplayType.NUMERIC;
            cl.show(displayCards, "NUMERIC");
        } else {
            display = DisplayType.DIAL;
            cl.show(displayCards, "DIAL");
        }
    }

    /**
     * Set the displays to mile per hour or kilometers per hour
     */
    protected void setUnits() {
        //TODO: TRW - I18N
        if (mphButton.isSelected()) {
            profileGraphPane.setUnitsMph();
            basicSpeedMatchTargetStartSpeedUnit.setText(" MPH");
            basicSpeedMatchTargetHighSpeedUnit.setText(" MPH");
        } else {
            profileGraphPane.setUnitsKph();
            basicSpeedMatchTargetStartSpeedUnit.setText(" KPH");
            basicSpeedMatchTargetHighSpeedUnit.setText(" KPH");
        }
        profileGraphPane.repaint();
        if (mphButton.isSelected()) {
            speedoDialDisplay.setUnitsMph();
        } else {
            speedoDialDisplay.setUnitsKph();
        }
        speedoDialDisplay.update(currentSpeed);
        speedoDialDisplay.repaint();
    }

    /**
     * Validate the users custom scale entry.
     */
    protected void getCustomScale() {
        if (selectedScale == -1) {
            try {
                customScale = Integer.parseUnsignedInt(customScaleField.getText());
            } catch (NumberFormatException ex) {
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("CustomScaleDialog"),
                        Bundle.getMessage("CustomScaleTitle"), JmriJOptionPane.ERROR_MESSAGE);
            }
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Address Helper Functions">
    /**
     * Handle changing/setting the address.
     */
    private synchronized void changeOfAddress() {
        if (addrSelector.getAddress() != null) {
            locomotiveAddress = addrSelector.getAddress();
            setTitle();
        } else {
            locomotiveAddress = new DccLocoAddress(0, true);
            setTitle();
        }
    }

    /**
     * Set the RosterEntry for this throttle.
     *
     * @param entry roster entry selected for throttle
     */
    public void setRosterEntry(RosterEntry entry) {
        rosterBox.setSelectedItem(entry);
        addrSelector.setAddress(entry.getDccLocoAddress());
        rosterEntry = entry;
        changeOfAddress();
    }

    /**
     * Called when a RosterEntry is selected
     */
    private void rosterItemSelected() {
        if (rosterBox.getSelectedRosterEntries().length != 0) {
            setRosterEntry(rosterBox.getSelectedRosterEntries()[0]);
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Power Manager Helper Functions">
    /**
     * {@inheritDoc}
     * <p>
     * Handles property changes from the power manager.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setPowerStatus();
    }

    /**
     * Switches the track power on or off
     */
    private void setPowerStatus() {
        if (pm == null) {
            return;
        }
        if (pm.getPower() == PowerManager.ON) {
            trackPowerButton.setText(Bundle.getMessage("PowerDown"));
            //statusLabel.setText(Bundle.getMessage("StatTOn"));
        } else if (pm.getPower() == PowerManager.OFF) {
            trackPowerButton.setText(Bundle.getMessage("PowerUp"));
            //statusLabel.setText(Bundle.getMessage("StatTOff"));
        }
    }

    /**
     * Called when the track power button is clicked to turn on or off track
     * power Allows user to power up and give time for sound decoder startup
     * sequence before running a profile
     */
    protected void trackPower() {
        try {
            if (pm.getPower() != PowerManager.ON) {
                pm.setPower(PowerManager.ON);
            } else {
                stopProfileAndSpeedMatch();
                pm.setPower(PowerManager.OFF);
            }
        } catch (JmriException e) {
            log.error("Exception during power on: {}", e.toString());
        }
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Speed Profiling">
    javax.swing.Timer profileTimer = null;

    /**
     * Start the speed profiling process
     */
    protected synchronized void startProfile() {
        if (locomotiveAddress.getNumber() > 0) {
            if (dirFwdButton.isSelected() || dirRevButton.isSelected()) {
                if ((speedMatcher == null || speedMatcher.IsIdle()) && (profileState == ProfileState.IDLE)) {
                    profileTimer = new javax.swing.Timer(4000, e -> profileTimeout());
                    profileTimer.setRepeats(false);
                    profileState = ProfileState.WAIT_FOR_THROTTLE;
                    // Request a throttle
                    statusLabel.setText(Bundle.getMessage("StatReqThrottle"));
                    spFwd.clear();
                    spRev.clear();
                    if (dirFwdButton.isSelected()) {
                        profileDir = ProfileDirection.FORWARD;
                    } else {
                        profileDir = ProfileDirection.REVERSE;
                    }
                    resetGraphButton.setEnabled(false);
                    profileGraphPane.repaint();
                    profileTimer.start();
                    log.info("Requesting throttle");
                    boolean requestOK = jmri.InstanceManager.throttleManagerInstance().requestThrottle(locomotiveAddress, this, true);
                    if (!requestOK) {
                        log.error("Loco Address in use, throttle request failed.");
                    }
                }
            }
        } else {
            // Must have a non-zero address
            //profileAddressField.setBackground(Color.RED);
            log.error("Attempt to profile loco address 0");
        }
    }

    /**
     * Profile timer timeout handler
     */
    protected synchronized void profileTimeout() {
        switch (profileState) {
            case WAIT_FOR_THROTTLE:
                tidyUp();
                log.error("Timeout waiting for throttle");
                statusLabel.setText(Bundle.getMessage("StatusTimeout"));
                break;
            case RUNNING:
                if (profileDir == ProfileDirection.FORWARD) {
                    spFwd.setPoint(profileStep, avSpeed);
                    statusLabel.setText(Bundle.getMessage("Fwd", profileStep));
                } else {
                    spRev.setPoint(profileStep, avSpeed);
                    statusLabel.setText(Bundle.getMessage("Rev", profileStep));
                }
                profileGraphPane.repaint();
                if (profileStep == 29) {
                    if ((profileDir == ProfileDirection.FORWARD)
                            && dirRevButton.isSelected()) {
                        // Start reverse profile
                        profileDir = ProfileDirection.REVERSE;
                        throttle.setIsForward(false);
                        profileStep = 0;
                        avClr();
                        statusLabel.setText(Bundle.getMessage("StatCreateRev"));
                    } else {
                        tidyUp();
                        statusLabel.setText(Bundle.getMessage("StatDone"));
                    }
                } else {
                    if (profileStep == 28) {
                        profileSpeed = 0.0F;
                    } else {
                        profileSpeed += throttleIncrement;
                    }
                    throttle.setSpeedSetting(profileSpeed);
                    profileStep += 1;
                    // adjust delay as we get faster and averaging is quicker
                    profileTimer.setDelay(7000 - range * 1000);
                }
                break;
            default:
                log.error("Unexpected profile timeout");
                profileTimer.stop();
                break;
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Speed Profiling and Speed Matching Cleanup">
    /**
     * Resets profiling and speed matching timers and other pertinent values and
     * releases the throttle and ops mode programmer
     * <p>
     * Called both when profiling or speed matching finish successfully or error
     * out
     */
    protected void tidyUp() {
        stopTimers();

        //turn off power
        //Turning power off is bad for some systems, e.g. Digitrax
//      try {
//          pm.setPower(PowerManager.OFF);
//      } catch (JmriException e) {
//          log.error("Exception during power off: "+e.toString());
//      }

        //release throttle
        if (throttle != null) {
            throttle.setSpeedSetting(0.0F);
            InstanceManager.throttleManagerInstance().releaseThrottle(throttle, this);
            throttle = null;
        }

        //release ops mode programmer
        if (ops_mode_prog != null) {
            InstanceManager.getDefault(AddressedProgrammerManager.class).releaseAddressedProgrammer(ops_mode_prog);
            ops_mode_prog = null;
        }

        //clean up speed matcher
        if (speedMatcher != null) {
            speedMatcher.CleanUp();
        }
        
        resetGraphButton.setEnabled(true);
        progState = ProgState.IDLE;
        profileState = ProfileState.IDLE;
        //TODO: TRW - I18N
        basicSpeedMatchStartStopButton.setText("Start Speed Match");
    }

    /**
     * Stops the profiling and speed matching processes. Called by pressing
     * either the stop profile or stop speed matching buttons.
     */
    protected synchronized void stopProfileAndSpeedMatch() {
        if (profileState != ProfileState.IDLE) {
            tidyUp();
            profileState = ProfileState.IDLE;
            log.info("Profiling stopped by user");
        }

        speedMatcher.StopSpeedMatch();
    }

    /**
     * Stops profile and speed match timers
     */
    protected void stopTimers() {
        if (profileTimer != null) {
            profileTimer.stop();
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Notifiers">
    /**
     * Called when a throttle is found
     *
     * @param t the requested DccThrottle
     */
    @Override
    public synchronized void notifyThrottleFound(DccThrottle t) {
        stopTimers();

        throttle = t;
        log.info("Throttle acquired");
        throttle.setSpeedStepMode(SpeedStepMode.NMRA_DCC_28);
        if (throttle.getSpeedStepMode() != SpeedStepMode.NMRA_DCC_28) {
            log.error("Failed to set 28 step mode");
            statusLabel.setText(Bundle.getMessage("ThrottleError28"));
            InstanceManager.throttleManagerInstance().releaseThrottle(throttle, this);
            return;
        }

        // turn on power
        try {
            pm.setPower(PowerManager.ON);
        } catch (JmriException e) {
            log.error("Exception during power on: {}", e.toString());
            return;
        }

        throttleIncrement = throttle.getSpeedIncrement();

        if (profileState == ProfileState.WAIT_FOR_THROTTLE) {
            log.info("Starting profiling");
            profileState = ProfileState.RUNNING;
            // Start at step 0 with 28 step packets
            profileSpeed = 0.0F;
            profileStep = 0;
            throttle.setSpeedSetting(profileSpeed);
            if (profileDir == ProfileDirection.FORWARD) {
                throttle.setIsForward(true);
                statusLabel.setText(Bundle.getMessage("StatCreateFwd"));
            } else {
                throttle.setIsForward(false);
                statusLabel.setText(Bundle.getMessage("StatCreateRev"));
            }
            // using profile timer to trigger each next step
            profileTimer.setRepeats(true);
            profileTimer.start();
        } else {
            tidyUp();
        }
    }

    /**
     * Called when a throttle could not be obtained
     *
     * @param address the requested address
     * @param reason  the reason the throttle could not be obtained
     */
    @Override
    public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
    }

    /**
     * Called when we must decide to steal the throttle for the requested
     * address. Since this is a an automatically stealing implementation, the
     * throttle will be automatically stolen.
     */
    @Override
    public void notifyDecisionRequired(jmri.LocoAddress address, DecisionType question) {
        InstanceManager.throttleManagerInstance().responseThrottleDecision(address, this, DecisionType.STEAL);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Other Timers">
    javax.swing.Timer replyTimer = null;
    javax.swing.Timer displayTimer = null;
    javax.swing.Timer fastDisplayTimer = null;

    /**
     * Starts the speedo hardware reply timer. Once we receive a speedoReply we
     * expect them regularly, at least once every 4 seconds.
     */
    protected void startReplyTimer() {
        replyTimer = new javax.swing.Timer(4000, e -> replyTimeout());
        replyTimer.setRepeats(true);     // refresh until stopped by dispose
        replyTimer.start();
    }

    /**
     * Starts the timer used to update the speedometer display speed.
     */
    protected void startDisplayTimer() {
        displayTimer = new javax.swing.Timer(DISPLAY_UPDATE, e -> displayTimeout());
        displayTimer.setRepeats(true);     // refresh until stopped by dispose
        displayTimer.start();
    }

    /**
     * Starts the timer used to update the speedometer display speed at a faster
     * rate.
     */
    protected void startFastDisplayTimer() {
        fastDisplayTimer = new javax.swing.Timer(DISPLAY_UPDATE / FAST_DISPLAY_RATIO, e -> fastDisplayTimeout());
        fastDisplayTimer.setRepeats(true);     // refresh until stopped by dispose
        fastDisplayTimer.start();
    }

    //<editor-fold defaultstate="collapsed" desc="Timer Timeout Handlers">
    /**
     * Internal routine to reset the speed on a timeout.
     */
    protected synchronized void replyTimeout() {
        //log.debug("Timed out - display speed zero");
        targetSpeed = 0;
        avClr();
        oldSpeed = 0;
        showSpeed();
    }

    /**
     * Internal routine to update the target speed for display
     */
    protected synchronized void displayTimeout() {
        //log.info("Display timeout");
        targetSpeed = avSpeed;
        incSpeed = (targetSpeed - currentSpeed) / FAST_DISPLAY_RATIO;
    }

    /**
     * Internal routine to update the displayed speed
     */
    protected synchronized void fastDisplayTimeout() {
        //log.info("Display timeout");
        if (Math.abs(targetSpeed - currentSpeed) < Math.abs(incSpeed)) {
            currentSpeed = targetSpeed;
        } else {

            currentSpeed += incSpeed;
        }
        if (currentSpeed < 0.01F) {
            currentSpeed = 0.0F;
        }
        
        showSpeed();
        
        if (speedMatcher != null) {
            speedMatcher.UpdateCurrentSpeed(currentSpeed);
        }
    }

    /**
     * Timeout requesting a throttle.
     */
    protected synchronized void throttleTimeout() {
        jmri.InstanceManager.throttleManagerInstance().cancelThrottleRequest(locomotiveAddress, this);
        profileState = ProfileState.IDLE;
        log.error("Timeout waiting for throttle");
    }

    //</editor-fold>
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Programming Functions">
    /**
     * Starts reading the address (CVs 29 then 1 (short) or 17 and 18 (long))
     * using the service mode programmer
     */
    protected void readAddress() {
        progState = ProgState.READ29;
        statusLabel.setText(Bundle.getMessage("ProgRd29"));
        startRead("29");
    }
    
    /**
     * Starts reading the momentum CVs (CV 3 and 4) using the service mode programmer
     */
    protected void readMomentum() {
        progState = ProgState.READ3;
        //TODO: TRW - I18N
        statusLabel.setText("Read Acceleration");
        startRead("3");
    }

    /**
     * Starts reading a CV using the service mode programmer
     *
     * @param cv the CV
     */
    protected void startRead(String cv) {
        try {
            prog.readCV(String.valueOf(cv), this);
        } catch (ProgrammerException e) {
            log.error("Exception reading CV {}", cv, e);
        }
    }

    /**
     * Called when the programmer (ops mode or service mode) has completed its
     * operation
     *
     * @param value  Value from a read operation, or value written on a write
     * @param status Denotes the completion code. Note that this is a bitwise
     *               combination of the various states codes defined in this
     *               interface. (see ProgListener.java for possible values)
     */
    @Override
    public void programmingOpReply(int value, int status) {
        if (status == 0) {
            switch (progState) {
                case IDLE:
                    log.debug("unexpected reply in IDLE state");
                    break;

                case READ29:
                    // Check extended address bit
                    if ((value & 0x20) == 0) {
                        progState = ProgState.READ1;
                        statusLabel.setText(Bundle.getMessage("ProgRdShort"));
                        startRead("1");
                    } else {
                        progState = ProgState.READ17;
                        statusLabel.setText(Bundle.getMessage("ProgRdExtended"));
                        startRead("17");
                    }
                    break;

                case READ1:
                    readAddress = value;
                    //TODO: TRW - why is this commented out?
                    //profileAddressField.setText(Integer.toString(profileAddress));
                    //profileAddressField.setBackground(Color.WHITE);
                    addrSelector.setAddress(new DccLocoAddress(readAddress, false));
                    changeOfAddress();
                    progState = ProgState.IDLE;
                    break;
                    
                case READ3:
                    accelerationSM.setValue(value);
                    progState = ProgState.READ4;
                    //TODO: TRW - I18N
                    statusLabel.setText("Read deceleration");
                    startRead("4");
                    break;

                case READ4:
                    decelerationSM.setValue(value);
                    progState = ProgState.IDLE;
                    statusLabel.setText(Bundle.getMessage("ProgRdComplete"));
                    break;

                case READ17:
                    readAddress = value;
                    progState = ProgState.READ18;
                    startRead("18");
                    break;

                case READ18:
                    readAddress = (readAddress & 0x3f) * 256 + value;
                    //profileAddressField.setText(Integer.toString(profileAddress));
                    //profileAddressField.setBackground(Color.WHITE);
                    addrSelector.setAddress(new DccLocoAddress(readAddress, true));
                    changeOfAddress();
                    statusLabel.setText(Bundle.getMessage("ProgRdComplete"));
                    progState = ProgState.IDLE;
                    break;

                default:
                    progState = ProgState.IDLE;
                    log.warn("Unhandled read state: {}", progState);
                    break;
            }
        } else {
            // Error during programming
            log.error("Status not OK during {}: {}", progState.toString(), status);
            //profileAddressField.setText("Error");
            statusLabel.setText(Bundle.getMessage("ProgError"));
            progState = ProgState.IDLE;
            tidyUp();
        }
    }
    //</editor-fold>
    //debugging logger
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SpeedoConsoleFrame.class);

}
