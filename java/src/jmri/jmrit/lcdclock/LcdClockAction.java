package jmri.jmrit.lcdclock;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register a LcdClockFrame object
 *
 * @author Ken Cameron Copyright (C) 2007
 *
 * This was a direct steal form the Nixie clock code, ver 1.5. Thank you Bob
 * Jacobsen.
 */
@API(status = MAINTAINED)
public class LcdClockAction extends AbstractAction {

    public LcdClockAction() {
        this("LCD Clock");
    }

    public LcdClockAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        LcdClockFrame f = new LcdClockFrame();
        f.setVisible(true);

    }

}
