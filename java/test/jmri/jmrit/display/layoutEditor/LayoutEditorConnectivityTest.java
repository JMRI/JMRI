package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import jmri.Block;
import jmri.BlockManager;
import jmri.JmriException;
import jmri.Turnout;
import jmri.configurexml.ConfigXmlManager;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Swing tests for the LayoutEditor
 *
 * @author Dave Duchamp Copyright 2011
 */
public class LayoutEditorConnectivityTest {

    private ConfigXmlManager cm = null;

    @Test
    @DisabledIfHeadless
    public void testShowAndClose() throws JmriException {

        // load and display test panel file
        java.io.File f = new java.io.File("java/test/jmri/jmrit/display/layoutEditor/valid/LEConnectTest.xml");
        cm.load(f);

        // Find new window by name (should be more distinctive, comes from sample file)
        EditorFrameOperator to = new EditorFrameOperator("Connectivity Test");

        LayoutEditor le = (LayoutEditor) jmri.util.JmriJFrame.getFrame("Connectivity Test");
        assertNotNull(le);

        // Panel is up, continue set up for tests.
        ConnectivityUtil cu = new ConnectivityUtil(le);
        assertNotNull(cu);
        BlockManager bm = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
        assertNotNull(bm);

        // Test right-handed crossover connectivity turnout settings
        Block cBlock = bm.getBlock("4");
        Block pBlock = bm.getBlock("6");
        Block nBlock = bm.getBlock("5");
        List <LayoutTrackExpectedState<LayoutTurnout>> tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        int setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "6_4_5Connect");

        pBlock = bm.getBlock("5");
        nBlock = bm.getBlock("6");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "5_4_6Connect");

        pBlock = bm.getBlock("5");
        nBlock = bm.getBlock("2");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "5_4_2Connect");

        cBlock = bm.getBlock("2");
        pBlock = bm.getBlock("1");
        nBlock = bm.getBlock("3");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "1_2_3Connect");

        pBlock = bm.getBlock("3");
        nBlock = bm.getBlock("1");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "3_2_1Connect");

        pBlock = bm.getBlock("1");
        nBlock = bm.getBlock("4");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "1_2_4Connect");

        // Test left-handed crossover connectivity turnout settings
        cBlock = bm.getBlock("14");
        pBlock = bm.getBlock("13");
        nBlock = bm.getBlock("17");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "13_14_17Connect");

        pBlock = bm.getBlock("17");
        nBlock = bm.getBlock("13");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "17_14_13Connect");

        pBlock = bm.getBlock("17");
        nBlock = bm.getBlock("12");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "17_14_12Connect");

        cBlock = bm.getBlock("12");
        pBlock = bm.getBlock("11");
        nBlock = bm.getBlock("15");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "11_12_15Connect");

        pBlock = bm.getBlock("15");
        nBlock = bm.getBlock("11");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "15_12_11Connect");

        pBlock = bm.getBlock("15");
        nBlock = bm.getBlock("14");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "15_12_14Connect");

        // Test double crossover connectivity turnout settings
        cBlock = bm.getBlock("21");
        pBlock = bm.getBlock("20");
        nBlock = bm.getBlock("22");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "20_21_22Connect");

        pBlock = bm.getBlock("22");
        nBlock = bm.getBlock("20");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "22_21_20Connect");

        pBlock = bm.getBlock("20");
        nBlock = bm.getBlock("26");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "20_21_26Connect");

        cBlock = bm.getBlock("22");
        pBlock = bm.getBlock("23");
        nBlock = bm.getBlock("21");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "23_22_21Connect");

        pBlock = bm.getBlock("21");
        nBlock = bm.getBlock("23");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "21_22_23Connect");

        pBlock = bm.getBlock("23");
        nBlock = bm.getBlock("25");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "23_22_25Connect");

        cBlock = bm.getBlock("26");
        pBlock = bm.getBlock("27");
        nBlock = bm.getBlock("25");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "27_26_25Connect");

        pBlock = bm.getBlock("25");
        nBlock = bm.getBlock("27");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "25_26_27Connect");

        pBlock = bm.getBlock("27");
        nBlock = bm.getBlock("21");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "27_26_21Connect");

        cBlock = bm.getBlock("25");
        pBlock = bm.getBlock("24");
        nBlock = bm.getBlock("26");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "24_25_26Connect");

        pBlock = bm.getBlock("26");
        nBlock = bm.getBlock("24");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "26_25_24Connect");

        pBlock = bm.getBlock("24");
        nBlock = bm.getBlock("22");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "24_25_22Connect");

        // Test right handed turnout (with "wings" in same block) connectivity turnout settings
        cBlock = bm.getBlock("62");
        pBlock = bm.getBlock("64");
        nBlock = bm.getBlock("61");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "64_62_61Connect");

        pBlock = bm.getBlock("61");
        nBlock = bm.getBlock("64");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "61_62_64Connect");

        pBlock = bm.getBlock("63");
        nBlock = bm.getBlock("61");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "63_62_61Connect");

        pBlock = bm.getBlock("61");
        nBlock = bm.getBlock("63");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "61_62_63Connect");

        // Test extended track following connectivity turnout settings
        //   Each path must go through two turnouts, whose settings are tested in order
        cBlock = bm.getBlock("32");
        pBlock = bm.getBlock("31");
        nBlock = bm.getBlock("33");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "31_32_33ConnectA");
        setting = tsList.get(1).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "31_32_33ConnectB");

        pBlock = bm.getBlock("33");
        nBlock = bm.getBlock("31");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "33_32_31ConnectA");
        setting = tsList.get(1).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "33_32_31ConnectB");

        pBlock = bm.getBlock("31");
        nBlock = bm.getBlock("34");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "31_32_34ConnectA");
        setting = tsList.get(1).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "31_32_34ConnectB");

        pBlock = bm.getBlock("34");
        nBlock = bm.getBlock("31");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "34_32_31ConnectA");
        setting = tsList.get(1).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "34_32_31ConnectB");

        pBlock = bm.getBlock("31");
        nBlock = bm.getBlock("35");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "31_32_35ConnectA");
        setting = tsList.get(1).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "31_32_35ConnectB");

        pBlock = bm.getBlock("35");
        nBlock = bm.getBlock("31");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.CLOSED, setting, "35_32_31ConnectA");
        setting = tsList.get(1).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "35_32_31ConnectB");

        pBlock = bm.getBlock("31");
        nBlock = bm.getBlock("36");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "31_32_36ConnectA");
        setting = tsList.get(1).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "31_32_36ConnectB");

        pBlock = bm.getBlock("36");
        nBlock = bm.getBlock("31");
        tsList = cu.getTurnoutList(cBlock, pBlock, nBlock);
        setting = tsList.get(0).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "36_32_31ConnectA");
        setting = tsList.get(1).getExpectedState();
        assertEquals( Turnout.THROWN, setting, "36_32_31ConnectB");

        // and close the window
        to.closeFrameWithConfirmations();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        cm = new jmri.configurexml.ConfigXmlManager() {
        };
    }

    @AfterEach
    public void tearDown() {
        cm = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        jmri.jmrit.display.EditorFrameOperator.clearEditorFrameOperatorThreads();
        JUnitUtil.tearDown();
    }
}
