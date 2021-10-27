package jmri.jmrix.can.cbus.swing.modules.merg;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test simple functioning of CanmiosvoEditNVPane
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CanmiosvoEditNVPaneTest {
    
    @org.junit.jupiter.api.Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusNode nd = new CbusNode(memo, 12345);
        int [] nvs = new int[] {1, 1};
        nd.getNodeNvManager().setNVs(nvs);
        CanmiosvoEditNVPane t = new CanmiosvoEditNVPane(model, nd);
        Assert.assertNotNull("exists",t);
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
