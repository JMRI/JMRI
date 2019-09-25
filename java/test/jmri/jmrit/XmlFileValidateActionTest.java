package jmri.jmrit;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
                Assert.assertTrue("check message", text.startsWith("Error on line 14: cvc-identity-constraint.4.2.2: Duplicate key value [LT1] declared for identity constraint \"turnoutName\""));
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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        pass = false;
        fail = false;
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(XmlFileValidateActionTest.class);

}
