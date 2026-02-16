package jmri.jmrit.decoderdefn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.*;
import jmri.managers.DefaultProgrammerManager;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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
        assertEquals(here, found, "found proper value");
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
        assertEquals(8, cvRead, "step 1 reads CV ");
        assertTrue(i.isRunning(), "running after 1 ");

        // simulate CV read complete
        i.programmingOpReply(0x12, 0);
        assertEquals(7, cvRead, "step 2 reads CV ");
        assertTrue(i.isRunning(), "running after 2 ");

        // simulate CV read complete, ending check
        i.programmingOpReply(123, 0);
        assertFalse(i.isRunning(), "running after 2 ");
        assertEquals(0x12, i.intMfg, "found mfg int ID ");
        assertNull(i.mfgID, "found mfg ID ");
        assertEquals(123, i.modelID, "found model ID ");
        assertEquals(-1, i.productID, "found product ID ");

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
        assertEquals(8, cvRead, "step 1 reads CV ");
        assertTrue(i.isRunning(), "running after 1 ");

        // simulate CV read complete on CV8, start 7
        i.programmingOpReply(98, 0);
        assertEquals(7, cvRead, "step 2 reads CV ");
        assertTrue(i.isRunning(), "running after 2 ");

        // simulate CV read complete on CV7, start 112
        i.programmingOpReply(123, 0);
        assertEquals(112, cvRead, "step 3 reads CV ");
        assertTrue(i.isRunning(), "running after 3 ");

        // simulate CV read complete on CV7, does 113 and ends
        i.programmingOpReply(0xAB, 0);
        assertEquals(113, cvRead, "step 4 reads CV ");
        assertTrue(i.isRunning(), "running after 4 ");

        // simulate CV read complete on CV113, ends
        i.programmingOpReply(0xCD, 0);
        assertFalse(i.isRunning(), "running after 5 ");

        assertEquals(98, i.mfgID.value, "found mfg ID ");
        assertEquals(123, i.modelID, "found model ID ");
        assertEquals(0xABCD, i.productID, "found product ID ");

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
        assertEquals(8, cvRead, "step 1 reads CV ");
        assertTrue(i.isRunning(), "running after 1 ");

        // simulate CV read complete on CV8, start 7
        i.programmingOpReply(141, 0);
        assertEquals(7, cvRead, "step 2 reads CV ");
        assertTrue(i.isRunning(), "running after 2 ");

        // simulate CV read complete on CV7, start 253
        i.programmingOpReply(71, 0);
        assertEquals(253, cvRead, "step 3 reads CV ");
        assertTrue(i.isRunning(), "running after 3 ");

        // simulate CV read complete on CV253, does 256 and ends
        i.programmingOpReply(1, 0);
        assertEquals(256, cvRead, "step 4 reads CV ");
        assertTrue(i.isRunning(), "running after 4 ");

        // simulate CV read complete on CV256, ends
        i.programmingOpReply(2, 0);
        assertEquals(255, cvRead, "step 5 reads CV ");
        assertTrue(i.isRunning(), "running after 5 ");

        // simulate CV read complete on CV255, ends
        i.programmingOpReply(3, 0);
        assertFalse(i.isRunning(), "running after 6 ");

        assertEquals(141, i.mfgID.value, "found mfg ID ");
        assertEquals(71, i.modelID, "found model ID ");
        assertEquals(0x0B02, i.productID, "found product ID ");

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
        assertEquals(8, cvRead, "step 1 reads CV ");
        assertTrue(i.isRunning(), "running after 1 ");

        // simulate CV read complete on CV8, start 7
        i.programmingOpReply(141, 0);
        assertEquals(7, cvRead, "step 2 reads CV ");
        assertTrue(i.isRunning(), "running after 2 ");

        // simulate CV read complete on CV7, start 253
        i.programmingOpReply(71, 0);
        assertEquals(253, cvRead, "step 3 reads CV ");
        assertTrue(i.isRunning(), "running after 3 ");

        // simulate CV read complete on CV253, does 256 and ends
        i.programmingOpReply(1, 0);
        assertEquals(256, cvRead, "step 4 reads CV ");
        assertTrue(i.isRunning(), "running after 4 ");

        // simulate CV read complete on CV256, ends
        i.programmingOpReply(2, 0);
        assertEquals(255, cvRead, "step 5 reads CV ");
        assertTrue(i.isRunning(), "running after 5 ");

        // simulate CV read complete on CV255, ends
        i.programmingOpReply(3, 0);
        assertFalse(i.isRunning(), "running after 6 ");

        assertEquals(141, i.mfgID.value, "found mfg ID ");
        assertEquals(71, i.modelID, "found model ID ");
        assertEquals(0x0B02, i.productID, "found product ID ");

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
        assertEquals(8, cvRead, "step 1 reads CV ");
        assertTrue(i.isRunning(), "running after 1 ");

        // simulate CV read complete on CV8, start 7
        i.programmingOpReply(48, 0);
        assertEquals(7, cvRead, "step 2 reads CV ");
        assertTrue(i.isRunning(), "running after 2 ");

        // simulate CV read complete on CV7, start 159
        i.programmingOpReply(4, 0);
        assertEquals(159, cvRead, "step 3 reads CV ");
        assertTrue(i.isRunning(), "running after 3 ");

        // simulate CV read complete on CV159, does 158 and ends
        i.programmingOpReply(143, 0);
        assertEquals(158, cvRead, "step 4 reads CV ");
        assertTrue(i.isRunning(), "running after 4 ");

        // simulate CV read complete on 158, ends
        i.programmingOpReply(2, 0);
        assertFalse(i.isRunning(), "running after 5 ");

        assertEquals(48, i.mfgID.value, "found mfg ID ");
        assertEquals(4, i.modelID, "found model ID ");
        assertEquals((2 * 256) + 143, i.productID, "found product ID ");
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
        assertEquals(8, cvRead, "step 1 reads CV ");
        assertTrue(i.isRunning(), "running after 1 ");

        // simulate CV read complete on CV8, start 7
        i.programmingOpReply(48, 0);
        assertEquals(7, cvRead, "step 2 reads CV ");
        assertTrue(i.isRunning(), "running after 2 ");

        // simulate CV read complete on CV7, start 200
        i.programmingOpReply(254, 0);
        assertEquals(200, cvRead, "step 3 reads CV ");
        assertTrue(i.isRunning(), "running after 3 ");

        // simulate CV read complete on CV200, does CV201 and ends
        i.programmingOpReply(1, 0);
        assertEquals(201, cvRead, "step 4 reads CV ");
        assertTrue(i.isRunning(), "running after 4 ");

        i.programmingOpReply(2, 0);
        assertFalse(i.isRunning(), "running after 5 ");

        assertEquals(48, i.mfgID.value, "found mfg ID ");
        assertEquals(254, i.modelID, "found model ID ");
        assertEquals(1*256+ 2, i.productID, "found product ID ");
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
        assertEquals(8, cvRead, "step 1 reads CV ");
        assertTrue(i.isRunning(), "running after 1 ");

        // simulate CV read complete on CV8, start 7
        i.programmingOpReply(48, 0);
        assertEquals(7, cvRead, "step 2 reads CV ");
        assertTrue(i.isRunning(), "running after 2 ");

        // simulate CV read complete on CV7, start 159
        i.programmingOpReply(77, 0);
        assertEquals(159, cvRead, "step 3 reads CV ");
        assertTrue(i.isRunning(), "running after 3 ");

        // simulate CV read complete on CV159, ends
        i.programmingOpReply(142, 0);
        assertFalse(i.isRunning(), "running after 4 ");

        assertEquals(48, i.mfgID.value, "found mfg ID ");
        assertEquals(77, i.modelID, "found model ID ");
        assertEquals(142, i.productID, "found product ID ");
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
        assertEquals(8, cvRead, "step 1 reads CV ");
        assertTrue(i.isRunning(), "running after 1 ");

        // simulate CV read complete on CV8, start 7
        i.programmingOpReply(48, 0);
        assertEquals(7, cvRead, "step 2 reads CV ");
        assertTrue(i.isRunning(), "running after 2 ");

        // simulate CV read complete on CV7, start 159
        i.programmingOpReply(88, 0);
        assertEquals(159, cvRead, "step 3 reads CV ");
        assertTrue(i.isRunning(), "running after 3 ");

        // simulate CV read complete on CV159, ends
        i.programmingOpReply(144, 0);
        assertFalse(i.isRunning(), "running after 4 ");

        assertEquals(48, i.mfgID.value, "found mfg ID ");
        assertEquals(88, i.modelID, "found model ID ");
        assertEquals(144, i.productID, "found product ID ");
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

        assertNull(i.mfgID, "found mfg ID ");
        assertEquals(-1, i.modelID, "found model ID ");
        assertEquals(-1, i.productID, "found product ID ");
        assertFalse(i.isOptionalCv(), "Test isOptionalCv() before start");
        assertEquals(ProgrammingMode.DIRECTMODE, p.getMode(), "Programming mode before start");

        i.start();
        assertEquals(8, cvRead, "step 1 reads CV ");
        assertTrue(i.isRunning(), "running after 1 ");
        assertFalse(i.isOptionalCv(), "Test isOptionalCv() after 1");

        // simulate 5 failures on CV8 to trigger swap to PAGEMODE, start 7
        i.programmingOpReply(21, 2);
        i.programmingOpReply(31, 2);
        i.programmingOpReply(41, 2);
        i.programmingOpReply(51, 2);
        i.programmingOpReply(61, 2);
        i.programmingOpReply(48, 0);
        assertEquals(7, cvRead, "step 2 reads CV ");
        assertTrue(i.isRunning(), "running after 2 ");
        assertFalse(i.isOptionalCv(), "Test isOptionalCv() after 2");
        assertEquals(ProgrammingMode.PAGEMODE, p.getMode(), "Programming mode after 2");

        assertEquals(IdentifyDecoder.Manufacturer.HORNBY, i.mfgID, "found mfg ID ");
        assertEquals(-1, i.modelID, "found model ID ");
        assertEquals(-1, i.productID, "found product ID ");

        // simulate 2 failures on CV7, start 159
        i.programmingOpReply(22, 2);
        i.programmingOpReply(32, 2);
        i.programmingOpReply(88, 0);
        assertEquals(159, cvRead, "step 3 reads CV ");
        assertTrue(i.isRunning(), "running after 3 ");
        assertTrue(i.isOptionalCv(), "Test isOptionalCv() after 3");
        assertEquals(ProgrammingMode.PAGEMODE, p.getMode(), "Programming mode after 3");

        assertEquals(48, i.mfgID.value, "found mfg ID ");
        assertEquals(88, i.modelID, "found model ID ");
        assertEquals(-1, i.productID, "found product ID ");

        // simulate CV read read fail on CV159, ends
        i.programmingOpReply(145, 2);
        i.programmingOpReply(145, 2);
        i.programmingOpReply(145, 2);
        assertFalse(i.isRunning(), "running after 4 ");
        assertTrue(i.isOptionalCv(), "Test isOptionalCv() after 4");
        assertEquals(ProgrammingMode.DIRECTMODE, p.getMode(), "Programming mode after 4");

        assertEquals(48, i.mfgID.value, "found mfg ID ");
        assertEquals(88, i.modelID, "found model ID ");
        assertEquals(-1, i.productID, "found product ID ");

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

        assertNull(i.mfgID, "found mfg ID ");
        assertEquals(-1, i.modelID, "found model ID ");
        assertEquals(-1, i.productID, "found product ID ");
        assertFalse(i.isOptionalCv(), "Test isOptionalCv() before start");
        assertEquals(ProgrammingMode.DIRECTMODE, p.getMode(), "Programming mode before start");

        i.start();
        assertEquals(8, cvRead, "step 1 reads CV ");
        assertTrue(i.isRunning(), "running after 1 ");
        assertFalse(i.isOptionalCv(), "Test isOptionalCv() after 1");

        // simulate 2 failures on CV8, start 7
        i.programmingOpReply(21, 2);
        i.programmingOpReply(31, 2);
        i.programmingOpReply(48, 0);
        assertEquals(7, cvRead, "step 2 reads CV ");
        assertTrue(i.isRunning(), "running after 2 ");
        assertFalse(i.isOptionalCv(), "Test isOptionalCv() after 2");
        assertEquals(ProgrammingMode.DIRECTMODE, p.getMode(), "Programming mode after 2");

        assertEquals(IdentifyDecoder.Manufacturer.HORNBY, i.mfgID, "found mfg ID ");
        assertEquals(-1, i.modelID, "found model ID ");
        assertEquals(-1, i.productID, "found product ID ");

        // simulate 3 failures on CV7, to create fail since not switched to PAGEMODE
        i.programmingOpReply(22, 2);
        i.programmingOpReply(32, 2);
        i.programmingOpReply(42, 2);
        assertEquals(7, cvRead, "step 2 reads CV ");
        assertFalse(i.isRunning(), "running after 2 ");
        assertEquals(ProgrammingMode.DIRECTMODE, p.getMode(), "Programming mode after 3");

        assertEquals(48, i.mfgID.value, "found mfg ID ");
        assertEquals(-1, i.modelID, "found model ID ");
        assertEquals(-1, i.productID, "found product ID ");

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
        assertEquals(8, cvRead, "step 1 reads CV ");
        assertTrue(i.isRunning(), "running after 1 ");

        // simulate CV read complete on CV8 with 115
        i.programmingOpReply(115, 0);
        assertEquals(7, cvRead, "step 2 reads CV ");
        assertTrue(i.isRunning(), "running after 2 ");

        // simulate CV read complete on CV7 with 88
        i.programmingOpReply(88, 0);
        assertEquals(128, cvRead, "step 3 reads CV ");
        assertTrue(i.isRunning(), "running after 3 ");

        // simulate CV read complete on CV128 with 123
        i.programmingOpReply(123, 0);
        assertFalse(i.isRunning(), "running after 4 ");

        assertEquals(115, i.mfgID.value, "found mfg ID ");
        assertEquals(88, i.modelID, "found model ID ");
        assertEquals(123, i.productID, "found product ID ");
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
        assertEquals(8, cvRead, "step 1 reads CV ");
        assertTrue(i.isRunning(), "running after 1 ");

        // simulate CV read complete on CV8, start 7
        i.programmingOpReply(153, 0);
        assertEquals(7, cvRead, "step 2 reads CV ");
        assertTrue(i.isRunning(), "running after 2 ");

        // simulate CV read complete on CV7, start 249
        i.programmingOpReply(5, 0);
        assertEquals(249, cvRead, "step 3 reads CV ");
        assertTrue(i.isRunning(), "running after 3 ");

        // simulate CV read complete on CV249, start 248
        i.programmingOpReply(176, 0);
        assertEquals(248, cvRead, "step 4 reads CV ");
        assertTrue(i.isRunning(), "running after 4 ");

        // simulate CV read complete on CV248, start 111
        i.programmingOpReply(1, 0);
        assertEquals(111, cvRead, "step 5 reads CV 111");
        assertTrue(i.isRunning(), "running after 5 ");

        // simulate CV read complete on CV111, start 110
        i.programmingOpReply(1, 0);
        assertEquals(110, cvRead, "step 6 reads CV ");
        assertTrue(i.isRunning(), "running after 6 ");

        // simulate CV read complete on CV110, start end
        i.programmingOpReply(2, 0);

        assertEquals(153, i.mfgID.value, "found mfg ID ");
        assertEquals(5, i.modelID, "found model ID ");
        assertEquals(33620400, i.productID, "found product ID ");
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
        assertEquals(8, cvRead, "step 1 reads CV ");
        assertTrue(i.isRunning(), "running after 1 ");

        // simulate CV read complete on CV8, start 7
        i.programmingOpReply(153, 0);
        assertEquals(7, cvRead, "step 2 reads CV ");
        assertTrue(i.isRunning(), "running after 2 ");

        // simulate CV read complete on CV7, start 249
        i.programmingOpReply(4, 0);
        assertEquals(249, cvRead, "step 3 reads CV ");
        assertTrue(i.isRunning(), "running after 3 ");

        // simulate CV read complete on CV249, start 248
        i.programmingOpReply(176, 0);
        assertEquals(248, cvRead, "step 4 reads CV ");
        assertTrue(i.isRunning(), "running after 4 ");

        // simulate CV read complete on CV248, start 111
        i.programmingOpReply(1, 0);
        assertEquals(111, cvRead, "step 5 reads CV 111");
        assertTrue(i.isRunning(), "running after 5 ");

        // simulate CV read complete on CV111, start 110
        i.programmingOpReply(1, 0);
        assertEquals(110, cvRead, "step 6 reads CV ");
        assertTrue(i.isRunning(), "running after 6 ");

        // simulate CV read complete on CV110, start end
        i.programmingOpReply(2, 0);

        assertEquals(153, i.mfgID.value, "found mfg ID ");
        assertEquals(4, i.modelID, "found model ID ");
        assertEquals(176, i.productID, "found product ID ");
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
        assertEquals(8, cvRead, "step 1 reads CV ");
        assertTrue(i.isRunning(), "running after 1 ");

        // simulate CV read complete on CV8, start 7
        i.programmingOpReply(153, 0);
        assertEquals(7, cvRead, "step 2 reads CV ");
        assertTrue(i.isRunning(), "running after 2 ");

        // simulate CV read complete on CV7, start 249
        i.programmingOpReply(4, 0);
        assertEquals(249, cvRead, "step 3 reads CV ");
        assertTrue(i.isRunning(), "running after 3 ");

        // simulate CV read complete on CV249, end
        i.programmingOpReply(80, 0);

        assertEquals(153, i.mfgID.value, "found mfg ID ");
        assertEquals(4, i.modelID, "found model ID ");
        assertEquals(80, i.productID, "found product ID ");
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
        assertEquals(8, cvRead, "step 1 reads CV ");
        assertTrue(i.isRunning(), "running after 1 ");

        // simulate CV read complete on CV8, start 7
        i.programmingOpReply(162, 0);
        assertEquals(7, cvRead, "step 2 reads CV ");
        assertTrue(i.isRunning(), "running after 2 ");

        // simulate CV read complete on CV7, write 31
        i.programmingOpReply(6, 0);
        assertEquals(31, cvWrite, "step 3 writes CV ");
        assertEquals(0, cvValue, "step 3 writes value ");
        assertTrue(i.isRunning(), "running after 3 ");
        
        // simulate CV write complete on CV31, write 32
        i.programmingOpReply(0, 0);
        assertEquals(32, cvWrite, "step 4 writes CV ");
        assertEquals(255, cvValue, "step 4 writes value ");
        assertTrue(i.isRunning(), "running after 4 ");
        
        // simulate CV write complete on CV31, start read 315
        i.programmingOpReply(6, 0);
        assertEquals(315, cvRead, "step 5 reads CV ");
        assertTrue(i.isRunning(), "running after 5");

        // simulate CV read complete on CV315, start 316
        i.programmingOpReply(5, 0);
        assertEquals(316, cvRead, "step 6 reads CV ");
        assertTrue(i.isRunning(), "running after 6 ");

        // simulate CV read complete on CV316, start 317
        i.programmingOpReply(46, 0);
        assertEquals(317, cvRead, "step 7 reads CV ");
        assertTrue(i.isRunning(), "running after 7 ");

        // simulate CV read complete on CV317, start end
        i.programmingOpReply(37, 0);

        assertEquals(162, i.mfgID.value, "found mfg ID ");
        assertEquals(6, i.modelID, "found model ID ");
        assertEquals(54637, i.productID, "found product ID ");
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
