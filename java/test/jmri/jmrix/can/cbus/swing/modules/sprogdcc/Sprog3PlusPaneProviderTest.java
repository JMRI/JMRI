package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.*;
import jmri.jmrix.can.cbus.swing.modules.*;
import jmri.jmrix.can.cbus.simulator.CbusDummyNode;
import jmri.jmrix.can.cbus.simulator.moduletypes.SprogPiSprog3Plus;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test for [Pi]-SPROG 3 (v2|Plus) pane provider
 *
 * @author Andrew Crosland Copyright (C) 2022
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class Sprog3PlusPaneProviderTest {
    
    @Test
    public void testCtor() {
        Sprog3PlusPaneProvider t = new Sprog3PlusPaneProvider();
        Assertions.assertNotNull(t, "exists");
    }
    
    @Test
    public void testPaneFound() {
        CbusDummyNode node = new SprogPiSprog3Plus().getNewDummyNode(memo, 65534);
        CbusConfigPaneProvider t = CbusConfigPaneProvider.getProviderByNode(node);

        Assertions.assertNotNull(t);
        Assertions.assertFalse(t instanceof UnknownPaneProvider,"Not Unknown");
        Assertions.assertTrue(t instanceof Sprog3PlusPaneProvider,"found Sprog3PlusPaneProvider");

        node.dispose();
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
