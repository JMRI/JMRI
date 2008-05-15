/*
 * ListAction.java
 *
 * Created on August 17, 2007, 8:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmri.jmrix.tchtech.serial.assignment;

/**
 *
 * @author tim
 */
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ListAction extends AbstractAction {

    public ListAction(String s) { super(s);}

    public ListAction() {
        this("List Interface Card Assignments");
    }

    public void actionPerformed(ActionEvent e) {
        ListFrame f = new ListFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
        }
        f.setVisible(true);
    }

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ListAction.class.getName());
}

/* @(#)ListAction.java */

