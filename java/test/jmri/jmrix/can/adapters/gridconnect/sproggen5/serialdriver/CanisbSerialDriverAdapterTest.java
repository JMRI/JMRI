package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for CanisbSerialDriverAdapter class.
 *
 * @author Andrew Crosland (C) 2020
 **/
public class CanisbSerialDriverAdapterTest {

    @Test
    public void ConstructorTest(){
        CanisbSerialDriverAdapter c = new CanisbSerialDriverAdapter();
        Assert.assertNotNull("SerialDriverAdapter constructor", c);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown(){
        JUnitUtil.tearDown();
    }

}
