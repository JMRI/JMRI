# Set all icon tooltips empty

import java
import jmri

print 'ClearTooltips: Start'

mgr = jmri.InstanceManager.getDefault(jmri.jmrit.display.EditorManager)

for panel in mgr.getAll():
    for item in panel.getContents():
        if isinstance(item, jmri.jmrit.display.Positionable):
            tip = item.getToolTip()
            if tip is not None:
                tip.setText('')

print 'ClearTooltips: Done'

