package jmri.jmrix.lenz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class XNetAddressTest {

    // no Ctor test, tested class only supplies static methods

    @Test
    public void testXNetAddressConstants() {
        assertEquals( 1024, XNetAddress.MAXSENSORADDRESS);
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
