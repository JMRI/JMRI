package jmri.jmrix.loconet.duplexgroup;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for jmri.jmrix.loconet.duplexgroup.DuplexGropuMessageType
 * 
 * @author B. Milhaupt Copyright (C) 2018
 */
public class DuplexGroupMessageTypeTest {

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    public DuplexGroupMessageTypeTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

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
        assertArrayEquals(expResult, result);
        assertEquals("Correct number of entries", 13, DuplexGroupMessageType.values().length);
    }

}
