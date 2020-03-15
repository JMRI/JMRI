package jmri.jmrix.can.cbus.swing.eventtable;

import java.awt.GraphicsEnvironment;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConfigurationManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.util.JmriJFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of CbusEventTablePane.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventTablePaneTest extends jmri.util.swing.JmriPanelTest {
        
    @Test
    public void testInitComp() {
        
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
         
        ((CbusEventTablePane)panel).initComponents(memo);
        
        Assert.assertNotNull("exists", panel);
        Assert.assertEquals("name with memo","CAN " + Bundle.getMessage("EventTableTitle"),panel.getTitle());
        
        
        // check pane has loaded something
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
       

 
        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( panel.getTitle() );

        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        
        Assert.assertTrue(getNewEventButtonEnabled(jfo));
        
        new JButtonOperator(jfo, Bundle.getMessage("NewEvent")).doClick();  // NOI18N
        
        Assert.assertFalse(getNewEventButtonEnabled(jfo));
        
        new JTextFieldOperator(jfo,1).typeText("1");
        Assert.assertTrue(getNewEventButtonEnabled(jfo));

        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        
        Assert.assertFalse(getClearFilterButtonEnabled(jfo));
        new JTextFieldOperator(jfo,0).typeText("1");
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertTrue(getClearFilterButtonEnabled(jfo));
        
        new JButtonOperator(jfo, Bundle.getMessage("ClearFilter")).doClick();  // NOI18N
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertFalse(getClearFilterButtonEnabled(jfo));
        
        jfo.requestClose();
    }
    
    private boolean getNewEventButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("NewEvent")).isEnabled() );
    }
    
    private boolean getClearFilterButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("ClearFilter")).isEnabled() );
    }
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private CanSystemConnectionMemo memo; 
    private TrafficControllerScaffold tcis; 
    private CbusConfigurationManager configM;

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        title = Bundle.getMessage("EventTableTitle");
        helpTarget = "package.jmri.jmrix.can.cbus.swing.eventtable.EventTablePane";
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        configM = new CbusConfigurationManager(memo);
        
        jmri.InstanceManager.setDefault(CbusPreferences.class,new CbusPreferences() );

        try {
            JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder.newFolder(jmri.profile.Profile.PROFILE)));
        } catch ( java.io.IOException e) {
            Assert.assertFalse("Exception creating temp. user folder",true);
        }
        panel = new CbusEventTablePane();
    }
    
    @Override
    @After
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
        super.tearDown();
    }    
    
}
