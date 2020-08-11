
package jmri.jmrix.loconet.locomon;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.AbstractMonPaneScaffold;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;

import org.assertj.swing.edt.GuiActionRunner;
import org.junit.Assume;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Test of LocoMonPane
 * 
 * Initially written to test filtering
 *
 * @author Bob Jacobsen   Copyright 2015
 */
public class LocoMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {

    @Test
    public void testInput() {
        Throwable thrown = catchThrowable( () -> GuiActionRunner.execute( () ->  pane.initComponents()));
        assertThat(thrown).isNull();
        LocoNetMessage m = new LocoNetMessage(new int[]{0xA0, 0x07, 0x00, 0x58});
        ThreadingUtil.runOnGUI( () -> ((LocoMonPane)pane).message(m));
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        assertThat(getFrameTextONGUIThread()).withFailMessage("shows message").isEqualTo("Set speed of loco in slot 7 to 0.\n");
    }

    @Test
    public void testFilterNot() {
        Throwable thrown = catchThrowable( () -> GuiActionRunner.execute( () ->  pane.initComponents()));
        assertThat(thrown).isNull();
        // filter not match
        setAndCheckFilterTextEntry("A1","A1","filter set");

        LocoNetMessage m = new LocoNetMessage(new int[]{0xA0, 0x07, 0x00, 0x58});
        ThreadingUtil.runOnGUI( () -> ((LocoMonPane)pane).message(m));
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        assertThat(getFrameTextONGUIThread()).withFailMessage("shows message").isEqualTo("Set speed of loco in slot 7 to 0.\n");
    }

    @Test
    public void testFilterSimple() {
        Throwable thrown = catchThrowable( () -> GuiActionRunner.execute( () ->  pane.initComponents()));
        assertThat(thrown).isNull();
        // filter A0
        setAndCheckFilterTextEntry("A0","A0","filter set");
        
        LocoNetMessage m = new LocoNetMessage(new int[]{0xA0, 0x07, 0x00, 0x58});
        ThreadingUtil.runOnGUI( () -> ((LocoMonPane)pane).message(m));
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        assertThat(getFrameTextONGUIThread()).withFailMessage("shows message").isEqualTo("");
    }

    @Test
    public void testFilterMultiple() {
        Throwable thrown = catchThrowable( () -> GuiActionRunner.execute( () ->  pane.initComponents()));
        assertThat(thrown).isNull();
        // filter B1 A0
        setAndCheckFilterTextEntry("B1 A0","B1 A0","filter set");

        LocoNetMessage m = new LocoNetMessage(new int[]{0xA0, 0x07, 0x00, 0x58});
        ThreadingUtil.runOnGUI(()->((LocoMonPane)pane).message(m));
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        assertThat(getFrameTextONGUIThread()).withFailMessage("shows message").isEqualTo("");
    }

    // Test checking the AutoScroll checkbox.
    // for some reason the LocoMonPane has the checkbox value reversed on
    // startup compared to other AbstractMonPane derivatives.
    @Override
    @Test
    public void checkAutoScrollCheckBox(){
         Assume.assumeFalse(GraphicsEnvironment.isHeadless());
         AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

         // for Jemmy to work, we need the pane inside of a frame
         JmriJFrame f = new JmriJFrame();

         Throwable thrown = catchThrowable( () -> GuiActionRunner.execute( () ->  pane.initComponents()));
         assertThat(thrown).isNull();

        ThreadingUtil.runOnGUI( () -> {
            f.add(pane);
            // set title if available
            if (pane.getTitle() != null) {
                f.setTitle(pane.getTitle());
            }
            f.pack();
            f.setVisible(true);
        });
         assertThat(s.getAutoScrollCheckBoxValue()).isTrue();
         s.checkAutoScrollCheckBox();
         assertThat(s.getAutoScrollCheckBoxValue()).isFalse();
        ThreadingUtil.runOnGUI( () -> {
            f.setVisible(false);
            f.dispose();
        });
    }

    jmri.TurnoutManager l;
    jmri.SensorManager s;
    jmri.ReporterManager r;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();

        // prepare an interface, register
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo("L", "LocoNet");
        jmri.jmrix.loconet.LocoNetInterfaceScaffold lnis = new jmri.jmrix.loconet.LocoNetInterfaceScaffold(memo);
        // create and register the manager object
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        l = new jmri.jmrix.loconet.LnTurnoutManager(memo, lnis, false);
        jmri.InstanceManager.setTurnoutManager(l);

        jmri.util.JUnitUtil.initInternalSensorManager();
        s = new jmri.jmrix.loconet.LnSensorManager(memo);
        jmri.InstanceManager.setSensorManager(s);

        jmri.util.JUnitUtil.initReporterManager();
        r = new jmri.jmrix.loconet.LnReporterManager(memo);
        jmri.InstanceManager.setReporterManager(r);

        // pane for AbstractMonFrameTestBase, panel for JmriPanelTest
        panel = pane = new LocoMonPane();
        helpTarget = "package.jmri.jmrix.loconet.locomon.LocoMonFrame";
        title = Bundle.getMessage("MenuItemLocoNetMonitor"); 
    }

    @Override
    @AfterEach
    public void tearDown() {
        pane.dispose();
        panel = pane = null;
        
        l.dispose();
        s.dispose();
        r.dispose();

        jmri.util.JUnitUtil.tearDown();
    }

}
