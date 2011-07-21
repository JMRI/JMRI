// BlockManagerXmlTest.java

package jmri.configurexml;

import jmri.implementation.AbstractSensor;
import jmri.BeanSetting;
import jmri.Block;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Sensor;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for BlockManagerXml.
 * <P>
 * Just tests Elements, not actual files.
 * 
 * @author Bob Jacobsen Copyright 2008
 * @version $Revision$
 */
public class BlockManagerXmlTest extends TestCase {

    public BlockManagerXmlTest(String s) {
        super(s);
    }

    /**
     * This test checks that the store operation runs,
     * but doesn't check the output for correctness.
     */
    public void testStore() throws jmri.JmriException {
	    Block b1 = InstanceManager.blockManagerInstance().createNewBlock("SystemName1","");

	    Block b2 = InstanceManager.blockManagerInstance().createNewBlock("SystemName2","");
	    
        Sensor s2 = new AbstractSensor("IS2"){
            public void requestUpdateFromLayout() {}
        };
        b2.setSensor("IS2");
        s2.setState(Sensor.ACTIVE);
        b2.setValue("b2 contents");
        
        Path p21 = new Path();
        p21.setBlock(b1);
        p21.setFromBlockDirection(Path.RIGHT);
        p21.setToBlockDirection(Path.LEFT);
        p21.addSetting(new BeanSetting(new jmri.implementation.AbstractTurnout("IT1"){
                            public void turnoutPushbuttonLockout(boolean b){}
                            public void forwardCommandChangeToLayout(int i){}
                        }, 
                        jmri.Turnout.THROWN));
        b2.addPath(p21);

        //BlockManagerXml tb = new BlockManagerXml();
        
    }
    // Main entry point
    static public void main(String[] args) {
		String[] testCaseName = {BlockManagerXmlTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(BlockManagerXmlTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BlockManagerXmlTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
