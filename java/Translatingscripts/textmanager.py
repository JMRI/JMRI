# This Python class represents the text manager for the translation structure

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
#import curses.ascii
from directorymanager import directorymanager
from singlefile import singlefile

class textmanager:
    def __init__(self, dm):
        # print "Init Textmanager"
        self.dm = dm
        self.Defaults = []
        self.NonTrans = []
        self.Convertible = False
        #self.Verspat = re.compile(str("$"))
        self.elements = elementitem()
        self.getconf()
        if os.path.exists(self.dm.Defpath):
            self.addDefaults()
        
    def getstring(self, inputstring):
        outputstring = str(inputstring)
        return outputstring
    
    def getversion(self, inputstring):
    # Revision $Revision$
        partlist = inputstring.rsplit("$",2)
        #print partlist
        revstring = str(partlist[1]).strip()
        revnum = revstring.rsplit(" ",1)
        outputstring = str(revnum[1])
        #print outputstring
        return outputstring
    
    def getconf(self):
        # print "GetConf"
        if os.path.exists(self.dm.Progpath):
            os.chdir(self.dm.Progpath)
            temptstr = os.path.join(self.dm.Progpath,"Convertioncharacters.txt")
            # print temptstr
            if os.path.exists(os.path.join(self.dm.Progpath,"Convertioncharacters.txt")):
                cofile = open("Convertioncharacters.txt","rU")
                content = cofile.readlines()
                for currline in content:
                    if not currline[0] == "#":
                        self.elements.addelement(currline)
                cofile.close()
                self.Convertible = True
            else:
                print ('File not fond: Convertioncharacters.txt')

    def addDefaults(self):
        os.chdir(self.dm.Defpath)
        for filelistitem in os.listdir(self.dm.Defpath):
            if str(filelistitem).strip() == str("NonTranslatable.txt").strip():
                cpfile = open(filelistitem,'rU')
                self.NonTrans = singlefile([], [], [], [], [], cpfile.readlines())
                cpfile.close()
                # print ('NonTrans read...')
            else:
                fullfilename = filelistitem
                corename , ext = os.path.splitext(fullfilename)
                filepath = self.dm.getdirectorystring(filelistitem)
                # cpfile = open(filelistitem,'rU',errors='replace')
                cpfile = open(filelistitem,'rU')
                temp = singlefile(fullfilename, filepath, [], corename, [], cpfile.readlines())
                cpfile.close()
                self.Defaults.append(temp)
                # print ('Default file ' + filelistitem +  ' read...')
                
    def isDefaults(self, corename, seachstring):
        for filelistitem in self.Defaults:
            #print filelistitem.corename
            #print corename
            if filelistitem.corename == corename:
                if filelistitem.isitem(seachstring):
                    return 1
        return 0

    def isNonTrans(self, corename):
        #print ('Calling function isNonTrans...')
        if not self.NonTrans is []:
            print ('Searching: ' + corename)
            if self.NonTrans.isitem(corename):
                #print ('Found...')
                return 1
            #print ('Not Found...')
        return 0

            
class elementitem:
    def __init__(self):
        self.numels = 0
        self.elements = []
    
    def addelement(self, string):
        temp = string.split("=")
        if len(temp) == 2:
            ASCIItemp = temp[0].strip()
            Normaltemp = ASCIItemp
            UUCtemp = temp[1].strip()
        elif len(temp) == 3:
            ASCIItemp = temp[0].strip()
            Normaltemp = temp[1].strip()
            UUCtemp = temp[2].strip()
        tempel = confelement(ASCIItemp,Normaltemp,UUCtemp)
        self.elements.append(tempel)



class confelement:
    def __init__(self, ASCIIstring, Normalstring, UUCode):
        self.ASCII = ASCIIstring
        self.Normal = Normalstring
        self.UUCode = UUCode
        # print ASCIIstring
        # print Normalstring
        # print UUCode
    
