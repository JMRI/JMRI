//SprogMonAction.java
package jmri.jmrix.sprog.sprogmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SprogMonFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 */
public class SprogMonAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 8764899139164410382L;

    public SprogMonAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        // create a SprogMonFrame
        SprogMonFrame f = new SprogMonFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("SprogMonAction starting SprogMonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(SprogMonAction.class.getName());

}


/* @(#)SprogMonAction.java */
