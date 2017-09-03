package jmri.jmrix.can.adapters.gridconnect;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class GcSerialDriverAdapterTest {

    @Test
    public void testCTor() {
        GcSerialDriverAdapter t = new GcSerialDriverAdapter();
        Assert.assertNotNull("exists",t);
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

    // private final static Logger log = LoggerFactory.getLogger(GcSerialDriverAdapterTest.class);

}
