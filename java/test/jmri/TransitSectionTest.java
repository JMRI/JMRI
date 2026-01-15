package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for TransitSection class.
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2016
 **/
public class TransitSectionTest {

    @Test
    public void testSysNameConstructor(){
        assertNotNull( new TransitSection("TS1",0,0,false), "Delayed");
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
