package jmri.jmrix.rfid.swing;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests for RfidComponentFactory class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class RfidComponentFactoryTest {
        
    private RfidSystemConnectionMemo memo = null;

    @Test
    public void testMemoCtorRfidComponentFactory(){
        assertNotNull(new RfidComponentFactory(memo), "RfidComponentFactory constructor");
    }

    @Test
    @DisabledIfHeadless
    public void testGetMenu(){
        RfidComponentFactory zcf = new RfidComponentFactory(memo);
        assertNotNull( zcf.getMenu(), "Component Factory getMenu method");
    }

    @Test
    public void testGetMenuDisabled(){
        memo.setDisabled(true);
        RfidComponentFactory zcf = new RfidComponentFactory(memo);
        assertNull( zcf.getMenu(), "Disabled Component Factory getMenu method");
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
