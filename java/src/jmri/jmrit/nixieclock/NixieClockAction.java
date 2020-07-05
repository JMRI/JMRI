package jmri.jmrit.nixieclock;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register a NixieClockFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2004
 */
@API(status = MAINTAINED)
public class NixieClockAction extends AbstractAction {

    public NixieClockAction() {
        this("Nixie Clock");
    }

    public NixieClockAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        NixieClockFrame f = new NixieClockFrame();
        f.setVisible(true);
    }

}
