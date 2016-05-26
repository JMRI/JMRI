package jmri.jmrix.rps;

import java.util.ResourceBundle;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Minimal implementation of SystemConnectionMemo.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class RpsSystemConnectionMemo extends SystemConnectionMemo {

    public RpsSystemConnectionMemo() {
        super("R", "RPS");
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

}
