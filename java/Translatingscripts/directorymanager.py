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
This module represents the directory manager to be used with the translation utility

This module hast three major functionalities:
- Storage of all project relevant directory path's 
- Internal representation of a tree like 
  directory structure into a linear form
- Transforming path names from the tree structure 
  into the linear representation and vice versa
"""

import os
import re
from datetime import date
from datetime import datetime

class directorymanager:

    def __init__(self):
        """
        Initialize all required directories based on the current path
        
        At initialisation time the following path names will be defined:
        - Progpath: Where the program is stored
        - Abspath: The "java" subdirectory of the JMRI project
        - Startpath: The "src" subdirectory of the JMRI project
        - Destpath: The "translation" subdirectory where all internal files 
                    of the translation project are stored
        - Defpath: The "Defaults" subdirectory where all defaults are stored
        - Refdir: The "Ref" subdirectory where the reference data is stored
        - Currdir: The "Curr" subdirectory where all current data is stored
        - Testdir: The "Test" subdirectory where all test files are stored
        - Importdir: The "Import" subdirectory where translated files are stored
        
        Also a list of regular expression patterns are defined.

        """
        self.Progpath = os.getcwd()
        ##self.Progpath = os.path.normpath(os.path.join(os.getcwd(),"Def"))
        self.Abspath = os.path.normpath(os.path.join(os.getcwd(),os.pardir))
        self.Startpath = os.path.join(self.Abspath,"src")
        #Startstring = str(self.Startpath)
        self.Lineseparator = str(os.linesep)
        self.Startpat = re.compile(str(self.Startpath))
        self.Slashpat = re.compile(os.path.sep)
        self.Dashpat = re.compile("-")
        self.Dotpat = re.compile(".")
        self.Linendpat = re.compile("\\\\")
        self.Startdotpat = re.compile("^.")
        self.delimiter = ";"
        self.currlist = ""
        self.Destpath = os.path.join(self.Abspath,"translation")
        if not os.path.exists(self.Destpath):
            os.mkdir(self.Destpath)
        self.Defpath = os.path.join(self.Destpath,"Defaults")
        self.Currdir = os.path.join(self.Destpath, "Curr")
        self.Refdir = os.path.join(self.Destpath, "Ref")
        self.Testdir = os.path.join(self.Destpath, "Test")
        self.Importdir = os.path.join(self.Destpath, "Import")
    
    def getfullname(self, dirname, trunkname):
        """
        Return full file name combining the directory path and the filename
        """
        dirstr = re.sub(self.Dashpat, "", re.sub(self.Slashpat, "-", re.sub(self.Startpat, "", str(dirname))),1)
        fullfilename = dirstr + "-" + trunkname + ".txt"
        #print fullfilename
        return fullfilename

    def getinfo(self, fullname):
        """
        Return full directory information
        
        Returns several information based on the fullfilename:
        - filename
        - directory path
        - corename
        - trunkname
        - key
        """
        temp1 = str(fullname).split(".", 1)
        temp2 = str(temp1[0]).split("_", 1)
        dirstr = re.sub(self.Dashpat, str(os.path.sep), fullname)
        dirname  = os.path.dirname((self.Startpath + str(os.path.sep) + dirstr))
        corename = temp2[0]
        if len(temp2) > 1:
            key = temp2[1]
        else:
            key = ""        
        temp3 = str(temp1[0]).rsplit("-", 1)
        trunkname = temp3[0]
        if len(temp3) > 1:
            trunkname = temp3[1]
            filename = temp3[1] + ".properties"
        else:
            trunkname =""
            filename =""
        return filename, dirname, corename, trunkname, key
        
    def getfilename(self, fullname):
        """
        Return appropriate filename
        """
        #print fullname
        tempname = fullname.rsplit("-",1)
        #print tempname
        if len(tempname) > 1:
            filename = tempname[1] + ".properties"
        else:
            filename =""
        return filename
        
    def gettoday(self):
        """
        Return String containing today's date
        """
        return str(date.today())

    def gotodaydir(self):
        """
        Return directory path containing today's date
        """
        todaystr = str("Status_" + str(date.today()))
        self.Todaypath = os.path.join(self.Destpath,todaystr)
        if not os.path.exists(self.Todaypath):
            os.mkdir(self.Todaypath)
        os.chdir(self.Todaypath)
        
    def getnext(self,checkname):
        """
        Return fullname, append numbering if required to find a free one
        """
        #path = os.getcwd()
        filename = str(checkname + ".txt")
        fullname = os.path.join(os.getcwd(), filename)
        if os.path.exists(fullname):
            idx = 1
            fullname = os.path.join(os.getcwd(),str(checkname + "_" + str(idx)) + ".txt")
            while os.path.exists(fullname):
                idx = idx + 1
                fullname = os.path.join(os.getcwd(),str(checkname + "_" + str(idx)) + ".txt")
        return fullname
        
    def gosubdir(self,subdirname, overwrite = 1):
        """
        Navigate to the indicated subdirectory
        """
        fullpath = os.path.join(os.getcwd(),subdirname)
        if overwrite:
            os.mkdir(fullpath)
        else:
            if os.path.exists(fullpath):
                idx = 1
                fullpath = os.path.join(os.getcwd(),str(subdirname + "_" + str(idx)))
                while os.path.exists(fullpath):
                    idx = idx + 1
                    fullpath = os.path.join(os.getcwd(),str(subdirname + "_" + str(idx)))
            os.mkdir(fullpath)
        os.chdir(fullpath)
        
    def getnow(self):
        """
        Return today's date
        """
        return str(datetime.now())

    def getcorename(self, fullname):
        """
        Extract corename from fullname
        """
        temp1 = str(fullname).split(".", 1)
        temp2 = str(temp1[0]).split("_", 1)
        corename = temp2[0]
        return corename
        
    def gettrunkname(self, fullname):
        """
        Extract trunkname from fullname
        """
        temp1 = str(fullname).split(".", 1)
        temp2 = str(temp1[0]).split("_", 1)
        temp3 = str(temp2[0]).rsplit("-", 1)
        if len(temp3) > 1:
            trunkname = temp3[1]
        else:
            trunkname =""
        return trunkname
        
    def getkey(self, fullname):
        """
        Extract the language key from fullname
        """
        temp1 = str(fullname).split(".", 1)
        temp2 = str(temp1[0]).split("_", 1)
        corename = temp2[0]
        if len(temp2) > 1:
            key = temp2[1]
        else:
            key = ""
        return key

    def getdirectorystring(self, fullname):
        """
        Reconstruct the directory string from the fullname
        """
        dirstr = re.sub(self.Dashpat, str(os.path.sep), fullname)
        directorystring  = os.path.dirname((self.Startpath + str(os.path.sep) + dirstr))
        return directorystring

if __name__ == "__main__":
    import doctest
    doctest.testmod()
        
