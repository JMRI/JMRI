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
# Part of the JMRI distribution

import jmri

import java
import java.io
import com.csvreader

b = java.io.FileReader(java.io.File("demo.csv"))
c = com.csvreader.CsvReader(b)
c.readRecord()

while (c.getColumnCount() > 2) :
    systemname = c.get(0)
    username = c.get(1)
    comment = c.get(2)
    turnout = turnouts.provideTurnout(systemname)
    if (username != "") :
        turnout.setUserName(username)
    if (comment != "") :
        turnout.setComment(comment)    
    print c.get(0),"/",c.get(1),"/",c.get(2)
    c.readRecord()
    

