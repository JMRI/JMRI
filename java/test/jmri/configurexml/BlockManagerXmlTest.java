package jmri.configurexml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jmri.*;
import jmri.implementation.AbstractSensor;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for BlockManagerXml.
 * <p>
 * Just tests Elements, not actual files. Based upon a stub by Bob Jacobsen
 * Copyright 2008
 *
 * @author Bob Coleman Copyright 2012
 */
public class BlockManagerXmlTest {

    @Test
    public void testLoadCurrent() throws JmriException {
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLayoutBlockManager();
        // load file
        InstanceManager.getDefault(ConfigureManager.class)
                .load(new java.io.File("java/test/jmri/configurexml/load/BlockManagerXmlTest.xml"));

        // check existence of blocks
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("IB1"));
        assertNull(InstanceManager.getDefault(BlockManager.class).getBlock("no block"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("IB2"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("IB3"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("IB4"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("IB5"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("IB6"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("IB7"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("IB8"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("IB9"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("IB10"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("IB11"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("IB12"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("blocknorthwest"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("blocknorth"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("blocknorthsiding"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("blocknortheast"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("blockeast"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("blockeastsiding"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("blocksoutheast"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("blocksouth"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("blocksouthsiding"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("blocksouthwest"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("blockwest"));
        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("blockwestsiding"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB1"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB2"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB3"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB4"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB5"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB6"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB7"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB8"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB9"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB10"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB11"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB12"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocknorthwest"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocknorth"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocknorthsiding"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocknortheast"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blockeast"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blockeastsiding"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocksoutheast"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocksouth"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocksouthsiding"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocksouthwest"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blockwest"));
//         assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blockwestsiding"));

        // check existence of turmouts
        assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
        assertNull(InstanceManager.turnoutManagerInstance().getTurnout("no turnout"));
        assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT2"));
        assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT3"));
        assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT4"));
        assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT5"));
        assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT6"));
        assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT7"));
        assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT8"));

        // check existence of memories
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0001"));
        assertNull(InstanceManager.memoryManagerInstance().getMemory("no memory"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0002"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0003"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0004"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0005"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0006"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0007"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0008"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0009"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0010"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0011"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0012"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocknorthwestmemory"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocknorthmemory"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocknorthsidingmemory"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocknortheastmemory"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blockeastmemory"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blockeastsidingmemory"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocksoutheastmemory"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocksouthmemory"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocksouthsidingmemory"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocksouthwestmemory"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blockwestmemory"));
        assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blockwestsidingmemory"));

        // check existence of sensors
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO1"));
        assertNull(InstanceManager.sensorManagerInstance().getSensor("no sensor"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO2"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO3"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO4"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO5"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO6"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO7"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO8"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO9"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO10"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO11"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO12"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocknorthwestoccupied"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocknorthoccupied"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocknorthsidingoccupied"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocknortheastoccupied"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blockeastoccupied"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blockeastsidingoccupied"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocksoutheastoccupied"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocksouthoccupied"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocksouthsidingoccupied"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocksouthwestoccupied"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blockwestoccupied"));
        assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blockwestsidingoccupied"));

        // check existence of paths between blocks
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
        previousblock = new Block[12][4];     //Make sure this is bigger than the list below
        Block[][] nextblock;
        nextblock = new Block[12][4];         //Make sure this is bigger than the list below

        //  This matches up with the test file, ...
        blockstotest[0] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocknorthwest");
        assertNotNull(blockstotest[0]);
        occupiedsensor[0] = InstanceManager.sensorManagerInstance().getSensor("blocknorthwestoccupied");
        assertNotNull(occupiedsensor[0]);
        blockstotest[1] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocknorth");
        assertNotNull(blockstotest[1]);
        occupiedsensor[1] = InstanceManager.sensorManagerInstance().getSensor("blocknorthoccupied");
        assertNotNull(occupiedsensor[1]);
        blockstotest[2] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocknorthsiding");
        assertNotNull(blockstotest[2]);
        occupiedsensor[2] = InstanceManager.sensorManagerInstance().getSensor("blocknorthsidingoccupied");
        assertNotNull(occupiedsensor[2]);
        blockstotest[3] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocknortheast");
        assertNotNull(blockstotest[3]);
        occupiedsensor[3] = InstanceManager.sensorManagerInstance().getSensor("blocknortheastoccupied");
        assertNotNull(occupiedsensor[3]);
        blockstotest[4] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blockeast");
        assertNotNull(blockstotest[4]);
        occupiedsensor[4] = InstanceManager.sensorManagerInstance().getSensor("blockeastoccupied");
        assertNotNull(occupiedsensor[4]);
        blockstotest[5] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blockeastsiding");
        assertNotNull(blockstotest[5]);
        occupiedsensor[5] = InstanceManager.sensorManagerInstance().getSensor("blockeastsidingoccupied");
        assertNotNull(occupiedsensor[5]);
        blockstotest[6] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocksoutheast");
        assertNotNull(blockstotest[6]);
        occupiedsensor[6] = InstanceManager.sensorManagerInstance().getSensor("blocksoutheastoccupied");
        assertNotNull(occupiedsensor[6]);
        blockstotest[7] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocksouth");
        assertNotNull(blockstotest[7]);
        occupiedsensor[7] = InstanceManager.sensorManagerInstance().getSensor("blocksouthoccupied");
        assertNotNull(occupiedsensor[7]);
        blockstotest[8] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocksouthsiding");
        assertNotNull(blockstotest[8]);
        occupiedsensor[8] = InstanceManager.sensorManagerInstance().getSensor("blocksouthsidingoccupied");
        assertNotNull(occupiedsensor[8]);
        blockstotest[9] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocksouthwest");
        assertNotNull(blockstotest[9]);
        occupiedsensor[9] = InstanceManager.sensorManagerInstance().getSensor("blocksouthwestoccupied");
        assertNotNull(occupiedsensor[9]);
        blockstotest[10] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blockwest");
        assertNotNull(blockstotest[10]);
        occupiedsensor[10] = InstanceManager.sensorManagerInstance().getSensor("blockwestoccupied");
        assertNotNull(occupiedsensor[10]);
        blockstotest[11] = InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blockwestsiding");
        assertNotNull(blockstotest[11]);
        occupiedsensor[11] = InstanceManager.sensorManagerInstance().getSensor("blockwestsidingoccupied");
        assertNotNull(occupiedsensor[11]);

        // The references are circular so the definitons are split up, ...
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
            assertNotNull(expectedtestmemory);
            expectedtestmemory.setValue("Memory test: " + testblockfocus);
            assertNotNull(expectedtestmemory);
// TODO: BOB C: Memory Test
//            Memory actualtestmemory = (Memory) focusBlock.getValue();
//            Assert.assertNotNull(actualtestmemory);
//            Assert.assertEquals("Memory where Focus was: " + testblockfocus, expectedtestmemory, actualtestmemory);
            Sensor fbSensor = focusBlock.getSensor();
            assertNotNull(fbSensor);
            assertEquals( occupiedsensor[testblockfocus].getSystemName(),
                fbSensor.getSystemName(), "Sensor where Focus was: " + testblockfocus);
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
                assertTrue( passprevioustest[p],
                    "Block Focus was: " + testblockfocus + " previous path: " + p);
            }
            for (int n = 0; n < expectednextpaths[testblockfocus]; n++) {
                assertTrue( passnexttest[n],
                    "Block Focus was: " + testblockfocus + " next path: " + n);
            }

        }
    }

    /**
     * This test checks that the store operation runs, but doesn't check the
     * output for correctness.
     * 
     * @throws jmri.JmriException if unanticipated exception is thrown
     */
    @Test
    public void testStore() throws JmriException {
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLayoutBlockManager();

        Block b1 = InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock("SystemNameb1", "");

        Block b2 = InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock("SystemNameb2", "");
        assertNotNull(b2);
        Sensor s2 = new AbstractSensor("IS2") {
            @Override
            public void requestUpdateFromLayout() {
            }
        };
        b2.setSensor("IS2");
        s2.setState(Sensor.ACTIVE);
        b2.setValue("b2 contents");

        Path p21 = new Path();
        p21.setBlock(b1);
        p21.setFromBlockDirection(Path.RIGHT);
        p21.setToBlockDirection(Path.LEFT);
        p21.addSetting(new BeanSetting(new jmri.implementation.AbstractTurnout("IT1") {
            @Override
            public void turnoutPushbuttonLockout(boolean b) {
            }

            @Override
            public void forwardCommandChangeToLayout(int i) {
            }
        },
                jmri.Turnout.THROWN));
        b2.addPath(p21);

        //BlockManagerXml tb = new BlockManagerXml();
    }

    @Test
    public void testBlockAndSignalMastTest() throws JmriException {
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLayoutBlockManager();
        // load file
        InstanceManager.getDefault(ConfigureManager.class)
                .load(new java.io.File("java/test/jmri/configurexml/loadref/BlockAndSignalMastTest.xml"));
        // in loadref because comparison not working right

        assertNotNull(InstanceManager.getDefault(BlockManager.class).getBlock("IB1"));

        SignalMast m1 = InstanceManager.getDefault(SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0001)");
        assertNotNull(m1);
        SignalMast m2 = InstanceManager.getDefault(SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0002)");
        assertNotNull(m2);
        SignalMast m3 = InstanceManager.getDefault(SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0003)");
        assertNotNull(m3);
        SignalMast m4 = InstanceManager.getDefault(SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0004)");
        assertNotNull(m4);
        SignalMast m5 = InstanceManager.getDefault(SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0005)");
        assertNotNull(m5);
        SignalMast m6 = InstanceManager.getDefault(SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0006)");
        assertNotNull(m6);
        SignalMast m7 = InstanceManager.getDefault(SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0007)");
        assertNotNull(m7);

        // allow listeners to process, but keep it quick by looking for desired result
        JUnitUtil.waitFor(()->{
                return ( "Advance Approach".equals(m1.getAspect())
                    && "Clear".equals(m2.getAspect())
                    && "Clear".equals(m3.getAspect())
                    && "Clear".equals(m4.getAspect())
                    && "Approach".equals(m5.getAspect())
                    && "Stop".equals(m6.getAspect())
                    && "Stop".equals(m7.getAspect()) );
            },"Mast state ended as \""+m1.getAspect()+"\" \""+m2.getAspect()+"\" \""+m3.getAspect()+"\" \""
                +m4.getAspect()+"\" \""+m5.getAspect()+"\" \""+m6.getAspect()+"\" \""+m7.getAspect()+"\", desired state AA/C/C/C/A/S/S");

        // check for expected mast state 
        assertEquals( "Advance Approach", m1.getAspect(), "Signal 1");
        assertEquals( "Clear", m2.getAspect(), "Signal 2");
        assertEquals( "Clear", m3.getAspect(), "Signal 3");
        assertEquals( "Clear", m4.getAspect(), "Signal 4");
        assertEquals( "Approach", m5.getAspect(), "Signal 5");
        assertEquals( "Stop", m6.getAspect(), "Signal 6");
        assertEquals( "Stop", m7.getAspect(), "Signal 7");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
