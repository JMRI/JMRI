package apps.jmrit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Bob Jacobsen 2018
 */
public class XmlFileValidateRunnerTest {

    private boolean pass;
    private boolean fail;

    @Test
    public void testFileOK() {
        XmlFileValidateRunner t = new XmlFileValidateRunner() {
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

        assertTrue(pass);
        assertFalse(fail);
    }

    // should find a "Duplicate key value [LT1] declared" error on or about line 14 of java/test/jmri/configurexml/invalid/TurnoutDuplicateSystemName.xml
    @Test
    public void testFileFails() {
        XmlFileValidateRunner t = new XmlFileValidateRunner() {
            @Override
            protected void showOkResults(Component who, String text) {
                pass = true;
            }
            @Override
            protected void showFailResults(Component who, String fileName, String text) {
                assertTrue( text.startsWith("Error on line 14: cvc-identity-constraint.4.2.2: Duplicate key value [LT1]"),
                    "check message");
                fail = true;
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                 processFile(new File("java/test/jmri/configurexml/invalid/TurnoutDuplicateSystemName.xml"));
            }
        };

        t.actionPerformed(null);

        assertTrue(fail);
        assertFalse(pass);
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

    // private final static Logger log = LoggerFactory.getLogger(XmlFileValidateRunnerTest.class);

}
