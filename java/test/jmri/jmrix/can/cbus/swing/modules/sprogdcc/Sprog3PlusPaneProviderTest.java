package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.*;
import jmri.jmrix.can.cbus.swing.modules.*;
import jmri.jmrix.can.cbus.simulator.CbusDummyNode;
import jmri.jmrix.can.cbus.simulator.moduletypes.SprogPiSprog3Plus;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test for [Pi]-SPROG 3 (v2|Plus) pane provider
 *
 * @author Andrew Crosland Copyright (C) 2022
 */
public class Sprog3PlusPaneProviderTest {
    
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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

    private CanSystemConnectionMemo memo;
    private CbusNodeNVTableDataModel model;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        model = new CbusNodeNVTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
    }

    @AfterEach
    public void tearDown() {
        model.dispose();
        model = null;
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }
    
}
