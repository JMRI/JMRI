# Author: Lionel Jeanson copyright 2009 - 2021
# Part of the JMRI distribution
#
# Create a Jynstrument with video view from connected webcam
#
# Build using https://github.com/sarxos/webcam-capture
#

import java
import java.awt
import java.awt.event
import java.beans
import jmri
import jmri.jmrit.jython.Jynstrument as Jynstrument
import java.awt.BorderLayout as BorderLayout
import java.awt.event.ItemListener as ItemListener
import javax.swing.JCheckBoxMenuItem as JCheckBoxMenuItem
import com.github.sarxos.webcam.Webcam as Webcam
import com.github.sarxos.webcam.WebcamPanel as WebcamPanel
import com.github.sarxos.webcam.WebcamPicker as WebcamPicker
import com.github.sarxos.webcam.WebcamResolution as WebcamResolution

class VideoView(Jynstrument, ItemListener):

    def getExpectedContextClassName(self):
        # This Jynstrument likes to be in a ThrottleFrame and not anywhere else
        return "jmri.jmrit.throttle.ThrottleFrame"

    def init(self):
        self.setLayout( BorderLayout() )        
        self.miSelector =  JCheckBoxMenuItem ( "Show camera selector" )
        self.miSelector.addItemListener(self)
        self.miFill =  JCheckBoxMenuItem ( "Fill window" )
        self.miFill.addItemListener(self)
        self.getPopUpMenu().add( self.miFill )
        self.getPopUpMenu().add( self.miSelector )
        self.webcam = Webcam.getDefault()
        self.addCamPanel()

    def quit(self):   # very important to clean up everything to make sure GC will collect us
        self.webcamPanel.stop()
        self.webcam.close()
        self.webcam = None

    def addCamPanel(self):
        self.webcam.close()
        self.webcam.setViewSize(WebcamResolution.VGA.getSize());
        self.webcamPanel = WebcamPanel(self.webcam)
        self.webcamPanel.setMirrored(True)
        if ( self.miFill.isSelected() ) :
            self.webcamPanel.setFillArea( True  )
        else :
            self.webcamPanel.setFitArea( True )
        self.add(self.webcamPanel, BorderLayout.CENTER)
        self.webcam.open()

# this is a good way to make sure that we're are actaully GCed 
    def __del__(self):  
        print "in destructor"

    def itemStateChanged(self, evt):
        if (evt.getSource() == self.miSelector ):
            if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED ) :                           
                self.picker = WebcamPicker();
                self.picker.addItemListener(self)
                self.add(self.picker, BorderLayout.PAGE_END)                
            else :
                self.remove(self.picker)                 
                self.picker = None                
                self.revalidate()
        if (evt.getSource() == self.miFill ):
            if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED ) :
                self.webcamPanel.setFillArea( True  )
            else :
                self.webcamPanel.setFitArea( True )
        if (evt.getSource() == self.picker ):
            self.webcamPanel.stop()
            self.remove(self.webcamPanel)
            self.webcam.close()
            self.webcam = evt.getItem()
            self.addCamPanel()
            self.revalidate()
