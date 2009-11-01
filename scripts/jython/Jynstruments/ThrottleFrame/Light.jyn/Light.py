import jmri.util.jynstrument.Jynstrument as Jynstrument
import java.awt.CardLayout as CardLayout
import jmri.util.ResizableImagePanel as ResizableImagePanel
import java.awt.event.MouseListener as MouseListener
import java.beans.PropertyChangeListener as PropertyChangeListener
import jmri.jmrit.throttle.AddressListener as AddressListener
import jmri.jmrit.throttle.FunctionListener as FunctionListener

class Light(Jynstrument, PropertyChangeListener, AddressListener, MouseListener, FunctionListener):
#Jynstrument part
    def getExpectedContextClassName(self):
        return "jmri.jmrit.throttle.ThrottleFrame"

    def init(self):
        self.setLayout( CardLayout() )
        self.labelOff = ResizableImagePanel(self.getFolder() + "/LightOff.png",100,100 ) 
        self.labelOn = ResizableImagePanel(self.getFolder() + "/LightOn.png",100,100 )
        self.add(self.labelOff, "off")
        self.add(self.labelOn, "on")
        self.addComponentListener(self.labelOff)
        self.addComponentListener(self.labelOn)
        self.addMouseListener(self)
        self.getContext().getAddressPanel().addAddressListener(self)
        self.throttle = self.getContext().getAddressPanel().getThrottle()
        self.updateThrottle()
        self.setIcon()

    def quit(self):
        self.cleanThrottle()
        self.getContext().getAddressPanel().removeAddressListener(self)

#Inner workings:
    def updateThrottle(self):
        if self.throttle != None :
            self.throttle.addPropertyChangeListener(self)
        self.getContext().getFunctionPanel().getFunctionButtons()[0].addFunctionListener(self)
        
    def cleanThrottle(self):
        if self.throttle != None :
            self.throttle.removePropertyChangeListener(self)
        self.getContext().getFunctionPanel().getFunctionButtons()[0].removeFunctionListener(self)
        self.throttle = None

    def switch(self):
        if self.throttle != None :
            self.throttle.setF0( not self.throttle.getF0() )
        self.setIcon()

    def setIcon(self):
        cl = self.getLayout()
        if self.throttle == None :
            cl.show(self, "off")
        elif self.throttle.getF0() :
            cl.show(self, "on")
        else :
            cl.show(self, "off")

#FunctionListener
    def notifyFunctionStateChanged(self, functionNumber, isOn):
        self.setIcon()

    def notifyFunctionLockableChanged(self, functionNumber, isLockable):
        pass

#PropertyChangeListener part
    def propertyChange(self, event):
        if event.getPropertyName() == "F0" :
            self.setIcon()

#AddressListener part
    def notifyAddressChosen(self, address, isLong):
        pass

    def notifyAddressThrottleFound(self, throttle):
        self.throttle = throttle
        self.updateThrottle()
        self.setIcon()
    
    def notifyAddressReleased(self, address, isLong):
        self.cleanThrottle()
        self.setIcon()

#MouseListener part
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

