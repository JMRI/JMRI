package jmri.jmrit.beantable.signalmast;

import jmri.*;
import jmri.implementation.*;
import jmri.util.*;

import java.awt.GraphicsEnvironment;

import org.netbeans.jemmy.operators.*;

import org.junit.*;

/**
 * @author	Bob Jacobsen Copyright 2014
 */
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
        Assert.assertTrue("did not find Basic Model Signals", found);
    }

    @Test
    public void testIssueWarningUserName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // show and cancel each of the error dialogs
        AddSignalMastPanel a = new AddSignalMastPanel();

        jmri.util.ThreadingUtil.runOnLayoutEventually(() -> { a.issueWarningUserName("user name");});
        new JDialogOperator("Warning").close();
        JUnitAppender.assertErrorMessage("User Name \"user name\" is already in use");
    }
    
    @Test
    public void testIssueWarningUserNameAsSystem() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // show and cancel each of the error dialogs
        AddSignalMastPanel a = new AddSignalMastPanel();

        jmri.util.ThreadingUtil.runOnLayoutEventually(() -> { a.issueWarningUserNameAsSystem("user name");});
        new JButtonOperator(new JDialogOperator("Warning"), "OK").push();
        JUnitAppender.assertErrorMessage("User Name \"user name\" already exists as a System name");
    }
    
    @Test
    public void testIssueNoUserNameGiven() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // show and cancel each of the error dialogs
        AddSignalMastPanel a = new AddSignalMastPanel();

        jmri.util.ThreadingUtil.runOnLayoutEventually(() -> { a.issueNoUserNameGiven();}); // a ConfirmDialog
        new JButtonOperator(new JDialogOperator("No UserName Given"), "Yes").push();
    }
    
    @Test
    public void testIssueDialogFailMessage() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // show and cancel each of the error dialogs
        AddSignalMastPanel a = new AddSignalMastPanel();

        jmri.util.ThreadingUtil.runOnLayoutEventually(() -> { a.issueDialogFailMessage(new IllegalArgumentException("for testing"));});
        new JButtonOperator(new JDialogOperator("Mast Not Updated"), "OK").push();
        JUnitAppender.assertErrorMessage("Failed during createMast");               
    }
    
    @Test
    public void testSearch() throws Exception {
        try {
            AddSignalMastPanel a = new AddSignalMastPanel();

            // check that mock (test) system is present
            boolean found = false;
            for (int i = 0; i < a.sigSysBox.getItemCount(); i++) {
                if (a.sigSysBox.getItemAt(i).equals(SignalSystemTestUtil.getMockUserName())) {
                    found = true;
                }
            }
            Assert.assertTrue("did not find JUnit Test Signals", found);
        } catch (Exception e) {
            Assert.fail("testSearch exception thrown: " + e.getCause().getMessage());
        } 
    }

    @Test
    public void testCheckUserName() throws Exception {
        AddSignalMastPanel a = new AddSignalMastPanel();
        
        VirtualSignalMast s1 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)", "user name");
        Assert.assertNotNull(s1);
        InstanceManager.getDefault(jmri.SignalMastManager.class).register(s1);
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName("user name"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SignalMastManager.class).getBySystemName("IF$vsm:basic:one-searchlight($1)"));
        
        Assert.assertTrue(a.checkUserName("foo"));

        if (!GraphicsEnvironment.isHeadless()) {
            // set up a thread to close dialog box
            new Thread(() -> {
                // constructor for d will wait until the dialog is visible
                JDialogOperator d = new JDialogOperator(Bundle.getMessage("WarningTitle"));
                JButtonOperator bo = new JButtonOperator(d,"OK");
                bo.push();
            }).start();
        }
        Assert.assertFalse(a.checkUserName("user name"));
        
        JUnitAppender.assertErrorMessage("User Name \"user name\" is already in use");
        
        if (!GraphicsEnvironment.isHeadless()) {
            // set up a thread to close dialog box
            new Thread(() -> {
                // constructor for d will wait until the dialog is visible
                JDialogOperator d = new JDialogOperator(Bundle.getMessage("WarningTitle"));
                JButtonOperator bo = new JButtonOperator(d,"OK");
                bo.push();
            }).start();
        }
        Assert.assertFalse(a.checkUserName("IF$vsm:basic:one-searchlight($1)"));

        JUnitAppender.assertErrorMessage("User Name \"IF$vsm:basic:one-searchlight($1)\" already exists as a System name");
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        try {
            SignalSystemTestUtil.createMockSystem();
        } catch (Exception e) {
            log.error("exception creating mock system", e);
        }
    }

    @After
    public void tearDown() {
        try {
            SignalSystemTestUtil.deleteMockSystem();
        } catch (Exception e) {
            log.error("exception deleting mock system", e);
        }
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AddSignalMastPanelTest.class);
}
