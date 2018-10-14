package jmri.jmrix.secsi.serialmon;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;

/**
 * Test simple functioning of SerialMonAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialMonActionTest {

    private SecsiSystemConnectionMemo memo = null;

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SerialMonAction action = new SerialMonAction("Secsi test Action",memo);
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SerialMonAction action = new SerialMonAction(memo);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new SecsiSystemConnectionMemo();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
