package jmri.jmrix.tams.swing.locodatabase;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.tams.TamsSystemConnectionMemo;

/**
 * Test simple functioning of LocoDataPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LocoDataPaneTest {


    @Test
    public void testCtor() {
        LocoDataPane action = new LocoDataPane();
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testInit() {
        // this test currently just makes sure we don't throw any exceptions
        // initializing the panel
        TamsSystemConnectionMemo memo = new TamsSystemConnectionMemo();
        LocoDataPane action = new LocoDataPane();
        action.initComponents(memo);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
