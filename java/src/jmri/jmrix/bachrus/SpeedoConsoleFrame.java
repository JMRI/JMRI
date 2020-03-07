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
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
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
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//</editor-fold>
/**
 * Frame for Speedo Console for Bachrus running stand reader interface
 *
 * @author Andrew Crosland Copyright (C) 2010
 * @author Dennis Miller Copyright (C) 2015
 * @author Todd Wegter Copyright (C) 2019
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

    protected enum SpeedMatchState {
        IDLE,
        WAIT_FOR_THROTTLE,
        SETUP,
        FORWARD_WARM_UP,
        REVERSE_WARM_UP,
        FORWARD_SPEED_MATCH_STEP_1,
        FORWARD_SPEED_MATCH_STEP_28,
        REVERSE_SPEED_MATCH_TRIM,
        RESTORE_MOMENTUM
    }

    protected enum SpeedMatchSetupState {
        IDLE,
        MOMENTUM_ACCEL_READ,
        MOMENTUM_DECEL_READ,
        MOMENTUM_ACCEL_WRITE,
        MOMENTUM_DECEL_WRITE,
        VSTART,
        VHIGH,
        FORWARD_TRIM,
        REVERSE_TRIM,
        BEGIN_SPEED_MATCH
    }

    protected enum ProgState {
        IDLE,
        READ1,
        READ3,
        READ4,
        READ17,
        READ18,
        READ29,
        WRITE2,
        WRITE3,
        WRITE4,
        WRITE5,
        WRITE6,
        WRITE66,
        WRITE95
    }
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
    protected JRadioButton dirFwdButton = new JRadioButton(Bundle.getMessage("Forward"));
    protected JRadioButton dirRevButton = new JRadioButton(Bundle.getMessage("Reverse"));
    protected JRadioButton toggleGridButton = new JRadioButton(Bundle.getMessage("ToggleGrid"));

    GraphPane profileGraphPane;

    protected JLabel statusLabel = new JLabel(" ");

    protected javax.swing.JLabel readerLabel = new javax.swing.JLabel();
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="General Member Variables">
    protected static final int defaultScale = 8;

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
    static final int[] filterLength = {0, 3, 6, 10, 20};

    String selectedScalePref = this.getClass().getName() + ".SelectedScale"; // NOI18N
    String customScalePref = this.getClass().getName() + ".CustomScale"; // NOI18N
    String speedUnitsKphPref = this.getClass().getName() + ".SpeedUnitsKph"; // NOI18N
    String dialTypePref = this.getClass().getName() + ".DialType"; // NOI18N
    jmri.UserPreferencesManager prefs;

    // members for handling the Speedo interface
    SpeedoTrafficController tc = null;
    String replyString;

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
    protected JLabel speedStep1TargetLabel = new JLabel(Bundle.getMessage("lblSpeedStep1"));
    protected JTextField speedStep1TargetField = new JTextField("3", 3);
    protected JLabel speedStep1TargetUnit = new JLabel(Bundle.getMessage("lblMPH"));
    protected JLabel speedStep28TargetLabel = new JLabel(Bundle.getMessage("lblSpeedStep28"));
    protected JTextField speedStep28TargetField = new JTextField("55", 3);
    protected JLabel speedStep28TargetUnit = new JLabel(Bundle.getMessage("lblMPH"));
    protected JCheckBox speedMatchWarmUpCheckBox = new JCheckBox(Bundle.getMessage("chkbxWarmUp"));
    protected JButton speedMatchButton = new JButton(Bundle.getMessage("btnStartSpeedMatch"));
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Speed Matching Memeber Variables">
    //PID Controller Values
    protected static float kP = 0.75f;
    protected static float kI = 0.3f;
    protected static float kD = 0.4f;
    protected float speedMatchIntegral = 0;
    protected float speedMatchDerivative = 0;
    protected float lastSpeedMatchError = 0;
    protected float speedMatchError = 0;
    protected float speedStep1Target = 0;
    protected float speedStep28Target = 0;
    protected int lastVStart = 1;
    protected int lastVHigh = 255;
    protected int lastReverseTrim = 128;
    protected int vStart = 1;
    protected int vHigh = 255;
    protected int reverseTrim = 128;

    protected int speedMatchDuration = 0;

    protected int oldMomentumAccel;
    protected int oldMomentumDecel;

    protected SpeedMatchState speedMatchState = SpeedMatchState.IDLE;
    protected SpeedMatchSetupState speedMatchSetupState = SpeedMatchSetupState.IDLE;
    //</editor-fold>
    //</editor-fold>
    // For testing only, must be 1 for normal use
    protected static final int speedTestScaleFactor = 1;

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
        String annotate = "Bachrus MTS-DCC " + Bundle.getMessage("ProfileFor") + " "
                + locomotiveAddress.getNumber() + " " + Bundle.getMessage("CreatedOn")
                + " " + result;
        printTitleText.setText(annotate);
    }

    /**
     * Override for the JmriJFrame's dispose function
     */
    @Override
    public void dispose() {
        if(prefs!=null) {
           prefs.setComboBoxLastSelection(selectedScalePref, (String)scaleList.getSelectedItem());
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
                scaleList.setSelectedIndex(defaultScale);
            }
        } else {
            scaleList.setSelectedIndex(defaultScale);
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
                statusLabel.setText(Bundle.getMessage("StatProg"));
            }
        });

        mainButton.addActionListener(e -> {
            // no programmer available to read back CVs
            readAddressButton.setEnabled(false);
            statusLabel.setText(Bundle.getMessage("StatMain"));
        });

        basicPane.add(modePanel);

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
        //<editor-fold defaultstate="collapsed" desc="Speed Profiling and Speed Matching Panels">
        /*
         * Pane for profiling loco speed curve
         */
        JPanel profilePane = new JPanel();
        profilePane.setLayout(new BorderLayout());

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

        profilePane.add(addrPane, BorderLayout.NORTH);

        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Graph and Buttons Panel">
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

        // pane to hold the title
        JPanel profileTitlePane = new JPanel();
        profileTitlePane.setLayout(new BoxLayout(profileTitlePane, BoxLayout.X_AXIS));
        //JTextArea profileTitle = new JTextArea("Title: ");
        //profileTitlePane.add(profileTitle);
        printTitleText.setToolTipText(Bundle.getMessage("TTPrintTitle"));
        printTitleText.setText(Bundle.getMessage("TTText1"));
        profileTitlePane.add(printTitleText);

        // pane to wrap buttons and title
        JPanel profileSouthPane = new JPanel();
        profileSouthPane.setLayout(new BoxLayout(profileSouthPane, BoxLayout.Y_AXIS));
        profileSouthPane.add(profileButtonPane);

        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Speed Matching Panel">
        // pane for speed matching
        speedStep1TargetField.setHorizontalAlignment(JTextField.RIGHT);
        speedStep1TargetUnit.setPreferredSize(new Dimension(35, 16));
        speedStep28TargetField.setHorizontalAlignment(JTextField.RIGHT);
        speedStep28TargetUnit.setPreferredSize(new Dimension(35, 16));
        speedMatchWarmUpCheckBox.setSelected(true);
        JPanel speedMatchPane = new JPanel();
        speedMatchPane.setLayout(new FlowLayout());
        speedMatchPane.add(speedStep1TargetLabel);
        speedMatchPane.add(speedStep1TargetField);
        speedMatchPane.add(speedStep1TargetUnit);
        speedMatchPane.add(speedStep28TargetLabel);
        speedMatchPane.add(speedStep28TargetField);
        speedMatchPane.add(speedStep28TargetUnit);
        speedMatchPane.add(speedMatchWarmUpCheckBox);
        speedMatchPane.add(speedMatchButton);
        profileSouthPane.add(speedMatchPane);

        profileSouthPane.add(profileTitlePane);

        profilePane.add(profileSouthPane, BorderLayout.SOUTH);

        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Control Panel">
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

        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Speed Profiling and Speed Matching Button Handlers">
        // Listen to track Power button
        trackPowerButton.addActionListener(e -> trackPower());

        // Listen to start profile button
        startProfileButton.addActionListener(e -> {
            getCustomScale();
            startProfile();
        });

        // Listen to stop profile button
        stopProfileButton.addActionListener(e -> stopProfileAndSpeedMatch());

        // Listen to speed match button
        speedMatchButton.addActionListener(e -> {
            if ((speedMatchState == SpeedMatchState.IDLE) && (profileState == ProfileState.IDLE)) {
                getCustomScale();
                speedStep1Target = Integer.parseInt(speedStep1TargetField.getText());
                speedStep28Target = Integer.parseInt(speedStep28TargetField.getText());

                if (mphButton.isSelected()) {
                    speedStep1Target = Speed.mphToKph(speedStep1Target);
                    speedStep28Target = Speed.mphToKph(speedStep28Target);
                }

                startSpeedMatch();
            } else {
                stopProfileAndSpeedMatch();
            }
        });

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
        //</editor-fold>
        /*
         * Create the tabbed pane and add the panes
         */
        JPanel tabbedPane = new JPanel();
        tabbedPane.setLayout(new BoxLayout(tabbedPane, BoxLayout.X_AXIS));
        // make basic panel
        tabbedPane.add(basicPane);

        if (((dccServices & THROTTLE) == THROTTLE)
                || ((dccServices & COMMAND) == COMMAND)) {
            tabbedPane.add(profilePane);
        } else {
            log.info(Bundle.getMessage("StatNoDCC"));
            statusLabel.setText(Bundle.getMessage("StatNoDCC"));
        }

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.bachrus.SpeedoConsoleFrame", true);

        // Create a wrapper with a status line and add the main content
        JPanel statusWrapper = new JPanel();
        statusWrapper.setLayout(new BorderLayout());
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);

        statusPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        statusWrapper.add(tabbedPane, BorderLayout.CENTER);
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
        if (series > 0) {
            // Scale the data and calculate kph
            try {
                freq = 1500000 / count;
                sampleSpeed = (freq / 24) * circ * thisScale * 3600 / 1000000 * speedTestScaleFactor;
            } catch (ArithmeticException ae) {
                log.error("Exception calculating sampleSpeed " + ae);
            }
            avFn(sampleSpeed);
            log.debug("New sample: " + sampleSpeed + " Average: " + avSpeed);
            log.debug("Acc: " + acc + " range: " + range);
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
        avSpeed = acc / filterLength[range];
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
                    acc = acc * filterLength[2] / filterLength[1];
                }
                break;
            case 2:
                if (sampleSpeed < RANGE2LO) {
                    range--;
                    acc = acc * filterLength[1] / filterLength[2];
                } else if (sampleSpeed > RANGE2HI) {
                    range++;
                    acc = acc * filterLength[3] / filterLength[2];
                }
                break;
            case 3:
                if (sampleSpeed < RANGE3LO) {
                    range--;
                    acc = acc * filterLength[2] / filterLength[3];
                } else if (sampleSpeed > RANGE3HI) {
                    range++;
                    acc = acc * filterLength[4] / filterLength[3];
                }
                break;
            case 4:
                if (sampleSpeed < RANGE4LO) {
                    range--;
                    acc = acc * filterLength[3] / filterLength[4];
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
                log.error("Calculated speed out of range: " + currentSpeed);
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
        if (mphButton.isSelected()) {
            profileGraphPane.setUnitsMph();
            speedStep1TargetUnit.setText(Bundle.getMessage("lblMPH"));
            speedStep28TargetUnit.setText(Bundle.getMessage("lblMPH"));
        } else {
            profileGraphPane.setUnitsKph();
            speedStep1TargetUnit.setText(Bundle.getMessage("lblKPH"));
            speedStep28TargetUnit.setText(Bundle.getMessage("lblKPH"));
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
                JOptionPane.showMessageDialog(null, Bundle.getMessage("CustomScaleDialog"),
                        Bundle.getMessage("CustomScaleTitle"), JOptionPane.ERROR_MESSAGE);
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
     * 
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
        try {
            if (pm.getPower() == PowerManager.ON) {
                trackPowerButton.setText(Bundle.getMessage("PowerDown"));
                //statusLabel.setText(Bundle.getMessage("StatTOn"));
            } else if (pm.getPower() == PowerManager.OFF) {
                trackPowerButton.setText(Bundle.getMessage("PowerUp"));
                //statusLabel.setText(Bundle.getMessage("StatTOff"));
            }
        } catch (JmriException ex) {
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
            log.error("Exception during power on: " + e.toString());
        }
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Speed Matching">
    javax.swing.Timer speedMatchTimer = null;

    /**
     * Sets up the speed match timer by setting the throttle direction and
     * speed, clearing the speed match error, and setting the timer initial
     * delay (timer does not auto-repeat for accuracy)
     *
     * @param isForward    - throttle direction - true for forward, false for
     *                     reverse
     * @param speedStep    - throttle speed step
     * @param initialDelay - initial delay for the timer in milliseconds
     */
    protected void setupSpeedMatchTimer(boolean isForward, int speedStep, int initialDelay) {
        throttle.setIsForward(isForward);
        throttle.setSpeedSetting(speedStep * throttleIncrement);
        speedMatchError = 0;
        speedMatchTimer.setInitialDelay(initialDelay);
    }

    /**
     * Sets the PID controller's speed match error for speed matching
     *
     * @param speedTarget - target speed in KPH
     */
    protected void setSpeedMatchError(float speedTarget) {
        speedMatchError = speedTarget - currentSpeed;
    }

    /**
     * Gets the next value to try for speed matching using a PID controller
     *
     * @param lastValue - the last vStart or vHigh value tried
     * @return the next value to try for speed matching (1-255 inclusive)
     */
    protected int getNextSpeedMatchValue(int lastValue) {
        speedMatchIntegral += speedMatchError;
        speedMatchDerivative = speedMatchError - lastSpeedMatchError;

        int value = (lastValue + Math.round((kP * speedMatchError) + (kI * speedMatchIntegral) + (kD * speedMatchDerivative)));

        if (value > 255) {
            value = 255;
        } else if (value < 1) {
            value = 1;
        }

        return value;
    }

    /**
     * Starts the auto speed matching process
     */
    protected void startSpeedMatch() {
        DccLocoAddress dccLocoAddress = addrSelector.getAddress();

        //Validate require variables
        if (speedStep1Target < 1) {
            statusLabel.setText(Bundle.getMessage("StatInvalidSpeedStep1"));
            log.error("Attempt to speed match to invalid speed step 1 target speed");
            return;
        }
        if (speedStep28Target <= speedStep1Target) {
            statusLabel.setText(Bundle.getMessage("StatInvalidSpeedStep28"));
            log.error("Attempt to speed match to invalid speed step 28 target speed");
            return;
        }
        if (locomotiveAddress.getNumber() <= 0) {
            statusLabel.setText(Bundle.getMessage("StatInvalidDCCAddress"));
            log.error("Attempt to speed match loco address 0");
            return;
        }

        //start speed matching
        if ((speedMatchState == SpeedMatchState.IDLE) && (profileState == ProfileState.IDLE)) {
            speedMatchState = SpeedMatchState.WAIT_FOR_THROTTLE;
            speedMatchButton.setText(Bundle.getMessage("btnStopSpeedMatch"));

            //reset member variables
            vStart = 1;
            vHigh = 255;
            reverseTrim = 128;
            lastVStart = vStart;
            lastVHigh = vHigh;
            lastReverseTrim = reverseTrim;

            //get OPS MODE Programmer
            if (InstanceManager.getNullableDefault(AddressedProgrammerManager.class) != null) {
                if (InstanceManager.getDefault(AddressedProgrammerManager.class).isAddressedModePossible(dccLocoAddress)) {
                    ops_mode_prog = InstanceManager.getDefault(AddressedProgrammerManager.class).getAddressedProgrammer(dccLocoAddress);
                }
            }

            //start speed match timer
            speedMatchTimer = new javax.swing.Timer(4000, e -> speedMatchTimeout());
            speedMatchTimer.setRepeats(false); //timer is used without repeats to improve time accuracy when changing the delay

            //request a throttle
            statusLabel.setText(Bundle.getMessage("StatReqThrottle"));
            speedMatchTimer.start();
            log.info("Requesting Throttle");
            boolean requestOK = InstanceManager.throttleManagerInstance().requestThrottle(locomotiveAddress, this, true);
            if (!requestOK) {
                log.error("Loco Address in use, throttle request failed.");
                statusLabel.setText(Bundle.getMessage("StatAddressInUse"));
            }
        }
    }

    /**
     * Timer timeout handler for the speed match timer
     */
    protected synchronized void speedMatchTimeout() {
        switch (speedMatchState) {
            case WAIT_FOR_THROTTLE:
                tidyUp();
                log.error("Timeout waiting for throttle");
                statusLabel.setText(Bundle.getMessage("StatusTimeout"));
                break;

            case SETUP:
                //setup the decoder for speed matching
                switch (speedMatchSetupState) {
                    case MOMENTUM_ACCEL_READ:
                        //grab the current acceleration momentum value for later restoration (CV 3)
                        if (progState == ProgState.IDLE) {
                            readMomentumAccel();
                            speedMatchSetupState = SpeedMatchSetupState.MOMENTUM_DECEL_READ;
                        }
                        break;

                    case MOMENTUM_DECEL_READ:
                        //grab the current deceleration momentum value for later restoration (CV 4)
                        if (progState == ProgState.IDLE) {
                            readMomentumDecel();
                            speedMatchSetupState = SpeedMatchSetupState.MOMENTUM_ACCEL_WRITE;
                        }
                        break;

                    case MOMENTUM_ACCEL_WRITE:
                        //set acceleration momentum to 0 (CV 3)
                        if (progState == ProgState.IDLE) {
                            writeMomentumAccel(0);
                            speedMatchSetupState = SpeedMatchSetupState.MOMENTUM_DECEL_WRITE;
                            speedMatchTimer.setInitialDelay(5000);
                        }
                        break;

                    case MOMENTUM_DECEL_WRITE:
                        //set deceleration mementum to 0 (CV 4)
                        if (progState == ProgState.IDLE) {
                            writeMomentumDecel(0);
                            speedMatchSetupState = SpeedMatchSetupState.VSTART;
                            speedMatchTimer.setInitialDelay(1500);
                        }
                        break;

                    case VSTART:
                        //set vStart to 1 (CV 2 - also sets vMid CV 6 to halway between vStart and vHigh)
                        if (progState == ProgState.IDLE) {
                            writeVStart();
                            speedMatchSetupState = SpeedMatchSetupState.VHIGH;
                        }
                        break;

                    case VHIGH:
                        //set vHigh to 255 (CV 5 - also sets vMid CV 6 to halway between vStart and vHigh)
                        if (progState == ProgState.IDLE) {
                            writeVHigh();
                            speedMatchSetupState = SpeedMatchSetupState.FORWARD_TRIM;
                        }
                        break;

                    case FORWARD_TRIM:
                        //set forward trim to 128 (CV 66)
                        if (progState == ProgState.IDLE) {
                            writeForwardTrim(128);
                            speedMatchSetupState = SpeedMatchSetupState.REVERSE_TRIM;
                        }
                        break;

                    case REVERSE_TRIM:
                        //set revers trim to 128 (CV 95)
                        if (progState == ProgState.IDLE) {
                            writeReverseTrim(128);
                            speedMatchSetupState = SpeedMatchSetupState.BEGIN_SPEED_MATCH;
                        }
                        break;

                    case BEGIN_SPEED_MATCH:
                        //start warming up or speed matching
                        if (progState == ProgState.IDLE) {
                            speedMatchSetupState = SpeedMatchSetupState.IDLE;
                            if (speedMatchWarmUpCheckBox.isSelected()) {
                                speedMatchState = SpeedMatchState.FORWARD_WARM_UP;
                            } else {
                                speedMatchState = SpeedMatchState.FORWARD_SPEED_MATCH_STEP_1;
                            }
                            setupSpeedMatchTimer(true, 0, 5000);
                            speedMatchDuration = 0;
                        }
                        break;

                    default:
                        log.warn("Unhandled speed match setup state: {}", speedMatchSetupState);
                        break;
                }
                break;

            case FORWARD_WARM_UP:
                //Run 4 minutes at high speed forward
                statusLabel.setText(Bundle.getMessage("StatForwardWarmUp", 240 - speedMatchDuration));

                if (speedMatchDuration >= 240) {
                    speedMatchState = SpeedMatchState.FORWARD_SPEED_MATCH_STEP_1;
                    setupSpeedMatchTimer(true, 0, 5000);
                    speedMatchDuration = 0;
                    speedMatchTimer.start();
                } else {
                    setupSpeedMatchTimer(true, 28, 5000);
                    speedMatchDuration += 5;
                }
                break;

            case FORWARD_SPEED_MATCH_STEP_1:
                //Use PID Controller to adjust vStart (and VMid) to achieve desired speed
                if (progState == ProgState.IDLE) {
                    if (speedMatchDuration == 0) {
                        statusLabel.setText(Bundle.getMessage("StatSettingSpeedStep1"));
                        setupSpeedMatchTimer(true, 1, 15000);
                        speedMatchDuration = 1;
                    } else {
                        setSpeedMatchError(speedStep1Target);

                        if ((speedMatchError < 0.5) && (speedMatchError > -0.5)) {
                            speedMatchState = SpeedMatchState.FORWARD_SPEED_MATCH_STEP_28;
                            setupSpeedMatchTimer(true, 0, 8000);
                            speedMatchDuration = 0;
                        } else {
                            vStart = getNextSpeedMatchValue(lastVStart);

                            if (((lastVStart == 1) || (lastVStart == 255)) && (vStart == lastVStart)) {
                                statusLabel.setText(Bundle.getMessage("StatSetSpeedStep1Fail"));
                                log.debug("Unable to achieve desired speed at Speed Step 1");
                                tidyUp();
                            } else {
                                lastVStart = vStart;
                                writeVStart();
                            }
                            speedMatchTimer.setInitialDelay(8000);
                        }
                    }
                }
                break;

            case FORWARD_SPEED_MATCH_STEP_28:
                //Use PID Controller llogic to adjust vHigh (and vMid) to achieve desired speed
                if (progState == ProgState.IDLE) {
                    if (speedMatchDuration == 0) {
                        statusLabel.setText(Bundle.getMessage("StatSettingSpeedStep28"));
                        setupSpeedMatchTimer(true, 28, 15000);
                        speedMatchDuration = 1;
                    } else {
                        setSpeedMatchError(speedStep28Target);

                        if ((speedMatchError < 0.5) && (speedMatchError > -0.5)) {
                            if (speedMatchWarmUpCheckBox.isSelected()) {
                                speedMatchState = SpeedMatchState.REVERSE_WARM_UP;
                            } else {
                                speedMatchState = SpeedMatchState.REVERSE_SPEED_MATCH_TRIM;
                            }
                            setupSpeedMatchTimer(false, 0, 5000);
                            speedMatchDuration = 0;
                        } else {
                            vHigh = getNextSpeedMatchValue(lastVHigh);

                            if (((lastVHigh == 1) || (lastVHigh == 255)) && (vHigh == lastVHigh)) {
                                statusLabel.setText(Bundle.getMessage("StatSetSpeedStep28Fail"));
                                log.debug("Unable to achieve desired speed at Speed Step 28");
                                tidyUp();
                            } else {
                                lastVHigh = vHigh;
                                writeVHigh();
                            }
                            speedMatchTimer.setInitialDelay(8000);
                        }
                    }
                }
                break;

            case REVERSE_WARM_UP:
                //Run 2 minutes at high speed reverse
                statusLabel.setText(Bundle.getMessage("StatReverseWarmUp", 120 - speedMatchDuration));

                if (speedMatchDuration >= 120) {
                    speedMatchState = SpeedMatchState.REVERSE_SPEED_MATCH_TRIM;
                } else {
                    speedMatchDuration += 5;
                }
                setupSpeedMatchTimer(false, 28, 5000);
                break;

            case REVERSE_SPEED_MATCH_TRIM:
                //Use PID controller logic to adjust reverse trim until high speed reverse speed matches forward
                if (progState == ProgState.IDLE) {
                    if (speedMatchDuration == 0) {
                        statusLabel.setText(Bundle.getMessage("StatSettingReverseTrim"));
                        setupSpeedMatchTimer(false, 28, 15000);
                        speedMatchDuration = 1;
                    } else {
                        setSpeedMatchError(speedStep28Target);

                        if ((speedMatchError < 0.5) && (speedMatchError > -0.5)) {
                            speedMatchState = SpeedMatchState.RESTORE_MOMENTUM;
                            speedMatchSetupState = SpeedMatchSetupState.MOMENTUM_ACCEL_WRITE;
                            setupSpeedMatchTimer(false, 0, 1500);
                            speedMatchDuration = 0;
                        } else {
                            reverseTrim = getNextSpeedMatchValue(lastReverseTrim);

                            if (((lastReverseTrim == 1) || (lastReverseTrim == 255)) && (reverseTrim == lastReverseTrim)) {
                                statusLabel.setText(Bundle.getMessage("StatSetReverseTripFail"));
                                log.debug("Unable to trim reverse to match forward");
                                tidyUp();
                            } else {
                                lastReverseTrim = reverseTrim;
                                writeReverseTrim(reverseTrim);
                            }
                            speedMatchTimer.setInitialDelay(8000);
                        }
                    }
                }
                break;

            case RESTORE_MOMENTUM:
                //restore momentum CVs
                switch (speedMatchSetupState) {
                    case MOMENTUM_ACCEL_WRITE:
                        //restore acceleration momentum (CV 3)
                        if (progState == ProgState.IDLE) {
                            writeMomentumAccel(oldMomentumAccel);
                            speedMatchSetupState = SpeedMatchSetupState.MOMENTUM_DECEL_WRITE;
                        }
                        break;

                    case MOMENTUM_DECEL_WRITE:
                        //restore deceleration mumentum (CV 4)
                        if (progState == ProgState.IDLE) {
                            writeMomentumDecel(oldMomentumDecel);
                            speedMatchSetupState = SpeedMatchSetupState.IDLE;
                        }
                        break;

                    case IDLE:
                        //wrap everything up
                        if (progState == ProgState.IDLE) {
                            tidyUp();
                            statusLabel.setText(Bundle.getMessage("StatSpeedMatchComplete"));
                        }
                        break;

                    default:
                        log.warn("Unhandled speed match cleanup state: {}", speedMatchSetupState);
                }
                break;

            default:
                tidyUp();
                log.error("Unexpected speed match timeout");
                break;
        }

        if (speedMatchState != SpeedMatchState.IDLE) {
            speedMatchTimer.start();
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
                if ((speedMatchState == SpeedMatchState.IDLE) && (profileState == ProfileState.IDLE)) {
                    profileTimer = new javax.swing.Timer(4000, e -> profileTimeout());
                    profileTimer.setRepeats(false);
                    // Request a throttle
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
        if (profileState == ProfileState.WAIT_FOR_THROTTLE) {
            tidyUp();
            log.error("Timeout waiting for throttle");
            statusLabel.setText(Bundle.getMessage("StatusTimeout"));
        } else if (profileState == ProfileState.RUNNING) {
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
        } else {
            log.error("Unexpected profile timeout");
            profileTimer.stop();
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
            //throttle.release();
            throttle = null;
        }

        //release ops mode programmer
        if (ops_mode_prog != null) {
            InstanceManager.getDefault(AddressedProgrammerManager.class).releaseAddressedProgrammer(ops_mode_prog);
            ops_mode_prog = null;
        }

        resetGraphButton.setEnabled(true);
        progState = ProgState.IDLE;
        profileState = ProfileState.IDLE;
        speedMatchState = SpeedMatchState.IDLE;
        speedMatchSetupState = SpeedMatchSetupState.IDLE;
        speedMatchButton.setText(Bundle.getMessage("btnStartSpeedMatch"));
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

        if (speedMatchState != SpeedMatchState.IDLE) {
            tidyUp();
            speedMatchState = SpeedMatchState.IDLE;
            statusLabel.setText(" ");
            log.info("Speed matching stopped by user");
        }
    }

    /**
     * Stops profile and speed match timers
     */
    protected void stopTimers() {
        if (profileTimer != null) {
            profileTimer.stop();
        }
        if (speedMatchTimer != null) {
            speedMatchTimer.stop();
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
            //throttle.release();
            return;
        }

        // turn on power
        try {
            pm.setPower(PowerManager.ON);
        } catch (JmriException e) {
            log.error("Exception during power on: " + e.toString());
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
        } else if (speedMatchState == SpeedMatchState.WAIT_FOR_THROTTLE) {
            log.info("Starting speed matching");

            // using speed matching timer to trigger each phase of speed matching
            speedMatchState = SpeedMatchState.SETUP;
            speedMatchSetupState = SpeedMatchSetupState.MOMENTUM_ACCEL_READ;
            speedMatchTimer.setInitialDelay(1500);
            speedMatchTimer.start();
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
     * Called when a throttle must be stolen for the requested address. Since this is a 
     * an automatically stealing implementation, the throttle will be automatically stolen.
     * {@inheritDoc}
     * @deprecated since 4.15.7; use #notifyDecisionRequired
     */
    @Override
    @Deprecated
    public void notifyStealThrottleRequired(jmri.LocoAddress address) {
        InstanceManager.throttleManagerInstance().responseThrottleDecision(address, this, DecisionType.STEAL );
    }

    /**
     * Called when we must decide to steal the throttle for the requested address. Since this is a 
     * an automatically stealing implementation, the throttle will be automatically stolen.
     */
    @Override
    public void notifyDecisionRequired(jmri.LocoAddress address, DecisionType question) {
      InstanceManager.throttleManagerInstance().responseThrottleDecision(address, this, DecisionType.STEAL );
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
    }

    /**
     * Timeout requesting a throttle.
     */
    protected synchronized void throttleTimeout() {
        jmri.InstanceManager.throttleManagerInstance().cancelThrottleRequest(locomotiveAddress, this);
        profileState = ProfileState.IDLE;
        speedMatchState = SpeedMatchState.IDLE;
        log.error("Timeout waiting for throttle");
    }

    //</editor-fold>
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Programming Functions">
    /**
     * Starts writing acceleration momentum (CV 3) using the ops mode programmer
     *
     * @param value acceleration value (0-255 inclusive)
     */
    protected synchronized void writeMomentumAccel(int value) {
        progState = ProgState.WRITE3;
        statusLabel.setText(Bundle.getMessage("ProgSetAccel", value));
        startOpsModeWrite("3", value);
    }

    /**
     * Starts writing deceleration momentum (CV 4) using the ops mode programmer
     *
     * @param value deceleration value (0-255 inclusive)
     */
    protected synchronized void writeMomentumDecel(int value) {
        progState = ProgState.WRITE4;
        statusLabel.setText(Bundle.getMessage("ProgSetDecel", value));
        startOpsModeWrite("4", value);
    }

    /**
     * Starts writing vStart to vStart (CV 2) using the ops mode programmer
     */
    protected synchronized void writeVStart() {
        progState = ProgState.WRITE2;
        statusLabel.setText(Bundle.getMessage("ProgSetVStart", vStart));
        startOpsModeWrite("2", vStart);
    }

    /**
     * Starts writing the average of vStart and vHigh to vMid (CV 6) using the
     * ops mode programmer
     */
    protected synchronized void writeVMid() {
        int vMid = ((vStart + vHigh) / 2);
        progState = ProgState.WRITE6;
        //statusLabel.setText(Bundle.getMessage("ProgSetVMid", vMid));
        startOpsModeWrite("6", vMid);
    }

    /**
     * Starts writing vHigh to vHigh (CV 5) using the ops mode programmer
     */
    protected synchronized void writeVHigh() {
        progState = ProgState.WRITE5;
        statusLabel.setText(Bundle.getMessage("ProgSetVHigh", vHigh));
        startOpsModeWrite("5", vHigh);
    }

    /**
     * Starts writing forward trim (CV 66) using the ops mode programmer
     *
     * @param value forward trim value (0-255 inclusive)
     */
    protected synchronized void writeForwardTrim(int value) {
        progState = ProgState.WRITE66;
        statusLabel.setText(Bundle.getMessage("ProgSetForwardTrim", value));
        startOpsModeWrite("66", value);
    }

    /**
     * Starts writing reverse trim (CV 95) using the ops mode programmer
     *
     * @param value reverse trim value (0-255 inclusive)
     */
    protected synchronized void writeReverseTrim(int value) {
        progState = ProgState.WRITE95;
        statusLabel.setText(Bundle.getMessage("ProgSetReverseTrim", value));
        startOpsModeWrite("95", value);
    }

    /**
     * Starts reading the acceleration momentum (CV 3) using the service mode
     * programmer
     */
    protected void readMomentumAccel() {
        progState = ProgState.READ3;
        statusLabel.setText(Bundle.getMessage("ProgReadAccel"));
        startRead("3");
    }

    /**
     * Starts reading the deceleration momentum (CV 4) using the service mode
     * programmer
     */
    protected void readMomentumDecel() {
        progState = ProgState.READ4;
        statusLabel.setText(Bundle.getMessage("ProgReadDecel"));
        startRead("4");
    }

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
     * Starts writing a CV using the ops mode programmer
     *
     * @param cv    the CV
     * @param value the value to write to the CV (0-255 inclusive)
     */
    protected void startOpsModeWrite(String cv, int value) {
        try {
            ops_mode_prog.writeCV(cv, value, this);
        } catch (ProgrammerException e) {
            log.error("Exception writing CV " + cv + " " + e);
        }
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
            log.error("Exception reading CV " + cv + " " + e);
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
                    //profileAddressField.setText(Integer.toString(profileAddress));
                    //profileAddressField.setBackground(Color.WHITE);
                    addrSelector.setAddress(new DccLocoAddress(readAddress, false));
                    changeOfAddress();
                    progState = ProgState.IDLE;
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

                case READ3:
                    oldMomentumAccel = value;
                    progState = ProgState.IDLE;
                    break;

                case READ4:
                    oldMomentumDecel = value;
                    progState = ProgState.IDLE;
                    break;

                case WRITE3:
                case WRITE4:
                case WRITE6:
                case WRITE66:
                case WRITE95:
                    progState = ProgState.IDLE;
                    break;

                // when writing vStart or vHigh, also write vMid
                case WRITE2:
                case WRITE5:
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                    }
                    writeVMid();
                    break;

                default:
                    progState = ProgState.IDLE;
                    log.warn("Unhandled read state: {}", progState);
                    break;
            }
        } else {
            // Error during programming
            log.error("Status not OK during " + progState.toString() + ": " + status);
            //profileAddressField.setText("Error");
            statusLabel.setText(Bundle.getMessage("ProgError"));
            progState = ProgState.IDLE;
            tidyUp();
        }
    }
    //</editor-fold>
    //debugging logger
    private final static Logger log = LoggerFactory.getLogger(SpeedoConsoleFrame.class);

}
