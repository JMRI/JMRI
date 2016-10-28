#
# Sample code for disabling/enabling a Sensor icon on a 
# Layout Editor panel
#
# This is not meant to be directly run. You cut and past this
# code into a larger script of your own, and edit it to refer
# to the specific Sensor(s).
#
# Note that it refers to the _first_ Layout Editor panel
# that it finds. If you have more than one, the references
# to layoutPanels[0] need to be improved to find the right one.
#
# Written by Howard Watkins, Matt Harris and Randall Wood 2015
#

import jmri

#script to disable a sensor
panelMenu = jmri.jmrit.display.PanelMenu.instance()
layoutPanels = panelMenu.getLayoutEditorPanelList()
layoutEditor = layoutPanels[0]
for p in layoutEditor.sensorImage :
    if p.getSensor().getUserName() == "Sensor1" :
        p.setControlling(False)
    if p.getSensor().getUserName() == "Sensor2" :
        p.setControlling(False)


#script to enable a sensor
panelMenu = jmri.jmrit.display.PanelMenu.instance()
layoutPanels = panelMenu.getLayoutEditorPanelList()
layoutEditor = layoutPanels[0]
for p in layoutEditor.sensorImage :
    if p.getSensor().getUserName() == "Sensor1" :
        p.setControlling(True)
    if p.getSensor().getUserName() == "Sensor2" :
        p.setControlling(True)
