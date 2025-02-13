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

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SectionManagerXml.
 * @author Bob Coleman Copyright 2012
 */
public class SectionManagerXmlTest {

    @Test
    public void testLoadCurrent() {
        // load file
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initSectionManager();
        Assertions.assertDoesNotThrow( () -> InstanceManager.getDefault(ConfigureManager.class)
            .load(new java.io.File("java/test/jmri/configurexml/load/SectionManagerXmlTest.xml")));

        jmri.SensorManager sensorManager = InstanceManager.getDefault(jmri.SensorManager.class);
        jmri.BlockManager blockManager = InstanceManager.getDefault(jmri.BlockManager.class);
        jmri.SectionManager sectionManager = InstanceManager.getDefault(jmri.SectionManager.class);
        jmri.TurnoutManager turnoutManager = InstanceManager.getDefault( jmri.TurnoutManager.class);
        jmri.MemoryManager memoryManager = InstanceManager.getDefault( jmri.MemoryManager.class);

        // Note: This test assumes that BlockManagerXMLTest passes and more importantly (weakly)
        //       that LoadSectionManagerFileText.xml and LoadBlockManagerFileText.xml refer to the
        //       same block / section layout definition.
        // check existence of sections
        assertNotNull(sectionManager.getSection("IY:AUTO:0001"));
        assertNull(sectionManager.getSection("no section"));
        assertNotNull(sectionManager.getSection("IY:AUTO:0002"));
        assertNotNull(sectionManager.getSection("IY:AUTO:0003"));
        assertNotNull(sectionManager.getSection("IY:AUTO:0004"));
        assertNotNull(sectionManager.getSection("IY:AUTO:0005"));
        assertNotNull(sectionManager.getSection("IY:AUTO:0006"));
        assertNotNull(sectionManager.getSection("IY:AUTO:0007"));
        assertNotNull(sectionManager.getSection("IY:AUTO:0008"));
        assertNotNull(sectionManager.getSection("IY:AUTO:0009"));
        assertNotNull(sectionManager.getSection("IY:AUTO:0010"));
        assertNotNull(sectionManager.getSection("IY:AUTO:0011"));
        assertNotNull(sectionManager.getSection("IY:AUTO:0012"));
        assertNotNull(sectionManager.getSection("NorthWest"));
        assertNotNull(sectionManager.getSection("North"));
        assertNotNull(sectionManager.getSection("NorthSiding"));
        assertNotNull(sectionManager.getSection("NorthEast"));
        assertNotNull(sectionManager.getSection("East"));
        assertNotNull(sectionManager.getSection("EastSiding"));
        assertNotNull(sectionManager.getSection("SouthEast"));
        assertNotNull(sectionManager.getSection("South"));
        assertNotNull(sectionManager.getSection("SouthSiding"));
        assertNotNull(sectionManager.getSection("SouthWest"));
        assertNotNull(sectionManager.getSection("West"));
        assertNotNull(sectionManager.getSection("WestSiding"));

        // check existence of a couple of blocks just to be sure
        //       that LoadSectionManagerFileText.xml and LoadBlockManagerFileText.xml refer to the
        //       same block / section layout definition.
        assertNotNull(blockManager.getBlock("IB1"));
        assertNull(blockManager.getBlock("no block"));
        assertNotNull(blockManager.getBlock("IB12"));
        assertNotNull(blockManager.getBlock("blocknorthwest"));
        assertNotNull(blockManager.getBlock("blockwestsiding"));
        // assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB1"));
        // assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB12"));
        // assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocknorthwest"));
        // assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blockwestsiding"));

        // check existence of a couple of turmouts just to be sure
        //       that LoadSectionManagerFileText.xml and LoadBlockManagerFileText.xml refer to the
        //       same block / section layout definition.
        assertNotNull( turnoutManager.getTurnout("IT1"));
        assertNull( turnoutManager.getTurnout("no turnout"));
        assertNotNull( turnoutManager.getTurnout("IT2"));
        assertNotNull( turnoutManager.getTurnout("IT8"));

        // check existence of a couple of memories just to be sure
        //       that LoadSectionManagerFileText.xml and LoadBlockManagerFileText.xml refer to the
        //       same block / section layout definition.
        assertNotNull(memoryManager.getMemory("IM:AUTO:0001"));
        assertNull(memoryManager.getMemory("no memory"));
        assertNotNull(memoryManager.getMemory("IM:AUTO:0002"));
        assertNotNull(memoryManager.getMemory("IM:AUTO:0012"));
        assertNotNull(memoryManager.getMemory("blocknorthwestmemory"));
        assertNotNull(memoryManager.getMemory("blockwestsidingmemory"));

        // check existence of a couple of sensors just to be sure
        //       that LoadSectionManagerFileText.xml and LoadBlockManagerFileText.xml refer to the
        //       same block / section layout definition.
        assertNotNull(sensorManager.getSensor("ISBO1"));
        assertNull(sensorManager.getSensor("no sensor"));
        assertNotNull(sensorManager.getSensor("ISBO2"));
        assertNotNull(sensorManager.getSensor("ISBO12"));
        assertNotNull(sensorManager.getSensor("blocknorthwestoccupied"));
        assertNotNull(sensorManager.getSensor("blockwestsidingoccupied"));

        // check existence of a couple of paths between blocks just to be sure
        //       that LoadSectionManagerFileText.xml and LoadBlockManagerFileText.xml refer to the
        //       same block / section layout definition.

        Block[] blockstotest = new Block[12];         //Make sure this is bigger than the list below
        Sensor[] occupiedsensor = new Sensor[12];      //Make sure this is bigger than the list below
        int[] expectedpreviouspaths = new int[12];  //Make sure this is bigger than the list below
        int[] expectednextpaths = new int[12];      //Make sure this is bigger than the list below

        boolean[] passprevioustest = new boolean[4];    //Make sure this is bigger than needed
        boolean[] passnexttest = new boolean[4];        //Make sure this is bigger than needed

        Block[][] previousblock = new Block[12][4];         //Make sure this is bigger than the list below
        Block[][] nextblock = new Block[12][4];             //Make sure this is bigger than the list below

        //  This matches up with the test file, ..., just be sure
        //       that LoadSectionManagerFileText.xml and LoadBlockManagerFileText.xml refer to the
        //       same block / section layout definition.
        blockstotest[0] = blockManager.getBlock("blocknorthwest");
        assertNotNull(blockstotest[0]);
        occupiedsensor[0] = sensorManager.getSensor("blocknorthwestoccupied");
        assertNotNull(occupiedsensor[0]);
        blockstotest[1] = blockManager.getBlock("blocknorth");
        assertNotNull(blockstotest[1]);
        occupiedsensor[1] = sensorManager.getSensor("blocknorthoccupied");
        assertNotNull(occupiedsensor[1]);
        blockstotest[2] = blockManager.getBlock("blocknorthsiding");
        assertNotNull(blockstotest[2]);
        occupiedsensor[2] = sensorManager.getSensor("blocknorthsidingoccupied");
        assertNotNull(occupiedsensor[2]);
        blockstotest[3] = blockManager.getBlock("blocknortheast");
        assertNotNull(blockstotest[3]);
        occupiedsensor[3] = sensorManager.getSensor("blocknortheastoccupied");
        assertNotNull(occupiedsensor[3]);
        blockstotest[4] = blockManager.getBlock("blockeast");
        assertNotNull(blockstotest[4]);
        occupiedsensor[4] = sensorManager.getSensor("blockeastoccupied");
        assertNotNull(occupiedsensor[4]);
        blockstotest[5] = blockManager.getBlock("blockeastsiding");
        assertNotNull(blockstotest[5]);
        occupiedsensor[5] = sensorManager.getSensor("blockeastsidingoccupied");
        assertNotNull(occupiedsensor[5]);
        blockstotest[6] = blockManager.getBlock("blocksoutheast");
        assertNotNull(blockstotest[6]);
        occupiedsensor[6] = sensorManager.getSensor("blocksoutheastoccupied");
        assertNotNull(occupiedsensor[6]);
        blockstotest[7] = blockManager.getBlock("blocksouth");
        assertNotNull(blockstotest[7]);
        occupiedsensor[7] = sensorManager.getSensor("blocksouthoccupied");
        assertNotNull(occupiedsensor[7]);
        blockstotest[8] = blockManager.getBlock("blocksouthsiding");
        assertNotNull(blockstotest[8]);
        occupiedsensor[8] = sensorManager.getSensor("blocksouthsidingoccupied");
        assertNotNull(occupiedsensor[8]);
        blockstotest[9] = blockManager.getBlock("blocksouthwest");
        assertNotNull(blockstotest[9]);
        occupiedsensor[9] = sensorManager.getSensor("blocksouthwestoccupied");
        assertNotNull(occupiedsensor[9]);
        blockstotest[10] = blockManager.getBlock("blockwest");
        assertNotNull(blockstotest[10]);
        occupiedsensor[10] = sensorManager.getSensor("blockwestoccupied");
        assertNotNull(occupiedsensor[10]);
        blockstotest[11] = blockManager.getBlock("blockwestsiding");
        assertNotNull(blockstotest[11]);
        occupiedsensor[11] = sensorManager.getSensor("blockwestsidingoccupied");
        assertNotNull(occupiedsensor[11]);

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
            Memory expectedtestmemory = memoryManager.getMemory("blocknorthmemory");
            assertNotNull(expectedtestmemory);
            expectedtestmemory.setValue("Memory test: " + testblockfocus);
// TODO: BOB C: Memory Test
//            Memory actualtestmemory = (Memory) focusBlock.getValue();
//            Assert.assertNotNull(actualtestmemory);
//            Assert.assertEquals("Memory where Focus was: " + testblockfocus, expectedtestmemory, actualtestmemory);
            assertEquals( occupiedsensor[testblockfocus], focusBlock.getSensor(),
                "Sensor where Focus was: " + testblockfocus);
            List<Path> testpaths = focusBlock.getPaths();
            assertEquals( expectedcentrepaths, testpaths.size(),
                "Block Path size where Block Focus was: " + testblockfocus);
            for (int p = 0; p < expectedpreviouspaths[testblockfocus]; p++) {
                passprevioustest[p] = false;
            }
            for (int n = 0; n < expectednextpaths[testblockfocus]; n++) {
                passnexttest[n] = false;
            }
            for (int i = 0; i < testpaths.size(); i++) {
                Block testblock = testpaths.get(i).getBlock();
                assertNotNull(testblock);
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
                assertTrue(passprevioustest[p], "Block Focus was: " + testblockfocus + " previous path: " + p);
            }
            for (int n = 0; n < expectednextpaths[testblockfocus]; n++) {
                assertTrue(passnexttest[n], "Block Focus was: " + testblockfocus + " next path: " + n);
            }

        }

