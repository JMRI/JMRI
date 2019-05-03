# Author: Lionel Jeanson copyright 2009
# Part of the JMRI distribution
#
# Create a Jynstrument with video input preview
#
# Build on top of http://code.google.com/p/jvcp/
#
# You need jar files from this JVCP project, and dependencies depending on your platform.
#    ( Please refer to http://code.google.com/p/jvcp/wiki/BasicDocumentation )
#    to be copied into the JMRI distribution lib directory
#
# For Windows User, the lti-civil native lib civil.dll has to be copied to the windows/x86 folder of JMRI libs
#
# For OS X, the Rococoa native library librococoa.dylib has to be copied to the macosx folder of JMRI libs
# Further more this JMRI/lib/macosx has to be added to Java jna.library.path -Djna.library.path=/path/to/JMRI/lib/macosx
#    on the Java command line. A line like: OPTIONS="${OPTIONS} -Djna.library.path=.:$SYSLIBPATH:lib" should be added in 
#    the StartJMRI shell script in the JMRI application bundle you use (by the end of the file where other lines like that appear)

import java
import java.awt
import java.awt.event
import java.beans
import jmri
import jmri.jmrit.jython.Jynstrument as Jynstrument
import java.awt.CardLayout as CardLayout
import jmri.util.swing.ResizableImagePanel as ResizableImagePanel
import java.awt.event.MouseListener as MouseListener
import java.beans.PropertyChangeListener as PropertyChangeListener
import org.jvcp.VideoCapture as VideoCapture
import javax.swing.JCheckBoxMenuItem as JCheckBoxMenuItem

class VideoView(Jynstrument, MouseListener):
# Jynstrument mandatory part
# Here this JYnstrument like to be in a ThrottleFrame and no anywhere else
    def getExpectedContextClassName(self):
        return "jmri.jmrit.throttle.ThrottleFrame"

    def init(self):
        self.setLayout( CardLayout() )
        self.labelOn = None
        self.labelOff = ResizableImagePanel(self.getFolder() + "/CameraOff.png",100,100 )
        self.statusOn = False
        self.videoCapture = VideoCapture.init()
        self.add(self.labelOff, "off")
        self.addComponentListener(self.labelOff)
        self.addMouseListener(self)
        self.setIcon()
        self.captureMenuItem = []
        self.menuPopuled = False

    def quit(self):   # very important to clean up everything to make sure GC will collect us
        self.videoCapture.stop()
        self.videoCapture = None

# this is a good way to make sure that we're are actaully GCed 
    def __del__(self):  
        print "in destructor"

#Inner workings:
    def switch(self):      # actually do function value change
        self.statusOn  = not self.statusOn
        if self.statusOn :
            if (self.labelOn == None) :
                self.labelOn = self.videoCapture.getCaptureComponent()
                self.labelOn.addMouseListener(self)
                self.add(self.labelOn, "on")                
            self.videoCapture.start()
        else :
            self.videoCapture.stop()
        self.setIcon()

    def setIcon(self):     # update appearance
        cl = self.getLayout()
        if self.statusOn :
            cl.show(self, "on")
        else :
            cl.show(self, "off")

#MouseListener part: to listen for mouse events
    def mouseReleased(self, event):
        if event.getButton() == java.awt.event.MouseEvent.BUTTON1 :
            self.switch()

    def mousePressed(self, event):
        pass           
    def mouseClicked(self, event):
        pass
    def mouseExited(self, event):
        pass
    def mouseEntered(self, event):
        if self.menuPopuled == False :
            for cam in self.videoCapture.getDeviceList(): # The selection bellow might have to be modified
#                print cam.getDescription()
                mi =  JCheckBoxMenuItem ( cam.getDescription() )
                self.getPopUpMenu().add( mi )
                mi.addItemListener( CaptureItemListener(cam, self) )
                self.captureMenuItem.append( mi )
            if ( len(self.captureMenuItem) == 0 ):
                print "No video capture device found"
            else :
                self.captureMenuItem[0].setSelected(True)
            self.menuPopuled = True
        
# Menu entry changed for curent video device
    def setSelectedCaptureDevice(self, cam, item):
        for mi in self.captureMenuItem :
            if ( mi != item ):  # Force deselection of other ones
                mi.setSelected(False)
        self.videoCapture.setDevice(cam)
        
# Item listeners for the PopUp menu
class CaptureItemListener( java.awt.event.ItemListener):
    def __init__(self, cam, jyns):
        self.cam = cam
        self.jyns = jyns

    def itemStateChanged(self, evt):
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED ):
            self.jyns.setSelectedCaptureDevice( self.cam, evt.getItem() )
        
