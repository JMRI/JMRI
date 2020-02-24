package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import javax.swing.JTextArea;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the LightWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class LightWhereUsedTest {

    @Test
    public void testTurnoutWhereUsed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LightWhereUsed ctor = new LightWhereUsed();
        Assert.assertNotNull("exists", ctor);

//         Light light = InstanceManager.getDefault(jmri.LightManager.class).getLight("L-Sensor Control");
//         JTextArea result = LightWhereUsed.getWhereUsed(light);
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

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LightWhereUsedTest.class);
}