        Section[] sectionstotest = new Section[12];     //Make sure this is bigger than the list below
        Block[] expectedsectionentryblock = new Block[12];     //Make sure this is bigger than the list below
        Block[][] expectedsectionforwardblock = new Block[12][2];     //Make sure this is bigger than the list below
        Block[][] expectedsectionreverseblock = new Block[12][2];     //Make sure this is bigger than the list below
        Sensor[] expectedForwardBlockingSensors = new Sensor[12];  //Make sure this is bigger than the list below
        Sensor[] expectedReverseBlockingSensors = new Sensor[12];  //Make sure this is bigger than the list below
        Sensor[] expectedForwardStoppingSensors = new Sensor[12];  //Make sure this is bigger than the list below
        Sensor[] expectedReverseStoppingSensors = new Sensor[12];  //Make sure this is bigger than the list below
        int[] expectedsectionblocklistsize = new int[12];  //Make sure this is bigger than the list below
        int[] expectedforwardEntryPointList = new int[12];  //Make sure this is bigger than the list below
        int[] expectedreverseEntryPointList = new int[12];  //Make sure this is bigger than the list below
        // Sensor[] occupiedsensor = new Sensor[12];      //Make sure this is bigger than the list below
        // int[] expectedpreviouspaths = new int[12];  //Make sure this is bigger than the list below
        // int[] expectednextpaths = new int[12];      //Make sure this is bigger than the list below

