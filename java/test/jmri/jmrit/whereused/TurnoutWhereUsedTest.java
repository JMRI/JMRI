package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import javax.swing.JTextArea;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the TurnoutWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class TurnoutWhereUsedTest {

    @Test
    public void testTurnoutWhereUsed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Turnout turnout = InstanceManager.getDefault(jmri.TurnoutManager.class).getTurnout("LE Left");
        JTextArea result = TurnoutWhereUsed.getWhereUsed(turnout);
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
