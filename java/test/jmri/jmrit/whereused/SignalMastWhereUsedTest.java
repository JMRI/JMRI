package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import javax.swing.JTextArea;
import jmri.InstanceManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the SignalMastWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class SignalMastWhereUsedTest {

    @Test
    public void testSignalMastWhereUsed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SignalMast signalMast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast("Left-A");
        JTextArea result = SignalMastWhereUsed.getWhereUsed(signalMast);
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
