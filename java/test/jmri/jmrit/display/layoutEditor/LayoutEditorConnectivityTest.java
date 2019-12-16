package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.List;
import jmri.Block;
import jmri.BlockManager;
import jmri.Turnout;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;
import jmri.util.junit.rules.RetryRule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.Operator;

/**
 * Swing tests for the LayoutEditor
 *
 * @author	Dave Duchamp Copyright 2011
 * @author      George Warner Copyright (C) 2019
 */
public class LayoutEditorConnectivityTest {

    @Rule   //5 second timeout for methods in this test class.
    public Timeout globalTimeout = Timeout.seconds(5);

    @Rule   //allow 3 retries of intermittent tests
    public RetryRule retryRule = new RetryRule(3);

    private static EditorFrameOperator layoutEditorFrameOperator = null;
    private static LayoutEditor layoutEditor = null;
    private static ConnectivityUtil connectivityUtil = null;
    private static BlockManager blockManager = null;

    @Test
    public void testRHXover() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditorFrameOperator", layoutEditorFrameOperator);
        Assert.assertNotNull("layoutEditor", layoutEditor);
        Assert.assertNotNull("connectivityUtil", connectivityUtil);
        Assert.assertNotNull("blockManager", blockManager);

        Block block1 = blockManager.getBlock("1");
        Assert.assertNotNull("block1", block1);
        Block block2 = blockManager.getBlock("2");
        Assert.assertNotNull("block2", block2);
        Block block3 = blockManager.getBlock("3");
        Assert.assertNotNull("block3", block3);
        Block block4 = blockManager.getBlock("4");
        Assert.assertNotNull("block4", block4);
        Block block5 = blockManager.getBlock("5");
        Assert.assertNotNull("block5", block5);
        Block block6 = blockManager.getBlock("6");
        Assert.assertNotNull("block6", block6);

        //Test right-handed crossover connectivity turnout settings
        Block cBlock = block4;
        Block pBlock = block6;
        Block nBlock = block5;
        List<LayoutTrackExpectedState<LayoutTurnout>> tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("6_4_5Connect turnout", "TO1", tsList.get(0).getObject().getName());
        int setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("6_4_5Connect state", Turnout.CLOSED, setting);

