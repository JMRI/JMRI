package jmri.jmrix.can.cbus.swing.eventtable;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of CbusEventTableDataModel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CbusEventTableDataModelTest {
        
 
    jmri.jmrix.can.TrafficControllerScaffold tcs = null;
    jmri.jmrix.can.CanSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusEventTableDataModel model = new CbusEventTableDataModel(memo,5,5);
        Assert.assertNotNull("exists", model);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        tcs = new jmri.jmrix.can.TrafficControllerScaffold();
        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        memo.setTrafficController(tcs);

    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }


}
