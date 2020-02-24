package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import javax.swing.JTextArea;
import jmri.InstanceManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the ReporterWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class ReporterWhereUsedTest {

    @Test
    public void testReporterWhereUsed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ReporterWhereUsed ctor = new ReporterWhereUsed();
        Assert.assertNotNull("exists", ctor);
//         Reporter reporter = InstanceManager.getDefault(jmri.ReporterManager.class).getReporter("Test Reporter");
//         JTextArea result = ReporterWhereUsed.getWhereUsed(reporter);
//         Assert.assertFalse(result.getText().isEmpty());
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
//         JUnitUtil.resetProfileManager();
//         JUnitUtil.initRosterConfigManager();
//         Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//         jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
//         java.io.File f = new java.io.File("java/test/jmri/jmrit/whereused/load/WhereUsedTesting.xml");
//         cm.load(f);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReporterWhereUsedTest.class);
}
