package jmri.jmrit;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Bob Jacobsen 2018
 */
public class XmlFileValidateActionTest {

    private boolean pass;
    private boolean fail;

    @Test
    public void testFileOK() {
        XmlFileValidateAction t = new XmlFileValidateAction() {
            @Override
            protected void showOkResults(Component who, String text) {
                pass = true;
            }
            @Override
            protected void showFailResults(Component who, String fileName, String text) {
                fail = true;
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                 processFile(new File("java/test/jmri/configurexml/valid/RosterSchemaTest.xml"));
            }
        };

        t.actionPerformed(null);

        Assert.assertTrue(pass);
        Assert.assertFalse(fail);
    }

    // should find a "Duplicate key value [LT1] declared" error on or about line 14 of java/test/jmri/configurexml/invalid/TurnoutDuplicateSystemName.xml
    @Test
    public void testFileFails() {
        XmlFileValidateAction t = new XmlFileValidateAction() {
            @Override
            protected void showOkResults(Component who, String text) {
                pass = true;
            }
            @Override
            protected void showFailResults(Component who, String fileName, String text) {
                Assert.assertTrue("check message", text.startsWith("Error on line 14: cvc-identity-constraint.4.2.2: Duplicate key value [LT1]"));
                fail = true;
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                 processFile(new File("java/test/jmri/configurexml/invalid/TurnoutDuplicateSystemName.xml"));
            }
        };

        t.actionPerformed(null);

        Assert.assertTrue(fail);
        Assert.assertFalse(pass);
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testActionPerformed(){
        XmlFileValidateAction t = new XmlFileValidateAction();

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton( "Open", "Cancel"); // not from JMRI Bundle
        });
        t1.setName("click Cancel choose xml file Thread");
        t1.start();
        ThreadingUtil.runOnGUI(() -> t.actionPerformed(null));

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click Cancel Button in XmlFileValidateAction actionPerformed dialogue didn't happen");
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testDisplayOkResults() {
        XmlFileValidateAction t = new XmlFileValidateAction("testDisplayOkResults",(jmri.util.swing.WindowInterface)null);

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton( "Message", "OK"); // not from JMRI Bundle
        });
        t1.setName("click OK after OK Results Thread");
        t1.start();
        ThreadingUtil.runOnGUI(() -> t.showOkResults(null, "Test OK Text"));

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click ok Button in XmlFileValidateAction showOkResults dialogue didn't happen");
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testDisplayFailResults() {
        XmlFileValidateAction t = new XmlFileValidateAction();
        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton( Bundle.getMessage("ValidationErrorInFile", "testFileName"), "OK"); // not from JMRI Bundle
        });
        t1.setName("click OK after Fail Results Thread");
        t1.start();
        ThreadingUtil.runOnGUI(() -> t.showFailResults(null,"testFileName", "Test Failure Text"));

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click ok Button in XmlFileValidateAction showFailResults dialogue didn't happen");
    }

    @Test
    public void testNotPartOfAPanel() {
        XmlFileValidateAction t = new XmlFileValidateAction();
        Exception ex = Assertions.assertThrows(IllegalArgumentException.class, () -> { t.makePanel(); });
        Assertions.assertNotNull(ex);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        pass = false;
        fail = false;
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(XmlFileValidateActionTest.class);

}
