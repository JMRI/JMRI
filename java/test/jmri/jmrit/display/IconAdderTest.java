package jmri.jmrit.display;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of IconAdder
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class IconAdderTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        IconAdder frame = new IconAdder();
        Assert.assertNotNull("exists", frame );
    }

    @Test
    public void testBoolCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        IconAdder frame = new IconAdder(true);
        Assert.assertNotNull("exists", frame );
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
