package jmri.jmrix.dccpp.simulator;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DCCppSimulatorAdapterTest {

    @Test
    public void testCTor() {
        DCCppSimulatorAdapter t = new DCCppSimulatorAdapter();
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

    // private final static Logger log = LoggerFactory.getLogger(DCCppSimulatorAdapterTest.class);

}
