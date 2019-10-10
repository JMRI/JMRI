package jmri.jmrit.dispatcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jmri.Block;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Path;
import jmri.Section;
import jmri.Transit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds information and options for an ActiveTrain, that is a train
 * that has been linked to a Transit and activated for transit around the
 * layout.
 * <p>
 * An ActiveTrain may be assigned one of the following modes, which specify how
 * the active train will be run through its transit: AUTOMATIC - indicates the
 * ActiveTrain will be run under automatic control of the computer. (Automatic
 * Running) MANUAL - indicates an ActiveTrain running in AUTOMATIC mode has
 * reached a Special Action in its Transit that requires MANUAL operation. When
 * this happens, the status changes to WORKING, and the mode changes to MANUAL.
 * The ActiveTrain will be run by an operator using a throttle. AUTOMATIC
 * running is resumed when the work has been completed. DISPATCHED - indicates
 * the ActiveTrain will be run by an operator using a throttle. A dispatcher
 * will allocate Sections to the ActiveTrain as needed, control optional signals
 * using a CTC panel or computer logic, and arbitrate any conflicts between
 * ActiveTrains. (Human Dispatcher).
 * <p>
 * An ActiveTrain will have one of the following statuses:
 * <dl>
 * <dt>RUNNING</dt><dd>Actively running on the layout, according to its mode of
 * operation.</dd>
 * <dt>PAUSED</dt><dd>Paused waiting for a user-specified number of fast clock
 * minutes. The Active Train is expected to move to either RUNNING or WAITING
 * once the specified number of minutes has elapsed. This is intended for
 * automatic station stops. (automatic trains only)</dd>
 * <dt>WAITING</dt><dd>Stopped waiting for a Section allocation. This is the
 * state the Active Train is in when it is created in Dispatcher.</dd>
 * <dt>WORKING</dt><dd>Performing work under control of a human engineer. This is
 * the state an Active Train assumes when an engineer is picking up or setting
 * out cars at industries. (automatic trains only)</dd>
 * <dt>READY</dt><dd>Train has completed WORKING, and is awaiting a restart -
 * dispatcher clearance to resume running. (automatic trains only)</dd>
 * <dt>STOPPED</dt><dd>Train was stopped by the dispatcher. Dispatcher must
 * resume. (automatic trains only)</dd>
 * <dt>DONE</dt><dd>Train has completed its transit of the layout and is ready to
 * be terminated by the dispatcher, or Restart pressed to repeat the automated
 * run.</dd>
 * </dl>
 * Status is a bound property.
 * <p>
 * The ActiveTrain status should maintained (setStatus) by the running class, or
 * if running in DISPATCHED mode, by Dispatcher. When an ActiveTrain is WAITING,
 * and the dispatcher allocates a section to it, the status of the ActiveTrain
 * is automatically set to RUNNING. So an autoRun class can listen to the status
 * of the ActiveTrain to trigger start up if the train has been waiting for the
 * dispatcher. Note: There is still more to be programmed here.
 * <p>
 * Train information supplied when the ActiveTrain is created can come from any
 * of the following:
 * <dl>
 * <dt>ROSTER</dt><dd>The train was selected from the JMRI roster menu</dd>
 * <dt>OPERATIONS</dt><dd>The train was selected from trains available from JMRI
 * operations</dd>
 * <dt>USER</dt><dd>Neither menu was used--the user entered a name and DCC
 * address.</dd>
 * </dl>
 * Train source information is recorded when an ActiveTrain is created,
 * and may be referenced by getTrainSource if it is needed by other objects. The
 * train source should be specified in the Dispatcher Options window prior to
 * creating an ActiveTrain.
 * <p>
 * ActiveTrains are referenced via a list in DispatcherFrame, which serves as a
 * manager for ActiveTrain objects.
 * <p>
 * ActiveTrains are transient, and are not saved to disk. Active Train
 * information can be saved to disk, making set up with the same options, etc
 * very easy.
 * <p>
 * An ActiveTrain runs through its Transit in the FORWARD direction, until a
 * Transit Action reverses the direction of travel in the Transit. When running
 * with its Transit reversed, the Active Train returns to its starting Section.
 * Upon reaching and stopping in its starting Section, the Transit is
 * automatically set back to the forward direction. If AutoRestart is set, the
 * run is repeated. The direction of travel in the Transit is maintained here.
 *
 * <p>
 * This file is part of JMRI.
 * <p>
 * JMRI is open source software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 *
 * @author Dave Duchamp Copyright (C) 2008-2011
 */
public class ActiveTrain {

    /**
     * Create an ActiveTrain.
     *
     * @param t           the transit linked to this ActiveTrain
     * @param name        the train name
     * @param trainSource the source for this ActiveTrain
     */
    public ActiveTrain(Transit t, String name, int trainSource) {
        mTransit = t;
        mTrainName = name;
        mTrainSource = trainSource;
    }

