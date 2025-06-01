# This uses the XmlFile.writeConstantFiles variable to 
# get JMRI to write XML files without dynamic information
# like the date and time written, file history, etc.
# 
# This makes it easier to keep the resulting files in 
# a version control system without merge conflicts.
#
# Bob Jacobsen   2025

import jmri

jmri.jmrit.XmlFile.writeConstantFiles = True
