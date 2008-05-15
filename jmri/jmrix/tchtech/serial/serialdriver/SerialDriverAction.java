/*
 * SerialDriverAction.java
 *
 * Created on August 18, 2007, 10:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmri.jmrix.tchtech.serial.serialdriver;

/**
 *
 * @author tim
 */
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 * cmri.serial.SerialDriverFrame object
 *
 * @author   Bob Jacobsen    Copyright (C) 2002
 * @version  $Revision: 1.1 $
 */
public class SerialDriverAction 			extends AbstractAction {

    public SerialDriverAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
        SerialDriverFrame f = new SerialDriverFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("starting tchtech.serial.SerialDriverFrame caught exception: "+ex.toString());
        }
        f.setVisible(true);
    };

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialDriverAction.class.getName());

}


/* @(#)SerialDriverAction.java */
