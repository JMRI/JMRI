#   The MakeAllWindowsVisible.py script was designed to cope with a Windows OS problem described in the lengthy
#   "Layout Panels will not display." message thread found at
#   https://groups.io/g/jmriusers/topic/93328449#207914 and perhaps in earlier Groups.io topics.
#
#   One outcome of the identified message thread was the Issue "Copied Panel xml file is not
#   always usable on some Windows destination computers #11381" at https://github.com/JMRI/JMRI/issues/11381

#   A Panel xml file created on a computer with a large screen and/or with multiple screens an then used on different
#   Windows can be at risk.
#
#   If the Windows destination computer has either a smaller screen or has fewer screens, it is possible to leave
#   one or more defined panels completely or partially outside of the physical screen pixel area.

#       Apparently features of Linux and Mac operating systems will always move and display such panels.

#   The Pull Request "WIP - Move invisible windows to main screen #11384" at
#   https://github.com/JMRI/JMRI/pull/11384 was CLOSED without any modification to the JMRI software,
#   probably based on the statement:
#       "If this PR is to be merged, the comment by @devel-bobm needs to be resolved. There needs to be an
#       option to opt u[o]t of this test. That's easy to do, but I'm not sure if this issue should be resolved in another
#       way. See the thread for the discussion.
#       https://jmri-developers.groups.io/g/jmri/message/7772 "
#
#   This script offers Windows users another option, allowing the user to decide to move or skip each JmriJFrame.


    #   It should be noted that what we call the "main screen" is named within the JMRI software as "\Display0"
    #   and if there are other screens connected, the are named "\Display1" and "\Display2" etc.


# Author: Cliff Anderson, copyright 2022
# Part of the JMRI distribution


import jmri
import java
import javax
import javax.swing
import javax.swing.JButton

from org.slf4j import LoggerFactory     ##  For the log output
##
## NOTE: to enable logging, see https://www.jmri.org/help/en/html/apps/Debug.shtml
## Add the Logger Category name "jmri.jmrit.jython.exec" at DEBUG Level.



class moveLeftOutPanels(jmri.jmrit.automat.AbstractAutomaton):

    #   Creates two dialog windows for user input.
        #       The user override option is in response to the concerns identified by the developers.
    #   Allows the user to "Cancel" without modifying the PanelPro panel XML file.
    #   Discovers a list of all display screens.
        #       Based on limited hardware available for testing, assumes
        #       that the display screens are oriented
        #       from left to right as horizontal pixel count increases.
    #   Makes use of waitMsec() inherited from AbstractAutomaton to allow for user input delays
    #   Creates a list of all JmriJFrame objects that includes all PanelPro panels and a few other windows
    #   Ignores all JmriJFrame objects that are completely contained on the "combined display screen area"
    #   Allows user to decide to move hidden or even partially off screen Frames to the upper left reagion of the Main Screen
    #   Expects user to Store the file and restart PanelPro to resume editing.

########
# Define the common shared variable members

    # Debug logging
    log = LoggerFactory.getLogger( "jmri.jmrit.jython.exec.script.moveLeftOutPanels" )

    # Dialog related members:
    cautionJmriJFrameTitle = "CAUTION"
    cautionJmriJFrame = jmri.util.JmriJFrame( cautionJmriJFrameTitle )       # argument is the frames title
    moveJmriJFrameTitle = "Confirm"
    moveJmriJFrame = jmri.util.JmriJFrame( moveJmriJFrameTitle )       # argument is the frames title

    # Script bookkeeping:
    activeWindow = None         # Place holder for the selected JmriJFrame user decision
    activeWindowTitle = ''      # Place holder for each JmriJFrame name in turn
    isDelayed = False           # Logical switch used to control flow for human decision time intervals
                                #   used sequentially for each dialog
    isFindingHidden = False     # Allow user to Cancel (if false)
    newUpperLeftCorner = 30     # x and y values to use for the next moved JmriJFrame to Main Screen
    nextJFrameIncrement = newUpperLeftCorner    # incremental x & y step for any following moves

    # internal JMRI information concerning screen hardware
    screenDimensionsList = []   # JmriJFrame.getScreenDimensions()
    screenCount = 0             # len( screenDimensionsList )
    totalScreenWidthpixels = 0  # Assumption: JMRI aligns multiple screens from left to right???

# END OF moveLeftOutPanels common shared variable members definitions
##############################

    #   Initialization overhead stuff
    def init( self ) :    # inherited from AbstractAutomaton
        self.log.debug ( 'Testing the "moveLeftOutPanels" script' )
        self.cautionDialog()    #   set the isFindingHidden switch
        return
