/*
 * DiagnosticAction.java
 *
 * Created on August 18, 2007, 7:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 * @author tim
 */
package jmri.jmrix.tchtech.serial.diagnostic;


import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;


public class DiagnosticAction 	extends AbstractAction {

    public DiagnosticAction(String s) { super(s);}

    public DiagnosticAction() {
        this("Run Node Interface Card Diagnostic");
    }

    public void actionPerformed(ActionEvent e) {
        DiagnosticFrame f = new DiagnosticFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
        }
        f.setVisible(true);
    }

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DiagnosticAction.class.getName());
}

/* @(#)DiagnosticAction.java */

