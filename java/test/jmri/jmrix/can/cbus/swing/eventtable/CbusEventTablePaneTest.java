package jmri.jmrix.can.cbus.swing.eventtable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConfigurationManager;
import jmri.jmrix.can.cbus.CbusLightManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.CbusSensorManager;
import jmri.jmrix.can.cbus.CbusTurnoutManager;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.util.JmriJFrame;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.jemmy.operators.*;


/**
 * Test simple functioning of CbusEventTablePane.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventTablePaneTest extends jmri.util.swing.JmriPanelTest {
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testInitComp() {
         
        ((CbusEventTablePane)panel).initComponents(memo);
        
        assertThat(panel).isNotNull();
        assertEquals("CAN " + Bundle.getMessage("EventTableTitle"),panel.getTitle());
        
        
        // check pane has loaded something
        initFrame();
       
        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( panel.getTitle() );

        // new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        
        assertThat(getNewEventButtonEnabled(jfo)).isTrue();
        
        new JButtonOperator(jfo, Bundle.getMessage("NewEvent")).doClick();  // NOI18N
        
        assertThat(getNewEventButtonEnabled(jfo)).isFalse();
        
        new JTextFieldOperator(jfo,1).typeText("1");
        assertThat(getNewEventButtonEnabled(jfo)).isTrue();

        // new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        
        assertThat(getClearFilterButtonEnabled(jfo)).isFalse();
        new JTextFieldOperator(jfo,0).typeText("1");
        // new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        assertThat(getClearFilterButtonEnabled(jfo)).isTrue();
        
        new JButtonOperator(jfo, Bundle.getMessage("ClearFilter")).doClick();  // NOI18N
        // new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        assertThat(getClearFilterButtonEnabled(jfo)).isFalse();
        
        jfo.requestClose();
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
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
        assertThat(getEditBeanButtonEnabled(jfo)).isFalse();
        mainbar.pushMenu(Bundle.getMessage("Display")); // stops at top level
        jmo = new JMenuOperator(mainbar, Bundle.getMessage("Display"));
        new JMenuItemOperator(new JPopupMenuOperator(jmo.getPopupMenu()),Bundle.getMessage("NewTsl")).push();
        
        // show pane
        mainbar.pushMenu(Bundle.getMessage("Display")); // stops at top level
        jmo = new JMenuOperator(mainbar, Bundle.getMessage("Display"));
        new JMenuItemOperator(new JPopupMenuOperator(jmo.getPopupMenu()),Bundle.getMessage("ButtonSendEvent")).push();
        assertThat(getSendEventButtonEnabled(jfo)).isTrue();

        
        // new JButtonOperator(jfo, "Not a Button").doClick();  // NOI18N
        
        jfo.requestClose();
        
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
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
        
        
        // new JButtonOperator(jfo, "Not a Button").doClick();  // NOI18N
        
        jfo.requestClose();
    
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCellTsLValues(){
        
        CbusLightManager lm = new CbusLightManager(memo);
        CbusSensorManager sm = new CbusSensorManager(memo);
        CbusTurnoutManager tm = new CbusTurnoutManager(memo);
        
        InstanceManager.setDefault(jmri.TurnoutManager.class,tm);
        InstanceManager.setDefault(jmri.SensorManager.class,sm);
        InstanceManager.setDefault(jmri.LightManager.class,lm);
        
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
        assertThat( jTableOperator.getRowCount()).isEqualTo(3);
        
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
        
        // new JButtonOperator(jfo, "Not a Button").doClick();  // NOI18N
        
        jfo.requestClose();
        
        sm.dispose();
        lm.dispose();
        tm.dispose();
        
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
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
        
        assertThat(getEditBeanButtonEnabled(jfo)).isFalse();
        assertThat(getSendEventButtonEnabled(jfo)).isTrue();
        
        panel.dispose();
        
       //  new JButtonOperator(jfo, "Not a Button").doClick();  // NOI18N
        
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
        
        f.pack();
        f.setVisible(true);
    
    }
    
    @TempDir 
    protected Path tempDir;

    private CanSystemConnectionMemo memo; 
    private TrafficControllerScaffold tcis; 
    private CbusConfigurationManager configM;
    
    @BeforeEach
    @Override
    public void setUp() {
        // super.setUp();
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        title = Bundle.getMessage("EventTableTitle");
        helpTarget = "package.jmri.jmrix.can.cbus.swing.eventtable.EventTablePane";
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        assertThat(tempDir).isNotNull();
        
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
        memo.configureManagers();
        
        try {
            JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir.toFile()));
        } catch ( java.io.IOException e) {
            fail("Exception creating temp. user folder");
        }
        
        configM = new CbusConfigurationManager(memo);
        
        jmri.InstanceManager.setDefault(CbusPreferences.class,new CbusPreferences() );
        
        panel = new CbusEventTablePane();
    }
    
    
    @AfterEach
    @Override
    public void tearDown() {
        // event model instance should have been created following init
        CbusEventTableDataModel dm = InstanceManager.getNullableDefault(CbusEventTableDataModel.class);
        if ( dm !=null ){
            dm.skipSaveOnDispose();
            dm.dispose();
        }
        
        configM.dispose();
        tcis.terminateThreads();
        memo.dispose();
        memo = null;
        tcis = null;
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }    
    
}
