package jmri.jmrix.dccpp.swing.mon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import jmri.jmrix.dccpp.*;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of SerialMonFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true" )
public class DCCppMonFrameTest extends jmri.util.JmriJFrameTestBase {

    @Test
    public void testMessageReplyInTextArea(){

        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
            frame.setVisible(true);
        });
        JFrameOperator jfo = new JFrameOperator(frame);
        new JCheckBoxOperator(jfo,Bundle.getMessage("ButtonShowRaw")).setSelected(true);
        new JCheckBoxOperator(jfo,Bundle.getMessage("ButtonShowTranslation")).setSelected(true);

        ((DCCppMonFrame)frame).message(DCCppReply.parseDCCppReply("* this is a test diagnostic message 12345 *"));
        ((DCCppMonFrame)frame).message(DCCppMessage.makeWriteOpsModeCVMsg(17, 4, 3));

        JUnitUtil.waitFor(() -> ((DCCppMonFrame)frame).getTextArea().getText().contains("this is a test diagnostic message 12345"),
            "reply appears in textarea");

        JUnitUtil.waitFor(() -> ((DCCppMonFrame)frame).getTextArea().getText().contains("Ops Write Byte Cmd: Address: 17, CV: 4, Value: 3"),
            "message appears in textarea");

        jfo.requestClose();
        jfo.waitClosed();

    }

    @Test
    public void testCheckBoxesButtonsVisible(){

        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
            frame.setVisible(true);
        });
        JFrameOperator jfo = new JFrameOperator(frame);
        Assertions.assertTrue(new JCheckBoxOperator(jfo,Bundle.getMessage("ButtonShowRaw")).isVisible());
        Assertions.assertTrue(new JCheckBoxOperator(jfo,Bundle.getMessage("ButtonShowTranslation")).isVisible());
        Assertions.assertTrue(new JCheckBoxOperator(jfo,Bundle.getMessage("ButtonShowTimestamps")).isVisible());
        Assertions.assertTrue(new JCheckBoxOperator(jfo,Bundle.getMessage("ButtonWindowOnTop")).isVisible());
        Assertions.assertTrue(new JCheckBoxOperator(jfo,Bundle.getMessage("ButtonAutoScroll")).isVisible());

        Assertions.assertTrue(new JButtonOperator(jfo,Bundle.getMessage("ButtonClearScreen")).isVisible());
        Assertions.assertTrue(new JToggleButtonOperator(jfo,Bundle.getMessage("ButtonFreezeScreen")).isVisible());
        Assertions.assertTrue(new JButtonOperator(jfo,Bundle.getMessage("ButtonAddMessage")).isVisible());
        Assertions.assertTrue(new JButtonOperator(jfo,Bundle.getMessage("ButtonChooseLogFile")).isVisible());
        Assertions.assertTrue(new JButtonOperator(jfo,Bundle.getMessage("ButtonStartLogging")).isVisible());

        new JCheckBoxOperator(jfo,Bundle.getMessage("ButtonWindowOnTop")).doClick();
        new JCheckBoxOperator(jfo,Bundle.getMessage("ButtonAutoScroll")).doClick();

        jfo.requestClose();
        jfo.waitClosed();

    }

    @Test
    public void testUserText(){

        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
            frame.setVisible(true);
        });
        JFrameOperator jfo = new JFrameOperator(frame);

        new JCheckBoxOperator(jfo,Bundle.getMessage("ButtonShowTimestamps")).doClick(); // to enable
        new JTextFieldOperator(jfo, 0).setText("User Text to add to console");
        new JButtonOperator(jfo,Bundle.getMessage("ButtonAddMessage")).doClick();
        JUnitUtil.waitFor(() -> ((DCCppMonFrame)frame).getTextArea().getText().contains("User Text to add to console"),
            "user message appears in textarea");

        new JButtonOperator(jfo,Bundle.getMessage("ButtonClearScreen")).doClick();
        JUnitUtil.waitFor(() -> (((DCCppMonFrame)frame).getTextArea().getText().isEmpty()),
            "button clears content");

        new JTextFieldOperator(jfo, 0).setText("User Text to add to console log file");
        Thread t1 = JemmyUtil.createModalDialogOperatorThread("Save", "Save");
        new JButtonOperator(jfo,Bundle.getMessage("ButtonChooseLogFile")).doClick();
        JUnitUtil.waitFor(() -> {return !t1.isAlive();},"log file selection dialogue did not complete");
        
        new JButtonOperator(jfo,Bundle.getMessage("ButtonStartLogging")).doClick();
        new JButtonOperator(jfo,Bundle.getMessage("ButtonAddMessage")).doClick();
        JUnitUtil.waitFor(() -> (!((DCCppMonFrame)frame).getTextArea().getText().isEmpty()),
            "message added to content");
        new JButtonOperator(jfo,Bundle.getMessage("ButtonStopLogging")).doClick();
        Assertions.assertTrue(assertFileContainsString(tempDir+File.separator+"monitorLog.txt","User Text to add to console log file"));

        jfo.requestClose();
        jfo.waitClosed();
    }

    private static boolean assertFileContainsString(String filePath, String searchString) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(searchString)) {
                    return true;
                }
            }
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }
        return false;
    }

    @TempDir File tempDir;

    private DCCppSystemConnectionMemo memo = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        try {
            JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir));
        } catch (IOException ex){
            Assertions.fail("could not create temp profile", ex);
        }
        jmri.jmrix.dccpp.DCCppInterfaceScaffold t = new jmri.jmrix.dccpp.DCCppInterfaceScaffold(new jmri.jmrix.dccpp.DCCppCommandStation());
        memo = new DCCppSystemConnectionMemo(t);
        frame = new DCCppMonFrame(memo);
    }

    @AfterEach
    @Override
    public void tearDown() {
        Assertions.assertNotNull(memo);
        memo.getDCCppTrafficController().terminateThreads();
        memo.dispose();
        memo = null;
        super.tearDown();
    }

}
