package jmri.jmrix.lenz.swing.liusb;

import java.awt.event.ActionEvent;
import jmri.jmrix.lenz.swing.AbstractXPressNetAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register an LIUSBConfigFrame object.
 * <p>
 * The {@link LIUSBConfigFrame} is a configuration tool.
 *
 * @author Paul Bender Copyright (C) 2009
 */
@API(status = EXPERIMENTAL)
public class LIUSBConfigAction extends AbstractXPressNetAction {

    public LIUSBConfigAction(String s, jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super(s,memo);
    }

    public LIUSBConfigAction(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this(Bundle.getMessage("MenuItemLIUSBConfigurationManager"), memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create an LIUSBConfigFrame
        LIUSBConfigFrame f = new LIUSBConfigFrame(_memo);
        f.setVisible(true);
    }

}
