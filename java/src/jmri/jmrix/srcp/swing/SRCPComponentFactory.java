package jmri.jmrix.srcp.swing;

import jmri.jmrix.srcp.SRCPSystemConnectionMemo;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Provide access to Swing components for the SRCP subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2010,2016
 * @since 4.5.1
 */
@API(status = EXPERIMENTAL)
public class SRCPComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public SRCPComponentFactory(SRCPSystemConnectionMemo memo) {
        this.memo = memo;
    }

    SRCPSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new SystemMenu(memo);
    }
}

