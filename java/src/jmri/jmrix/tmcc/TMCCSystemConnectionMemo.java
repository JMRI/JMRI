package jmri.jmrix.tmcc;

import java.util.ResourceBundle;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Provide the minimal required SystemConnectionMemo.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class TMCCSystemConnectionMemo extends SystemConnectionMemo {

    public TMCCSystemConnectionMemo() {
        super("T", "Lionel TMCC"); // Prefix from SerialTurnoutManager, UserName from SerialThrottleManager
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

}