# END OF moveLeftOutPanels.init()
##############################

    #   Onetime pass through the handle() method will, assuming the user has
    #   not canceled, discover a list of all of the JMRI Windows including
    #   all the Panels and some other things inherited from the Java class JFrame.
    def handle( self ) :  # inherited from AbstractAutomaton

        # What the user chose to do or not
        if self.isFindingHidden :

            # Begin allowing the user to decide which panels to move to a visible
            # location in the main screen AKA \Display0
            self.buildMoveSkipDialog()

            allKnownFrameList = 0 # The actual count of JmriJFrames

            dummyIndex = 0

            #   Since each panel is a JmriJFrame object the cleanest option is
            #   to first retrieve the list of those windows.
            #       There is also a slight chance that one of the other kind of
            #       Frames got hidden too.
            allKnownFrameList = jmri.util.JmriJFrame.getFrameList().size()
            self.log.debug ( "JmriJFrameList Count = {0}".format ( allKnownFrameList ) )

    #   Then we loop through that list

            #   Loop through that list and allow the user to decide whether to move or skip
            while ( ( dummyIndex < allKnownFrameList) ) :

                someJmriJFrame = jmri.util.JmriJFrame.getFrameList().get(dummyIndex)
                self.activeWindowTitle = someJmriJFrame.getTitle()
                self.log.debug (
                    'JmriJFrame Index # ' + str(dummyIndex)
                )

                self.log.debug (
                    'JmriJFrame Title \"' + self.activeWindowTitle + '\"'
                )

                #   Because we created a Dialog window that is also a JmriJFrame,
                #   we need to ignore that dialog Frame to avoid confusion
                if ( self.activeWindowTitle <> self.moveJmriJFrameTitle ) :

                    # Establish a mechanism to force a delay until user chooses
                    # to either move or skip any outside-of-the-box windows
                    self.isDelayed = True
                    delayCount = 0

                    #   the winnowing process for the currently selected "someJmriJFrame"
                    self.maybeMoveSkip ( someJmriJFrame )

                    #   In general, we return here too soon, so we wait for the user to click choice
                    while self.isDelayed :
                        delayCount += 1
                        self.waitMsec ( 100 )

                    self.log.debug ( "Wait Count = {0}".format ( delayCount ) )

                dummyIndex = dummyIndex + 1

            #   Clean up and go home
            self.moveButton.enabled = False
            self.skipButton.enabled = False
            self.moveJmriJFrame.dispose()

        return False    # terminate the thread
                        # and thus release all resources.

