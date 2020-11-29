package jmri.jmrix.loconet.locoio;

import jmri.util.JUnitUtil;

import org.junit.Assert;
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
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void test() {
        new LocoIOModeList() {  // just have to create it to test it via initializer
            {
                /*
                 * This used to be in main class file, so we run
                 * it as an initializer
                 */
                log.debug("Starting test sequence"); // NOI18N
                for (int i = 0; i <= modeList.size() - 1; i++) {
                    LocoIOMode m = modeList.elementAt(i);

                    int hadError = 0;
                    for (i = 1; i <= 2047; i++) {
                        int svA = m.getSV();
                        int v1A = addressToValue1(m, i);
                        int v2A = addressToValue2(m, i);

                        log.debug("{}=> Address {} encodes into {} {} {} {}", m.getFullMode(), Integer.toHexString(i), LnConstants.OPC_NAME(m.getOpCode()), Integer.toHexString(svA), Integer.toHexString(v1A), Integer.toHexString(v2A));

                        LocoIOMode lim = getLocoIOModeFor(svA, v1A, v2A);
                        if (lim == null) {
                            if (hadError == 0) {
                                log.error("Testing {}      ERROR:", m.getFullMode()); // NOI18N
                            }
                            String err
                                    = "    Could Not find mode for Packet: " // NOI18N
                                    + Integer.toHexString(svA) + " "
                                    + Integer.toHexString(v1A) + " "
                                    + Integer.toHexString(v2A) + " <CHK>\n"; // NOI18N
                            log.error(err);
                            hadError++;
                        } else {
                            int decodedaddress = valuesToAddress(lim.getOpCode(), svA, v1A, v2A);
                            if ((i) != decodedaddress) {
                                if (hadError == 0) {
                                    log.error("Testing {}      ERROR:", m.getFullMode()); // NOI18N
                                }
                                String err
                                        = "    Could Not Match Address: (" // NOI18N
                                        + Integer.toHexString(i - 1) + "=>" // NOI18N
                                        + Integer.toHexString(decodedaddress) + ") from " // NOI18N
                                        + LnConstants.OPC_NAME(lim.getOpCode()) + " "
                                        + Integer.toHexString(svA) + " "
                                        + Integer.toHexString(v1A) + " "
                                        + Integer.toHexString(v2A) + "[mask=" + Integer.toHexString(lim.getV2()) + "]\n"; // NOI18N
                                log.error(err);
                                hadError++;
                            }
                        }
                    }
                    Assert.assertEquals("find 0", 0, hadError);
                }
                log.debug("Finished test sequence\n"); // NOI18N
            }
        };
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LocoIOModeListTest.class);

}
