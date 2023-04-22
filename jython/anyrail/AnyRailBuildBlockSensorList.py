##  Version 2023-04-19:
#       Essentially rewritten with augmented comments
#       Avoid potential cross-thread contamination and resulting crashes 
#           by minimizing Table windows that get updated as Sensors are
#           created and Blocks are edited.
#       Cope with Blocks exported from unnamed AnyRail Sections.
#       Cope with Blocks exported with Section names containing one or more
#           LowLineChars AKA the "_" character.  Turned out to be troublesome.
#       Challenge Blocks exported with Section names containing only WhiteSpace
#           characters.
#       Extensive print output to the PanelPro Script Output window.
#       Tested with AnyRail Version 6.51.0


##  Version 2023-04-04:     Original upload.
#       Developed with PanelPro version 5.3.4+Raae2040ea3
#           under Java 11.0.13
#           on Windows 10 amd64 v10.0 
#       Tested with AnyRail Version 6.50.2 


#   The AnyRail program, Copyright © 2023 DRail Software, can export a track plan
#   to a JMRI PanelPro XML file with a Layout Editor formatted Panel.

#   See the discussion at https://www.jmri.org/community/connections/AnyRail/index.shtml

#   Fair disclosure:
#       MOST OF WHAT FOLLOWS is dependent upon the English (EN) language choice from
#       within the AnyRail menu:
#               FILE >> Options >> Languages item. 
#                                       NO OTHER LANGUAGE CHOICE HAS BEEN 
#                                       TESTED AS OF THIS TIME
#       AnyRail Version 6.51.0 (Apr 6, 2023)

#   If the track plan has defined "Sections," then there is a one-to-one
#   translation to "Blocks" in the exported Panel file.

#   The first exported PanelPro Block systemName is always "IB1"
#   and the rest of the systemName values are numbered sequentially.

#   An exported Block systemName might be "IB123" for some iteration, but have a
#   different "IB" number the next time the plan is exported.

#   The exported PanelPro Blocks do not have any assigned occupancy Sensors.

#   In fact, no Sensors are defined in the exported file.

#   Each PanelPro user has many options as to how to best define their own
#   occupancy Sensors, but this script implements a simplistic rule.

#   The derived exported PanelPro Block will have a userName that is constructed
#   by concatenating several strings that are separated with LowLineChars.
#       1)  A slight variation on the artificially constructed systemName,
#       2)  The specific AnyRail section name, if actually defined,
#       3)  The pull-down choice for the "Usage" value.

#   If the AnyRail Section name were something like "West Station," and the
#   selected Usage is "Detection," then the exported Block userName might look
#   like "B_123_West Station_Detection" but only if the exported Block systemName
#   were "IB123."

#   To augment the exported PanelPro file, for each named Section and hence each
#   exported Block with systemName "IBxxx", this script will create a new occupancy
#   Sensor with a systemName "ISxxx" with matching digits "xxx." 

#       Usually, the Sensor assigned to the exported Block will be set to
#       the Sensor userName, but some contingency is built in for AnyRail
#       Sections with names that are either not unique or even left blank.

#   If the option is set to rename the exported Block, then it's userName
#   and it's comment field will match those of the created occupancy Sensor.

#   The choices described above might not meet your needs.

#   Each time it becomes necessary to modify the AnyRail track plan and export a 
#   new version of the PanelPro file, the user is forced to rebuild the list of
#   Sensors that define the related Occupancy status for each Block.  After
#   many such revisions and the resultant efforts on a large club layout, the
#   painful need for a script has led to what you are reading now.
#
#   This script automates what would be a tedious and error prone process when
#   done manually.  It does however, require careful attention to the AnyRail
#   track plan details.
#
#   Be sure to investigate the lines marked with IMPORTANT OPTIONS DEFINITIONS 

#   This script is intended be executed EXACTLY ONCE on each NEWLY EXPORTED
#   PanelPro XML file. 

