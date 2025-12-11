package jmri.jmrix.rfid.swing;

import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests for RfidMenu class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class RfidMenuTest {
        
    private RfidSystemConnectionMemo memo = null;

    @Test
    @DisabledIfHeadless
    public void testRfidMenuConstructor(){
        Assertions.assertNotNull(new RfidMenu(memo), "RfidMenu constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        memo = new RfidSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown(){
        memo=null;
        JUnitUtil.tearDown();
    }

}
