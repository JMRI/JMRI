# Script to migrate specific decoder names 
# to new names.
#
# Author: Bob Jacobsen, copyright 2008
# Part of the JMRI distribution
#

import jmri
import jmri.jmrit.roster
import java
import javax.swing

# define a routine that looks for a specific model, and offers to change to a list
def checkForEntries(model, chooseList) :
    rosterlist = jmri.jmrit.roster.Roster.instance().matchingList(None, None, None, None, None, None, None)
    # Loop through the list of all Roster entries, checking each one
    for entry in rosterlist.toArray() :
        if (entry.getDecoderModel() == model) :
            checkEntry(entry, chooseList)
    return
    
# define a routine to check entries
def checkEntry(entry, chooseList) : 
    print "Match ", entry.getId(), " model ", entry.getDecoderModel(), chooseList
    # running on Swing thread, pop request
    s = javax.swing.JOptionPane.showInputDialog(
                    None,
                    "For locomotive with ID: "+entry.getId()+"\n"
                    + "Current decoder model: "+entry.getDecoderModel()+"\n"
                    + "Please select a new decoder model:",
                    "Update/Change Decoder Model",
                    javax.swing.JOptionPane.PLAIN_MESSAGE,
                    None,
                    chooseList,
                    None);
    if (s!=None) :
        # selected
        print "New model: ", s
        entry.setDecoderModel(s)
        entry.updateFile()
        jmri.jmrit.roster.Roster.writeRosterFile()
    else :
        # not selected
        print "no change"
    return

############
#  main code
############

# class assumes it's being run on the Swing event dispatcher thread; confirm
if (not javax.swing.SwingUtilities.isEventDispatchThread()) :
    print "ERROR: NOT RUNNING ON SWING THREAD!!"
    
checkForEntries("Tsunami Heavy Steam",["TSU-750 Heavy Steam","TSU-1000 Heavy Steam","TSU-1000 Southern Steam"])
 
checkForEntries("Tsunami Medium Steam",["TSU-750 Medium Steam","TSU-1000 Medium Steam","TSU-1000 Southern Steam"])
 
checkForEntries("Tsunami Light Steam",["TSU-750 Light Steam","TSU-1000 Light Steam","TSU-1000 Southern Steam"])
 
checkForEntries("Tsunami Lt Logging Steam",["TSU-750 Lt Logging Steam","TSU-1000 Lt Logging Steam"])
 
checkForEntries("Tsunami DRG C-Class Steam",["TSU-750 DRGW C-Class Steam","TSU-1000 DRGW C-Class Steam"])
 
checkForEntries("Tsunami DRGW K-Class Steam",["TSU-750 DRGW K-Class Steam","TSU-1000 DRGW K-Class Steam"])

checkForEntries("Tsunami Cab Forward",["TSU-750 SP Cab Forward Steam","TSU-1000 SP Cab Forward Steam"])
 
all = ["TSU-750 Heavy Steam","TSU-1000 Heavy Steam",
       "TSU-750 Medium Steam","TSU-1000 Medium Steam",
       "TSU-750 Light Steam","TSU-1000 Light Steam",
       "TSU-750 Lt Logging Steam","TSU-1000 Lt Logging Steam",
       "TSU-750 DRGW C-Class Steam","TSU-1000 DRGW C-Class Steam",
       "TSU-750 DRGW K-Class Steam","TSU-1000 DRGW K-Class Steam",
       "TSU-750 SP Cab Forward Steam","TSU-1000 SP Cab Forward Steam",
       "TSU-1000 Southern Steam"
       ]

checkForEntries("Tsunami Steam",all)

