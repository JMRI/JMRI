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
This Python class represents one key of one single file for translation or one text
"""

class fileitem:
    def __init__(self, linenum, numlines, key, content, isText):
        #print linenum
        #print numlines
        #print content
        self.linenum = linenum
        self.numlines = numlines
        self.key = key
        self.content = content
        self.isText = isText
        self.nextitem = []
        self.previtem = []
                
    def copy(self):
        newitem = fileitem(self.linenum, self.numlines, self.key, self.content, self.isText)
        return newitem
        
    def iskey(self, seachstring):
        if self.isText:
            return 0
        else:
            return (seachstring == self.key)
        
    def isitem(self, seachstring):
        if self.isText:
            return 0
        else:
            if seachstring.strip() == self.getcontent():
                return 1
            else:
                return 0

    def istext(self, seachstring):
        if self.isText:
            if seachstring.strip() == self.content.strip():
                return 1
            else:
                return 0
        else:
            return 0

    def moveposition(self, deltalines):
        self.linenum = self.linenum + deltalines
        
    def setitem(self, newstring, numlines):
        self.content = newstring
        self.numlines = numlines
            
    def getitem(self):
        return self.content
            
    def getlinenum(self):
        return self.linenum
            
    def getnumlines(self):
        return self.numlines
            
    def getcontent(self):
        if self.isText:
            return str(self.content)
        else:
            if self.numlines == 1:
                return str(self.key + " = " + str(self.content) + "\n")
            else:
                tempstr = str(self.key + " = ")
                initstr = ""
                followstr = self.addwhite(len(tempstr))
                for lines in self.content:
                    tempstr = str(tempstr + initstr + lines + "\n")
                    initstr = followstr
                return tempstr
       
    def write(self, filehandle):
        filehandle.write(self.getcontent())

    def addwhite(self, len):
        tempstr = ""
        for i in range(len):
            tempstr = tempstr + " "
        return str(tempstr)
        
