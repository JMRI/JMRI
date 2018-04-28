package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
