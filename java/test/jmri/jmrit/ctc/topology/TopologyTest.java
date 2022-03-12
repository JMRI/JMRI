
package jmri.jmrit.ctc.topology;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/*
* Tests for the Topology Class
* @author  Gregory J. Bedlek   Copyright (C) 2020
*/
public class TopologyTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("TopologyTest Constructor Return", new Topology());
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
