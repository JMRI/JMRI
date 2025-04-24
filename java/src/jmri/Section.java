package jmri;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Sections represent a group of one or more connected Blocks that may be
 * allocated to a train traveling in a given direction.
 * <p>
 * A Block may be in multiple Sections. All Blocks contained in a given section
 * must be unique. Blocks are kept in order--the first block is connected to the
 * second, the second is connected to the third, etc.
 * <p>
 * A Block in a Section must be connected to the Block before it (if there is
 * one) and to the Block after it (if there is one), but may not be connected to
 * any other Block in the Section. This restriction is enforced when a Section
 * is created, and checked when a Section is loaded from disk.
 * <p>
 * A Section has a "direction" defined by the sequence in which Blocks are added
 * to the Section. A train may run through a Section in either the forward
 * direction (from first block to last block) or reverse direction (from last
 * block to first block).
 * <p>
 * A Section has one or more EntryPoints. Each EntryPoint is a Path of one of
 * the Blocks in the Section that defines a connection to a Block outside of the
 * Section. EntryPoints are grouped into two lists: "forwardEntryPoints" - entry
 * through which will result in a train traveling in the "forward" direction
 * "reverseEntryPoints" - entry through which will result in a train traveling
 * in the "reverse" direction Note that "forwardEntryPoints" are also reverse
 * exit points, and vice versa.
 * <p>
 * A Section has one of the following states" FREE - available for allocation by
 * a dispatcher FORWARD - allocated for travel in the forward direction REVERSE
 * - allocated for travel in the reverse direction
 * <p>
 * A Section has an occupancy. A Section is OCCUPIED if any of its Blocks is
 * OCCUPIED. A Section is UNOCCUPIED if all of its Blocks are UNOCCUPIED
 * <p>
 * A Section of may be allocated to only one train at a time, even if the trains
 * are travelling in the same direction. If a Section has sufficient space for
 * multiple trains travelling in the same direction it should be broken up into
 * multiple Sections so the trains can follow each other through the original
 * Section.
 * <p>
 * A Section may not contain any reverse loops. The track that is reversed in a
 * reverse loop must be in a separate Section.
 * <p>
 * Each Section optionally carries two direction sensors, one for the forward
 * direction and one for the reverse direction. These sensors force signals for
 * travel in their respective directions to "RED" when they are active. When the
 * Section is free, both the sensors are Active. These internal sensors follow
 * the state of the Section, permitting signals to function normally in the
 * direction of allocation.
 * <p>
 * Each Section optionally carries two stopping sensors, one for the forward
 * direction and one for the reverse direction. These sensors change to active
 * when a train traversing the Section triggers its sensing device. Stopping
 * sensors are physical layout sensors, and may be either point sensors or
 * occupancy sensors for short blocks at the end of the Section. A stopping
 * sensor is used during automatic running to stop a train that has reached the
 * end of its allocated Section. This is needed, for example, to allow a train
 * to enter a passing siding and clear the track behind it. When not running
 * automatically, these sensors may be used to light panel lights to notify the
 * dispatcher that the train has reached the end of the Section.
 * <p>
 * This Section implementation provides for delayed initialization of blocks and
 * direction sensors to be independent of order of items in panel files.
 *
 * @author Dave Duchamp  Copyright (C) 2008,2010
 * @author Bob Jacobsen  Copyright (C) 2022
 */
public interface Section extends NamedBean {

    /**
     * The value of {@link #getState()} if section is available for allocation.
     */
    int FREE = 0x02;

    /**
     * The value of {@link #getState()} if section is allocated for travel in
     * the forward direction.
     */
    int FORWARD = 0x04;

    /**
     * The value of {@link #getState()} if section is allocated for travel in
     * the reverse direction.
     */
    int REVERSE = 0X08;

    /**
     * Value representing an occupied section.
     */
    int OCCUPIED = Block.OCCUPIED;

    /**
     * Value representing an unoccupied section.
     */
    int UNOCCUPIED = Block.UNOCCUPIED;

    /**
     * String constant for the property occupancy.
     */
    String PROPERTY_OCCUPANCY = "occupancy";

    /**
     * Provide generic access to internal state.
     * <p>
     * This generally shouldn't be used by Java code; use the class-specific
     * form instead (e.g. setCommandedState in Turnout). This is provided to
     * make scripts access easier to read.
     * <p>
     * This isn't an exact override because it doesn't throw JmriException
     *
     * @param newState the state
     */
    @Override
    void setState(int newState);

