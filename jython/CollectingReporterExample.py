# Provides an example of retreiving the collection maintained
# by a jmri.CollectingReporter.
#
# This script simply retreives the contents of the collection
# and prints each entry.  Entries may not be strings, so the
# output may require further interpretation, which this script
# does not perform.
#
# Author: Paul Bender, copyright 2019
# Part of the JMRI distribution
#
# The Reporter name is hardcoded in the example.  
# Change the value to something that makes sense for your layout

import jmri

# get the reporter manager from the InstanceManager
rm = jmri.InstanceManager.getDefault(jmri.ReporterManager)
# ask for the reporter, and store it in rptr.
rptr = rm.getReporter("ZRD5C3:7")
# ask the reporter for the collection
array = rptr.getCollection().toArray()
# and print each entry
for i in array:
   print i
