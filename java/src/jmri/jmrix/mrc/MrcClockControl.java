package jmri.jmrix.mrc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.text.DecimalFormat;
import java.util.Date;
import jmri.InstanceManager;
import jmri.Timebase;
import jmri.implementation.DefaultClockControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MrcClockControl.java
 *
 * Implementation of the Hardware Fast Clock for Mrc
 * <p>
 * This module is based on the NCE version.
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
 * @author Ken Cameron Copyright (C) 2014
 * @author Dave Duchamp Copyright (C) 2007
 * @author Bob Jacobsen, Alex Shepherd
 */
public class MrcClockControl extends DefaultClockControl implements MrcTrafficListener {

    /**
     * Create a ClockControl object for a Mrc clock
     * @param tc traffic control for connection
     * @param prefix system prefix for connection
     */
    public MrcClockControl(MrcTrafficController tc, String prefix) {
        super();
        this.tc = tc;
        this.prefix = prefix;

        // Create a timebase listener for the Minute change events
        internalClock = InstanceManager.getNullableDefault(jmri.Timebase.class);
        if (internalClock == null) {
            log.error("No Internal Timebase Instance"); //IN18N
            return;
        }
        minuteChangeListener = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                newInternalMinute();
            }
        };

        internalClock.addMinuteChangeListener(minuteChangeListener);
        tc.addTrafficListener(MrcInterface.CLOCK, this);
    }
    @SuppressWarnings("unused")
    private String prefix = "";
    private MrcTrafficController tc = null;

    /* constants, variables, etc */
    private static final boolean DEBUG_SHOW_PUBLIC_CALLS = true; // enable debug for each public interface
    private static final boolean DEBUG_SHOW_SYNC_CALLS = false; // enable debug for sync logic

    public static final int CS_CLOCK_SCALE = 0x00;
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
    public static final int SYNCMODE_MRC_MASTER = 1;  //1 - Mrc sets Internal
    public static final int SYNCMODE_INTERNAL_MASTER = 2; //2 - Internal sets Mrc
    public static final int WAIT_CMD_EXECUTION = 1000;

    DecimalFormat fiveDigits = new DecimalFormat("0.00000");
    DecimalFormat fourDigits = new DecimalFormat("0.0000");
    DecimalFormat threeDigits = new DecimalFormat("0.000");
    DecimalFormat twoDigits = new DecimalFormat("0.00");

    private int clockMode = SYNCMODE_OFF;
    private MrcMessage lastClockReadPacket = null;
    //private Date lastClockReadAtTime;
    private int mrcLastHour;
    private int mrcLastMinute;
    private int mrcLastRatio;
    private boolean mrcLastAmPm;
    private boolean mrcLast1224;

    private int mrcSyncInitStateCounter = 0; // MRC master sync initialization state machine
    private int mrcSyncRunStateCounter = 0; // MRC master sync runtime state machine

    Timebase internalClock;
    javax.swing.Timer alarmSyncUpdate = null;
    java.beans.PropertyChangeListener minuteChangeListener;

    //  Filter reply messages for the clock poll
    public void message(MrcMessage r) {
        if ((r.getMessageClass() & MrcInterface.CLOCK) != MrcInterface.CLOCK) {
            return;
        }
        if (r.getNumDataElements() != 6 || r.getElement(0) != 0 || r.getElement(1) != 1
                || r.getElement(3) != 0 || r.getElement(5) != 0) {
            // not a clock packet
            return;
        }
        log.debug("MrcReply(len {})", r.getNumDataElements()); //IN18N

        readClockPacket(r);

        return;
    }

    @Override
    public synchronized void notifyXmit(Date timestamp, MrcMessage m) {
    }

    @Override
    public synchronized void notifyFailedXmit(Date timestamp, MrcMessage m) {
    }

    @Override
    public synchronized void notifyRcv(Date timestamp, MrcMessage m) {
        message(m);
    }

    /**
     * name of Mrc clock
     */
    @Override
    public String getHardwareClockName() {
        if (DEBUG_SHOW_PUBLIC_CALLS) {
            log.debug("getHardwareClockName"); //IN18N
        }
        return (Bundle.getMessage("MrcClockName")); //IN18N
    }

    /**
     * Mrc clock runs stable enough
     */
    @Override
    public boolean canCorrectHardwareClock() {
        if (DEBUG_SHOW_PUBLIC_CALLS) {
            log.debug("getHardwareClockName"); //IN18N
        }
        return false;
    }

    /**
     * Mrc clock supports 12/24 operation
     */
    @Override
    public boolean canSet12Or24HourClock() {
        if (DEBUG_SHOW_PUBLIC_CALLS) {
            log.debug("canSet12Or24HourClock"); //IN18N
        }
        return true;
    }

    /**
     * sets Mrc clock speed, must be 1 to 60
     */
    @Override
    public void setRate(double newRate) {
        if (DEBUG_SHOW_PUBLIC_CALLS) {
            log.debug("setRate: " + newRate); //IN18N
        }
        int newRatio = (int) newRate;
        if (newRatio < 1 || newRatio > 60) {
            log.error("Mrc clock ratio out of range:"); //IN18N
        } else {
            issueClockRatio(newRatio);
        }
    }

    /**
     * Mrc only supports integer rates
     */
    @Override
    public boolean requiresIntegerRate() {
        if (DEBUG_SHOW_PUBLIC_CALLS) {
            log.debug("requiresIntegerRate"); //IN18N
        }
        return true;
    }

    /**
     * last known ratio from Mrc clock
     */
    @Override
    public double getRate() {
        if (DEBUG_SHOW_PUBLIC_CALLS) {
            log.debug("getRate: {}", mrcLastRatio); //IN18N
        }
        return (mrcLastRatio);
    }

    /**
     * set the time, the date part is ignored
     */
    @SuppressWarnings("deprecation")
    @Override
    public void setTime(Date now) {
        if (DEBUG_SHOW_PUBLIC_CALLS) {
            log.debug("setTime: {}", now); //IN18N
        }
        issueClockTime(now.getHours(), now.getMinutes());
    }

    /**
     * returns the current Mrc time, does not have a date component
     */
    @SuppressWarnings("deprecation")
    @Override
    public Date getTime() {
        Date now = internalClock.getTime();
        if (lastClockReadPacket != null) {
            if (mrcLast1224) { // is 24 hour mode
                now.setHours(mrcLastHour);
            } else {
                if (mrcLastAmPm) { // is AM
                    now.setHours(mrcLastHour);
                } else {
                    now.setHours(mrcLastHour + 12);
                }
            }
            now.setMinutes(mrcLastMinute);
            now.setSeconds(0);
        }
        if (DEBUG_SHOW_PUBLIC_CALLS) {
            log.debug("getTime returning: {}", now); //IN18N
        }
        return (now);
    }

    /**
     * set Mrc clock and start clock
     */
    @SuppressWarnings("deprecation")
    @Override
    public void startHardwareClock(Date now) {
        if (DEBUG_SHOW_PUBLIC_CALLS) {
            log.debug("startHardwareClock: {}", now); //IN18N
        }
        issueClockTime(now.getHours(), now.getMinutes());
    }

    @SuppressFBWarnings(value="FE_FLOATING_POINT_EQUALITY", justification="testing for any change from previous value")
    @Override
    public void initializeHardwareClock(double rate, Date now, boolean getTime) {
        // clockMode controls what we are doing: SYNCMODE_OFF, SYNCMODE_INTERNAL_MASTER, SYNCMODE_MRC_MASTER
        boolean synchronizeWithInternalClock = internalClock.getSynchronize();
        boolean correctFastClock = internalClock.getCorrectHardware();
        boolean setInternal = !internalClock.getInternalMaster();
        if (!setInternal && !synchronizeWithInternalClock && !correctFastClock) {
            // No request to interact with hardware fast clock - ignore call
            return;
        }
        int newRate = (int) rate;
        
        // next line is the FE_FLOATING_POINT_EQUALITY annotated above
        if (newRate != getRate()) {
            setRate(rate);
        }
        if (!getTime) {
            setTime(now);
        }
    }

    /**
     * stops any sync, removes listeners
     */
    public void dispose() {

        // Remove ourselves from the timebase minute rollover event
        if (minuteChangeListener != null) {
            internalClock.removeMinuteChangeListener(minuteChangeListener);
            minuteChangeListener = null;
        }
    }

    /**
     * Handles minute notifications for MRC Clock Monitor/Synchronizer
     */
    public void newInternalMinute() {
        if (DEBUG_SHOW_SYNC_CALLS) {
            log.debug("newInternalMinute clockMode: {} mrcInit: {} mrcRun: {}",
                    clockMode, mrcSyncInitStateCounter, mrcSyncRunStateCounter); //IN18N
        }
        // if sync and Internal is master
        // clockMode - SYNCMODE_OFF, SYNCMODE_INTERNAL_MASTER, SYNCMODE_MRC_MASTER
        if (clockMode == SYNCMODE_INTERNAL_MASTER) {
            Date now = internalClock.getTime();
            setTime(now);
        }
    }

    @SuppressWarnings("deprecation")
    private void readClockPacket(MrcMessage r) {
        lastClockReadPacket = r;
        mrcLastHour = r.getElement(2) & 0x1F;
        mrcLastMinute = r.getElement(4) & 0xFF;
        if ((r.getElement(2) & 0xC0) == 0x80) {
            mrcLast1224 = true;
        } else {
            mrcLast1224 = false;
        }
        if ((r.getElement(2) & 0xC0) == 0x0) {
            mrcLastAmPm = true;
        } else {
            mrcLastAmPm = false;
        }
        Date now = internalClock.getTime();
        if (mrcLast1224) { // is 24 hour mode
            now.setHours(mrcLastHour);
        } else {
            if (mrcLastAmPm) { // is AM
                now.setHours(mrcLastHour);
            } else {
                now.setHours(mrcLastHour + 12);
            }
        }
        now.setMinutes(mrcLastMinute);
        now.setSeconds(0);
        if (clockMode == SYNCMODE_MRC_MASTER) {
            internalClock.userSetTime(now);
        }
    }

    private void issueClockRatio(int r) {
        log.debug("sending ratio " + r + " to mrc cmd station"); //IN18N
        MrcMessage cmdMrc = jmri.jmrix.mrc.MrcMessage.setClockRatio(r);
        tc.sendMrcMessage(cmdMrc);
    }

    private void issueClockTime(int hh, int mm) {
        MrcMessage cmdMrc = jmri.jmrix.mrc.MrcMessage.setClockTime(hh, mm);
        tc.sendMrcMessage(cmdMrc);
    }

    private final static Logger log = LoggerFactory.getLogger(MrcClockControl.class);
}
