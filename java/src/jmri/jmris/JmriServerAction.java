// JmriServerAction.java
package jmri.jmris;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a JmriServerControlFrame object
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision$
 */
public class JmriServerAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -6687184200606555912L;

    public JmriServerAction(String s) {
        super(s);
    }

    public JmriServerAction() {
        this("Start Jmri Server");
    }

    public void actionPerformed(ActionEvent e) {

        JmriServerFrame f = new JmriServerFrame();
        f.setVisible(true);

    }
    private final static Logger log = LoggerFactory.getLogger(JmriServerAction.class.getName());
}


/* @(#)JmriServerAction.java */
