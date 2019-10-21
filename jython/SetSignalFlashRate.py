# Set the period for flashing signals
#
# This should be invoked before the signals are defined,
# i.e. before the panel files are read.
#
# Author: Bob Jacobsen, copyright 2019
# Part of the JMRI distribution

import jmri

# set the on and off time
jmri.implementation.DefaultSignalHead.masterDelay = 1000
