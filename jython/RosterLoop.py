# Sample script showing how to loop 
# through the Roster entries.
#
# This example just prints the name, but you
# can extract or use other info too.
#
# Author: Bob Jacobsen, copyright 2008
# Part of the JMRI distribution

import jmri
import jmri.jmrit.roster

# get a list of matched roster entries;
# the list of None's means match everything
rosterlist = jmri.jmrit.roster.Roster.instance().matchingList(None, None, None, None, None, None, None)

# now loop through the matched entries, printing things
for entry in rosterlist.toArray() :
  print entry.getId(), entry.getDccAddress(), entry.isLongAddress()

