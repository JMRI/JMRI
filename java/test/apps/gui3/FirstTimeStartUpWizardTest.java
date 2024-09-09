package apps.gui3;

import apps.gui3.dp3.DecoderPro3;

import java.io.File;
import java.io.IOException;

import jmri.InstanceManager;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.gui.GuiLafPreferencesManager;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.jemmy.operators.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2022
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class FirstTimeStartUpWizardTest {

    @Test
    public void testCTor() {
        String[] args = {"DecoderProConfig3.xml"};
        Apps3 a = new DecoderPro3Implm(args); // creates new FirstTimeStartUpWizard frame
        assertNotNull(a);
        JFrameOperator jfo = new JFrameOperator("DecoderPro Wizard");
        getCancelButton(jfo).doClick();
        jfo.waitClosed();
        JUnitUtil.waitFor(() -> {
            return JUnitAppender.checkForMessageStartingWith("No pre-existing config file found, searched for ") != null;
        }, "no existing config Info line seen");

        JUnitUtil.waitThreadTerminated("Start-Up Wizard Connect");

    }

    @Test
    public void testLoadSim() {
        String[] args = {"DecoderProConfig3.xml"};
        Apps3 a = new DecoderPro3Implm(args);
        assertNotNull(a);

        JFrameOperator jfo = new JFrameOperator("DecoderPro Wizard");
        Assertions.assertNotNull(jfo);
        assertEquals("Welcome to JMRI StartUp Wizard", getPageTitleOperator(jfo).getText());
        assertFalse(getBackButton(jfo).isEnabled());
        assertTrue(getNextButton(jfo).isEnabled());
        assertTrue(getCancelButton(jfo).isEnabled());

        getNextButton(jfo).doClick();
        getNextButton(jfo).getQueueTool().waitEmpty();
        assertEquals("Set the Default Language and Owner", getPageTitleOperator(jfo).getText());
        assertTrue(getBackButton(jfo).isEnabled());
        assertTrue(getNextButton(jfo).isEnabled());
        assertTrue(getCancelButton(jfo).isEnabled());
        
        getBackButton(jfo).doClick();
        getBackButton(jfo).getQueueTool().waitEmpty();
        assertEquals("Welcome to JMRI StartUp Wizard", getPageTitleOperator(jfo).getText());
        getNextButton(jfo).doClick();
        getNextButton(jfo).getQueueTool().waitEmpty();

        JComboBoxOperator jcboLanguage = new JComboBoxOperator(jfo);
        assertNotNull(jcboLanguage);
        assertNotEquals(-1, jcboLanguage.getSelectedIndex());

        JTextFieldOperator jtfo = new JTextFieldOperator(jfo);
        assertNotNull(jtfo);
        jtfo.clearText();
        jtfo.typeText("My FTSUWTest Name");

        getNextButton(jfo).doClick();
        getNextButton(jfo).getQueueTool().waitEmpty();

        assertEquals("Select your DCC Connection", getPageTitleOperator(jfo).getText());
        assertTrue(getBackButton(jfo).isEnabled());
        assertTrue(getNextButton(jfo).isEnabled());
        assertTrue(getCancelButton(jfo).isEnabled());

        JComboBoxOperator jcboHardwareManufacturer = new JComboBoxOperator(jfo,0);
        assertNotNull(jcboHardwareManufacturer);
        jcboHardwareManufacturer.setSelectedItem("MERG");
        jcboHardwareManufacturer.getQueueTool().waitEmpty();
        jfo.getQueueTool().waitEmpty();
        assertEquals("MERG",jcboHardwareManufacturer.getSelectedItem());

        JComboBoxOperator jcboHardwareType = new JComboBoxOperator(jfo,1);
        assertNotNull(jcboHardwareType);
        jcboHardwareType.setSelectedItem("CAN Simulation");
        jfo.getQueueTool().waitEmpty();
        assertEquals("CAN Simulation",jcboHardwareType.getSelectedItem());

        getNextButton(jfo).doClick();
        getNextButton(jfo).getQueueTool().waitEmpty();

        assertEquals("Finish and Connect", getPageTitleOperator(jfo).getText());
        assertTrue(getBackButton(jfo).isEnabled());
        assertTrue(getFinishButton(jfo).isEnabled());
        assertTrue(getCancelButton(jfo).isEnabled());

        getFinishButton(jfo).doClick();
        jfo.waitClosed();
        
        var memo = InstanceManager.getDefault(jmri.jmrix.can.CanSystemConnectionMemo.class);
        assertNotNull(memo);
        memo.getTrafficController().terminateThreads();
        JUnitUtil.waitFor(() -> {
            return JUnitAppender.checkForMessageStartingWith("No pre-existing config file found, searched for ") != null;
        }, "no existing config Info line seen");

        Assertions.assertNotEquals(0, InstanceManager.getDefault(GuiLafPreferencesManager.class).getFontSize(),
            "Font size should not be 0");

    }

    @Test
    public void testDisplayDialogues(){
        InstanceManager.setDefault(jmri.jmrix.ConnectionConfigManager.class, new jmri.jmrix.ConnectionConfigManager());
        InstanceManager.setDefault(RosterConfigManager.class, new RosterConfigManager());

        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("testDisplayDialogues", false, false);
        FirstTimeStartUpWizard t = new FirstTimeStartUpWizard(jf, null);
        assertNotNull(t);
        t.createScreens();

        Thread t1 = new Thread(() -> {
            jmri.util.swing.JemmyUtil.pressDialogButton( "Error Opening Connection", "OK"); // not from JMRI Bundle
        });
        t1.setName("click OK after dialogue Thread");
        t1.start();

        Thread t2 = new Thread(() -> {
            throw new IllegalArgumentException("Test IAE");
        });
        t2.setName("throw IAE Thread");
        t2.setUncaughtExceptionHandler(t);
        t2.start();

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click ok Button in display exception dialogue didn't happen");

        jmri.util.JUnitAppender.assertErrorMessageStartsWith("Exception: ");

        JUnitUtil.dispose(jf);
    }

    private JLabelOperator getPageTitleOperator(JFrameOperator jfo) {
        return new JLabelOperator(jfo,0);
    }

    private JButtonOperator getBackButton(JFrameOperator jfo){
        return new JButtonOperator(jfo, "< Back");
    }

    private JButtonOperator getNextButton(JFrameOperator jfo){
        return new JButtonOperator(jfo, "Next >");
    }

    private JButtonOperator getFinishButton(JFrameOperator jfo){
        return new JButtonOperator(jfo, "Finish");
    }

    private JButtonOperator getCancelButton(JFrameOperator jfo){
        return new JButtonOperator(jfo, "Cancel");
    }

    private static class DecoderPro3Implm extends DecoderPro3 {

        DecoderPro3Implm(String[] args) {
            super(args);
        }
    
        // force the application to not actually start.
        // Just checking construction.
        @Override
        protected void start() {
        }

        @Override
        protected void configureProfile() {
            JUnitUtil.resetInstanceManager();
        }

        @Override
        protected void installConfigurationManager() {
            JUnitUtil.initConfigureManager();
            JUnitUtil.initDefaultUserMessagePreferences();
        }

        @Override
        protected void installManagers() {
            JUnitUtil.initInternalTurnoutManager();
            JUnitUtil.initInternalLightManager();
            JUnitUtil.initInternalSensorManager();
            JUnitUtil.initRouteManager();
            JUnitUtil.initMemoryManager();
            JUnitUtil.initDebugThrottleManager();
        }

        @Override
        public void createAndDisplayFrame() {
            // called when wizard is disposed, but do nothing in tests
        }

    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException  {
        JUnitUtil.setUp();
        JUnitUtil.resetApplication();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.resetApplication();
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(FirstTimeStartUpWizardTest.class);
}
