// SensorTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Sensor;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import java.util.*;

/**
 * Swing action to create and register a
 * SensorTable GUI
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.4 $
 */

public class SensorTableAction extends AbstractAction {

    public SensorTableAction(String s) { super(s); }
    public SensorTableAction() {}

    public void actionPerformed(ActionEvent e) {
        final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");

        // create the model, with modifications for Sensors
        BeanTableDataModel m = new BeanTableDataModel() {
            public String getValue(String name) {
                int val = InstanceManager.sensorManagerInstance().getBySystemName(name).getKnownState();
                switch (val) {
                case Sensor.ACTIVE: return rbean.getString("SensorStateActive");
                case Sensor.INACTIVE: return rbean.getString("SensorStateInactive");
                case Sensor.UNKNOWN: return rbean.getString("BeanStateUnknown");
                case Sensor.INCONSISTENT: return rbean.getString("BeanStateInconsistent");
                default: return "Unexpected value: "+val;
                }
            }
            public Manager getManager() { return InstanceManager.sensorManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.sensorManagerInstance().getBySystemName(name);}
            public void clickOn(NamedBean t) {
                try {
                    int state = ((Sensor)t).getKnownState();
                    if (state==Sensor.INACTIVE) ((Sensor)t).setKnownState(Sensor.ACTIVE);
                    else ((Sensor)t).setKnownState(Sensor.INACTIVE);
                } catch (JmriException e) { log.warn("Error setting state: "+e); }
            }
            public JButton configureButton() {
                return new JButton(rbean.getString("SensorStateInactive"));
            }

        };
        // create the frame
        BeanTableFrame f = new BeanTableFrame(m);
        f.setTitle(f.rb.getString("TitleSensorTable"));
        f.show();
    }
}


/* @(#)SensorTableAction.java */
