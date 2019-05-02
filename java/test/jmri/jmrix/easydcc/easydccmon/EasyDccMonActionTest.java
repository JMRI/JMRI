package jmri.jmrix.easydcc.easydccmon;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;

/**
 * JUnit tests for the EasyDccProgrammer class
 *
 * @author	Bob Jacobsen
 */
public class EasyDccMonActionTest {

    @Test
    public void testCreate() {
        EasyDccMonAction a = new EasyDccMonAction("Monitor", new EasyDccSystemConnectionMemo("E", "EasyDCC via Serial"));
        Assert.assertNotNull("exists", a);
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
