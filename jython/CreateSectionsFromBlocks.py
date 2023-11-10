# CreateSectionsFromBlocks.py - SteveT - 05/09/2023
#  For each defined occupancy block, attempts to create a one-block Section.
#  The systemname of the section is based on the systemname of the (required) occupancy sensor for the Block.
#  It will add a comment to any Sections which need additional manual intervention.
#  Intended to be run once when creating a new layout Editor panel, such as from AnyRail.
# Based on Bill Fitch's CreateSignalLogicAndSections.py
#
# NOTE: to enable jython logging, see https://www.jmri.org/help/en/html/apps/Debug.shtml
# Add the Logger Category name "jmri.jmrit.jython.exec" at DEBUG Level.

import jmri
import java
import org.slf4j.LoggerFactory
import re

def getBlockEntryPointsList(b, entryPointList) :
    list = java.util.ArrayList();
    for i in range( 0, entryPointList.size()) :
        ep = entryPointList.get(i)
        if (ep.getBlock() == b) :
            list.add(ep)
    return list

def createBlockSection(layout_block, sectionName, sectionUserName):
    def getBlockEntryPointsList(b, entryPointList) :
        list = java.util.ArrayList();
        for i in range( 0, entryPointList.size()) :
            ep = entryPointList.get(i)
            if (ep.getBlock() == b) :
                list.add(ep)
        return list
   
    SectionManager = jmri.InstanceManager.getDefault(jmri.SectionManager)
    sb = layout_block.getBlock()
    section = SectionManager.createNewSection(sectionName, sectionUserName)
    section.addBlock(sb)
    entryPointList = java.util.ArrayList()
    paths= sb.getPaths()
    for j in range(0, paths.size()):
        p = paths.get(j)
        if p.getBlock() != sb:
            #this is path to an outside block, so need an Entry Point
            pbDir = jmri.Path.decodeDirection(p.getFromBlockDirection())
            # ep = getEntryPointInList(oldList, sb, p.getBlock(), pbDir)
            # if (ep == None) :
            ep = jmri.EntryPoint(sb, p.getBlock(), pbDir)
            entryPointList.add(ep)

    beginBlock = sb
    # Set directions where possible
    epList = getBlockEntryPointsList(beginBlock,entryPointList)
    #print epList
    if ((epList.size() == 2)):
        log.debug('Block {} has {} entry pts, directions auto-set.'.format(layout_block.getUserName(), epList.size()))
        if (((epList.get(0)).isUnknownType()) \
                and ((epList.get(1)).isUnknownType())) :
            (epList.get(0)).setTypeForward()
            (epList.get(1)).setTypeReverse()
    elif (epList.size() == 1) :
        log.debug('Block {} has {} entry pts, directions auto-set.'.format(layout_block.getUserName(), epList.size()))
        (epList.get(0)).setTypeForward()
    else : 
        log.warn('Block {} has {} entry pts, you must set directions manually'.format(layout_block.getUserName(), epList.size()))
        section.setComment("Need Manual EntryPoints ({})".format(epList.size()))
        
    for j in range(0, entryPointList.size()) :
        ep = entryPointList.get(j)
        if ep.isForwardType():
            section.addToForwardList(ep)
        elif ep.isReverseType():
            section.addToReverseList(ep)

log = org.slf4j.LoggerFactory.getLogger("jmri.jmrit.jython.exec.script.CreateSectionsFromBlocks")

log.info( "CreateSectionsFromBlocks.py started" )
sectionsCreated = 0 #count successes

blocksSet = set(blocks.getNamedBeanSet()) # use a copy for the loop
for blockBean in blocksSet :
    # if (blocksCreated > 5) : continue
    
    blockSystemName = str ( blockBean )
    block = blocks.getBlock(blockSystemName)
    layoutBlock = layoutblocks.getLayoutBlock(block)
    blockUserName = block.getUserName()
    blockSensor = block.getSensor()
    if (blockSensor == None) :
        log.warn("Block {} has no occupancy sensor, skipped".format(blockSystemName))
        continue
    blockSensorName = blockSensor.getSystemName()       
    result = re.search("(\d+)", blockSensorName)
    if (result == None) :
        log.warn("Sensor {} not in expected format, skipping".format(blockSensorName))
        continue
    blockNumber = result.group(1)
    sectionName = "IY" + blockNumber
    result = sections.getSection(sectionName)
    if (result) :
        log.warn("Section {} already exists, skipping".format(sectionName))
        continue  
        
    createBlockSection(layoutBlock, sectionName, blockUserName) #create one Section for each block       
    sectionsCreated += 1

log.info("CreateSectionsFromBlocks.py complete. {} Sections created.".format(sectionsCreated))
