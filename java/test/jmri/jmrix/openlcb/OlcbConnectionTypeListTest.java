package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OlcbConnectionTypeListTest {

    @Test
    public void testCTor() {
        OlcbConnectionTypeList t = new OlcbConnectionTypeList();
        Assertions.assertNotNull( t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(OlcbConnectionTypeListTest.class);

}
