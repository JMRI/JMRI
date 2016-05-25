// ListAction.java
package jmri.jmrix.maple.assignment;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a ListFrame object
 *
 * @author Dave Duchamp Copyright (C) 2006
 * @version	$Revision$
 */
public class ListAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 969660510115409385L;

    public ListAction(String s) {
        super(s);
    }

    public ListAction() {
        this("List C/MRI Assignments");
    }

    public void actionPerformed(ActionEvent e) {
        ListFrame f = new ListFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(ListAction.class.getName());
}

/* @(#)ListAction.java */
