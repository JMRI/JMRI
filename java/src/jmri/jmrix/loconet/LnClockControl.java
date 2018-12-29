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
        // Create a Timebase listener for Minute change events from the internal clock
        minuteChangeListener = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                newMinute();
            }
        };
        clock.addMinuteChangeListener(minuteChangeListener);
    }

    @Override
    public void message(LocoNetMessage msg) {
        // we are master, respond..
        if (useInternal && synchronizeWithInternalClock) {
            // is it a time request or tetherless (sic) query, yes: respond to time request reply.
            if (msg.getOpCode() == LnConstants.OPC_RQ_SL_DATA &&
                    msg.getElement(1) == 0x7B &&
                    msg.getElement(2) == 0x00) {
                log.debug("Replying to FC slot read");
                sendClockMsg(false, true);
            } else if (msg.getOpCode() == LnConstants.OPC_PANEL_QUERY && msg.getElement(1) == 0x00) {
                log.debug("Replying to Panel Query");
                sendClockMsg(false, true);
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
    /* constants */
    final static long MSECPERHOUR = 3600000;
    final static long MSECPERMINUTE = 60000;
    final static double CORRECTION = 915.0;

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
        if (useInternal && synchronizeWithInternalClock) {
            sendClockMsg(true, false);  // we  are master, invalidate cs clock.
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
        nNumMSec += (long) (((CORRECTION - curFractionalMinutes) / CORRECTION * MSECPERMINUTE));
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

    private javax.swing.Timer loconetClientTimer;
    private javax.swing.Timer loconetMasterTimer;
    /**
     * Timer for when acting as LoconetClient
     */
    private void setClientTimer() {
        if (loconetClientTimer != null) {
            log.debug("Restarting loconetClientTimer timer");
            loconetClientTimer.restart();
            return;
        }
        log.debug("Setting Up Client New Timer");
        loconetClientTimer = new javax.swing.Timer(50000, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // no updates in 50secs, ask for it
                initiateRead();
                log.debug("try read");
            }
        });
        loconetClientTimer.setRepeats(false);
        loconetClientTimer.start();
    }

    /**
     * Timer for when acting as Loconet Master
     */
    private void setMasterTimer() {
        if (loconetMasterTimer != null) {
            log.debug("Restarting Master timer");
            loconetMasterTimer.restart();
            return;
        }
        log.debug("Setting Up New Master Timer");
        loconetMasterTimer = new javax.swing.Timer(55000, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                //  every 55 secs send fcslot
                sendClockMsg(false, true);
                log.debug("Send FCSlot");
            }
        });
        loconetMasterTimer.setRepeats(true);
        loconetMasterTimer.start();
    }

    /**
     * Cancel any timers that may be running
     */
    private void turnOffAllTimers() {
        // turn off any timers
        if (loconetMasterTimer != null) {
            loconetMasterTimer.stop();
            log.debug("loconetMasterTimer Off");
        }
        if (loconetClientTimer != null) {
            loconetClientTimer.stop();
            log.debug("loconetClientTimer Off");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initializeHardwareClock(double rate, Date now, boolean getTime) {
        synchronizeWithInternalClock = clock.getSynchronize();
        correctFastClock = clock.getCorrectHardware();
        useInternal = clock.getInternalMaster();
        log.debug("useInternal[{}",useInternal);
        turnOffAllTimers();
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
        if ((!useInternal)) {
            // only pay attention to loconet FC slot if we loconet is source.
            log.debug("Starting LocoNet Client Listen");
            setClientTimer();
        } else if (useInternal && synchronizeWithInternalClock) {
            // send first update
            sendClockMsg(false, true);
            // we are master on the loconet, start braodcast timer.
            log.debug("Starting LocoNet Master Blaster");
            setMasterTimer();
        }
    }

    /**
     * Requests read of the Loconet fast clock
     */
    public void initiateRead() {
        sm.sendReadSlot(LnConstants.FC_SLOT);
    }

    /**
     * Performs all necessary task for a new fast clock minute
     */
     public void newMinute() {
        if (useInternal && correctFastClock && !synchronizeWithInternalClock) {  //if not  master, but want to keep master in sync with our internal clock
            log.debug("Sending Correction");
            sendClockMsg(true, true);
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
        // set the internal time base to the LocoNet clock
        // Work out how far through the current fast minute we are
        // and add that on to the time.
        long tmpcor = (long) (((CORRECTION - curFractionalMinutes) / CORRECTION * MSECPERMINUTE));
        nNumMSec += tmpcor;
        //log.info("tmpcor[{}]",tmpcor);
        clock.setTime(new Date(nNumMSec));
        // re-trigger timeout
        setClientTimer();
    }

    /**
     * Push current Clock Control parameters out to LocoNet slot.
     */
    private void setClock() {
        if (useInternal && ( synchronizeWithInternalClock || correctFastClock)) {
            // we are allowed to send commands to the fast clock
            LocoNetSlot s = sm.slot(LnConstants.FC_SLOT);

            // load time
            s.setFcDays(curDays);
            s.setFcHours(curHours);
            s.setFcMinutes(curMinutes);
            s.setFcRate(curRate);
            s.setFcFracMins(curFractionalMinutes);

            // set other content
            //     power (GTRK_POWER, 0x01 bit in byte 7)
            boolean power = true;
            if (pm != null) {
                power = (pm.getPower() == PowerManager.ON);
            } else {
                jmri.util.Log4JUtil.warnOnce(log, "Can't access power manager for fast clock");
            }
            s.setTrackStatus(s.getTrackStatus() &  (~LnConstants.GTRK_POWER) );
            if (power) s.setTrackStatus(s.getTrackStatus() | LnConstants.GTRK_POWER);
            s.setThrottleIdentity(clockThrottleId);
            // and write
            tc.sendLocoNetMessage(s.writeSlot());
        }
    }

    /**
     * Send a read response FC Slot or a Write new FC Data data
     * @param sendWrite true - this a write slot message
     * @param setValid true - this contains valid data
     */
    private void sendClockMsg(boolean sendWrite, boolean setValid) {
        // set the time            // get time from the internal clock
        Date now = clock.getTime();
        // If this code is left in then we can never set a clock to 12:00 13:00 etc...
        // and the CS is going to send the 0 minute for us.....
        // skip the correction if minutes is 0 because Logic Rail Clock displays incorrectly
        //  if a correction is sent at zero minutes.
        // if (now.getMinutes() != 0) {
        // Set the Fast Clock Day to the current Day of the month 1-31
        curDays = now.getDate();
        // Update current time
        curHours = now.getHours();
        curMinutes = now.getMinutes();
        long millis = now.getTime();
        // How many ms are we into the fast minute as we want to sync the
        // Fast Clock Master Frac_Mins to the right 65.535 ms tick
        long elapsedMS = millis % MSECPERMINUTE;
        double frac_min = elapsedMS / (double) MSECPERMINUTE;
        curFractionalMinutes = (int) CORRECTION - (int) (CORRECTION * frac_min);
        //}
        // we are allowed to send commands to the fast clock
        LocoNetSlot s = sm.slot(LnConstants.FC_SLOT);

        // load time
        s.setFcDays(curDays);
        s.setFcHours(curHours);
        s.setFcMinutes(curMinutes);
        s.setFcRate(curRate);
        s.setFcFracMins(curFractionalMinutes);
        s.setThrottleIdentity(clockThrottleId);
        if (setValid) {
            s.setFcCntrlBitOn(LnConstants.FC_VALID); // valid time
        } else {
            s.setFcCntrlBitOff(LnConstants.FC_VALID);
        }
        // set other content
        //     power (GTRK_POWER, 0x01 bit in byte 7)
        boolean power = true;
        if (pm != null) {
            power = (pm.getPower() == PowerManager.ON);
        } else {
            jmri.util.Log4JUtil.warnOnce(log, "Can't access power manager for fast clock");
        }
        s.setTrackStatus(s.getTrackStatus() & (~LnConstants.GTRK_POWER));
        if (power)
            s.setTrackStatus(s.getTrackStatus() | LnConstants.GTRK_POWER);

        // and write
        LocoNetMessage msg = s.writeSlot();
        // change to send read...
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
        
        turnOffAllTimers();
    }

    private final static Logger log = LoggerFactory.getLogger(LnClockControl.class);

}

