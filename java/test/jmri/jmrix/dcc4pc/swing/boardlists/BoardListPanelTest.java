package jmri.jmrix.dcc4pc.swing.boardlists;

import jmri.jmrix.dcc4pc.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of BoardListPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class BoardListPanelTest {

    @Test
    public void testCTor() {
        BoardListPanel action = new BoardListPanel();
        Assertions.assertNotNull(action, "exists");
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
        Assertions.assertNotNull(action, "exists");

        tc.terminateThreads();
        scm.dispose();
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testDisplayTable() {

        Dcc4PcTrafficController tc = new Dcc4PcTrafficController(){
           @Override
           public void sendDcc4PcMessage(Dcc4PcMessage m,Dcc4PcListener reply) {
           }
        };
        Dcc4PcSystemConnectionMemo scm = new Dcc4PcSystemConnectionMemo(tc){
            @Override
            public Dcc4PcSensorManager getSensorManager() {
                return senMan;
            }
        };

        senMan = new Dcc4PcSensorManagerImpl(tc,scm);
        senMan.addAnActiveBoard(0, "1", 16, 0);

        senMan.provideSensor("DS0:1");
        senMan.provideSensor("DS0:2");
        senMan.provideSensor("DS0:3");
        senMan.provideSensor("DS0:4");

        javax.swing.JFrame f = new javax.swing.JFrame("Frame for BoardListPanel");
        BoardListPanel t = new BoardListPanel();
        t.initComponents(scm);
        f.add(t);
        f.pack();
        f.setVisible(true);

        JFrameOperator jfo = new JFrameOperator("Frame for BoardListPanel");
        Assertions.assertNotNull(   jfo);

        jfo.requestClose();
        jfo.waitClosed();

        senMan.dispose();
        tc.terminateThreads();
        scm.dispose();

    }

    private static class Dcc4PcSensorManagerImpl extends Dcc4PcSensorManager {
        Dcc4PcSensorManagerImpl(Dcc4PcTrafficController tc, Dcc4PcSystemConnectionMemo scm){
            super(tc, scm);
        }
        void addAnActiveBoard(int address, String version, int inputs, int encoding){
            addActiveBoard(address, version, inputs, encoding);
        }
    }

    private Dcc4PcSensorManagerImpl senMan;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
