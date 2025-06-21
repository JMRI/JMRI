package jmri.jmrit.roster;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

import org.jdom2.JDOMException;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintRosterEntryTest {

    @Test
    public void testCTorDTD() throws JDOMException, IOException {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriJFrame jf = new JmriJFrame("TestPrintWindow");
        RosterEntry r = RosterEntry.fromFile(new File("java/test/jmri/jmrit/roster/ACL1012-DTD.xml"));
        r.setFileName("java/test/jmri/jmrit/roster/ACL1012-DTD.xml");
        PrintRosterEntry t = new PrintRosterEntry(r, jf, "xml/programmers/Basic.xml");
        Assertions.assertNotNull(t, "exists");
        JUnitUtil.dispose(jf);
    }

    @Test
    public void testCTorSchema() throws JDOMException, IOException {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriJFrame jf = new JmriJFrame("TestPrintWindow");
        RosterEntry r = RosterEntry.fromFile(new File("java/test/jmri/jmrit/roster/ACL1012-Schema.xml"));
        r.setFileName("java/test/jmri/jmrit/roster/ACL1012-Schema.xml");
        PrintRosterEntry t = new PrintRosterEntry(r, jf, "xml/programmers/Basic.xml");
        Assertions.assertNotNull(t, "exists");
        JUnitUtil.dispose(jf);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintRosterEntryTest.class);
}
