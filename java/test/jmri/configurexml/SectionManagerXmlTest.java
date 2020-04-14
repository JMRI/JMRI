package jmri.configurexml;

import java.util.List;
import jmri.Block;
import jmri.ConfigureManager;
import jmri.EntryPoint;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.Path;
import jmri.Section;
import jmri.Sensor;
import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Tests for SectionManagerXml.
 * <p>
 * Just tests Elements, not actual files.
 *
 * @author Bob Coleman Copyright 2012
 */
public class SectionManagerXmlTest {

    @Test
    public void testLoadCurrent() throws Exception {
        // load file
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initSectionManager();
        InstanceManager.getDefault(ConfigureManager.class)
                .load(new java.io.File("java/test/jmri/configurexml/load/SectionManagerXmlTest.xml"));

        // Note: This test assumes that BlockManagerXMLTest passes and more importantly (weakly)
        //       that LoadSectionManagerFileText.xml and LoadBlockManagerFileText.xml refer to the
        //       same block / section layout definition.
        // check existence of sections
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("IY:AUTO:0001"));
        Assert.assertNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("no section"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("IY:AUTO:0002"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("IY:AUTO:0003"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("IY:AUTO:0004"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("IY:AUTO:0005"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("IY:AUTO:0006"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("IY:AUTO:0007"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("IY:AUTO:0008"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("IY:AUTO:0009"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("IY:AUTO:0010"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("IY:AUTO:0011"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("IY:AUTO:0012"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("NorthWest"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("North"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("NorthSiding"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("NorthEast"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("East"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("EastSiding"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("SouthEast"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("South"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("SouthSiding"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("SouthWest"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("West"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.SectionManager.class).getSection("WestSiding"));

        // check existence of a couple of blocks just to be sure
        //       that LoadSectionManagerFileText.xml and LoadBlockManagerFileText.xml refer to the
        //       same block / section layout definition.
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("IB1"));
        Assert.assertNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("no block"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("IB12"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocknorthwest"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blockwestsiding"));
        //Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB1"));
        //Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB12"));
        //Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocknorthwest"));
        //Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blockwestsiding"));

        // check existence of a couple of turmouts just to be sure
        //       that LoadSectionManagerFileText.xml and LoadBlockManagerFileText.xml refer to the
        //       same block / section layout definition.
        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
        Assert.assertNull(InstanceManager.turnoutManagerInstance().getTurnout("no turnout"));
        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT2"));
        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT8"));

        // check existence of a couple of memories just to be sure
        //       that LoadSectionManagerFileText.xml and LoadBlockManagerFileText.xml refer to the
        //       same block / section layout definition.
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0001"));
        Assert.assertNull(InstanceManager.memoryManagerInstance().getMemory("no memory"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0002"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0012"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocknorthwestmemory"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blockwestsidingmemory"));

        // check existence of a couple of sensors just to be sure
        //       that LoadSectionManagerFileText.xml and LoadBlockManagerFileText.xml refer to the
        //       same block / section layout definition.
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO1"));
        Assert.assertNull(InstanceManager.sensorManagerInstance().getSensor("no sensor"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO2"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO12"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocknorthwestoccupied"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blockwestsidingoccupied"));

        // check existence of a couple of paths between blocks just to be sure
        //       that LoadSectionManagerFileText.xml and LoadBlockManagerFileText.xml refer to the
        //       same block / section layout definition.
        Block[] blockstotest;
        Sensor[] occupiedsensor;
        int[] expectedpreviouspaths;
        int[] expectednextpaths;
        blockstotest = new Block[12];         //Make sure this is bigger than the list below
        occupiedsensor = new Sensor[12];      //Make sure this is bigger than the list below
        expectedpreviouspaths = new int[12];  //Make sure this is bigger than the list below
        expectednextpaths = new int[12];      //Make sure this is bigger than the list below

        Boolean[] passprevioustest;
        Boolean[] passnexttest;
        passprevioustest = new Boolean[4];    //Make sure this is bigger than needed
        passnexttest = new Boolean[4];        //Make sure this is bigger than needed

        Block[][] previousblock;
        previousblock = new Block[12][4];         //Make sure this is bigger than the list below
        Block[][] nextblock;
        nextblock = new Block[12][4];             //Make sure this is bigger than the list below

        //  This matches up with the test file, ..., just be sure
        //       that LoadSectionManagerFileText.xml and LoadBlockManagerFileText.xml refer to the
        //       same block / section layout definition.
        blockstotest[0] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocknorthwest");
        Assert.assertNotNull(blockstotest[0]);
        occupiedsensor[0] = InstanceManager.sensorManagerInstance().getSensor("blocknorthwestoccupied");
        Assert.assertNotNull(occupiedsensor[0]);
        blockstotest[1] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocknorth");
        Assert.assertNotNull(blockstotest[1]);
        occupiedsensor[1] = InstanceManager.sensorManagerInstance().getSensor("blocknorthoccupied");
        Assert.assertNotNull(occupiedsensor[1]);
        blockstotest[2] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocknorthsiding");
        Assert.assertNotNull(blockstotest[2]);
        occupiedsensor[2] = InstanceManager.sensorManagerInstance().getSensor("blocknorthsidingoccupied");
        Assert.assertNotNull(occupiedsensor[2]);
        blockstotest[3] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocknortheast");
        Assert.assertNotNull(blockstotest[3]);
        occupiedsensor[3] = InstanceManager.sensorManagerInstance().getSensor("blocknortheastoccupied");
        Assert.assertNotNull(occupiedsensor[3]);
        blockstotest[4] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blockeast");
        Assert.assertNotNull(blockstotest[4]);
        occupiedsensor[4] = InstanceManager.sensorManagerInstance().getSensor("blockeastoccupied");
        Assert.assertNotNull(occupiedsensor[4]);
        blockstotest[5] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blockeastsiding");
        Assert.assertNotNull(blockstotest[5]);
        occupiedsensor[5] = InstanceManager.sensorManagerInstance().getSensor("blockeastsidingoccupied");
        Assert.assertNotNull(occupiedsensor[5]);
        blockstotest[6] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocksoutheast");
        Assert.assertNotNull(blockstotest[6]);
        occupiedsensor[6] = InstanceManager.sensorManagerInstance().getSensor("blocksoutheastoccupied");
        Assert.assertNotNull(occupiedsensor[6]);
        blockstotest[7] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocksouth");
        Assert.assertNotNull(blockstotest[7]);
        occupiedsensor[7] = InstanceManager.sensorManagerInstance().getSensor("blocksouthoccupied");
        Assert.assertNotNull(occupiedsensor[7]);
        blockstotest[8] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocksouthsiding");
        Assert.assertNotNull(blockstotest[8]);
        occupiedsensor[8] = InstanceManager.sensorManagerInstance().getSensor("blocksouthsidingoccupied");
        Assert.assertNotNull(occupiedsensor[8]);
        blockstotest[9] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocksouthwest");
        Assert.assertNotNull(blockstotest[9]);
        occupiedsensor[9] = InstanceManager.sensorManagerInstance().getSensor("blocksouthwestoccupied");
        Assert.assertNotNull(occupiedsensor[9]);
        blockstotest[10] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blockwest");
        Assert.assertNotNull(blockstotest[10]);
        occupiedsensor[10] = InstanceManager.sensorManagerInstance().getSensor("blockwestoccupied");
        Assert.assertNotNull(occupiedsensor[10]);
        blockstotest[11] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blockwestsiding");
        Assert.assertNotNull(blockstotest[11]);
        occupiedsensor[11] = InstanceManager.sensorManagerInstance().getSensor("blockwestsidingoccupied");
        Assert.assertNotNull(occupiedsensor[11]);

        // The references are circular so the definitons are split up, ..., just be sure
        //       that LoadSectionManagerFileText.xml and LoadBlockManagerFileText.xml refer to the
        //       same block / section layout definition.
        expectedpreviouspaths[0] = 2;
        previousblock[0][0] = blockstotest[10];
        previousblock[0][1] = blockstotest[11];
        expectednextpaths[0] = 2;
        nextblock[0][0] = blockstotest[1];
        nextblock[0][1] = blockstotest[2];
        expectedpreviouspaths[1] = 1;
        previousblock[1][0] = blockstotest[0];
        expectednextpaths[1] = 1;
        nextblock[1][0] = blockstotest[3];
        expectedpreviouspaths[2] = 1;
        previousblock[2][0] = blockstotest[0];
        expectednextpaths[2] = 1;
        nextblock[2][0] = blockstotest[3];
        expectedpreviouspaths[3] = 2;
        previousblock[3][0] = blockstotest[1];
        previousblock[3][1] = blockstotest[2];
        expectednextpaths[3] = 2;
        nextblock[3][0] = blockstotest[4];
        nextblock[3][1] = blockstotest[5];
        expectedpreviouspaths[4] = 1;
        previousblock[4][0] = blockstotest[3];
        expectednextpaths[4] = 1;
        nextblock[4][0] = blockstotest[6];
        expectedpreviouspaths[5] = 1;
        previousblock[5][0] = blockstotest[3];
        expectednextpaths[5] = 1;
        nextblock[5][0] = blockstotest[6];
        expectedpreviouspaths[6] = 2;
        previousblock[6][0] = blockstotest[4];
        previousblock[6][1] = blockstotest[5];
        expectednextpaths[6] = 2;
        nextblock[6][0] = blockstotest[7];
        nextblock[6][1] = blockstotest[8];
        expectedpreviouspaths[7] = 1;
        previousblock[7][0] = blockstotest[6];
        expectednextpaths[7] = 1;
        nextblock[7][0] = blockstotest[9];
        expectedpreviouspaths[8] = 1;
        previousblock[8][0] = blockstotest[6];
        expectednextpaths[8] = 1;
        nextblock[8][0] = blockstotest[9];
        expectedpreviouspaths[9] = 2;
        previousblock[9][0] = blockstotest[7];
        previousblock[9][1] = blockstotest[8];
        expectednextpaths[9] = 2;
        nextblock[9][0] = blockstotest[10];
        nextblock[9][1] = blockstotest[11];
        expectedpreviouspaths[10] = 1;
        previousblock[10][0] = blockstotest[9];
        expectednextpaths[10] = 1;
        nextblock[10][0] = blockstotest[0];
        expectedpreviouspaths[11] = 1;
        previousblock[11][0] = blockstotest[9];
        expectednextpaths[11] = 1;
        nextblock[11][0] = blockstotest[0];

        for (int testblockfocus = 0; testblockfocus < 12; testblockfocus++) {  // Set to one greater than above
            int expectedcentrepaths = expectedpreviouspaths[testblockfocus] + expectednextpaths[testblockfocus];
            Block focusBlock = blockstotest[testblockfocus];
            Memory expectedtestmemory = InstanceManager.memoryManagerInstance().getMemory("blocknorthmemory");
            Assert.assertNotNull(expectedtestmemory);
            expectedtestmemory.setValue("Memory test: " + testblockfocus);
// TODO: BOB C: Memory Test
//            Memory actualtestmemory = (Memory) focusBlock.getValue();
//            Assert.assertNotNull(actualtestmemory);
//            Assert.assertEquals("Memory where Focus was: " + testblockfocus, expectedtestmemory, actualtestmemory);
            Assert.assertEquals("Sensor where Focus was: " + testblockfocus, occupiedsensor[testblockfocus].getSystemName(), focusBlock.getSensor().getSystemName());
            List<Path> testpaths = focusBlock.getPaths();
            Assert.assertEquals("Block Path size where Block Focus was: " + testblockfocus, expectedcentrepaths, testpaths.size());
            for (int p = 0; p < expectedpreviouspaths[testblockfocus]; p++) {
                passprevioustest[p] = false;
            }
            for (int n = 0; n < expectednextpaths[testblockfocus]; n++) {
                passnexttest[n] = false;
            }
            for (int i = 0; i < testpaths.size(); i++) {
                Block testblock = testpaths.get(i).getBlock();
                Assert.assertNotNull(testblock);
                for (int p = 0; p < expectedpreviouspaths[testblockfocus]; p++) {
                    if (testblock == previousblock[testblockfocus][p]) {
                        passprevioustest[p] = true;
                    }
                }
                for (int n = 0; n < expectednextpaths[testblockfocus]; n++) {
                    if (testblock == nextblock[testblockfocus][n]) {
                        passnexttest[n] = true;
                    }
                }
            }

            for (int p = 0; p < expectedpreviouspaths[testblockfocus]; p++) {
                Assert.assertTrue("Block Focus was: " + testblockfocus + " previous path: " + p, passprevioustest[p]);
            }
            for (int n = 0; n < expectednextpaths[testblockfocus]; n++) {
                Assert.assertTrue("Block Focus was: " + testblockfocus + " next path: " + n, passnexttest[n]);
            }

        }

        Section[] sectionstotest;
        Block[] expectedsectionentryblock;
        Block[][] expectedsectionforwardblock;
        Block[][] expectedsectionreverseblock;
        Sensor[] expectedForwardBlockingSensors;
        Sensor[] expectedReverseBlockingSensors;
        Sensor[] expectedForwardStoppingSensors;
        Sensor[] expectedReverseStoppingSensors;
        int[] expectedsectionblocklistsize;
        int[] expectedforwardEntryPointList;
        int[] expectedreverseEntryPointList;
//        Sensor[] occupiedsensor;
//        int[] expectedpreviouspaths;
//        int[] expectednextpaths;
        sectionstotest = new Section[12];     //Make sure this is bigger than the list below
        expectedsectionentryblock = new Block[12];     //Make sure this is bigger than the list below
        expectedsectionforwardblock = new Block[12][2];     //Make sure this is bigger than the list below
        expectedsectionreverseblock = new Block[12][2];     //Make sure this is bigger than the list below
        expectedForwardBlockingSensors = new Sensor[12];  //Make sure this is bigger than the list below
        expectedReverseBlockingSensors = new Sensor[12];  //Make sure this is bigger than the list below
        expectedForwardStoppingSensors = new Sensor[12];  //Make sure this is bigger than the list below
        expectedReverseStoppingSensors = new Sensor[12];  //Make sure this is bigger than the list below
        expectedsectionblocklistsize = new int[12];  //Make sure this is bigger than the list below
        expectedforwardEntryPointList = new int[12];  //Make sure this is bigger than the list below
        expectedreverseEntryPointList = new int[12];  //Make sure this is bigger than the list below
//        occupiedsensor = new Sensor[12];      //Make sure this is bigger than the list below
//        expectedpreviouspaths = new int[12];  //Make sure this is bigger than the list below
//        expectednextpaths = new int[12];      //Make sure this is bigger than the list below

        //  This matches up with the test file, ...
        sectionstotest[0] = InstanceManager.getDefault(jmri.SectionManager.class).getSection("NorthWest");
        Assert.assertNotNull(sectionstotest[0]);
        expectedsectionblocklistsize[0] = 1;
        expectedsectionentryblock[0] = blockstotest[0];
        expectedforwardEntryPointList[0] = 2;
        expectedsectionforwardblock[0][0] = blockstotest[10];
        expectedsectionforwardblock[0][1] = blockstotest[11];
        expectedreverseEntryPointList[0] = 2;
        expectedsectionreverseblock[0][0] = blockstotest[1];
        expectedsectionreverseblock[0][1] = blockstotest[2];
        expectedForwardBlockingSensors[0] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRF1");
        expectedReverseBlockingSensors[0] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRR1");
        expectedForwardStoppingSensors[0] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPF1");
        expectedReverseStoppingSensors[0] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPR1");

        sectionstotest[1] = InstanceManager.getDefault(jmri.SectionManager.class).getSection("North");
        Assert.assertNotNull(sectionstotest[1]);
        expectedsectionblocklistsize[1] = 1;
        expectedsectionentryblock[1] = blockstotest[1];
        expectedforwardEntryPointList[1] = 1;
        expectedsectionforwardblock[1][0] = blockstotest[0];
        expectedreverseEntryPointList[1] = 1;
        expectedsectionreverseblock[1][0] = blockstotest[3];
        expectedForwardBlockingSensors[1] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRF2");
        expectedReverseBlockingSensors[1] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRR2");
        expectedForwardStoppingSensors[1] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPF2");
        expectedReverseStoppingSensors[1] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPR2");

        sectionstotest[2] = InstanceManager.getDefault(jmri.SectionManager.class).getSection("NorthSiding");
        Assert.assertNotNull(sectionstotest[2]);
        expectedsectionblocklistsize[2] = 1;
        expectedsectionentryblock[2] = blockstotest[2];
        expectedforwardEntryPointList[2] = 1;
        expectedsectionforwardblock[2][0] = blockstotest[0];
        expectedreverseEntryPointList[2] = 1;
        expectedsectionreverseblock[2][0] = blockstotest[3];
        expectedForwardBlockingSensors[2] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRF3");
        expectedReverseBlockingSensors[2] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRR3");
        expectedForwardStoppingSensors[2] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPF3");
        expectedReverseStoppingSensors[2] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPR3");

        sectionstotest[3] = InstanceManager.getDefault(jmri.SectionManager.class).getSection("NorthEast");
        Assert.assertNotNull(sectionstotest[3]);
        expectedsectionblocklistsize[3] = 1;
        expectedsectionentryblock[3] = blockstotest[3];
        expectedforwardEntryPointList[3] = 2;
        expectedsectionforwardblock[3][0] = blockstotest[1];
        expectedsectionforwardblock[3][1] = blockstotest[2];
        expectedreverseEntryPointList[3] = 2;
        expectedsectionreverseblock[3][0] = blockstotest[4];
        expectedsectionreverseblock[3][1] = blockstotest[5];
        expectedForwardBlockingSensors[3] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRF4");
        expectedReverseBlockingSensors[3] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRR4");
        expectedForwardStoppingSensors[3] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPF4");
        expectedReverseStoppingSensors[3] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPR4");

        sectionstotest[4] = InstanceManager.getDefault(jmri.SectionManager.class).getSection("East");
        Assert.assertNotNull(sectionstotest[4]);
        expectedsectionblocklistsize[4] = 1;
        expectedsectionentryblock[4] = blockstotest[4];
        expectedforwardEntryPointList[4] = 1;
        expectedsectionforwardblock[4][0] = blockstotest[3];
        expectedreverseEntryPointList[4] = 1;
        expectedsectionreverseblock[4][0] = blockstotest[6];
        expectedForwardBlockingSensors[4] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRF5");
        expectedReverseBlockingSensors[4] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRR5");
        expectedForwardStoppingSensors[4] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPF5");
        expectedReverseStoppingSensors[4] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPR5");

        sectionstotest[5] = InstanceManager.getDefault(jmri.SectionManager.class).getSection("EastSiding");
        Assert.assertNotNull(sectionstotest[5]);
        expectedsectionblocklistsize[5] = 1;
        expectedsectionentryblock[5] = blockstotest[5];
        expectedforwardEntryPointList[5] = 1;
        expectedsectionforwardblock[5][0] = blockstotest[3];
        expectedreverseEntryPointList[5] = 1;
        expectedsectionreverseblock[5][0] = blockstotest[6];
        expectedForwardBlockingSensors[5] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRF6");
        expectedReverseBlockingSensors[5] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRR6");
        expectedForwardStoppingSensors[5] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPF6");
        expectedReverseStoppingSensors[5] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPR6");

        sectionstotest[6] = InstanceManager.getDefault(jmri.SectionManager.class).getSection("SouthEast");
        Assert.assertNotNull(sectionstotest[6]);
        expectedsectionblocklistsize[6] = 1;
        expectedsectionentryblock[6] = blockstotest[6];
        expectedforwardEntryPointList[6] = 2;
        expectedsectionforwardblock[6][0] = blockstotest[4];
        expectedsectionforwardblock[6][1] = blockstotest[5];
        expectedreverseEntryPointList[6] = 2;
        expectedsectionreverseblock[6][0] = blockstotest[7];
        expectedsectionreverseblock[6][1] = blockstotest[8];
        expectedForwardBlockingSensors[6] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRF7");
        expectedReverseBlockingSensors[6] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRR7");
        expectedForwardStoppingSensors[6] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPF7");
        expectedReverseStoppingSensors[6] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPR7");

        sectionstotest[7] = InstanceManager.getDefault(jmri.SectionManager.class).getSection("South");
        Assert.assertNotNull(sectionstotest[7]);
        expectedsectionblocklistsize[7] = 1;
        expectedsectionentryblock[7] = blockstotest[7];
        expectedforwardEntryPointList[7] = 1;
        expectedsectionforwardblock[7][0] = blockstotest[6];
        expectedreverseEntryPointList[7] = 1;
        expectedsectionreverseblock[7][0] = blockstotest[9];
        expectedForwardBlockingSensors[7] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRF8");
        expectedReverseBlockingSensors[7] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRR8");
        expectedForwardStoppingSensors[7] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPF8");
        expectedReverseStoppingSensors[7] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPR8");

        sectionstotest[8] = InstanceManager.getDefault(jmri.SectionManager.class).getSection("SouthSiding");
        Assert.assertNotNull(sectionstotest[8]);
        expectedsectionblocklistsize[8] = 1;
        expectedsectionentryblock[8] = blockstotest[8];
        expectedforwardEntryPointList[8] = 1;
        expectedsectionforwardblock[8][0] = blockstotest[6];
        expectedreverseEntryPointList[8] = 1;
        expectedsectionreverseblock[8][0] = blockstotest[9];
        expectedForwardBlockingSensors[8] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRF9");
        expectedReverseBlockingSensors[8] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRR9");
        expectedForwardStoppingSensors[8] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPF9");
        expectedReverseStoppingSensors[8] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPR9");

        sectionstotest[9] = InstanceManager.getDefault(jmri.SectionManager.class).getSection("SouthWest");
        Assert.assertNotNull(sectionstotest[9]);
        expectedsectionblocklistsize[9] = 1;
        expectedsectionentryblock[9] = blockstotest[9];
        expectedforwardEntryPointList[9] = 2;
        expectedsectionforwardblock[9][0] = blockstotest[7];
        expectedsectionforwardblock[9][1] = blockstotest[8];
        expectedreverseEntryPointList[9] = 2;
        expectedsectionreverseblock[9][0] = blockstotest[10];
        expectedsectionreverseblock[9][1] = blockstotest[11];
        expectedForwardBlockingSensors[9] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRF10");
        expectedReverseBlockingSensors[9] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRR10");
        expectedForwardStoppingSensors[9] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPF10");
        expectedReverseStoppingSensors[9] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPR10");

        sectionstotest[10] = InstanceManager.getDefault(jmri.SectionManager.class).getSection("West");
        Assert.assertNotNull(sectionstotest[10]);
        expectedsectionblocklistsize[10] = 1;
        expectedsectionentryblock[10] = blockstotest[10];
        expectedforwardEntryPointList[10] = 1;
        expectedsectionforwardblock[10][0] = blockstotest[9];
        expectedreverseEntryPointList[10] = 1;
        expectedsectionreverseblock[10][0] = blockstotest[0];
        expectedForwardBlockingSensors[10] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRF11");
        expectedReverseBlockingSensors[10] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRR11");
        expectedForwardStoppingSensors[10] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPF11");
        expectedReverseStoppingSensors[10] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPR11");

        sectionstotest[11] = InstanceManager.getDefault(jmri.SectionManager.class).getSection("WestSiding");
        Assert.assertNotNull(sectionstotest[11]);
        expectedsectionblocklistsize[11] = 1;
        expectedsectionentryblock[11] = blockstotest[11];
        expectedforwardEntryPointList[11] = 1;
        expectedsectionforwardblock[11][0] = blockstotest[9];
        expectedreverseEntryPointList[11] = 1;
        expectedsectionreverseblock[11][0] = blockstotest[0];
        expectedForwardBlockingSensors[11] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRF12");
        expectedReverseBlockingSensors[11] = InstanceManager.sensorManagerInstance().getSensor("ISSDIRR12");
        expectedForwardStoppingSensors[11] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPF12");
        expectedReverseStoppingSensors[11] = InstanceManager.sensorManagerInstance().getSensor("ISSSTOPR12");

        for (int testsectionfocus = 0; testsectionfocus < 12; testsectionfocus++) {  // Set to one greater than above
            // check existence of sections
            Section testsection = sectionstotest[testsectionfocus];
            List<Block> blockList = testsection.getBlockList();
            Assert.assertEquals("Section size where Focus was: " + testsectionfocus, expectedsectionblocklistsize[testsectionfocus], blockList.size());

            Block entryblock = testsection.getEntryBlock();
            Assert.assertNotNull(entryblock);
            Assert.assertEquals("Section entry block where Focus was: " + testsectionfocus, expectedsectionentryblock[testsectionfocus].getSystemName(), entryblock.getSystemName());

            List<EntryPoint> forwardEntryPointList = testsection.getForwardEntryPointList();
            Assert.assertEquals("Section forward size where Focus was: " + testsectionfocus, expectedforwardEntryPointList[testsectionfocus], forwardEntryPointList.size());
            for (int e = 0; e < forwardEntryPointList.size(); e++) {
                EntryPoint get = forwardEntryPointList.get(e);
                Assert.assertEquals("Focus was: " + testsectionfocus + " next forward entry point: " + e, expectedsectionentryblock[testsectionfocus].getSystemName(), get.getBlock().getSystemName());
                Assert.assertEquals("Focus was: " + testsectionfocus + " next forward from: " + e, expectedsectionforwardblock[testsectionfocus][e].getUserName(), get.getFromBlock().getUserName());
                Assert.assertEquals("Focus was: " + testsectionfocus + " next forward dir: " + e, 4, get.getDirection());
            }
            List<EntryPoint> reverseEntryPointList = testsection.getReverseEntryPointList();
            Assert.assertEquals("Section forward size where Focus was: " + testsectionfocus, expectedreverseEntryPointList[testsectionfocus], reverseEntryPointList.size());
            for (int e = 0; e < reverseEntryPointList.size(); e++) {
                EntryPoint get = reverseEntryPointList.get(e);
                Assert.assertEquals("Focus was: " + testsectionfocus + " next reverse entry point: " + e, expectedsectionentryblock[testsectionfocus].getSystemName(), get.getBlock().getSystemName());
                Assert.assertEquals("Focus was: " + testsectionfocus + " next reverse from: " + e, expectedsectionreverseblock[testsectionfocus][e].getUserName(), get.getFromBlock().getUserName());
                Assert.assertEquals("Focus was: " + testsectionfocus + " next reverse dir: " + e, 8, get.getDirection());
            }

            Sensor expectedForwardBlockingSensor = expectedForwardBlockingSensors[testsectionfocus];
            Sensor actualForwardBlockingSensor = testsection.getForwardBlockingSensor();
            Assert.assertNotNull("Focus was: " + testsectionfocus + " expectedForwardBlockingSensor", expectedForwardBlockingSensor);
            Assert.assertNotNull("Focus was: " + testsectionfocus + " actualForwardBlockingSensor", actualForwardBlockingSensor);
            Assert.assertTrue("Focus was: " + testsectionfocus + " ForwardBlockingSensor", expectedForwardBlockingSensor.getSystemName().equals(actualForwardBlockingSensor.getSystemName()));
            Sensor expectedReverseBlockingSensor = expectedReverseBlockingSensors[testsectionfocus];
            Sensor actualReverseBlockingSensor = testsection.getReverseBlockingSensor();
            Assert.assertNotNull("Focus was: " + testsectionfocus + " expectedReverseBlockingSensor", expectedReverseBlockingSensor);
            Assert.assertNotNull("Focus was: " + testsectionfocus + " actualReverseBlockingSensor", actualReverseBlockingSensor);
            Assert.assertTrue("Focus was: " + testsectionfocus + " ReverseBlockingSensor", expectedReverseBlockingSensor.getSystemName().equals(actualReverseBlockingSensor.getSystemName()));
            Sensor expectedForwardStoppingSensor = expectedForwardStoppingSensors[testsectionfocus];
            Sensor actualForwardStoppingSensor = testsection.getForwardStoppingSensor();
            Assert.assertNotNull("Focus was: " + testsectionfocus + " expectedForwardStoppingSensor", expectedForwardStoppingSensor);
            Assert.assertNotNull("Focus was: " + testsectionfocus + " actualForwardStoppingSensor", actualForwardStoppingSensor);
            Assert.assertTrue("Focus was: " + testsectionfocus + " ForwardStoppingSensor", expectedForwardStoppingSensor.getSystemName().equals(actualForwardStoppingSensor.getSystemName()));
            Sensor expectedReverseStoppingSensor = expectedReverseStoppingSensors[testsectionfocus];
            Sensor actualReverseStoppingSensor = testsection.getReverseStoppingSensor();
            Assert.assertNotNull("Focus was: " + testsectionfocus + " expectedReverseStoppingSensor", expectedReverseStoppingSensor);
            Assert.assertNotNull("Focus was: " + testsectionfocus + " actualReverseStoppingSensor", actualReverseStoppingSensor);
            Assert.assertTrue("Focus was: " + testsectionfocus + " ReverseStoppingSensor", expectedReverseStoppingSensor.getSystemName().equals(actualReverseStoppingSensor.getSystemName()));
        }

    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
