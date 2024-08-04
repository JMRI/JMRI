# ExportLogixNG.py -- Export LogixNG definitions

# The details are at https://jmri.org/help/en/html/tools/logixng/ExportLogixNG.shtml



# The LogixNG content in the PanelPro xml data file looks simple but it is internally complex.

# The default is to use automatic system names.  The format is xxxx:AUTO:nnnn.  The xxxx is one of
# a couple dozen values.  The nnnn is a sequential number.  Since the system names are only unique
# within a xml data file, copying LogixNG content to another xml data file is impossible.

# The export process changes the system names to be unique.  The AUTO keyword is replaced by a user
# supplied keyword. For example using XYZ, the result is that xxxx:AUTO:nnnn becomes xxxx$XYZ:nnnn.
# Notice that the first colon becomes a dollar character.

# The script has three prompts when started.

# 1 -- The input xml data file.  If you want to export all of the LogixNG definitions, a current file
# can be used.  If only some of the LogixNG definitions are to be exported, then some pre-processing
# is required.  Except for the LogixNGs and/or Modules to be exported, the other LogixNGs, Modules,
# Tables and Global Variables need to be deleted.  Also, the LogixNG clipboard should be emptied.
# After removing the other items, store the file with a new name.  Select this file as the export
# input file.

# 2 -- The output xml data file.  This file will be created or over written with no warnings. This
# is a PanelPro data file with the standard header and the LogixNG definitions.  There are no other
# tables or panels. This file can be loaded after the normal data file.  This results in adding the
# exported LogixNG definitions. This is a one time activity.

# 3 -- The alternate system name keyword.

# # # Warnings # # #

# The references in LogixNG definitions to other tables, such as sensors, turnouts, etc., are not
# changed. These will create errors if there are no matching entries in the normal tables.

# Any messages from the script will be on the JMRI system console.

# Author: Dave Sand, copyright 2024
# Part of the JMRI distribution.

import java
import jmri

from javax.swing import JFileChooser, JOptionPane
from javax.swing.filechooser import FileNameExtensionFilter

from org.slf4j import LoggerFactory
log = LoggerFactory.getLogger("jmri.jmrit.jython.exec.ExportLogixNG")

log.info('Run LogixNG Export v1.0')

inputFileName = None
outputFileName = None
newKeyword = None

header = True
logixNGs = False
lngItems = False
ready = True

lines = []

# Create a file chooser.
fc = JFileChooser(jmri.util.FileUtil.getUserFilesPath())
fc.setFileFilter(FileNameExtensionFilter("PanelPro XML Data File", ["xml"]));
fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES)

# Select a XML file to be exported.
fc.setDialogTitle('Select Input XML Data File')
ret = fc.showOpenDialog(None)
if ret == JFileChooser.APPROVE_OPTION:
    inputFileName = fc.getSelectedFile().toString()
if ret is None: ready = False

# Select a XML file to be created/replaced.
fc.setDialogTitle('Select Output XML Data File')
ret = fc.showSaveDialog(None)
if ret == JFileChooser.APPROVE_OPTION:
    outputFileName = fc.getSelectedFile().toString()
if ret is None: ready = False

# Select the keyword to replace AUTO
newKeyword = JOptionPane.showInputDialog(
        None,
        'Enter the replacement keyword for AUTO',
        '',
        JOptionPane.PLAIN_MESSAGE);
if not newKeyword.isalnum():
    log.error('"{}" is an invalid keyword'.format(newKeyword))
    ready = False

if ready:
    # Process each input line and add the retained lines to the 'lines' list.
    with open(inputFileName) as infile:
        for line in infile:
            if header:
                lines.append(line)
                if '</jmriversion>' in line:
                    header = False

            # Handle LogixNGs list
            if not logixNGs:
                if '<LogixNGs' in line:
                    logixNGs = True

            if logixNGs:
                lines.append(line)
                if '</LogixNGs>' in line:
                    logixNGs = False
                continue

            # Handle remaining lines
            if not lngItems:
                if '<LogixNG' in line:
                    lngItems = True

            if lngItems:
                lines.append(line)
                if '</LogixNG' in line:
                    lngItems = False

    # Write each line from the "lines" list to the output file, finish with the final xml line.
    with open(outputFileName, 'w') as outfile:
        for line in lines:
            outfile.write(line.replace(':AUTO:', '$' + newKeyword + ':'))
        outfile.write('</layout-config>\n')

    log.info('Export completed, AUTO replaced by {}, file {} has been created'.format(newKeyword, outputFileName))
else:
    log.warn('Export aborted due to incomplete input')

