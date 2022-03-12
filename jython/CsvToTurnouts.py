# Read a CSV (comma-separated variable) file containing
#
#  System Name
#  User Name (optional)
#  Comment (optional)
#
# and create a turnout from each line.
# Stops when it first encounters a line without
# three fields; remove all your blank lines!
# Values can be enclosed in quotes (single or double) if desired.
#
# Author: Bob Jacobsen, copyright 2010
# Author: Randall Wood, copyright 2020
# Part of the JMRI distribution

import jmri

import java
import java.io
import org.apache.commons.csv

b = java.io.FileReader(java.io.File("demo.csv"))
c = org.apache.commons.csv.CSVFormat.DEFAULT.parse(b)

for r in c.getRecords() :
    systemname = r.get(0)
    username = r.get(1)
    comment = r.get(2)
    turnout = turnouts.provideTurnout(systemname)
    if (username != "") :
        turnout.setUserName(username)
    if (comment != "") :
        turnout.setComment(comment)
    print systemname,"/",username,"/",comment


