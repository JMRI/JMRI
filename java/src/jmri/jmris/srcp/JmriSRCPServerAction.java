package jmri.jmris.srcp;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a JmriSRCPServerControlFrame object
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class JmriSRCPServerAction extends AbstractAction {

    public JmriSRCPServerAction(String s) {
        super(s);
    }

    public JmriSRCPServerAction() {
        this("Start JMRI SRCP Server");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        //JmriSRCPServerFrame f = new JmriSRCPServerFrame();
        //f.setVisible(true);
        JmriSRCPServerManager.getInstance().getServer().start();
    }
}



