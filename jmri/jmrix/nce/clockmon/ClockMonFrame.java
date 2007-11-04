// ClockMonFrame.java

package jmri.jmrix.nce.clockmon;

import jmri.InstanceManager;
import jmri.Timebase;
import jmri.TimebaseRateException;
import jmri.jmrix.nce.NceListener;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceEpromChecker;

import java.util.Date;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.awt.event.*;

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
 * @version			$Revision: 1.3 $
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
 *  A. every internal minute, read nce clock
 *  B. compute error and record last X errors for correction calc
 *  C. adjust internal as needed
 *  C. adjust internal clock factor as needed
 *  
 * 5. The clock message only seems to go out to the throttles on the tic of the minute.
 * 6. The nce clock must be left running, or it doesn't tic and therefore doesn't go out the bus.
 *  
 */
public class ClockMonFrame extends jmri.util.JmriJFrame implements NceListener {
	
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.nce.clockmon.ClockMonBundle");
	
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
	public static final int MAX_ERROR_ARRAY = 6;
	public static final double MIN_POLLING_INTERVAL = 0.5;
	public static final double MAX_POLLING_INTERVAL = 120;
	public static final double DEFAULT_POLLING_INTERVAL = 5;
	public static final double TARGET_SYNC_DELAY = 55;
	public static final double TARGET_SYNC_OFFSET = 5;
	public static final int SYNCMODE_OFF = 0;				//0 - clocks independent
	public static final int SYNCMODE_NCE_MASTER = 1;		//1 - NCE sets Internal
	public static final int SYNCMODE_INTERNAL_MASTER = 2;	//2 - Internal sets NCE
	public static final int WAIT_CMD_EXECUTION = 1000;
	
	private int waiting = 0;
	private int clockMode = SYNCMODE_OFF;
	private boolean waitingForRead = false;
	private boolean waitingForCmdStop = false;
	private boolean waitingForCmdStart = false;
	private boolean waitingForCmdRatio = false;
	private boolean waitingForCmdTime = false;
	private boolean waitingForMemTime = false;
	private boolean waitingForCmd1224 = false;
	private boolean waitingForCmdScale = false;
	private boolean updateTimeFromRead = false;
	private boolean updateRatioFromRead = false;
	private boolean updateFormatFromRead = false;
	private boolean updateStatusFromRead = false;
	private NceReply lastClockReadPacket;
	private Date lastClockReadAtTime;
	private int	nceLastHour;
	private int nceLastMinute;
	private int nceLastSecond;
	private int nceLastRatio;
	private boolean nceLastAmPm;
	private boolean nceLast1224;
	private boolean nceLastRunning;
	private double pollingInterval = DEFAULT_POLLING_INTERVAL;
	private ArrayList priorDiffs = new ArrayList();
	private ArrayList priorOffsetErrors = new ArrayList();
	private double syncInterval = TARGET_SYNC_DELAY;
	private double syncOffset = TARGET_SYNC_OFFSET;
	private int syncStateCounter = 0;
	private Date priorReadAt_1;
	private Date priorReadAt_2;

    Timebase internalClock ;
    javax.swing.Timer timerDisplayUpdate = null;
    javax.swing.Timer timerSyncUpdate = null;

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

    JTextField internalDisplayStatus = new JTextField(40);
    
    JTextField nceDisplayStatus = new JTextField(40);
    
    JTextField pollingSpeed = new JTextField(5);
    
    java.beans.PropertyChangeListener minuteChangeListener ;

    JButton setSyncButton = new JButton(rb.getString("SetSyncMode"));
    JButton setClockButton = new JButton(rb.getString("SetHoursMinutes"));
    JButton setRatioButton = new JButton(rb.getString("SetRatio"));
    JButton set1224Button = new JButton(rb.getString("Set12/24Mode"));
    JButton setStopNceButton = new JButton(rb.getString("StopNceClock"));
    JButton setStartNceButton = new JButton(rb.getString("StartNceClock"));
    JButton readButton = new JButton(rb.getString("ReadAll"));
    JButton setPollingSpeedButton = new JButton(rb.getString("SetInterfaceUpdRate"));

    public ClockMonFrame() {}
    	
