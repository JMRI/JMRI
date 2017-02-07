package jmri.managers;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ManagerDefaultSelector
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ManagerDefaultSelectorTest {

    @Test
    public void testCtor() {
        ManagerDefaultSelector mds = new ManagerDefaultSelector();
        Assert.assertNotNull("exists", mds);
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
