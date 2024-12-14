package jmri.jmrit.logix;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class WarrantShutdownTaskTest {

    @Test
    public void testCTor() {
        WarrantShutdownTask t = new WarrantShutdownTask("test warrant shutdown task");
        Assertions.assertNotNull( t, "exists");
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(WarrantShutdownTaskTest.class);

}
