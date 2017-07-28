package jmri.jmrit.roster;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.GraphicsEnvironment;
import jmri.util.JmriJFrame;
import org.jdom2.JDOMException;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PrintRosterEntryTest {

    @Test
    public void testCTor() throws JDOMException, IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriJFrame jf = new JmriJFrame("TestPrintWindow");
        RosterEntry r = RosterEntry.fromFile(new File("java/test/jmri/jmrit/roster/ACL1012.xml"));
        PrintRosterEntry t = new PrintRosterEntry(r,jf,"xml/programmers/Basic.xml");
        Assert.assertNotNull("exists",t);
        jf.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintRosterEntryTest.class.getName());

}
