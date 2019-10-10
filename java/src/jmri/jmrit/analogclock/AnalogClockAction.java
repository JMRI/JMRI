package jmri.jmrit.analogclock;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a AnalogClockFrame object.
 * Copied from code for NixieClockAction by Bob Jacobsen.
 *
 * @author Dennis Miller Copyright (C) 2004
 */
public class AnalogClockAction extends AbstractAction {

    public AnalogClockAction() {
        this(Bundle.getMessage("MenuItemAnalogClock"));
    }

    public AnalogClockAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        AnalogClockFrame f = new AnalogClockFrame();
        f.setVisible(true);
    }

}
