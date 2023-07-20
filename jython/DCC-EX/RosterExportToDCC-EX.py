# RosterExportToDCC-EX.py, v1.2.0, 04/14/2023
# Export all JMRI Roster Entries as ROSTER() macro calls
#  Syntax: ROSTER(999,"Loco Name","F0/F1/*F2/F3/F4/F5/F6/F7/F8")
#  This is accessed from the "DCC++ Menu" (See jmri.jmrix.dccpp.swing.DCCppRosterExportAction.java)
#  Run the script. It will show a list of formatted ROSTER() calls which you can copy.
#  Copy the lines into your myAutomation.h and compile+upload DCC-EX to your Arduino. 
# NOTE: EX-CommandStation cannot (yet) handle multiple roster entries with the same address
# Author: mstevetodd, copyright 2023
# Part of the JMRI distribution

import java
import jmri
import java.awt
import javax.swing

#find or create the display to show the macro calls
title = "Roster Export To DCC-EX"
f = jmri.util.JmriJFrame.getFrame(title)
if (f is None) :
  f = jmri.util.JmriJFrame(title)
  jta = javax.swing.JTextArea(100,100)
  jsp = javax.swing.JScrollPane(jta)
  f.getContentPane().add(jsp, java.awt.BorderLayout.CENTER)
  f.pack();
else :
  jta = f.getContentPane().getComponents()[0].getComponents()[0].getComponents()[0] #get the text area from the open window
f.setExtendedState(java.awt.Frame.NORMAL) #restore if minimized
f.setVisible(True); 

#build a text message that will be displayed in the new window
msg = "//copy the lines below into your myAutomation.h file for EX-CommandStation\n"

dups = []

# get the list of roster entries
rosterlist = jmri.jmrit.roster.Roster.getDefault().matchingList(None, None, None, None, None, None, None)

# loop thru the entries
for entry in rosterlist.toArray() :
  da = entry.getDccAddress()

  # EX-CommandStation doesn't support address 0
  if (int(da)==0) :
    commentOut = "//address 0 not supported, skipping: "
  else :
    # EX-CommandStation doesn't support multiple entries with same address (yet)
    if (da in dups) :   
      commentOut = "//duplicate address not supported, skipping: "
    else : 
      commentOut = ""
      dups.append(da)    

  # start macro syntax and replace some unsupported chars
  rc = commentOut + "ROSTER(" + da + ",\"" + entry.getId().replace("\"","'") + "\",\"" 
  
  # loop thru and append function labels
  for func in range(0, entry.getMaxFnNumAsInt()+1):   
    label = entry.getFunctionLabel(func)
    if (label is None) : label = ""
    else :
      label = label.replace("/","-").replace("*","-").replace("\"","'")  # replace some unsupported chars
      if (not entry.getFunctionLockable(func)) : label = '*' + label # prepend '*' to indicate momentary
    rc = rc + label + "/" # add this label to entry

  # close the syntax and add this completed line to the message
  msg += rc + "\")\n"

#put the message in the window's text area
msg += "//copy the lines above into your myAutomation.h file for EX-CommandStation\n"
jta.setText(msg)
