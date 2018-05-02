package jmri.jmrix.dcc4pc.swing.boardlists;

import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import jmri.jmrix.dcc4pc.Dcc4PcTrafficController;
import jmri.jmrix.dcc4pc.Dcc4PcMessage;
import jmri.jmrix.dcc4pc.Dcc4PcListener;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of BoardListPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class BoardListPanelTest {

    @Test
    public void testCTor() {
        BoardListPanel action = new BoardListPanel();
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testInitComponents() {
        Dcc4PcTrafficController tc = new Dcc4PcTrafficController(){
           @Override
           public void sendDcc4PcMessage(Dcc4PcMessage m,Dcc4PcListener reply) {
           }
        };
        Dcc4PcSystemConnectionMemo scm = new Dcc4PcSystemConnectionMemo(tc);
        BoardListPanel action = new BoardListPanel();
        action.initComponents(scm);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
