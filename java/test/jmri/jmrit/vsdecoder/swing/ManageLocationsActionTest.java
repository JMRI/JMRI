package jmri.jmrit.vsdecoder.swing;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ManageLocationsAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ManageLocationsActionTest {

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ManageLocationsAction action = new ManageLocationsAction("Test VSD Manage Locations Action","");
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
