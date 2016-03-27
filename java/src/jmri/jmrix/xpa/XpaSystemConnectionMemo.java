package jmri.jmrix.xpa;

import java.util.ResourceBundle;
import jmri.jmrix.SystemConnectionMemo;
import jmri.InstanceManager;

/**
 * Provide the bare minimum required in a SystemConnectionMemo for the XPressNet
 * adapters.
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public class XpaSystemConnectionMemo extends SystemConnectionMemo {

    public XpaSystemConnectionMemo() {
        this("P", "XPA"); // Prefix from XpaTurnoutManager, UserName from XpaThrottleManager
    }

    public XpaSystemConnectionMemo(String prefix, String userName){
        super(prefix, userName); 
        register(); // registers general type
        InstanceManager.store(this,XpaSystemConnectionMemo.class); // also register as specific type
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

}
