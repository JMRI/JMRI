package jmri.jmrit.display.controlPanelEditor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 * Test simple functioning of the CircuitBuilder class.
 *
 * @author  Paul Bender Copyright (C) 2017
 * @author  Pete Cressman Copyright (C) 2019
 */
public class CircuitBuilderTest {

    private ControlPanelEditor cpe;
    private CircuitBuilder cb;

    @Test
    @DisabledIfHeadless
    public void testCtor() {
        ControlPanelEditor f = new ControlPanelEditor();
        CircuitBuilder builder = new CircuitBuilder(f);
        assertNotNull( builder, "exists");
        JUnitUtil.dispose(f);
    }

    @Test
    @DisabledIfHeadless
    public void testOpenCBWindow() {
        getCPEandCB();

        cb.openCBWindow();
        cb.closeCBWindow();
    }


    @Test
    @DisabledIfHeadless
    public void testEditCircuitFrame() {
        getCPEandCB();

        OBlock ob3 = InstanceManager.getDefault(OBlockManager.class).getOBlock("OB3");
        cb.setCurrentBlock(ob3);

        cb.editCircuit("editCircuitItem", false);
        assertNotNull( cb.getEditFrame(), "exists");
/*
        cb.editCircuit("editCircuitItem", true);
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("editCircuitItem"));
        JTableOperator table = new JTableOperator(jdo);
        table.clickOnCell(3, 2);
        JButtonOperator button = new JButtonOperator(table, Bundle.getMessage("ButtonOpenCircuit"));
        button.doClick();
      */

        JFrameOperator editFrame = new JFrameOperator(cb.getEditFrame());
        JemmyUtil.pressButton(editFrame, Bundle.getMessage("ButtonDone"));
    }

    @Test
    @DisabledIfHeadless
    public void testEditCircuitError() {
        getCPEandCB();

        cb.editCircuitError("OB1");

        JFrameOperator nfo = new JFrameOperator(cb.getEditFrame());
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonDone"));
    }

    @Test
    @DisabledIfHeadless
    public void testEditPortals() {
        getCPEandCB();

        OBlock ob3 = InstanceManager.getDefault(OBlockManager.class).getOBlock("OB2");
        cb.setCurrentBlock(ob3);

        cb.editPortals("editPortalsItem", false);

        JFrameOperator nfo = new JFrameOperator(cb.getEditFrame());
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonDone"));
    }

    @Test
    @DisabledIfHeadless
    public void testEditCircuitPaths() {
        getCPEandCB();

        OBlock ob3 = InstanceManager.getDefault(OBlockManager.class).getOBlock("OB3");
        cb.setCurrentBlock(ob3);

        cb.editCircuitPaths("editCircuitPathsItem", false);

        JFrameOperator nfo = new JFrameOperator(cb.getEditFrame());
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonDone"));
    }

    @Test
    @DisabledIfHeadless
    public void testEditPortalDirection() {
        getCPEandCB();

        OBlock ob3 = InstanceManager.getDefault(OBlockManager.class).getOBlock("OB5");
        cb.setCurrentBlock(ob3);

        cb.editPortalDirection("editDirectionItem", false);

        JFrameOperator nfo = new JFrameOperator(cb.getEditFrame());
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonDone"));
    }

    @Test
    @DisabledIfHeadless
    public void testEditSignalFrame() {
        getCPEandCB();

        OBlock ob3 = InstanceManager.getDefault(OBlockManager.class).getOBlock("OB4");
        cb.setCurrentBlock(ob3);

        cb.editSignalFrame("editSignalItem", false);

        JFrameOperator nfo = new JFrameOperator(cb.getEditFrame());
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonDone"));
    }

    @Test
    @DisabledIfHeadless
    public void testEditPortalError() {
        getCPEandCB();
/*
        new Thread(() -> {
            JFrameOperator jfo = new JFrameOperator("Edit \"WestSiding\" Portals");
            JDialogOperator jdo = new JDialogOperator(jfo, Bundle.getMessage("incompleteCircuit"));
            JButtonOperator jbo = new JButtonOperator(jdo, "OK");
            jbo.push();
        }).start();*/

        cb.editPortalError("EastExit-EastJunction");

        JFrameOperator nfo = new JFrameOperator(cb.getEditFrame());
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonDone"));
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        cpe.dispose();
    }

    @Test
    @DisabledIfHeadless
    public void testEditPortalErrorIcon() {
        getCPEandCB();

        OBlock block = InstanceManager.getDefault(OBlockManager.class).getByUserName("WestSiding");
        Portal portal = InstanceManager.getDefault(jmri.jmrit.logix.PortalManager.class).getPortal("Crapolla");
        new Thread(() -> {
            JFrameOperator jfo = new JFrameOperator("Edit \"WestSiding\" Portals");
            JDialogOperator jdo = new JDialogOperator(jfo, Bundle.getMessage("incompleteCircuit"));
            JButtonOperator jbo = new JButtonOperator(jdo, "OK");
            jbo.push();
        }).start();

        cb.editPortalError(block, portal, null);

        JFrameOperator nfo = new JFrameOperator(cb.getEditFrame());

        // continue or exit?
        // Yes to ignore message and continue
        Thread t = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("continue"), "Yes");

        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonDone"));
        JUnitUtil.waitThreadTerminated(t);
    }

    @Test
    @Disabled("Cannot get button pushed!")
    @DisabledIfHeadless
    public void testNoBlock() {
        getCPEandCB();
        cb.editCircuitPaths("editCircuitPathsItem", false);

        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("NeedDataTitle"));
        JButtonOperator ok = new JButtonOperator(jdo, "OK");
        ok.push();
        //fixed: jmri.util.JUnitAppender.assertWarnMessage("getIconMap failed. family \"null\" not found in item type \"Portal\"");
    }

    void getCPEandCB() {

        ResourceBundle rbxWarrant = ResourceBundle.getBundle("jmri.jmrit.logix.WarrantBundle");
        Thread t = JemmyUtil.createModalDialogOperatorThread(
            rbxWarrant.getString("ErrorDialogTitle"), "OK");

        File f = new File("java/test/jmri/jmrit/display/controlPanelEditor/valid/CircuitBuilderTest.xml");
        assertDoesNotThrow( () ->
            InstanceManager.getDefault(ConfigureManager.class).load(f));
        JUnitUtil.waitThreadTerminated(t);

        cpe = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("CircuitBuilderTest Editor");
        assertNotNull( cpe, "exists");
        cb = cpe.getCircuitBuilder();
        assertNotNull( cb, "exists");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initOBlockManager();
    }

    @AfterEach
    public void tearDown() {
        if (cpe != null) {
            JUnitUtil.dispose(cpe);
            cpe = null;
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        if (InstanceManager.containsDefault(ShutDownManager.class)) {
            ShutDownManager sm = InstanceManager.getDefault(jmri.ShutDownManager.class);
            List<Runnable> rlist = sm.getRunnables();
            if (rlist.size() == 1 && rlist.get(0) instanceof jmri.jmrit.logix.WarrantShutdownTask) {
                sm.deregister((ShutDownTask)rlist.get(0));
            }
        }
        // cleaning up nameless invisible frame created by creating a dialog with a null parent
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CircuitBuilderTest.class);
}
