package jmri.jmrix.nce.clockmon;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.Timer;
import jmri.InstanceManager;
import jmri.Timebase;
import jmri.TimebaseRateException;
import jmri.jmrix.nce.NceListener;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;
import jmri.jmrix.nce.swing.NcePanelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying and programming a NCE clock monitor.
 * <p>
 * Some of the message formats used in this class are Copyright NCE Inc. and
 * used with permission as part of the JMRI project. That permission does not
 * extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact NCE Inc
 * for separate permission.
 *
 * Notes:
 *
 * 1. the commands for time don't include seconds so I had to use memory write
 * to sync nce clock.
 *
 * 2. I tried fiddling with the internal nce clock loop values, didn't work.
 *
 * 3. to sync nce to internal clock: A. set an alarm about 5 seconds before next
 * minute B. read nce clock C. compute error and record last X errors for
 * correction calc D. adjust nce clock as needed E. reset alarm after next
 * internal minute ticks
 *
 * 4. to sync internal to nce clock A. every so often, read nce clock and
 * compare to internal B. compute error and record last X errors for correction
 * calc C. adjust internal clock rate factor as needed
 *
 * 5. The clock message only seems to go out to the throttles on the tic of the
 * minute.
 *
 * 6. The nce clock must be left running, or it doesn't tic and
 * therefore doesn't go out over the bus.
 *
 * @author Ken Cameron Copyright (C) 2007
 * derived from loconet.clockmonframe by Bob Jacobson Copyright (C) 2003
 */
public class ClockMonPanel extends jmri.jmrix.nce.swing.NcePanel implements NceListener {

    public static final int CS_CLOCK_MEM_ADDR = 0xDC00;
    public static final int CS_CLOCK_MEM_SIZE = 0x10;
    public static final int CS_CLOCK_SCALE = 0x00;
    public static final int CS_CLOCK_TICK = 0x01;
    public static final int CS_CLOCK_SECONDS = 0x02;
    public static final int CS_CLOCK_MINUTES = 0x03;
    public static final int CS_CLOCK_HOURS = 0x04;
    public static final int CS_CLOCK_AMPM = 0x05;
    public static final int CS_CLOCK_1224 = 0x06;
    public static final int CS_CLOCK_STATUS = 0x0D;
    public static final int CMD_CLOCK_SET_TIME_SIZE = 0x03;
    public static final int CMD_CLOCK_SET_PARAM_SIZE = 0x02;
    public static final int CMD_CLOCK_SET_RUN_SIZE = 0x01;
    public static final int CMD_CLOCK_SET_REPLY_SIZE = 0x01;
    public static final int CMD_MEM_SET_REPLY_SIZE = 0x01;
    public static final int MAX_ERROR_ARRAY = 4;
    public static final double MIN_POLLING_INTERVAL = 1.0;
    public static final double MAX_POLLING_INTERVAL = 120;
    public static final double DEFAULT_POLLING_INTERVAL = 5;
    public static final double TARGET_SYNC_DELAY = 55;
    public static final int SYNCMODE_OFF = 0;    //0 - clocks independent
    public static final int SYNCMODE_NCE_MASTER = 1;  //1 - NCE sets Internal
    public static final int SYNCMODE_INTERNAL_MASTER = 2; //2 - Internal sets NCE
    public static final int WAIT_CMD_EXECUTION = 1000;
    private static final long MAX_SECONDS_IN_DAY = 24 * 3600;
    private static final double ESTIMATED_NCE_RATE_FACTOR = 0.92;
    DecimalFormat fiveDigits = new DecimalFormat("0.00000");
    DecimalFormat fourDigits = new DecimalFormat("0.0000");
    DecimalFormat threeDigits = new DecimalFormat("0.000");
    DecimalFormat twoDigits = new DecimalFormat("0.00");

    private int waiting = 0;
    private int clockMode = SYNCMODE_OFF;
    private boolean waitingForCmdRead = false;
    private boolean waitingForCmdStop = false;
    private boolean waitingForCmdStart = false;
    private boolean waitingForCmdRatio = false;
    private boolean waitingForCmdTime = false;
    private boolean waitingForCmd1224 = false;
    private boolean updateTimeFromRead = false;
    private boolean updateRatioFromRead = false;
    private boolean updateFormatFromRead = false;
    private boolean updateStatusFromRead = false;
    private NceReply lastClockReadPacket = null;
    //private Date lastClockReadAtTime;
    private int nceLastHour;
    private int nceLastMinute;
    private int nceLastSecond;
    private int nceLastRatio;
    private boolean nceLastAmPm;
    private boolean nceLast1224;
    private boolean nceLastRunning;
    private double internalLastRatio;
    private boolean internalLastRunning;
    private double pollingInterval = DEFAULT_POLLING_INTERVAL;
    private final ArrayList<Double> priorDiffs = new ArrayList<>();
    private final ArrayList<Double> priorOffsetErrors = new ArrayList<>();
    private final ArrayList<Double> priorCorrections = new ArrayList<>();
    private double syncInterval = TARGET_SYNC_DELAY;
    private int internalSyncInitStateCounter = 0;
    private int internalSyncRunStateCounter = 0;
    private double ncePidGainPv = 0.04;
    private double ncePidGainIv = 0.01;
    private double ncePidGainDv = 0.005;
    private final double intPidGainPv = 0.02;
    private final double intPidGainIv = 0.001;
    private final double intPidGainDv = 0.01;

    private final double rateChgMinimum = 0.001;

    private int nceSyncInitStateCounter = 0; // NCE master sync initialzation state machine
    private int nceSyncRunStateCounter = 0; // NCE master sync runtime state machine
    private int alarmDisplayStateCounter = 0; // manages the display update from the alarm

    Timebase internalClock;
    Timer timerDisplayUpdate = null;
    Timer alarmSyncUpdate = null;

    JTextField hours = new JTextField("  00");
    JTextField minutes = new JTextField("  00");
    JTextField seconds = new JTextField("  00");

    JTextField rateNce = new JTextField("   1");
    JTextField amPm = new JTextField(2);
    JCheckBox twentyFour = new JCheckBox(Bundle.getMessage("CheckBox24HourFormat"));
    JTextField status = new JTextField(10);

    JRadioButton setSyncModeNceMaster = new JRadioButton(Bundle.getMessage("ClockModeNCE"));
    JRadioButton setSyncModeInternalMaster = new JRadioButton(Bundle.getMessage("ClockModeInternal"));
    JRadioButton setSyncModeOff = new JRadioButton(Bundle.getMessage("ClockModeIndependent"));

    JTextField internalDisplayStatus = new JTextField(60);

    JTextField nceDisplayStatus = new JTextField(60);

    JTextField pollingSpeed = new JTextField(5);

    JTextField ncePidGainP = new JTextField(7);
    JTextField ncePidGainI = new JTextField(7);
    JTextField ncePidGainD = new JTextField(7);
    JTextField intPidGainP = new JTextField(7);
    JTextField intPidGainI = new JTextField(7);
    JTextField intPidGainD = new JTextField(7);

    transient java.beans.PropertyChangeListener minuteChangeListener;

    JButton setSyncButton = new JButton(Bundle.getMessage("SetSyncMode"));
    JButton setClockButton = new JButton(Bundle.getMessage("SetHoursMinutes"));
    JButton setRatioButton = new JButton(Bundle.getMessage("SetRatio"));
    JButton set1224Button = new JButton(Bundle.getMessage("Set12/24Mode"));
    JButton setStopNceButton = new JButton(Bundle.getMessage("StopNceClock"));
    JButton setStartNceButton = new JButton(Bundle.getMessage("StartNceClock"));
    JButton readButton = new JButton(Bundle.getMessage("ReadAll"));
    JButton setPollingSpeedButton = new JButton(Bundle.getMessage("SetInterfaceUpdRate"));
    JButton setPidButton = new JButton(Bundle.getMessage("SetPid"));

