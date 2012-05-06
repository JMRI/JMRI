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
This Python class represents one single file for translation containing several keys
"""

import os
from textmanager import textmanager
from directorymanager import directorymanager
from singlefilestructure import singlefilestructure
from translatefilestructure import translatefilestructure

class filestruct:
    def __init__(self):
        self.count = 0
        self.kinds = []
        self.files = []
        self.Defaults = []
        self.statistic = []
        self.statrun = 0
        #self.sortgroups = []
        self.refdata = []
        self.currdata = []
        #self.reports = []
        self.dm = directorymanager()
        self.tm = textmanager(self.dm)
        
    def CopyRef(self, key):
        for actfile in self.files:
            if actfile.iskey(""):
                temp = translatefilestructure(self.tm, actfile.corename, actfile.copy())
                #print ('Copy Reference File: ' + actfile.corename)
                self.refdata.append(temp)
        for group in self.refdata:
            for actfile in self.files:
                if not actfile.iskey("") and actfile.isname(group.corename):
                    group.add(actfile.copy())
        
    def CopyCurr(self, key):
        for actfile in self.files:
            if actfile.iskey(""):
                #print ('Copy Current File: ' + actfile.corename)
                temp = translatefilestructure(self.tm, actfile.corename, actfile.copy())
                #print ('Copy Current File: ' + actfile.corename)
                self.currdata.append(temp)
        for group in self.currdata:
            for actfile in self.files:
                if not actfile.iskey("") and actfile.isname(group.corename):
                    group.add(actfile.copy())
        
    def ReadRef(self, key):
        tempstruct = []
        for root, dirs, files in os.walk(self.dm.Refdir):
            for name in files:
                #print (' Read Reference File: ' + name)
                fullname, ext = os.path.splitext(name.strip())
                #print str("fullname is " + fullname)
                if ext == ".txt":
                    filename =  self.dm.getfilename(fullname)
                    #print str("filename is " + filename)
                    dirname = self.dm.getdirectorystring(fullname)
                    #print dirname
                    corename = self.dm.getcorename(fullname)
                    #print corename
                    key = self.dm.getkey(fullname)
                    #print key
                    if not fullname is "":
                        os.chdir(root)
                        cpfile = open(name,"r")
                        temp = singlefilestructure(self.tm, name, dirname, filename, corename, key, cpfile.readlines())
                        tempstruct.append(temp)
                        #tempstruct.add(name, dirname, corename, cpfile.readlines())
                        cpfile.close()
            #print "End of loop..."
        #print str("Length of structure: " + str(len(tempstruct)))
        for actfile in tempstruct:
            if actfile.iskey(""):
                temp = translatefilestructure(self.tm, actfile.corename, actfile)
                self.refdata.append(temp)
                #print str("Creating Group " + actfile.corename + "...")
        for group in self.refdata:
            for actfile in tempstruct:
                #print str("Comparing " + group.corename + " with " + actfile.corename + " ...")
                if not actfile.iskey("") and actfile.isname(group.corename):
                    group.add(actfile)
                    #print str("Adding " + actfile.corename + "...")

    def ReadCurr(self, key):
        tempstruct = []
        for root, dirs, files in os.walk(self.dm.Currdir):
            for name in files:
                #print ('Read Current File: ' + name)
                fullname, ext = os.path.splitext(name.strip())
                #print str("fullname is " + fullname)
                #print ("fullpath is " + os.path.normpath(os.path.join(self.dm.getdirectorystring(fullname),self.dm.getfilename(fullname))))
                if ext == ".txt":
                    filename =  self.dm.getfilename(fullname)
                    #print str("filename is " + filename)
                    dirname = self.dm.getdirectorystring(fullname)
                    #print dirname
                    corename = self.dm.getcorename(fullname)
                    #print corename
                    key = self.dm.getkey(fullname)
                    #print key
                    if not fullname is "":
                        os.chdir(root)
                        cpfile = open(name,"r")
                        temp = singlefilestructure(self.tm, name, dirname, filename, corename, key, cpfile.readlines())
                        tempstruct.append(temp)
                        #tempstruct.add(name, dirname, corename, cpfile.readlines())
                        cpfile.close()
            #print "End of loop..."
        for actfile in tempstruct:
            if actfile.iskey(""):
                temp = translatefilestructure(self.tm, actfile.corename, actfile)
                self.currdata.append(temp)
        for group in self.currdata:
            for actfile in tempstruct:
                if not actfile.iskey("") and actfile.isname(group.corename):
                    group.add(actfile)

    def add(self, fullfilename, filepath, filename, filecontent): 
        corename = self.dm.getcorename(fullfilename)
        key = self.dm.getkey(fullfilename)
        temp = singlefilestructure(self.tm, fullfilename, filepath, filename, corename, key, filecontent)
        self.files.append(temp)
        if not key in self.kinds:
            self.kinds.append(key)
        self.count = self.count + 1

    def checkKinds(self):
        for group in self.refdata:
            #print group.getKinds()
            for kind in group.getKinds():
                if not kind in self.kinds:
                    self.kinds.append(kind)
                self.count = self.count + 1
        for group in self.currdata:
            for kind in group.getKinds():
                if not kind in self.kinds:
                    self.kinds.append(kind)
                self.count = self.count + 1
        #print self.kinds
                        
    def datalen(self):
        return len(self.files)
        
    def exportref(self, languagestring):
        for group in self.refdata:
            group.export(languagestring)

    def findrefgroup(self, corename):
        for group in self.refdata:
            if str(group.corename).strip() == str(corename).strip():
                return group
        return []
    
    def exportcurr(self, languagestring):
        for group in self.currdata:
            group.export(languagestring)

    def findcurrgroup(self, corename):
        for group in self.currdata:
            if str(group.corename).strip() == str(corename).strip():
                return group
        return []
    
    def testfun1(self):
        #Lineseparator = str(os.linesep)
        if not os.path.exists(self.dm.Testdir):
            os.mkdir(self.dm.Testdir)
        os.chdir(self.dm.Testdir)
        descfile = open('Testdesc.txt','w')
        for group in self.currdata:
            group.testversion(descfile)
        descfile.close()

    def testfun2(self):
        if not os.path.exists(self.dm.Testdir):
            os.mkdir(self.dm.Testdir)
        os.chdir(self.dm.Testdir)
        for root, dirs, files in os.walk(self.dm.Startpath):
            for name in files:
                trunkname, ext = os.path.splitext(name)
                if ext == ".properties":
                    fullfilename =  self.dm.getfullname(root, trunkname)
                    os.chdir(root)
                    origfile = open(name,"rU")
                    originalcontent = origfile.readlines()
                    origfile.close()
                    newcontent = []
                    for indline in originalcontent:
                        templine = indline.decode('utf_16','replace')
                        newline = templine.encode('utf_8','replace')
                        newcontent = newcontent + newline
                    cpfile = open(fullfilename,'w')
                    cpfile.write(newcontent)
                    cpfile.close()            

    def savestruct(self, languagestring):
        for group in self.sortgroups:
            if group.exist(languagestring):
                origfile = open(group.original.fullfilename,'w')
                origfile.writelines(group.original.content)
                origfile.close()
                transfilehandle = group.get(languagestring)
                if not transfilehandle == []:
                    transfile = open(transfilehandle.fullfilename,'w')
                    transfile.writelines(transfilehandle.content)
                    transfile.close()

    def create_exceptions(self):
        #Lineseparator = str(os.linesep)
        for actfile in self.files:
            if actfile.iskey(""):
                actfilename = actfile.corename + ".txt"
                actfiletxt = open(actfilename,'w')
                #linecnt = 0;
                #for lines in actfile.content:
                #    linecnt = linecnt + 1
                #    if not lines[0] == "#":
                #        temp = lines.split("=")
                #        if not len(temp) == 1:
                #            string_key = temp[0].strip()
                #            actfiletxt.write(string_key + "\n")
                actfiletxt.close()

    def getreport(self, repfile, languagestring):
        repfile.write(str("Language: " + languagestring + " \n"))
        repfile.write(str("Currfiles: \n"))
        for group in self.currdata:
            for kind in group.getKinds():
                if str(kind).strip() == str(languagestring).strip(): 
                    repfile.write(str("File: " + group.original.fullfilename + " \n"))
        
    def getstat(self, languagestring):
        #self.statistics = []
        if languagestring.strip() == "All":
            self.dm.gotodaydir()
            statfilename = self.dm.getnext('Statisticfile')
            self.statrun = 1
            self.statistics = open(statfilename,'w')
            self.statistics.flush()
            if (len(self.currdata) > 0):
                self.statistics.write(str("\n" + "Number of original Files: " + str(len(self.currdata))) + "\n")
                self.statistics.write(self.dm.Lineseparator)
                for kind in self.kinds:
                    if not kind is "":
                        idx = 0
                        for group in self.currdata:
                            if group.exist( kind):
                                idx = idx + 1
                        self.statistics.write(str("Number of " + kind + " Files: " + str(idx) + " (" + str((100*idx)/len(self.currdata)) + "%)" + "\n"))
            else:
                self.statistics.write(str("\n No Files loaded! \n"))
            #print self.statistics
            for actlang in self.kinds:
                if not actlang.strip() == '':
                    self.getstat(actlang.strip())
            self.statistics.close()
            self.statrun = 0
        else:            
            Num_of_total_properties = 0
            Num_of_translated_properties = 0
            Files_not_exist_list = []
            Files_new_list = []
            Files_incomplete_list = []
            Files_nontrans_list = []
            Files_complete_list = []
            #print self.statistics
            self.dm.gotodaydir()
            self.dm.gosubdir(languagestring,0)
            langdir = os.getcwd()
            statfile = open("Statisticfile_" + languagestring + ".txt",'w')
            statfile.flush()
            statfile.write(str("Statistic Output generated " + self.dm.getnow() + "\n\n"))
            for currgroup in self.currdata:
                #print ('Testing ' + currgroup.corename.strip() + ' ...')
                refgroup = []
                for tempgroup in self.refdata:
                    #print ('Checking '+  tempgroup.corename.strip())
                    if tempgroup.corename.strip() == currgroup.corename.strip():
                        refgroup = tempgroup
                        #print ('Refgroup fond!')
                        break
                reforigfile = []
                reftransfile = []
                if not refgroup == []:
                    reforigfile = refgroup.original
                    reftransfile = refgroup.get(languagestring)
                currorigfile = currgroup.original
                currtransfile = currgroup.get(languagestring)
                if currtransfile == []:
                    if self.tm.isNonTrans(currorigfile.fullfilename):
                        Files_nontrans_list.append(currorigfile)
                    else:
                        Files_not_exist_list.append(currorigfile)
                else:
                    numMissing, numNontrans, numObsolete = currtransfile.compare(self.tm, currorigfile)
                    if not reftransfile == []:
                        if (numMissing == 0) and (numNontrans == 0) and (numObsolete == 0):
                            Files_complete_list.append(currorigfile)
                            Files_complete_list.append(currtransfile)
                        else:
                            Files_incomplete_list.append(currorigfile)
                            Files_incomplete_list.append(currtransfile)
                    else:
                        if (numMissing == 0) and (numNontrans == 0) and (numObsolete == 0):
                            Files_complete_list.append(currorigfile)
                            Files_complete_list.append(currtransfile)
                        else:
                            Files_new_list.append(currorigfile)
                            Files_new_list.append(currtransfile)
                    Num_of_translated_properties = Num_of_translated_properties + currtransfile.numkeys
                Num_of_total_properties = Num_of_total_properties + currorigfile.numkeys
            os.chdir(langdir)
            statfile.write(str("\nNon translatable files: " + str(len(Files_nontrans_list)) + " \n"))
            if len(Files_nontrans_list) > 0:
                self.dm.gosubdir('Non_Translatable')
                for filename in Files_nontrans_list:
                    if filename.key == "":
                        statfile.write(str(filename.fullfilename + ":\n"))
                        statfile.write(str("Nontrans: " + str(filename.numkeys) + "\n"))
                    transfile = open(filename.fullfilename,'w')
                    filename.write(transfile)
                    transfile.close()
            os.chdir(langdir)
            statfile.write(str("\nNot existing files: " + str(len(Files_not_exist_list)) + " \n"))
            if len(Files_not_exist_list) > 0:
                self.dm.gosubdir('Not_Existing')
                for filename in Files_not_exist_list:
                    if filename.key == "":
                        statfile.write(str(filename.fullfilename + ":\n"))
                        statfile.write(str("Missing: " + str(filename.numkeys) + "\n"))
                    transfile = open(filename.fullfilename,'w')
                    filename.write(transfile)
                    transfile.close()
            os.chdir(langdir)
            statfile.write(str("\nNew files: " + str(len(Files_new_list)/2) + " \n"))
            if len(Files_new_list) > 0:
                self.dm.gosubdir('New_Files')
                for filename in Files_new_list:
                    if filename.key == "":
                        statfile.write(str(filename.fullfilename + "\n"))
                    transfile = open(filename.fullfilename,'w')
                    filename.write(transfile)
                    transfile.close()
                    if not filename.getreport() == []:
                        updfile = open(str(filename.corename + "_todo.txt"),'w')
                        updfile.write(filename.getreport())
                        updfile.close()            
                        statfile.write(str("Total: " + str(filename.numkeys) + ", Missing: " + str(filename.numMissing) + ", Nontrans: " + str(filename.numNontrans) + ", Obsolete: " + str(filename.numObsolete) + "\n"))
            os.chdir(langdir)
            statfile.write(str("\nIncomplete files: " + str(len(Files_incomplete_list)/2) + " \n"))
            if len(Files_incomplete_list) > 0:
                self.dm.gosubdir('Incomplete')
                for filename in Files_incomplete_list:
                    if filename.key == "":
                        statfile.write(str(filename.fullfilename + ":\n"))
                    transfile = open(filename.fullfilename,'w')
                    filename.write(transfile)
                    transfile.close()
                    if not filename.getreport() == []:
                        updfile = open(str(filename.corename + "_todo.txt"),'w')
                        updfile.write(filename.getreport())
                        updfile.close()
                        statfile.write(str("Total: " + str(filename.numkeys) + ", Missing: " + str(filename.numMissing) + ", Nontrans: " + str(filename.numNontrans) + ", Obsolete: " + str(filename.numObsolete) + "\n"))
            os.chdir(langdir)
            statfile.write(str("\nComplete files: " + str(len(Files_complete_list)/2) + " \n"))
            if len(Files_complete_list) > 0:
                self.dm.gosubdir('Complete')
                for filename in Files_complete_list:
                    if filename.key == "":
                        statfile.write(str(filename.fullfilename + "\n"))
                        statfile.write(str("Completed: " + str(filename.numkeys) + "\n"))
                    transfile = open(filename.fullfilename,'w')
                    filename.write(transfile)
                    transfile.close()  
            #print self.statistics
            if self.statrun == 1:
                self.statistics.write(str("\nNumber of " + languagestring + " Properties\n"))
                self.statistics.write(str("\n" + "Total number of properties: " + str(Num_of_total_properties) + "\n"))
                if Num_of_total_properties == 0:
                    self.statistics.write(str("Number of translated properties: " + str(Num_of_translated_properties) + " \n"))
                else:
                    self.statistics.write(str("Number of translated properties: " + str(Num_of_translated_properties) + " (" + str((100*Num_of_translated_properties)/Num_of_total_properties) + "%)" + "\n"))
            statfile.write(str("\n"))
            statfile.write(str("\n" + "Total number of properties: " + str(Num_of_total_properties) + "\n"))
            #print self.statistics
            if Num_of_total_properties == 0:
                statfile.write("Number of translated properties: " + str(Num_of_translated_properties) + " \n")
            else:
                statfile.write("Number of translated properties: " + str(Num_of_translated_properties) + " (" + str((100*Num_of_translated_properties)/Num_of_total_properties) + "%)" + "\n")
            if (len(self.currdata) > 0):
                statfile.write(str("\n" + "Number of original Files: " + str(len(self.currdata))) + "\n")
                for kind in self.kinds:
                    #if not kind is "":
                    if  kind is languagestring:
                        idx = 0
                        for group in self.currdata:
                            if group.exist( kind):
                                idx = idx + 1
                        statfile.write(str("Number of " + kind + " Files: " + str(idx) + " (" + str((100*idx)/len(self.currdata)) + "%)" + "\n"))
            else:
                statfile.write(str("\n No Files loaded! \n"))
            statfile.close()
        
    def export(self, languagestring):
        self.dm.gotodaydir()
        #print ("Entering function filestruct.export...")
        logfile = open("Logfile_" + languagestring +".txt",'a')
        cvsfile = open("CVSfile_" + languagestring +".txt",'a')
        logfile.write(str("Logging Output generated " + self.dm.getnow() + "\n\n"))
        cvsfile.write("export CVS_RSH=ssh\n")
        cvsfile.write("CVSROOT=ginsburg@jmri.cvs.sourceforge.net:/cvsroot/jmri\n")
        for currgroup in self.currdata:
            #print ("Testing " + currgroup.corename.strip() + " ...")
            refgroup = []
            for tempgroup in self.refdata:
                #print ('Checking '+  tempgroup.corename.strip())
                if tempgroup.corename.strip() == currgroup.corename.strip():
                    refgroup = tempgroup
                    #print ('Refgroup fond!')
                    break
            reforigfile = []
            reftransfile = []
            if not refgroup == []:
                reforigfile = refgroup.original
                reftransfile = refgroup.get(languagestring)
            currorigfile = currgroup.original
            currtransfile = currgroup.get(languagestring)
            #print(currtransfile)
            if not currtransfile == []:
                if os.path.exists(os.path.normpath(os.path.join(currtransfile.path,currtransfile.filename))):
                    numMissing, numNontrans, numObsolete = currtransfile.compare(self.tm, currorigfile)
                    if (numMissing == 0) and (numNontrans == 0) and (numObsolete == 0):
                        os.chdir(currtransfile.path)
                        cpfile = open(currtransfile.filename,"r")
                        reffile = singlefilestructure(self.tm, currtransfile.fullfilename, currtransfile.path, currtransfile.filename, currtransfile.corename, currtransfile.key, cpfile.readlines())
                        #print(currtransfile.filename)
                        #print(currtransfile.fullfilename)
                        cpfile.close()
                        if currtransfile.isdifferent(self.tm, reffile):
                            #print(currtransfile.path)
                            #print(currtransfile.filename)
                            #print(currtransfile.fullfilename)
                            #print(reffile.filename)
                            #print(reffile.fullfilename)
                            cpfile = open(currtransfile.filename,"w")
                            currtransfile.write(cpfile)
                            cpfile.close()
                            os.chdir(self.dm.Refdir)
                            cpfile = open(currorigfile.fullfilename,"w")
                            currorigfile.write(cpfile)
                            cpfile.close()
                            cpfile = open(currtransfile.fullfilename,"w")
                            currtransfile.write(cpfile)
                            cpfile.close()
                            logfile.write(str("Updating file: " + currorigfile.filename + "\n"))
                            cvsfile.write("\n" + "cd " + str(currorigfile.path).strip() + "\n")
                            redname = currorigfile.filename.split(".", 1)
                            cvsfile.write("cvs -z3 -d:ext:ginsburg@jmri.cvs.sourceforge.net:/cvsroot/jmri update " + currorigfile.filename + "\n")
                else:
                    numMissing, numNontrans, numObsolete = currtransfile.compare(self.tm, currorigfile)
                    #print(currtransfile.path)
                    #print(currtransfile.filename)
                    if (numMissing == 0) and (numNontrans == 0) and (numObsolete == 0):
                        os.chdir(currtransfile.path)
                        #print(currtransfile.path)
                        #print(currtransfile.filename)
                        #print(currtransfile.fullfilename)
                        cpfile = open(currtransfile.filename,"w")
                        currtransfile.write(cpfile)
                        cpfile.close()
                        os.chdir(self.dm.Refdir)
                        cpfile = open(currorigfile.fullfilename,"w")
                        currorigfile.write(cpfile)
                        cpfile.close()
                        cpfile = open(currtransfile.fullfilename,"w")
                        currtransfile.write(cpfile)
                        cpfile.close()
                        logfile.write(str("Adding file: " + currorigfile.filename + "\n"))
                        cvsfile.write("\n" + "cd " + str(currorigfile.path).strip() + "\n")
                        redname = currorigfile.filename.split(".", 1)
                        cvsfile.write("cvs -z3 -d:ext:ginsburg@jmri.cvs.sourceforge.net:/cvsroot/jmri add " + currorigfile.filename + "\n")
        logfile.write(str("\n"))
        cvsfile.write("\n" + "cd " + str(self.dm.Startpath).strip() + "\n")
        cvsfile.write("cvs -d:ext:ginsburg@jmri.cvs.sourceforge.net:/cvsroot/jmri commit -m \"Translation Update " + self.dm.gettoday() + "\"\n")
        cvsfile.close()
        logfile.close()
        
            
            
            
            
            
            
            
            
