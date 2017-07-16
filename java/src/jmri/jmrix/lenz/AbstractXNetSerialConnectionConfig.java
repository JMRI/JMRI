package jmri.jmrix.lenz;

/**
 * Abstract Configuration for an XpressNet Serial Connection
 *
 * @author Paul Bender Copyright (C) 2010
 */
abstract public class AbstractXNetSerialConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public AbstractXNetSerialConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public AbstractXNetSerialConnectionConfig() {
        super();
    }

}