    private NceTrafficController tc = null;

    public ClockMonPanel() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initContext(Object context) {
        if (context instanceof NceSystemConnectionMemo) {
            try {
                initComponents((NceSystemConnectionMemo) context);
            } catch (Exception e) {
                log.error("NceClockMon initContext failed");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.nce.clockmon.ClockMonFrame";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        StringBuilder x = new StringBuilder();
        if (memo != null) {
            x.append(memo.getUserName());
        } else {
            x.append("NCE_");
        }
        x.append(": ");
        x.append(Bundle.getMessage("TitleNceClockMonitor"));
        return x.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(NceSystemConnectionMemo m) {
        this.memo = m;
        this.tc = m.getNceTrafficController();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Internal Clock Info Panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel pane2 = new JPanel();
        GridBagLayout gLayout = new GridBagLayout();
        GridBagConstraints gConstraints = new GridBagConstraints();

        javax.swing.border.Border pane2Border = BorderFactory.createEtchedBorder();
        javax.swing.border.Border pane2Titled = BorderFactory.createTitledBorder(pane2Border,
                Bundle.getMessage("InternalClockStatusBorderText"));
        pane2.setBorder(pane2Titled);
        pane2.add(internalDisplayStatus);
        internalDisplayStatus.setEditable(false);
        internalDisplayStatus.setBorder(BorderFactory.createEmptyBorder());
        add(pane2);

        // NCE Clock Info Panel
        pane2 = new JPanel();
        pane2Border = BorderFactory.createEtchedBorder();
        pane2Titled = BorderFactory.createTitledBorder(pane2Border,
                Bundle.getMessage("NceClockStatusBorderText"));
        pane2.setBorder(pane2Titled);
        pane2.add(nceDisplayStatus);
        nceDisplayStatus.setEditable(false);
        nceDisplayStatus.setBorder(BorderFactory.createEmptyBorder());
        add(pane2);

        // setting time items
        pane2 = new JPanel();
        pane2Border = BorderFactory.createEtchedBorder();
        pane2Titled = BorderFactory.createTitledBorder(pane2Border,
                Bundle.getMessage("SetClockValuesBorderText"));
        pane2.setBorder(pane2Titled);
        pane2.add(new JLabel(Bundle.getMessage("LabelTime")));
        pane2.add(hours);
        pane2.add(new JLabel(Bundle.getMessage("LabelTimeSep")));
        pane2.add(minutes);
        pane2.add(new JLabel(Bundle.getMessage("LabelTimeSep")));
        pane2.add(seconds);
        seconds.setEditable(false);
        pane2.add(new JLabel(" "));
        pane2.add(amPm);
        amPm.setEditable(false);
        pane2.add(new JLabel(" "));
        pane2.add(setClockButton);
        add(pane2);

        // set clock ratio items
        pane2 = new JPanel();
        pane2Border = BorderFactory.createEtchedBorder();
        pane2Titled = BorderFactory.createTitledBorder(pane2Border,
                Bundle.getMessage("SetClockRatioBorderText"));
        pane2.setBorder(pane2Titled);
        pane2.add(new JLabel(Bundle.getMessage("LabelRatio")));
        pane2.add(rateNce);
        pane2.add(new JLabel(Bundle.getMessage("LabelToOne")));
        pane2.add(setRatioButton);
        add(pane2);

        // add 12/24 clock options
        pane2 = new JPanel();
        pane2Border = BorderFactory.createEtchedBorder();
        pane2Titled = BorderFactory.createTitledBorder(pane2Border,
                Bundle.getMessage("SetClock12/24ModeBorderText"));
        pane2.setBorder(pane2Titled);
        pane2.add(twentyFour);
        pane2.add(new JLabel(" "));
        pane2.add(set1224Button);
        add(pane2);

        //  pane2 = new JPanel();
        //  pane2.setLayout(new BoxLayout(pane2, BoxLayout.X_AXIS));
        //  pane2.add(new JLabel(" "));
        //  pane2.add(status);
        //  add(pane2);
        pane2 = new JPanel();
        pane2Border = BorderFactory.createEtchedBorder();
        pane2Titled = BorderFactory.createTitledBorder(pane2Border,
                Bundle.getMessage("InterfaceCommandBorderText"));
        pane2.setBorder(pane2Titled);
        pane2.setLayout(gLayout);
        gConstraints.gridx = 0;
        gConstraints.gridy = 0;
        gConstraints.gridwidth = 1;
        gConstraints.gridheight = 1;
        gConstraints.ipadx = 10;
        gConstraints.ipady = 1;
        gConstraints.insets = new Insets(1, 1, 1, 1);
        pane2.add(setStartNceButton, gConstraints);
        gConstraints.gridx++;
        pane2.add(setStopNceButton, gConstraints);
        gConstraints.gridx++;
        pane2.add(readButton, gConstraints);

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(setSyncModeInternalMaster);
        modeGroup.add(setSyncModeNceMaster);
        modeGroup.add(setSyncModeOff);

        gConstraints.gridx = 0;
        gConstraints.gridy++;
        gConstraints.gridwidth = 3;
        pane2.add(setSyncModeNceMaster, gConstraints);
        gConstraints.gridy++;
        pane2.add(setSyncModeInternalMaster, gConstraints);
        gConstraints.gridy++;
        pane2.add(setSyncModeOff, gConstraints);
        gConstraints.gridy++;
        pane2.add(setSyncButton, gConstraints);
        setSyncModeInternalMaster.setEnabled(true);
        setSyncModeNceMaster.setEnabled(true);
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) { // needs memory commands to sync
            setSyncModeInternalMaster.setEnabled(false);
            setSyncModeNceMaster.setEnabled(false);
        }
        add(pane2);

        // add polling speed
        pane2 = new JPanel();
        pane2Border = BorderFactory.createEtchedBorder();
        pane2Titled = BorderFactory.createTitledBorder(pane2Border,
                Bundle.getMessage("InterfaceUpdRateBorderText"));
        pane2.setBorder(pane2Titled);
        pane2.add(new JLabel(Bundle.getMessage("InterfaceUpdRate")));
        pane2.add(new JLabel(" "));
        pane2.add(pollingSpeed);
        pollingSpeed.setText("" + pollingInterval);
        pane2.add(new JLabel(" "));
        pane2.add(new JLabel(Bundle.getMessage("InterfaceUpdRateSufix")));
        pane2.add(new JLabel(" "));
        pane2.add(setPollingSpeedButton);
        add(pane2);

//        // add PID values
//        gLayout = new GridBagLayout();
//        gConstraints = new GridBagConstraints();
//        pane2 = new JPanel();
//        pane2Border = BorderFactory.createEtchedBorder();
//        pane2Titled = BorderFactory.createTitledBorder(pane2Border,
//                                                       Bundle.getMessage("InterfacePidBorderText"));
//        pane2.setBorder(pane2Titled);
//        pane2.setLayout(gLayout);
//        gConstraints.gridx = 0;
//        gConstraints.gridy = 0;
//        gConstraints.gridwidth = 1;
//        gConstraints.gridheight = 1;
//        gConstraints.ipadx = 10;
//        gConstraints.ipady = 1;
//        gConstraints.insets = new Insets(3, 3, 3, 3);
//        pane2.add(new JLabel(Bundle.getMessage("InterfacePidNce")), gConstraints);
//        gConstraints.gridx++;
//        pane2.add(new JLabel(Bundle.getMessage("InterfacePidGainP")), gConstraints);
//        gConstraints.gridx++;
//        pane2.add(ncePidGainP, gConstraints);
//        gConstraints.gridx++;
//        pane2.add(new JLabel(Bundle.getMessage("InterfacePidGainI")), gConstraints);
//        gConstraints.gridx++;
//        pane2.add(ncePidGainI, gConstraints);
//        gConstraints.gridx++;
//        pane2.add(new JLabel(Bundle.getMessage("InterfacePidGainD")), gConstraints);
//        gConstraints.gridx++;
//        pane2.add(ncePidGainD, gConstraints);
//        gConstraints.gridx++;
//        gConstraints.gridheight = 2;
//        pane2.add(setPidButton, gConstraints);
//        gConstraints.gridheight = 0;
//        gConstraints.gridx = 0;
//        gConstraints.gridy = 1;
//        pane2.add(new JLabel(Bundle.getMessage("InterfacePidInt")), gConstraints);
//        gConstraints.gridx++;
//        pane2.add(new JLabel(Bundle.getMessage("InterfacePidGainP")), gConstraints);
//        gConstraints.gridx++;
//        pane2.add(intPidGainP, gConstraints);
//        gConstraints.gridx++;
//        pane2.add(new JLabel(Bundle.getMessage("InterfacePidGainI")), gConstraints);
//        gConstraints.gridx++;
//        pane2.add(intPidGainI, gConstraints);
//        gConstraints.gridx++;
//        pane2.add(new JLabel(Bundle.getMessage("InterfacePidGainD")), gConstraints);
//        gConstraints.gridx++;
//        pane2.add(intPidGainD, gConstraints);
//        ncePidGainP.setText(fiveDigits.format(ncePidGainPv));
//        ncePidGainI.setText(fiveDigits.format(ncePidGainIv));
//        ncePidGainD.setText(fiveDigits.format(ncePidGainDv));
//        intPidGainP.setText(fiveDigits.format(intPidGainPv));
//        intPidGainI.setText(fiveDigits.format(intPidGainIv));
//        intPidGainD.setText(fiveDigits.format(intPidGainDv));
//        add(pane2);
        // install "read" button handler
        readButton.addActionListener((ActionEvent a) -> {
            issueReadAllRequest();
        });
        // install "set" button handler
        setClockButton.addActionListener((ActionEvent a) -> {
            issueClockSet(Integer.parseInt(hours.getText().trim()),
                    Integer.parseInt(minutes.getText().trim()),
                    Integer.parseInt(seconds.getText().trim())
            );
        });
        // install "stop" clock button handler
        setStopNceButton.addActionListener((ActionEvent a) -> {
            issueClockStop();
        });
        // install "start" clock button handler
        setStartNceButton.addActionListener((ActionEvent a) -> {
            issueClockStart();
        });
        // install set fast clock ratio
        setRatioButton.addActionListener((ActionEvent a) -> {
            changeNceClockRatio();
        });
        // install set 12/24 button
        set1224Button.addActionListener((ActionEvent a) -> {
            issueClock1224(twentyFour.isSelected());
        });
        // install Sync Change Clock button
        setSyncButton.addActionListener((ActionEvent a) -> {
            changeSyncMode();
        });

        // install "setPolling" button handler
        setPollingSpeedButton.addActionListener((ActionEvent a) -> {
            changePollingSpeed(Double.parseDouble(pollingSpeed.getText().trim()));
        });

        // install "setPid" button handler
        setPidButton.addActionListener((ActionEvent a) -> {
            changePidValues();
        });

        if (clockMode == SYNCMODE_OFF) {
            setSyncModeOff.setSelected(true);
        }
        if (clockMode == SYNCMODE_INTERNAL_MASTER) {
            setSyncModeInternalMaster.setSelected(true);
        }
        if (clockMode == SYNCMODE_NCE_MASTER) {
            setSyncModeNceMaster.setSelected(true);
        }
        this.setSize(400, 300);

        // Create a timebase listener for the Minute change events
        internalClock = InstanceManager.getNullableDefault(jmri.Timebase.class);
        if (internalClock == null) {
            log.error("No Timebase Instance; clock will not run");
            return;
        }
        minuteChangeListener = (PropertyChangeEvent e) -> {
            newInternalMinute();
        };
        internalClock.addMinuteChangeListener(minuteChangeListener);

        // start display alarm timer
        alarmDisplayUpdateHandler();
    }

    //  ignore replies
    @Override
    public void message(NceMessage m) {
        log.error("clockmon message received: {}", m);
    }

    @Override
    public void reply(NceReply r) {
        log.trace("nceReplyCatcher() waiting: {} watingForRead: {} waitingForCmdTime: {} waitingForCmd1224: {} waitingForCmdRatio: {} waitingForCmdStop: {} waitingForCmdStart: {}",
                waiting, waitingForCmdRead, waitingForCmdTime, waitingForCmd1224, waitingForCmdRatio, waitingForCmdStop, waitingForCmdStart);
        if (waiting <= 0) {
            log.debug("unexpected response");
            return;
        }
        waiting--;
        if (waitingForCmdRead && r.getNumDataElements() == CS_CLOCK_MEM_SIZE) {
            readClockPacket(r);
            waitingForCmdRead = false;
            callStateMachines();
            return;
        }
        if (waitingForCmdTime) {
            if (r.getNumDataElements() != CMD_CLOCK_SET_REPLY_SIZE) {
                log.error("NCE clock command reply, invalid length:{}", r.getNumDataElements());
                return;
            } else {
                waitingForCmdTime = false;
                if (r.getElement(0) != '!') {
                    log.error("NCE set clock replied: {}", r.getElement(0));
                }
                callStateMachines();
                return;
            }
        }
        if (r.getNumDataElements() != CMD_CLOCK_SET_REPLY_SIZE) {
            log.error("NCE clock command reply, invalid length:{}", r.getNumDataElements());
            return;
        } else {
            if (waitingForCmd1224) {
                waitingForCmd1224 = false;
                if (r.getElement(0) != '!') {
                    log.error("NCE set clock 12/24 replied:{}", r.getElement(0));
                }
                callStateMachines();
                return;
            }
            if (waitingForCmdRatio) {
                waitingForCmdRatio = false;
                if (r.getElement(0) != '!') {
                    log.error("NCE clock ratio cmd replied:{}", r.getElement(0));
                }
                callStateMachines();
                return;
            }
            if (waitingForCmdStop) {
                waitingForCmdStop = false;
                if (r.getElement(0) != '!') {
                    log.error("NCE clock stop cmd replied:{}", r.getElement(0));
                }
                callStateMachines();
                return;
            }
            if (waitingForCmdStart) {
                waitingForCmdStart = false;
                if (r.getElement(0) != '!') {
                    log.error("NCE clock start cmd replied:{}", r.getElement(0));
                }
                callStateMachines();
                return;
            }
        }
        log.debug("unexpected response");
    }

    private void callStateMachines() {
        if (internalSyncInitStateCounter > 0) {
            internalSyncInitStates();
        }
        if (internalSyncRunStateCounter > 0) {
            internalSyncRunStates();
        }
        if (nceSyncInitStateCounter > 0) {
            nceSyncInitStates();
        }
        if (nceSyncRunStateCounter > 0) {
            nceSyncRunStates();
        }
        if (alarmDisplayStateCounter > 0) {
            alarmDisplayStates();
        }
    }

    private void readClockPacket(NceReply r) {
        NceReply priorClockReadPacket = lastClockReadPacket;
        int priorNceRatio = nceLastRatio;
        boolean priorNceRunning = nceLastRunning;
        lastClockReadPacket = r;
        //lastClockReadAtTime = internalClock.getTime();
        //log.debug("readClockPacket - at time: " + lastClockReadAtTime);
        nceLastHour = r.getElement(CS_CLOCK_HOURS) & 0xFF;
        nceLastMinute = r.getElement(CS_CLOCK_MINUTES) & 0xFF;
        nceLastSecond = r.getElement(CS_CLOCK_SECONDS) & 0xFF;
        nceLast1224 = r.getElement(CS_CLOCK_1224) == 1;
        nceLastAmPm = r.getElement(CS_CLOCK_AMPM) == 'A';
        int sc = r.getElement(CS_CLOCK_SCALE) & 0xFF;
        if (sc > 0) {
            nceLastRatio = 250 / sc;
        }
        if (clockMode == SYNCMODE_NCE_MASTER) {
            if (priorClockReadPacket != null && priorNceRatio != nceLastRatio) {
                if (log.isDebugEnabled()) {
                    log.debug("NCE Change Rate from cab: prior vs last: " + priorNceRatio + " vs " + nceLastRatio);
                }
                rateNce.setText("" + nceLastRatio);
                nceSyncInitStateCounter = 1;
                nceSyncInitStates();
            }
        }
        nceLastRunning = r.getElement(CS_CLOCK_STATUS) != 1;
        if (clockMode == SYNCMODE_NCE_MASTER) {
            if (priorClockReadPacket != null && priorNceRunning != nceLastRunning) {
                if (log.isDebugEnabled()) {
                    log.debug("NCE Stop/Start: prior vs last: " + priorNceRunning + " vs " + nceLastRunning);
                }
                if (nceLastRunning) {
                    nceSyncInitStateCounter = 1;
                } else {
                    nceSyncInitStateCounter = -1;
                }
                nceSyncInitStates();
                internalClock.setRun(nceLastRunning);
            }
        }
        updateSettingsFromNce();
    }

    private void alarmDisplayUpdateHandler() {
        if (pollingInterval < MIN_POLLING_INTERVAL || pollingInterval > MAX_POLLING_INTERVAL) {
            log.error("reseting pollingInterval, invalid value:{}", pollingInterval);
            pollingInterval = DEFAULT_POLLING_INTERVAL;
        }
        // initialize things if not running
        alarmSetup();
        alarmDisplayStates();
        updateInternalClockDisplay();
    }

    private void alarmSetup() {
        // initialize things if not running
        if (timerDisplayUpdate == null) {
            timerDisplayUpdate = new Timer((int) (pollingInterval * 1000.0), (ActionEvent e) -> {
                alarmDisplayUpdateHandler();
            });
        }
        timerDisplayUpdate.setInitialDelay((1 * 1000));
        timerDisplayUpdate.setRepeats(true);     // in case we run by
        timerDisplayUpdate.start();
        alarmDisplayStateCounter = 1;
    }

    private void alarmSyncInit() {
        // initialize things if not running
        int delay = 1000;
        if (alarmSyncUpdate == null) {
            alarmSyncUpdate = new Timer(delay, (ActionEvent e) -> {
                alarmSyncHandler();
            });
            if (clockMode == SYNCMODE_INTERNAL_MASTER) {
                delay = (int) (syncInterval * 1000 / nceLastRatio);
                alarmSyncUpdate.setRepeats(false);
            }
            if (clockMode == SYNCMODE_NCE_MASTER) {
                delay = 10 * 1000;
                alarmSyncUpdate.setRepeats(true);
            }
            alarmSyncUpdate.setInitialDelay(delay);
            alarmSyncUpdate.setDelay(delay);
            alarmSyncUpdate.stop();
        }
    }

    @SuppressWarnings("deprecation")
    private void alarmSyncStart() {
        // initialize things if not running
        Date now = internalClock.getTime();
        if (alarmSyncUpdate == null) {
            alarmSyncInit();
        }
        int delay = 60 * 1000;
        if (clockMode == SYNCMODE_INTERNAL_MASTER) {
            if (syncInterval - 3 - now.getSeconds() <= 0) {
                delay = 10; // basically trigger right away
            } else {
                delay = (int) ((syncInterval - now.getSeconds()) * 1000 / internalClock.getRate());
            }
        }
        if (clockMode == SYNCMODE_NCE_MASTER) {
            delay = 10 * 1000;
        }
        alarmSyncUpdate.setDelay(delay);
        alarmSyncUpdate.setInitialDelay(delay);
        alarmSyncUpdate.start();
        log.trace("alarmSyncStart delay: {} @ {}", delay, now);
    }

    private void alarmSyncHandler() {
        if (clockMode == SYNCMODE_INTERNAL_MASTER) {
            internalSyncRunStateCounter = 1;
            internalSyncRunStates();
        }
        if (clockMode == SYNCMODE_NCE_MASTER) {
            if (nceSyncRunStateCounter == 0) {
                nceSyncRunStateCounter = 1;
                nceSyncRunStates();
            }
        }
        if (clockMode == SYNCMODE_OFF) {
            alarmSyncUpdate.stop();
        }
        if (alarmDisplayStateCounter == 0) {
            alarmDisplayStateCounter = 1;
            alarmDisplayStates();
        }
    }

    private void alarmDisplayStates() {
        int priorState;
        do {
            log.trace("alarmDisplayStates: before: {} {}", alarmDisplayStateCounter, internalClock.getTime());
            priorState = alarmDisplayStateCounter;
            switch (alarmDisplayStateCounter) {
                case 0:
                    // inactive
                    break;
                case 1:
                    // issue nce read
                    internalClockStatusCheck();
                    issueReadOnlyRequest();
                    alarmDisplayStateCounter++;
                    break;
                case 2:
                    // wait for update
                    if (!waitingForCmdRead) {
                        alarmDisplayStateCounter++;
                    }
                    break;
                case 3:
                    // update clock display
                    alarmDisplayStateCounter = 0;
                    updateNceClockDisplay();
                    updateInternalClockDisplay();
                    break;
                default:
                    log.warn("Unexpected alarmDisplayStateCounter {} in alarmDisplayStates", alarmDisplayStateCounter);
                    break;
            }
            log.trace("alarmDisplayStates: after: {} {}", alarmDisplayStateCounter, internalClock.getTime());
        } while (priorState != alarmDisplayStateCounter);
    }

    private double getNceTime() {
        double nceTime = 0;
        if (lastClockReadPacket != null) {
            nceTime = (lastClockReadPacket.getElement(CS_CLOCK_HOURS) * 3600)
                    + (lastClockReadPacket.getElement(CS_CLOCK_MINUTES) * 60)
                    + lastClockReadPacket.getElement(CS_CLOCK_SECONDS)
                    + (lastClockReadPacket.getElement(CS_CLOCK_TICK) * 0.25);
        }
        return (nceTime);
    }

    @SuppressWarnings("deprecation")
    private Date getNceDate() {
        Date now = internalClock.getTime();
        if (lastClockReadPacket != null) {
            now.setHours(lastClockReadPacket.getElement(CS_CLOCK_HOURS));
            now.setMinutes(lastClockReadPacket.getElement(CS_CLOCK_MINUTES));
            now.setSeconds(lastClockReadPacket.getElement(CS_CLOCK_SECONDS));
        }
        return (now);
    }

    @SuppressWarnings("deprecation")
    private double getIntTime() {
        Date now = internalClock.getTime();
        int ms = (int) (now.getTime() % 1000);
        int ss = now.getSeconds();
        int mm = now.getMinutes();
        int hh = now.getHours();
        log.trace("getIntTime: {}:{}:{}.{}", hh, mm, ss, ms);
        return ((hh * 60 * 60) + (mm * 60) + ss + (ms / 1000));
    }

    private void changeNceClockRatio() {
        try {
            int newRatio = Integer.parseInt(rateNce.getText().trim());
            issueClockRatio(newRatio);
        } catch (NumberFormatException e) {
            log.error("Invalid value: {}", rateNce.getText().trim());
        }
    }

    @SuppressWarnings("deprecation")
    private void internalSyncInitStates() {
        Date now = internalClock.getTime();
        int priorState;
        do {
            if (internalSyncInitStateCounter != 0) {
                log.trace("internalSyncInitStates begin: {} @ {}", internalSyncInitStateCounter, now);
            }
            priorState = internalSyncInitStateCounter;
            switch (internalSyncInitStateCounter) {
                case 0:
                    // do nothing, idle state
                    break;
                case -1:
                    // cleanup, halt state
                    alarmSyncUpdate.stop();
                    internalSyncInitStateCounter = 0;
                    internalSyncRunStateCounter = 0;
                    setClockButton.setEnabled(true);
                    setRatioButton.setEnabled(true);
                    set1224Button.setEnabled(true);
                    setStopNceButton.setEnabled(true);
                    setStartNceButton.setEnabled(true);
                    break;
                case -3:
                    // stopping from internal clock
                    internalSyncRunStateCounter = 0;
                    alarmSyncUpdate.stop();
                    issueClockStop();
                    internalSyncInitStateCounter++;
                    break;
                case -2:
                    // waiting for nce to stop
                    if (!waitingForCmdStop) {
                        internalSyncInitStateCounter = 0;
                    }
                    break;
                case 1:
                    // get current values + initialize all values for sync operations
                    priorDiffs.clear();
                    priorCorrections.clear();
                    priorOffsetErrors.clear();
                    syncInterval = TARGET_SYNC_DELAY;
                    // disable NCE clock options
                    setClockButton.setEnabled(false);
                    setRatioButton.setEnabled(false);
                    set1224Button.setEnabled(false);
                    setStopNceButton.setEnabled(false);
                    setStartNceButton.setEnabled(false);
                    // stop NCE clock
                    issueClockStop();
                    internalSyncInitStateCounter++;
                    break;
                case 2:
                    if (!waitingForCmdStop) {
                        internalSyncInitStateCounter++;
                    }
                    break;
                case 3:
                    // set NCE ratio, mode etc...
                    issueClockRatio((int) internalClock.getRate());
                    internalSyncInitStateCounter++;
                    break;
                case 4:
                    if (!waitingForCmdRatio) {
                        internalSyncInitStateCounter++;
                    }
                    break;
                case 5:
                    issueClock1224(true);
                    internalSyncInitStateCounter++;
                    break;
                case 6:
                    if (!waitingForCmd1224) {
                        internalSyncInitStateCounter++;
                    }
                    break;
                case 7:
                    // set initial NCE time
                    // set NCE from internal settings
                    // start NCE clock
                    now = internalClock.getTime();
                    issueClockSet(now.getHours(), now.getMinutes(), now.getSeconds());
                    internalSyncInitStateCounter++;
                    break;
                case 8:
                    if (!waitingForCmdTime) {
                        internalSyncInitStateCounter++;
                    }
                    break;
                case 9:
                    issueClockStart();
                    internalSyncInitStateCounter++;
                    break;
                case 10:
                    if (!waitingForCmdStart) {
                        internalSyncInitStateCounter++;
                    }
                    break;
                case 11:
                    issueReadOnlyRequest();
                    internalSyncInitStateCounter++;
                    break;
                case 12:
                    if (!waitingForCmdRead) {
                        internalSyncInitStateCounter++;
                    }
                    break;
                case 13:
                    updateNceClockDisplay();
                    updateInternalClockDisplay();
                    alarmSyncStart();
                    internalSyncInitStateCounter++;
                    break;
                case 14:
                    // initialization complete
                    internalSyncInitStateCounter = 0;
                    internalSyncRunStateCounter = 1;
                    log.trace("internalSyncState: init done");
                    break;
                default:
                    internalSyncInitStateCounter = 0;
                    log.error("Uninitialized value: internalSyncInitStateCounter");
                    break;
            }
        } while (priorState != internalSyncInitStateCounter);
    }

    @SuppressWarnings("deprecation")
    private void internalSyncRunStates() {
        double intTime;
        double nceTime;
        double diffTime;
        Date now = internalClock.getTime();
        if (internalSyncRunStateCounter != 0) {
            log.trace("internalSyncRunStates: {} @ {}", internalSyncRunStateCounter, now);
        }
        int priorState;
        do {
            priorState = internalSyncRunStateCounter;
            switch (internalSyncRunStateCounter) {
                case -1:
                    // turn off any sync parts
                    internalSyncInitStateCounter = -1;
                    internalSyncInitStates();
                    break;
                case 1:
                    internalClockStatusCheck();
                    // alarm fired, issue fresh nce reads
                    issueReadOnlyRequest();
                    internalSyncRunStateCounter++;
                    break;
                case 2:
                case 6:
                    if (!waitingForCmdRead) {
                        internalSyncRunStateCounter++;
                    }
                    break;
                case 3:
                    // compute error
                    nceTime = getNceTime();
                    intTime = getIntTime();
                    diffTime = intTime - nceTime;
                    if (log.isTraceEnabled()) {
                        log.trace("syncStates2 begin. NCE: {}{}:{}{}:{}{} Internal: {}{}:{}{}:{}{} diff: {}",
                                nceLastHour / 10, nceLastHour - ((nceLastHour / 10) * 10),
                                nceLastMinute / 10, nceLastMinute - ((nceLastMinute / 10) * 10),
                                nceLastSecond / 10, nceLastSecond - ((nceLastSecond / 10) * 10),
                                now.getHours() / 10, now.getHours() - ((now.getHours() / 10) * 10),
                                now.getMinutes() / 10, now.getMinutes() - ((now.getMinutes() / 10) * 10),
                                now.getSeconds() / 10, now.getSeconds() - ((now.getSeconds() / 10) * 10),
                                diffTime);
                    }
                    // save error to array
                    while (priorDiffs.size() >= MAX_ERROR_ARRAY) {
                        priorDiffs.remove(0);
                    }
                    priorDiffs.add(diffTime);
                    recomputeInternalSync();
                    issueClockSet(
                            now.getHours(),
                            now.getMinutes(),
                            (int) syncInterval
                    );
                    internalSyncRunStateCounter++;
                    break;
                case 4:
                    if (!waitingForCmdTime) {
                        internalSyncRunStateCounter++;
                    }
                    break;
                case 5:
                    issueReadOnlyRequest();
                    internalSyncRunStateCounter++;
                    break;
                case 7:
                    // compute offset delay
                    intTime = now.getSeconds();
                    diffTime = TARGET_SYNC_DELAY - intTime;
                    // save offset error to array
                    while (priorOffsetErrors.size() >= MAX_ERROR_ARRAY) {
                        priorOffsetErrors.remove(0);
                    }
                    priorOffsetErrors.add(diffTime);
                    recomputeOffset();
                    if (log.isTraceEnabled()) {
                        log.trace("syncState compute offset. NCE: {}{}:{}{}:{}{} Internal: {}{}:{}{}:{}{}",
                                nceLastHour / 10, nceLastHour - ((nceLastHour / 10) * 10),
                                nceLastMinute / 10, nceLastMinute - ((nceLastMinute / 10) * 10),
                                nceLastSecond / 10, nceLastSecond - ((nceLastSecond / 10) * 10),
                                now.getHours() / 10, now.getHours() - ((now.getHours() / 10) * 10),
                                now.getMinutes() / 10, now.getMinutes() - ((now.getMinutes() / 10) * 10),
                                now.getSeconds() / 10, now.getSeconds() - ((now.getSeconds() / 10) * 10));
                    }
                    internalSyncRunStateCounter = 0;
                    break;
                default:
                    internalSyncRunStateCounter = 0;
                    break;
            }
        } while (priorState != internalSyncRunStateCounter);
    }

    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY", justification = "testing for change from stored value")
    private void internalClockStatusCheck() {
        // if change to internal clock
        if (clockMode == SYNCMODE_INTERNAL_MASTER) {
            if (internalLastRunning != internalClock.getRun()) {
                if (internalClock.getRun()) {
                    internalSyncInitStateCounter = 1;
                } else {
                    internalSyncInitStateCounter = -3;
                }
                internalSyncInitStates();
            }
            // next line is the FE_FLOATING_POINT_EQUALITY annotated above
            if (internalLastRatio != internalClock.getRate()) {
                internalSyncInitStateCounter = 1;
                internalSyncInitStates();
            }
        }
        internalLastRunning = internalClock.getRun();
        internalLastRatio = internalClock.getRate();
    }

    private void changePidValues() {
        double p = 0;
        double i = 0;
        double d = 0;
        boolean ok = true;
        try {
            p = Double.parseDouble(ncePidGainP.getText().trim());
        } catch (NumberFormatException e) {
            log.error("Invalid value: {}", ncePidGainP.getText().trim());
            ok = false;
        }
        try {
            i = Double.parseDouble(ncePidGainI.getText().trim());
        } catch (NumberFormatException e) {
            log.error("Invalid value: {}", ncePidGainP.getText().trim());
            ok = false;
        }
        try {
            d = Double.parseDouble(ncePidGainD.getText().trim());
        } catch (NumberFormatException e) {
            log.error("Invalid value: {}", ncePidGainP.getText().trim());
            ok = false;
        }
        if (ok) {
            if (p < 0) {
                p = 0;
            }
            if (p > 1) {
                p = 1;
            }
            if (i < 0) {
                i = 0;
            }
            if (d > 1) {
                d = 1;
            }
            ncePidGainPv = p;
            ncePidGainIv = i;
            ncePidGainDv = d;
            ncePidGainP.setText(fiveDigits.format(p));
            ncePidGainI.setText(fiveDigits.format(i));
            ncePidGainD.setText(fiveDigits.format(d));
        }
    }

    private void recomputeOffset() {

        double sumDiff = 0;
        if (priorOffsetErrors.size() > 1) {
            sumDiff = priorOffsetErrors.get(0) + priorOffsetErrors.get(1);
        }
        double avgDiff = sumDiff / 2;
        syncInterval = syncInterval + avgDiff;
        if (syncInterval < 30) {
            syncInterval = 30;
        }
        if (syncInterval > 58) {
            syncInterval = 58;
        }
        if (log.isTraceEnabled()) {
            Date now = internalClock.getTime();
            StringBuilder txt = new StringBuilder("");
            for (int i = 0; i < priorOffsetErrors.size(); i++) {
                txt.append(" ").append(priorOffsetErrors.get(i).doubleValue());
            }
            log.trace("priorOffsetErrors: {}", txt);
            log.trace("syncOffset: {} avgDiff: {} @ {}", syncInterval, avgDiff, now);
        }
    }

    private void recomputeInternalSync() {
        //Date now = internalClock.getTime();
        double sumDiff = 0;
        double currError = 0;
        //double diffError = 0;
        //double avgDiff = 0;
        if (priorDiffs.size() > 0) {
            currError = priorDiffs.get(priorDiffs.size() - 1);
            //diffError = priorDiffs.get(priorDiffs.size() - 1).doubleValue() - ((Double) priorDiffs.get(0)).doubleValue();
        }
        for (int i = 0; i < priorDiffs.size(); i++) {
            sumDiff = sumDiff + priorDiffs.get(i);
        }
        double corrDiff = 0;
        if (priorCorrections.size() > 0) {
            corrDiff = priorCorrections.get(priorCorrections.size() - 1) - priorCorrections.get(0);
        }
        double pCorr = currError * intPidGainPv;
        double iCorr = sumDiff * intPidGainIv;
        double dCorr = corrDiff * intPidGainDv;
        double newRateAdj = pCorr + iCorr + dCorr;
        // save correction to array
        while (priorCorrections.size() >= MAX_ERROR_ARRAY) {
            priorCorrections.remove(0);
        }
        priorCorrections.add(newRateAdj);
        syncInterval = syncInterval + newRateAdj;
        if (syncInterval > 57) {
            syncInterval = 57;
        }
        if (syncInterval < 40) {
            syncInterval = 40;
        }
        if (log.isTraceEnabled()) {
            StringBuilder txt = new StringBuilder("");
            for (int i = 0; i < priorDiffs.size(); i++) {
                txt.append(" ").append(priorDiffs.get(i));
            }
            log.trace("priorDiffs: {}", txt);
            log.trace("syncInterval: {} pCorr: {} iCorr: {} dCorr: {}",
                    syncInterval, fiveDigits.format(pCorr), fiveDigits.format(iCorr), fiveDigits.format(dCorr));
        }
    }

    private void recomputeNceSync() {
        //Date now = internalClock.getTime();
        double sumDiff = 0;
        double currError = 0;
        double diffError = 0;
        if (priorDiffs.size() > 0) {
            currError = priorDiffs.get(priorDiffs.size() - 1);
            diffError = priorDiffs.get(priorDiffs.size() - 1) - priorDiffs.get(0);
        }
        for (int i = 0; i < priorDiffs.size(); i++) {
            sumDiff = sumDiff + priorDiffs.get(i);
        }
        double corrDiff = 0;
        if (priorCorrections.size() > 0) {
            corrDiff = priorCorrections.get(priorCorrections.size() - 1) - priorCorrections.get(0);
        }
        double pCorr = currError * ncePidGainPv;
        double iCorr = diffError * ncePidGainIv;
        double dCorr = corrDiff * ncePidGainDv;
        double newRateAdj = pCorr + iCorr + dCorr;
        //  if (newRateAdj > 0.5) {
        // newRateAdj = 0.5;
        //}
        //if (newRateAdj < -0.5) {
        //  newRateAdj = -0.5;
        //  }
        // save correction to array
        while (priorCorrections.size() >= MAX_ERROR_ARRAY) {
            priorCorrections.remove(0);
        }
        priorCorrections.add(newRateAdj);
        double oldInternalRate = internalClock.getRate();
        double newInternalRate = oldInternalRate + newRateAdj;
        if (Math.abs(currError) > 60) {
            // don't try to drift, just reset
            nceSyncInitStateCounter = 1;
            nceSyncInitStates();
        } else if (Math.abs(oldInternalRate - newInternalRate) >= rateChgMinimum) {
            try {
                internalClock.setRate(newInternalRate);
                if (log.isDebugEnabled()) {
                    log.debug("changing internal rate: " + newInternalRate);
                }
            } catch (TimebaseRateException e) {
                log.error("recomputeNceSync: Failed setting new internal rate: {}", newInternalRate);
                // just set the internal to NCE and set the clock
                nceSyncInitStateCounter = 1;
                nceSyncInitStates();
            }
        }
        if (log.isTraceEnabled()) {
            StringBuilder txt = new StringBuilder("");
            for (int i = priorDiffs.size() - 1; i >= 0; i--) {
                txt.append(" ").append(threeDigits.format(priorDiffs.get(i)));
            }
            log.trace("priorDiffs: {}", txt);
            txt = new StringBuilder("");
            for (int i = priorCorrections.size() - 1; i >= 0; i--) {
                txt.append(" ").append(threeDigits.format(priorCorrections.get(i)));
            }
            log.trace("priorCorrections: {}", txt);
            log.trace("currError: {} pCorr: {} iCorr: {} dCorr: {} newInternalRate: {}",
                    fiveDigits.format(currError), fiveDigits.format(pCorr), fiveDigits.format(iCorr), fiveDigits.format(dCorr), threeDigits.format(newInternalRate));
        }
    }

    private void changePollingSpeed(double newInterval) {
        if (newInterval < MIN_POLLING_INTERVAL || newInterval > MAX_POLLING_INTERVAL) {
            log.error("reseting pollingInterval, invalid value:{}", newInterval);
        } else {
            pollingInterval = newInterval;
            pollingSpeed.setText("" + pollingInterval);
            if (timerDisplayUpdate == null) {
                alarmSetup();
            }
            timerDisplayUpdate.setDelay((int) (pollingInterval * 1000));
        }
    }

    private void changeSyncMode() {
        int oldMode = clockMode;
        int newMode = SYNCMODE_OFF;
        if (setSyncModeNceMaster.isSelected() == true) {
            newMode = SYNCMODE_NCE_MASTER;
        }
        if (setSyncModeInternalMaster.isSelected() == true) {
            newMode = SYNCMODE_INTERNAL_MASTER;
        }
        if (internalClock != null) {
            log.debug("changeSyncMode(): New Mode: {} Old Mode: {}", newMode, oldMode);
            if (oldMode != newMode) {
                clockMode = SYNCMODE_OFF;
                // some change so, change settings
                if (oldMode == SYNCMODE_OFF) {
                    if (newMode == SYNCMODE_INTERNAL_MASTER) {
                        log.debug("starting Internal mode");
                        internalSyncInitStateCounter = 1;
                        internalSyncRunStateCounter = 0;
                        internalSyncInitStates();
                        clockMode = SYNCMODE_INTERNAL_MASTER;
                    }
                    if (newMode == SYNCMODE_NCE_MASTER) {
                        log.debug("starting NCE mode");
                        nceSyncInitStateCounter = 1;
                        nceSyncRunStateCounter = 0;
                        nceSyncInitStates();
                        clockMode = SYNCMODE_NCE_MASTER;
                    }
                } else {
                    if (oldMode == SYNCMODE_NCE_MASTER) {
                        // clear nce sync
                        nceSyncInitStateCounter = -1;
                        nceSyncInitStates();
                        internalSyncInitStateCounter = 1;
                        internalSyncInitStates();
                    }
                    if (oldMode == SYNCMODE_INTERNAL_MASTER) {
                        // clear internal mode
                        internalSyncInitStateCounter = -1;
                        internalSyncInitStates();
                        nceSyncInitStateCounter = 1;
                        nceSyncInitStates();
                    }
                }
            }
        }
    }

    private void nceSyncInitStates() {
        int priorState;
        do {
            if (log.isTraceEnabled()) {
                log.trace("Before nceSyncInitStateCounter: {} {}", nceSyncInitStateCounter, internalClock.getTime());
            }
            priorState = nceSyncInitStateCounter;
            switch (nceSyncInitStateCounter) {
                case -1:
                    // turn all this off
                    if (alarmSyncUpdate != null) {
                        alarmSyncUpdate.stop();
                    }
                    // clear any old records
                    priorDiffs.clear();
                    priorCorrections.clear();
                    nceSyncInitStateCounter = 0;
                    nceSyncRunStateCounter = 0;
                    break;
                case 0:
                    // idle state
                    break;
                case 1:
                    // first init step
                    log.debug("Init/Reset NCE Clock Sync");
                    // make sure other state is off
                    nceSyncRunStateCounter = 0;
                    // stop internal clock
                    internalClock.setRun(false);
                    if (alarmSyncUpdate != null) {
                        alarmSyncUpdate.stop();
                    }
                    // clear any old records
                    priorDiffs.clear();
                    priorCorrections.clear();
                    // request all current nce values
                    issueReadOnlyRequest();
                    nceSyncInitStateCounter++;
                    break;
                case 2:
                    // make sure the read only has happened
                    if (!waitingForCmdRead) {
                        nceSyncInitStateCounter++;
                    }
                    break;
                case 3:
                    // set ratio, modes etc...
                    try {
                        internalClock.setRate(nceLastRatio * ESTIMATED_NCE_RATE_FACTOR);
                    } catch (TimebaseRateException e) {
                        log.error("nceSyncInitStates: failed to set internal clock rate: {}", nceLastRatio);
                    }
                    // get time from NCE settings and set internal clock
                    setInternalClockFromNce();
                    internalClock.setRun(true);
                    nceSyncInitStateCounter = 0; // init is done
                    nceSyncRunStateCounter = 1;
                    nceSyncRunStates();
                    alarmSyncStart();
                    updateNceClockDisplay();
                    updateInternalClockDisplay();
                    break;
                default:
                    log.warn("Unexpected nceSyncInitStateCounter {} in nceSyncInitStates", nceSyncInitStateCounter);
                    break;
            }
            log.trace("After nceSyncInitStateCounter: {} {}", nceSyncInitStateCounter, internalClock.getTime());
        } while (priorState != nceSyncInitStateCounter);
    }

    private void nceSyncRunStates() {
        double intTime;
        double nceTime;
        double diffTime;
        if (log.isTraceEnabled()) {
            log.trace("Before nceSyncRunStateCounter: {} {}", nceSyncRunStateCounter, internalClock.getTime());
        }
        int priorState;
        do {
            priorState = nceSyncRunStateCounter;
            switch (nceSyncRunStateCounter) {
                case 1: // issue read for nce time
                    issueReadOnlyRequest();
                    nceSyncRunStateCounter++;
                    break;
                case 2:
                    // did read happen??
                    if (!waitingForCmdRead) {
                        nceSyncRunStateCounter++;
                    }
                    break;
                case 3: // compare internal with nce time
                    intTime = getIntTime();
                    nceTime = getNceTime();
                    diffTime = nceTime - intTime;
                    // deal with end of day reset
                    if (diffTime > MAX_SECONDS_IN_DAY / 2) {
                        diffTime = MAX_SECONDS_IN_DAY + nceTime - intTime;
                    } else if (diffTime < MAX_SECONDS_IN_DAY / -2) {
                        diffTime = nceTime;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("new diffTime: " + diffTime + " = " + nceTime + " - " + intTime);
                    }
                    // save error to array
                    while (priorDiffs.size() >= MAX_ERROR_ARRAY) {
                        priorDiffs.remove(0);
                    }
                    priorDiffs.add(diffTime);
                    recomputeNceSync();
                    // initialize things if not running
                    if (alarmSyncUpdate == null) {
                        alarmSyncInit();
                    }
                    updateNceClockDisplay();
                    updateInternalClockDisplay();
                    nceSyncRunStateCounter++;
                    break;
                case 4:
                    // wait for next minute
                    nceSyncRunStateCounter = 0;
                    break;
                default:
                    log.warn("Unexpected state {} in nceSyncRunStates", nceSyncRunStateCounter);
                    break;
            }
        } while (priorState != nceSyncRunStateCounter);
        if (log.isTraceEnabled()) {
            log.trace("After nceSyncRunStateCounter: {} {}", nceSyncRunStateCounter, internalClock.getTime());
        }
    }

    private void setInternalClockFromNce() {
        Date newTime = getNceDate();
        internalClock.setTime(newTime);
        log.debug("setInternalClockFromNce nceClock: {}", newTime);
    }

    private void updateSettingsFromNce() {
        if (updateTimeFromRead == true) {
            hours.setText("" + (nceLastHour / 10) + (nceLastHour - ((nceLastHour / 10) * 10)));
            minutes.setText("" + (nceLastMinute / 10) + (nceLastMinute - ((nceLastMinute / 10) * 10)));
            seconds.setText("" + (nceLastSecond / 10) + (nceLastSecond - ((nceLastSecond / 10) * 10)));
            if (nceLast1224) {
                twentyFour.setSelected(true);
                amPm.setText(" ");
            } else {
                twentyFour.setSelected(false);
                if (nceLastAmPm) {
                    amPm.setText(Bundle.getMessage("TagAm"));
                } else {
                    amPm.setText(Bundle.getMessage("TagPm"));
                }
            }
            updateTimeFromRead = false;
        }
        if (updateRatioFromRead == true) {
            rateNce.setText("" + nceLastRatio);
            updateRatioFromRead = false;
        }
        if (updateFormatFromRead == true) {
            if (nceLast1224) {
                twentyFour.setSelected(true);
            } else {
                twentyFour.setSelected(false);
            }
            updateFormatFromRead = false;
        }
        if (updateStatusFromRead == true) {
            if (nceLastRunning) {
                status.setText(Bundle.getMessage("TagRunning"));
            } else {
                status.setText(Bundle.getMessage("TagStopped"));
            }
        }
    }

    private void updateNceClockDisplay() {
        String txt = nceLastRunning ? Bundle.getMessage("TagRunning") : Bundle.getMessage("TagStopped");
        txt = txt + " "
                + (nceLastHour / 10) + (nceLastHour - ((nceLastHour / 10) * 10)) + Bundle.getMessage("LabelTimeSep")
                + (nceLastMinute / 10) + (nceLastMinute - ((nceLastMinute / 10) * 10)) + Bundle.getMessage("LabelTimeSep")
                + (nceLastSecond / 10) + (nceLastSecond - ((nceLastSecond / 10) * 10));
        if (!nceLast1224) {
            if (nceLastAmPm) {
                txt = txt + " " + Bundle.getMessage("TagAm");
            } else {
                txt = txt + " " + Bundle.getMessage("TagPm");
            }
        }
        txt = txt + " " + Bundle.getMessage("LabelRatio") + " "
                + nceLastRatio + Bundle.getMessage("LabelToOne");
        if (clockMode == SYNCMODE_NCE_MASTER) {
            txt = txt + " " + Bundle.getMessage("TagIsNceMaster");
            double intTime = getIntTime();
            double nceTime = getNceTime();
            double diffTime = nceTime - intTime;
            txt = txt + " " + Bundle.getMessage("ClockError");
            txt = txt + " " + threeDigits.format(diffTime);
            log.trace("intTime: {} nceTime: {} diffTime: {}", intTime, nceTime, diffTime);
        }
        nceDisplayStatus.setText(txt);
    }

    @SuppressWarnings("deprecation")
    private void updateInternalClockDisplay() {
        String txt = internalClock.getRun() ? Bundle.getMessage("TagRunning") : Bundle.getMessage("TagStopped");
        Date now = internalClock.getTime();
        txt = txt + " "
                + (now.getHours() / 10) + (now.getHours() - ((now.getHours() / 10) * 10))
                + Bundle.getMessage("LabelTimeSep")
                + (now.getMinutes() / 10) + (now.getMinutes() - ((now.getMinutes() / 10) * 10))
                + Bundle.getMessage("LabelTimeSep")
                + (now.getSeconds() / 10) + (now.getSeconds() - ((now.getSeconds() / 10) * 10));
        txt = txt + " "
                + Bundle.getMessage("LabelRatio") + " "
                + threeDigits.format(internalClock.getRate()) + Bundle.getMessage("LabelToOne");
        if (clockMode == SYNCMODE_INTERNAL_MASTER) {
            txt = txt + " " + Bundle.getMessage("TagIsInternalMaster");
            double intTime = getIntTime();
            double nceTime = getNceTime();
            double diffTime = nceTime - intTime;
            txt = txt + " " + Bundle.getMessage("ClockError");
            txt = txt + " " + threeDigits.format(diffTime);
        }
        internalDisplayStatus.setText(txt);
    }

    private void issueReadOnlyRequest() {
        if (!waitingForCmdRead) {
            byte[] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
            NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CS_CLOCK_MEM_SIZE);
            waiting++;
            waitingForCmdRead = true;
            tc.sendNceMessage(cmdNce, this);
            //   log.debug("issueReadOnlyRequest at " + internalClock.getTime());
        }
    }

    private void issueReadAllRequest() {
        if (!waitingForCmdRead) {
            byte[] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
            NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CS_CLOCK_MEM_SIZE);
            waiting++;
            waitingForCmdRead = true;
            tc.sendNceMessage(cmdNce, this);
        }
        updateTimeFromRead = true;
        updateRatioFromRead = true;
        updateFormatFromRead = true;
        updateStatusFromRead = true;
    }

    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification="was previously marked with @SuppressWarnings, reason unknown")
    private void issueReadTimeRequest() {
        if (!waitingForCmdRead) {
            byte[] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
            NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CS_CLOCK_MEM_SIZE);
            waiting++;
            waitingForCmdRead = true;
            tc.sendNceMessage(cmdNce, this);
        }
        updateTimeFromRead = true;
    }

    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification="was previously marked with @SuppressWarnings, reason unknown")
    private void issueReadRatioRequest() {
        if (!waitingForCmdRead) {
            byte[] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
            NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CS_CLOCK_MEM_SIZE);
            waiting++;
            waitingForCmdRead = true;
            tc.sendNceMessage(cmdNce, this);
        }
        updateRatioFromRead = true;
    }

    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification="was previously marked with @SuppressWarnings, reason unknown")
    private void issueReadFormatRequest() {
        if (!waitingForCmdRead) {
            byte[] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
            NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CS_CLOCK_MEM_SIZE);
            waiting++;
            waitingForCmdRead = true;
            tc.sendNceMessage(cmdNce, this);
        }
        updateFormatFromRead = true;
    }

    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification="was previously marked with @SuppressWarnings, reason unknown")
    private void issueReadStatusRequest() {
        if (!waitingForCmdRead) {
            byte[] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
            NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CS_CLOCK_MEM_SIZE);
            waiting++;
            waitingForCmdRead = true;
            tc.sendNceMessage(cmdNce, this);
        }
        updateStatusFromRead = true;
    }

    private void issueClockSet(int hh, int mm, int ss) {
        issueClockSetMem(hh, mm, ss);
    }

    private void issueClockSetMem(int hh, int mm, int ss) {
        byte[] b = new byte[3];
        b[0] = (byte) ss;
        b[1] = (byte) mm;
        b[2] = (byte) hh;
        byte[] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryWriteN(CS_CLOCK_MEM_ADDR + CS_CLOCK_SECONDS, b);
        NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CMD_MEM_SET_REPLY_SIZE);
        waiting++;
        waitingForCmdTime = true;
        tc.sendNceMessage(cmdNce, this);
    }

    private void issueClockRatio(int r) {
        byte[] cmd = jmri.jmrix.nce.NceBinaryCommand.accSetClockRatio(r);
        NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CMD_CLOCK_SET_REPLY_SIZE);
        waiting++;
        waitingForCmdRatio = true;
        tc.sendNceMessage(cmdNce, this);
    }

    private void issueClock1224(boolean mode) {
        byte[] cmd = jmri.jmrix.nce.NceBinaryCommand.accSetClock1224(mode);
        NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CMD_CLOCK_SET_REPLY_SIZE);
        waiting++;
        waitingForCmd1224 = true;
        tc.sendNceMessage(cmdNce, this);
    }

    private void issueClockStop() {
        byte[] cmd = jmri.jmrix.nce.NceBinaryCommand.accStopClock();
        NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CMD_CLOCK_SET_REPLY_SIZE);
        waiting++;
        waitingForCmdStop = true;
        tc.sendNceMessage(cmdNce, this);
    }

    private void issueClockStart() {
        byte[] cmd = jmri.jmrix.nce.NceBinaryCommand.accStartClock();
        NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CMD_CLOCK_SET_REPLY_SIZE);
        waiting++;
        waitingForCmdStart = true;
        tc.sendNceMessage(cmdNce, this);
    }

    /**
     * Handles minute notifications for NCE Clock Monitor/Synchronizer
     */
    public void newInternalMinute() {
        //   if (log.isDebugEnabled()) {
        // log.debug("newInternalMinute clockMode: " + clockMode + " nceInit: " + nceSyncInitStateCounter + " nceRun: " + nceSyncRunStateCounter);
        //}
        //NCE clock is running
        if (lastClockReadPacket != null && lastClockReadPacket.getElement(CS_CLOCK_STATUS) == 0) {
            if (clockMode == SYNCMODE_INTERNAL_MASTER) {
                // start alarm timer
                alarmSyncStart();
            }
        }
    }

    // handle window closing event
    public void windowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        if (timerDisplayUpdate != null) {
            timerDisplayUpdate.stop();
        }
        //super.windowClosing(e);
    }

    @Override
    public void dispose() {
        // stop alarm
        if (timerDisplayUpdate != null) {
            timerDisplayUpdate.stop();
            timerDisplayUpdate = null;
        }
        // Remove ourselves from the Timebase minute rollover event
        InstanceManager.getDefault(jmri.Timebase.class).removeMinuteChangeListener(minuteChangeListener);
        minuteChangeListener = null;

        // take apart the JFrame
        super.dispose();
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.nce.swing.NceNamedPaneAction {

        public Default() {
            super("Open NCE Clock Monitor",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    ClockMonPanel.class.getName(),
                    jmri.InstanceManager.getDefault(NceSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ClockMonPanel.class);

}