#   ##### Begin CAUTION notification:
#       AnyRail allows duplicated Section names.  It also allows duplicate Label
#       names for Turnouts, but that is not addressed here.
#
#       Due to the script’s method of providing a userName for each Block that
#       is derived from, but NOT directly copied from the AnyRail Section names,
#       the exported file provides unique Block userNames that this script cannot
#       always use.
#
#       This script parses the original Section Name and tries to use
#       it for the provided Sensor userName 
#               but PanelPro does not allow Sensors to have duplicated userNames.
#
#       If the AnyRail track plan contains Sections with duplicated names, only
#       the Block with the lowest numbered Block userName will be assigned an
#       occupancy Sensor with that userName.
#
#   If the script discovers a Section name that was previously used to provide
#   an occupancy Sensor userName, the subsequent Sensors that would have
#   duplicated that name will not be assigned a userName, and the exported
#   Block userName will be used for the new Sensor’s Comment field.
#       For each such case, an ERROR message is provided in the JMRI System Console
#       and the log files.
#   ##### End of CAUTION notification
#
# Author: Cliff Anderson, Copyright 2023, Part of the JMRI distribution

import jmri
import java
import org.slf4j.Logger
import org.slf4j.LoggerFactory

#############  IMPORTANT OPTIONS DEFINITIONS:
# Define the Sensor Prefix for a DCC address (not for a LCC address) or else
# default to a temporary internal address.
defaultSensorPrefix = "IS"  # Can be user modified now or at some later time 
                            # with more control over the hardware address 
                            # from within the 
                            # Tools >> Tables >> Sensors window 
                            # using the "Edit" pull-down menu
                            # to "move" the userNames to newly defined Sensors
                            
##  ONLY if the Block "hardware" addresses match the layout hardware Sensor addresses:
# defaultSensorPrefix = "LS"    # for LocoNet
# defaultSensorPrefix = "L2S"   # for a second LocoNet
# defaultSensorPrefix = "NS"    # for NCE


# Decide to make the Block userName match the Sensor userName
isRenameBlocks = True     #   Match Block userNames with non-empty Sensor userNames
# isRenameBlocks = False    #   Retain AnyRail assigned Block userNames

# Not really an option, but AnyRail might choose to use some other character as a
# separator character in a future AnyRail version.
fragmentSeparator = "_" #   The Low Line character
# recognized by reverse engineering, not from any documentation.

#############  IMPORTANT OPTIONS DEFINITIONS ENDS


# The AnyRailBuildSensorList class controls the action of looping through
# the list of AnyRail exported Blocks and creating the Occupancy Sensors.  
# In spite of an early choice to derive this class from AbstractAutomaton,
# other than the utilization of a separate thread and the labeling on the log
# file, little or no features from the parent class now come into play.  
class AnyRailBuildSensorList(jmri.jmrit.automat.AbstractAutomaton):

##############################
# Predefine class variables for scope protection

##############################
# Script necessity:
    log = org.slf4j.LoggerFactory.getLogger(
                "jmri.jmrit.jython.exec.script.AnyRailBuildSensorList"
            )
##############################
# PanelPro target information:
    SensorSystemName = ""
    SensorUserName   = ""
    SensorComment    = ""

##############################
# AnyRail parsing details:
    # AnyRail provides a complicated userName for each Block with:
    #       a string identifying what Block Number it assigned (the most recent
    #       export anyway)
    #   concatenated with
    #       a string telling what "Section" Name, if one was provided by the
    #       track plan user(s),
    #   concatenated with
    #       a string reporting what "Usage" was assigned from a pull-down list
    #       dialog when the Section was most recently edited.
    #
    #   The AnyRail Usage choices list follows here:
    
    pickUsageList = [ # plagiarized from the AnyRail Sections menu pull-down list
        "Break",
        "Detection",
        "Pass through",
        "Polarization",
        "Powerless",
        "Slow down and accelerate",
        "Stop",
        "Unspecified"
    ]   # In alphabetic order as shown on the AnyRail screen.

