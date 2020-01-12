# Script to operate a Roco crane from screen
#
# The crane DCC addresses are set by the user in the main window
# This is for the older Roco crane (part number 46800 (DCC))or newer ones where the user has not set CV53 to 2
# Functions are as follows
    # Rotating the crane:
    # F0
    # LED (GREEN) flashes in single-cycle interval
    # Raising and lowering the Boom:
    # F1
    # LED (GREEN) flashes in two-cycle interval
    # Raising and lowering the crane hook
    # F0 F1
    # LED (GREEN) flashes in three-cycle interval
    # Additional function Magnet, Bucket or Spotlight
    # Throttle Rev and 1 speed step = OFF, Fwd and 1 speed step = ON
    # LED may light up RED
#
# Author: Nelson Allison, copyright 2009
# With help from Bob Jacobsen
# Part of the JMRI distribution

import jmri
import java
import java.awt
import javax.swing

class RocoCrane(jmri.jmrit.automat.AbstractAutomaton) :
        
    # init() is called exactly once at the beginning to do
    # any necessary configuration.
    def init(self):

        self.status.text = "Getting throttle"
        Addr = int(self.Address.text)
    if (Addr > 100) :
        long = True
    else :
        long = False
        self.Throttle = self.getThrottle(Addr, long)
        if (self.Throttle == None) :
             print "Couldn't assign throttle!"
        else :
             self.status.text ="Crane Ready"
        return

        
    # handle() will only execute once here, and doesn't do much
    #
    # Modify this to do your calculation.
    def handle(self):

        # and stop
        return 0

    # define what each button does when clicked
    def whenStartButtonClicked(self,event) :
        self.start()
        self.Address.enabled = False
        self.StartButton.enabled = False
    self.ReleaseButton.enabled = True
        self.AccessoryButton.enabled = True
        self.RotateLeftButton.enabled = True
        self.RotateRightButton.enabled = True
        self.BoomDownButton.enabled = True
        self.BoomUpButton.enabled = True
        self.HookDownButton.enabled = True
        self.HookUpButton.enabled = True
        return

    def whenReleaseButtonClicked(self,event) :
    self.Throttle.release(None)
        self.status = javax.swing.JLabel("Enter address & click start")
        self.Address.enabled = True
        self.StartButton.enabled = True
    self.ReleaseButton.enabled = False
        self.AccessoryButton.enabled = False
        self.RotateLeftButton.enabled = False
        self.RotateRightButton.enabled = False
        self.BoomDownButton.enabled = False
        self.BoomUpButton.enabled = False
        self.HookDownButton.enabled = False
        self.HookUpButton.enabled = False
    return
    
    def whenAccessoryButtonClicked(self,event) :
        if (self.AccessoryButton.isSelected()) :
            self.Throttle.setF0(False)
        self.Throttle.setF1(False)
        self.Throttle.setIsForward(True)
        self.Throttle.speedSetting = 0.1
        self.Throttle.speedSetting = 0.
            self.status.text = "Accessory on"
        else :
            self.Throttle.setIsForward(False)
        self.Throttle.setF0(False)
        self.Throttle.setF1(False)
        self.Throttle.speedSetting = 0.1
        self.Throttle.speedSetting = 0.
            self.status.text = "Accessory off"
        return

    def startRotateRight(self,event) :
    self.Throttle.setF1(False)
        self.Throttle.setF0(True)
    self.Throttle.setIsForward(True)
    self.Throttle.speedSetting = 0.5
        self.status.text = "Rotating Right"
        return
    def endRotateRight(self,event) :
    self.Throttle.speedSetting = 0.
        self.status.text = "Rotation Stopped"
        return

    def startRotateLeft(self,event) :
        self.Throttle.setF1(False)
    self.Throttle.setF0(True)
        self.Throttle.setIsForward(False)
    self.Throttle.speedSetting = 0.5
    self.status.text = "Rotating Left"
        return
    def endRotateLeft(self,event) :
    self.Throttle.speedSetting = 0.
        self.status.text = "Rotation Stopped"
        return

    def startBoomUp(self,event) :
        self.Throttle.setF0(False)
    self.Throttle.setF1(True)
    self.Throttle.setIsForward(True)
    self.Throttle.speedSetting = 0.5
        self.status.text = "Rasing Boom"
        return
    def endBoomUp(self,event) :
    self.Throttle.speedSetting = 0.
        self.status.text = "Boom Stopped"
        return

    def startBoomDown(self,event) :
        self.Throttle.setF0(False)
    self.Throttle.setF1(True)
    self.Throttle.setIsForward(False)
    self.Throttle.speedSetting = 0.5
        self.status.text = "Lowering Boom"
        return
    def endBoomDown(self,event) :
    self.Throttle.speedSetting = 0.
        self.status.text = "Boom Stopped"
        return
    
    def startHookUp(self,event) :
        self.Throttle.setF0(True)
    self.Throttle.setF1(True)
    self.Throttle.setIsForward(True)
    self.Throttle.speedSetting = 0.5
        self.status.text = "Rasing Hook"
        return
    def endHookUp(self,event) :
    self.Throttle.speedSetting = 0.
        self.status.text = "Hook Stopped"
        return

    def startHookDown(self,event) :
    self.Throttle.setF0(True)
    self.Throttle.setF1(True)
    self.Throttle.setIsForward(False)
    self.Throttle.speedSetting = 0.5
        self.status.text = "Lowering Hook"
        return
    def endHookDown(self,event) :
        self.Throttle.speedSetting = 0.
        self.status.text = "Hook Stopped"
        return
    
    # routine to show the panel, starting the whole process     
    def setup(self):
        # create a frame to hold the button, set up for nice layout
        f = javax.swing.JFrame("Roco Crane")       # argument is the frames title
        f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.Y_AXIS))
        
    # create the text field
        self.Address = javax.swing.JTextField(5)    # sized to hold 5 characters, initially empty
    
    # create the buttons
        self.StartButton = javax.swing.JButton("Start")
        self.StartButton.actionPerformed = self.whenStartButtonClicked
    
    self.ReleaseButton = javax.swing.JButton("Release")
    self.ReleaseButton.actionPerformed = self.whenReleaseButtonClicked
    
        self.AccessoryButton = javax.swing.JToggleButton("Accessory")
        self.AccessoryButton.itemStateChanged = self.whenAccessoryButtonClicked
    
        self.RotateLeftButton = javax.swing.JButton("Rotate Left")
        self.RotateLeftButton.mousePressed = self.startRotateLeft
        self.RotateLeftButton.mouseReleased = self.endRotateLeft

        self.RotateRightButton = javax.swing.JButton("Rotate Right")
        self.RotateRightButton.mousePressed = self.startRotateRight
        self.RotateRightButton.mouseReleased = self.endRotateRight

        self.BoomDownButton = javax.swing.JButton("Boom Down")
        self.BoomDownButton.mousePressed = self.startBoomDown
        self.BoomDownButton.mouseReleased = self.endBoomDown
        
        self.BoomUpButton = javax.swing.JButton("Boom Up")
        self.BoomUpButton.mousePressed = self.startBoomUp
        self.BoomUpButton.mouseReleased = self.endBoomUp
    
        self.HookDownButton = javax.swing.JButton("Hook Down")
        self.HookDownButton.mousePressed = self.startHookDown
        self.HookDownButton.mouseReleased = self.endHookDown
    
        self.HookUpButton = javax.swing.JButton("Hook Up")
        self.HookUpButton.mousePressed = self.startHookUp
        self.HookUpButton.mouseReleased = self.endHookUp
    
    self.ReleaseButton.enabled = False
        self.AccessoryButton.enabled = False
        self.RotateLeftButton.enabled = False
        self.RotateRightButton.enabled = False
        self.BoomDownButton.enabled = False
        self.BoomUpButton.enabled = False
        self.HookDownButton.enabled = False
        self.HookUpButton.enabled = False
    
        # add the status field
        self.status = javax.swing.JLabel("Enter address & click start")
        
        # Put contents in frame and display
    
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        f.contentPane.add(temppanel)
    
        # put the text field on a line preceded by a label
        temppanel = javax.swing.JPanel()
        temppanel.add(javax.swing.JLabel("Address"))
        temppanel.add(self.Address)
        f.contentPane.add(temppanel)
    
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())  
        temppanel.add(self.StartButton)
    temppanel.add(self.ReleaseButton)
        f.contentPane.add(temppanel)
    
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.RotateLeftButton)
        temppanel.add(self.RotateRightButton)
        f.contentPane.add(temppanel)
    
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.BoomDownButton)
        temppanel.add(self.BoomUpButton)
        f.contentPane.add(temppanel)
    
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.HookDownButton)
        temppanel.add(self.HookUpButton)
        f.contentPane.add(temppanel)
        
        temppanel = javax.swing.JPanel()
        temppanel.setLayout(java.awt.FlowLayout())
        temppanel.add(self.AccessoryButton)
        f.contentPane.add(temppanel)
    
        f.contentPane.add(self.status)
        f.pack()
        f.show()

        return

# create one of these
a = RocoCrane()

# set the name
a.setName("Crane setup")

# and show the initial panel
a.setup()


