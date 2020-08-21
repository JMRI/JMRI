package jmri.jmrix.dcc4pc.swing.boardlists;

import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import jmri.jmrix.dcc4pc.Dcc4PcTrafficController;
import jmri.jmrix.dcc4pc.Dcc4PcMessage;
import jmri.jmrix.dcc4pc.Dcc4PcListener;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of BoardListPanel
 *
 * @author Paul Bender Copyright (C) 2016
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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
