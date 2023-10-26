package jmri.jmrix.dcc4pc.swing;

import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Tests for Dcc4PcMenu class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class Dcc4PcMenuTest {

    private Dcc4PcSystemConnectionMemo memo = null;

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testDcc4pcMenuConstructor(){
        Assertions.assertNotNull(new Dcc4PcMenu(memo),"Dcc4PcMenu constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Dcc4PcSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown(){
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo=null;
        JUnitUtil.tearDown();
    }

}
