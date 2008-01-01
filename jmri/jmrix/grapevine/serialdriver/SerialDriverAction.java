// SerialDriverAction.java

package jmri.jmrix.grapevine.serialdriver;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 * grapevine.SerialDriverFrame object
 *
 * @author   Bob Jacobsen    Copyright (C) 2002, 2007
 * @version  $Revision: 1.1 $
 */
public class SerialDriverAction extends AbstractAction {

    public SerialDriverAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
        SerialDriverFrame f = new SerialDriverFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("starting grapevine.SerialDriverFrame caught exception: "+ex.toString());
        }
        f.setVisible(true);
    };

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialDriverAction.class.getName());

}


/* @(#)SerialDriverAction.java */
