package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import org.junit.*;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the SensorWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class SensorWhereUsedTest {

    @Test
    public void testSensorMethods() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Sensor sensor = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IT101");
        String result = SensorWhereUsed.checkTurnouts(sensor);
        Assert.assertTrue(result.length() == 0);    // Nothing found

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Feedback-1");
        result = SensorWhereUsed.checkTurnouts(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Light-Control");
        result = SensorWhereUsed.checkLights(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Occupancy");
        result = SensorWhereUsed.checkBlocks(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Main");
        result = SensorWhereUsed.checkSignalHeadLogic(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Light-Control");
        result = SensorWhereUsed.checkPanels(sensor);
        Assert.assertTrue(result.length() > 0);
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
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
