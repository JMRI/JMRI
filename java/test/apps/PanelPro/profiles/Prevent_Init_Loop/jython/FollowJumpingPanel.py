# FollowJumpingPanel is a diagnostic tool that can provide panel location metrics
# to indicate an inconsistent and even intermittent symptom, but not yet (as of this date) 
# provide a fix for a problem that has shown up in 4.9.1
#
# Some, but not all, Layout Editor Panels will visually appear to load in the proper place,
# but then move to the upper left corner region of the screen as the loading process completes.
#
# While testing this script, the data has show x=-10 and y=-10 even though the panel appears to
# be where expected, until some correction is invoked???
#
"""    The assumption here is that you are NOT running JMRI without a screen.    """
#
#
# Based on information gleaned from DisableOpsMode.py and from the 
# http://jmri.org/help/en/html/tools/scripting/FAQ.shtml web page 
# and from other examples in the JMRI distribution download jython folder. 
# Information concerning the need to delay access to the Main JFrame Window 
# until it is visible was discovered during preliminary testing. 
#
# Author: Cliff Anderson, copyright 2017
# Part of the JMRI distribution

"""
    Normal usage of this script should not require the DEBUG log messages, HOWEVER:

    If you wish to enable the DEBUG log messages, COPY the following TWO LINES:
log4j.category.jmri.jmrit.jython.exec=DEBUG

# ###########  Copy the two lines above this one  ############# #
    into the file named "default.lcf" found in your JMRI Programs folder or into the copy that you have placed in your settings directory. Place those copied lines near or at the END of the file.  But, be certain that the last line of that file is an empty string! 

            IMPORTANT FURTHER INFORMATION: 
    EFFECTIVE with JMRI Version 4.6, the following change is described in the Release Notes: 
        'Logging Preferences in a file named "default.lcf" in the settings directory take precedence over logging preferences that ship with JMRI. The "default.lcf" can be copied from the JMRI installation as a starting point.' 

"""
# Execution is staged on enabling evidence of the completion of three Start Up 
# events and is NOT based on clock interval estimations for some hypothetical 
# configuration. 
#
# The pieces of enabling evidence are listed here, in time order of appearance:
#        1.    The list of JmriJFrame windows is not empty. 
#        2.    An entry on that list has a Main JMRI Window name that we recognize. 
#        3.    That identified Main JMRI Window must be visible. 
#
#   AND:   Each piece of evidence is obtained by simplistic "repeated polling" rather 
#            than by the use of more sophisticated "listener" features of 
#            Java and/or Jython. 
#
# Using the Preferences dialog Window with the Start Up tab and the list of 
# "Items will be executed or opened in the order listed," is the expected normal 
# way to execute ANY script.  Observe the note at the bottom of that tab suggesting 
# that "Scripts should be run last in most cases."  
#
'''
However, for this script to provide useful metrics, it should be executed before loading a panel file that has a Layout Editor panel that is suspected of jumping.
'''
#
# This script waits for evidence provided by events that are known to occur late in the 
# Start Up process.  This script works best if it is executed LAST in the Start Up process! 
#
# The Main JMRI Window doesn't become visible until  VERY LATE  in the Start Up process. 
#
# Manually executing this script from within a running App does not pose the need 
# for the programmed delays.  The logic used here will quickly fall through the 
# checkpoints to provide an __almost__ immediate result. 
#
#
import jmri
import java
import org.apache.log4j


