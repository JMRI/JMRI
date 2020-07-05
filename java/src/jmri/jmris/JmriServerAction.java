package jmri.jmris;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register a JmriServerControlFrame object
 *
 * @author Paul Bender Copyright (C) 2010
 */
@API(status = EXPERIMENTAL)
public class JmriServerAction extends AbstractAction {

    public JmriServerAction(String s) {
        super(s);
    }

    public JmriServerAction() {
        this("Start Jmri Server");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        JmriServerFrame f = new JmriServerFrame();
        f.setVisible(true);

    }
}



