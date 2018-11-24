package jmri.jmrit.withrottle;

import org.junit.*;

/**
 * Test simple functioning of TrackPowerController
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class TrackPowerControllerTest {

    @Test
    public void testCtor() {
        TrackPowerController panel = new TrackPowerController();
        Assert.assertNotNull("exists", panel );
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
