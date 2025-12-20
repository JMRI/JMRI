package jmri.jmrix.pricom.downloader;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the PdiFile class
 *
 * @author Bob Jacobsen Copyright 2005
 */
public class PdiFileTest {

    @Test
    public void testPdiFileCTor() {
        Assertions.assertNotNull( new PdiFile(null) );
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
