package jmri.jmrix.configurexml;

import org.junit.*;

/**
 * Base tests for ConnectionConfigXml objects.
 *
 * @author Paul Bender Copyright (C) 2018	
 */
abstract public class AbstractConnectionConfigXmlTestBase extends jmri.configurexml.AbstractXmlAdapterTestBase {

    @Test
    public void getInstanceTest() {
        ((AbstractConnectionConfigXml)xmlAdapter).getInstance();
    }

}
