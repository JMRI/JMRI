// ClockMonFrame.java

package jmri.jmrix.nce.clockmon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.Timebase;
import jmri.TimebaseRateException;
import jmri.jmrix.nce.NceListener;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;
import jmri.jmrix.nce.swing.NcePanelInterface;

import java.util.Date;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.text.DecimalFormat;
import java.awt.event.*;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.*;

/**
 * Frame displaying and programming a NCE clock monitor.
 *
 * Some of the message formats used in this class are Copyright NCE Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact NCE Inc for separate permission.
 *
 * @author			Ken Cameron   Copyright (C) 2007
 * @version			$Revision$
 *
 * derived from loconet.clockmonframe by Bob Jacobson Copyright (C) 2003
 * 
 * Notes:
 * 
 * 1. the commands for time don't include seconds so I had to use memory write
 * 	 to sync nce clock.
 * 
 * 2. I tried fiddling with the internal nce clock loop values, didn't work.
 * 
 * 3. to sync nce to internal clock:
 * 	A. set an alarm about 5 seconds before next minute
 *  B. read nce clock
 *  C. compute error and record last X errors for correction calc
 *  D. adjust nce clock as needed
 *  E. reset alarm after next internal minute ticks
 *  
 * 4. to sync internal to nce clock
 *  A. every so often, read nce clock and compare to internal
 *  B. compute error and record last X errors for correction calc
 *  C. adjust internal clock rate factor as needed
 *  
 * 5. The clock message only seems to go out to the throttles on the tic of the minute.
 * 6. The nce clock must be left running, or it doesn't tic and therefore doesn't go out the bus.
 *  
 */
public class ClockMonPanel extends jmri.jmrix.nce.swing.NcePanel implements NcePanelInterface, NceListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = -388876083150227345L;

	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.nce.clockmon.ClockMonBundle");
    
	private boolean extraDebug = false;
    public static final int CS_CLOCK_MEM_ADDR = 0xDC00;
    public static final int CS_CLOCK_MEM_SIZE = 0x10;
    public static final int CS_CLOCK_SCALE = 0x00;
    public static final int	CS_CLOCK_TICK = 0x01;
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
    public static final int SYNCMODE_OFF = 0;				//0 - clocks independent
    public static final int SYNCMODE_NCE_MASTER = 1;		//1 - NCE sets Internal
    public static final int SYNCMODE_INTERNAL_MASTER = 2;	//2 - Internal sets NCE
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
    private int	nceLastHour;
    private int nceLastMinute;
    private int nceLastSecond;
    private int nceLastRatio;
    private boolean nceLastAmPm;
    private boolean nceLast1224;
    private boolean nceLastRunning;
    private double internalLastRatio;
    private boolean internalLastRunning;
    private double pollingInterval = DEFAULT_POLLING_INTERVAL;
    private ArrayList<Double> priorDiffs = new ArrayList<Double>();
    private ArrayList<Double> priorOffsetErrors = new ArrayList<Double>();
    private ArrayList<Double> priorCorrections = new ArrayList<Double>();
    private double syncInterval = TARGET_SYNC_DELAY;
    private int internalSyncInitStateCounter = 0;
    private int internalSyncRunStateCounter = 0;
    private double ncePidGainPv = 0.04;
    private double ncePidGainIv = 0.01;
    private double ncePidGainDv = 0.005;
    private double intPidGainPv = 0.02;
    private double intPidGainIv = 0.001;
    private double intPidGainDv = 0.01;
    
    private double rateChgMinimum = 0.001;
    
    private int nceSyncInitStateCounter = 0;	// NCE master sync initialzation state machine
    private int	nceSyncRunStateCounter = 0;	// NCE master sync runtime state machine
    private int	alarmDisplayStateCounter = 0;	// manages the display update from the alarm
    
    Timebase internalClock ;
    javax.swing.Timer timerDisplayUpdate = null;
    javax.swing.Timer alarmSyncUpdate = null;
    
    JTextField hours = new JTextField("  00");
    JTextField minutes = new JTextField("  00");
    JTextField seconds = new JTextField("  00");
    
    JTextField rateNce = new JTextField("   1");
    JTextField amPm = new JTextField(2);
    JCheckBox twentyFour = new JCheckBox(rb.getString("CheckBox24HourFormat"));
    JTextField status = new JTextField(10);
    
    JRadioButton setSyncModeNceMaster = new JRadioButton(rb.getString("ClockModeNCE"));
    JRadioButton setSyncModeInternalMaster = new JRadioButton(rb.getString("ClockModeInternal"));
    JRadioButton setSyncModeOff = new JRadioButton(rb.getString("ClockModeIndependent"));
    
    JTextField internalDisplayStatus = new JTextField(60);
    
    JTextField nceDisplayStatus = new JTextField(60);
    
    JTextField pollingSpeed = new JTextField(5);
    
    JTextField ncePidGainP = new JTextField(7);
    JTextField ncePidGainI = new JTextField(7);
    JTextField ncePidGainD = new JTextField(7);
    JTextField intPidGainP = new JTextField(7);
    JTextField intPidGainI = new JTextField(7);
    JTextField intPidGainD = new JTextField(7);
    
    transient java.beans.PropertyChangeListener minuteChangeListener ;
    
    JButton setSyncButton = new JButton(rb.getString("SetSyncMode"));
    JButton setClockButton = new JButton(rb.getString("SetHoursMinutes"));
    JButton setRatioButton = new JButton(rb.getString("SetRatio"));
    JButton set1224Button = new JButton(rb.getString("Set12/24Mode"));
    JButton setStopNceButton = new JButton(rb.getString("StopNceClock"));
    JButton setStartNceButton = new JButton(rb.getString("StartNceClock"));
    JButton readButton = new JButton(rb.getString("ReadAll"));
    JButton setPollingSpeedButton = new JButton(rb.getString("SetInterfaceUpdRate"));
    JButton setPidButton = new JButton(rb.getString("SetPid"));
    
    private NceTrafficController tc = null;
    
    public ClockMonPanel() {
    	super();
    }
    
    public void initContext(Object context) throws Exception {
        if (context instanceof NceSystemConnectionMemo ) {
            try {
				initComponents((NceSystemConnectionMemo) context);
			} catch (Exception e) {
				log.error("NceClockMon initContext failed");
			}
        }
    }

    public String getHelpTarget() { return "package.jmri.jmrix.nce.clockmon.ClockMonFrame"; }
    public String getTitle() { 
    	StringBuilder x = new StringBuilder();
    	if (memo != null) {
    		x.append(memo.getUserName());
    	} else {
    		x.append("NCE_");
    	}
		x.append(": ");
    	x.append(rb.getString("TitleNceClockMonitor"));
        return x.toString(); 
    }
    
    public void initComponents(NceSystemConnectionMemo m) throws Exception {
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
                                                                                 rb.getString("InternalClockStatusBorderText"));
        pane2.setBorder(pane2Titled);
        pane2.add(internalDisplayStatus);
        internalDisplayStatus.setEditable(false);
        internalDisplayStatus.setBorder(BorderFactory.createEmptyBorder());
        add(pane2);
        
        // NCE Clock Info Panel
        pane2 = new JPanel();
        pane2Border = BorderFactory.createEtchedBorder();
        pane2Titled = BorderFactory.createTitledBorder(pane2Border,
                                                       rb.getString("NceClockStatusBorderText"));
        pane2.setBorder(pane2Titled);
        pane2.add(nceDisplayStatus);
        nceDisplayStatus.setEditable(false);
        nceDisplayStatus.setBorder(BorderFactory.createEmptyBorder());
        add(pane2);
        
        // setting time items
        pane2 = new JPanel();
        pane2Border = BorderFactory.createEtchedBorder();
        pane2Titled = BorderFactory.createTitledBorder(pane2Border,
                                                       rb.getString("SetClockValuesBorderText"));
        pane2.setBorder(pane2Titled);
        pane2.add(new JLabel(rb.getString("LabelTime")));
        pane2.add(hours);
        pane2.add(new JLabel(rb.getString("LabelTimeSep")));
        pane2.add(minutes);
        pane2.add(new JLabel(rb.getString("LabelTimeSep")));
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
                                                       rb.getString("SetClockRatioBorderText"));
        pane2.setBorder(pane2Titled);
        pane2.add(new JLabel(rb.getString("LabelRatio")));
        pane2.add(rateNce);
        pane2.add(new JLabel(rb.getString("LabelToOne")));
        pane2.add(setRatioButton);
        add(pane2);
        
        // add 12/24 clock options
        pane2 = new JPanel();
        pane2Border = BorderFactory.createEtchedBorder();
        pane2Titled = BorderFactory.createTitledBorder(pane2Border,
                                                       rb.getString("SetClock12/24ModeBorderText"));
        pane2.setBorder(pane2Titled);
        pane2.add(twentyFour);
        pane2.add(new JLabel(" "));
        pane2.add(set1224Button);
        add(pane2);
        
        //		pane2 = new JPanel();
        //		pane2.setLayout(new BoxLayout(pane2, BoxLayout.X_AXIS));
        //		pane2.add(new JLabel(" "));
        //		pane2.add(status);
        //		add(pane2);
        
        pane2 = new JPanel();
        pane2Border = BorderFactory.createEtchedBorder();
        pane2Titled = BorderFactory.createTitledBorder(pane2Border,
                                                       rb.getString("InterfaceCommandBorderText"));
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
        if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {	// needs memory commands to sync
            setSyncModeInternalMaster.setEnabled(false);
            setSyncModeNceMaster.setEnabled(false);
        }
        add(pane2);
        
        // add polling speed
        pane2 = new JPanel();
        pane2Border = BorderFactory.createEtchedBorder();
        pane2Titled = BorderFactory.createTitledBorder(pane2Border,
                                                       rb.getString("InterfaceUpdRateBorderText"));
        pane2.setBorder(pane2Titled);
        pane2.add(new JLabel(rb.getString("InterfaceUpdRate")));
        pane2.add(new JLabel(" "));
        pane2.add(pollingSpeed);
        pollingSpeed.setText("" + pollingInterval);
        pane2.add(new JLabel(" "));
        pane2.add(new JLabel(rb.getString("InterfaceUpdRateSufix")));
        pane2.add(new JLabel(" "));
        pane2.add(setPollingSpeedButton);
        add(pane2);
        
