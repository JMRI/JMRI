// AbstractXNetSerialConnectionConfig.java
package jmri.jmrix.dccpp;

/**
 * Abstract Configuration for a DCC++ Serial Connection
 * <P>
 *
 * @author Mark Underwood Copyright (C) 2015
 * @version	$Revision$
 *
 * Based on AbstractXNetSerialConnectionConfig by Paul Bender
 */
abstract public class AbstractDCCppSerialConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public AbstractDCCppSerialConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public AbstractDCCppSerialConnectionConfig() {
        super();
    }

}
