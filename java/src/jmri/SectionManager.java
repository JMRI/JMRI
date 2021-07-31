package jmri;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.managers.AbstractManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class SectionManager extends AbstractManager<Section> implements InstanceManagerAutoDefault {

    public SectionManager() {
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
        y = new Section(sysName, userName);
        // save in the maps
        register(y);

        // Keep track of the last created auto system name
        updateAutoNumber(systemName);

        return y;
    }
    
    /**
     * Return an existing Section or
     * Create a new Section if the Section does not exist.
     *
     * @param systemName the desired system name
     * @param userName   the desired user name
     * @return existing section, by UserName search then SystemName, or creates new Section.
     * @throws IllegalArgumentException if there is trouble creating a new Section.
     */
    @Nonnull
    public Section provideSection(@CheckForNull String systemName, @CheckForNull String userName) throws IllegalArgumentException {
        // Check that Section does not already exist
        Section y;
        if (userName != null && !userName.isEmpty()) {
            y = getByUserName(userName);
            if (y != null) {
                return y;
            }
        }
        if ( systemName==null || systemName.isEmpty()) {
            throw new IllegalArgumentException("Section System Name Empty, username " + userName + " not found");
        }
        
        String sysName = systemName;
        if (!sysName.startsWith(getSystemNamePrefix())) {
            sysName = makeSystemName(sysName);
        }
        
        y = getBySystemName(sysName);
        if (y != null) {
            return y;
        }
        // Section does not exist, create a new Section
        y = new Section(sysName, userName);
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
     * @param frame   ignored
     * @param lePanel the panel containing sections to validate
     * @return number or validation errors; -2 is returned if there are no
     *         sections
     */
    public int validateAllSections(jmri.util.JmriJFrame frame, LayoutEditor lePanel) {
        Set<Section> set = getNamedBeanSet();
        int numSections = 0;
        int numErrors = 0;
        if (set.size() <= 0) {
            return -2;
        }
        for (Section section : set) {
            String s = section.validate(lePanel);
            if (!s.isEmpty()) {
                log.error(s);
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
     * @param lePanel the panel containing direction sensors
     * @return the number or errors; 0 if no errors; -1 if the panel is null; -2
     *         if there are no sections
     */
    public int setupDirectionSensors(LayoutEditor lePanel) {
        if (lePanel == null) {
            return -1;
        }
        Set<Section> set = getNamedBeanSet();
        int numSections = 0;
        int numErrors = 0;
        if (set.size() <= 0) {
            return -2;
        }
        for (Section section : set) {
            int errors = section.placeDirectionSensors(lePanel);
            numErrors = numErrors + errors;
            numSections++;
        }
        log.debug("Checked direction sensors for {} Sections - {} errors or warnings.", numSections, numErrors);
        return numErrors;
    }

    /**
     * Remove direction sensors from SSL for all signals.
     *
     * @param lePanel the panel containing direction sensors
     * @return the number or errors; 0 if no errors; -1 if the panel is null; -2
     *         if there are no sections
     */
    public int removeDirectionSensorsFromSSL(LayoutEditor lePanel) {
        if (lePanel == null) {
            return -1;
        }
        jmri.jmrit.display.layoutEditor.ConnectivityUtil cUtil = lePanel.getConnectivityUtil();
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
        SignalHeadManager shManager = InstanceManager.getDefault(SignalHeadManager.class);
        for (SignalHead sh : shManager.getNamedBeanSet()) {
            if (!cUtil.removeSensorsFromSignalHeadLogic(sensorList, sh)) {
                numErrors++;
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
    
    
    public void generateBlockSections() {
        //find blocks with no paths through i.e. stub (siding)
        LayoutBlockManager LayoutBlockManager = InstanceManager.getDefault(LayoutBlockManager.class);
        //print "Layout Block"
        for (LayoutBlock layoutBlock : LayoutBlockManager.getNamedBeanSet()){
            if (layoutBlock.getNumberOfThroughPaths() == 0){
                if (!blockSectionExists(layoutBlock)){
                    //create block section"
                    createBlockSection(layoutBlock);
                }
            }
        }
    }
                               
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
      
    private void createBlockSection(LayoutBlock layoutBlock){
        Section section;
        try {
            section = createNewSection(layoutBlock.getUserName());
        }
        catch (IllegalArgumentException ex){
            log.error("Could not create Section from LayoutBlock {}",layoutBlock.getDisplayName());
            return;
        }
        section.addBlock(layoutBlock.getBlock());
        ArrayList<EntryPoint> entryPointList = new ArrayList<>();
        Block sb = layoutBlock.getBlock();
        List <Path> paths = sb.getPaths();
        for (int j=0; j<paths.size(); j++){
            Path p = paths.get(j);
            if (p.getBlock() != sb){
                //this is path to an outside block, so need an Entry Point
                String pbDir = Path.decodeDirection(p.getFromBlockDirection());
                EntryPoint ep = new EntryPoint(sb, p.getBlock(), pbDir);
                entryPointList.add(ep);
            }
        }
                
        Block beginBlock = sb;
        // Set directions where possible
        List <EntryPoint> epList = getBlockEntryPointsList(beginBlock,entryPointList);
        if (epList.size() == 1) {
            (epList.get(0)).setTypeForward();
        }  
        Block endBlock = sb;
        epList = getBlockEntryPointsList(endBlock, entryPointList);
        if (epList.size() == 1) {
            (epList.get(0)).setTypeReverse();          
        }
            
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

    private final static Logger log = LoggerFactory.getLogger(SectionManager.class);

}