    /**
     * Get the occupancy of a Section.
     *
     * @return {@link #OCCUPIED}, {@link #UNOCCUPIED}, or the state of the first
     *         block that is neither occupied or unoccupied
     */
    int getOccupancy();

    String getForwardBlockingSensorName();

    @CheckForNull
    Sensor getForwardBlockingSensor();

    Sensor setForwardBlockingSensorName(String forwardSensor);

    void delayedSetForwardBlockingSensorName(String forwardSensor);

    String getReverseBlockingSensorName();

    Sensor setReverseBlockingSensorName(String reverseSensor);

    void delayedSetReverseBlockingSensorName(String reverseSensor);

    @CheckForNull
    Sensor getReverseBlockingSensor();

    @CheckForNull
    Block getLastBlock();

    String getForwardStoppingSensorName();

    @CheckForNull
    Sensor getForwardStoppingSensor();

    Sensor setForwardStoppingSensorName(String forwardSensor);

    void delayedSetForwardStoppingSensorName(String forwardSensor);

    String getReverseStoppingSensorName();

    @CheckForNull
    Sensor setReverseStoppingSensorName(String reverseSensor);

    void delayedSetReverseStoppingSensorName(String reverseSensor);

    @CheckForNull
    Sensor getReverseStoppingSensor();

    /**
     * Add a Block to the Section. Block and sequence number must be unique
     * within the Section. Block sequence numbers are set automatically as
     * blocks are added.
     *
     * @param b the block to add
     * @return true if Block was added or false if Block does not connect to the
     *         current Block, or the Block is not unique.
     */
    boolean addBlock(Block b);

    void delayedAddBlock(String blockName);

    /**
     * Get a list of blocks in this section
     *
     * @return a list of blocks
     */
    @Nonnull
    List<Block> getBlockList();

    /**
     * Gets the number of Blocks in this Section
     *
     * @return the number of blocks
     */
    int getNumBlocks();

    /**
     * Get the scale length of Section. Length of the Section is calculated by
     * summing the lengths of all Blocks in the section. If all Block lengths
     * have not been entered, length will not be correct.
     *
     * @param meters true to return length in meters, false to use feet
     * @param scale  the scale; one of {@link jmri.Scale}
     * @return the scale length
     */
    float getLengthF(boolean meters, Scale scale);

    int getLengthI(boolean meters, Scale scale);

    /**
     * Gets the actual length of the Section without any scaling
     *
     * @return the real length in millimeters
     */
    int getActualLength();

    /**
     * Get Block by its Sequence number in the Section.
     *
     * @param seqNumber the sequence number
     * @return the block or null if the sequence number is invalid
     */
    @CheckForNull
    Block getBlockBySequenceNumber(int seqNumber);

    /**
     * Get the sequence number of a Block.
     *
     * @param b the block to get the sequence of
     * @return the sequence number of b or -1 if b is not in the Section
     */
    int getBlockSequenceNumber(Block b);

    /**
     * Remove all Blocks, Block Listeners, and Entry Points
     */
    void removeAllBlocksFromSection();

    @CheckForNull
    Block getEntryBlock();

    @CheckForNull
    Block getNextBlock();

    @CheckForNull
    Block getExitBlock();

    boolean containsBlock(Block b);

    boolean connectsToBlock(Block b);

    String getBeginBlockName();

    String getEndBlockName();

    void addToForwardList(EntryPoint ep);

    void addToReverseList(EntryPoint ep);

    void removeEntryPoint(EntryPoint ep);

    @Nonnull
    List<EntryPoint> getForwardEntryPointList();

    @Nonnull
    List<EntryPoint> getReverseEntryPointList();

    @Nonnull
    List<EntryPoint> getEntryPointList();

    boolean isForwardEntryPoint(EntryPoint ep);

    boolean isReverseEntryPoint(EntryPoint ep);

    /**
     * Get the EntryPoint for entry from the specified Section for travel in
     * specified direction.
     *
     * @param s   the section
     * @param dir the direction of travel; one of {@link #FORWARD} or
     *            {@link #REVERSE}
     * @return the entry point or null if not found
     */
    @CheckForNull
    EntryPoint getEntryPointFromSection(Section s, int dir);

    /**
     * Get the EntryPoint for exit to specified Section for travel in the
     * specified direction.
     *
     * @param s   the section
     * @param dir the direction of travel; one of {@link #FORWARD} or
     *            {@link #REVERSE}
     * @return the entry point or null if not found
     */
    @CheckForNull
    EntryPoint getExitPointToSection(Section s, int dir);

