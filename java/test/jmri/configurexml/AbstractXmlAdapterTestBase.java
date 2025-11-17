package jmri.configurexml;

import org.junit.jupiter.api.*;

/**
 * Base class for tests of classes inheriting and implementing AbstractXmlAdapter
 *
 * @author Paul Bender Copyright (C) 2018 
 */
abstract public class AbstractXmlAdapterTestBase {

    protected AbstractXmlAdapter xmlAdapter = null;

    @Test
    public void testCtor() {
        Assertions.assertNotNull(xmlAdapter);
    }

    abstract public void setUp();

    abstract public void tearDown();

}
