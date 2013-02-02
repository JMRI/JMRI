// SensorGroupAction.java

package jmri.jmrit.sensorgroup;

import org.apache.log4j.Logger;
import java.awt.event.*;

import javax.swing.*;

/**
 * Swing action to create and register a SensorGroupFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2007
 * @version     $Revision$
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
        f.initComponents();
        f.setVisible(true);
    }
    static Logger log = Logger.getLogger(SensorGroupAction.class.getName());
}

/* @(#)SensorGroupAction.java */
