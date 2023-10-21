package jmri.jmrit.powerpanel;

import javax.swing.JFrame;

import jmri.InstanceManager;
import jmri.PowerManager;
import jmri.jmrix.lenz.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for the Jmrit PowerPanel
 *
 * @author Bob Jacobsen
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class PowerPaneTest extends jmri.util.swing.JmriPanelTest {

    private void createPowerFrame(String frameName){
        JFrame f = new JFrame(frameName);
        f.getContentPane().add(panel);
        f.pack();
        jmri.util.ThreadingUtil.runOnGUI(() -> f.setVisible(true));
    }

    // test on button routine
    @Test
    public void testPushOn() {
        createPowerFrame("testPushOn");
        JFrameOperator jfo = new JFrameOperator("testPushOn");

        Assertions.assertEquals(PowerManager.UNKNOWN, InstanceManager.getDefault(PowerManager.class).getPower());
        Assertions.assertEquals(Bundle.getMessage("StatusUnknown"), new JLabelOperator(jfo, 1).getText());

        new JButtonOperator(jfo,Bundle.getMessage("ButtonOn")).doClick();
        JUnitUtil.waitFor(() -> InstanceManager.getDefault(PowerManager.class).getPower()== PowerManager.ON,"power on");
        JUnitUtil.waitFor(() -> Bundle.getMessage("StatusOn").equals( new JLabelOperator(jfo, 1).getText()),"Status not changed on");

        jfo.requestClose();
        jfo.waitClosed();
    }

    // test off button routine
    @Test
    public void testPushOff() {
        createPowerFrame("testPushOff");
        JFrameOperator jfo = new JFrameOperator("testPushOff");

        Assertions.assertEquals(PowerManager.UNKNOWN, InstanceManager.getDefault(PowerManager.class).getPower());
        Assertions.assertEquals(Bundle.getMessage("StatusUnknown"), new JLabelOperator(jfo, 1).getText());

        new JButtonOperator(jfo,Bundle.getMessage("ButtonOff")).doClick();
        JUnitUtil.waitFor(() -> InstanceManager.getDefault(PowerManager.class).getPower()== PowerManager.OFF,"power off");
        JUnitUtil.waitFor(() -> Bundle.getMessage("StatusOff").equals( new JLabelOperator(jfo, 1).getText()),"Status not changed off");

        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testPowerPanelMultiConn(){

        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        PowerManager pm = new XNetPowerManager(tc.getSystemConnectionMemo());
        InstanceManager.setDefault(PowerManager.class, pm);

        jmri.util.ThreadingUtil.runOnGUI( () ->
            new PowerPanelAction().actionPerformed(null));

        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("TitlePowerPanel")
                + " : " + Bundle.getMessage("AllConnections"));
        new JMenuBarOperator(jfo).getMenu(0).doClick();
        JPopupMenuOperator jpo = new JPopupMenuOperator(jfo);
        Assertions.assertEquals(3, jpo.getComponentCount());

        new JMenuItemOperator(jpo, 2).doClick(); // select xnet
        jfo.getQueueTool().waitEmpty();
        Assertions.assertEquals(Bundle.getMessage("TitlePowerPanel")
                + " : " + pm.getUserName(), jfo.getTitle());

        int framesBefore = tc.outbound.size();
        new JButtonOperator(jfo,Bundle.getMessage("ButtonIdle")).doClick();
        Assertions.assertTrue( tc.outbound.size() > framesBefore);

        new JMenuItemOperator(jpo, 0).doClick(); // select all connections
        jfo.getQueueTool().waitEmpty();
        Assertions.assertEquals(Bundle.getMessage("TitlePowerPanel")
                + " : " + Bundle.getMessage("AllConnections"), jfo.getTitle());

        tc.terminateThreads();
        jfo.requestClose();
        jfo.waitClosed();

    }

    // setup a default PowerManager interface
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initDebugPowerManager();
        panel = new PowerPane();
        helpTarget="package.jmri.jmrit.powerpanel.PowerPanelFrame";
        title=Bundle.getMessage("TitlePowerPanel") + " : Internal";
    }

}
