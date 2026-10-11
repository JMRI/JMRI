package jmri.jmrit.analogclock;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to create and register a AnalogClockFrame object.
 * Copied from code for NixieClockAction by Bob Jacobsen.
 *
 * @author Dennis Miller Copyright (C) 2004
 */
public class AnalogClockAction extends JmriAbstractAction {

    public AnalogClockAction() {
        this(Bundle.getMessage("MenuItemAnalogClock"));
    }

    public AnalogClockAction(String s) {
        super(s);
    }

    public AnalogClockAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public AnalogClockAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        AnalogClockFrame f = new AnalogClockFrame();
        f.setVisible(true);
    }

    @Override
    public jmri.util.swing.JmriPanel makePanel() { return null; } // not used here
}