        //  This matches up with the test file, ...
        sectionstotest[0] = sectionManager.getSection("NorthWest");
        assertNotNull(sectionstotest[0]);
        expectedsectionblocklistsize[0] = 1;
        expectedsectionentryblock[0] = blockstotest[0];
        expectedforwardEntryPointList[0] = 2;
        expectedsectionforwardblock[0][0] = blockstotest[10];
        expectedsectionforwardblock[0][1] = blockstotest[11];
        expectedreverseEntryPointList[0] = 2;
        expectedsectionreverseblock[0][0] = blockstotest[1];
        expectedsectionreverseblock[0][1] = blockstotest[2];
        expectedForwardBlockingSensors[0] = sensorManager.getSensor("ISSDIRF1");
        expectedReverseBlockingSensors[0] = sensorManager.getSensor("ISSDIRR1");
        expectedForwardStoppingSensors[0] = sensorManager.getSensor("ISSSTOPF1");
        expectedReverseStoppingSensors[0] = sensorManager.getSensor("ISSSTOPR1");

        sectionstotest[1] = sectionManager.getSection("North");
        assertNotNull(sectionstotest[1]);
        expectedsectionblocklistsize[1] = 1;
        expectedsectionentryblock[1] = blockstotest[1];
        expectedforwardEntryPointList[1] = 1;
        expectedsectionforwardblock[1][0] = blockstotest[0];
        expectedreverseEntryPointList[1] = 1;
        expectedsectionreverseblock[1][0] = blockstotest[3];
        expectedForwardBlockingSensors[1] = sensorManager.getSensor("ISSDIRF2");
        expectedReverseBlockingSensors[1] = sensorManager.getSensor("ISSDIRR2");
        expectedForwardStoppingSensors[1] = sensorManager.getSensor("ISSSTOPF2");
        expectedReverseStoppingSensors[1] = sensorManager.getSensor("ISSSTOPR2");

