package jmri.util.swing;

import java.awt.Toolkit;
import java.awt.datatransfer.*;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@Timeout(10)
@DisabledIfSystemProperty( named = "java.awt.headless", matches = "true" )
public class ExceptionDisplayFrameTest {

    @Test
    public void testCTorExceptionContext() {
        Thread t = JemmyUtil.createModalDialogOperatorThread("Test Exception Message", "OK");
        ExceptionContext ex = new ExceptionContext(new Exception("Test Exception Message"), "Test Operation", "Test Hint");
        ExceptionDisplayFrame.displayExceptionDisplayFrame(null, ex);
        JUnitUtil.waitFor( () -> !t.isAlive(), "Dialog found and OKd");
    }

    @Test
    public void testCTorException() {
        Thread t = JemmyUtil.createModalDialogOperatorThread("ExceptionDisplayFrameTest testCTor", "OK");
        Exception ex = new Exception("ExceptionDisplayFrameTest testCTor");
        ExceptionDisplayFrame.displayExceptionDisplayFrame(null, ex);
        JUnitUtil.waitFor( () -> !t.isAlive(), "Dialog found and OKd");
    }

    @Test
    public void testCopyExceptionToClipboard() {
        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        systemClipboard.setContents(new StringSelection(""), null);
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator("testCopyExceptionToClipboard");
            JButtonOperator jbo = new JButtonOperator(jdo, Bundle.getMessage("ExceptionDisplayCopyButton"));
            jbo.push();
            
            JButtonOperator ok = new JButtonOperator(jdo, Bundle.getMessage("ButtonOK"));
            ok.pushNoBlock();
        });
        t.setName("ExceptionDisplayCopyButton Dialog Thread");
        t.start();

        ExceptionDisplayFrame.displayExceptionDisplayFrame(null, new Exception("testCopyExceptionToClipboard"));

        JUnitUtil.waitFor( () -> !t.isAlive(), "Dialog found and Ex details copied");
        Assertions.assertDoesNotThrow(() -> {
            String newClip = (String)systemClipboard.getData(DataFlavor.stringFlavor);
            Assertions.assertTrue(newClip.contains("testCopyExceptionToClipboard"));
        });
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
