package jmri;

import jmri.implementation.DefaultTransit;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for Transit class.
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2016
 **/
public class TransitTest {

    @Test
    public void testSysNameConstructor(){
        assertNotNull( new DefaultTransit("TT1"), "Constructor");
    }

    @Test
    public void testTwoNameStringConstructor(){
        assertNotNull( new DefaultTransit("TT1", "user name"), "Constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown(){
        JUnitUtil.tearDown();
    }

}