# END OF moveLeftOutPanels.handle()
#################################################

    #   Before doing anything
    #   provide the user with a CAUTION dialog
    def cautionDialog( self ) :

        self.log.debug ( '  Begin building the CAUTION dialog' )
        self.cautionJmriJFrame.contentPane.setLayout (
                javax.swing.BoxLayout (
                self.cautionJmriJFrame.contentPane,
                javax.swing.BoxLayout.Y_AXIS
                )
            )

        ## put the first row text field on a line preceded by a label
        tempPanel_1 = javax.swing.JPanel()
        tempPanel_1.add(javax.swing.JLabel(
            "This script will identify all Hidden or off the edge of the screen Panels and one-by-one allow ")
            )
        self.cautionJmriJFrame.contentPane.add(tempPanel_1)
        tempPanel_2 = javax.swing.JPanel()
        tempPanel_2.add(javax.swing.JLabel(
            "you to choose whether to move each one to the upper left corner of this main Display screen or Skip.")
            )
        self.cautionJmriJFrame.contentPane.add(tempPanel_2)
        tempPanel_3 = javax.swing.JPanel()
        tempPanel_3.add(javax.swing.JLabel(
            "--- You may Cancel now with NO changes. ---")
            )
        self.cautionJmriJFrame.contentPane.add(tempPanel_3)
        tempPanel_4 = javax.swing.JPanel()
        tempPanel_4.add(javax.swing.JLabel(
            "Otherwise save the modified file and then Quit. Restart PanlePro to resume editing.")
            )
        self.cautionJmriJFrame.contentPane.add(tempPanel_4)
        self.log.debug ( "CAUTION dialog" )

        ## create a second row button to continue
        self.continueButton = javax.swing.JButton( "Continue" )
        self.continueButton.actionPerformed = self.whenContinueButtonClicked
        self.continueButton.enabled = True
        tempPanel = javax.swing.JPanel()
        tempPanel.setLayout(java.awt.FlowLayout())
        tempPanel.add( self.continueButton )
        self.cautionJmriJFrame.contentPane.add(tempPanel)
        # self.log.debug ( "Continue Button of CAUTION dialog" )

        ## create another second row button to cancel the move
        self.cancelButton = javax.swing.JButton("Cancel")
        self.cancelButton.actionPerformed = self.whenCancelButtonClicked
        self.cancelButton.enabled = True
        tempPanel.add( self.cancelButton )
        self.cautionJmriJFrame.contentPane.add(tempPanel)
        # self.log.debug ( "Cancel Button of CAUTION dialog" )

        #   lock and load
        self.cautionJmriJFrame.setLocation( 20, 20 )
        self.cautionJmriJFrame.pack()
        self.cautionJmriJFrame.setVisible( True )

        self.waitMsec ( 500 )   # Allow for some visual recognition interval

        #       Because the information about the number of and size of your screens
        #       is available as a member function of the JmriJFrame class, we
        #       piggyback this confusing code here prior to looking for any Panels
        self.screenDimensionsList = self.cautionJmriJFrame.getScreenDimensions()
        self.screenCount = len( self.screenDimensionsList )
        self.log.debug ( "Number of Screens = {0}".format( self.screenCount ) )
        # Number of Screens = 2
        self.totalScreenWidthpixels = 0
        '''
        What follows here has only been tested on a laptop with a HDMI connection to a TV
        screen and has not been verified to work with any other multiple screen hardware.
        '''
        for screenDimensions in self.screenDimensionsList :
            self.log.debug ( screenDimensions.bounds.toString() )
            # java.awt.Rectangle[x=0,y=0,width=1920,height=1080]
            # java.awt.Rectangle[x=1920,y=0,width=3840,height=2160]
            self.log.debug ( "--Rectangle x = {0}".format( screenDimensions.bounds.x ) )
            self.log.debug ( "--Rectangle y = {0}".format( screenDimensions.bounds.y ) )
            self.log.debug ( "--Rectangle width = {0}".format( screenDimensions.bounds.width ) )
            self.log.debug ( "--Rectangle height = {0}".format( screenDimensions.bounds.height ) )
            self.totalScreenWidthpixels = screenDimensions.bounds.width
            self.log.debug ( screenDimensions.getGraphicsDevice().getIDstring() )
            ### \Display0 [Finding JmriJFrames]
            ### \Display1 [Finding JmriJFrames]
            ###     etc...

        self.log.debug ( "  totalScreenWidthpixels = {0}".format( self.totalScreenWidthpixels ) )
        self.log.debug ( '  Finished building the Caution Dialog' )


        self.isDelayed = True
        delayCount = 0
        #   In general, we get here too soon, so we wait for the click
        while self.isDelayed :
            # self.log.debug ( "waiting" )
            delayCount += 1
            self.waitMsec ( 100 )

        #   Clean up and go home
        self.continueButton.enabled = False
        self.cancelButton.enabled = False
        self.cautionJmriJFrame.dispose()

        self.log.debug ( "cautionDialog() Wait Count = {0}".format ( delayCount ) )

        if self.isFindingHidden :
            self.log.debug ( 'User selected "Continue"' )
        else:
            self.log.debug ( 'User selected "Cancel"' )

        return # self.isFindingHidden


# END OF moveLeftOutPanels.cautionDialog()
#################################################

    def whenContinueButtonClicked ( self, event ) :
        self.isFindingHidden = True
        self.isDelayed = False      #   Trigger the 'Break-out of the Wait loop'
        return

# END OF moveLeftOutPanels.whenContinueButtonClicked()
#################################################

    def whenCancelButtonClicked ( self, event ) :
        self.isFindingHidden = False
        self.isDelayed = False      #   Trigger the 'Break-out of the Wait loop'
        return