    /**
     * Constants representing the Status of this ActiveTrain When created, the
     * Status of an Active Train is always WAITING,
     */
    public static final int RUNNING = 0x01;   // running on the layout
    public static final int PAUSED = 0x02;    // paused for a number of fast minutes
    public static final int WAITING = 0x04;   // waiting for a section allocation
    public static final int WORKING = 0x08;   // actively working
    public static final int READY = 0x10;   // completed work, waiting for restart
    public static final int STOPPED = 0x20;   // stopped by the dispatcher (auto trains only)
    public static final int DONE = 0x40;   // completed its transit

    /**
     * Constants representing Type of ActiveTrains.
     */
    public static final int NONE = 0x00;               // no train type defined
    public static final int LOCAL_PASSENGER = 0x01;    // low priority local passenger train
    public static final int LOCAL_FREIGHT = 0x02;      // low priority freight train performing local tasks
    public static final int THROUGH_PASSENGER = 0x03;  // normal priority through passenger train
    public static final int THROUGH_FREIGHT = 0x04;    // normal priority through freight train
    public static final int EXPRESS_PASSENGER = 0x05;  // high priority passenger train
    public static final int EXPRESS_FREIGHT = 0x06;    // high priority freight train
    public static final int MOW = 0x07;          // low priority maintenance of way train

    /**
     * Constants representing the mode of running of the Active Train The mode
     * is set when the Active Train is created. The mode may be switched during
     * a run.
     */
    public static final int AUTOMATIC = 0x02;   // requires mAutoRun to be "true" (auto trains only)
    public static final int MANUAL = 0x04;    // requires mAutoRun to be "true" (auto trains only)
    public static final int DISPATCHED = 0x08;

    /**
     * Constants representing the source of the train information
     */
    public static final int ROSTER = 0x01;
    public static final int OPERATIONS = 0x02;
    public static final int USER = 0x04;

    /**
     * The value of {@link #getAllocateMethod()} if allocating as many sections as are clear.
     */
    public static final int ALLOCATE_AS_FAR_AS_IT_CAN = -1;
    /**
     * The value of {@link #getAllocateMethod()} if allocating up until the next safe section
     */
    public static final int ALLOCATE_BY_SAFE_SECTIONS = 0;

    // instance variables
    private Transit mTransit = null;
    private String mTrainName = "";
    private int mTrainSource = ROSTER;
    private jmri.jmrit.roster.RosterEntry mRoster = null;
    private int mStatus = WAITING;
    private int mMode = DISPATCHED;
    private boolean mTransitReversed = false;  // true if Transit is running in reverse
    private boolean mAllocationReversed = false;  // true if allocating Sections in reverse
    private AutoActiveTrain mAutoActiveTrain = null;
    private final List<AllocatedSection> mAllocatedSections = new ArrayList<>();
    private jmri.Section mLastAllocatedSection = null;
    private int mLastAllocatedSectionSeqNumber = 0;
    private jmri.Section mSecondAllocatedSection = null;
    private int mNextAllocationNumber = 1;
    private jmri.Section mNextSectionToAllocate = null;
    private int mNextSectionSeqNumber = 0;
    private int mNextSectionDirection = 0;
    private jmri.Block mStartBlock = null;
    private int mStartBlockSectionSequenceNumber = 0;
    private jmri.Block mEndBlock = null;
    private jmri.Section mEndBlockSection = null;
    private int mEndBlockSectionSequenceNumber = 0;
    private int mPriority = 0;
    private boolean mAutoRun = false;
    private String mDccAddress = "";
    private boolean mResetWhenDone = true;
    private boolean mReverseAtEnd = false;
    private int mAllocateMethod = 3;
    public final static int NODELAY = 0x00;
    public final static int TIMEDDELAY = 0x01;
    public final static int SENSORDELAY = 0x02;

    private int mDelayedRestart = NODELAY;
    private int mDelayedStart = NODELAY;
    private int mDepartureTimeHr = 8;
    private int mDepartureTimeMin = 0;
    private int mRestartDelay = 0;
    private NamedBeanHandle<jmri.Sensor> mStartSensor = null; // A Sensor that when changes state to active will trigger the trains start.
    private boolean resetStartSensor = true;
    private NamedBeanHandle<jmri.Sensor> mRestartSensor = null; // A Sensor that when changes state to active will trigger the trains restart.
    private boolean resetRestartSensor = true;
    private int mTrainType = LOCAL_FREIGHT;
    private boolean terminateWhenFinished = false;

    // start up instance variables
    private boolean mStarted = false;

    //
    // Access methods
    //
    public boolean getStarted() {
        return mStarted;
    }

    public void setStarted() {
        mStarted = true;
        mStatus = RUNNING;
        setStatus(WAITING);
        if (mAutoActiveTrain != null && InstanceManager.getDefault(DispatcherFrame.class).getSignalType() == DispatcherFrame.SIGNALMAST) {
            mAutoActiveTrain.setupNewCurrentSignal(null,false);
        }
    }

    public Transit getTransit() {
        return mTransit;
    }

    public String getTransitName() {
        String s = mTransit.getSystemName();
        String u = mTransit.getUserName();
        if ((u != null) && (!u.equals("") && (!u.equals(s)))) {
            return (s + "(" + u + ")");
        }
        return s;
    }

    public String getActiveTrainName() {
        return (mTrainName + "/" + getTransitName());
    }