    public void initComponents() throws Exception {
        setTitle(rb.getString("TitleNceClockMonitor"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Internal Clock Info Panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel pane2 = new JPanel();

        javax.swing.border.Border pane2Border = BorderFactory.createEtchedBorder();
        javax.swing.border.Border pane2Titled = BorderFactory.createTitledBorder(pane2Border,
        		rb.getString("InternalClockStatusBorderText"));
        pane2.setBorder(pane2Titled);
        pane2.add(internalDisplayStatus);
        internalDisplayStatus.setEditable(false);
        internalDisplayStatus.setBorder(BorderFactory.createEmptyBorder());
        getContentPane().add(pane2);

        // NCE Clock Info Panel
        pane2 = new JPanel();
        pane2Border = BorderFactory.createEtchedBorder();
        pane2Titled = BorderFactory.createTitledBorder(pane2Border,
        		rb.getString("NceClockStatusBorderText"));
        pane2.setBorder(pane2Titled);
        pane2.add(nceDisplayStatus);
        nceDisplayStatus.setEditable(false);
        nceDisplayStatus.setBorder(BorderFactory.createEmptyBorder());
        getContentPane().add(pane2);
        
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
        getContentPane().add(pane2);

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
        getContentPane().add(pane2);

        // add 12/24 clock options
        pane2 = new JPanel();
        pane2Border = BorderFactory.createEtchedBorder();
        pane2Titled = BorderFactory.createTitledBorder(pane2Border,
        		rb.getString("SetClock12/24ModeBorderText"));
        pane2.setBorder(pane2Titled);
        pane2.add(twentyFour);
        pane2.add(new JLabel(" "));
        pane2.add(set1224Button);
        getContentPane().add(pane2);
        
//        panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
//        panel.add(new JLabel(" "));
//        panel.add(status);
//        getContentPane().add(panel);
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(setStartNceButton);
        panel.add(new JLabel(" "));
        panel.add(setStopNceButton);
        getContentPane().add(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(readButton);
        getContentPane().add(panel);

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(setSyncModeInternalMaster);
        modeGroup.add(setSyncModeNceMaster);
        modeGroup.add(setSyncModeOff);

        getContentPane().add(setSyncModeNceMaster);
        getContentPane().add(setSyncModeInternalMaster);
        getContentPane().add(setSyncModeOff);
        getContentPane().add(setSyncButton);
        setSyncModeInternalMaster.setEnabled(false);
        setSyncModeNceMaster.setEnabled(false);
        if (NceEpromChecker.nceUSBdetected) {	// needs memory commands to sync
        	setSyncModeInternalMaster.setEnabled(false);
            setSyncModeNceMaster.setEnabled(false);
        }

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
        getContentPane().add(pane2);

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
                	changePollingSpeed(Double.parseDouble(pollingSpeed.getText().trim())
                	);
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
        this.setSize(360, 300);

        // Create a Timebase listener for the Minute change events
        internalClock = InstanceManager.timebaseInstance();
        if (internalClock == null){
        	log.error("No Timebase Instance");
        }
        minuteChangeListener = new java.beans.PropertyChangeListener() {
        	public void propertyChange(java.beans.PropertyChangeEvent e) {
        		newInternalMinute();
        	}
        } ;
        if (minuteChangeListener == null){
        	log.error("No minuteChangeListener");
        }
        internalClock.addMinuteChangeListener(minuteChangeListener);
        
        // add help menu to window
    	addHelpMenu("package.jmri.jmrix.nce.clockmon.ClockMonFrame", true);

        // pack for display
        pack();

        // start display alarm timer
        alarmDisplayUpdateHandler();
    }
    
    //  ignore replies
    public void  message(NceMessage m) {
    	log.error("clockmon message received: " + m);
    }  

    public void reply(NceReply r) {
//    	log.debug("nceReplyCatcher() waiting: " + waiting +
//    			" watingForRead: " + waitingForRead +
//    			" waitingForCmdTime: " + waitingForCmdTime +
//    			" waitingForMemTime: " + waitingForMemTime +
//    			" waitingForCmd1224: " + waitingForCmd1224 +
//    			" waitingForCmdRatio: " + waitingForCmdRatio +
//    			" waitingForCmdStop: " + waitingForCmdStop +
//    			" waitingForCmdStart: " + waitingForCmdStart +
//    			" waitingForCmdScale: " + waitingForCmdScale
//    			);
    	if (waiting <= 0) {
    		log.error(rb.getString("LogReplyEnexpected"));
    		return;
    	}
    	waiting--;
    	if (waitingForRead && r.getNumDataElements() == CS_CLOCK_MEM_SIZE) {
        	readClockPacket(r);
        	waitingForRead = false;
        	syncStates();
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
            	syncStates();
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
        		return;
        	}
    		if (waitingForCmdRatio) {
        		waitingForCmdRatio = false;
        		if (r.getElement(0) != '!') {
        			log.error(rb.getString("LogNceClockRatioCmdError") + r.getElement(0));
        		}
        		return;
        	}
    		if (waitingForCmdStop) {
        		waitingForCmdStop = false;
        		if (r.getElement(0) != '!') {
        			log.error(rb.getString("LogNceClockStopCmdError") + r.getElement(0));
        		}
        		return;
        	}
    		if (waitingForCmdStart) {
        		waitingForCmdStart = false;
        		if (r.getElement(0) != '!') {
        			log.error(rb.getString("LogNceClockStartCmdError") + r.getElement(0));
        		}
        		return;
        	}
    	}
		log.error(rb.getString("LogReplyEnexpected"));
		return;
    }
    
    void alarmDisplayUpdateHandler(){
		if (pollingInterval < MIN_POLLING_INTERVAL || pollingInterval > MAX_POLLING_INTERVAL) {
			log.error(rb.getString("LogAlarmTimeIntervalError") + pollingInterval);
			pollingInterval = DEFAULT_POLLING_INTERVAL;
		}
    	// initialize things if not running
    	if (timerDisplayUpdate == null){
    		timerDisplayUpdate = new javax.swing.Timer((int)(pollingInterval * 1000.0), new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                	alarmDisplayUpdateHandler();
                }
            });
    	}
        timerDisplayUpdate.setInitialDelay((int)(1 * 1000));
        timerDisplayUpdate.setRepeats(true);     // in case we run by
        timerDisplayUpdate.start();
        if (!NceEpromChecker.nceUSBdetected){
            issueReadOnlyRequest();
        } else {
        	
        }
        updateNceClockDisplay();
        updateInternalClockDisplay();
    }

