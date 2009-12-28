// SensorTableAction.java

package jmri.jmrit.beantable;

import jmri.*;
import jmri.util.JmriJFrame;

import java.awt.event.ActionEvent;

import javax.swing.*;


/**
 * Swing action to create and register a
 * SensorTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2009
 * @version     $Revision: 1.24 $
 */

public class SensorTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public SensorTableAction(String actionName) {
        super(actionName);

        // disable ourself if there is no primary sensor manager available
        if (jmri.InstanceManager.sensorManagerInstance()==null ||
            (((jmri.managers.AbstractProxyManager)jmri.InstanceManager
                                                 .sensorManagerInstance())
                                                 .systemLetter()=='\0')) {
            setEnabled(false);
        }
    }
    public SensorTableAction() { this("Sensor Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Sensors
     */
    void createModel() {
        m = new jmri.jmrit.beantable.sensor.SensorTableDataModel();
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleSensorTable"));
    }

    String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorTable";
    }

    JmriJFrame addFrame = null;
    
    void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new jmri.jmrit.beantable.sensor.AddSensorJFrame();
        }
        addFrame.setVisible(true);
    }

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SensorTableAction.class.getName());
}


/* @(#)SensorTableAction.java */
