package jmri.jmrix.sprog.sprogslotmon;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class SprogSlotMonStartupActionFactoryTest {

    @Test
    public void testCTor() {
        SprogSlotMonStartupActionFactory t = new SprogSlotMonStartupActionFactory();
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

    // private final static Logger log = LoggerFactory.getLogger(LnTcpStartupActionFactoryTest.class);

}
