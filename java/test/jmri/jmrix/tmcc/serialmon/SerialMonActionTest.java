package jmri.jmrix.tmcc.serialmon;

import jmri.jmrix.tmcc.TmccSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialMonActionTest.class);

}
