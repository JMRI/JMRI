package jmri.jmrix.loconet.locoio;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.loconet.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LocoIOModeListTest {

    @Test
    public void testCTor() {
        LocoIOModeList t = new LocoIOModeList();
        Assertions.assertNotNull( t, "exists");
    }

    @Test
    public void test() {
        Assertions.assertNotNull( new LocoIOModeList() {  // just have to create it to test it via initializer
            {
                /*
                 * This used to be in main class file, so we run
                 * it as an initializer
                 */
                log.debug("Starting test sequence"); // NOI18N
                for (int ii = 0; ii <= modeList.size() - 1; ii++) {
                    LocoIOMode m = modeList.elementAt(ii);

                    int hadError = 0;
                    for (int i = 1; i <= 2047; i++) {
                        int svA = m.getSV();
                        int v1A = addressToValue1(m, i);
                        int v2A = addressToValue2(m, i);

                        log.debug("{}=> Address {} encodes into {} {} {} {}", m.getFullMode(), Integer.toHexString(i), LnConstants.OPC_NAME(m.getOpCode()), Integer.toHexString(svA), Integer.toHexString(v1A), Integer.toHexString(v2A));

                        LocoIOMode lim = getLocoIOModeFor(svA, v1A, v2A);
                        if (lim == null) {
                            if (hadError == 0) {
                                log.error("Testing {}      ERROR:", m.getFullMode()); // NOI18N
                            }
                            log.error("    Could Not find mode for Packet: {} {} {} <CHK>\n",
                                Integer.toHexString(svA), Integer.toHexString(v1A), Integer.toHexString(v2A));
                            hadError++;
                        } else {
                            int decodedaddress = valuesToAddress(lim.getOpCode(), svA, v1A, v2A);
                            if ((i) != decodedaddress) {
                                if (hadError == 0) {
                                    log.error("Testing {}      ERROR:", m.getFullMode()); // NOI18N
                                }
                                String err
                                        = " ("
                                        + Integer.toHexString(i - 1) + "=>" // NOI18N
                                        + Integer.toHexString(decodedaddress) + ") from " // NOI18N
                                        + LnConstants.OPC_NAME(lim.getOpCode()) + " "
                                        + Integer.toHexString(svA) + " "
                                        + Integer.toHexString(v1A) + " "
                                        + Integer.toHexString(v2A) + "[mask=" + Integer.toHexString(lim.getV2()) + "]\n"; // NOI18N
                                log.error("    Could Not Match Address: {}", err);
                                hadError++;
                            }
                        }
                    }
                    Assertions.assertEquals( 0, hadError, "find 0");
                }
                log.debug("Finished test sequence\n"); // NOI18N
            }
        });
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LocoIOModeListTest.class);

}
