#!/usr/bin/pythonw 
# This file is part of JMRI.
#
# JMRI is free software; you can redistribute it and/or modify it under 
# the terms of version 2 of the GNU General Public License as published 
# by the Free Software Foundation. See the "COPYING" file for a copy
# of this license.
#
# JMRI is distributed in the hope that it will be useful, but WITHOUT 
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
# for more details.
#
# Revision $Revision$
# by Simon Ginsburg (simon.ginsburg at bluewin.ch)
"""
This Python class represents the content of one single file for translation
It contains all keys individually and also all commentary text lines
It technically extends and replaces the earlier class "singlefile".
"""

from fileitem import fileitem
from Property_File_Error import Property_File_Error
from textmanager import textmanager

class singlefilestructure:
    def __init__(self, tm, fullfilename, path, filename, corename, key, content):
        """
        At initialisation the class will be filled basic naming information as well as the file content.
        """
        self.fullfilename = fullfilename
        self.path = path
        self.filename = filename
        self.corename = corename
        self.key = key
        self.version = []
        self.numitems = 0
        self.numkeys = 0
        self.numMissing = 0
        self.numNontrans = 0
        self.numObsolete = 0
        self.list = []
        self.firstel = []
        self.tm = tm
        self.report = []
        #self.Debugkey = 0
        #if self.corename == "jmri-web-server-Html":
        #    self.Debugkey = 1
        if not (content == []):
            self.createstructure(content)
        if self.version == []:
            self.version = -1
       
    def createstructure(self, content):
        """
        This is the helper function to fill the file content into the class at initialisation time
        """
        lineidx = 0
        tempstr = []
        tempkey = []
        numlines = 1
        tempprevel = []
        #print self.filename
        for lines in content:
            lineidx = lineidx + 1
            if not (len(lines.strip()) < 1):
            # Exclude empty lines
                if lines[0] is "#" or (lines[0] is "/" and lines[1] is "/"):
                    tempitem = fileitem(lineidx,1,'',lines,1)
                    self.list.append(tempitem)
                    self.numitems = self.numitems + 1
                    if self.version == []:
                        if lines.strip().endswith("$"):
                            self.version = self.tm.getversion(lines)
                    #if self.Debugkey:
                    #    print str(str(lineidx) + ": " + "Textstring: " + lines)
                else:
                    #errorflag = 0
                    if tempstr == []:
                        temp = lines.split("=",1)
                        if len(temp) == 1:
                            raise Property_File_Error(self.fullfilename, str(lineidx),  temp[0])
                        else:
                            tempkey = temp[0].strip()
                            tempstr.append(temp[1].strip())
                    else:
                        tempstr.append(str(lines.strip()))
                        numlines = numlines + 1
                        #if self.Debugkey:
                        #    print str(str(lineidx) + ": " + "Addon line " + str(numlines) + ": " + lines)
                    if not (lines.strip().endswith("\\")):
                        if numlines is 1:
                            tempitem = fileitem(lineidx,1,tempkey,tempstr[0],0)
                            #if self.Debugkey:
                            #    print str(str(lineidx) + ": " + "Singleline Key: " + tempkey)
                        else:
                            tempitem = fileitem(lineidx + 1 - numlines,numlines,tempkey,tempstr,0)
                            #if self.Debugkey:
                            #    print str(str(lineidx) + ": " + "Multiline Key: " + tempkey + str(numlines) + " lines")
                        self.list.append(tempitem)
                        self.numitems = self.numitems + 1
                        self.numkeys = self.numkeys + 1
                        tempstr = []
                        tempkey = []
                        numlines = 1
                if not tempprevel == []:
                    tempprevel.nextitem = tempitem
                    tempitem.previtem = tempprevel
                    tempprevel = tempitem
                else:
                    tempprevel = tempitem
                    self.firstel = tempitem
        if numlines > 1:
            raise Property_File_Error(self.fullfilename, str(lineidx + 1 - numlines),  tempkey)
        #if self.Debugkey:
        #    print str(str(lineidx) + ": " + str(numlines) + " Key: " + tempkey)
        #if not self.tm.isvalidversion(self.version):
        #    print "Found Version string found in:"
        #    print self.fullfilename
        #    print self.version
            
    def copystructure(self, firstel):
        self.firstel = firstel
        curritem = firstel
        tempprevel = []
        while not (curritem == []):
            newitem = curritem.copy()
            newitem.previtem = tempprevel
            if not (tempprevel == []):
                tempprevel.nextitem = newitem
            tempprevel = newitem
            curritem = curritem.nextitem
        
    def copy(self):
        newstruct = singlefilestructure(self.tm, self.fullfilename, self.path, self.filename, self.corename, self.key, [])
        newstruct.copystructure(self.firstel)
        newstruct.version = self.version
        newstruct.numitems = self.numitems
        newstruct.numkeys = self.numkeys
        return newstruct
                
    def iskey(self, seachstring):
        return (seachstring == self.key)
        
    def isname(self, seachstring):
        return (seachstring.strip() == self.corename.strip())

    def isitem(self, seachstring):
        curritem = self.firstel
        while not (curritem == []):
            if curritem.iskey(seachstring):
                return 1
            curritem = curritem.nextitem
        return 0

    def getitem(self, seachstring):
        curritem = self.firstel
        while not (curritem == []):
            if curritem.iskey(seachstring):
                return  curritem.getitem()
            curritem = curritem.nextitem
        return []

    def setitem(self, seachstring, content, numlines):
        curritem = self.firstel
        while not (curritem == []):
            if curritem.iskey(seachstring):
                 curritem.setitem(content, numlines)
            curritem = curritem.nextitem

    def creatitem(self, linenum, numlines, key, content, istext):
        # Create new item
        newitem = fileitem(linenum,numlines,key,content,istext)
        # Insert as new first element?
        if (self.firstel.getlinenum >= linenum + numlines):
            newitem.nextitem = self.firstel
            self.firstel = newitem
        else:
            # Insert in between
            lastitem = []
            curritem = self.firstel
            found = 0
            while not (curritem == [] or found):
                # Check if ne item needs to be inserted before current item
                if (curritem.getlinenum >= linenum):
                    newitem.nextitem = curritem
                    newitem.previtem = curritem.previtem
                    curritem.previtem.nextitem = newitem
                    curritem.previtem = newitem
                    found = 1
                # Append
                if (curritem.nextitem == []):
                    curritem.nextitem = newitem
                    newitem.previtem = curritem
                curritem = curritem.nextitem                    
        # Check if lines need to be shifted
        curritem = self.firstel
        lastline = 0
        while not (curritem.nextitem == []):
            if curritem.getlinenum <= lastline:
                curritem.moveposition(self, lastline - curritem.getlinenum + 1)
            lastline = curritem.getlinenum + curritem.numlines
            curritem = curritem.nextitem                
        
    def addreport(self, report):
        if self.report == []:
            self.report = report
        else:
            if self.report.endswith("\n"): 
                self.report = str(self.report + report)
            else:
                self.report = str(self.report + "\n" + report)

    def getreport(self):
        return self.report

    def write(self, filehandle):
        intcnt = 1
        elidx = 0
        curritem = self.firstel
        while not (curritem == []):
            while intcnt < curritem.linenum:
                filehandle.write(str("\n"))
                # print "Add newline"
                intcnt = intcnt + 1
            curritem.write(filehandle)
            intcnt = intcnt + curritem.numlines
            curritem = curritem.nextitem
            
    def compare(self, tm, origfile):
        self.report = []
        self.numMissing = 0
        self.numNontrans = 0
        self.numObsolete = 0
        found = 0
        self.addreport(str("Items in original File: " + str(origfile.numkeys)))
        self.addreport(str("Revision of original File: " + str(origfile.version)))
        self.addreport(str("Items in translated File: " + str(self.numkeys)))
        self.addreport(str("Revision of translated File: " + str(self.version)))
        currel = origfile.firstel
        while not currel == []:
            if not currel.isText:
                tranel = self.getitem(currel.key)
                if tranel == []:
                    if found == 0:
                        self.addreport("\nMissing Items:")
                        found = 1
                    self.addreport(str(str(currel.linenum).zfill(3) + ": " + str(currel.getcontent())))
                    self.numMissing += 1
            currel = currel.nextitem
        found = 0
        currel = origfile.firstel
        while not currel == []:
            if not currel.isText:
                tranel = self.getitem(currel.key)
                if not tranel == []:
                    if str(tranel).strip() == str(currel.getitem()).strip():
                        if not tm.isDefaults(self.corename, currel.key):
                            if found == 0:
                                self.addreport("\nNon translated Items:")
                                found = 1
                            self.addreport(str(str(currel.linenum).zfill(3) + ": " + str(currel.getcontent())))
                            #print str(str(currel.linenum).zfill(3) + ": " + str(currel.getcontent()))
                            self.numNontrans += 1
            currel = currel.nextitem
        found = 0
        currel = self.firstel
        while not currel == []:
            if not currel.isText:
                tranel = origfile.getitem(currel.key)
                if tranel == []:
                    if found == 0:
                        self.addreport("\nObsolete Items:")
                        found = 1
                    self.addreport(str(str(currel.linenum).zfill(3) + ": " + str(currel.getcontent())))
                    self.numObsolete += 1
            currel = currel.nextitem
        return self.numMissing, self.numNontrans, self.numObsolete
        
    def isdifferent(self, tm, reffile):
        currel = reffile.firstel
        while not currel == []:
            if not currel.isText:
                tranel = self.getitem(currel.key)
                if tranel == []:
                    #print(str("Returnvalue due to empty Test with " + currel.key))
                    return 1
                else:
                    if not str(tranel).strip() == str(currel.getitem()).strip():
                        #print(str("Returnvalue due to Test with " + str(tranel).strip() + " and " + str(currel.getitem()).strip()))
                        return 1
            currel = currel.nextitem
        currel = self.firstel
        while not currel == []:
            if not currel.isText:
                tranel = reffile.getitem(currel.key)
                if tranel == []:
                    #print(str("Returnvalue due to empty Test with " + currel.key))
                    return 1
            currel = currel.nextitem
        return 0
        
    def comparefull(self, tm, origfile, reforigfile, reftransfile):
        self.numMissing = 0
        self.numNontrans = 0
        self.numObsolete = 0
        found = 0
        self.addreport(str("Items in current original File: " + str(origfile.numkeys)))
        self.addreport(str("Revision of original File: " + str(origfile.version)))
        self.addreport(str("Items in current translated File: " + str(self.numkeys)))
        self.addreport(str("Revision of translated File: " + str(self.version)))
        self.addreport(str("Items in reference original File: " + str(reforigfile.numkeys)))
        self.addreport(str("Revision of original File: " + str(reforigfile.version)))
        self.addreport(str("Items in reference translated File: " + str(reftransfile.numkeys)))
        self.addreport(str("Revision of translated File: " + str(reftransfile.version)))
        currel = origfile.firstel
        while not currel == []:
            if not currel.isText:
                tranel = self.getitem(currel.key)
                if tranel == []:
                    if not tm.isDefaults(self.corename, currel.key):
                        if found == 0:
                            self.addreport("\nMissing Items:")
                            found = 1
                        self.addreport(str(str(currel.linenum).zfill(3) + ": " + str(currel.getcontent())))
                        self.numMissing += 1
            currel = currel.nextitem
        found = 0
        currel = origfile.firstel
        while not currel == []:
            if not currel.isText:
                tranel = self.getitem(currel.key)
                if not tranel == []:
                    if str(tranel).strip() == str(currel.getitem()).strip():
                        if not tm.isDefaults(self.corename, currel.key):
                            if found == 0:
                                self.addreport("\nNon translated Items:")
                                found = 1
                            self.addreport(str(str(currel.linenum).zfill(3) + ": " + str(currel.getcontent())))
                            self.numNontrans += 1
            currel = currel.nextitem
        found = 0
        currel = self.firstel
        while not currel == []:
            if not currel.isText:
                tranel = origfile.getitem(currel.key)
                if tranel == []:
                    if found == 0:
                        self.addreport("\nObsolete Items:")
                        found = 1
                    self.addreport(str(str(currel.linenum).zfill(3) + ": " + str(currel.getcontent())))
                    self.numObsolete += 1
            currel = currel.nextitem
        found = 0
        currel = origfile.firstel
        while not currel == []:
            if not currel.isText:
                tranel = reforigfile.getitem(currel.key)
                if tranel == []:
                    if found == 0:
                        self.addreport("\nAdded Reference Items:")
                        found = 1
                    self.addreport(str(str(currel.linenum).zfill(3) + ": " + str(currel.getcontent())))
            currel = currel.nextitem
        found = 0
        currel = self.firstel
        while not currel == []:
            if not currel.isText:
                tranel = reftransfile.getitem(currel.key)
                if tranel == []:
                    if found == 0:
                        self.addreport("\nAdded Translation:")
                        found = 1
                    self.addreport(str(str(currel.linenum).zfill(3) + ": " + str(currel.getcontent())))
            currel = currel.nextitem
        return self.numMissing, self.numNontrans, self.numObsolete
        
        
        
        
