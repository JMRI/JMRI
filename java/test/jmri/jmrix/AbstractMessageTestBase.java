package jmri.jmrix;

import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Base tests for messages implementing the jmri.jmrix.Message interface.
 *
 * @author Paul Bender Copyright (C) 2017	
 */
abstract public class AbstractMessageTestBase {

    protected AbstractMessage m = null; // set in setUp

    @Before
    abstract public void setUp();

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
