package jmri.jmrit.dispatcher;

import jmri.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle planning information for AutoAllocate
 * <p>
 * An Allocation Plan involves a planned meet of two ActiveTrains in a specified
 * area of the layout.
 * <p>
 * AllocationPlan objects are transient (not saved between runs).
 * <p>
 * AllocationPlan objects are created and disposed by AutoAllocate as needed.
 * AutoAllocate serves as the manager of AllocationPlan objects.
 * <p>
 * An ActiveTrain may be in more than one AllocationPlan of the same type,
 * provided its target Section in all active AllocationPlans is the same.
 * <p>
 * An AllocationPlan is "complete" when both Active Trains have been allocated
 * their target Sections.
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
 * @author Dave Duchamp Copyright (C) 2011
 */
public class AllocationPlan {

    public AllocationPlan(AutoAllocate aa, int planNum) {
        _autoAllocate = aa;
        if (_autoAllocate == null) {
            log.error("null AutoAllocate when constructing an AllocationPlan");
        }
        _planNum = planNum;
    }

    /**
     * Constants representing the type of AllocationPlan
     */
    protected static final int NONE = 0x00;   // no plan type
    protected static final int XING_MEET = 0x01;
    protected static final int PASSING_MEET = 0x02;
    protected static final int CONTINUING = 0x04;

    // instance variables
    private AutoAllocate _autoAllocate = null;
    private int _planNum = 0;     // Note: _planNum may not be changed. It is the ID of this plan.
    private int _planType = NONE;
    private ActiveTrain _atOne = null;
    private ActiveTrain _atTwo = null;
    private Section _tSectionOne = null;
    private Section _tSectionTwo = null;
    private int _tSectionOneSeq = 0;
    private int _tSectionTwoSeq = 0;

    //
    // Access methods
    //
    protected int getPlanNum() {
        return _planNum;
    }

    protected int getPlanType() {
        return _planType;
    }

    protected void setPlanType(int type) {
        _planType = type;
    }

    protected ActiveTrain getActiveTrain(int i) {
        if (i == 1) {
            return _atOne;
        } else if (i == 2) {
            return _atTwo;
        }
        return null;
    }

    protected void setActiveTrain(ActiveTrain at, int i) {
        if (i == 1) {
            _atOne = at;
        } else if (i == 2) {
            _atTwo = at;
        } else {
            log.error("out of range index argument in call to 'setActiveTrain'");
        }
    }

    protected Section getTargetSection(int i) {
        if (i == 1) {
            return _tSectionOne;
        } else if (i == 2) {
            return _tSectionTwo;
        }
        return null;
    }

    protected void setTargetSection(Section s, int seq, int i) {
        if (i == 1) {
            _tSectionOne = s;
            _tSectionOneSeq = seq;
        } else if (i == 2) {
            _tSectionTwo = s;
            _tSectionTwoSeq = seq;
        } else {
            log.error("out of range index argument in call to 'setTargetSection'");
        }
    }

    protected int getTargetSectionSequenceNum(int i) {
        if (i == 1) {
            return _tSectionOneSeq;
        } else if (i == 2) {
            return _tSectionTwoSeq;
        }
        return 0;
    }

    //
    // Other Methods
    //
    protected boolean isComplete() {
        if ((_atOne == null) || (_atTwo == null)) {
            return false;
        }
        java.util.List<AllocatedSection> aSections = _atOne.getAllocatedSectionList();
        boolean complete = false;
        for (int i = 0; i < aSections.size(); i++) {
            if ((aSections.get(i).getSection() == _tSectionOne)
                    && (aSections.get(i).getSequence() == _tSectionOneSeq)) {
                complete = true;
            }
        }
        if (!complete) {
            return false;
        }
        complete = false;
        aSections = _atTwo.getAllocatedSectionList();
        for (int j = 0; j < aSections.size(); j++) {
            if ((aSections.get(j).getSection() == _tSectionTwo)
                    && (aSections.get(j).getSequence() == _tSectionTwoSeq)) {
                complete = true;
            }
        }
        return complete;
    }

    public void dispose() {
        // does nothing for now
    }

    private final static Logger log = LoggerFactory.getLogger(AllocationPlan.class);
}
