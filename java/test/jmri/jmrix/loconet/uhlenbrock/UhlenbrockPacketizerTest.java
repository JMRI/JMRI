package jmri.jmrix.loconet.uhlenbrock;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class UhlenbrockPacketizerTest {

    @Test
    public void testCTor() {
        UhlenbrockSystemConnectionMemo memo = new UhlenbrockSystemConnectionMemo();
        UhlenbrockPacketizer t = new UhlenbrockPacketizer(memo);
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

    // private final static Logger log = LoggerFactory.getLogger(UhlenbrockPacketizerTest.class);

}
