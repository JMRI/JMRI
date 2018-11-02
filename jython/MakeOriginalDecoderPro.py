# Script that reconfigures PanelPro to make it look
# like the original (pre-2015) DecoderPro
#
# Author: Bob Jacobsen, copyright 2015
# Part of the JMRI distribution

import jmri
import javax.swing.JButton
import java.util
import java.util.ResourceBundle

import jmri.Application
import apps

f = javax.swing.JPanel()
f.setLayout(javax.swing.BoxLayout(f, javax.swing.BoxLayout.Y_AXIS))

r = java.util.ResourceBundle.getBundle("apps.AppsBundle")

# create the first button and add to the main screen
b = javax.swing.JButton(jmri.jmrit.roster.swing.RosterFrameAction())
p = javax.swing.JPanel()
p.add(b)
f.add(p)

b = javax.swing.JButton(jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction(r.getString("DpButtonUseProgrammingTrack")))
p = javax.swing.JPanel()
p.add(b)
f.add(p)

b = javax.swing.JButton(jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction(r.getString("DpButtonProgramOnMainTrack")))
p = javax.swing.JPanel()
p.add(b)
f.add(p)

apps.Apps.buttonSpace().add(f)

jmri.Application.setLogo("resources/decoderpro.gif")

# force redisplay
apps.Apps.buttonSpace().revalidate()