    void alarmSyncInit(){
    	// initialize things if not running
    	if (timerSyncUpdate == null){
        	int delay = (int)(syncInterval * 1000 / nceLastRatio);
    		timerSyncUpdate = new javax.swing.Timer(delay,
    				new java.awt.event.ActionListener() {
                		public void actionPerformed(java.awt.event.ActionEvent e) {
                			alarmSyncHandler();
                		}
            	}
    		);
    		timerSyncUpdate.setInitialDelay(delay);
            timerSyncUpdate.setRepeats(false);
            timerSyncUpdate.stop();
    	}
    }
    
    void alarmSyncStart(){
    	// initialize things if not running
    	if (timerSyncUpdate == null){
    		alarmSyncInit();
    	}
    	int delay = (int)(60 - syncOffset) * 1000;
    	timerSyncUpdate.setDelay(delay);
        timerSyncUpdate.setInitialDelay(delay);
        timerSyncUpdate.start();
    }
    
    void alarmSyncHandler(){
    	syncStateCounter = 1;
    	syncStates();
    }
    
    void syncStateAdvanceCmdTime() {
    	syncStates();
    }

    void syncStates() {
    	double intTime = 0;
    	double nceTime = 0;
    	double diffTime = 0;
    	double offsetTime = 0;
		Date now = internalClock.getTime();
		if (log.isDebugEnabled() && syncStateCounter > 0 && syncStateCounter < 5){
			log.debug("syncStates: " + syncStateCounter + " @ " + now);
		}
    	switch (syncStateCounter) {
    	case 1:
    		// get current values
    		priorReadAt_1 = lastClockReadAtTime;
    		log.debug("syncStates1 issue Read at " + priorReadAt_1);
    		issueReadOnlyRequest();
    		syncStateCounter++;
    		break;
    	case 2:
    		// compute error
    		nceTime = (lastClockReadPacket.getElement(CS_CLOCK_HOURS) * 3600) +
    			(lastClockReadPacket.getElement(CS_CLOCK_MINUTES) * 60) +
    			lastClockReadPacket.getElement(CS_CLOCK_SECONDS);
    		intTime = (now.getHours() * 3600) + 
    			(now.getMinutes() * 60) + 
    			now.getSeconds() +
    			((now.getTime() % 1000) / 1000.0);
    		diffTime = intTime - nceTime;
    		if (log.isDebugEnabled()) {
    			log.debug("state2 delay: " + now.compareTo(priorReadAt_1) +
    					" diff: " + diffTime);
/*    			log.debug("syncStates2 begin. NCE: " +
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
    					diffTime);*/
    		}
    		// initialize things if not running
    		if (timerSyncUpdate == null){
    			alarmSyncInit();
    		}
    		// save error to array
    		while (priorDiffs.size() >= MAX_ERROR_ARRAY) {
    			priorDiffs.remove(0);
    		}
    		priorDiffs.add(new Double(diffTime));
    		recomputeSync();
    		syncStateCounter++;
    		break;
    	case 3:
    		priorReadAt_2 = lastClockReadAtTime;
    		issueReadOnlyRequest();
    		syncStateCounter++;
    		break;
    	case 4:
    		// compute offset delay
    		intTime = (now.getHours() * 3600) + 
			(now.getMinutes() * 60) + 
			now.getSeconds() +
			((now.getTime() % 1000) / 1000.0);
    		diffTime = intTime - TARGET_SYNC_OFFSET - nceLastRatio;
    		// save offset erro to array
    		while (priorOffsetErrors.size() >= MAX_ERROR_ARRAY) {
    			priorOffsetErrors.remove(0);
    		}
    		priorOffsetErrors.add(new Double(diffTime));
    		recomputeOffset();
    		if (log.isDebugEnabled()) {
    			log.debug("syncStates4 after. NCE: " +
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
    		syncStateCounter++;
    		break;
    	}
    }

    private void recomputeOffset() {
    	 Date now = internalClock.getTime();
    	 double sumDiff = 0;
    	 for (int i = 0; i < priorOffsetErrors.size(); i++) {
    		 sumDiff = sumDiff + ((Double) priorOffsetErrors.get(i)).doubleValue();
    	 }
    	 double avgDiff = sumDiff / (double) priorOffsetErrors.size();
    	 syncOffset = syncOffset + avgDiff;
    	 if (syncOffset > 30) {
    		 syncOffset = 30;
    	 }
    	 if (syncOffset < 3) {
    		 syncOffset = 3;
    	 }
    	 if (log.isDebugEnabled()) {
    		 String txt = "";
    		 for (int i = 0; i < priorOffsetErrors.size(); i++) {
    			 txt = txt + " " + ((Double)priorOffsetErrors.get(i)).doubleValue();
    		 }
    		 log.debug("priorOffsetErrors: " + txt);
    		 log.debug("avgDiff: " + avgDiff + " sumDiff: " + sumDiff + " sumCount: " + priorOffsetErrors.size() + " syncOffset: " + syncOffset);
    	 }
    }
    
    private void recomputeSync() {
    	 Date now = internalClock.getTime();
    	 double sumDiff = 0;
    	 for (int i = 0; i < priorDiffs.size(); i++) {
    		 sumDiff = sumDiff + ((Double) priorDiffs.get(i)).doubleValue();
    	 }
    	 double avgDiff = sumDiff / (double) priorDiffs.size();
    	 syncInterval = syncInterval + avgDiff;
    	 if (syncInterval > 57) {
    		 syncInterval = 57;
    	 }
    	 if (syncInterval < 40) {
    		 syncInterval = 40;
    	 }
    	 issueClockSet(
 	    		now.getHours(),
 	    		now.getMinutes(),
 	    		(int) syncInterval
 	    	);
    	 if (log.isDebugEnabled()) {
    		 String txt = "";
    		 for (int i = 0; i < priorDiffs.size(); i++) {
    			 txt = txt + " " + priorDiffs.get(i);
    		 }
    		 log.debug("priorDiffs: " + txt);
    		 log.debug("avgDiff: " + avgDiff + " sumDiff: " + sumDiff + " sumCount: " + priorDiffs.size() + " syncInterval: " + syncInterval);
    	 }
    }
    
    private void changePollingSpeed(double newInterval) {
		if (newInterval < MIN_POLLING_INTERVAL || newInterval > MAX_POLLING_INTERVAL) {
			log.error(rb.getString("LogAlarmTimeIntervalError") + newInterval);
		} else {
			pollingInterval = newInterval;
	        pollingSpeed.setText("" + pollingInterval);
	        timerDisplayUpdate.setDelay((int)(pollingInterval * 1000));
		}
    }
    
    private void changeSyncMode() {
    	int oldMode = clockMode;
        if (internalClock != null) {
        	if (setSyncModeOff.isSelected() == true) {
        		clockMode = SYNCMODE_OFF;
        	}
        	if (setSyncModeNceMaster.isSelected() == true) {
        		clockMode = SYNCMODE_NCE_MASTER;
        	}
        	if (setSyncModeInternalMaster.isSelected() == true) {
        		clockMode = SYNCMODE_INTERNAL_MASTER;
        	}
        	if (oldMode != clockMode) {
        		// some change so, change settings
        		if (oldMode == SYNCMODE_OFF && clockMode == SYNCMODE_INTERNAL_MASTER) {
        			for (int i = priorDiffs.size() - 1; i >= 0; i++) {
        				priorDiffs.remove(i);
        			}
        			syncInterval = TARGET_SYNC_DELAY;
        			syncOffset = TARGET_SYNC_OFFSET;
        			syncStateCounter = 0;
        			// stop NCE clock
        			// set NCE ratio, mode etc...
        			// set initial NCE time
        			// set NCE from internal settings
        			// start NCE clock
        			issueClockStop();
        			issueClockRatio((int)internalClock.getRate());
        			issueClock1224(true);
        			Date now = internalClock.getTime();
        			issueClockSet(now.getHours(), now.getMinutes(), now.getSeconds());
        			issueClockStart();
        		}
        		if (oldMode == SYNCMODE_OFF && clockMode == SYNCMODE_NCE_MASTER) {
        			// set ratio, modes etc...
        			// get time from NCE settings
        			// set internal time
        			
        			try {
						internalClock.setRate(nceLastRatio);
					} catch (TimebaseRateException e) {
						e.printStackTrace();
					}
        			long newTime = (nceLastHour * 60 * 60 * 1000) +
        				(nceLastMinute * 60 * 1000) +
        				(nceLastSecond * 1000);
        			internalClock.setTime(new Date(newTime));
        		}
        		if (oldMode == SYNCMODE_INTERNAL_MASTER && clockMode == SYNCMODE_OFF) {
        			// clear sync timer
        		}
        	}
        }
    }
    
    private void readClockPacket (NceReply r) {
    	lastClockReadPacket = r;
    	lastClockReadAtTime = internalClock.getTime();
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
    	if (r.getElement(CS_CLOCK_STATUS) == 1) {
    		nceLastRunning = false;
    	} else {
    		nceLastRunning = true;
    	}
    	updateSettingsFromNce();
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
        if (nceLastRunning) {
        	nceDisplayStatus.setText(rb.getString("TagRunning"));
        } else {
        	nceDisplayStatus.setText(rb.getString("TagStopped"));
        }
        nceDisplayStatus.setText(nceDisplayStatus.getText().trim() + " " + 
        		(nceLastHour / 10) + (nceLastHour - ((nceLastHour / 10) * 10)) + rb.getString("LabelTimeSep") +
        		(nceLastMinute / 10) + (nceLastMinute - ((nceLastMinute / 10) * 10)) + rb.getString("LabelTimeSep") +
        		(nceLastSecond / 10) + (nceLastSecond - ((nceLastSecond / 10) * 10)));
        if (nceLast1224) {
        	if (nceLastAmPm) {
        		nceDisplayStatus.setText(nceDisplayStatus.getText().trim() + " " + rb.getString("TagAm"));
        	} else {
        		nceDisplayStatus.setText(nceDisplayStatus.getText().trim() + " " + rb.getString("TagPm"));
        	}
        }
        nceDisplayStatus.setText(nceDisplayStatus.getText().trim() + " " + rb.getString("LabelRatio") + " " + 
        		nceLastRatio + rb.getString("LabelToOne"));
    }
    
    private void updateInternalClockDisplay() {
        Date now = internalClock.getTime();
        if (internalClock.getRun()) {
        	internalDisplayStatus.setText(rb.getString("TagRunning"));
        } else {
        	internalDisplayStatus.setText(rb.getString("TagStopped"));
        }
        internalDisplayStatus.setText(internalDisplayStatus.getText().trim() + " " + 
        		(now.getHours() / 10) + (now.getHours() - ((now.getHours() / 10) * 10)) + 
        		rb.getString("LabelTimeSep") +
        		(now.getMinutes() / 10) + (now.getMinutes() - ((now.getMinutes() / 10) * 10)) + 
        		rb.getString("LabelTimeSep") +
        		(now.getSeconds() / 10) + (now.getSeconds() - ((now.getSeconds() / 10) * 10)));
        internalDisplayStatus.setText(internalDisplayStatus.getText().trim() + " " + 
        		rb.getString("LabelRatio") + " " + 
        		internalClock.getRate() + rb.getString("LabelToOne"));
    }
    
    private void issueReadOnlyRequest() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CS_CLOCK_MEM_SIZE);
    	waiting++;
    	waitingForRead = true;
		updateTimeFromRead = false;
		updateRatioFromRead = false;
		updateFormatFromRead = false;
		updateStatusFromRead = false;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
//    	log.debug("issueReadOnlyRequest at " + internalClock.getTime());
    }

