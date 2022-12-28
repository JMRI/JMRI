package jmri.jmrix.ipocs;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for IpocsConnectionTypeList class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.11.5
 */
public class IpocsConnectionTypeListTest {

    @Test
    public void testCtor() {
        Assertions.assertNotNull( new IpocsConnectionTypeList(),"constructor");
    }

    @Test
    public void testGetManufacturers() {
        Assertions.assertNotNull(new IpocsConnectionTypeList().getManufacturers());
    }

    @Test
    public void testGetAvailableProtocolClasses() {
        Assertions.assertNotNull(new IpocsConnectionTypeList().getAvailableProtocolClasses());
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
