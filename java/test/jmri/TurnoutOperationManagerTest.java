package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TurnoutOperationManagerTest {

    @Test
    public void testCTor() {
        TurnoutOperationManager t = new TurnoutOperationManager();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testDefaultObjectCreation() {
        TurnoutOperationManager t = jmri.InstanceManager.getDefault(TurnoutOperationManager.class);
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

    // private final static Logger log = LoggerFactory.getLogger(TurnoutOperationManagerTest.class);

}
