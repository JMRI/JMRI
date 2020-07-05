package jmri.jmrix.can.cbus.swing;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.ConfigurationManager;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Provide access to Swing components for the Cbus subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * 
 * @since 2.99.2
 */
@API(status = EXPERIMENTAL)
public class CbusComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public CbusComponentFactory(CanSystemConnectionMemo memo) {
        this.memo = memo;
    }

    CanSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        if (memo.getProtocol().equals(ConfigurationManager.SPROGCBUS)) {
            return new SprogCbusMenu(memo);
        } else {
            return new CbusMenu(memo);
        }
    }

}
