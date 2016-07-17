# Sample script showing how to  
# create a RosterEntry and store it
#
# Author: Bob Jacobsen, copyright 2008
# Part of the JMRI distribution

import jmri
import jmri.jmrit.roster
import java

# create a new entry
entry = jmri.jmrit.roster.RosterEntry()

# first, include it in the Roster
jmri.jmrit.roster.Roster.getDefault().addEntry(entry)

# need to set the ID to something
entry.setId("New empty entry")

# set the decoder model, family
entry.setDecoderModel("LE030")
entry.setDecoderFamily("Lenz 4th gen BEMF decoders")

# give it a file name using the usual pattern
entry.ensureFilenameExists()

# store results
entry.writeFile(None, None, None)
jmri.jmrit.roster.Roster.writeRosterFile()
