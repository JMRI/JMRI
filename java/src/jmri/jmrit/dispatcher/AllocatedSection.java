package jmri.jmrit.dispatcher;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.Block;
import jmri.EntryPoint;
import jmri.InstanceManager;
import jmri.Section;
import jmri.Sensor;
import jmri.TransitSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds information and options for an AllocatedSection, a Section
 * that is currently allocated to an ActiveTrain.
 * <p>
 * AllocatedSections are referenced via a list in DispatcherFrame, which serves
 * as a manager for AllocatedSection objects. Each ActiveTrain also maintains a
 * list of AllocatedSections currently assigned to it.
 * <p>
 * AllocatedSections are transient, and are not saved to disk.
 * <p>
 * AllocatedSections keep track of whether they have been entered and exited.
 * <p>
 * If the Active Train this Section is assigned to is being run automatically,
 * support is provided for monitoring Section changes and changes for Blocks
 * within the Section.
 * <hr>
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
 * @author Dave Duchamp Copyright (C) 2008-2011
 */
public class AllocatedSection {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * Create an AllocatedSection.
     *
     * @param s         the section to allocation
     * @param at        the train to allocate the section to
     * @param seq       the sequence location of the section in the route
     * @param next      the following section
     * @param nextSeqNo the sequence location of the following section
     */
    public AllocatedSection(@Nonnull Section s, ActiveTrain at, int seq, Section next, int nextSeqNo) {
        mSection = s;
        mActiveTrain = at;
        mSequence = seq;
        mNextSection = next;
        mNextSectionSequence = nextSeqNo;
        if (mSection.getOccupancy() == Section.OCCUPIED) {
            mEntered = true;
        }
        // listen for changes in Section occupancy
        mSection.addPropertyChangeListener(mSectionListener = (PropertyChangeEvent e) -> {
            handleSectionChange(e);
        });
        setStoppingSensors();
        if ((mActiveTrain.getAutoActiveTrain() == null) && !(InstanceManager.getDefault(DispatcherFrame.class).getSupportVSDecoder())) {
            // for manual running, monitor block occupancy for selected Blocks only
            if (mActiveTrain.getReverseAtEnd()
                    && ((mSequence == mActiveTrain.getEndBlockSectionSequenceNumber())
                    || (mActiveTrain.getResetWhenDone()
                    && (mSequence == mActiveTrain.getStartBlockSectionSequenceNumber())))) {
                initializeMonitorBlockOccupancy();
            } else if (mSequence == mActiveTrain.getEndBlockSectionSequenceNumber()) {
                initializeMonitorBlockOccupancy();
            }
        } else {
            // monitor block occupancy for all Sections of automatially running trains
            initializeMonitorBlockOccupancy();
        }
    }

    // instance variables
    private Section mSection = null;
    private ActiveTrain mActiveTrain = null;
    private int mSequence = 0;
    private Section mNextSection = null;
    private int mNextSectionSequence = 0;
    private PropertyChangeListener mSectionListener = null;
    private boolean mEntered = false;
    private boolean mExited = false;
    private int mAllocationNumber = 0;     // used to keep track of allocation order
    private Sensor mForwardStoppingSensor = null;
    private Sensor mReverseStoppingSensor = null;

