package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import javax.swing.JTextArea;
import jmri.InstanceManager;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the EntryExitWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class EntryExitWhereUsedTest {

    @Test
    public void testEntryExitWhereUsed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        EntryExitWhereUsed ctor = new EntryExitWhereUsed();
        Assert.assertNotNull("exists", ctor);

        DestinationPoints dp = InstanceManager.getDefault(EntryExitPairs.class).getNamedBean("NX-LeftTO-A (Left-A) to NX-RIghtTO-B (Right-B)");
        JTextArea result = EntryExitWhereUsed.getWhereUsed(dp);
        Assert.assertFalse(result.getText().isEmpty());
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
        java.io.File f = new java.io.File("java/test/jmri/jmrit/whereused/load/WhereUsedTesting.xml");
        cm.load(f);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SensorWhereUsedTest.class);
}
