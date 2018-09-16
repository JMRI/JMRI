import java
import java.awt
import java.awt.event
import jmri
import jmri.jmrit.jython.Jynstrument as Jynstrument
import java.awt.CardLayout as CardLayout
import jmri.util.swing.ResizableImagePanel as ResizableImagePanel
import java.awt.event.MouseListener as MouseListener
import jmri.jmrit.throttle.AddressListener as AddressListener
from java.lang import Thread, Runnable

class SoundOn2xF0(Jynstrument, AddressListener, MouseListener):
# Jynstrument mandatory part
# Here this JYnstrument like to be in a ThrottleFrame and no anywhere else
    def getExpectedContextClassName(self):
        return "jmri.jmrit.throttle.ThrottleFrame"

    def init(self):
        self.setLayout( CardLayout() )
        self.labelOff = ResizableImagePanel(self.getFolder() + "/SoundOff.png",100,100 ) 
        self.labelOn = ResizableImagePanel(self.getFolder() + "/SoundOn.png",100,100 )
        self.add(self.labelOff, "off")
        self.add(self.labelOn, "on")
        self.addComponentListener(self.labelOff)
        self.addComponentListener(self.labelOn)
        self.addMouseListener(self)
        self.getContext().getAddressPanel().addAddressListener(self)
        self.throttle = self.getContext().getAddressPanel().getThrottle()
        self.onOffValue = False # There is no way to ask for value, hence this value
        self.updateThrottle()
        self.setIcon()

    def quit(self):   # very important to clean up everything to make sure GC will collect us
        self.cleanThrottle()
        self.getContext().getAddressPanel().removeAddressListener(self)

# this is a good way to make sure that we're are actaully GCed 
# using memory watcher in development menu, we can force a GC from there
    def __del__(self):  
        print "in destructor"

#Inner workings:
    def updateThrottle(self):    # update throttle informations when a new one is detected
        pass
        
    def cleanThrottle(self):     # clean up throttle information when it is deconnected
        self.throttle = None

    def switch(self):      # actually do function value change
        if self.throttle != None :
            t = Thread( Sequence( self.throttle ) )
            t.start()      
            self.onOffValue = not self.onOffValue
            self.setIcon()

    def setIcon(self):     # update appearance
        cl = self.getLayout()
        if self.onOffValue :
            cl.show(self, "on")
        else :
            cl.show(self, "off")

#AddressListener part: to listen for address changes in address panel (release, acquired)
    def notifyAddressChosen(self, address):
        pass

    def notifyAddressThrottleFound(self, throttle):
        self.throttle = throttle
        self.updateThrottle()
        self.setIcon()
    
    def notifyAddressReleased(self, address):
        self.cleanThrottle()
        self.setIcon()

    def notifyConsistAddressChosen(self, address, isLong):
        self.notifyAddressChosen(address)

    def notifyConsistAddressThrottleFound(self, throttle):
        self.notifyAddressThrottleFound(throttle)

    def notifyConsistAddressReleased(self, address, isLong):
        self.notifyAddressReleased(address)

#MouseListener part: to listen for mouse events
    def mouseReleased(self, event):
        self.switch()

    def mousePressed(self, event):
        pass
    def mouseClicked(self, event):
        pass
    def mouseExited(self, event):
        pass
    def mouseEntered(self, event):
        pass

class Sequence(Thread):
    def __init__( self, t ):
        self.throttle = t
        self.timeSleep = 500
        
    def run(self):
        funVal = self.throttle.getF0()
        self.throttle.setF0( not funVal )   # HERE!
        print self.timeSleep
        Thread.sleep(self.timeSleep)
        self.throttle.setF0( funVal )   # HERE!        

