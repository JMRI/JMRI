
package jmri.jmrit.ctc.topology;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/*
* Tests for the Topology Class
* @author  Gregory J. Bedlek   Copyright (C) 2020
*/
public class TopologyInfoTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("TopologyInfoTest Constructor Return", new TopologyInfo());
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
