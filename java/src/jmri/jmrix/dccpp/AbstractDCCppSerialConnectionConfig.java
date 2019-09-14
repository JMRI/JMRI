package jmri.jmrix.dccpp;

/**
 * Abstract Configuration for a DCC++ Serial Connection
 * <p>
 *
 * @author Mark Underwood Copyright (C) 2015
  *
 * Based on AbstractXNetSerialConnectionConfig by Paul Bender
 */
public abstract class AbstractDCCppSerialConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public AbstractDCCppSerialConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public AbstractDCCppSerialConnectionConfig() {
        super();
    }

}