        sectionstotest[2] = sectionManager.getSection("NorthSiding");
        assertNotNull(sectionstotest[2]);
        expectedsectionblocklistsize[2] = 1;
        expectedsectionentryblock[2] = blockstotest[2];
        expectedforwardEntryPointList[2] = 1;
        expectedsectionforwardblock[2][0] = blockstotest[0];
        expectedreverseEntryPointList[2] = 1;
        expectedsectionreverseblock[2][0] = blockstotest[3];
        expectedForwardBlockingSensors[2] = sensorManager.getSensor("ISSDIRF3");
        expectedReverseBlockingSensors[2] = sensorManager.getSensor("ISSDIRR3");
        expectedForwardStoppingSensors[2] = sensorManager.getSensor("ISSSTOPF3");
        expectedReverseStoppingSensors[2] = sensorManager.getSensor("ISSSTOPR3");

        sectionstotest[3] = sectionManager.getSection("NorthEast");
        assertNotNull(sectionstotest[3]);
        expectedsectionblocklistsize[3] = 1;
        expectedsectionentryblock[3] = blockstotest[3];
        expectedforwardEntryPointList[3] = 2;
        expectedsectionforwardblock[3][0] = blockstotest[1];
        expectedsectionforwardblock[3][1] = blockstotest[2];
        expectedreverseEntryPointList[3] = 2;
        expectedsectionreverseblock[3][0] = blockstotest[4];
        expectedsectionreverseblock[3][1] = blockstotest[5];
        expectedForwardBlockingSensors[3] = sensorManager.getSensor("ISSDIRF4");
        expectedReverseBlockingSensors[3] = sensorManager.getSensor("ISSDIRR4");
        expectedForwardStoppingSensors[3] = sensorManager.getSensor("ISSSTOPF4");
        expectedReverseStoppingSensors[3] = sensorManager.getSensor("ISSSTOPR4");

