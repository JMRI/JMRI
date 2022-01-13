package jmri.jmrix.ipocs;

import org.junit.jupiter.api.*;

import jmri.util.JUnitUtil;
import org.junit.Assert;

/**
 * Tests for IpocsConnectionTypeList class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.11.5
 */
public class IpocsConnectionTypeListTest {

    @Test
    public void ConstructorTest() {
        Assert.assertNotNull("constructor", new IpocsConnectionTypeList());
    }

    @Test
    public void GetManufacturersTest() {
        new IpocsConnectionTypeList().getManufacturers();
    }

    @Test
    public void GetAvailableProtocolClassesTest() {
        new IpocsConnectionTypeList().getAvailableProtocolClasses();
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
