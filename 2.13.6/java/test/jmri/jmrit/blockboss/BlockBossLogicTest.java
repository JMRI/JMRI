// BlockBossLogicTest.java

package jmri.jmrit.blockboss;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.util.JUnitUtil;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;

/**
 * Tests for the BlockBossLogic class
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class BlockBossLogicTest extends TestCase {
	
	// test creation
	public void testCreate() {
		BlockBossLogic p = new BlockBossLogic("IH2");
		Assert.assertEquals("driven signal name", "IH2", p.getDrivenSignal());
	}

	// test simplest block, just signal following
	public void testSimpleBlock() {
		BlockBossLogic p = new BlockBossLogic("IH1");
		p.setMode(BlockBossLogic.SINGLEBLOCK);
		p.setWatchedSignal1("IH2", false);
		p.start();
		Assert.assertEquals("driven signal name", "IH1", p.getDrivenSignal());
		
		h2.setAppearance(SignalHead.RED);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("red sets yellow", SignalHead.YELLOW, h1.getAppearance());
		
		h2.setAppearance(SignalHead.YELLOW);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("yellow sets green", SignalHead.GREEN, h1.getAppearance());
		
		h2.setAppearance(SignalHead.GREEN);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("green sets green", SignalHead.GREEN, h1.getAppearance());
		
		p.stop();
	}

	// test signal following in distant simple block
	public void testSimpleBlockDistant() {
		BlockBossLogic p = new BlockBossLogic("IH1");
		p.setMode(BlockBossLogic.SINGLEBLOCK);
		p.setWatchedSignal1("IH2", false);
		p.setDistantSignal(true);
		p.start();
		Assert.assertEquals("driven signal name", "IH1", p.getDrivenSignal());
		
		h2.setAppearance(SignalHead.RED);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("red sets red", SignalHead.RED, h1.getAppearance());
		
		h2.setAppearance(SignalHead.YELLOW);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("yellow sets yellow", SignalHead.YELLOW, h1.getAppearance());
		
		h2.setAppearance(SignalHead.GREEN);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("green sets green", SignalHead.GREEN, h1.getAppearance());
		
		p.stop();
	}

	// test signal following in limited simple block
	// (not particularly interesting, as next signal can't set red)
	public void testSimpleBlockLimited() {
		BlockBossLogic p = new BlockBossLogic("IH1");
		p.setMode(BlockBossLogic.SINGLEBLOCK);
		p.setWatchedSignal1("IH2", false);
		p.setLimitSpeed1(true);
		p.start();
		
		h2.setAppearance(SignalHead.RED);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("red sets yellow", SignalHead.YELLOW, h1.getAppearance());
		
		h2.setAppearance(SignalHead.YELLOW);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("yellow sets yellow", SignalHead.YELLOW, h1.getAppearance());
		
		h2.setAppearance(SignalHead.GREEN);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("green sets yellow", SignalHead.YELLOW, h1.getAppearance());
		
		p.stop();
	}

	// test signal following in distant, limited simple block
	public void testSimpleBlockDistantLimited() {
		BlockBossLogic p = new BlockBossLogic("IH1");
		p.setMode(BlockBossLogic.SINGLEBLOCK);
		p.setWatchedSignal1("IH2", false);
		p.setDistantSignal(true);
		p.setLimitSpeed1(true);
		p.start();
		
		h2.setAppearance(SignalHead.RED);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("red sets red", SignalHead.RED, h1.getAppearance());
		
		h2.setAppearance(SignalHead.YELLOW);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("yellow sets yellow", SignalHead.YELLOW, h1.getAppearance());
		
		h2.setAppearance(SignalHead.GREEN);
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("green sets yellow", SignalHead.YELLOW, h1.getAppearance());
		
		p.stop();
	}

	// if no next signal, it's considered green
	public void testSimpleBlockNoNext() {
		BlockBossLogic p = new BlockBossLogic("IH1");
		p.setMode(BlockBossLogic.SINGLEBLOCK);
		p.start();
		
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("missing signal is green", SignalHead.GREEN, h1.getAppearance());
		p.stop();
	}

	// if no next signal, it's considered green
	public void testSimpleBlockNoNextLimited() {
		BlockBossLogic p = new BlockBossLogic("IH1");
		p.setMode(BlockBossLogic.SINGLEBLOCK);
		p.setLimitSpeed1(true);
		p.start();
		
		JUnitUtil.releaseThread(this);  // release control
		Assert.assertEquals("missing signal is green, show yellow", SignalHead.YELLOW, h1.getAppearance());
		p.stop();
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
    * Test-by test initialization.
    * Does log4j for standalone use, and then
    * creates a set of turnouts, sensors and signals
    * as common background for testing
    */
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        
        // create a new instance manager
        InstanceManager i = new InstanceManager(){
            protected void init() {
                root = null;
                super.init();
                root = this;
            }
        };
        
       	Assert.assertNotNull("Instance exists", i );
        
        // reset InstanceManager
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
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

        h1 = new jmri.implementation.VirtualSignalHead("IH1","1");
        InstanceManager.signalHeadManagerInstance().register(h1);
        h2 = new jmri.implementation.VirtualSignalHead("IH2","2");
        InstanceManager.signalHeadManagerInstance().register(h2);
        h3 = new jmri.implementation.VirtualSignalHead("IH3","3");
        InstanceManager.signalHeadManagerInstance().register(h3);
        h4 = new jmri.implementation.VirtualSignalHead("IH4","4");
        InstanceManager.signalHeadManagerInstance().register(h4);
    }

	public BlockBossLogicTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", BlockBossLogicTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(BlockBossLogicTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
