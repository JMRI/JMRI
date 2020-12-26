package jmri.jmrit.vsdecoder.listener;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ListeningSpot
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ListeningSpotTest {

    @Test
    public void testCtor() {
        ListeningSpot s = new ListeningSpot();
        Assert.assertNotNull("exists", s);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
