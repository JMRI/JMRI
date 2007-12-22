// LnClockControl.java

package jmri.jmrix.nce;

import jmri.DefaultClockControl;
import jmri.InstanceManager;
import jmri.Timebase;
import jmri.TimebaseRateException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.awt.event.*;

/**
 * NceClockControl.java
 *
 * Implementation of the Hardware Fast Clock for NCE
 * <P>
 * This module is based on the LocoNet version as worked over by David Duchamp
 * based on original work by Bob Jacobsen and Alex Shepherd.
 * It implements the sync logic to keep the Nce clock in sync with the internal
 * clock or keeps the internal in sync to the Nce clock.
 * The following of the Nce clock is better than the other way around due to
 * the fine tuning availble to on the internal clock while the Nce clock doesn't.
 * <P>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author      Ken Cameron Copyright (C) 2007
 * @author      Dave Duchamp Copyright (C) 2007
 * @author		Bob Jacobsen, Alex Shepherd
 * @version     $Revision: 1.5 $
 */
public class NceClockControl extends DefaultClockControl implements NceListener
{
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.nce.NceClockControlBundle");

    /**
     * Create a ClockControl object for a Loconet clock
     */
    public NceClockControl() {
        super();

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
    }
	
    /* constants, variables, etc */
    
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
    private NceReply lastClockReadPacket = null;;
    private Date lastClockReadAtTime;
    private int	nceLastHour;
    private int nceLastMinute;
    private int nceLastSecond;
    private int nceLastRatio;
    private boolean nceLastAmPm;
    private boolean nceLast1224;
    private boolean nceLastRunning;
    private double internalLastRatio;
    private boolean internalLastRunning;
    private ArrayList priorDiffs = new ArrayList();
    private ArrayList priorOffsetErrors = new ArrayList();
    private ArrayList priorCorrections = new ArrayList();
    private double syncInterval = TARGET_SYNC_DELAY;
    private int internalSyncInitStateCounter = 0;
    private int internalSyncRunStateCounter = 0;
    private double ncePidGainPv = 0.04;
    private double ncePidGainIv = 0.01;
    private double ncePidGainDv = 0.005;
    private double intPidGainPv = 0.02;
    private double intPidGainIv = 0.001;
    private double intPidGainDv = 0.01;
    
    private int nceSyncInitStateCounter = 0;	// NCE master sync initialzation state machine
    private int	nceSyncRunStateCounter = 0;	// NCE master sync runtime state machine
    private int	alarmDisplayStateCounter = 0;	// manages the display update from the alarm
    
    Timebase internalClock ;
    javax.swing.Timer alarmSyncUpdate = null;
    java.beans.PropertyChangeListener minuteChangeListener;

    //  ignore replies
    public void  message(NceMessage m) {
        log.error("clockmon message received: " + m);
    }  
    
