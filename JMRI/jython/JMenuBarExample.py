# Sample script to show how to work with the PanelPro
# main menu bar.
#
# Author: Bob Jacobsen, copyright 2015
# Part of the JMRI distribution

import java
import jmri
import apps
import javax
import javax.swing

# get the menubar
menubar = javax.swing.SwingUtilities.getWindowAncestor(apps.Apps.buttonSpace().getParent()).getJMenuBar()

print menubar

# get the Debug menu
# (you can also search for through a loop, that's more flexible if you have multiple connections)
# watch for internationalization of this!
menu = menubar.getMenu(2)

print menu.getText()  # should say Debug iff one connection


