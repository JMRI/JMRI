package jmri.jmrix.can.cbus.swing.modules.base;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.*;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test simple functioning of Servo8BasePaneProvider
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class Servo8BasePaneProviderTest {
    
    @org.junit.jupiter.api.Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Servo8BasePaneProvider t = new Servo8BasePaneProvider();
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
