##
# Identify keys within a bundle properties file that are not being used.
# The JMRI source package is required.
#
# Dave Sand copyright 2017
##

import io
import os
from os.path import join
import glob
from time import time

import jmri
import java
from javax.swing import JFileChooser, JOptionPane
from javax.swing.filechooser import FileNameExtensionFilter

dialogTitle = "Bundle Keys Report"

##
# Select a properties file to be anaylzed.  This will normally be the
# default (English) file.
##
fc = JFileChooser(FileUtil.getProgramPath())
fc.setDialogTitle(dialogTitle)
fc.setFileFilter(FileNameExtensionFilter("Bundle Properties", ["properties"]));
ret = fc.showOpenDialog(None)
if ret == JFileChooser.APPROVE_OPTION:
    selectedBundle = fc.getSelectedFile().toString()
    startTime = time()
else:
    print "No file selected, bye"
    quit()

# set up path info
bundleFile = os.path.basename(selectedBundle)
fullPath = os.path.dirname(selectedBundle)
splitPath = fullPath.split(os.sep + "src" + os.sep)
jmriPath = splitPath[0]
bundlePath = splitPath[1]
dotPath = ".".join(bundlePath.split(os.sep))
testPath = join(jmriPath, "test", bundlePath)

print dialogTitle + "\n"
print "Bundle Property File: '{}'\n".format(bundleFile)

print "Full path: {}".format(fullPath)
print "Test path: {}".format(testPath)
print "JMRI path: {}".format(jmriPath)
print "Bundle path: {}".format(bundlePath)
print "Java format: {}".format(dotPath)

cntUse = 0
cntNot = 0
notUsedList = []
bundleType = "Standard" # Specified in Bundle.java, follow the tree structure

##
# Check Bundle.java
# If the selected file is not the specified bundle for this package,
# display a warning dialog.  Set the bundleType variable to Single
##
try:
    with io.open(join(fullPath, "Bundle.java"), "r", encoding="utf-8") as javaFile:
        javaContent = javaFile.read()
        checkName = dotPath + "." + bundleFile[:-11]
        if javaContent.find(checkName) == -1:
            bundleType = "Single"
            JOptionPane.showMessageDialog(None,
                    "The selected property file is not in the bundle hierarchy.\nA single level search will be performed.",
                    dialogTitle, JOptionPane.INFORMATION_MESSAGE)
except IOError:
    JOptionPane.showMessageDialog(None,
            "Bundle.java does not exist.\nUsing a single level search.",
            dialogTitle, JOptionPane.INFORMATION_MESSAGE)
    bundleType = "Single"

##
# If a class references the current bundle outside of the bundle inheritance tree,
# see if any of the unused keys where used.  If so, they can be removed from
# the unused list.
# chkPath : The full path to the class being checked
##
def checkKeys(chkPath):
    remCnt = 0
    notUsedWork = notUsedList[:]

    with io.open(chkPath, "r", encoding="utf-8") as chkFile:
        chkLine = chkFile.read()
        for cKey in notUsedWork:
            ck = '"{}"'.format(cKey)
            if chkLine.find(ck) != -1:
                try:
                    notUsedList.remove(cKey)
                    print "    {} removed".format(cKey)
                    remCnt += 1
                except ValueError:
                    print "Error removing {} from not list".format(cKey)

    print "  {} keys removed\n".format(remCnt)

##
# Follow the file tree recursively
# Search each java or python source file for occurrences of a key
# key    : The current bundle key or package name
# path   : The starting path for the file recursion
# mode   : Key = quiet, return on match; Reference = Print matches
# return : True as soon as a match is found in Key mode; otherwise False
##
def searchFiles(key, path, mode):
    if mode == "Key":
        k = '"{}"'.format(key)
    else:
        k = key
    for root, dirNames, fileNames in os.walk(path):
        for fileName in fileNames:
            if fileName[-5:] == ".java" or fileName[-3:] == ".py":
                with io.open(join(root, fileName), "r", encoding="utf-8") as reFile:
                    reLine = reFile.read()
                    if reLine.find(k) != -1:
                        if mode == "Key":
                            return True
                        if reLine.find("public class Bundle extends") == -1:
                            print "  {}".format(join(root, fileName))
                            checkKeys(join(root, fileName))
        if bundleType == "Single" and mode == "Key":
            return False
    return False

