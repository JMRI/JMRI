// TransitSection.java
package jmri;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds information and options for a Section when assigned to a
 * Transit. Corresponds to an allocatable "Section" of track assigned to a
 * Transit.
 * <P>
 * A TransitSection holds the following information: Section ID Section
 * Direction Sequence number of Section within the Transit Special actions list
 * for train in this Section, if requested (see TransitSectionAction.java)
 * Whether this Section is a primary section or an alternate section
 * <P>
 * A TransitSection is referenced via a list in its parent Transit, and is
 * stored on disk when its parent Transit is stored.
 * <P>
 * Provides for delayed initializatio of Section when loading panel files, so
 * that this is not dependent on order of items in the panel file.
 *
 * @author	Dave Duchamp Copyright (C) 2008
 * @version	$Revision$
 */
public class TransitSection {

    /**
     * Main constructor method
     */
    public TransitSection(jmri.Section s, int seq, int direction) {
        mSection = s;
        mSequence = seq;
        mDirection = direction;
    }

    /**
     * Convenience constructor
     */
    public TransitSection(jmri.Section s, int seq, int direction, boolean alt) {
        mSection = s;
        mSequence = seq;
        mDirection = direction;
        mAlternate = alt;
    }

    /**
     * Special constructor to delay Section initialization
     */
    public TransitSection(String secName, int seq, int direction, boolean alt) {
        tSectionName = secName;
        mSequence = seq;
        mDirection = direction;
        mAlternate = alt;
        needsInitialization = true;
    }

    // instance variables
    private Section mSection = null;
    private int mSequence = 0;
    private int mDirection = 0;
    private ArrayList<TransitSectionAction> mTransitSectionActionList = new ArrayList<TransitSectionAction>();
    private boolean mAlternate = false;

    // temporary variables and method for delayed initialization of Section
    private String tSectionName = "";
    private boolean needsInitialization = false;

    private void initialize() {
        if (tSectionName.equals("null")) {
            log.error("Null Section Name when initializing a TransitSection");
        } else {
            mSection = InstanceManager.sectionManagerInstance().getSection(tSectionName);
            if (mSection == null) {
                log.error("Missing Section - " + tSectionName + " - when initializing a TransitSection");
            }
        }
        needsInitialization = false;
    }

    boolean temporary = false;

    public void setTemporary(boolean boo) {
        temporary = boo;
    }

    public boolean isTemporary() {
        return temporary;
    }

    /**
     * Access methods
     */
    public Section getSection() {
        if (needsInitialization) {
            initialize();
        }
        return mSection;
    }

    public String getSectionName() {
        if (needsInitialization) {
            initialize();
        }
        String s = mSection.getSystemName();
        String u = mSection.getUserName();
        if ((u != null) && (!u.equals(""))) {
            return (s + "( " + u + " )");
        }
        return s;
    }

    // Note: once TransitSection is created, Section and its sequence and direction may not be changed.
    public int getDirection() {
        return mDirection;
    }

    public int getSequenceNumber() {
        return mSequence;
    }

    public void addAction(TransitSectionAction act) {
        mTransitSectionActionList.add(act);
    }

    public boolean isAlternate() {
        return mAlternate;
    }

    public void setAlternate(boolean alt) {
        mAlternate = alt;
    }

    /**
     * Get a copy of this TransitSection's TransitSectionAction list
     */
    public ArrayList<TransitSectionAction> getTransitSectionActionList() {
        ArrayList<TransitSectionAction> list = new ArrayList<TransitSectionAction>();
        for (int i = 0; i < mTransitSectionActionList.size(); i++) {
            list.add(mTransitSectionActionList.get(i));
        }
        return list;
    }

    private final static Logger log = LoggerFactory.getLogger(TransitSection.class.getName());
}

/* @(#)TransitSection.java */
