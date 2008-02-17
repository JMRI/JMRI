// BlockTableActionTest.java

package jmri.jmrit.beantable;

import junit.framework.*;

import javax.swing.JComboBox;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;

/**
 * Tests for the jmri.jmrit.beantable.BlockTableAction class
 * @author	Bob Jacobsen  Copyright 2004, 2007, 2008
 * @version	$Revision: 1.1 $
 */
public class BlockTableActionTest extends TestCase {

    public void testCreate() {
        new BlockTableAction();
    }

    public void testInvoke() {
        new BlockTableAction().actionPerformed(null);
        
        // create a couple blocks, and see if they show
        InstanceManager.blockManagerInstance().createNewBlock("IB1", "block 1");
        
        Block b2 = InstanceManager.blockManagerInstance().createNewBlock("IB2", "block 2");
        b2.setDirection(jmri.Path.EAST);
    }


    // from here down is testing infrastructure

    public BlockTableActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {BlockTableActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(BlockTableActionTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BlockTableActionTest.class.getName());
}
