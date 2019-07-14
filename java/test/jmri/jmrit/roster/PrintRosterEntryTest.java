package jmri.jmrit.roster;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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
        r.setFileName("java/test/jmri/jmrit/roster/ACL1012.xml");
        PrintRosterEntry t = new PrintRosterEntry(r,jf,"xml/programmers/Basic.xml");
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(jf);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintRosterEntryTest.class);

}
