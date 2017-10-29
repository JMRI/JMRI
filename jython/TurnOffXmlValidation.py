# Sample script to turn off validation when reading XML files
#
# Author: Bob Jacobsen, copyright 2017
# Part of the JMRI distribution

import java
import jmri

# to turn off panel file
jmri.InstanceManager.getDefault(jmri.ConfigureManager).setValidate(jmri.jmrit.XmlFile.Validate.None)

# to turn off globally (but might be overridden locally)
jmri.jmrit.XmlFile.setDefaultValidate(jmri.jmrit.XmlFile.Validate.None)
