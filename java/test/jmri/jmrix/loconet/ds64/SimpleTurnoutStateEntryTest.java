package jmri.jmrix.loconet.ds64;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SimpleTurnoutStateEntryTest {

    @Test
    public void testCTor() {
        SimpleTurnoutStateEntry t = new SimpleTurnoutStateEntry();
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SimpleTurnoutStateEntryTest.class);

}
