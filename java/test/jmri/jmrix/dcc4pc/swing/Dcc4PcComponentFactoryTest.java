package jmri.jmrix.dcc4pc.swing;

import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Tests for Dcc4PcComponentFactory class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class Dcc4PcComponentFactoryTest {

    private Dcc4PcSystemConnectionMemo memo = null;

    @Test
    public void testDccPcCfConstructor(){
       Assertions.assertNotNull(new Dcc4PcComponentFactory(memo),"Dcc4PcComponentFactory constructor");
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true" )
    public void getMenu(){
       Dcc4PcComponentFactory zcf = new Dcc4PcComponentFactory(memo);
       Assertions.assertNotNull(zcf.getMenu(),"Component Factory getMenu method");
    }

    @Test
    public void getMenuDisabled(){
       memo.setDisabled(true);
       Dcc4PcComponentFactory zcf = new Dcc4PcComponentFactory(memo);
       Assertions.assertNull(zcf.getMenu(),"Disabled Component Factory getMenu method");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
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
