package jmri.jmrix.maple;

import java.util.ResourceBundle;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Minimum required SystemConnectionMemo.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class MapleSystemConnectionMemo extends SystemConnectionMemo {

    public MapleSystemConnectionMemo() {
        super("K", "Maple");
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

}
