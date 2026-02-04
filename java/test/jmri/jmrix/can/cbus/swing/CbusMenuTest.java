package jmri.jmrix.can.cbus.swing;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of CbusMenu
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class CbusMenuTest {

    private CanSystemConnectionMemo m = null;
 
    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testCtor() {
        CbusMenu action = new CbusMenu(m);
        Assertions.assertNotNull(action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        m = new CanSystemConnectionMemo();
        m.setSystemPrefix("ABC");

    }

    @AfterEach
    public void tearDown() { 
        Assertions.assertNotNull(m);
        m.dispose();
        m = null;
        JUnitUtil.tearDown();

    }
}
