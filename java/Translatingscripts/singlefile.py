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

class singlefile:
    def __init__(self, fullfilename, path, filename, corename, key, content):
        self.fullfilename = fullfilename
        self.path = path
        self.filename = filename
        self.corename = corename
        self.key = key
        self.content = content
                
    def iskey(self, seachstring):
        return (seachstring == self.key)
        
    def isname(self, seachstring):
        return (seachstring == self.corename)

    def isitem(self, seachstring):
        #print ('Calling function isitem with ' + seachstring + ' ...')
        for lines in self.content:
            #print(lines)
            if seachstring.strip() == lines.strip():
                return 1
        return 0
