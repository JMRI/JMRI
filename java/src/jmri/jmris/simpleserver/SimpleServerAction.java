package jmri.jmris.simpleserver;

import jmri.InstanceManager;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a SimpleServerControlFrame object
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class SimpleServerAction extends AbstractAction {

    public SimpleServerAction(String s) {
        super(s);
    }

    public SimpleServerAction() {
        this("Start JMRI Simple Server");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        InstanceManager.getDefault(SimpleServerManager.class).getServer().start();
    }
}



