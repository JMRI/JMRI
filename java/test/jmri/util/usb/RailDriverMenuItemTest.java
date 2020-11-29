package jmri.util.usb;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RailDriverMenuItemTest {

    @Test
    public void testCTor() {
        RailDriverMenuItem t = new RailDriverMenuItem();
        Assert.assertNotNull("exists",t);
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
