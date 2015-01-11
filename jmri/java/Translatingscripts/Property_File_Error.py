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
This Python class is used to trigger file processing errors
"""

class Property_File_Error(Exception):
    def __init__(self, filename, linenum, tag):
        self.filename = filename
        self.linenum = linenum
        self.tag = tag
    def __str__(self):
        return str("File " +  self.filename + ", " + "Line " + str(self.linenum) + ": Unknown Key: " + str(self.tag))
