###############################################################
#   Clears <File History> area before you save Panel XML file #
#   Gerald Wolfson   9/2017                                   #
###############################################################
import jmri

jmri.InstanceManager.getDefault(jmri.jmrit.revhistory.FileHistory).purge(0)
print("The <File History> section has been cleared")