class FollowJumpingPanel(jmri.jmrit.automat.AbstractAutomaton):
    #
    # As each panel appears, the x and y locations of the upper left corner are discovered and reported.
    #
    # Executing this script will provide "before" and "after" values on the JMRI
    # System Console and in your "session.log" file.



    # Perform any initialization.
    #   In fact, the main body of the FJP could have been executed here instead 
    #     of being executed within the handle() function.   ??? 
    def init(self):
        return

    # The main body of the FJP that is executed once, and then the resources 
    # are dismissed. 
    def handle(self):

        # Debug logging log4j overhead:
        LogfileWrite = org.apache.log4j.Logger.getLogger("jmri.jmrit.jython.exec.script.FollowJumpingPanel")

        # The JMRI Main Window is moved to the lower left corner of the screen, but only
        # because, a script to do that task has been modified for the Jumping Panel diagnostics
        # Tenative new location values are provided here, 
        #   but unless the script is edited, 
        #      the actual values are recalculated later 
        #         using data obtained from your computer. 
        New_MainWindow_X_Value = 1350      #   Lower right corner for a 1920 by 1080 pixel display, 
        New_MainWindow_Y_Value =  786      #   but only IF the App happens to be PanelPro, 
                                           #   AND if no buttons have been added during Start Up. 

        # Internal variables needed here.  
        FrameListEntryCount = 0 # The actual value will change during the execution 
                                #    process, perhaps several times. 
        PriorFrameCount = -1    # Check to identify a change in FrameListEntryCount 
                                #    intentionally set to initiaize the loop. 
        DummyIndex = 0          # Dummy running index for FrameList queries. 
        WindowTitle = ''        # Variable string to compare with App titles. 
        isMainWindow = False    # Have we found the Main Window yet? 
        MainWindowIndex = -1    # Intentionally invalid index value to be modified 
                                #    when we find the Main Window. 

        LogfileWrite.debug ( "FJP ============== ======= == =======" )
        LogfileWrite.debug ( "FJP FollowJumpingPanel version is \"0.0\"" )
        LogfileWrite.debug ( "FJP ============== ======= == =======" )

        # First we delay until at least one JmriJFrame gets entered onto the FrameList. 
        # This probably is not needed if the script is run last in the Start Up 
        # process, but is here for safety due to problems that arose during testing. 
        while (FrameListEntryCount == 0) :
            FrameListEntryCount = jmri.util.JmriJFrame.getFrameList().size()
            LogfileWrite.debug ( "FJP Waiting for non-empty FrameList" )
            DummyIndex = DummyIndex + 1
            # We go through this loop AT LEAST ONCE, and usually, also at most once, but ... 

        # Our first necessary event has been confirmed by evidence. 
        #       We now have a list of potential suspects to search.  
        LogfileWrite.info ( "FJP We have at least one entry on the JmriJFrame FrameList" )

        # Now we apply a series of waits and retrys at least until the Main JMRI Window 
        # shows up. During the testing, the Main JMRI Window always shows up first, but 
        # the logic here can cope with waiting until the FrameList is fully populated.  
        #   Some of the older examples indicate that the index of the Main Window 
        #   was not always predicable with earlier versions of JMRI.  
        while ( not (isMainWindow) ) :
            self.waitMsec(500)

            # In each iteration, we search the current list of Frames for a Main Window 
            # title that we can recognize.  As of this version of the script, we only 
            # know about the Apps named PanelPro, DecoderPro, & SoundPro 
            # The script has no prior information as to which App has started this execution. 

            DummyIndex = 0      # Begin a new ordered interrogation for a 
                                # window with an App name.  

            # Obtain an initialization count of JMRIJFrames.  
            # If executing at Start Up, the count is probably zero, 
            # but if executed manually from a menu item, the count is the final number. 
            FrameListEntryCount = jmri.util.JmriJFrame.getFrameList().size()

            # Don't bother even searching the list if the count has not changed since the 
            # previous pass through the loop. 
            # NOTE that the value of PriorFrameCount is initially set to -1 to force 
            # at least one pass through this search. 
            if ( FrameListEntryCount > PriorFrameCount ) :
                PriorFrameCount = FrameListEntryCount