    //
    // Access methods
    //
    public Section getSection() {
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

    public ActiveTrain getActiveTrain() {
        return mActiveTrain;
    }

    public String getActiveTrainName() {
        return (mActiveTrain.getTrainName() + "/" + mActiveTrain.getTransitName());
    }

    public int getSequence() {
        return mSequence;
    }

    public Section getNextSection() {
        return mNextSection;
    }

    public int getNextSectionSequence() {
        return mNextSectionSequence;
    }

    protected boolean setNextSection(Section sec, int i) {
        if (sec == null) {
            mNextSection = null;
            mNextSectionSequence = i;
            return true;
        }
        if (mNextSection != null) {
            log.error("Next section is already set");
            return false;
        }
        mNextSection = sec;
        return true;
    }

    public void setNextSectionSequence(int i) {
        mNextSectionSequence = i;
    }

    public boolean getEntered() {
        return mEntered;
    }

    public boolean getExited() {
        return mExited;
    }

    public int getAllocationNumber() {
        return mAllocationNumber;
    }

    public void setAllocationNumber(int n) {
        mAllocationNumber = n;
    }

    public Sensor getForwardStoppingSensor() {
        return mForwardStoppingSensor;
    }

    public Sensor getReverseStoppingSensor() {
        return mReverseStoppingSensor;
    }

    // instance variables used with automatic running of trains
    private int mIndex = 0;
    private PropertyChangeListener mExitSignalListener = null;
    private final List<PropertyChangeListener> mBlockListeners = new ArrayList<>();
    private List<Block> mBlockList = null;
    private final List<Block> mActiveBlockList = new ArrayList<>();

    //
    // Access methods for automatic running instance variables
    //
    public void setIndex(int i) {
        mIndex = i;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setExitSignalListener(PropertyChangeListener xSigListener) {
        mExitSignalListener = xSigListener;
    }

    public PropertyChangeListener getExitSignalListener() {
        return mExitSignalListener;
    }

    /**
     * Methods
     */
    protected void setStoppingSensors() {
        if (mSection.getState() == Section.FORWARD) {
            mForwardStoppingSensor = mSection.getForwardStoppingSensor();
            mReverseStoppingSensor = mSection.getReverseStoppingSensor();
        } else {
            mForwardStoppingSensor = mSection.getReverseStoppingSensor();
            mReverseStoppingSensor = mSection.getForwardStoppingSensor();
        }
    }

    protected TransitSection getTransitSection() {
        return mActiveTrain.getTransit().getTransitSectionFromSectionAndSeq(mSection, mSequence);
    }

    public int getDirection() {
        return mSection.getState();
    }

    public int getLength() {
        return mSection.getLengthI(InstanceManager.getDefault(DispatcherFrame.class).getUseScaleMeters(),
                InstanceManager.getDefault(DispatcherFrame.class).getScale());
    }

    public void reset() {
        mExited = false;
        mEntered = false;
        if (mSection.getOccupancy() == Section.OCCUPIED) {
            mEntered = true;
        }
    }

    private synchronized void handleSectionChange(PropertyChangeEvent e) {
        if (mSection.getOccupancy() == Section.OCCUPIED) {
            mEntered = true;
        } else if (mSection.getOccupancy() == Section.UNOCCUPIED) {
            if (mEntered) {
                mExited = true;
            }
        }
        if (mActiveTrain.getAutoActiveTrain() != null) {
            if (e.getPropertyName().equals("state")) {
                mActiveTrain.getAutoActiveTrain().handleSectionStateChange(this);
            } else if (e.getPropertyName().equals("occupancy")) {
                mActiveTrain.getAutoActiveTrain().handleSectionOccupancyChange(this);
            }
        }

        //       if (mEntered && !mExited && mActiveTrain.getResetWhenDone() && mActiveTrain.getDelayedRestart() != ActiveTrain.NODELAY) {
        //           if (getSequence() == mActiveTrain.getEndBlockSectionSequenceNumber()) {
        //               mActiveTrain.setRestart();
        //           }
        //       }
        InstanceManager.getDefault(DispatcherFrame.class).sectionOccupancyChanged();
    }

    public synchronized void initializeMonitorBlockOccupancy() {
        if (mBlockList != null) {
            return;
        }
        mBlockList = mSection.getBlockList();
        for (int i = 0; i < mBlockList.size(); i++) {
            Block b = mBlockList.get(i);
            if (b != null) {
                final int index = i;  // block index
                PropertyChangeListener listener = (PropertyChangeEvent e) -> {
                    handleBlockChange(index, e);
                };
                b.addPropertyChangeListener(listener);
                mBlockListeners.add(listener);
            }
        }
    }

    private synchronized void handleBlockChange(int index, PropertyChangeEvent e) {
        if (e.getPropertyName().equals("state")) {
            if (mBlockList == null) {
                mBlockList = mSection.getBlockList();
            }

            Block b = mBlockList.get(index);
            if (!isInActiveBlockList(b)) {
                int occ = b.getState();
                Runnable handleBlockChange = new RespondToBlockStateChange(b, occ, this);
                Thread tBlockChange = new Thread(handleBlockChange, "Allocated Section Block Change on " + b.getDisplayName());
                tBlockChange.start();
                addToActiveBlockList(b);
                if (InstanceManager.getDefault(DispatcherFrame.class).getSupportVSDecoder()) {
                    firePropertyChangeEvent("BlockStateChange", null, b.getSystemName()); // NOI18N
                }
            }
        }
    }

    protected Block getExitBlock() {
        if (mNextSection == null) {
            return null;
        }
        EntryPoint ep = mSection.getExitPointToSection(mNextSection, mSection.getState());
        if (ep != null) {
            return ep.getBlock();
        }
        return null;
    }

    protected Block getEnterBlock(AllocatedSection previousAllocatedSection) {
        if (previousAllocatedSection == null) {
            return null;
        }
        Section sPrev = previousAllocatedSection.getSection();
        EntryPoint ep = mSection.getEntryPointFromSection(sPrev, mSection.getState());
        if (ep != null) {
            return ep.getBlock();
        }
        return null;
    }

    protected synchronized void addToActiveBlockList(Block b) {
        if (b != null) {
            mActiveBlockList.add(b);
        }
    }

    protected synchronized void removeFromActiveBlockList(Block b) {
        if (b != null) {
            for (int i = 0; i < mActiveBlockList.size(); i++) {
                if (b == mActiveBlockList.get(i)) {
                    mActiveBlockList.remove(i);
                    return;
                }
            }
        }
    }

    protected synchronized boolean isInActiveBlockList(Block b) {
        if (b != null) {
            for (int i = 0; i < mActiveBlockList.size(); i++) {
                if (b == mActiveBlockList.get(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized void dispose() {
        if ((mSectionListener != null) && (mSection != null)) {
            mSection.removePropertyChangeListener(mSectionListener);
        }
        mSectionListener = null;
        for (int i = mBlockListeners.size(); i > 0; i--) {
            Block b = mBlockList.get(i - 1);
            b.removePropertyChangeListener(mBlockListeners.get(i - 1));
        }
    }

// _________________________________________________________________________________________
    // This class responds to Block state change in a separate thread
    class RespondToBlockStateChange implements Runnable {

        public RespondToBlockStateChange(Block b, int occ, AllocatedSection as) {
            _block = b;
            _aSection = as;
            _occ = occ;
        }

        @Override
        public void run() {
            // delay to insure that change is not a short spike
            // The forced delay has been removed. The delay can be controlled by the debounce
            // values in the sensor table. The use of an additional fixed 250 milliseconds
            // caused it to always fail when crossing small blocks at speed.
            if (mActiveTrain.getAutoActiveTrain() != null) {
                // automatically running train
                mActiveTrain.getAutoActiveTrain().handleBlockStateChange(_aSection, _block);
            } else if (_occ == Block.OCCUPIED) {
                // manual running train - block newly occupied
                if (!mActiveTrain.getAutoRun()) {
                    if ((_block == mActiveTrain.getEndBlock()) && mActiveTrain.getReverseAtEnd()) {
                        // reverse direction of Allocated Sections
                        mActiveTrain.reverseAllAllocatedSections();
                        mActiveTrain.setRestart();
                    } else if ((_block == mActiveTrain.getStartBlock()) && mActiveTrain.getResetWhenDone()) {
                        // reset the direction of Allocated Sections
                        mActiveTrain.resetAllAllocatedSections();
                        mActiveTrain.setRestart();
                    } else if (_block == mActiveTrain.getEndBlock() || _block == mActiveTrain.getStartBlock() ) {
                        mActiveTrain.setStatus(ActiveTrain.DONE);
                    }
                }
            }
            // remove from lists
            removeFromActiveBlockList(_block);
        }

        private Block _block = null;
        private int _occ = 0;
        private AllocatedSection _aSection = null;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    protected void firePropertyChangeEvent(PropertyChangeEvent evt) {
        pcs.firePropertyChange(evt);
    }

    protected void firePropertyChangeEvent(String name, Object oldVal, Object newVal) {
        pcs.firePropertyChange(name, oldVal, newVal);
    }

    private final static Logger log = LoggerFactory.getLogger(AllocatedSection.class);
}
