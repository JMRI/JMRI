package jmri.jmrix.dcc4pc;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Dcc4PcConnectionTypeListTest {

    @Test
    public void testCTor() {
        Dcc4PcConnectionTypeList t = new Dcc4PcConnectionTypeList();
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

    // private final static Logger log = LoggerFactory.getLogger(Dcc4PcConnectionTypeListTest.class);

}
