package jmri.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Block;
import jmri.BlockManager;
import jmri.EntryPoint;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.Manager;
import jmri.Path;
import jmri.Section;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;

import jmri.implementation.DefaultSection;

import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutBlock;

/**
 * Basic Implementation of a SectionManager.
 * <p>
 * This doesn't have a "new" interface, since Sections are independently
 * implemented, instead of being system-specific.
 * <p>
 * Note that Section system names must begin with system prefix and type character,
 * usually IY, and be followed by a string, usually, but not always, a number. This
 * is enforced when a Section is created.
 * <br>
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
 * @author Dave Duchamp Copyright (C) 2008
 */
public class DefaultSectionManager extends AbstractManager<Section> implements jmri.SectionManager {

    public DefaultSectionManager() {
        super();
        addListeners();
    }

    final void addListeners() {
        InstanceManager.getDefault(SensorManager.class).addVetoableChangeListener(this);
        InstanceManager.getDefault(BlockManager.class).addVetoableChangeListener(this);
    }

    @Override
    public int getXMLOrder() {
        return Manager.SECTIONS;
    }

    @Override
    public char typeLetter() {
        return 'Y';
    }

    @Override
    public Class<Section> getNamedBeanClass() {
        return Section.class;
    }

    /**
     * Create a new Section if the Section does not exist.
     *
     * @param systemName the desired system name
     * @param userName   the desired user name
     * @return a new Section or
     * @throws IllegalArgumentException if a Section with the same systemName or
     *         userName already exists, or if there is trouble creating a new
     *         Section.
     */
    @Override
    @Nonnull
    public Section createNewSection(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        Objects.requireNonNull(systemName, "SystemName cannot be null. UserName was " + ((userName == null) ? "null" : userName));  // NOI18N
        // check system name
        if (systemName.isEmpty()) {
            throw new IllegalArgumentException("Section System Name Empty");
            // no valid system name entered, return without creating
        }
        String sysName = systemName;
        if (!sysName.startsWith(getSystemNamePrefix())) {
            sysName = makeSystemName(sysName);
        }
        // Check that Section does not already exist
        Section y;
        if (userName != null && !userName.isEmpty()) {
            y = getByUserName(userName);
            if (y != null) {
                throw new IllegalArgumentException("Section Already Exists with UserName " + userName);
            }
        }
        y = getBySystemName(sysName);
        if (y != null) {
            throw new IllegalArgumentException("Section Already Exists with SystemName " + sysName);
        }
        // Section does not exist, create a new Section
        y = new DefaultSection(sysName, userName);
        // save in the maps
        register(y);

        // Keep track of the last created auto system name
        updateAutoNumber(systemName);

        return y;
    }

    /**
     * Create a New Section with Auto System Name.
     * @param userName UserName for new Section
     * @return new Section with Auto System Name.
     * @throws IllegalArgumentException if existing Section, or
     *          unable to create a new Section.
     */
    @Override
    @Nonnull
    public Section createNewSection(String userName) throws IllegalArgumentException {
        return createNewSection(getAutoSystemName(), userName);
    }

    /**
     * Remove an existing Section.
     *
     * @param y the section to remove
     */
    @Override
    public void deleteSection(Section y) {
        // delete the Section
        deregister(y);
        y.dispose();
    }

    /**
     * Get an existing Section. First look up assuming that name is a User
     * Name. If this fails look up assuming that name is a System Name.
     *
     * @param name the name to find; user names are searched for a match first,
     *             followed by system names
     * @return the found section of null if no matching Section found
     */
    @Override
    @CheckForNull
    public Section getSection(String name) {
        Section y = getByUserName(name);
        if (y != null) {
            return y;
        }
        return getBySystemName(name);
    }

    /**
     * Validate all Sections.
     *
     * @return number or validation errors; -2 is returned if there are no sections
     */
    @Override
    public int validateAllSections() {
        Set<Section> set = getNamedBeanSet();
        int numSections = 0;
        int numErrors = 0;
        if (set.size() <= 0) {
            return -2;
        }
        for (Section section : set) {
            String s = section.validate();
            if (!s.isEmpty()) {
                log.error("Validate result for section {}: {}", section.getDisplayName(), s);
                numErrors++;
            }
            numSections++;
        }
        log.debug("Validated {} Sections - {} errors or warnings.", numSections, numErrors);
        return numErrors;
    }

