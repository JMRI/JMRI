package jmri.jmrix.bachrus;

//<editor-fold defaultstate="collapsed" desc="Imports">
import jmri.jmrix.bachrus.speedmatcher.basic.BasicSpeedMatcherFactory;

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
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

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
import jmri.jmrix.bachrus.speedmatcher.SpeedMatcher.SpeedTableStep;
import jmri.jmrix.bachrus.speedmatcher.basic.*;
import jmri.jmrix.bachrus.speedmatcher.speedStepScale.*;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriJOptionPane;

//</editor-fold>
/**
 * Frame for Speedo Console for Bachrus running stand reader interface
 *
 * @author Andrew Crosland Copyright (C) 2010
 * @author Dennis Miller Copyright (C) 2015
 * @author Todd Wegter Copyright (C) 2019-2024
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
        WRITE3,
        WRITE4,
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

    //<editor-fold defaultstate="collapsed" desc="Momentum GUI Elements">
    protected SpinnerNumberModel accelerationSM = new SpinnerNumberModel(0, 0, 255, 1);
    protected SpinnerNumberModel decelerationSM = new SpinnerNumberModel(0, 0, 255, 1);

    protected JLabel accelerationLabel = new JLabel(Bundle.getMessage("MomentumAccelLabel"));
    protected JSpinner accelerationField = new JSpinner(accelerationSM);

    protected JLabel decelerationLabel = new JLabel(Bundle.getMessage("MomentumDecelLabel"));
    protected JSpinner decelerationField = new JSpinner(decelerationSM);

    protected JButton readMomentumButton = new JButton(Bundle.getMessage("MomentumReadBtn"));
    protected JButton setMomentumButton = new JButton(Bundle.getMessage("MomentumSetBtn"));
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
    //<editor-fold defaultstate="collapsed" desc="Basic">
    protected JLabel basicSpeedMatchInfo = new JLabel("<html><p>"
            + Bundle.getMessage("BasicSpeedMatchDescLine1")
            + "<br/><br/>" + Bundle.getMessage("BasicSpeedMatchDescLine2")
            + "<br/><br/>" + Bundle.getMessage("BasicSpeedMatchDescSettings")
            + "<br/><ul>"
            + "<li>" + Bundle.getMessage("BasicSpeedMatchDescDigitrax") + "</li>"
            + "<li>" + Bundle.getMessage("BasicSpeedMatchDescESU") + "</li>"
            + "<li>" + Bundle.getMessage("BasicSpeedMatchDescNCE") + "</li>"
            + "<li>" + Bundle.getMessage("BasicSpeedMatchDescSoundtraxx") + "</li>"
            + "</ul>"
            + Bundle.getMessage("BasicSpeedMatchDescLine3")
            + "<br/><br/>" + Bundle.getMessage("BasicSpeedMatchDescLine4")
            + "<br/><br/>" + Bundle.getMessage("BasicSpeedMatchDescLine5")
            + "<br/><br/></p></html>");

    protected ButtonGroup basicSpeedMatcherTypeGroup = new ButtonGroup();
    protected JRadioButton basicSimpleCVSpeedMatchButton = new JRadioButton(Bundle.getMessage("SpeedMatchSimpleCVRadio"));
    protected JRadioButton basicSpeedTableSpeedMatchButton = new JRadioButton(Bundle.getMessage("SpeedMatchSpeedTableRadio"));
    protected JRadioButton basicESUSpeedMatchButton = new JRadioButton(Bundle.getMessage("SpeedMatchESUSpeedTableRadio"));

    protected SpinnerNumberModel basicSpeedMatchWarmUpForwardSecondsSM = new SpinnerNumberModel(240, 0, 480, 1);
    protected SpinnerNumberModel basicSpeedMatchWarmUpReverseSecondsSM = new SpinnerNumberModel(120, 0, 480, 1);
    protected JCheckBox basicSpeedMatchReverseCheckbox = new JCheckBox(Bundle.getMessage("SpeedMatchTrimReverseChk"));
    protected JCheckBox basicSpeedMatchWarmUpCheckBox = new JCheckBox(Bundle.getMessage("SpeedMatchWarmUpChk"));
    protected JLabel basicSpeedMatchWarmUpForwardLabel = new JLabel(Bundle.getMessage("SpeedMatchForwardWarmUpLabel"));
    protected JSpinner basicSpeedMatchWarmUpForwardSeconds = new JSpinner(basicSpeedMatchWarmUpForwardSecondsSM);
    protected JLabel basicSpeedMatchWarmUpForwardUnit = new JLabel(Bundle.getMessage("SpeedMatchSecondsLabel"));
    protected JLabel basicSpeedMatchWarmUpReverseLabel = new JLabel(Bundle.getMessage("SpeedMatchReverseWarmUpLabel"));
    protected JSpinner basicSpeedMatchWarmUpReverseSeconds = new JSpinner(basicSpeedMatchWarmUpReverseSecondsSM);
    protected JLabel basicSpeedMatchWarmUpReverseUnit = new JLabel(Bundle.getMessage("SpeedMatchSecondsLabel"));

    protected JLabel basicSpeedMatchTargetStartSpeedLabel = new JLabel(Bundle.getMessage("BasioSpeedMatchStartSpeedLabel"));
    protected SpinnerNumberModel startSpeedSM = new SpinnerNumberModel(3, 1, 255, 1);
    protected JSpinner basicSpeedMatchTargetStartSpeedField = new JSpinner(startSpeedSM);
    protected JLabel basicSpeedMatchTargetStartSpeedUnit = new JLabel(Bundle.getMessage("SpeedMatchMPHLabel"));

    protected JLabel basicSpeedMatchTargetHighSpeedLabel = new JLabel(Bundle.getMessage("BasicSpeedMatchTopSpeedLabel"));
    protected SpinnerNumberModel highSpeedSM = new SpinnerNumberModel(55, 1, 255, 1);
    protected JSpinner basicSpeedMatchTargetHighSpeedField = new JSpinner(highSpeedSM);
    protected JLabel basicSpeedMatchTargetHighSpeedUnit = new JLabel(Bundle.getMessage("SpeedMatchMPHLabel"));
    protected JButton basicSpeedMatchStartStopButton = new JButton(Bundle.getMessage(("SpeedMatchStartBtn")));
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Advanced">
    protected JLabel speedStepScaleSpeedMatchInfo = new JLabel("<html><p>"
            + Bundle.getMessage("AdvancedSpeedMatchDescLine1")
            + "<br/><br/>" + Bundle.getMessage("AdvancedSpeedMatchDescLine2")
            + "<br/><br/>" + Bundle.getMessage("AdvancedSpeedMatchDescSettings")
            + "<br/><ul>"
            + "<li>" + Bundle.getMessage("AdvancedSpeedMatchDescDigitrax") + "</li>"
            + "<li>" + Bundle.getMessage("AdvancedSpeedMatchDescESU") + "</li>"
            + "<li>" + Bundle.getMessage("AdvancedSpeedMatchDescNCE") + "</li>"
            + "<li>" + Bundle.getMessage("AdvancedSpeedMatchDescSoundtraxx") + "</li>"
            + "</ul>"
            + Bundle.getMessage("AdvancedSpeedMatchDescLine3")
            + "<br/><br/>" + Bundle.getMessage("AdvancedSpeedMatchDescLine4")
            + "<br/><br/>" + Bundle.getMessage("AdvancedSpeedMatchDescLine5")
            + "<br/><br/>" + Bundle.getMessage("AdvancedSpeedMatchDescLine6")
            + "<br/><br/></p></html>");

    protected ButtonGroup speedStepScaleSpeedMatcherTypeGroup = new ButtonGroup();
    protected JRadioButton speedStepScaleSpeedTableSpeedMatchButton = new JRadioButton(Bundle.getMessage("SpeedMatchSpeedTableRadio"));
    protected JRadioButton speedStepScaleESUSpeedMatchButton = new JRadioButton(Bundle.getMessage("SpeedMatchESUSpeedTableRadio"));

    protected SpinnerNumberModel speedStepScaleSpeedMatchWarmUpForwardSecondsSM = new SpinnerNumberModel(240, 0, 480, 1);
    protected SpinnerNumberModel speedStepScaleSpeedMatchWarmUpReverseSecondsSM = new SpinnerNumberModel(120, 0, 480, 1);
    protected JCheckBox speedStepScaleSpeedMatchReverseCheckbox = new JCheckBox(Bundle.getMessage("SpeedMatchTrimReverseChk"));
    protected JCheckBox speedStepScaleSpeedMatchWarmUpCheckBox = new JCheckBox(Bundle.getMessage("SpeedMatchWarmUpChk"));
    protected JLabel speedStepScaleSpeedMatchWarmUpForwardLabel = new JLabel(Bundle.getMessage("SpeedMatchForwardWarmUpLabel"));
    protected JSpinner speedStepScaleSpeedMatchWarmUpForwardSeconds = new JSpinner(speedStepScaleSpeedMatchWarmUpForwardSecondsSM);
    protected JLabel speedStepScaleSpeedMatchWarmUpForwardUnit = new JLabel(Bundle.getMessage("SpeedMatchSecondsLabel"));
    protected JLabel speedStepScaleSpeedMatchWarmUpReverseLabel = new JLabel(Bundle.getMessage("SpeedMatchReverseWarmUpLabel"));
    protected JSpinner speedStepScaleSpeedMatchWarmUpReverseSeconds = new JSpinner(speedStepScaleSpeedMatchWarmUpReverseSecondsSM);
    protected JLabel speedStepScaleSpeedMatchWarmUpReverseUnit = new JLabel(Bundle.getMessage("SpeedMatchSecondsLabel"));

    protected JLabel speedStepScaleMaxSpeedTargetLabel = new JLabel(Bundle.getMessage("AdvancedSpeedMatchMaxSpeed"));
    protected JComboBox<SpeedTableStepSpeed> speedStepScaleSpeedMatchMaxSpeedField = new JComboBox<>();
    protected JLabel speedStepScaleSpeedMatchMaxSpeedUnit = new JLabel(Bundle.getMessage("SpeedMatchMPHLabel"));
    protected JButton speedStepScaleSpeedMatchStartStopButton = new JButton(Bundle.getMessage(("SpeedMatchStartBtn")));
    protected JLabel speedStepScaleMaxSpeedActualLabel = new JLabel(Bundle.getMessage("AdvancedSpeedMatchActualMaxSpeed"), SwingConstants.RIGHT);
    protected JLabel speedStepScaleMaxSpeedActualField = new JLabel("___");
    protected JLabel speedStepScaleMaxSpeedActualUnit = new JLabel(Bundle.getMessage("SpeedMatchMPHLabel"));
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

        //<editor-fold defaultstate="collapsed" desc="Address and Momentum Panel">      
        //<editor-fold defaultstate="collapsed" desc="Address Pane">
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

        if ((dccServices & PROG) != PROG) {
            // No programming facility so user must enter address
            readAddressButton.setEnabled(false);
            readMomentumButton.setEnabled(false);
        } else {
            readAddressButton.setEnabled(true);
            readMomentumButton.setEnabled(true);
        }

        // Listen to read button
        readAddressButton.addActionListener(e -> readAddress());
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Momentum Panel">
        JPanel momentumPane = new JPanel();
        momentumPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Bundle.getMessage("MomentumTitle")));
        momentumPane.setLayout(new FlowLayout());
        momentumPane.add(accelerationLabel);
        momentumPane.add(accelerationField);
        momentumPane.add(decelerationLabel);
        momentumPane.add(decelerationField);
        momentumPane.add(readMomentumButton);
        momentumPane.add(setMomentumButton);

        // Listen to read momentum button
        readMomentumButton.addActionListener(e -> readMomentum());

        //Listen to set momentum button
        setMomentumButton.addActionListener(e -> setMomentum());
        //</editor-fold>

        JPanel profileAndSpeedMatchingNorthPane = new JPanel();
        profileAndSpeedMatchingNorthPane.setLayout(new BoxLayout(profileAndSpeedMatchingNorthPane, BoxLayout.Y_AXIS));
        profileAndSpeedMatchingNorthPane.add(addrPane);
        profileAndSpeedMatchingNorthPane.add(momentumPane);

        profileAndSpeedMatchingPane.add(profileAndSpeedMatchingNorthPane, BorderLayout.NORTH);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Speed Matching and Profiling Panel">
        JTabbedPane profileAndSpeedMatchingTabs = new JTabbedPane();

        GridBagConstraints row1 = new GridBagConstraints();
        row1.anchor = GridBagConstraints.WEST;
        row1.fill = GridBagConstraints.HORIZONTAL;
        GridBagConstraints row2 = new GridBagConstraints();
        row2.gridy = 1;
        row2.anchor = GridBagConstraints.EAST;
        GridBagConstraints row3 = new GridBagConstraints();
        row3.gridy = 2;
        row3.anchor = GridBagConstraints.WEST;

        GridBagConstraints gbc = new GridBagConstraints();

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

        // pane to hold the title
        JPanel titlePane = new JPanel();
        titlePane.setLayout(new BoxLayout(titlePane, BoxLayout.X_AXIS));
        titlePane.setBorder(new EmptyBorder(3, 0, 3, 0));
        //JTextArea profileTitle = new JTextArea("Title: ");
        //profileTitlePane.add(profileTitle);
        printTitleText.setToolTipText(Bundle.getMessage("TTPrintTitle"));
        printTitleText.setText(Bundle.getMessage("TTText1"));
        titlePane.add(printTitleText);

        // pane to wrap buttons and title
        JPanel profileSouthPane = new JPanel();
        profileSouthPane.setLayout(new BoxLayout(profileSouthPane, BoxLayout.Y_AXIS));
        profileSouthPane.add(profileButtonPane);
        profileSouthPane.add(titlePane);

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
        //</editor-fold> 

        //<editor-fold defaultstate="collapsed" desc="Basic Speed Matching Tab">
        basicSpeedMatcherTypeGroup.add(basicSimpleCVSpeedMatchButton);
        basicSpeedMatcherTypeGroup.add(basicSpeedTableSpeedMatchButton);
        basicSpeedMatcherTypeGroup.add(basicESUSpeedMatchButton);
        basicSimpleCVSpeedMatchButton.setSelected(true);

        basicSpeedMatchReverseCheckbox.setSelected(true);
        basicSpeedMatchWarmUpCheckBox.setSelected(true);

        JPanel basicSpeedMatcherPane = new JPanel();
        basicSpeedMatcherPane.setLayout(new BorderLayout());
        JPanel basicSpeedMatchSettingsPane = new JPanel();
        basicSpeedMatchSettingsPane.setLayout(new BoxLayout(basicSpeedMatchSettingsPane, BoxLayout.PAGE_AXIS));

        //Important Information
        JPanel basicSpeedMatchImportantInfoPane = new JPanel();
        basicSpeedMatchImportantInfoPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Bundle.getMessage("SpeedMatchDescTitle")));
        basicSpeedMatchImportantInfoPane.setLayout(new BoxLayout(basicSpeedMatchImportantInfoPane, BoxLayout.LINE_AXIS));
        basicSpeedMatchImportantInfoPane.add(basicSpeedMatchInfo);
        basicSpeedMatchSettingsPane.add(basicSpeedMatchImportantInfoPane);

        //Speed Matcher Mode
        JPanel basicSpeedMatchModePane = new JPanel();
        basicSpeedMatchModePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Bundle.getMessage("SpeedMatchModeTitle")));
        basicSpeedMatchModePane.setLayout(new FlowLayout());
        basicSpeedMatchModePane.add(basicSimpleCVSpeedMatchButton);
        basicSpeedMatchModePane.add(basicSpeedTableSpeedMatchButton);
        basicSpeedMatchModePane.add(basicESUSpeedMatchButton);
        basicSpeedMatchSettingsPane.add(basicSpeedMatchModePane);

        //Other Settings
        JPanel basicSpeedMatchOtherSettingsPane = new JPanel();
        basicSpeedMatchOtherSettingsPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Bundle.getMessage("SpeedMatchOtherSettingTitle")));
        basicSpeedMatchOtherSettingsPane.setLayout(new GridBagLayout());
        basicSpeedMatchOtherSettingsPane.add(basicSpeedMatchWarmUpCheckBox, row1);
        basicSpeedMatchOtherSettingsPane.add(basicSpeedMatchWarmUpForwardLabel, row2);
        basicSpeedMatchOtherSettingsPane.add(Box.createRigidArea(new Dimension(5, 0)), row2);
        basicSpeedMatchOtherSettingsPane.add(basicSpeedMatchWarmUpForwardSeconds, row2);
        basicSpeedMatchOtherSettingsPane.add(Box.createRigidArea(new Dimension(5, 0)), row2);
        basicSpeedMatchOtherSettingsPane.add(basicSpeedMatchWarmUpForwardUnit, row2);
        basicSpeedMatchOtherSettingsPane.add(Box.createRigidArea(new Dimension(30, 0)), row2);
        basicSpeedMatchOtherSettingsPane.add(basicSpeedMatchWarmUpReverseLabel, row2);
        basicSpeedMatchOtherSettingsPane.add(Box.createRigidArea(new Dimension(5, 0)), row2);
        basicSpeedMatchOtherSettingsPane.add(basicSpeedMatchWarmUpReverseSeconds, row2);
        basicSpeedMatchOtherSettingsPane.add(Box.createRigidArea(new Dimension(5, 0)), row2);
        basicSpeedMatchOtherSettingsPane.add(basicSpeedMatchWarmUpReverseUnit, row2);
        basicSpeedMatchOtherSettingsPane.add(basicSpeedMatchReverseCheckbox, row3);
        basicSpeedMatchSettingsPane.add(basicSpeedMatchOtherSettingsPane);

        //Speed Settings
        JPanel basicSpeedMatchSpeedPane = new JPanel();
        basicSpeedMatchSpeedPane.setLayout(new GridBagLayout());
        basicSpeedMatchSpeedPane.add(basicSpeedMatchTargetStartSpeedLabel, gbc);
        basicSpeedMatchSpeedPane.add(Box.createRigidArea(new Dimension(5, 0)), gbc);
        basicSpeedMatchSpeedPane.add(basicSpeedMatchTargetStartSpeedField, gbc);
        basicSpeedMatchSpeedPane.add(Box.createRigidArea(new Dimension(5, 0)), gbc);
        basicSpeedMatchSpeedPane.add(basicSpeedMatchTargetStartSpeedUnit, gbc);
        basicSpeedMatchSpeedPane.add(Box.createRigidArea(new Dimension(15, 0)), gbc);
        basicSpeedMatchSpeedPane.add(basicSpeedMatchTargetHighSpeedLabel, gbc);
        basicSpeedMatchSpeedPane.add(Box.createRigidArea(new Dimension(5, 0)), gbc);
        basicSpeedMatchSpeedPane.add(basicSpeedMatchTargetHighSpeedField, gbc);
        basicSpeedMatchSpeedPane.add(Box.createRigidArea(new Dimension(5, 0)), gbc);
        basicSpeedMatchSpeedPane.add(basicSpeedMatchTargetHighSpeedUnit, gbc);
        basicSpeedMatchSpeedPane.add(Box.createRigidArea(new Dimension(15, 0)), gbc);
        basicSpeedMatchSpeedPane.add(basicSpeedMatchStartStopButton, gbc);

        basicSpeedMatcherPane.add(basicSpeedMatchSettingsPane, BorderLayout.NORTH);
        basicSpeedMatcherPane.add(basicSpeedMatchSpeedPane, BorderLayout.CENTER);

        profileAndSpeedMatchingTabs.add(Bundle.getMessage("BasicSpeedMatchTab"), basicSpeedMatcherPane);

        //<editor-fold defaultstate="collapsed" desc="Basic Speed Matcher Button Handlers">
        // Listen to speed match button
        basicSpeedMatchStartStopButton.addActionListener(e -> {
            int targetStartSpeed;
            int targetHighSpeed;
            boolean speedMatchReverse;
            boolean warmUpLoco;
            int warmUpForwardSeconds;
            int warmUpReverseSeconds;

            BasicSpeedMatcherConfig.SpeedTable speedTableType;

            if ((speedMatcher == null || speedMatcher.isSpeedMatcherIdle()) && (profileState == ProfileState.IDLE)) {
                targetStartSpeed = startSpeedSM.getNumber().intValue();
                targetHighSpeed = highSpeedSM.getNumber().intValue();

                if (basicSpeedTableSpeedMatchButton.isSelected()) {
                    speedTableType = BasicSpeedMatcherConfig.SpeedTable.ADVANCED;
                } else if (basicESUSpeedMatchButton.isSelected()) {
                    speedTableType = BasicSpeedMatcherConfig.SpeedTable.ESU;
                } else {
                    speedTableType = BasicSpeedMatcherConfig.SpeedTable.SIMPLE;
                }

                speedMatchReverse = basicSpeedMatchReverseCheckbox.isSelected();
                warmUpLoco = basicSpeedMatchWarmUpCheckBox.isSelected();
                warmUpForwardSeconds = basicSpeedMatchWarmUpForwardSecondsSM.getNumber().intValue();
                warmUpReverseSeconds = basicSpeedMatchWarmUpReverseSecondsSM.getNumber().intValue();

                speedMatcher = BasicSpeedMatcherFactory.getSpeedMatcher(
                        speedTableType,
                        new BasicSpeedMatcherConfig(
                                locomotiveAddress,
                                targetStartSpeed,
                                targetHighSpeed,
                                mphButton.isSelected() ? Speed.Unit.MPH : Speed.Unit.KPH,
                                speedMatchReverse,
                                warmUpLoco ? warmUpForwardSeconds : 0,
                                warmUpLoco ? warmUpReverseSeconds : 0,
                                pm,
                                statusLabel,
                                basicSpeedMatchStartStopButton
                        )
                );

                if (!speedMatcher.startSpeedMatcher()) {
                    speedMatcher = null;
                }
            } else {
                stopProfileAndSpeedMatch();
            }
        });

        basicSpeedMatchWarmUpCheckBox.addActionListener(e -> {
            boolean enableWarmUp = basicSpeedMatchWarmUpCheckBox.isSelected();

            basicSpeedMatchWarmUpForwardLabel.setEnabled(enableWarmUp);
            basicSpeedMatchWarmUpForwardSeconds.setEnabled(enableWarmUp);
            basicSpeedMatchWarmUpForwardUnit.setEnabled(enableWarmUp);
            basicSpeedMatchWarmUpReverseLabel.setEnabled(enableWarmUp);
            basicSpeedMatchWarmUpReverseSeconds.setEnabled(enableWarmUp);
            basicSpeedMatchWarmUpReverseUnit.setEnabled(enableWarmUp);
        });
        //</editor-fold>
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Advanced Speed Matcher Tab">
        speedStepScaleSpeedMatcherTypeGroup.add(speedStepScaleSpeedTableSpeedMatchButton);
        speedStepScaleSpeedMatcherTypeGroup.add(speedStepScaleESUSpeedMatchButton);
        speedStepScaleSpeedTableSpeedMatchButton.setSelected(true);

        speedStepScaleSpeedMatchReverseCheckbox.setSelected(true);
        speedStepScaleSpeedMatchWarmUpCheckBox.setSelected(true);

        JPanel speedStepScaleSpeedMatcherPane = new JPanel();
        speedStepScaleSpeedMatcherPane.setLayout(new BorderLayout());
        JPanel speedStepScaleSpeedMatchSettingsPane = new JPanel();
        speedStepScaleSpeedMatchSettingsPane.setLayout(new BoxLayout(speedStepScaleSpeedMatchSettingsPane, BoxLayout.PAGE_AXIS));

        //Important Information
        JPanel speedStepScaleSpeedMatchImportantInfoPane = new JPanel();
        speedStepScaleSpeedMatchImportantInfoPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Bundle.getMessage("SpeedMatchDescTitle")));
        speedStepScaleSpeedMatchImportantInfoPane.setLayout(new BoxLayout(speedStepScaleSpeedMatchImportantInfoPane, BoxLayout.LINE_AXIS));
        speedStepScaleSpeedMatchImportantInfoPane.add(speedStepScaleSpeedMatchInfo);
        speedStepScaleSpeedMatchSettingsPane.add(speedStepScaleSpeedMatchImportantInfoPane);

        //Speed Matcher Mode
        JPanel speedStepScaleSpeedMatchModePane = new JPanel();
        speedStepScaleSpeedMatchModePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Bundle.getMessage("SpeedMatchModeTitle")));
        speedStepScaleSpeedMatchModePane.setLayout(new FlowLayout());
        speedStepScaleSpeedMatchModePane.add(speedStepScaleSpeedTableSpeedMatchButton);
        speedStepScaleSpeedMatchModePane.add(speedStepScaleESUSpeedMatchButton);
        speedStepScaleSpeedMatchSettingsPane.add(speedStepScaleSpeedMatchModePane);

        //Other Settings
        JPanel speedStepScaleSpeedMatchOtherSettingsPane = new JPanel();
        speedStepScaleSpeedMatchOtherSettingsPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Bundle.getMessage("SpeedMatchOtherSettingTitle")));
        speedStepScaleSpeedMatchOtherSettingsPane.setLayout(new GridBagLayout());
        speedStepScaleSpeedMatchOtherSettingsPane.add(speedStepScaleSpeedMatchWarmUpCheckBox, row1);
        speedStepScaleSpeedMatchOtherSettingsPane.add(speedStepScaleSpeedMatchWarmUpForwardLabel, row2);
        speedStepScaleSpeedMatchOtherSettingsPane.add(Box.createRigidArea(new Dimension(5, 0)), row2);
        speedStepScaleSpeedMatchOtherSettingsPane.add(speedStepScaleSpeedMatchWarmUpForwardSeconds, row2);
        speedStepScaleSpeedMatchOtherSettingsPane.add(Box.createRigidArea(new Dimension(5, 0)), row2);
        speedStepScaleSpeedMatchOtherSettingsPane.add(speedStepScaleSpeedMatchWarmUpForwardUnit, row2);
        speedStepScaleSpeedMatchOtherSettingsPane.add(Box.createRigidArea(new Dimension(30, 0)), row2);
        speedStepScaleSpeedMatchOtherSettingsPane.add(speedStepScaleSpeedMatchWarmUpReverseLabel, row2);
        speedStepScaleSpeedMatchOtherSettingsPane.add(Box.createRigidArea(new Dimension(5, 0)), row2);
        speedStepScaleSpeedMatchOtherSettingsPane.add(speedStepScaleSpeedMatchWarmUpReverseSeconds, row2);
        speedStepScaleSpeedMatchOtherSettingsPane.add(Box.createRigidArea(new Dimension(5, 0)), row2);
        speedStepScaleSpeedMatchOtherSettingsPane.add(speedStepScaleSpeedMatchWarmUpReverseUnit, row2);
        speedStepScaleSpeedMatchOtherSettingsPane.add(speedStepScaleSpeedMatchReverseCheckbox, row3);
        speedStepScaleSpeedMatchSettingsPane.add(speedStepScaleSpeedMatchOtherSettingsPane);

        //Speed Settings        
        SpeedTableStep tempStep = SpeedTableStep.STEP1;
        while (tempStep != null) {
            speedStepScaleSpeedMatchMaxSpeedField.addItem(new SpeedTableStepSpeed(tempStep));
            tempStep = tempStep.getNext();
        }
        speedStepScaleSpeedMatchMaxSpeedField.setSelectedIndex(12);
        
        JPanel speedStepScaleSpeedMatchSpeedPane = new JPanel();
        speedStepScaleSpeedMatchSpeedPane.setLayout(new GridBagLayout());
        speedStepScaleSpeedMatchSpeedPane.add(speedStepScaleMaxSpeedTargetLabel, gbc);
        speedStepScaleSpeedMatchSpeedPane.add(Box.createRigidArea(new Dimension(5, 0)), gbc);
        speedStepScaleSpeedMatchSpeedPane.add(speedStepScaleSpeedMatchMaxSpeedField, gbc);
        speedStepScaleSpeedMatchSpeedPane.add(Box.createRigidArea(new Dimension(5, 0)), gbc);
        speedStepScaleSpeedMatchSpeedPane.add(speedStepScaleSpeedMatchMaxSpeedUnit, gbc);
        speedStepScaleSpeedMatchSpeedPane.add(Box.createRigidArea(new Dimension(15, 0)), gbc);
        speedStepScaleSpeedMatchSpeedPane.add(speedStepScaleSpeedMatchStartStopButton, gbc);
        speedStepScaleSpeedMatchSpeedPane.add(Box.createRigidArea(new Dimension(15, 0)), gbc);
        speedStepScaleSpeedMatchSpeedPane.add(speedStepScaleMaxSpeedActualLabel, gbc);
        speedStepScaleSpeedMatchSpeedPane.add(Box.createRigidArea(new Dimension(5, 0)), gbc);
        speedStepScaleSpeedMatchSpeedPane.add(speedStepScaleMaxSpeedActualField, gbc);
        speedStepScaleSpeedMatchSpeedPane.add(Box.createRigidArea(new Dimension(5, 0)), gbc);
        speedStepScaleSpeedMatchSpeedPane.add(speedStepScaleMaxSpeedActualUnit, gbc);

        speedStepScaleSpeedMatcherPane.add(speedStepScaleSpeedMatchSettingsPane, BorderLayout.NORTH);
        speedStepScaleSpeedMatcherPane.add(speedStepScaleSpeedMatchSpeedPane, BorderLayout.CENTER);

        profileAndSpeedMatchingTabs.add(Bundle.getMessage("AdvancedSpeedMatchTab"), speedStepScaleSpeedMatcherPane);

        //<editor-fold defaultstate="collapsed" desc="Speed Step Scale Speed Matcher Button Handlers">
        // Listen to speed match button
        speedStepScaleSpeedMatchStartStopButton.addActionListener(e -> {
            SpeedTableStepSpeed targetMaxSpeedStep;
            boolean speedMatchReverse;
            boolean warmUpLoco;
            int warmUpForwardSeconds;
            int warmUpReverseSeconds;

            SpeedStepScaleSpeedMatcherConfig.SpeedTable speedTableType;

            if ((speedMatcher == null || speedMatcher.isSpeedMatcherIdle()) && (profileState == ProfileState.IDLE)) {
                targetMaxSpeedStep = (SpeedTableStepSpeed)speedStepScaleSpeedMatchMaxSpeedField.getSelectedItem();

                if (speedStepScaleESUSpeedMatchButton.isSelected()) {
                    speedTableType = SpeedStepScaleSpeedMatcherConfig.SpeedTable.ESU;
                } else {
                    speedTableType = SpeedStepScaleSpeedMatcherConfig.SpeedTable.ADVANCED;
                }

                speedMatchReverse = speedStepScaleSpeedMatchReverseCheckbox.isSelected();
                warmUpLoco = speedStepScaleSpeedMatchWarmUpCheckBox.isSelected();
                warmUpForwardSeconds = speedStepScaleSpeedMatchWarmUpForwardSecondsSM.getNumber().intValue();
                warmUpReverseSeconds = speedStepScaleSpeedMatchWarmUpReverseSecondsSM.getNumber().intValue();

                speedMatcher = SpeedStepScaleSpeedMatcherFactory.getSpeedMatcher(
                        speedTableType,
                        new SpeedStepScaleSpeedMatcherConfig(
                                locomotiveAddress,
                                targetMaxSpeedStep,
                                mphButton.isSelected() ? Speed.Unit.MPH : Speed.Unit.KPH,
                                speedMatchReverse,
                                warmUpLoco ? warmUpForwardSeconds : 0,
                                warmUpLoco ? warmUpReverseSeconds : 0,
                                pm,
                                statusLabel,
                                speedStepScaleMaxSpeedActualField,
                                speedStepScaleSpeedMatchStartStopButton
                        )
                );

                if (!speedMatcher.startSpeedMatcher()) {
                    speedMatcher = null;
                }
            } else {
                stopProfileAndSpeedMatch();
            }
        });

        speedStepScaleSpeedMatchWarmUpCheckBox.addActionListener(e -> {
            boolean enableWarmUp = speedStepScaleSpeedMatchWarmUpCheckBox.isSelected();

            speedStepScaleSpeedMatchWarmUpForwardLabel.setEnabled(enableWarmUp);
            speedStepScaleSpeedMatchWarmUpForwardSeconds.setEnabled(enableWarmUp);
            speedStepScaleSpeedMatchWarmUpForwardUnit.setEnabled(enableWarmUp);
            speedStepScaleSpeedMatchWarmUpReverseLabel.setEnabled(enableWarmUp);
            speedStepScaleSpeedMatchWarmUpReverseSeconds.setEnabled(enableWarmUp);
            speedStepScaleSpeedMatchWarmUpReverseUnit.setEnabled(enableWarmUp);
        });
        //</editor-fold>
        //</editor-fold>

        profileAndSpeedMatchingPane.add(profileAndSpeedMatchingTabs, BorderLayout.CENTER);
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        // Create the main pane and add the sub-panes
        JPanel mainPane = new JPanel();
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.X_AXIS));
        // make basic panel
        mainPane.add(basicPane);

        if (((dccServices & THROTTLE) == THROTTLE) || ((dccServices & COMMAND) == COMMAND)) {
            mainPane.add(profileAndSpeedMatchingPane);
        } else {
            log.info("{} Connection:{}", Bundle.getMessage("StatNoDCC"), _memo.getUserName());
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
        if (mphButton.isSelected()) {
            profileGraphPane.setUnitsMph();
            basicSpeedMatchTargetStartSpeedUnit.setText(Bundle.getMessage("SpeedMatchMPHLabel"));
            basicSpeedMatchTargetHighSpeedUnit.setText(Bundle.getMessage("SpeedMatchMPHLabel"));
            speedStepScaleSpeedMatchMaxSpeedUnit.setText(Bundle.getMessage("SpeedMatchMPHLabel"));
            speedStepScaleMaxSpeedActualUnit.setText(Bundle.getMessage("SpeedMatchMPHLabel"));
        } else {
            profileGraphPane.setUnitsKph();
            basicSpeedMatchTargetStartSpeedUnit.setText(Bundle.getMessage("SpeedMatchKPHLabel"));
            basicSpeedMatchTargetHighSpeedUnit.setText(Bundle.getMessage("SpeedMatchKPHLabel"));
            speedStepScaleSpeedMatchMaxSpeedUnit.setText(Bundle.getMessage("SpeedMatchKPHLabel"));
            speedStepScaleMaxSpeedActualUnit.setText(Bundle.getMessage("SpeedMatchKPHLabel"));
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
                if ((speedMatcher == null || speedMatcher.isSpeedMatcherIdle()) && (profileState == ProfileState.IDLE)) {
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

        //clean up speed matcher
        if (speedMatcher != null) {
            speedMatcher.stopSpeedMatcher();
            speedMatcher = null;
        }

        resetGraphButton.setEnabled(true);
        progState = ProgState.IDLE;
        profileState = ProfileState.IDLE;
    }

    /**
     * Stops the profiling and speed matching processes. Called by pressing
     * either the stop profile or stop speed matching buttons.
     */
    protected synchronized void stopProfileAndSpeedMatch() {
        if (profileState != ProfileState.IDLE || !speedMatcher.isSpeedMatcherIdle()) {
            if (profileState != ProfileState.IDLE) {
                log.info("Profiling/Speed Matching stopped by user");
            }

            tidyUp();
        }
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
            speedMatcher.updateCurrentSpeed(currentSpeed);
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
        if ((speedMatcher == null || speedMatcher.isSpeedMatcherIdle()) && (profileState == ProfileState.IDLE)) {
            progState = ProgState.READ29;
            statusLabel.setText(Bundle.getMessage("ProgRd29"));
            startRead("29");
        }
    }

    /**
     * Starts reading the momentum CVs (CV 3 and 4) using the global programmer
     */
    protected void readMomentum() {
        if ((speedMatcher == null || speedMatcher.isSpeedMatcherIdle()) && (profileState == ProfileState.IDLE)) {
            progState = ProgState.READ3;
            statusLabel.setText(Bundle.getMessage("ProgReadAccel"));
            startRead("3");
        }
    }

    /**
     * Starts writing the momentum CVs (CV 3 and 4) using the global programmer
     */
    protected void setMomentum() {
        if ((speedMatcher == null || speedMatcher.isSpeedMatcherIdle()) && (profileState == ProfileState.IDLE)) {
            progState = ProgState.WRITE3;
            int acceleration = accelerationSM.getNumber().intValue();
            statusLabel.setText(Bundle.getMessage("ProgSetAccel", acceleration));
            startWrite("3", acceleration);
        }
    }

    /**
     * Starts reading a CV using the service mode programmer
     *
     * @param cv the CV
     */
    protected void startRead(String cv) {
        try {
            prog.readCV(cv, this);
        } catch (ProgrammerException e) {
            log.error("Exception reading CV {}", cv, e);
        }
    }

    /**
     * STarts writing a CV using the global programmer
     *
     * @param cv    the CV
     * @param value the value to write to the CV
     */
    protected void startWrite(String cv, int value) {
        try {
            prog.writeCV(cv, value, this);
        } catch (ProgrammerException e) {
            log.error("Exception setting CV {} to {}", cv, value, e);
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

                case READ3:
                    accelerationSM.setValue(value);
                    progState = ProgState.READ4;
                    statusLabel.setText(Bundle.getMessage("ProgReadDecel"));
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

                case WRITE3:
                    progState = ProgState.WRITE4;
                    int deceleration = decelerationSM.getNumber().intValue();
                    statusLabel.setText(Bundle.getMessage("ProgSetDecel", deceleration));
                    startWrite("4", deceleration);
                    break;

                case WRITE4:
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
