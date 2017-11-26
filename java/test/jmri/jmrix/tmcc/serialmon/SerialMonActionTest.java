package jmri.jmrix.tmcc.serialmon;

import jmri.jmrix.tmcc.TmccSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.tmcc.serialmon.SerialMonAction
 * class
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SerialMonActionTest {

    @Test
    public void testCTor() {
        SerialMonAction t = new SerialMonAction("Monitor", new TmccSystemConnectionMemo("T", "TMCC Test"));
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

    // private final static Logger log = LoggerFactory.getLogger(SerialMonActionTest.class);

}
