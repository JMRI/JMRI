// DiagnosticAction.java

package jmri.jmrix.cmri.serial.diagnostic;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
// temporary for testing, remove when multiple serial nodes is operational
import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.jmrix.cmri.serial.SerialNode;
import jmri.jmrix.cmri.serial.SerialSensorManager;
// end temporary

/**
 * Swing action to create and register a
 *       			DiagnosticFrame object
 *
 * @author                  Dave Duchamp Copyright (C) 2004
 * @version
 */
public class DiagnosticAction 	extends AbstractAction {

    public DiagnosticAction(String s) { super(s);}

    public DiagnosticAction() {
        this("Run C/MRI Diagnostic");
    }

    public void actionPerformed(ActionEvent e) {
// temporary for testing, remove when multiple serial nodes is operational
        if (SerialTrafficController.instance().getSerialNode(0) == null) {
            setUpTestConfiguration();
        }
//  end temporary
        DiagnosticFrame f = new DiagnosticFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
        }
        f.show();
    }

// temporary for testing, remove when multiple serial nodes is operational
    protected synchronized void setUpTestConfiguration() {
        // Define three SMINI's
        SerialNode n0 = new SerialNode();
        SerialNode n1 = new SerialNode(1,SerialNode.SMINI);
        SerialNode n2 = new SerialNode(2,SerialNode.SMINI);
        // Define a Sensor for each serial node (needed for polling)
        SerialSensorManager m = SerialSensorManager.instance();
        m.provideSensor("1");
        m.provideSensor("1001");
        m.provideSensor("2001");
    }
//  end temporary

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DiagnosticAction.class.getName());
}

/* @(#)DiagnosticAction.java */
