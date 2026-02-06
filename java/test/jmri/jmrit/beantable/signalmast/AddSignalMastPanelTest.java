package jmri.jmrit.beantable.signalmast;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;

import jmri.*;
import jmri.implementation.*;
import jmri.util.*;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.netbeans.jemmy.operators.*;

import org.junit.jupiter.api.*;

/**
 * @author Bob Jacobsen Copyright 2014
 */
@DisabledIfHeadless
public class AddSignalMastPanelTest {

    @Test
    public void testDefaultSystems() {
        AddSignalMastPanel a = new AddSignalMastPanel();

        // check that "Basic Model Signals" (basic directory) system is present
        boolean found = false;
        for (int i = 0; i < a.sigSysBox.getItemCount(); i++) {
            if (a.sigSysBox.getItemAt(i).equals("Basic Model Signals")) {
                found = true;
            }
        }
        assertTrue(found, "did not find Basic Model Signals");
    }

    @Test
    @Disabled("possible cause of 'No output has been received in the last 10m0s' failure")    
    public void testIssueWarningUserName() {
        assumeFalse(Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"),
            "Ignoring intermittent test");

        // show and cancel each of the error dialogs
        AddSignalMastPanel a = new AddSignalMastPanel();

        ThreadingUtil.runOnLayoutEventually(() -> { a.issueWarningUserName("user name");});
        JDialogOperator jdo = new JDialogOperator("Warning");
        jdo.requestClose();
        jdo.waitClosed();
        JUnitAppender.assertErrorMessage("User Name \"user name\" is already in use");
    }

    @Test
    @Disabled("possible cause of 'No output has been received in the last 10m0s' failure")    
    public void testIssueWarningUserNameAsSystem() {
        assumeFalse(Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"),
            "Ignoring intermittent test");

        // show and cancel each of the error dialogs
        AddSignalMastPanel a = new AddSignalMastPanel();

        ThreadingUtil.runOnLayoutEventually(() -> { a.issueWarningUserNameAsSystem("user name");});
        new JButtonOperator(new JDialogOperator("Warning"), "OK").push();
        JUnitAppender.assertErrorMessage("User Name \"user name\" already exists as a System name");
    }

    @Test
    @Disabled("possible cause of 'No output has been received in the last 10m0s' failure")
    public void testIssueNoUserNameGiven() {
        assumeFalse(Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"),
            "Ignoring intermittent test");

        // show and cancel each of the error dialogs
        AddSignalMastPanel a = new AddSignalMastPanel();

        ThreadingUtil.runOnLayoutEventually(() -> { a.issueNoUserNameGiven();}); // a ConfirmDialog
        new JButtonOperator(new JDialogOperator("No UserName Given"), "Yes").push();
    }

    @Test
    @Disabled("possible cause of 'No output has been received in the last 10m0s' failure")
    public void testIssueDialogFailMessage() {
        assumeFalse(Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"),
            "Ignoring intermittent test");
        // show and cancel each of the error dialogs
        AddSignalMastPanel a = new AddSignalMastPanel();

        ThreadingUtil.runOnLayoutEventually(() -> { a.issueDialogFailMessage(new IllegalArgumentException("for testing"));});
        new JButtonOperator(new JDialogOperator("Mast Not Updated"), "OK").push();
        JUnitAppender.assertErrorMessage("Failed during createMast");               
    }

    @Test
    public void testSearch() {
        assumeFalse(Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"),
            "Ignoring intermittent test");

        AddSignalMastPanel a = new AddSignalMastPanel();

        // check that mock (test) system is present
        boolean found = false;
        for (int i = 0; i < a.sigSysBox.getItemCount(); i++) {
            if (a.sigSysBox.getItemAt(i).equals(SignalSystemTestUtil.getMockUserName())) {
                found = true;
            }
        }
        assertTrue(found, "did not find JUnit Test Signals");

    }

    @Test
    @Disabled("possible cause of 'No output has been received in the last 10m0s' failure")
    public void testCheckUserName() {
        assumeFalse(Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"),
            "Ignoring intermittent test");

        AddSignalMastPanel a = new AddSignalMastPanel();

        VirtualSignalMast s1 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)", "user name");
        assertNotNull(s1);
        InstanceManager.getDefault(SignalMastManager.class).register(s1);
        assertNotNull(InstanceManager.getDefault(SignalMastManager.class).getByUserName("user name"));
        assertNotNull(InstanceManager.getDefault(SignalMastManager.class).getBySystemName("IF$vsm:basic:one-searchlight($1)"));

        assertTrue(a.checkUserName("foo"));

        // set up a thread to close dialog box
        new Thread(() -> {
            // constructor for d will wait until the dialog is visible
            JDialogOperator d = new JDialogOperator(Bundle.getMessage("WarningTitle"));
            JButtonOperator bo = new JButtonOperator(d,"OK");
            bo.push();
        }).start();

        assertFalse(a.checkUserName("user name"));

        JUnitAppender.assertErrorMessage("User Name \"user name\" is already in use");

        // set up a thread to close dialog box
        new Thread(() -> {
            // constructor for d will wait until the dialog is visible
            JDialogOperator d = new JDialogOperator(Bundle.getMessage("WarningTitle"));
            JButtonOperator bo = new JButtonOperator(d,"OK");
            bo.push();
        }).start();
        assertFalse(a.checkUserName("IF$vsm:basic:one-searchlight($1)"));

        JUnitAppender.assertErrorMessage("User Name \"IF$vsm:basic:one-searchlight($1)\" already exists as a System name");
    }

    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        SignalSystemTestUtil.createMockSystem();
    }

    @AfterEach
    public void tearDown() throws IOException {
        SignalSystemTestUtil.deleteMockSystem();
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AddSignalMastPanelTest.class);
}
