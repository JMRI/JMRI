package jmri.jmrit.entryexit;

import java.awt.Component;
import java.awt.Container;
import java.util.HashMap;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.SensorIcon;
import jmri.jmrit.display.layoutEditor.LayoutEditor;

import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
import javax.swing.JFrame;
import javax.swing.JLabel;

class EntryExitTestTools {
    static HashMap<String, LayoutEditor> getPanels() throws Exception {
        HashMap<String, LayoutEditor> panels = new HashMap<>();
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
        java.io.File f = new java.io.File("java/test/jmri/jmrit/entryexit/load/EntryExitTest.xml");
        cm.load(f);

        for (LayoutEditor panel : InstanceManager.getDefault(PanelMenu.class).getLayoutEditorPanelList()) {
            switch (panel.getLayoutName()) {
                case "Alpha":
                    panels.put("Alpha", panel);
                    break;
                case "Beta":
                    panels.put("Beta", panel);
                    break;
                default:
                    break;
            }
        }

        InstanceManager.getDefault(SensorManager.class).getSensor("Reset").setKnownState(Sensor.ACTIVE);
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
        return panels;
    }

    PointDetails getPoint(Sensor sensor, LayoutEditor panel, EntryExitPairs eep) {
        return (sensor == null) ? null : eep.providePoint(sensor, panel);
    }

    Source getSourceInstance(Sensor sensor, LayoutEditor panel, EntryExitPairs eep) {
        PointDetails pd = getPoint(sensor, panel, eep);
        return (pd == null) ? null : eep.getSourceForPoint(pd);
    }

    DestinationPoints getDestinationPoint(Sensor srcSensor, Sensor destSensor, LayoutEditor panel,  EntryExitPairs eep) {
        Source src = getSourceInstance(srcSensor, panel, eep);
        PointDetails pd = getPoint(destSensor, panel, eep);
        return (src == null || pd == null) ? null : src.getDestForPoint(pd);
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EntryExitTestTools.class);
}