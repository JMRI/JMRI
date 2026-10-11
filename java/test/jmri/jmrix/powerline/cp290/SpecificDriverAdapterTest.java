package jmri.jmrix.powerline.cp290;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SpecificDriverAdapter class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class SpecificDriverAdapterTest {

    @Test
    public void testCp290SpecificDriverAdapterConstructor(){
        Assertions.assertNotNull( new SpecificDriverAdapter(), "SpecificDriverAdapter constructor");
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
