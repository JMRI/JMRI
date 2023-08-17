# This parses a backup file from a RR-CirKits Tower LCC to make a CSV file of selected contents
# Bob Jacobsen  Copyright 2023

import java


fcIn = JFileChooser()
fcIn.setSelectedFile(java.io.File("."))
retIn = fcIn.showOpenDialog(None)

fcOut = JFileChooser()
fcOut.setSelectedFile(java.io.File("."))
retOut = fcOut.showSaveDialog(None)

if retIn == JFileChooser.APPROVE_OPTION and retOut == JFileChooser.APPROVE_OPTION :
    # We've got valid filenames

    inFileName = fcIn.getSelectedFile().toString()
    outFileName = fcOut.getSelectedFile().toString()

    outfile = open(outFileName,"w")

    with open(inFileName) as infile:
      for item in infile:

        # locate the front and back parts
        index = item.index("=")
        front = item[:index]
        back = item[index+1:-1] # skip NL at end of line
    
        outfile.write("\""+front+"\",\""+back+"\"\n")

    outfile.close()

