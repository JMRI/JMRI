package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import javax.swing.JTextArea;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the SensorWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class SensorWhereUsedTest {

    @Test
    public void testSensorWhereUsed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        SensorWhereUsed ctor = new SensorWhereUsed();
        Assert.assertNotNull("exists", ctor);

//         Sensor sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Feedback-1");
//         JTextArea result = SensorWhereUsed.getWhereUsed(sensor);
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

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SensorWhereUsedTest.class);
}
