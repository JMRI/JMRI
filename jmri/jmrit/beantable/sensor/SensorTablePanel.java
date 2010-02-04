// SensorTablePanel.java

package jmri.jmrit.beantable.sensor;

import javax.swing.*;
import java.awt.event.*;

/**
 * Swing action to create and register a
 * SensorTable GUI in a JPanel
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2009, 2010
 * @version     $Revision: 1.1 $
 */

public class SensorTablePanel extends jmri.jmrit.beantable.BeanTablePane {

    public SensorTablePanel() {
    
        createModel();
        init(m);
        
    }
    
    jmri.jmrit.beantable.sensor.SensorTableDataModel m;
    
    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Sensors
     */
    void createModel() {
        m = new jmri.jmrit.beantable.sensor.SensorTableDataModel();
    }

    jmri.util.JmriJFrame addFrame = null;
    
    void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new jmri.jmrit.beantable.sensor.AddSensorJFrame();
        }
        addFrame.setVisible(true);
    }

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SensorTablePanel.class.getName());
}


/* @(#)SensorTablePanel.java */