# END OF moveLeftOutPanels.whenCancelButtonClicked()
#################################################


    #   For each Frame AKA "window," we first decide if it is ENTIRELY OUTSIDE
    #   the screen and that requires four comparisons
    def maybeMoveSkip( self, faJmriJFrame) :

        #   Each "JFrame" object has a member that allows us to discover
        #   something about the display screen.  For a computer with
        #   multiple screens and a JFrame that spans the boundary between
        #   two screens it is not clear what the provided display screen
        #   information means.
        pixelScreenSize = faJmriJFrame.getToolkit().getScreenSize()
        #   pixelScreenSize is in pixels ( width, height ) and includes all of the
        #   physical pixels on the user's screen, including any task bar or other
        #   system defined insert or assistant.

        # self.log.debug (
            # '/\/\ Screen Dimension in pixels is ' + str (pixelScreenSize)
        # )

        # pixelScreenWidth  = pixelScreenSize.width     ### NOT USED
        pixelScreenHeight = pixelScreenSize.height

        self.log.debug (
           # ' pixelScreenWidth = ' + str( pixelScreenWidth ) +
           '\/\/ pixelScreenHeight = ' + str( pixelScreenHeight )
        )

        # get the upper left corner of the faJmriJFrame under scrutiny in Screen coordinates
        frame_X = faJmriJFrame.x
        frame_Y = faJmriJFrame.y
        self.log.debug (
            '\/\/ Upper Left Corner = ( ' + str ( frame_X )
            + ', ' +str( frame_Y ) + ' )'
        )

        # get the width and height of the faJmriJFrame under scrutiny in Screen coordinates
        frameWidth = faJmriJFrame.width
        frameHeight = faJmriJFrame.height
        self.log.debug (
            "\/\/ JmriJFrame width = " + str(frameWidth)
            + " and height = " + str(frameHeight)
        )

        # calculate the lower right corner of the faJmriJFrame under scrutiny in Screen coordinates
        frame_Right_X = faJmriJFrame.x + frameWidth
        frame_Lower_Y = faJmriJFrame.y + frameHeight

        self.log.debug (
            '\/\/ Lower Right Corner = ( ' + str ( frame_Right_X )
            + ', ' +str( frame_Lower_Y ) + ' )'
        )

        # Four tests are made to detect a JmriJFrame to possibly be "hidden" or "invisible"
        # But in fact we are testing for Frames that are even PARTIALLY off one of
        # the edges of the entire display screen combination.

        # Top and bottom edge testing is done on the basis of the individual containing Screen.

        # Left and right edge testing is done for the entire combined screen width.
                # Assumption done with the results of limited number of screen hardware available

        # NOTE that some panel edges may meet more than one condition, but only
        # the first reason detected is used.  NO further testing is done.
        reasonText = ''
        if ( frame_Right_X < 0 ) :
            reasonText = 'Tooo far LEFT'
            self.log.debug ( '/\/\ Reason ="' + reasonText + '"' )
            self.log.debug ( '/\/\ frame_Right_X = {0} < 0'.format( frame_Right_X ) )
            self.updateMoveSkipDialog ( faJmriJFrame, reasonText )
        elif ( frame_Lower_Y < 0 ) :
            reasonText = 'Tooo far UP'
            self.log.debug ( '/\/\ Reason ="' + reasonText + '"' )
            self.log.debug ( '/\/\ frame_Lower_Y = {0} < 0'.format( frame_Lower_Y ) )
            self.updateMoveSkipDialog ( faJmriJFrame, reasonText )
        elif ( frame_X > self.totalScreenWidthpixels ) :
            #   Only if the Frame is outside the entire left to right cumulative display
            #   Assumption: JMRI aligns multiple screens from left to right???
            reasonText = 'Tooo far RIGHT'
            self.log.debug ( '/\/\ Reason ="' + reasonText + '"' )
            self.log.debug ( '/\/\ frame_X = {0} > self.totalScreenWidthpixels = {1}'.format( frame_X, self.totalScreenWidthpixels ) )
            self.updateMoveSkipDialog ( faJmriJFrame, reasonText )
        elif ( frame_Y > pixelScreenHeight ) :
            reasonText = 'Tooo far DOWN'
            self.log.debug ( '/\/\ Reason ="' + reasonText + '"' )
            self.log.debug ( '/\/\ frame_Y = {0} > pixelScreenHeight = {1}'.format( frame_Y, pixelScreenHeight ) )
            self.updateMoveSkipDialog ( faJmriJFrame, reasonText )
        else :  ##  The Usual window position is the "Ignore" exception in this logic
            #       Expect the Usual case that some, perhaps most of the windows sit
            #       completely within the main screen.
            #       In this twisted logic, the usual case requires handling an exceptional
            #       "Ignore" condition.
            self.log.debug ( "  Ignoring ON-SCREEN JmriJFrame" )
            #   Kick the trigger in lieu of a user decision
            #   to cancel the delay waiting for human response
            self.isDelayed = False      #   Trigger the 'Break-out of the Wait loop'

# END OF moveLeftOutPanels.maybeMoveSkip()
#################################################

    #   If any one of the four tests indicates a window that is even partially
    #   out of bounds, give the user the information and let them make a choice
    def updateMoveSkipDialog( self, faJmriJFrame, faReason ) :
        self.activeWindow = faJmriJFrame
        #   Display the title of the Frame
        self.candiateWindowName.text = self.activeWindowTitle
        #   Display (at least the first) condition detected.
        self.reasonMessage.text = faReason
        #   Thi returns, to a waiting loop for the user to make a choice.
        return

