package jmri.jmrit;

import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the AbstractIdentify class. Since that's an abstract base class, we
 * define a local subclass here for the tests.
 *
 * @author Bob Jacobsen Copyright 2001
 */
public class AbstractIdentifyTest {

    @Test
    public void testFullSequence() {
        // walk through all 8 steps
        AITest a = new AITest(new jmri.ProgrammerScaffold(ProgrammingMode.DIRECTMODE));

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

    @Test
    public void testShortSequence() {
        // walk through just 4 steps
        AITest a = new AITest(new jmri.ProgrammerScaffold(ProgrammingMode.DIRECTMODE));

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

    @Test
    public void testOptionalCv() {
        // walk through just 4 steps
        AITest a = new AITest(new jmri.ProgrammerScaffold(ProgrammingMode.DIRECTMODE));

        a.setOptionalCv(true);
        Assert.assertEquals("Test setOptionalCv(true)", a.isOptionalCv(), true);
        a.setOptionalCv(false);
        Assert.assertEquals("Test setOptionalCv(true)", a.isOptionalCv(), false);
        a.setOptionalCv(true);
        Assert.assertEquals("Test setOptionalCv(true)", a.isOptionalCv(), true);

    }

    // internal class for testing
    class AITest extends AbstractIdentify {

        public AITest(Programmer p) {
            super(p);
        }

        @Override
        public boolean test1() {
            invoked = 1;
            return retval;
        }

        @Override
        public boolean test2(int value) {
            invoked = 2;
            ivalue = value;
            return retval;
        }

        @Override
        public boolean test3(int value) {
            invoked = 3;
            ivalue = value;
            return retval;
        }

        @Override
        public boolean test4(int value) {
            invoked = 4;
            ivalue = value;
            return retval;
        }

        @Override
        public boolean test5(int value) {
            invoked = 5;
            ivalue = value;
            return retval;
        }

        @Override
        public boolean test6(int value) {
            invoked = 6;
            ivalue = value;
            return retval;
        }

        @Override
        public boolean test7(int value) {
            invoked = 7;
            ivalue = value;
            return retval;
        }

        @Override
        public boolean test8(int value) {
            invoked = 8;
            ivalue = value;
            return retval;
        }

        @Override
        public boolean test9(int value) {
            invoked = 8;
            ivalue = value;
            return retval;
        }

        @Override
        protected void statusUpdate(String s) {
        }

        @Override
        public void error() {
        }

    }

    public static int invoked = -1;
    public static int ivalue = -1;
    public static boolean retval = false;

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AbstractIdentifyTest.class);
}
