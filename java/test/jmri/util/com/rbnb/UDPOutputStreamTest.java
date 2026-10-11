package jmri.util.com.rbnb;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the UDPOutputStream class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class UDPOutputStreamTest  {

    @Test
    public void testCtor(){
        Assertions.assertNotNull(new UDPOutputStream());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
