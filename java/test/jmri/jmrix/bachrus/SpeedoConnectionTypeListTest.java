package jmri.jmrix.bachrus;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SpeedoConnectionTypeListTest {

    @Test
    public void testCTor() {
        SpeedoConnectionTypeList t = new SpeedoConnectionTypeList();
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

    // private final static Logger log = LoggerFactory.getLogger(SpeedoConnectionTypeListTest.class);

}
