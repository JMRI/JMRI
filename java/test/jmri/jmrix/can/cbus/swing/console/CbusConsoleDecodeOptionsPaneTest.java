package jmri.jmrix.can.cbus.swing.console;

import java.nio.file.Path;

import javax.swing.JFrame;

import jmri.jmrix.can.*;
import jmri.util.JUnitUtil;

import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test simple functioning of CbusConsoleDecodeOptionsPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusConsoleDecodeOptionsPaneTest  {

    @Test
    public void testInitComponents() {
        // for now, just makes sure there isn't an exception.
        CbusConsoleDecodeOptionsPane t = new CbusConsoleDecodeOptionsPane(mainConsolePane);
        assertNotNull(t);
        t.dispose();
    }

    @Test
    public void testCheckBoxPersistence() {

        CbusConsoleDecodeOptionsPane t = new CbusConsoleDecodeOptionsPane(mainConsolePane);
        JFrame f = new JFrame(t.getClass().getName());
        f.add(t);
        ThreadingUtil.runOnGUI( () -> {
            f.pack();
            f.setVisible(true);
        });
        JFrameOperator jfo = new JFrameOperator( t.getClass().getName() );

        assertEquals("101111000000",getAllCheckBoxStatus(jfo));

        for (String checkBoxLabel : checkBoxLabels) {
            new JCheckBoxOperator(jfo, checkBoxLabel).setSelected(true);
        }

        assertEquals("111111111111",getAllCheckBoxStatus(jfo));

        t.dispose();
        JUnitUtil.dispose(f);

        CbusConsoleDecodeOptionsPane tt = new CbusConsoleDecodeOptionsPane(mainConsolePane);
        JFrame ff = new JFrame(tt.getClass().getName());
        ff.add(tt);
        ThreadingUtil.runOnGUI( () -> {
            ff.pack();
            ff.setVisible(true);
        });
        JFrameOperator jffo = new JFrameOperator( tt.getClass().getName() );

        assertEquals("111111111111",getAllCheckBoxStatus(jffo));

        for (int i=0; i<checkBoxLabels.length; i++ ){
            new JCheckBoxOperator(jffo,checkBoxLabels[i]).setSelected((i % 2 == 0));
        }

        assertEquals("101010101010",getAllCheckBoxStatus(jffo));
        tt.dispose();
        JUnitUtil.dispose(ff);

        CbusConsoleDecodeOptionsPane ttt = new CbusConsoleDecodeOptionsPane(mainConsolePane);
        JFrame fff = new JFrame(ttt.getClass().getName());
        fff.add(ttt);

        ThreadingUtil.runOnGUI( () -> {
            fff.pack();
            fff.setVisible(true);
        });
        JFrameOperator jfffo = new JFrameOperator( ttt.getClass().getName() );

        assertEquals("101010101010",getAllCheckBoxStatus(jfffo));

        ttt.dispose();
        JUnitUtil.dispose(fff);

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

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tc = null;
    private CbusConsolePane mainConsolePane = null;

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
        assertNotNull(mainConsolePane);
        mainConsolePane.dispose();
        assertNotNull(tc);
        tc.terminateThreads();
        assertNotNull(memo);
        memo.dispose();
        mainConsolePane = null;
        tc = null;
        memo = null;

        JUnitUtil.tearDown();
    }

}