##
# Phase 1
# For each key in the property file, search the directory tree for
# any usage.  The normal case is that any uses will be at or below the
# directory that contains the property file.
##
print "\n---- Bundle Key Not Used List ----"
with io.open(selectedBundle, "r", encoding="utf-8") as propertyFile:
    for propertyLine in propertyFile:
        splitLine = propertyLine.split("=")
        if len(splitLine) > 1:
            propertyKey = splitLine[0].strip()
            if searchFiles(propertyKey, fullPath, "Key"):
                cntUse += 1
            else:
                # Not used in the src path, try the test path
                if searchFiles(propertyKey, testPath, "Key"):
                    cntUse += 1
                else:
                    cntNot += 1
                    print "  {}".format(propertyKey)
                    notUsedList.append(propertyKey)
print "\nSummary: {} used, {} not used".format(cntUse, cntNot)
midTime = time()

##
# Phase 2
# Find any external references to the property file.  This covers those
# cases where bundle inheritance was not used.
# The proper Bundle.java should always be listed.
##
bundleName = bundleFile.split(".")
bundleArg = ".".join([dotPath, bundleName[0]])
print "\n---- Find Bundle References :: {}".format(bundleArg)
searchFiles(bundleArg, jmriPath, "References")
searchFiles(bundleArg, join(FileUtil.getProgramPath(), "jython"), "References")

print "Final notUsedList size = {}".format(len(notUsedList))
endTime = time()

print "\nTiming"
print ("  Phase 1: {}").format(midTime-startTime)
print ("  Phase 2: {}").format(endTime-midTime)
print ("    Total: {}").format(endTime-startTime)

##
# Provide the option to export the unused key list to an external file.
##
saveResp = JOptionPane.showConfirmDialog(None, "Do you want to save the unused key list?", dialogTitle, JOptionPane.YES_NO_OPTION)
if saveResp == 0:
    fo = JFileChooser(FileUtil.getUserFilesPath())
    fo.setDialogTitle(dialogTitle)
    ret = fo.showSaveDialog(None)
    if ret == JFileChooser.APPROVE_OPTION:
        keyFile = fo.getSelectedFile().toString()
        with io.open(keyFile, "w", encoding="utf-8") as outputFile:
            outputFile.write("\n".join(notUsedList))

##
# Provide the option to update the property files.
#   All of the files for the current bundle name will be updated.
#   A backup will be created for each file before it is updated.  4 copies of the backup will be retained.
#   The unused keys will be converted to comments with the NotUsed tag.
# Note:  If the Java tree is managed by Git, the backups will create new file entries.
#   These backup files should not be included in a commit.
##
updateResp = JOptionPane.showConfirmDialog(None, "Do you want to update the property files?", dialogTitle, JOptionPane.YES_NO_OPTION)
if updateResp == 0:
    globArgument = join(fullPath, bundleFile[:-11] + "*properties")
    print globArgument
    for propName in glob.glob(globArgument):
        print "---- Update {}".format(propName)
        FileUtil.backup(FileUtil.getFile(propName))
        with io.open(propName, "r+", encoding="utf-8") as ioFile:
            propLines = ioFile.readlines()
            ioFile.seek(0)
            ioFile.truncate()
            for propLine in propLines:
                splitLine = propLine.split("=")
                if len(splitLine) > 1:
                    checkKey = splitLine[0].strip()
                    if notUsedList.count(checkKey) > 0:
                        propLine = "#NotUsed " + propLine
                ioFile.write(propLine)

print "\n{} Done".format(dialogTitle)