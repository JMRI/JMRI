// JmriSRCPServerAction.java
package jmri.jmris.srcp;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a JmriSRCPServerControlFrame object
 *
 * @author Paul Bender Copyright (C) 2009
 * @version $Revision$
 */
public class JmriSRCPServerAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 3322670049459400936L;

    public JmriSRCPServerAction(String s) {
        super(s);
    }

    public JmriSRCPServerAction() {
        this("Start SRCP Jmri Server");
    }

    public void actionPerformed(ActionEvent e) {

        //JmriSRCPServerFrame f = new JmriSRCPServerFrame();
        //f.setVisible(true);
        JmriSRCPServerManager.getInstance().getServer().start();
    }
    private final static Logger log = LoggerFactory.getLogger(JmriSRCPServerAction.class.getName());
}


/* @(#)JmriSRCPServerAction.java */
