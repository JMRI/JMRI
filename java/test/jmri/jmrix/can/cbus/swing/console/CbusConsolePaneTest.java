package jmri.jmrix.can.cbus.swing.console;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CbusConsolePane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CbusConsolePaneTest {

    jmri.jmrix.can.CanSystemConnectionMemo memo = null;
    jmri.jmrix.can.TrafficController tc = null;

    @Test
    public void testCtor() {
        CbusConsolePane pane = new CbusConsolePane();
        Assert.assertNotNull("exists", pane);
    }

    @Test
    public void testInitComponents() throws Exception{
        CbusConsolePane pane = new CbusConsolePane();
        // for now, just makes ure there isn't an exception.
        pane.initComponents(memo);
    }

    @Test
    public void testInitContext() throws Exception {
        CbusConsolePane pane = new CbusConsolePane();
        // for now, just makes ure there isn't an exception.
        pane.initContext(memo);
    }

    @Test
    public void testGetHelpTarget(){
        CbusConsolePane pane = new CbusConsolePane();
        Assert.assertEquals("help target","package.jmri.jmrix.can.cbus.swing.console.CbusConsoleFrame",pane.getHelpTarget());
    }

    @Test
    public void testGetTitle(){
        CbusConsolePane pane = new CbusConsolePane();
        Assert.assertEquals("title","CBUS Console",pane.getTitle());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        tc = new jmri.jmrix.can.TrafficControllerScaffold();
        memo.setTrafficController(tc);
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
