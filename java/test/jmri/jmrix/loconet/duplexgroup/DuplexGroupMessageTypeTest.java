package jmri.jmrix.loconet.duplexgroup;


import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for jmri.jmrix.loconet.duplexgroup.DuplexGropuMessageType
 * 
 * @author B. Milhaupt Copyright (C) 2018
 */
public class DuplexGroupMessageTypeTest {

    /**
     * Test of values method, of class DuplexGroupMessageType.
     */
    @Test
    public void testValues() {
        DuplexGroupMessageType[] expResult = {
            DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
            DuplexGroupMessageType.DUPLEX_GROUP_NAME_ETC_REPORT_MESSAGE,
            DuplexGroupMessageType.DUPLEX_GROUP_NAME_QUERY_MESSAGE,
            DuplexGroupMessageType.DUPLEX_GROUP_NAME_WRITE_MESSAGE,
            DuplexGroupMessageType.DUPLEX_GROUP_PASSWORD_REPORT_MESSAGE,
            DuplexGroupMessageType.DUPLEX_GROUP_PASSWORD_QUERY_MESSAGE,
            DuplexGroupMessageType.DUPLEX_GROUP_PASSWORD_WRITE_MESSAGE,
            DuplexGroupMessageType.DUPLEX_GROUP_CHANNEL_REPORT_MESSAGE,
            DuplexGroupMessageType.DUPLEX_GROUP_CHANNEL_QUERY_MESSAGE,
            DuplexGroupMessageType.DUPLEX_GROUP_CHANNEL_WRITE_MESSAGE,
            DuplexGroupMessageType.DUPLEX_GROUP_ID_REPORT_MESSAGE,
            DuplexGroupMessageType.DUPLEX_GROUP_ID_QUERY_MESSAGE,
            DuplexGroupMessageType.DUPLEX_GROUP_ID_WRITE_MESSAGE
        };
        DuplexGroupMessageType[] result = DuplexGroupMessageType.values();
        Assertions.assertArrayEquals(expResult, result);
        Assertions.assertEquals( 13, DuplexGroupMessageType.values().length,
            "Correct number of entries");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
