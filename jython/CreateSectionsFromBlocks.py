# CreateSectionsFromBlocks.py - SteveT - 07/09/2024
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

def createBlockSection(block, sectionName):
    global sectionsFailed   
    section = sections.createNewSection(sectionName, block.getDisplayName())
    section.addBlock(block)
    entryPointList = java.util.ArrayList()
    paths= block.getPaths()
    for j in range(0, paths.size()):
        p = paths.get(j)
        if p.getBlock() != block:
            #this is path to an outside block, so need an Entry Point
            pbDir = jmri.Path.decodeDirection(p.getFromBlockDirection())
            #log.debug("sb={}, p.getBlock={}, pbDir={}",sb.getUserName(), p.getBlock(), pbDir)
            ep = jmri.EntryPoint(block, p.getBlock(), pbDir)
            entryPointList.add(ep)

    beginBlock = block
    # Set directions where possible
    epList = getBlockEntryPointsList(beginBlock,entryPointList)

    fail = False
    # arbitrarily set first travel direction to forward
    ep = epList.get(0)
    ep.setTypeForward()
    fwdDir = ep.getFromBlockDirection() # save forward compass direction
    revDir = None # will be set when we encounter the 2nd compass direction 
        
    # compare rest of eps to first
    for j in range(1, epList.size()) :
        ep = epList.get(j)
        epDir = ep.getFromBlockDirection()
        if (fwdDir == epDir) : # if compass direction matches first, set to forward
            ep.setTypeForward()
        elif (revDir == None or revDir == epDir) :                
            ep.setTypeReverse()
            revDir = epDir # use this compass direction as reverse
        else :
            fail = True # set if a 3rd compass direction found, must handle manually
            continue

    if (fail) :                
        msg = "WARN: Set {} EntryPoints for section {} manually".format(entryPointList.size(), section.getDisplayName())
        section.setComment(msg)                              
        log.warn(msg)
        sectionsFailed += 1
    else :
        log.debug('Section {} directions auto-set, {} entry pts.'.format(section.getDisplayName(), epList.size()))
        # copy the entrypointlist into section's forward and reverse lists
        for j in range(0, epList.size()) :
            ep = epList.get(j)
            #log.debug("ep.getBlock={}, ep.getDirection={}, ep.getFromBlockDirection={}",ep.getBlock().getUserName(), ep.getDirection(), ep.getFromBlockDirection())
            if ep.isForwardType():
                section.addToForwardList(ep)
            elif ep.isReverseType():
                section.addToReverseList(ep)

log = org.slf4j.LoggerFactory.getLogger("jmri.jmrit.jython.exec.script.CreateSectionsFromBlocks")

log.info( "CreateSectionsFromBlocks.py started" )
sectionsCreated = 0 # count successes
sectionsFailed  = 0 # count failures

for block in blocks.getNamedBeanSet() :    
    #if (sectionsCreated > 5) : continue # helps when debugging.
    blockSensor = block.getSensor()
    if (blockSensor == None) :
        log.warn("Block {} has no occupancy sensor, skipped".format(block.getDisplayName()))
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
        
    createBlockSection(block, sectionName) #create one Section for each block       
    sectionsCreated += 1

log.info("CreateSectionsFromBlocks.py complete. {} Sections created, {} Sections need manual completion.".format(sectionsCreated, sectionsFailed))
