package jmri;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import jmri.implementation.AbstractNamedBean;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Transit is a group of Sections representing a specified path through a
 * layout.
 * <p>
 * A Transit may have the following states:
 * <dl>
 * <dt>IDLE</dt>
 * <dd>available for assignment to a train</dd>
 * <dt>ASSIGNED</dt>
 * <dd>linked to a train in an {@link jmri.jmrit.dispatcher.ActiveTrain}</dd>
 * </dl>
 * <p>
 * When assigned to a Transit, options may be set for the assigned Section. The
 * Section and its options are kept in a {@link jmri.TransitSection} object.
 * <p>
 * To accommodate passing sidings and other track features, there may be
 * multiple Sections connecting two other Sections in a Transit. If so, one
 * Section is assigned as primary, and the other connecting Sections are
 * assigned as alternates.
 * <p>
 * A Section may be in a Transit more than once, for example if a train is to
 * make two or more loops around a layout before going elsewhere.
 * <p>
 * A Transit is normally traversed in the forward direction, that is, the
 * direction of increasing Section Numbers. When a Transit traversal is started
 * up, it is always started in the forward direction. However, to accommodate
 * point-to-point (back and forth) route designs, the direction of travel in a
 * Transit may be "reversed". While the Transit direction is "reversed", the
 * direction of travel is the direction of decreasing Section numbers. Whether a
 * Transit is in the "reversed" direction is kept in the ActiveTrain using the
 * Transit.
 *
 * @author Dave Duchamp Copyright (C) 2008-2011
 */
public class Transit extends AbstractNamedBean {

    /**
     * The idle, or available for assignment to an ActiveTrain state.
     */
    public static final int IDLE = 0x02;
    /**
     * The assigned to an ActiveTrain state.
     */
    public static final int ASSIGNED = 0x04;

    /*
     * Instance variables (not saved between runs)
     */
    private int mState = Transit.IDLE;
    private final ArrayList<TransitSection> mTransitSectionList = new ArrayList<>();
    private int mMaxSequence = 0;
    private final ArrayList<Integer> blockSecSeqList = new ArrayList<>();
    private final ArrayList<Integer> destBlocksSeqList = new ArrayList<>();

    public Transit(String systemName, String userName) {
        super(systemName, userName);
    }

    public Transit(String systemName) {
        super(systemName);
    }

    /**
     * Query the state of this Transit.
     *
     * @return {@link #IDLE} or {@link #ASSIGNED}
     */
    @Override
    public int getState() {
        return mState;
    }

    /**
     * Set the state of this Transit.
     *
     * @param state {@link #IDLE} or {@link #ASSIGNED}
     */
    @Override
    public void setState(int state) {
        if ((state == Transit.IDLE) || (state == Transit.ASSIGNED)) {
            int old = mState;
            mState = state;
            firePropertyChange("state", old, mState);
        } else {
            log.error("Attempt to set Transit state to illegal value - " + state);
        }
    }

    /**
     * Add a Section to this Transit.
     *
     * @param s the Section object to add
     */
    public void addTransitSection(TransitSection s) {
        mTransitSectionList.add(s);
        mMaxSequence = s.getSequenceNumber();
    }

    /**
     * Get the list of TransitSections.
     *
     * @return a copy of the internal list of TransitSections or an empty list
     */
    public ArrayList<TransitSection> getTransitSectionList() {
        return new ArrayList<>(mTransitSectionList);
    }

    /**
     * Get the maximum sequence number used in this Transit.
     *
     * @return the maximum sequence
     */
    public int getMaxSequence() {
        return mMaxSequence;
    }

    /**
     * Remove all TransitSections in this Transit.
     */
    public void removeAllSections() {
        mTransitSectionList.clear();
    }

    /**
     * Check if a Section is in this Transit.
     *
     * @param s the section to check for
     * @return true if the section is present; false otherwise
     */
    public boolean containsSection(Section s) {
        return mTransitSectionList.stream().anyMatch((ts) -> (ts.getSection() == s));
    }

    /**
     * Get a List of Sections with a given sequence number.
     *
     * @param seq the sequence number
     * @return the list of of matching sections or an empty list if none
     */
    public ArrayList<Section> getSectionListBySeq(int seq) {
        ArrayList<Section> list = new ArrayList<>();
        for (TransitSection ts : mTransitSectionList) {
            if (seq == ts.getSequenceNumber()) {
                list.add(ts.getSection());
            }
        }
        return list;
    }

    /**
     * Get a List of TransitSections with a given sequence number.
     *
     * @param seq the sequence number
     * @return the list of of matching sections or an empty list if none
     */
    public ArrayList<TransitSection> getTransitSectionListBySeq(int seq) {
        ArrayList<TransitSection> list = new ArrayList<>();
        for (TransitSection ts : mTransitSectionList) {
            if (seq == ts.getSequenceNumber()) {
                list.add(ts);
            }
        }
        return list;
    }

