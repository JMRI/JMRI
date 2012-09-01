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
This module represents the text manager to be used with the translation utility

This module hast four major functionalities:
- Access to the directory manager
- Access to Default texts and files
- Access to the text conversion utility 
  from a human readible representation to UNICODE
- Tests the validity and existance of a version number in a file
   
This modue consists of the public class textmanager and two nested local classes:
- elementitem: Managment class for the confelement class
  - confelement: Representation of the three forms ASCIIstring Normalstring UUCode

These characters are stored in a file called Convertioncharacters.txt and are "=" separated.
"""

import os
from directorymanager import directorymanager
from singlefile import singlefile

class textmanager:
    def __init__(self, dm):
        """
        At startup a directory manager will be handed over
        """
        self.dm = dm
        self.Defaults = []
        self.NonTrans = []
        self.Convertible = False
        self.elements = elementitem()
        self.getconf()
        if os.path.exists(self.dm.Defpath):
            self.addDefaults()
        
    def getstring(self, inputstring):
        """
        Convert input to string
        """
        outputstring = str(inputstring)
        return outputstring
    
    def getversion(self, inputstring):
        """
        Return version string from file
        """
        # Revision $Revision$
        partlist = inputstring.rsplit("$",2)
        #print partlist
        revstring = str(partlist[1]).strip()
        revnum = revstring.rsplit(" ",1)
        outputstring = str(revnum[1])
        #print outputstring
        return outputstring
    
    def isvalidversion(self, inputstring):
        """
        Check validity of the version string
        
        If no version number is detected the return value is -1
        If an old CVS style version number is detected the return value is 0
        """
        # Revision $Revision$
        if float(inputstring) < 0.0:
            return -1
        temp1 = str(inputstring).split(".", 1)
        if len(temp1) > 1:
            return 0
        return 1
      
    def getconf(self):
        """
        Load Configuration files 
        """
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
        """
        Load Defaults files 
        """
        os.chdir(self.dm.Defpath)
        for filelistitem in os.listdir(self.dm.Defpath):
            if str(filelistitem).strip() == str("NonTranslatable.txt").strip():
                cpfile = open(filelistitem,'rU')
                self.NonTrans = singlefile([], [], [], [], [], cpfile.readlines())
                cpfile.close()
                # print ('NonTrans read...')
            else:
                fullfilename = filelistitem
                #print ('Reading default file ' + filelistitem +  ' ...')
                if not filelistitem.strip().startswith("."):
                    corename , ext = os.path.splitext(fullfilename)
                    filepath = self.dm.getdirectorystring(filelistitem)
                    # cpfile = open(filelistitem,'rU',errors='replace')
                    cpfile = open(filelistitem,'rU')
                    temp = singlefile(fullfilename, filepath, [], corename, [], cpfile.readlines())
                    cpfile.close()
                    self.Defaults.append(temp)
                    # print ('Default file ' + filelistitem +  ' read...')
                
    def isDefaults(self, corename, seachstring):
        """
        Check if string is part of (non translatable) default values
        """
        for filelistitem in self.Defaults:
            #print filelistitem.corename
            #print corename
            if filelistitem.corename == corename:
                if filelistitem.isitem(seachstring):
                    return 1
        return 0

    def isNonTrans(self, corename):
        """
        Check if filename belongs to non translatable list
        """
        #print ('Calling function isNonTrans...')
        if not self.NonTrans is []:
            #print ('Searching: ' + corename)
            if self.NonTrans.isitem(corename):
                #print ('Found...')
                return 1
            #print ('Not Found...')
        return 0

            
class elementitem:
    """
    This internal class maintaines the character triples
    """
    def __init__(self):
        """
        Initialize an empty class
        """
        self.numels = 0
        self.elements = []
    
    def addelement(self, string):
        """
        Add a character triple
        """
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
        self.numels = self.numels + 1



class confelement:
    """
    This internal class represents the character triples
    """
    def __init__(self, ASCIIstring, Normalstring, UUCode):
        """
        At startup the triples are initialized
        """
        self.ASCII = ASCIIstring
        self.Normal = Normalstring
        self.UUCode = UUCode
        # print ASCIIstring
        # print Normalstring
        # print UUCode
    
