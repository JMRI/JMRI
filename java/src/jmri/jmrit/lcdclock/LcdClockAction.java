package jmri.jmrit.lcdclock;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to create and register a LcdClockFrame object
 *
 * @author Ken Cameron Copyright (C) 2007
 * @author Bob Jacobsen Copyright (C) 2024
 *
 * This was a direct steal form the Nixie clock code, ver 1.5. 
 */
public class LcdClockAction extends JmriAbstractAction {

    public LcdClockAction() {
        this("LCD Clock");
    }

    public LcdClockAction(String s) {
        super(s);
    }

    public LcdClockAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public LcdClockAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        LcdClockFrame f = new LcdClockFrame();
        f.setVisible(true);

    }

    @Override
    public jmri.util.swing.JmriPanel makePanel() { return null; } // not used here

}
