# This Python class represents one set of translation files for translation containing several keys

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

# Revision $Revision$
# by Simon Ginsburg (simon.ginsburg at bluewin.ch)

import sys
import os
import re
import curses.ascii
from Property_File_Error import Property_File_Error
from singlefilestructure import singlefilestructure
from textmanager import textmanager

class translatefilestructure:
    def __init__(self, tm, corename, original):
        self.tm = tm
        self.corename = corename
        self.original = original
        self.translations = []
        #self.export = []
        
    def add(self, translation):
        self.translations.append(translation)

    def exchange(self, newfile):
        exkey = newfile.key
        oldfile = self.get(exkey)
        if not oldfile == []:
            self.translations.remove(oldfile)
        self.translations.append(newfile)
    
    def exist(self, key):
        if len(self.translations) is 0:
            return 0
        else:
            if key is "All":
                return 1
            else:
                for actfile in self.translations:
                    #print str(actfile.key).strip()
                    if str(key).strip() == str(actfile.key).strip():
                        return 1
                return 0
 
    def getKinds(self):
        retstruct = []
        for actfile in self.translations:
            #print actfile.fullfilename
            retstruct.append(actfile.key)
        return retstruct
        
    def export(self, key):
        #print "Export Function Call"
        origfile = open(self.original.fullfilename,'w')
        self.original.write(origfile)
        origfile.close()
        #print str("Group " +  self.original.corename + " contains " + str(len(self.translations)) + " Members")
        if key.strip() == "All":
            #print "Key is All"
            for actfile in self.translations:
                transfile = open(actfile.fullfilename,'w')
                actfile.write(transfile)
                transfile.close()
        else:
            transfilehandle = self.get(key)
            if not transfilehandle == []:
                #print str("Key is " + key)
                transfile = open(transfilehandle.fullfilename,'w')
                transfilehandle.write(transfile)
                #transfile.writelines(transfilehandle.content)
                transfile.close()

    def get(self, key):
        #print ('Calling function get...')
        if len(self.translations) is 0:
            #print ('No Entry! Return...')
            return []
        else:
            #print ('Start search for '+ key + ' ...')
            for actfile in self.translations:
                #print ('Found ' + str(actfile.key).strip())
                if str(key).strip() == str(actfile.key).strip():
                    return actfile
            return []

    def finditem(self, key, string):
        returnstring = ""
        #print string
        if key is "":
            self.original.getitem(string)
        else:
            if len(self.translations) is 0:
                #print "No translation!"
                return ""
            else:
                for actfile in self.translations:
                    #print str(actfile.key).strip() + " " + str(key).strip()
                    if str(key).strip() == str(actfile.key).strip():
                        return actfile.getitem(string)
                        
