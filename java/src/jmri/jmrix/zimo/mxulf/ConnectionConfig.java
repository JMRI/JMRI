// ConnectionConfig.java
package jmri.jmrix.zimo.mxulf;

import java.util.ResourceBundle;
import jmri.util.SystemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Definition of objects to handle configuring an Zimo MXULF SerialDriverAdapter
 * object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @version	$Revision: 18323 $
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    private final static Logger log = LoggerFactory.getLogger(ConnectionConfig.class);

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    protected void checkInitDone() {
        if (log.isDebugEnabled()) {
            log.debug("init called for " + name());
        }
        if (init) {
            return;
        }
        super.checkInitDone();
        connectionNameField.setText(name());
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.zimo.ZimoActionListBundle");
    }

    public String name() {
        return "MXULF";
    }

    protected void setInstance() {
        if (adapter == null) {
            adapter = new SerialDriverAdapter();
        }
    }

    protected String[] getPortFriendlyNames() {
        if (SystemType.isWindows()) {
            return new String[]{"MX31ZL", "ZIMO"};
        }
        return new String[]{};
    }
}
