package jmri.jmrit;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.Programmer;

/**
 * Test the AbstractIdentify class. Since that's an abstract base class, we
 * define a local subclass here for the tests.
 *
 * @author	Bob Jacobsen Copyright 2001
 * @version	$Revision$
 */
public class AbstractIdentifyTest extends TestCase {

    public void testFullSequence() {
        // walk through all 8 steps
        AITest a = new AITest(new jmri.ProgrammerScaffold(jmri.managers.DefaultProgrammerManager.DIRECTMODE));

        retval = false;
        invoked = -1;
        ivalue = -1;

        Assert.assertEquals("before start, running ", false, a.isRunning());

        // start to state 1, invokes test1
        a.start();
        Assert.assertEquals("start invoked ", 1, invoked);
        Assert.assertEquals("at start running ", true, a.isRunning());

        // reply 1, state 1 -> 2, invokes test2
        a.programmingOpReply(12, jmri.ProgListener.OK);
        Assert.assertEquals("reply 1 invoked ", 2, invoked);
        Assert.assertEquals("reply 1 value ", 12, ivalue);
        Assert.assertEquals("reply 1 running ", true, a.isRunning());

        // reply 2, state 2 -> 3, invokes test3
        a.programmingOpReply(13, jmri.ProgListener.OK);
        Assert.assertEquals("reply 2 invoked ", 3, invoked);
        Assert.assertEquals("reply 2 value ", 13, ivalue);
        Assert.assertEquals("reply 2 running ", true, a.isRunning());

        // reply 3, state 3 -> 4, invokes test4
        a.programmingOpReply(14, jmri.ProgListener.OK);
        Assert.assertEquals("reply 3 invoked ", 4, invoked);
        Assert.assertEquals("reply 3 value ", 14, ivalue);
        Assert.assertEquals("reply 3 running ", true, a.isRunning());

        // reply 4, state 4 -> 5, invokes test5
        a.programmingOpReply(15, jmri.ProgListener.OK);
        Assert.assertEquals("reply 4 invoked ", 5, invoked);
        Assert.assertEquals("reply 4 value ", 15, ivalue);
        Assert.assertEquals("reply 4 running ", true, a.isRunning());

        // reply 5, state 5 -> 6, invokes test6
        a.programmingOpReply(16, jmri.ProgListener.OK);
        Assert.assertEquals("reply 5 invoked ", 6, invoked);
        Assert.assertEquals("reply 5 value ", 16, ivalue);
        Assert.assertEquals("reply 5 running ", true, a.isRunning());

        // reply 6, state 6 -> 7, invokes test7
        a.programmingOpReply(17, jmri.ProgListener.OK);
        Assert.assertEquals("reply 6 invoked ", 7, invoked);
        Assert.assertEquals("reply 6 value ", 17, ivalue);
        Assert.assertEquals("reply 6 running ", true, a.isRunning());

        // reply 7, state 7 -> 8, invokes test8
        retval = true;
        a.programmingOpReply(18, jmri.ProgListener.OK);
        Assert.assertEquals("reply 7 invoked ", 8, invoked);
        Assert.assertEquals("reply 7 value ", 18, ivalue);
        Assert.assertEquals("reply 7 running ", false, a.isRunning());

    }

    public void testShortSequence() {
        // walk through just 4 steps
        AITest a = new AITest(new jmri.ProgrammerScaffold(jmri.managers.DefaultProgrammerManager.DIRECTMODE));

        retval = false;
        invoked = -1;
        ivalue = -1;

        Assert.assertEquals("before start, running ", false, a.isRunning());

        // start to state 1, invokes test1
        a.start();
        Assert.assertEquals("start invoked ", 1, invoked);
        Assert.assertEquals("at start running ", true, a.isRunning());

        // reply 1, state 1 -> 2, invokes test2
        a.programmingOpReply(12, jmri.ProgListener.OK);
        Assert.assertEquals("reply 1 invoked ", 2, invoked);
        Assert.assertEquals("reply 1 value ", 12, ivalue);
        Assert.assertEquals("reply 1 running ", true, a.isRunning());

        // reply 2, state 2 -> 3, invokes test3
        a.programmingOpReply(13, jmri.ProgListener.OK);
        Assert.assertEquals("reply 2 invoked ", 3, invoked);
        Assert.assertEquals("reply 2 value ", 13, ivalue);
        Assert.assertEquals("reply 2 running ", true, a.isRunning());

        // reply 3, state 3 -> 4, invokes test4
        a.programmingOpReply(14, jmri.ProgListener.OK);
        Assert.assertEquals("reply 3 invoked ", 4, invoked);
        Assert.assertEquals("reply 3 value ", 14, ivalue);
        Assert.assertEquals("reply 3 running ", true, a.isRunning());

        // reply 4, state 4 -> 5, invokes test5, which ends
        retval = true;
        a.programmingOpReply(15, jmri.ProgListener.OK);
        Assert.assertEquals("reply 4 invoked ", 5, invoked);
        Assert.assertEquals("reply 4 value ", 15, ivalue);
        Assert.assertEquals("reply 4 running ", false, a.isRunning());

    }

    // internal class for testing
    class AITest extends AbstractIdentify {
        public AITest(Programmer p) { super(p);}
        
        public boolean test1() {
            invoked = 1;
            return retval;
        }

        public boolean test2(int value) {
            invoked = 2;
            ivalue = value;
            return retval;
        }

        public boolean test3(int value) {
            invoked = 3;
            ivalue = value;
            return retval;
        }

        public boolean test4(int value) {
            invoked = 4;
            ivalue = value;
            return retval;
        }

        public boolean test5(int value) {
            invoked = 5;
            ivalue = value;
            return retval;
        }

        public boolean test6(int value) {
            invoked = 6;
            ivalue = value;
            return retval;
        }

        public boolean test7(int value) {
            invoked = 7;
            ivalue = value;
            return retval;
        }

        public boolean test8(int value) {
            invoked = 8;
            ivalue = value;
            return retval;
        }

        public boolean test9(int value) {
            invoked = 8;
            ivalue = value;
            return retval;
        }

        protected void statusUpdate(String s) {
        }

        public void error() {
        }

    }

    public static int invoked = -1;
    public static int ivalue = -1;
    public static boolean retval = false;

    // from here down is testing infrastructure
    public AbstractIdentifyTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AbstractIdentifyTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AbstractIdentifyTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

	// static private Logger log = LoggerFactory.getLogger(AbstractIdentifyTest.class.getName());
}
