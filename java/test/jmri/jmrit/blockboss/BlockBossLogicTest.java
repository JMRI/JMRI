package jmri.jmrit.blockboss;

import java.util.ArrayList;
import java.util.Enumeration;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the BlockBossLogic class
 *
 * @author Bob Jacobsen
 */
public class BlockBossLogicTest {
    
    // test creation
    @Test
    public void testCreate() {
        p = new BlockBossLogic("IH2");
        assertThat(p.getDrivenSignal()).withFailMessage("driven signal name").isEqualTo("IH2");
    }

    // test simplest block, just signal following
    @Test
    public void testSimpleBlock() {
        setupSimpleBlock();
        startLogic();
        assertThat(p.getDrivenSignal()).withFailMessage("driven signal name").isEqualTo("IH1");
        
        JUnitUtil.setBeanStateAndWait(h2, SignalHead.YELLOW);
        JUnitUtil.waitFor(()-> SignalHead.GREEN == h1.getAppearance(), "Stuck at "+h1.getAppearance()+" so yellow sets green");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.RED);
        JUnitUtil.waitFor(()-> SignalHead.YELLOW == h1.getAppearance(), "Stuck at "+h1.getAppearance()+" so red sets yellow");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.GREEN);
        JUnitUtil.waitFor(()-> SignalHead.GREEN == h1.getAppearance(), "Stuck at "+h1.getAppearance()+" so green sets green");  // wait and test
    }

    // test that initial conditions are set right
    @Test
    public void testSimpleBlockInitial() {
        setupSimpleBlock();
        startLogic();

        JUnitUtil.waitFor(()-> SignalHead.YELLOW == h1.getAppearance(), "initial red sets yellow");  // wait and test
    }

    // occupancy check
    @Test
    public void testSimpleBlockOccupancy() {
        setupSimpleBlock();
        p.setSensor1("IS1");
        startLogic();
        JUnitUtil.setBeanState(s1, Sensor.INACTIVE);
        
        JUnitUtil.setBeanStateAndWait(h2, SignalHead.YELLOW);
        JUnitUtil.waitFor(()-> SignalHead.GREEN == h1.getAppearance(), "Stuck at "+h1.getAppearance()+" so yellow sets green");  // wait and test

        JUnitUtil.setBeanState(s1, Sensor.ACTIVE);
        JUnitUtil.waitFor(()-> SignalHead.RED == h1.getAppearance(), "Stuck at "+h1.getAppearance()+" so occupied sets red");  // wait and test

        JUnitUtil.setBeanState(s1, Sensor.INACTIVE);
        JUnitUtil.waitFor(()-> SignalHead.GREEN == h1.getAppearance(), "Stuck at "+h1.getAppearance()+" so unoccupied sets green");  // wait and test
    }

    // test signal following in distant simple block
    @Test
    public void testSimpleBlockDistant() {
        setupSimpleBlock();
        p.setDistantSignal(true);
        startLogic();

        assertThat(p.getDrivenSignal()).withFailMessage("driven signal name").isEqualTo("IH1");

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.YELLOW);
        JUnitUtil.waitFor(()-> SignalHead.YELLOW == h1.getAppearance(), "yellow sets yellow");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.RED);
        JUnitUtil.waitFor(()-> SignalHead.RED == h1.getAppearance(), "red sets red");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.GREEN);
        JUnitUtil.waitFor(()-> SignalHead.GREEN == h1.getAppearance(), "green sets green");  // wait and test
    }

    // test signal following in limited simple block
    // (not particularly interesting, as next signal can't set red)
    @Test
    public void testSimpleBlockLimited() {
        setupSimpleBlock();
        p.setLimitSpeed1(true);
        startLogic();

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.RED);
        JUnitUtil.waitFor(()-> SignalHead.YELLOW == h1.getAppearance(), "red sets yellow");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.YELLOW);
        JUnitUtil.waitFor(()-> SignalHead.YELLOW == h1.getAppearance(), "yellow sets yellow");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.GREEN);
        JUnitUtil.waitFor(()-> SignalHead.YELLOW == h1.getAppearance(), "green sets yellow");  // wait and test
    }

    // test signal following in distant, limited simple block
    @Test
    public void testSimpleBlockDistantLimited() {
        setupSimpleBlock();
        p.setDistantSignal(true);
        p.setLimitSpeed1(true);
        startLogic();

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.YELLOW);
        JUnitUtil.waitFor(()-> SignalHead.YELLOW == h1.getAppearance(), "yellow sets yellow");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.RED);
        JUnitUtil.waitFor(()-> SignalHead.RED == h1.getAppearance(), "red sets red");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.GREEN);
        JUnitUtil.waitFor(()-> SignalHead.YELLOW == h1.getAppearance(), "green sets yellow");  // wait and test
    }

    // test signal following in restricting simple block
    @Test
    public void testSimpleBlockRestricting() {
        JUnitUtil.setBeanState(s1, Sensor.INACTIVE);

        setupSimpleBlock();
        p.setSensor1("IS1");
        p.setRestrictingSpeed1(true);
        startLogic();
        
        JUnitUtil.setBeanStateAndWait(h2, SignalHead.YELLOW);
        JUnitUtil.waitFor(()-> SignalHead.FLASHRED == h1.getAppearance(), "yellow sets flashing red");  // wait and test

        JUnitUtil.setBeanState(s1, Sensor.ACTIVE);
        JUnitUtil.waitFor(()-> SignalHead.RED == h1.getAppearance(), "Stuck at "+h1.getAppearance()+" so occupied sets red");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.GREEN);
        JUnitUtil.setBeanState(s1, Sensor.INACTIVE);
        JUnitUtil.waitFor(()-> SignalHead.FLASHRED == h1.getAppearance(), "Stuck at "+h1.getAppearance()+" so unoccupied green sets flashing red");  // wait and test
    }

    // if no next signal, next signal considered green
    @Test
    public void testSimpleBlockNoNext() throws jmri.JmriException {
        s1.setState(Sensor.INACTIVE);
        
        p = new BlockBossLogic("IH1");
        p.setSensor1("1");
        p.setMode(BlockBossLogic.SINGLEBLOCK);
        startLogic();

        JUnitUtil.waitFor(()-> SignalHead.GREEN == h1.getAppearance(), "missing signal is green");  // wait and test
    }

    // if no next signal, next signal is considered green
    @Test
    public void testSimpleBlockNoNextLimited() throws jmri.JmriException {
        s1.setState(Sensor.INACTIVE);
        
        p = new BlockBossLogic("IH1");
        p.setMode(BlockBossLogic.SINGLEBLOCK);
        p.setSensor1("1");
        p.setLimitSpeed1(true);

        startLogic();

        JUnitUtil.waitFor(()-> SignalHead.YELLOW == h1.getAppearance(), "missing signal is green, show yellow");  // wait and test
    }

    // check for basic not-fail if no signal name was set
    @Test
    public void testSimpleBlockNoSignal() {
        try { 
            new BlockBossLogic(null);
        } catch (java.lang.NullPointerException e) {
            // this is expected
        }
    }

    // check for basic not-fail if empty signal name was set
    @Test
    public void testSimpleBlockEmptyName() {
        try {
            new BlockBossLogic("");
        } catch (java.lang.IllegalArgumentException e) {
            // this is expected
        }
        jmri.util.JUnitAppender.assertWarnMessage("Signal Head \"\" was not found");
    }

    // test interruption
    @Test
    public void testInterrupt() throws jmri.JmriException {
        s1.setState(Sensor.INACTIVE);
        
        forceInterrupt = false;
        p = new BlockBossLogic("IH1") {
            @Override
            public void setOutput() {
                testThread = this.thread;
                if (forceInterrupt) {
                    testThread.interrupt(); // force an interrupt of the SSL thread
                }
                super.setOutput();
            }
        };
        p.setMode(BlockBossLogic.SINGLEBLOCK);
        p.setSensor1("1");
        p.setLimitSpeed1(true);

        startLogic();

        JUnitUtil.waitFor(()-> p.isRunning(), "is running");
                
        forceInterrupt = true;
        s1.setState(Sensor.ACTIVE);
        
        JUnitUtil.waitFor(()-> !p.isRunning(), "is stopped");
    }


    // check that user names were preserved
    @Test
    public void testUserNamesRetained() {
        p = new BlockBossLogic("IH1");

        p.setSensor1("1");
        p.setSensor2("2");
        p.setSensor3("3");
        p.setSensor4("4");
        p.setSensor5("10");

        p.setTurnout("1");

        p.setWatchedSignal1("1", false);
        p.setWatchedSignal1Alt("2");
        p.setWatchedSignal2("3");
        p.setWatchedSignal2Alt("4");

        p.setWatchedSensor1("5");
        p.setWatchedSensor1Alt("6");
        p.setWatchedSensor2("7");
        p.setWatchedSensor2Alt("8");

        p.setApproachSensor1("9");

        assertThat(p.getSensor1()).withFailMessage("sensor1").isEqualTo("1");
        assertThat(p.getSensor2()).withFailMessage("sensor2").isEqualTo("2");
        assertThat(p.getSensor3()).withFailMessage("sensor3").isEqualTo("3");
        assertThat(p.getSensor4()).withFailMessage("sensor4").isEqualTo("4");
        assertThat(p.getSensor5()).withFailMessage("sensor5").isEqualTo("10");

        assertThat(p.getTurnout()).withFailMessage("turnout1").isEqualTo("1");

        assertThat(p.getWatchedSignal1()).withFailMessage("watchedsignal1").isEqualTo("1");
        assertThat(p.getWatchedSignal1Alt()).withFailMessage("watchedsignal1alt").isEqualTo("2");
        assertThat(p.getWatchedSignal2()).withFailMessage("watchedsignal2").isEqualTo("3");
        assertThat(p.getWatchedSignal2Alt()).withFailMessage("watchedsignal2alt").isEqualTo("4");

        assertThat(p.getWatchedSensor1()).withFailMessage("watchedsensor1").isEqualTo("5");
        assertThat(p.getWatchedSensor1Alt()).withFailMessage("watchedsensor1alt").isEqualTo("6");
        assertThat(p.getWatchedSensor2()).withFailMessage("watchedsensor2").isEqualTo("7");
        assertThat(p.getWatchedSensor2Alt()).withFailMessage("watchedsensor2alt").isEqualTo("8");

        assertThat(p.getApproachSensor1()).withFailMessage("approach").isEqualTo("9");

    }

    // check that system names were preserved
    @Test
    public void testSystemNamesRetained() {
        p = new BlockBossLogic("IH1");

        p.setSensor1("IS1");
        p.setSensor2("IS2");
        p.setSensor3("IS3");
        p.setSensor4("IS4");
        p.setSensor5("IS10");

        p.setTurnout("IT1");

        p.setWatchedSignal1("IH1", false);
        p.setWatchedSignal1Alt("IH2");
        p.setWatchedSignal2("IH3");
        p.setWatchedSignal2Alt("IH4");

        p.setWatchedSensor1("IS5");
        p.setWatchedSensor1Alt("IS6");
        p.setWatchedSensor2("IS7");
        p.setWatchedSensor2Alt("IS8");

        p.setApproachSensor1("IS9");

        assertThat(p.getSensor1()).withFailMessage("sensor1").isEqualTo("IS1");
        assertThat(p.getSensor2()).withFailMessage("sensor2").isEqualTo("IS2");
        assertThat(p.getSensor3()).withFailMessage("sensor3").isEqualTo("IS3");
        assertThat(p.getSensor4()).withFailMessage("sensor4").isEqualTo("IS4");
        assertThat(p.getSensor5()).withFailMessage("sensor5").isEqualTo("IS10");

        assertThat(p.getTurnout()).withFailMessage("turnout1").isEqualTo("IT1");

        assertThat(p.getWatchedSignal1()).withFailMessage("watchedsignal1").isEqualTo("IH1");
        assertThat(p.getWatchedSignal1Alt()).withFailMessage("watchedsignal1alt").isEqualTo("IH2");
        assertThat(p.getWatchedSignal2()).withFailMessage("watchedsignal2").isEqualTo("IH3");
        assertThat(p.getWatchedSignal2Alt()).withFailMessage("watchedsignal2alt").isEqualTo("IH4");

        assertThat(p.getWatchedSensor1()).withFailMessage("watchedsensor1").isEqualTo("IS5");
        assertThat(p.getWatchedSensor1Alt()).withFailMessage("watchedsensor1alt").isEqualTo("IS6");
        assertThat(p.getWatchedSensor2()).withFailMessage("watchedsensor2").isEqualTo("IS7");
        assertThat(p.getWatchedSensor2Alt()).withFailMessage("watchedsensor2alt").isEqualTo("IS8");

        assertThat(p.getApproachSensor1()).withFailMessage("approach").isEqualTo("IS9");

    }

    Turnout t1, t2, t3;
    Sensor s1, s2, s3, s4, s5, s6, s7, s8, s9, s10;
    SignalHead h1, h2, h3, h4;
    BlockBossLogic p;

    Thread testThread = null;
    boolean forceInterrupt = false;

    protected void startLogic() {
        if (p != null) {
            p.start();
        }
    }

    protected void stopLogic() {
        if (p != null) {
            p.stop();
        }
    }

    void setupSimpleBlock() {
        p = new BlockBossLogic("IH1");
        p.setMode(BlockBossLogic.SINGLEBLOCK);
        p.setWatchedSignal1("IH2", false);
    }

    /**
     * Test-by test initialization. Does log4j for standalone use, and then
     * creates a set of turnouts, sensors and signals as common background for
     * testing
     */
    @BeforeEach
    public void setUp() {
        // reset InstanceManager
        JUnitUtil.setUp();
        
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSignalHeadManager();

        // clear the BlockBossLogic static list
        Enumeration<BlockBossLogic> en = BlockBossLogic.entries();
        ArrayList<SignalHead> heads = new ArrayList<>();
        while (en.hasMoreElements()) {
            heads.add(en.nextElement().getDrivenSignalNamedBean().getBean());
        }
        for (SignalHead head : heads) {  // avoids ConcurrentModificationException
            BlockBossLogic.getStoppedObject(head);
        }
        
        t1 = InstanceManager.turnoutManagerInstance().newTurnout("IT1", "1");
        t2 = InstanceManager.turnoutManagerInstance().newTurnout("IT2", "2");
        t3 = InstanceManager.turnoutManagerInstance().newTurnout("IT3", "3");

        s1 = InstanceManager.sensorManagerInstance().newSensor("IS1", "1");
        s2 = InstanceManager.sensorManagerInstance().newSensor("IS2", "2");
        s3 = InstanceManager.sensorManagerInstance().newSensor("IS3", "3");
        s4 = InstanceManager.sensorManagerInstance().newSensor("IS4", "4");
        s5 = InstanceManager.sensorManagerInstance().newSensor("IS5", "5");
        s6 = InstanceManager.sensorManagerInstance().newSensor("IS6", "6");
        s7 = InstanceManager.sensorManagerInstance().newSensor("IS7", "7");
        s8 = InstanceManager.sensorManagerInstance().newSensor("IS8", "8");
        s9 = InstanceManager.sensorManagerInstance().newSensor("IS9", "9");
        s10 = InstanceManager.sensorManagerInstance().newSensor("IS10", "10");

        h1 = new jmri.implementation.VirtualSignalHead("IH1", "1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h1);
        JUnitUtil.setBeanStateAndWait(h1, SignalHead.RED); // ensure starting point
        
        h2 = new jmri.implementation.VirtualSignalHead("IH2", "2");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h2);
        JUnitUtil.setBeanStateAndWait(h2, SignalHead.RED); // ensure starting point

        h3 = new jmri.implementation.VirtualSignalHead("IH3", "3");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h3);
        JUnitUtil.setBeanStateAndWait(h3, SignalHead.RED); // ensure starting point

        h4 = new jmri.implementation.VirtualSignalHead("IH4", "4");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h4);
        JUnitUtil.setBeanStateAndWait(h4, SignalHead.RED); // ensure starting point
    }

    @AfterEach
    public void tearDown() {
        t1=t2=t3=null;
        s1=s2=s3=s4=s5=s6=s7=s8=s9=s10=null;
        h1=h2=h3=h4=null;
        testThread = null;
        stopLogic();
        // reset InstanceManager
        JUnitUtil.tearDown();
    }
}
