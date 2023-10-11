package jmri;

import java.util.ArrayList;

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
public interface Transit extends NamedBean {

    /**
     * The idle, or available for assignment to an ActiveTrain state.
     */
    int IDLE = 0x02;
    /**
     * The assigned to an ActiveTrain state.
     */
    int ASSIGNED = 0x04;

    /**
     * Set the state of this Transit.
     *
     * @param state {@link #IDLE} or {@link #ASSIGNED}
     */
    @Override
    void setState(int state);

    /**
     * Add a Section to this Transit.
     *
     * @param s the Section object to add
     */
    void addTransitSection(TransitSection s);

    /**
     * Get the list of TransitSections.
     *
     * @return a copy of the internal list of TransitSections or an empty list
     */
    ArrayList<TransitSection> getTransitSectionList();

    /**
     * Get the maximum sequence number used in this Transit.
     *
     * @return the maximum sequence
     */
    int getMaxSequence();

    /**
     * Remove all TransitSections in this Transit.
     */
    void removeAllSections();

    /**
     * Check if a Section is in this Transit.
     *
     * @param s the section to check for
     * @return true if the section is present; false otherwise
     */
    boolean containsSection(Section s);

    /**
     * Get a List of Sections with a given sequence number.
     *
     * @param seq the sequence number
     * @return the list of of matching sections or an empty list if none
     */
    ArrayList<Section> getSectionListBySeq(int seq);

    /**
     * Get a List of TransitSections with a given sequence number.
     *
     * @param seq the sequence number
     * @return the list of of matching sections or an empty list if none
     */
    ArrayList<TransitSection> getTransitSectionListBySeq(int seq);

    /**
     * Get a List of sequence numbers for a given Section.
     *
     * @param s the section to match
     * @return the list of matching sequence numbers or an empty list if none
     */
    ArrayList<Integer> getSeqListBySection(Section s);

    /**
     * Check if a Block is in this Transit.
     *
     * @param block the block to check for
     * @return true if block is present; false otherwise
     */
    boolean containsBlock(Block block);

    /**
     * Get the number of times a Block is in this Transit.
     *
     * @param block the block to check for
     * @return the number of times block is present; 0 if block is not present
     */
    int getBlockCount(Block block);

    /**
     * Get a Section from one of its Blocks and its sequence number.
     *
     * @param b   the block within the Section
     * @param seq the sequence number of the Section
     * @return the Section or null if no matching Section is present
     */
    Section getSectionFromBlockAndSeq(Block b, int seq);

    /**
     * Get Section from one of its EntryPoint Blocks and its sequence number.
     *
     * @param b   the connecting block to the Section
     * @param seq the sequence number of the Section
     * @return the Section or null if no matching Section is present
     */
    Section getSectionFromConnectedBlockAndSeq(Block b, int seq);

    /**
     * Get the direction of a Section in the transit from its sequence number.
     *
     * @param s   the Section to check
     * @param seq the sequence number of the Section
     * @return the direction of the Section (one of {@link jmri.Section#FORWARD}
     *         or {@link jmri.Section#REVERSE} or zero if s and seq are not in a
     *         TransitSection together
     */
    int getDirectionFromSectionAndSeq(Section s, int seq);

    /**
     * Get a TransitSection in the transit from its Section and sequence number.
     *
     * @param s   the Section to check
     * @param seq the sequence number of the Section
     * @return the transit section or null if not found
     */
    TransitSection getTransitSectionFromSectionAndSeq(Section s, int seq);

    /**
     * Get a list of all blocks internal to this Transit. Since Sections may be
     * present more than once, blocks may be listed more than once. The sequence
     * numbers of the Section the Block was found in are accumulated in a
     * parallel list, which can be accessed by immediately calling
     * {@link #getBlockSeqList()}.
     *
     * @return the list of all Blocks or an empty list if none are present
     */
    ArrayList<Block> getInternalBlocksList();

    /**
     * Get a list of sequence numbers in this Transit. This list is generated by
     * calling {@link #getInternalBlocksList()} or
     * {@link #getEntryBlocksList()}.
     *
     * @return the list of all sequence numbers or an empty list if no Blocks
     *         are present
     */
    ArrayList<Integer> getBlockSeqList();

    /**
     * Get a list of all entry Blocks to this Transit. These are Blocks that a
     * Train might enter from and be going in the direction of this Transit. The
     * sequence numbers of the Section the Block will enter are accumulated in a
     * parallel list, which can be accessed by immediately calling
     * {@link #getBlockSeqList()}.
     *
     * @return the list of all blocks or an empty list if none are present
     */
    ArrayList<Block> getEntryBlocksList();

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
    ArrayList<Block> getDestinationBlocksList(Block startBlock, boolean startInTransit);

    /**
     * Get a list of destination Block sequence numbers in this Transit. This
     * list is generated by calling
     * {@link #getDestinationBlocksList(jmri.Block, boolean)}.
     *
     * @return the list of all destination Block sequence numbers or an empty
     *         list if no destination Blocks are present
     */
    ArrayList<Integer> getDestBlocksSeqList();

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
    boolean canBeResetWhenDone();

    /**
     * Initialize blocking sensors for Sections in this Transit. This should be
     * done before any Sections are allocated for this Transit. Only Sections
     * that are {@link jmri.Section#FREE} are initialized, so as not to
     * interfere with running active trains. If any Section does not have
     * blocking sensors, warning messages are logged.
     *
     * @return 0 if no errors, number of errors otherwise.
     */
    int initializeBlockingSensors();

    void removeTemporarySections();

    boolean removeLastTemporarySection(Section s);

}
