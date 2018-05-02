# Sample script to show a JFrame for data entry.
# The frame contains two JTextFields, and a button
# which is inactive until data has been entered.  Once 
# activated, the button prints a little diagnostic
# message when clicked.  The print statement can be 
# changed to include whatever desired, e.g. throw a turnout,
# program a CV, etc.
#
# Comments for/by a non-programmer.
# To get this to script to run, start DecoderPro
# (set preferences to "Loconet Simulator to run without connecting to a layout),
# then under Panels select Run Script. Now find and select this script
# in the jython folder of the JMRI program. This script
# creates a panel called "Data entry" with two fields that
# data is entered in. The script waits for something in both
# fields to be entered and then enables "Enter values". When
# "Enter values" button is clicked, it causes the data to be displayed
# on the Java console and the "Data entry" panel to disappear.
# To see the output on the Java console you need to open the
# Java console. On a Windows System the Java console can be
# displayed by clicking the icon for it on the Taskbar.
#      comments added by wsthompson@earthlink.net 20061101
#
# Added comment explaining how to change field size.
#                     by wsthompson@earthlink.net 20090202
#
#
#
# Author: Bob Jacobsen, copyright 2004, 2006
# Part of the JMRI distribution

import jmri

import java
import javax.swing

# create a frame to hold the button, set up for nice layout
f = javax.swing.JFrame("Data entry")       # argument is the frames title
f.contentPane.setLayout(javax.swing.BoxLayout(f.contentPane, javax.swing.BoxLayout.Y_AXIS))

# keep track of whether both fields have been changed
addressChanged = False    # True means the field has changed
commandChanged = False

# Create the first text field
# Sized to show 5 characters, initially empty
# To make the field a different size, change the (5) to the desired size
address = javax.swing.JTextField(5)    

# put the text field on a line preceded by a label
temppanel1 = javax.swing.JPanel()
temppanel1.add(javax.swing.JLabel("Address"))
temppanel1.add(address)

# create the second text field similarly
command = javax.swing.JTextField(5)    # sized to show 5 characters

temppanel2 = javax.swing.JPanel()
temppanel2.add(javax.swing.JLabel("Command"))
temppanel2.add(command)

# have that text field enable the button when OK
def whenAddressChanged(event) :                
    global addressChanged, commandChanged
    if (address.text != "") :                  # address only changed if a value was entered
        addressChanged = True
    if (commandChanged and addressChanged) :   # if both have been changed
        enterButton.setEnabled(True)
    return
    
address.actionPerformed = whenAddressChanged   # if user hit return or enter
address.focusLost = whenAddressChanged         # if user tabs away

# have that 2nd text field enable the button when OK also
def whenCommandChanged(event) :
    global addressChanged, commandChanged
    if (command.text != "") :
        commandChanged = True
    if (commandChanged and addressChanged) :
        enterButton.setEnabled(True)
    return
    
command.actionPerformed = whenCommandChanged
command.focusLost = whenCommandChanged

# create the button
enterButton = javax.swing.JButton("Enter values")
enterButton.setEnabled(False)           # button starts as grayed out (disabled)

# define what button does when clicked and attach that routine to the button
def whenMyButtonClicked(event) :
        print "clicked with address: ", address.text, " command: ", command.text
        f.dispose()
        return
        
enterButton.actionPerformed = whenMyButtonClicked

# Put contents in frame and display
f.contentPane.add(temppanel1)
f.contentPane.add(temppanel2)
f.contentPane.add(enterButton)
f.pack()
f.show()

