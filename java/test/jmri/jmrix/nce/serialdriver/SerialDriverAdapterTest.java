package jmri.jmrix.nce.serialdriver;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SerialDriverAdapter class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class SerialDriverAdapterTest {

    @Test
    public void testNceSerialDriverAdapterConstructor(){
        Assertions.assertNotNull( new SerialDriverAdapter(), "SerialDriverAdapter constructor");
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
