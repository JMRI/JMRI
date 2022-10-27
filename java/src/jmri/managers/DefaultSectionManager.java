package jmri.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;

import jmri.*;

import jmri.implementation.DefaultSection;

import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.layoutEditor.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
    @Nonnull
    public Section createNewSection(String userName) throws IllegalArgumentException {
        return createNewSection(getAutoSystemName(), userName);
    }

    /**
     * Remove an existing Section.
     *
     * @param y the section to remove
     */
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
    public int setupDirectionSensors() {
        Set<Section> set = getNamedBeanSet();
        int numSections = 0;
        int numErrors = 0;
        if (set.size() <= 0) {
            return -2;
        }
        for (Section section : set) {
            int errors = section.placeDirectionSensors();
            numErrors = numErrors + errors;
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
    public void initializeBlockingSensors() {
        for (Section s : getNamedBeanSet()) {
            try {
                if (s.getForwardBlockingSensor() != null) {
                    s.getForwardBlockingSensor().setState(Sensor.ACTIVE);
                }
                if (s.getReverseBlockingSensor() != null) {
                    s.getReverseBlockingSensor().setState(Sensor.ACTIVE);
                }
            } catch (JmriException reason) {
                log.error("Exception when initializing blocking Sensors for Section {}", s.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME));
            }
        }
    }

    /**
     * Generate Block Sections in stubs/sidings. Called after generating signal logic.
     */

    /**
     * Check if Block Section already exists
     * @param layoutBlock
     * @return true or false
     */
    private boolean blockSectionExists(LayoutBlock layoutBlock){

        for (Section section : getNamedBeanSet()){
            if (section.getNumBlocks() == 1
                    && section.getSectionType() != Section.SIGNALMASTLOGIC
                    && layoutBlock.getBlock().equals(section.getEntryBlock())){
                return true;
            }
        }
        return false;
    }

    public void generateBlockSections() {
        //find blocks with no paths through i.e. stub (siding)
        LayoutBlockManager LayoutBlockManager = InstanceManager.getDefault(LayoutBlockManager.class);
        //// print "Layout Block"
        for (LayoutBlock layoutBlock : LayoutBlockManager.getNamedBeanSet()){
            if (layoutBlock.getNumberOfThroughPaths() == 0){
                if (!blockSectionExists(layoutBlock)){
                    //create block section"
                    createBlockSection(layoutBlock);
                }
            }
        }
    }

    private void createBlockSection(LayoutBlock layoutBlock) {
         /*
         Method
         get block at siding b, and call createBlockSection
         create sectionBlockList: set origin_block = b curr_block = b prev_block = None
         call createSections
         get blocks from curr_block (paths) and iterate through them
         each iteration set nextBlock, do not use if nextBlock == prevBlock
         get anchors of nextBlock, we want the ones which goes to the one after next
         get the blocks either side of each anchor
         if one of those blocks is currBlock eliminate that anchor
         get the signal masts connected to the anchor
         get the direction of the nextBlock, get the corresponding signal mast
         if the signal mast exists finish, and set the entry points (i=only one because stub/siding)
         otherwise call createSections again
          */
        Block layoutBlk = layoutBlock.getBlock();
        ArrayList<Block> sectionBlockList = new ArrayList<>();
        int index = 0;
        Block originBlock = layoutBlk;
        Block currBlock = layoutBlk;
        Block prevBlock = null;
        String indent = "  ";    //used to indent the debug statements
        createSections(originBlock, currBlock, prevBlock, sectionBlockList, index, indent);
    }

    private void createSections(Block originBlock, Block currBlock, Block prevBlock,
                                ArrayList<Block>sectionBlockList, int index, String indent){

        index += 1;
        // print Indent + "index", index
        sectionBlockList.add(currBlock);
        List <Path>paths = currBlock.getPaths();
        //log.debug ( "{} paths {} size {} block {}", indent, paths, paths.size(), currBlock.getUserName());
        for (int j=0; j<paths.size(); j++){
            Path p = paths.get(j);
            Block nextBlock = p.getBlock();
            log.debug( "{}****", indent);
            log.debug( "{} iterating through paths {} nextBlock {}", indent, j, nextBlock.getUserName());
            if (isNull(prevBlock) || nextBlock != prevBlock){
                log.debug("{}", indent);
                log.debug("{} index {} path interation {} path {} block {} ", indent, index, j, p, nextBlock.getUserName());
                if (isNull(prevBlock)) {
                    log.debug("{} nextBlock {} must not equal prevBlock {}", indent, nextBlock.getUserName(), "null");
                }else{
                    log.debug("{} nextBlock {} must not equal prevBlock {}", indent, nextBlock.getUserName(), prevBlock.getUserName());
                }
                String pbDir = Path.decodeDirection(p.getFromBlockDirection());
                // get the panel and cutil for this Block
                LayoutBlockManager LayoutBlockManager = InstanceManager.getDefault(LayoutBlockManager.class);
                LayoutBlock lBlock = LayoutBlockManager.getLayoutBlock(currBlock);
                LayoutEditor panel = lBlock.getMaxConnectedPanel();
                if (isNull(panel)) {
                    log.error("Unable to get a panel for '{}' in 'createSections'", currBlock.getDisplayName());
                    continue;
                }
                ConnectivityUtil cUtil = new ConnectivityUtil(panel);
                List<PositionablePoint> anchors = cUtil.getAnchorBoundariesThisBlock(nextBlock);
                int a = 0;
                for(PositionablePoint p1 : anchors){
                    a +=1;
                    log.debug ("{} before check anchor blocks {} :  {} must not be current block {}",
                        indent, p1.getConnect1().getBlockName(), p1.getConnect2().getBlockName(),
                        currBlock.getUserName());
                    boolean Connect1_OK, Connect2_OK;
                    if(!p1.getConnect1().getBlockName().equals(currBlock.getUserName()) &&
                            !p1.getConnect2().getBlockName().equals(currBlock.getUserName())){
                        log.debug ("{} after check anchor blocks {} :  {} must not be current block {}",
                                indent, p1.getConnect1().getBlockName(), p1.getConnect2().getBlockName(),
                                currBlock.getUserName());
                        log.debug("{} passed check  p1 {} pbDir {}", indent, p1, pbDir);
                        NamedBeanHandle<SignalMast> sme = p1.getEastBoundSignalMastNamed();
                        NamedBeanHandle<SignalMast> smw = p1.getWestBoundSignalMastNamed();
                        if (isNull(sme)){
                            log.debug( "{} sme = null", indent);
                        }else{
                            log.debug("{} sme = {}", indent, sme);
                        }
                        if (isNull(smw)){
                            log.debug( "{} smw = null", indent);
                        }else{
                            log.debug("{} smw = {}", indent, smw);
                        }
                        NamedBeanHandle<SignalMast> sm;
                        if (pbDir != "East") {
                            sm = p1.getEastBoundSignalMastNamed();
                        } else {
                            sm = p1.getWestBoundSignalMastNamed();
                        }
                        if (isNull(sm)){
                            log.debug( "{} sm = null", indent);
                        }else{
                            log.debug("{} sm = {}", indent, sm);
                        }
                        if (nonNull(sm)) {
                            Section section;
                            String sectionName = originBlock.getUserName() + ":" + sm.getName();
                            // print Indent + "sec_name", sec_name
                            log.debug("{} sectionName = {}", indent, sectionName);
                            try {
                                section = createNewSection(sectionName);
                            } catch (IllegalArgumentException ex) {
                                log.error("Could not create Section named {}", sectionName);
                                return;
                            }
                            log.debug("{} sectionBlockList = {}", indent, sectionBlockList);
                            for (Block blk : sectionBlockList) {
                                section.addBlock(blk);
                            }
                            log.debug("{} finished", indent);
                            setEntryPoints(currBlock, nextBlock, pbDir, section);
                        } else {
                            indent = indent + "  ";    // increase indent
                            createSections(originBlock, nextBlock, currBlock, sectionBlockList, index, indent);
                            indent = indent.substring(2);
                        }
                    }else{
                        log.warn("anchor {} {} not valid in createSections", a , p1 );
                    }
                }
            }
        }
    }

    private void setEntryPoints(Block curr_block, Block next_block, String pbDir, Section section) {

        ArrayList<EntryPoint> entryPointList = new ArrayList<>();
        EntryPoint ep1 = new EntryPoint(curr_block, next_block, pbDir);
        entryPointList.add(ep1);

//        Block beginBlock = ep.getBlock();          // don't need this because eminating from stub
//        // Set directions where possible
//        List <EntryPoint> epList = getBlockEntryPointsList(beginBlock, entryPointList)
//        if( epList.size() == 1) {
//            epList.get(0).setTypeForward();
//        }

        Block endBlock = ep1.getBlock();
        List <EntryPoint> epList = getBlockEntryPointsList(endBlock, entryPointList);
        if( epList.size() == 1) {
            (epList.get(0)).setTypeReverse();
        }

        // print "entryPointList.size()", entryPointList.size(), "entryPointList", entryPointList
        for (int j=0; j<entryPointList.size(); j++){
            EntryPoint ep = entryPointList.get(j);
            if (ep.isForwardType()){
                section.addToForwardList(ep);
            }else if (ep.isReverseType()){
                section.addToReverseList(ep);
            }
        }
    }

    private List <EntryPoint> getBlockEntryPointsList(Block b, List <EntryPoint> entryPointList) {
        List <EntryPoint> list = new ArrayList<>();
        for (int i=0; i<entryPointList.size(); i++) {
            EntryPoint ep = entryPointList.get(i);
            if (ep.getBlock().equals(b)) {
                list.add(ep);
            }
        }
        return list;
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

    private final static Logger log = LoggerFactory.getLogger(DefaultSectionManager.class);

}