    /**
     * Check direction sensors in SSL for signals.
     *
     * @return the number or errors; 0 if no errors; -1 if the panel is null; -2 if there are no sections
     */
    @Override
    public int setupDirectionSensors() {
        Set<Section> set = getNamedBeanSet();
        int numSections = 0;
        int numErrors = 0;
        if (set.size() <= 0) {
            return -2;
        }
        for (Section section : set) {
            int errors = section.placeDirectionSensors();
            numErrors += errors;
            numSections++;
        }
        log.debug("Checked direction sensors for {} Sections - {} errors or warnings.", numSections, numErrors);
        return numErrors;
    }

    /**
     * Remove direction sensors from SSL for all signals.
     *
     * @return the number or errors; 0 if no errors; -1 if the panel is null; -2 if there are no sections
     */
    @Override
    public int removeDirectionSensorsFromSSL() {
        Set<Section> set = getNamedBeanSet();
        if (set.size() <= 0) {
            return -2;
        }
        int numErrors = 0;
        List<String> sensorList = new ArrayList<>();
        for (Section s : set) {
            String name = s.getReverseBlockingSensorName();
            if ((name != null) && (!name.isEmpty())) {
                sensorList.add(name);
            }
            name = s.getForwardBlockingSensorName();
            if ((name != null) && (!name.isEmpty())) {
                sensorList.add(name);
            }
        }

        var editorManager = InstanceManager.getDefault(EditorManager.class);
        var shManager = InstanceManager.getDefault(SignalHeadManager.class);

        for (var panel : editorManager.getAll(LayoutEditor.class)) {
            var cUtil = panel.getConnectivityUtil();
            for (SignalHead sh : shManager.getNamedBeanSet()) {
                if (!cUtil.removeSensorsFromSignalHeadLogic(sensorList, sh)) {
                    numErrors++;
                }
            }
        }
        return numErrors;
    }

