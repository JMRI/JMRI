# Script to test a locomotive and decoder after installation.
#
# It pops a field for entering the locomotive address, and 
# a "Go" button.  Upon clicking the button, it 
# sequences through a series of operations:
# F0 on
# F0 off
# F1 on
# F1 off
#
# Author: Bob Jacobsen, copyright 2004
# Part of the JMRI distribution

import jmri

import java
import javax.swing

class LocoTest(jmri.jmrit.automat.AbstractAutomaton) :
    
    # define how long to wait between settings (seconds)
    delay = 2
    
    # init() is called exactly once at the beginning to do
    # any necessary configuration.
    def init(self):
    
        self.status.text = "Getting throttle"
        number = int(self.address.text)
        if (number > 100) :
            long = True
        else :
            long = False
        self.throttle = self.getThrottle(number, long)
        if (self.throttle == None) :
            print "Couldn't assign throttle!"
        return
        
    # handle() will only execute once here, to run a single test
    #
    # Modify this to do your calculation.
    def handle(self):

        # set loco to forward, stopped
        self.status.text = "Forward, stopped"
        self.throttle.speedSetting = 0.
        self.throttle.setIsForward(True)
        self.waitMsec(self.delay*1000)
                
        # set loco to forward, 50%
        self.status.text = "Forward, 50%"
        self.throttle.speedSetting = 0.5
        self.throttle.setIsForward(True)
        self.waitMsec(self.delay*1000)
                
        # set loco to forward, 100%
        self.status.text = "Forward, 100%"
        self.throttle.speedSetting = 1.0
        self.throttle.setIsForward(True)
        self.waitMsec(self.delay*1000)
                
        # set loco to reverse, stopped
        self.status.text = "Reverse, stopped"
        self.throttle.speedSetting = 0.
        self.throttle.setIsForward(False)
        self.waitMsec(self.delay*1000)
                
        # set loco to reverse, 50%
        self.status.text = "Reverse, 50%"
        self.throttle.speedSetting = 0.5
        self.throttle.setIsForward(False)
        self.waitMsec(self.delay*1000)
                
        # set loco to reverse, 100%
        self.status.text = "Reverse, 100%"
        self.throttle.speedSetting = 1.0
        self.throttle.setIsForward(False)
        self.waitMsec(self.delay*1000)
         
        # set loco to forward, stopped,
        # and start function tests
        self.throttle.speedSetting = 0.
        self.throttle.setIsForward(True)


        self.status.text = "F0 on, forward"
        self.throttle.setF0(True);
        self.throttle.setIsForward(True)
        self.waitMsec(self.delay*1000)
                
        self.status.text = "F0 on, reverse"
        self.throttle.setF0(True);
        self.throttle.setIsForward(False)
        self.waitMsec(self.delay*1000)
        
        self.status.text = "F0 off"
        self.throttle.setF0(False);
        self.throttle.setIsForward(True)
        self.waitMsec(self.delay*1000)

        self.status.text = "F1 on"
        self.throttle.setF1(True);
        self.waitMsec(self.delay*1000)

        self.status.text = "F1 off"
        self.throttle.setF1(False);
        self.waitMsec(self.delay*1000)

        # done!
        self.status.text = "Done"
        self.throttle.release(None)
        #re-enable button
        self.startButton.enabled = True
        # and stop
        return 0

    # define what button does when clicked and attach that routine to the button
    def whenMyButtonClicked(self,event) :
        self.start()
        # we leave the button off
        self.startButton.enabled = False

        return
        
    # routine to show the panel, starting the whole process     
    def setup(self):
        # create a frame to hold the button, set up for nice layout
        f = javax.swing.JFrame("Data entry")       # argument is the frames title
        f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.Y_AXIS))

        # create the text field
        self.address = javax.swing.JTextField(5)    # sized to hold 5 characters, initially empty

        # put the text field on a line preceded by a label
        temppanel1 = javax.swing.JPanel()
        temppanel1.add(javax.swing.JLabel("Address"))
        temppanel1.add(self.address)
    
        # create the button
        self.startButton = javax.swing.JButton("Start")
        self.startButton.actionPerformed = self.whenMyButtonClicked

        self.status = javax.swing.JLabel("Enter address & click start")
        
        # Put contents in frame and display
        f.contentPane.add(temppanel1)
        temppanel2 = javax.swing.JPanel()
        temppanel2.add(self.startButton)
        f.contentPane.add(temppanel2)
        f.contentPane.add(self.status)
        f.pack()
        f.show()

        return

# create one of these
a = LocoTest()

# set the name, as a example of configuring it
a.setName("Locomotive test script")

# set the time between settings
#a.delay = 3

# and show the initial panel
a.setup()
