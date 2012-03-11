# This Python class represents the directory manager for the translation structure

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
from datetime import date
from datetime import datetime
#import curses.ascii

class directorymanager:
    def __init__(self):
        self.Progpath = os.getcwd()
        self.Progpath = os.path.normpath(os.path.join(os.getcwd(),'Def'))
        self.Abspath = os.path.normpath(os.path.join(os.getcwd(),os.pardir))
        self.Startpath = os.path.join(self.Abspath,'src')
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
        self.Destpath = os.path.join(self.Abspath,'translation')
        if not os.path.exists(self.Destpath):
            os.mkdir(self.Destpath)
        self.Defpath = os.path.join(self.Destpath,'Defaults')
        self.Currdir = os.path.join(self.Destpath, "Curr")
        self.Refdir = os.path.join(self.Destpath, "Ref")
        self.Testdir = os.path.join(self.Destpath, "Test")
        self.Importdir = os.path.join(self.Destpath, "Import")
       
    def getfullname(self, dirname, trunkname):
        dirstr = re.sub(self.Dashpat, "", re.sub(self.Slashpat, "-", re.sub(self.Startpat, "", str(dirname))),1)
        fullfilename = dirstr + "-" + trunkname + ".txt"
        #print fullfilename
        return fullfilename

    def getinfo(self, fullname):
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
        #print fullname
        tempname = fullname.rsplit("-",1)
        #print tempname
        if len(tempname) > 1:
            filename = tempname[1] + ".properties"
        else:
            filename =""
        return filename
        
    def gettoday(self):
        return str(date.today())

    def gotodaydir(self):
        todaystr = str("Status_" + str(date.today()))
        self.Todaypath = os.path.join(self.Destpath,todaystr)
        if not os.path.exists(self.Todaypath):
            os.mkdir(self.Todaypath)
        os.chdir(self.Todaypath)

    #def gosubdir(self,subdirname):
    #    gosubdir(subdirname, 1)
 
    #    statfile = self.dm.getnext('Statisticfile')    
    def getnext(self,checkname):
        #path = os.getcwd()
        filename = str(checkname + '.txt')
        fullname = os.path.join(os.getcwd(), filename)
        if os.path.exists(fullname):
            idx = 1
            fullname = os.path.join(os.getcwd(),str(checkname + "_" + str(idx)))
            while os.path.exists(fullname):
                idx = idx + 1
                fullname = os.path.join(os.getcwd(),str(checkname + "_" + str(idx)))
        return fullname
        
    def gosubdir(self,subdirname, overwrite = 1):
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
        return str(datetime.now())

    def getcorename(self, fullname):
        temp1 = str(fullname).split(".", 1)
        temp2 = str(temp1[0]).split("_", 1)
        corename = temp2[0]
        return corename
        
    def gettrunkname(self, fullname):
        temp1 = str(fullname).split(".", 1)
        temp2 = str(temp1[0]).split("_", 1)
        temp3 = str(temp2[0]).rsplit("-", 1)
        if len(temp3) > 1:
            trunkname = temp3[1]
        else:
            trunkname =""
        return trunkname
        
    def getkey(self, fullname):
        temp1 = str(fullname).split(".", 1)
        temp2 = str(temp1[0]).split("_", 1)
        corename = temp2[0]
        if len(temp2) > 1:
            key = temp2[1]
        else:
            key = ""
        return key

    def getdirectorystring(self, fullname):
        dirstr = re.sub(self.Dashpat, str(os.path.sep), fullname)
        directorystring  = os.path.dirname((self.Startpath + str(os.path.sep) + dirstr))
        return directorystring
        