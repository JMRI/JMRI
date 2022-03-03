package jmri.jmrix.can.cbus.swing.simulator;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.simulator.CbusDummyNode;
import jmri.jmrix.can.cbus.simulator.moduletypes.MergCanpan;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of NdPane
 *
 * @author Steve Young Copyright (C) 2019
 */
public class NdPaneTest  {

    @Test
    public void testCTor() {
        NdPane t = new NdPane(null, null);
        Assertions.assertNotNull(t, "exists");
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testChangeNode() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
    
        NdPane t = new NdPane(null, memo);
    
        // check pane has loaded something
        JmriJFrame f = new JmriJFrame();
        f.add(t);
        f.setTitle("NdPaneTest 1");
        f.pack();
        f.setVisible(true);
        
        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( "NdPaneTest 1" );
        Assertions.assertFalse(getFlimButtonEnabled(jfo),"Flim button disabled");
        
        JComboBoxOperator jcbo = new JComboBoxOperator(jfo);
        jcbo.selectItem(2);
        Assertions.assertTrue(getFlimButtonEnabled(jfo),"Flim button enabled");
        JUnitAppender.assertWarnMessageStartsWith("No Simulator Running to register Node ");
        
        Assertions.assertEquals(0, tcis.inbound.size(),"nothing yet sent by module");
        new JButtonOperator(jfo, "FLiM").doClick();
        JUnitUtil.waitFor(()->{ return(tcis.inbound.size()==1); }, "module sent node num request");
        
        jcbo.selectItem(0);
        Assertions.assertFalse(getFlimButtonEnabled(jfo),"Flim button disabled");
        JUnitAppender.assertWarnMessageStartsWith("No Simulator Running to deregister Node ");
        
        // Ask to close window
        jfo.requestClose();
        jfo.waitClosed();

        tcis.terminateThreads();
        memo.dispose();
        
    }
    
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testExistingNode() {
        
        CbusDummyNode nd = new MergCanpan().getNewDummyNode(null, 1234);
        NdPane t = new NdPane(nd, null);
    
        // check pane has loaded something
        JmriJFrame f = new JmriJFrame();
        f.add(t);
        f.setTitle("NdPaneTest 2");
        f.pack();
        f.setVisible(true);
        
        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( "NdPaneTest 2" );
        
        JComboBoxOperator jcbo = new JComboBoxOperator(jfo);
        String s = (String) jcbo.getSelectedItem();
        Assertions.assertEquals(new MergCanpan().getModuleType(), s,"node slected in combobox");
        Assertions.assertTrue(getFlimButtonEnabled(jfo),"Flim button enabled");
        
        // Ask to close window
        jfo.requestClose();
        jfo.waitClosed();

        nd.dispose();
        
    }
    
    private boolean getFlimButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo, "FLiM").isEnabled() );
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
