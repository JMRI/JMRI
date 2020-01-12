# Script to illustrate Roster drop boxes
#
# Locomotive can be selected from Roster, or by directly typing in address,
# use as fragment for use in other scripts.
#
# Illustrative script, does nothing useful, add your own actions onto it!
#
# Version 1.0,
#
# Nigel Cliffe, copyright 2010
#
# Components based on Bob Jacobsen's scripts in JMRI distribution,
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
        locoChosen = self.address.text
        print "Loco Chosen is", locoChosen
        return

    def rosterBoxChange(self, event) :
        entry = self.box.getSelectedItem()
                if isinstance(entry, jmri.jmrit.roster.RosterEntry):
            theDccAddress = entry.getDccAddress()
            # print theDccAddress
            self.address.text = theDccAddress
                else:
                    self.address.text = ""
                    theDccAddress = 3
        return 0


    # routine to show the panel, starting the whole process
    def setup(self):
        # create a frame to hold the button, set up for nice layout
        f = javax.swing.JFrame("roster drop-box illustration")       # argument is the frames title
        f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.Y_AXIS))

        # top explanatory panel, text only
        temppanel0 = javax.swing.JPanel()
        temppanel0.add(javax.swing.JLabel("<html>Select your locomotive from the roster, or type in the address in the box.<br> Then press start button.</html>"))

        # second panel has a label, then drop box and text field.
        temppanel1 = javax.swing.JPanel()
        temppanel1.add(javax.swing.JLabel("Locomotive "))

        # create the drop-box from the roster,
        # getEntriesWithAttributeKeyValue(String key, String value)
        self.roster = jmri.jmrit.roster.Roster.getDefault()
        self.box = jmri.jmrit.roster.swing.GlobalRosterEntryComboBox()
        self.box.itemStateChanged = self.rosterBoxChange

        # create the text field
        self.address = javax.swing.JTextField(5)
        # Read the first entry from the roster drop box and write its DccAddress to the address box.
        entry = self.roster.entryFromTitle(self.box.getSelectedItem())
        if( entry != None ) :
            theDccAddress = entry.getDccAddress()
            self.address.text = theDccAddress

        # create the start button to perform actions.
        self.startButton = javax.swing.JButton("Start")
        self.startButton.actionPerformed = self.whenMyButtonClicked

        # add the drop-box, address field, and start button to the panel
        temppanel1.add(self.box)
        temppanel1.add(self.address)
        temppanel1.add(self.startButton)

        # Put contents in frame and display
        f.contentPane.add(temppanel0)
        f.contentPane.add(temppanel1)

        f.pack()
        f.show()

        return



# create one of these
a = LocoSelector()

# set the name, as a example of configuring it
a.setName("Locomotive selection test script")

# and show the initial panel
a.setup()
