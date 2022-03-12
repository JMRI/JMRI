package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for Sprog3PlusSerialDriverAdapter class.
 *
 * @author Andrew Crosland (C) 2021
 **/
public class PiSprog3v2SerialDriverAdapterTest {

    @Test
    public void ConstructorTest() {
        PiSprog3v2SerialDriverAdapter a = new PiSprog3v2SerialDriverAdapter();
        Assert.assertNotNull("SerialDriverAdapter constructor",a);
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
