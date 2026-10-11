package jmri.util.com.rbnb;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the UDPInputStream class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class UDPInputStreamTest  {

    @Test
    public void testCtor(){
        Assertions.assertNotNull(new UDPInputStream());
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
