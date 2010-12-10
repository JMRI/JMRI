# Sample script showing how to loop through the Roster entries and export
# specific information to a .csv file.
#
# Inspired by Bob Jacobsen's 'RosterLoop' and 'CsvToTurnouts'
#
# Note: this will replace any existing file
#
# Author: Matthew Harris, copyright 2010
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $

import jmri.jmrit.roster
import com.csvreader

# Create output file.
# Unless modified here, this will create the output file in the same
# location as the roster itself.
# Default behaviour is for this to be in the user preferences directory
outFile = jmri.jmrit.roster.Roster.instance().getFileLocation()+"roster.csv"
print "Output file:", outFile
csvFile = com.csvreader.CsvWriter(outFile)

# Get a list of matched roster entries;
# the list of None's means match everything
rosterlist = jmri.jmrit.roster.Roster.instance().matchingList(None, None, None, None, None, None, None)

# Write the header line
csvFile.write("RosterID")
csvFile.write("RoadName")
csvFile.write("RoadNumber")
csvFile.write("Manufacturer")
csvFile.write("Owner")
csvFile.write("Model")
csvFile.write("Address")
csvFile.write("Is Long?")
csvFile.write("Speed Limit")
csvFile.write("Comment")
csvFile.write("Decoder Family")
csvFile.write("Decoder Model")
csvFile.write("Decoder Comment")
for func in range(0,28+1):
    csvFile.write("Fn%02d" % func)

# Notify the writer of the end of the header record
csvFile.endRecord()
print "Header written"

# now loop through the matched entries, outputing things
for entry in rosterlist.toArray() :
    # Most parameters are text-based, so can be output directly
    csvFile.write(entry.getId())
    csvFile.write(entry.getRoadName())
    csvFile.write(entry.getRoadNumber())
    csvFile.write(entry.getMfg())
    csvFile.write(entry.getOwner())
    csvFile.write(entry.getModel())
    csvFile.write(entry.getDccAddress())

    # 'isLongAddress' is a boolean function so we need
    # to deal with outputting that in this way
    if entry.longAddress:
        csvFile.write("Yes")
    else:
        csvFile.write("No")

    # Max Speed is a number - we need to convert to a string
    csvFile.write("%d%%" % entry.getMaxSpeedPCT())

    csvFile.write(entry.getComment())
    csvFile.write(entry.getDecoderFamily())
    csvFile.write(entry.getDecoderModel())
    csvFile.write(entry.getDecoderComment())
    
    # Now output function labels
    for func in range(0,28+1):
        csvFile.write(entry.getFunctionLabel(func))

    # Notify the writer of the end of this detail record
    csvFile.endRecord()
    print "Entry", entry.getId(), "written"

# Flush the write buffer and close the file
csvFile.flush()
csvFile.close()
print "Export complete"