package jmri.jmrit.ampmeter;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a AmpMeterFrame object.
 *
 * @author Ken Cameron Copyright (C) 2007
 * @author Mark Underwood Copyright (C) 2007
 *
 * This was a direct steal form the LCDClock code by Ken Cameron,
 * which was a direct steal from the Nixie clock code, ver 1.5. 
 * Thank you Bob Jacobsen and Ken Cameron.
 */
public class AmpMeterAction extends AbstractAction {

    public AmpMeterAction() {
        this(Bundle.getMessage("MenuItemAmpMeter"));
    }

    public AmpMeterAction(String s) {
        super(s);
        // disable ourself if no MultiMeter available
        if (jmri.InstanceManager.getNullableDefault(jmri.MultiMeter.class) == null) {
            setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        AmpMeterFrame f = new AmpMeterFrame();
        f.initComponents();
        f.setVisible(true);
    }

}
