package jmri.configurexml;

import org.junit.Assert;
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
        Assert.assertNotNull(xmlAdapter);
    }

    abstract public void setUp();

    abstract public void tearDown();

}