    // Note: Transit and Train may not be changed once an ActiveTrain is created.
    public String getTrainName() {
        return mTrainName;
    }

    public int getTrainSource() {
        return mTrainSource;
    }

    public void setRosterEntry(jmri.jmrit.roster.RosterEntry re) {
        mRoster = re;
    }

    public jmri.jmrit.roster.RosterEntry getRosterEntry() {
        if (mRoster == null && getTrainSource() == ROSTER) {
            //Try to resolve the roster based upon the train name
            mRoster = jmri.jmrit.roster.Roster.getDefault().getEntryForId(getTrainName());
        } else if (getTrainSource() != ROSTER) {
            mRoster = null;
        }
        return mRoster;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        if (restartPoint) {
            return;
        }
        if ((status == RUNNING) || (status == PAUSED) || (status == WAITING) || (status == WORKING)
                || (status == READY) || (status == STOPPED) || (status == DONE)) {
            if (mStatus != status) {
                int old = mStatus;
                mStatus = status;
                firePropertyChange("status", Integer.valueOf(old), Integer.valueOf(mStatus));
            }
            if (mStatus == DONE && terminateWhenFinished) {
                InstanceManager.getDefault(DispatcherFrame.class).terminateActiveTrain(this);
            }
        } else {
            log.error("Invalid ActiveTrain status - " + status);
        }
    }

    public String getStatusText() {
        if (mStatus == RUNNING) {
            return Bundle.getMessage("RUNNING");
        } else if (mStatus == PAUSED) {
            return Bundle.getMessage("PAUSED");
        } else if (mStatus == WAITING) {
            if (!mStarted) {
                if (mDelayedStart == TIMEDDELAY) {
                    return jmri.jmrit.beantable.LogixTableAction.formatTime(mDepartureTimeHr,
                            mDepartureTimeMin) + " " + Bundle.getMessage("START");
                } else if (mDelayedStart == SENSORDELAY) {
                    return (Bundle.getMessage("BeanNameSensor") + " " + getDelaySensorName());
                }
            }
            return Bundle.getMessage("WAITING");
        } else if (mStatus == WORKING) {
            return Bundle.getMessage("WORKING");
        } else if (mStatus == READY) {
            if (restartPoint && getDelayedRestart() == TIMEDDELAY) {
                return jmri.jmrit.beantable.LogixTableAction.formatTime(restartHr,
                        restartMin) + " " + Bundle.getMessage("START");
            } else if (restartPoint && getDelayedRestart() == SENSORDELAY) {
                return (Bundle.getMessage("BeanNameSensor") + " " + getRestartSensorName());
            }
            return Bundle.getMessage("READY");
        } else if (mStatus == STOPPED) {
            return Bundle.getMessage("STOPPED");
        } else if (mStatus == DONE) {
            return Bundle.getMessage("DONE");
        }
        return ("");
    }

    public boolean isTransitReversed() {
        return mTransitReversed;
    }

    public void setTransitReversed(boolean set) {
        mTransitReversed = set;
    }

    public boolean isAllocationReversed() {
        return mAllocationReversed;
    }

    public void setAllocationReversed(boolean set) {
        mAllocationReversed = set;
    }

    public int getDelayedStart() {
        return mDelayedStart;
    }

    public void setDelayedStart(int delay) {
        mDelayedStart = delay;
    }

    public int getDelayedRestart() {
        return mDelayedRestart;
    }

    public void setDelayedRestart(int delay) {
        mDelayedRestart = delay;
    }

    public int getDepartureTimeHr() {
        return mDepartureTimeHr;
    }

    public void setDepartureTimeHr(int hr) {
        mDepartureTimeHr = hr;
    }

    public int getDepartureTimeMin() {
        return mDepartureTimeMin;
    }

    public void setDepartureTimeMin(int min) {
        mDepartureTimeMin = min;
    }

    public void setRestartDelay(int min) {
        mRestartDelay = min;
    }

    public int getRestartDelay() {
        return mRestartDelay;
    }
    int restartHr = 0;
    int restartMin = 0;

    public int getRestartDepartHr() {
        return restartHr;
    }

    public int getRestartDepartMin() {
        return restartMin;
    }

    public void setTerminateWhenDone(boolean boo) {
        terminateWhenFinished = boo;
    }

    public jmri.Sensor getDelaySensor() {
        if (mStartSensor == null) {
            return null;
        }
        return mStartSensor.getBean();
    }

    public String getDelaySensorName() {
        if (mStartSensor == null) {
            return null;
        }
        return mStartSensor.getName();
    }

    public void setDelaySensor(jmri.Sensor s) {
        if (s == null) {
            mStartSensor = null;
            return;
        }
        mStartSensor = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(s.getDisplayName(), s);
    }

    public void setResetStartSensor(boolean b) {
        resetStartSensor = b;
    }

    public jmri.Sensor getRestartSensor() {
        if (mRestartSensor == null) {
            return null;
        }
        return mRestartSensor.getBean();
    }

    public String getRestartSensorName() {
        if (mRestartSensor == null) {
            return null;
        }
        return mRestartSensor.getName();
    }

