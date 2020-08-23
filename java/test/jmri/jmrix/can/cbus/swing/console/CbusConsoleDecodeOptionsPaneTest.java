package jmri.jmrix.can.cbus.swing.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;

import jmri.jmrix.can.*;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test simple functioning of CbusConsoleDecodeOptionsPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
public class CbusConsoleDecodeOptionsPaneTest  {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testInitComponents() throws Exception{
        // for now, just makes sure there isn't an exception.
        CbusConsoleDecodeOptionsPane t = new CbusConsoleDecodeOptionsPane(mainConsolePane);
        assertThat(t).isNotNull();
        t.dispose();
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCheckBoxPersistence() {
    
        CbusConsoleDecodeOptionsPane t = new CbusConsoleDecodeOptionsPane(mainConsolePane);
        JmriJFrame f = new JmriJFrame();
        f.add(t);
        f.setTitle(t.getClass().getName());
        f.pack();
        f.setVisible(true);
        JFrameOperator jfo = new JFrameOperator( t.getClass().getName() );
        
        assertEquals("101111000000",getAllCheckBoxStatus(jfo));
        
        for (String checkBoxLabel : checkBoxLabels) {
            new JCheckBoxOperator(jfo, checkBoxLabel).setSelected(true);
        }
        
        assertEquals("111111111111",getAllCheckBoxStatus(jfo));
        
        t.dispose();
        f.dispose();
        
        CbusConsoleDecodeOptionsPane tt = new CbusConsoleDecodeOptionsPane(mainConsolePane);
        JmriJFrame ff = new JmriJFrame();
        ff.add(tt);
        ff.setTitle(tt.getClass().getName());
        ff.pack();
        ff.setVisible(true);
        JFrameOperator jffo = new JFrameOperator( tt.getClass().getName() );
        
        assertEquals("111111111111",getAllCheckBoxStatus(jffo));
    
        for (int i=0; i<checkBoxLabels.length; i++ ){
            new JCheckBoxOperator(jffo,checkBoxLabels[i]).setSelected((i % 2 == 0));
        }
        
        // new org.netbeans.jemmy.QueueTool().waitEmpty();
        // new JButtonOperator(jfo, "Not a Button").doClick();
        assertEquals("101010101010",getAllCheckBoxStatus(jffo));
        tt.dispose();
        ff.dispose();
        
        CbusConsoleDecodeOptionsPane ttt = new CbusConsoleDecodeOptionsPane(mainConsolePane);
        JmriJFrame fff = new JmriJFrame();
        fff.add(ttt);
        fff.setTitle(ttt.getClass().getName());
        fff.pack();
        fff.setVisible(true);
        JFrameOperator jfffo = new JFrameOperator( ttt.getClass().getName() );
        
        assertEquals("101010101010",getAllCheckBoxStatus(jfffo));

        ttt.dispose();
        fff.dispose();
        
    }
    
    private String getAllCheckBoxStatus( JFrameOperator jfo ){
        StringBuilder sb= new StringBuilder(checkBoxLabels.length);
        for (String checkBoxLabel : checkBoxLabels) {
            sb.append(new JCheckBoxOperator(jfo, checkBoxLabel).isSelected() ? 1 : 0);
        }
        return sb.toString();
    }
    
    /**
     * Local order of check boxes is used for test, not screen display order.
     */
    private final static String[] checkBoxLabels = new String[]{
        Bundle.getMessage("TrafficDirection"),
        Bundle.getMessage("ButtonShowTimestamp"),
        Bundle.getMessage("showOpcCheckbox"),
        Bundle.getMessage("MenuItemCBUS"),
        "JMRI",
        Bundle.getMessage("OpcName"),
        Bundle.getMessage("OpcExtraCheckbox"),
        Bundle.getMessage("showAddressCheckBox"),
        Bundle.getMessage("ButtonShowPriorities"),
        Bundle.getMessage("CanID"),
        Bundle.getMessage("showCanCheckBox"),
        Bundle.getMessage("RtrCheckbox")
    };
    
    @TempDir 
    protected Path tempDir;
    
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tc;
    private CbusConsolePane mainConsolePane;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        try {
            JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir.toFile()));
        } catch ( java.io.IOException e) {
            fail("Exception creating temp. user folder");
        }
        memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
        mainConsolePane = new CbusConsolePane();
        mainConsolePane.initComponents(memo,false);
    }

    @AfterEach
    public void tearDown() {
        mainConsolePane.dispose();
        tc.terminateThreads();
        memo.dispose();
        tc = null;
        memo = null;
        
        JUnitUtil.tearDown();
    }

}
