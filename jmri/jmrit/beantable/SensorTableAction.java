// SensorTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Sensor;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 * SensorTable GUI
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */

public class SensorTableAction extends AbstractAction {

    public SensorTableAction(String s) { super(s);}
    public SensorTableAction() { this("Sensor Table");}

    public void actionPerformed(ActionEvent e) {

        // create the model, with modifications for Sensors
        BeanTableDataModel m = new BeanTableDataModel() {
            public String getValue(String name) {
                int val = InstanceManager.sensorManagerInstance().getBySystemName(name).getKnownState();
                switch (val) {
                case Sensor.ACTIVE: return "Active";
                case Sensor.INACTIVE: return "Inactive";
                case Sensor.UNKNOWN: return "Unknown";
                case Sensor.INCONSISTENT: return "Inconsistent";
                default: return "Unexpected value: "+val;
                }
            }
            public Manager getManager() { return InstanceManager.sensorManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.sensorManagerInstance().getBySystemName(name);}
        };
        // create the frame
        BeanTableFrame f = new BeanTableFrame(m);
        f.show();
    }
}


/* @(#)SensorTableAction.java */