# END OF moveLeftOutPanels.updateMoveSkipDialog()
#################################################

    #   The tedious GUI stuff from the initialization is corralled
    #   into an isolated lump
    def buildMoveSkipDialog( self ) :

        self.log.debug ( '  Begin building the Move or Skip Dialog' )
        self.moveJmriJFrame.contentPane.setLayout (
                javax.swing.BoxLayout (
                self.moveJmriJFrame.contentPane,
                javax.swing.BoxLayout.Y_AXIS
                )
            )

        ## build the first row to identify the name of the panel
        tempPanel = javax.swing.JPanel()
        tempPanel.add(javax.swing.JLabel("Panel"))
        self.candiateWindowName = javax.swing.JTextField(15)
        tempPanel.add(self.candiateWindowName)
        self.moveJmriJFrame.contentPane.add(tempPanel)

        ## build the second row to display the reason
        self.reasonMessage = javax.swing.JTextField(15)
        tempPanel = javax.swing.JPanel()
        tempPanel.add(self.reasonMessage)
        self.moveJmriJFrame.contentPane.add(tempPanel)

        ## create a third row button to approve the move
        self.identifyJFrame = javax.swing.JLabel( "Panel Name" )
        self.moveButton = javax.swing.JButton( "Move" )
        self.moveButton.actionPerformed = self.whenApproveButtonClicked
        self.moveButton.enabled = True
        tempPanel = javax.swing.JPanel()
        tempPanel.setLayout(java.awt.FlowLayout())
        tempPanel.add( self.identifyJFrame )
        tempPanel.add( self.moveButton )
        self.moveJmriJFrame.contentPane.add(tempPanel)

        ## create another third row button to cancel the move
        self.skipButton = javax.swing.JButton("Skip")
        self.skipButton.actionPerformed = self.whenSkipButtonClicked
        self.skipButton.enabled = True
        tempPanel.add( self.skipButton )
        self.moveJmriJFrame.contentPane.add(tempPanel)

        #   lock and load
        self.moveJmriJFrame.setLocation( 1200, 700 )
        self.moveJmriJFrame.pack()
        self.moveJmriJFrame.setVisible( True )

        self.log.debug ( "Finished building the Move Skip Dialog" )

        self.waitMsec ( 500 )   # Allow for some visual recognition interval

        return

# END OF moveLeftOutPanels.buildMoveSkipDialog()
#################################################


## Clicking the move Button will put the window in the upper left region of the main window
    def whenApproveButtonClicked( self, event ) :
        #   Do an actual move
        self.activeWindow.setLocation( self.newUpperLeftCorner, self.newUpperLeftCorner )
        self.activeWindow.setVisible( True ) # just in case the Panel had be minimized
        #   Prepare for the next potential needed move.
        self.newUpperLeftCorner += self.nextJFrameIncrement
        self.log.debug ( '  the next planned upper-left corner will use {0}'.format( self.newUpperLeftCorner ) )
        # bring dialog up front in case it got covered
        self.moveJmriJFrame.setVisible( True )
        # Trigger the condition that breaks us out of the delay loop
        self.isDelayed = False      #   Trigger the 'Break-out of the Wait loop'
        return

# END OF moveLeftOutPanels.whenApproveButtonClicked()
#################################################


## Clicking the skip Button will cancel the delay interval and continue with the looping
    def whenSkipButtonClicked( self, event ) :
        # Essentially do nothing, but allow the the next Frame
        self.log.debug ( "  skipping at user's choice" )
        # Trigger the condition that breaks us out of the delay loop
        self.isDelayed = False      #   Trigger the 'Break-out of the Wait loop'
        return

# END OF moveLeftOutPanels.whenSkipButtonClicked()
#################################################


# END OF moveLeftOutPanels()  CLASS DEFINITION
#################################################
#################################################
#################################################

# create one object from the class and one thread with the name "UnHidding" that appears on the log entries.
moveLeftOutPanels0 = moveLeftOutPanels( "UnHidding" )

# Kick start the process
moveLeftOutPanels0.start()

#           List of methods
#   def init( self ) :    # inherited from AbstractAutomaton
#   def handle( self ) :  # inherited from AbstractAutomaton
#   def cautionDialog( self ) :
#   def whenContinueButtonClicked ( self, event ) :
#   def whenCancelButtonClicked ( self, event ) :
#   def maybeMoveSkip( self, faJmriJFrame) :
#   def updateMoveSkipDialog( self, faJmriJFrame, faReason ) :
#   def buildMoveSkipDialog( self ) :
#   def whenApproveButtonClicked( self, event ) :
#   def whenSkipButtonClicked( self, event ) :
