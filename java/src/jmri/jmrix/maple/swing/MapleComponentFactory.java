package jmri.jmrix.maple.swing;

import jmri.jmrix.maple.MapleSystemConnectionMemo;
import jmri.jmrix.maple.MapleMenu;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Provide access to Swing components for the Maple subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2017
 * @since 4.9.6
 */
@API(status = EXPERIMENTAL)
public class MapleComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public MapleComponentFactory(MapleSystemConnectionMemo memo) {
        this.memo = memo;
    }

    MapleSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection.
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new MapleMenu(memo);
    }
}

