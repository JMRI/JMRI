// AcelaMonAction.java
package jmri.jmrix.acela.acelamon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register an AcelaMonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 *
 * @author	Bob Coleman, Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaMonAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -6993893709144598203L;

    public AcelaMonAction(String s) {
        super(s);
    }

    public AcelaMonAction() {
        this("Acela message monitor");
    }

    public void actionPerformed(ActionEvent e) {
        // create a AcelaMonFrame
        AcelaMonFrame f = new AcelaMonFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("AcelaMonAction starting AcelaMonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(AcelaMonAction.class.getName());
}

/* @(#)AcelaMonAction.java */