        pBlock = block5;
        nBlock = block6;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("5_4_6Connect turnout", "TO1", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("5_4_6Connect state", Turnout.CLOSED, setting);

        pBlock = block5;
        nBlock = block2;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("5_4_2Connect turnout", "TO1", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("5_4_2Connect state", Turnout.THROWN, setting);

        cBlock = block2;
        pBlock = block1;
        nBlock = block3;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("1_2_3Connect turnout", "TO1", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("1_2_3Connect state", Turnout.CLOSED, setting);

        pBlock = block3;
        nBlock = block1;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("3_2_1Connect turnout", "TO1", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("3_2_1Connect state", Turnout.CLOSED, setting);

        pBlock = block1;
        nBlock = block4;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("1_2_4Connect turnout", "TO1", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("1_2_4Connect state", Turnout.THROWN, setting);
    }

    @Test
    public void testLHXover() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditorFrameOperator", layoutEditorFrameOperator);
        Assert.assertNotNull("layoutEditor", layoutEditor);
        Assert.assertNotNull("connectivityUtil", connectivityUtil);
        Assert.assertNotNull("blockManager", blockManager);

        Block block11 = blockManager.getBlock("11");
        Assert.assertNotNull("block11", block11);
        Block block12 = blockManager.getBlock("12");
        Assert.assertNotNull("block12", block12);
        Block block13 = blockManager.getBlock("13");
        Assert.assertNotNull("block13", block13);
        Block block14 = blockManager.getBlock("14");
        Assert.assertNotNull("block14", block14);
        Block block15 = blockManager.getBlock("15");
        Assert.assertNotNull("block15", block15);
        Block block17 = blockManager.getBlock("17");
        Assert.assertNotNull("block17", block17);

        //Test left-handed crossover connectivity turnout settings
        Block cBlock = block14;
        Block pBlock = block13;
        Block nBlock = block17;
        List<LayoutTrackExpectedState<LayoutTurnout>> tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("13_14_17Connect turnout", "TO2", tsList.get(0).getObject().getName());
        int setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("13_14_17Connect state", Turnout.CLOSED, setting);

        pBlock = block17;
        nBlock = block13;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("17_14_13Connect turnout", "TO2", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("17_14_13Connect state", Turnout.CLOSED, setting);

        pBlock = block17;
        nBlock = block12;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("17_14_12Connect turnout", "TO2", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("17_14_12Connect state", Turnout.THROWN, setting);

        cBlock = block12;
        pBlock = block11;
        nBlock = block15;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("11_12_15Connect turnout", "TO2", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("11_12_15Connect state", Turnout.CLOSED, setting);

        pBlock = block15;
        nBlock = block11;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("15_12_11Connect turnout", "TO2", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("15_12_11Connect state", Turnout.CLOSED, setting);

        pBlock = block15;
        nBlock = block14;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("15_12_14Connect turnout", "TO2", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("15_12_14Connect state", Turnout.THROWN, setting);
    }

    @Test
    public void testDoubleXover() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditorFrameOperator", layoutEditorFrameOperator);
        Assert.assertNotNull("layoutEditor", layoutEditor);
        Assert.assertNotNull("connectivityUtil", connectivityUtil);
        Assert.assertNotNull("blockManager", blockManager);

        Block block20 = blockManager.getBlock("20");
        Assert.assertNotNull("block20", block20);
        Block block21 = blockManager.getBlock("21");
        Assert.assertNotNull("block21", block21);
        Block block22 = blockManager.getBlock("22");
        Assert.assertNotNull("block22", block22);
        Block block23 = blockManager.getBlock("23");
        Assert.assertNotNull("block23", block23);
        Block block24 = blockManager.getBlock("24");
        Assert.assertNotNull("block24", block24);
        Block block25 = blockManager.getBlock("25");
        Assert.assertNotNull("block25", block25);
        Block block26 = blockManager.getBlock("26");
        Assert.assertNotNull("block26", block26);
        Block block27 = blockManager.getBlock("27");
        Assert.assertNotNull("block27", block27);

        //Test double crossover connectivity turnout settings
        Block cBlock = block21;
        Block pBlock = block20;
        Block nBlock = block22;
        List<LayoutTrackExpectedState<LayoutTurnout>> tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("20_21_22Connect turnout", "TO3", tsList.get(0).getObject().getName());
        int setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("20_21_22Connect state", Turnout.CLOSED, setting);

        pBlock = block22;
        nBlock = block20;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("22_21_20Connect turnout", "TO3", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("22_21_20Connect state", Turnout.CLOSED, setting);

        pBlock = block20;
        nBlock = block26;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("20_21_26Connect turnout", "TO3", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("20_21_26Connect state", Turnout.THROWN, setting);

        cBlock = block22;
        pBlock = block23;
        nBlock = block21;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("23_22_21Connect turnout", "TO3", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("23_22_21Connect state", Turnout.CLOSED, setting);

        pBlock = block21;
        nBlock = block23;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("21_22_23Connect turnout", "TO3", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("21_22_23Connect state", Turnout.CLOSED, setting);

        pBlock = block23;
        nBlock = block25;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("23_22_25Connect turnout", "TO3", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("23_22_25Connect state", Turnout.THROWN, setting);

        cBlock = block26;
        pBlock = block27;
        nBlock = block25;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("27_26_25Connect turnout", "TO3", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("27_26_25Connect state", Turnout.CLOSED, setting);

        pBlock = block25;
        nBlock = block27;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("25_26_27Connect turnout", "TO3", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("25_26_27Connect state", Turnout.CLOSED, setting);

        pBlock = block27;
        nBlock = block21;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("27_26_21Connect turnout", "TO3", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("27_26_21Connect state", Turnout.THROWN, setting);

        cBlock = block25;
        pBlock = block24;
        nBlock = block26;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("24_25_26Connect turnout", "TO3", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("24_25_26Connect state", Turnout.CLOSED, setting);

        pBlock = block26;
        nBlock = block24;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("26_25_24Connect turnout", "TO3", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("26_25_24Connect state", Turnout.CLOSED, setting);

        pBlock = block24;
        nBlock = block22;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("24_25_22Connect turnout", "TO3", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("24_25_22Connect state", Turnout.THROWN, setting);
    }

    @Test
    public void testYard() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditorFrameOperator", layoutEditorFrameOperator);
        Assert.assertNotNull("layoutEditor", layoutEditor);
        Assert.assertNotNull("connectivityUtil", connectivityUtil);
        Assert.assertNotNull("blockManager", blockManager);

        Block block31 = blockManager.getBlock("31");
        Assert.assertNotNull("block31", block31);
        Block block32 = blockManager.getBlock("32");
        Assert.assertNotNull("block32", block32);
        Block block33 = blockManager.getBlock("33");
        Assert.assertNotNull("block33", block33);
        Block block34 = blockManager.getBlock("34");
        Assert.assertNotNull("block34", block34);
        Block block35 = blockManager.getBlock("35");
        Assert.assertNotNull("block35", block35);
        Block block36 = blockManager.getBlock("36");
        Assert.assertNotNull("block36", block36);

        //Test extended track following connectivity turnout settings
        //Each path must go through two turnouts, whose settings are tested in order
        Block cBlock = block32;
        Block pBlock = block31;
        Block nBlock = block33;
        List<LayoutTrackExpectedState<LayoutTurnout>> tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 2, tsList.size());
        Assert.assertEquals("31_32_33ConnectA turnout", "TO5", tsList.get(0).getObject().getName());
        int setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("31_32_33ConnectA state", Turnout.CLOSED, setting);
        Assert.assertEquals("31_32_33ConnectB turnout", "TO6", tsList.get(1).getObject().getName());
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("31_32_33ConnectB state", Turnout.THROWN, setting);

        cBlock = block32;
        pBlock = block33;
        nBlock = block31;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 2, tsList.size());
        Assert.assertEquals("33_32_31ConnectA turnout", "TO6", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("33_32_31ConnectA state", Turnout.THROWN, setting);
        Assert.assertEquals("33_32_31ConnectB turnout", "TO5", tsList.get(1).getObject().getName());
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("33_32_31ConnectB state", Turnout.CLOSED, setting);

        cBlock = block32;
        pBlock = block31;
        nBlock = block34;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 2, tsList.size());
        Assert.assertEquals("31_32_34ConnectA turnout", "TO5", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("31_32_34ConnectA state", Turnout.CLOSED, setting);
        Assert.assertEquals("31_32_34ConnectB turnout", "TO6", tsList.get(1).getObject().getName());
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("31_32_34ConnectB state", Turnout.CLOSED, setting);

        cBlock = block32;
        pBlock = block34;
        nBlock = block31;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 2, tsList.size());
        Assert.assertEquals("34_32_31ConnectA turnout", "TO6", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("34_32_31ConnectA state", Turnout.CLOSED, setting);
        Assert.assertEquals("34_32_31ConnectB turnout", "TO5", tsList.get(1).getObject().getName());
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("34_32_31ConnectB state", Turnout.CLOSED, setting);

        cBlock = block32;
        pBlock = block31;
        nBlock = block35;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 2, tsList.size());
        Assert.assertEquals("31_32_35ConnectA turnout", "TO5", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("31_32_35ConnectA state", Turnout.THROWN, setting);
        Assert.assertEquals("31_32_35ConnectB turnout", "TO7", tsList.get(1).getObject().getName());
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("31_32_35ConnectB state", Turnout.CLOSED, setting);

        cBlock = block32;
        pBlock = block35;
        nBlock = block31;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 2, tsList.size());
        Assert.assertEquals("35_32_31ConnectA turnout", "TO7", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("35_32_31ConnectA state", Turnout.CLOSED, setting);
        Assert.assertEquals("35_32_31ConnectB turnout", "TO5", tsList.get(1).getObject().getName());
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("35_32_31ConnectB state", Turnout.THROWN, setting);

        cBlock = block32;
        pBlock = block31;
        nBlock = block36;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 2, tsList.size());
        Assert.assertEquals("31_32_36ConnectA turnout", "TO5", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("31_32_36ConnectA state", Turnout.THROWN, setting);
        Assert.assertEquals("31_32_36ConnectB turnout", "TO7", tsList.get(1).getObject().getName());
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("31_32_36ConnectB state", Turnout.THROWN, setting);

        cBlock = block32;
        pBlock = block36;
        nBlock = block31;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 2, tsList.size());
        Assert.assertEquals("36_32_31ConnectA turnout", "TO7", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("36_32_31ConnectA state", Turnout.THROWN, setting);
        Assert.assertEquals("36_32_31ConnectB turnout", "TO5", tsList.get(1).getObject().getName());
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("36_32_31ConnectB state", Turnout.THROWN, setting);

        //You can't get there from here! How is this working?!?
        cBlock = block32;
        pBlock = block33;
        nBlock = block36;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 2, tsList.size());
        Assert.assertEquals("33_36_31ConnectA turnout", "TO6", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("33_36_31ConnectA state", Turnout.THROWN, setting);
        Assert.assertEquals("33_36_31ConnectB turnout", "TO5", tsList.get(1).getObject().getName());
        setting = tsList.get(1).getExpectedState();
        Assert.assertEquals("33_36_31ConnectB state", Turnout.CLOSED, setting);
    }

    @Test
    public void testXing() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditorFrameOperator", layoutEditorFrameOperator);
        Assert.assertNotNull("layoutEditor", layoutEditor);
        Assert.assertNotNull("connectivityUtil", connectivityUtil);
        Assert.assertNotNull("blockManager", blockManager);

        Block block40 = blockManager.getBlock("40");
        Assert.assertNotNull("block40", block40);
        Block block41 = blockManager.getBlock("41");
        Assert.assertNotNull("block41", block41);
        Block block42 = blockManager.getBlock("42");
        Assert.assertNotNull("block42", block42);
        Block block43 = blockManager.getBlock("43");
        Assert.assertNotNull("block43", block43);
        Block block44 = blockManager.getBlock("44");
        Assert.assertNotNull("block44", block44);
        Block block45 = blockManager.getBlock("45");
        Assert.assertNotNull("block45", block45);

        //Test xing
        Block cBlock = block40;
        Block pBlock = block42;
        Block nBlock = block44;
        List<LayoutTrackExpectedState<LayoutTurnout>> tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 0, tsList.size());

        cBlock = block40;
        pBlock = block44;
        nBlock = block42;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 0, tsList.size());

        cBlock = block41;
        pBlock = block43;
        nBlock = block45;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 0, tsList.size());

        cBlock = block41;
        pBlock = block45;
        nBlock = block43;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 0, tsList.size());
    }

    @Test
    public void testRHTurnout() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditorFrameOperator", layoutEditorFrameOperator);
        Assert.assertNotNull("layoutEditor", layoutEditor);
        Assert.assertNotNull("connectivityUtil", connectivityUtil);
        Assert.assertNotNull("blockManager", blockManager);

        Block block61 = blockManager.getBlock("61");
        Assert.assertNotNull("block61", block61);
        Block block62 = blockManager.getBlock("62");
        Assert.assertNotNull("block62", block62);
        Block block63 = blockManager.getBlock("63");
        Assert.assertNotNull("block63", block63);
        Block block64 = blockManager.getBlock("64");
        Assert.assertNotNull("block64", block64);

        //Test right handed turnout (with "wings" in same block) connectivity turnout settings
        Block cBlock = block62;
        Block pBlock = block64;
        Block nBlock = block61;
        List<LayoutTrackExpectedState<LayoutTurnout>> tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("62_64_61Connect turnout", "TO4", tsList.get(0).getObject().getName());
        int setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("62_64_61Connect state", Turnout.THROWN, setting);

        cBlock = block62;
        pBlock = block61;
        nBlock = block64;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("62_61_64Connect turnout", "TO4", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("62_61_64Connect state", Turnout.THROWN, setting);

        cBlock = block62;
        pBlock = block63;
        nBlock = block61;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("62_63_61Connect turnout", "TO4", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("62_63_61Connect state", Turnout.CLOSED, setting);

        cBlock = block62;
        pBlock = block61;
        nBlock = block63;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("62_61_63Connect turnout", "TO4", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("62_61_63Connect state", Turnout.CLOSED, setting);
    }

    @Test
    public void testSingleSlip() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditorFrameOperator", layoutEditorFrameOperator);
        Assert.assertNotNull("layoutEditor", layoutEditor);
        Assert.assertNotNull("connectivityUtil", connectivityUtil);
        Assert.assertNotNull("blockManager", blockManager);

        Block block70 = blockManager.getBlock("70");
        Assert.assertNotNull("block70", block70);
        Block block71 = blockManager.getBlock("71");
        Assert.assertNotNull("block71", block71);
        Block block72 = blockManager.getBlock("72");
        Assert.assertNotNull("block72", block72);
        Block block73 = blockManager.getBlock("73");
        Assert.assertNotNull("block73", block73);
        Block block74 = blockManager.getBlock("74");
        Assert.assertNotNull("block74", block74);

        //Test single slip
        Block cBlock = block70;
        Block pBlock = block71;
        Block nBlock = block74;
        List<LayoutTrackExpectedState<LayoutTurnout>> tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("70_71_74Connect turnout", "SL1", tsList.get(0).getObject().getName());
        int setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("70_71_74Connect state", LayoutTurnout.STATE_AD, setting);

        cBlock = block70;
        pBlock = block74;
        nBlock = block71;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("70_74_71Connect turnout", "SL1", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("70_74_71Connect state", LayoutTurnout.STATE_AD, setting);

        cBlock = block70;
        pBlock = block71;
        nBlock = block73;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("70_71_73Connect turnout", "SL1", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("70_71_73Connect state", LayoutTurnout.STATE_AC, setting);

        cBlock = block70;
        pBlock = block73;
        nBlock = block71;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("70_73_71Connect turnout", "SL1", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("70_73_71Connect state", LayoutTurnout.STATE_AC, setting);

        cBlock = block70;
        pBlock = block71;
        nBlock = block73;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("70_71_73Connect turnout", "SL1", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("70_71_73Connect state", LayoutTurnout.STATE_AC, setting);

        cBlock = block70;
        pBlock = block72;
        nBlock = block74;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("70_72_74Connect turnout", "SL1", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("70_72_74Connect state", LayoutTurnout.STATE_BD, setting);

        cBlock = block70;
        pBlock = block74;
        nBlock = block72;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("70_74_72Connect turnout", "SL1", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("70_74_72Connect state", LayoutTurnout.STATE_BD, setting);
    }

    @Test
    public void testDoubleSlip() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditorFrameOperator", layoutEditorFrameOperator);
        Assert.assertNotNull("layoutEditor", layoutEditor);
        Assert.assertNotNull("connectivityUtil", connectivityUtil);
        Assert.assertNotNull("blockManager", blockManager);

        Block block75 = blockManager.getBlock("75");
        Assert.assertNotNull("block75", block75);
        Block block76 = blockManager.getBlock("76");
        Assert.assertNotNull("block76", block76);
        Block block77 = blockManager.getBlock("77");
        Assert.assertNotNull("block77", block77);
        Block block78 = blockManager.getBlock("78");
        Assert.assertNotNull("block78", block78);
        Block block79 = blockManager.getBlock("79");
        Assert.assertNotNull("block79", block79);

        //Test double slip
        Block cBlock = block75;
        Block pBlock = block76;
        Block nBlock = block79;
        List<LayoutTrackExpectedState<LayoutTurnout>> tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("75_76_79Connect turnout", "SL2", tsList.get(0).getObject().getName());
        int setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("75_76_79Connect state", LayoutTurnout.STATE_AD, setting);

        cBlock = block75;
        pBlock = block79;
        nBlock = block76;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("75_79_76Connect turnout", "SL2", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("75_79_76Connect state", LayoutTurnout.STATE_AD, setting);

        cBlock = block75;
        pBlock = block76;
        nBlock = block78;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("75_76_78Connect turnout", "SL2", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("75_76_78Connect state", LayoutTurnout.STATE_AC, setting);

        cBlock = block75;
        pBlock = block78;
        nBlock = block76;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("75_78_76Connect turnout", "SL2", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("75_78_76Connect state", LayoutTurnout.STATE_AC, setting);

        cBlock = block75;
        pBlock = block77;
        nBlock = block79;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("75_77_79Connect turnout", "SL2", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("75_77_79Connect state", LayoutTurnout.STATE_BD, setting);

        cBlock = block75;
        pBlock = block79;
        nBlock = block77;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("75_79_77Connect turnout", "SL2", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("75_79_77Connect state", LayoutTurnout.STATE_BD, setting);

        cBlock = block75;
        pBlock = block77;
        nBlock = block78;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("75_77_78Connect turnout", "SL2", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("75_77_78Connect state", LayoutTurnout.STATE_BC, setting);

        cBlock = block75;
        pBlock = block78;
        nBlock = block77;
        tsList = connectivityUtil.getTurnoutList(cBlock, pBlock, nBlock);
        Assert.assertNotNull("tsList", tsList);
        Assert.assertEquals("tsList.size()", 1, tsList.size());
        Assert.assertEquals("75_78_77Connect turnout", "SL2", tsList.get(0).getObject().getName());
        setting = tsList.get(0).getExpectedState();
        Assert.assertEquals("75_78_77Connect state", LayoutTurnout.STATE_BC, setting);
    }

    //
    //from here down is testing infrastructure
    //
    @BeforeClass
    public static void setUpClass() throws Exception {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();
            JUnitUtil.initInternalTurnoutManager();
            JUnitUtil.initInternalSensorManager();

            //set default string matching comparator to one that exactly matches and is case sensitive
            Operator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));
            ThreadingUtil.runOnLayoutEventually(() -> {
                //load and display test panel file
                ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
                };
                File file = new File("java/test/jmri/jmrit/display/layoutEditor/valid/LEConnectTest.xml");
                try {
                    Assert.assertTrue("loaded successfully", cm.load(file));
                } catch (JmriConfigureXmlException ex) {
                    Assert.fail("JmriConfigureXmlException " + ex);
                }
            });

            //Find new window by name (should be more distinctive, comes from sample file)
            layoutEditorFrameOperator = new EditorFrameOperator("LayoutConnectivityTest");
            layoutEditor = (LayoutEditor) JmriJFrame.getFrame("LayoutConnectivityTest");
            Assert.assertNotNull(layoutEditor);

            connectivityUtil = layoutEditor.getConnectivityUtil();
            Assert.assertNotNull(connectivityUtil);

            blockManager = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
            Assert.assertNotNull(blockManager);

            //wait for layout editor to finish setup and drawing
            new QueueTool().waitEmpty();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            new QueueTool().waitEmpty();
            //Ask to close window
            if (layoutEditorFrameOperator != null) {
                layoutEditorFrameOperator.closeFrameWithConfirmations();
                //jFrameOperator.waitClosed();    //make sure the dialog actually closed
            }
        }
        JUnitUtil.tearDown();
    }

    @Before
    public void setUp() {
        JUnitUtil.resetProfileManager();
//    if (!GraphicsEnvironment.isHeadless()) {
//        layoutConnectivity = new LayoutConnectivity(b, d);
//        Assert.assertNotNull("layoutConnectivity", layoutConnectivity);
//    }
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
