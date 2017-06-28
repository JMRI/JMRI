package jmri.jmrix.rps.aligntable;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of AlignTableAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class AlignTableActionTest {

    @Test
    public void testStringMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AlignTableAction action = new AlignTableAction("RPS test Action");
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AlignTableAction action = new AlignTableAction();
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AlignTableAction action = new AlignTableAction("RPS test Action");
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testDefaultCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AlignTableAction action = new AlignTableAction();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