    public void setRestartSensor(jmri.Sensor s) {
        if (s == null) {
            mRestartSensor = null;
            return;
        }
        mRestartSensor = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(s.getDisplayName(), s);
    }

    public void setResetRestartSensor(boolean b) {
        resetRestartSensor = b;
    }


    private java.beans.PropertyChangeListener delaySensorListener = null;
    private java.beans.PropertyChangeListener restartSensorListener = null;
    private java.beans.PropertyChangeListener restartAllocationSensorListener = null;

    public void initializeDelaySensor() {
        if (mStartSensor == null) {
            log.error("Call to initialise delay on start sensor, but none specified");
            return;
        }
        if (delaySensorListener == null) {
            final ActiveTrain at = this;
            delaySensorListener = new java.beans.PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("KnownState")) {
                        if (((Integer) e.getNewValue()).intValue() == jmri.Sensor.ACTIVE) {
                            getDelaySensor().removePropertyChangeListener(delaySensorListener);
                            InstanceManager.getDefault(DispatcherFrame.class).removeDelayedTrain(at);
                            setStarted();
                            InstanceManager.getDefault(DispatcherFrame.class).forceScanOfAllocation();
                            if (resetStartSensor) {
                                try {
                                    getDelaySensor().setKnownState(jmri.Sensor.INACTIVE);
                                    log.debug("Start sensor {} set back to inActive", getDelaySensorName());                                    
                                } catch (jmri.JmriException ex) {
                                    log.error("Error resetting start sensor {} back to inActive", getDelaySensorName());
                                }
                            }
                        }
                    }
                }
            };
        }
        getDelaySensor().addPropertyChangeListener(delaySensorListener);
    }

    public void initializeRestartSensor() {
        if (mRestartSensor == null) {
            log.error("Call to initialise delay on restart sensor, but none specified");
            return;
        }
        if (restartSensorListener == null) {
            final ActiveTrain at = this;
            restartSensorListener = new java.beans.PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("KnownState")) {
                        if (((Integer) e.getNewValue()).intValue() == jmri.Sensor.ACTIVE) {
                            getRestartSensor().removePropertyChangeListener(restartSensorListener);
                            restartSensorListener = null;
                            InstanceManager.getDefault(DispatcherFrame.class).removeDelayedTrain(at);
                            restart();
                            InstanceManager.getDefault(DispatcherFrame.class).forceScanOfAllocation();
                            if (resetRestartSensor) {
                                try {
                                    getRestartSensor().setKnownState(jmri.Sensor.INACTIVE);
                                    log.debug("Restart sensor {} set back to inActive", getRestartSensorName());
                                } catch (jmri.JmriException ex) {
                                    log.error("Error resetting restart sensor back to inActive");
                                }
                            }
                        }
                    }
                }
            };
        }
        getRestartSensor().addPropertyChangeListener(restartSensorListener);
    }

    public void initializeRestartAllocationSensor(NamedBeanHandle<jmri.Sensor> restartAllocationSensor) {
        if (restartAllocationSensor == null) {
            log.error("Call to initialise delay on restart allocation sensor, but none specified");
            return;
        }
        if (restartAllocationSensorListener == null) {
            restartAllocationSensorListener = new java.beans.PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("KnownState")) {
                        if (((Integer) e.getNewValue()).intValue() == jmri.Sensor.INACTIVE) {
                            restartAllocationSensor.getBean().removePropertyChangeListener(restartAllocationSensorListener);
                            restartAllocationSensorListener = null;
                            InstanceManager.getDefault(DispatcherFrame.class).forceScanOfAllocation();
                        }
                    }
                }
            };
        }
        restartAllocationSensor.getBean().addPropertyChangeListener(restartAllocationSensorListener);
    }

    public void setTrainType(int type) {
        mTrainType = type;
    }

    /**
     * set train type using localized string name as stored
     *
     * @param sType  name, such as "LOCAL_PASSENGER"
     */
    public void setTrainType(String sType) {
        if (sType.equals(Bundle.getMessage("LOCAL_FREIGHT"))) {
            setTrainType(LOCAL_FREIGHT);
        } else if (sType.equals(Bundle.getMessage("LOCAL_PASSENGER"))) {
            setTrainType(LOCAL_PASSENGER);
        } else if (sType.equals(Bundle.getMessage("THROUGH_FREIGHT"))) {
            setTrainType(THROUGH_FREIGHT);
        } else if (sType.equals(Bundle.getMessage("THROUGH_PASSENGER"))) {
            setTrainType(THROUGH_PASSENGER);
        } else if (sType.equals(Bundle.getMessage("EXPRESS_FREIGHT"))) {
            setTrainType(EXPRESS_FREIGHT);
        } else if (sType.equals(Bundle.getMessage("EXPRESS_PASSENGER"))) {
            setTrainType(EXPRESS_PASSENGER);
        } else if (sType.equals(Bundle.getMessage("MOW"))) {
            setTrainType(MOW);
        }
    }

    public int getTrainType() {
        return mTrainType;
    }

    public String getTrainTypeText() {
        if (mTrainType == LOCAL_FREIGHT) {
            return Bundle.getMessage("LOCAL_FREIGHT");
        } else if (mTrainType == LOCAL_PASSENGER) {
            return Bundle.getMessage("LOCAL_PASSENGER");
        } else if (mTrainType == THROUGH_FREIGHT) {
            return Bundle.getMessage("THROUGH_FREIGHT");
        } else if (mTrainType == THROUGH_PASSENGER) {
            return Bundle.getMessage("THROUGH_PASSENGER");
        } else if (mTrainType == EXPRESS_FREIGHT) {
            return Bundle.getMessage("EXPRESS_FREIGHT");
        } else if (mTrainType == EXPRESS_PASSENGER) {
            return Bundle.getMessage("EXPRESS_PASSENGER");
        } else if (mTrainType == MOW) {
            return Bundle.getMessage("MOW");
        }
        return ("");
    }

    public int getMode() {
        return mMode;
    }

    public void setMode(int mode) {
        if ((mode == AUTOMATIC) || (mode == MANUAL)
                || (mode == DISPATCHED)) {
            int old = mMode;
            mMode = mode;
            firePropertyChange("mode", Integer.valueOf(old), Integer.valueOf(mMode));
        } else {
            log.error("Attempt to set ActiveTrain mode to illegal value - " + mode);
        }
    }

    public String getModeText() {
        if (mMode == AUTOMATIC) {
            return Bundle.getMessage("AUTOMATIC");
        } else if (mMode == MANUAL) {
            return Bundle.getMessage("MANUAL");
        } else if (mMode == DISPATCHED) {
            return Bundle.getMessage("DISPATCHED");
        }
        return ("");
    }

    public void setAutoActiveTrain(AutoActiveTrain aat) {
        mAutoActiveTrain = aat;
    }

    public AutoActiveTrain getAutoActiveTrain() {
        return mAutoActiveTrain;
    }

    public int getRunningDirectionFromSectionAndSeq(jmri.Section s, int seqNo) {
        int dir = mTransit.getDirectionFromSectionAndSeq(s, seqNo);
        if (mTransitReversed) {
            if (dir == jmri.Section.FORWARD) {
                dir = jmri.Section.REVERSE;
            } else {
                dir = jmri.Section.FORWARD;
            }
        }
        return dir;
    }

    public int getAllocationDirectionFromSectionAndSeq(jmri.Section s, int seqNo) {
        int dir = mTransit.getDirectionFromSectionAndSeq(s, seqNo);
        if (mAllocationReversed) {
            if (dir == jmri.Section.FORWARD) {
                dir = jmri.Section.REVERSE;
            } else {
                dir = jmri.Section.FORWARD;
            }
        }
        return dir;
    }

    public void addAllocatedSection(AllocatedSection as) {
        if (as != null) {
            mAllocatedSections.add(as);
            if (as.getSection() == mNextSectionToAllocate) {
                // this  is the next Section in the Transit, update pointers
                mLastAllocatedSection = as.getSection();
                mLastAllocatedSectionSeqNumber = mNextSectionSeqNumber;
                mNextSectionToAllocate = as.getNextSection();
                mNextSectionSeqNumber = as.getNextSectionSequence();
                mNextSectionDirection = getAllocationDirectionFromSectionAndSeq(
                        mNextSectionToAllocate, mNextSectionSeqNumber);
                as.setAllocationNumber(mNextAllocationNumber);
                mNextAllocationNumber++;
            } else {
                // this is an extra allocated Section
                as.setAllocationNumber(-1);
            }
            if ((mStatus == WAITING) && mStarted) {
                setStatus(RUNNING);
            }
            if (as.getSequence() == 2) {
                mSecondAllocatedSection = as.getSection();
            }
            if (InstanceManager.getDefault(DispatcherFrame.class).getNameInAllocatedBlock()) {
                if (InstanceManager.getDefault(DispatcherFrame.class).getRosterEntryInBlock() && getRosterEntry() != null) {
                    as.getSection().setNameFromActiveBlock(getRosterEntry());
                } else {
                    as.getSection().setNameInBlocks(mTrainName);
                }
                as.getSection().suppressNameUpdate(true);
            }
            if (InstanceManager.getDefault(DispatcherFrame.class).getExtraColorForAllocated()) {
                as.getSection().setAlternateColorFromActiveBlock(true);
            }
            refreshPanel();
        } else {
            log.error("Null Allocated Section reference in addAllocatedSection of ActiveTrain");
        }
    }

    private void refreshPanel() {
        if (InstanceManager.getDefault(DispatcherFrame.class).getLayoutEditor() != null) {
            InstanceManager.getDefault(DispatcherFrame.class).getLayoutEditor().redrawPanel();
        }
    }

    public void removeAllocatedSection(AllocatedSection as) {
        if (as == null) {
            log.error("Null AllocatedSection reference in removeAllocatedSection of ActiveTrain");
            return;
        }
        int index = -1;
        for (int i = 0; i < mAllocatedSections.size(); i++) {
            if (as == mAllocatedSections.get(i)) {
                index = i;
            }
        }
        if (index < 0) {
            log.error("Attempt to remove an unallocated Section " + as.getSectionName());
            return;
        }
        mAllocatedSections.remove(index);
        if (mAutoRun) {
            mAutoActiveTrain.removeAllocatedSection(as);
        }
        if (InstanceManager.getDefault(DispatcherFrame.class).getNameInAllocatedBlock()) {
            as.getSection().clearNameInUnoccupiedBlocks();
            as.getSection().suppressNameUpdate(false);
        }
        as.getSection().setAlternateColor(false);
        refreshPanel();
        if (as.getSection() == mLastAllocatedSection) {
            mLastAllocatedSection = null;
            if (mAllocatedSections.size() > 0) {
                mLastAllocatedSection = mAllocatedSections.get(
                        mAllocatedSections.size() - 1).getSection();
                mLastAllocatedSectionSeqNumber = mAllocatedSections.size() - 1;
            }
        }
    }

    /**
     * This resets the state of the ActiveTrain so that it can be reallocated.
     */
    public void allocateAFresh() {
        setStatus(WAITING);
        setTransitReversed(false);
        List<AllocatedSection> sectionsToRelease = new ArrayList<>();
        for (AllocatedSection as : InstanceManager.getDefault(DispatcherFrame.class).getAllocatedSectionsList()) {
            if (as.getActiveTrain() == this) {
                sectionsToRelease.add(as);
            }
        }
        for (AllocatedSection as : sectionsToRelease) {
            InstanceManager.getDefault(DispatcherFrame.class).releaseAllocatedSection(as, true); // need to find Allocated Section
            as.getSection().setState(jmri.Section.FREE);
        }
        if (mLastAllocatedSection != null) {
            mLastAllocatedSection.setState(jmri.Section.FREE);
        }
        resetAllAllocatedSections();
        clearAllocations();
        if (mAutoRun) {
            mAutoActiveTrain.allocateAFresh();
        }
        InstanceManager.getDefault(DispatcherFrame.class).allocateNewActiveTrain(this);
    }

    public void clearAllocations() {
        for (AllocatedSection as : getAllocatedSectionList()) {
            removeAllocatedSection(as);
        }
    }

    public List<AllocatedSection> getAllocatedSectionList() {
        List<AllocatedSection> list = new ArrayList<>();
        for (int i = 0; i < mAllocatedSections.size(); i++) {
            list.add(mAllocatedSections.get(i));
        }
        return list;
    }

    /**
     * Returns list of all Blocks occupied by or allocated to this train. They
     * are in order from the tail of the train to the head of the train then on
     * to the forward-most allocated block. Note that unoccupied blocks can
     * exist before and after the occupied blocks.
     *
     * TODO: doesn't handle reversing of adjacent multi-block sections well
     *
     * @return the list of blocks order of occupation
     */
    public List<Block> getBlockList() {
        List<Block> list = new ArrayList<>();
        for (int i = 0; i < mAllocatedSections.size(); i++) { // loop thru allocated sections, then all blocks for each section
            Section s = mAllocatedSections.get(i).getSection();
            List<Block> bl = s.getBlockList();
            if (bl.size() > 1) { //sections with multiple blocks need extra logic

                boolean blocksConnected = true;
                //determine if blocks should be added in forward or reverse order based on connectivity
                if (i == 0) { //for first section, compare last block to first of next section
                    if (mAllocatedSections.size() > 1
                            && //only one section, assume forward
                            !connected(bl.get(bl.size() - 1), mAllocatedSections.get(i + 1).getSection().getBlockList().get(0))) {
                        blocksConnected = false;
                    }
                } else { //not first section, check for connectivity between last block in list, and first block in this section
                    if (!connected(list.get(list.size() - 1), bl.get(0))) { //last block is not connected to first block, add reverse
                        blocksConnected = false;
                    }
                }
                if (blocksConnected) { //blocks were connected, so add to outgoing in forward order
                    for (int j = 0; j < bl.size(); j++) {
                        Block b = bl.get(j);
                        list.add(b);
                        log.trace("block {} ({}) added to list for Section {} (fwd)", b.getDisplayName(),
                                (b.getState() == Block.OCCUPIED ? "OCCUPIED" : "UNOCCUPIED"),
                                s.getDisplayName());
                    }
                } else { //not connected, add in reverse order
                    for (int j = bl.size() - 1; j >= 0; j--) {
                        Block b = bl.get(j);
                        list.add(b);
                        log.trace("block {} ({}) added to list for Section {} (rev)", b.getDisplayName(),
                                (b.getState() == Block.OCCUPIED ? "OCCUPIED" : "UNOCCUPIED"),
                                s.getDisplayName());
                    }
                }

            } else { //single block sections are simply added to the outgoing list
                Block b = bl.get(0);
                list.add(b);
                log.trace("block {} ({}) added to list for Section {} (one)", b.getDisplayName(),
                        (b.getState() == Block.OCCUPIED ? "OCCUPIED" : "UNOCCUPIED"),
                        s.getDisplayName());
            }
        }
        return list;
    }

    /* copied from Section.java */
    private boolean connected(Block b1, Block b2) {
        if ((b1 != null) && (b2 != null)) {
            List<Path> paths = b1.getPaths();
            for (int i = 0; i < paths.size(); i++) {
                if (paths.get(i).getBlock() == b2) {
                    return true;
                }
            }
        }
        return false;
    }

    public jmri.Section getLastAllocatedSection() {
        return mLastAllocatedSection;
    }

    public int getLastAllocatedSectionSeqNumber() {
        return mLastAllocatedSectionSeqNumber;
    }

    public String getLastAllocatedSectionName() {
        if (mLastAllocatedSection == null) {
            return "<" + Bundle.getMessage("None").toLowerCase() + ">"; // <none>
        }
        return getSectionName(mLastAllocatedSection);
    }

    public jmri.Section getNextSectionToAllocate() {
        return mNextSectionToAllocate;
    }

    public int getNextSectionSeqNumber() {
        return mNextSectionSeqNumber;
    }

    public String getNextSectionToAllocateName() {
        if (mNextSectionToAllocate == null) {
            return "<" + Bundle.getMessage("None").toLowerCase() + ">"; // <none>
        }
        return getSectionName(mNextSectionToAllocate);
    }

    private String getSectionName(jmri.Section sc) {
        String s = sc.getSystemName();
        String u = sc.getUserName();
        if ((u != null) && (!u.equals("") && (!u.equals(s)))) {
            return (s + "(" + u + ")");
        }
        return s;
    }

    public jmri.Block getStartBlock() {
        return mStartBlock;
    }

    public void setStartBlock(jmri.Block sBlock) {
        mStartBlock = sBlock;
    }

    public int getStartBlockSectionSequenceNumber() {
        return mStartBlockSectionSequenceNumber;
    }

    public void setStartBlockSectionSequenceNumber(int sBlockSeqNum) {
        mStartBlockSectionSequenceNumber = sBlockSeqNum;
    }

    public jmri.Block getEndBlock() {
        return mEndBlock;
    }

    public void setEndBlock(jmri.Block eBlock) {
        mEndBlock = eBlock;
    }

    public jmri.Section getEndBlockSection() {
        return mEndBlockSection;
    }

    public void setEndBlockSection(jmri.Section eSection) {
        mEndBlockSection = eSection;
    }

    public int getEndBlockSectionSequenceNumber() {
        return mEndBlockSectionSequenceNumber;
    }

    public void setEndBlockSectionSequenceNumber(int eBlockSeqNum) {
        mEndBlockSectionSequenceNumber = eBlockSeqNum;
    }

    public int getPriority() {
        return mPriority;
    }

    public void setPriority(int priority) {
        mPriority = priority;
    }

    public boolean getAutoRun() {
        return mAutoRun;
    }

    public void setAutoRun(boolean autoRun) {
        mAutoRun = autoRun;
    }

    public String getDccAddress() {
        return mDccAddress;
    }

    public void setDccAddress(String dccAddress) {
        mDccAddress = dccAddress;
    }

    public boolean getResetWhenDone() {
        return mResetWhenDone;
    }

    public void setResetWhenDone(boolean s) {
        mResetWhenDone = s;
    }

    public boolean getReverseAtEnd() {
        return mReverseAtEnd;
    }

    public void setReverseAtEnd(boolean s) {
        mReverseAtEnd = s;
    }

    protected jmri.Section getSecondAllocatedSection() {
        return mSecondAllocatedSection;
    }

    /**
     * Returns the AllocateM Method to be used by autoAllocate
     *
     * @return The number of Blocks ahead to be allocated or 0 = Allocate By Safe
     *         sections or -1 - Allocate All The Way.
     */
    public int getAllocateMethod() {
        return mAllocateMethod;
    }

    /**
     * Sets the Allocation Method to be used bu autoAllocate
     * @param i The number of Blocks ahead to be allocated or 0 = Allocate By Safe
     *          sections or -1 - Allocate All The Way.
     */
    public void setAllocateMethod(int i) {
        mAllocateMethod = i;
    }

    //
    // Operating methods
    //
    public AllocationRequest initializeFirstAllocation() {
        if (mAllocatedSections.size() > 0) {
            log.error("ERROR - Request to initialize first allocation, when allocations already present");
            return null;
        }
        if ((mStartBlockSectionSequenceNumber > 0) && (mStartBlock != null)) {
            mNextSectionToAllocate = mTransit.getSectionFromBlockAndSeq(mStartBlock,
                    mStartBlockSectionSequenceNumber);
            if (mNextSectionToAllocate == null) {
                mNextSectionToAllocate = mTransit.getSectionFromConnectedBlockAndSeq(mStartBlock,
                        mStartBlockSectionSequenceNumber);
                if (mNextSectionToAllocate == null) {
                    log.error("ERROR - Cannot find Section for first allocation of ActiveTrain"
                            + getActiveTrainName());
                    return null;
                }
            }
            mNextSectionSeqNumber = mStartBlockSectionSequenceNumber;
            mNextSectionDirection = getAllocationDirectionFromSectionAndSeq(mNextSectionToAllocate,
                    mNextSectionSeqNumber);
        } else {
            log.error("ERROR - Insufficient information to initialize first allocation");
            return null;
        }
        if (!InstanceManager.getDefault(DispatcherFrame.class).requestAllocation(this,
                mNextSectionToAllocate, mNextSectionDirection, mNextSectionSeqNumber, true, null)) {
            log.error("Allocation request failed for first allocation of " + getActiveTrainName());
        }
        if (InstanceManager.getDefault(DispatcherFrame.class).getRosterEntryInBlock() && getRosterEntry() != null) {
            mStartBlock.setValue(getRosterEntry());
        } else if (InstanceManager.getDefault(DispatcherFrame.class).getShortNameInBlock()) {
            mStartBlock.setValue(mTrainName);
        }
        AllocationRequest ar = InstanceManager.getDefault(DispatcherFrame.class).findAllocationRequestInQueue(mNextSectionToAllocate,
                mNextSectionSeqNumber, mNextSectionDirection, this);
        return ar;
    }

    protected boolean addEndSection(jmri.Section s, int seq) {
        AllocatedSection as = mAllocatedSections.get(mAllocatedSections.size() - 1);
        if (!as.setNextSection(s, seq)) {
            return false;
        }
        setEndBlockSection(s);
        setEndBlockSectionSequenceNumber(seq);
        //At this stage the section direction hasn't been set, by default the exit block returned is the reverse if the section is free
        setEndBlock(s.getExitBlock());
        mNextSectionSeqNumber = seq;
        mNextSectionToAllocate = s;
        return true;
    }

    /*This is for use where the transit has been extended, then the last section has been cancelled no
     checks are performed, these should be done by a higher level code*/
    protected void removeLastAllocatedSection() {
        AllocatedSection as = mAllocatedSections.get(mAllocatedSections.size() - 1);
        //Set the end block using the AllocatedSections exit block before clearing the next section in the allocatedsection
        setEndBlock(as.getExitBlock());

        as.setNextSection(null, 0);
        setEndBlockSection(as.getSection());

        setEndBlockSectionSequenceNumber(getEndBlockSectionSequenceNumber() - 1);
        // In theory the following values should have already been set if there are no more sections to allocate.
        mNextSectionSeqNumber = 0;
        mNextSectionToAllocate = null;
    }

    protected AllocatedSection reverseAllAllocatedSections() {
        AllocatedSection aSec = null;
        for (int i = 0; i < mAllocatedSections.size(); i++) {
            aSec = mAllocatedSections.get(i);
            int dir = mTransit.getDirectionFromSectionAndSeq(aSec.getSection(), aSec.getSequence());
            if (dir == jmri.Section.FORWARD) {
                aSec.getSection().setState(jmri.Section.REVERSE);
            } else {
                aSec.getSection().setState(jmri.Section.FORWARD);
            }
            aSec.setStoppingSensors();
        }
        return aSec;
    }

    protected void resetAllAllocatedSections() {
        for (int i = 0; i < mAllocatedSections.size(); i++) {
            AllocatedSection aSec = mAllocatedSections.get(i);
            int dir = mTransit.getDirectionFromSectionAndSeq(aSec.getSection(), aSec.getSequence());
            aSec.getSection().setState(dir);
            aSec.setStoppingSensors();
        }
    }

    protected void setRestart() {
        if (getDelayedRestart() == NODELAY) {
            return;
        }

        setStatus(READY);
        restartPoint = true;
        if (getDelayedRestart() == TIMEDDELAY) {
            Date now = jmri.InstanceManager.getDefault(jmri.Timebase.class).getTime();
            @SuppressWarnings("deprecation")
            int nowHours = now.getHours();
            @SuppressWarnings("deprecation")
            int nowMinutes = now.getMinutes();
            int hours = getRestartDelay() / 60;
            int minutes = getRestartDelay() % 60;
            restartHr = nowHours + hours + ((nowMinutes + minutes) / 60);
            restartMin = ((nowMinutes + minutes) % 60);
            if (restartHr>23){
                restartHr=restartHr-24;
            }
        }
        InstanceManager.getDefault(DispatcherFrame.class).addDelayedTrain(this);
    }

    boolean restartPoint = false;

    boolean holdAllocation = false;

    protected void holdAllocation(boolean boo) {
        holdAllocation = boo;
    }

    protected boolean holdAllocation() {
        return holdAllocation;
    }

    protected boolean reachedRestartPoint() {
        return restartPoint;
    }

    protected void restart() {
        restartPoint = false;
        holdAllocation = false;
        setStatus(WAITING);
        if (mAutoActiveTrain != null) {
            mAutoActiveTrain.setupNewCurrentSignal(null,true);
        }
    }

    public void terminate() {
        InstanceManager.getDefault(DispatcherFrame.class).removeDelayedTrain(this);
        if (getDelaySensor() != null && delaySensorListener != null) {
            getDelaySensor().removePropertyChangeListener(delaySensorListener);
        }
        if (getRestartSensor() != null && restartSensorListener != null) {
            getRestartSensor().removePropertyChangeListener(restartSensorListener);
        }
        mTransit.setState(Transit.IDLE);
    }

    public void dispose() {
        getTransit().removeTemporarySections();
    }

    // Property Change Support
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private final static Logger log = LoggerFactory.getLogger(ActiveTrain.class);

}