        sectionstotest[4] = sectionManager.getSection("East");
        assertNotNull(sectionstotest[4]);
        expectedsectionblocklistsize[4] = 1;
        expectedsectionentryblock[4] = blockstotest[4];
        expectedforwardEntryPointList[4] = 1;
        expectedsectionforwardblock[4][0] = blockstotest[3];
        expectedreverseEntryPointList[4] = 1;
        expectedsectionreverseblock[4][0] = blockstotest[6];
        expectedForwardBlockingSensors[4] = sensorManager.getSensor("ISSDIRF5");
        expectedReverseBlockingSensors[4] = sensorManager.getSensor("ISSDIRR5");
        expectedForwardStoppingSensors[4] = sensorManager.getSensor("ISSSTOPF5");
        expectedReverseStoppingSensors[4] = sensorManager.getSensor("ISSSTOPR5");

        sectionstotest[5] = sectionManager.getSection("EastSiding");
        assertNotNull(sectionstotest[5]);
        expectedsectionblocklistsize[5] = 1;
        expectedsectionentryblock[5] = blockstotest[5];
        expectedforwardEntryPointList[5] = 1;
        expectedsectionforwardblock[5][0] = blockstotest[3];
        expectedreverseEntryPointList[5] = 1;
        expectedsectionreverseblock[5][0] = blockstotest[6];
        expectedForwardBlockingSensors[5] = sensorManager.getSensor("ISSDIRF6");
        expectedReverseBlockingSensors[5] = sensorManager.getSensor("ISSDIRR6");
        expectedForwardStoppingSensors[5] = sensorManager.getSensor("ISSSTOPF6");
        expectedReverseStoppingSensors[5] = sensorManager.getSensor("ISSSTOPR6");

        sectionstotest[6] = sectionManager.getSection("SouthEast");
        assertNotNull(sectionstotest[6]);
        expectedsectionblocklistsize[6] = 1;
        expectedsectionentryblock[6] = blockstotest[6];
        expectedforwardEntryPointList[6] = 2;
        expectedsectionforwardblock[6][0] = blockstotest[4];
        expectedsectionforwardblock[6][1] = blockstotest[5];
        expectedreverseEntryPointList[6] = 2;
        expectedsectionreverseblock[6][0] = blockstotest[7];
        expectedsectionreverseblock[6][1] = blockstotest[8];
        expectedForwardBlockingSensors[6] = sensorManager.getSensor("ISSDIRF7");
        expectedReverseBlockingSensors[6] = sensorManager.getSensor("ISSDIRR7");
        expectedForwardStoppingSensors[6] = sensorManager.getSensor("ISSSTOPF7");
        expectedReverseStoppingSensors[6] = sensorManager.getSensor("ISSSTOPR7");

        sectionstotest[7] = sectionManager.getSection("South");
        assertNotNull(sectionstotest[7]);
        expectedsectionblocklistsize[7] = 1;
        expectedsectionentryblock[7] = blockstotest[7];
        expectedforwardEntryPointList[7] = 1;
        expectedsectionforwardblock[7][0] = blockstotest[6];
        expectedreverseEntryPointList[7] = 1;
        expectedsectionreverseblock[7][0] = blockstotest[9];
        expectedForwardBlockingSensors[7] = sensorManager.getSensor("ISSDIRF8");
        expectedReverseBlockingSensors[7] = sensorManager.getSensor("ISSDIRR8");
        expectedForwardStoppingSensors[7] = sensorManager.getSensor("ISSSTOPF8");
        expectedReverseStoppingSensors[7] = sensorManager.getSensor("ISSSTOPR8");

        sectionstotest[8] = sectionManager.getSection("SouthSiding");
        assertNotNull(sectionstotest[8]);
        expectedsectionblocklistsize[8] = 1;
        expectedsectionentryblock[8] = blockstotest[8];
        expectedforwardEntryPointList[8] = 1;
        expectedsectionforwardblock[8][0] = blockstotest[6];
        expectedreverseEntryPointList[8] = 1;
        expectedsectionreverseblock[8][0] = blockstotest[9];
        expectedForwardBlockingSensors[8] = sensorManager.getSensor("ISSDIRF9");
        expectedReverseBlockingSensors[8] = sensorManager.getSensor("ISSDIRR9");
        expectedForwardStoppingSensors[8] = sensorManager.getSensor("ISSSTOPF9");
        expectedReverseStoppingSensors[8] = sensorManager.getSensor("ISSSTOPR9");

