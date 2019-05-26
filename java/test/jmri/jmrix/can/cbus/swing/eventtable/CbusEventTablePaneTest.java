package jmri.jmrix.can.cbus.swing.eventtable;

import java.awt.GraphicsEnvironment;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.util.JmriJFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of CbusEventTablePane.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventTablePaneTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new CbusEventTablePane();
        title = Bundle.getMessage("MenuItemEventTable");
        helpTarget = "package.jmri.jmrix.can.cbus.swing.eventtable.EventTablePane";
    }
    
    

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    @Test
    public void testInitComp() {
        
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        jmri.InstanceManager.setDefault(jmri.jmrix.can.cbus.CbusPreferences.class,new CbusPreferences() );
        
        CbusEventTableDataModel m = new CbusEventTableDataModel(memo, 2,CbusEventTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusEventTableDataModel.class,m );
        
        CbusEventTablePane panel = new CbusEventTablePane();
        panel.initComponents(memo);
        
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
        
        Assert.assertTrue(getNewEventButtonEnabled(jfo));
        
        new JButtonOperator(jfo, Bundle.getMessage("NewEvent")).doClick();  // NOI18N
        
        Assert.assertFalse(getNewEventButtonEnabled(jfo));
        
        new JTextFieldOperator(jfo,1).typeText("1");
        Assert.assertTrue(getNewEventButtonEnabled(jfo));
        
        Assert.assertFalse(getClearFilterButtonEnabled(jfo));
        new JTextFieldOperator(jfo,0).typeText("1");
        Assert.assertTrue(getClearFilterButtonEnabled(jfo));
        
        new JButtonOperator(jfo, Bundle.getMessage("ClearFilter")).doClick();  // NOI18N
        Assert.assertFalse(getClearFilterButtonEnabled(jfo));
        
        panel.dispose();
        
        panel = new CbusEventTablePane();
        tcis = null;
        memo = null;
        
    }
    
    private boolean getNewEventButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("NewEvent")).isEnabled() );
    }
    
    private boolean getClearFilterButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("ClearFilter")).isEnabled() );
    }

}
