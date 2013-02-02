// SensorTablePanel.java

package jmri.jmrit.beantable.sensor;

import org.apache.log4j.Logger;
import java.awt.event.*;


/**
 * Swing action to create and register a
 * SensorTable GUI in a JPanel
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2009, 2010
 * @version     $Revision$
 */

public class SensorTablePanel extends jmri.jmrit.beantable.BeanTablePane {

    public SensorTablePanel() {        
    }
    
    public void initComponents() throws Exception {
        createModel();
        init(m);
        
        // fill lower panel
        jmri.util.swing.JmriPanel p = new jmri.jmrit.beantable.sensor.AddSensorPanel();
        p.initComponents();
        p.setMinimumSize(new java.awt.Dimension(0,0));
        getWindowInterface().show(p, null, jmri.util.swing.WindowInterface.Hint.EXTEND);
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

    static final Logger log = Logger.getLogger(SensorTablePanel.class.getName());
}


/* @(#)SensorTablePanel.java */
