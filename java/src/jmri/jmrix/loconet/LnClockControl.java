package jmri.jmrix.loconet;

import java.util.Date;
import jmri.JmriException;

import jmri.PowerManager;
import jmri.implementation.DefaultClockControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Hardware Fast Clock for LocoNet.
 * <p>
 * This module is based on a GUI module developed by Bob Jacobsen and Alex
 * Shepherd to correct the LocoNet fast clock rate and synchronize it with the
 * internal JMRI fast clock Timebase. The methods that actually send, correct,
 * or receive information from the LocoNet hardware are repackaged versions of
 * their code.
 * <p>
 * The LocoNet Fast Clock is controlled by the user via the Fast Clock Setup GUI
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
     * Create a ClockControl object for a LocoNet clock.
     *
     * @param scm  the LocoNet System Connection Memo to associate with this
     *              Clock Control object
     */
    public LnClockControl(LocoNetSystemConnectionMemo scm) {
        this(scm.getSlotManager(), scm.getLnTrafficController(), scm.getPowerManager());
    }

    /**
     * Create a ClockControl object for a LocoNet clock.
     *
     * @param sm the Slot Manager associated with this object
     * @param tc the Traffic Controller associated with this object
     * @param pm the PowerManager associated with this object
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

    final SlotManager sm;
    final LnTrafficController tc;
    final LnPowerManager pm;

    /**
     * The throttle ID used for setting the clock and broadcasting LnClockControl time packets
     */
    private final int clockThrottleId = 0x01CC;

    /* Operational variables */
    jmri.Timebase clock = null;
    java.beans.PropertyChangeListener minuteChangeListener = null;

    /* current values of clock variables */
    private int curDays = 0;
    private int curHours = 0;
    private int curMinutes = 0;
    private int curMilliSeconds = 0;
    private int curRate = 1;
    private int savedRate = 1;

    /**
     * When less that 1, we are on a whole real minute.
     */
    private int fastClockCounter = -1;

    /**
     * CommandStation timings discovery is active while the value is greater than 0.
     * exposed for tests
     */
    protected int commandStationSyncLimit = 0;
    private boolean found7FCommandStationClockSync = false;
    private int prevHiFrac = 0;
    private int prevLoFrac = 0;
    private int newCommandStationZero = 0x8000;

    /**
     * Initialized to commandStationEndMinute. Indicates that
     * the Command Station fast clock has not be calibrated.
     * Once calibrated it holds the value of the minFrac
     * minute = zero.
     */
    private int commandStationZeroSecond =  0x8000;
    private final int commandStationEndMinute = 0x8000;
    private final int commandStationEndMinuteType1 = 0x4000; //14bit
    private final int commandStationEndMinuteType2 = 0x8000; //16bit

    public enum CommandStationFracType {
        TYPE1,
        TYPE2
    }
    private CommandStationFracType commandStationFracType = CommandStationFracType.TYPE1;

    /**
     * This must be in the correct type 1 or type 2 format
     * If set will prevent CS clock speed and type discovery
     * @param val - the value of the Zero Second.
     *       either 16bit integer.
     *       or 14 bit integer ( Hi &gt;&gt; 1 + LO )
     */
    public void setCommandStationZeroSecond(int val) {
        commandStationZeroSecond = val;
    }
    
    public int getCommandStationZeroSecond() {
        return commandStationZeroSecond;
    }

    /**
     * The command station clock type
     * @param val 1 = type 1 (14bit) type 2 = 16bit.
     */
    public void setCommandStationFracType(CommandStationFracType val) {
        commandStationFracType = val;
    }

    public CommandStationFracType getCommandStationFracType() {
        return commandStationFracType;
    }

    /**
     * Force a Calibration Cycle - Total Reset
     */
    public void startCalibrate() {
        commandStationFracType = CommandStationFracType.TYPE1;
        commandStationZeroSecond =  commandStationEndMinute;
        calibrateCommandStationClock();
    }

    /**
     * Convert milliseconds to minFrac
     * @param milliSecs time in milliseconds
     * @return the HI LO as an integer and adjusted.
     */
    public int convertMilliSecondsToFcFracMin(int milliSecs) {
        int fracmins;
        if (commandStationFracType == CommandStationFracType.TYPE1) {
            fracmins = (((commandStationEndMinuteType1 - commandStationZeroSecond) * milliSecs) / (int) MSECPERMINUTE ) + commandStationZeroSecond ;
            // the completed calculation fits.
            return (((fracmins & 0x7F80) << 1) +  (fracmins & 0x00F7));
        } else {
            fracmins = (((commandStationEndMinuteType2 - commandStationZeroSecond) * milliSecs) / (int) MSECPERMINUTE ) + commandStationZeroSecond ;
            return (fracmins & 0x7FFF);
        }
    }

    /**
     * Convert  minFrac to milliseconds
     * @param fcMinFrac time in milliseconds
     * @return the HI LO as an integer and adjusted.
     */
    public int convertFcFracMinToMilliSeconds(int fcMinFrac) {
        long millis = 0;
        if (commandStationZeroSecond == commandStationEndMinute) {
            // not calibrated
            return 0;
        }
        if (commandStationFracType == CommandStationFracType.TYPE1) {
            int temp = (( fcMinFrac & 0x4f00 ) * 128) + ( fcMinFrac & 0x7F );
            millis = (( temp - commandStationZeroSecond ) * MSECPERMINUTE )/ (commandStationEndMinuteType1 - commandStationZeroSecond );
        } else {
            millis = (( fcMinFrac - commandStationZeroSecond ) * MSECPERMINUTE )/ (commandStationEndMinuteType2 - commandStationZeroSecond );
        }
        return (int) millis;
    }

    @Override
    public void message(LocoNetMessage msg) {
        if (commandStationSyncLimit > 0) {
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
                if (commandStationFracType == CommandStationFracType.TYPE1 && prevHiFrac == msg.getElement(5) && prevLoFrac > msg.getElement(4) ) {
                    log.debug("Found CS Type 2");
                    commandStationFracType = CommandStationFracType.TYPE2;
                }
                prevHiFrac = msg.getElement(5);
                prevLoFrac = msg.getElement(4);
            }
            commandStationSyncLimit--;
            return;
        }
        if (useInternal && synchronizeWithInternalClock) {
            // we are master, respond..
            // is it a time request or tetherless (sic) query, yes: respond to time request reply.
            if (msg.getOpCode() == LnConstants.OPC_RQ_SL_DATA &&
                    msg.getElement(1) == 0x7B &&
                    msg.getElement(2) == 0x00) {
                log.debug("Replying to FC slot read");
                sendClockMsg(false, MinuteFracType.MINUTE_NORMAL, false);
            } else if (msg.getOpCode() == LnConstants.OPC_PANEL_QUERY && msg.getElement(1) == 0x00) {
                log.debug("Replying to Panel Query");
                sendClockMsg(false, MinuteFracType.MINUTE_NORMAL, false);
            }
        }
    }

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
     * Number of milliseconds per hour
     */
    final static long MSECPERHOUR = 3600000;
    /**
     * Number of milliseconds per minute
     */
    final static long MSECPERMINUTE = 60000;

    /**
     * Describes the type of minute fraction required
     *
     */
    private enum  MinuteFracType {
        MINUTE_START,
        MINUTE_NORMAL,
        MINUTE_END
    }

    /**
     * Accessor routines
     * @return the associated name
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
        curMilliSeconds = (int) (now.getTime() % MSECPERMINUTE);
        setClock();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Date getTime() {
        return clock.getTime();
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

    /**
     * Used for the sole purpose of disabling calibration in the test environment
     */
    public enum TestState {
        NOT_TESTING,
        TESTING_NO_SYNC,
        TESTING_WITH_SYNC
    }

    private TestState testState =  TestState.NOT_TESTING;

    public void setTestState(TestState val) {
        testState = val;
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
        curMilliSeconds = (int) (now.getTime() % MSECPERMINUTE);
        if (!getTime) {
            setTime(now);
        }

        if (commandStationZeroSecond == commandStationEndMinute && testState != TestState.TESTING_NO_SYNC) {
            calibrateCommandStationClock();
        }

        // force a correction at next fast minute after calibration
        fastClockCounter = -1;
    }

    /**
     *
     * Send a series of fast clock reads to establish the roll over
     * from the last increment or the minute to xx
     *
     */
    public void calibrateCommandStationClock() {
        // ensure old thread dead.
        commandStationSyncLimit = 0;
        try {
            Thread.sleep(1000);
        } catch (Exception Ex) {
            log.trace("Killed - Give Up");
            return;
        }
        sendClockMsg(true, MinuteFracType.MINUTE_END, true);
        newCommandStationZero = 0x7FFF; // force big,type 2. we need the min.
        found7FCommandStationClockSync = false;
        // start new thread to pump the FastSlot
        new Thread(new Runnable() {
            @Override
            public void run() {
                int everyMilli = 100;
                int limit = 10000 / everyMilli;
                if (testState == TestState.TESTING_WITH_SYNC) {
                    everyMilli = 100;
                    limit = 5;
                }
                commandStationSyncLimit = limit;
                for (int i = 0; i < limit; i++) {
                    if (commandStationSyncLimit < 1) {
                        return;
                    }
                    initiateRead();
                    try { Thread.sleep(everyMilli); }
                    catch (Exception Ex) {
                        // we are killed, die.
                        commandStationSyncLimit = 0;
                        return;
                    }
                }
            }
        }).start();
        // emergency brake so no command station?
        // 30 secs max.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(30000);
                } catch (Exception Ex) {
                    // we are killed, die.
                    commandStationSyncLimit = 0;
                    return;
                }
                commandStationSyncLimit = 0;
            }
        }).start();
    }

    /**
     * Requests read of the LocoNet fast clock
     */
    public void initiateRead() {
        sm.sendReadSlot(LnConstants.FC_SLOT);
    }

    /**
     * Performs all necessary task for a new fast clock minute
     * Skip this if we are calibrating
     */
    public void newMinute() {
        if (commandStationSyncLimit > 0) {
            // dont mess with the syncing
            return;
        }
        // cant use real time for testing
        if (testState == TestState.NOT_TESTING) {
            Date now = clock.getTime();
            curDays = now.getDate();
            curHours = now.getHours();
            curMinutes = now.getMinutes();
            curMilliSeconds = (int) (now.getTime() % MSECPERMINUTE);
            curRate = (int) clock.getRate();
        }

        if (newCommandStationZero < commandStationZeroSecond) {
            if (commandStationFracType == CommandStationFracType.TYPE1) {
                commandStationZeroSecond = (( newCommandStationZero & 0x7f00 ) >> 1) + (newCommandStationZero & 0x7f);
            } else {
                commandStationZeroSecond = newCommandStationZero;
            }
            // if synchronizing send immediate.
            fastClockCounter = -1;
        }
        if (useInternal && correctFastClock) {
            // we are not master, but want to correct the master
            fastClockCounter -= 1;
            if (fastClockCounter < 1) {
                log.debug("Send Write Master Time");
                sendClockMsg(true,  MinuteFracType.MINUTE_START, false);
                fastClockCounter = curRate;
            }
        }
        if (useInternal && synchronizeWithInternalClock) {
            // We are LocoNet Master Send Heartbeat every real minute
            fastClockCounter -= 1;
            if (fastClockCounter < 1) {
                log.debug("Send Heartbeat/Master Blast");
                sendClockMsg(false, MinuteFracType.MINUTE_START, false);
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
     * changing.
     *
     * @param s the LocoNetSlot object which has been changed
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
        curMilliSeconds = convertFcFracMinToMilliSeconds(s.getFcFracMins());
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
        // We are a  LocoNet Slave. Do not calculate or use minFrac/curMilliSeconds
        // as DTxxx throttles dont, and if we did we would be fast.
        // curMilliSeconds = milliSecondsFromFrac(s.getFcFracMins());
        // we calculate a new msec value for a specific hour/minute
        // in the current day, then set that.
        Date tem = clock.getTime();
        int cHours = tem.getHours();
        long cNumMSec = tem.getTime();
        long nNumMSec = ((cNumMSec / MSECPERHOUR) * MSECPERHOUR) - (cHours * MSECPERHOUR)
                + (curHours * MSECPERHOUR) + (curMinutes * MSECPERMINUTE);
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
        sendClockMsg(true, MinuteFracType.MINUTE_START, false);
    }

    /**
     * Send a response FC Slot or a Write new FC Data data
     * @param sendWrite true - this a write slot message
     * @param minFracType , START, END or normal
     */
    private void sendClockMsg(boolean sendWrite, MinuteFracType minFracType, boolean calibrate) {
        int fractionalMinutes;
        // use the current time set in curDays,Hours,Minutes,Milliseconds
        //if we are calibrating or have not done so successfully, skip this
        if (!calibrate && commandStationZeroSecond != commandStationEndMinute) {
            switch (minFracType) {
                case MINUTE_START:
                    curMilliSeconds = 0;
                    break;
                case MINUTE_NORMAL:
                    // leave as set
                    break;
                case MINUTE_END:
                default:
                    curMilliSeconds = 59000;
                    break;
            }
            fractionalMinutes = convertMilliSecondsToFcFracMin(curMilliSeconds);
        } else {
            // set to near end of minute.
            fractionalMinutes = 0x7f4f;
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
        } else {
            s.setFcMinutes(curMinutes);
            s.setFcRate(curRate);
        }
        s.setFcFracMins(fractionalMinutes);
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
        commandStationSyncLimit = 0;
    }

    private final static Logger log = LoggerFactory.getLogger(LnClockControl.class);

}
