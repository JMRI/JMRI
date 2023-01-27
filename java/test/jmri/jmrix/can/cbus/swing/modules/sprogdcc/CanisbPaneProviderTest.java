package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test for [Pi]-SPROG 3 (v2|Plus) pane provider
 *
 * @author Andrew Crosland Copyright (C) 2022
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class CanisbPaneProviderTest {
    
    @Test
    public void testCtor() {
        CanisbPaneProviderTest t = new CanisbPaneProviderTest();
        Assertions.assertNotNull(t, "exists");
    }
    
    private CanSystemConnectionMemo memo = null;
    private CbusNodeNVTableDataModel model = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        model = new CbusNodeNVTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(model);
        model.dispose();
        model = null;
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }
    
}
