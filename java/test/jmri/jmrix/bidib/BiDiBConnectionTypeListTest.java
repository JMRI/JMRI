package jmri.jmrix.bidib;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the BiDiBConnectionTypeList class
 *
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBConnectionTypeListTest {
    
    @Test
    public void testCtor() {

        BiDiBConnectionTypeList c = new BiDiBConnectionTypeList();
        Assertions.assertNotNull(c);
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
