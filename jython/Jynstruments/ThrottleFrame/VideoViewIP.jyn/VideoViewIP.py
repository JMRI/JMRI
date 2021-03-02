# Author: Lionel Jeanson copyright 2009 - 2021
# Part of the JMRI distribution
#
# Create a Jynstrument with video view from connected webcam
#
# Build using https://github.com/sarxos/webcam-capture
# You'll need webcam-capture-XXX.jar and bridj-XXX.jar copied from webcam-capture release to your JMRI lib folder
# And webcam-capture-driver-ipcam-XXX.jar from there https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/driver-ipcam
#

import java
import java.awt
import java.awt.event
import java.beans
import jmri.jmrit.jython.Jynstrument as Jynstrument
import java.awt.BorderLayout as BorderLayout
import java.awt.event.ItemListener as ItemListener
import javax.swing.JCheckBoxMenuItem as JCheckBoxMenuItem
import com.github.sarxos.webcam.Webcam as Webcam
import com.github.sarxos.webcam.WebcamPanel as WebcamPanel
import com.github.sarxos.webcam.ds.ipcam.IpCamDeviceRegistry as IpCamDeviceRegistry
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver as IpCamDriver
import com.github.sarxos.webcam.ds.ipcam.IpCamMode as IpCamMode

class VideoViewIP(Jynstrument, ItemListener):

    def getExpectedContextClassName(self):
        # This Jynstrument likes to be in a ThrottleFrame and not anywhere else
        return "jmri.jmrit.throttle.ThrottleFrame"

    def init(self):
        Webcam.setDriver( IpCamDriver() )
        self.setLayout( BorderLayout() )        
        self.miFill =  JCheckBoxMenuItem ( "Fill window" )
        self.miFill.addItemListener(self)
        self.getPopUpMenu().add( self.miFill )
        self.miMirror =  JCheckBoxMenuItem ( "Mirror", True )        
        self.miMirror.addItemListener(self)        
        self.getPopUpMenu().add( self.miMirror )                
        # Adjust bellow URL accordingly
        IpCamDeviceRegistry.register("MyTest", "http://localhost:8080/?action=stream", IpCamMode.PUSH);        
        self.addCamPanel()

    def quit(self):   # very important to clean up everything to make sure GC will collect us
        self.webcamPanel.stop()
        IpCamDeviceRegistry.unregisterAll()        
        self.webcamPanel = None

    def addCamPanel(self):
        self.webcamPanel = WebcamPanel(Webcam.getWebcams().get(0))
        self.webcamPanel.setMirrored(self.miMirror.isSelected())
        if ( self.miFill.isSelected() ) :
            self.webcamPanel.setFillArea( True  )
        else :
            self.webcamPanel.setFitArea( True )
        self.add(self.webcamPanel, BorderLayout.CENTER)        

# this is a good way to make sure that we're are actaully GCed 
    def __del__(self):  
        print "in destructor"

    def itemStateChanged(self, evt):
        if (evt.getSource() == self.miFill ):
            if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED ) :
                self.webcamPanel.setFillArea( True  )
            else :
                self.webcamPanel.setFitArea( True )
        if (evt.getSource() == self.miMirror ):
            self.webcamPanel.setMirrored(self.miMirror.isSelected())

