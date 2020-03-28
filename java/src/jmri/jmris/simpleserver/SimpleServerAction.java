package jmri.jmris.simpleserver;

import jmri.InstanceManagerDelegate;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a SimpleServerControlFrame object
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class SimpleServerAction extends AbstractAction {

    public final InstanceManagerDelegate instanceManagerDelegate;

    public SimpleServerAction(String s) {
        this(s,new InstanceManagerDelegate());
    }

    public SimpleServerAction() {
        this("Start JMRI Simple Server");
    }

    public SimpleServerAction(String s, InstanceManagerDelegate instanceManagerDelegate){
        super(s);
        this.instanceManagerDelegate = instanceManagerDelegate;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        instanceManagerDelegate.getDefault(SimpleServerManager.class).getServer().start();
    }
}



