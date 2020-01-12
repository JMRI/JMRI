# Script to migrate specific decoder names 
# to new names.
#
# Author: Bob Jacobsen, copyright 2008
# Part of the JMRI distribution

import jmri
import jmri.jmrit.roster
import java
import javax.swing

# define a routine that looks for a specific model, and offers to change to a list
def checkForEntries(model, chooseList) :
    rosterlist = jmri.jmrit.roster.Roster.getDefault().matchingList(None, None, None, None, None, None, None)
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
        entry.setDecoderFamily("Tsunami Steam Bachmann OEM")
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
    
checkForEntries("Tsunami Bachmann OEM",["HO 2-6-6-2",
                                        "HO 2-8-0",
                                        "HO 2-10-0 Russian Decapod",
                                        "HO 2-10-2 USRA Light",
                                        "HO 4-4-0 Low Boiler",
                                        "HO 4-4-0 High Boiler",
                                        "HO 4-6-0",
                                        "HO 4-6-2 K4",
                                        "HO 4-8-2 USRA Heavy",
                                        "HO 4-8-4 J Class",
                                        "HO 3 Truck Shay",
                                        "On30 2-4-4 Forney",
                                        "On30 2-6-6-2",
                                        "On30 4-6-0",
                                        "On30 2 Truck T Boiler Shay",
                                        "On30 Climax"])
 

