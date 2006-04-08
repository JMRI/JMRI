// ListAction.java

package jmri.jmrix.cmri.serial.assignment;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			ListFrame object
 *
 * @author   Dave Duchamp Copyright (C) 2006
 * @version	$Revision: 1.1 $
 */
public class ListAction extends AbstractAction {

    public ListAction(String s) { super(s);}

    public ListAction() {
        this("List C/MRI Assignments");
    }

    public void actionPerformed(ActionEvent e) {
        ListFrame f = new ListFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
        }
        f.show();
    }

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ListAction.class.getName());
}

/* @(#)ListAction.java */