        sectionstotest[9] = sectionManager.getSection("SouthWest");
        assertNotNull(sectionstotest[9]);
        expectedsectionblocklistsize[9] = 1;
        expectedsectionentryblock[9] = blockstotest[9];
        expectedforwardEntryPointList[9] = 2;
        expectedsectionforwardblock[9][0] = blockstotest[7];
        expectedsectionforwardblock[9][1] = blockstotest[8];
        expectedreverseEntryPointList[9] = 2;
        expectedsectionreverseblock[9][0] = blockstotest[10];
        expectedsectionreverseblock[9][1] = blockstotest[11];
        expectedForwardBlockingSensors[9] = sensorManager.getSensor("ISSDIRF10");
        expectedReverseBlockingSensors[9] = sensorManager.getSensor("ISSDIRR10");
        expectedForwardStoppingSensors[9] = sensorManager.getSensor("ISSSTOPF10");
        expectedReverseStoppingSensors[9] = sensorManager.getSensor("ISSSTOPR10");

        sectionstotest[10] = sectionManager.getSection("West");
        assertNotNull(sectionstotest[10]);
        expectedsectionblocklistsize[10] = 1;
        expectedsectionentryblock[10] = blockstotest[10];
        expectedforwardEntryPointList[10] = 1;
        expectedsectionforwardblock[10][0] = blockstotest[9];
        expectedreverseEntryPointList[10] = 1;
        expectedsectionreverseblock[10][0] = blockstotest[0];
        expectedForwardBlockingSensors[10] = sensorManager.getSensor("ISSDIRF11");
        expectedReverseBlockingSensors[10] = sensorManager.getSensor("ISSDIRR11");
        expectedForwardStoppingSensors[10] = sensorManager.getSensor("ISSSTOPF11");
        expectedReverseStoppingSensors[10] = sensorManager.getSensor("ISSSTOPR11");

        sectionstotest[11] = sectionManager.getSection("WestSiding");
        assertNotNull(sectionstotest[11]);
        expectedsectionblocklistsize[11] = 1;
        expectedsectionentryblock[11] = blockstotest[11];
        expectedforwardEntryPointList[11] = 1;
        expectedsectionforwardblock[11][0] = blockstotest[9];
        expectedreverseEntryPointList[11] = 1;
        expectedsectionreverseblock[11][0] = blockstotest[0];
        expectedForwardBlockingSensors[11] = sensorManager.getSensor("ISSDIRF12");
        expectedReverseBlockingSensors[11] = sensorManager.getSensor("ISSDIRR12");
        expectedForwardStoppingSensors[11] = sensorManager.getSensor("ISSSTOPF12");
        expectedReverseStoppingSensors[11] = sensorManager.getSensor("ISSSTOPR12");

