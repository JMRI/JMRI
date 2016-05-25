// pricom.pockettester.StatusAction.java
package jmri.jmrix.pricom.pockettester;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a pricom.pockettester.StatusFrame object
 *
 * @see jmri.jmrix.pricom.pockettester.StatusFrame
 *
 * @author	Bob Jacobsen Copyright (C) 2002,2004, 2005
 * @version	$Revision$
 */
public abstract class StatusAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -2710088536076104047L;

    public StatusAction(String s) {
        super(s);
    }

    public StatusAction() {
        java.util.ResourceBundle rb
                = java.util.ResourceBundle.getBundle("jmri.jmrix.pricom.pockettester.TesterBundle");
        putValue(javax.swing.Action.NAME, rb.getString("ActionStatus"));
    }

    public void actionPerformed(ActionEvent e) {
        StatusFrame f = new StatusFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("starting StatusFrame caught exception: " + ex.toString());
        }
        connect(f);
        f.setVisible(true);
    }

    abstract void connect(StatusFrame l);

    private final static Logger log = LoggerFactory.getLogger(StatusAction.class.getName());

}


/* @(#)StatusAction.java */
