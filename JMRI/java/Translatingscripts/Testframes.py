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
This is the main function and GUI for the translation utility

The main class of this module is Mainframe_Translation where all GUI interactions are concentrated.

There's a pdf document provided with this suite.
Read "Translation_documentation.pdf" for details.

This is the calling hierarchy of this module:
Testframes: Mainframe_Translation
  --> Property_File_Error
  --> filestruct
      --> directorymanager
  --> singlefilestructure
  --> os
  --> os.path
  --> Tkinter
  
Test version 1.2.1 contains these updates:
- added internal documentation within the code
- added a test function, which checks the existence 
  and validity of the versin number string.
- streamlined import funcion removing all obsolete statements
"""

import Tkinter
import os
from Property_File_Error import Property_File_Error
from filestruct import filestruct
from singlefilestructure import singlefilestructure


class Mainframe_Translation(Tkinter.Frame):

    def Create_Filestructure(self):
        """
        This function builds up the internal file structure based on the original project structure.
        """
        self.statustext.set("Creating structure...")
        self.update()
        try:
            for root, dirs, files in os.walk(self.filestruct.dm.Startpath):
                for name in files:
                    trunkname, ext = os.path.splitext(name)
                    if ext == ".properties":
                        fullfilename =  self.filestruct.dm.getfullname(root, trunkname)
                        os.chdir(root)
                        #corename = self.filestruct.dm.getcorename(fullname):
                        cpfile = open(name,"rU")
                        self.filestruct.add(fullfilename, root, name, cpfile.readlines())                        
                        cpfile.close()            
            self.List.delete(0, Tkinter.END)
            self.List.insert(Tkinter.END, "All")
            #print self.filestruct.kinds
            for kinds in self.filestruct.kinds: 
                if not kinds.strip() == '':
                    self.List.insert(Tkinter.END, kinds)
            if not os.path.exists(self.filestruct.dm.Refdir):
                self.filestruct.CopyRef("All")
            else:
                self.filestruct.ReadRef("All")
            self.filestruct.CopyCurr("All")
            if not os.path.exists(self.filestruct.dm.Defpath):
                self.Strlist["state"] = Tkinter.NORMAL
            #else:
            #    self.filestruct.addDefaults()
            self.Load["state"] = Tkinter.NORMAL
            self.Import["state"] = Tkinter.NORMAL
            self.Init["state"] = Tkinter.DISABLED
            self.statustext.set("Done!")
        except Property_File_Error as e:
            self.statustext.set(str(e.filename + ": " + str(e.linenum)))
        self.update()
        
    def Load_Filestructure(self): 
        """
        This function reloads the internal file structure from disk
        """
        try:
            if os.path.exists(self.filestruct.dm.Refdir):
                self.statustext.set("Loading structure...")
                self.update()
            else:
                self.Load["state"] = Tkinter.DISABLED
                self.statustext.set("No reference structure available...")
                self.update()
                return
            self.filestruct.ReadRef("All")
            if os.path.exists(self.filestruct.dm.Currdir):
                self.filestruct.ReadCurr("All")
            else:
                for root, dirs, files in os.walk(self.filestruct.dm.Startpath):
                    for name in files:
                        trunkname, ext = os.path.splitext(name)
                        if ext == ".properties":
                            fullfilename =  self.filestruct.dm.getfullname(root, trunkname)
                            os.chdir(root)
                            cpfile = open(name,"rU")
                            self.filestruct.add(fullfilename, root, name, cpfile.readlines())
                            cpfile.close()
            self.filestruct.CopyCurr("All")
            self.filestruct.checkKinds()
            self.List.delete(0, Tkinter.END)
            self.List.insert(Tkinter.END, "All")
            for kinds in self.filestruct.kinds: 
                if not kinds.strip() == '':
                    self.List.insert(Tkinter.END, kinds)
            if not os.path.exists(self.filestruct.dm.Defpath):
                self.Strlist["state"] = Tkinter.NORMAL
            self.Load["state"] = Tkinter.DISABLED
            self.Init["state"] = Tkinter.DISABLED
            self.Import["state"] = Tkinter.NORMAL
            self.statustext.set("Done!")
        except Property_File_Error as e:
            self.statustext.set(str(e.filename + ": " + str(e.linenum)))
        self.update()
        
    def Create_Stringlists(self): 
        """
        This function creates the default files to simplify ths initial defaults
        """
        if self.filestruct == []:
            self.statustext.set("Failed! Load Data First!")
            self.update()
        else:
            self.statustext.set("Starting...")
            self.update()
            if not os.path.exists(self.filestruct.dm.Defpath):
                os.mkdir(self.filestruct.dm.Defpath)
            os.chdir(self.filegroup.dm.Defpath)
            self.filestruct.create_exceptions()
            self.Strlist["state"] = Tkinter.DISABLED
            self.statustext.set("Done!")
            self.update()
           
    def Evallist(self):
        """
        This function activates or disables function buttons based on the language selection
        """
        self.currlist = self.List.get(self.List.curselection())
        if not self.currlist == "":
            if not os.path.exists(self.filestruct.dm.Refdir):
                self.Init["state"] = Tkinter.NORMAL
                self.Load["state"] = Tkinter.DISABLED
                self.Refdata["state"] = Tkinter.NORMAL
                self.Update["state"] = Tkinter.DISABLED
            else:
                self.Init["state"] = Tkinter.DISABLED
                self.Load["state"] = Tkinter.NORMAL
                self.Refdata["state"] = Tkinter.DISABLED
                self.Update["state"] = Tkinter.NORMAL
                self.STAT["state"] = Tkinter.NORMAL
                self.Export["state"] = Tkinter.NORMAL
                self.Test1["state"] = Tkinter.NORMAL
        
        
    def Exportfunction(self):
        """
        This function exports all new or improved files into the project structure
        """
        #print ("Entering function Exportfunction...")
        if self.filestruct == []:
            self.statustext.set("Failed! Load Data First!")
            self.update()
        elif self.currlist == []:
            self.statustext.set("Failed! Select Language First!")
            self.update()
        else:
            self.statustext.set("Starting...")
            self.update()
            if self.currlist.strip() == "All":
                for actlang in self.filestruct.kinds:
                    if not actlang.strip() == '':
                        self.filestruct.export(self.currlist)
            else:
                self.filestruct.export(self.currlist)
            self.statustext.set("Done!")
            self.update()
            
    def Testfunction1(self):     
        self.statustext.set("Starting...")
        self.update()
        self.filestruct.testfun1()
        self.statustext.set("Done!")
        self.update()
        
    def Testfunction2(self):        
        self.statustext.set("Starting...")
        self.update()
        self.filestruct.testfun2()
        self.statustext.set("Done!")
        self.update()
        
    def Set_Reference_Data(self):
        """
        This function saves the current data as reference data
        """
        if self.filestruct == []:
            self.statustext.set("Failed! Load Data First!")
            self.update()
        else:
            self.statustext.set("Starting...")
            self.update()
            self.statustext.set("Copying " + self.currlist)
            if not os.path.exists(self.filestruct.dm.Refdir):
                os.mkdir(self.filestruct.dm.Refdir)
            os.chdir(self.filestruct.dm.Refdir)
            self.filestruct.exportref(str(self.currlist))
            self.Refdata["state"] = Tkinter.DISABLED
            self.Update["state"] = Tkinter.NORMAL
            self.statustext.set("Done!")
            self.update()

    def Update_Data(self):
        """
        This function updates the internal file structure based on the internal data
        """
        if self.filestruct == []:
            self.statustext.set("Failed! Load Data First!")
            self.update()
        else:
            self.statustext.set("Starting...")
            self.update()
            self.statustext.set("Copying " + self.currlist)
            if not os.path.exists(self.filestruct.dm.Currdir):
                os.mkdir(self.filestruct.dm.Currdir)
            os.chdir(self.filestruct.dm.Currdir)
            self.filestruct.exportcurr(self.currlist)
            self.Refdata["state"] = Tkinter.DISABLED
            self.Update["state"] = Tkinter.NORMAL
            self.statustext.set("Done!")
            self.update()
            
    def Importfunction(self):
        """
        This function imports new or improved translation documents
        """
        if not os.path.exists(self.filestruct.dm.Currdir):
            self.statustext.set("Failed! Create Data First!")
            self.update()
        else:
            if os.path.exists(self.filestruct.dm.Importdir):
                os.chdir(self.filestruct.dm.Importdir)
                self.statustext.set("Starting...")
                self.update()
                tempstruct = []
                for root, dirs, files in os.walk(self.filestruct.dm.Importdir):
                    for name in files:
                        fullname, ext = os.path.splitext(name.strip())
                        if ext == ".txt":
                            #print ('Fullname: ' + fullname)
                            filename, dirname, corename, trunkname, key = self.filestruct.dm.getinfo(fullname)
                            #print ('Filename: ' + filename)
                            #print ('Dirname: ' + dirname)
                            #print ('Corename: ' + corename)
                            #print ('Trunkname: ' + trunkname)
                            #print ('Key: ' + key)
                            if not fullname is "":
                                os.chdir(root)
                                cpfile = open(name,"r")
                                temp = singlefilestructure(self.filestruct.tm, name, dirname, filename, corename, key, cpfile.readlines())
                                tempstruct.append(temp)
                                cpfile.close()
                for file in tempstruct:
                    transstruct = self.filestruct.findcurrgroup(file.corename)
                    transstruct.exchange(file)
                self.statustext.set("Done!")
                self.update()
            else:
                self.statustext.set("No Import found...")
                self.update()

    def Output_Statistics(self):
        """
        This function creates the statistical information for the selected language
        """
        if self.filestruct == []:
            self.statustext.set("Failed! Load Data First!")
            self.update()
        elif self.currlist == []:
            self.statustext.set("Failed! Select Language First!")
            self.update()
        else:
            self.statustext.set("Starting...")
            self.update()
            self.filestruct.getstat(self.currlist)
            self.statustext.set("Done!")
            self.update()
            
    def createWidgets(self):
        """
        This function defines all GUI elements and their pointers to callback functions
        """
        self.Init = Tkinter.Button(self)
        self.Init["text"] = "Read File Structure"
        self.Init["command"] = self.Create_Filestructure
        self.Init.grid(row = 0, column = 0)

        self.Load = Tkinter.Button(self)
        self.Load["text"] = "Load File Structure"
        self.Load["command"] = self.Load_Filestructure
        if os.path.exists(self.filestruct.dm.Destpath):
            if os.path.exists(self.filestruct.dm.Refdir):
                self.Load["state"] = Tkinter.NORMAL
            else:
                self.Load["state"] = Tkinter.DISABLED
        else:
            self.Load["state"] = Tkinter.DISABLED
        self.Load.grid(row = 0, column = 1)

        self.Refdata = Tkinter.Button(self)
        self.Refdata["text"] = "Set Reference Data"
        self.Refdata["command"] = self.Set_Reference_Data
        self.Refdata["state"] = Tkinter.DISABLED
        self.Refdata.grid(row = 1, column = 0)

        self.Update = Tkinter.Button(self)
        self.Update["text"] = "Update Current Data"
        self.Update["command"] = self.Update_Data
        self.Update["state"] = Tkinter.DISABLED
        self.Update.grid(row = 1, column = 1)

        self.List = Tkinter.Listbox(self, selectmode=Tkinter.SINGLE)
        self.List.insert(Tkinter.END, "No Choice available!")
        self.List.grid(row = 0, column = 2, rowspan = 4, columnspan = 2)

        self.Import = Tkinter.Button(self)
        self.Import["text"] = "Import Translation"
        self.Import["command"] = self.Importfunction
        self.Import["state"] = Tkinter.DISABLED
        self.Import.grid(row = 2, column = 0)

        self.Export = Tkinter.Button(self)
        self.Export["text"] = "Export Translations"
        self.Export["command"] =  self.Exportfunction
        self.Export["state"] = Tkinter.DISABLED
        self.Export.grid(row = 2, column = 1)

        self.STAT = Tkinter.Button(self)
        self.STAT["text"] = "Create Statistic"
        self.STAT["command"] =  self.Output_Statistics
        self.STAT["state"] = Tkinter.DISABLED
        self.STAT.grid(row = 3, column = 0)

        self.Strlist = Tkinter.Button(self)
        self.Strlist["text"] = "Create Stringlists"
        self.Strlist["command"] = self.Create_Stringlists
        self.Strlist["state"] = Tkinter.DISABLED
        self.Strlist.grid(row = 3, column = 1)

        self.Test1 = Tkinter.Button(self)
        self.Test1["text"] = "Version number test"
        self.Test1["command"] =  self.Testfunction1
        self.Test1["state"] = Tkinter.NORMAL
        self.Test1.grid(row = 4, column = 0)

        self.Test2 = Tkinter.Button(self)
        self.Test2["text"] = "Encoding Tests"
        self.Test2["command"] = self.Testfunction2
        self.Test2["state"] = Tkinter.NORMAL
        self.Test2.grid(row = 4, column = 1)

        self.OK = Tkinter.Button(self)
        self.OK["text"] = "OK"
        self.OK["command"] =  self.Evallist
        self.OK.grid(row = 5, column = 2)

        self.QUIT = Tkinter.Button(self)
        self.QUIT["text"] = "QUIT"
        self.QUIT["command"] =  self.quit
        self.QUIT.grid(row = 5, column = 3)

        self.statustext = Tkinter.StringVar()
        self.Status = Tkinter.Label(self,width=20,  textvariable=self.statustext)
        self.statustext.set("Init")
        self.Status.grid(row = 5,  column = 0, columnspan=2, sticky="news")

    def __init__(self, master=None):
        Tkinter.Frame.__init__(self, master)
        self.filestruct = filestruct()
        self.pack()
        self.createWidgets()

app = Mainframe_Translation()
app.mainloop()