    private void issueReadAllRequest() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CS_CLOCK_MEM_SIZE);
    	waiting++;
    	waitingForRead = true;
		updateTimeFromRead = true;
		updateRatioFromRead = true;
		updateFormatFromRead = true;
		updateStatusFromRead = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    }

    private void issueReadTimeRequest() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CS_CLOCK_MEM_SIZE);
    	waiting++;
    	waitingForRead = true;
		updateTimeFromRead = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    }

    private void issueReadRatioRequest() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CS_CLOCK_MEM_SIZE);
    	waiting++;
    	waitingForRead = true;
		updateRatioFromRead = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    }

    private void issueReadFormatRequest() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CS_CLOCK_MEM_SIZE);
    	waiting++;
    	waitingForRead = true;
		updateFormatFromRead = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    }

    private void issueReadStatusRequest() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CS_CLOCK_MEM_SIZE);
    	waiting++;
    	waitingForRead = true;
		updateStatusFromRead = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    }
    
    private void issueClockSet(int hh, int mm, int ss) {
    	issueClockSetMem(hh, mm, ss);
    }

    private void issueClockSetMem(int hh, int mm, int ss){
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryWriteN(CS_CLOCK_MEM_ADDR + CS_CLOCK_SECONDS, 3);
    	cmd[4] = (byte) ss;
    	cmd[5] = (byte) mm;
    	cmd[6] = (byte) hh;
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CMD_MEM_SET_REPLY_SIZE);
    	waiting++;
    	waitingForCmdTime = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    }

    private void issueClockScaleMem(int sc){
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryWriteN(CS_CLOCK_MEM_ADDR + CS_CLOCK_SCALE, 1);
    	cmd[4] = (byte) sc;
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CMD_MEM_SET_REPLY_SIZE);
    	waiting++;
    	waitingForCmdScale = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    }
    
    private void changeNceClockRatio() {
    	try {
    		int newRatio = Integer.parseInt(rateNce.getText().trim());
    		issueClockRatio(newRatio);
    	} catch (NumberFormatException e) {
    		log.error("Invalid value: " + rateNce.getText().trim());
    	}
    }
    
    private void issueClockRatio(int r) {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accSetClockRatio(r);
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CMD_CLOCK_SET_REPLY_SIZE);
    	waiting++;
    	waitingForCmdRatio = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    }
    
    private void issueClock1224(boolean mode) {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accSetClock1224(mode);
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CMD_CLOCK_SET_REPLY_SIZE);
    	waiting++;
    	waitingForCmd1224 = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    }
    
    private void issueClockStop() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accStopClock();
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CMD_CLOCK_SET_REPLY_SIZE);
    	waiting++;
    	waitingForCmdStop = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    }
    
    private void issueClockStart() {
    	byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accStartClock();
    	NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CMD_CLOCK_SET_REPLY_SIZE);
    	waiting++;
    	waitingForCmdStart = true;
    	jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
    }
    
    /**
     * 
     */
    public void newInternalMinute()
    {
	    //NCE clock is running
	    if (lastClockReadPacket.getElement(CS_CLOCK_STATUS) == 0) {
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
            timerDisplayUpdate = null;
        }
        super.windowClosing(e);
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ClockMonFrame.class.getName());

}
