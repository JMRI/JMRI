// SerialMonAction.java
package jmri.jmrix.powerline.swing.serialmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.powerline.SerialTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SerialMonFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 * @version	$Revision$
 */
@Deprecated
public class SerialMonAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -563795110468683083L;

    public SerialMonAction(String s, SerialTrafficController tc) {
        super(s);
        this.tc = tc;
    }

    SerialTrafficController tc = null;

    public SerialMonAction(SerialTrafficController tc) {
        this("Powerline Device Monitor", tc);
    }

    public void actionPerformed(ActionEvent e) {
        // create a SerialMonFrame
        SerialMonFrame f = new SerialMonFrame(tc);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("SerialMonAction starting SerialMonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(SerialMonAction.class.getName());

}


/* @(#)SerialMonAction.java */