    /**
     * Get a List of sequence numbers for a given Section.
     *
     * @param s the section to match
     * @return the list of matching sequence numbers or an empty list if none
     */
    public ArrayList<Integer> getSeqListBySection(Section s) {
        ArrayList<Integer> list = new ArrayList<>();
        for (TransitSection ts : mTransitSectionList) {
            if (s == ts.getSection()) {
                list.add(ts.getSequenceNumber());
            }
        }
        return list;
    }

    /**
     * Check if a Block is in this Transit.
     *
     * @param block the block to check for
     * @return true if block is present; false otherwise
     */
    public boolean containsBlock(Block block) {
        for (Block b : getInternalBlocksList()) {
            if (b == block) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the number of times a Block is in this Transit.
     *
     * @param block the block to check for
     * @return the number of times block is present; 0 if block is not present
     */
    public int getBlockCount(Block block) {
        int count = 0;
        for (Block b : getInternalBlocksList()) {
            if (b == block) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get a Section from one of its Blocks and its sequence number.
     *
     * @param b   the block within the Section
     * @param seq the sequence number of the Section
     * @return the Section or null if no matching Section is present
     */
    public Section getSectionFromBlockAndSeq(Block b, int seq) {
        for (TransitSection ts : mTransitSectionList) {
            if (ts.getSequenceNumber() == seq) {
                Section s = ts.getSection();
                if (s.containsBlock(b)) {
                    return s;
                }
            }
        }
        return null;
    }

    /**
     * Get Section from one of its EntryPoint Blocks and its sequence number.
     *
     * @param b   the connecting block to the Section
     * @param seq the sequence number of the Section
     * @return the Section or null if no matching Section is present
     */
    public Section getSectionFromConnectedBlockAndSeq(Block b, int seq) {
        for (TransitSection ts : mTransitSectionList) {
            if (ts.getSequenceNumber() == seq) {
                Section s = ts.getSection();
                if (s.connectsToBlock(b)) {
                    return s;
                }
            }
        }
        return null;
    }

    /**
     * Get the direction of a Section in the transit from its sequence number.
     *
     * @param s   the Section to check
     * @param seq the sequence number of the Section
     * @return the direction of the Section (one of {@link jmri.Section#FORWARD}
     *         or {@link jmri.Section#REVERSE} or zero if s and seq are not in a
     *         TransitSection together
     */
    public int getDirectionFromSectionAndSeq(Section s, int seq) {
        for (TransitSection ts : mTransitSectionList) {
            if ((ts.getSection() == s) && (ts.getSequenceNumber() == seq)) {
                return ts.getDirection();
            }
        }
        return 0;
    }

    /**
     * Get a TransitSection in the transit from its Section and sequence number.
     *
     * @param s   the Section to check
     * @param seq the sequence number of the Section
     * @return the transit section or null if not found
     */
    public TransitSection getTransitSectionFromSectionAndSeq(Section s, int seq) {
        for (TransitSection ts : mTransitSectionList) {
            if ((ts.getSection() == s) && (ts.getSequenceNumber() == seq)) {
                return ts;
            }
        }
        return null;
    }

    /**
     * Get a list of all blocks internal to this Transit. Since Sections may be
     * present more than once, blocks may be listed more than once. The sequence
     * numbers of the Section the Block was found in are accumulated in a
     * parallel list, which can be accessed by immediately calling
     * {@link #getBlockSeqList()}.
     *
     * @return the list of all Blocks or an empty list if none are present
     */
    public ArrayList<Block> getInternalBlocksList() {
        ArrayList<Block> list = new ArrayList<>();
        blockSecSeqList.clear();
        mTransitSectionList.forEach((ts) -> {
            ts.getSection().getBlockList().stream().forEach((b) -> {
                list.add(b);
                blockSecSeqList.add(ts.getSequenceNumber());
            });
        });
        return list;
    }

    /**
     * Get a list of sequence numbers in this Transit. This list is generated by
     * calling {@link #getInternalBlocksList()} or
     * {@link #getEntryBlocksList()}.
     *
     * @return the list of all sequence numbers or an empty list if no Blocks
     *         are present
     */
    public ArrayList<Integer> getBlockSeqList() {
        return new ArrayList<>(blockSecSeqList);
    }

    /**
     * Get a list of all entry Blocks to this Transit. These are Blocks that a
     * Train might enter from and be going in the direction of this Transit. The
     * sequence numbers of the Section the Block will enter are accumulated in a
     * parallel list, which can be accessed by immediately calling
     * {@link #getBlockSeqList()}.
     *
     * @return the list of all blocks or an empty list if none are present
     */
    public ArrayList<Block> getEntryBlocksList() {
        ArrayList<Block> list = new ArrayList<>();
        ArrayList<Block> internalBlocks = getInternalBlocksList();
        blockSecSeqList.clear();
        for (TransitSection ts : mTransitSectionList) {
            List<EntryPoint> ePointList;
            if (ts.getDirection() == Section.FORWARD) {
                ePointList = ts.getSection().getForwardEntryPointList();
            } else {
                ePointList = ts.getSection().getReverseEntryPointList();
            }
            for (EntryPoint ep : ePointList) {
                Block eb = ep.getFromBlock();
                boolean isInternal = false;
                for (Block ib : internalBlocks) {
                    if (eb == ib) {
                        isInternal = true;
                    }
                }
                if (!isInternal) {
                    // not an internal Block, keep it
                    list.add(eb);
                    blockSecSeqList.add(ts.getSequenceNumber());
                }
            }
        }
        return list;
    }

    /**
     * Get a list of all destination blocks that can be reached from a specified
     * starting block. The sequence numbers of the Sections destination blocks
     * were found in are accumulated in a parallel list, which can be accessed
     * by immediately calling {@link #getDestBlocksSeqList()}.
     * <p>
     * <strong>Note:</strong> A Train may not terminate in the same Section in
     * which it starts.
     * <p>
     * <strong>Note:</strong> A Train must terminate in a Block within the
     * Transit.
     *
     * @param startBlock     the starting Block to find destinations for
     * @param startInTransit true if startBlock is within this Transit; false
     *                       otherwise
     * @return a list of destination Blocks or an empty list if none exist
     */
    public ArrayList<Block> getDestinationBlocksList(Block startBlock, boolean startInTransit) {
        ArrayList<Block> list = new ArrayList<>();
        destBlocksSeqList.clear();
        if (startBlock == null) {
            return list;
        }
        // get the sequence number of the Section of the starting Block
        int startSeq = -1;
        ArrayList<Block> startBlocks;
        if (startInTransit) {
            startBlocks = getInternalBlocksList();
        } else {
            startBlocks = getEntryBlocksList();
        }
        // programming note: the above calls initialize blockSecSeqList.
        for (int k = 0; ((k < startBlocks.size()) && (startSeq == -1)); k++) {
            if (startBlock == startBlocks.get(k)) {
                startSeq = (blockSecSeqList.get(k));
            }
        }
        ArrayList<Block> internalBlocks = getInternalBlocksList();
        for (int i = internalBlocks.size(); i > 0; i--) {
            if (blockSecSeqList.get(i - 1) > startSeq) {
                // could stop in this block, keep it
                list.add(internalBlocks.get(i - 1));
                destBlocksSeqList.add(blockSecSeqList.get(i - 1));
            }
        }
        return list;
    }

    /**
     * Get a list of destination Block sequence numbers in this Transit. This
     * list is generated by calling
     * {@link #getDestinationBlocksList(jmri.Block, boolean)}.
     *
     * @return the list of all destination Block sequence numbers or an empty
     *         list if no destination Blocks are present
     */
    public ArrayList<Integer> getDestBlocksSeqList() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < destBlocksSeqList.size(); i++) {
            list.add(destBlocksSeqList.get(i));
        }
        return list;
    }

    /**
     * Check if this Transit is capable of continuous running.
     * <p>
     * A Transit is capable of continuous running if, after an Active Train
     * completes the Transit, it can automatically be restarted. To be
     * restartable, the first Section and the last Section must be the same
     * Section, and the first and last Sections must be defined to run in the
     * same direction. If the last Section is an alternate Section, the previous
     * Section is tested. However, if the Active Train does not complete its
     * Transit in the same Section it started in, the restart will not take
     * place.
     *
     * @return true if continuous running is possible; otherwise false
     */
    public boolean canBeResetWhenDone() {
        TransitSection firstTS = mTransitSectionList.get(0);
        int lastIndex = mTransitSectionList.size() - 1;
        TransitSection lastTS = mTransitSectionList.get(lastIndex);
        boolean OK = false;
        while (!OK) {
            if (firstTS.getSection() != lastTS.getSection()) {
                if (lastTS.isAlternate() && (lastIndex > 1)) {
                    lastIndex--;
                    lastTS = mTransitSectionList.get(lastIndex);
                } else {
                    log.warn("Section mismatch " + (firstTS.getSection()).getSystemName() + " "
                            + (lastTS.getSection()).getSystemName());
                    return false;
                }
            }
            OK = true;
        }
        // same Section, check direction
        if (firstTS.getDirection() != lastTS.getDirection()) {
            log.warn("Direction mismatch " + (firstTS.getSection()).getSystemName() + " "
                    + (lastTS.getSection()).getSystemName());
            return false;
        }
        return true;
    }

    /**
     * Checks that exit Signal Heads are in place for all Sections in this
     * Transit and for Block boundaries at turnouts or level crossings within
     * Sections of the Transit for the direction defined in this Transit. Signal
     * Heads are not required at anchor point block boundaries where both blocks
     * are within the same Section, and for turnouts with two or more
     * connections in the same Section.
     *
     * @param panel the panel to check against
     * @return 0 if all Sections have all required signals or the number of
     *         Sections missing required signals; -1 if the panel is null
     */
    public int checkSignals(LayoutEditor panel) {
        if (panel == null) {
            log.error("checkSignals called with a null LayoutEditor panel");
            return -1;
        }
        int numErrors = 0;
        for (TransitSection ts : mTransitSectionList) {
            numErrors = numErrors + ts.getSection().placeDirectionSensors(panel);
        }
        return numErrors;
    }

    /**
     * Validates connectivity through the Transit. Returns the number of errors
     * found. Sends log messages detailing the errors if break in connectivity
     * is detected. Checks all Sections before quitting.
     *
     * @param panel the panel containing Sections to validate
     * @return number of invalid sections or -1 if panel if null
     */
    public int validateConnectivity(LayoutEditor panel) {
        if (panel == null) {
            log.error("validateConnectivity called with a null LayoutEditor panel");
            return -1;
        }
        int numErrors = 0;
        for (int i = 0; i < mTransitSectionList.size(); i++) {
            String s = mTransitSectionList.get(i).getSection().validate(panel);
            if (!s.equals("")) {
                log.error(s);
                numErrors++;
            }
        }
        return numErrors;
    }

    /**
     * Initialize blocking sensors for Sections in this Transit. This should be
     * done before any Sections are allocated for this Transit. Only Sections
     * that are {@link jmri.Section#FREE} are initialized, so as not to
     * interfere with running active trains. If any Section does not have
     * blocking sensors, warning messages are logged.
     *
     * @return 0 if no errors, number of errors otherwise.
     */
    public int initializeBlockingSensors() {
        int numErrors = 0;
        for (int i = 0; i < mTransitSectionList.size(); i++) {
            Section s = mTransitSectionList.get(i).getSection();
            try {
                if (s.getForwardBlockingSensor() != null) {
                    if (s.getState() == Section.FREE) {
                        s.getForwardBlockingSensor().setState(Sensor.ACTIVE);
                    }
                } else {
                    log.warn("Missing forward blocking sensor for section " + s.getSystemName());
                    numErrors++;
                }
            } catch (JmriException reason) {
                log.error("Exception when initializing forward blocking Sensor for Section " + s.getSystemName());
                numErrors++;
            }
            try {
                if (s.getReverseBlockingSensor() != null) {
                    if (s.getState() == Section.FREE) {
                        s.getReverseBlockingSensor().setState(Sensor.ACTIVE);
                    }
                } else {
                    log.warn("Missing reverse blocking sensor for section " + s.getSystemName());
                    numErrors++;
                }
            } catch (JmriException reason) {
                log.error("Exception when initializing reverse blocking Sensor for Section " + s.getSystemName());
                numErrors++;
            }
        }
        return numErrors;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "UC_USELESS_OBJECT",
            justification = "SpotBugs doesn't see that toBeRemoved is being read by the forEach clause")
    public void removeTemporarySections() {
        ArrayList<TransitSection> toBeRemoved = new ArrayList<>();
        for (TransitSection ts : mTransitSectionList) {
            if (ts.isTemporary()) {
                toBeRemoved.add(ts);
            }
        }
        toBeRemoved.forEach((ts) -> {
            mTransitSectionList.remove(ts);
        });
    }

    public boolean removeLastTemporarySection(Section s) {
        TransitSection last = mTransitSectionList.get(mTransitSectionList.size() - 1);
        if (last.getSection() != s) {
            log.info("Section asked to be removed is not the last one");
            return false;
        }
        if (!last.isTemporary()) {
            log.info("Section asked to be removed is not a temporary section");
            return false;
        }
        mTransitSectionList.remove(last);
        return true;

    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameTransit");
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // NOI18N
            NamedBean nb = (NamedBean) evt.getOldValue();
            if (nb instanceof Section) {
                if (containsSection((Section) nb)) {
                    throw new PropertyVetoException(Bundle.getMessage("VetoTransitSection", getDisplayName()), evt);
                }
            }
        }
        // we ignore the property setConfigureManager
    }

    private final static Logger log = LoggerFactory.getLogger(Transit.class);

}
