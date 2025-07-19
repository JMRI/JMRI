package jmri.jmrix.bidib;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the BiDiBConnectionTypeList class
 *
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBConstantsTest {

    @Test
    public void testBiDiBConstantsTest(){
        Assertions.assertEquals("localhost", BiDiBConstants.BIDIB_OVER_TCP_DEFAULT_HOST);
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
