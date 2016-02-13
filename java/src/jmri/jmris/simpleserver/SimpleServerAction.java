// SimpleServerAction.java
package jmri.jmris.simpleserver;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SimpleServerControlFrame object
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision$
 */
public class SimpleServerAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 9027149722115966187L;

    public SimpleServerAction(String s) {
        super(s);
    }

    public SimpleServerAction() {
        this("Start Simple Jmri Server");
    }

    public void actionPerformed(ActionEvent e) {

        // SimpleServerFrame f = new SimpleServerFrame();
        // f.setVisible(true);
        SimpleServerManager.getInstance().getServer().start();
    }
    private final static Logger log = LoggerFactory.getLogger(SimpleServerAction.class.getName());
}


/* @(#)SimpleServerAction.java */
