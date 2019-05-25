package jmri.jmrix.nce;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.Timebase;
import jmri.implementation.DefaultClockControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Hardware Fast Clock for NCE.
 * <p>
 * This module is based on the LocoNet version as worked over by David Duchamp
 * based on original work by Bob Jacobsen and Alex Shepherd. It implements the
 * sync logic to keep the Nce clock in sync with the internal clock or keeps the
 * internal in sync to the Nce clock. The following of the Nce clock is better
 * than the other way around due to the fine tuning available on the internal
 * clock while the Nce clock doesn't.
 * <br>
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Ken Cameron Copyright (C) 2007
 * @author Dave Duchamp Copyright (C) 2007
 * @author Bob Jacobsen, Alex Shepherd
 */
public class NceClockControl extends DefaultClockControl implements NceListener {

    /**
     * Create a ClockControl object for a NCE clock.
     *
     * @param tc traffic controller for connection
     * @param prefix system connection prefix
     */
    public NceClockControl(NceTrafficController tc, String prefix) {
        super();
        this.tc = tc;
        this.prefix = prefix;

        // Create a timebase listener for the Minute change events
        internalClock = InstanceManager.getNullableDefault(jmri.Timebase.class);
        if (internalClock == null) {
            log.error("No Timebase Instance");
            return;
        }
        minuteChangeListener = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                newInternalMinute();
            }
        };
        internalClock.addMinuteChangeListener(minuteChangeListener);
    }
    @SuppressWarnings("unused")
    private String prefix = "";
    private NceTrafficController tc = null;

    /* constants, variables, etc */
    private static final boolean DEBUG_SHOW_PUBLIC_CALLS = true; // enable debug for each public interface
    private static final boolean DEBUG_SHOW_SYNC_CALLS = false; // enable debug for sync logic

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
    public static final double TARGET_SYNC_DELAY = 55;
    public static final int SYNCMODE_OFF = 0;    //0 - clocks independent
    public static final int SYNCMODE_NCE_MASTER = 1;  //1 - NCE sets Internal
    public static final int SYNCMODE_INTERNAL_MASTER = 2; //2 - Internal sets NCE
    public static final int WAIT_CMD_EXECUTION = 1000;

    DecimalFormat fiveDigits = new DecimalFormat("0.00000");
    DecimalFormat fourDigits = new DecimalFormat("0.0000");
    DecimalFormat threeDigits = new DecimalFormat("0.000");
    DecimalFormat twoDigits = new DecimalFormat("0.00");

    private int waiting = 0;
    private final int clockMode = SYNCMODE_OFF;
    private boolean waitingForCmdRead = false;
    private boolean waitingForCmdStop = false;
    private boolean waitingForCmdStart = false;
    private boolean waitingForCmdRatio = false;
    private boolean waitingForCmdTime = false;
    private boolean waitingForCmd1224 = false;
    private NceReply lastClockReadPacket = null;
    //private Date lastClockReadAtTime;
    private int nceLastHour;
    private int nceLastMinute;
    private int nceLastSecond;
    private int nceLastRatio;
    private boolean nceLastAmPm;
    private boolean nceLast1224;
    //private boolean nceLastRunning;
    //private double internalLastRatio;
    //private boolean internalLastRunning;
    //private double syncInterval = TARGET_SYNC_DELAY;
    //private int internalSyncInitStateCounter = 0;
    //private int internalSyncRunStateCounter = 0;
    private boolean issueDeferredGetTime = false;
    //private boolean issueDeferredGetRate = false;
    //private boolean initNeverCalledBefore = true;

    private final int nceSyncInitStateCounter = 0; // NCE master sync initialzation state machine
    private final int nceSyncRunStateCounter = 0; // NCE master sync runtime state machine
    //private int alarmDisplayStateCounter = 0; // manages the display update from the alarm

    Timebase internalClock;
    javax.swing.Timer alarmSyncUpdate = null;
    java.beans.PropertyChangeListener minuteChangeListener;

    //  ignore replies
    @Override
    public void message(NceMessage m) {
        log.error("message received: " + m);
    }

    // TODO: Why does this if statement contain a direct false? FIXME!
    @Override
    public void reply(NceReply r) {
        if (false && log.isDebugEnabled()) {
            log.debug("NceReply(len " + r.getNumDataElements() + ") waiting: " + waiting
                    + " watingForRead: " + waitingForCmdRead
                    + " waitingForCmdTime: " + waitingForCmdTime
                    + " waitingForCmd1224: " + waitingForCmd1224
                    + " waitingForCmdRatio: " + waitingForCmdRatio
                    + " waitingForCmdStop: " + waitingForCmdStop
                    + " waitingForCmdStart: " + waitingForCmdStart
            );

        }
        if (waiting <= 0) {
            log.error(Bundle.getMessage("LogReplyEnexpected"));
            return;
        }
        waiting--;
        if (waitingForCmdRead && r.getNumDataElements() == CS_CLOCK_MEM_SIZE) {
            readClockPacket(r);
            waitingForCmdRead = false;
            return;
        }
        if (waitingForCmdTime) {
            if (r.getNumDataElements() != CMD_CLOCK_SET_REPLY_SIZE) {
                log.error(Bundle.getMessage("LogNceClockReplySizeError") + r.getNumDataElements());
                return;
            } else {
                waitingForCmdTime = false;
                if (r.getElement(0) != '!') {
                    log.error("NCE set clock replied: " + r.getElement(0));
                }
                return;
            }
        }
        if (r.getNumDataElements() != CMD_CLOCK_SET_REPLY_SIZE) {
            log.error(Bundle.getMessage("LogNceClockReplySizeError") + r.getNumDataElements());
            return;
        } else {
            if (waitingForCmd1224) {
                waitingForCmd1224 = false;
                if (r.getElement(0) != '!') {
                    log.error(Bundle.getMessage("LogNceClock1224CmdError") + r.getElement(0));
                }
                return;
            }
            if (waitingForCmdRatio) {
                waitingForCmdRatio = false;
                if (r.getElement(0) != '!') {
                    log.error(Bundle.getMessage("LogNceClockRatioCmdError") + r.getElement(0));
                }
                return;
            }
            if (waitingForCmdStop) {
                waitingForCmdStop = false;
                if (r.getElement(0) != '!') {
                    log.error(Bundle.getMessage("LogNceClockStopCmdError") + r.getElement(0));
                }
                return;
            }
            if (waitingForCmdStart) {
                waitingForCmdStart = false;
                if (r.getElement(0) != '!') {
                    log.error(Bundle.getMessage("LogNceClockStartCmdError") + r.getElement(0));
                }
                return;
            }
        }
        // unhandled reply, nothing to do about it
        if (log.isDebugEnabled()) {
            StringBuffer buf = new StringBuffer();
            buf.append("NceReply(len " + r.getNumDataElements() + ")");
            if (waiting > 0) {
                buf.append(" waiting: " + waiting);
            }
            if (waitingForCmdRead) {
                buf.append(" waitingForCmdRead: " + waitingForCmdRead);
            }
            if (waitingForCmdTime) {
                buf.append(" waitingForCmdTime: " + waitingForCmdTime);
            }
            if (waitingForCmd1224) {
                buf.append(" waitingForCmd1224: " + waitingForCmd1224);
            }
            if (waitingForCmdRatio) {
                buf.append(" waitingForCmdRatio: " + waitingForCmdRatio);
            }
            if (waitingForCmdStop) {
                buf.append(" waitingForCmdStop: " + waitingForCmdStop);
            }
            if (waitingForCmdStart) {
                buf.append(" waitingForCmdStart: " + waitingForCmdStart);
            }
            log.debug(buf.toString());
            buf = new StringBuffer();
            buf.append(Bundle.getMessage("LogReplyUnexpected") + ":");
            for (int i = 0; i < r.getNumDataElements(); i++) {
                buf.append(" " + r.getElement(i));
            }
            log.debug(buf.toString());
        }
        return;
    }

    /**
     * name of Nce clock
     */
    @Override
    public String getHardwareClockName() {
        if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()) {
            log.debug("getHardwareClockName");
        }
        return ("Nce Fast Clock");
    }

    /**
     * Nce clock runs stable enough
     */
    @Override
    public boolean canCorrectHardwareClock() {
        if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()) {
            log.debug("getHardwareClockName");
        }
        return false;
    }

    /**
     * Nce clock supports 12/24 operation
     */
    @Override
    public boolean canSet12Or24HourClock() {
        if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()) {
            log.debug("canSet12Or24HourClock");
        }
        return true;
    }

    /**
     * Set Nce clock speed, must be 1 to 15.
     */
    @Override
    public void setRate(double newRate) {
        if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()) {
            log.debug("setRate: " + newRate);
        }
        int newRatio = (int) newRate;
        if (newRatio < 1 || newRatio > 15) {
            log.error(Bundle.getMessage("LogNceClockRatioRangeError"));
        } else {
            issueClockRatio(newRatio);
        }
    }

    /**
     * NCE only supports integer rates.
     */
    @Override
    public boolean requiresIntegerRate() {
        if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()) {
            log.debug("requiresIntegerRate");
        }
        return true;
    }

    /**
     * Get last known ratio from Nce clock.
     */
    @Override
    public double getRate() {
        issueReadOnlyRequest(); // get the current rate
        //issueDeferredGetRate = true;
        if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()) {
            log.debug("getRate: " + nceLastRatio);
        }
        return (nceLastRatio);
    }

    /**
     * Set the time, the date part is ignored.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void setTime(Date now) {
        if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()) {
            log.debug("setTime: " + now);
        }
        issueClockSet(now.getHours(), now.getMinutes(), now.getSeconds());
    }

    /**
     * Get the current Nce time, does not have a date component.
     */
    @SuppressWarnings("deprecation")
    @Override
    public Date getTime() {
        issueReadOnlyRequest(); // go get the current time value
        issueDeferredGetTime = true;
        Date now = internalClock.getTime();
        if (lastClockReadPacket != null) {
            if (nceLast1224) { // is 24 hour mode
                now.setHours(nceLastHour);
            } else {
                if (nceLastAmPm) { // is AM
                    now.setHours(nceLastHour);
                } else {
                    now.setHours(nceLastHour + 12);
                }
            }
            now.setMinutes(nceLastMinute);
            now.setSeconds(nceLastSecond);
        }
        if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()) {
            log.debug("getTime returning: " + now);
        }
        return (now);
    }

    /**
     * Set Nce clock and start clock.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void startHardwareClock(Date now) {
        if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()) {
            log.debug("startHardwareClock: " + now);
        }
        issueClockSet(now.getHours(), now.getMinutes(), now.getSeconds());
        issueClockStart();
    }

    /**
     * Stop the Nce Clock.
     */
    @Override
    public void stopHardwareClock() {
        if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()) {
            log.debug("stopHardwareClock");
        }
        issueClockStop();
    }

    /**
     * not sure when or if this gets called, but will issue a read to get latest
     * time
     */
    public void initiateRead() {
        if (DEBUG_SHOW_PUBLIC_CALLS && log.isDebugEnabled()) {
            log.debug("initiateRead");
        }
        issueReadOnlyRequest();
    }

    /**
     * Stop any sync, removes listeners.
     */
    public void dispose() {

        // Remove ourselves from the timebase minute rollover event
        if (minuteChangeListener != null) {
            internalClock.removeMinuteChangeListener(minuteChangeListener);
            minuteChangeListener = null;
        }
    }

    /**
     * Handles minute notifications for NCE Clock Monitor/Synchronizer
     */
    public void newInternalMinute() {
        if (DEBUG_SHOW_SYNC_CALLS) {
            if (log.isDebugEnabled()) {
                log.debug("newInternalMinute clockMode: " + clockMode + " nceInit: " + nceSyncInitStateCounter + " nceRun: " + nceSyncRunStateCounter);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void readClockPacket(NceReply r) {
        //NceReply priorClockReadPacket = lastClockReadPacket;
        //int priorNceRatio = nceLastRatio;
        //boolean priorNceRunning = nceLastRunning;
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
        if (issueDeferredGetTime) {
            issueDeferredGetTime = false;
            Date now = internalClock.getTime();
            if (nceLast1224) { // is 24 hour mode
                now.setHours(nceLastHour);
            } else {
                if (nceLastAmPm) { // is AM
                    now.setHours(nceLastHour);
                } else {
                    now.setHours(nceLastHour + 12);
                }
            }
            now.setMinutes(nceLastMinute);
            now.setSeconds(nceLastSecond);
            internalClock.userSetTime(now);
        }
        int sc = r.getElement(CS_CLOCK_SCALE) & 0xFF;
        if (sc > 0) {
            nceLastRatio = 250 / sc;
        }
    }

    private void issueClockRatio(int r) {
        log.debug("sending ratio " + r + " to nce cmd station");
        byte[] cmd = jmri.jmrix.nce.NceBinaryCommand.accSetClockRatio(r);
        NceMessage cmdNce = jmri.jmrix.nce.NceMessage.createBinaryMessage(tc, cmd, CMD_CLOCK_SET_REPLY_SIZE);
        waiting++;
        waitingForCmdRatio = true;
        tc.sendNceMessage(cmdNce, this);
    }

    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification="was previously marked with @SuppressWarnings, reason unknown")
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

    @SuppressWarnings({"deprecation", "unused"})
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification="was previously marked with @SuppressWarnings, reason unknown")
    private Date getNceDate() {
        Date now = internalClock.getTime();
        if (lastClockReadPacket != null) {
            now.setHours(lastClockReadPacket.getElement(CS_CLOCK_HOURS));
            now.setMinutes(lastClockReadPacket.getElement(CS_CLOCK_MINUTES));
            now.setSeconds(lastClockReadPacket.getElement(CS_CLOCK_SECONDS));
        }
        return (now);
    }

    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification="was previously marked with @SuppressWarnings, reason unknown")
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

    @SuppressWarnings({"deprecation", "unused"})
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification="was previously marked with @SuppressWarnings, reason unknown")
    private double getIntTime() {
        Date now = internalClock.getTime();
        int ms = (int) (now.getTime() % 1000);
        int ss = now.getSeconds();
        int mm = now.getMinutes();
        int hh = now.getHours();
        if (false && log.isDebugEnabled()) {
            log.debug("getIntTime: " + hh + ":" + mm + ":" + ss + "." + ms);
        }
        return ((hh * 60 * 60) + (mm * 60) + ss + (ms / 1000));
    }

    private final static Logger log = LoggerFactory.getLogger(NceClockControl.class);

}
