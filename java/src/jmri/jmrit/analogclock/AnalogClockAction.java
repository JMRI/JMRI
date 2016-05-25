// AnalogClock.Action.java
package jmri.jmrit.analogclock;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a AnalogClockFrame object Copied from
 * code for NixieClockAction by Bob Jacobsen
 *
 * @author	Dennis Miller Copyright (C) 2004
 * @version	$Revision$
 */
public class AnalogClockAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 4819621738581568597L;

    public AnalogClockAction() {
        this("Analog Clock");
    }

    public AnalogClockAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {

        AnalogClockFrame f = new AnalogClockFrame();
        f.setVisible(true);

    }

}
