package jmri.util;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ConnectionNameFromSystemNameTest {

    // no testCtor as tested class only supplies static methods

    @Test
    public void testMethodsExist() {
        Assertions.assertNull(ConnectionNameFromSystemName.getConnectionName("NOTAPREFIX"));
        Assertions.assertNull(ConnectionNameFromSystemName.getPrefixFromName("Not a User Name"));
        Assertions.assertNull(ConnectionNameFromSystemName.getSystemConnectionMemoFromSystemPrefix("NOTAPREFIX"));
        Assertions.assertNull(ConnectionNameFromSystemName.getSystemConnectionMemoFromUserName("Not a User Name"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ConnectionNameFromSystemNameTest.class);

}
