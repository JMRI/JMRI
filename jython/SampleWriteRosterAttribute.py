#
# Write an attribute to the Roster

import jmri

# get a specific entry by name
entry = jmri.jmrit.roster.Roster.instance().entryFromTitle("000test attribute")

# add an attribute
entry.putAttribute("newKey", "newValue")

# and rewrite the file
entry.updateFile();

