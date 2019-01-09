package jmri.jmrix.can.cbus.simulator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Steve Young Copyright (c) 2019
 */
public class CbusDummyCSSessionTest {

    @Test
    public void testCTor() {
        CbusDummyCSSession t = new CbusDummyCSSession(null,0,0,false);
        Assert.assertNotNull("exists",t);
        t.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusDummyCSTest.class);

}
