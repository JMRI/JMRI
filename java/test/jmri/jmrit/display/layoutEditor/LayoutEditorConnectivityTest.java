package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.util.List;
import jmri.Block;
import jmri.BlockManager;
import jmri.Turnout;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Swing tests for the LayoutEditor
 *
 * @author	Dave Duchamp Copyright 2011
 */
public class LayoutEditorConnectivityTest {

    @Test
    public void testShowAndClose() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };

        // load and display test panel file
        java.io.File f = new java.io.File("java/test/jmri/jmrit/display/layoutEditor/valid/LEConnectTest.xml");
        cm.load(f);

        // Find new window by name (should be more distinctive, comes from sample file)
        EditorFrameOperator to = new EditorFrameOperator("Connectivity Test");

        LayoutEditor le = (LayoutEditor) jmri.util.JmriJFrame.getFrame("Connectivity Test");
        Assert.assertNotNull(le);

        // Panel is up, continue set up for tests.
        ConnectivityUtil cu = new ConnectivityUtil(le);
        Assert.assertNotNull(cu);
        BlockManager bm = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
        Assert.assertNotNull(bm);

        // Test right-handed crossover connectivity turnout settings
        Block cBlock = bm.getBlock("4");
        Block pBlock = bm.getBlock("6");
        Block nBlock = bm.getBlock("5");
        List <LayoutTrackExpectedState<LayoutTurnout>> tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        int setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("6_4_5Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("5");
        nBlock = bm.getBlock("6");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("5_4_6Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("5");
        nBlock = bm.getBlock("2");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("5_4_2Connect", Turnout.THROWN, setting);

        cBlock = bm.getBlock("2");
        pBlock = bm.getBlock("1");
        nBlock = bm.getBlock("3");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("1_2_3Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("3");
        nBlock = bm.getBlock("1");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("3_2_1Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("1");
        nBlock = bm.getBlock("4");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("1_2_4Connect", Turnout.THROWN, setting);

        // Test left-handed crossover connectivity turnout settings
        cBlock = bm.getBlock("14");
        pBlock = bm.getBlock("13");
        nBlock = bm.getBlock("17");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("13_14_17Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("17");
        nBlock = bm.getBlock("13");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("17_14_13Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("17");
        nBlock = bm.getBlock("12");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("17_14_12Connect", Turnout.THROWN, setting);

        cBlock = bm.getBlock("12");
        pBlock = bm.getBlock("11");
        nBlock = bm.getBlock("15");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("11_12_15Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("15");
        nBlock = bm.getBlock("11");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("15_12_11Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("15");
        nBlock = bm.getBlock("14");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("15_12_14Connect", Turnout.THROWN, setting);

        // Test double crossover connectivity turnout settings
        cBlock = bm.getBlock("21");
        pBlock = bm.getBlock("20");
        nBlock = bm.getBlock("22");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("20_21_22Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("22");
        nBlock = bm.getBlock("20");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("22_21_20Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("20");
        nBlock = bm.getBlock("26");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("20_21_26Connect", Turnout.THROWN, setting);

        cBlock = bm.getBlock("22");
        pBlock = bm.getBlock("23");
        nBlock = bm.getBlock("21");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("23_22_21Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("21");
        nBlock = bm.getBlock("23");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("21_22_23Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("23");
        nBlock = bm.getBlock("25");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("23_22_25Connect", Turnout.THROWN, setting);

        cBlock = bm.getBlock("26");
        pBlock = bm.getBlock("27");
        nBlock = bm.getBlock("25");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("27_26_25Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("25");
        nBlock = bm.getBlock("27");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("25_26_27Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("27");
        nBlock = bm.getBlock("21");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("27_26_21Connect", Turnout.THROWN, setting);

        cBlock = bm.getBlock("25");
        pBlock = bm.getBlock("24");
        nBlock = bm.getBlock("26");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("24_25_26Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("26");
        nBlock = bm.getBlock("24");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("26_25_24Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("24");
        nBlock = bm.getBlock("22");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("24_25_22Connect", Turnout.THROWN, setting);

        // Test right handed turnout (with "wings" in same block) connectivity turnout settings
        cBlock = bm.getBlock("62");
        pBlock = bm.getBlock("64");
        nBlock = bm.getBlock("61");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("64_62_61Connect", Turnout.THROWN, setting);

        pBlock = bm.getBlock("61");
        nBlock = bm.getBlock("64");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("61_62_64Connect", Turnout.THROWN, setting);

        pBlock = bm.getBlock("63");
        nBlock = bm.getBlock("61");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("63_62_61Connect", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("61");
        nBlock = bm.getBlock("63");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("61_62_63Connect", Turnout.CLOSED, setting);

        // Test extended track following connectivity turnout settings
        //   Each path must go through two turnouts, whose settings are tested in order
        cBlock = bm.getBlock("32");
        pBlock = bm.getBlock("31");
        nBlock = bm.getBlock("33");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("31_32_33ConnectA", Turnout.CLOSED, setting);
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("31_32_33ConnectB", Turnout.THROWN, setting);

        pBlock = bm.getBlock("33");
        nBlock = bm.getBlock("31");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("33_32_31ConnectA", Turnout.THROWN, setting);
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("33_32_31ConnectB", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("31");
        nBlock = bm.getBlock("34");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("31_32_34ConnectA", Turnout.CLOSED, setting);
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("31_32_34ConnectB", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("34");
        nBlock = bm.getBlock("31");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("34_32_31ConnectA", Turnout.CLOSED, setting);
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("34_32_31ConnectB", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("31");
        nBlock = bm.getBlock("35");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("31_32_35ConnectA", Turnout.THROWN, setting);
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("31_32_35ConnectB", Turnout.CLOSED, setting);

        pBlock = bm.getBlock("35");
        nBlock = bm.getBlock("31");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("35_32_31ConnectA", Turnout.CLOSED, setting);
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("35_32_31ConnectB", Turnout.THROWN, setting);

        pBlock = bm.getBlock("31");
        nBlock = bm.getBlock("36");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("31_32_36ConnectA", Turnout.THROWN, setting);
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("31_32_36ConnectB", Turnout.THROWN, setting);

        pBlock = bm.getBlock("36");
        nBlock = bm.getBlock("31");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("36_32_31ConnectA", Turnout.THROWN, setting);
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("36_32_31ConnectB", Turnout.THROWN, setting);

        // and close the window
        to.closeFrameWithConfirmations();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
