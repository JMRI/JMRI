package jmri.jmrix.can.cbus.swing.eventtable;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import org.netbeans.jemmy.operators.*;


/**
 * Test simple functioning of CbusEventTablePane.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusEventTablePaneTest extends jmri.util.swing.JmriPanelTest {

    @Test
    public void testInitComp() {

        ((CbusEventTablePane)panel).initComponents(memo);

        assertNotNull(panel);
        assertEquals("CAN " + Bundle.getMessage("EventTableTitle"),panel.getTitle());


        // check pane has loaded something
        initFrame();

        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( panel.getTitle() );

        // new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        assertTrue(getNewEventButtonEnabled(jfo));

        new JButtonOperator(jfo, Bundle.getMessage("NewEvent")).doClick();  // NOI18N

        assertFalse(getNewEventButtonEnabled(jfo));

        new JTextFieldOperator(jfo,1).typeText("1");
        assertTrue(getNewEventButtonEnabled(jfo));

        // new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        assertFalse(getClearFilterButtonEnabled(jfo));
        new JTextFieldOperator(jfo,0).typeText("1");
        // new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        assertTrue(getClearFilterButtonEnabled(jfo));

        new JButtonOperator(jfo, Bundle.getMessage("ClearFilter")).doClick();  // NOI18N
        // new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        assertFalse(getClearFilterButtonEnabled(jfo));

        jfo.requestClose();
    }

    @Test
    public void testMenuViewPanes(){

        ((CbusEventTablePane) panel).initComponents(memo);

        initFrame();
        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( panel.getTitle() );
        JMenuBarOperator mainbar = new JMenuBarOperator(jfo);

        // hide pane
        mainbar.pushMenu(Bundle.getMessage("Display")); // stops at top level
        JMenuOperator jmo = new JMenuOperator(mainbar, Bundle.getMessage("Display"));
        new JMenuItemOperator(new JPopupMenuOperator(jmo.getPopupMenu()),Bundle.getMessage("FilterSurround")).push();

        // hide pane
        mainbar.pushMenu(Bundle.getMessage("Display")); // stops at top level
        jmo = new JMenuOperator(mainbar, Bundle.getMessage("Display"));
        new JMenuItemOperator(new JPopupMenuOperator(jmo.getPopupMenu()),Bundle.getMessage("NewEvent")).push();


        // show pane
        mainbar.pushMenu(Bundle.getMessage("Display")); // stops at top level
        jmo = new JMenuOperator(mainbar, Bundle.getMessage("Display"));
        new JMenuItemOperator(new JPopupMenuOperator(jmo.getPopupMenu()),Bundle.getMessage("NewTsl")).push();
        assertFalse(getEditBeanButtonEnabled(jfo));
        mainbar.pushMenu(Bundle.getMessage("Display")); // stops at top level
        jmo = new JMenuOperator(mainbar, Bundle.getMessage("Display"));
        new JMenuItemOperator(new JPopupMenuOperator(jmo.getPopupMenu()),Bundle.getMessage("NewTsl")).push();

        // show pane
        mainbar.pushMenu(Bundle.getMessage("Display")); // stops at top level
        jmo = new JMenuOperator(mainbar, Bundle.getMessage("Display"));
        new JMenuItemOperator(new JPopupMenuOperator(jmo.getPopupMenu()),Bundle.getMessage("ButtonSendEvent")).push();
        assertTrue(getSendEventButtonEnabled(jfo));

        jfo.requestClose();

    }

    @Test
    public void testCellValues(){

        ((CbusEventTablePane) panel).initComponents(memo);
        initFrame();
        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( panel.getTitle() );

        ((CbusEventTablePane) panel).eventModel.provideEvent(0, 123);
        ((CbusEventTablePane) panel).eventModel.provideEvent(456, 789);

        JTableOperator jTableOperator = new JTableOperator(jfo);
        jTableOperator.waitCell("", 0, 0);
        jTableOperator.waitCell("123", 0, 1);
        jTableOperator.waitCell("456", 1, 0);
        jTableOperator.waitCell("789", 1, 1);

        jfo.requestClose();

    }

    @Test
    public void testCellTsLValues(){

        jmri.LightManager lm = memo.get(jmri.LightManager.class);
        jmri.SensorManager sm = memo.get(jmri.SensorManager.class);
        jmri.TurnoutManager tm = memo.get(jmri.TurnoutManager.class);

        ((CbusEventTablePane) panel).initComponents(memo);
        initFrame();
        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( panel.getTitle() );

        // hide panes
        JMenuBarOperator mainbar = new JMenuBarOperator(jfo);
        mainbar.pushMenu(Bundle.getMessage("evColMenuName")); // stops at top level
        JMenuOperator jmo = new JMenuOperator(mainbar, Bundle.getMessage("evColMenuName"));
        new JMenuItemOperator(new JPopupMenuOperator(jmo.getPopupMenu()),Bundle.getMessage("ColumnName")).push();

        mainbar.pushMenu(Bundle.getMessage("buttonCols")); // stops at top level
        jmo = new JMenuOperator(mainbar, Bundle.getMessage("buttonCols"));
        new JMenuItemOperator(new JPopupMenuOperator(jmo.getPopupMenu()),Bundle.getMessage("ColumnToggle")).push();

        mainbar.pushMenu(Bundle.getMessage("evColMenuName")); // stops at top level
        jmo = new JMenuOperator(mainbar, Bundle.getMessage("evColMenuName"));
        new JMenuItemOperator(new JPopupMenuOperator(jmo.getPopupMenu()),Bundle.getMessage("ColumnComment")).push();

        mainbar.pushMenu(Bundle.getMessage("latestEvCols")); // stops at top level
        jmo = new JMenuOperator(mainbar, Bundle.getMessage("latestEvCols"));
        new JMenuItemOperator(new JPopupMenuOperator(jmo.getPopupMenu()),Bundle.getMessage("CbusEventOnOrOff")).push();

        lm.provide("ML+12").setUserName("MyL");
        sm.provide("MS+N34E56").setUserName("MyS");
        tm.provide("MT+N78E90").setUserName("MyT");
        lm.provide("ML-12").setUserName("MyLb");
        tm.provide("MT+12;-N34E56").setUserName("MyTb");

        JTableOperator jTableOperator = new JTableOperator(jfo);
        assertEquals(3, jTableOperator.getRowCount());

        jTableOperator.waitCell("Turnout Thrown: MyT", 2, 2);
        jTableOperator.waitCell("Turnout Closed: MyT", 2, 3);

        tm.provide("MT+N78E90").setInverted(true);
        jTableOperator.waitCell("Turnout Closed: MyT", 2, 2);
        jTableOperator.waitCell("Turnout Thrown: MyT", 2, 3);

        sm.provide("MS+65535").setUserName("MySa");
        jTableOperator.waitCell("Sensor Active: MySa", 3, 2);
        jTableOperator.waitCell("Sensor Inactive: MySa", 3, 3);

        sm.provide("MS+65535").setInverted(true);
        jTableOperator.waitCell("Sensor Inactive: MySa", 3, 2);
        jTableOperator.waitCell("Sensor Active: MySa", 3, 3);

        jfo.requestClose();

        sm.dispose();
        lm.dispose();
        tm.dispose();

    }

    @Test
    public void testPersistedViewPanes(){

        ((CbusEventTablePane) panel).initComponents(memo);
        initFrame();
        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( panel.getTitle() );
        JMenuBarOperator mainbar = new JMenuBarOperator(jfo);

        // show pane
        mainbar.pushMenu(Bundle.getMessage("Display")); // stops at top level
        JMenuOperator jmo = new JMenuOperator(mainbar, Bundle.getMessage("Display"));
        new JMenuItemOperator(new JPopupMenuOperator(jmo.getPopupMenu()),Bundle.getMessage("NewTsl")).push();

        // show pane
        mainbar.pushMenu(Bundle.getMessage("Display")); // stops at top level
        jmo = new JMenuOperator(mainbar, Bundle.getMessage("Display"));
        new JMenuItemOperator(new JPopupMenuOperator(jmo.getPopupMenu()),Bundle.getMessage("ButtonSendEvent")).push();

        panel.dispose();
        panel = null;

        panel = new CbusEventTablePane();

        ((CbusEventTablePane) panel).initComponents(memo);
        initFrame();

        assertFalse(getEditBeanButtonEnabled(jfo));
        assertTrue(getSendEventButtonEnabled(jfo));

        panel.dispose();

        JUnitUtil.dispose(jfo.getWindow());

    }

    private boolean getNewEventButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("NewEvent")).isEnabled() );
    }

    private boolean getClearFilterButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("ClearFilter")).isEnabled() );
    }

    private boolean getEditBeanButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("EditUserName")).isEnabled() );
    }

    private boolean getSendEventButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("ButtonSend")).isEnabled() );
    }


    private void initFrame(){

        JmriJFrame f = new JmriJFrame();
        f.add(panel);
        f.setTitle(panel.getTitle());

        List<JMenu> list = panel.getMenus();
        JMenuBar bar = f.getJMenuBar();
        if (bar == null) {
            bar = new JMenuBar();
        }
        for (JMenu menu : list) {
            bar.add(menu);
        }
        f.setJMenuBar(bar);

        ThreadingUtil.runOnGUI( () -> {
            f.pack();
            f.setVisible(true);
        });

    }

    @TempDir
    protected File tempDir;

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis = null;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        assertNotNull(tempDir);
        try {
            JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir));
        } catch ( java.io.IOException e) {
            fail("Exception creating temp. user folder");
        }
        JUnitUtil.initDefaultUserMessagePreferences();
        title = Bundle.getMessage("EventTableTitle");
        helpTarget = "package.jmri.jmrix.can.cbus.swing.eventtable.EventTablePane";
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
        memo.configureManagers();

        panel = new CbusEventTablePane();
    }

    @AfterEach
    @Override
    public void tearDown() {
        // event model instance should have been created following init
        CbusEventTableDataModel dm = memo.get(CbusEventTableDataModel.class);
        if ( dm !=null ){
            dm.skipSaveOnDispose();
            dm.dispose();
        }
        Assertions.assertNotNull(tcis);
        tcis.terminateThreads();
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        tcis = null;
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        super.tearDown();
    }

}
