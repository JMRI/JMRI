package jmri.jmrix.lenz;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class XNetAddressTest {

    @Test
    public void testCTor() {
        XNetAddress t = new XNetAddress();
        Assert.assertNotNull("exists", t);
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
