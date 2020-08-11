package jmri.jmrix.direct;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class DirectConnectionTypeListTest {

    @Test
    public void testCTor() {
        DirectConnectionTypeList t = new DirectConnectionTypeList();
        Assert.assertNotNull("exists",t);
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
