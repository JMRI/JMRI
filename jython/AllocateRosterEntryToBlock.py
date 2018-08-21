# Script to allocate a roster Entry to a block. if the Layout editor is being used
# then this will allow the roster entry id or icon to be displayed on the panel.
#
# Version 1.0, 
#
# Kevin Dickerson, copyright 2012
#
# Components based on Bob Jacobsen's and Nigel Cliffe, scripts in JMRI distribution, 
# and comments on JMRI-Users group 

import javax.swing
import jmri

class LocoSelector(jmri.jmrit.automat.AbstractAutomaton) :

    # init() is called exactly once at the beginning to do
    # any necessary configuration.
    def init(self):
        # initialisation
        return
        
    # handle() will only execute once here, to run a single test
    #
    # Modify this to do your calculation.
    def handle(self):
        return 0    
        # use return 1 for an infinite loop, or return 0 for once through !


    # define what button does when clicked and attach that routine to the button
    def whenMyButtonClicked(self,event) :
        # Use this to perform actions from the "start" button. 
        # In this case we print the chosen loco's address
        mem = self.memory.getSelectedBean()
        mem.setValue(self.box.getSelectedItem())
        return
    
    # routine to show the panel, starting the whole process     
    def setup(self):
        # create a frame to hold the button, set up for nice layout
        f = javax.swing.JFrame("Allocate Roster Entry to Block")       # argument is the frames title
        f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.Y_AXIS))

        # top explanatory panel, text only
        temppanel0 = javax.swing.JPanel()
        temppanel0.add(javax.swing.JLabel("<html>Select your locomotive from the roster<br>Select the block that you wish to allocate it to<br>Then press the Allocate button.</html>"))

        # second panel has a label, then drop box and text field. 
        temppanel1 = javax.swing.JPanel()
        temppanel1.add(javax.swing.JLabel("Locomotive "))

        # create the drop-box from the roster, 
        # getEntriesWithAttributeKeyValue(String key, String value) 
        self.roster = jmri.jmrit.roster.Roster.getDefault()
        self.box = jmri.jmrit.roster.swing.GlobalRosterEntryComboBox()

        # add the drop-box, address field, and start button to the panel
        temppanel1.add(self.box)

        self.memory = jmri.util.swing.JmriBeanComboBox(blocks)
        temppanel2 = javax.swing.JPanel()
        temppanel2.add(javax.swing.JLabel("Block "))
        temppanel2.add(self.memory)

        temppanel3 = javax.swing.JPanel()
        # create the start button to perform actions.
        self.startButton = javax.swing.JButton("Allocate")
        self.startButton.actionPerformed = self.whenMyButtonClicked 
        temppanel3.add(self.startButton)


        # Put contents in frame and display
        f.contentPane.add(temppanel0)
        f.contentPane.add(temppanel1)
        f.contentPane.add(temppanel2)
        f.contentPane.add(temppanel3)

        f.pack()
        f.show()

        return



# create one of these
a = LocoSelector()

# set the name, as a example of configuring it
a.setName("Locomotive selection test script")

# and show the initial panel
a.setup()
