package jmri.jmrix.xpa;

import java.util.ResourceBundle;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Provide the bare minimum required in a SystemConnectionMemo for the XPressNet
 * adapters.
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public class XpaSystemConnectionMemo extends SystemConnectionMemo {

    public XpaSystemConnectionMemo() {
        super("P", "XPA"); // Prefix from XpaTurnoutManager, UserName from XpaThrottleManager
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

}
