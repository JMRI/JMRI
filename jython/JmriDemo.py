# Python code to start a JmriDemo app, complete with menu bar
#
#
# Obsolete: the usual method now is to start DecoderPro or PanelPro
# and then run Python scripts from within it; left as
# an example of the approach
#
# Author: Bob Jacobsen, copyright 2004
# Part of the JMRI distribution

import jmri

# start the program
import apps
apps.JmriDemo.JMRIdemo.main([]) 

# define the usual defaults
execfile("jython/jmri_defaults.py")