        for (int testsectionfocus = 0; testsectionfocus < 12; testsectionfocus++) {  // Set to one greater than above
            // check existence of sections
            Section testsection = sectionstotest[testsectionfocus];
            List<Block> blockList = testsection.getBlockList();
            assertEquals(expectedsectionblocklistsize[testsectionfocus], blockList.size(),
                "Section size where Focus was: " + testsectionfocus);

            Block entryblock = testsection.getEntryBlock();
            assertNotNull(entryblock);
            assertEquals( expectedsectionentryblock[testsectionfocus].getSystemName(), entryblock.getSystemName(),
                "Section entry block where Focus was: " + testsectionfocus);

            List<EntryPoint> forwardEntryPointList = testsection.getForwardEntryPointList();
            assertEquals( expectedforwardEntryPointList[testsectionfocus], forwardEntryPointList.size(),
                "Section forward size where Focus was: " + testsectionfocus);
            for (int e = 0; e < forwardEntryPointList.size(); e++) {
                EntryPoint get = forwardEntryPointList.get(e);
                assertEquals( expectedsectionentryblock[testsectionfocus].getSystemName(),
                    get.getBlock().getSystemName(),
                    "Focus was: " + testsectionfocus + " next forward entry point: " + e);
                assertEquals( expectedsectionforwardblock[testsectionfocus][e].getUserName(),
                    get.getFromBlock().getUserName(),
                    "Focus was: " + testsectionfocus + " next forward from: " + e);
                assertEquals( 4, get.getDirection(),
                    "Focus was: " + testsectionfocus + " next forward dir: " + e);
            }
            List<EntryPoint> reverseEntryPointList = testsection.getReverseEntryPointList();
            assertEquals( expectedreverseEntryPointList[testsectionfocus], reverseEntryPointList.size(),
                "Section forward size where Focus was: " + testsectionfocus);
            for (int e = 0; e < reverseEntryPointList.size(); e++) {
                EntryPoint get = reverseEntryPointList.get(e);
                assertEquals( expectedsectionentryblock[testsectionfocus].getSystemName(),
                    get.getBlock().getSystemName(),
                    "Focus was: " + testsectionfocus + " next reverse entry point: " + e);
                assertEquals( expectedsectionreverseblock[testsectionfocus][e].getUserName(),
                    get.getFromBlock().getUserName(),
                    "Focus was: " + testsectionfocus + " next reverse from: " + e);
                assertEquals( 8, get.getDirection(),
                    "Focus was: " + testsectionfocus + " next reverse dir: " + e);
            }

            Sensor expectedForwardBlockingSensor = expectedForwardBlockingSensors[testsectionfocus];
            Sensor actualForwardBlockingSensor = testsection.getForwardBlockingSensor();
            assertNotNull( expectedForwardBlockingSensor, "Focus was: " + testsectionfocus + " expectedForwardBlockingSensor");
            assertNotNull( actualForwardBlockingSensor, "Focus was: " + testsectionfocus + " actualForwardBlockingSensor");
            assertEquals( expectedForwardBlockingSensor,actualForwardBlockingSensor,
                "Focus was: " + testsectionfocus + " ForwardBlockingSensor");

            Sensor expectedReverseBlockingSensor = expectedReverseBlockingSensors[testsectionfocus];
            Sensor actualReverseBlockingSensor = testsection.getReverseBlockingSensor();
            assertNotNull( expectedReverseBlockingSensor, "Focus was: " + testsectionfocus + " expectedReverseBlockingSensor");
            assertNotNull( actualReverseBlockingSensor, "Focus was: " + testsectionfocus + " actualReverseBlockingSensor");
            assertEquals( expectedReverseBlockingSensor, actualReverseBlockingSensor,
                "Focus was: " + testsectionfocus + " ReverseBlockingSensor");

            Sensor expectedForwardStoppingSensor = expectedForwardStoppingSensors[testsectionfocus];
            Sensor actualForwardStoppingSensor = testsection.getForwardStoppingSensor();
            assertNotNull( expectedForwardStoppingSensor, "Focus was: " + testsectionfocus + " expectedForwardStoppingSensor");
            assertNotNull( actualForwardStoppingSensor, "Focus was: " + testsectionfocus + " actualForwardStoppingSensor");
            assertEquals( expectedForwardStoppingSensor, actualForwardStoppingSensor,
                "Focus was: " + testsectionfocus + " ForwardStoppingSensor");

            Sensor expectedReverseStoppingSensor = expectedReverseStoppingSensors[testsectionfocus];
            Sensor actualReverseStoppingSensor = testsection.getReverseStoppingSensor();
            assertNotNull( expectedReverseStoppingSensor, "Focus was: " + testsectionfocus + " expectedReverseStoppingSensor");
            assertNotNull( actualReverseStoppingSensor, "Focus was: " + testsectionfocus + " actualReverseStoppingSensor");
            assertEquals( expectedReverseStoppingSensor, actualReverseStoppingSensor,
                "Focus was: " + testsectionfocus + " ReverseStoppingSensor");
        }

    }

    @BeforeEach
    public void setUp(@TempDir java.io.File folder) {
        JUnitUtil.setUp();
        Assertions.assertDoesNotThrow( () ->
            JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder)));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
