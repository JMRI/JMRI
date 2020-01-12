package jmri;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds information and options for a Section when assigned to a
 * Transit. Corresponds to an allocatable "Section" of track assigned to a
 * Transit.
 * <p>
 * A TransitSection holds the following information: Section ID Section
 * Direction Sequence number of Section within the Transit Special actions list
 * for train in this Section, if requested (see TransitSectionAction.java)
 * Whether this Section is a primary section or an alternate section
 * <p>
 * A TransitSection is referenced via a list in its parent Transit, and is
 * stored on disk when its parent Transit is stored.
 * <p>
 * Provides for delayed initializatio of Section when loading panel files, so
 * that this is not dependent on order of items in the panel file.
 *
 * @author Dave Duchamp Copyright (C) 2008
 */
public class TransitSection {

    /**
     * Create a TransitSection. This calls the alternate constructor with false
     * for the alt value.
     *
     * @param s         the section to add to the transit
     * @param seq       the sequence number of the section in the transit
     * @param direction the direction of travel within the transit
     */
    public TransitSection(jmri.Section s, int seq, int direction) {
        this(s, seq, direction, false);
    }

    /**
     * Create an alternate or primary TransitSection.
     *
     * @param s         the section to add to the transit
     * @param seq       the sequence number of the section in the transit
     * @param direction the direction of travel within the transit
     * @param alt       true if the section is an alternate; false if section is
     *                  primary or has no alternates
     */
    public TransitSection(jmri.Section s, int seq, int direction, boolean alt) {
        mSection = s;
        mSequence = seq;
        mDirection = direction;
        mAlternate = alt;
    }

    /**
     * Create an alternate or primary TransitSection, and defined as safe or not
     *
     * @param s         the section to add to the transit
     * @param seq       the sequence number of the section in the transit
     * @param direction the direction of travel within the transit
     * @param alt       true if the section is an alternate; false if section is
     *                  primary or has no alternates
     * @param safe      true if this is a safe section. When dispatcher by safe sections
     *                  a train is dispatched safe section to safe section with all intervening sections available.
     * @param stopAllocatingSensorName If this sensor is present, valid, and Active allocation will stop until
     *                  it is no longer Active.
     */
    public TransitSection(jmri.Section s, int seq, int direction, boolean alt, boolean safe, String stopAllocatingSensorName) {
        mSection = s;
        mSequence = seq;
        mDirection = direction;
        mAlternate = alt;
        mSafe = safe;
        mStopAllocatingSensorName = stopAllocatingSensorName;
    }

    /**
     * Create an alternate or primary TransitSection with a delayed
     * initialization.
     *
     * @param secName   the name of the section to add to the transit
     * @param seq       the sequence number of the section in the transit
     * @param direction the direction of travel within the transit
     * @param alt       true if the section is an alternate; false if section is
     *                  primary or has no alternates
     */
    public TransitSection(String secName, int seq, int direction, boolean alt) {
        tSectionName = secName;
        mSequence = seq;
        mDirection = direction;
        mAlternate = alt;
        needsInitialization = true;
    }

    /**
     * Create an alternate or primary TransitSection with a delayed
     * initialization.
     *
     * @param secName   the name of the section to add to the transit
     * @param seq       the sequence number of the section in the transit
     * @param direction the direction of travel within the transit
     * @param alt       true if the section is an alternate; false if section is
     *                  primary or has no alternates
     * @param safe      true if this is a safe section. When dispatcher by safe sections
     *                  a train is dispatched safe section to safe section with all intervening sections available.
     * @param stopAllocatingSensorName If this sensor is present, valid, and Active allocation will stop until
     *                  it is no longer Active.
     */
    public TransitSection(String secName, int seq, int direction, boolean alt, boolean safe, String stopAllocatingSensorName) {
        tSectionName = secName;
        mSequence = seq;
        mDirection = direction;
        mAlternate = alt;
        mSafe = safe;
        mStopAllocatingSensorName = stopAllocatingSensorName;
        needsInitialization = true;
    }

    // instance variables
    private Section mSection = null;
    private int mSequence = 0;
    private int mDirection = 0;
    private final ArrayList<TransitSectionAction> mTransitSectionActionList = new ArrayList<>();
    private boolean mAlternate = false;
    private boolean mSafe = false;
    private String mStopAllocatingSensorName = "";

    // temporary variables and method for delayed initialization of Section
    private String tSectionName = "";
    private boolean needsInitialization = false;

    private void initialize() {
        if (tSectionName.equals("null")) {
            log.error("Null Section Name when initializing a TransitSection");
        } else {
            mSection = InstanceManager.getDefault(jmri.SectionManager.class).getSection(tSectionName);
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
     * Get the associated section.
     *
     * @return the section or null if no section is associated with this
     *         TransitSection
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

    public boolean isSafe() {
        return mSafe;
    }

    public void setSafe(boolean safe) {
        mSafe = safe;
    }

    public String getStopAllocatingSensor() {
        return mStopAllocatingSensorName;
    }

    public void setStopAllocatingSensor(String stopAllocatingSensor ) {
        mStopAllocatingSensorName = stopAllocatingSensor;
    }


    /**
     * Get a list of the actions for this TransitSection
     *
     * @return a list of actions or an empty list if there are no actions
     */
    public ArrayList<TransitSectionAction> getTransitSectionActionList() {
        return new ArrayList<>(mTransitSectionActionList);
    }

    private final static Logger log = LoggerFactory.getLogger(TransitSection.class);
}