##############################
# Java Swing multi-thread issue avoidance strategy, of sorts:
#   Original version would sometimes crash within a Java Swing
#   activity, but only if a Table window was visible on the
#   screen and getting updated as a Block edit or a Sensor Create
#   or Edit.
# 
    dangerousFrameTitleList = [  #   Avoid cross thread contamination
        "Block Table",
        "Blocks",
        "Sensor Table",
        "Sensors"
        ]   # JMRIJFrame titles to be minimized AKA hidden

    # Define an empty list that might get populated.
    hiddenFrames = []   # List JMRIJFrame objects that the
                        # script has minimized

###     The Jython print function to the Script Output window seems to
###     be immune or maybe thread safe.

# END OF Data Declarations
##############################

    # Create an unique Occupancy Sensor for each defined AnyRail Block
    def init(self):

        BlockCounter = 0
        DuplicateSectionNameCounter = 0
        NamelessBlockCounter = 0
        NonAnyRailBlockCounter = 0
        QuestionableSectionNameCounter = 0

        self.log.info( "AnyRailBuildSensorList ver: 2023-04-19" )

        self.log.info( "Creating an Occupancy Sensor for each AnyRail Block" )

        # Prevent potential crash condition
        self.AviodCrossThreadPossibilities() 

        for someBlock in blocks.getNamedBeanSet() :

            ##
            ## For sanity checking, report what the incoming data looks like
            ##
            blockUserName = self.ReportBlock(
                                someBlock,
                                BlockCounter
                                )

            # Protect against some prior assignment of an Occupancy Sensor
            #
            # Unless this script is accidentally executed a second time,
            # it is highly unlikely that there is a prior assigned Sensor
            priorOccupancySensor = someBlock.getSensor()

            if not (priorOccupancySensor == None) :
                # Do not attempt to override any previous assignment
                tempString1 = priorOccupancySensor.getSystemName()
                tempString2 = priorOccupancySensor.getUserName()
                tempString  = 'Previously defined Occupancy Sensor = "' \
                            + tempString1 \
                            + '" with userName = "' \
                            + tempString2 \
                            + '"'
                self.log.warn(tempString)
                print (tempString)
                continue    # skip over the rest of this iteration

            # priorOccupancySensor does not exist

            # pick out the useful information from the exported
            # Block userName noise
            isBlockAnyRail \
                = self.parseBlockUserName(
                        blockUserName
                        )

            if not (isBlockAnyRail) :
                tempString = \
                    'Block Named "{0}" is NOT exported AnyRail - NO new Sensor is created' \
                    .format(blockUserName )
                self.log.info(tempString)
                print tempString
                NonAnyRailBlockCounter += 1
                continue    # skip to next someBlock iteration

            ##
            ## Report parsed Occupancy Sensor Attributes
            ##
            tempString1 = 'New Sensor systemName = "{0}"'.format(self.SensorSystemName)
            tempString2 = ' userName = "{0}"'.format(self.SensorUserName)
            tempString3 = ' comment = "{0}"'.format(self.SensorComment)
            tempString = tempString1 + tempString2 + tempString3
            # self.log.info(tempString)
            print tempString

            # Create a new Occupancy Sensor
            newSensor = sensors.provideSensor(self.SensorSystemName)

            # AnyRail does not require unique Section names
            # If this script attempts to assign a previously
            # defined userName to a different Sensor, JMRI
            # will ignore the request without any error or warning.
            
            trialSensor = sensors.getByUserName(self.SensorUserName)

            if not (trialSensor == None) :
                # A duplicate use of the userName has been discovered
                # Announce it with output message
                # also provide a non-typical comment for this 
                # subsequent Sensor to allow easy identification
                # self.log.debug('trialSensor = "{0}"'.format(str(trialSensor)))
                tempString = 'Another Occupancy Sensor = "' \
                             + trialSensor.getSystemName() \
                             + '\" with matching userName = \"' \
                             + self.SensorUserName \
                             + '\" was previously created'
                self.log.info(tempString)
                print (tempString)
                tempString = \
                    '\tDUPLICATED AnyRail Section Name = \"' \
                    + self.SensorUserName \
                    + '\" from currently selected Block = \"' \
                    + blockUserName \
                    + '\"'
                self.log.warn(tempString)
                print (tempString)
                newSensor.setComment(blockUserName)
                DuplicateSectionNameCounter += 1

            else :
                # This is the desired case, the AnyRail Section name
                # self.log.debug('OK userName = "{0}"'.format(self.SensorUserName) )
                newSensor.setUserName(self.SensorUserName)

                # It is still possible for the track plan to have 
                # unnamed Sections
                # This one seems to be the first duplicated Block
                # userName found on the list
                if (self.SensorUserName == "") :
                    tempString = 'Occupancy Sensor = "' \
                                 + self.SensorSystemName \
                                 + '" was exported from an unnamed Section'
                    self.log.info(tempString)
                    print (tempString)
                    # tempString = '\tQuestionable Section Name = "' \
                                 # + blockUserName \
                                 # + '\" with only WhiteSpaces characters'
                    # self.log.warn(tempString)
                    # print (tempString)
                    QuestionableSectionNameCounter += 1

                else :
                    # A new Sensor with a unique and non-blank userName
                    # has been created
                    newSensor.setComment(self.SensorComment)

                    if (isRenameBlocks) : # User has selected the option
                        someBlock.setUserName(self.SensorUserName)
                        someBlock.setComment(self.SensorComment)

                someBlock.setSensor(newSensor.getSystemName() )

            BlockCounter += 1
            print ("")  # Place a blank line separator on the Script Output window
            # self.log.debug( 'BlockCounter = {0}'.format(BlockCounter))

        ###############################
        ####### END of "for" loop through exported Blocks

        # print the tallies
        self.SummariseTallies(
                NonAnyRailBlockCounter,
                QuestionableSectionNameCounter,
                DuplicateSectionNameCounter,
                BlockCounter
            )

        return

    ###############################
    ## End of       AnyRailBuildSensorList.init()

    # Define an almost empty task to run once
    def handle(self):
        #   Clean up the minimized windows (if any) mess
        for minimizedFrame in self.hiddenFrames :
            minimizedFrame.setVisible(True)
            self.log.info('Restoring window named "{0}"'.format(minimizedFrame.getTitle()) ) 

        # Make a record for the log to indicate that we have completed the task
        self.log.info( "All done" )
        return False    # To run at most once, and die

    ###############################
    ## End of       AnyRailBuildSensorList.handle()

    # Check the list of open windows AKA JMRIJFrame objects
    # for known potential cross-thread contamination
    #
    # PanelPro is a multi-thread program, and Java Swing is
    # not thread-safe.  A duck-and-cover strategy is employed.
    def AviodCrossThreadPossibilities(self) :
        frameList = jmri.util.JmriJFrame.getFrameList()

        for someFrame in frameList :
            frameTitle = someFrame.getTitle()
            if frameTitle in self.dangerousFrameTitleList :
                # This someFrame window will be updated as the script
                # creates and edits Sensors or else as the script
                # edits Block attributes.
                self.log.info ('Must minimize frame "{0}"'.format(frameTitle) )
                someFrame.setVisible(False)
                # keep the cookie crumbs for undo later
                self.hiddenFrames.append(someFrame)

        return

    ###############################
    ## End of       AnyRailBuildSensorList.AviodCrossThreadPossibilities()


    # PanelPro Blocks are always internal.
    #
    # The AnyRail exported XML file provides a one-to-one translation of
    # the track plan list of "Sections" to PanelPro Blocks.
    # 
    # AnyRail exported Blocks are sequentially identified with individual
    # systemName choices as if some kind of "hardware" numerical
    # identification were involved. 
    #
    # The systemName list always starts with "IB1" and then "IB2", "IB3", ...

    ##  At this time, a typical blockSystemName is of the form:
    ##      "IB" + str(blockNumeralString), 
    ##          where blockNumeralString is a positive integer,
    ##          starting with one.
    ##  Expect one or more digits.
    ##      Typically 3 or fewer, but script allows for up to 7.

    # Isolate the important fragments of the exported Block userName
    def parseBlockUserName(
                self, 
                exportedBlockUserName     #   exported Block userName
            ) :

        # Predefine local variables for scope protection
        self.SensorSystemName = ""
        self.SensorUserName = ""
        self.SensorComment = ""

        countSeparatorChars = exportedBlockUserName.count(fragmentSeparator)
        # tempString = 'countSeparatorChars = {0}'.format(countSeparatorChars)
        # self.log.debug(tempString)
        # print(tempString)

        if (countSeparatorChars < 2) :    
            #   Not an AnyRail exported Block, too few fragmentSeparator characters
            tempString = "Not an AnyRail exported Block"
            # self.log.debug(tempString)
            print(tempString)
            return False

        else :
            #   put parsed substring fragments into the list 
            fragmenteList = []  # the intended list of exported Block
                                # userName attributes
            tempPartitonList = []   # a list of length 3 that is
                                    # repeatedly the target of 
                                    # Python function named str.partition()
            tempString = exportedBlockUserName
            # iterate through the Block userName String
            for n in range(countSeparatorChars + 1) :
                tempPartitonList = tempString.partition(fragmentSeparator)
                # of the three items in the returned list,
                # only the first is important in this slice and dice
                fragmenteList.append(str(tempPartitonList[0]))
                # for j in range(3) : # range is defined by str.partition() results
                    # thing = '  {0}  "{1}"'.format(j,tempPartitonList[j])
                    # print(thing)
                if (tempPartitonList[2] == None) :
                    # only happens when the final fragmentSeparator has been discovered
                    # print "==== break out of loop"
                    break

                # now examine what is still remaining of the 
                # original exportedBlockUserName string
                tempString = tempPartitonList[2]    # for next pass through loop

            # print (fragmenteList)   # debug only

            # Check for AnyRail formated pattern
            if (fragmenteList[0]=='B') :
                # Prefix test OK
                # 'B_nnn... where nnn represents a string of one
                #   or more numerals
                # tempString = '1st test: AnyRail Prefix found'
                # self.log.debug(tempString)
                # print(tempString)

                #   The final fragment should match one of the Usage choices
                if (fragmenteList[countSeparatorChars] in self.pickUsageList) :
                    # Postfix test OK
                    self.SensorComment = fragmenteList[countSeparatorChars]
                        # at least until something ugly is discovered
                    
                    # AT THIS TIME: AnyRail exported Block userNames
                    # are formated "IB" + a numeric string.
                    #   Since an Internal Block systemName does not require
                    #   a hardware related choice, future versions of
                    #   AniRail may not retain that formated pattern.
                    if (fragmenteList[1].isnumeric()) :
                        # Third test OK
                        tempAddress = int(fragmenteList[1])
                        # parsedSensorSystemName = \
                        self.SensorSystemName = \
                            defaultSensorPrefix + str(tempAddress)
                        # print 'Sensor systemName = "' + self.SensorSystemName + "'"
                        if (countSeparatorChars == 2) :
                            # AnyRail Section name is empty string
                            self.SensorUserName = ""
                            self.SensorComment = fragmenteList[2].strip()
                        elif (countSeparatorChars == 3) :
                            # Conventional String as the name for the Section
                            # but just for safety, trim off any leading
                            #   trailing WhiteSpaces
                            self.SensorUserName = fragmenteList[2].strip()
                        else :
                            # Section name includes at least one fragmentSeparator
                            tempStart = len(fragmenteList[1]) + 3
                            tempSuffixLen = len(self.SensorComment) + 1
                            tempStop = len(exportedBlockUserName) - tempSuffixLen
                            # Finally the messy Section name is isolated
                            # but just for safety, trim off any leading
                            #   trailing WhiteSpaces
                            self.SensorUserName = \
                                exportedBlockUserName[tempStart:tempStop].strip()
                    # tempString = 'Sensor userName = "' \
                        # + self.SensorUserName \
                        # + '"'
                    # self.log.debug(tempString)
                    # print(tempString)
                    # tempString = 'Sensor comment = "' \
                        # + self.SensorComment \
                        # + '"'
                    # self.log.debug(tempString)
                    # print(tempString)
                else :
                    # Trailing Fragment is not an identified Usage choice
                    tempString = 'NOT an identified AnyRail Usage name'
                    # self.log.debug(tempString)
                    print(tempString)
                    return False
            else :
                # First character of exported Block userName is not "B"
                tempString = 'NO leading "B" in Block userName'
                # self.log.debug(tempString)
                print(tempString)
                return False

        # self.log.debug('self.SensorUserName = "{0}"'.format(self.SensorUserName) )
        # self.log.debug('parsedSensorSystemName = "{0}"'.format(parsedSensorSystemName) )
        # self.log.debug('self.SensorUserName = "{0}"'.format(self.SensorUserName) )
        # self.log.debug('parsedSensorComment = "{0}"'.format(parsedSensorComment) )

        # All tests OK, ...
        return True

    ###############################
    ## End of       AnyRailBuildSensorList.parseBlockUserName()

    # For the purpose of chasing down typos and other
    # unpleasant details, in the original track plan data,
    # provide the Block attributes before starting the parsing:
    def ReportBlock(
                self,
                exportedBlock,
                Counter
            ) :

        # Get all important details for the selected Block:

        tempString1 = 'Block # = "{0}", '.format(Counter)

        # get the Block systemName from AnyRail Section
        blockSystemName = exportedBlock.getSystemName()
        tempString2 = 'systemName = "{0}", '.format(blockSystemName)

        # get the  userName for the AnyRail exported someBlock
        blockUserName = exportedBlock.getUserName()
        tempString3 = 'userName = "{0}", '.format(blockUserName)

        # just to be cautious, get the comment field too
        blockCommentField = exportedBlock.getComment()
        tempString4 = 'comment = "{0}"'.format(blockCommentField)

        # Provide the Block's identifying Attributes
        tempString = tempString1 + tempString2 + tempString3 + tempString4
        self.log.info(tempString)
        print (tempString)

        return blockUserName

    ###############################
    ## End of       AnyRailBuildSensorList.ReportBlock()

    # Provide the summary tallies
    def SummariseTallies(
            self,
            NonAnyRailBlockCounter,
            QuestionableSectionNameCounter,
            DuplicateSectionNameCounter,
            BlockCounter
            ):
        print("")
        print('---   Results   ---')
        print("")
        if (NonAnyRailBlockCounter > 0) :
            tempString = \
                "A total of {0} Blocks are not recognized as exported from AnyRail" \
                .format(NonAnyRailBlockCounter)
            self.log.warn(tempString)
            print (tempString)
        if (QuestionableSectionNameCounter > 0) :
            tempString = \
                "A total of {0} Blocks have Questionable Section names" \
                .format(QuestionableSectionNameCounter)
            self.log.warn(tempString)
            print (tempString)
        if (DuplicateSectionNameCounter > 0) :
            tempString = \
                'A total of {0} duplicated AnyRail Section Names have been identified.' \
                .format(DuplicateSectionNameCounter)
            self.log.warn(tempString)
            print (tempString)
        tempString = \
            "AnyRail exported Block Count = {0}" \
            .format(BlockCounter)
        self.log.info(tempString)
        print (tempString)
        return

    ###############################
    ## End of       AnyRailBuildSensorList.SummariseTallies()

###############################
###############################
## End of       AnyRailBuildSensorList class definition

# Launch the task
ABSL = AnyRailBuildSensorList( "AnyRail Sensors" )
ABSL.start()

