# Changes the color of all turnout cicles on all LayoutEditor panels
# to a specific color code, including transparency
#
# By Dave Sand, copyright 2025
# See https://github.com/JMRI/JMRI/issues/13759


import java
import java.awt.Color
import jmri

colorClosed = (0, 255, 0, 128)  # r,g,b,a  1/2 transparent green
colorThrown = (255, 0, 0, 128)  # r,g,b,a  1/2 transparent red

editorManager = jmri.InstanceManager.getDefault(jmri.jmrit.display.EditorManager)

for panel in editorManager.getAll(jmri.jmrit.display.layoutEditor.LayoutEditor):
    r, g, b, a = colorClosed
    panel.setTurnoutCircleColor(java.awt.Color(r, g, b, a))
    r, g, b, a = colorThrown
    panel.setTurnoutCircleThrownColor(java.awt.Color(r, g, b, a))
    panel.repaint()

