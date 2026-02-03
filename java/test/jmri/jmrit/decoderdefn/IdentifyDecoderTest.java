package jmri.jmrit.decoderdefn;

import jmri.*;
import jmri.managers.DefaultProgrammerManager;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * IdentifyDecoderTest.java.
 * <p>
 * Test for the jmrit.roster.IdentifyDecoder class
 *
 * @author Bob Jacobsen
 */
public class IdentifyDecoderTest {

    /** 
     * Test enum search routine
     */
    @Test
    public void testEnum() {
        var here = IdentifyDecoder.Manufacturer.DIETZ;
        var found = IdentifyDecoder.Manufacturer.forValue(115);
        Assert.assertEquals("found proper value", here, found);
    }
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
        Assert.assertEquals("found mfg int ID ", 0x12, i.intMfg);
        Assert.assertEquals("found mfg ID ", null, i.mfgID);
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

        Assert.assertEquals("found mfg ID ", 98, i.mfgID.value);
        Assert.assertEquals("found model ID ", 123, i.modelID);
        Assert.assertEquals("found product ID ", 0xABCD, i.productID);

    }

    /**
     * Test Tsunami2 decoder with productID in CV253 , CV255 and CV256.
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
        i.programmingOpReply(2, 0);
        Assert.assertEquals("step 5 reads CV ", 255, cvRead);
        Assert.assertEquals("running after 5 ", true, i.isRunning());

        // simulate CV read complete on CV255, ends
        i.programmingOpReply(3, 0);
        Assert.assertEquals("running after 6 ", false, i.isRunning());

        Assert.assertEquals("found mfg ID ", 141, i.mfgID.value);
        Assert.assertEquals("found model ID ", 71, i.modelID);
        Assert.assertEquals("found product ID ", 0x0B02, i.productID);

    }

    /**
     * Test Blunami decoder with productID in CV253 , CV255 and CV256.
     */
    @Test
    public void testIdentifyBlunami() {
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
        i.programmingOpReply(2, 0);
        Assert.assertEquals("step 5 reads CV ", 255, cvRead);
        Assert.assertEquals("running after 5 ", true, i.isRunning());

        // simulate CV read complete on CV255, ends
        i.programmingOpReply(3, 0);
        Assert.assertEquals("running after 6 ", false, i.isRunning());

        Assert.assertEquals("found mfg ID ", 141, i.mfgID.value);
        Assert.assertEquals("found model ID ", 71, i.modelID);
        Assert.assertEquals("found product ID ", 0x0B02, i.productID);

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

        Assert.assertEquals("found mfg ID ", 48, i.mfgID.value);
        Assert.assertEquals("found model ID ", 4, i.modelID);
        Assert.assertEquals("found product ID ", (2 * 256) + 143, i.productID);
    }

    /**
     * Test Hornby HN7000 decoder with CV7=254, then reads 200/201
     */
    @Test
    public void testIdentifyHornby7000() {
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

        // simulate CV read complete on CV7, start 200
        i.programmingOpReply(254, 0);
        Assert.assertEquals("step 3 reads CV ", 200, cvRead);
        Assert.assertEquals("running after 3 ", true, i.isRunning());

        // simulate CV read complete on CV200, does CV201 and ends
        i.programmingOpReply(1, 0);
        Assert.assertEquals("step 4 reads CV ", 201, cvRead);
        Assert.assertEquals("running after 4 ", true, i.isRunning());

        i.programmingOpReply(2, 0);
        Assert.assertEquals("running after 5 ", false, i.isRunning());

        Assert.assertEquals("found mfg ID ", 48, i.mfgID.value);
        Assert.assertEquals("found model ID ", 254, i.modelID);
        Assert.assertEquals("found product ID ", 1*256+ 2, i.productID);
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

        Assert.assertEquals("found mfg ID ", 48, i.mfgID.value);
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

        Assert.assertEquals("found mfg ID ", 48, i.mfgID.value);
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

        Assert.assertEquals("found mfg ID ", null, i.mfgID);
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

        Assert.assertEquals("found mfg ID ", IdentifyDecoder.Manufacturer.HORNBY, i.mfgID);
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

        Assert.assertEquals("found mfg ID ", 48, i.mfgID.value);
        Assert.assertEquals("found model ID ", 88, i.modelID);
        Assert.assertEquals("found product ID ", -1, i.productID);

        // simulate CV read read fail on CV159, ends
        i.programmingOpReply(145, 2);
        i.programmingOpReply(145, 2);
        i.programmingOpReply(145, 2);
        Assert.assertEquals("running after 4 ", false, i.isRunning());
        Assert.assertEquals("Test isOptionalCv() after 4", i.isOptionalCv(), true);
        Assert.assertEquals("Programming mode after 4", ProgrammingMode.DIRECTMODE, p.getMode());

        Assert.assertEquals("found mfg ID ", 48, i.mfgID.value);
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

        Assert.assertEquals("found mfg ID ", null, i.mfgID);
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

        Assert.assertEquals("found mfg ID ", IdentifyDecoder.Manufacturer.HORNBY, i.mfgID);
        Assert.assertEquals("found model ID ", -1, i.modelID);
        Assert.assertEquals("found product ID ", -1, i.productID);

        // simulate 3 failures on CV7, to create fail since not switched to PAGEMODE
        i.programmingOpReply(22, 2);
        i.programmingOpReply(32, 2);
        i.programmingOpReply(42, 2);
        Assert.assertEquals("step 2 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 2 ", false, i.isRunning());
        Assert.assertEquals("Programming mode after 3", ProgrammingMode.DIRECTMODE, p.getMode());

        Assert.assertEquals("found mfg ID ", 48, i.mfgID.value);
        Assert.assertEquals("found model ID ", -1, i.modelID);
        Assert.assertEquals("found product ID ", -1, i.productID);

        jmri.util.JUnitAppender.assertWarnMessage("Stopping due to error: "
                            + p.decodeErrorCode(2));
    }

    /**
     * Test Dietz decoder with productID in CV128.
     */
    @Test
    public void testIdentifyDietz() {
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

        // simulate CV read complete on CV8 with 115
        i.programmingOpReply(115, 0);
        Assert.assertEquals("step 2 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 2 ", true, i.isRunning());

        // simulate CV read complete on CV7 with 88
        i.programmingOpReply(88, 0);
        Assert.assertEquals("step 3 reads CV ", 128, cvRead);
        Assert.assertEquals("running after 3 ", true, i.isRunning());

        // simulate CV read complete on CV128 with 123
        i.programmingOpReply(123, 0);
        Assert.assertEquals("running after 4 ", false, i.isRunning());

        Assert.assertEquals("found mfg ID ", 115, i.mfgID.value);
        Assert.assertEquals("found model ID ", 88, i.modelID);
        Assert.assertEquals("found product ID ", 123, i.productID);
    }


    /**
     * Test TCS decoder with 4-digit productID.
     * Should pass
     */
    @Test
    public void testIdentifyTCSV5() {
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
        i.programmingOpReply(153, 0);
        Assert.assertEquals("step 2 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 2 ", true, i.isRunning());

        // simulate CV read complete on CV7, start 249
        i.programmingOpReply(5, 0);
        Assert.assertEquals("step 3 reads CV ", 249, cvRead);
        Assert.assertEquals("running after 3 ", true, i.isRunning());

        // simulate CV read complete on CV249, start 248
        i.programmingOpReply(176, 0);
        Assert.assertEquals("step 4 reads CV ", 248, cvRead);
        Assert.assertEquals("running after 4 ", true, i.isRunning());

        // simulate CV read complete on CV248, start 111
        i.programmingOpReply(1, 0);
        Assert.assertEquals("step 5 reads CV 111", 111, cvRead);
        Assert.assertEquals("running after 5 ", true, i.isRunning());

        // simulate CV read complete on CV111, start 110
        i.programmingOpReply(1, 0);
        Assert.assertEquals("step 6 reads CV ", 110, cvRead);
        Assert.assertEquals("running after 6 ", true, i.isRunning());

        // simulate CV read complete on CV110, start end
        i.programmingOpReply(2, 0);

        Assert.assertEquals("found mfg ID ", 153, i.mfgID.value);
        Assert.assertEquals("found model ID ", 5, i.modelID);
        Assert.assertEquals("found product ID ", 33620400, i.productID);
    }

    /**
     * Test TCS decoder with single byte, CV249 productID. Sound decoders pre V5
     * Should pass
     */
    @Test
    public void testIdentifyTCSV4() {
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
        i.programmingOpReply(153, 0);
        Assert.assertEquals("step 2 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 2 ", true, i.isRunning());

        // simulate CV read complete on CV7, start 249
        i.programmingOpReply(4, 0);
        Assert.assertEquals("step 3 reads CV ", 249, cvRead);
        Assert.assertEquals("running after 3 ", true, i.isRunning());

        // simulate CV read complete on CV249, start 248
        i.programmingOpReply(176, 0);
        Assert.assertEquals("step 4 reads CV ", 248, cvRead);
        Assert.assertEquals("running after 4 ", true, i.isRunning());

        // simulate CV read complete on CV248, start 111
        i.programmingOpReply(1, 0);
        Assert.assertEquals("step 5 reads CV 111", 111, cvRead);
        Assert.assertEquals("running after 5 ", true, i.isRunning());

        // simulate CV read complete on CV111, start 110
        i.programmingOpReply(1, 0);
        Assert.assertEquals("step 6 reads CV ", 110, cvRead);
        Assert.assertEquals("running after 6 ", true, i.isRunning());

        // simulate CV read complete on CV110, start end
        i.programmingOpReply(2, 0);

        Assert.assertEquals("found mfg ID ", 153, i.mfgID.value);
        Assert.assertEquals("found model ID ", 4, i.modelID);
        Assert.assertEquals("found product ID ", 176, i.productID);
    }

    /**
     * Test TCS decoder with single byte, CV249 productID. Non-sound decoders only).
     * Should pass
     */
    @Test
    public void testIdentifyTCSMobile() {
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
        i.programmingOpReply(153, 0);
        Assert.assertEquals("step 2 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 2 ", true, i.isRunning());

        // simulate CV read complete on CV7, start 249
        i.programmingOpReply(4, 0);
        Assert.assertEquals("step 3 reads CV ", 249, cvRead);
        Assert.assertEquals("running after 3 ", true, i.isRunning());

        // simulate CV read complete on CV249, end
        i.programmingOpReply(80, 0);

        Assert.assertEquals("found mfg ID ", 153, i.mfgID.value);
        Assert.assertEquals("found model ID ", 4, i.modelID);
        Assert.assertEquals("found product ID ", 80, i.productID);
    }

    /**
     * Test Piko decoder with 3-CV productID.
     * Should pass
     */
    @Test
    public void testIdentifyPiko() {
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
        i.programmingOpReply(168, 0);
        Assert.assertEquals("step 2 reads CV ", 7, cvRead);
        Assert.assertEquals("running after 2 ", true, i.isRunning());

        // simulate CV read complete on CV7, write 31
        i.programmingOpReply(6, 0);
        Assert.assertEquals("step 3 writes CV ", 31, cvWrite);
        Assert.assertEquals("step 3 writes value ", 0, cvValue);
        Assert.assertEquals("running after 3 ", true, i.isRunning());
        
        // simulate CV write complete on CV31, write 32
        i.programmingOpReply(0, 0);
        Assert.assertEquals("step 4 writes CV ", 32, cvWrite);
        Assert.assertEquals("step 4 writes value ", 255, cvValue);
        Assert.assertEquals("running after 4 ", true, i.isRunning());
        
        // simulate CV write complete on CV31, start read 315
        i.programmingOpReply(6, 0);
        Assert.assertEquals("step 5 reads CV ", 315, cvRead);
        Assert.assertEquals("running after 5 ", true, i.isRunning());

        // simulate CV read complete on CV315, start 316
        i.programmingOpReply(5, 0);
        Assert.assertEquals("step 6 reads CV ", 316, cvRead);
        Assert.assertEquals("running after 6 ", true, i.isRunning());

        // simulate CV read complete on CV316, start 317
        i.programmingOpReply(46, 0);
        Assert.assertEquals("step 7 reads CV ", 317, cvRead);
        Assert.assertEquals("running after 7 ", true, i.isRunning());

        // simulate CV read complete on CV317, start end
        i.programmingOpReply(37, 0);

        Assert.assertEquals("found mfg ID ", 168, i.mfgID.value);
        Assert.assertEquals("found model ID ", 6, i.modelID);
        Assert.assertEquals("found product ID ", 54637, i.productID);
    }

    private int cvRead = -1;
    private int cvWrite = -1;
    private int cvValue = -1;
    private ProgDebugger p;


    /**
     * Initialize the system.
     */
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        p = new ProgDebugger() {
            @Override
            public void readCV(String CV, jmri.ProgListener p) throws ProgrammerException {
                cvRead = Integer.parseInt(CV);
                cvWrite = -1;
                cvValue = -1;
            }
            @Override
            public void writeCV(String CV, int val, ProgListener p) throws ProgrammerException {
                System.err.println("wrote");
                cvRead = -1;
                cvWrite = Integer.parseInt(CV);
                cvValue = val;
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
    @AfterEach
    public void tearDown() {
        p = null;
        JUnitUtil.tearDown();
    }
}
