package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.*;
import jmri.jmrix.can.cbus.swing.modules.*;
import jmri.jmrix.can.cbus.simulator.CbusDummyNode;
import jmri.jmrix.can.cbus.simulator.moduletypes.SprogPiSprog3;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of CbusNodeInfoPane
 *
 * @author Andrew Crosland Copyright (C) 2021
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
        CbusDummyNode node = new SprogPiSprog3().getNewDummyNode(memo, 65534);
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
