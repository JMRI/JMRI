// SensorGroupAction.java
package jmri.jmrit.sensorgroup;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SensorGroupFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2007
 * @version $Revision$
 */
public class SensorGroupAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -6704486852181647895L;

    public SensorGroupAction(String s) {
        super(s);

        // disable ourself if there is no route manager object available
        if (jmri.InstanceManager.routeManagerInstance() == null) {
            setEnabled(false);
        }
    }

    public SensorGroupAction() {
        this("Define Sensor Group...");
    }

    public void actionPerformed(ActionEvent e) {
        SensorGroupFrame f = new SensorGroupFrame();
        f.initComponents();
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(SensorGroupAction.class.getName());
}

/* @(#)SensorGroupAction.java */
