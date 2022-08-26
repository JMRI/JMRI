package jmri.jmrix.can.cbus.swing;

import static org.assertj.core.api.Assertions.assertThat;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of CbusMenu
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class CbusMenuTest {

    private CanSystemConnectionMemo m = null;
 
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCtor() {
        CbusMenu action = new CbusMenu(m);
        assertThat(action).isNotNull();
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
