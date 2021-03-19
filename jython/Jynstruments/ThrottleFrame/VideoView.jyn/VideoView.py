# Author: Lionel Jeanson copyright 2009 - 2021
# Part of the JMRI distribution
#
# Create a Jynstrument with video view from connected webcam
#
# Build using https://github.com/sarxos/webcam-capture
# You'll need webcam-capture-XXX.jar and bridj-XXX.jar copied from webcam-capture release to your JMRI lib folder
#
depErr="""Required dependency must be installed!

 You need to install libraries from sarxos webcam library.
 
       https://github.com/sarxos/webcam-capture

 Copy to your JMRI lib folder :
     webcam-capture-driver-ipcam-XXX.jar
     bridj-XXX.jar (from zip file libs subfolder)
    """

import java
import java.awt
import java.awt.event
import java.beans
import jmri
import jmri.jmrit.jython.Jynstrument as Jynstrument
import java.awt.BorderLayout as BorderLayout
import java.awt.event.ItemListener as ItemListener
import javax.swing.JCheckBoxMenuItem as JCheckBoxMenuItem
import javax.swing.JOptionPane as JOptionPane
import javax.swing.JTextArea as JTextArea
import javax.swing.JFrame as JFrame
try:
    import com.github.sarxos.webcam.Webcam as Webcam
    import com.github.sarxos.webcam.WebcamPanel as WebcamPanel
    import com.github.sarxos.webcam.WebcamPicker as WebcamPicker
    import com.github.sarxos.webcam.WebcamResolution as WebcamResolution
except:
    JOptionPane.showMessageDialog(JFrame(), JTextArea(depErr), "Missing dependency", JOptionPane.ERROR_MESSAGE);

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
        self.miMirror =  JCheckBoxMenuItem ( "Mirror", True )
        self.miMirror.addItemListener(self)
        self.getPopUpMenu().add( self.miFill )
        self.getPopUpMenu().add( self.miSelector )
        self.getPopUpMenu().add( self.miMirror )
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
        self.webcamPanel.setMirrored(self.miMirror.isSelected())
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
        if (evt.getSource() == self.miMirror ):
            self.webcamPanel.setMirrored(self.miMirror.isSelected())
        if (evt.getSource() == self.picker ):
            self.webcamPanel.stop()
            self.remove(self.webcamPanel)
            self.webcam.close()
            self.webcam = evt.getItem()
            self.addCamPanel()
            self.revalidate()
