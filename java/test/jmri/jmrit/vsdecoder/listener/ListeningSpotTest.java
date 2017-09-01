package jmri.jmrit.vsdecoder.listener;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
