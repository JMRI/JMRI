package jmri.jmrit.dispatcher;

import java.util.ArrayList;
import jmri.InstanceManager;

/**
 * This class holds information and options for an AllocationRequestt.
 * <p>
 * An AllocationRequest holds the following information: Section to be allocated
 * Active Train requesting the allocation
 * <p>
 * A AllocationRequests is referenced via a list in DispatcherFrame, which
 * serves as a manager for AllocationRequest objects.
 * <p>
 * AllocationRequests are transient, and are not saved to disk.
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
 *
 * @author Dave Duchamp Copyright (C) 2008-2010
 */
public class AllocationRequest {

    /**
     * Create an AllocationRequest.
     *
     * @param s   the requested section
     * @param num the sequence number for the requested section
     * @param dir the direction the train is traveling on the section
     * @param at  the train for which the section is requested
     */
    public AllocationRequest(jmri.Section s, int num, int dir, ActiveTrain at) {
        mSection = s;
        mActiveTrain = at;
        mSectionSeqNum = num;
        mSectionDirection = dir;
        // listen for changes in Section occupancy
        if (mSection != null) {
            mSection.addPropertyChangeListener(mSectionListener = new java.beans.PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    handleSectionChange(e);
                }
            });
        }
    }

    // instance variables
    private jmri.Section mSection = null;
    private ActiveTrain mActiveTrain = null;
    private int mSectionSeqNum = 0;
    private int mSectionDirection = jmri.Section.UNKNOWN;
    private java.beans.PropertyChangeListener mSectionListener = null;
    // instance variables related to automatic allocation of Sections
    private boolean mWaitingForTrain = false;
    private ArrayList<ActiveTrain> mMeetingTrainList = new ArrayList<ActiveTrain>();

    //
    // Access methods
    //
    public jmri.Section getSection() {
        return mSection;
    }

    public String getSectionName() {
        String s = mSection.getSystemName();
        String u = mSection.getUserName();
        if ((u != null) && (!u.equals("") && (!u.equals(s)))) {
            return (s + "(" + u + ")");
        }
        return s;
    }

    protected ActiveTrain getActiveTrain() {
        return mActiveTrain;
    }

    protected String getActiveTrainName() {
        return (mActiveTrain.getTrainName() + "/" + mActiveTrain.getTransitName());
    }

    protected int getSectionSeqNumber() {
        return mSectionSeqNum;
    }

    protected int getSectionDirection() {
        return mSectionDirection;
    }

    protected String getSectionDirectionName() {
        if (mSectionDirection == jmri.Section.FORWARD) {
            return Bundle.getMessage("FORWARD");
        }
        if (mSectionDirection == jmri.Section.REVERSE) {
            return Bundle.getMessage("REVERSE");
        }
        return Bundle.getMessage("UNKNOWN");
    }

    protected boolean getWaitingForTrain() {
        return mWaitingForTrain;
    }

    protected void setWaitingForTrain(boolean set) {
        mWaitingForTrain = set;
    }

    protected void addMeetingTrain(ActiveTrain at) {
        mMeetingTrainList.add(at);
    }

    protected void removeMeetingTrain(ActiveTrain at) {
        for (int i = 0; i < mMeetingTrainList.size(); i++) {
            if (at == mMeetingTrainList.get(i)) {
                mMeetingTrainList.remove(i);
                return;
            }
        }
    }

    protected ArrayList<ActiveTrain> getMeetingTrainList() {
        return mMeetingTrainList;
    }

    /**
     * Methods
     */
    private void handleSectionChange(java.beans.PropertyChangeEvent e) {
        InstanceManager.getDefault(DispatcherFrame.class).sectionOccupancyChanged();
        //This forces us to rescan the allocation list if the section has gone unoccupied, thus this might get re-allocated
        if (e.getPropertyName().equals("occupancy")) {
            if (((Integer) e.getNewValue()).intValue() == jmri.Section.UNOCCUPIED) {
                InstanceManager.getDefault(DispatcherFrame.class).forceScanOfAllocation();
            }
        }
    }

    public void dispose() {
        if ((mSectionListener != null) && (mSection != null)) {
            mSection.removePropertyChangeListener(mSectionListener);
        }

        if ((mSignalMastListener != null) && (mWaitingForSignalMast != null)) {
            mWaitingForSignalMast.removePropertyChangeListener(mSignalMastListener);
        }

        if ((mWaitingOnBlock != null) && (mWaitingOnBlockListener != null)) {
            mWaitingOnBlock.removePropertyChangeListener(mWaitingOnBlockListener);
        }
        mWaitingOnBlock = null;
        mWaitingOnBlockListener = null;
        mSignalMastListener = null;
        mWaitingForSignalMast = null;
        mSectionListener = null;
        mSection = null;
        mActiveTrain = null;
    }

    private java.beans.PropertyChangeListener mSignalMastListener = null;

    private jmri.SignalMast mWaitingForSignalMast = null;

    public void setWaitingForSignalMast(jmri.SignalMast sm) {
        if (mSignalMastListener == null) {
            mSignalMastListener = new java.beans.PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("Held")) {
                        if (!((Boolean) e.getNewValue()).booleanValue()) {
                            mWaitingForSignalMast.removePropertyChangeListener(mSignalMastListener);
                            InstanceManager.getDefault(DispatcherFrame.class).forceScanOfAllocation();
                        }
                    }
                }
            };
        }
        if (mWaitingForSignalMast != null) {
            mWaitingForSignalMast.removePropertyChangeListener(mSignalMastListener);
        }
        mWaitingForSignalMast = sm;
        if (mWaitingForSignalMast != null) {
            mWaitingForSignalMast.addPropertyChangeListener(mSignalMastListener);
        }
    }

    jmri.Block mWaitingOnBlock = null;
    private java.beans.PropertyChangeListener mWaitingOnBlockListener = null;

    protected void setWaitingOnBlock(jmri.Block b) {
        if (mWaitingOnBlockListener == null) {
            mWaitingOnBlockListener = new java.beans.PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("state")) {
                        if (((Integer) e.getNewValue()).intValue() == jmri.Block.UNOCCUPIED) {
                            mWaitingOnBlock.removePropertyChangeListener(mWaitingOnBlockListener);
                            InstanceManager.getDefault(DispatcherFrame.class).forceScanOfAllocation();
                        }
                    }
                }
            };
        }
        if (mWaitingOnBlock != null) {
            mWaitingOnBlock.removePropertyChangeListener(mWaitingOnBlockListener);
        }
        mWaitingOnBlock = b;
        if (mWaitingOnBlock != null) {
            mWaitingOnBlock.addPropertyChangeListener(mWaitingOnBlockListener);
        }

    }
}
