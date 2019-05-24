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
 *
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    private final static Logger log = LoggerFactory.getLogger(ConnectionConfig.class);

    /**
     * Create a connection with a known serial port.
     *
     * @param p the serial port
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Create a connection without a known serial port.
     */
    public ConnectionConfig() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkInitDone() {
        log.debug("init called for {}", name());
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

    @Override
    public String name() {
        return "MXULF"; // NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new SerialDriverAdapter();
        }
    }

    @Override
    protected String[] getPortFriendlyNames() {
        if (SystemType.isWindows()) {
            return new String[]{"MX31ZL", "ZIMO"};
        }
        return new String[]{};
    }

}
