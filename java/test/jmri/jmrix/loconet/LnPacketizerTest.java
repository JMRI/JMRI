package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2018
 */
public class LnPacketizerTest {

    protected LnPacketizer lnp;

    @Test
    public void testCtor() {
       Assert.assertNotNull("exists", lnp );
    }

    @Test
    public void testStatusWithoutInit() {
       Assert.assertFalse("not connected", lnp.status() );
    }

    @Test
    public void testStartThreads() {
       // for now, just make sure there isn't an exception.
       // the port hasn't been connected yet.
       lnp.startThreads();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        lnp = new LnPacketizer();
    }

    @After
    public void tearDown() {
        lnp = null;
        JUnitUtil.tearDown();
    }
}
