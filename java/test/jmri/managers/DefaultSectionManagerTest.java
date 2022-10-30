package jmri.managers;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultSectionManagerTest {

    @Test
    public void testCTor() {
        SectionManager t = new jmri.managers.DefaultSectionManager();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testInstanceManagerAccess() {
        SectionManager t = InstanceManager.getDefault(SectionManager.class);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SectionManagerTest.class);

}
