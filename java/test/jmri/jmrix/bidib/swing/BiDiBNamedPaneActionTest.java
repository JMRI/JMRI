package jmri.jmrix.bidib.swing;

import java.awt.GraphicsEnvironment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrix.bidib.BiDiBInterfaceScaffold;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.jmrix.bidib.TestBiDiBTrafficController;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;

/**
 * Tests for the BiDiBNamedPaneAction class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBNamedPaneActionTest {
    
    BiDiBSystemConnectionMemo memo;
    
    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Nce Named Pane Test");
        BiDiBNamedPaneAction t = new BiDiBNamedPaneAction("Test Action", jf, "test", memo);
        Assert.assertNotNull("exists",t);
        jf.dispose();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
    }
    
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
