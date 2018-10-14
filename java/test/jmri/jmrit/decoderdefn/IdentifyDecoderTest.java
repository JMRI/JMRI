package jmri.jmrit.decoderdefn;

import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.managers.DefaultProgrammerManager;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * IdentifyDecoderTest.java
 * <p>
 * Description:	tests for the jmrit.roster.IdentifyDecoder class
 *
 * @author	Bob Jacobsen
 */
public class IdentifyDecoderTest {

    static int cvRead = -1;
    private ProgDebugger p;

    @Test
    public void testIdentifyStandard() {
        // create our test object
        IdentifyDecoder i = new IdentifyDecoder(p) {
            @Override
            public void done(int mfgID, int modelID, int productID) {
            }

            @Override
            public void message(String m) {
            }

            @Override
            public void error() {
            }
        };

        i.start();
        Assert.assertEquals("step 1 reads CV ", 8, cvRead);
        Assert.assertEquals("running after 1 ", true, i.isRunning());

        // simulate CV read complete
        i.programmingOpReply(0x12, 0);
        Assert.assertEquals("step 2 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 2 ", true, i.isRunning());

        // simulate CV read complete, ending check
        i.programmingOpReply(123, 0);
        Assert.assertEquals("running after 2 ", false, i.isRunning());
        Assert.assertEquals("found mfg ID ", 0x12, i.mfgID);
        Assert.assertEquals("found model ID ", 123, i.modelID);

    }

    @Test
    public void testIdentifyHarman() {
        // create our test object
        IdentifyDecoder i = new IdentifyDecoder(p) {
            @Override
            public void done(int mfgID, int modelID, int productID) {
            }

            @Override
            public void message(String m) {
            }

            @Override
            public void error() {
            }
        };

        i.start();
        Assert.assertEquals("step 1 reads CV ", 8, cvRead);
        Assert.assertEquals("running after 1 ", true, i.isRunning());

        // simulate CV read complete on CV8, start 7
        i.programmingOpReply(98, 0);
        Assert.assertEquals("step 2 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 2 ", true, i.isRunning());

        // simulate CV read complete on CV7, start 112
        i.programmingOpReply(123, 0);
        Assert.assertEquals("step 3 reads CV ", 112, cvRead);
        Assert.assertEquals("running after 3 ", true, i.isRunning());

        // simulate CV read complete on CV7, does 113 and ends
        i.programmingOpReply(0xAB, 0);
        Assert.assertEquals("step 4 reads CV ", 113, cvRead);
        Assert.assertEquals("running after 4 ", true, i.isRunning());

        // simulate CV read complete on CV113, ends
        i.programmingOpReply(0xCD, 0);
        Assert.assertEquals("running after 5 ", false, i.isRunning());

        Assert.assertEquals("found mfg ID ", 98, i.mfgID);
        Assert.assertEquals("found model ID ", 123, i.modelID);
        Assert.assertEquals("found product ID ", 0xABCD, i.productID);

    }

    @Test
    public void testIdentifyTsu2() {
        // create our test object
        IdentifyDecoder i = new IdentifyDecoder(p) {
            @Override
            public void done(int mfgID, int modelID, int productID) {
            }

            @Override
            public void message(String m) {
            }

            @Override
            public void error() {
            }
        };

        i.start();
        Assert.assertEquals("step 1 reads CV ", 8, cvRead);
        Assert.assertEquals("running after 1 ", true, i.isRunning());

        // simulate CV read complete on CV8, start 7
        i.programmingOpReply(141, 0);
        Assert.assertEquals("step 2 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 2 ", true, i.isRunning());

        // simulate CV read complete on CV7, start 253
        i.programmingOpReply(71, 0);
        Assert.assertEquals("step 3 reads CV ", 253, cvRead);
        Assert.assertEquals("running after 3 ", true, i.isRunning());

        // simulate CV read complete on CV253, does 256 and ends
        i.programmingOpReply(1, 0);
        Assert.assertEquals("step 4 reads CV ", 256, cvRead);
        Assert.assertEquals("running after 4 ", true, i.isRunning());

        // simulate CV read complete on CV256, ends
        i.programmingOpReply(29, 0);
        Assert.assertEquals("running after 5 ", false, i.isRunning());

        Assert.assertEquals("found mfg ID ", 141, i.mfgID);
        Assert.assertEquals("found model ID ", 71, i.modelID);
        Assert.assertEquals("found product ID ", 285, i.productID);

    }

