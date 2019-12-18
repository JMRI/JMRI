package jmri.jmrix.zimo.mx1;

import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Definition of objects to handle configuring an LocoBuffer layout connection
 * via a Zimo MX-1 SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    private final static Logger log = LoggerFactory.getLogger(ConnectionConfig.class);
    
    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     *
     * @param p the associated serial port
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
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
        return "MX-1"; // NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("deprecation") // until MX1 is migrated to multiple systems
    protected void setInstance() {
        adapter = Mx1Adapter.instance();
    }

}