    /**
     * Initialize all blocking sensors that exist - set them to 'ACTIVE'.
     */
    @Override
    public void initializeBlockingSensors() {
        Sensor sensor;
        for (Section s : getNamedBeanSet()) {
            try {
                sensor = s.getForwardBlockingSensor();
                if (sensor != null) {
                    sensor.setState(Sensor.ACTIVE);
                }
                sensor = s.getReverseBlockingSensor();
                if (sensor != null) {
                    sensor.setState(Sensor.ACTIVE);
                }
            } catch (JmriException reason) {
                log.error("Exception when initializing blocking Sensors for Section {}", s.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME));
            }
        }
    }

    /**
     * Generate Block Sections in stubs/sidings. Called after generating SML based sections.
     */

    /**
     * A list of blocks that will be used to create a block based section.
     */
    private List<Block> blockList;

    /**
     * Find stub end blocks.
     */
    @Override
    public void generateBlockSections() {
        //find blocks with no paths through i.e. stub (siding)
        LayoutBlockManager layoutBlockManager = InstanceManager.getDefault(LayoutBlockManager.class);

        for (LayoutBlock layoutBlock : layoutBlockManager.getNamedBeanSet()){
            if (layoutBlock.getNumberOfThroughPaths() == 0){
                if (!blockSectionExists(layoutBlock)){
                    createBlockSection(layoutBlock);
                }
            }
        }
    }

    /**
     * Check if a block based section has a first block that matches.
     * @param layoutBlock
     * @return true or false
     */
    private boolean blockSectionExists(LayoutBlock layoutBlock){
        for (Section section : getNamedBeanSet()){
            if (section.getNumBlocks() > 0 && section.getSectionType() != Section.SIGNALMASTLOGIC) {
                if (layoutBlock.getBlock().equals(section.getBlockList().get(0))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Create a block section that has one or more blocks.  The initial block is one that has
     * no through paths, which will normally be track segments that end at an end bumper (EB).
     * Incomplete track arrangements can also mimmic this behavior.
     * <p>
     * The first phase calls a recursive method to build a list of blocks.
     * The second phase creates the section with an entry point from the next section.
     * @param layoutBlock The starting layout block.
     */
    private void createBlockSection(LayoutBlock layoutBlock){
        blockList = new ArrayList<>();
        var block = layoutBlock.getBlock();
        createSectionBlockList(block);

        if (blockList.isEmpty()) {
            log.error("No blocks found for layout block '{}'", layoutBlock.getDisplayName());
            return;
        }

        // Create a new section using the block name(s) as the section name.
        var sectionName = blockList.get(0).getDisplayName();
        if (blockList.size() > 1) {
            sectionName = sectionName + ":::" + blockList.get(blockList.size() - 1).getDisplayName();
        }

        Section section;
        try {
            section = createNewSection(sectionName);
        }
        catch (IllegalArgumentException ex){
            log.error("Could not create Section for layout block '{}'",layoutBlock.getDisplayName());
            return;
        }

        blockList.forEach( blk -> section.addBlock(blk));

        // Create entry point
        Block lastBlock = blockList.get(blockList.size() - 1);
        Block nextBlock = null;
        String pathDirection = "";

        for (Path path : lastBlock.getPaths()) {
            var checkBlock = path.getBlock();
            if (!blockList.contains(checkBlock)) {
                nextBlock = checkBlock;
                pathDirection = Path.decodeDirection(path.getFromBlockDirection());
                break;
            }
        }

        if (nextBlock == null) {
            log.error("Unable to find a next block after block '{}'", lastBlock.getDisplayName());
            return;
        }
        log.debug("last = {}, next = {}", lastBlock.getDisplayName(), nextBlock.getDisplayName());

        EntryPoint ep = new EntryPoint(lastBlock, nextBlock, pathDirection);
        ep.setTypeReverse();
        section.addToReverseList(ep);
    }

    /**
     * Recursive calls to find a block that is a facing block for SML, a block that has more than
     * 2 neighbors, or the recursion limit of 100 is reached
     * @param block The current block being processed.
     */
    private void createSectionBlockList(@Nonnull Block block) {
        blockList.add(block);
        if (blockList.size() < 100) {
            var nextBlock = getNextBlock(block);
            if (nextBlock != null) {
                createSectionBlockList(nextBlock);
            }
        }
    }

    /**
     * Get the next block if this one is not the last block.
     * The last block is one that is a SML facing block.
     * The other restriction is only 1 or 2 neighbors.
     * @param block The block to be checked.
     * @return the next block or null if it is the last block.
     */
    @CheckForNull
    private Block getNextBlock(@Nonnull Block block) {
        var lbmManager = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
        var smlManager = InstanceManager.getDefault(jmri.SignalMastLogicManager.class);
        var layoutBlock = lbmManager.getLayoutBlock(block);

        if (layoutBlock == null) {
            return null;
        }

        // If the current block is a SML facing block, the next block is not needed.
        for (jmri.SignalMastLogic sml : smlManager.getSignalMastLogicList()) {
            if (sml.getFacingBlock().equals(layoutBlock)) {
                return null;
            }
        }

        Block nextBlock = null;
        switch (layoutBlock.getNumberOfNeighbours()) {
            case 0:
                log.debug("No neighbors for layout block '{}'", layoutBlock.getDisplayName());
                break;

            case 1:
                nextBlock = layoutBlock.getNeighbourAtIndex(0);
                break;

            case 2:
                nextBlock = layoutBlock.getNeighbourAtIndex(0);
                if (blockList.contains(nextBlock)) {
                    nextBlock = layoutBlock.getNeighbourAtIndex(1);
                }
                break;

            default:
                log.debug("More than 2 neighbors for layout block '{}'", layoutBlock.getDisplayName());
                nextBlock = getNextConnectedBlock(layoutBlock);
        }
        return nextBlock;
    }

    /**
     * Attempt to find the next block when there are multiple connections.  Track segments have
     * two connections but blocks with turnouts can have any number of connections.
     * <p>
     * The checkValidDest method in getLayoutBlockConnectivityTools is used to find the first valid
     * connection between the current block, its facing block and the possible destination blocks.
     * @param currentBlock The layout block with more than 2 connections.
     * @return the next block or null.
     */
    @CheckForNull
    private Block getNextConnectedBlock(LayoutBlock currentBlock) {
        var lbmManager = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
        var lbTools = lbmManager.getLayoutBlockConnectivityTools();
        var pathMethod = jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools.Routing.NONE;

        // The facing block is the one before the current block or the first block.
        var index = blockList.size() - 2;
        if (index < 0) {
            index = 0;
        }
        var facingBlock = lbmManager.getLayoutBlock(blockList.get(index));
        if (facingBlock == null) {
            log.error("The facing block not found for current block '{}'", currentBlock.getDisplayName());
            return null;
        }

        for (int i = 0; i < currentBlock.getNumberOfNeighbours(); i++) {
            var dest = currentBlock.getNeighbourAtIndex(i);
            var destBlock = lbmManager.getLayoutBlock(dest);
            try {
                boolean valid = lbTools.checkValidDest(facingBlock,
                    currentBlock, destBlock, new ArrayList<>(), pathMethod);
                if (valid) {
                    return dest;
                }
            } catch (JmriException ex) {
                log.error("getNextConnectedBlock exeption: {}", ex.getMessage());
            }
        }

        return null;
    }

    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameSections" : "BeanNameSection");
    }

    @Override
    public void dispose(){
        InstanceManager.getDefault(SensorManager.class).removeVetoableChangeListener(this);
        InstanceManager.getDefault(BlockManager.class).removeVetoableChangeListener(this);
        super.dispose();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSectionManager.class);

}
