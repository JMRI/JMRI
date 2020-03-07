package jmri.jmrix.loconet;

import java.util.Date;

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
 * For this implementation, "synchronize" implies "correct", since the two
 * clocks run at a different rate.
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
public class LnClockControl extends DefaultClockControl implements SlotListener {


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

    final SlotManager sm;
    final LnTrafficController tc;
    final LnPowerManager pm;

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
    private boolean setInternal = false;   // true if LocoNet Clock is the master
    private boolean synchronizeWithInternalClock = false;
    private boolean inSyncWithInternalFastClock = false;
    private boolean timebaseErrorReported = false;
    private boolean correctFastClock = false;
    private boolean readInProgress = false;
    /* constants */
    final static long MSECPERHOUR = 3600000;
    final static long MSECPERMINUTE = 60000;
    final static double CORRECTION = 915.0;

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
        if (curRate == 0) {
            savedRate = (int) newRate;      // clock stopped case
        } else {
            curRate = (int) newRate;        // clock running case
            savedRate = curRate;
        }
        setClock();
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
        curRate = savedRate;
        setTime(now);
    }

    @Override
    public void stopHardwareClock() {
        savedRate = curRate;
        curRate = 0;
        setClock();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initializeHardwareClock(double rate, Date now, boolean getTime) {
        synchronizeWithInternalClock = clock.getSynchronize();
        correctFastClock = clock.getCorrectHardware();
        setInternal = !clock.getInternalMaster();
        if (!setInternal && !synchronizeWithInternalClock && !correctFastClock) {
            // No request to interact with hardware fast clock - ignore call
            return;
        }
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
        if (getTime || synchronizeWithInternalClock || correctFastClock) {
            inSyncWithInternalFastClock = false;
            initiateRead();
        }
    }

    /**
     * Requests read of the LocoNet fast clock
     */
    public void initiateRead() {
        if (!readInProgress) {
            sm.sendReadSlot(LnConstants.FC_SLOT);
            readInProgress = true;
        }
    }

    /**
     * Corrects the LocoNet Fast Clock
     */
    @SuppressWarnings("deprecation")
    public void newMinute() {
        // ignore if waiting on LocoNet clock read
        if (!inSyncWithInternalFastClock) {
            return;
        }
        if (correctFastClock || synchronizeWithInternalClock) {
            // get time from the internal clock
            Date now = clock.getTime();
            // skip the correction if minutes is 0 because Logic Rail Clock displays incorrectly
            //  if a correction is sent at zero minutes.
            if (now.getMinutes() != 0) {
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
                setClock();
            }
        } else if (setInternal) {
            inSyncWithInternalFastClock = false;
            initiateRead();
        }
    }

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
        // only watch the clock slot
        if (s.getSlot() != LnConstants.FC_SLOT) {
            return;
        }
        // if don't need to know, simply return
        if (!correctFastClock && !synchronizeWithInternalClock && !setInternal) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("slot update " + s);
        }
        // update current clock variables from the new slot contents
        curDays = s.getFcDays();
        curHours = s.getFcHours();
        curMinutes = s.getFcMinutes();
        int temRate = s.getFcRate();
        // reject the new rate if different and not resetting the internal clock
        if ((temRate != curRate) && !setInternal) {
            setRate(curRate);
        } // keep the new rate if different and resetting the internal clock
        else if ((temRate != curRate) && setInternal) {
            try {
                clock.userSetRate(temRate);
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
        // set the internal timebase based on the LocoNet clock
        if (readInProgress && !inSyncWithInternalFastClock) {
            // Work out how far through the current fast minute we are
            // and add that on to the time.
            nNumMSec += (long) (((CORRECTION - curFractionalMinutes) / CORRECTION * MSECPERMINUTE));
            clock.setTime(new Date(nNumMSec));
        } else if (setInternal) {
            // unsolicited time change from the LocoNet
            clock.setTime(new Date(nNumMSec));
        }
        // Once we have done everything else set the flag to say we are in sync
        inSyncWithInternalFastClock = true;
    }

    /**
     * Push current Clock Control parameters out to LocoNet slot.
     */
    private void setClock() {
        if (setInternal || synchronizeWithInternalClock || correctFastClock) {
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

            // and write
            tc.sendLocoNetMessage(s.writeSlot());
        }
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
    }

    private final static Logger log = LoggerFactory.getLogger(LnClockControl.class);

}
