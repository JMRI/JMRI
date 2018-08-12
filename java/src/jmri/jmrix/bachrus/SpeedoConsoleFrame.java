package jmri.jmrix.bachrus;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
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
import jmri.ThrottleListener;
import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterEntrySelector;
import jmri.jmrit.roster.swing.GlobalRosterEntryComboBox;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for Speedo Console for Bachrus running stand reader interface
 *
 * @author Andrew Crosland Copyright (C) 2010
 * @author Dennis Miller Copyright (C) 2015
 */
public class SpeedoConsoleFrame extends JmriJFrame implements SpeedoListener,
        ThrottleListener,
        ProgListener,
        PropertyChangeListener {

    /**
     *
     * TODO: Complete the help file
     */
    private PowerManager pm = null;

    // member declarations
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
    //protected JLabel profileAddressLabel = new JLabel(Bundle.getMessage("LocoAddress"));
    //protected JTextField profileAddressField = new JTextField(6);
    protected JButton readAddressButton = new JButton(Bundle.getMessage("Read"));

    private final DccLocoAddressSelector addrSelector = new DccLocoAddressSelector();
    private JButton setButton;
    private GlobalRosterEntryComboBox rosterBox;
    protected RosterEntry rosterEntry;
    private final boolean disableRosterBoxActions = false;

    private int profileAddress = 0;
    private boolean profileIsLong = false;

    protected JButton trackPowerButton = new JButton(Bundle.getMessage("PowerUp"));
    protected JButton startProfileButton = new JButton(Bundle.getMessage("Start"));
    protected JButton stopProfileButton = new JButton(Bundle.getMessage("Stop"));
    protected JButton exportProfileButton = new JButton(Bundle.getMessage("Export"));
    protected JButton printProfileButton = new JButton(Bundle.getMessage("Print"));
    protected JButton resetGraphButton = new JButton(Bundle.getMessage("ResetGraph"));
    protected JButton loadProfileButton = new JButton(Bundle.getMessage("LoadRef"));
    protected JTextField printTitleText = new JTextField();
    protected JLabel statusLabel = new JLabel(" ");

    protected javax.swing.JLabel readerLabel = new javax.swing.JLabel();

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
     * is applied to minimise the delay in updating the display
     *
     * Speed measurement is split into 4 ranges with an overlap, tp
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

    protected enum DisplayType {

        NUMERIC, DIAL
    }
    protected DisplayType display = DisplayType.NUMERIC;

    /*
     * Keep track of the DCC services available
     */
    protected int dccServices;
    protected static final int BASIC = 0;
    protected static final int PROG = 1;
    protected static final int COMMAND = 2;
    protected static final int THROTTLE = 4;

    protected boolean timerRunning = false;

    protected DccSpeedProfile spFwd;
    protected DccSpeedProfile spRev;
    protected DccSpeedProfile spRef;

    protected enum ProfileState {

        IDLE, WAIT_FOR_THROTTLE, RUNNING
    }
    protected ProfileState state = ProfileState.IDLE;

    protected enum ProfileDirection {

        FORWARD, REVERSE
    }
    protected ProfileDirection profileDir = ProfileDirection.FORWARD;
    protected DccThrottle throttle = null;
    protected int profileStep = 0;
    protected float profileSpeed;
    protected float profileIncrement;
    //protected int profileAddress = 0;
    protected int readAddress = 0;
    protected Programmer prog = null;
    protected CommandStation commandStation = null;

    String selectedScalePref = this.getClass().getName() + ".SelectedScale"; // NOI18N
    String customScalePref = this.getClass().getName() + ".CustomScale"; // NOI18N
    String speedUnitsKphPref = this.getClass().getName() + ".SpeedUnitsKph"; // NOI18N
    String dialTypePref = this.getClass().getName() + ".DialType"; // NOI18N
    jmri.UserPreferencesManager prefs;

    protected enum ProgState {

        IDLE, WAIT29, WAIT1, WAIT17, WAIT18
    }
    protected ProgState readState = ProgState.IDLE;

    // For testing only, must be 1 for normal use
    protected static final int speedTestScaleFactor = 1;

    //Create the combo box, and assign the scales to it
    JComboBox<String> scaleList = new JComboBox<>(scaleStrings);

    // members for handling the Speedo interface
    SpeedoTrafficController tc = null;
    String replyString;

    private SpeedoSystemConnectionMemo _memo = null;

    public SpeedoConsoleFrame(SpeedoSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    protected String title() {
        return Bundle.getMessage("SpeedoConsole");
    }

    @Override
    public void dispose() {
        prefs.setComboBoxLastSelection(selectedScalePref, (String)scaleList.getSelectedItem());
        prefs.setProperty(customScalePref, "customScale", customScale);
        prefs.setSimplePreferenceState(speedUnitsKphPref, kphButton.isSelected());
        prefs.setSimplePreferenceState(dialTypePref, dialButton.isSelected());
        _memo.getTrafficController().removeSpeedoListener(this);
        super.dispose();
    }

    // FIXME: Why does the if statement in this method include a direct false?
    @SuppressWarnings("unused")
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
        try {
            scaleList.setSelectedItem(prefs.getComboBoxLastSelection(selectedScalePref));
        } catch (ArrayIndexOutOfBoundsException e) {
            scaleList.setSelectedItem(defaultScale);
        }
        if ( scaleList.getSelectedIndex() > -1 ) {
            selectedScale = scales[scaleList.getSelectedIndex()];
        }
        // Listen to selection of scale
        scaleList.addActionListener(new java.awt.event.ActionListener() {
            @SuppressWarnings("unchecked") // action semantics pass an Object that must be a JComboBox<String>
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                JComboBox<String> cb = (JComboBox<String>) e.getSource();
                selectedScale = scales[cb.getSelectedIndex()];
                checkCustomScale();
            }
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
        customScaleField.setText(prefs.getProperty(customScalePref, "customScale").toString());
        checkCustomScale();
        getCustomScale();

        // Let user press return to enter custom scale
        customScaleField.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                getCustomScale();
            }
        });

        customScalePanel.add(customScaleLabel);
        customScalePanel.add(customScaleField);

        basicPane.add(scalePanel);
        basicPane.add(customScalePanel);

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
        progButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (((dccServices & PROG) == PROG)) {
                    // Programmer is available to read back CVs
                    readAddressButton.setEnabled(true);
                    statusLabel.setText(Bundle.getMessage("StatProg"));
                }
            }
        });
        mainButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // no programmer available to read back CVs
                readAddressButton.setEnabled(false);
                statusLabel.setText(Bundle.getMessage("StatMain"));
            }
        });

        basicPane.add(modePanel);

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
        mphButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setUnits();
            }
        });
        kphButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setUnits();
            }
        });

        // Listen to change of display
        numButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setDial();
            }
        });
        dialButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setDial();
            }
        });

        basicPane.add(speedPanel);

        /*
         * Pane for profiling loco speed curve
         */
        JPanel profilePane = new JPanel();
        profilePane.setLayout(new BorderLayout());

        JPanel addrPane = new JPanel();
        GridBagLayout gLayout = new GridBagLayout();
        GridBagConstraints gConstraints = new GridBagConstraints();
        gConstraints.insets = new Insets(3, 3, 3, 3);
        Border addrPaneBorder = javax.swing.BorderFactory.createEtchedBorder();
        TitledBorder addrPaneTitle = javax.swing.BorderFactory.createTitledBorder(addrPaneBorder, Bundle.getMessage("LocoSelection"));
        addrPane.setLayout(gLayout);
        addrPane.setBorder(addrPaneTitle);

        setButton = new JButton(Bundle.getMessage("ButtonSet"));
        setButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeOfAddress();
            }
        });
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
        rosterBox.addPropertyChangeListener(
                RosterEntrySelector.SELECTED_ROSTER_ENTRIES, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (!disableRosterBoxActions) { //Have roster box actions been disabled?
                    rosterItemSelected();
                }
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
        readAddressButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                readAddress();
            }
        });

        profilePane.add(addrPane, BorderLayout.NORTH);

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
        //       JTextArea profileTitle = new JTextArea("Title: ");
        //       profileTitlePane.add(profileTitle);
        printTitleText.setToolTipText(Bundle.getMessage("TTPrintTitle"));
        printTitleText.setText(Bundle.getMessage("TTText1"));
        profileTitlePane.add(printTitleText);

        // pane to wrap buttons and title
        JPanel profileSouthPane = new JPanel();
        profileSouthPane.setLayout(new BoxLayout(profileSouthPane, BoxLayout.Y_AXIS));
        profileSouthPane.add(profileButtonPane);
        profileSouthPane.add(profileTitlePane);

        // Listen to track Power button
        trackPowerButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                trackPower();
            }
        });

        // Listen to start button
        startProfileButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                getCustomScale();
                startProfile();
            }
        });

        // Listen to stop button
        stopProfileButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                stopProfile();
            }
        });

        // Listen to grid button
        toggleGridButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                profileGraphPane.showGrid(toggleGridButton.isSelected());
                profileGraphPane.repaint();
            }
        });

        // Listen to export button
        exportProfileButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (dirFwdButton.isSelected() && dirRevButton.isSelected()) {
                    DccSpeedProfile[] sp = {spFwd, spRev};
                    DccSpeedProfile.export(sp, profileAddress, profileGraphPane.getUnits());
                } else if (dirFwdButton.isSelected()) {
                    DccSpeedProfile.export(spFwd, profileAddress, "fwd", profileGraphPane.getUnits());
                } else if (dirRevButton.isSelected()) {
                    DccSpeedProfile.export(spRev, profileAddress, "rev", profileGraphPane.getUnits());
                }
            }
        });

        // Listen to print button
        printProfileButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                profileGraphPane.printProfile(printTitleText.getText());
            }
        });

        // Listen to reset graph button
        resetGraphButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                spFwd.clear();
                spRev.clear();
                spRef.clear();
                speedoDialDisplay.reset();
                profileGraphPane.repaint();
            }
        });

        // Listen to Load Reference button
        loadProfileButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                spRef.clear();
                int response = spRef.importDccProfile(profileGraphPane.getUnits());
                if (response == -1) {
                    statusLabel.setText(Bundle.getMessage("StatFileError"));
                } else {
                    statusLabel.setText(Bundle.getMessage("StatFileSuccess"));
                }
                profileGraphPane.repaint();
            }
        });

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

        // connect to TrafficController
        tc = _memo.getTrafficController();
        tc.addSpeedoListener(this);

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

        setUnits();
        setDial();

        // pack for display
        pack();

        speedoDialDisplay.scaleFace();
    }

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
     * Set the displays to mile pre hour or kilometers per hour
     */
    protected void setUnits() {
        if (mphButton.isSelected()) {
            profileGraphPane.setUnitsMph();
        } else {
            profileGraphPane.setUnitsKph();
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

    /**
     * Handle changing/setting the address.
     */
    private synchronized void changeOfAddress() {
        if (addrSelector.getAddress() != null) {
            profileAddress = addrSelector.getAddress().getNumber();
            profileIsLong = addrSelector.getAddress().isLongAddress();
            setTitle();
        } else {
            profileAddress = 0;
            profileIsLong = true;
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

    private void rosterItemSelected() {
        if (rosterBox.getSelectedRosterEntries().length != 0) {
            setRosterEntry(rosterBox.getSelectedRosterEntries()[0]);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setPowerStatus();
    }

    private void setTitle() {
        Date today;
        String result;
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault());
        today = new Date();
        result = formatter.format(today);
        String annotate = "Bachrus MTS-DCC " + Bundle.getMessage("ProfileFor") + " "
                + profileAddress + " " + Bundle.getMessage("CreatedOn")
                + " " + result;
        printTitleText.setText(annotate);
    }

    private void setPowerStatus() {
        if (pm == null) {
            return;
        }
        try {
            if (pm.getPower() == PowerManager.ON) {
                trackPowerButton.setText("Power Down");
                statusLabel.setText(Bundle.getMessage("StatTOn"));
            } else if (pm.getPower() == PowerManager.OFF) {
                trackPowerButton.setText("Power Up");
                statusLabel.setText(Bundle.getMessage("StatTOff"));
            }
        } catch (JmriException ex) {
        }
    }

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

    /*
     * Calculate the scale speed in KPH
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
    protected void avFn(float speed) {
        acc = acc - avSpeed + speed;
        avSpeed = acc / filterLength[range];
    }

    // Clear out the filter
    protected void avClr() {
        acc = 0;
        avSpeed = 0;
    }

    // When we switch range we must compensate the current accumulator
    // value for the longer filter.
    protected void switchRange() {
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

    /*
     * Display the speed
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

    // Allows user to power up and give time for sound decoder
    // startup sequence before running a profile
    protected void trackPower() {
        try {
            if (pm.getPower() != PowerManager.ON) {
                pm.setPower(PowerManager.ON);
            } else {
                stopProfile();
                pm.setPower(PowerManager.OFF);
            }
        } catch (JmriException e) {
            log.error("Exception during power on: " + e.toString());
        }
    }

    protected synchronized void startProfile() {
        if (profileAddress > 0) {
            if (dirFwdButton.isSelected() || dirRevButton.isSelected()) {
                if (state == ProfileState.IDLE) {
                    profileTimer = new javax.swing.Timer(4000, new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            profileTimeout();
                        }
                    });
                    profileTimer.setRepeats(false);
                    // Request a throttle
                    state = ProfileState.WAIT_FOR_THROTTLE;
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
                    boolean requestOK = jmri.InstanceManager.throttleManagerInstance().requestThrottle(profileAddress, profileIsLong, this);
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

    protected synchronized void stopProfile() {
        if (state != ProfileState.IDLE) {
            tidyUp();
            state = ProfileState.IDLE;
            log.info("Profiling stopped by user");
        }
    }

    @Override
    public synchronized void notifyThrottleFound(DccThrottle t) {
        profileTimer.stop();
        throttle = t;
        log.info("Throttle aquired, starting profiling");
        throttle.setSpeedStepMode(DccThrottle.SpeedStepMode28);
        if (throttle.getSpeedStepMode() != DccThrottle.SpeedStepMode28) {
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
        state = ProfileState.RUNNING;
        // Start at step 0 with 28 step packets
        profileSpeed = 0.0F;
        profileStep = 0;
        profileIncrement = throttle.getSpeedIncrement();
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
    }

    @Override
    public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
    }

    @Override
    public void notifyStealThrottleRequired(jmri.LocoAddress address) {
        // this is an automatically stealing impelementation.
        InstanceManager.throttleManagerInstance().stealThrottleRequest(address, this, true);
    }

    javax.swing.Timer replyTimer = null;
    javax.swing.Timer displayTimer = null;
    javax.swing.Timer fastDisplayTimer = null;
    javax.swing.Timer profileTimer = null;

    // Once we receive a speedoReply we expect them regularly, at
    // least once every 4 seconds
    protected void startReplyTimer() {
        replyTimer = new javax.swing.Timer(4000, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                replyTimeout();
            }
        });
        replyTimer.setRepeats(true);     // refresh until stopped by dispose
        replyTimer.start();
    }

    /**
     * Internal routine to reset the speed on a timeout.
     */
    synchronized protected void replyTimeout() {
        //log.debug("Timed out - display speed zero");
        targetSpeed = 0;
        avClr();
        oldSpeed = 0;
        showSpeed();
    }

    // Timer, used to update the target display speed.
    protected void startDisplayTimer() {
        displayTimer = new javax.swing.Timer(DISPLAY_UPDATE, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                displayTimeout();
            }
        });
        displayTimer.setRepeats(true);     // refresh until stopped by dispose
        displayTimer.start();
    }

    // Timer, used to update the display at faster rate.
    protected void startFastDisplayTimer() {
        fastDisplayTimer = new javax.swing.Timer(DISPLAY_UPDATE / FAST_DISPLAY_RATIO, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                fastDisplayTimeout();
            }
        });
        fastDisplayTimer.setRepeats(true);     // refresh until stopped by dispose
        fastDisplayTimer.start();
    }

    /**
     * Internal routine to update the target speed for display
     */
    synchronized protected void displayTimeout() {
        //log.info("Display timeout");
        targetSpeed = avSpeed;
        incSpeed = (targetSpeed - currentSpeed) / FAST_DISPLAY_RATIO;
    }

    /**
     * Internal routine to update the displayed speed
     */
    synchronized protected void fastDisplayTimeout() {
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
    synchronized protected void throttleTimeout() {
        jmri.InstanceManager.throttleManagerInstance().cancelThrottleRequest(profileAddress, profileIsLong, this);
        state = ProfileState.IDLE;
        log.error("Timeout waiting for throttle");
    }

    /**
     * Time to change to next speed increment.
     */
    protected synchronized void profileTimeout() {
        if (state == ProfileState.WAIT_FOR_THROTTLE) {
            tidyUp();
            log.error("Timeout waiting for throttle");
            statusLabel.setText(Bundle.getMessage("StatusTimeout"));
        } else if (state == ProfileState.RUNNING) {
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
                    profileSpeed += profileIncrement;
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

    protected void tidyUp() {
        profileTimer.stop();
        // turn off power
        // Turning power off is bad for some systems, e.g. Digitrax
//        try {
//            pm.setPower(PowerManager.OFF);
//        } catch (JmriException e) {
//            log.error("Exception during power off: "+e.toString());
//        }
        if (throttle != null) {
            throttle.setSpeedSetting(0.0F);
            //jmri.InstanceManager.throttleManagerInstance().cancelThrottleRequest(profileAddress, this);
            InstanceManager.throttleManagerInstance().releaseThrottle(throttle, this);
            //throttle.release();
            throttle = null;
        }
        resetGraphButton.setEnabled(true);
        state = ProfileState.IDLE;
    }

    protected void readAddress() {
        readState = ProgState.WAIT29;
        statusLabel.setText(Bundle.getMessage("ProgRd29"));
        startRead(29);
    }

    protected void startRead(int cv) {
        try {
            prog.readCV(cv, this);
        } catch (ProgrammerException e) {
            log.error("Exception reading CV " + cv + " " + e);
        }
    }

    @Override
    public void programmingOpReply(int value, int status) {
        if (status == 0) {
            switch (readState) {
                case IDLE:
                    log.debug("unexpected reply in IDLE state");
                    break;

                case WAIT29:
                    // Check extended address bit
                    if ((value & 0x20) == 0) {
                        readState = ProgState.WAIT1;
                        statusLabel.setText(Bundle.getMessage("ProgRdShort"));
                        startRead(1);
                    } else {
                        readState = ProgState.WAIT17;
                        statusLabel.setText(Bundle.getMessage("ProgRdExtended"));
                        startRead(17);
                    }
                    break;

                case WAIT1:
                    readAddress = value;
                    //profileAddressField.setText(Integer.toString(profileAddress));
                    //profileAddressField.setBackground(Color.WHITE);
                    addrSelector.setAddress(new DccLocoAddress(readAddress, false));
                    changeOfAddress();
                    readState = ProgState.IDLE;
                    break;

                case WAIT17:
                    readAddress = value;
                    readState = ProgState.WAIT18;
                    startRead(18);
                    break;

                case WAIT18:
                    readAddress = (readAddress & 0x3f) * 256 + value;
                    //profileAddressField.setText(Integer.toString(profileAddress));
                    //profileAddressField.setBackground(Color.WHITE);
                    addrSelector.setAddress(new DccLocoAddress(readAddress, true));
                    changeOfAddress();
                    statusLabel.setText(Bundle.getMessage("ProgRdComplete"));
                    readState = ProgState.IDLE;
                    break;
                default:
                    log.warn("Unhandled read state: {}", readState);
                    break;
            }
        } else {
            // Error during programming
            log.error("Status not OK during read: " + status);
            //profileAddressField.setText("Error");
            statusLabel.setText(Bundle.getMessage("ProgRdError"));
            readState = ProgState.IDLE;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SpeedoConsoleFrame.class);

}