#                while ( (DummyIndex < FrameListEntryCount) and (DummyIndex < 5) ):
#                    # In version 4.6 and perhaps several earlier versions, the Main 
#                    # Window will be the first entry on this FrameList with index zero. 
#                    # In Versions 4.8, the Main Window had index value 0
#                    # Starting with Version 4.9.1, the Main Window is the FINAL one on the list.
                while ( (DummyIndex < FrameListEntryCount) ) :
                    SomeJmriJFrame = jmri.util.JmriJFrame.getFrameList().get(DummyIndex)
                    WindowTitle = SomeJmriJFrame.getTitle()
                    LogfileWrite.info ( 
                        "FJP Index #" + str(DummyIndex) 
                        + "  Title = \"" + WindowTitle
                        + "\"   upper left corner = ( " + str (SomeJmriJFrame.x) 
                        + ", " +str(SomeJmriJFrame.y) + " )"
                    )
                    isMainWindow = (
                          (WindowTitle == "PanelPro")
                        or
                          (WindowTitle == "SoundPro")
                        or
                          (WindowTitle[0:10] == "DecoderPro")
                          # Sometimes we get extra stuff with the DecoderPro Window name
                          #   However the DecoderPro App seems the least likely of the 
                          #   App's to need this script. 
                        )

                    if ( isMainWindow ) :
                        LogfileWrite.debug ( 
                            "FJP JmriJFrame isMainWindow = " + str(isMainWindow) )
                        MainWindowIndex    = DummyIndex
                        # Kick the DummyIndex to force an exit of the inner loop 
                        DummyIndex = FrameListEntryCount + 999
                    DummyIndex = DummyIndex + 1

        # Our second necessary event has been confirmed by evidence. 
        #       A recognized window name has been found.
        LogfileWrite.info ( 
            "FJP The Main JMRI Window Index = #" + str( MainWindowIndex ) 
            + "  Title = \"" + WindowTitle + '"'
        )

        # Verify that the invalid index entry has now been corrected (Just a precaution). 
        if ( MainWindowIndex >= 0 ) :

            # The test for visible is not just for protection against crashes! 
            # Run a very tight loop to compensate for the fact that JMRI calculates 
            # the center location for the Main Window very late in the Start Up 
            # process. We must avoid our location data getting overwritten.  
            # In earlier stages of this development, the loop was defined by: 
            #    "while ( SomeJmriJFrame.y== 0 )" but this control provides better 
            # results.  Especially with DecoderPro, which sometimes provided an 
            # earlier copy of (x,y) but then ignored our new setLocation() values.  

            while ( not (SomeJmriJFrame.visible) ) :
                # A superfluous debug message just to see how many times this might have 
                # repeated.  The results are not consistent for Start Up for the same 
                # App and configuration and computer. 
                LogfileWrite.debug (
                    "FJP MainJmriWindow.visible = " + str(SomeJmriJFrame.visible) )
                # Let the other threads play for at least two tenths of a second. 
                self.waitMsec(200)  #delay interval is based on guesses & experiments 
                # spin our wheels 
                #     waiting for evidence that
                #         the initialization process has completed
            # IF executed manually, this test always works the first time, 
            # but, if executing during the Start Up process, another game is on. 

        # Our third necessary event has been confirmed by evidence. 
        #       The Main JMRI Window is FINALLY visible! 
        #           NOW we set about to move it! 
            LogfileWrite.info ( "FJP Getting ready to move the Main JMRI Window." )

            ScreenDimension = SomeJmriJFrame.getToolkit().getScreenSize()
            # ScreenDimension is in pixels ( width, height ) and includes all of the 
            # physical pixels on the user's screen, including any task bar or other 
            # system defined assistant. 
            LogfileWrite.debug ( 
                "FJP Screen Dimension in pixels is " + str (ScreenDimension)
            )

            # The User's Screen Size is:
            ScreenWidth  = ScreenDimension.width
            ScreenHeight = ScreenDimension.height
            
            LogfileWrite.debug ( 
                "FJP Main Window " + WindowTitle 
                + " initial upper left corner = ( " + str (SomeJmriJFrame.x) 
                + ", " +str(SomeJmriJFrame.y) + " )"
            )
            
            JFrameDimension = SomeJmriJFrame.getSize()
#            LogfileWrite.debug ( 
#                "FJP Window Size = " + str(JFrameDimension)
#            )

            MainWindowWidth = JFrameDimension.width
            MainWindowHeight = JFrameDimension.height
#            LogfileWrite.debug ( 
#                 "FJP Window width = " + str(MainWindowWidth)
#                 + " and height = " + str(MainWindowHeight)
#            )

            # Calculate for Lower Right corner of the screen are provided here 
            # and the next two lines must be commented out if you pick the values
            New_MainWindow_X_Value = ScreenWidth  - MainWindowWidth
            New_MainWindow_Y_Value = ScreenHeight - MainWindowHeight

            SomeJmriJFrame.setLocation( New_MainWindow_X_Value, New_MainWindow_Y_Value)

            LogfileWrite.info ( 
                "FJP Window " + WindowTitle 
                + " NEW     upper left corner = ( " + str (SomeJmriJFrame.x) 
                + ", " +str(SomeJmriJFrame.y) + " )"
            )

        return 0    # return zero to release all resouces. 

# Launch the diagnostics metrics task
FollowJumpingPanel().start()

