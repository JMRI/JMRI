package jmri.jmrix.anyma;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for AnymaDMX_ConnectionTypeList class.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class AnymaDMX_ConnectionTypeListTest {

    @Test
    public void ConstructorTest() {
        Assert.assertNotNull("ConnectionConfig constructor", new AnymaDMX_ConnectionTypeList());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