//        // add PID values
//        gLayout = new GridBagLayout();
//        gConstraints = new GridBagConstraints();
//        pane2 = new JPanel();
//        pane2Border = BorderFactory.createEtchedBorder();
//        pane2Titled = BorderFactory.createTitledBorder(pane2Border,
//                                                       rb.getString("InterfacePidBorderText"));
//        pane2.setBorder(pane2Titled);
//        pane2.setLayout(gLayout);
//        gConstraints.gridx = 0;
//        gConstraints.gridy = 0;
//        gConstraints.gridwidth = 1;
//        gConstraints.gridheight = 1;
//        gConstraints.ipadx = 10;
//        gConstraints.ipady = 1;
//        gConstraints.insets = new Insets(3, 3, 3, 3);
//        pane2.add(new JLabel(rb.getString("InterfacePidNce")), gConstraints);
//        gConstraints.gridx++;
//        pane2.add(new JLabel(rb.getString("InterfacePidGainP")), gConstraints);
//        gConstraints.gridx++;
//        pane2.add(ncePidGainP, gConstraints);
//        gConstraints.gridx++;
//        pane2.add(new JLabel(rb.getString("InterfacePidGainI")), gConstraints);
//        gConstraints.gridx++;
//        pane2.add(ncePidGainI, gConstraints);
//        gConstraints.gridx++;
//        pane2.add(new JLabel(rb.getString("InterfacePidGainD")), gConstraints);
//        gConstraints.gridx++;
//        pane2.add(ncePidGainD, gConstraints);
//        gConstraints.gridx++;
//        gConstraints.gridheight = 2;
//        pane2.add(setPidButton, gConstraints);
//        gConstraints.gridheight = 0;
//        gConstraints.gridx = 0;
//        gConstraints.gridy = 1;
//        pane2.add(new JLabel(rb.getString("InterfacePidInt")), gConstraints);
//        gConstraints.gridx++;
//        pane2.add(new JLabel(rb.getString("InterfacePidGainP")), gConstraints);
//        gConstraints.gridx++;
//        pane2.add(intPidGainP, gConstraints);
//        gConstraints.gridx++;
//        pane2.add(new JLabel(rb.getString("InterfacePidGainI")), gConstraints);
//        gConstraints.gridx++;
//        pane2.add(intPidGainI, gConstraints);
//        gConstraints.gridx++;
//        pane2.add(new JLabel(rb.getString("InterfacePidGainD")), gConstraints);
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
        readButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    issueReadAllRequest();
                }
            }
		);
        // install "set" button handler
        setClockButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    issueClockSet(Integer.parseInt(hours.getText().trim()),
                                  Integer.parseInt(minutes.getText().trim()),
                                  Integer.parseInt(seconds.getText().trim())
                              );
                }
            }
		);
        // install "stop" clock button handler
        setStopNceButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    issueClockStop();
                }
            }
		);
        // install "start" clock button handler
        setStartNceButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    issueClockStart();
                }
            }
		);
        // install set fast clock ratio
        setRatioButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    changeNceClockRatio();
                }
            }
		);
        // install set 12/24 button
        set1224Button.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    issueClock1224(twentyFour.isSelected());
                }
            }
		);
        // install Sync Change Clock button
        setSyncButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    changeSyncMode();
                }
            }
        );
        
        // install "setPolling" button handler
        setPollingSpeedButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                changePollingSpeed(Double.parseDouble(pollingSpeed.getText().trim()));
                }
            }
		);
        
        // install "setPid" button handler
        setPidButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    changePidValues();
                }
            }
                                        );
        
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
        
        // Create a Timebase listener for the Minute change events
        internalClock = InstanceManager.timebaseInstance();
        if (internalClock == null){
            log.error("No Timebase Instance");
        }
        minuteChangeListener = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                newInternalMinute();
            }
        };
        if (minuteChangeListener == null){
            log.error("No minuteChangeListener");
        }
        internalClock.addMinuteChangeListener(minuteChangeListener);
                
        // start display alarm timer
        alarmDisplayUpdateHandler();
    }
    
    //  ignore replies
    public void  message(NceMessage m) {
        log.error("clockmon message received: " + m);
    }  
    
	public void reply(NceReply r) {
    	if (log.isDebugEnabled() && extraDebug){
            log.debug("nceReplyCatcher() waiting: " + waiting +
        		" watingForRead: " + waitingForCmdRead +
        		" waitingForCmdTime: " + waitingForCmdTime +
        		" waitingForCmd1224: " + waitingForCmd1224 +
        		" waitingForCmdRatio: " + waitingForCmdRatio +
        		" waitingForCmdStop: " + waitingForCmdStop +
        		" waitingForCmdStart: " + waitingForCmdStart
        	);
    		
    	}
        if (waiting <= 0) {
        	if (log.isDebugEnabled()) {
        		log.debug(rb.getString("LogReplyUnexpected"));
        	}
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
                log.error(rb.getString("LogNceClockReplySizeError") + r.getNumDataElements());
                return;
            } else {
                waitingForCmdTime = false;
                if (r.getElement(0) != '!') {
                    log.error("NCE set clock replied: " + r.getElement(0));
                }
                callStateMachines();
                return;
            }
        }
        if (r.getNumDataElements() != CMD_CLOCK_SET_REPLY_SIZE) {
            log.error(rb.getString("LogNceClockReplySizeError") + r.getNumDataElements());
            return;
        } else {
            if (waitingForCmd1224) {
                waitingForCmd1224 = false;
                if (r.getElement(0) != '!') {
                    log.error(rb.getString("LogNceClock1224CmdError") + r.getElement(0));
                }
                callStateMachines();
                return;
            }
            if (waitingForCmdRatio) {
                waitingForCmdRatio = false;
                if (r.getElement(0) != '!') {
                    log.error(rb.getString("LogNceClockRatioCmdError") + r.getElement(0));
                }
                callStateMachines();
                return;
            }
            if (waitingForCmdStop) {
                waitingForCmdStop = false;
                if (r.getElement(0) != '!') {
                    log.error(rb.getString("LogNceClockStopCmdError") + r.getElement(0));
                }
                callStateMachines();
                return;
            }
            if (waitingForCmdStart) {
                waitingForCmdStart = false;
                if (r.getElement(0) != '!') {
                    log.error(rb.getString("LogNceClockStartCmdError") + r.getElement(0));
                }
                callStateMachines();
                return;
            }
        }
        if (log.isDebugEnabled()) {
        	log.debug(rb.getString("LogReplyUnexpected"));
        }
        return;
    }
    
    private void callStateMachines(){
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
    
    private void readClockPacket (NceReply r) {
    	NceReply priorClockReadPacket = lastClockReadPacket;
    	int priorNceRatio = nceLastRatio;
    	boolean priorNceRunning = nceLastRunning;
        lastClockReadPacket = r;
        //lastClockReadAtTime = internalClock.getTime();
        //log.debug("readClockPacket - at time: " + lastClockReadAtTime);
        nceLastHour = r.getElement(CS_CLOCK_HOURS) & 0xFF;
        nceLastMinute = r.getElement(CS_CLOCK_MINUTES) & 0xFF;
        nceLastSecond = r.getElement(CS_CLOCK_SECONDS) & 0xFF;
        if (r.getElement(CS_CLOCK_1224) == 1) {
            nceLast1224 = true;
        } else {
            nceLast1224 = false;
        }
        if (r.getElement(CS_CLOCK_AMPM) == 'A') {
            nceLastAmPm = true;
        } else {
            nceLastAmPm = false;
        }
        int sc = r.getElement(CS_CLOCK_SCALE) & 0xFF;
        if (sc > 0) {
            nceLastRatio = 250 / sc;
        }
        if (clockMode == SYNCMODE_NCE_MASTER) {
        	if (priorClockReadPacket != null && priorNceRatio != nceLastRatio) {
        		if (log.isDebugEnabled()){
        			log.debug("NCE Change Rate from cab: prior vs last: " + priorNceRatio + " vs " + nceLastRatio);
        		}
        		rateNce.setText("" + nceLastRatio);
        		nceSyncInitStateCounter = 1;
        		nceSyncInitStates();
        	}
        }
        if (r.getElement(CS_CLOCK_STATUS) == 1) {
            nceLastRunning = false;
        } else {
            nceLastRunning = true;
        }
        if (clockMode == SYNCMODE_NCE_MASTER) {
        	if (priorClockReadPacket != null && priorNceRunning != nceLastRunning) {
        		if (log.isDebugEnabled()){
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
    
    private void alarmDisplayUpdateHandler(){
        if (pollingInterval < MIN_POLLING_INTERVAL || pollingInterval > MAX_POLLING_INTERVAL) {
            log.error(rb.getString("LogAlarmTimeIntervalError") + pollingInterval);
            pollingInterval = DEFAULT_POLLING_INTERVAL;
        }
        // initialize things if not running
        alarmSetup();
    	alarmDisplayStates();
        updateInternalClockDisplay();
    }
    
    private void alarmSetup() {
        // initialize things if not running
        if (timerDisplayUpdate == null){
            timerDisplayUpdate = new javax.swing.Timer((int)(pollingInterval * 1000.0), new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        alarmDisplayUpdateHandler();
                    }
                });
        }
        timerDisplayUpdate.setInitialDelay((1 * 1000));
        timerDisplayUpdate.setRepeats(true);     // in case we run by
        timerDisplayUpdate.start();
    	alarmDisplayStateCounter = 1;
    }
    
    private void alarmSyncInit(){
        // initialize things if not running
        int delay = 1000;
        if (alarmSyncUpdate == null){
            alarmSyncUpdate = new javax.swing.Timer(delay,
            		new java.awt.event.ActionListener() {
            			public void actionPerformed(java.awt.event.ActionEvent e) {
            				alarmSyncHandler();
            			}
            		}
            );
            if (clockMode == SYNCMODE_INTERNAL_MASTER) {
                delay = (int)(syncInterval * 1000 / nceLastRatio);
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
    private void alarmSyncStart(){
        // initialize things if not running
        Date now = internalClock.getTime();
        if (alarmSyncUpdate == null){
            alarmSyncInit();
        }
        int delay = 60 * 1000;
        if (clockMode == SYNCMODE_INTERNAL_MASTER) {
            if (syncInterval - 3 - now.getSeconds() <= 0) {
                delay = 10;	// basically trigger right away
            } else {
                delay = (int)((syncInterval - now.getSeconds()) * 1000 / internalClock.getRate());
            }
        }
        if (clockMode == SYNCMODE_NCE_MASTER) {
            delay = 10 * 1000;
        }
        alarmSyncUpdate.setDelay(delay);
        alarmSyncUpdate.setInitialDelay(delay);
        alarmSyncUpdate.start();
        if (log.isDebugEnabled() && extraDebug) {
            log.debug("alarmSyncStart delay: " + delay + " @ " + now);
        }
    }
    
    private void alarmSyncHandler(){
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
        if (alarmDisplayStateCounter == 0){
            alarmDisplayStateCounter = 1;
            alarmDisplayStates();
        }
    }
    
    private void alarmDisplayStates() {
		int priorState = 0;
		do {
			if (log.isDebugEnabled() && extraDebug){
				log.debug("alarmDisplayStates: before: " + alarmDisplayStateCounter + " " + internalClock.getTime());
			}
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
	    	}
			if (log.isDebugEnabled() && extraDebug){
				log.debug("alarmDisplayStates: after: " + alarmDisplayStateCounter + " " + internalClock.getTime());
			}
		} while (priorState != alarmDisplayStateCounter);
    }
    
    private double getNceTime() {
        double nceTime = 0;
        if (lastClockReadPacket != null) {
            nceTime = (lastClockReadPacket.getElement(CS_CLOCK_HOURS) * 3600) +
                (lastClockReadPacket.getElement(CS_CLOCK_MINUTES) * 60) +
                lastClockReadPacket.getElement(CS_CLOCK_SECONDS) +
                (lastClockReadPacket.getElement(CS_CLOCK_TICK) * 0.25);
        }
        return(nceTime);
    }
    
    @SuppressWarnings("deprecation")
    private Date getNceDate() {
        Date now = internalClock.getTime();
        if (lastClockReadPacket != null) {
            now.setHours(lastClockReadPacket.getElement(CS_CLOCK_HOURS));
            now.setMinutes(lastClockReadPacket.getElement(CS_CLOCK_MINUTES));
            now.setSeconds(lastClockReadPacket.getElement(CS_CLOCK_SECONDS));
        }
        return(now);
    }
    
    @SuppressWarnings("deprecation")
    private double getIntTime() {
        Date now = internalClock.getTime();
        int ms = (int)(now.getTime() % 1000);
        int ss = now.getSeconds();
        int mm = now.getMinutes();
        int hh = now.getHours();
        if (log.isDebugEnabled() && extraDebug) {
            log.debug("getIntTime: " + hh + ":" + mm + ":" + ss + "." + ms);
        }
        return((hh * 60 * 60) + (mm * 60) + ss + (ms / 1000));
    }
    
    private void changeNceClockRatio() {
        try {
            int newRatio = Integer.parseInt(rateNce.getText().trim());
            issueClockRatio(newRatio);
        } catch (NumberFormatException e) {
            log.error("Invalid value: " + rateNce.getText().trim());
        }
    }
    
    @SuppressWarnings("deprecation")
    private void internalSyncInitStates() {
        Date now = internalClock.getTime();
        int priorState = internalSyncInitStateCounter;
        do {
            if (log.isDebugEnabled() && internalSyncInitStateCounter != 0 && extraDebug){
                log.debug("internalSyncInitStates begin: " + internalSyncInitStateCounter + " @ " + now);
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
	        	if (!waitingForCmdStop){
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
	        	if (!waitingForCmdStop){
		            internalSyncInitStateCounter++;
	        	}
	        	break;
	        case 3:
	            // set NCE ratio, mode etc...
	            issueClockRatio((int)internalClock.getRate());
	            internalSyncInitStateCounter++;
	            break;
	        case 4:
	        	if (!waitingForCmdRatio){
		            internalSyncInitStateCounter++;
	        	}
	        	break;
	        case 5:
	            issueClock1224(true);
	            internalSyncInitStateCounter++;
	            break;
	        case 6:
	        	if (!waitingForCmd1224){
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
	        	if (!waitingForCmdTime){
		            internalSyncInitStateCounter++;
	        	}
	        	break;
	        case 9:
	            issueClockStart();
	            internalSyncInitStateCounter++;
	            break;
	        case 10:
	        	if (!waitingForCmdStart){
		            internalSyncInitStateCounter++;
	        	}
	        	break;
	        case 11:
	            issueReadOnlyRequest();
	            internalSyncInitStateCounter++;
	            break;
	        case 12:
	        	if (!waitingForCmdRead){
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
	            if (log.isDebugEnabled() && extraDebug){
	                log.debug("internalSyncState: init done");
	            }
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
        double intTime = 0;
        double nceTime = 0;
        double diffTime = 0;
        Date now = internalClock.getTime();
        if (log.isDebugEnabled() && internalSyncRunStateCounter != 0 && extraDebug){
            log.debug("internalSyncRunStates: " + internalSyncRunStateCounter + " @ " + now);
        }
        int priorState = internalSyncRunStateCounter;
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
	        	if (!waitingForCmdRead){
	        		internalSyncRunStateCounter++;
	        	}
	        	break;
	        case 3:
	            // compute error
	            nceTime = getNceTime();
	            intTime = getIntTime();
	            diffTime = intTime - nceTime;
	            if (log.isDebugEnabled() && extraDebug) {
                    log.debug("syncStates2 begin. NCE: " +
    					(nceLastHour / 10) + (nceLastHour - ((nceLastHour / 10) * 10)) +
    					rb.getString("LabelTimeSep") +
    					(nceLastMinute / 10) + (nceLastMinute - ((nceLastMinute / 10) * 10)) +
    					rb.getString("LabelTimeSep") +
    					(nceLastSecond / 10) + (nceLastSecond - ((nceLastSecond / 10) * 10)) +
    					" Internal: " +
    					(now.getHours() / 10) + (now.getHours() - ((now.getHours() / 10) * 10)) + 
    					rb.getString("LabelTimeSep") +
    					(now.getMinutes() / 10) + (now.getMinutes() - ((now.getMinutes() / 10) * 10)) + 
    					rb.getString("LabelTimeSep") +
    					(now.getSeconds() / 10) + (now.getSeconds() - ((now.getSeconds() / 10) * 10)) +
    					" diff: " +
    					diffTime);
	            }
	            // save error to array
	            while (priorDiffs.size() >= MAX_ERROR_ARRAY) {
	                priorDiffs.remove(0);
	            }
	            priorDiffs.add(new Double(diffTime));
	            recomputeInternalSync();
	            issueClockSet(
	                      now.getHours(),
	                      now.getMinutes(),
	                      (int) syncInterval
	                      );
	            internalSyncRunStateCounter++;
	            break;
	        case 4:
	        	if (!waitingForCmdTime){
	        		internalSyncRunStateCounter++;
	        	}
	        	break;
	        case 5:
	            issueReadOnlyRequest();
	            internalSyncRunStateCounter++;
	            break;
	        case 6:
	        	if (!waitingForCmdRead){
	        		internalSyncRunStateCounter++;
	        	}
	        	break;
	        case 7:
	            // compute offset delay
	            intTime = now.getSeconds();
	            diffTime = TARGET_SYNC_DELAY - intTime;
	            // save offset error to array
	            while (priorOffsetErrors.size() >= MAX_ERROR_ARRAY) {
	                priorOffsetErrors.remove(0);
	            }
	            priorOffsetErrors.add(new Double(diffTime));
	            recomputeOffset();
	            if (log.isDebugEnabled() && extraDebug) {
	                log.debug("syncState compute offset. NCE: " +
	                          (nceLastHour / 10) + (nceLastHour - ((nceLastHour / 10) * 10)) +
	                          rb.getString("LabelTimeSep") +
	                          (nceLastMinute / 10) + (nceLastMinute - ((nceLastMinute / 10) * 10)) +
	                          rb.getString("LabelTimeSep") +
	                          (nceLastSecond / 10) + (nceLastSecond - ((nceLastSecond / 10) * 10)) +
	                          " Internal: " +
	                          (now.getHours() / 10) + (now.getHours() - ((now.getHours() / 10) * 10)) + 
	                          rb.getString("LabelTimeSep") +
	                          (now.getMinutes() / 10) + (now.getMinutes() - ((now.getMinutes() / 10) * 10)) + 
	                          rb.getString("LabelTimeSep") +
	                          (now.getSeconds() / 10) + (now.getSeconds() - ((now.getSeconds() / 10) * 10)));
	            }
	            internalSyncRunStateCounter = 0;
	            break;
	        default:
	        	internalSyncRunStateCounter = 0;
	            break;
	        }
        } while (priorState != internalSyncRunStateCounter);
    }
    
    private void internalClockStatusCheck(){

    	// if change to internal clock
    	if (clockMode == SYNCMODE_INTERNAL_MASTER) {
	    	if (internalLastRunning != internalClock.getRun()) {
	    		if (internalClock.getRun()){
	    			internalSyncInitStateCounter = 1;
	    		} else {
	    			internalSyncInitStateCounter = -3;
	    		}
	    		internalSyncInitStates();
	    	}
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
        }
        catch (NumberFormatException e) {
            log.error("Invalid value: " + ncePidGainP.getText().trim());
            ok = false;
        }
        try {
            i = Double.parseDouble(ncePidGainI.getText().trim());
        }
        catch (NumberFormatException e) {
            log.error("Invalid value: " + ncePidGainP.getText().trim());
            ok = false;
        }
        try {
            d = Double.parseDouble(ncePidGainD.getText().trim());
        }
        catch (NumberFormatException e) {
            log.error("Invalid value: " + ncePidGainP.getText().trim());
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
            sumDiff = priorOffsetErrors.get(0).doubleValue() + priorOffsetErrors.get(1).doubleValue();
        }
        double avgDiff = sumDiff / 2;
        syncInterval = syncInterval + avgDiff;
        if (syncInterval < 30) {
        	syncInterval = 30;
        }
        if (syncInterval > 58) {
        	syncInterval = 58;
        }
        if (log.isDebugEnabled() && extraDebug) {
            debugOutputForRecomputeOffset(avgDiff);
        }
    }
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION",justification="Slow operation in debug OK for now")
    private void debugOutputForRecomputeOffset(double avgDiff) {
        Date now = internalClock.getTime();
        String txt = "";
        for (int i = 0; i < priorOffsetErrors.size(); i++) {
            txt = txt + " " + priorOffsetErrors.get(i).doubleValue();
        }
        log.debug("priorOffsetErrors: " + txt);
        log.debug("syncOffset: " + syncInterval + " avgDiff: " + avgDiff + " @ " + now.toString());
    }
     
    private void recomputeInternalSync() {
        //Date now = internalClock.getTime();
        double sumDiff = 0;
        double currError = 0;
        //double diffError = 0;
        //double avgDiff = 0;
        if (priorDiffs.size() > 0) {
            currError = priorDiffs.get(priorDiffs.size() - 1).doubleValue();
            //diffError = priorDiffs.get(priorDiffs.size() - 1).doubleValue() - ((Double) priorDiffs.get(0)).doubleValue();
        }
        for (int i = 0; i < priorDiffs.size(); i++) {
            sumDiff = sumDiff + priorDiffs.get(i).doubleValue();
        }
        double corrDiff = 0;
        if (priorCorrections.size() > 0) {
            corrDiff = priorCorrections.get(priorCorrections.size() - 1).doubleValue() - priorCorrections.get(0).doubleValue();
        }
        double pCorr = currError * intPidGainPv;
        double iCorr = sumDiff * intPidGainIv;
        double dCorr = corrDiff * intPidGainDv;
        double newRateAdj = pCorr + iCorr + dCorr;
        // save correction to array
        while (priorCorrections.size() >= MAX_ERROR_ARRAY) {
            priorCorrections.remove(0);
        }
        priorCorrections.add(new Double(newRateAdj));
        syncInterval = syncInterval + newRateAdj;
        if (syncInterval > 57) {
            syncInterval = 57;
        }
        if (syncInterval < 40) {
            syncInterval = 40;
        }
        if (log.isDebugEnabled() && extraDebug) {
            debugOutputForRecomputeInternalSync(pCorr, iCorr, dCorr);
        }
    }
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION",justification="Slow operation in debug OK for now")
    private void debugOutputForRecomputeInternalSync(double pCorr, double iCorr, double dCorr) {
        String txt = "";
        for (int i = 0; i < priorDiffs.size(); i++) {
            txt = txt + " " + priorDiffs.get(i);
        }
        log.debug("priorDiffs: " + txt);
        log.debug("syncInterval: " + syncInterval +
                  " pCorr: " + fiveDigits.format(pCorr) +
                  " iCorr: " + fiveDigits.format(iCorr) +
                  " dCorr: " + fiveDigits.format(dCorr)
                  );
    }
    
    private void recomputeNceSync() {
        //Date now = internalClock.getTime();
        double sumDiff = 0;
        double currError = 0;
        double diffError = 0;
        if (priorDiffs.size() > 0) {
            currError = priorDiffs.get(priorDiffs.size() - 1).doubleValue();
            diffError = priorDiffs.get(priorDiffs.size() - 1).doubleValue() - priorDiffs.get(0).doubleValue();
        }
        for (int i = 0; i < priorDiffs.size(); i++) {
            sumDiff = sumDiff + priorDiffs.get(i).doubleValue();
        }
        double corrDiff = 0;
        if (priorCorrections.size() > 0) {
            corrDiff = priorCorrections.get(priorCorrections.size() - 1).doubleValue() - priorCorrections.get(0).doubleValue();
        }
        double pCorr = currError * ncePidGainPv;
        double iCorr = diffError * ncePidGainIv;
        double dCorr = corrDiff * ncePidGainDv;
        double newRateAdj = pCorr + iCorr + dCorr;
        //		if (newRateAdj > 0.5) {
        //	newRateAdj = 0.5;
        //}
        //if (newRateAdj < -0.5) {
        //		newRateAdj = -0.5;
        //		}
        // save correction to array
        while (priorCorrections.size() >= MAX_ERROR_ARRAY) {
            priorCorrections.remove(0);
        }
        priorCorrections.add(new Double(newRateAdj));
        double oldInternalRate = internalClock.getRate();
        double newInternalRate = oldInternalRate + newRateAdj;
        if (Math.abs(currError) > 60){
            // don't try to drift, just reset
            nceSyncInitStateCounter = 1;
            nceSyncInitStates();
        } else if (Math.abs(oldInternalRate - newInternalRate) >= rateChgMinimum) {
            try {
                internalClock.setRate(newInternalRate);
                if (log.isDebugEnabled()){
                    log.debug("changing internal rate: " + newInternalRate);
                }
            } catch (TimebaseRateException e) {
                log.error("recomputeNceSync: Failed setting new internal rate: " + newInternalRate);
                // just set the internal to NCE and set the clock
                nceSyncInitStateCounter = 1;
                nceSyncInitStates();
            }
        }
        if (log.isDebugEnabled() && extraDebug) {
            debugOutputForRecomputeNceSync(pCorr, iCorr, dCorr, newInternalRate, currError);
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION",justification="Slow operation in debug OK for now")
    private void debugOutputForRecomputeNceSync(double pCorr, double iCorr, double dCorr, double newInternalRate, double currError) {
        String txt = "";
        for (int i = priorDiffs.size() - 1; i >= 0 ; i--) {
            txt = txt + " " + threeDigits.format(priorDiffs.get(i));
        }
        log.debug("priorDiffs: " + txt);
        txt = "";
        for (int i = priorCorrections.size() - 1; i >= 0 ; i--) {
            txt = txt + " " + threeDigits.format(priorCorrections.get(i));
        }
        log.debug("priorCorrections: " + txt);
        log.debug("currError: " + fiveDigits.format(currError) +
                  " pCorr: " + fiveDigits.format(pCorr) +
                  " iCorr: " + fiveDigits.format(iCorr) +
                  " dCorr: " + fiveDigits.format(dCorr) +
                  " newInternalRate: " + threeDigits.format(newInternalRate));
    }
       
    private void changePollingSpeed(double newInterval) {
        if (newInterval < MIN_POLLING_INTERVAL || newInterval > MAX_POLLING_INTERVAL) {
            log.error(rb.getString("LogAlarmTimeIntervalError") + newInterval);
        } else {
            pollingInterval = newInterval;
            pollingSpeed.setText("" + pollingInterval);
            if (timerDisplayUpdate == null) {
                alarmSetup();
            }
            timerDisplayUpdate.setDelay((int)(pollingInterval * 1000));
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
            if (log.isDebugEnabled()) {
                log.debug("changeSyncMode(): New Mode: " + newMode + " Old Mode: " + oldMode);
            }
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
        int priorState = 0;
        do {
            if (log.isDebugEnabled() && extraDebug) {
                log.debug("Before nceSyncInitStateCounter: " + nceSyncInitStateCounter + " " + internalClock.getTime());
            }
	        priorState = nceSyncInitStateCounter;
	        switch (nceSyncInitStateCounter) {
	        case -1:
	            // turn all this off
	            if (alarmSyncUpdate != null){
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
	            if (log.isDebugEnabled()) {
	                log.debug("Init/Reset NCE Clock Sync");
	            }
	            // make sure other state is off
	            nceSyncRunStateCounter = 0;
	            // stop internal clock
	            internalClock.setRun(false);
	            if (alarmSyncUpdate != null){
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
	                log.error("nceSyncInitStates: failed to set internal clock rate: " + nceLastRatio);
	            }
	            // get time from NCE settings and set internal clock
	            setInternalClockFromNce();
	            internalClock.setRun(true);
	            nceSyncInitStateCounter = 0;	// init is done
	            nceSyncRunStateCounter = 1;
	            nceSyncRunStates();
	            alarmSyncStart();
	            updateNceClockDisplay();
	            updateInternalClockDisplay();
	            break;
	        }
	        if (log.isDebugEnabled() && extraDebug) {
	            log.debug("After nceSyncInitStateCounter: " + nceSyncInitStateCounter + " " + internalClock.getTime());
	        }
        } while (priorState != nceSyncInitStateCounter);
    }
    private void nceSyncRunStates() {
        double intTime = 0;
        double nceTime = 0;
        double diffTime = 0;
        if (log.isDebugEnabled() && extraDebug) {
            log.debug("Before nceSyncRunStateCounter: " + nceSyncRunStateCounter + " " + internalClock.getTime());
        }
        int priorState = 0;
        do {
	        priorState = nceSyncRunStateCounter;
	        switch (nceSyncRunStateCounter) {
	        case 1:	// issue read for nce time
	            issueReadOnlyRequest();
	            nceSyncRunStateCounter++;
	            break;
	        case 2:
	            // did read happen??
	            if (!waitingForCmdRead) {
	                nceSyncRunStateCounter++;
	            }
	            break;
	        case 3:	// compare internal with nce time
	            intTime = getIntTime();
	            nceTime = getNceTime();
	            diffTime = nceTime - intTime;
	            // deal with end of day reset
	            if (diffTime > MAX_SECONDS_IN_DAY / 2) {
	                diffTime = MAX_SECONDS_IN_DAY + nceTime - intTime;
	            } else if (diffTime < MAX_SECONDS_IN_DAY / -2) {
	                diffTime = nceTime ;
	            }
	            if (log.isDebugEnabled()){
	                log.debug("new diffTime: " + diffTime + " = " + nceTime + " - " + intTime);
	            }
	            // save error to array
	            while (priorDiffs.size() >= MAX_ERROR_ARRAY) {
	                priorDiffs.remove(0);
	            }
	            priorDiffs.add(new Double(diffTime));
	            recomputeNceSync();
	            // initialize things if not running
	            if (alarmSyncUpdate == null){
	                alarmSyncInit();
	            }
	            updateNceClockDisplay();
	            updateInternalClockDisplay();
	            nceSyncRunStateCounter++;
	            break;
	        case 4:
	            // wait for next minute
	            nceSyncRunStateCounter = 0;
	        }
        } while (priorState != nceSyncRunStateCounter);
        if (log.isDebugEnabled() && extraDebug) {
            log.debug("After nceSyncRunStateCounter: " + nceSyncRunStateCounter + " " + internalClock.getTime());
        }
    }
    
    private void setInternalClockFromNce() {
        Date newTime = getNceDate();
        internalClock.setTime(newTime);
        if (log.isDebugEnabled()) {
            log.debug("setInternalClockFromNce nceClock: " + newTime);
        }
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
                    amPm.setText(rb.getString("TagAm"));
                } else {
                    amPm.setText(rb.getString("TagPm"));
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
                status.setText(rb.getString("TagRunning"));
            } else {
                status.setText(rb.getString("TagStopped"));
            }
        }
    }
    
    private void updateNceClockDisplay() {
        String txt = "";
        if (nceLastRunning) {
            txt = rb.getString("TagRunning");
        } else {
            txt = rb.getString("TagStopped");
        }
        txt = txt + " " + 
            (nceLastHour / 10) + (nceLastHour - ((nceLastHour / 10) * 10)) + rb.getString("LabelTimeSep") +
            (nceLastMinute / 10) + (nceLastMinute - ((nceLastMinute / 10) * 10)) + rb.getString("LabelTimeSep") +
            (nceLastSecond / 10) + (nceLastSecond - ((nceLastSecond / 10) * 10));
        if (!nceLast1224) {
            if (nceLastAmPm) {
                txt = txt + " " + rb.getString("TagAm");
            } else {
                txt = txt + " " + rb.getString("TagPm");
            }
        }
        txt = txt + " " + rb.getString("LabelRatio") + " " + 
            nceLastRatio + rb.getString("LabelToOne");
        if (clockMode == SYNCMODE_NCE_MASTER) {
            txt = txt + " " + rb.getString("TagIsNceMaster");
            double intTime = getIntTime();
            double nceTime = getNceTime();
            double diffTime = nceTime - intTime;
            txt = txt + " " + rb.getString("ClockError");
            txt = txt + " " + threeDigits.format(diffTime);
            if (log.isDebugEnabled() && extraDebug){
                log.debug("intTime: " + intTime + " nceTime: " + nceTime + " diffTime: " + diffTime);
            }
        }
        nceDisplayStatus.setText(txt);
    }
    
    @SuppressWarnings("deprecation")
    private void updateInternalClockDisplay() {
        String txt = "";
        Date now = internalClock.getTime();
        if (internalClock.getRun()) {
            txt = rb.getString("TagRunning");
        } else {
            txt = rb.getString("TagStopped");
        }
        txt = txt + " " + 
            (now.getHours() / 10) + (now.getHours() - ((now.getHours() / 10) * 10)) + 
            rb.getString("LabelTimeSep") +
            (now.getMinutes() / 10) + (now.getMinutes() - ((now.getMinutes() / 10) * 10)) + 
            rb.getString("LabelTimeSep") +
            (now.getSeconds() / 10) + (now.getSeconds() - ((now.getSeconds() / 10) * 10));
        txt = txt + " " + 
            rb.getString("LabelRatio") + " " + 
            threeDigits.format(internalClock.getRate()) + rb.getString("LabelToOne");
        if (clockMode == SYNCMODE_INTERNAL_MASTER) {
            txt = txt + " " + rb.getString("TagIsInternalMaster");
            double intTime = getIntTime();
            double nceTime = getNceTime();
            double diffTime = nceTime - intTime;
            txt = txt + " " + rb.getString("ClockError");
            txt = txt + " " + threeDigits.format(diffTime);
        }
        internalDisplayStatus.setText(txt);
    }
    
    private void issueReadOnlyRequest() {
        if (!waitingForCmdRead){
            byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
            NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CS_CLOCK_MEM_SIZE);
            waiting++;
            waitingForCmdRead = true;
            tc.sendNceMessage(cmdNce, this);
            //			log.debug("issueReadOnlyRequest at " + internalClock.getTime());
        }
    }
    
    private void issueReadAllRequest() {
        if(!waitingForCmdRead){
            byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
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
	private void issueReadTimeRequest() {
        if (!waitingForCmdRead) {
            byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
            NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CS_CLOCK_MEM_SIZE);
            waiting++;
            waitingForCmdRead = true;
            tc.sendNceMessage(cmdNce, this);
        }
        updateTimeFromRead = true;
    }
    
    @SuppressWarnings("unused")
	private void issueReadRatioRequest() {
        if (!waitingForCmdRead){
            byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
            NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CS_CLOCK_MEM_SIZE);
            waiting++;
            waitingForCmdRead = true;
            tc.sendNceMessage(cmdNce, this);
        }
        updateRatioFromRead = true;
    }
    
    @SuppressWarnings("unused")
	private void issueReadFormatRequest() {
        if (!waitingForCmdRead){
            byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
            NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CS_CLOCK_MEM_SIZE);
            waiting++;
            waitingForCmdRead = true;
            tc.sendNceMessage(cmdNce, this);
        }
        updateFormatFromRead = true;
    }
    
    @SuppressWarnings("unused")
	private void issueReadStatusRequest() {
        if (!waitingForCmdRead){
            byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
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
    
    private void issueClockSetMem(int hh, int mm, int ss){
        byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryWriteN(CS_CLOCK_MEM_ADDR + CS_CLOCK_SECONDS, 3);
        cmd[4] = (byte) ss;
        cmd[5] = (byte) mm;
        cmd[6] = (byte) hh;
        NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CMD_MEM_SET_REPLY_SIZE);
        waiting++;
        waitingForCmdTime = true;
        tc.sendNceMessage(cmdNce, this);
    }
   
    private void issueClockRatio(int r) {
        byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accSetClockRatio(r);
        NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CMD_CLOCK_SET_REPLY_SIZE);
        waiting++;
        waitingForCmdRatio = true;
        tc.sendNceMessage(cmdNce, this);
    }
    
    private void issueClock1224(boolean mode) {
        byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accSetClock1224(mode);
		NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CMD_CLOCK_SET_REPLY_SIZE);
		waiting++;
		waitingForCmd1224 = true;
		tc.sendNceMessage(cmdNce, this);
    }
    
    private void issueClockStop() {
        byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accStopClock();
        NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CMD_CLOCK_SET_REPLY_SIZE);
        waiting++;
        waitingForCmdStop = true;
        tc.sendNceMessage(cmdNce, this);
    }
    
    private void issueClockStart() {
        byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accStartClock();
        NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CMD_CLOCK_SET_REPLY_SIZE);
        waiting++;
        waitingForCmdStart = true;
        tc.sendNceMessage(cmdNce, this);
    }
    
    /**
     * Handles minute notifications for NCE Clock Monitor/Synchronizer
     */
    public void newInternalMinute()
    {
        //		 if (log.isDebugEnabled()) {
        //	log.debug("newInternalMinute clockMode: " + clockMode + " nceInit: " + nceSyncInitStateCounter + " nceRun: " + nceSyncRunStateCounter);
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
        if (timerDisplayUpdate!=null) {
            timerDisplayUpdate.stop();
        }
        //super.windowClosing(e);
    }
    
    public void dispose() {
        // stop alarm
        if (timerDisplayUpdate!=null) {
            timerDisplayUpdate.stop();
            timerDisplayUpdate = null;
        }
        // Remove ourselves from the Timebase minute rollover event
        InstanceManager.timebaseInstance().removeMinuteChangeListener( minuteChangeListener );
        minuteChangeListener = null ;
        
        // take apart the JFrame
        super.dispose();
    }
    
    static Logger log = LoggerFactory.getLogger(ClockMonPanel.class.getName());
    
}
