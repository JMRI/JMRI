package jmri.jmrit.withrottle;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of TrackPowerController
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class TrackPowerControllerTest {

    @Test
    public void testCtor() {
        TrackPowerController panel = new TrackPowerController();
        Assert.assertNotNull("exists", panel );
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

    }
    
    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();

    }
}
