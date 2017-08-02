package jmri.jmrit.vsdecoder.listener;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ListeningSpot
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ListeningSpotTest {

    @Test
    public void testCtor() {
        ListeningSpot s = new ListeningSpot();
        Assert.assertNotNull("exists", s);
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
