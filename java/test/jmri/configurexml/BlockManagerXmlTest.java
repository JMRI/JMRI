package jmri.configurexml;

import java.util.List;
import jmri.BeanSetting;
import jmri.Block;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.Path;
import jmri.Sensor;
import jmri.SignalMast;
import jmri.implementation.AbstractSensor;
import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

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
    public void testLoadCurrent() throws Exception {
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
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("IB1"));
        Assert.assertNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("no block"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("IB2"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("IB3"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("IB4"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("IB5"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("IB6"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("IB7"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("IB8"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("IB9"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("IB10"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("IB11"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("IB12"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocknorthwest"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocknorth"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocknorthsiding"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocknortheast"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blockeast"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blockeastsiding"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocksoutheast"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocksouth"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocksouthsiding"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blocksouthwest"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blockwest"));
        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("blockwestsiding"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB1"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB2"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB3"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB4"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB5"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB6"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB7"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB8"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB9"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB10"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB11"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("ILB12"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocknorthwest"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocknorth"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocknorthsiding"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocknortheast"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blockeast"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blockeastsiding"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocksoutheast"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocksouth"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocksouthsiding"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blocksouthwest"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blockwest"));
//         Assert.assertNotNull(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock("blockwestsiding"));

        // check existence of turmouts
        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
        Assert.assertNull(InstanceManager.turnoutManagerInstance().getTurnout("no turnout"));
        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT2"));
        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT3"));
        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT4"));
        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT5"));
        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT6"));
        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT7"));
        Assert.assertNotNull(InstanceManager.turnoutManagerInstance().getTurnout("IT8"));

        // check existence of memories
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0001"));
        Assert.assertNull(InstanceManager.memoryManagerInstance().getMemory("no memory"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0002"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0003"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0004"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0005"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0006"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0007"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0008"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0009"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0010"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0011"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("IM:AUTO:0012"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocknorthwestmemory"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocknorthmemory"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocknorthsidingmemory"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocknortheastmemory"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blockeastmemory"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blockeastsidingmemory"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocksoutheastmemory"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocksouthmemory"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocksouthsidingmemory"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blocksouthwestmemory"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blockwestmemory"));
        Assert.assertNotNull(InstanceManager.memoryManagerInstance().getMemory("blockwestsidingmemory"));

        // check existence of sensors
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO1"));
        Assert.assertNull(InstanceManager.sensorManagerInstance().getSensor("no sensor"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO2"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO3"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO4"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO5"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO6"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO7"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO8"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO9"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO10"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO11"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("ISBO12"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocknorthwestoccupied"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocknorthoccupied"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocknorthsidingoccupied"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocknortheastoccupied"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blockeastoccupied"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blockeastsidingoccupied"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocksoutheastoccupied"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocksouthoccupied"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocksouthsidingoccupied"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blocksouthwestoccupied"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blockwestoccupied"));
        Assert.assertNotNull(InstanceManager.sensorManagerInstance().getSensor("blockwestsidingoccupied"));

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
            Assert.assertNotNull(expectedtestmemory);
            expectedtestmemory.setValue("Memory test: " + testblockfocus);
            Assert.assertNotNull(expectedtestmemory);
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
    }

    /**
     * This test checks that the store operation runs, but doesn't check the
     * output for correctness.
     * 
     * @throws jmri.JmriException if unanticipated exception is thrown
     */
    @Test
    public void testStore() throws jmri.JmriException {
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initLayoutBlockManager();

        Block b1 = InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock("SystemNameb1", "");

        Block b2 = InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock("SystemNameb2", "");
        Assert.assertNotNull(b2);
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
    public void testBlockAndSignalMastTest() throws Exception {
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

        Assert.assertNotNull(InstanceManager.getDefault(jmri.BlockManager.class).getBlock("IB1"));

        SignalMast m1 = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0001)");
        Assert.assertNotNull(m1);
        SignalMast m2 = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0002)");
        Assert.assertNotNull(m2);
        SignalMast m3 = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0003)");
        Assert.assertNotNull(m3);
        SignalMast m4 = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0004)");
        Assert.assertNotNull(m4);
        SignalMast m5 = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0005)");
        Assert.assertNotNull(m5);
        SignalMast m6 = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0006)");
        Assert.assertNotNull(m6);
        SignalMast m7 = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0007)");
        Assert.assertNotNull(m7);

        // allow listeners to process, but keep it quick by looking for desired result
        JUnitUtil.waitFor(()->{
                return (m1.getAspect().equals("Advance Approach")
                    && m2.getAspect().equals("Clear")
                    && m3.getAspect().equals("Clear")
                    && m4.getAspect().equals("Clear")
                    && m5.getAspect().equals("Approach")
                    && m6.getAspect().equals("Stop")
                    && m7.getAspect().equals("Stop"));
            },"Mast state ended as \""+m1.getAspect()+"\" \""+m2.getAspect()+"\" \""+m3.getAspect()+"\" \""+m4.getAspect()+"\" \""+m5.getAspect()+"\" \""+m6.getAspect()+"\" \""+m7.getAspect()+"\", desired state AA/C/C/C/A/S/S");

        // check for expected mast state 
        Assert.assertEquals("Signal 1", "Advance Approach", InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0001)").getAspect());
        Assert.assertEquals("Signal 2", "Clear",            InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0002)").getAspect());
        Assert.assertEquals("Signal 3", "Clear",            InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0003)").getAspect());
        Assert.assertEquals("Signal 4", "Clear",            InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0004)").getAspect());
        Assert.assertEquals("Signal 5", "Approach",         InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0005)").getAspect());
        Assert.assertEquals("Signal 6", "Stop",             InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0006)").getAspect());
        Assert.assertEquals("Signal 7", "Stop",             InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast("IF$vsm:AAR-1946:SL-2-high-abs($0007)").getAspect());

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
