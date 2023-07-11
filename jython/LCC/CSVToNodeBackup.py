# Read a CSV (comma-separated variable) file containing a LCC node backup and convert 
# row-by-row to a LCC backup file.
#
# Author: Bob Jacobsen, copyright 2023
# Part of the JMRI distribution

import jmri

import java
import java.io

import javax
from javax.swing import JFileChooser

import org.apache.commons.csv


fcIn = JFileChooser()
fcIn.setSelectedFile(java.io.File("."))
retIn = fcIn.showOpenDialog(None)

fcOut = JFileChooser()
fcOut.setSelectedFile(java.io.File("."))
retOut = fcOut.showSaveDialog(None)

if retIn == JFileChooser.APPROVE_OPTION and retOut == JFileChooser.APPROVE_OPTION :
    # We've got valid filenames
    
    inFileName = fcIn.getSelectedFile().toString()
    infile = java.io.FileReader(java.io.File(inFileName))
    
    outFileName = fcOut.getSelectedFile().toString()
    outfile = open(outFileName, "w")

    c = org.apache.commons.csv.CSVFormat.DEFAULT.parse(infile)

    for r in c.getRecords() :
        variable = r.get(0)
        value = r.get(1)
        outfile.write(variable+"="+value+"\n")

    outfile.close()

