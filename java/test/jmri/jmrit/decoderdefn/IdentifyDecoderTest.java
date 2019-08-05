package jmri.jmrit.decoderdefn;

import jmri.*;
import jmri.managers.DefaultProgrammerManager;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * IdentifyDecoderTest.java.
 * <p>
 * Description: tests for the jmrit.roster.IdentifyDecoder class
 *
 * @author Bob Jacobsen
 */
public class IdentifyDecoderTest {

    static int cvRead = -1;
    private ProgDebugger p;

    /**
     * Test standard decoder without productID.
     */
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
        Assert.assertEquals("found product ID ", -1, i.productID);

    }

    /**
     * Test Harman decoder with productID in CV112 and CV113.
     */
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

    /**
     * Test Tsunami2 decoder with productID in CV253 and CV256.
     */
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

    /**
     * Test Hornby decoder with CV159 = 143, productIDlow in CV159 and
     * productIDhigh in CV153.
     */
    @Test
    public void testIdentifyHornby1() {
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

    /**
     * Test Hornby decoder with CV159 &lt; 143, hence productID in CV159.
     */
    @Test
    public void testIdentifyHornby2() {
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

    /**
     * Test Hornby decoder with CV159 &gt; 143, hence productID in CV159.
     */
    @Test
    public void testIdentifyHornby3() {
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

    /**
     * Test Hornby decoder with CV159 not available, hence productID is -1.
     * Test with 5 fails on CV8 to trigger PAGEMODE and not abort.
     */
    @Test
    public void testIdentifyHornby4() { // CV159 not available hence productID is -1
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

        Assert.assertEquals("found mfg ID ", -1, i.mfgID);
        Assert.assertEquals("found model ID ", -1, i.modelID);
        Assert.assertEquals("found product ID ", -1, i.productID);
        Assert.assertEquals("Test isOptionalCv() before start", i.isOptionalCv(), false);
        Assert.assertEquals("Programming mode before start", ProgrammingMode.DIRECTMODE, p.getMode());

        i.start();
        Assert.assertEquals("step 1 reads CV ", 8, cvRead);
        Assert.assertEquals("running after 1 ", true, i.isRunning());
        Assert.assertEquals("Test isOptionalCv() after 1", i.isOptionalCv(), false);

        // simulate 5 failures on CV8 to trigger swap to PAGEMODE, start 7
        i.programmingOpReply(21, 2);
        i.programmingOpReply(31, 2);
        i.programmingOpReply(41, 2);
        i.programmingOpReply(51, 2);
        i.programmingOpReply(61, 2);
        i.programmingOpReply(48, 0);
        Assert.assertEquals("step 2 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 2 ", true, i.isRunning());
        Assert.assertEquals("Test isOptionalCv() after 2", i.isOptionalCv(), false);
        Assert.assertEquals("Programming mode after 2", ProgrammingMode.PAGEMODE, p.getMode());

        Assert.assertEquals("found mfg ID ", 48, i.mfgID);
        Assert.assertEquals("found model ID ", -1, i.modelID);
        Assert.assertEquals("found product ID ", -1, i.productID);

        // simulate 2 failures on CV7, start 159
        i.programmingOpReply(22, 2);
        i.programmingOpReply(32, 2);
        i.programmingOpReply(88, 0);
        Assert.assertEquals("step 3 reads CV ", 159, cvRead);
        Assert.assertEquals("running after 3 ", true, i.isRunning());
        Assert.assertEquals("Test isOptionalCv() after 3", i.isOptionalCv(), true);
        Assert.assertEquals("Programming mode after 3", ProgrammingMode.PAGEMODE, p.getMode());

        Assert.assertEquals("found mfg ID ", 48, i.mfgID);
        Assert.assertEquals("found model ID ", 88, i.modelID);
        Assert.assertEquals("found product ID ", -1, i.productID);

        // simulate CV read read fail on CV159, ends
        i.programmingOpReply(145, 2);
        i.programmingOpReply(145, 2);
        i.programmingOpReply(145, 2);
        Assert.assertEquals("running after 4 ", false, i.isRunning());
        Assert.assertEquals("Test isOptionalCv() after 4", i.isOptionalCv(), true);
        Assert.assertEquals("Programming mode after 4", ProgrammingMode.DIRECTMODE, p.getMode());

        Assert.assertEquals("found mfg ID ", 48, i.mfgID);
        Assert.assertEquals("found model ID ", 88, i.modelID);
        Assert.assertEquals("found product ID ", -1, i.productID);

        jmri.util.JUnitAppender.assertWarnMessage("error 2 readng CV 8, trying Paged mode");
        jmri.util.JUnitAppender.assertWarnMessage("Restoring Direct mode");
        jmri.util.JUnitAppender.assertWarnMessage("CV 159 is optional. Will assume not present...");
    }

    /**
     * Test Hornby decoder with only 2 failures on CV8 but 3 on CV7.
     * Should fail as shouldn't switch to PAGEMODE.
     */
    @Test
    public void testIdentifyHornby5() { // CV159 not available hence productID is -1
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

        Assert.assertEquals("found mfg ID ", -1, i.mfgID);
        Assert.assertEquals("found model ID ", -1, i.modelID);
        Assert.assertEquals("found product ID ", -1, i.productID);
        Assert.assertEquals("Test isOptionalCv() before start", i.isOptionalCv(), false);
        Assert.assertEquals("Programming mode before start", ProgrammingMode.DIRECTMODE, p.getMode());

        i.start();
        Assert.assertEquals("step 1 reads CV ", 8, cvRead);
        Assert.assertEquals("running after 1 ", true, i.isRunning());
        Assert.assertEquals("Test isOptionalCv() after 1", i.isOptionalCv(), false);

        // simulate 2 failures on CV8, start 7
        i.programmingOpReply(21, 2);
        i.programmingOpReply(31, 2);
        i.programmingOpReply(48, 0);
        Assert.assertEquals("step 2 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 2 ", true, i.isRunning());
        Assert.assertEquals("Test isOptionalCv() after 2", i.isOptionalCv(), false);
        Assert.assertEquals("Programming mode after 2", ProgrammingMode.DIRECTMODE, p.getMode());

        Assert.assertEquals("found mfg ID ", 48, i.mfgID);
        Assert.assertEquals("found model ID ", -1, i.modelID);
        Assert.assertEquals("found product ID ", -1, i.productID);

        // simulate 3 failures on CV7, to create fail since not switched to PAGEMODE
        i.programmingOpReply(22, 2);
        i.programmingOpReply(32, 2);
        i.programmingOpReply(42, 2);
        Assert.assertEquals("step 2 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 2 ", false, i.isRunning());
        Assert.assertEquals("Programming mode after 3", ProgrammingMode.DIRECTMODE, p.getMode());

        Assert.assertEquals("found mfg ID ", 48, i.mfgID);
        Assert.assertEquals("found model ID ", -1, i.modelID);
        Assert.assertEquals("found product ID ", -1, i.productID);

        jmri.util.JUnitAppender.assertWarnMessage("Stopping due to error: "
                            + p.decodeErrorCode(2));
    }

    /**
     * Initialize the system.
     */
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        p = new ProgDebugger() {
            @Override
            public void readCV(String CV, jmri.ProgListener p) throws jmri.ProgrammerException {
                cvRead = Integer.parseInt(CV);
            }
        };
        p.setMode(ProgrammingMode.DIRECTMODE);
        DefaultProgrammerManager dpm = new DefaultProgrammerManager(p);
        InstanceManager.store(dpm, AddressedProgrammerManager.class);
        InstanceManager.store(dpm, GlobalProgrammerManager.class);
    }

    /**
     * Tear down the system.
     */
    @After
    public void tearDown() {
        p = null;
        JUnitUtil.tearDown();
    }
}
