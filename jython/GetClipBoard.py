# Sample script showing how to get the contents of the system clipboard
#
# Author: Bob Jacobsen, copyright 2022
# Part of the JMRI distribution


import java
import java.awt
import java.awt.datatransfer

clip = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
data = clip.getData(java.awt.datatransfer.DataFlavor.stringFlavor)

print (data)
