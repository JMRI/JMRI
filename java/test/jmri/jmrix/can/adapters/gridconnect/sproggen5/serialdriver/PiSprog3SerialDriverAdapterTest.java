package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for Sprog3PlusSerialDriverAdapter class.
 *
 * @author Andrew Crosland (C) 2020
 **/
public class PiSprog3SerialDriverAdapterTest {

    @Test
    public void ConstructorTest(){
        PiSprog3SerialDriverAdapter a = new PiSprog3SerialDriverAdapter();
        Assert.assertNotNull("SerialDriverAdapter constructor", a);
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
