##
# Look for non-standard getString and getMessage key arguments.
# These will normally be variables.
# Other exceptions:
#   If a key contains special characters, it will be flagged as a variable.
#     The valid characters are a-z, A-Z, 0-9 and underscore.
#
# The JMRI source package is required.
#
# Dave Sand copyright 2017
##

import io
import re
import os
from os.path import join
import glob

import jmri
import java
import com.csvreader
from javax.swing import JFileChooser, JOptionPane
from javax.swing.filechooser import FileNameExtensionFilter

dialogTitle = "Class Keys Report"
print "   {}".format(dialogTitle)
keyList = []

# Select a Java program or package directory to be analyzed.
fc = JFileChooser(FileUtil.getProgramPath())
fc.setDialogTitle(dialogTitle)
fc.setFileFilter(FileNameExtensionFilter("Java Program", ["java"]));
fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES)
ret = fc.showOpenDialog(None)
if ret == JFileChooser.APPROVE_OPTION:
    selectedItem = fc.getSelectedFile().toString()
else:
    print 'No file selected, bye'
    quit()

# RegEx patterns.  Capture the first word after the start of the match string.
# The first one looks for a word within double quotes.
# The second one looks for a plain word.
# A word contains a-z, A-Z, 0-9 or underscore characters.
reStrKey = re.compile('\W*"(\w+)"\W*[,)]')
reVarKey = re.compile('\W*(\w+)\W')

##
# Determine class for a variable key.
# The first getMessage parameter can be a Locale object.
# variableKey : The current key 
# filePath    : The current file being processed
# return      : The class type: String, Locale or Unknown
##
def findVariableClass(variableKey, filePath):
    with io.open(filePath, "r", encoding="utf-8") as chkFile:
        for chkLine in chkFile:
            for classType in ['String', 'Locale']:
                chkClassArg = '.*{}\s+{}'.format(classType, variableKey)
                reChkClass = re.compile(chkClassArg)
                chkMatch = re.match(chkClassArg, chkLine)
                if chkMatch is not None:
                    return classType
        return 'Unknown'

##
# Search for the next word in the line fragment.
# lineFragment : The line fragment to be searched.
# return       : A tuple that has the key and key type
##
def findKey(lineFragment):
    returnValues = 'None', 'Unknown'
    chkStr = re.match(reStrKey, lineFragment)
    if chkStr is not None:
        returnValues = chkStr.group(1), 'String'
    else:
        chkVar = re.match(reVarKey, lineFragment)
        if chkVar is not None:
            returnValues = chkVar.group(1), 'Variable'
    return returnValues

##
# Search a line of code for occruances of getString or getMessage.
# Display a message for non-standard key references.
# All keys are added to a list that can be exported as a CSV file.
# classLine : The current line of code
# num       : The current line number
# filePath  : The current file path
##
def findKeys(classLine, num, filePath):
    for searchText in ['Bundle.getMessage(', 'getString(']:
        searchType = 'Local' if searchText == 'getString(' else 'Bundle'
        searchSize = len(searchText)
        idx = 0
        while idx >= 0 and idx < len(classLine):
            idx = classLine.find(searchText, idx)
            if idx == -1:
                break
            idx += searchSize
            key, keyType = findKey(classLine[idx:])
            if keyType == 'Variable':
                if findVariableClass(key, filePath) == 'Locale':
                    key, keyType = findKey(classLine[idx + len(key):])
            if keyType != 'String':
                print "   {}, Search Type = {}, Key Type = {}, Key = '{}', \tText = {}".format(num, searchType, keyType, key, classLine.strip())
            keyList.append([num, searchType, keyType, key, classLine.strip()])

##
# Read each line of the supplied file and call the parser
# filePath : The full path for the file
##
def doFile(filePath):
    keyList.append([filePath])
    with io.open(filePath, "r", encoding="utf-8") as classFile:
        num = 0
        for classLine in classFile:
            num += 1
            findKeys(classLine, num, filePath)

# Process a single file selection
if os.path.isfile(selectedItem):
    print '\n Check {}'.format(selectedItem)
    doFile(selectedItem)

# Process package directory selection
if os.path.isdir(selectedItem):
    globArgument = join(selectedItem, "*java")
    for className in glob.glob(globArgument):
        if os.path.basename(className) == 'Bundle.java':
            continue
        print '\n Check {}'.format(className)
        doFile(className)
##
# Create the CSV header record
# csvFile : The output file
##
def writeHeader(csvFile):
    csvFile.write("Line #")
    csvFile.write("Search Type")
    csvFile.write("Key Type")
    csvFile.write("Key")
    csvFile.write("Line Text / Class File")
    csvFile.endRecord()

##
# Create the CSV detail record
# The file path and name go into column 5
# csvFile : The output file
##
def writeDetails(csvFile):
    for keyRow in keyList:
        if len(keyRow) == 1:
            csvFile.write("")
            csvFile.write("")
            csvFile.write("")
            csvFile.write("")
            csvFile.write(keyRow[0])
        else:
            csvFile.write('{}'.format(keyRow[0]))
            csvFile.write(keyRow[1])
            csvFile.write(keyRow[2])
            csvFile.write(keyRow[3])
            csvFile.write(keyRow[4])
        csvFile.endRecord()

# Provide the option to export the full class key list to an external file.
saveResp = JOptionPane.showConfirmDialog(None, "Do you want to export the full class key list to a CSV file?", dialogTitle, JOptionPane.YES_NO_OPTION)
if saveResp == 0:
    fo = JFileChooser(FileUtil.getUserFilesPath())
    fo.setDialogTitle(dialogTitle)
    fo.setFileFilter(FileNameExtensionFilter("CSV", ["csv"]));
    ret = fo.showSaveDialog(None)
    if ret == JFileChooser.APPROVE_OPTION:
        keyFile = fo.getSelectedFile().toString()
        csvFile = com.csvreader.CsvWriter(java.io.BufferedOutputStream(java.io.FileOutputStream(keyFile)),',',java.nio.charset.Charset.defaultCharset())
        writeHeader(csvFile)
        writeDetails(csvFile)
        csvFile.flush()
        csvFile.close()
        JOptionPane.showMessageDialog(None,"Class keys export completed",dialogTitle,JOptionPane.INFORMATION_MESSAGE)

print '\n   {} Done'.format(dialogTitle)

