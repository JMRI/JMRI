// SerialSensorAction.java

package jmri.jmrix.serialsensor;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			SerialSensorFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version	$Revision$
 */
public class SerialSensorAction extends AbstractAction  {

    public SerialSensorAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
        // create a SerialSensorFrame
        SerialSensorFrame f = new SerialSensorFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("starting SerialSensorFrame caught exception: "+ex.toString());
        }
        f.setVisible(true);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialSensorAction.class.getName());

}


/* @(#)SerialSensorAction.java */
