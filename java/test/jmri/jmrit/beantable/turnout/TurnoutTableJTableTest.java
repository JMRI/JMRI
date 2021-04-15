package jmri.jmrit.beantable.turnout;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021
 */
public class TurnoutTableJTableTest {

    @Test
    public void testCTor() {
        TurnoutTableJTable t = new TurnoutTableJTable(null);
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

}
