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
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        List<Integer> tsList = cu.getTurnoutSettingList();
        int setting = tsList.get(0).intValue();
        Assert.assertEquals("6_4_5Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("5");
        nBlock = bm.getBlock("6");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("5_4_6Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("5");
        nBlock = bm.getBlock("2");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("5_4_2Connect", setting, Turnout.THROWN);

        cBlock = bm.getBlock("2");
        pBlock = bm.getBlock("1");
        nBlock = bm.getBlock("3");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("1_2_3Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("3");
        nBlock = bm.getBlock("1");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("3_2_1Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("1");
        nBlock = bm.getBlock("4");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("1_2_4Connect", setting, Turnout.THROWN);

        // Test left-handed crossover connectivity turnout settings
        cBlock = bm.getBlock("14");
        pBlock = bm.getBlock("13");
        nBlock = bm.getBlock("17");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("13_14_17Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("17");
        nBlock = bm.getBlock("13");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("17_14_13Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("17");
        nBlock = bm.getBlock("12");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("17_14_12Connect", setting, Turnout.THROWN);

        cBlock = bm.getBlock("12");
        pBlock = bm.getBlock("11");
        nBlock = bm.getBlock("15");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("11_12_15Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("15");
        nBlock = bm.getBlock("11");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("15_12_11Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("15");
        nBlock = bm.getBlock("14");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("15_12_14Connect", setting, Turnout.THROWN);

        // Test double crossover connectivity turnout settings
        cBlock = bm.getBlock("21");
        pBlock = bm.getBlock("20");
        nBlock = bm.getBlock("22");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("20_21_22Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("22");
        nBlock = bm.getBlock("20");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("22_21_20Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("20");
        nBlock = bm.getBlock("26");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("20_21_26Connect", setting, Turnout.THROWN);

        cBlock = bm.getBlock("22");
        pBlock = bm.getBlock("23");
        nBlock = bm.getBlock("21");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("23_22_21Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("21");
        nBlock = bm.getBlock("23");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("21_22_23Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("23");
        nBlock = bm.getBlock("25");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("23_22_25Connect", setting, Turnout.THROWN);

        cBlock = bm.getBlock("26");
        pBlock = bm.getBlock("27");
        nBlock = bm.getBlock("25");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("27_26_25Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("25");
        nBlock = bm.getBlock("27");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("25_26_27Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("27");
        nBlock = bm.getBlock("21");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("27_26_21Connect", setting, Turnout.THROWN);

        cBlock = bm.getBlock("25");
        pBlock = bm.getBlock("24");
        nBlock = bm.getBlock("26");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("24_25_26Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("26");
        nBlock = bm.getBlock("24");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("26_25_24Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("24");
        nBlock = bm.getBlock("22");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("24_25_22Connect", setting, Turnout.THROWN);

        // Test right handed turnout (with "wings" in same block) connectivity turnout settings
        cBlock = bm.getBlock("62");
        pBlock = bm.getBlock("64");
        nBlock = bm.getBlock("61");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("64_62_61Connect", setting, Turnout.THROWN);

        pBlock = bm.getBlock("61");
        nBlock = bm.getBlock("64");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("61_62_64Connect", setting, Turnout.THROWN);

        pBlock = bm.getBlock("63");
        nBlock = bm.getBlock("61");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("63_62_61Connect", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("61");
        nBlock = bm.getBlock("63");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("61_62_63Connect", setting, Turnout.CLOSED);

        // Test extended track following connectivity turnout settings
        //   Each path must go through two turnouts, whose settings are tested in order
        cBlock = bm.getBlock("32");
        pBlock = bm.getBlock("31");
        nBlock = bm.getBlock("33");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("31_32_33ConnectA", setting, Turnout.CLOSED);
        setting = tsList.get(1).intValue();
        Assert.assertEquals("31_32_33ConnectB", setting, Turnout.THROWN);

        pBlock = bm.getBlock("33");
        nBlock = bm.getBlock("31");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("33_32_31ConnectA", setting, Turnout.THROWN);
        setting = tsList.get(1).intValue();
        Assert.assertEquals("33_32_31ConnectB", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("31");
        nBlock = bm.getBlock("34");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("31_32_34ConnectA", setting, Turnout.CLOSED);
        setting = tsList.get(1).intValue();
        Assert.assertEquals("31_32_34ConnectB", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("34");
        nBlock = bm.getBlock("31");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("34_32_31ConnectA", setting, Turnout.CLOSED);
        setting = tsList.get(1).intValue();
        Assert.assertEquals("34_32_31ConnectB", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("31");
        nBlock = bm.getBlock("35");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("31_32_35ConnectA", setting, Turnout.THROWN);
        setting = tsList.get(1).intValue();
        Assert.assertEquals("31_32_35ConnectB", setting, Turnout.CLOSED);

        pBlock = bm.getBlock("35");
        nBlock = bm.getBlock("31");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("35_32_31ConnectA", setting, Turnout.CLOSED);
        setting = tsList.get(1).intValue();
        Assert.assertEquals("35_32_31ConnectB", setting, Turnout.THROWN);

        pBlock = bm.getBlock("31");
        nBlock = bm.getBlock("36");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("31_32_36ConnectA", setting, Turnout.THROWN);
        setting = tsList.get(1).intValue();
        Assert.assertEquals("31_32_36ConnectB", setting, Turnout.THROWN);

        pBlock = bm.getBlock("36");
        nBlock = bm.getBlock("31");
        cu.getTurnoutList(cBlock, pBlock, nBlock);
        tsList = cu.getTurnoutSettingList();
        setting = tsList.get(0).intValue();
        Assert.assertEquals("36_32_31ConnectA", setting, Turnout.THROWN);
        setting = tsList.get(1).intValue();
        Assert.assertEquals("36_32_31ConnectB", setting, Turnout.THROWN);

        // and close the window
        to.closeFrameWithConfirmations();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initShutDownManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
