package jmri.jmrit.dispatcher;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import jmri.Block;
import jmri.EntryPoint;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.Scale;
import jmri.ScaleManager;
import jmri.Section;
import jmri.Sensor;
import jmri.SignalMast;
import jmri.Timebase;
import jmri.Transit;
import jmri.TransitManager;
import jmri.TransitSection;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.swing.JTablePersistenceManager;
import jmri.util.JmriJFrame;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatcher functionality, working with Sections, Transits and ActiveTrain.
 * <p>
 * Dispatcher serves as the manager for ActiveTrains. All allocation of Sections
 * to ActiveTrains is performed here.
 * <p>
 * Programming Note: Use the managed instance returned by
 * {@link jmri.InstanceManager#getDefault(java.lang.Class)} to access the
 * running Dispatcher.
 * <p>
 * Dispatcher listens to fast clock minutes to handle all ActiveTrain items tied
 * to fast clock time.
 * <p>
 * Delayed start of manual and automatic trains is enforced by not allocating
 * Sections for trains until the fast clock reaches the departure time.
 * <p>
 * This file is part of JMRI.
 * <p>
 * JMRI is open source software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Dave Duchamp Copyright (C) 2008-2011
 */
public class DispatcherFrame extends jmri.util.JmriJFrame implements InstanceManagerAutoDefault {

    public DispatcherFrame() {
        super(true, true); // remember size a position.
        initializeOptions();
        openDispatcherWindow();
        autoTurnouts = new AutoTurnouts(this);
        if (_LE != null) {
            autoAllocate = new AutoAllocate(this);
        }
        InstanceManager.getDefault(jmri.SectionManager.class).initializeBlockingSensors();
        getActiveTrainFrame();

        if (fastClock == null) {
            log.error("Failed to instantiate a fast clock when constructing Dispatcher");
        } else {
            minuteChangeListener = new java.beans.PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    //process change to new minute
                    newFastClockMinute();
                }
            };
            fastClock.addMinuteChangeListener(minuteChangeListener);
        }
    }

    /***
     *  reads thru all the traininfo files found in the dispatcher directory
     *  and loads the ones flagged as "loadAtStartup"
     */
    public void loadAtStartup() {
        log.debug("Loading saved trains flagged as LoadAtStartup");
        TrainInfoFile tif = new TrainInfoFile();
        String[] names = tif.getTrainInfoFileNames();
        log.debug("initializing block paths early"); //TODO: figure out how to prevent the "regular" init
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class)
                .initializeLayoutBlockPaths();
        if (names.length > 0) {
            for (int i = 0; i < names.length; i++) {
                TrainInfo info = null;
                try {
                    info = tif.readTrainInfo(names[i]);
                } catch (java.io.IOException ioe) {
                    log.error("IO Exception when reading train info file {}: {}", names[i], ioe);
                    continue;
                } catch (org.jdom2.JDOMException jde) {
                    log.error("JDOM Exception when reading train info file {}: {}", names[i], jde);
                    continue;
                }
                if (info != null && info.getLoadAtStartup()) {
                    if (loadTrainFromTrainInfo(info) != 0) {
                        /*
                         * Error loading occurred The error will have already
                         * been sent to the log and to screen
                         */
                    } else {
                        /* give time to set up throttles etc */
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            log.warn("Sleep Interrupted in loading trains, likely being stopped", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Constants for the override type
     */
    public static final String OVERRIDETYPE_NONE = "NONE";
    public static final String OVERRIDETYPE_USER = "USER";
    public static final String OVERRIDETYPE_DCCADDRESS = "DCCADDRESS";
    public static final String OVERRIDETYPE_OPERATIONS = "OPERATIONS";
    public static final String OVERRIDETYPE_ROSTER = "ROSTER";

    /**
     * Loads a train into the Dispatcher from a traininfo file
     *
     * @param traininfoFileName  the file name of a traininfo file.
     * @return 0 good, -1 create failure, -2 -3 file errors, -9 bother.
     */
    public int loadTrainFromTrainInfo(String traininfoFileName) {
        return loadTrainFromTrainInfo(traininfoFileName, "NONE", "");
    }

    /**
     * Loads a train into the Dispatcher from a traininfo file, overriding
     * dccaddress
     *
     * @param traininfoFileName  the file name of a traininfo file.
     * @param overRideType  "NONE", "USER", "ROSTER" or "OPERATIONS"
     * @param overRideValue  "" , dccAddress, RosterEntryName or Operations
     *            trainname.
     * @return 0 good, -1 create failure, -2 -3 file errors, -9 bother.
     */
    public int loadTrainFromTrainInfo(String traininfoFileName, String overRideType, String overRideValue) {
        //read xml data from selected filename and move it into trainfo
        try {
            // maybe called from jthon protect our selves
            TrainInfoFile tif = new TrainInfoFile();
            TrainInfo info = null;
            try {
                info = tif.readTrainInfo(traininfoFileName);
            } catch (java.io.IOException ioe) {
                log.error("IO Exception when reading train info file {}: {}", traininfoFileName, ioe);
                return -2;
            } catch (org.jdom2.JDOMException jde) {
                log.error("JDOM Exception when reading train info file {}: {}", traininfoFileName, jde);
                return -3;
            }
            return loadTrainFromTrainInfo(info, overRideType, overRideValue);
        } catch (RuntimeException ex) {
            log.error("Unexpected, uncaught exception loading traininfofile [{}]", traininfoFileName, ex);
            return -9;
        }
    }

    /**
     * Loads a train into the Dispatcher
     *
     * @param info  a completed TrainInfo class.
     * @return 0 good, -1 failure
     */
    public int loadTrainFromTrainInfo(TrainInfo info) {
        return loadTrainFromTrainInfo(info, "NONE", "");
    }

    /**
     * Loads a train into the Dispatcher
     *
     * @param info  a completed TrainInfo class.
     * @param overRideType  "NONE", "USER", "ROSTER" or "OPERATIONS"
     * @param overRideValue  "" , dccAddress, RosterEntryName or Operations
     *            trainname.
     * @return 0 good, -1 failure
     */
    public int loadTrainFromTrainInfo(TrainInfo info, String overRideType, String overRideValue) {

        log.debug("loading train:{}, startblockname:{}, destinationBlockName:{}", info.getTrainName(),
                info.getStartBlockName(), info.getDestinationBlockName());
        // create a new Active Train

        //set updefaults from traininfo
        int tSource = ActiveTrain.ROSTER;
        if (info.getTrainFromTrains()) {
            tSource = ActiveTrain.OPERATIONS;
        } else if (info.getTrainFromUser()) {
            tSource = ActiveTrain.USER;
        }
        String dccAddressToUse = info.getDccAddress();
        String trainNameToUse = info.getTrainName();

        //process override
        switch (overRideType) {
            case "":
            case OVERRIDETYPE_NONE:
                break;
            case OVERRIDETYPE_USER:
            case OVERRIDETYPE_DCCADDRESS:
                tSource = ActiveTrain.USER;
                dccAddressToUse = overRideValue;
                trainNameToUse = overRideValue;
                break;
            case OVERRIDETYPE_OPERATIONS:
                tSource = ActiveTrain.OPERATIONS;
                trainNameToUse = overRideValue;
                break;
            case OVERRIDETYPE_ROSTER:
                tSource = ActiveTrain.ROSTER;
                trainNameToUse = overRideValue;
                break;
            default:
                /* just leave as in traininfo */
        }

        ActiveTrain at = createActiveTrain(info.getTransitId(), trainNameToUse, tSource,
                info.getStartBlockId(), info.getStartBlockSeq(), info.getDestinationBlockId(),
                info.getDestinationBlockSeq(),
                info.getAutoRun(), dccAddressToUse, info.getPriority(),
                info.getResetWhenDone(), info.getReverseAtEnd(), true, null, info.getAllocationMethod());
        if (at != null) {
            if (tSource == ActiveTrain.ROSTER) {
                RosterEntry re = Roster.getDefault().getEntryForId(trainNameToUse);
                at.setRosterEntry(re);
                at.setDccAddress(re.getDccAddress());
            }
            at.setAllocateMethod(info.getAllocationMethod());
            at.setDelayedStart(info.getDelayedStart()); //this is a code: NODELAY, TIMEDDELAY, SENSORDELAY
            at.setDepartureTimeHr(info.getDepartureTimeHr()); // hour of day (fast-clock) to start this train
            at.setDepartureTimeMin(info.getDepartureTimeMin()); //minute of hour to start this train
            at.setDelayedRestart(info.getDelayedRestart()); //this is a code: NODELAY, TIMEDDELAY, SENSORDELAY
            at.setRestartDelay(info.getRestartDelayMin()); //this is number of minutes to delay between runs
            at.setDelaySensor(info.getDelaySensor());
            at.setResetStartSensor(info.getResetStartSensor());
            if ((isFastClockTimeGE(at.getDepartureTimeHr(), at.getDepartureTimeMin()) &&
                    info.getDelayedStart() != ActiveTrain.SENSORDELAY) ||
                    info.getDelayedStart() == ActiveTrain.NODELAY) {
                at.setStarted();
            }
            at.setRestartSensor(info.getRestartSensor());
            at.setResetRestartSensor(info.getResetRestartSensor());
            at.setTrainType(info.getTrainType());
            at.setTerminateWhenDone(info.getTerminateWhenDone());
            if (info.getAutoRun()) {
                AutoActiveTrain aat = new AutoActiveTrain(at);
                aat.setSpeedFactor(info.getSpeedFactor());
                aat.setMaxSpeed(info.getMaxSpeed());
                aat.setRampRate(AutoActiveTrain.getRampRateFromName(info.getRampRate()));
                aat.setResistanceWheels(info.getResistanceWheels());
                aat.setRunInReverse(info.getRunInReverse());
                aat.setSoundDecoder(info.getSoundDecoder());
                aat.setMaxTrainLength(info.getMaxTrainLength());
                aat.setStopBySpeedProfile(info.getStopBySpeedProfile());
                aat.setStopBySpeedProfileAdjust(info.getStopBySpeedProfileAdjust());
                aat.setUseSpeedProfile(info.getUseSpeedProfile());
                if (!aat.initialize()) {
                    log.error("ERROR initializing autorunning for train {}", at.getTrainName());
                    JOptionPane.showMessageDialog(dispatcherFrame, Bundle.getMessage(
                            "Error27", at.getTrainName()), Bundle.getMessage("MessageTitle"),
                            JOptionPane.INFORMATION_MESSAGE);
                    return -1;
                }
                getAutoTrainsFrame().addAutoActiveTrain(aat);
            }
            allocateNewActiveTrain(at);
            newTrainDone(at);

        } else {
            log.warn("failed to create create Active Train {}", info.getTrainName());
            return -1;
        }
        return 0;
    }

    // Dispatcher options (saved to disk if user requests, and restored if present)
    private LayoutEditor _LE = null;
    public static final int SIGNALHEAD = 0x00;
    public static final int SIGNALMAST = 0x01;
    private int _SignalType = SIGNALHEAD;
    private String _StoppingSpeedName = "RestrictedSlow";
    private boolean _UseConnectivity = false;
    private boolean _HasOccupancyDetection = false; // "true" if blocks have occupancy detection
    private boolean _TrainsFromRoster = true;
    private boolean _TrainsFromTrains = false;
    private boolean _TrainsFromUser = false;
    private boolean _AutoAllocate = false;
    private boolean _AutoTurnouts = false;
    private boolean _TrustKnownTurnouts = false;
    private boolean _ShortActiveTrainNames = false;
    private boolean _ShortNameInBlock = true;
    private boolean _RosterEntryInBlock = false;
    private boolean _ExtraColorForAllocated = true;
    private boolean _NameInAllocatedBlock = false;
    private boolean _UseScaleMeters = false;  // "true" if scale meters, "false" for scale feet
    private Scale _LayoutScale = ScaleManager.getScale("HO");
    private boolean _SupportVSDecoder = false;
    private int _MinThrottleInterval = 100; //default time (in ms) between consecutive throttle commands
    private int _FullRampTime = 10000; //default time (in ms) for RAMP_FAST to go from 0% to 100%
    private float maximumLineSpeed = 0.0f;

    // operational instance variables
    private final List<ActiveTrain> activeTrainsList = new ArrayList<>();  // list of ActiveTrain objects
    private final List<java.beans.PropertyChangeListener> _atListeners
            = new ArrayList<>();
    private final List<ActiveTrain> delayedTrains = new ArrayList<>();  // list of delayed Active Trains
    private final List<ActiveTrain> restartingTrainsList = new ArrayList<>();  // list of Active Trains with restart requests
    private final TransitManager transitManager = InstanceManager.getDefault(jmri.TransitManager.class);
    private final List<AllocationRequest> allocationRequests = new ArrayList<>();  // List of AllocatedRequest objects
    private final List<AllocatedSection> allocatedSections = new ArrayList<>();  // List of AllocatedSection objects
    private boolean optionsRead = false;
    private AutoTurnouts autoTurnouts = null;
    private AutoAllocate autoAllocate = null;
    private OptionsMenu optionsMenu = null;
    private ActivateTrainFrame atFrame = null;

    public ActivateTrainFrame getActiveTrainFrame() {
        if (atFrame == null) {
            atFrame = new ActivateTrainFrame(this);
        }
        return atFrame;
    }
    private boolean newTrainActive = false;

    public boolean getNewTrainActive() {
        return newTrainActive;
    }

    public void setNewTrainActive(boolean boo) {
        newTrainActive = boo;
    }
    private AutoTrainsFrame _autoTrainsFrame = null;
    private final Timebase fastClock = InstanceManager.getNullableDefault(jmri.Timebase.class);
    private final Sensor fastClockSensor = InstanceManager.sensorManagerInstance().provideSensor("ISCLOCKRUNNING");
    private transient java.beans.PropertyChangeListener minuteChangeListener = null;

    // dispatcher window variables
    protected JmriJFrame dispatcherFrame = null;
    private Container contentPane = null;
    private ActiveTrainsTableModel activeTrainsTableModel = null;
    private JButton addTrainButton = null;
    private JButton terminateTrainButton = null;
    private JButton cancelRestartButton = null;
    private JButton allocateExtraButton = null;
    private JCheckBox autoReleaseBox = null;
    private JCheckBox autoAllocateBox = null;
    private AllocationRequestTableModel allocationRequestTableModel = null;
    private AllocatedSectionTableModel allocatedSectionTableModel = null;

    void initializeOptions() {
        if (optionsRead) {
            return;
        }
        optionsRead = true;
        try {
            InstanceManager.getDefault(OptionsFile.class).readDispatcherOptions(this);
        } catch (org.jdom2.JDOMException jde) {
            log.error("JDOM Exception when retrieving dispatcher options {}", jde);
        } catch (java.io.IOException ioe) {
            log.error("I/O Exception when retrieving dispatcher options {}", ioe);
        }
    }

    void openDispatcherWindow() {
        if (dispatcherFrame == null) {
            dispatcherFrame = this;
            dispatcherFrame.setTitle(Bundle.getMessage("TitleDispatcher"));
            JMenuBar menuBar = new JMenuBar();
            optionsMenu = new OptionsMenu(this);
            menuBar.add(optionsMenu);
            setJMenuBar(menuBar);
            dispatcherFrame.addHelpMenu("package.jmri.jmrit.dispatcher.Dispatcher", true);
            contentPane = dispatcherFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

            // set up active trains table
            JPanel p11 = new JPanel();
            p11.setLayout(new FlowLayout());
            p11.add(new JLabel(Bundle.getMessage("ActiveTrainTableTitle")));
            contentPane.add(p11);
            JPanel p12 = new JPanel();
            p12.setLayout(new BorderLayout());
             activeTrainsTableModel = new ActiveTrainsTableModel();
            JTable activeTrainsTable = new JTable(activeTrainsTableModel);
            activeTrainsTable.setName(this.getClass().getName().concat(":activeTrainsTableModel"));
            activeTrainsTable.setRowSelectionAllowed(false);
            activeTrainsTable.setPreferredScrollableViewportSize(new java.awt.Dimension(950, 160));
            activeTrainsTable.setColumnModel(new XTableColumnModel());
            activeTrainsTable.createDefaultColumnsFromModel();
            XTableColumnModel activeTrainsColumnModel = (XTableColumnModel)activeTrainsTable.getColumnModel();
            // Button Columns
            TableColumn allocateButtonColumn = activeTrainsColumnModel.getColumn(ActiveTrainsTableModel.ALLOCATEBUTTON_COLUMN);
            allocateButtonColumn.setCellEditor(new ButtonEditor(new JButton()));
            allocateButtonColumn.setResizable(true);
            ButtonRenderer buttonRenderer = new ButtonRenderer();
            activeTrainsTable.setDefaultRenderer(JButton.class, buttonRenderer);
            JButton sampleButton = new JButton("WWW..."); //by default 3 letters and elipse
            activeTrainsTable.setRowHeight(sampleButton.getPreferredSize().height);
            allocateButtonColumn.setPreferredWidth((sampleButton.getPreferredSize().width) + 2);
            TableColumn terminateTrainButtonColumn = activeTrainsColumnModel.getColumn(ActiveTrainsTableModel.TERMINATEBUTTON_COLUMN);
            terminateTrainButtonColumn.setCellEditor(new ButtonEditor(new JButton()));
            terminateTrainButtonColumn.setResizable(true);
            buttonRenderer = new ButtonRenderer();
            activeTrainsTable.setDefaultRenderer(JButton.class, buttonRenderer);
            sampleButton = new JButton("WWW...");
            activeTrainsTable.setRowHeight(sampleButton.getPreferredSize().height);
            terminateTrainButtonColumn.setPreferredWidth((sampleButton.getPreferredSize().width) + 2);

            addMouseListenerToHeader(activeTrainsTable);

            activeTrainsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            JScrollPane activeTrainsTableScrollPane = new JScrollPane(activeTrainsTable);
            p12.add(activeTrainsTableScrollPane, BorderLayout.CENTER);
            contentPane.add(p12);

            JPanel p13 = new JPanel();
            p13.setLayout(new FlowLayout());
            p13.add(addTrainButton = new JButton(Bundle.getMessage("InitiateTrain") + "..."));
            addTrainButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!newTrainActive) {
                        atFrame.initiateTrain(e);
                        newTrainActive = true;
                    } else {
                        atFrame.showActivateFrame();
                    }
                }
            });
            addTrainButton.setToolTipText(Bundle.getMessage("InitiateTrainButtonHint"));
            p13.add(new JLabel("   "));
            p13.add(new JLabel("   "));
            p13.add(allocateExtraButton = new JButton(Bundle.getMessage("AllocateExtra") + "..."));
            allocateExtraButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    allocateExtraSection(e);
                }
            });
            allocateExtraButton.setToolTipText(Bundle.getMessage("AllocateExtraButtonHint"));
            p13.add(new JLabel("   "));
            p13.add(cancelRestartButton = new JButton(Bundle.getMessage("CancelRestart") + "..."));
            cancelRestartButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!newTrainActive) {
                        cancelRestart(e);
                    } else if (restartingTrainsList.size() > 0) {
                        atFrame.showActivateFrame();
                        JOptionPane.showMessageDialog(dispatcherFrame, Bundle.getMessage("Message2"),
                                Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        atFrame.showActivateFrame();
                    }
                }
            });
            cancelRestartButton.setToolTipText(Bundle.getMessage("CancelRestartButtonHint"));
            p13.add(new JLabel("   "));
            p13.add(terminateTrainButton = new JButton(Bundle.getMessage("TerminateTrain"))); // immediate if there is only one train
            terminateTrainButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!newTrainActive) {
                        terminateTrain(e);
                    } else if (activeTrainsList.size() > 0) {
                        atFrame.showActivateFrame();
                        JOptionPane.showMessageDialog(dispatcherFrame, Bundle.getMessage("Message1"),
                                Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        atFrame.showActivateFrame();
                    }
                }
            });
            terminateTrainButton.setToolTipText(Bundle.getMessage("TerminateTrainButtonHint"));
            contentPane.add(p13);

            // Reset and then persist the table's ui state
            JTablePersistenceManager tpm = InstanceManager.getNullableDefault(JTablePersistenceManager.class);
            if (tpm != null) {
                tpm.resetState(activeTrainsTable);
                tpm.persist(activeTrainsTable);
            }

            // set up pending allocations table
            contentPane.add(new JSeparator());
            JPanel p21 = new JPanel();
            p21.setLayout(new FlowLayout());
            p21.add(new JLabel(Bundle.getMessage("RequestedAllocationsTableTitle")));
            contentPane.add(p21);
            JPanel p22 = new JPanel();
            p22.setLayout(new BorderLayout());
            allocationRequestTableModel = new AllocationRequestTableModel();
            JTable allocationRequestTable = new JTable(allocationRequestTableModel);
            allocationRequestTable.setName(this.getClass().getName().concat(":allocationRequestTable"));
            allocationRequestTable.setRowSelectionAllowed(false);
            allocationRequestTable.setPreferredScrollableViewportSize(new java.awt.Dimension(950, 100));
            allocationRequestTable.setColumnModel(new XTableColumnModel());
            allocationRequestTable.createDefaultColumnsFromModel();
            XTableColumnModel allocationRequestColumnModel = (XTableColumnModel)allocationRequestTable.getColumnModel();
            // Button Columns
            TableColumn allocateColumn = allocationRequestColumnModel.getColumn(AllocationRequestTableModel.ALLOCATEBUTTON_COLUMN);
            allocateColumn.setCellEditor(new ButtonEditor(new JButton()));
            allocateColumn.setResizable(true);
            buttonRenderer = new ButtonRenderer();
            allocationRequestTable.setDefaultRenderer(JButton.class, buttonRenderer);
            sampleButton = new JButton(Bundle.getMessage("AllocateButton"));
            allocationRequestTable.setRowHeight(sampleButton.getPreferredSize().height);
            allocateColumn.setPreferredWidth((sampleButton.getPreferredSize().width) + 2);
            TableColumn cancelButtonColumn = allocationRequestColumnModel.getColumn(AllocationRequestTableModel.CANCELBUTTON_COLUMN);
            cancelButtonColumn.setCellEditor(new ButtonEditor(new JButton()));
            cancelButtonColumn.setResizable(true);
            cancelButtonColumn.setPreferredWidth((sampleButton.getPreferredSize().width) + 2);
            // add listener
            addMouseListenerToHeader(allocationRequestTable);
            allocationRequestTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            JScrollPane allocationRequestTableScrollPane = new JScrollPane(allocationRequestTable);
            p22.add(allocationRequestTableScrollPane, BorderLayout.CENTER);
            contentPane.add(p22);
            if (tpm != null) {
                tpm.resetState(allocationRequestTable);
                tpm.persist(allocationRequestTable);
            }

            // set up allocated sections table
            contentPane.add(new JSeparator());
            JPanel p30 = new JPanel();
            p30.setLayout(new FlowLayout());
            p30.add(new JLabel(Bundle.getMessage("AllocatedSectionsTitle") + "    "));
            autoAllocateBox = new JCheckBox(Bundle.getMessage("AutoDispatchItem"));
            p30.add(autoAllocateBox);
            autoAllocateBox.setToolTipText(Bundle.getMessage("AutoAllocateBoxHint"));
            autoAllocateBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleAutoAllocateChanged(e);
                }
            });
            autoAllocateBox.setSelected(_AutoAllocate);
            autoReleaseBox = new JCheckBox(Bundle.getMessage("AutoReleaseBoxLabel"));
            p30.add(autoReleaseBox);
            autoReleaseBox.setToolTipText(Bundle.getMessage("AutoReleaseBoxHint"));
            autoReleaseBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleAutoReleaseChanged(e);
                }
            });
            autoReleaseBox.setSelected(_AutoAllocate); // initialize autoRelease to match autoAllocate
            contentPane.add(p30);
            JPanel p31 = new JPanel();
            p31.setLayout(new BorderLayout());
            allocatedSectionTableModel = new AllocatedSectionTableModel();
            JTable allocatedSectionTable = new JTable(allocatedSectionTableModel);
            allocatedSectionTable.setName(this.getClass().getName().concat(":allocatedSectionTable"));
            allocatedSectionTable.setRowSelectionAllowed(false);
            allocatedSectionTable.setPreferredScrollableViewportSize(new java.awt.Dimension(730, 200));
            allocatedSectionTable.setColumnModel(new XTableColumnModel());
            allocatedSectionTable.createDefaultColumnsFromModel();
            XTableColumnModel allocatedSectionColumnModel = (XTableColumnModel)allocatedSectionTable.getColumnModel();
            // Button columns
            TableColumn releaseColumn = allocatedSectionColumnModel.getColumn(AllocatedSectionTableModel.RELEASEBUTTON_COLUMN);
            releaseColumn.setCellEditor(new ButtonEditor(new JButton()));
            releaseColumn.setResizable(true);
            allocatedSectionTable.setDefaultRenderer(JButton.class, buttonRenderer);
            JButton sampleAButton = new JButton(Bundle.getMessage("ReleaseButton"));
            allocatedSectionTable.setRowHeight(sampleAButton.getPreferredSize().height);
            releaseColumn.setPreferredWidth((sampleAButton.getPreferredSize().width) + 2);
            JScrollPane allocatedSectionTableScrollPane = new JScrollPane(allocatedSectionTable);
            p31.add(allocatedSectionTableScrollPane, BorderLayout.CENTER);
            // add listener
            addMouseListenerToHeader(allocatedSectionTable);
            allocatedSectionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            contentPane.add(p31);
            if (tpm != null) {
                tpm.resetState(allocatedSectionTable);
                tpm.persist(allocatedSectionTable);
            }
        }
        dispatcherFrame.pack();
        dispatcherFrame.setVisible(true);
    }

    void releaseAllocatedSectionFromTable(int index) {
        AllocatedSection as = allocatedSections.get(index);
        releaseAllocatedSection(as, false);
    }

    // allocate extra window variables
    private JmriJFrame extraFrame = null;
    private Container extraPane = null;
    private final JComboBox<String> atSelectBox = new JComboBox<>();
    private final JComboBox<String> extraBox = new JComboBox<>();
    private final List<Section> extraBoxList = new ArrayList<>();
    private int atSelectedIndex = -1;

    public void allocateExtraSection(ActionEvent e, ActiveTrain at) {
        allocateExtraSection(e);
        if (_ShortActiveTrainNames) {
            atSelectBox.setSelectedItem(at.getTrainName());
        } else {
            atSelectBox.setSelectedItem(at.getActiveTrainName());
        }
    }

    // allocate an extra Section to an Active Train
    private void allocateExtraSection(ActionEvent e) {
        if (extraFrame == null) {
            extraFrame = new JmriJFrame(Bundle.getMessage("ExtraTitle"));
            extraFrame.addHelpMenu("package.jmri.jmrit.dispatcher.AllocateExtra", true);
            extraPane = extraFrame.getContentPane();
            extraPane.setLayout(new BoxLayout(extraFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(new JLabel(Bundle.getMessage("ActiveColumnTitle") + ":"));
            p1.add(atSelectBox);
            atSelectBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleATSelectionChanged(e);
                }
            });
            atSelectBox.setToolTipText(Bundle.getMessage("ATBoxHint"));
            extraPane.add(p1);
            JPanel p2 = new JPanel();
            p2.setLayout(new FlowLayout());
            p2.add(new JLabel(Bundle.getMessage("ExtraBoxLabel") + ":"));
            p2.add(extraBox);
            extraBox.setToolTipText(Bundle.getMessage("ExtraBoxHint"));
            extraPane.add(p2);
            JPanel p7 = new JPanel();
            p7.setLayout(new FlowLayout());
            JButton cancelButton = null;
            p7.add(cancelButton = new JButton(Bundle.getMessage("ButtonCancel")));
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelExtraRequested(e);
                }
            });
            cancelButton.setToolTipText(Bundle.getMessage("CancelExtraHint"));
            p7.add(new JLabel("    "));
            JButton aExtraButton = null;
            p7.add(aExtraButton = new JButton(Bundle.getMessage("AllocateButton")));
            aExtraButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addExtraRequested(e);
                }
            });
            aExtraButton.setToolTipText(Bundle.getMessage("AllocateButtonHint"));
            extraPane.add(p7);
        }
        initializeATComboBox();
        initializeExtraComboBox();
        extraFrame.pack();
        extraFrame.setVisible(true);
    }

    private void handleAutoAllocateChanged(ActionEvent e) {
        setAutoAllocate(autoAllocateBox.isSelected());
        if (optionsMenu != null) {
            optionsMenu.initializeMenu();
        }
        if (_AutoAllocate) {
            autoAllocate.scanAllocationRequestList(allocationRequests);
        }
    }

    protected void forceScanOfAllocation() {
        if (_AutoAllocate) {
            autoAllocate.scanAllocationRequestList(allocationRequests);
        }
    }

    private void handleAutoReleaseChanged(ActionEvent e) {
        if (autoReleaseBox.isSelected()) {
            checkAutoRelease();
        }
    }

    private void handleATSelectionChanged(ActionEvent e) {
        atSelectedIndex = atSelectBox.getSelectedIndex();
        initializeExtraComboBox();
        extraFrame.pack();
        extraFrame.setVisible(true);
    }

    private void initializeATComboBox() {
        atSelectedIndex = -1;
        atSelectBox.removeAllItems();
        for (int i = 0; i < activeTrainsList.size(); i++) {
            ActiveTrain at = activeTrainsList.get(i);
            if (_ShortActiveTrainNames) {
                atSelectBox.addItem(at.getTrainName());
            } else {
                atSelectBox.addItem(at.getActiveTrainName());
            }
        }
        if (activeTrainsList.size() > 0) {
            atSelectBox.setSelectedIndex(0);
            atSelectedIndex = 0;
        }
    }

    private void initializeExtraComboBox() {
        extraBox.removeAllItems();
        extraBoxList.clear();
        if (atSelectedIndex < 0) {
            return;
        }
        ActiveTrain at = activeTrainsList.get(atSelectedIndex);
        //Transit t = at.getTransit();
        List<AllocatedSection> allocatedSectionList = at.getAllocatedSectionList();
        for (Section s : InstanceManager.getDefault(jmri.SectionManager.class).getNamedBeanSet()) {
            if (s.getState() == Section.FREE) {
                // not already allocated, check connectivity to this train's allocated sections
                boolean connected = false;
                for (int k = 0; k < allocatedSectionList.size(); k++) {
                    if (connected(s, allocatedSectionList.get(k).getSection())) {
                        connected = true;
                    }
                }
                if (connected) {
                    // add to the combo box, not allocated and connected to allocated
                    extraBoxList.add(s);
                    extraBox.addItem(getSectionName(s));
                }
            }
        }
        if (extraBoxList.size() > 0) {
            extraBox.setSelectedIndex(0);
        }
    }

    private boolean connected(Section s1, Section s2) {
        if ((s1 != null) && (s2 != null)) {
            List<EntryPoint> s1Entries = s1.getEntryPointList();
            List<EntryPoint> s2Entries = s2.getEntryPointList();
            for (int i = 0; i < s1Entries.size(); i++) {
                Block b = s1Entries.get(i).getFromBlock();
                for (int j = 0; j < s2Entries.size(); j++) {
                    if (b == s2Entries.get(j).getBlock()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getSectionName(Section sec) {
        String s = sec.getSystemName();
        String u = sec.getUserName();
        if ((u != null) && (!u.equals("") && (!u.equals(s)))) {
            return (s + "(" + u + ")");
        }
        return s;
    }

    private void cancelExtraRequested(ActionEvent e) {
        extraFrame.setVisible(false);
        extraFrame.dispose();   // prevent listing in the Window menu.
        extraFrame = null;
    }

    private void addExtraRequested(ActionEvent e) {
        int index = extraBox.getSelectedIndex();
        if ((atSelectedIndex < 0) || (index < 0)) {
            cancelExtraRequested(e);
            return;
        }
        ActiveTrain at = activeTrainsList.get(atSelectedIndex);
        Transit t = at.getTransit();
        Section s = extraBoxList.get(index);
        //Section ns = null;
        AllocationRequest ar = null;
        boolean requested = false;
        if (t.containsSection(s)) {
            if (s == at.getNextSectionToAllocate()) {
                // this is a request that the next section in the transit be allocated
                allocateNextRequested(atSelectedIndex);
                return;
            } else {
                // requesting allocation of a section in the Transit, but not the next Section
                int seq = -99;
                List<Integer> seqList = t.getSeqListBySection(s);
                if (seqList.size() > 0) {
                    seq = seqList.get(0);
                }
                if (seqList.size() > 1) {
                    // this section is in the Transit multiple times
                    int test = at.getNextSectionSeqNumber() - 1;
                    int diff = java.lang.Math.abs(seq - test);
                    for (int i = 1; i < seqList.size(); i++) {
                        if (diff > java.lang.Math.abs(test - seqList.get(i))) {
                            seq = seqList.get(i);
                            diff = java.lang.Math.abs(seq - test);
                        }
                    }
                }
                requested = requestAllocation(at, s, at.getAllocationDirectionFromSectionAndSeq(s, seq),
                        seq, true, extraFrame);
                ar = findAllocationRequestInQueue(s, seq,
                        at.getAllocationDirectionFromSectionAndSeq(s, seq), at);
            }
        } else {
            // requesting allocation of a section outside of the Transit, direction set arbitrary
            requested = requestAllocation(at, s, Section.FORWARD, -99, true, extraFrame);
            ar = findAllocationRequestInQueue(s, -99, Section.FORWARD, at);
        }
        // if allocation request is OK, allocate the Section, if not already allocated
        if (requested && (ar != null)) {
            allocateSection(ar, null);
        }
        if (extraFrame != null) {
            extraFrame.setVisible(false);
            extraFrame.dispose();   // prevent listing in the Window menu.
            extraFrame = null;
        }
    }

    /**
     * Extend the allocation of a section to a active train. Allows a dispatcher
     * to manually route a train to its final destination.
     *
     * @param s      the section to allocate
     * @param at     the associated train
     * @param jFrame the window to update
     * @return true if section was allocated; false otherwise
     */
    public boolean extendActiveTrainsPath(Section s, ActiveTrain at, JmriJFrame jFrame) {
        if (s.getEntryPointFromSection(at.getEndBlockSection(), Section.FORWARD) != null
                && at.getNextSectionToAllocate() == null) {

            int seq = at.getEndBlockSectionSequenceNumber() + 1;
            if (!at.addEndSection(s, seq)) {
                return false;
            }
            jmri.TransitSection ts = new jmri.TransitSection(s, seq, Section.FORWARD);
            ts.setTemporary(true);
            at.getTransit().addTransitSection(ts);

            // requesting allocation of a section outside of the Transit, direction set arbitrary
            boolean requested = requestAllocation(at, s, Section.FORWARD, seq, true, jFrame);

            AllocationRequest ar = findAllocationRequestInQueue(s, seq, Section.FORWARD, at);
            // if allocation request is OK, force an allocation the Section so that the dispatcher can then allocate futher paths through
            if (requested && (ar != null)) {
                allocateSection(ar, null);
                return true;
            }
        }
        return false;
    }

    public boolean removeFromActiveTrainPath(Section s, ActiveTrain at, JmriJFrame jFrame) {
        if (s == null || at == null) {
            return false;
        }
        if (at.getEndBlockSection() != s) {
            log.error("Active trains end section " + at.getEndBlockSection().getDisplayName() + " is not the same as the requested section to remove " + s.getDisplayName());
            return false;
        }
        if (!at.getTransit().removeLastTemporarySection(s)) {
            return false;
        }

        //Need to find allocation and remove from list.
        for (int k = allocatedSections.size(); k > 0; k--) {
            if (at == allocatedSections.get(k - 1).getActiveTrain()
                    && allocatedSections.get(k - 1).getSection() == s) {
                releaseAllocatedSection(allocatedSections.get(k - 1), true);
            }
        }
        at.removeLastAllocatedSection();
        return true;
    }

    // cancel the automatic restart request of an Active Train from the button in the Dispatcher window
    void cancelRestart(ActionEvent e) {
        ActiveTrain at = null;
        if (restartingTrainsList.size() == 1) {
            at = restartingTrainsList.get(0);
        } else if (restartingTrainsList.size() > 1) {
            Object choices[] = new Object[restartingTrainsList.size()];
            for (int i = 0; i < restartingTrainsList.size(); i++) {
                if (_ShortActiveTrainNames) {
                    choices[i] = restartingTrainsList.get(i).getTrainName();
                } else {
                    choices[i] = restartingTrainsList.get(i).getActiveTrainName();
                }
            }
            Object selName = JOptionPane.showInputDialog(dispatcherFrame,
                    Bundle.getMessage("CancelRestartChoice"),
                    Bundle.getMessage("CancelRestartTitle"), JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
            if (selName == null) {
                return;
            }
            for (int j = 0; j < restartingTrainsList.size(); j++) {
                if (selName.equals(choices[j])) {
                    at = restartingTrainsList.get(j);
                }
            }
        }
        if (at != null) {
            at.setResetWhenDone(false);
            for (int j = restartingTrainsList.size(); j > 0; j--) {
                if (restartingTrainsList.get(j - 1) == at) {
                    restartingTrainsList.remove(j - 1);
                    return;
                }
            }
        }
    }

    // terminate an Active Train from the button in the Dispatcher window
    void terminateTrain(ActionEvent e) {
        ActiveTrain at = null;
        if (activeTrainsList.size() == 1) {
            at = activeTrainsList.get(0);
        } else if (activeTrainsList.size() > 1) {
            Object choices[] = new Object[activeTrainsList.size()];
            for (int i = 0; i < activeTrainsList.size(); i++) {
                if (_ShortActiveTrainNames) {
                    choices[i] = activeTrainsList.get(i).getTrainName();
                } else {
                    choices[i] = activeTrainsList.get(i).getActiveTrainName();
                }
            }
            Object selName = JOptionPane.showInputDialog(dispatcherFrame,
                    Bundle.getMessage("TerminateTrainChoice"),
                    Bundle.getMessage("TerminateTrainTitle"), JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
            if (selName == null) {
                return;
            }
            for (int j = 0; j < activeTrainsList.size(); j++) {
                if (selName.equals(choices[j])) {
                    at = activeTrainsList.get(j);
                }
            }
        }
        if (at != null) {
            terminateActiveTrain(at);
        }
    }

    // allocate the next section for an ActiveTrain at dispatcher's request
    void allocateNextRequested(int index) {
        // set up an Allocation Request
        ActiveTrain at = activeTrainsList.get(index);
        Section next = at.getNextSectionToAllocate();
        if (next == null) {
            return;
        }
        int seqNext = at.getNextSectionSeqNumber();
        int dirNext = at.getAllocationDirectionFromSectionAndSeq(next, seqNext);
        if (requestAllocation(at, next, dirNext, seqNext, true, dispatcherFrame)) {
            AllocationRequest ar = findAllocationRequestInQueue(next, seqNext, dirNext, at);
            if (ar == null) {
                return;
            }
            // attempt to allocate
            allocateSection(ar, null);
        }
    }

    /**
     * Creates a new ActiveTrain, and registers it with Dispatcher.
     *
     * @param transitID                       system or user name of a Transit
     *                                        in the Transit Table
     * @param trainID                         any text that identifies the train
     * @param tSource                         either ROSTER, OPERATIONS, or USER
     *                                        (see ActiveTrain.java)
     * @param startBlockName                  system or user name of Block where
     *                                        train currently resides
     * @param startBlockSectionSequenceNumber sequence number in the Transit of
     *                                        the Section containing the
     *                                        startBlock (if the startBlock is
     *                                        within the Transit), or of the
     *                                        Section the train will enter from
     *                                        the startBlock (if the startBlock
     *                                        is outside the Transit)
     * @param endBlockName                    system or user name of Block where
     *                                        train will end up after its
     *                                        transit
     * @param endBlockSectionSequenceNumber   sequence number in the Transit of
     *                                        the Section containing the
     *                                        endBlock.
     * @param autoRun                         set to "true" if computer is to
     *                                        run the train automatically,
     *                                        otherwise "false"
     * @param dccAddress                      required if "autoRun" is "true",
     *                                        set to null otherwise
     * @param priority                        any integer, higher number is
     *                                        higher priority. Used to arbitrate
     *                                        allocation request conflicts
     * @param resetWhenDone                   set to "true" if the Active Train
     *                                        is capable of continuous running
     *                                        and the user has requested that it
     *                                        be automatically reset for another
     *                                        run thru its Transit each time it
     *                                        completes running through its
     *                                        Transit.
     * @param reverseAtEnd                    true if train should automatically
     *                                        reverse at end of transit; false
     *                                        otherwise
     * @param showErrorMessages               "true" if error message dialogs
     *                                        are to be displayed for detected
     *                                        errors Set to "false" to suppress
     *                                        error message dialogs from this
     *                                        method.
     * @param frame                           window request is from, or "null"
     *                                        if not from a window
     * @param allocateMethod                  How allocations will be performed.
     *                                        999 - Allocate as many section from start to finish as it can
     *                                        0 - Allocate to the next "Safe" section. If it cannot allocate all the way to
     *                                        the next "safe" section it does not allocate any sections. It will
     *                                        not allocate beyond the next safe section until it arrives there. This
     *                                        is useful for bidirectional single track running.
     *                                        Any other positive number (in reality thats 1-150 as the create transit
     *                                        allows a max of 150 sections) allocate the specified number of sections a head.
     * @return a new ActiveTrain or null on failure
     */
    public ActiveTrain createActiveTrain(String transitID, String trainID, int tSource, String startBlockName,
            int startBlockSectionSequenceNumber, String endBlockName, int endBlockSectionSequenceNumber,
            boolean autoRun, String dccAddress, int priority, boolean resetWhenDone, boolean reverseAtEnd,
            boolean showErrorMessages, JmriJFrame frame, int allocateMethod) {
        log.debug("trainID:{}, tSource:{}, startBlockName:{}, startBlockSectionSequenceNumber:{}, endBlockName:{}, endBlockSectionSequenceNumber:{}",
                trainID,tSource,startBlockName,startBlockSectionSequenceNumber,endBlockName,endBlockSectionSequenceNumber);
        // validate input
        Transit t = transitManager.getTransit(transitID);
        if (t == null) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(Bundle.getMessage(
                        "Error1"), new Object[]{transitID}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
            }
            log.error("Bad Transit name '{}' when attempting to create an Active Train", transitID);
            return null;
        }
        if (t.getState() != Transit.IDLE) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(Bundle.getMessage(
                        "Error2"), new Object[]{transitID}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
            }
            log.error("Transit '{}' not IDLE, cannot create an Active Train", transitID);
            return null;
        }
        if ((trainID == null) || trainID.equals("")) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, Bundle.getMessage("Error3"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
            log.error("TrainID string not provided, cannot create an Active Train");
            return null;
        }
        if ((tSource != ActiveTrain.ROSTER) && (tSource != ActiveTrain.OPERATIONS)
                && (tSource != ActiveTrain.USER)) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, Bundle.getMessage("Error21"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
            log.error("Train source is invalid - {} - cannot create an Active Train", tSource);
            return null;
        }
        Block startBlock = InstanceManager.getDefault(jmri.BlockManager.class).getBlock(startBlockName);
        if (startBlock == null) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(Bundle.getMessage(
                        "Error4"), new Object[]{startBlockName}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
            }
            log.error("Bad startBlockName '{}' when attempting to create an Active Train", startBlockName);
            return null;
        }
        if (isInAllocatedSection(startBlock)) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(Bundle.getMessage(
                        "Error5"), new Object[]{startBlock.getDisplayName()}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
            }
            log.error("Start block '{}' in allocated Section, cannot create an Active Train", startBlockName);
            return null;
        }
        if (_HasOccupancyDetection && (!(startBlock.getState() == Block.OCCUPIED))) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(Bundle.getMessage(
                        "Error6"), new Object[]{startBlock.getDisplayName()}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
            }
            log.error("No train in start block '{}', cannot create an Active Train", startBlockName);
            return null;
        }
        if (startBlockSectionSequenceNumber <= 0) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, Bundle.getMessage("Error12"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
        } else if (startBlockSectionSequenceNumber > t.getMaxSequence()) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(Bundle.getMessage(
                        "Error13"), new Object[]{"" + startBlockSectionSequenceNumber}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
            log.error("Invalid sequence number '{}' when attempting to create an Active Train", startBlockSectionSequenceNumber);
            return null;
        }
        Block endBlock = InstanceManager.getDefault(jmri.BlockManager.class).getBlock(endBlockName);
        if ((endBlock == null) || (!t.containsBlock(endBlock))) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(Bundle.getMessage(
                        "Error7"), new Object[]{endBlockName}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
            }
            log.error("Bad endBlockName '{}' when attempting to create an Active Train", endBlockName);
            return null;
        }
        if ((endBlockSectionSequenceNumber <= 0) && (t.getBlockCount(endBlock) > 1)) {
            JOptionPane.showMessageDialog(frame, Bundle.getMessage("Error8"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
        } else if (endBlockSectionSequenceNumber > t.getMaxSequence()) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(Bundle.getMessage(
                        "Error9"), new Object[]{"" + endBlockSectionSequenceNumber}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
            log.error("Invalid sequence number '{}' when attempting to create an Active Train", endBlockSectionSequenceNumber);
            return null;
        }
        if ((!reverseAtEnd) && resetWhenDone && (!t.canBeResetWhenDone())) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(Bundle.getMessage(
                        "Error26"), new Object[]{(t.getSystemName() + "(" + t.getUserName() + ")")}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
            log.error("Incompatible Transit set up and request to Reset When Done when attempting to create an Active Train");
            return null;
        }
        if (autoRun && ((dccAddress == null) || dccAddress.equals(""))) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, Bundle.getMessage("Error10"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
            log.error("AutoRun requested without a dccAddress when attempting to create an Active Train");
            return null;
        }
        if (autoRun) {
            if (_autoTrainsFrame == null) {
                // This is the first automatic active train--check if all required options are present
                //   for automatic running.  First check for layout editor panel
                if (!_UseConnectivity || (_LE == null)) {
                    if (showErrorMessages) {
                        JOptionPane.showMessageDialog(frame, Bundle.getMessage("Error33"),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                        log.error("AutoRun requested without a LayoutEditor panel for connectivity.");
                        return null;
                    }
                }
                if (!_HasOccupancyDetection) {
                    if (showErrorMessages) {
                        JOptionPane.showMessageDialog(frame, Bundle.getMessage("Error35"),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                        log.error("AutoRun requested without occupancy detection.");
                        return null;
                    }
                }
                // get Maximum line speed once. We need to use this when the current signal mast is null.
                for (int iSM = 0; iSM <_LE.signalMastList.size();  iSM++ )  {
                    float msl = _LE.signalMastList.get(iSM).getSignalMast().getSignalSystem().getMaximumLineSpeed();
                    if ( msl > maximumLineSpeed ) {
                        maximumLineSpeed = msl;
                    }
                }
            }
            // check/set Transit specific items for automatic running
            // validate connectivity for all Sections in this transit
            int numErrors = t.validateConnectivity(_LE);
            if (numErrors != 0) {
                if (showErrorMessages) {
                    JOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(Bundle.getMessage(
                            "Error34"), new Object[]{("" + numErrors)}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }
            // check/set direction sensors in signal logic for all Sections in this Transit.
            if (getSignalType() == SIGNALHEAD) {
                numErrors = t.checkSignals(_LE);
                if (numErrors == 0) {
                    t.initializeBlockingSensors();
                }
                if (numErrors != 0) {
                    if (showErrorMessages) {
                        JOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(Bundle.getMessage(
                                "Error36"), new Object[]{("" + numErrors)}),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
            }
            // TODO: Need to check signalMasts as well
            // this train is OK, activate the AutoTrains window, if needed
            if (_autoTrainsFrame == null) {
                _autoTrainsFrame = new AutoTrainsFrame(this);
            } else {
                _autoTrainsFrame.setVisible(true);
            }
        } else if (_UseConnectivity && (_LE != null)) {
            // not auto run, set up direction sensors in signals since use connectivity was requested
            if (getSignalType() == SIGNALHEAD) {
                int numErrors = t.checkSignals(_LE);
                if (numErrors == 0) {
                    t.initializeBlockingSensors();
                }
                if (numErrors != 0) {
                    if (showErrorMessages) {
                        JOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(Bundle.getMessage(
                                "Error36"), new Object[]{("" + numErrors)}),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
            }
        }
        // all information checks out - create
        ActiveTrain at = new ActiveTrain(t, trainID, tSource);
        //if (at==null) {
        // if (showErrorMessages) {
        //  JOptionPane.showMessageDialog(frame,java.text.MessageFormat.format(Bundle.getMessage(
        //    "Error11"),new Object[] { transitID, trainID }), Bundle.getMessage("ErrorTitle"),
        //     JOptionPane.ERROR_MESSAGE);
        // }
        // log.error("Creating Active Train failed, Transit - "+transitID+", train - "+trainID);
        // return null;
        //}
        activeTrainsList.add(at);
        java.beans.PropertyChangeListener listener = null;
        at.addPropertyChangeListener(listener = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                handleActiveTrainChange(e);
            }
        });
        _atListeners.add(listener);
        t.setState(Transit.ASSIGNED);
        at.setStartBlock(startBlock);
        at.setStartBlockSectionSequenceNumber(startBlockSectionSequenceNumber);
        at.setEndBlock(endBlock);
        at.setEndBlockSection(t.getSectionFromBlockAndSeq(endBlock, endBlockSectionSequenceNumber));
        at.setEndBlockSectionSequenceNumber(endBlockSectionSequenceNumber);
        at.setResetWhenDone(resetWhenDone);
        if (resetWhenDone) {
            restartingTrainsList.add(at);
        }
        at.setReverseAtEnd(reverseAtEnd);
        at.setAllocateMethod(allocateMethod);
        at.setPriority(priority);
        at.setDccAddress(dccAddress);
        at.setAutoRun(autoRun);
        return at;
    }

    public void allocateNewActiveTrain(ActiveTrain at) {
        if (at.getDelayedStart() == ActiveTrain.SENSORDELAY && at.getDelaySensor() != null) {
            if (at.getDelaySensor().getState() != jmri.Sensor.ACTIVE) {
                at.initializeDelaySensor();
            }
        }
        AllocationRequest ar = at.initializeFirstAllocation();
        if (ar != null) {
            if ((ar.getSection()).containsBlock(at.getStartBlock())) {
                // Active Train is in the first Section, go ahead and allocate it
                AllocatedSection als = allocateSection(ar, null);
                if (als == null) {
                    log.error("Problem allocating the first Section of the Active Train - " + at.getActiveTrainName());
                }
            }
        }
        activeTrainsTableModel.fireTableDataChanged();
        if (allocatedSectionTableModel != null) {
            allocatedSectionTableModel.fireTableDataChanged();
        }
    }

    private void handleActiveTrainChange(java.beans.PropertyChangeEvent e) {
        activeTrainsTableModel.fireTableDataChanged();
    }

    private boolean isInAllocatedSection(jmri.Block b) {
        for (int i = 0; i < allocatedSections.size(); i++) {
            Section s = allocatedSections.get(i).getSection();
            if (s.containsBlock(b)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Terminate an Active Train and remove it from the Dispatcher. The
     * ActiveTrain object should not be used again after this method is called.
     *
     * @param at the train to terminate
     */
    public void terminateActiveTrain(ActiveTrain at) {
        // ensure there is a train to terminate
        if (at == null) {
            log.error("Null ActiveTrain pointer when attempting to terminate an ActiveTrain");
            return;
        }
        // terminate the train - remove any allocation requests
        for (int k = allocationRequests.size(); k > 0; k--) {
            if (at == allocationRequests.get(k - 1).getActiveTrain()) {
                allocationRequests.get(k - 1).dispose();
                allocationRequests.remove(k - 1);
            }
        }
        // remove any allocated sections
        for (int k = allocatedSections.size(); k > 0; k--) {
            try {
                if (at == allocatedSections.get(k - 1).getActiveTrain()) {
                    releaseAllocatedSection(allocatedSections.get(k - 1), true);
                }
            } catch (RuntimeException e) {
                log.warn("releaseAllocatedSection failed - maybe the AllocatedSection was removed due to a terminating train?? {}", e.getMessage());
            }
        }
        // remove from restarting trains list, if present
        for (int j = restartingTrainsList.size(); j > 0; j--) {
            if (at == restartingTrainsList.get(j - 1)) {
                restartingTrainsList.remove(j - 1);
            }
        }
        // terminate the train
        for (int m = activeTrainsList.size(); m > 0; m--) {
            if (at == activeTrainsList.get(m - 1)) {
                activeTrainsList.remove(m - 1);
                at.removePropertyChangeListener(_atListeners.get(m - 1));
                _atListeners.remove(m - 1);
            }
        }
        if (at.getAutoRun()) {
            AutoActiveTrain aat = at.getAutoActiveTrain();
            _autoTrainsFrame.removeAutoActiveTrain(aat);
            aat.terminate();
            aat.dispose();
        }
        removeHeldMast(null, at);
        at.terminate();
        at.dispose();
        activeTrainsTableModel.fireTableDataChanged();
        if (allocatedSectionTableModel != null) {
            allocatedSectionTableModel.fireTableDataChanged();
        }
        allocationRequestTableModel.fireTableDataChanged();
    }

    /**
     * Creates an Allocation Request, and registers it with Dispatcher
     * <p>
     * Required input entries:
     *
     * @param activeTrain       ActiveTrain requesting the allocation
     * @param section           Section to be allocated
     * @param direction         direction of travel in the allocated Section
     * @param seqNumber         sequence number of the Section in the Transit of
     *                          the ActiveTrain. If the requested Section is not
     *                          in the Transit, a sequence number of -99 should
     *                          be entered.
     * @param showErrorMessages "true" if error message dialogs are to be
     *                          displayed for detected errors Set to "false" to
     *                          suppress error message dialogs from this method.
     * @param frame             window request is from, or "null" if not from a
     *                          window
     * @return true if successful; false otherwise
     */
    protected boolean requestAllocation(ActiveTrain activeTrain, Section section, int direction,
            int seqNumber, boolean showErrorMessages, JmriJFrame frame) {
        // check input entries
        if (activeTrain == null) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, Bundle.getMessage("Error16"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
            log.error("Missing ActiveTrain specification");
            return false;
        }
        if (section == null) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(Bundle.getMessage(
                        "Error17"), new Object[]{activeTrain.getActiveTrainName()}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
            }
            log.error("Missing Section specification in allocation request from " + activeTrain.getActiveTrainName());
            return false;
        }
        if (((seqNumber <= 0) || (seqNumber > (activeTrain.getTransit().getMaxSequence()))) && (seqNumber != -99)) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(Bundle.getMessage(
                        "Error19"), new Object[]{"" + seqNumber, activeTrain.getActiveTrainName()}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
            }
            log.error("Out-of-range sequence number *{}* in allocation request", seqNumber);
            return false;
        }
        if ((direction != Section.FORWARD) && (direction != Section.REVERSE)) {
            if (showErrorMessages) {
                JOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(Bundle.getMessage(
                        "Error18"), new Object[]{"" + direction, activeTrain.getActiveTrainName()}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
            }
            log.error("Invalid direction '" + direction + "' specification in allocation request");
            return false;
        }
        // check if this allocation has already been requested
        AllocationRequest ar = findAllocationRequestInQueue(section, seqNumber, direction, activeTrain);
        if (ar == null) {
            ar = new AllocationRequest(section, seqNumber, direction, activeTrain);
            allocationRequests.add(ar);
            if (_AutoAllocate) {
                autoAllocate.scanAllocationRequestList(allocationRequests);
            }
        }
        activeTrainsTableModel.fireTableDataChanged();
        allocationRequestTableModel.fireTableDataChanged();
        return true;
    }

    // ensures there will not be any duplicate allocation requests
    protected AllocationRequest findAllocationRequestInQueue(Section s, int seq, int dir, ActiveTrain at) {
        for (int i = 0; i < allocationRequests.size(); i++) {
            AllocationRequest ar = allocationRequests.get(i);
            if ((ar.getActiveTrain() == at) && (ar.getSection() == s) && (ar.getSectionSeqNumber() == seq)
                    && (ar.getSectionDirection() == dir)) {
                return ar;
            }
        }
        return null;
    }

    private void cancelAllocationRequest(int index) {
        AllocationRequest ar = allocationRequests.get(index);
        allocationRequests.remove(index);
        ar.dispose();
        allocationRequestTableModel.fireTableDataChanged();
    }

    private void allocateRequested(int index) {
        AllocationRequest ar = allocationRequests.get(index);
        allocateSection(ar, null);
    }

    protected void addDelayedTrain(ActiveTrain at) {
        if (at.getDelayedRestart() == ActiveTrain.TIMEDDELAY) {
            if (!delayedTrains.contains(at)) {
                delayedTrains.add(at);
            }
        } else if (at.getDelayedRestart() == ActiveTrain.SENSORDELAY) {
            if (at.getRestartSensor() != null) {
                at.initializeRestartSensor();
            }
        }
        activeTrainsTableModel.fireTableDataChanged();
    }

    /**
     * Allocates a Section to an Active Train according to the information in an
     * AllocationRequest.
     * <p>
     * If successful, returns an AllocatedSection and removes the
     * AllocationRequest from the queue. If not successful, returns null and
     * leaves the AllocationRequest in the queue.
     * <p>
     * To be allocatable, a Section must be FREE and UNOCCUPIED. If a Section is
     * OCCUPIED, the allocation is rejected unless the dispatcher chooses to
     * override this restriction. To be allocatable, the Active Train must not
     * be waiting for its start time. If the start time has not been reached,
     * the allocation is rejected, unless the dispatcher chooses to override the
     * start time.
     *
     * @param ar the request containing the section to allocate
     * @param ns the next section; use null to allow the next section to be
     *           automatically determined, if the next section is the last
     *           section, of if an extra section is being allocated
     * @return the allocated section or null if not successful
     */
    public AllocatedSection allocateSection(AllocationRequest ar, Section ns) {
        AllocatedSection as = null;
        Section nextSection = null;
        int nextSectionSeqNo = 0;
        if (ar != null) {
            ActiveTrain at = ar.getActiveTrain();
            if (at.holdAllocation() || at.reachedRestartPoint()) {
                return null;
            }
            Section s = ar.getSection();
            if (s.getState() != Section.FREE) {
                return null;
            }
            // skip occupancy check if this is the first allocation and the train is occupying the Section
            boolean checkOccupancy = true;
            if ((at.getLastAllocatedSection() == null) && (s.containsBlock(at.getStartBlock()))) {
                checkOccupancy = false;
            }
            // check if section is occupied
            if (checkOccupancy && (s.getOccupancy() == Section.OCCUPIED)) {
                if (_AutoAllocate) {
                    return null;  // autoAllocate never overrides occupancy
                }
                int selectedValue = JOptionPane.showOptionDialog(dispatcherFrame,
                        Bundle.getMessage("Question1"), Bundle.getMessage("WarningTitle"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        new Object[]{Bundle.getMessage("ButtonOverride"), Bundle.getMessage("ButtonNo")}, Bundle.getMessage("ButtonNo"));
                if (selectedValue == 1) {
                    return null;   // return without allocating if "No" response
                }
            }
            // check if train has reached its start time if delayed start
            if (checkOccupancy && (!at.getStarted()) && at.getDelayedStart() != ActiveTrain.NODELAY) {
                if (_AutoAllocate) {
                    return null;  // autoAllocate never overrides start time
                }
                int selectedValue = JOptionPane.showOptionDialog(dispatcherFrame,
                        Bundle.getMessage("Question4"), Bundle.getMessage("WarningTitle"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        new Object[]{Bundle.getMessage("ButtonOverride"), Bundle.getMessage("ButtonNo")}, Bundle.getMessage("ButtonNo"));
                if (selectedValue == 1) {
                    return null;
                } else {
                    at.setStarted();
                    for (int i = delayedTrains.size() - 1; i >= 0; i--) {
                        if (delayedTrains.get(i) == at) {
                            delayedTrains.remove(i);
                        }
                    }
                }
            }
            //check here to see if block is already assigned to an allocated section;
            if (getSignalType() == SIGNALMAST && checkBlocksNotInAllocatedSection(s, ar) != null) {
                return null;
            }
            // Programming
            // Note: if ns is not null, the program will not check for end Block, but will use ns.
            // Calling code must do all validity checks on a non-null ns.
            if (ns != null) {
                nextSection = ns;
            } else if ((ar.getSectionSeqNumber() != -99) && (at.getNextSectionSeqNumber() == ar.getSectionSeqNumber())
                    && (!((s == at.getEndBlockSection()) && (ar.getSectionSeqNumber() == at.getEndBlockSectionSequenceNumber())))
                    && (!(at.isAllocationReversed() && (ar.getSectionSeqNumber() == 1)))) {
                // not at either end - determine the next section
                 int seqNum = ar.getSectionSeqNumber();
                if (at.isAllocationReversed()) {
                    seqNum -= 1;
                } else {
                    seqNum += 1;
                }
                List<Section> secList = at.getTransit().getSectionListBySeq(seqNum);
                if (secList.size() == 1) {
                    nextSection = secList.get(0);

                } else if (secList.size() > 1) {
                    if (_AutoAllocate) {
                        nextSection = autoChoice(secList, ar);
                    } else {
                        nextSection = dispatcherChoice(secList, ar);
                    }
                }
                nextSectionSeqNo = seqNum;
            } else if (at.getReverseAtEnd() && (!at.isAllocationReversed()) && (s == at.getEndBlockSection())
                    && (ar.getSectionSeqNumber() == at.getEndBlockSectionSequenceNumber())) {
                // need to reverse Transit direction when train is in the last Section, set next section.
                if (at.getResetWhenDone()) {
                    if (at.getDelayedRestart() != ActiveTrain.NODELAY) {
                        at.holdAllocation(true);
                    }
                }
                nextSectionSeqNo = at.getEndBlockSectionSequenceNumber() - 1;
                at.setAllocationReversed(true);
                List<Section> secList = at.getTransit().getSectionListBySeq(nextSectionSeqNo);
                if (secList.size() == 1) {
                    nextSection = secList.get(0);
                } else if (secList.size() > 1) {
                    if (_AutoAllocate) {
                        nextSection = autoChoice(secList, ar);
                    } else {
                        nextSection = dispatcherChoice(secList, ar);
                    }
                }
            } else if (((!at.isAllocationReversed()) && (s == at.getEndBlockSection())
                    && (ar.getSectionSeqNumber() == at.getEndBlockSectionSequenceNumber()))
                    || (at.isAllocationReversed() && (ar.getSectionSeqNumber() == 1))) {
                // request to allocate the last block in the Transit, or the Transit is reversed and
                //      has reached the beginning of the Transit--check for automatic restart
                if (at.getResetWhenDone()) {
                    if (at.getDelayedRestart() != ActiveTrain.NODELAY) {
                        at.holdAllocation(true);
                    }
                    nextSection = at.getSecondAllocatedSection();
                    nextSectionSeqNo = 2;
                    at.setAllocationReversed(false);
//                } else if ((at.isAllocationReversed() && ar.getSectionSeqNumber() == 1)) {
                    //nextSection = at.getSecondAllocatedSection();
                    //nextSectionSeqNo = 2;
//                    nextSection = ar.getSection();
//                    nextSectionSeqNo = 1;
//                    log.debug("I");
                    //at.holdAllocation(true);
                    //at.setAllocationReversed(false);
                }
            }

            //This might be the location to check to see if we have an intermediate section that we then need to perform extra checks on.
            //Working on the basis that if the nextsection is not null, then we are not at the end of the transit.
            List<Section> intermediateSections = new ArrayList<>();
            Section mastHeldAtSection = null;
            if (nextSection != null && ar.getSection().getProperty("intermediateSection") != null && ((Boolean) ar.getSection().getProperty("intermediateSection")).booleanValue()) {
                String property = "forwardMast";
                if (at.isAllocationReversed()) {
                    property = "reverseMast";
                }
                if (ar.getSection().getProperty(property) != null) {
                    SignalMast endMast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(ar.getSection().getProperty(property).toString());
                    if (endMast != null) {
                        if (endMast.getHeld()) {
                            mastHeldAtSection = ar.getSection();
                        }
                    }
                }
                List<TransitSection> tsList = ar.getActiveTrain().getTransit().getTransitSectionList();
                boolean found = false;
                if (at.isAllocationReversed()) {
                    for (int i = tsList.size() - 1; i > 0; i--) {
                        TransitSection ts = tsList.get(i);
                        if (ts.getSection() == ar.getSection() && ts.getSequenceNumber() == ar.getSectionSeqNumber()) {
                            found = true;
                        } else if (found) {
                            if (ts.getSection().getProperty("intermediateSection") != null && ((Boolean) ts.getSection().getProperty("intermediateSection")).booleanValue()) {
                                intermediateSections.add(ts.getSection());
                            } else {
                                //we add the section after the last intermediate in, so that the last allocation request can be built correctly
                                intermediateSections.add(ts.getSection());
                                break;
                            }
                        }
                    }
                } else {
                    for (int i = 0; i <= tsList.size() - 1; i++) {
                        TransitSection ts = tsList.get(i);
                        if (ts.getSection() == ar.getSection() && ts.getSequenceNumber() == ar.getSectionSeqNumber()) {
                            found = true;
                        } else if (found) {
                            if (ts.getSection().getProperty("intermediateSection") != null && ((Boolean) ts.getSection().getProperty("intermediateSection")).booleanValue()) {
                                intermediateSections.add(ts.getSection());
                            } else {
                                //we add the section after the last intermediate in, so that the last allocation request can be built correctly
                                intermediateSections.add(ts.getSection());
                                break;
                            }
                        }
                    }
                }
                boolean intermediatesOccupied = false;

                for (int i = 0; i < intermediateSections.size() - 1; i++) {  // ie do not check last section which is not an intermediate section
                    Section se = intermediateSections.get(i);
                    if (se.getState() == Section.FREE) {
                        //If the section state is free, we need to look to see if any of the blocks are used else where
                        Section conflict = checkBlocksNotInAllocatedSection(se, null);
                        if (conflict != null) {
                            //We have a conflicting path
                            //We might need to find out if the section which the block is allocated to is one in our transit, and if so is it running in the same direction.
                            return null;
                        } else {
                            if (mastHeldAtSection == null) {
                                if (se.getProperty(property) != null) {
                                    SignalMast endMast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(se.getProperty(property).toString());
                                    if (endMast != null && endMast.getHeld()) {
                                        mastHeldAtSection = se;
                                    }
                                }
                            }
                        }
                    } else if (at.getLastAllocatedSection() != null && se.getState() != at.getLastAllocatedSection().getState()) {
                        //Last allocated section and the checking section direction are not the same
                        return null;
                    } else {
                        intermediatesOccupied = true;
                    }
                }
                //If the intermediate sections are already occupied or allocated then we clear the intermediate list and only allocate the original request.
                if (intermediatesOccupied) {
                    intermediateSections = new ArrayList<>();
                }
            }

            // check/set turnouts if requested or if autorun
            // Note: If "Use Connectivity..." is specified in the Options window, turnouts are checked. If
            //   turnouts are not set correctly, allocation will not proceed without dispatcher override.
            //   If in addition Auto setting of turnouts is requested, the turnouts are set automatically
            //   if not in the correct position.
            // Note: Turnout checking and/or setting is not performed when allocating an extra section.
            if ((_UseConnectivity) && (ar.getSectionSeqNumber() != -99)) {
                if (!checkTurnoutStates(s, ar.getSectionSeqNumber(), nextSection, at, at.getLastAllocatedSection())) {
                    return null;
                }
                Section preSec = s;
                Section tmpcur = nextSection;
                int tmpSeqNo = nextSectionSeqNo;
                //The first section in the list will be the same as the nextSection, so we skip that.
                for (int i = 1; i < intermediateSections.size(); i++) {
                    Section se = intermediateSections.get(i);
                    if (preSec == mastHeldAtSection) {
                        log.debug("Section is beyond held mast do not set turnouts {}", (tmpcur != null ? tmpcur.getDisplayName() : "null"));
                        break;
                    }
                    if (!checkTurnoutStates(tmpcur, tmpSeqNo, se, at, preSec)) {
                        return null;
                    }
                    preSec = tmpcur;
                    tmpcur = se;
                    if (at.isAllocationReversed()) {
                        tmpSeqNo -= 1;
                    } else {
                        tmpSeqNo += 1;
                    }
                }
            }

            as = allocateSection(at, s, ar.getSectionSeqNumber(), nextSection, nextSectionSeqNo, ar.getSectionDirection());
            if (intermediateSections.size() > 1 && mastHeldAtSection != s) {
                Section tmpcur = nextSection;
                int tmpSeqNo = nextSectionSeqNo;
                int tmpNxtSeqNo = tmpSeqNo;
                if (at.isAllocationReversed()) {
                    tmpNxtSeqNo -= 1;
                } else {
                    tmpNxtSeqNo += 1;
                }
                //The first section in the list will be the same as the nextSection, so we skip that.
                for (int i = 1; i < intermediateSections.size(); i++) {
                    if (tmpcur == mastHeldAtSection) {
                        log.debug("Section is beyond held mast do not allocate any more sections {}", (tmpcur != null ? tmpcur.getDisplayName() : "null"));
                        break;
                    }
                    Section se = intermediateSections.get(i);
                    as = allocateSection(at, tmpcur, tmpSeqNo, se, tmpNxtSeqNo, ar.getSectionDirection());
                    tmpcur = se;
                    if (at.isAllocationReversed()) {
                        tmpSeqNo -= 1;
                        tmpNxtSeqNo -= 1;
                    } else {
                        tmpSeqNo += 1;
                        tmpNxtSeqNo += 1;
                    }
                }
            }
            int ix = -1;
            for (int i = 0; i < allocationRequests.size(); i++) {
                if (ar == allocationRequests.get(i)) {
                    ix = i;
                }
            }
            allocationRequests.remove(ix);
            ar.dispose();
            allocationRequestTableModel.fireTableDataChanged();
            activeTrainsTableModel.fireTableDataChanged();
            if (allocatedSectionTableModel != null) {
                allocatedSectionTableModel.fireTableDataChanged();
            }
            if (extraFrame != null) {
                cancelExtraRequested(null);
            }
            if (_AutoAllocate) {
                requestNextAllocation(at);
                autoAllocate.scanAllocationRequestList(allocationRequests);
            }

        } else {
            log.error("Null Allocation Request provided in request to allocate a section");
        }
        return as;
    }

    AllocatedSection allocateSection(ActiveTrain at, Section s, int seqNum, Section nextSection, int nextSectionSeqNo, int direction) {
        AllocatedSection as = null;
        // allocate the section
        as = new AllocatedSection(s, at, seqNum, nextSection, nextSectionSeqNo);
        if (_SupportVSDecoder) {
            as.addPropertyChangeListener(InstanceManager.getDefault(jmri.jmrit.vsdecoder.VSDecoderManager.class));
        }

        s.setState(direction/*ar.getSectionDirection()*/);
        if (getSignalType() == SIGNALMAST) {
            String property = "forwardMast";
            if (s.getState() == Section.REVERSE) {
                property = "reverseMast";
            }
            if (s.getProperty(property) != null) {
                SignalMast toHold = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(s.getProperty(property).toString());
                if (toHold != null) {
                    if (!toHold.getHeld()) {
                        heldMasts.add(new HeldMastDetails(toHold, at));
                        toHold.setHeld(true);
                    }
                }

            }

            if (at.getLastAllocatedSection() != null && at.getLastAllocatedSection().getProperty(property) != null) {
                SignalMast toRelease = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(at.getLastAllocatedSection().getProperty(property).toString());
                if (toRelease != null && isMastHeldByDispatcher(toRelease, at)) {
                    removeHeldMast(toRelease, at);
                    //heldMasts.remove(toRelease);
                    toRelease.setHeld(false);
                }
            }
        }
        at.addAllocatedSection(as);
        allocatedSections.add(as);
        return as;
    }

    boolean checkTurnoutStates(Section s, int sSeqNum, Section nextSection, ActiveTrain at, Section prevSection) {
        boolean turnoutsOK = true;
        if (_AutoTurnouts || at.getAutoRun()) {
            // automatically set the turnouts for this section before allocation
            turnoutsOK = autoTurnouts.setTurnoutsInSection(s, sSeqNum, nextSection,
                    at, _LE, _TrustKnownTurnouts, prevSection);
        } else {
            // check that turnouts are correctly set before allowing allocation to proceed
            turnoutsOK = autoTurnouts.checkTurnoutsInSection(s, sSeqNum, nextSection,
                    at, _LE, prevSection);
        }
        if (!turnoutsOK) {
            if (_AutoAllocate) {
                return false;
            } else {
                // give the manual dispatcher a chance to override turnouts not OK
                int selectedValue = JOptionPane.showOptionDialog(dispatcherFrame,
                        Bundle.getMessage("Question2"), Bundle.getMessage("WarningTitle"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        new Object[]{Bundle.getMessage("ButtonOverride"), Bundle.getMessage("ButtonNo")}, Bundle.getMessage("ButtonNo"));
                if (selectedValue == 1) {
                    return false;   // return without allocating if "No" response
                }
            }
        }
        return true;
    }

    List<HeldMastDetails> heldMasts = new ArrayList<>();

    static class HeldMastDetails {

        SignalMast mast = null;
        ActiveTrain at = null;

        HeldMastDetails(SignalMast sm, ActiveTrain a) {
            mast = sm;
            at = a;
        }

        ActiveTrain getActiveTrain() {
            return at;
        }

        SignalMast getMast() {
            return mast;
        }
    }

    public boolean isMastHeldByDispatcher(SignalMast sm, ActiveTrain at) {
        for (HeldMastDetails hmd : heldMasts) {
            if (hmd.getMast() == sm && hmd.getActiveTrain() == at) {
                return true;
            }
        }
        return false;
    }

    private void removeHeldMast(SignalMast sm, ActiveTrain at) {
        List<HeldMastDetails> toRemove = new ArrayList<>();
        for (HeldMastDetails hmd : heldMasts) {
            if (hmd.getActiveTrain() == at) {
                if (sm == null) {
                    toRemove.add(hmd);
                } else if (sm == hmd.getMast()) {
                    toRemove.add(hmd);
                }
            }
        }
        for (HeldMastDetails hmd : toRemove) {
            hmd.getMast().setHeld(false);
            heldMasts.remove(hmd);
        }
    }

    /*
     * This is used to determine if the blocks in a section we want to allocate are already allocated to a section, or if they are now free.
     */
    protected Section checkBlocksNotInAllocatedSection(Section s, AllocationRequest ar) {

        for (AllocatedSection as : allocatedSections) {
            if (as.getSection() != s) {
                List<Block> blas = as.getSection().getBlockList();
                //
                // When allocating the initial section for an Active Train,
                // we need not be concerned with any blocks in the initial section
                // which are unoccupied and to the rear of any occupied blocks in
                // the section as the train is not expected to enter those blocks.
                // When sections include the OS section these blocks prevented
                // allocation.
                //
                // The procedure is to remove those blocks (for the moment) from
                // the blocklist for the section during the initial allocation.
                //

                List<Block> bls = new ArrayList<>();
                if (ar != null && ar.getActiveTrain().getAllocatedSectionList().size() == 0) {
                    int j;
                    if (ar.getSectionDirection() == Section.FORWARD) {
                        j = 0;
                        for (int i = 0; i < s.getBlockList().size(); i++) {
                            if (j == 0 && s.getBlockList().get(i).getState() == Block.OCCUPIED) {
                                j = 1;
                            }
                            if (j == 1) {
                                bls.add(s.getBlockList().get(i));
                            }
                        }
                    } else {
                        j = 0;
                        for (int i = s.getBlockList().size() - 1; i >= 0; i--) {
                            if (j == 0 && s.getBlockList().get(i).getState() == Block.OCCUPIED) {
                                j = 1;
                            }
                            if (j == 1) {
                                bls.add(s.getBlockList().get(i));
                            }
                        }
                    }
                } else {
                    bls = s.getBlockList();
                }

                for (Block b : bls) {
                    if (blas.contains(b)) {
                        if (as.getSection().getOccupancy() == Block.OCCUPIED) {
                            //The next check looks to see if the block has already been passed or not and therefore ready for allocation.
                            if (as.getSection().getState() == Section.FORWARD) {
                                for (int i = 0; i < blas.size(); i++) {
                                    //The block we get to is occupied therefore the subsequent blocks have not been entered
                                    if (blas.get(i).getState() == Block.OCCUPIED) {
                                        if (ar != null) {
                                            ar.setWaitingOnBlock(b);
                                        }
                                        return as.getSection();
                                    } else if (blas.get(i) == b) {
                                        break;
                                    }
                                }
                            } else {
                                for (int i = blas.size(); i >= 0; i--) {
                                    //The block we get to is occupied therefore the subsequent blocks have not been entered
                                    if (blas.get(i).getState() == Block.OCCUPIED) {
                                        if (ar != null) {
                                            ar.setWaitingOnBlock(b);
                                        }
                                        return as.getSection();
                                    } else if (blas.get(i) == b) {
                                        break;
                                    }
                                }
                            }
                        } else if (as.getSection().getOccupancy() != Section.FREE) {
                            if (ar != null) {
                                ar.setWaitingOnBlock(b);
                            }
                            return as.getSection();
                        }
                    }
                }
            }
        }
        return null;
    }

    // automatically make a choice of next section
    private Section autoChoice(List<Section> sList, AllocationRequest ar) {
        Section tSection = autoAllocate.autoNextSectionChoice(sList, ar);
        if (tSection != null) {
            return tSection;
        }
        // if automatic choice failed, ask the dispatcher
        return dispatcherChoice(sList, ar);
    }

    // manually make a choice of next section
    private Section dispatcherChoice(List<Section> sList, AllocationRequest ar) {
        Object choices[] = new Object[sList.size()];
        for (int i = 0; i < sList.size(); i++) {
            Section s = sList.get(i);
            String txt = s.getSystemName();
            String user = s.getUserName();
            if ((user != null) && (!user.equals("")) && (!user.equals(txt))) {
                txt = txt + "(" + user + ")";
            }
            choices[i] = txt;
        }
        Object secName = JOptionPane.showInputDialog(dispatcherFrame,
                Bundle.getMessage("ExplainChoice", ar.getSectionName()),
                Bundle.getMessage("ChoiceFrameTitle"), JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
        if (secName == null) {
            JOptionPane.showMessageDialog(dispatcherFrame, Bundle.getMessage("WarnCancel"));
            return sList.get(0);
        }
        for (int j = 0; j < sList.size(); j++) {
            if (secName.equals(choices[j])) {
                return sList.get(j);
            }
        }
        return sList.get(0);
    }

    // submit an AllocationRequest for the next Section of an ActiveTrain
    private void requestNextAllocation(ActiveTrain at) {
        // set up an Allocation Request
        Section next = at.getNextSectionToAllocate();
        if (next == null) {
            return;
        }
        int seqNext = at.getNextSectionSeqNumber();
        int dirNext = at.getAllocationDirectionFromSectionAndSeq(next, seqNext);
        requestAllocation(at, next, dirNext, seqNext, true, dispatcherFrame);
    }

    /**
     * Check if any allocation requests need to be allocated, or if any
     * allocated sections need to be released
     */
    private void checkAutoRelease() {
        if ((autoReleaseBox != null) && (autoReleaseBox.isSelected())) {
            // Auto release of exited sections has been requested - because of possible noise in block detection
            //    hardware, allocated sections are automatically released in the order they were allocated only
            // Only unoccupied sections that have been exited are tested.
            // The next allocated section must be assigned to the same train, and it must have been entered for
            //    the exited Section to be released.
            // Extra allocated sections are not automatically released (allocation number = -1).
            boolean foundOne = true;
            while ((allocatedSections.size() > 0) && foundOne) {
                try {
                    foundOne = false;
                    AllocatedSection as = null;
                    for (int i = 0; (i < allocatedSections.size()) && !foundOne; i++) {
                        as = allocatedSections.get(i);
                        if (as.getExited() && (as.getSection().getOccupancy() != Section.OCCUPIED)
                                && (as.getAllocationNumber() != -1)) {
                            // possible candidate for deallocation - check order
                            foundOne = true;
                            for (int j = 0; (j < allocatedSections.size()) && foundOne; j++) {
                                if (j != i) {
                                    AllocatedSection asx = allocatedSections.get(j);
                                    if ((asx.getActiveTrain() == as.getActiveTrain())
                                            && (asx.getAllocationNumber() != -1)
                                            && (asx.getAllocationNumber() < as.getAllocationNumber())) {
                                        foundOne = false;
                                    }
                                }
                            }
                            if (foundOne) {
                                // check if the next section is allocated to the same train and has been entered
                                ActiveTrain at = as.getActiveTrain();
                                Section ns = as.getNextSection();
                                AllocatedSection nas = null;
                                for (int k = 0; (k < allocatedSections.size()) && (nas == null); k++) {
                                    if (allocatedSections.get(k).getSection() == ns) {
                                        nas = allocatedSections.get(k);
                                    }
                                }
                                if ((nas == null) || (at.getStatus() == ActiveTrain.WORKING)
                                        || (at.getStatus() == ActiveTrain.STOPPED)
                                        || (at.getStatus() == ActiveTrain.READY)
                                        || (at.getMode() == ActiveTrain.MANUAL)) {
                                    // do not autorelease allocated sections from an Active Train that is
                                    //    STOPPED, READY, or WORKING, or is in MANUAL mode.
                                    foundOne = false;
                                    //But do so if the active train has reached its restart point
                                    if (nas != null && at.reachedRestartPoint()) {
                                        foundOne = true;
                                    }
                                } else {
                                    if ((nas.getActiveTrain() != as.getActiveTrain()) || (!nas.getEntered())) {
                                        foundOne = false;
                                    }
                                }
                                if (foundOne) {
                                    // have section to release - delay before release
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        // ignore this exception
                                    }
                                    // if section is still allocated, release it
                                    foundOne = false;
                                    for (int m = 0; m < allocatedSections.size(); m++) {
                                        if ((allocatedSections.get(m) == as) && (as.getActiveTrain() == at)) {
                                            foundOne = true;
                                        }
                                    }
                                    if (foundOne) {
                                        log.debug("{}: releasing {}", at.getTrainName(), as.getSectionName());
                                        releaseAllocatedSection(as, false);
                                    }
                                }
                            }
                        }
                    }
                } catch (RuntimeException e) {
                    log.warn("checkAutoRelease failed  - maybe the AllocatedSection was removed due to a terminating train? {}", e.toString());
                    continue;
                }
            }
        }
    }

    /**
     * Releases an allocated Section, and removes it from the Dispatcher Input.
     *
     * @param as               the section to release
     * @param terminatingTrain true if the associated train is being terminated;
     *                         false otherwise
     */
    public void releaseAllocatedSection(AllocatedSection as, boolean terminatingTrain) {
        // check that section is not occupied if not terminating train
        if (!terminatingTrain && (as.getSection().getOccupancy() == Section.OCCUPIED)) {
            // warn the manual dispatcher that Allocated Section is occupied
            int selectedValue = JOptionPane.showOptionDialog(dispatcherFrame, java.text.MessageFormat.format(
                    Bundle.getMessage("Question5"), new Object[]{as.getSectionName()}), Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{Bundle.getMessage("ButtonRelease"), Bundle.getMessage("ButtonNo")},
                    Bundle.getMessage("ButtonNo"));
            if (selectedValue == 1) {
                return;   // return without releasing if "No" response
            }
        }
        // release the Allocated Section
        for (int i = allocatedSections.size(); i > 0; i--) {
            if (as == allocatedSections.get(i - 1)) {
                allocatedSections.remove(i - 1);
            }
        }
        as.getSection().setState(Section.FREE);
        as.getActiveTrain().removeAllocatedSection(as);
        as.dispose();
        if (allocatedSectionTableModel != null) {
            allocatedSectionTableModel.fireTableDataChanged();
        }
        allocationRequestTableModel.fireTableDataChanged();
        activeTrainsTableModel.fireTableDataChanged();
        if (_AutoAllocate) {
            autoAllocate.scanAllocationRequestList(allocationRequests);
        }
    }

    /**
     * Updates display when occupancy of an allocated section changes Also
     * drives auto release if it is selected
     */
    public void sectionOccupancyChanged() {
        checkAutoRelease();
        if (allocatedSectionTableModel != null) {
            allocatedSectionTableModel.fireTableDataChanged();
        }
        allocationRequestTableModel.fireTableDataChanged();
    }

    /**
     * Handle activity that is triggered by the fast clock
     */
    protected void newFastClockMinute() {
        for (int i = delayedTrains.size() - 1; i >= 0; i--) {
            ActiveTrain at = delayedTrains.get(i);
            // check if this Active Train is waiting to start
            if ((!at.getStarted()) && at.getDelayedStart() != ActiveTrain.NODELAY) {
                // is it time to start?
                if (at.getDelayedStart() == ActiveTrain.TIMEDDELAY) {
                    if (isFastClockTimeGE(at.getDepartureTimeHr(), at.getDepartureTimeMin())) {
                        // allow this train to start
                        at.setStarted();
                        delayedTrains.remove(i);
                        if (_AutoAllocate) {
                            autoAllocate.scanAllocationRequestList(allocationRequests);
                        }
                    }
                }
            } else if (at.getStarted() && at.getStatus() == ActiveTrain.READY && at.reachedRestartPoint()) {
                if (isFastClockTimeGE(at.getRestartDepartHr(), at.getRestartDepartMin())) {
                    at.restart();
                    delayedTrains.remove(i);
                    if (_AutoAllocate) {
                        autoAllocate.scanAllocationRequestList(allocationRequests);
                    }
                }
            }
        }
    }

    /**
     * This method tests time
     *
     * @param hr  the hour to test against (0-23)
     * @param min the minute to test against (0-59)
     * @return true if fast clock time and tested time are the same
     */
    protected boolean isFastClockTimeGE(int hr, int min) {
        Calendar now = Calendar.getInstance();
        now.setTime(fastClock.getTime());
        int nowHours = now.get(Calendar.HOUR_OF_DAY);
        int nowMinutes = now.get(Calendar.MINUTE);
        return ((nowHours * 60) + nowMinutes) == ((hr * 60) + min);
    }

    // option access methods
    protected LayoutEditor getLayoutEditor() {
        return _LE;
    }

    protected void setLayoutEditor(LayoutEditor editor) {
        _LE = editor;
    }

    protected boolean getUseConnectivity() {
        return _UseConnectivity;
    }

    protected void setUseConnectivity(boolean set) {
        _UseConnectivity = set;
    }

    protected void setSignalType(int type) {
        _SignalType = type;
    }

    protected int getSignalType() {
        return _SignalType;
    }

    protected void setStoppingSpeedName(String speedName) {
        _StoppingSpeedName = speedName;
    }

    protected String getStoppingSpeedName() {
        return _StoppingSpeedName;
    }

    protected float getMaximumLineSpeed() {
        return maximumLineSpeed;
    }
    protected boolean getTrainsFromRoster() {
        return _TrainsFromRoster;
    }

    protected void setTrainsFromRoster(boolean set) {
        _TrainsFromRoster = set;
    }

    protected boolean getTrainsFromTrains() {
        return _TrainsFromTrains;
    }

    protected void setTrainsFromTrains(boolean set) {
        _TrainsFromTrains = set;
    }

    protected boolean getTrainsFromUser() {
        return _TrainsFromUser;
    }

    protected void setTrainsFromUser(boolean set) {
        _TrainsFromUser = set;
    }

    protected boolean getAutoAllocate() {
        return _AutoAllocate;
    }

    protected void setAutoAllocate(boolean set) {
        if (set && (autoAllocate == null)) {
            if (_LE != null) {
                autoAllocate = new AutoAllocate(this);
            } else {
                JOptionPane.showMessageDialog(dispatcherFrame, Bundle.getMessage("Error39"),
                        Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
                _AutoAllocate = false;
                if (autoAllocateBox != null) {
                    autoAllocateBox.setSelected(_AutoAllocate);
                }
                return;
            }
        }
        _AutoAllocate = set;
        if ((!_AutoAllocate) && (autoAllocate != null)) {
            autoAllocate.clearAllocationPlans();
        }
        if (autoAllocateBox != null) {
            autoAllocateBox.setSelected(_AutoAllocate);
        }
    }

    protected boolean getAutoTurnouts() {
        return _AutoTurnouts;
    }

    protected void setAutoTurnouts(boolean set) {
        _AutoTurnouts = set;
    }

    protected boolean getTrustKnownTurnouts() {
        return _TrustKnownTurnouts;
    }

    protected void setTrustKnownTurnouts(boolean set) {
        _TrustKnownTurnouts = set;
    }

    protected int getMinThrottleInterval() {
        return _MinThrottleInterval;
    }

    protected void setMinThrottleInterval(int set) {
        _MinThrottleInterval = set;
    }

    protected int getFullRampTime() {
        return _FullRampTime;
    }

    protected void setFullRampTime(int set) {
        _FullRampTime = set;
    }

    protected boolean getHasOccupancyDetection() {
        return _HasOccupancyDetection;
    }

    protected void setHasOccupancyDetection(boolean set) {
        _HasOccupancyDetection = set;
    }

    protected boolean getUseScaleMeters() {
        return _UseScaleMeters;
    }

    protected void setUseScaleMeters(boolean set) {
        _UseScaleMeters = set;
    }

    protected boolean getShortActiveTrainNames() {
        return _ShortActiveTrainNames;
    }

    protected void setShortActiveTrainNames(boolean set) {
        _ShortActiveTrainNames = set;
        if (allocatedSectionTableModel != null) {
            allocatedSectionTableModel.fireTableDataChanged();
        }
        if (allocationRequestTableModel != null) {
            allocationRequestTableModel.fireTableDataChanged();
        }
    }

    protected boolean getShortNameInBlock() {
        return _ShortNameInBlock;
    }

    protected void setShortNameInBlock(boolean set) {
        _ShortNameInBlock = set;
    }

    protected boolean getRosterEntryInBlock() {
        return _RosterEntryInBlock;
    }

    protected void setRosterEntryInBlock(boolean set) {
        _RosterEntryInBlock = set;
    }

    protected boolean getExtraColorForAllocated() {
        return _ExtraColorForAllocated;
    }

    protected void setExtraColorForAllocated(boolean set) {
        _ExtraColorForAllocated = set;
    }

    protected boolean getNameInAllocatedBlock() {
        return _NameInAllocatedBlock;
    }

    protected void setNameInAllocatedBlock(boolean set) {
        _NameInAllocatedBlock = set;
    }

    protected Scale getScale() {
        return _LayoutScale;
    }

    protected void setScale(Scale sc) {
        _LayoutScale = sc;
    }

    public List<ActiveTrain> getActiveTrainsList() {
        return activeTrainsList;
    }

    protected List<AllocatedSection> getAllocatedSectionsList() {
        return allocatedSections;
    }

    public ActiveTrain getActiveTrainForRoster(RosterEntry re) {
        if (!_TrainsFromRoster) {
            return null;
        }
        for (ActiveTrain at : activeTrainsList) {
            if (at.getRosterEntry().equals(re)) {
                return at;
            }
        }
        return null;

    }

    protected boolean getSupportVSDecoder() {
        return _SupportVSDecoder;
    }

    protected void setSupportVSDecoder(boolean set) {
        _SupportVSDecoder = set;
    }

    // called by ActivateTrainFrame after a new train is all set up
    //      Dispatcher side of activating a new train should be completed here
    // Jay Janzen protection changed to public for access via scripting
    public void newTrainDone(ActiveTrain at) {
        if (at != null) {
            // a new active train was created, check for delayed start
            if (at.getDelayedStart() != ActiveTrain.NODELAY && (!at.getStarted())) {
                delayedTrains.add(at);
                fastClockWarn(true);
            } // djd needs work here
            // check for delayed restart
            else if (at.getDelayedRestart() == ActiveTrain.TIMEDDELAY) {
                fastClockWarn(false);
            }
        }
        newTrainActive = false;
    }

    protected void removeDelayedTrain(ActiveTrain at) {
        delayedTrains.remove(at);
    }

    private void fastClockWarn(boolean wMess) {
        if (fastClockSensor.getState() == Sensor.ACTIVE) {
            return;
        }
        // warn that the fast clock is not running
        String mess = "";
        if (wMess) {
            mess = Bundle.getMessage("FastClockWarn");
        } else {
            mess = Bundle.getMessage("FastClockWarn2");
        }
        int selectedValue = JOptionPane.showOptionDialog(dispatcherFrame,
                mess, Bundle.getMessage("WarningTitle"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new Object[]{Bundle.getMessage("ButtonYesStart"), Bundle.getMessage("ButtonNo")},
                Bundle.getMessage("ButtonNo"));
        if (selectedValue == 0) {
            try {
                fastClockSensor.setState(Sensor.ACTIVE);
            } catch (jmri.JmriException reason) {
                log.error("Exception when setting fast clock sensor");
            }
        }
    }

    // Jay Janzen
    // Protection changed to public to allow access via scripting
    public AutoTrainsFrame getAutoTrainsFrame() {
        return _autoTrainsFrame;
    }

    /**
     * Table model for Active Trains Table in Dispatcher window
     */
    public class ActiveTrainsTableModel extends javax.swing.table.AbstractTableModel implements
            java.beans.PropertyChangeListener {

        public static final int TRANSIT_COLUMN = 0;
        public static final int TRANSIT_COLUMN_U = 1;
        public static final int TRAIN_COLUMN = 2;
        public static final int TYPE_COLUMN = 3;
        public static final int STATUS_COLUMN = 4;
        public static final int MODE_COLUMN = 5;
        public static final int ALLOCATED_COLUMN = 6;
        public static final int ALLOCATED_COLUMN_U = 7;
        public static final int NEXTSECTION_COLUMN = 8;
        public static final int NEXTSECTION_COLUMN_U = 9;
        public static final int ALLOCATEBUTTON_COLUMN = 10;
        public static final int TERMINATEBUTTON_COLUMN = 11;
        public static final int RESTARTCHECKBOX_COLUMN = 12;
        public static final int ISAUTO_COLUMN = 13;
        public static final int CURRENTSIGNAL_COLUMN = 14;
        public static final int CURRENTSIGNAL_COLUMN_U = 15;
        public static final int MAX_COLUMN = 15;
        public ActiveTrainsTableModel() {
            super();
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                fireTableDataChanged();
            }
        }

        @Override
        public Class<?> getColumnClass(int col) {
            switch (col) {
                case ALLOCATEBUTTON_COLUMN:
                case TERMINATEBUTTON_COLUMN:
                    return JButton.class;
                case RESTARTCHECKBOX_COLUMN:
                case ISAUTO_COLUMN:
                    return Boolean.class;
                default:
                    return String.class;
            }
        }

        @Override
        public int getColumnCount() {
            return MAX_COLUMN + 1;
        }

        @Override
        public int getRowCount() {
            return (activeTrainsList.size());
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case ALLOCATEBUTTON_COLUMN:
                case TERMINATEBUTTON_COLUMN:
                case RESTARTCHECKBOX_COLUMN:
                    return (true);
                default:
                    return (false);
            }
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case TRANSIT_COLUMN:
                    return Bundle.getMessage("TransitColumnSysTitle");
                case TRANSIT_COLUMN_U:
                    return Bundle.getMessage("TransitColumnTitle");
                case TRAIN_COLUMN:
                    return Bundle.getMessage("TrainColumnTitle");
                case TYPE_COLUMN:
                    return Bundle.getMessage("TrainTypeColumnTitle");
                case STATUS_COLUMN:
                    return Bundle.getMessage("TrainStatusColumnTitle");
                case MODE_COLUMN:
                    return Bundle.getMessage("TrainModeColumnTitle");
                case ALLOCATED_COLUMN:
                    return Bundle.getMessage("AllocatedSectionColumnSysTitle");
                case ALLOCATED_COLUMN_U:
                    return Bundle.getMessage("AllocatedSectionColumnTitle");
                case NEXTSECTION_COLUMN:
                    return Bundle.getMessage("NextSectionColumnSysTitle");
                case NEXTSECTION_COLUMN_U:
                    return Bundle.getMessage("NextSectionColumnTitle");
                case RESTARTCHECKBOX_COLUMN:
                    return(Bundle.getMessage("AutoRestartColumnTitle"));
                case ALLOCATEBUTTON_COLUMN:
                    return(Bundle.getMessage("AllocateButton"));
                case TERMINATEBUTTON_COLUMN:
                    return(Bundle.getMessage("TerminateTrain"));
                case ISAUTO_COLUMN:
                    return(Bundle.getMessage("AutoColumnTitle"));
                case CURRENTSIGNAL_COLUMN:
                    return(Bundle.getMessage("CurrentSignalSysColumnTitle"));
                case CURRENTSIGNAL_COLUMN_U:
                    return(Bundle.getMessage("CurrentSignalColumnTitle"));
                default:
                    return "";
            }
        }

        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
                                justification="better to keep cases in column order rather than to combine")
        public int getPreferredWidth(int col) {
            switch (col) {
                case TRANSIT_COLUMN:
                case TRANSIT_COLUMN_U:
                case TRAIN_COLUMN:
                    return new JTextField(17).getPreferredSize().width;
                case TYPE_COLUMN:
                    return new JTextField(16).getPreferredSize().width;
                case STATUS_COLUMN:
                    return new JTextField(8).getPreferredSize().width;
                case MODE_COLUMN:
                    return new JTextField(11).getPreferredSize().width;
                case ALLOCATED_COLUMN:
                case ALLOCATED_COLUMN_U:
                    return new JTextField(17).getPreferredSize().width;
                case NEXTSECTION_COLUMN:
                case NEXTSECTION_COLUMN_U:
                    return new JTextField(17).getPreferredSize().width;
                case ALLOCATEBUTTON_COLUMN:
                case TERMINATEBUTTON_COLUMN:
                case RESTARTCHECKBOX_COLUMN:
                case ISAUTO_COLUMN:
                case CURRENTSIGNAL_COLUMN:
                case CURRENTSIGNAL_COLUMN_U:
                    return new JTextField(5).getPreferredSize().width;
                default:
                    // fall through
                    break;
            }
            return new JTextField(5).getPreferredSize().width;
        }

        @Override
        public Object getValueAt(int r, int c) {
            int rx = r;
            if (rx >= activeTrainsList.size()) {
                return null;
            }
            ActiveTrain at = activeTrainsList.get(rx);
            switch (c) {
                case TRANSIT_COLUMN:
                    return (at.getTransit().getSystemName());
                case TRANSIT_COLUMN_U:
                    if (at.getTransit() != null && at.getTransit().getUserName() != null) {
                        return (at.getTransit().getUserName());
                    } else {
                        return "";
                    }
                case TRAIN_COLUMN:
                    return (at.getTrainName());
                case TYPE_COLUMN:
                    return (at.getTrainTypeText());
                case STATUS_COLUMN:
                    return (at.getStatusText());
                case MODE_COLUMN:
                    return (at.getModeText());
                case ALLOCATED_COLUMN:
                    if (at.getLastAllocatedSection() != null) {
                        return (at.getLastAllocatedSection().getSystemName());
                    } else {
                        return "<none>";
                    }
                case ALLOCATED_COLUMN_U:
                    if (at.getLastAllocatedSection() != null && at.getLastAllocatedSection().getUserName() != null) {
                        return (at.getLastAllocatedSection().getUserName());
                    } else {
                        return "<none>";
                    }
                case NEXTSECTION_COLUMN:
                    if (at.getNextSectionToAllocate() != null) {
                        return (at.getNextSectionToAllocate().getSystemName());
                    } else {
                        return "<none>";
                    }
                case NEXTSECTION_COLUMN_U:
                    if (at.getNextSectionToAllocate() != null && at.getNextSectionToAllocate().getUserName() != null) {
                        return (at.getNextSectionToAllocate().getUserName());
                    } else {
                        return "<none>";
                    }
                case ALLOCATEBUTTON_COLUMN:
                    return Bundle.getMessage("AllocateButtonName");
                case TERMINATEBUTTON_COLUMN:
                    return Bundle.getMessage("TerminateTrain");
                case RESTARTCHECKBOX_COLUMN:
                    return at.getResetWhenDone();
                case ISAUTO_COLUMN:
                    return at.getAutoRun();
                case CURRENTSIGNAL_COLUMN:
                    if (at.getAutoRun()) {
                        return(at.getAutoActiveTrain().getCurrentSignal());
                    } else {
                        return("NA");
                    }
                case CURRENTSIGNAL_COLUMN_U:
                    if (at.getAutoRun()) {
                        return(at.getAutoActiveTrain().getCurrentSignalUserName());
                    } else {
                        return("NA");
                    }
                default:
                    return (" ");
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == ALLOCATEBUTTON_COLUMN) {
                // open an allocate window
                allocateNextRequested(row);
            }
            if (col == TERMINATEBUTTON_COLUMN) {
                if (activeTrainsList.get(row) != null) {
                    terminateActiveTrain(activeTrainsList.get(row));
                }
            }
            if (col == RESTARTCHECKBOX_COLUMN) {
                ActiveTrain at = null;
                at = activeTrainsList.get(row);
                if (activeTrainsList.get(row) != null) {
                    if (!at.getResetWhenDone()) {
                        at.setResetWhenDone(true);
                        return;
                    }
                    at.setResetWhenDone(false);
                    for (int j = restartingTrainsList.size(); j > 0; j--) {
                        if (restartingTrainsList.get(j - 1) == at) {
                            restartingTrainsList.remove(j - 1);
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Table model for Allocation Request Table in Dispatcher window
     */
    public class AllocationRequestTableModel extends javax.swing.table.AbstractTableModel implements
            java.beans.PropertyChangeListener {

        public static final int TRANSIT_COLUMN = 0;
        public static final int TRANSIT_COLUMN_U = 1;
        public static final int TRAIN_COLUMN = 2;
        public static final int PRIORITY_COLUMN = 3;
        public static final int TRAINTYPE_COLUMN = 4;
        public static final int SECTION_COLUMN = 5;
        public static final int SECTION_COLUMN_U = 6;
        public static final int STATUS_COLUMN = 7;
        public static final int OCCUPANCY_COLUMN = 8;
        public static final int SECTIONLENGTH_COLUMN = 9;
        public static final int ALLOCATEBUTTON_COLUMN = 10;
        public static final int CANCELBUTTON_COLUMN = 11;
        public static final int MAX_COLUMN = 11;

        public AllocationRequestTableModel() {
            super();
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                fireTableDataChanged();
            }
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == CANCELBUTTON_COLUMN) {
                return JButton.class;
            }
            if (c == ALLOCATEBUTTON_COLUMN) {
                return JButton.class;
            }
            //if (c == CANCELRESTART_COLUMN) {
            //    return JButton.class;
            //}
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return MAX_COLUMN + 1;
        }

        @Override
        public int getRowCount() {
            return (allocationRequests.size());
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            if (c == CANCELBUTTON_COLUMN) {
                return (true);
            }
            if (c == ALLOCATEBUTTON_COLUMN) {
                return (true);
            }
            return (false);
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case TRANSIT_COLUMN:
                    return Bundle.getMessage("TransitColumnSysTitle");
                case TRANSIT_COLUMN_U:
                    return Bundle.getMessage("TransitColumnTitle");
                case TRAIN_COLUMN:
                    return Bundle.getMessage("TrainColumnTitle");
                case PRIORITY_COLUMN:
                    return Bundle.getMessage("PriorityLabel");
                case TRAINTYPE_COLUMN:
                    return Bundle.getMessage("TrainTypeColumnTitle");
                case SECTION_COLUMN:
                    return Bundle.getMessage("SectionColumnSysTitle");
                case SECTION_COLUMN_U:
                    return Bundle.getMessage("SectionColumnTitle");
                case STATUS_COLUMN:
                    return Bundle.getMessage("StatusColumnTitle");
                case OCCUPANCY_COLUMN:
                    return Bundle.getMessage("OccupancyColumnTitle");
                case SECTIONLENGTH_COLUMN:
                    return Bundle.getMessage("SectionLengthColumnTitle");
                case ALLOCATEBUTTON_COLUMN:
                    return Bundle.getMessage("AllocateButton");
                case CANCELBUTTON_COLUMN:
                    return Bundle.getMessage("ButtonCancel");
                default:
                    return "";
            }
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case TRANSIT_COLUMN:
                case TRANSIT_COLUMN_U:
                case TRAIN_COLUMN:
                    return new JTextField(17).getPreferredSize().width;
                case PRIORITY_COLUMN:
                    return new JTextField(8).getPreferredSize().width;
                case TRAINTYPE_COLUMN:
                    return new JTextField(15).getPreferredSize().width;
                case SECTION_COLUMN:
                    return new JTextField(25).getPreferredSize().width;
                case STATUS_COLUMN:
                    return new JTextField(15).getPreferredSize().width;
                case OCCUPANCY_COLUMN:
                    return new JTextField(10).getPreferredSize().width;
                case SECTIONLENGTH_COLUMN:
                    return new JTextField(8).getPreferredSize().width;
                case ALLOCATEBUTTON_COLUMN:
                    return new JTextField(12).getPreferredSize().width;
                case CANCELBUTTON_COLUMN:
                    return new JTextField(10).getPreferredSize().width;
                default:
                    // fall through
                    break;
            }
            return new JTextField(5).getPreferredSize().width;
        }

        @Override
        public Object getValueAt(int r, int c) {
            int rx = r;
            if (rx >= allocationRequests.size()) {
                return null;
            }
            AllocationRequest ar = allocationRequests.get(rx);
            switch (c) {
                case TRANSIT_COLUMN:
                    return (ar.getActiveTrain().getTransit().getSystemName());
                case TRANSIT_COLUMN_U:
                    if (ar.getActiveTrain().getTransit() != null && ar.getActiveTrain().getTransit().getUserName() != null) {
                        return (ar.getActiveTrain().getTransit().getUserName());
                    } else {
                        return "";
                    }
                case TRAIN_COLUMN:
                    return (ar.getActiveTrain().getTrainName());
                case PRIORITY_COLUMN:
                    return ("   " + ar.getActiveTrain().getPriority());
                case TRAINTYPE_COLUMN:
                    return (ar.getActiveTrain().getTrainTypeText());
                case SECTION_COLUMN:
                    if (ar.getSection() != null) {
                        return (ar.getSection().getSystemName());
                    } else {
                        return "<none>";
                    }
                case SECTION_COLUMN_U:
                    if (ar.getSection() != null && ar.getSection().getUserName() != null) {
                        return (ar.getSection().getUserName());
                    } else {
                        return "<none>";
                    }
                case STATUS_COLUMN:
                    if (ar.getSection().getState() == Section.FREE) {
                        return Bundle.getMessage("FREE");
                    }
                    return Bundle.getMessage("ALLOCATED");
                case OCCUPANCY_COLUMN:
                    if (!_HasOccupancyDetection) {
                        return Bundle.getMessage("UNKNOWN");
                    }
                    if (ar.getSection().getOccupancy() == Section.OCCUPIED) {
                        return Bundle.getMessage("OCCUPIED");
                    }
                    return Bundle.getMessage("UNOCCUPIED");
                case SECTIONLENGTH_COLUMN:
                    return ("  " + ar.getSection().getLengthI(_UseScaleMeters, _LayoutScale));
                case ALLOCATEBUTTON_COLUMN:
                    return Bundle.getMessage("AllocateButton");
                case CANCELBUTTON_COLUMN:
                    return Bundle.getMessage("ButtonCancel");
                default:
                    return (" ");
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == ALLOCATEBUTTON_COLUMN) {
                // open an allocate window
                allocateRequested(row);
            }
            if (col == CANCELBUTTON_COLUMN) {
                // open an allocate window
                cancelAllocationRequest(row);
            }
        }
    }

    /**
     * Table model for Allocated Section Table
     */
    public class AllocatedSectionTableModel extends javax.swing.table.AbstractTableModel implements
            java.beans.PropertyChangeListener {

        public static final int TRANSIT_COLUMN = 0;
        public static final int TRANSIT_COLUMN_U = 1;
        public static final int TRAIN_COLUMN = 2;
        public static final int SECTION_COLUMN = 3;
        public static final int SECTION_COLUMN_U = 4;
        public static final int OCCUPANCY_COLUMN = 5;
        public static final int USESTATUS_COLUMN = 6;
        public static final int RELEASEBUTTON_COLUMN = 7;
        public static final int MAX_COLUMN = 7;

        public AllocatedSectionTableModel() {
            super();
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                fireTableDataChanged();
            }
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == RELEASEBUTTON_COLUMN) {
                return JButton.class;
            }
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return MAX_COLUMN + 1;
        }

        @Override
        public int getRowCount() {
            return (allocatedSections.size());
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            if (c == RELEASEBUTTON_COLUMN) {
                return (true);
            }
            return (false);
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case TRANSIT_COLUMN:
                    return Bundle.getMessage("TransitColumnSysTitle");
                case TRANSIT_COLUMN_U:
                    return Bundle.getMessage("TransitColumnTitle");
                case TRAIN_COLUMN:
                    return Bundle.getMessage("TrainColumnTitle");
                case SECTION_COLUMN:
                    return Bundle.getMessage("AllocatedSectionColumnSysTitle");
                case SECTION_COLUMN_U:
                    return Bundle.getMessage("AllocatedSectionColumnTitle");
                case OCCUPANCY_COLUMN:
                    return Bundle.getMessage("OccupancyColumnTitle");
                case USESTATUS_COLUMN:
                    return Bundle.getMessage("UseStatusColumnTitle");
                case RELEASEBUTTON_COLUMN:
                    return Bundle.getMessage("ReleaseButton");
                default:
                    return "";
            }
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case TRANSIT_COLUMN:
                case TRANSIT_COLUMN_U:
                case TRAIN_COLUMN:
                    return new JTextField(17).getPreferredSize().width;
                case SECTION_COLUMN:
                case SECTION_COLUMN_U:
                    return new JTextField(25).getPreferredSize().width;
                case OCCUPANCY_COLUMN:
                    return new JTextField(10).getPreferredSize().width;
                case USESTATUS_COLUMN:
                    return new JTextField(15).getPreferredSize().width;
                case RELEASEBUTTON_COLUMN:
                    return new JTextField(12).getPreferredSize().width;
                default:
                    // fall through
                    break;
            }
            return new JTextField(5).getPreferredSize().width;
        }

        @Override
        public Object getValueAt(int r, int c) {
            int rx = r;
            if (rx >= allocatedSections.size()) {
                return null;
            }
            AllocatedSection as = allocatedSections.get(rx);
            switch (c) {
                case TRANSIT_COLUMN:
                    return (as.getActiveTrain().getTransit().getSystemName());
                case TRANSIT_COLUMN_U:
                    if (as.getActiveTrain().getTransit() != null && as.getActiveTrain().getTransit().getUserName() != null) {
                        return (as.getActiveTrain().getTransit().getUserName());
                    } else {
                        return "";
                    }
                case TRAIN_COLUMN:
                    return (as.getActiveTrain().getTrainName());
                case SECTION_COLUMN:
                    if (as.getSection() != null) {
                        return (as.getSection().getSystemName());
                    } else {
                        return "<none>";
                    }
                case SECTION_COLUMN_U:
                    if (as.getSection() != null && as.getSection().getUserName() != null) {
                        return (as.getSection().getUserName());
                    } else {
                        return "<none>";
                    }
                case OCCUPANCY_COLUMN:
                    if (!_HasOccupancyDetection) {
                        return Bundle.getMessage("UNKNOWN");
                    }
                    if (as.getSection().getOccupancy() == Section.OCCUPIED) {
                        return Bundle.getMessage("OCCUPIED");
                    }
                    return Bundle.getMessage("UNOCCUPIED");
                case USESTATUS_COLUMN:
                    if (!as.getEntered()) {
                        return Bundle.getMessage("NotEntered");
                    }
                    if (as.getExited()) {
                        return Bundle.getMessage("Exited");
                    }
                    return Bundle.getMessage("Entered");
                case RELEASEBUTTON_COLUMN:
                    return Bundle.getMessage("ReleaseButton");
                default:
                    return (" ");
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == RELEASEBUTTON_COLUMN) {
                releaseAllocatedSectionFromTable(row);
            }
        }
    }

    /*
     * Mouse popup stuff
     */

    /**
     * Process the column header click
     * @param e     the evnt data
     * @param table the JTable
     */
    protected void showTableHeaderPopup(MouseEvent e, JTable table) {
        JPopupMenu popupMenu = new JPopupMenu();
        XTableColumnModel tcm = (XTableColumnModel) table.getColumnModel();
        for (int i = 0; i < tcm.getColumnCount(false); i++) {
            TableColumn tc = tcm.getColumnByModelIndex(i);
            String columnName = table.getModel().getColumnName(i);
            if (columnName != null && !columnName.equals("")) {
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(table.getModel().getColumnName(i), tcm.isColumnVisible(tc));
                menuItem.addActionListener(new HeaderActionListener(tc, tcm));
                popupMenu.add(menuItem);
            }

        }
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Adds the column header pop listener to a JTable using XTableColumnModel
     * @param table The JTable effected.
     */
    protected void addMouseListenerToHeader(JTable table) {
        MouseListener mouseHeaderListener = new TableHeaderListener(table);
        table.getTableHeader().addMouseListener(mouseHeaderListener);
    }

    static protected class HeaderActionListener implements ActionListener {

        TableColumn tc;
        XTableColumnModel tcm;

        HeaderActionListener(TableColumn tc, XTableColumnModel tcm) {
            this.tc = tc;
            this.tcm = tcm;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBoxMenuItem check = (JCheckBoxMenuItem) e.getSource();
            //Do not allow the last column to be hidden
            if (!check.isSelected() && tcm.getColumnCount(true) == 1) {
                return;
            }
            tcm.setColumnVisible(tc, check.isSelected());
        }
    }

    /**
     * Class to support Columnheader popup menu on XTableColum model.
     */
    class TableHeaderListener extends MouseAdapter {

        JTable table;

        TableHeaderListener(JTable tbl) {
            super();
            table = tbl;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e, table);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e, table);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e, table);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DispatcherFrame.class);

}
