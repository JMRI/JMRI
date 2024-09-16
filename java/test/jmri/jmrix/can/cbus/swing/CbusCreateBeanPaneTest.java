package jmri.jmrix.can.cbus.swing;

import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JTable;

import jmri.NamedBean;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.jmrix.can.swing.CanPanel;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import org.netbeans.jemmy.operators.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple functioning of CbusCreateBeanPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusCreateBeanPaneTest  {

    @Test
    public void testInitComponents() throws Exception{
        // for now, just makes sure there isn't an exception.
        assertNotNull(new CbusCreateBeanPane(mainPane));
    }

    @Test
    public void testCreateBean() {
        // for now, just makes sure there isn't an exception.

        CbusCreateBeanPane t = new CbusCreateBeanPane(mainPane);

        JmriJFrame f = new JmriJFrame();
        f.add(t);
        f.setTitle("Test CBUS Create Bean");
        f.pack();
        f.setVisible(true);
        JFrameOperator jfo = new JFrameOperator( "Test CBUS Create Bean" );

        dm = new CbusEventTableDataModel(memo,0,0);
        dm.provideEvent(123, 456).setName("TestEvent1");
        dm.provideEvent(0, 7);
        JTable table = new JTable(dm);
        table.setName("jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel");
        CbusTableRowEventDnDHandler dh = new CbusTableRowEventDnDHandler(memo,table);
        table.setRowSelectionInterval(0, 0);
        Transferable trnfr = dh.createTransferable(table);

        Thread dialog_thread = new Thread(() -> {
            JDialogOperator jdo = new JDialogOperator( Bundle.getMessage("ReminderTitle") );
            new JCheckBoxOperator(jdo,Bundle.getMessage("HideFurtherDialog")).setSelected(true);
            new JButtonOperator(jdo,"OK").doClick();
        });
        dialog_thread.setName("Reminder Dialog Close Thread");
        dialog_thread.start();

        t.transferArray[0].importData(new JLabel("Turnout"), trnfr);

        JUnitUtil.waitFor(()->{return !(dialog_thread.isAlive());}, "Reminder Dialog closed");

        t.transferArray[0].importData(new JLabel("NotABeanType"), trnfr);

        assertNotNull(memo);
        NamedBean bean = memo.get(jmri.TurnoutManager.class).getBySystemName("MT+N123E456");
        NamedBean notAbean = memo.get(jmri.TurnoutManager.class).getBySystemName("NotABean");

        assertNotNull(bean);
        assertNull(notAbean);

        table.setRowSelectionInterval(1, 1);
        trnfr = dh.createTransferable(table);

        t.transferArray[2].importData(new JLabel("Light"), trnfr);
        t.transferArray[2].importData(new JLabel("Light"), trnfr);

        bean = memo.get(jmri.LightManager.class).getBySystemName("ML+7");
        notAbean = memo.get(jmri.LightManager.class).getBySystemName("NotABean");

        assertNotNull(bean);
        assertNull(notAbean);

        new JTextFieldOperator(jfo,0).setText("NewName");
        new JButtonOperator(jfo,0).doClick();
        assertEquals("NewName",bean.getUserName());

        dh.dispose();

        JUnitUtil.dispose(f);
        jfo.waitClosed();
    }

    private static class TestPane extends CanPanel{
    }

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis = null;

    private TestPane mainPane = null;
    private CbusEventTableDataModel dm;

    @BeforeEach
    public void setUp ( @TempDir File tempDir ) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir));
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
        memo.configureManagers();

        mainPane = new TestPane();
        mainPane.initComponents(memo);

    }

    @AfterEach
    public void tearDown() {
        assertNotNull(memo);
        memo.dispose();
        memo = null;
        assertNotNull(tcis);
        tcis.terminateThreads();
        tcis = null;
        assertNotNull(mainPane);
        mainPane.dispose();
        mainPane = null;

        if (dm!=null){
            dm.skipSaveOnDispose();
            dm.dispose();
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