    public void reply(NceReply r) {
    	if (false && log.isDebugEnabled()){
            log.debug("NceReply(len " + r.getNumDataElements() + ") waiting: " + waiting +
        		" watingForRead: " + waitingForCmdRead +
        		" waitingForCmdTime: " + waitingForCmdTime +
        		" waitingForCmd1224: " + waitingForCmd1224 +
        		" waitingForCmdRatio: " + waitingForCmdRatio +
        		" waitingForCmdStop: " + waitingForCmdStop +
        		" waitingForCmdStart: " + waitingForCmdStart
        	);
    		
    	}
        if (waiting <= 0) {
            log.error(rb.getString("LogReplyEnexpected"));
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
        log.error(rb.getString("LogReplyUnexpected"));
        return;
    }
    
    /** name of Nce clock */
	public String getHardwareClockName() {
		if (false && log.isDebugEnabled()){
			log.debug("getHardwareClockName");
		}
		return ("Nce Fast Clock");
	}
	
	/** Nce clock runs stable enough */
	public boolean canCorrectHardwareClock() {
		if (true && log.isDebugEnabled()){
			log.debug("getHardwareClockName");
		}
		return false;
	}

	/** Nce clock supports 12/24 operation */
	public boolean canSet12Or24HourClock() {
		if (true && log.isDebugEnabled()){
			log.debug("canSet12Or24HourClock");
		}
		return true;
	}
	
	/** sets Nce clock speed, must be 1 to 15 */
	public void setRate(double newRate) {
		if (log.isDebugEnabled()){
			log.debug("setRate: " + newRate);
		}
		int newRatio = (int)newRate;
		if (newRatio < 1 || newRatio > 15) {
			log.error(rb.getString("LogNceClockRatioRangeError"));
		} else {
        	issueClockRatio(newRatio);
        	changeSyncMode();
		}
	}
	
	/** Nce only supports integer rates */
	public boolean requiresIntegerRate() {
		if (log.isDebugEnabled()){
			log.debug("requiresIntegerRate");
		}
		return true;
	}
	
	/** last known ratio from Nce clock */
	public double getRate() {
		if (log.isDebugEnabled()){
			log.debug("getRate: " + nceLastRatio);
		}
		return((double)nceLastRatio);
	}
	
	/** set the time, the date part is ignored */
	public void setTime(Date now) {
		if (log.isDebugEnabled()){
			log.debug("setTime: " + now);
		}
		issueClockSet(now.getHours(), now.getMinutes(), now.getSeconds());
		changeSyncMode();
	}
//	
//	/** returns the current Nce time, does not have a date component */
//	public Date getTime() {
//        Date now = internalClock.getTime();
//        if (lastClockReadPacket != null) {
//            now.setHours(lastClockReadPacket.getElement(CS_CLOCK_HOURS));
//            now.setMinutes(lastClockReadPacket.getElement(CS_CLOCK_MINUTES));
//            now.setSeconds(lastClockReadPacket.getElement(CS_CLOCK_SECONDS));
//        }
//        if (log.isDebugEnabled()){
//        	log.debug("getTime returning: " + now);
//        }
//        return(now);
//	}
	
	/** set Nce clock and start clock */
	public void startHardwareClock(Date now) {
		if (log.isDebugEnabled()){
			log.debug("startHardwareClock: " + now);
		}
		if (!internalClock.getInternalMaster() && internalClock.getMasterName() == getHardwareClockName()){
			
		}
		issueClockSet(now.getHours(), now.getMinutes(), now.getSeconds());
		issueClockStart();
		changeSyncMode();
	}
	
	/** stops the Nce Clock */
	public void stopHardwareClock() {
		if (log.isDebugEnabled()){
			log.debug("stopHardwareClock");
		}
		issueClockStop();
	}
	
	/** not ksc */
	public void initializeHardwareClock(double rate, Date now, boolean getTime) {
		if (log.isDebugEnabled()){
			log.debug("initializeHardwareClock(" + rate + ", " + now + ", " + getTime + "}");
		}
		issueClockRatio((int)rate);
		issueClockSet(now.getHours(), now.getMinutes(), now.getSeconds());
		changeSyncMode();
	}
	
	/** not ksc */
	public void initiateRead() {
		if (log.isDebugEnabled()){
			log.debug("initiateRead");
		}
	}
	
	/** stops any sync, removes listeners */
    public void dispose() {

		// Remove ourselves from the Timebase minute rollover event
		if (minuteChangeListener != null) {
			internalClock.removeMinuteChangeListener( minuteChangeListener );
			minuteChangeListener = null ;
		}
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

    /** determines what to do about mode changes */
    private void changeSyncMode() {
        int oldMode = clockMode;
        if (false && log.isDebugEnabled()){
        	log.debug("pre changeSyncMode was: " + oldMode 
        			+ " intMaster: " + internalClock.getInternalMaster() 
        			+ " master: " + internalClock.getMasterName()
        			);
        }
        int newMode = SYNCMODE_OFF;
        if (internalClock.getSynchronize()) {
	        if (internalClock.getInternalMaster() == false && internalClock.getMasterName().equals(getHardwareClockName())) {
	            newMode = SYNCMODE_NCE_MASTER;
	        }
	        if (internalClock.getInternalMaster() == true) {
	            newMode = SYNCMODE_INTERNAL_MASTER;
	        }
        }
        if (internalClock != null) {
            if (false && log.isDebugEnabled()) {
                log.debug("post changeSyncMode(): New Mode: " + newMode + " Old Mode: " + oldMode);
            }
            if (oldMode != newMode) {
                clockMode = SYNCMODE_OFF;
                // some change so, change settings
	            if (oldMode == SYNCMODE_NCE_MASTER) {
	                // clear nce sync
	            	log.debug("stopping Nce master sync");
	                nceSyncInitStateCounter = -1;
	                nceSyncInitStates();
	                internalSyncInitStateCounter = 1;
	                internalSyncInitStates();
	            }
	            if (oldMode == SYNCMODE_INTERNAL_MASTER) {
	                // clear internal mode
	            	log.debug("stopping Internal master sync");
	                internalSyncInitStateCounter = -1;
	                internalSyncInitStates();
	                nceSyncInitStateCounter = 1;
	                nceSyncInitStates();
	            }
                // now we've stopped anything running, see what to start
                if (newMode == SYNCMODE_INTERNAL_MASTER) {
                	log.debug("starting Internal master mode");
                    internalSyncInitStateCounter = 1;
                    internalSyncRunStateCounter = 0;
                    internalSyncInitStates();
                    clockMode = SYNCMODE_INTERNAL_MASTER;
                }
                if (newMode == SYNCMODE_NCE_MASTER) {
                	log.debug("starting NCE master mode");
                    nceSyncInitStateCounter = 1;
                    nceSyncRunStateCounter = 0;
                    nceSyncInitStates();
                    clockMode = SYNCMODE_NCE_MASTER;
                }
            }
        }
    }
    
    private void alarmSyncStart(){
        // initialize things if not running
        Date now = internalClock.getTime();
        if (alarmSyncUpdate == null){
            alarmSyncInit();
        }
        int delay = 60 * 1000;
        if (clockMode == SYNCMODE_INTERNAL_MASTER) {
            if (syncInterval - 3 - now.getSeconds() <= 0) {
                delay = (int)10;	// trigger right away
            } else {
                delay = (int)((syncInterval - now.getSeconds()) * 1000 / internalClock.getRate());
            }
        }
        if (clockMode == SYNCMODE_NCE_MASTER) {
            delay = (int) 10 * 1000;
        }
        alarmSyncUpdate.setDelay(delay);
        alarmSyncUpdate.setInitialDelay(delay);
        alarmSyncUpdate.start();
        if (false && log.isDebugEnabled()) {
            log.debug("alarmSyncStart delay: " + delay + " @ " + now);
        }
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
                delay = (int)(10 * 1000);
                alarmSyncUpdate.setRepeats(true);
            }
            alarmSyncUpdate.setInitialDelay(delay);
            alarmSyncUpdate.setDelay(delay);
            alarmSyncUpdate.stop();
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
    }

    private void internalSyncInitStates() {
        Date now = internalClock.getTime();
        int priorState = internalSyncInitStateCounter;
        do {
            if (false && log.isDebugEnabled() && internalSyncInitStateCounter != 0){
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
	            alarmSyncStart();
	            internalSyncInitStateCounter++;
	            break;
	        case 14:
	            // initialization complete
	        	internalSyncInitStateCounter = 0;
	            internalSyncRunStateCounter = 1;
	            if (false && log.isDebugEnabled()){
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

    private void internalSyncRunStates() {
        double intTime = 0;
        double nceTime = 0;
        double diffTime = 0;
        Date now = internalClock.getTime();
        if (true && log.isDebugEnabled() && internalSyncRunStateCounter != 0){
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
	            if (false && log.isDebugEnabled()) {
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
	            if (false && log.isDebugEnabled()) {
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
    
    private void readClockPacket (NceReply r) {
    	NceReply priorClockReadPacket = lastClockReadPacket;
    	int priorNceRatio = nceLastRatio;
    	boolean priorNceRunning = nceLastRunning;
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
        if (clockMode == SYNCMODE_NCE_MASTER) {
        	if (priorClockReadPacket != null && priorNceRatio != nceLastRatio) {
        		if (log.isDebugEnabled()){
        			log.debug("NCE Change Rate from cab: prior vs last: " + priorNceRatio + " vs " + nceLastRatio);
        		}
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

    private void issueReadOnlyRequest() {
        if (!waitingForCmdRead){
            byte [] cmd = jmri.jmrix.nce.NceBinaryCommand.accMemoryRead(CS_CLOCK_MEM_ADDR);
            NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(cmd, CS_CLOCK_MEM_SIZE);
            waiting++;
            waitingForCmdRead = true;
            jmri.jmrix.nce.NceTrafficController.instance().sendNceMessage(cmdNce, this);
            //			log.debug("issueReadOnlyRequest at " + internalClock.getTime());
        }
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
   
    private void recomputeOffset() {
        Date now = internalClock.getTime();
        double sumDiff = 0;
        if (priorOffsetErrors.size() > 1) {
            sumDiff = ((Double) priorOffsetErrors.get(0)).doubleValue() + ((Double) priorOffsetErrors.get(1)).doubleValue();
        }
        double avgDiff = sumDiff / 2;
        syncInterval = syncInterval + avgDiff;
        if (syncInterval < 30) {
        	syncInterval = 30;
        }
        if (syncInterval > 58) {
        	syncInterval = 58;
        }
        if (false && log.isDebugEnabled()) {
            String txt = "";
            for (int i = 0; i < priorOffsetErrors.size(); i++) {
                txt = txt + " " + ((Double)priorOffsetErrors.get(i)).doubleValue();
            }
            log.debug("priorOffsetErrors: " + txt);
            log.debug("syncOffset: " + syncInterval + " avgDiff: " + avgDiff + " @ " + now);
        }
    }
    
    private void recomputeInternalSync() {
        Date now = internalClock.getTime();
        double sumDiff = 0;
        double currError = 0;
        double diffError = 0;
        double avgDiff = 0;
        if (priorDiffs.size() > 0) {
            currError = ((Double) priorDiffs.get(priorDiffs.size() - 1)).doubleValue();
            diffError = ((Double) priorDiffs.get(priorDiffs.size() - 1)).doubleValue() - ((Double) priorDiffs.get(0)).doubleValue();
        }
        for (int i = 0; i < priorDiffs.size(); i++) {
            sumDiff = sumDiff + ((Double) priorDiffs.get(i)).doubleValue();
        }
        double corrDiff = 0;
        if (priorCorrections.size() > 0) {
            corrDiff = ((Double) priorCorrections.get(priorCorrections.size() - 1)).doubleValue() - ((Double) priorCorrections.get(0)).doubleValue();
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
        if (false && log.isDebugEnabled()) {
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
    }
    
    private void recomputeNceSync() {
        Date now = internalClock.getTime();
        double sumDiff = 0;
        double currError = 0;
        double diffError = 0;
        if (priorDiffs.size() > 0) {
            currError = ((Double) priorDiffs.get(priorDiffs.size() - 1)).doubleValue();
            diffError = ((Double) priorDiffs.get(priorDiffs.size() - 1)).doubleValue() - ((Double) priorDiffs.get(0)).doubleValue();
        }
        for (int i = 0; i < priorDiffs.size(); i++) {
            sumDiff = sumDiff + ((Double) priorDiffs.get(i)).doubleValue();
        }
        double corrDiff = 0;
        if (priorCorrections.size() > 0) {
            corrDiff = ((Double) priorCorrections.get(priorCorrections.size() - 1)).doubleValue() - ((Double) priorCorrections.get(0)).doubleValue();
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
        } else if (oldInternalRate != newInternalRate) {
            try {
                internalClock.setRate(newInternalRate);
                if (false && log.isDebugEnabled()){
                    log.debug("changing internal rate: " + newInternalRate);
                }
            } catch (TimebaseRateException e) {
                log.error("recomputeNceSync: Failed setting new internal rate: " + newInternalRate);
                // just set the internal to NCE and set the clock
                nceSyncInitStateCounter = 1;
                nceSyncInitStates();
            }
        }
        if (false && log.isDebugEnabled()) {
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
    }

    private void nceSyncInitStates() {
        int priorState = 0;
        do {
            if (false && log.isDebugEnabled()) {
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
	            break;
	        }
	        if (false && log.isDebugEnabled()) {
	            log.debug("After nceSyncInitStateCounter: " + nceSyncInitStateCounter + " " + internalClock.getTime());
	        }
        } while (priorState != nceSyncInitStateCounter);
    }
    private void nceSyncRunStates() {
        double intTime = 0;
        double nceTime = 0;
        double diffTime = 0;
        if (false && log.isDebugEnabled()) {
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
	            nceSyncRunStateCounter++;
	            break;
	        case 4:
	            // wait for next minute
	            nceSyncRunStateCounter = 0;
	        }
        } while (priorState != nceSyncRunStateCounter);
        if (false && log.isDebugEnabled()) {
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

    private Date getNceDate() {
        Date now = internalClock.getTime();
        if (lastClockReadPacket != null) {
            now.setHours(lastClockReadPacket.getElement(CS_CLOCK_HOURS));
            now.setMinutes(lastClockReadPacket.getElement(CS_CLOCK_MINUTES));
            now.setSeconds(lastClockReadPacket.getElement(CS_CLOCK_SECONDS));
        }
        return(now);
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

    private double getIntTime() {
        Date now = internalClock.getTime();
        int ms = (int)(now.getTime() % 1000);
        int ss = now.getSeconds();
        int mm = now.getMinutes();
        int hh = now.getHours();
        if (false && log.isDebugEnabled()) {
            log.debug("getIntTime: " + hh + ":" + mm + ":" + ss + "." + ms);
        }
        return((hh * 60 * 60) + (mm * 60) + ss + (ms / 1000));
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceClockControl.class.getName());
}

/* @(#)LnClockControl.java */
