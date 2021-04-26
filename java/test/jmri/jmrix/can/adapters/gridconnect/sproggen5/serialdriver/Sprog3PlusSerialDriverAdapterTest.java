package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for Sprog3PlusSerialDriverAdapter class.
 *
 * @author Andrew Crosland (C) 2020
 **/
public class Sprog3PlusSerialDriverAdapterTest {

    @Test
    public void ConstructorTest() {
        Sprog3PlusSerialDriverAdapter a = new Sprog3PlusSerialDriverAdapter();
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
