package jmri.jmrix.can.cbus.swing;

import java.io.File;
import javax.swing.JFrame;

import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.jmrix.can.cbus.swing.eventtable.CbusEventTablePane;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple functioning of CbusNewEventPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusNewEventPaneTest  {

    @Test
    public void testCbusNewEventPaneCtor() {
        CbusNewEventPane t = new CbusNewEventPane(mainPanel);
        assertNotNull(t);
    }

    @Test
    public void testPersistenceEvNumNodeNum() {

        CbusNewEventPane t = new CbusNewEventPane(mainPanel);
        JFrame f = new JFrame(t.getClass().getName());
        f.add(t);
        jmri.util.ThreadingUtil.runOnGUI( () -> {
            f.pack();
            f.setVisible(true);
        });
        JFrameOperator jfo = new JFrameOperator( t.getClass().getName() );

        assertEquals( "0", new JTextFieldOperator(jfo,0).getText());
        assertEquals( "1", new JTextFieldOperator(jfo,1).getText());

        new JTextFieldOperator(jfo,0).setText("123"); // nn
        new JTextFieldOperator(jfo,1).setText("456"); // en
        f.dispose();
        jfo.dispose();

        CbusNewEventPane tt = new CbusNewEventPane(mainPanel);
        JFrame ff = new JFrame(tt.getClass().getName());
        ff.add(tt);
        jmri.util.ThreadingUtil.runOnGUI( () -> {
            ff.pack();
            ff.setVisible(true);
        });
        JFrameOperator jffo = new JFrameOperator( tt.getClass().getName() );

        assertEquals( "123", new JTextFieldOperator(jffo,0).getText());
        assertEquals( "456", new JTextFieldOperator(jffo,1).getText());

        JUnitUtil.dispose( jfo.getWindow() );
        JUnitUtil.dispose( jffo.getWindow() );
        jfo.waitClosed();
        jffo.waitClosed();
    }

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tc = null;
    private CbusEventTablePane mainPanel = null;

    @BeforeEach
    public void setUp(@TempDir File tempDir) {
        JUnitUtil.setUp();
        try {
            JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir));
        } catch ( java.io.IOException e) {
            fail("Exception creating temp. user folder");
        }

        memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
        memo.configureManagers();
        mainPanel = new CbusEventTablePane();
        mainPanel.initComponents(memo);
        mainPanel.getMenus();

    }

    @AfterEach
    public void tearDown() {
        CbusEventTableDataModel dm = InstanceManager.getNullableDefault(CbusEventTableDataModel.class);
        if ( dm !=null ){
            dm.skipSaveOnDispose();
            dm.dispose();
        }

        assertNotNull(mainPanel);
        mainPanel.dispose();
        assertNotNull(tc);
        tc.terminateThreads();
        assertNotNull(memo);
        memo.dispose();
        tc = null;
        memo = null;

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
