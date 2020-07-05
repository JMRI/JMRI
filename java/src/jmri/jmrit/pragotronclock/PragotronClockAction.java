package jmri.jmrit.pragotronclock;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register a PragotronClockFrame object.
 *
 * @author Petr Sidlo Copyright (C) 2019
 *
 * Based on Nixie clock by Bob Jacobsen.
 */
@API(status = MAINTAINED)
public class PragotronClockAction extends AbstractAction {

    public PragotronClockAction() {
        this("Pragotron Clock");
    }

    public PragotronClockAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        PragotronClockFrame f = new PragotronClockFrame();
        f.setVisible(true);
    }

}
