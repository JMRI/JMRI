package jmri.jmrix.ipocs;

import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

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

    @BeforeEach        
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

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
}
