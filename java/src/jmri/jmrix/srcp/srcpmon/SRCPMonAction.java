// SRCPMonAction.java
package jmri.jmrix.srcp.srcpmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SRCPMonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @version $Revision$
 */
public class SRCPMonAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 1842953700313402490L;

    public SRCPMonAction() {
        super("SRCP Monitor");
    }

    public SRCPMonAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        // create a SRCPMonFrame
        SRCPMonFrame f = new SRCPMonFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("SRCPMonAction starting SRCPMonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(SRCPMonAction.class.getName());

}


/* @(#)SRCPMonAction.java */
