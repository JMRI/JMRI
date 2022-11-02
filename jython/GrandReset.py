#  Grand Reset
#     Delete all transits
#     Delete all sections
#     Delete all Signal Mast Logics
#     Remove all saved paths

#  Caution:
#      Only use this script if you have built a valid representation
#      of your railroad with Layout Editor.

#
#  This script (if you respond "OK" to the confirmation dialog) removes
#     all transits, sections and SignalMastLogics.
#     To be effective, you must do a "Store" operation, quit the application
#     and restart it.
#  You can then go to the Signal Mast Logic table
#     and select Tools "Auto Generate Signaling Pairs".
#     If you know all the pairs are already correct, you can select the
#     option to "Generate Sections from Mast Pairs".
#  Finally you can rebuild Transits right click signals on your
#     Layout Editor panel and using "Create Transit from here".
#     Follow that by right clicking the next signal and selecting "Add to Transit".
#     Continue and end with "Add to Transit and Complete"
#

import jmri
import java
from javax.swing import JOptionPane

#  verify that we REALLY want to do this:
msg = 'Are you SURE that you want do this?\n- Deletes Transits\n- Deletes Sections\n- Deletes SML\n- Deletes Block Paths'
msg = msg + '\nNote: Do a Store and Quit when done.'

response = JOptionPane.showConfirmDialog(None, msg, 'Grand Reset', JOptionPane.OK_CANCEL_OPTION)
if response == 0 :
    # if the user doesn't click "OK", we want to leave the script

    #   make a copy of transit list (you can't modify the list itself when looping through it
    #   attempting to use the original list results in ConcurrentModificationException
    newList = []
    for trans in transits.getNamedBeanSet() :
        newList.append(trans)
    # remove all transits
    for trans in newList :
        transits.deleteTransit(trans)

    #   make a copy of section list (you can't modify the list itself when looping through it
    #   attempting to use the original list results in ConcurrentModificationException
    newList = []
    for section in sections.getNamedBeanSet():
        newList.append(section)
    # remove all sections
    for section in newList:
        sections.deleteSection(section)

    mastManager = jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager)
    mastList = mastManager.getSignalMastLogicList()
    # remove all Signal Mast Logics
    # this call removes all logics from the specified mast, regardless of destination mast
    for mast in mastList:
        mastManager.removeSignalMastLogic(mast)

    # remove all saved paths from the Block menu (not effective until a Save and Quit)
    jmri.InstanceManager.getDefault(jmri.BlockManager).setSavedPathInfo(False)
    msg = 'Grand reset done\nDo a Store and Quit'
    JOptionPane.showMessageDialog(None, msg, 'Grand Reset', JOptionPane.INFORMATION_MESSAGE)
else :
    print "Script aborted"
