// SensorGroupAction.java

package jmri.jmrit.sensorgroup;

import java.awt.event.*;

import javax.swing.*;

/**
 * Swing action to create and register a SensorGroupFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2007
 * @version     $Revision: 1.1 $
 */
public class SensorGroupAction extends AbstractAction {

    public SensorGroupAction(String s) { 
	super(s);

     // disable ourself if there is no route manager object available
        if (jmri.InstanceManager.routeManagerInstance()==null) {
            setEnabled(false);
        }
    }

    public SensorGroupAction() { this("Define Sensor Group...");}

    public void actionPerformed(ActionEvent e) {
        SensorGroupFrame f = new SensorGroupFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
        }
        f.setVisible(true);
    }
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SensorGroupAction.class.getName());
}

/* @(#)SensorGroupAction.java */