    /**
     * Get the EntryPoint for entry from the specified Block for travel in the
     * specified direction.
     *
     * @param b   the block
     * @param dir the direction of travel; one of {@link #FORWARD} or
     *            {@link #REVERSE}
     * @return the entry point or null if not found
     */
    @CheckForNull
    EntryPoint getEntryPointFromBlock(Block b, int dir);

    /**
     * Get the EntryPoint for exit to the specified Block for travel in the
     * specified direction.
     *
     * @param b   the block
     * @param dir the direction of travel; one of {@link #FORWARD} or
     *            {@link #REVERSE}
     * @return the entry point or null if not found
     */
    @CheckForNull
    EntryPoint getExitPointToBlock(Block b, int dir);

    /**
     * Place direction sensors in SSL for all Signal Heads in this Section if
     * the Sensors are not already present in the SSL.
     * <p>
     * Only anchor point block boundaries that have assigned signals are
     * considered. Only turnouts that have assigned signals are considered. Only
     * level crossings that have assigned signals are considered. Turnouts and
     * anchor points without signals are counted, and reported in warning
     * messages during this procedure, if there are any missing signals.
     * <p>
     * If this method has trouble, an error message is placed in the log
     * describing the trouble.
     *
     * @return the number or errors placing sensors; 1 is returned if no
     *         direction sensor is defined for this section
     */
    int placeDirectionSensors();

    /**
     * Validate the Section. This checks block connectivity, warns of redundant
     * EntryPoints, and otherwise checks internal consistency of the Section. An
     * appropriate error message is logged if a problem is found. This method
     * assumes that Block Paths are correctly initialized.
     *
     * @return an error description or empty string if there are no errors
     */
    String validate();

    /**
     * Set/reset the display to use alternate color for unoccupied blocks in
     * this section. If Layout Editor panel is not present, Layout Blocks will
     * not be present, and nothing will be set.
     *
     * @param set true to use alternate unoccupied color; false otherwise
     */
    void setAlternateColor(boolean set);

    /**
     * Set/reset the display to use alternate color for unoccupied blocks in
     * this Section. If the Section already contains an active block, then the
     * alternative color will be set from the active block, if no active block
     * is found or we are clearing the alternative color then all the blocks in
     * the Section will be set. If Layout Editor panel is not present, Layout
     * Blocks will not be present, and nothing will be set.
     *
     * @param set true to use alternate unoccupied color; false otherwise
     */
    void setAlternateColorFromActiveBlock(boolean set);

    /**
     * Set the block values for blocks in this Section.
     *
     * @param name the value to set all blocks to
     */
    void setNameInBlocks(String name);

    /**
     * Set the block values for blocks in this Section.
     *
     * @param value the name to set block values to
     */
    void setNameInBlocks(Object value);

    void setNameFromActiveBlock(Object value);

    /**
     * Clear the block values for blocks in this Section.
     */
    void clearNameInUnoccupiedBlocks();

    /**
     * Suppress the update of a memory variable when a block goes to unoccupied,
     * so the text set above doesn't get wiped out.
     *
     * @param set true to suppress the update; false otherwise
     */
    void suppressNameUpdate(boolean set);

    @SuppressWarnings("hiding")     // Field has same name as a field in the super class
    enum SectionType {
        DYNAMICADHOC,   // Created on an as required basis, not to be saved.
        USERDEFINED,    // Default Save all the information
        SIGNALMASTLOGIC // Save only the name, blocks will be added by the signalmast logic
    }
    SectionType USERDEFINED     = SectionType.USERDEFINED;
    SectionType SIGNALMASTLOGIC = SectionType.SIGNALMASTLOGIC;
    SectionType DYNAMICADHOC    = SectionType.DYNAMICADHOC;

    /**
     * Set Section Type.
     * <ul>
     * <li>USERDEFINED - Default Save all the information.
     * <li>SIGNALMASTLOGIC - Save only the name, blocks will be added by the SignalMast logic.
     * <li>DYNAMICADHOC - Created on an as required basis, not to be saved.
     * </ul>
     * @param type constant of section type.
     */
    void setSectionType(SectionType type);

    /**
     * Get Section Type.
     * Defaults to USERDEFINED.
     * @return constant of section type.
     */
    SectionType getSectionType();

}
