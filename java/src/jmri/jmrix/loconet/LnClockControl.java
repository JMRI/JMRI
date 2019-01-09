package jmri.jmrix.loconet;

import java.util.Date;
import jmri.PowerManager;
import jmri.implementation.DefaultClockControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Hardware Fast Clock for Loconet
 * <p>
 * This module is based on a GUI module developed by Bob Jacobsen and Alex
 * Shepherd to correct the Loconet fast clock rate and synchronize it with the
 * internal JMRI fast clock Timebase. The methods that actually send, correct,
 * or receive information from the Loconet hardware are repackaged versions of
 * their code.
 * <p>
 * The Loconet Fast Clock is controlled by the user via the Fast Clock Setup GUI
 * that is accessed from the JMRI Tools menu.
 * <p>
 * For this implementation, "internal clock" and "synchronize" means we act as a LocoNet master/server",
 * "internal clock" and "correct" means we correct the CS or other master device to the internal clock.
 * "internal clock" and neither "synchronize" nor "correct" and we just use the internal clock and ignore the LocoNet.
 * "LocoNet clock" means we accept the LocoNet CS or other LocoNet master device clock as correct.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
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
 * @author Dave Duchamp Copyright (C) 2007
 * @author Bob Jacobsen, Alex Shepherd
 */
public class LnClockControl extends DefaultClockControl implements SlotListener, LocoNetListener {


    /**
     * Create a ClockControl object for a Loconet clock
     * @param scm - connection memo
     */
    public LnClockControl(LocoNetSystemConnectionMemo scm) {
        this(scm.getSlotManager(), scm.getLnTrafficController(), scm.getPowerManager());
    }

    /*
     * Create a ClockControl object for a Loconet clock
     * @deprecated 4.11.5
     */
    @Deprecated // 4.11.5
    public LnClockControl(SlotManager sm, LnTrafficController tc) {
        this(sm, tc, null);
    }

