// ListAction.java

package jmri.jmrix.cmri.serial.assignment;

import org.apache.log4j.Logger;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			ListFrame object
 *
 * @author   Dave Duchamp Copyright (C) 2006
 * @version	$Revision$
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
        f.setVisible(true);
    }

   static Logger log = Logger.getLogger(ListAction.class.getName());
}

/* @(#)ListAction.java */
