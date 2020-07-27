package jmri.jmrix.can.cbus.swing;

import static org.assertj.core.api.Assertions.assertThat;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.Test;

/**
 * Test simple functioning of CbusMenu
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class CbusMenuTest {


    // private TrafficController tc = null;
    private CanSystemConnectionMemo m;
 
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCtor() {
        CbusMenu action = new CbusMenu(m);
        assertThat(action).isNotNull();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // tc = new TestTrafficController();
        m = new CanSystemConnectionMemo();
        m.setSystemPrefix("ABC");

    }

    @AfterEach
    public void tearDown() { 
        m.dispose();
        m = null;
        JUnitUtil.tearDown();
        // tc = null;
    }
}