    /**
     * Create a ClockControl object for a Loconet clock
     * @param sm slotmanager
     * @param tc traffic controller
     * @param pm powermanager
     */
    public LnClockControl(SlotManager sm, LnTrafficController tc, LnPowerManager pm) {
        super();

        this.sm = sm;
        this.tc = tc;
        this.pm = pm;

        tc.addLocoNetListener( ~0, this);

        // listen for updated slot contents
        if (sm != null) {
            sm.addSlotListener(this);
        } else {
            log.error("No LocoNet connection available, LnClockControl can't function");
        }

        // Get internal timebase
        clock = jmri.InstanceManager.getDefault(jmri.Timebase.class);
        // Create a Time base listener for Minute change events from the internal clock
        minuteChangeListener = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                newMinute();
            }
        };
        clock.addMinuteChangeListener(minuteChangeListener);
    }

    /**
     * True, we are trying to find the minimum valid value of fracMin.
     * If false will terminate thread, so must be volatile
     */
    private volatile boolean waitingForCommandStationClockSync = false;
    private boolean found7FCommandStationClockSync = false;
    private int commandStationFracType =1;
    private int prevHiFrac = 0;
    private int prevLoFrac = 0;
    private int newCommandStationZero = 0x8000;

    @Override
    public void message(LocoNetMessage msg) {
        if (waitingForCommandStationClockSync) {
            // get the FracHigh Byte
            if ((msg.getOpCode() == LnConstants.OPC_SL_RD_DATA ) &&
                    msg.getElement(1) == 0x0E &&
                    msg.getElement(2) == 0x7B) {
                // minute roll detection
                if (found7FCommandStationClockSync && msg.getElement(5) != 0x7F) {
                    // dont convert to type 1 format here, its done when moving
                    // newCommandStationZero to official value.
                    int temp = ( msg.getElement(5) * 256 ) + msg.getElement(4);
                    if (temp < newCommandStationZero) {
                        newCommandStationZero = temp;
                        log.debug("sync fracMin [{}]", newCommandStationZero);
                    }
                } else if( msg.getElement(5) == 0x7F ) {
                    log.debug("Found 7F");
                    found7FCommandStationClockSync = true;
                }
                if (commandStationFracType == 1 && prevHiFrac == msg.getElement(5) && prevLoFrac > msg.getElement(4) ) {
                    log.debug("Found CS Type 2");
                    commandStationFracType = 2;
                }
                prevHiFrac = msg.getElement(5);
                prevLoFrac = msg.getElement(4);
            }
            return;
        }
        if (useInternal && synchronizeWithInternalClock) {
            // we are master, respond..
            // is it a time request or tetherless (sic) query, yes: respond to time request reply.
            if (msg.getOpCode() == LnConstants.OPC_RQ_SL_DATA &&
                    msg.getElement(1) == 0x7B &&
                    msg.getElement(2) == 0x00) {
                log.debug("Replying to FC slot read");
                sendClockMsg(false, minuteFracType.MINUTE_NORMAL, false);
            } else if (msg.getOpCode() == LnConstants.OPC_PANEL_QUERY && msg.getElement(1) == 0x00) {
                log.debug("Replying to Panel Query");
                sendClockMsg(false, minuteFracType.MINUTE_NORMAL, false);
            }
        }
    }

    final SlotManager sm;
    final LnTrafficController tc;
    final LnPowerManager pm;

    /**
     * The throttle ID used for setting the clock and broadcasting LnClockControl time packets
     */
    private final int clockThrottleId = 0x01CC;

    /**
     * Initialized to commandStationEndMinute. Indicates that
     * the Command Station fast clock has not be calibrated.
     * Once calibrated it holds the value of the minFrac
     * minute = zero.
     */
    private int commandStationZeroSecond =  0x8000;
    private int commandStationEndMinute = 0x8000;
    private final int commandStationEndMinuteType1 = 0x4000;
    private final int commandStationEndMinuteType2 = 0x8000;

    /* Operational variables */
    jmri.Timebase clock = null;
    java.beans.PropertyChangeListener minuteChangeListener = null;

    /* current values of clock variables */
    private int curDays = 0;
    private int curHours = 0;
    private int curMinutes = 0;
    private int curFractionalMinutes = 900;
    private int curRate = 1;
    private int savedRate = 1;

    /**
     * When less that 1, we are on a whole real minute.
     */
    private int fastClockCounter = -1;

    /* current options and flags */
    /**
     *  true if our PC clock is our definitive fast clock
     *  false LocoNet fast clock is definitive fast clock (we are client)
     */
    private boolean useInternal = true;
    /**
     * If useInternal True and synchronizeWithInternalClock true we are locoNet master
     * Ignore if useInternal is false
     */
    private boolean synchronizeWithInternalClock = false;
    /**
     * if useInternal is True and correctFastClock is true
     *      we update CS fast clock with internal clock time every fast clock minute.
     * Ignore if useInternal is false or useInternal True and synchronizeWithInternalClock true.
     */
    private boolean correctFastClock = false;

    private boolean timebaseErrorReported = false;

    /**
     * Number of milli seconds per hour
     */
    final static long MSECPERHOUR = 3600000;
    /**
     * Number of milli seconds per minute
     */
    final static long MSECPERMINUTE = 60000;

    /**
     * Describes the type of minute fraction required
     *
     */
    private enum  minuteFracType {
        MINUTE_START,
        MINUTE_NORMAL,
        MINUTE_END
    };

    /**
     * Accessor routines
     */
    @Override
    public String getHardwareClockName() {
        return (Bundle.getMessage("LocoNetFastClockName"));
    }

    @Override
    public boolean canCorrectHardwareClock() {
        return true;
    }

    @Override
    public void setRate(double newRate) {
        log.debug("Set New Rate[{}]",newRate);
        if (curRate == 0) {
            savedRate = (int) newRate;      // clock stopped case
        } else {
            curRate = (int) newRate;        // clock running case
            savedRate = curRate;
        }
        setClock();
        if (useInternal && ( synchronizeWithInternalClock || correctFastClock)) {
            // next full minute braodcast..
            fastClockCounter = -1;
        }
    }

    @Override
    public boolean requiresIntegerRate() {
        return true;
    }

    @Override
    public double getRate() {
        return curRate;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setTime(Date now) {
        log.debug("Set new Time [{}]",now);
        curDays = now.getDate();
        curHours = now.getHours();
        curMinutes = now.getMinutes();
        setClock();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Date getTime() {
        Date tem = clock.getTime();
        int cHours = tem.getHours();
        long cNumMSec = tem.getTime();
        long nNumMSec = ((cNumMSec / MSECPERHOUR) * MSECPERHOUR) - (cHours * MSECPERHOUR)
                + (curHours * MSECPERHOUR) + (curMinutes * MSECPERMINUTE);
        // Work out how far through the current fast minute we are
        // and add that on to the time.
        nNumMSec += (long) ( curFractionalMinutes - commandStationZeroSecond ) / (commandStationEndMinute - commandStationZeroSecond) * MSECPERMINUTE;
        return (new Date(nNumMSec));
    }

    @Override
    public void startHardwareClock(Date now) {
        log.debug("Start Clock");
        curRate = savedRate;
        if (useInternal && correctFastClock && !synchronizeWithInternalClock || !useInternal ) {
            setTime(now);
        }
    }

    @Override
    public void stopHardwareClock() {
        log.debug("Stop Clock");
        savedRate = curRate;
        curRate = 0;
        setClock();
    }


    @SuppressWarnings("deprecation")
    @Override
    public void initializeHardwareClock(double rate, Date now, boolean getTime) {
        synchronizeWithInternalClock = clock.getSynchronize();
        correctFastClock = clock.getCorrectHardware();
        useInternal = clock.getInternalMaster();
        log.debug("useInternal[{}",useInternal);
        if (useInternal && !synchronizeWithInternalClock && !correctFastClock) {
            // No request to interact with hardware fast clock - ignore call
            log.debug("Setup as straight internal");
            return;
        }
        log.debug("Setup data Rate[{}], Date[{}], getTime[{}]", rate, now, getTime);
        if (rate == 0.0) {
            if (curRate != 0) {
                savedRate = curRate;
            }
            curRate = 0;
        } else {
            savedRate = (int) rate;
            if (curRate != 0) {
                curRate = savedRate;
            }
        }
        curDays = now.getDate();
        curHours = now.getHours();
        curMinutes = now.getMinutes();

        if (!getTime) {
            setTime(now);
        }

        calibrateCommandStationClock();

        // force a correction at next fast minute after calibration
        fastClockCounter = -1;
    }

    /**
     *
     * Send a series of fast clock reads to establish the roll over
     * from the last increment or the minute to xx
     *
     */
    private void calibrateCommandStationClock() {
        // ensure old thread dead.
        waitingForCommandStationClockSync = false;
        try {
            Thread.sleep(1000);
        } catch (Exception Ex) {
            return;
        }
        sendClockMsg(true, minuteFracType.MINUTE_END, true);
        newCommandStationZero = 0x7FFF; // force big,type 2. we need the min.
        // start new thread.
        waitingForCommandStationClockSync = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int everyMilli = 250;
                int limit = 5000 / everyMilli;
                for (int i = 0; i < limit; i++) {
                    if (!waitingForCommandStationClockSync) {
                        return;
                    }
                    try { Thread.sleep(everyMilli); }
                    catch (Exception Ex) {
                        // we are killed, die.
                        waitingForCommandStationClockSync =false;
                        return;
                    }
                    initiateRead();
                }
                waitingForCommandStationClockSync = false;
            }
        }).start();
    }

    /**
     * Requests read of the Loconet fast clock
     */
    public void initiateRead() {
        sm.sendReadSlot(LnConstants.FC_SLOT);
    }

    /**
     * Performs all necessary task for a new fast clock minute
     * Skip this if we are calibrating
     */
    public void newMinute() {
        if (waitingForCommandStationClockSync) {
            // dont mess with the syncing
            return;
        }
        if (newCommandStationZero < commandStationZeroSecond) {
            if (commandStationFracType == 1) {
                commandStationZeroSecond = newCommandStationZero & 0x7f00 >> 1 & newCommandStationZero & 0x7f;
            }
            commandStationZeroSecond = newCommandStationZero;
            // if synchronizing send immediate.
            fastClockCounter = -1;
        }
        if (useInternal && correctFastClock) {
            // we are not master, but want to correct the master
            fastClockCounter -= 1;
            if (fastClockCounter < 1) {
                log.debug("Send Write Master Time");
                sendClockMsg(true,  minuteFracType.MINUTE_START, false);
                fastClockCounter = curRate;
            }
        }
        if (useInternal && synchronizeWithInternalClock) {
            // We are LocoNet Master Send Heartbeat every real minute
            fastClockCounter -= 1;
            if (fastClockCounter < 1) {
                log.debug("Send Heartbeat/Master Blast");
                sendClockMsg(false, minuteFracType.MINUTE_START, false);
                fastClockCounter = curRate;
            }
        }
        if (!useInternal) {
            Date tem = clock.getTime();
            if (tem.getMinutes() == 0) {
                // if the expected new time is on the hour wait for next fast minute
                // as LocoNet returns wrong time
                log.debug("Skip 00 minutes request");
                return;
            }
            fastClockCounter -= 1;
            if (fastClockCounter < 1) {
                log.debug("Send Request Time");
                initiateRead();
                fastClockCounter = curRate;
            }

        }
    }

    boolean nextRequestIsUpdate = false;
    /**
     * Handle changed slot contents, due to clock changes. Can get here three
     * ways: 1) clock slot as a result of action by a throttle and 2) clock slot
     * responding to a read from this module 3) a slot not involving the clock
     * changing
     *
     */
    @SuppressWarnings("deprecation")
    @Override
    public void notifyChangedSlot(LocoNetSlot s) {
        // only watch the clock slot and ignore our own messages
        if (s.getSlot() != LnConstants.FC_SLOT || s.getThrottleIdentity() == clockThrottleId) {
            return;
        }
        // only pay attention if we are a LocoNet clock client
        // we currently do not allow our master parameters to be
        // updated over LocoNet
        //TODO: optionally allow update to clock over LocoNet
        if (useInternal) {
            log.debug("Ignore Slot Update setInternal[{}] synchronizeWithInternalClock[{}] ",useInternal,synchronizeWithInternalClock);
            return;
        }
        log.debug("FC slot update");
        // update current clock variables from the new slot contents
        curDays = s.getFcDays();
        curHours = s.getFcHours();
        curMinutes = s.getFcMinutes();
        if (curRate != s.getFcRate()) {
            try {
                clock.userSetRate(s.getFcRate());
                setRate(s.getFcRate());
            } catch (jmri.TimebaseRateException e) {
                if (!timebaseErrorReported) {
                    timebaseErrorReported = true;
                    log.warn("Time base exception on setting rate from LocoNet");
                }
            }
        }
        curFractionalMinutes = s.getFcFracMins();
        // we calculate a new msec value for a specific hour/minute
        // in the current day, then set that.
        Date tem = clock.getTime();
        int cHours = tem.getHours();
        long cNumMSec = tem.getTime();
        long nNumMSec = ((cNumMSec / MSECPERHOUR) * MSECPERHOUR) - (cHours * MSECPERHOUR)
                + (curHours * MSECPERHOUR) + (curMinutes * MSECPERMINUTE);
        // We are a  LocoNet Slave. Do not calculate or use Fast Minute Frac
        // as DTxxx throttles dont, and if we did we would be fast.
        clock.setTime(new Date(nNumMSec));

        // re-set timeout
        fastClockCounter = curRate;
    }

    /**
     * Push current Clock Control parameters out to LocoNet slot
     * if write time to master is needed.
     */
    private void setClock() {
        if (useInternal && !synchronizeWithInternalClock && !correctFastClock) {
            // pure internal, no change LocoNet
            return;
        }
        // we are not calibrating - send start of minute.
        sendClockMsg(true, minuteFracType.MINUTE_START, false);
    }

    /**
     * Convert milliseconds to minFrac
     * @param milliSecs time in milliseconds
     * @return the HI LO as an integer and adjusted.
     */
    private int convertMilliSecondsToFcFracMin(int milliSecs) {
        long fracmins;
        if (commandStationFracType == 1) {
            fracmins = (((commandStationEndMinuteType1 - commandStationZeroSecond) * milliSecs) / MSECPERMINUTE ) + commandStationZeroSecond ;
            // the completed calculation fits.
            return (int) ((fracmins & 0x7F00) / 2 +  (fracmins & 0x00F7));
        } else {
            fracmins = (((commandStationEndMinuteType2 - commandStationZeroSecond) * milliSecs) / MSECPERMINUTE ) + commandStationZeroSecond ;
            return (int) (fracmins & 0x7FFF);
        }
    }

    /**
     * Send a response FC Slot or a Write new FC Data data
     * @param sendWrite true - this a write slot message
     * @param minFracType , START, END or normal
     */
    private void sendClockMsg(boolean sendWrite, minuteFracType minFracType, boolean calibrate) {
        // set the time            // get time from the internal clock
        Date now = clock.getTime();
        curDays = now.getDate();
        // Update current time
        curHours = now.getHours();
        curMinutes = now.getMinutes();
        long millis = now.getTime();
        /* calculate curFractionalMinutes as required */
        int milliSeconds;
        //if we are calibrating or have not done so successfully, skip this
        if (!calibrate && commandStationZeroSecond != commandStationEndMinute) {
            switch (minFracType) {
                case MINUTE_START:
                    milliSeconds = 0;
                    curFractionalMinutes = commandStationZeroSecond;
                    break;
                case MINUTE_NORMAL:
                    milliSeconds = (int) (millis % MSECPERMINUTE);
                    break;
                case MINUTE_END:
                default:
                    milliSeconds = 59000;
                    break;
            }
            curFractionalMinutes = convertMilliSecondsToFcFracMin(milliSeconds);
        } else {
            // set to near end of minute.
            curFractionalMinutes = 0x7f4f;
        }

        /* Build the base slot, and then modify for specific need */
        LocoNetSlot s = sm.slot(LnConstants.FC_SLOT);

        // load time
        s.setFcDays(curDays);
        s.setFcHours(curHours);
        if (calibrate) {
            // set back so we don't move forward afterwards
            s.setFcMinutes(curMinutes-1);
            s.setFcRate(1);
            s.setFcFracMins(curFractionalMinutes);
        } else {
            s.setFcMinutes(curMinutes);
            s.setFcRate(curRate);
            s.setFcFracMins(curFractionalMinutes);
        }
        s.setFcFracMins(curFractionalMinutes);
        s.setThrottleIdentity(clockThrottleId);
        s.setFcCntrlBitOn(LnConstants.FC_VALID);
        // set other content
        //     power (GTRK_POWER, 0x01 bit in byte 7)
        boolean power = true;
        if (pm != null) {
            power = (pm.getPower() == PowerManager.ON);
        } else {
            jmri.util.Log4JUtil.warnOnce(log, "Can't access power manager for fast clock");
        }
        s.setTrackStatus(s.getTrackStatus() & (~LnConstants.GTRK_POWER));
        if (power) {
            s.setTrackStatus(s.getTrackStatus() | LnConstants.GTRK_POWER);
        }

        // and get the message
        LocoNetMessage msg = s.writeSlot();

        // change to send write if needed.
        if (!sendWrite) {
            msg.setOpCode(LnConstants.OPC_SL_RD_DATA);
        }
        tc.sendLocoNetMessage(msg);
    }

    public void dispose() {
        // Drop LocoNet connection
        if (sm != null) {
            sm.removeSlotListener(this);
        }

        // Remove ourselves from the Timebase minute rollover event
        if (minuteChangeListener != null) {
            clock.removeMinuteChangeListener(minuteChangeListener);
            minuteChangeListener = null;
        }
        // force waiting for sync thread stopped
        waitingForCommandStationClockSync = false;
    }

    private final static Logger log = LoggerFactory.getLogger(LnClockControl.class);

}

