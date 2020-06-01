package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import jmri.util.JUnitUtil;
import org.junit.*;

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

    @Before
    public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown(){
        JUnitUtil.tearDown();
    }

}
