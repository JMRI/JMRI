# RosterExportToDCC-EX.py, v1.0.0, 04/13/2023
# Export all JMRI Roster Entries as ROSTER() macro calls
#  Syntax: ROSTER(999,"Loco Name","F0/F1/*F2/F3/F4/F5/F6/F7/F8")
#  Open the Script Output Window, run the script, and then copy the lines into your myAutomation.h
# NOTE: EX-CommandStation cannot (yet) handle multiple roster entries with the same address
# Author: mstevetodd, copyright 2023
# Part of the JMRI distribution

#import jmri
import jmri.jmrit.roster

print("")
print("//copy the lines below into your myAutomation.h file for EX-CommandStation")

dups = []

# get the list of roster entries
rosterlist = jmri.jmrit.roster.Roster.getDefault().matchingList(None, None, None, None, None, None, None)

# loop thru the entries
for entry in rosterlist.toArray() :
  da = entry.getDccAddress()

  # EX-CommandStation doesn't support multiple entries with same address (yet)
  if (da in dups) :   
    commentOut = "//multiples not supported, skipping: "
  else : 
    commentOut = ""
    dups.append(da)    

  # start macro syntax
  rc = commentOut + "ROSTER(" + da + ",\"" + entry.getId() + "\",\"" 
  
  # loop thru and append function labels
  for func in range(0, entry.getMAXFNNUM()+1):   
    label = entry.getFunctionLabel(func)
    if (label is None) : label = ""
    else :
      label = label.replace("/","-").replace("*","-").replace("\"","-")  # replace some unsupported chars
      if (not entry.getFunctionLockable(func)) : label = '*' + label # prepend '*' to indicate momentary
    rc = rc + label + "/" # add this label to entry

  # close the syntax and write out the completed line
  print (rc + "\")")
