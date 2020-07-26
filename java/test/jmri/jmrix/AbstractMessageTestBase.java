package jmri.jmrix;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Base tests for messages implementing the jmri.jmrix.Message interface.
 *
 * @author Paul Bender Copyright (C) 2017
 */
abstract public class AbstractMessageTestBase {

    protected AbstractMessage m = null; // set in setUp

    @BeforeEach
    public void setUp() { // minimum needed, usually overridden
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() { // minimum needed, usually overridden
        jmri.util.JUnitUtil.tearDown();
    }
    
    @Test
    public void testCtor() {
        Assert.assertNotNull("exists",m);
    }

    @Test
    public void testToString() {
        Assert.assertNotNull("toString has result",m.toString());
    }

    @Test
    public void testToMonitorString() {
        Assert.assertNotNull("toMonitorString has result",m.toMonitorString());
    }

}
