package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the SensorWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class WhereUsedCollectorsTest {

    @Test
    public void testCollectorMethods() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        WhereUsedCollectors ctor = new WhereUsedCollectors();
        Assert.assertNotNull("exists", ctor);

        Sensor sensor = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IT101");
        String result = WhereUsedCollectors.checkTurnouts(sensor);
        Assert.assertTrue(result.length() == 0);    // Nothing found

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Feedback-1");
        result = WhereUsedCollectors.checkTurnouts(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Light-Control");
        result = WhereUsedCollectors.checkLights(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Main");
        result = WhereUsedCollectors.checkRoutes(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Occupancy");
        result = WhereUsedCollectors.checkBlocks(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Main");
        result = WhereUsedCollectors.checkLayoutBlocks(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Main");
        result = WhereUsedCollectors.checkSignalHeadLogic(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-SML-Sensor");
        result = WhereUsedCollectors.checkSignalMastLogic(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Group-Control-1");
        result = WhereUsedCollectors.checkSignalGroups(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-OBlock-Error");
        result = WhereUsedCollectors.checkOBlocks(sensor);
        Assert.assertTrue(result.length() > 0);
        OBlock oblock = InstanceManager.getDefault(OBlockManager.class).getOBlock("OB::Left-TO");
        result = WhereUsedCollectors.checkWarrants(oblock);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("NX-LeftTO-A");
        result = WhereUsedCollectors.checkEntryExit(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Group-Center");
        result = WhereUsedCollectors.checkLogixConditionals(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Fwd");
        result = WhereUsedCollectors.checkSections(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Stop-Allocation");
        result = WhereUsedCollectors.checkTransits(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Transit-When-Action");
        result = WhereUsedCollectors.checkTransits(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Light-Control");
        result = WhereUsedCollectors.checkPanels(sensor);
        Assert.assertTrue(result.length() > 0);

        sensor = InstanceManager.getDefault(jmri.SensorManager.class).getSensor("S-Feedback-1");  // Test switchboard
        result = WhereUsedCollectors.checkPanels(sensor);
        Assert.assertTrue(result.length() > 0);
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

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WhereUsedCollectorsTest.class);
}
