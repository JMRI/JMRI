package jmri.jmrit.logix;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class WarrantTableActionTest {

    private WarrantTableAction wta;

    @Test
    public void testCTor() {
        Assertions.assertNotNull( wta, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        wta = WarrantTableAction.getDefault();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(WarrantTableActionTest.class);

}