    @Test
    public void testIdentifyHornby1() { // CV159 == 143, hence productIDlow in CV159 and productIDhigh in CV153
        // create our test object
        IdentifyDecoder i = new IdentifyDecoder(p) {
            @Override
            public void done(int mfgID, int modelID, int productID) {
            }

            @Override
            public void message(String m) {
            }

            @Override
            public void error() {
            }
        };

        i.start();
        Assert.assertEquals("step 1 reads CV ", 8, cvRead);
        Assert.assertEquals("running after 1 ", true, i.isRunning());

        // simulate CV read complete on CV8, start 7
        i.programmingOpReply(48, 0);
        Assert.assertEquals("step 2 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 2 ", true, i.isRunning());

        // simulate CV read complete on CV7, start 159
        i.programmingOpReply(4, 0);
        Assert.assertEquals("step 3 reads CV ", 159, cvRead);
        Assert.assertEquals("running after 3 ", true, i.isRunning());

        // simulate CV read complete on CV159, does 158 and ends
        i.programmingOpReply(143, 0);
        Assert.assertEquals("step 4 reads CV ", 158, cvRead);
        Assert.assertEquals("running after 4 ", true, i.isRunning());

        // simulate CV read complete on 158, ends
        i.programmingOpReply(2, 0);
        Assert.assertEquals("running after 5 ", false, i.isRunning());

        Assert.assertEquals("found mfg ID ", 48, i.mfgID);
        Assert.assertEquals("found model ID ", 4, i.modelID);
        Assert.assertEquals("found product ID ", (2 * 256) + 143, i.productID);
    }

    @Test
    public void testIdentifyHornby2() { // CV159 < 143, hence productID in CV159
        // create our test object
        IdentifyDecoder i = new IdentifyDecoder(p) {
            @Override
            public void done(int mfgID, int modelID, int productID) {
            }

            @Override
            public void message(String m) {
            }

            @Override
            public void error() {
            }
        };

        i.start();
        Assert.assertEquals("step 1 reads CV ", 8, cvRead);
        Assert.assertEquals("running after 1 ", true, i.isRunning());

        // simulate CV read complete on CV8, start 7
        i.programmingOpReply(48, 0);
        Assert.assertEquals("step 2 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 2 ", true, i.isRunning());

        // simulate CV read complete on CV7, start 159
        i.programmingOpReply(77, 0);
        Assert.assertEquals("step 3 reads CV ", 159, cvRead);
        Assert.assertEquals("running after 3 ", true, i.isRunning());

        // simulate CV read complete on CV159, ends
        i.programmingOpReply(142, 0);
        Assert.assertEquals("running after 4 ", false, i.isRunning());

        Assert.assertEquals("found mfg ID ", 48, i.mfgID);
        Assert.assertEquals("found model ID ", 77, i.modelID);
        Assert.assertEquals("found product ID ", 142, i.productID);
    }

    @Test
    public void testIdentifyHornby3() { // CV159 > 143, hence productID in CV159
        // create our test object
        IdentifyDecoder i = new IdentifyDecoder(p) {
            @Override
            public void done(int mfgID, int modelID, int productID) {
            }

            @Override
            public void message(String m) {
            }

            @Override
            public void error() {
            }
        };

        i.start();
        Assert.assertEquals("step 1 reads CV ", 8, cvRead);
        Assert.assertEquals("running after 1 ", true, i.isRunning());

        // simulate CV read complete on CV8, start 7
        i.programmingOpReply(48, 0);
        Assert.assertEquals("step 2 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 2 ", true, i.isRunning());

        // simulate CV read complete on CV7, start 159
        i.programmingOpReply(88, 0);
        Assert.assertEquals("step 3 reads CV ", 159, cvRead);
        Assert.assertEquals("running after 3 ", true, i.isRunning());

        // simulate CV read complete on CV159, ends
        i.programmingOpReply(144, 0);
        Assert.assertEquals("running after 4 ", false, i.isRunning());

        Assert.assertEquals("found mfg ID ", 48, i.mfgID);
        Assert.assertEquals("found model ID ", 88, i.modelID);
        Assert.assertEquals("found product ID ", 144, i.productID);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // initialize the system
        p = new ProgDebugger() {
            @Override
            public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
                cvRead = CV;
            }
        };
        DefaultProgrammerManager dpm = new DefaultProgrammerManager(p);
        InstanceManager.setAddressedProgrammerManager(dpm);
        InstanceManager.store(dpm, GlobalProgrammerManager.class);
    }

    @After
    public void tearDown() {
        p = null;
        JUnitUtil.tearDown();
    }
}
