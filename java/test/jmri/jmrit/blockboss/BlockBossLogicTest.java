package jmri.jmrit.blockboss;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the BlockBossLogic class
 *
 * @author	Bob Jacobsen
 */
public class BlockBossLogicTest extends TestCase {

    protected void startLogic(BlockBossLogic b) {
        p.start();
    }

    protected void stopLogic() {
        if (p!=null) {
            p.stop();
            p=null;
        }
    }

    BlockBossLogic p;
    void setupSimpleBlock() {
        p = new BlockBossLogic("IH1");
        p.setMode(BlockBossLogic.SINGLEBLOCK);
        p.setWatchedSignal1("IH2", false);
    }
    
    // test creation
    public void testCreate() {
        BlockBossLogic p = new BlockBossLogic("IH2");
        Assert.assertEquals("driven signal name", "IH2", p.getDrivenSignal());
    }

    // test simplest block, just signal following
    public void testSimpleBlock() {
        setupSimpleBlock();
        startLogic(p);
        Assert.assertEquals("driven signal name", "IH1", p.getDrivenSignal());
        
        JUnitUtil.setBeanStateAndWait(h2, SignalHead.YELLOW);
        JUnitUtil.waitFor(()->{return SignalHead.GREEN == h1.getAppearance();}, "Stuck at "+h1.getAppearance()+" so yellow sets green");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.RED);
        JUnitUtil.waitFor(()->{return SignalHead.YELLOW == h1.getAppearance();}, "Stuck at "+h1.getAppearance()+" so red sets yellow");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.GREEN);
        JUnitUtil.waitFor(()->{return SignalHead.GREEN == h1.getAppearance();}, "Stuck at "+h1.getAppearance()+" so green sets green");  // wait and test
    }

    // test that initial conditions are set right
    public void testSimpleBlockInitial() {
        setupSimpleBlock();
        startLogic(p);

        JUnitUtil.waitFor(()->{return SignalHead.YELLOW == h1.getAppearance();}, "initial red sets yellow");  // wait and test
    }

    // occupancy check
    public void testSimpleBlockOccupancy() throws jmri.JmriException {
        setupSimpleBlock();
        p.setSensor1("IS1");
        startLogic(p);
        JUnitUtil.setBeanState(s1, Sensor.INACTIVE);
        
        JUnitUtil.setBeanStateAndWait(h2, SignalHead.YELLOW);
        JUnitUtil.waitFor(()->{return SignalHead.GREEN == h1.getAppearance();}, "Stuck at "+h1.getAppearance()+" so yellow sets green");  // wait and test

        JUnitUtil.setBeanState(s1, Sensor.ACTIVE);
        JUnitUtil.waitFor(()->{return SignalHead.RED == h1.getAppearance();}, "Stuck at "+h1.getAppearance()+" so occupied sets red");  // wait and test

        JUnitUtil.setBeanState(s1, Sensor.INACTIVE);
        JUnitUtil.waitFor(()->{return SignalHead.GREEN == h1.getAppearance();}, "Stuck at "+h1.getAppearance()+" so unoccupied sets green");  // wait and test
    }

    // test signal following in distant simple block
    public void testSimpleBlockDistant() {
        setupSimpleBlock();
        p.setDistantSignal(true);
        startLogic(p);

        Assert.assertEquals("driven signal name", "IH1", p.getDrivenSignal());

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.YELLOW);
        JUnitUtil.waitFor(()->{return SignalHead.YELLOW == h1.getAppearance();}, "yellow sets yellow");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.RED);
        JUnitUtil.waitFor(()->{return SignalHead.RED == h1.getAppearance();}, "red sets red");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.GREEN);
        JUnitUtil.waitFor(()->{return SignalHead.GREEN == h1.getAppearance();}, "green sets green");  // wait and test
    }

    // test signal following in limited simple block
    // (not particularly interesting, as next signal can't set red)
    public void testSimpleBlockLimited() {
        setupSimpleBlock();
        p.setLimitSpeed1(true);
        startLogic(p);

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.RED);
        JUnitUtil.waitFor(()->{return SignalHead.YELLOW == h1.getAppearance();}, "red sets yellow");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.YELLOW);
        JUnitUtil.waitFor(()->{return SignalHead.YELLOW == h1.getAppearance();}, "yellow sets yellow");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.GREEN);
        JUnitUtil.waitFor(()->{return SignalHead.YELLOW == h1.getAppearance();}, "green sets yellow");  // wait and test
    }

    // test signal following in distant, limited simple block
    public void testSimpleBlockDistantLimited() {
        setupSimpleBlock();
        p.setDistantSignal(true);
        p.setLimitSpeed1(true);
        startLogic(p);

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.YELLOW);
        JUnitUtil.waitFor(()->{return SignalHead.YELLOW == h1.getAppearance();}, "yellow sets yellow");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.RED);
        JUnitUtil.waitFor(()->{return SignalHead.RED == h1.getAppearance();}, "red sets red");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.GREEN);
        JUnitUtil.waitFor(()->{return SignalHead.YELLOW == h1.getAppearance();}, "green sets yellow");  // wait and test
    }

    // test signal following in restricting simple block
    public void testSimpleBlockRestricting() throws jmri.JmriException {
        JUnitUtil.setBeanState(s1, Sensor.INACTIVE);

        setupSimpleBlock();
        p.setSensor1("IS1");
        p.setRestrictingSpeed1(true);
        startLogic(p);
        
        JUnitUtil.setBeanStateAndWait(h2, SignalHead.YELLOW);
        JUnitUtil.waitFor(()->{return SignalHead.FLASHRED == h1.getAppearance();}, "yellow sets flashing red");  // wait and test

        JUnitUtil.setBeanState(s1, Sensor.ACTIVE);
        JUnitUtil.waitFor(()->{return SignalHead.RED == h1.getAppearance();}, "Stuck at "+h1.getAppearance()+" so occupied sets red");  // wait and test

        JUnitUtil.setBeanStateAndWait(h2, SignalHead.GREEN);
        JUnitUtil.setBeanState(s1, Sensor.INACTIVE);
        JUnitUtil.waitFor(()->{return SignalHead.FLASHRED == h1.getAppearance();}, "Stuck at "+h1.getAppearance()+" so unoccupied green sets flashing red");  // wait and test
    }

    // if no next signal, next signal considered green
    public void testSimpleBlockNoNext() throws jmri.JmriException {
        s1.setState(Sensor.INACTIVE);
        
        p = new BlockBossLogic("IH1");
        p.setSensor1("1");
        p.setMode(BlockBossLogic.SINGLEBLOCK);
        startLogic(p);

        JUnitUtil.waitFor(()->{return SignalHead.GREEN == h1.getAppearance();}, "missing signal is green");  // wait and test
    }

    // if no next signal, next signal is considered green
    public void testSimpleBlockNoNextLimited() throws jmri.JmriException {
        s1.setState(Sensor.INACTIVE);
        
        p = new BlockBossLogic("IH1");
        p.setMode(BlockBossLogic.SINGLEBLOCK);
        p.setSensor1("1");
        p.setLimitSpeed1(true);

        startLogic(p);

        JUnitUtil.waitFor(()->{return SignalHead.YELLOW == h1.getAppearance();}, "missing signal is green, show yellow");  // wait and test
    }

    // check for basic not-fail if no signal name was set
    public void testSimpleBlockNoSignal() throws jmri.JmriException {

        try { 
            p = new BlockBossLogic(null);
        } catch (java.lang.IllegalArgumentException e) {
            // this is expected
        }
        jmri.util.JUnitAppender.assertWarnMessage("Signal Head \"null\" was not found");
    }

    Thread testThread = null;
    boolean forceInterrupt = false;

    // test interruption
    public void testInterrupt() throws jmri.JmriException {
        s1.setState(Sensor.INACTIVE);
        
        forceInterrupt = false;
        p = new BlockBossLogic("IH1") {
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

        startLogic(p);

        JUnitUtil.waitFor(()->{return p.isRunning();}, "is running");
                
        forceInterrupt = true;
        s1.setState(Sensor.ACTIVE);
        
        JUnitUtil.waitFor(()->{return !p.isRunning();}, "is stopped");
    }


    // check that user names were preserved
    public void testUserNamesRetained() {
        BlockBossLogic p = new BlockBossLogic("IH1");

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

        Assert.assertEquals("sensor1", "1", p.getSensor1());
        Assert.assertEquals("sensor2", "2", p.getSensor2());
        Assert.assertEquals("sensor3", "3", p.getSensor3());
        Assert.assertEquals("sensor4", "4", p.getSensor4());
        Assert.assertEquals("sensor5", "10", p.getSensor5());

        Assert.assertEquals("turnout1", "1", p.getTurnout());

        Assert.assertEquals("watchedsignal1", "1", p.getWatchedSignal1());
        Assert.assertEquals("watchedsignal1alt", "2", p.getWatchedSignal1Alt());
        Assert.assertEquals("watchedsignal2", "3", p.getWatchedSignal2());
        Assert.assertEquals("watchedsignal2alt", "4", p.getWatchedSignal2Alt());

        Assert.assertEquals("watchedsensor1", "5", p.getWatchedSensor1());
        Assert.assertEquals("watchedsensor1alt", "6", p.getWatchedSensor1Alt());
        Assert.assertEquals("watchedsensor2", "7", p.getWatchedSensor2());
        Assert.assertEquals("watchedsensor2alt", "8", p.getWatchedSensor2Alt());

        Assert.assertEquals("approach", "9", p.getApproachSensor1());

    }

    // check that system names were preserved
    public void testSystemNamesRetained() {
        BlockBossLogic p = new BlockBossLogic("IH1");

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

        Assert.assertEquals("sensor1", "IS1", p.getSensor1());
        Assert.assertEquals("sensor2", "IS2", p.getSensor2());
        Assert.assertEquals("sensor3", "IS3", p.getSensor3());
        Assert.assertEquals("sensor4", "IS4", p.getSensor4());
        Assert.assertEquals("sensor5", "IS10", p.getSensor5());

        Assert.assertEquals("turnout1", "IT1", p.getTurnout());

        Assert.assertEquals("watchedsignal1", "IH1", p.getWatchedSignal1());
        Assert.assertEquals("watchedsignal1alt", "IH2", p.getWatchedSignal1Alt());
        Assert.assertEquals("watchedsignal2", "IH3", p.getWatchedSignal2());
        Assert.assertEquals("watchedsignal2alt", "IH4", p.getWatchedSignal2Alt());

        Assert.assertEquals("watchedsensor1", "IS5", p.getWatchedSensor1());
        Assert.assertEquals("watchedsensor1alt", "IS6", p.getWatchedSensor1Alt());
        Assert.assertEquals("watchedsensor2", "IS7", p.getWatchedSensor2());
        Assert.assertEquals("watchedsensor2alt", "IS8", p.getWatchedSensor2Alt());

        Assert.assertEquals("approach", "IS9", p.getApproachSensor1());

    }

    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    Turnout t1, t2, t3;
    Sensor s1, s2, s3, s4, s5, s6, s7, s8, s9, s10;
    SignalHead h1, h2, h3, h4;

    /**
     * Test-by test initialization. Does log4j for standalone use, and then
     * creates a set of turnouts, sensors and signals as common background for
     * testing
     */
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();

        // reset InstanceManager
        JUnitUtil.resetInstanceManager();
        
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSignalHeadManager();

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

    public BlockBossLogicTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.Log4JFixture.initLogging();
        String[] testCaseName = {"-noloading", BlockBossLogicTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(BlockBossLogicTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() {
        stopLogic();
        // reset InstanceManager
        JUnitUtil.tearDown();
    }
}
