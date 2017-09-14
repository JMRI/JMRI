package jmri.jmrix.tams.swing.locodatabase;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.tams.TamsSystemConnectionMemo;
import jmri.jmrix.tams.TamsInterfaceScaffold;
import jmri.jmrix.tams.TamsTrafficController;

/**
 * Test simple functioning of LocoDataPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LocoDataPaneTest {

    private TamsSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        LocoDataPane action = new LocoDataPane();
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testInit() {
        // this test currently just makes sure we don't throw any exceptions
        // initializing the panel
        LocoDataPane action = new LocoDataPane();
        action.initComponents(memo);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        TamsTrafficController tc = new TamsInterfaceScaffold();
        memo = new TamsSystemConnectionMemo(tc);
    }

    @After
    public void tearDown() {
        memo = null;
        JUnitUtil.tearDown();    
    }
}
