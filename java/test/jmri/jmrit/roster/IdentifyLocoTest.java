package jmri.jmrit.roster;

import jmri.*;
import jmri.managers.DefaultProgrammerManager;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * IdentifyLocoTest.java
 * <p>
 * Test for the jmrit.roster.IdentifyLoco class
 *
 * @author Bob Jacobsen
 */
public class IdentifyLocoTest {

    private int cvRead = -1;
    private ProgDebugger p;

    @Test
    public void testShort() {
        // create our test object
        IdentifyLoco i = new IdentifyLoco(p) {
            @Override
            public void message(String m) {
            }

            @Override
            public void done(int i) {
            }

            @Override
            public void error() {
            }
        };

        i.start();
        Assert.assertEquals("step 1 reads CV ", 29, cvRead);
        Assert.assertEquals("running after 1 ", true, i.isRunning());

        // simulate CV read complete, with long read bit off
        i.programmingOpReply(0x00, 0);
        Assert.assertEquals("step 2 reads CV ", 1, cvRead);
        Assert.assertEquals("running after 2 ", true, i.isRunning());

        // simulate CV read of short address complete
        i.programmingOpReply(123, 0);
        Assert.assertEquals("step 3 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 3 ", true, i.isRunning());

        i.programmingOpReply(7, 0);
        Assert.assertEquals("step 4 reads CV ", 8, cvRead);
        Assert.assertEquals("running after 4 ", true, i.isRunning());

        i.programmingOpReply(8, 0);
        Assert.assertEquals("running after 5 ", false, i.isRunning());
        Assert.assertEquals("found address ", 123, i.address);

    }

    @Test
    public void testLong() {
        // create our test object
        IdentifyLoco i = new IdentifyLoco(p) {
            @Override
            public void message(String m) {
            }

            @Override
            public void done(int i) {
            }

            @Override
            public void error() {
            }
        };

        i.start();
        Assert.assertEquals("step 1 reads CV ", 29, cvRead);
        Assert.assertEquals("running after 1 ", true, i.isRunning());

        // simulate CV read complete, with long read bit on
        i.programmingOpReply(0x20, 0);
        Assert.assertEquals("step 2 reads CV ", 17, cvRead);
        Assert.assertEquals("running after 2 ", true, i.isRunning());

        // simulate read of CV17 complete
        i.programmingOpReply(210, 0);
        Assert.assertEquals("step 3 reads CV ", 18, cvRead);
        Assert.assertEquals("running after 3 ", true, i.isRunning());

        // simulate read of CV18 complete
        i.programmingOpReply(189, 0);
        Assert.assertEquals("step 4 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 4 ", true, i.isRunning());

        i.programmingOpReply(7, 0);
        Assert.assertEquals("step 5 reads CV ", 8, cvRead);
        Assert.assertEquals("running after 5 ", true, i.isRunning());

        i.programmingOpReply(8, 0);
        Assert.assertEquals("running after 6 ", false, i.isRunning());
        Assert.assertEquals("found address ", 4797, i.address);

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // initialize the system
        cvRead = -1;
        p = new ProgDebugger() {
            @Override
            public void readCV(String CV, jmri.ProgListener p) throws jmri.ProgrammerException {
                cvRead = Integer.parseInt(CV);
            }
        };
        DefaultProgrammerManager dpm = new DefaultProgrammerManager(p);
        InstanceManager.store(dpm, AddressedProgrammerManager.class);
        InstanceManager.store(dpm, GlobalProgrammerManager.class);
    }

    @AfterEach
    public void tearDown() {
        p = null;
        JUnitUtil.tearDown();
    }
}
